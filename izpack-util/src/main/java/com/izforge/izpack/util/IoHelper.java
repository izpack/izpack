/*
 * Copyright 2016 Julien Ponge, René Krell and the IzPack team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.izforge.izpack.util;

import com.izforge.izpack.api.data.Variables;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

/**
 * I/O-related utility methods.
 */
public class IoHelper
{
    // This class uses the same values for family and flavor as
    // TargetFactory. But this class should not depends on TargetFactory,
    // because it is possible that TargetFactory is not bound. Therefore
    // the definition here again.

    /**
     * Placeholder during translatePath computing
     */
    private static final String MASKED_SLASH_PLACEHOLDER = "~&_&~";

    private static Properties envVars = null;

    /**
     * Creates a temp file with delete on exit rule. The extension is extracted
     * from the template if possible, else the default extension is used. The
     * contents of template will be copied into the temporary file.
     *
     * @param template file path to copy from and define file extension
     * @param defaultExtension file extension if no is contained in template
     * @return newly created and filled temporary file
     * @throws IOException if an I/O error occurred
     */
    public static File copyToTempFile(String template, String defaultExtension) throws IOException
    {
        return copyToTempFile(new File(template), defaultExtension);
    }

    /**
     * Creates a temp file with delete on exit rule. The extension is extracted
     * from the template if possible, else the default extension is used. The
     * contents of template will be copied into the temporary file.
     *
     * @param templateFile file to copy from and define file extension
     * @param defaultExtension file extension if no is contained in template
     * @return newly created and filled temporary file
     * @throws IOException if an I/O error occurred
     */
    public static File copyToTempFile(File templateFile, String defaultExtension) throws IOException
    {
        String path = templateFile.getCanonicalPath();
        int pos = path.lastIndexOf('.');
        String ext = path.substring(pos);
        if (ext.isEmpty())
        {
            ext = defaultExtension;
        }
        File tmpFile = File.createTempFile("izpack_io", ext);
        tmpFile.deleteOnExit();
        FileUtils.copyFile(templateFile, tmpFile);
        return tmpFile;
    }

    /**
     * Returns the free (disk) space for the given path. If it is not
     * ascertainable -1 returns.
     *
     * @param path path for which the free space should be detected
     * @return the free space for the given path
     */
    public static long getFreeSpace(String path)
    {
        long ret = -1;
        if (OsVersion.IS_WINDOWS)
        {
            String command = "cmd.exe";
            if (System.getProperty("os.name").toLowerCase().contains("windows 9"))
            {
                return (-1);
            }
            String[] params =
            {
                command, "/C", "\"dir /D /-C \"" + path + "\"\""
            };
            String[] output = new String[2];
            FileExecutor fe = new FileExecutor();
            fe.executeCommand(params, output);
            ret = extractLong(output[0], -3);
        }
        else if (OsVersion.IS_SUNOS)
        {
            String[] params =
            {
                "df", "-k", path
            };
            String[] output = new String[2];
            FileExecutor fe = new FileExecutor();
            fe.executeCommand(params, output);
            ret = extractLong(output[0], -3) * 1024;
        }
        else if (OsVersion.IS_HPUX)
        {
            String[] params =
            {
                "bdf", path
            };
            String[] output = new String[2];
            FileExecutor fe = new FileExecutor();
            fe.executeCommand(params, output);
            ret = extractLong(output[0], -3) * 1024;
        }
        else if (OsVersion.IS_UNIX)
        {
            String[] params =
            {
                "df", "-Pk", path
            };
            String[] output = new String[2];
            FileExecutor fe = new FileExecutor();
            fe.executeCommand(params, output);
            ret = extractLong(output[0], -3) * 1024;
        }
        return ret;
    }

    /**
     * Returns whether the given method will be supported with the given
     * environment. Some methods of this class are not supported on all
     * operation systems.
     *
     * @param method name of the method
     * @return true if the method will be supported with the current environment
     * else false
     * @throws RuntimeException if the given method name does not exist
     */
    public static boolean supported(String method)
    {
        if ("getFreeSpace".equals(method))
        {
            if (OsVersion.IS_UNIX)
            {
                return true;
            }
            if (OsVersion.IS_WINDOWS)
            { // getFreeSpace do not work on Windows 98.
                return !System.getProperty("os.name").toLowerCase().contains("windows 9");
            }
        }
        else if ("chmod".equals(method))
        {
            if (OsVersion.IS_UNIX)
            {
                return true;
            }
        }
        else if ("copyFile".equals(method))
        {
            return true;
        }
        else if ("getPrimaryGroup".equals(method))
        {
            if (OsVersion.IS_UNIX)
            {
                return true;
            }
        }
        else if ("getenv".equals(method))
        {
            return true;
        }
        else
        {
            throw new RuntimeException("method name " + method + "not supported by this method");
        }
        return false;

    }

    /**
     * Returns the first existing parent directory in a path
     *
     * @param path path which should be scanned
     * @return the first existing parent directory in a path
     */
    public static File existingParent(File path)
    {
        File result = path;
        while (!result.exists())
        {
            if (result.getParent() == null)
            {
                return result;
            }
            result = result.getParentFile();
        }
        return result;
    }

    /**
     * Extracts a long value from a string in a special manner. The string will
     * be broken into tokens with a standard StringTokenizer. Around the assumed
     * place (with the given half range) the tokens are scanned reverse for a
     * token which represents a long. if useNotIdentifier is not null, tokens
     * which are contains this string will be ignored. The first founded long
     * returns.
     *
     * @param in the string which should be parsed
     * @param assumedPlace token number which should contain the value
     * @return founded long
     */
    private static long extractLong(String in, int assumedPlace)
    {
        long ret = -1;
        StringTokenizer st = new StringTokenizer(in);
        int length = st.countTokens();
        int i;
        int currentRange = 0;
        String[] interestedEntries = new String[3 + 3];
        for (i = 0; i < length - 3 + assumedPlace; ++i)
        {
            st.nextToken(); // Forget this entries.
        }

        for (i = 0; i < 3 + 3; ++i)
        { // Put the interesting Strings into an intermediate array.
            if (st.hasMoreTokens())
            {
                interestedEntries[i] = st.nextToken();
                currentRange++;
            }
        }

        for (i = currentRange - 1; i >= 0; --i)
        {
            if (interestedEntries[i].contains("%"))
            {
                continue;
            }
            try
            {
                ret = Long.parseLong(interestedEntries[i]);
            }
            catch (NumberFormatException nfe)
            {
                continue;
            }
            break;
        }
        return ret;
    }

    /**
     * Returns a string resulting from replacing all occurrences of what in this
     * string with with. In opposite to the String.replaceAll method this method
     * do not use regular expression or other methods which are only available
     * in JRE 1.4 and later. This method was special made to mask masked slashes
     * to avert a conversion during path translation.
     *
     * @param destination string for which the replacing should be performed
     * @param what what string should be replaced
     * @param with with what string what should be replaced
     * @return a new String object if what was found in the given string, else
     * the given string self
     */
    private static String replaceString(String destination, String what, String with)
    {
        if (destination.contains(what))
        { // what found, with (placeholder) not included in destination ->
            // perform changing.
            StringBuilder buf = new StringBuilder();
            int last = 0;
            int current = destination.indexOf(what);
            int whatLength = what.length();
            while (current >= 0)
            { // Do not use Methods from JRE 1.4 and higher ...
                if (current > 0)
                {
                    buf.append(destination.substring(last, current));
                }
                buf.append(with);
                last = current + whatLength;
                current = destination.indexOf(what, last);
            }
            if (destination.length() > last)
            {
                buf.append(destination.substring(last));
            }
            return buf.toString();
        }
        return destination;
    }

    /**
     * Translates a relative path to a local system path.
     *
     * @param destination the path to translate
     * @param variables used to replaces variables in the path
     * @return the translated path
     */
    public static String translatePath(String destination, Variables variables)
    {
        destination = variables.replace(destination);
        return translatePath(destination);
    }

    /**
     * Translates a path.
     *
     * @param destination the path to translate
     * @return the translated path
     */
    public static String translatePath(String destination)
    {
        // Convert the file separator characters

        // destination = destination.replace('/', File.separatorChar);
        // Undo the conversion if the slashes was masked with
        // a backslash
        // Not all occurrences of slashes are path separators. To differ
        // between it we allow to mask a slash with a leading backslash.
        // Unfortunately we cannot use String.replaceAll because it
        // handles backslashes in the replacement string in a special way
        // and the method exist only beginning with JRE 1.4.
        // Therefore the little bit crude way following ...
        if (destination.contains("\\/") && !destination.contains(MASKED_SLASH_PLACEHOLDER))
        { // Masked slash found, placeholder not included in destination ->
            // perform masking.
            destination = replaceString(destination, "\\/", MASKED_SLASH_PLACEHOLDER);
            // Masked slashes changed to MASKED_SLASH_PLACEHOLDER.
            // Replace unmasked slashes.
            destination = destination.replace('/', File.separatorChar);
            // Replace the MASKED_SLASH_PLACEHOLDER to slashes; masking
            // backslashes will
            // be removed.
            destination = replaceString(destination, MASKED_SLASH_PLACEHOLDER, "/");
        }
        else
        {
            destination = destination.replace('/', File.separatorChar);
        }
        return destination;
    }

    /**
     * Returns the value of the environment variable given by key. This method
     * is a work around for VM versions which do not support getenv in an other
     * way. At the first call all environment variables will be loaded via an
     * exec. On Windows keys are not case sensitive.
     *
     * @param key variable name for which the value should be resolved
     * @return the value of the environment variable given by key
     */
    public static String getenv(String key)
    {
        if (envVars == null)
        {
            loadEnv();
        }
        if (envVars == null)
        {
            return (null);
        }
        if (OsVersion.IS_WINDOWS)
        {
            key = key.toUpperCase();
        }
        return (String) (envVars.get(key));
    }

    /**
     * Loads all environment variables via an exec.
     */
    private static void loadEnv()
    {
        String[] output = new String[2];
        String[] params;
        if (OsVersion.IS_WINDOWS)
        {
            String command = "cmd.exe";
            if (System.getProperty("os.name").toLowerCase().contains("windows 9"))
            {
                command = "command.com";
            }
            params = new String[]
            {
                command, "/C", "set"
            };
        }
        else
        {
            params = new String[]
            {
                "env"
            };
        }
        FileExecutor fe = new FileExecutor();
        fe.executeCommand(params, output);
        if (output[0].length() <= 0)
        {
            return;
        }
        String lineSep = System.getProperty("line.separator");
        StringTokenizer tokenizer = new StringTokenizer(output[0], lineSep);
        envVars = new Properties();
        String var = null;
        while (tokenizer.hasMoreTokens())
        {
            String line = tokenizer.nextToken();
            if (line.indexOf('=') == -1)
            { // May be a env var with a new line in it.
                if (var == null)
                {
                    var = lineSep + line;
                }
                else
                {
                    var += lineSep + line;
                }
            }
            else
            { // New var, perform the previous one.
                setEnvVar(var);
                var = line;
            }
        }
        setEnvVar(var);
    }

    /**
     * Extracts key and value from the given string var. The key should be
     * separated from the value by a sign. On Windows all chars of the key are
     * translated to upper case.
     *
     * @param var expression for setting the environment variable
     */
    private static void setEnvVar(String var)
    {
        if (var == null)
        {
            return;
        }
        int index = var.indexOf('=');
        if (index < 0)
        {
            return;
        }
        String key = var.substring(0, index);
        // On windows change all key chars to upper.
        if (OsVersion.IS_WINDOWS)
        {
            key = key.toUpperCase();
        }
        envVars.setProperty(key, var.substring(index + 1));

    }

    /**
     * Copies specified contents of one jar to another.
     * <p/>
     * <p/>
     * TODO it would be useful to be able to keep signature information from
     * signed jar files, can we combine manifests and still have their content
     * signed?
     */
    public static void copyZip(ZipInputStream zin, org.apache.tools.zip.ZipOutputStream out,
            List<String> files, Map<FilterOutputStream, Set<String>> alreadyWrittenFiles)
            throws IOException
    {
        ZipEntry zEntry;
        if (!alreadyWrittenFiles.containsKey(out))
        {
            alreadyWrittenFiles.put(out, new HashSet<String>());
        }
        Set<String> currentSet = alreadyWrittenFiles.get(out);
        while ((zEntry = zin.getNextEntry()) != null)
        {
            String currentName = zEntry.getName();
            String testName = currentName.replace('/', '.');
            testName = testName.replace('\\', '.');
            if (files != null)
            {
                boolean founded = false;
                for (String doInclude : files)
                {   // Make "includes" self to support regex.
                    if (testName.matches(doInclude))
                    {
                        founded = true;
                        break;
                    }
                }
                if (!founded)
                {
                    continue;
                }
            }
            if (currentSet.contains(currentName))
            {
                continue;
            }
            try
            {
                // Get input file date and time.
                long fileTime = zEntry.getTime();
                copyStreamToJar(zin, out, currentName, fileTime);
                zin.closeEntry();
                currentSet.add(currentName);
            }
            catch (ZipException x)
            {
                // This avoids any problem that can occur with duplicate
                // directories. for instance all META-INF data in jars
                // unfortunately this do not work with the apache ZipOutputStream...
            }
        }
    }

    public static void copyStreamToJar(InputStream zin, ZipOutputStream out, String currentName, long fileTime)
            throws IOException
    {
        // Create new entry for zip file.
        org.apache.tools.zip.ZipEntry newEntry = new org.apache.tools.zip.ZipEntry(currentName);
        // Make sure there is date and time set.
        if (fileTime != -1)
        {
            newEntry.setTime(fileTime); // If found set it into output file.
        }
        out.putNextEntry(newEntry);
        if (zin != null)
        {
            IOUtils.copy(zin, out);
        }
    }

    public static void copyStreamToJar(InputStream zin, java.util.zip.ZipOutputStream out, String currentName,
            long fileTime) throws IOException
    {
        // Create new entry for zip file.
        org.apache.tools.zip.ZipEntry newEntry = new org.apache.tools.zip.ZipEntry(currentName);
        // Make sure there is date and time set.
        if (fileTime != -1)
        {
            newEntry.setTime(fileTime); // If found set it into output file.
        }
        out.putNextEntry(newEntry);
        if (zin != null)
        {
            IOUtils.copy(zin, out);
        }
        out.closeEntry();
    }
}

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
import com.izforge.izpack.api.merge.MergeTarget;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    private IoHelper()
    {
    }

    /**
     * Creates a temp file with delete on exit rule. The extension is extracted from the template if
     * possible, else the default extension is used. The contents of template will be copied into
     * the temporary file.
     *
     * @param template         file path to copy from and define file extension
     * @param defaultExtension file extension if no is contained in template
     * @return newly created and filled temporary file
     * @throws IOException if an I/O error occurred
     */
    public static File copyToTempFile(String template, String defaultExtension) throws IOException
    {
        return copyToTempFile(new File(template), defaultExtension);
    }

    /**
     * Creates a temp file with delete on exit rule. The extension is extracted from the template if
     * possible, else the default extension is used. The contents of template will be copied into
     * the temporary file.
     *
     * @param templateFile         file to copy from and define file extension
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
     * Returns the free (disk) space for the given path. If it is not ascertainable -1 returns.
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
            String[] params = {command, "/C", "\"dir /D /-C \"" + path + "\"\""};
            String[] output = new String[2];
            FileExecutor fe = new FileExecutor();
            fe.executeCommand(params, output);
            ret = extractLong(output[0], -3);
        }
        else if (OsVersion.IS_SUNOS)
        {
            String[] params = {"df", "-k", path};
            String[] output = new String[2];
            FileExecutor fe = new FileExecutor();
            fe.executeCommand(params, output);
            ret = extractLong(output[0], -3) * 1024;
        }
        else if (OsVersion.IS_HPUX)
        {
            String[] params = {"bdf", path};
            String[] output = new String[2];
            FileExecutor fe = new FileExecutor();
            fe.executeCommand(params, output);
            ret = extractLong(output[0], -3) * 1024;
        }
        else if (OsVersion.IS_UNIX)
        {
            String[] params = {"df", "-Pk", path};
            String[] output = new String[2];
            FileExecutor fe = new FileExecutor();
            fe.executeCommand(params, output);
            ret = extractLong(output[0], -3) * 1024;
        }
        return ret;
    }

    /**
     * Returns whether the given method will be supported with the given environment. Some methods
     * of this class are not supported on all operation systems.
     *
     * @param method name of the method
     * @return true if the method will be supported with the current environment else false
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
     * Extracts a long value from a string in a special manner. The string will be broken into
     * tokens with a standard StringTokenizer. Around the assumed place (with the given half range)
     * the tokens are scanned reverse for a token which represents a long. if useNotIdentifier is not
     * null, tokens which are contains this string will be ignored. The first founded long returns.
     *
     * @param in               the string which should be parsed
     * @param assumedPlace     token number which should contain the value
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
     * Returns a string resulting from replacing all occurrences of what in this string with with.
     * In opposite to the String.replaceAll method this method do not use regular expression or
     * other methods which are only available in JRE 1.4 and later. This method was special made to
     * mask masked slashes to avert a conversion during path translation.
     *
     * @param destination string for which the replacing should be performed
     * @param what        what string should be replaced
     * @param with        with what string what should be replaced
     * @return a new String object if what was found in the given string, else the given string self
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
     * @param variables   used to replaces variables in the path
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

    public static void copyStreamToJar(MergeTarget.WriteAction writeAction, ZipOutputStream zipOutputStream, String currentName,
                                       long fileTime) throws IOException
    {
        // Create new entry for zip file.
        final ZipEntry newEntry = new ZipEntry(currentName);
        // Make sure there is date and time set.
        if (fileTime != -1)
        {
            newEntry.setTime(fileTime); // If found set it into output file.
        }
        zipOutputStream.putNextEntry(newEntry);
        writeAction.accept(zipOutputStream);
        zipOutputStream.closeEntry();
    }

    public static MergeTarget mergeTarget(ZipOutputStream zipOutputStream)
    {
        final Set<String> mergedFiles = new HashSet<>();
        return (name, timestamp, writeAction) ->
        {
            if (mergedFiles.add(name))
            {
                copyStreamToJar(writeAction, zipOutputStream, name, timestamp);
                return true;
            }
            return false;
        };
    }
}

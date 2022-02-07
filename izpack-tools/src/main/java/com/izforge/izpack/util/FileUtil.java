/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005 Marc Eppelmann
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Provides general global file utility methods
 *
 * @author marc.eppelmann
 */
public class FileUtil
{
    private static final Pattern WINDOWS_DRIVE_PATTERN = Pattern.compile("^/([a-zA-Z]:.+)$");

    public static File convertUrlToFile(URL url)
    {
        return new File(convertUrlToFilePath(url));
    }

    public static String convertUrlToFilePath(URL url)
    {
        try
        {
            URI uri;
            try
            {
                uri = url.toURI();
            }
            catch (URISyntaxException e)
            {
                String escaped = new URI("ignore", url.toString(), null).getRawSchemeSpecificPart();
                uri = new URI(escaped);
            }
            if ("jar".equals(uri.getScheme()))
            {
              uri = new URI(uri.getRawSchemeSpecificPart());
            }
            StringBuilder result = new StringBuilder();
            final String host = uri.getHost();
            if (host != null && !host.isEmpty())
            {
              result.append("//").append(host);
            }
            final String path = uri.getPath();
            result.append(handleWindowsUrl(path == null ? uri.getSchemeSpecificPart() : path));
            final String encodedQuery = url.getQuery();
            if (encodedQuery !=null && encodedQuery.isEmpty())
            {
              result.append('?').append(URLDecoder.decode(encodedQuery, "UTF-8"));
            }
            return result.toString();
        }
        catch (final URISyntaxException | UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    private static String handleWindowsUrl(String path)
    {
      Matcher matcher = WINDOWS_DRIVE_PATTERN.matcher(path);
      if (matcher.matches())
      {
        return matcher.group(1);
      }
      return path;
    }

    public static File getLockFile(String applicationName){
        String tempDir = System.getProperty("java.io.tmpdir");
        String fileName = "iz-" + applicationName + ".tmp";
        return new File(tempDir, fileName);
    }

    /**
     * Gets the content from a File as StringArray List.
     *
     * @param fileName A file to read from.
     * @return List of individual line of the specified file. List may be empty but not
     *         null.
     */
    public static List<String> getFileContent(String fileName)
            throws IOException
    {
        List<String> result = new ArrayList<String>();

        File aFile = new File(fileName);
        if (!aFile.isFile())
        {
            return result; // None
        }

        BufferedReader reader;
        try
        {
            reader = new BufferedReader(new FileReader(aFile));
        }
        catch (FileNotFoundException e)
        {
            return result;
        }

        String aLine;
        while ((aLine = reader.readLine()) != null)
        {
            result.add(aLine + "\n");
        }

        reader.close();

        return result;
    }

    /**
     * Gets file date and time.
     *
     * @param url The URL of the file for which date and time will be returned.
     * @return Returns long value which is the date and time of the file. If any error
     *         occurs returns -1 (=no file date and time available).
     */
    public static long getFileDateTime(URL url)
    {
        if (url == null)
        {
            return -1;
        }

        String fileName = url.getFile();
        if (fileName.charAt(0) == '/' || fileName.charAt(0) == '\\')
        {
            fileName = fileName.substring(1, fileName.length());
        }

        try
        {
            File file = new File(fileName);
            // File name must be a file or a directory.
            if (!file.isDirectory() && !file.isFile())
            {
                return -1;
            }

            return file.lastModified();
        }
        catch (java.lang.Exception e)
        {   // Trap all Exception based exceptions and return -1.
            return -1;
        }
    }

    public static String[] getFileNames(String dirPath, FilenameFilter fileNameFilter)
    {
        String fileNames[] = null;
        File dir = new File(dirPath);
        if (dir.isDirectory())
        {
            if (fileNameFilter != null)
            {
                fileNames = dir.list(fileNameFilter);
            }
            else
            {
                fileNames = dir.list();
            }
        }
        return fileNames;
    }

    /**
     * Gets an absolute file from a filename. In difference to File.isAbsolute()
     * this method bases relative file names on a given base directory.
     *
     * @param filename The filename to build an absolute file from
     * @param basedir  The base directory for a relative filename
     * @return The absolute file according to the described algorithm
     */
    public static File getAbsoluteFile(String filename, String basedir)
    {
        if (filename == null)
        {
            return null;
        }
        File file = new File(filename);
        if (file.isAbsolute())
        {
            return file;
        }
        else
        {
            return new File(basedir, file.getPath());
        }
    }

    /**
     * Gets a relative file from a filename against a base directory.
     * Enclosed '.' and '..' characters are resolved for all parameters.
     *
     * @param file The filename to build a relative file from
     * @param basedir  The base directory for a relative filename
     * @return The relative file name, or null, if one input parameter was null
     * @throws IOException if the file paths cannot be canonicalized
     */
    public static String getRelativeFileName(File file, File basedir) throws IOException
    {
        if (!basedir.isDirectory())
        {
            throw new IOException("Base path " + basedir + " is expected to be a directory");
        }
        String canonicalFilePath = file.getCanonicalPath();
        String canonicalBaseDirPath = basedir.getCanonicalPath();
        if (canonicalFilePath.startsWith(canonicalBaseDirPath))
        {
            int length = canonicalBaseDirPath.length();
            if (length < canonicalFilePath.length())
            {
                return canonicalFilePath.substring(length + 1);
            }
        }
        return null;
    }

}

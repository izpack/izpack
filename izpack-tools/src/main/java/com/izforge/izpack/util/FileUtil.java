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

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;


/**
 * Provides general global file utility methods
 *
 * @author marc.eppelmann
 */
public class FileUtil
{

    public static File convertUrlToFile(URL url)
    {
        return new File(convertUrlToFilePath(url));
    }

    public static String convertUrlToFilePath(URL url)
    {
        try
        {
            return URLDecoder.decode(url.getFile(), "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            // yet another stupid checked exception...
            throw new RuntimeException(e);
        }
    }

    public static File getLockFile(String applicationName)
    {
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
     * @throws IOException
     */
    public static List<String> getFileContent(String fileName)
            throws IOException
    {
        List<String> result = new ArrayList<String>();

        File aFile = new File(fileName);

        if (!aFile.isFile())
        {
            //throw new IOException( fileName + " is not a regular File" );
            return result; // None
        }

        BufferedReader reader = null;

        try
        {
            reader = new BufferedReader(new FileReader(aFile));
        }
        catch (FileNotFoundException e)
        {
            // TODO handle Exception
            e.printStackTrace();

            return result;
        }

        String aLine = null;

        while ((aLine = reader.readLine()) != null)
        {
            result.add(aLine + "\n");
        }

        reader.close();

        return result;
    }

    /**
     * Searches case sensitively, and returns true if the given SearchString occurs in the
     * first File with the given Filename.
     *
     * @param aFileName     A files name
     * @param aSearchString the string search for
     * @return true if found in the file otherwise false
     */
    public static boolean fileContains(String aFileName, String aSearchString)
    {
        return (fileContains(aFileName, aSearchString, false));
    }

    /**
     * Tests if the given File contains the given Search String
     *
     * @param aFileName             A files name
     * @param aSearchString         the String to search for
     * @param caseInSensitiveSearch If false the Search is casesensitive
     * @return true if found in the file otherwise false
     */
    public static boolean fileContains(String aFileName, String aSearchString,
                                       boolean caseInSensitiveSearch)
    {
        boolean result = false;

        String searchString = caseInSensitiveSearch
                ? aSearchString.toLowerCase() : aSearchString;

        List<String> fileContent = new ArrayList<String>();

        try
        {
            fileContent = getFileContent(aFileName);
        }
        catch (IOException e)
        {
            // TODO handle Exception
            e.printStackTrace();
        }

        for (String currentline : fileContent)
        {
            if (caseInSensitiveSearch)
            {
                currentline = currentline.toLowerCase();
            }

            if (currentline.contains(searchString))
            {
                result = true;

                break;
            }
        }

        return result;
    }

    /**
     * Gets file date and time.
     *
     * @param url The URL of the file for which date and time will be returned.
     * @return Returns long value which is the date and time of the file. If any error
     *         occures returns -1 (=no file date and time available).
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

    public static String[] getFileNames(String dirPath) throws Exception
    {
        return getFileNames(dirPath, null);
    }

    public static String[] getFileNames(String dirPath, FilenameFilter fileNameFilter) throws Exception
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
     * @param filename The filename to build a relative file from
     * @param basedir  The base directory for a relative filename
     * @return The relative file name, or null, if one input parameter was null
     * @throws IOException if the file paths cannot be canonicalized
     */
    public static String getRelativeFileName(File file, File basedir) throws IOException
    {
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

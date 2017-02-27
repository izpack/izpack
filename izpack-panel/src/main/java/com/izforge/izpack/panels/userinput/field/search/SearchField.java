/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
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
package com.izforge.izpack.panels.userinput.field.search;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.panels.userinput.field.Field;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Search field.
 *
 * @author Tim Anderson
 */
public class SearchField extends Field
{

    private static final Logger LOGGER = Logger.getLogger(SearchField.class.getName());

    private final InstallData installData;
    /**
     * The filename to search on. May be {@code null}
     */
    private final String filename;

    /**
     * The filename to check the existence of. May be {@code null}
     */
    private final String checkFilename;

    /**
     * The search type.
     */
    private final SearchType type;

    /**
     * The result type.
     */
    private final ResultType resultType;

    /**
     * The choices.
     */
    private final List<String> choices;

    /**
     * The selected choice.
     */
    private final int selected;

    /**
     * Constructs a {@code SearchField}.
     *
     * @param config the field configuration
     * @param installData the installation data
     * @throws IzPackException if the field cannot be read
     */
    public SearchField(SearchFieldConfig config, InstallData installData)
    {
        super(config, installData);
        this.installData = installData;
        filename = config.getFilename();
        checkFilename = config.getCheckFilename();
        type = config.getSearchType();
        resultType = config.getResultType();
        choices = config.getChoices();
        selected = config.getSelectedIndex();
    }

    /**
     * Returns the name of the file to search for.
     *
     * @return the name of the file to search for. May be {@code null} if
     * searching for directories
     */
    public String getFilename()
    {
        return filename;
    }

    /**
     * Returns the filename to check the existence of.
     * <p/>
     * This is used when searching for directories; the file name is appended to
     * a directory to determine if the correct directory has been located.
     *
     * @return the filename to to check the existence of. May be {@code null}
     */
    public String getCheckFilename()
    {
        return checkFilename;
    }

    /**
     * Returns the search type.
     *
     * @return the search type
     */
    public SearchType getType()
    {
        return type;
    }

    /**
     * Returns the result type.
     *
     * @return the result type
     */
    public ResultType getResultType()
    {
        return resultType;
    }

    /**
     * Returns the search choices. Checks whether a placeholder item is in the
     * combobox and resolve the paths automatically: /usr/lib/&#42; searches all
     * folders in usr/lib to find /usr/lib/&#42;/lib/tools.jar
     *
     * @return the search choices
     */
    public List<String> getChoices()
    {
        return getChoices(this.choices);
    }

    /**
     * Returns the search choices depending on a given list of pre-selected
     * choices. Checks whether a placeholder item is in the combobox and resolve
     * the paths automatically: /usr/lib/&#42; searches all folders in usr/lib
     * to find /usr/lib/&#42;/lib/tools.jar
     *
     * @return the search choices
     */
    public List<String> getChoices(List<String> choices)
    {
        List<String> items = new ArrayList<String>();

        for (String path : choices)
        {
            path = installData.getVariables().replace(path);

            if (path.endsWith("*"))
            {
                path = path.substring(0, path.length() - 1);
                File dir = new File(path);

                if (dir.isDirectory())
                {
                    File[] subdirs = dir.listFiles();
                    if (subdirs != null)
                    {
                        for (File subdir : subdirs)
                        {
                            String search = subdir.getAbsolutePath();
                            if (pathMatches(search))
                            {
                                items.add(search);
                            }
                        }
                    }
                }
            }
            else
            {
                if (pathMatches(path))
                {
                    items.add(path);
                }
            }
        }

        return items;
    }

    /**
     * Returns the index of the selected choice.
     *
     * @return the selected index or {@code -1} if no choice is selected
     */
    public int getSelectedIndex()
    {
        return selected;
    }


    /*
     * UTILITIES
     */
    /**
     * Resolve Windows environment variables
     *
     * @param path - eg: "%JAVA_HOME%\\bin"
     * @param env - System.getenv() (or forced config for testing)
     */
    public static String resolveEnvValue(String path, Map<String, String> env)
    {
        StringBuilder str = new StringBuilder();
        int start = 0, envStart;

        while ((envStart = path.indexOf('%', start)) >= 0)
        {
            int end = path.indexOf('%', envStart + 1);
            if (end < 0)
            {
                break;
            }
            String envKey = path.substring(envStart + 1, end);
            String envValue = env.get(envKey); //System.getenv( envKey );

            if (envStart > start)
            {
                str.append(path.substring(start, envStart));
            }
            str.append(envValue);
            start = end + 1;
        }
        if (start > 0)
        {
            str.append(path.substring(start));
            return str.toString();
        }

        return path;
    }

    /**
     * check whether the given path matches
     */
    public boolean pathMatches(String path)
    {
        if (path != null)
        {
            path = resolveEnvValue(path, System.getenv());

            File file;
            if (filename == null || type == SearchType.DIRECTORY)
            {
                file = new File(path);
            }
            else
            {
                file = new File(path, filename);
            }

            if (file.exists())
            {
                if (checkFilename == null)
                {
                    return true;        // no file to check for
                }

                if (file.isDirectory())
                {
                    file = new File(file, checkFilename);
                    if (!file.exists())
                    {
                        LOGGER.fine(file.getAbsolutePath() + " does not exist");
                        return false;
                    }
                }
                else
                {
                    // Check that the file's path and name ends with "checkFilename"
                    if (!file.getAbsolutePath().endsWith(checkFilename.replaceAll("\\\\/", File.separator)))
                    {
                        return false;
                    }
                }

                // "file" now points to "checkfilename", but is it the correct type?
                return file.isDirectory() == (type == SearchType.DIRECTORY);
            }
        }

        return false;
    }

    /**
     * Return the result of the search according to result type.
     * <p/>
     * Sometimes, the whole path of the file is wanted, sometimes only the
     * directory where the file is in, sometimes the parent directory.
     *
     * @return null on error
     */
    /*--------------------------------------------------------------------------*/
    public String getResult(String item)
    {
        if (item != null)
        {
            item = item.trim();
            String path = item;

            File file = new File(item);

            if (!file.isDirectory())
            {
                path = file.getParent();
            }

            // path now contains the final content of the combo box
            if (resultType == ResultType.DIRECTORY)
            {
                return path;
            }
            else if (resultType == ResultType.FILE)
            {
                if (filename != null)
                {
                    return path + File.separatorChar + filename;
                }
                else
                {
                    return item;
                }
            }
            else if (resultType == ResultType.PARENTDIR)
            {
                File dir = new File(path);
                return dir.getParent();
            }
        }

        return null;
    }
}

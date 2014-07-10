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

package com.izforge.izpack.panels.userinput.field.file;

import java.io.File;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.panels.userinput.field.Field;


/**
 * Common file field functionality.
 *
 * @author Tim Anderson
 */
public class AbstractFileField extends Field
{

    /**
     * The file extension. May be {@code null}.
     */
    private final String fileExtension;

    /**
     * The file extension description. May be {@code null}.
     */
    private final String fileExtensionDescription;

    /**
     * Determines if empty input values are allowed.
     */
    protected final boolean allowEmptyValue;


    /**
     * Determines if the file must exist
     */
    protected final boolean mustExist;

    /**
     * Constructs an {@code AbstractFileField}.
     *
     * @param config      the configuration to get field information from
     * @param installData the installation data
     * @throws IzPackException if the field cannot be read
     */
    public AbstractFileField(FileFieldConfig config, InstallData installData)
    {
        super(config, installData);
        fileExtension = config.getFileExtension();
        fileExtensionDescription = config.getFileExtensionDescription();
        allowEmptyValue = config.getAllowEmptyValue();
        mustExist = config.mustExist();
    }

    /**
     * Returns the file extension.
     *
     * @return the file extension. May be {@code null}
     */
    public String getFileExtension()
    {
        return fileExtension;
    }

    /**
     * Returns the file extension description.
     *
     * @return the file extension description. May be {@code null}
     */
    public String getFileExtensionDescription()
    {
        return fileExtensionDescription;
    }

    /**
     * Determines if empty input values are allowed.
     *
     * @return {@code true} if empty input values are allowed; otherwise {@code false}
     */
    public boolean getAllowEmptyValue()
    {
        return allowEmptyValue;
    }

    /**
     * Determines if the file must exist
     *
     * @return {@code true} if the file must exist, otherwise {@code false}
     */
    public boolean isMustExist()
    {
        return mustExist;
    }


    /**
     * Returns the absolute file for the specified path, expanding any unix home reference (~).
     *
     * @param path the path
     * @return the absolute file for the path
     */
    public File getAbsoluteFile(String path)
    {
        // Expand unix home reference
        if (path.startsWith("~"))
        {
            String home = System.getProperty("user.home");
            path = home + path.substring(1);
        }

        // Normalize the path
        return new File(path).getAbsoluteFile();
    }

}

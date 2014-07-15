/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2001 Johannes Lehtinen
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

package com.izforge.izpack.data;

import java.io.Serializable;
import java.util.List;

import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.substitutor.SubstitutionType;

/**
 * Encloses information about a parsable file. This class abstracts the way the information is
 * stored to package.
 *
 * @author Johannes Lehtinen <johannes.lehtinen@iki.fi>
 */
public class ParsableFile implements Serializable
{

    static final long serialVersionUID = -7761309341843715721L;

    /**
     * The file path.
     */
    private String path;

    /**
     * The file type (or null for default)
     */
    private final SubstitutionType type;

    /**
     * The file encoding (or null for default)
     */
    private final String encoding;

    /**
     * The list of OS constraints limiting file installation.
     */
    private final List<OsModel> osConstraints;

    /**
     * condition for this Parsable
     */
    private String condition;

    /**
     * Constructs and initializes a new instance.
     *
     * @param path          the file path
     * @param type          the file type (or null for default)
     * @param encoding      the file encoding (or null for default)
     * @param osConstraints the OS constraint (or null for any OS)
     */
    public ParsableFile(String path, SubstitutionType type, String encoding, List<OsModel> osConstraints)
    {
        this.path = path;
        this.type = type;
        this.encoding = encoding;
        this.osConstraints = osConstraints;
    }

    /**
     * Returns the file path.
     *
     * @return the file path
     */
    public String getPath()
    {
        return path;
    }

    /**
     * Sets the path.
     *
     * @param path the path
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    /**
     * Returns the substitution type.
     *
     * @return the substitution type, or {@code null} for the default
     */
    public SubstitutionType getType()
    {
        return type;
    }

    /**
     * Returns the file encoding.
     *
     * @return the file encoding, or {@code null} for the default
     */
    public String getEncoding()
    {
        return encoding;
    }

    /**
     * Returns the list of OS constraints limiting file installation.
     *
     * @return the OS constraints, or {@code null} to indicate all OSes
     */
    public List<OsModel> getOsConstraints()
    {
        return osConstraints;
    }

    /**
     * @return the condition
     */
    public String getCondition()
    {
        return this.condition;
    }


    /**
     * @param condition the condition to set
     */
    public void setCondition(String condition)
    {
        this.condition = condition;
    }

    public boolean hasCondition()
    {
        return this.condition != null;
    }

    @Override
    public String toString()
    {
        StringBuffer retval = new StringBuffer();
        retval.append("path = ").append(path);
        retval.append("\n");
        retval.append("type = ").append(type);
        retval.append("\n");
        retval.append("osList = ").append(osConstraints);
        retval.append("\n");
        if (osConstraints != null)
        {
            for (OsModel anOsList : osConstraints)
            {
                retval.append("\tos: ").append(anOsList);
                retval.append("\n");
            }
        }
        return retval.toString();
    }
}

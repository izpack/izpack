/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005 Chad McHenry
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
package com.izforge.izpack.ant;

import org.apache.tools.ant.Project;

import java.util.Properties;

/**
 * A subclass of Ant Property to validate values, but not add to the ant
 * project's properties.
 *
 * @author Chad McHenry
 */
public class Property extends org.apache.tools.ant.taskdefs.Property
{

    /**
     * Store the property[s] of this Property tag.
     */
    protected Properties props = new Properties();

    /**
     * Creates new IZPackTask
     */
    public Property()
    {
        super(false);
    }

    public Properties getProperties()
    {
        return props;
    }

    /**
     * Overridden to store properties locally, not in the Ant Project.
     *
     * @param name name of property
     * @param value value to set
     */
    protected void addProperty(String name, String value)
    {
        if (props.get(name) == null)
        {
            props.put(name, value);
        }
        else
        {
            log("Override ignored for " + name, Project.MSG_VERBOSE);
        }
    }
}

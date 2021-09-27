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

import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Reference;

/**
 * A subclass of Ant Property to validate values, but not add to the ant
 * project's properties.
 *
 * @author Chad McHenry
 */
public class Property extends Task
{
	private String name;
	private String value;
	private Reference ref;

    /**
     * Creates new property
     */
    public Property()
    {
    }
    
    protected void addProperty(Properties properties)
    {
        log("Adding property: " + getClass() + name + "=" + value, Project.MSG_VERBOSE);
        properties.setProperty(name, value);
    }

    public void setName(String name)
    {
    	this.name = name;
    }
    
    public void setValue(String value)
    {
    	this.value = value;
    }
    
    public void setRefid(Reference ref)
    {
    	this.ref = ref;
    }

    /**
     * set the property in the project to the value.
     * if the task was give a file, resource or env attribute
     * here is where it is loaded
     * @throws BuildException on error
     */
    @Override
    public void execute() throws BuildException
    {
        if (getProject() == null)
        {
            throw new IllegalStateException("project has not been set");
        }

        if (name != null)
        {
            if (value == null && ref == null)
            {
                throw new BuildException("You must specify value or "
                                         + "refid with the name attribute",
                                         getLocation());
            }
        }

        if ((name != null) && (ref != null))
        {
            value = ref.getReferencedObject(getProject()).toString();
        }
    }

    /**
     * get the value of this property
     * @return the current value or the empty string
     */
    @Override
    public String toString()
    {
        return value == null ? "" : value;
    }
}

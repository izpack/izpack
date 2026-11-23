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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.FileList;
import org.apache.tools.ant.types.Reference;

/**
 * A subclass of Ant Property to validate values, but not add to the ant
 * project's properties.
 *
 * @author Chad McHenry
 */
public class Property extends DataType
{
	private String name;
    private File file;
	private String value;

    /**
     * Creates new property
     */
    public Property()
    {
    }

    private Property getRef()
    {
        return getCheckedRef(Property.class);
    }

    protected void addProperties(BiConsumer<String, String> propertyConsumer)
    {
        if (isReference())
        {
            getRef().addProperties(propertyConsumer);
        }
        else
        {
            if (file != null)
            {
                if (name != null || value != null)
                {
                    throw new BuildException("You must not specify more than the 'file' property");
                }
                try (FileInputStream in = new FileInputStream(file))
                {
                    Properties properties = new Properties();
                    properties.load(in);
                    properties.forEach((k,v) -> propertyConsumer.accept((String)k, (String)v));
                } catch (IOException e) {
                    throw new BuildException("Unable to load properties file: " + file.getAbsolutePath(), e);
                }
            }
            else if (name == null)
            {
                throw new BuildException("You must specify the 'name' property");
            }
            else if (value == null)
            {
                throw new BuildException("You must specify the 'value' property");
            }
            else
            {
                propertyConsumer.accept(name, value);
            }
        }
    }

    /**
     * The name of the property to set.
     * @param name property name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Get the property name.
     * @return the property name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Filename of a property file to load.
     * @param file filename
     *
     * @ant.attribute group="noname"
     */
    public void setFile(File file)
    {
        this.file = file;
    }

    /**
     * Get the file attribute.
     * @return the file attribute
     */
    public File getFile()
    {
        return file;
    }

    /**
     * Set the value of the property as a String.
     * @param value value to assign
     *
     * @ant.attribute group="name"
     */
    public void setValue(String value)
    {
    	this.value = value;
    }

    /**
     * Get the property value.
     * @return the property value
     */
    public String getValue()
    {
        return value;
    }

    /**
     * Sets a reference to an Ant datatype
     * declared elsewhere.
     * Only yields reasonable results for references
     * PATH like structures or properties.
     * @param ref reference
     *
     * @ant.attribute group="name"
     */
    @Override
    public void setRefid(Reference ref)
    {
        if (name != null || value!=null || file != null)
        {
            throw tooManyAttributes();
        }
        super.setRefid(ref);
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

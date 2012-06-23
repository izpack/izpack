/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.compiler.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import org.apache.tools.ant.taskdefs.Execute;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.exception.CompilerException;
import com.izforge.izpack.api.substitutor.SubstitutionType;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.compiler.helper.AssertionHelper;
import com.izforge.izpack.compiler.listener.PackagerListener;


/**
 * Sets a property by name, or set of properties (from file or resource) in the project. This is
 * modeled after ant properties
 * <p/>
 * <p/>
 * Properties are immutable: once a property is set it cannot be changed. They are most definately
 * not variable.
 * <p/>
 * <p/>
 * There are five ways to set properties:
 * <ul>
 * <li>By supplying both the <i>name</i> and <i>value</i> attributes.</li>
 * <li>By setting the <i>file</i> attribute with the filename of the property file to load. This
 * property file has the format as defined by the file used in the class java.util.Properties.</li>
 * <li>By setting the <i>environment</i> attribute with a prefix to use. Properties will be
 * defined for every environment variable by prefixing the supplied name and a period to the name of
 * the variable.</li>
 * </ul>
 * <p/>
 * Combinations of the above are considered an error.
 * <p/>
 * <p/>
 * The value part of the properties being set, might contain references to other properties. These
 * references are resolved when the properties are set.
 * <p/>
 * <p/>
 * This also holds for properties loaded from a property file.
 * <p/>
 * <p/>
 * Properties are case sensitive.
 * <p/>
 * <p/>
 * When specifying the environment attribute, it's value is used as a prefix to use when retrieving
 * environment variables. This functionality is currently only implemented on select platforms.
 * <p/>
 * <p/>
 * Thus if you specify environment=&quot;myenv&quot; you will be able to access OS-specific
 * environment variables via property names &quot;myenv.PATH&quot; or &quot;myenv.TERM&quot;.
 * <p/>
 * <p/>
 * Note also that properties are case sensitive, even if the environment variables on your operating
 * system are not, e.g. it will be ${env.Path} not ${env.PATH} on Windows 2000.
 * <p/>
 * <p/>
 * Note that when specifying either the <code>prefix</code> or <code>environment</code>
 * attributes, if you supply a property name with a final &quot;.&quot; it will not be doubled. ie
 * environment=&quot;myenv.&quot; will still allow access of environment variables through
 * &quot;myenv.PATH&quot; and &quot;myenv.TERM&quot;.
 * <p/>
 */
public class PropertyManager
{

    private Properties properties;

    private CompilerData compilerData;
    private VariableSubstitutor variableSubstitutor;
    private PackagerListener packagerListener;
    private AssertionHelper assertionHelper;

    public PropertyManager(Properties properties, VariableSubstitutor variableSubstitutor, CompilerData compilerData, PackagerListener packagerListener, AssertionHelper assertionHelper)
    {
        this.assertionHelper = assertionHelper;
        this.properties = properties;
        this.variableSubstitutor = variableSubstitutor;
        this.compilerData = compilerData;
        this.packagerListener = packagerListener;
        this.setProperty("izpack.version", CompilerData.IZPACK_VERSION);
        this.setProperty("basedir", compilerData.getBasedir());
    }


    /**
     * Add a name value pair to the project property set. It is <i>not</i> replaced it is already
     * in the set of properties.
     *
     * @param name  the name of the property
     * @param value the value to set
     * @return true if the property was not already set
     */
    public boolean addProperty(String name, String value)
    {
        if (properties.containsKey(name))
        {
            return false;
        }
        addPropertySubstitute(name, value);
        return true;
    }

    /**
     * Add a name value pair to the project property set. Overwriting any existing value except system properties.
     *
     * @param name  the name of the property
     * @param value the value to set
     * @return an indicator if the name value pair was added.
     */
    public boolean setProperty(String name, String value)
    {
        if (System.getProperties().containsKey(name))
        {
            return false;
        }
        addPropertySubstitute(name, value);
        return true;
    }

    /**
     * Get the value of a property currerntly known to izpack.
     *
     * @param name the name of the property
     * @return the value of the property, or null
     */
    public String getProperty(String name)
    {
        return properties.getProperty(name);
    }

    /**
     * Set the property in the project to the value. If the task was give a file, resource or env
     * attribute here is where it is loaded.
     *
     * @param xmlProp
     */
    public void execute(IXMLElement xmlProp) throws CompilerException
    {
        File file = null;
        String name = xmlProp.getAttribute("name");
        String value = xmlProp.getAttribute("value");
        String environnement = xmlProp.getAttribute("environment");
        if (environnement != null && !environnement.endsWith("."))
        {
            environnement += ".";
        }

        String prefix = xmlProp.getAttribute("prefix");
        if (prefix != null && !prefix.endsWith("."))
        {
            prefix += ".";
        }

        String filename = xmlProp.getAttribute("file");

        if (filename != null)
        {
            file = new File(filename);
        }
        if (name != null)
        {
            if (value == null)
            {
                assertionHelper.parseError(xmlProp, "You must specify a value with the name attribute");
            }
        }
        else
        {
            if (file == null && environnement == null)
            {
                assertionHelper.parseError(xmlProp,
                        "You must specify file, or environment when not using the name attribute");
            }
        }

        if (file == null && prefix != null)
        {
            assertionHelper.parseError(xmlProp, "Prefix is only valid when loading from a file ");
        }

        if ((name != null) && (value != null))
        {
            addProperty(name, value);
        }
        else if (file != null)
        {
            loadFile(file, xmlProp, prefix);
        }
        else if (environnement != null)
        {
            loadEnvironment(environnement, xmlProp, file);
        }
    }

    /**
     * load properties from a file
     *
     * @param file    file to load
     * @param xmlProp
     * @param prefix
     */
    private void loadFile(File file, IXMLElement xmlProp, String prefix) throws CompilerException
    {
        Properties props = new Properties();
        packagerListener.packagerMsg("Loading " + file.getAbsolutePath(),
                PackagerListener.MSG_VERBOSE);
        try
        {
            if (file.exists())
            {
                FileInputStream fis = new FileInputStream(file);
                try
                {
                    props.load(fis);
                }
                finally
                {
                    fis.close();
                }
                addProperties(props, xmlProp, file, prefix);
            }
            else
            {
                packagerListener.packagerMsg(
                        "Unable to find property file: " + file.getAbsolutePath(),
                        PackagerListener.MSG_VERBOSE);
            }
        }
        catch (IOException ex)
        {
            assertionHelper.parseError(xmlProp, "Faild to load file: " + file.getAbsolutePath(), ex);
        }
    }

    /**
     * load the environment values
     *
     * @param prefix  prefix to place before them
     * @param xmlProp
     * @param file
     */
    protected void loadEnvironment(String prefix, IXMLElement xmlProp, File file) throws CompilerException
    {
        Properties props = new Properties();
        packagerListener.packagerMsg("Loading Environment " + prefix,
                PackagerListener.MSG_VERBOSE);
        Vector osEnv = Execute.getProcEnvironment();
        for (Enumeration e = osEnv.elements(); e.hasMoreElements();)
        {
            String entry = (String) e.nextElement();
            int pos = entry.indexOf('=');
            if (pos == -1)
            {
                packagerListener.packagerMsg("Ignoring " + prefix,
                        PackagerListener.MSG_WARN);
            }
            else
            {
                props.put(prefix + entry.substring(0, pos), entry.substring(pos + 1));
            }
        }
        addProperties(props, xmlProp, file, prefix);
    }

    /**
     * iterate through a set of properties, resolve them then assign them
     *
     * @param props
     * @param xmlProp
     * @param file
     * @param prefix
     */
    public void addProperties(Properties props, IXMLElement xmlProp, File file, String prefix) throws CompilerException
    {
        resolveAllProperties(props, xmlProp, file);
        Enumeration e = props.keys();
        while (e.hasMoreElements())
        {
            String name = (String) e.nextElement();
            String value = props.getProperty(name);

            if (prefix != null)
            {
                name = prefix + name;
            }
            addPropertySubstitute(name, value);
        }
    }

    /**
     * Add a name value pair to the project property set
     *
     * @param name  name of property
     * @param value value to set
     */
    private void addPropertySubstitute(String name, String value)
    {
        try
        {
            value = variableSubstitutor.substitute(value, SubstitutionType.TYPE_AT);
        }
        catch (Exception e)
        {
            // ignore
        }
        properties.put(name, value);
    }

    /**
     * resolve properties inside a properties object
     *
     * @param props   properties to resolve
     * @param xmlProp
     * @param file
     */
    private void resolveAllProperties(Properties props, IXMLElement xmlProp, File file) throws CompilerException
    {
        variableSubstitutor.setBracesRequired(true);

        for (Enumeration e = props.keys(); e.hasMoreElements();)
        {
            String name = (String) e.nextElement();
            String value = props.getProperty(name);

            int mods = -1;
            do
            {
                StringReader read = new StringReader(value);
                StringWriter write = new StringWriter();

                try
                {
                    try
                    {
                        mods = variableSubstitutor.substitute(read, write, SubstitutionType.TYPE_AT);
                    }
                    catch (Exception e1)
                    {
                        throw new IOException(e1.getMessage());
                    }
                    // TODO: check for circular references. We need to know
                    // which
                    // variables were substituted to do that
                    props.put(name, value);
                }
                catch (IOException ex)
                {
                    assertionHelper.parseError(xmlProp, "Faild to load file: " + file.getAbsolutePath(),
                            ex);
                }
            }
            while (mods != 0);
        }
    }
}

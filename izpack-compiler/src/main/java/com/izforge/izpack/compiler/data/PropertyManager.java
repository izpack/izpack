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

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.CompilerException;
import com.izforge.izpack.api.substitutor.SubstitutionType;
import com.izforge.izpack.compiler.helper.AssertionHelper;
import com.izforge.izpack.compiler.listener.PackagerListener;
import com.izforge.izpack.core.substitutor.VariableSubstitutorReader;
import jakarta.inject.Inject;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;


/**
 * Sets a property by name, or set of properties (from file or resource) in the project. This is
 * modeled after ant properties
 * <p/>
 * <p/>
 * Properties are immutable: once a property is set it cannot be changed. They are most definitely
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

    private final Properties properties;
    private final Variables variables;
    private final PackagerListener packagerListener;
    private final String basedir;

    @Inject
    public PropertyManager(Properties properties, Variables variables, CompilerData compilerData, PackagerListener packagerListener)
    {
        this.properties = properties;
        this.variables = variables;
        this.packagerListener = packagerListener;
        this.basedir = compilerData.getBasedir();
        this.setProperty("izpack.version", CompilerData.IZPACK_VERSION);
        this.setProperty("basedir", basedir);
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
     * Get the value of a property.
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
     * @param propertyNode the properties XML definition
     */
    public void execute(IXMLElement propertyNode) throws CompilerException
    {
        String name = propertyNode.getAttribute("name");
        String value = propertyNode.getAttribute("value");
        String environment = propertyNode.getAttribute("environment");
        if (environment != null && !environment.endsWith("."))
        {
            environment += ".";
        }

        String prefix = propertyNode.getAttribute("prefix");
        if (prefix != null && !prefix.endsWith("."))
        {
            prefix += ".";
        }

        String fileName = propertyNode.getAttribute("file");
        AssertionHelper assertionHelper = new AssertionHelper(fileName);

        if (name != null)
        {
            if (value == null)
            {
                assertionHelper.parseError(propertyNode, "You must specify a value with the name attribute");
            }
        }
        else
        {
            if (fileName == null && environment == null)
            {
                assertionHelper.parseError(propertyNode,
                        "You must specify file, or environment when not using the name attribute");
            }
        }

        if (fileName == null && prefix != null)
        {
            assertionHelper.parseError(propertyNode, "Prefix is only valid when loading from a file ");
        }

        if ((name != null) && (value != null))
        {
            addProperty(name, value);
        }
        else if (environment != null)
        {
            try
            {
                loadEnvironment(environment);
            }
            catch (IOException e)
            {
                assertionHelper.parseError(propertyNode, "Failed loading properties from environment variables", e);
            }
        }
        else if (fileName != null)
        {
            final String expandedFileName = variables.replace(fileName);
            try
            {
                loadFile(expandedFileName, prefix);
            }
            catch (IOException e)
            {
                packagerListener.packagerMsg("Unable to load property file: " + expandedFileName,
                        PackagerListener.MSG_VERBOSE);

                assertionHelper.parseError(propertyNode, "Failed loading properties from file " + expandedFileName, e);
            }
        }
    }

    /**
     * load properties from a file
     *
     * @param fileName name of the file to load
     * @param prefix prefix to to be automatically added to the property name, can be null
     */
    private void loadFile(final String fileName, final String prefix) throws IOException
    {
        File file = new File(fileName);
        if (!file.isAbsolute())
        {
            file = new File(basedir, fileName);
        }

        packagerListener.packagerMsg("Loading " + file,
                PackagerListener.MSG_VERBOSE);

        final Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file))
        {
            props.load(fis);
        }

        addProperties(props, prefix);
    }

    /**
     * load the environment values
     *
     * @param prefix prefix to to be automatically added to the property name, can be null
     */
    private void loadEnvironment(String prefix) throws IOException
    {
        packagerListener.packagerMsg("Loading Environment " + prefix,
                PackagerListener.MSG_VERBOSE);

        Properties props = new Properties();
        Map<String, String> envVarMap = System.getenv();
        for (String key : envVarMap.keySet()) {
            props.put(prefix + key, envVarMap.get(key));
        }

        addProperties(props, prefix);
    }

    /**
     * iterate through a set of properties, resolve them then assign them
     *
     * @param props properties to resolve
     * @param prefix prefix to to be automatically added to the property name, can be null
     */
    private void addProperties(Properties props, String prefix) throws IOException
    {
        resolveAllProperties(props);
        for (String name : props.stringPropertyNames())
        {
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
     * @param name name of property
     * @param value value to set
     */
    private void addPropertySubstitute(String name, String value)
    {
        //noinspection EmptyCatchBlock
        try
        {
            properties.put(name, IOUtils.toString(new VariableSubstitutorReader(new StringReader(value), variables, SubstitutionType.TYPE_AT)));
        }
        catch (IOException e) {}
    }

    /**
     * Resolve references to IzPack variables in all values of a properties object
     *
     * @param props properties to resolve
     */
    private void resolveAllProperties(Properties props) throws IOException
    {
        for (String name : props.stringPropertyNames())
        {
            String value = props.getProperty(name);
            props.put(name, IOUtils.toString(new VariableSubstitutorReader(new StringReader(value), variables, SubstitutionType.TYPE_AT, true)));
        }
    }
}

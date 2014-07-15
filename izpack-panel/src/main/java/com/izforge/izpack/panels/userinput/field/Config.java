/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.panels.userinput.field;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.IXMLParser;
import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.util.file.FileUtils;


/**
 * User input configuration.
 *
 * @author Tim Anderson
 */
public class Config
{

    /**
     * The resource path.
     */
    private final String path;

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * The object factory for creating validators.
     */
    private final ObjectFactory factory;

    /**
     * The root element.
     */
    private final IXMLElement root;

    /**
     * Localisation messages.
     */
    private final Messages messages;

    /**
     * Descriptive text attribute name.
     */
    private static final String TEXT = "txt";

    /**
     * Localisation key attribute name.
     */
    private static final String KEY = "id";

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(Config.class.getName());


    /**
     * Constructs a {@code Config}.
     *
     * @param path        the resource path
     * @param resources   the resources
     * @param installData the installation data
     * @param factory     the factory
     * @param messages    the messages
     */
    public Config(String path, Resources resources, InstallData installData, ObjectFactory factory,
                  Messages messages)
    {
        IXMLParser parser = new XMLParser();

        URL url = resources.getURL(path);
        this.path = url.getPath();
        this.installData = installData;
        this.factory = factory;
        this.messages = messages;

        InputStream input = null;
        try
        {
            input = url.openStream();
            root = parser.parse(input);
        }
        catch (IOException exception)
        {
            throw new IzPackException("Failed to open: " + path);
        }
        finally
        {
            FileUtils.close(input);
        }
    }

    /**
     * Returns the configuration file path.
     *
     * @return the file path
     */
    public String getPath()
    {
        return path;
    }

    /**
     * Returns the root element.
     *
     * @return the root element
     */
    public IXMLElement getRoot()
    {
        return root;
    }

    /**
     * Returns the named element.
     *
     * @param parent the parent element
     * @param name   the element name
     * @return the corresponding element
     * @throws IzPackException if the element is not found
     */
    public IXMLElement getElement(IXMLElement parent, String name)
    {
        IXMLElement child = parent.getFirstChildNamed(name);
        if (child == null)
        {
            String message = "<" + parent.getName() + "> requires child <" + name + ">";
            throw new IzPackException(getContext(parent) + ": " + message);
        }
        return child;
    }

    /**
     * Returns the named attribute value.
     *
     * @param element the element
     * @param name    the attribute name
     * @return the attribute value
     * @throws IzPackException if the attribute is not found
     */
    public String getAttribute(IXMLElement element, String name)
    {
        return getAttribute(element, name, false);
    }

    /**
     * Returns the named attribute value.
     *
     * @param element   the element
     * @param name      the attribute name
     * @param optional  flag to allow null to be returned if attribute is optional
     * @return the attribute value, or null if attribute does not exist and attribute is optional
     * @throws IzPackException if the attribute is not found and attribute is required
     */
    public String getAttribute(IXMLElement element, String name, boolean optional)
    {
        String value = element.getAttribute(name);
        if (!optional && value == null)
        {
            String message = "<" + element.getName() + "> requires attribute '" + name + "'";
            throw new IzPackException(getContext(element) + ": " + message);
        }
        return value;
    }

    /**
     * Returns the named attribute value, replacing any variables present.
     *
     * @param element      the element
     * @param name         the attribute name
     * @param defaultValue the default value if the attribute isn't set
     * @return the corresponding value
     */
    public String getString(IXMLElement element, String name, String defaultValue)
    {
        String value = element.getAttribute(name);
        if (value != null && !value.equals(""))
        {
            value = installData.getVariables().replace(value);
        }
        if (value == null)
        {
            value = defaultValue;
        }
        return value;
    }

    /**
     * Returns the named attribute value without replacing any variables present.
     *
     * @param element      the element
     * @param name         the attribute name
     * @param defaultValue the default value if the attribute isn't set
     * @return the corresponding value
     */
    public String getRawString(IXMLElement element, String name, String defaultValue)
    {
        String value = element.getAttribute(name);
        if (value == null)
        {
            value = defaultValue;
        }
        return value;
    }

    /**
     * Returns a localised version of a string.
     *
     * @param value the string value
     * @return the localised version of {@code value}, or {@code value} if there is no localised version
     */
    public String getLocalString(String value)
    {
        return messages.get(value);
    }

    /**
     * Returns the named attribute as an integer.
     * <p/>
     * Any variables are replaced.
     *
     * @param element      the element name
     * @param name         the attribute name
     * @param defaultValue the value to return if the attribute isn't set or is invalid
     * @return the attribute value or {@code defaultValue} if it isn't set or is invalid
     */
    public int getInt(IXMLElement element, String name, int defaultValue)
    {
        int result = defaultValue;
        String value = getString(element, name, null);
        if (value != null)
        {
            try
            {
                result = Integer.parseInt(value);
            }
            catch (NumberFormatException exception)
            {
                logger.warning("Invalid value for attribute '" + name + "':" + value + " in " + getContext(element));
            }
        }
        return result;
    }

    /**
     * Returns the named attribute as an integer.
     *
     * @param element      the element name
     * @param name         the attribute name
     * @param defaultValue the value to return if the attribute isn't set or is invalid
     * @return the attribute value or {@code defaultValue} if it isn't set or is invalid
     */
    public boolean getBoolean(IXMLElement element, String name, boolean defaultValue)
    {
        boolean result = defaultValue;
        String value = getString(element, name, null);
        if (value != null && !value.equals(""))
        {
            // support "yes" for backwards compatibility, but don't encourage its use...
            result = value.equals("yes") || Boolean.valueOf(value);
        }
        return result;
    }

    /**
     * Extracts the text from an element. The text can be defined:
     * <ol>
     * <li>in the locale's messages, under the key defined by the {@code id} attribute; or
     * <li>as value of the attribute {@code txt}.
     * </ol>
     *
     * @param element the element from which to extract the text
     * @return the text, or {@code null} if none can be found
     */
    public String getText(IXMLElement element)
    {
        String result = null;
        if (element != null)
        {
            String key = element.getAttribute(KEY);

            if (key != null)
            {
                result = messages.get(key);
                if (key.equals(result))
                {
                    result = null;
                }
            }

            if (result == null)
            {
                // no localised message found, so use the txt attribute
                result = element.getAttribute(TEXT);
            }

            // replace any variables
            result = installData.getVariables().replace(result);
        }
        return result;
    }

    /**
     * Returns the named alignment value, replacing any variables present.
     *
     * @param element      the element
     * @param name         the attribute name
     * @param defaultValue the default value if the attribute isn't set or is invalid
     * @return the attribute value or {@code defaultValue} if it isn't set or is invalid
     */
    public Alignment getAlignment(IXMLElement element, String name, Alignment defaultValue)
    {
        Alignment result = defaultValue;
        String value = getString(element, name, (defaultValue != null) ? defaultValue.toString() : null);
        if (value != null)
        {
            try
            {
                result = Alignment.valueOf(value.toUpperCase());
            }
            catch (IllegalArgumentException exception)
            {
                logger.log(Level.INFO, "Invalid value for 'align': " + value + " in " + getContext(element));
            }
        }
        return result;
    }

    /**
     * Returns the context of an element, for error reporting purposes.
     *
     * @param element the element
     * @return the element context, in the form "<path>:<line number>"
     */
    public String getContext(IXMLElement element)
    {
        return path + ":" + element.getLineNr();
    }

    /**
     * Returns a factory for creating configuration items.
     *
     * @return the factory
     */
    public ObjectFactory getFactory()
    {
        return factory;
    }

}

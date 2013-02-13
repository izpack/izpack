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

package com.izforge.izpack.api.data;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.IXMLParser;
import com.izforge.izpack.api.adaptator.XMLException;
import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.exception.ResourceException;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Messages;

/**
 * Represents a database of a locale.
 *
 * @author Julien Ponge
 * @author J. Chris Folsom <jchrisfolsom@gmail.com>
 */
public class LocaleDatabase extends TreeMap<String, String> implements Messages
{

    /**
     * The directory where language packs are kept inside the installer jar file.
     */
    @Deprecated
    public static final String LOCALE_DATABASE_DIRECTORY = "/langpacks/";

    /**
     * The suffix for language pack definitions (.xml).
     */
    @Deprecated
    public static final String LOCALE_DATABASE_DEF_SUFFIX = ".xml";

    /*
     * static character for replacing quotes
     */
    private static final char TEMP_QUOTING_CHARACTER = '\uffff';

    /**
     * The parent messages. May be {@code null}.
     */
    private final Messages parent;

    /**
     * The locales.
     */
    private final Locales locales;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(LocaleDatabase.class.getName());

    /**
     * Constructs a {@code LocaleDatabase}.
     *
     * @param in      the stream to read the translation from
     * @param locales the supported locales
     * @throws ResourceException if the stream is not an IzPack langpack file
     */
    public LocaleDatabase(InputStream in, Locales locales)
    {
        this(in, null, locales);
    }

    /**
     * Constructs a {@code LocaleDatabase}.
     *
     * @param parent  the parent messages. May be {@code null}
     * @param locales the supported locales
     */
    public LocaleDatabase(Messages parent, Locales locales)
    {
        this(null, parent, locales);
    }

    /**
     * Constructs a {@code LocaleDatabase}.
     *
     * @param in      the stream to read the translation from. May be {@code null}
     * @param parent  the parent messages. May be {@code null}
     * @param locales the supported locales
     * @throws ResourceException if the stream is not an IzPack langpack file
     */
    public LocaleDatabase(InputStream in, Messages parent, Locales locales)
    {
        this.parent = parent;
        this.locales = locales;
        if (in != null)
        {
            add(in);
        }
    }

    /**
     * Adds the contents of the given stream to the data base. The stream have to contain key value
     * pairs as declared by the DTD langpack.dtd.
     *
     * @param in an InputStream to read the translation from.
     * @throws ResourceException if the stream is not an IzPack langpack file or cannot be read
     */
    public void add(InputStream in)
    {
        IXMLElement data;

        try
        {
            IXMLParser parser = new XMLParser();
            data = parser.parse(in);
        }
        catch (XMLException exception)
        {
            throw new ResourceException("Failed to read langpack stream", exception);
        }

        // We check the data
        if (!"langpack".equalsIgnoreCase(data.getElement().getLocalName()))
        {
            throw new ResourceException("Invalid IzPack XML langpack file");
        }

        // We fill the Hashtable
        for (IXMLElement child : data.getChildren())
        {
            String text = child.getContent();
            if (text != null && !"".equals(text))
            {
                put(child.getAttribute("id"), text.trim());
            }
            else
            {
                put(child.getAttribute("id"), child.getAttribute("txt"));
            }
        }
    }

    /**
     * Returns the message with the specified identifier.
     *
     * @param id the message identifier
     * @return the corresponding message, or {@code id} if the message does not exist
     */
    @Override
    public String get(Object id)
    {
        String result = super.get(id);
        return result != null ? result : id.toString();
    }

    /**
     * Formats the message with the specified identifier, replacing placeholders with the supplied arguments.
     * <p/>
     * This uses {@link java.text.MessageFormat} to format the message.
     *
     * @param id   the message identifier
     * @param args message arguments to replace placeholders in the message with
     * @return the corresponding message, or {@code id} if the message does not exist
     */
    @Override
    public String get(String id, Object... args)
    {
        String result;
        String pattern = (id != null) ? super.get(id) : null;
        if (pattern != null)
        {
            if (args.length > 0)
            {
                try
                {
                    // replace all ' characters because MessageFormat.format() doesn't substitute quoted place
                    // holders '{0}'
                    // TODO - fix quotes in langpacks to MessageFormat format
                    pattern = pattern.replace('\'', TEMP_QUOTING_CHARACTER);

                    pattern = MessageFormat.format(pattern, args);
                    result = MessageFormat.format(pattern, args);

                    // replace all ' characters back
                    result = result.replace(TEMP_QUOTING_CHARACTER, '\'');
                }
                catch (IllegalArgumentException exception)
                {
                    result = id;
                    logger.log(Level.WARNING, "Failed to format pattern=" + pattern + ", for key=" + id, exception);
                }
            }
            else
            {
                result = pattern;
            }
        }
        else if (parent != null)
        {
            result = parent.get(id, args);
        }
        else
        {
            result = id;
        }
        return result;
    }

    /**
     * Adds messages.
     * <p/>
     * This merges the supplied messages with the current messages. If an existing message exists with the same
     * identifier as that supplied, it will be replaced.
     *
     * @param messages the messages to add
     */
    @Override
    public void add(Messages messages)
    {
        putAll(messages.getMessages());
    }

    /**
     * Returns the messages.
     *
     * @return the message identifiers, and their corresponding formats
     */
    @Override
    public Map<String, String> getMessages()
    {
        return Collections.unmodifiableMap(this);
    }

    /**
     * Creates a new messages instance from the named resource that inherits the current messages.
     *
     * @param name the messages resource name
     * @return the messages
     */
    @Override
    public Messages newMessages(String name)
    {
        Messages child = locales.getMessages(name);
        Messages result = new LocaleDatabase(this, locales);
        result.add(child);
        return result;
    }

    /**
     * Convenience method to retrieve an element.
     *
     * @param key The key of the element to retrieve.
     * @return The element value or the key if not found.
     * @deprecated use {@link #get(String, Object...)}
     */
    @Deprecated
    public String getString(String key)
    {
        String val = get(key);
        // At a change of the return value at val == null the method
        // com.izforge.izpack.installer.IzPanel.getI18nStringForClass
        // should be also addapted.
        if (val == null)
        {
            val = key;
        }
        return val;
    }

    /**
     * Convenience method to retrieve an element and simultaneously insert variables into the
     * string. A place holder has to be build with the substring {n} where n is the parameter
     * argument beginning with 0. The first argument is therefore {0}. If a parameter starts with a
     * dollar sign the value will be used as key into the LocalDatabase. The key can be written as
     * $MYKEY or ${MYKEY}. For all place holders an argument should be exist and vis a versa.
     *
     * @param key       The key of the element to retrieve.
     * @param variables the variables to insert
     * @return The element value with the variables inserted or the key if not found.
     */
    public String getString(String key, String[] variables)
    {
        for (int i = 0; i < variables.length; ++i)
        {
            if (variables[i] == null)
            {
                // The argument array with index is NULL! Replace it with N/A
                variables[i] = "N/A";
            }
            else if (variables[i].startsWith("$"))
            { // Argument is also a key into the LocaleDatabase.
                String curArg = variables[i];
                if (curArg.startsWith("${"))
                {
                    curArg = curArg.substring(2, curArg.length() - 1);
                }
                else
                {
                    curArg = curArg.substring(1);
                }
                variables[i] = get(curArg);
            }
        }

        String message = get(key);

        // replace all ' characters because MessageFormat.format()
        // don't substitute quoted place holders '{0}'
        message = message.replace('\'', TEMP_QUOTING_CHARACTER);

        message = MessageFormat.format(message, (Object[]) variables);

        // replace all ' characters back
        return message.replace(TEMP_QUOTING_CHARACTER, '\'');
    }

}

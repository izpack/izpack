/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.IXMLParser;
import com.izforge.izpack.adaptator.impl.XMLParser;
import com.izforge.izpack.installer.ResourceManager;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

/**
 * Represents a database of a locale.
 *
 * @author Julien Ponge
 * @author J. Chris Folsom <jchrisfolsom@gmail.com>
 */
public class LocaleDatabase extends TreeMap
{

    /*
     * Static cache of locale databases mapped by their iso name.
     */
    private static Map<String, LocaleDatabase> cachedLocales = new HashMap<String, LocaleDatabase>();

    /**
     * The directory where language packs are kept inside the installer jar file.
     */
    public static final String LOCALE_DATABASE_DIRECTORY = "/langpacks/";

    /**
     * The suffix for language pack definitions (.xml).
     */
    public static final String LOCALE_DATABASE_DEF_SUFFIX = ".xml";

    /*
     * static character for replacing quotes
     */
    private static final char TEMP_QUOTING_CHARACTER = '\uffff';

    /**
     * Load a locale database. If the database has already been loaded it will not be reloaded.
     *
     * @param isoCode The io code of the locale database.
     *
     * @return The locale database or null if it cannot be found.
     *
     * @throws Exception
     */
    public static synchronized LocaleDatabase getLocaleDatabase(String isoCode) throws Exception
    {
        return getLocaleDatabase(isoCode, false);
    }

    /**
     * Load a LocaleDatabase.
     *
     * @param isoCode The ISO language prefix for the locale.
     * @param reload  Whether or not to reload the locale database if it has already been loaded.
     *
     * @return The locale database or null if it cannot be found. <p/> FIXME Maybe we should define
     *         some custom exception like LocaleLoadException or something similar so that this class can
     *         have a method signature that does not throw Exception
     */
    public static synchronized LocaleDatabase getLocaleDatabase(String isoCode, boolean reload)
            throws Exception
    {
        LocaleDatabase langpack = cachedLocales.get(isoCode);

        if (reload || langpack == null)
        {
            StringBuffer localeDefPath = new StringBuffer();

            localeDefPath.append(LOCALE_DATABASE_DIRECTORY);
            localeDefPath.append(isoCode);
            localeDefPath.append(LOCALE_DATABASE_DEF_SUFFIX);

            String path = localeDefPath.toString();

            // The resource exists
            if (LocaleDatabase.class.getResource(path) != null)
            {
                langpack = new LocaleDatabase(LocaleDatabase.class.getResourceAsStream(path));

                cachedLocales.put(isoCode, langpack);
            }
        }

        return langpack;
    }

    /**
     * Load the current default LocaleDatabase.
     *
     * @throws Exception FIXME
     */
    public static synchronized LocaleDatabase getLocaleDatabase() throws Exception
    {
        ResourceManager resourceManager = ResourceManager.getInstance();

        String defaultLocale = resourceManager.getLocale();

        return getLocaleDatabase(defaultLocale);
    }

    // End JCF changes

    static final long serialVersionUID = 4941525634108401848L;

    /**
     * The constructor.
     *
     * @param in An InputStream to read the translation from.
     *
     * @throws Exception Description of the Exception
     */
    public LocaleDatabase(InputStream in) throws Exception
    {
        // We call the superclass default constructor
        super();
        add(in);
    }

    /**
     * Adds the contents of the given stream to the data base. The stream have to contain key value
     * pairs as declared by the DTD langpack.dtd.
     *
     * @param in an InputStream to read the translation from.
     *
     * @throws Exception
     */
    public void add(InputStream in) throws Exception
    {
        // Initialises the parser
        IXMLParser parser = new XMLParser();
        // We get the data
        IXMLElement data = parser.parse(in);

        // We check the data
        if (!"langpack".equalsIgnoreCase(data.getName()))
        {
            throw new Exception(
                    "this is not an IzPack XML langpack file");
        }

        // We fill the Hashtable
        Vector children = data.getChildren();
        int size = children.size();
        for (int i = 0; i < size; i++)
        {
            IXMLElement e = (IXMLElement) children.get(i);
            String text = e.getContent();
            
            if (e.getAttribute("id").equals("osrequirementfailed"))
            {
                System.out.println ();
            }
            
            if (text != null && !"".equals(text))
            {
                put(e.getAttribute("id"), text.trim());
            }
            else
            {
                put(e.getAttribute("id"), e.getAttribute("txt"));
            }
        }

    }

    /**
     * Convenience method to retrieve an element.
     *
     * @param key The key of the element to retrieve.
     *
     * @return The element value or the key if not found.
     */
    public String getString(String key)
    {
        String val = (String) get(key);
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
     *
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
                variables[i] = getString(curArg);
            }
        }

        String message = getString(key);

        // replace all ' characters because MessageFormat.format()
        // don't substitute quoted place holders '{0}'
        message = message.replace('\'', TEMP_QUOTING_CHARACTER);

        message = MessageFormat.format(message, variables);

        // replace all ' characters back
        return message.replace(TEMP_QUOTING_CHARACTER, '\'');
    }

}

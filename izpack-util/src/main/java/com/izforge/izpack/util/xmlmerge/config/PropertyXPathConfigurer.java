/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2009 Laurent Bovet, Alex Mathey
 * Copyright 2010, 2012 René Krell
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
package com.izforge.izpack.util.xmlmerge.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.izforge.izpack.util.xmlmerge.ConfigurationException;

/**
 * Reads the
 * {@link com.izforge.izpack.util.xmlmerge.factory.XPathOperationFactory}
 * configuration from a property file or a map.
 *
 * @author Laurent Bovet (LBO)
 * @author Alex Mathey (AMA)
 */
public class PropertyXPathConfigurer extends AbstractXPathConfigurer
{

    /**
     * Default action pathname.
     */
    public static final String DEFAULT_ACTION_KEY = "action.default";

    /**
     * Default mapper pathname.
     */
    public static final String DEFAULT_MAPPER_KEY = "mapper.default";

    /**
     * Default matcher pathname.
     */
    public static final String DEFAULT_MATCHER_KEY = "matcher.default";

    /**
     * XPath pathname prefix.
     */
    public static final String PATH_PREFIX = "xpath.";

    /**
     * Mapper pathname prefix.
     */
    public static final String MAPPER_PREFIX = "mapper.";

    /**
     * Matcher pathname prefix.
     */
    public static final String MATCHER_PREFIX = "matcher.";

    /**
     * Action pathname prefix.
     */
    public static final String ACTION_PREFIX = "action.";

    /**
     * Configuration properties.
     */
    Properties props;

    /**
     * Set of XPath paths.
     */
    Set<String> paths = new LinkedHashSet<String>();

    /**
     * Creates a PropertyXPathConfigurer which reads the configuration from a
     * properties file.
     *
     * @param propString A string representing the name of a properties file
     * @throws ConfigurationException If an error occurred during the creation
     * of the configurer
     */
    public PropertyXPathConfigurer(String propString) throws ConfigurationException
    {
        props = new Properties();
        try
        {
            props.load(new ByteArrayInputStream(propString.getBytes()));
        }
        catch (IOException ioe)
        {
            // Should not happen
            throw new ConfigurationException(ioe);
        }
    }

    /**
     * Creates a PropertyXPathConfigurer which reads the configuration from a
     * map.
     *
     * @param map A map containing configuration properties
     */
    public PropertyXPathConfigurer(Map<String, String> map)
    {
        props = new Properties();
        props.putAll(map);
    }

    /**
     * Creates a PropertyXPathConfigurer which reads the configuration from a
     * <code>Properties</code> object.
     *
     * @param properties The configuration properties
     */
    public PropertyXPathConfigurer(Properties properties)
    {
        props = properties;
    }

    @Override
    protected void readConfiguration() throws ConfigurationException
    {
        String token;

        token = props.getProperty(DEFAULT_ACTION_KEY);
        if (token != null)
        {
            setDefaultAction(token);
        }

        token = props.getProperty(DEFAULT_MAPPER_KEY);
        if (token != null)
        {
            setDefaultMapper(token);
        }

        token = props.getProperty(DEFAULT_MATCHER_KEY);
        if (token != null)
        {
            setDefaultMatcher(token);
        }

        Enumeration<Object> keys = props.keys();

        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();

            if (key.startsWith(PATH_PREFIX))
            {
                paths.add(key.substring(PATH_PREFIX.length()));
            }
        }

        for (String path : paths)
        {
            token = props.getProperty(ACTION_PREFIX + path);
            if (token != null)
            {
                addAction(props.getProperty(PATH_PREFIX + path), token);
            }
            token = props.getProperty(MAPPER_PREFIX + path);
            if (token != null)
            {
                addMapper(props.getProperty(PATH_PREFIX + path), token);
            }
            token = props.getProperty(MATCHER_PREFIX + path);
            if (token != null)
            {
                addMatcher(props.getProperty(PATH_PREFIX + path), token);
            }
        }

    }

}

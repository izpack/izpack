/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005,2009 Ivan SZKIBA
 * Copyright 2010,2011 Rene Krell
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
package com.izforge.izpack.api.config;

import com.izforge.izpack.api.config.Ini;
import com.izforge.izpack.api.config.IniPreferences;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

public class IniPreferencesFactory implements PreferencesFactory
{

    public static final String PROPERTIES = "ini4j.properties";
    public static final String KEY_USER = "org.ini4j.prefs.user";
    public static final String KEY_SYSTEM = "org.ini4j.prefs.system";
    private Preferences system;
    private Preferences user;

    @Override
    public synchronized Preferences systemRoot()
    {
        if (this.system == null)
        {
            this.system = newIniPreferences(KEY_SYSTEM);
        }

        return this.system;
    }

    @Override
    public synchronized Preferences userRoot()
    {
        if (this.user == null)
        {
            this.user = newIniPreferences(KEY_USER);
        }

        return this.user;
    }

    String getIniLocation(String key)
    {
        String location = Config.getSystemProperty(key);

        if (location == null)
        {
            try
            {
                Properties props = new Properties();

                props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES));
                location = props.getProperty(key);
            }
            catch (Exception x)
            {
                assert true;
            }
        }

        return location;
    }

    URL getResource(String location) throws IllegalArgumentException
    {
        try
        {
            URI uri = new URI(location);
            URL url;

            if (uri.getScheme() == null)
            {
                url = Thread.currentThread().getContextClassLoader().getResource(location);
            }
            else
            {
                url = uri.toURL();
            }

            return url;
        }
        catch (Exception x)
        {
            throw (IllegalArgumentException) new IllegalArgumentException().initCause(x);
        }
    }

    InputStream getResourceAsStream(String location) throws IllegalArgumentException
    {
        try
        {
            return getResource(location).openStream();
        }
        catch (Exception x)
        {
            throw (IllegalArgumentException) new IllegalArgumentException().initCause(x);
        }
    }

    Preferences newIniPreferences(String key)
    {
        Ini ini = new Ini();
        String location = getIniLocation(key);

        if (location != null)
        {
            try
            {
                ini.load(getResourceAsStream(location));
            }
            catch (Exception x)
            {
                throw (IllegalArgumentException) new IllegalArgumentException().initCause(x);
            }
        }

        return new IniPreferences(ini);
    }
}

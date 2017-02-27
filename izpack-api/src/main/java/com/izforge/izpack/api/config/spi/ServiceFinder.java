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
package com.izforge.izpack.api.config.spi;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

final class ServiceFinder
{

    private static final String SERVICES_PATH = "META-INF/services/";

    private ServiceFinder()
    {
    }

    static <T> T findService(Class<T> clazz)
    {
        try
        {

            // ez a cast nem lenne szükséges, de úgy a ClassCastException csak a hívónál jön...
            return clazz.cast(findServiceClass(clazz).newInstance());
        }
        catch (Exception x)
        {
            throw (IllegalArgumentException) new IllegalArgumentException("Provider " + clazz.getName() + " could not be instantiated: " + x)
                    .initCause(x);
        }
    }

    @SuppressWarnings(Warnings.UNCHECKED)
    static <T> Class<? extends T> findServiceClass(Class<T> clazz) throws IllegalArgumentException
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String serviceClassName = findServiceClassName(clazz.getName());
        Class<T> ret = clazz;

        if (serviceClassName != null)
        {
            try
            {
                ret = (Class<T>) ((classLoader == null) ? Class.forName(serviceClassName) : classLoader.loadClass(serviceClassName));
            }
            catch (ClassNotFoundException x)
            {
                throw (IllegalArgumentException) new IllegalArgumentException("Provider " + serviceClassName + " not found").initCause(x);
            }
        }

        return ret;
    }

    static String findServiceClassName(String serviceId) throws IllegalArgumentException
    {
        String serviceClassName = null;

        // Use the system property first
        try
        {
            String systemProp = System.getProperty(serviceId);

            if (systemProp != null)
            {
                serviceClassName = systemProp;
            }
        }
        catch (SecurityException x)
        {
            assert true;
        }

        if (serviceClassName == null)
        {
            serviceClassName = loadLine(SERVICES_PATH + serviceId);
        }

        return serviceClassName;
    }

    private static String loadLine(String servicePath)
    {
        String ret = null;

        // try to find services in CLASSPATH
        try
        {
            InputStream is = null;
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            if (classLoader == null)
            {
                is = ClassLoader.getSystemResourceAsStream(servicePath);
            }
            else
            {
                is = classLoader.getResourceAsStream(servicePath);
            }

            if (is != null)
            {
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line = rd.readLine();

                rd.close();
                if (line != null)
                {
                    line = line.trim();
                    if (line.length() != 0)
                    {
                        ret = line.split("\\s|#")[0];
                    }
                }
            }
        }
        catch (Exception x)
        {
            assert true;
        }

        return ret;
    }
}

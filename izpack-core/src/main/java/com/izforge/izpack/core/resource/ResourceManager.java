/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Marcus Stursberg
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

package com.izforge.izpack.core.resource;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import com.izforge.izpack.api.exception.ResourceException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Locales;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * With this ResourceManager you are able to get resources from the jar file.
 * <p/>
 * All resources are loaded language dependent as it's done in java.util.ResourceBundle. To set a
 * language dependent resource just append '_' and the locale to the end of the Resourcename<br>
 * <br>
 * Example:
 * <li>InfoPanel.info - for default value</li>
 * <li>InfoPanel.info_deu - for german value</li>
 * <li>InfoPanel.info_eng - for english value</li> <br>
 * <p/>
 *
 * @author Marcus Stursberg
 * @author Tim Anderson
 */
@ApplicationScoped
public class ResourceManager extends AbstractResources
{
    /**
     * The locales.
     */
    private Locales locales;

    /**
     * The base path where to find the resources: resourceBasePathDefaultConstant = "/res/"
     */
    public final static String RESOURCE_BASEPATH_DEFAULT = "/resources/";

    /**
     * Internally used resourceBasePath = "/resources/"
     */
    private String resourceBasePath = "/resources/";


    /**
     * Constructs a <tt>ResourceManager</tt>.
     */
    public ResourceManager()
    {
        this(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Constructs a <tt>ResourceManager</tt>.
     *
     * @param loader the class loader to use to load resources
     */
    public ResourceManager(ClassLoader loader)
    {
        super(loader);
    }

    public static String getInstallLoggingConfigurationResourceName()
    {
        return RESOURCE_BASEPATH_DEFAULT + DEFAULT_INSTALL_LOGGING_CONFIGURATION_RES;
    }

    /**
     * Registers the supported locales.
     *
     * @param locales the locales. May be {@code null}
     */
    public void setLocales(Locales locales)
    {
        this.locales = locales;
    }

    /**
     * Returns an InputStream contains the given Resource The Resource is loaded language dependen
     * by the informations from <code>this.locale</code> If there is no Resource for the current
     * language found, the default Resource is given.
     *
     * @param resource The resource to load
     * @return an InputStream contains the requested resource
     * @throws ResourceNotFoundException thrown if there is no resource found
     */
    @Override
    public InputStream getInputStream(String resource)
    {
        resource = getLanguageResourceString(resource);
        return super.getInputStream(resource);
    }

    /**
     * Returns the URL to a resource.
     *
     * @param name the resource name
     * @return the URL to the resource
     * @throws ResourceNotFoundException if the resource cannot be found
     */
    @Override
    public URL getURL(String name)
    {
        return getResource(getLanguageResourceString(name));
    }

    /**
     * Returns the locale's ISO3 language code.
     *
     * @return the current language code, or {@code null} if no locale is set
     */
    public String getLocale()
    {
        if (locales != null)
        {
            Locale locale = locales.getLocale();
            return (locale != null) ? locale.getISO3Language() : null;
        }
        return null;
    }

    public String getResourceBasePath()
    {
        return resourceBasePath;
    }

    public void setResourceBasePath(String resourceBasePath)
    {
        this.resourceBasePath = resourceBasePath;
    }

    /**
     * Returns an ArrayList of the available langpacks ISO3 codes.
     *
     * @return The available langpacks list.
     * @throws ResourceNotFoundException if the langpacks resource cannot be found
     * @throws ResourceException         if the langpacks resource cannot be retrieved
     * @deprecated use {@link com.izforge.izpack.api.resource.Locales#getLocales()}
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public List<String> getAvailableLangPacks()
    {
        return (List<String>) getObject("langpacks.info");
    }

    /**
     * Resolves relative resource names.
     * <p/>
     * This implementation prefixes relative resource names with {@link #getResourceBasePath()}.
     *
     * @param name the resource name
     * @return the absolute resource name
     */
    @Override
    protected String resolveName(String name)
    {
        name = (name.charAt(0) == '/') ? name : getResourceBasePath() + name;
        return super.resolveName(name);
    }

    /**
     * This method is used to get the language dependent path of the given resource. If there is a
     * resource for the current language the path of the language dependent resource is returned. If
     * there's no resource for the current lanugage the default path is returned.
     *
     * @param resource the resource to load
     * @return the language dependent path of the given resource
     * @throws ResourceNotFoundException If the resource is not found
     */
    private String getLanguageResourceString(String resource)
    {
        Locale locale = (locales != null) ? locales.getLocale() : null;
        String country = null;
        String language = null;
        if (locale != null)
        {
            country = LocaleHelper.getISO3Country(locale);
            language = LocaleHelper.getISO3Language(locale);
        }

        // use lowercase country code for backwards compatibility
        String resourcePath = (country != null) ? resource + "_" + country.toLowerCase() : null;
        if (resourcePath != null && getResource(resourcePath) != null)
        {
            return resourcePath;
        }
        resourcePath = (language != null) ? resource + "_" + language : null;
        if (resourcePath != null && getResource(resourcePath) != null)
        {
            return resourcePath;
        }
        if (getResource(resource) != null)
        {
            return resource;
        }
        if (resourcePath != null)
        {
            throw new ResourceNotFoundException("Cannot find named resource: '" + resource
                                                        + "' AND '" + resourcePath + "'");
        }
        throw new ResourceNotFoundException("Cannot find named resource: '" + resource + "'");
    }

}

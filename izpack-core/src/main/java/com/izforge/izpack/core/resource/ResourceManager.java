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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import javax.swing.ImageIcon;

import com.izforge.izpack.api.exception.ResourceException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Locales;

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
public class ResourceManager extends AbstractResources
{
    /**
     * The locales.
     */
    private Locales locales;

    /**
     * The base path where to find the resources: resourceBasePathDefaultConstant = "/res/"
     */
    public final String resourceBasePathDefaultConstant = "/resources/";

    /**
     * Internally used resourceBasePath = "/resources/"
     */
    private String resourceBasePath = "/resources/";


    /**
     * Constructs a <tt>ResourceManager</tt>.
     */
    public ResourceManager()
    {
        this(ClassLoader.getSystemClassLoader());
    }

    /**
     * Constructs a <tt>ResourceManager</tt>.
     *
     * @param loader     the class loader to use to load resources
     */
    public ResourceManager(ClassLoader loader)
    {
        super(loader);
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
     * If null was given the Default BasePath "/res/" is set If otherwise the Basepath is set to the
     * given String. This is useful if someone needs direct access to Reosurces in the jar.
     *
     * @param aDefaultBasePath If null was given the DefaultBasepath is re/set "/res/"
     * @deprecated no replacement
     */
    @Deprecated
    public void setDefaultOrResourceBasePath(String aDefaultBasePath)
    {
        // For direct access of named resources the BasePath should be empty
        if (null != aDefaultBasePath)
        {
            this.setResourceBasePath(aDefaultBasePath);
        }
        else
        {
            this.setResourceBasePath(resourceBasePathDefaultConstant);
        }
    }

    @Deprecated
    public boolean isResourceExist(String resource)
    {
        return this.getLanguageResourceString(resource) != null;
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
     * Get Input stream with a default value
     *
     * @param resource     Path of resource
     * @param defaultValue Default value if stream is not found
     * @return Stream found or default value
     * @deprecated no replacement
     */
    @Deprecated
    public InputStream getInputStream(String resource, InputStream defaultValue)
    {
        String resourcepath = this.getLanguageResourceString(resource);
        if (resourcepath == null)
        {
            return defaultValue;
        }
        return getInputStream(resourcepath);
    }

    /**
     * Returns a URL refers to the given Resource
     *
     * @param resource the resource to load
     * @return A languagedependen URL spezifies the requested resource
     * @throws ResourceNotFoundException thrown if there is no resource found
     * @deprecated use {@link #getURL(String)}
     */
    @Deprecated
    public URL getLocalizedURL(String resource)
    {
        return getResource(getLanguageResourceString(resource));
    }

    /**
     * Returns a text resource from the jar file. The resource is loaded by
     * ResourceManager#getResource and then converted into text.
     *
     * @param resource - a text resource to load
     * @param encoding - the encoding, which should be used to read the resource
     * @return a String contains the text of the resource
     * @throws ResourceNotFoundException if the resource can not be
     *                                   found
     * @throws IOException               if the resource can not be loaded
     * @deprecated use {@link com.izforge.izpack.api.resource.Resources#getString(String, String, String)}
     */
    @Deprecated
    public String getTextResource(String resource, String encoding) throws IOException
    {
        return readString(resource, encoding);
    }

    /**
     * Returns a text resource from the jar file. The resource is loaded by
     * ResourceManager#getResource and then converted into text.
     *
     * @param resource - a text resource to load
     * @return a String contains the text of the resource
     * @throws ResourceNotFoundException if the resource can not be found
     * @throws IOException               if the resource can not be loaded
     * @deprecated use {@link com.izforge.izpack.api.resource.Resources#getString(String)}
     */
    @Deprecated
    public String getTextResource(String resource) throws IOException
    {
        return this.getTextResource(resource, null);
    }

    /**
     * Returns a laguage dependent ImageIcon for the given Resource
     *
     * @param resource resrouce of the Icon
     * @param fallback fallback resources
     * @return a ImageIcon loaded from the given Resource
     * @throws ResourceNotFoundException thrown when the resource can not be found
     * @deprecated use {@link #getImageIcon(String, String...)}
     */
    @Deprecated
    public ImageIcon getImageIconResource(String resource, String... fallback)
    {
        return getImageIcon(resource, fallback);
    }

    /**
     * Sets the locale for the resourcefiles. The locale is taken from
     * InstallData#installData#getAttribute("langpack") If there is no language set, the default
     * language is english.
     *
     * @param locale of the resourcefile
     * @deprecated use {@link Locales#setLocale(java.util.Locale)}
     */
    @Deprecated
    public void setLocale(String locale)
    {
        locales.setLocale(locales.getLocale(locale));
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
     * Get langpack of the given locale
     *
     * @param localeISO3 langpack to get
     * @return InputStream on the xml
     * @deprecated use {@link Locales#getMessages(String)}
     */
    @Deprecated
    public InputStream getLangPack(String localeISO3)
    {
        return getInputStream("langpacks/" + localeISO3 + ".xml");
    }

    /**
     * Get langpack of the locale present in installData
     *
     * @return InputStream on the xml
     * @deprecated use {@link com.izforge.izpack.api.resource.Locales#getMessages()}
     */
    @Deprecated
    public InputStream getLangPack()
    {
        return this.getLangPack(locales.getLocale().getISO3Language());
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
     * resource for the current language the path of the language dependen resource is returnd. If
     * there's no resource for the current lanuage the default path is returned.
     *
     * @param resource Resource to load language dependen
     * @return the language dependent path of the given resource
     * @throws ResourceNotFoundException If the resource is not
     *                                   found
     */
    private String getLanguageResourceString(String resource)
    {
        String code = getLocale();
        String resourcePath = (code != null) ? resource + "_" + code : null;
        if (resourcePath != null && getResource(resourcePath) != null)
        {
            return resourcePath;
        }
        else if (getResource(resource) != null)
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

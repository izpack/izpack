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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.event.InstallerListener;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.util.Platform;

/**
 * Encloses information about the install process. This implementation is not thread safe.
 *
 * @author Julien Ponge <julien@izforge.com>
 * @author Johannes Lehtinen <johannes.lehtinen@iki.fi>
 */
public class AutomatedInstallData implements InstallData
{

    private RulesEngine rules;

    /**
     * The locale.
     */
    private Locale locale;

    /**
     * The messages.
     */
    private Messages messages;

    /**
     * The inforamtions.
     */
    private Info info;

    /**
     * The platform being installed to.
     */
    private Platform platform;

    /**
     * The complete list of packs.
     */
    private List<Pack> allPacks;

    /**
     * The available packs.
     */
    private List<Pack> availablePacks;

    /**
     * The selected packs.
     */
    private List<Pack> selectedPacks;

    /**
     * The panels order.
     */
    private List<Panel> panelsOrder;

    /**
     * The current panel.
     */
    private int curPanelNumber;

    /**
     * Can we close the installer ?
     */
    private boolean canClose = false;

    /**
     * Did the installation succeed ?
     */
    private boolean installSuccess = true;

    /**
     * Is a reboot necessary to complete the installation ?
     */
    private boolean rebootNecessary = false;

    /**
     * The xmlData for automated installers.
     */
    private IXMLElement xmlData;

    /**
     * Custom data.
     */
    private Map<String, List> customData = new HashMap<String, List>();

    /**
     * The variables.
     */
    private final Variables variables;

    /**
     * Dynamic variables
     */
    private Map<String, List<DynamicVariable>> dynamicvariables;

    /**
     * Dynamic conditions
     */
    private List<DynamicInstallerRequirementValidator> dynamicinstallerrequirements;

    /**
     * List of install requirements
     */
    private List<InstallerRequirement> installerrequirements;

    /**
     * The attributes used by the panels
     */
    private Map<String, Object> attributes;

    /**
     * The default install path
     */
    public final static String DEFAULT_INSTALL_PATH = "DEFAULT_INSTALL_PATH";
    /**
     * The install drive (Windows only, otherwise not set)
     */
    public final static String INSTALL_DRIVE = "INSTALL_DRIVE";
    /**
     * The default install drive (Windows only, otherwise not set)
     */
    public final static String DEFAULT_INSTALL_DRIVE = "DEFAULT_INSTALL_DRIVE";

    /**
     * The listeners.
     */
    private List<InstallerListener> installerListener = new ArrayList<InstallerListener>();


    /**
     * Constructs an <tt>AutomatedInstallData</tt>.
     *
     * @param variables the variables
     * @param platform  the platform
     */
    public AutomatedInstallData(Variables variables, Platform platform)
    {
        this.variables = variables;
        this.platform = platform;
        setAvailablePacks(new ArrayList<Pack>());
        setSelectedPacks(new ArrayList<Pack>());
        setPanelsOrder(new ArrayList<Panel>());
        setXmlData(new XMLElementImpl("AutomatedInstallation"));
        setAttributes(new HashMap<String, Object>());
    }

    /**
     * Returns the variables.
     *
     * @return the variables
     */
    @Override
    public Variables getVariables()
    {
        return variables;
    }

    /**
     * Sets a variable to the specified value. This is short hand for
     * {@code getVariables().set(name, value)}.
     *
     * @param name  the name of the variable
     * @param value the new value of the variable
     * @see #getVariable
     */
    @Override
    public void setVariable(String name, String value)
    {
        variables.set(name, value);
    }

    /**
     * Returns the current value of the specified variable. This is short hand for
     * {@code getVariables().get(name)}.
     *
     * @param name the name of the variable
     * @return the value of the variable or null if not set
     * @see #setVariable
     */
    @Override
    public String getVariable(String name)
    {
        return variables.get(name);
    }

    /**
     * Refreshes dynamic variables. This is short hand for {@code getVariables().refresh()}.
     */
    @Override
    public void refreshVariables()
    {
        variables.refresh();
    }

    /**
     * Sets the install path.
     *
     * @param path the new install path
     * @see #getInstallPath
     */
    @Override
    public void setInstallPath(String path)
    {
        setVariable(INSTALL_PATH, path);
    }

    /**
     * Returns the install path.
     *
     * @return the current install path or null if none set yet
     * @see #setInstallPath
     */
    @Override
    public String getInstallPath()
    {
        return getVariable(INSTALL_PATH);
    }

    /**
     * Sets the default install path.
     *
     * @param path the default install path
     * @see #getDefaultInstallPath
     */
    @Override
    public void setDefaultInstallPath(String path)
    {
        setVariable(DEFAULT_INSTALL_PATH, path);
    }

    /**
     * Returns the default install path.
     *
     * @return the default install path or null if none set yet
     * @see #setDefaultInstallPath
     */
    @Override
    public String getDefaultInstallPath()
    {
        return getVariable(DEFAULT_INSTALL_PATH);
    }

    /**
     * Sets the media path for multi-volume installation.
     *
     * @param path the media path. May be <tt>null</tt>
     */
    @Override
    public void setMediaPath(String path)
    {
        setVariable(MEDIA_PATH, path);
    }

    /**
     * Returns the media path for multi-volume installation.
     *
     * @return the media path. May be <tt>null</tt>
     */
    @Override
    public String getMediaPath()
    {
        return getVariable(MEDIA_PATH);
    }

    /**
     * Returns the value of the named attribute.
     *
     * @param name the name of the attribute
     * @return the value of the attribute or null if not set
     * @see #setAttribute
     */
    @Override
    public Object getAttribute(String name)
    {
        return getAttributes().get(name);
    }

    /**
     * Sets a named attribute. The panels and other IzPack components can attach custom attributes
     * to InstallData to communicate with each other. For example, a set of co-operating custom
     * panels do not need to implement a common data storage but can use InstallData singleton. The
     * name of the attribute should include the package and class name to prevent name space
     * collisions.
     *
     * @param name  the name of the attribute to set
     * @param value the value of the attribute or null to unset the attribute
     * @see #getAttribute
     */
    @Override
    public void setAttribute(String name, Object value)
    {
        if (value == null)
        {
            getAttributes().remove(name);
        }
        else
        {
            getAttributes().put(name, value);
        }
    }

    /**
     * Set Locale in xml, installdata, and langpack.
     *
     * @param locale         Locale to set
     * @param localeDatabase database containing the desired locale
     */
    @Deprecated
    public void setAndProcessLocal(String locale, LocaleDatabase localeDatabase)
    {
        // We add an xml data information
        getXmlData().setAttribute("langpack", locale);
        // We load the langpack
        setVariable(ScriptParserConstant.ISO3_LANG, getLocaleISO3());
        setVariable(ScriptParserConstant.ISO2_LANG, getLocaleISO3());
        setMessages(localeDatabase);
    }

    @Override
    public RulesEngine getRules()
    {
        return rules;
    }


    public void setRules(RulesEngine rules)
    {
        this.rules = rules;
    }

    @Override
    public String getLocaleISO3()
    {
        return getVariable(ScriptParserConstant.ISO3_LANG);
    }
    
    @Override
    public String getLocaleISO2()
    {
        return getVariable(ScriptParserConstant.ISO2_LANG);
    }

    @Deprecated
    public void setLocaleISO3(String localeISO3)
    {
    }

    @Override
    public Locale getLocale()
    {
        return locale;
    }

    /**
     * Sets the locale.
     * <p/>
     * NOTE: for backwards compatibility, this sets the {@link ScriptParserConstant#ISO3_LANG ISO3} variable to be the
     * the <em>lowercase</em> version of the supplied ISO code.
     *
     * @param locale the locale
     * @param code   the 3 character ISO code used to select the locale. May be an ISO country code or an ISO language
     *               code
     */
    public void setLocale(Locale locale, String code)
    {
        this.locale = locale;
        getXmlData().setAttribute("langpack", code.toLowerCase());
        setVariable(ScriptParserConstant.ISO3_LANG, code.toLowerCase());
        if(locale != null) 
        {
            setVariable(ScriptParserConstant.ISO2_LANG, locale.getLanguage());
        }
    }

    /**
     * Sets the localised messages.
     *
     * @param messages the localised messages
     */
    public void setMessages(Messages messages)
    {
        this.messages = messages;
    }

    /**
     * Returns the localised messages.
     *
     * @return the localised messages
     */
    @Override
    public Messages getMessages()
    {
        return messages;
    }

    @Deprecated
    public LocaleDatabase getLangpack()
    {
        return (LocaleDatabase) messages;
    }

    @Deprecated
    public void setLangpack(LocaleDatabase langpack)
    {
        setMessages(langpack);
    }

    @Override
    public Info getInfo()
    {
        return info;
    }

    public void setInfo(Info info)
    {
        this.info = info;
    }

    /**
     * Returns the current platform.
     *
     * @return the current platform
     */
    @Override
    public Platform getPlatform()
    {
        return platform;
    }

    @Override
    public List<Pack> getAllPacks()
    {
        return allPacks;
    }

    public void setAllPacks(List<Pack> allPacks)
    {
        this.allPacks = allPacks;
    }

    @Override
    public List<Pack> getAvailablePacks()
    {
        return availablePacks;
    }

    public void setAvailablePacks(List<Pack> availablePacks)
    {
        this.availablePacks = availablePacks;
    }

    @Override
    public List<Pack> getSelectedPacks()
    {
        return selectedPacks;
    }

    @Override
    public void setSelectedPacks(List<Pack> selectedPacks)
    {
        this.selectedPacks = selectedPacks;
    }

    @Override
    public List<Panel> getPanelsOrder()
    {
        return panelsOrder;
    }

    public void setPanelsOrder(List<Panel> panelsOrder)
    {
        this.panelsOrder = panelsOrder;
    }

    /**
     * Returns the current panel number.
     *
     * @return the current panel  number
     * @deprecated use {@code Panels#getIndex()}.
     */
    @Deprecated
    public int getCurPanelNumber()
    {
        return curPanelNumber;
    }

    /**
     * Sets the current panel number.
     *
     * @param curPanelNumber the current panel number
     * @deprecated no replacement
     */
    @Deprecated
    public void setCurPanelNumber(int curPanelNumber)
    {
        this.curPanelNumber = curPanelNumber;
    }

    @Override
    public boolean isCanClose()
    {
        return canClose;
    }

    public void setCanClose(boolean canClose)
    {
        this.canClose = canClose;
    }

    @Override
    public boolean isInstallSuccess()
    {
        return installSuccess;
    }

    @Override
    public void setInstallSuccess(boolean installSuccess)
    {
        this.installSuccess = installSuccess;
    }

    @Override
    public boolean isRebootNecessary()
    {
        return rebootNecessary;
    }

    @Override
    public void setRebootNecessary(boolean rebootNecessary)
    {
        this.rebootNecessary = rebootNecessary;
    }

    @Override
    public IXMLElement getXmlData()
    {
        return xmlData;
    }

    public void setXmlData(IXMLElement xmlData)
    {
        this.xmlData = xmlData;
    }

    @Deprecated
    public Map<String, List> getCustomData()
    {
        return customData;
    }

    @Deprecated
    public void setCustomData(Map<String, List> customData)
    {
        this.customData = customData;
    }

    @Deprecated
    public void setVariables(Variables variables)
    {
    }

    public Map<String, Object> getAttributes()
    {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes)
    {
        this.attributes = attributes;
    }

    @Deprecated
    public Map<String, List<DynamicVariable>> getDynamicvariables()
    {
        return this.dynamicvariables;
    }

    @Deprecated
    public void setDynamicvariables(Map<String, List<DynamicVariable>> dynamicvariables)
    {
        this.dynamicvariables = dynamicvariables;
    }

    public List<DynamicInstallerRequirementValidator> getDynamicInstallerRequirements()
    {
        return this.dynamicinstallerrequirements;
    }

    @Deprecated
    public List<DynamicInstallerRequirementValidator> getDynamicinstallerrequirements()
    {
        return getDynamicInstallerRequirements();
    }

    public void setDynamicInstallerRequirements(List<DynamicInstallerRequirementValidator> requirements)
    {
        this.dynamicinstallerrequirements = requirements;
    }

    public void setInstallerRequirements(List<InstallerRequirement> requirements)
    {
        this.installerrequirements = requirements;
    }

    @Override
    public List<InstallerRequirement> getInstallerRequirements()
    {
        return installerrequirements;
    }

    @Deprecated
    public List<InstallerRequirement> getInstallerrequirements()
    {
        return getInstallerRequirements();
    }

    @Deprecated
    public List<InstallerListener> getInstallerListener()
    {
        return installerListener;
    }

    @Deprecated
    public void setInstallerListener(List<InstallerListener> installerListener)
    {
        this.installerListener = installerListener;
    }
}

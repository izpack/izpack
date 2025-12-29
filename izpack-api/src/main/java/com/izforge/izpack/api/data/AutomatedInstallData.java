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
import java.util.function.Predicate;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.util.Platform;

import jakarta.enterprise.inject.Vetoed;
import jakarta.inject.Inject;

import static com.izforge.izpack.util.Platform.Name.WINDOWS;

/**
 * Encloses information about the install process. This implementation is not thread safe.
 *
 * @author Julien Ponge <julien@izforge.com>
 * @author Johannes Lehtinen <johannes.lehtinen@iki.fi>
 */
@Vetoed
public class AutomatedInstallData implements InstallData
{
    /**
     * The attribute for a panel ID in the auto-install.xml record
     */
    public static final String AUTOINSTALL_PANELROOT_ATTR_ID = "id";

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
    private final Platform platform;

    /**
     * The complete list of packs.
     */
    private final List<Pack> allPacks;

    /**
     * The available packs.
     */
    private final List<Pack> availablePacks;

    /**
     * The selected packs.
     */
    private final List<Pack> selectedPacks;

    /**
     * The panels order.
     */
    private final List<Panel> panelsOrder;

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
     * The variables.
     */
    private final Variables variables;

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
     * Index that maps panel IDs to their XML root from the auto-install.xml record
     */
    private HashMap<String, IXMLElement> panelRootXml;

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
     * Constructs an <tt>AutomatedInstallData</tt>.
     *
     * @param variables the variables
     * @param platform  the platform
     */
    @Inject
    public AutomatedInstallData(Variables variables, Platform platform)
    {
        this.variables = variables;
        this.platform = platform;
        this.allPacks = new ArrayList<>();
        this.selectedPacks = new ArrayList<>();
        this.availablePacks = new ArrayList<>();
        this.panelsOrder = new ArrayList<>();
        this.attributes = new HashMap<>();
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
    public final void setInstallPath(String path)
    {
        setVariable(INSTALL_PATH, path);
        setInstallDriveFromPath(path, INSTALL_DRIVE);
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
    public final void setDefaultInstallPath(String path)
    {
        setVariable(DEFAULT_INSTALL_PATH, path);
        setInstallDriveFromPath(path, DEFAULT_INSTALL_DRIVE);
    }


    private void setInstallDriveFromPath(String path, String variable)
    {
        if (getPlatform().isA(WINDOWS))
        {
            String[] parts = path.trim().split(":", 2);
            if (parts.length > 0 && parts[0].length() == 1)
            {
                setVariable(variable, parts[0] + ":");
            }
        }
    }

    /**
     * Returns the default install path.
     *
     * @return the default install path or null if none set yet
     * @see #setDefaultInstallPath
     */
    @Override
    public final String getDefaultInstallPath()
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
        getInstallationRecord().setAttribute("langpack", code.toLowerCase());
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

    @Override
    public List<Pack> getAvailablePacks()
    {
        return availablePacks;
    }

    public void updateAvailablePacks(Predicate<Pack> availablePredicate)
    {
        availablePacks.clear();
        allPacks.stream().filter(availablePredicate).forEach(availablePacks::add);
    }

    @Override
    public void updateSelectedPacks(Predicate<Pack> selectedPredicate)
    {
        selectedPacks.clear();
        availablePacks.stream().filter(selectedPredicate).forEach(selectedPacks::add);
    }

    @Override
    public List<Pack> getSelectedPacks()
    {
        return selectedPacks;
    }

    @Override
    public List<Panel> getPanelsOrder()
    {
        return panelsOrder;
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
    public IXMLElement getInstallationRecord()
    {
        return xmlData;
    }

    public IXMLElement getInstallationRecordPanelRoot(String panelId)
    {
        return panelRootXml.get(panelId);
    }

    public void setInstallationRecord(IXMLElement xmlData)
    {
        panelRootXml = new HashMap<>();
        List<IXMLElement> panelRoots = xmlData.getChildren();
        for (IXMLElement panelRoot : panelRoots)
        {
            panelRootXml.put(panelRoot.getAttribute(AUTOINSTALL_PANELROOT_ATTR_ID), panelRoot);
        }

        this.xmlData = xmlData;
    }

    public Map<String, Object> getAttributes()
    {
        return attributes;
    }

    @Override
    public List<DynamicInstallerRequirementValidator> getDynamicInstallerRequirements()
    {
        return this.dynamicinstallerrequirements;
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

    @Override
    public boolean isEnableConsoleReader()
    {
        return false;
    }
}

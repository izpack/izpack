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

package com.izforge.izpack.api.data;

import java.util.List;
import java.util.Locale;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.util.Platform;


/**
 * {@code InstallData} holds data used throughout the installation.
 *
 * @author Tim Anderson
 */
public interface InstallData
{
    /**
     * The path for multi-volume installation media.
     */
    String MEDIA_PATH = "MEDIA_PATH";

    /**
     * The install path.
     */
    String INSTALL_PATH = "INSTALL_PATH";

    /**
     * Determines if the installation is being modified.
     */
    String MODIFY_INSTALLATION = "modify.izpack.install";

    /**
     * Installation information file name.
     */
    String INSTALLATION_INFORMATION = ".installationinformation";

    /**
     * Sets a variable to the specified value.
     * <p/>
     * This is short hand for {@code getVariables().set(name, value)}.
     *
     * @param name  the name of the variable
     * @param value the new value of the variable. May be {@code null}
     * @see #getVariable
     */
    void setVariable(String name, String value);

    /**
     * Returns the current value of the specified variable.
     * <p/>
     * This is short hand for {@code getVariables().get(name)}.
     *
     * @param name the name of the variable
     * @return the value of the variable or {@code null} if not set
     * @see #setVariable
     */
    String getVariable(String name);

    /**
     * Refreshes dynamic variables.
     * <p/>
     * This is short hand for {@code getVariables().refresh()}.
     */
    void refreshVariables();

    /**
     * Returns the variables.
     *
     * @return the variables
     */
    Variables getVariables();

    /**
     * Sets the install path.
     *
     * @param path the new install path. May be {@code null}
     * @see #getInstallPath
     */
    void setInstallPath(String path);

    /**
     * Returns the install path.
     *
     * @return the current install path. May be {@code null}
     * @see #setInstallPath
     */
    String getInstallPath();

    /**
     * Sets the default install path.
     *
     * @param path the default install path. May be {@code null}
     * @see #getDefaultInstallPath
     */
    void setDefaultInstallPath(String path);

    /**
     * Returns the default install path.
     *
     * @return the default install path. May be {@code null}
     * @see #setDefaultInstallPath
     */
    String getDefaultInstallPath();

    /**
     * Sets the media path for multi-volume installation.
     *
     * @param path the media path. May be {@code null}
     */
    void setMediaPath(String path);

    /**
     * Returns the media path for multi-volume installation.
     *
     * @return the media path. May be {@code null}
     */
    String getMediaPath();

    /**
     * Returns the rules.
     *
     * @return the rules
     */
    RulesEngine getRules();

    /**
     * Returns the current locale.
     *
     * @return the current locale. May be {@code null}
     */
    Locale getLocale();

    /**
     * Returns the current locale's ISO3 language code.
     *
     * @return the current locale's ISO3 language code. May be {@code null}
     */
    String getLocaleISO3();
    
     /**
     * Returns the current locale's ISO2 language code.
     *
     * @return the current locale's ISO2 language code. May be {@code null}
     */
    String getLocaleISO2();

    /**
     * Returns the localised messages.
     *
     * @return the localised messages
     */
    Messages getMessages();

    /**
     * Returns the installation information.
     *
     * @return the installation information
     */
    Info getInfo();

    /**
     * Returns the current platform.
     *
     * @return the current platform
     */
    Platform getPlatform();

    /**
     * Returns all packs.
     *
     * @return the packs
     */
    List<Pack> getAllPacks();

    /**
     * Returns the packs available to be installed on the current platform.
     *
     * @return the available packs
     */
    List<Pack> getAvailablePacks();

    /**
     * Returns the selected packs.
     *
     * @return the selected packs
     */
    List<Pack> getSelectedPacks();

    /**
     * Sets the selected packs.
     *
     * @param selectedPacks the selected packs
     */
    void setSelectedPacks(List<Pack> selectedPacks);

    /**
     * Returns the panels.
     *
     * @return the panels
     */
    List<Panel> getPanelsOrder();

    /**
     * Determines if the installer can close.
     *
     * @return {@code true} if the installer can close; otherwise {@code false}
     */
    boolean isCanClose();

    /**
     * Determines if the installation was successful.
     * <p/>
     * NOTE: the result of this method is undefined until the panels have completed or failed.
     *
     * @return {@code true} if the installation was successful
     */
    boolean isInstallSuccess();

    /**
     * Determines if the installation was successful.
     *
     * @param success if {@code true} indicates installation was successful
     */
    void setInstallSuccess(boolean success);

    /**
     * Determines if a reboot is required after installation.
     *
     * @return {@code true} if a reboot is required
     */
    boolean isRebootNecessary();

    /**
     * Determines if a reboot is required after installation.
     *
     * @param reboot if {@code true} indicates a reboot is required
     */
    void setRebootNecessary(boolean reboot);

    /**
     * Returns the XML data for automated installation.
     *
     * @return the XML data
     */
    IXMLElement getInstallationRecord();

    /**
     * Returns the installer requirements.
     * <p/>
     * These are evaluated prior to installation, to ensure all prerequisites are met.
     *
     * @return the installer requirements
     */
    List<InstallerRequirement> getInstallerRequirements();

    /**
     * Returns the dynamic installer requirement validators.
     * <p/>
     * These are evaluated on each panel change.
     *
     * @return the dynamic installer requirement validators
     */
    List<DynamicInstallerRequirementValidator> getDynamicInstallerRequirements();

    /**
     * Sets a named attribute.
     * <p/>
     * The panels and other IzPack components can attach custom attributes to InstallData to communicate with each
     * other. For example, a set of co-operating custom panels do not need to implement a common data storage but can
     * use InstallData singleton. The name of the attribute should include the package and class name to prevent name
     * space collisions.
     *
     * @param name  the name of the attribute to set
     * @param value the value of the attribute or null to unset the attribute
     * @see #getAttribute
     */
    void setAttribute(String name, Object value);

    /**
     * Returns the value of the named attribute.
     *
     * @param name the name of the attribute
     * @return the value of the attribute or null if not set
     * @see #setAttribute
     */
    Object getAttribute(String name);

}


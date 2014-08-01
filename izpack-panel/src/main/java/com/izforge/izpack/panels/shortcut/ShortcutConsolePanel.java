/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://www.izforge.com/izpack/ http://izpack.codehaus.org/
 *
 * Copyright 2013 Tim Anderson
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.izforge.izpack.panels.shortcut;


import static com.izforge.izpack.api.handler.Prompt.Option.NO;
import static com.izforge.izpack.api.handler.Prompt.Option.YES;
import static com.izforge.izpack.api.handler.Prompt.Options.YES_NO;
import static com.izforge.izpack.api.handler.Prompt.Type.INFORMATION;
import static com.izforge.izpack.api.handler.Prompt.Type.QUESTION;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.console.AbstractConsolePanel;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.event.InstallerListeners;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.util.Console;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.PlatformModelMatcher;
import com.izforge.izpack.util.TargetFactory;
import com.izforge.izpack.util.os.Shortcut;

/**
 * Console implementation of the {@link ShortcutPanel}.
 *
 * @author Tim Anderson
 */
public class ShortcutConsolePanel extends AbstractConsolePanel
{
    private final Prompt prompt;
    private final InstallData installData;
    private final ShortcutPanelLogic shortcutPanelLogic;

    private static final Logger logger = Logger.getLogger(ShortcutConsolePanel.class.getName());

    /**
     * Constructs a {@code ShortcutConsolePanel}.
     *
     * @param installData   the installation data
     * @param resources     the resources
     * @param uninstallData the uninstallation data
     * @param housekeeper   the housekeeper
     * @param factory       the target factory
     * @param listeners     the installation listeners
     * @param matcher       the platform-model matcher
     * @param prompt        the prompt
     * @param panel         the parent panel/view
     */
    public ShortcutConsolePanel(InstallData installData, Resources resources, UninstallData uninstallData,
                                Housekeeper housekeeper, TargetFactory factory, InstallerListeners listeners,
                                PlatformModelMatcher matcher, Prompt prompt, PanelView<ConsolePanel> panel)
    {
        super(panel);
        ShortcutPanelLogic shortcutPanelLogic = null;
        try
        {
            shortcutPanelLogic = new ShortcutPanelLogic(
                    installData, resources, uninstallData, housekeeper, factory, listeners, matcher);
        }
        catch (Exception exception)
        {
            logger.log(Level.WARNING, "Failed to initialise shortcuts: " + exception.getMessage(), exception);
        }

        this.prompt = prompt;
        this.installData = installData;
        this.shortcutPanelLogic = shortcutPanelLogic;
    }

    /**
     * Runs the panel using the supplied properties.
     *
     * @param installData the installation data
     * @param properties  the properties
     * @return {@code true} if the installation is successful, otherwise {@code false}
     */
    @Override
    public boolean run(InstallData installData, Properties properties)
    {
        boolean result = false;
        if (shortcutPanelLogic != null)
        {
            if (shortcutPanelLogic.isSupported())
            {
            }
            else if (shortcutPanelLogic.skipIfNotSupported())
            {
                result = true;
            }
        }
        return result;
    }

    /**
     * Runs the panel using the specified console.
     *
     * @param installData the installation data
     * @param console     the console
     * @return {@code true} if the panel ran successfully, otherwise {@code false}
     */
    @Override
    public boolean run(InstallData installData, Console console)
    {
        boolean result = true;
        try
        {
            shortcutPanelLogic.refreshShortcutData();
        }
        catch (Exception e)
        {
            return result;
        }


        if (shortcutPanelLogic != null  && shortcutPanelLogic.canCreateShortcuts())
        {
            if (shortcutPanelLogic.isSupported())
            {
                chooseShortcutLocations();
                chooseEffectedUsers();
                chooseProgramGroup(console);

                if (shortcutPanelLogic.isCreateShortcutsImmediately())
                {
                    try
                    {
                        shortcutPanelLogic.createAndRegisterShortcuts();
                    }
                    catch (Exception e)
                    {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
                return true;
            }
            else if (!shortcutPanelLogic.skipIfNotSupported())
            {
                Messages messages = installData.getMessages();
                String message = messages.get("ShortcutPanel.alternate.apology");
                prompt.message(INFORMATION, message);
            }
        }
        return result;
    }

    /**
     * Prompt user where the shortcuts should be placed.
     */
    private void chooseShortcutLocations()
    {
        Prompt.Option createMenuShortcuts = prompt.confirm(QUESTION, shortcutPanelLogic.getCreateShortcutsPrompt(), YES_NO);
        shortcutPanelLogic.setCreateMenuShortcuts(createMenuShortcuts == YES);

        if (shortcutPanelLogic.hasDesktopShortcuts())
        {
            boolean selected = shortcutPanelLogic.isDesktopShortcutCheckboxSelected();
            Prompt.Option createDesktopShortcuts = prompt.confirm(QUESTION, shortcutPanelLogic.getCreateDesktopShortcutsPrompt(), YES_NO, selected ? YES : NO);
            shortcutPanelLogic.setCreateDesktopShortcuts(createDesktopShortcuts == YES);
        }

        if (shortcutPanelLogic.hasStartupShortcuts())
        {
            boolean selected = shortcutPanelLogic.isStartupShortcutCheckboxSelected();
            Prompt.Option createStartupShortcuts = prompt.confirm(QUESTION, shortcutPanelLogic.getCreateStartupShortcutsPrompt(), YES_NO, selected ? YES : NO);
            shortcutPanelLogic.setCreateStartupShortcuts(createStartupShortcuts == YES);
        }
    }

    /**
     * Choose for which user's the shortcuts should be created for
     */
    private void chooseEffectedUsers()
    {
        boolean isAdmin = shortcutPanelLogic.initUserType();
        if (isAdmin && shortcutPanelLogic.isSupportingMultipleUsers())
        {
            boolean selected = !shortcutPanelLogic.isDefaultCurrentUserFlag();
            String message = shortcutPanelLogic.getCreateForUserPrompt() + " " + shortcutPanelLogic.getCreateForAllUsersPrompt();
            Prompt.Option allUsers = prompt.confirm(QUESTION, message, YES_NO, selected ? YES : NO);
            shortcutPanelLogic.setUserType(allUsers == YES ? Shortcut.ALL_USERS : Shortcut.CURRENT_USER);
        }
    }

    /**
     * Choose under which program group to place the shortcuts.
     */
    private void chooseProgramGroup(Console console)
    {
        String programGroup = shortcutPanelLogic.getSuggestedProgramGroup();
        if (programGroup != null && "".equals(programGroup))
        {
            programGroup = console.prompt(installData.getMessages().get("ShortcutPanel.regular.list"), "");
        }
        shortcutPanelLogic.setGroupName(programGroup);
    }

    @Override
    public void createInstallationRecord(IXMLElement panelRoot)
    {
        try
        {
            new ShortcutPanelAutomationHelper(shortcutPanelLogic).createInstallationRecord(installData, panelRoot);
        }
        catch (Exception e)
        {
            logger.log(Level.WARNING, "Could generate automatic installer description for shortcuts.");
        }
    }
}

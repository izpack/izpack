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

    /**
     * The panel shortcutPanelLogic.
     */
    private final ShortcutPanelLogic shortcutPanelLogic;

    private final InstallData installData;
    /**
     * The prompt.
     */
    private final Prompt prompt;

    /**
     * The logger.
     */
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
            shortcutPanelLogic = new ShortcutPanelLogic(installData, resources, uninstallData, housekeeper, factory, listeners,
                                           matcher);
        }
        catch (Exception exception)
        {
            logger.log(Level.WARNING, "Failed to initialise shortcuts: " + exception.getMessage(), exception);
        }
        this.shortcutPanelLogic = shortcutPanelLogic;
        this.prompt = prompt;
        this.installData = installData;

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
            else if (shortcutPanelLogic.isSkipIfNotSupported())
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
        if (shortcutPanelLogic != null)
        {
            if (shortcutPanelLogic.isSupported())
            {
                if (prompt.confirm(QUESTION, shortcutPanelLogic.getCreateShortcutsPrompt(), YES_NO) == YES)
                {
                    result = createShortcuts(installData, console);
                }
                else
                {
                    shortcutPanelLogic.setCreateShortcuts(false);
                }
            }
            else if (!shortcutPanelLogic.isSkipIfNotSupported())
            {
                Messages messages = installData.getMessages();
                String message = messages.get("ShortcutPanel.alternate.apology");
                prompt.message(INFORMATION, message);
            }
        }
        return result;
    }

    private boolean createShortcuts(InstallData installData, Console console)
    {
        Messages messages = installData.getMessages();
        boolean isAdmin = shortcutPanelLogic.initUserType();
        boolean createDesktopShortcuts = false;
        boolean allUsers = false;
        String programGroup = shortcutPanelLogic.getSuggestedProgramGroup();

        if (shortcutPanelLogic.hasDesktopShortcuts())
        {
            boolean selected = shortcutPanelLogic.isDesktopShortcutCheckboxSelected();
            if (prompt.confirm(QUESTION, shortcutPanelLogic.getCreateDesktopShortcutsPrompt(), YES_NO,
                               selected ? YES : NO) == YES)
            {
                createDesktopShortcuts = true;
            }
        }
        if (isAdmin && shortcutPanelLogic.isSupportingMultipleUsers())
        {
            boolean selected = !shortcutPanelLogic.isDefaultCurrentUserFlag();
            String message = shortcutPanelLogic.getCreateForUserPrompt() + " " + shortcutPanelLogic.getCreateForAllUsersPrompt();
            // TODO - really should have a separate message
            if (prompt.confirm(QUESTION, message, YES_NO, selected ? YES : NO) == YES)
            {
                allUsers = true;
            }
        }
        if (programGroup != null && "".equals(programGroup))
        {
            programGroup = console.prompt(messages.get("ShortcutPanel.regular.list"), null);
            if (programGroup == null)
            {
                return false;
            }
        }
        shortcutPanelLogic.setGroupName(programGroup);
        shortcutPanelLogic.setCreateDesktopShortcuts(createDesktopShortcuts);
        shortcutPanelLogic.setCreateShortcuts(true);
        shortcutPanelLogic.setUserType(allUsers ? Shortcut.ALL_USERS : Shortcut.CURRENT_USER);
        if (shortcutPanelLogic.isCreateShortcutsImmediately())
        {
            try
            {
                shortcutPanelLogic.createAndRegisterShortcuts();
            }
            catch (Exception e)
            {
                logger.log(Level.WARNING, e.getMessage(), e);
                // ignore exception
            }
        }
        return true;
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

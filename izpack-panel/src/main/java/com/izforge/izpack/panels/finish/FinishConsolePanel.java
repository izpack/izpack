/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Jan Blok
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

package com.izforge.izpack.panels.finish;

import java.io.File;
import java.util.Properties;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.handler.Prompt.Option;
import com.izforge.izpack.api.handler.Prompt.Options;
import com.izforge.izpack.api.handler.Prompt.Type;
import com.izforge.izpack.installer.automation.AutomatedPanelView;
import com.izforge.izpack.installer.automation.AutomatedPanels;
import com.izforge.izpack.installer.console.AbstractConsolePanel;
import com.izforge.izpack.installer.console.ConsoleInstaller;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.console.ConsolePanelAutomationHelper;
import com.izforge.izpack.installer.container.provider.AutomatedPanelsProvider;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.util.Console;
import com.izforge.izpack.util.PlatformModelMatcher;

/**
 * Console implementation of the {@link FinishPanel}.
 *
 * @author Mounir el hajj
 */
public class FinishConsolePanel extends AbstractConsolePanel
{
    private static final Logger LOGGER = Logger.getLogger(FinishConsolePanel.class.getName());
    private static final String AUTO_INSTALL_SCRIPT_NAME = "auto-install.xml";

    private final Prompt prompt;
    private final ObjectFactory factory;
    private final PlatformModelMatcher matcher;
    private final ConsoleInstaller parent;
    private final UninstallData uninstallData;

    /**
     * Constructs an {@code FinishConsolePanel}.
     *
     * @param panel the parent panel/view. May be {@code null}
     */
    public FinishConsolePanel(final ObjectFactory factory, ConsoleInstaller parent, final PlatformModelMatcher matcher,
            UninstallData uninstallData, Prompt prompt, PanelView<ConsolePanel> panel)
    {
        super(panel);
        this.parent = parent;
        this.prompt = prompt;
        this.factory = factory;
        this.matcher = matcher;
        this.uninstallData = uninstallData;
    }

    public FinishConsolePanel(PanelView<ConsolePanel> panel)
    {
        this(null, null, null, null, null, panel);
    }

    /**
     * Runs the panel using the supplied properties.
     *
     * @param installData the installation data
     * @param properties the properties
     * @return <tt>true</tt>
     */
    @Override
    public boolean run(InstallData installData, Properties properties)
    {
        return true;
    }

    /**
     * Runs the panel using the specified console.
     *
     * @param installData the installation data
     * @param console the console
     * @return <tt>true</tt>
     */
    @Override
    public boolean run(InstallData installData, Console console)
    {
        if (doGenerateAutoInstallScript())
        {
            generateAutoInstallScript(installData, uninstallData, console);
        }

        if (installData.isInstallSuccess())
        {
            console.println("Installation was successful");
            console.println("application installed on " + installData.getInstallPath());
        }
        else
        {
            console.println("Install Failed!!!");
        }
        return true;
    }

    private boolean doGenerateAutoInstallScript()
    {
        return (factory != null && matcher != null && prompt != null);
    }

    private void generateAutoInstallScript(InstallData installData, UninstallData uninstallData, Console console)
    {
        Option userAnswer;
        userAnswer = prompt.confirm(Type.QUESTION, installData.getMessages()
                .get("FinishPanel.auto"), Options.YES_NO);

        if (userAnswer == Option.YES)
        {
            String parentPath;
            parentPath = installData.getVariable("INSTALL_PATH");

            if (parentPath == null)
            {
                parentPath = installData.getVariable("USER_HOME");
            }

            File file;
            file = new File(parentPath, AUTO_INSTALL_SCRIPT_NAME);

            String filePath;
            filePath = console.promptLocation("Select the installation script (path must be absolute)["
                    + file.getAbsolutePath() + "]", file.getAbsolutePath(), null);

            File newFile;
            newFile = new File(filePath);

            if (!newFile.isAbsolute())
            {
                /*
                 * Path must be absolute otherwise when the installer is embedded in a shell script
                 * (e.g. with launch4j), the autoInstall script is generated in the /tmp directory
                 * of the installer
                 */
                console.println("path of the installation script must be absolute");
                promptRerunPanel(installData, console);
            }
            else
            {
                generateAutoInstallScript(newFile, installData, uninstallData, console);
            }
        }
    }

    private void generateAutoInstallScript(final File file, final InstallData installData,
            final UninstallData uninstallData, final Console console)
    {
        try
        {
            parent.writeInstallationRecord(file, uninstallData);
        }
        catch (Exception err)
        {
            console.println("failed to save the installation into file [" + file.getAbsolutePath() + "]");
        }
    }

    protected AutomatedPanels getAutomatedPanels(final InstallData aInstallData)
    {
        AutomatedPanelsProvider provider;
        provider = new AutomatedPanelsProvider();

        AutomatedPanels automatedPanels;
        automatedPanels = provider.provide(factory, (AutomatedInstallData) aInstallData,
                new ConsolePanelAutomationHelper(), matcher);

        return automatedPanels;
    }

    protected void makeXML(final AutomatedPanelView panelView, final InstallData installData,
            final IXMLElement root)
    {
        try
        {
            panelView.getView().createInstallationRecord(installData, root);

        }
        catch (Exception e)
        {
            // some panels have no Automated counter-part
            LOGGER.warning("Unsupported panel " + panelView.getPanel().getClassName()
                    + ": no automated helper associated?");
            prompt.warn(e.getMessage());
        }
    }
}

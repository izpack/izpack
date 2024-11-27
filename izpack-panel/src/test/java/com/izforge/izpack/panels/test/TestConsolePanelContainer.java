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
package com.izforge.izpack.panels.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.ConsolePrefs;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.handler.ConsolePrompt;
import com.izforge.izpack.installer.automation.AutomatedPanels;
import com.izforge.izpack.installer.automation.PanelAutomationHelper;
import com.izforge.izpack.installer.console.ConsolePanelAutomationHelper;
import com.izforge.izpack.installer.container.provider.AutomatedPanelsProvider;
import com.izforge.izpack.installer.container.provider.MessagesProvider;
import com.izforge.izpack.installer.data.ConsoleInstallData;
import com.izforge.izpack.installer.panel.Panels;
import com.izforge.izpack.test.provider.ConsoleInstallDataMockProvider;
import com.izforge.izpack.test.util.TestConsole;
import com.izforge.izpack.util.Console;

/**
 * Container for testing console panels.
 *
 * @author Tim Anderson
 */
public class TestConsolePanelContainer extends AbstractTestPanelContainer {

    /**
     * Invoked by {@link #initialise} to fill the container.
     *
     * @throws ContainerException if initialisation fails
     */
    @Override
    protected void fillContainer() {
        super.fillContainer();
        addProvider(Messages.class, MessagesProvider.class);
        addProvider(ConsoleInstallData.class, ConsoleInstallDataMockProvider.class);
        addProvider(InstallData.class, ConsoleInstallDataMockProvider.class);
        addProvider(AutomatedInstallData.class, ConsoleInstallDataMockProvider.class);
        addProvider(ConsolePrefs.class, ConsolePrefsProvider.class);
        addComponent(Console.class, TestConsole.class);
        addComponent(Prompt.class, ConsolePrompt.class);
        addComponent(ConsolePrompt.class);
        addProvider(AutomatedPanels.class, AutomatedPanelsProvider.class);
        addComponent(PanelAutomationHelper.class, ConsolePanelAutomationHelper.class);

//        getComponent(RulesEngine.class); // force creation of the rules
    }

    private static class ConsolePrefsProvider implements Provider<ConsolePrefs> {

        private final ConsoleInstallData installData;

        @Inject
        public ConsolePrefsProvider(ConsoleInstallData installData) {
            this.installData = installData;
        }

        @Override
        public ConsolePrefs get() {
            return installData.consolePrefs;
        }
    }
}

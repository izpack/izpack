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

package com.izforge.izpack.panels.userinput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.core.data.DynamicVariableImpl;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.installer.console.ConsoleInstallAction;
import com.izforge.izpack.installer.console.ConsolePanelView;
import com.izforge.izpack.installer.console.ConsolePanels;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.panels.test.TestConsolePanelContainer;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.test.util.TestConsole;


/**
 * Tests the {@link UserInputPanelConsole}.
 *
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@Container(TestConsolePanelContainer.class)
public class UserInputPanelConsoleTest
{

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * The factory for creating panels.
     */
    private final ObjectFactory factory;

    /**
     * The resources.
     */
    private final ResourceManager resources;

    /**
     * The console.
     */
    private final TestConsole console;

    /**
     * The container.
     */
    private final TestConsolePanelContainer container;


    /**
     * Constructs an {@code UserInputPanelConsoleHelperTest}.
     *
     * @param installData the installation data
     * @param factory     the factory for creating panels
     * @param resources   the resources
     * @param console     the console
     * @param container   the container
     */
    public UserInputPanelConsoleTest(InstallData installData, ObjectFactory factory, ResourceManager resources,
                                     TestConsole console, TestConsolePanelContainer container)
    {
        this.installData = installData;
        this.factory = factory;
        this.resources = resources;
        this.console = console;
        this.container = container;
    }

    /**
     * Tests rule fields.
     *
     * @throws Exception for any error
     */
    @Test
    public void testRuleField() throws Exception
    {
        // Set the base path in order to pick up com/izforge/izpack/panels/userinput/rule/userInputSpec.xml
        resources.setResourceBasePath("/com/izforge/izpack/panels/userinput/rule/");

        ConsolePanels panels = createPanels(UserInputPanel.class, "ruleinput");

        console.addScript("rule1", "127.0.0.1");
        console.addScript("Continue", "1");

        assertTrue(panels.next());

        assertEquals("127.0.0.1", installData.getVariable("rule1"));
    }

    /**
     * Tests text fields.
     */
    @Test
    public void testTextFields()
    {
        // Set the base path in order to pick up com/izforge/izpack/panels/userinput/text/userInputSpec.xml
        resources.setResourceBasePath("/com/izforge/izpack/panels/userinput/text/");

        installData.setVariable("text3value", "text3 default value");

        ConsolePanels panels = createPanels(UserInputPanel.class, "textinput");

        console.addScript("text1", "text1 value");
        console.addScript("text2", "\n");
        console.addScript("text3", "\n");
        console.addScript("Continue", "1");

        assertTrue(panels.next());

        assertEquals("text1 value", installData.getVariable("text1"));
        assertEquals("text2 value", installData.getVariable("text2"));
        assertEquals("text3 default value", installData.getVariable("text3"));
    }

    /**
     * Verifies that dynamic variables are refreshed when the panel is validated.
     *
     * @throws Exception for any error
     */
    @Test
    public void testRefreshDynamicVariables() throws Exception
    {
        resources.setResourceBasePath("/com/izforge/izpack/panels/userinput/refresh/");

        // create a variable to be used in userInputSpec.xml to set a default value for the address field
        installData.setVariable("defaultAddress", "localhost");

        // create a dynamic variable that will be updated with the value of the address field
        installData.getVariables().add(new DynamicVariableImpl("dynamicMasterAddress", "${address}"));

        ConsolePanels panels = createPanels(UserInputPanel.class, "userinputAddress");

        console.addScript("Select address", "myhost");
        console.addScript("Continue", "1");

        assertTrue(panels.next());

        assertEquals("myhost", installData.getVariable("address"));
        assertEquals("${address}", installData.getVariable("dynamicMasterAddress"));

        assertTrue(panels.isValid());
        assertEquals("myhost", installData.getVariable("dynamicMasterAddress"));
    }

    /**
     * Creates a {@code ConsolePanels} containing an instance of the console version of the supplied panel
     * implementation.
     *
     * @param panelClass the panel class
     * @param id         the panel identifier
     * @return a new {@code ConsolePanels}
     */
    private ConsolePanels createPanels(Class panelClass, String id)
    {
        Panel panel = new Panel();
        panel.setClassName(panelClass.getName());
        panel.setPanelId(id);
        ConsolePanelView panelView = new ConsolePanelView(panel, factory, installData, console);
        ConsolePanels panels = new ConsolePanels(Arrays.asList(panelView), installData.getVariables());
        container.addComponent(ConsolePanels.class, panels);
        panels.setAction(new ConsoleInstallAction(console, installData, mock(UninstallDataWriter.class)));
        return panels;
    }
}

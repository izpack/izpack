/*
 * IzPack - Copyright 2024 Hitesh A. Bosamiya, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2024 Hitesh A. Bosamiya
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
package com.izforge.izpack.panels.installationtype;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Overrides;
import com.izforge.izpack.panels.test.TestConsolePanelContainer;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import jakarta.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests the {@link InstallationTypePanelAutomation} class.
 *
 * @author Hitesh A. Bosamiya
 */
@RunWith(PicoRunner.class)
@Container(TestConsolePanelContainer.class)
public class InstallationTypePanelAutomationTest
{
    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * Constructs a {@code TargetPanelAutomationTest}.
     *
     * @param installData the installation data
     */
    @Inject
    public InstallationTypePanelAutomationTest(InstallData installData)
    {
        this.installData = installData;
    }

    /**
     * Verifies new installation. createInstallationRecord and runAutomated methods get tested here!
     */
    @Test
    public void testNewInstallation()
    {
        installData.setVariable(InstallData.MODIFY_INSTALLATION, "false");
        InstallationTypePanelAutomation panel = new InstallationTypePanelAutomation();
        IXMLElement root = new XMLElementImpl("root");
        panel.createInstallationRecord(installData, root);
        // Before running the method runAutomated, we set the variable "modify.izpack.install" to true. So that we can
        // make sure that it is set back to false only by runAutomated
        installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");
        panel.runAutomated(installData, root);
        assertEquals("false", installData.getVariable(InstallData.MODIFY_INSTALLATION));
    }

    /**
     * Verifies new installation. createInstallationRecord and runAutomated methods get tested here!
     */
    @Test
    public void testModifyInstallation()
    {
        installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");
        InstallationTypePanelAutomation panel = new InstallationTypePanelAutomation();
        IXMLElement root = new XMLElementImpl("root");
        panel.createInstallationRecord(installData, root);
        // Before running the method runAutomated, we set the variable "modify.izpack.install" to false. So that we can
        // make sure that it is set back to true only by runAutomated
        installData.setVariable(InstallData.MODIFY_INSTALLATION, "false");
        panel.runAutomated(installData, root);
        assertEquals("true", installData.getVariable(InstallData.MODIFY_INSTALLATION));
    }

    /**
     * Verifies new installation when installation record does not have modifyInstallation element
     */
    @Test
    public void testInstallationTypeNoElement()
    {
        InstallationTypePanelAutomation panel = new InstallationTypePanelAutomation();
        IXMLElement root = new XMLElementImpl("root");
        panel.runAutomated(installData, root);
        assertNull(installData.getVariable(InstallData.MODIFY_INSTALLATION));
    }

    /**
     * Verifies new installation when no overrides specified and the variable "modify.izpack.install" is not set.
     */
    @Test
    public void testProcessOptionNoOverride()
    {
        Overrides overrides = Mockito.mock(Overrides.class);
        InstallationTypePanelAutomation panel = new InstallationTypePanelAutomation();
        panel.processOptions(installData, overrides);
        assertNull(installData.getVariable(InstallData.MODIFY_INSTALLATION));
    }

    /**
     * Verifies new installation when no overrides specified but the variable "modify.izpack.install" is set to false.
     */
    @Test
    public void testProcessOptionNoOverrideNewInstallation()
    {
        installData.setVariable(InstallData.MODIFY_INSTALLATION, "false");
        Overrides overrides = Mockito.mock(Overrides.class);
        InstallationTypePanelAutomation panel = new InstallationTypePanelAutomation();
        panel.processOptions(installData, overrides);
        assertEquals("false", installData.getVariable(InstallData.MODIFY_INSTALLATION));
    }

    /**
     * Verifies new installation when no overrides specified but the variable "modify.izpack.install" is set to true.
     */
    @Test
    public void testProcessOptionNoOverrideModifyInstallation()
    {
        installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");
        Overrides overrides = Mockito.mock(Overrides.class);
        InstallationTypePanelAutomation panel = new InstallationTypePanelAutomation();
        panel.processOptions(installData, overrides);
        assertEquals("true", installData.getVariable(InstallData.MODIFY_INSTALLATION));
    }

    /**
     * Verifies new installation when overrides sets "modify.izpack.install" to true.
     */
    @Test
    public void testProcessOptionOverrideIsFalse()
    {
        Overrides overrides = Mockito.mock(Overrides.class);
        Mockito.when(overrides.fetch(InstallData.MODIFY_INSTALLATION)).thenReturn("false");
        InstallationTypePanelAutomation panel = new InstallationTypePanelAutomation();
        panel.processOptions(installData, overrides);
        assertEquals("false", installData.getVariable(InstallData.MODIFY_INSTALLATION));
    }

    /**
     * Verifies new installation when overrides sets "modify.izpack.install" to true.
     */
    @Test
    public void testProcessOptionOverrideIsTrue()
    {
        Overrides overrides = Mockito.mock(Overrides.class);
        Mockito.when(overrides.fetch(InstallData.MODIFY_INSTALLATION)).thenReturn("true");
        InstallationTypePanelAutomation panel = new InstallationTypePanelAutomation();
        panel.processOptions(installData, overrides);
        assertEquals("true", installData.getVariable(InstallData.MODIFY_INSTALLATION));
    }

    /**
     * Verifies new installation when overrides sets "modify.izpack.install" to "something".
     */
    @Test
    public void testProcessOptionOverrideIsSomething()
    {
        Overrides overrides = Mockito.mock(Overrides.class);
        Mockito.when(overrides.fetch(InstallData.MODIFY_INSTALLATION)).thenReturn("something");
        InstallationTypePanelAutomation panel = new InstallationTypePanelAutomation();
        panel.processOptions(installData, overrides);
        assertEquals("false", installData.getVariable(InstallData.MODIFY_INSTALLATION));
    }
}

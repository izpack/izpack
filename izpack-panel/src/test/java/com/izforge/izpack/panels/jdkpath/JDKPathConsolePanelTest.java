/*
 * IzPack - Copyright 2001-2026 Julien Ponge, All Rights Reserved.
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
package com.izforge.izpack.panels.jdkpath;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.panels.test.TestConsolePanelContainer;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.test.util.TestConsole;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static org.junit.Assert.*;

/**
 * Tests for JDKPathConsolePanel.
 */
@RunWith(PicoRunner.class)
@Container(TestConsolePanelContainer.class)
public class JDKPathConsolePanelTest
{
    private final InstallData installData;
    private final TestConsole console;

    public JDKPathConsolePanelTest(InstallData installData, TestConsole console)
    {
        this.console = console;
        this.installData = installData;
    }

    @Test
    public void testJDKPathConsolePanelRun() throws Exception
    {
        // Setup mock JDK
        java.io.File tempDir = new java.io.File(System.getProperty("java.io.tmpdir"), "test-jdk");
        java.io.File binDir = new java.io.File(tempDir, "bin");
        binDir.mkdirs();
        java.io.File javac = new java.io.File(binDir, "javac");
        javac.createNewFile();
        java.io.File java = new java.io.File(binDir, "java");
        java.io.FileWriter writer = new java.io.FileWriter(java);
        writer.write("#!/bin/sh\necho \"java version 1.8.0_202\"");
        writer.close();
        java.setExecutable(true);

        RegistryDefaultHandler registryDefaultHandler = Mockito.mock(RegistryDefaultHandler.class);
        VariableSubstitutor variableSubstitutor = Mockito.mock(VariableSubstitutor.class);
        JDKPathConsolePanel panel = new JDKPathConsolePanel(variableSubstitutor, registryDefaultHandler, null, installData);
        
        // Mock inputs
        console.addScript("test", tempDir.getAbsolutePath(), "\n", "1", "\n");
        
        assertTrue(panel.run(installData, console));
        assertEquals(tempDir.getAbsolutePath(), installData.getVariable(JDKPathPanelHelper.JDK_PATH));
        
        // Cleanup
        java.delete();
        javac.delete();
        binDir.delete();
        tempDir.delete();
    }
}

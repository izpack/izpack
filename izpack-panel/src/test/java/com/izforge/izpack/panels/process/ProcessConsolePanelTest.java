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
package com.izforge.izpack.panels.process;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.panels.test.TestConsolePanelContainer;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.test.util.TestConsole;
import com.izforge.izpack.util.PlatformModelMatcher;


/**
 * Tests the {@link ProcessConsolePanel} class.
 * TODO - this only covers a fraction of ProcessPanel functionality.
 *
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@Container(TestConsolePanelContainer.class)
public class ProcessConsolePanelTest
{

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * The rules.
     */
    private final RulesEngine rules;

    /**
     * The resources.
     */
    private final ResourceManager resources;

    /**
     * The prompt.
     */
    private final Prompt prompt;

    /**
     * The platform-model matcher.
     */
    private final PlatformModelMatcher matcher;

    /**
     * The console.
     */
    private final TestConsole console;

    /**
     * Constructs a {@code ProcessConsolePanelTest}.
     *
     * @param installData the installation data
     * @param rules       the rules
     * @param resources   the resources
     * @param prompt      the prompt
     * @param matcher     the platform-model matcher
     * @param console     the console
     */
    public ProcessConsolePanelTest(InstallData installData, RulesEngine rules, ResourceManager resources,
                                   Prompt prompt, PlatformModelMatcher matcher, TestConsole console)
    {
        this.installData = installData;
        this.rules = rules;
        this.resources = resources;
        this.prompt = prompt;
        this.matcher = matcher;
        this.console = console;
        resources.setResourceBasePath("/com/izforge/izpack/panels/process/");
    }

    /**
     * Tests a job with <em>executeclass</em> elements.
     */
    @Test
    public void testExecuteClass()
    {
        Executable.init();
        Executable.setReturn(true);

        ProcessConsolePanel panel = new ProcessConsolePanel(rules, resources, prompt, matcher, null);
        assertTrue(panel.run(installData, console));

        // verify Executable was run the expected no. of times, with the expected arguments
        assertEquals(2, Executable.getInvocations());
        assertArrayEquals(Executable.getArgs(0), new String[]{"run0"});
        assertArrayEquals(Executable.getArgs(1), new String[]{"run1", "somearg"});
    }

    /**
     * Verifies that an error is displayed if the specified <em>executeclass</em> throws an exception.
     *
     * @throws Exception for any error
     */
    @Test
    public void testExecuteClassException() throws Exception
    {
        Executable.init();
        Executable.setException(true);

        ProcessConsolePanel panel = new ProcessConsolePanel(rules, resources, prompt, matcher, null);
        assertFalse(panel.run(installData, console));

        assertEquals(3, console.getOutput().size());
        assertTrue(console.getOutput().get(2).equals(
                "Invocation Problem calling : com.izforge.izpack.panels.process.Executable, Executable exception"));

        // verify Executable was run the expected no. of times, with the expected arguments
        assertEquals(1, Executable.getInvocations());
        assertArrayEquals(Executable.getArgs(0), new String[]{"run0"});
    }

}

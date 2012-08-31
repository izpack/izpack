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
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.panels.test.TestConsolePanelContainer;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.util.PlatformModelMatcher;

/**
 * Tests the {@link ProcessPanelAutomation} class.
 * TODO - this only covers a fraction of ProcessPanel functionality.
 *
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@Container(TestConsolePanelContainer.class)
public class ProcessPanelAutomationTest
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
     * The platform-model matcher.
     */
    private final PlatformModelMatcher matcher;


    /**
     * Constructs a {@code ProcessPanelAutomationTest}.
     *
     * @param installData the installation data
     * @param rules       the rules
     * @param resources   the resources
     * @param matcher     the platform-model matcher
     */
    public ProcessPanelAutomationTest(InstallData installData, RulesEngine rules, ResourceManager resources,
                                      PlatformModelMatcher matcher)
    {
        this.installData = installData;
        this.rules = rules;
        this.resources = resources;
        this.matcher = matcher;
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

        ProcessPanelAutomation panel = new ProcessPanelAutomation(installData, rules, resources, matcher);
        panel.runAutomated(installData, new XMLElementImpl("root"));   // XML element not used

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

        ProcessPanelAutomation panel = new ProcessPanelAutomation(installData, rules, resources, matcher);
        try
        {
            panel.runAutomated(installData, new XMLElementImpl("root"));   // XML element not used
            fail("InstallerException not thrown");
        }
        catch (InstallerException expected)
        {
            // expected behaviour
        }

        // verify Executable was run the expected no. of times, with the expected arguments
        assertEquals(1, Executable.getInvocations());
        assertArrayEquals(Executable.getArgs(0), new String[]{"run0"});
    }


}

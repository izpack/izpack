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

package com.izforge.izpack.installer.requirement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.core.handler.ConsolePrompt;
import com.izforge.izpack.installer.data.InstallData;
import com.izforge.izpack.test.util.TestConsole;
import com.izforge.izpack.util.Platforms;

/**
 * Tests the {@link JavaVersionChecker} class.
 *
 * @author Tim Anderson
 */
public class JavaVersionCheckerTest
{
    /**
     * The installation data.
     */
    private InstallData installData;

    /**
     * Constructs a <tt>JavaVersionCheckerTest</tt>.
     */
    public JavaVersionCheckerTest()
    {
        installData = new InstallData(null, Platforms.LINUX);
        Info info = new Info();
        installData.setInfo(info);
    }

    /**
     * Tests the {@link JavaVersionChecker}.
     */
    @Test
    public void testJavaVersion()
    {
        TestConsole console = new TestConsole();
        ConsolePrompt prompt = new ConsolePrompt(console);
        JavaVersionChecker checker = new JavaVersionChecker(installData, prompt);

        installData.getInfo().setJavaVersion(null);
        assertTrue(checker.check());

        String currentVersion = System.getProperty("java.version");
        installData.getInfo().setJavaVersion("9" + currentVersion);
        assertFalse(checker.check());

        installData.getInfo().setJavaVersion(currentVersion);
        assertTrue(checker.check());
    }
}

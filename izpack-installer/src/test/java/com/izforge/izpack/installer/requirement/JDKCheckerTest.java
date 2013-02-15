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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.core.handler.ConsolePrompt;
import com.izforge.izpack.test.util.TestConsole;
import com.izforge.izpack.util.FileExecutor;

/**
 * Tests the {@link JDKChecker} class.
 *
 * @author Tim Anderson
 */
public class JDKCheckerTest extends AbstractRequirementCheckerTest
{
    /**
     * Tests the {@link JDKChecker} when the JDK is not required.
     */
    @Test
    public void testNotRequired()
    {
        TestJDKChecker checker = new TestJDKChecker(installData, prompt);

        installData.getInfo().setJdkRequired(false);
        checker.setExists(true);
        assertTrue(checker.check());
        checker.setExists(false);
        assertTrue(checker.check());
    }

    /**
     * Tests the {@link JDKChecker} when the JDK is required.
     */
    @Test
    public void testRequired()
    {
        TestJDKChecker checker = new TestJDKChecker(installData, prompt);

        installData.getInfo().setJdkRequired(true);
        checker.setExists(true);
        assertTrue(checker.check());

        console.addScript("NoJDK-enter-N", "n");
        checker.setExists(false);
        assertFalse(checker.check());
        assertTrue(console.scriptCompleted());

        // re-run the check, but this time enter Y to continue
        console.addScript("NoJDK-enter-Y", "y");
        assertTrue(checker.check());
    }

    /**
     * Verifies that when the JDK is required, the {@link JDKChecker} determines the existence of the JDK correctly.
     */
    @Test
    public void testLocalJDKInstallation()
    {
        String[] output = new String[2];
        int code = new FileExecutor().executeCommand(new String[]{"javac", "-help"}, output, null);
        boolean exists = (code == 0); // exists if javac is in the path
        installData.getInfo().setJdkRequired(true);

        TestConsole console = new TestConsole();
        ConsolePrompt prompt = new ConsolePrompt(console, installData.getMessages());
        JDKChecker checker = new JDKChecker(installData, prompt);
        assertEquals(exists, checker.check());
    }

    private static class TestJDKChecker extends JDKChecker
    {
        /**
         * Determines if the JDK exists.
         */
        private boolean exists = true;

        public TestJDKChecker(AutomatedInstallData installData, Prompt prompt)
        {
            super(installData, prompt);
        }

        /**
         * Determines if the JDK exists.
         *
         * @param exists <tt>true</tt> if the JDK exists, otherwise <tt>false</tt>
         */
        public void setExists(boolean exists)
        {
            this.exists = exists;
        }

        @Override
        protected boolean exists()
        {
            return exists;
        }
    }
}

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

import java.io.File;

import org.junit.Test;

import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.core.handler.ConsolePrompt;
import com.izforge.izpack.installer.data.InstallData;
import com.izforge.izpack.test.util.TestConsole;
import com.izforge.izpack.util.FileUtil;
import com.izforge.izpack.util.Platforms;

/**
 * Tests the {@link LockFileChecker} class.
 *
 * @author Tim Anderson
 */
public class LockFileCheckerTest
{
    /**
     * The installation data.
     */
    private InstallData installData;

    /**
     * Constructs a <tt>LockFileCheckerTest</tt>.
     */
    public LockFileCheckerTest()
    {
        installData = new InstallData(null, Platforms.UNIX);
        Info info = new Info();
        installData.setInfo(info);
    }

    /**
     * Tests the {@link LockFileChecker}.
     */
    @Test
    public void testLockFile()
    {
        String appName = "TestApp" + System.currentTimeMillis();
        installData.getInfo().setAppName(appName);
        TestConsole console = new TestConsole();
        ConsolePrompt prompt = new ConsolePrompt(console);
        LockFileChecker checker = new LockFileChecker(installData, prompt);

        // no lock file yet.
        assertTrue(checker.check());

        // lock file should now exist. Enter n to cancel
        console.addScript("LockFileExists-enter-N", "n");
        assertFalse(checker.check());

        // rerun the check, this time selecting Y to continue
        console.addScript("LockFileExists-enter-Y", "y");
        assertTrue(checker.check());

        // now delete the lock file and verify the check returns true
        File file = FileUtil.getLockFile(appName);
        assertTrue(file.delete());
        assertTrue(checker.check());
    }
}

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

import static com.izforge.izpack.util.FileUtil.getLockFile;
import static java.lang.System.currentTimeMillis;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link LockFileChecker} class.
 *
 * @author Tim Anderson
 */
public class LockFileCheckerTest extends AbstractRequirementCheckerTest
{

    /**
     * Tests the {@link LockFileChecker}.
     */
    @Test
    public void testLockFile()
    {
        String appName = "TestApp" + currentTimeMillis();
        installData.getInfo().setAppName(appName);
        LockFileChecker checker = new LockFileChecker(installData, prompt);

        // no lock file yet.
        assertThat(checker.check()).isTrue();

        // lock file should now exist. Enter n to cancel
        console.addScript("LockFileExists-enter-N", "n");
        assertThat(checker.check()).isFalse();

        // rerun the check, this time selecting Y to continue
        console.addScript("LockFileExists-enter-Y", "y");
        assertThat(checker.check()).isTrue();

        // now delete the lock file and verify the check returns true
        File file = getLockFile(appName);
        assertThat(file.delete()).isTrue();
        assertThat(checker.check()).isTrue();
    }
}

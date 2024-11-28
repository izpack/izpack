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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.apache.commons.lang3.StringUtils;

/**
 * Tests the {@link JavaVersionChecker} class.
 *
 * @author Tim Anderson
 */
public class JavaVersionCheckerTest extends AbstractRequirementCheckerTest
{
    /**
     * Tests the {@link JavaVersionChecker}.
     */
    @Test
    public void testJavaVersion()
    {
        JavaVersionChecker checker = new JavaVersionChecker(installData, prompt);

        installData.getInfo().setJavaVersion(null);
        assertTrue(checker.check());

        String currentVersion = System.getProperty("java.version");
        installData.getInfo().setJavaVersion("9" + currentVersion);
        assertFalse(checker.check());

        installData.getInfo().setJavaVersion(currentVersion);
        assertTrue(checker.check());

        // in case of OpenJDK, version number is e.g 1.8.0_102-redhat
        // therefore the version must increased to
        // 1.8.0_1029 instead of 1.8.0_102-redhat9
        String[] parts = currentVersion.split("-|_|\\.");
        int pos=0;
        // find the last part of version with numeric parts
        for(int i=0; i<parts.length; i++){
            if(isNumeric(parts[i])){
                pos=i;
            } else{
                break;
            }
        }
        // and add the "9" on it
        parts[pos]=parts[pos].concat("9");
        installData.getInfo().setJavaVersion(StringUtils.join(parts,"."));
        assertFalse(checker.check());

        String[] splitCurrentVersion = currentVersion.split("_");
        installData.getInfo().setJavaVersion(splitCurrentVersion[0]);
        assertTrue(checker.check());
    }

    private boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}

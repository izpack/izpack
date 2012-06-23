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
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.Returns;

import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.installer.RequirementChecker;

/**
 * Tests the {@link RequirementsChecker} class.
 *
 * @author Tim Anderson
 */
public class RequirementsCheckerTest
{
    /**
     * Tests the {@link RequirementsChecker#check()} method when all {@link RequirementChecker} implementations
     * return <tt>true</tt>.
     */
    @Test
    public void testCheckSuccess()
    {
        Variables variables = Mockito.mock(Variables.class);
        RequirementChecker checker = new RequirementsChecker(variables, mock(LangPackChecker.class, true),
                                                             mock(JavaVersionChecker.class, true),
                                                             mock(JDKChecker.class, true),
                                                             mock(LockFileChecker.class, true),
                                                             mock(InstallerRequirementChecker.class, true));
        assertTrue(checker.check());
    }


    /**
     * Tests the {@link RequirementsChecker#check()} method when one of the {@link RequirementChecker} implementations
     * returns <tt>false</tt>.
     */
    @Test
    public void testCheckFailure()
    {
        Variables variables = Mockito.mock(Variables.class);
        for (int i = 0; i < 5; ++i)
        {
            LangPackChecker langChecker = mock(LangPackChecker.class, (i != 0));
            JavaVersionChecker javaChecker = mock(JavaVersionChecker.class, (i != 1));
            JDKChecker jdkChecker = mock(JDKChecker.class, (i != 2));
            LockFileChecker lockChecker = mock(LockFileChecker.class, (i != 3));
            InstallerRequirementChecker requirementChecker = mock(InstallerRequirementChecker.class, (i != 4));

            RequirementsChecker checker2 = new RequirementsChecker(variables, langChecker, javaChecker, jdkChecker,
                                                                   lockChecker, requirementChecker);
            assertFalse(checker2.check());
        }
    }

    /**
     * Mocks a RequirementChecker that returns the specified value.
     *
     * @param clazz  the class to mock
     * @param result the value to return
     * @return the mocked instance
     */
    private <T extends RequirementChecker> T mock(Class<T> clazz, boolean result)
    {
        return Mockito.mock(clazz, new Returns(result));
    }
}

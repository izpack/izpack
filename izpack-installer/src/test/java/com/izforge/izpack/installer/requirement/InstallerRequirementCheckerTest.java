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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.izforge.izpack.api.data.InstallerRequirement;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.rules.RulesEngineImpl;
import com.izforge.izpack.core.rules.logic.NotCondition;
import com.izforge.izpack.core.rules.process.JavaCondition;

/**
 * Tests the {@link InstallerRequirementChecker} class.
 *
 * @author Tim Anderson
 */
public class InstallerRequirementCheckerTest extends AbstractRequirementCheckerTest
{
    /**
     * The rules.
     */
    private RulesEngine rules;

    /**
     * Constructs a <tt>InstallerRequirementCheckerTest</tt>.
     */
    public InstallerRequirementCheckerTest()
    {
        DefaultVariables variables = new DefaultVariables();
        installData.setInstallerRequirements(new ArrayList<InstallerRequirement>());
        rules = new RulesEngineImpl(installData, null, installData.getPlatform());
        variables.setRules(rules);

        Map<String, Condition> conditions = new HashMap<String, Condition>();
        Condition alwaysFalse = new JavaCondition();
        conditions.put("false", alwaysFalse);

        Condition alwaysTrue = NotCondition.createFromCondition(alwaysFalse, rules);
        conditions.put("true", alwaysTrue);

        rules.readConditionMap(conditions);
    }

    /**
     * Tests the {@link InstallerRequirementChecker}.
     */
    @Test
    public void testInstallerRequirementChecker()
    {
        InstallerRequirementChecker checker = new InstallerRequirementChecker(installData, rules, prompt);

        // no requirements - should evaluate true
        assertTrue(checker.check());

        // add a requirement that always evaluates false
        InstallerRequirement req1 = new InstallerRequirement();
        req1.setCondition("false");
        req1.setMessage("requirement1 = always false");
        installData.getInstallerRequirements().add(req1);

        // should evaluate false
        assertFalse(checker.check());

        // add a requirement that always evaluates true
        InstallerRequirement req2 = new InstallerRequirement();
        req2.setCondition("true");
        req2.setMessage("requirement2 = always true");
        installData.getInstallerRequirements().add(req2);

        // should still evaluate false, due to presence of req1
        assertFalse(checker.check());

        // remove req1 and verify evaluates true
        installData.getInstallerRequirements().remove(req1);
        assertTrue(checker.check());
    }
}

/*
 * IzPack - Copyright 2024 Hitesh A. Bosamiya, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2024 Hitesh A. Bosamiya
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
package com.izforge.izpack.panels.datacheck;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link DataCheckCommon} class.
 *
 * @author Hitesh A. Bosamiya
 */
public class DataCheckCommonTest
{
    /**
     * Verifies DataCheckCommon.testGetMainLabelWithDashes method.
     */
    @Test
    public void testGetMainLabelWithDashes()
    {
        String output = DataCheckCommon.getMainLabelWithDashes(0, "myPanel");
        verifyOutput(output, "------------------------", "0", "myPanel");
    }

    /**
     * Verifies DataCheckCommon.getMainLabel method.
     */
    @Test
    public void testGetMainLabel()
    {
        String output = DataCheckCommon.getMainLabel(0, "myPanel");
        verifyOutput(output, "Data Check Panel, instance: ", "0", "myPanel");
    }

    /**
     * Verifies DataCheckCommon.getInstallDataVariables method.
     */
    @Test
    public void testGetInstallDataVariables()
    {
        InstallData installData = Mockito.mock(InstallData.class);
        mockVariables(installData);

        String output = DataCheckCommon.getInstallDataVariables(installData);

        verifyOutput(output, "InstallData Variables:", "Variable1", "Variable2");
    }

    /**
     * Verifies DataCheckCommon.getPackNames method.
     */
    @Test
    public void testGetPackNames()
    {
        InstallData installData = Mockito.mock(InstallData.class);
        mockPacks(installData);

        String output = DataCheckCommon.getPackNames(installData);

        verifyOutput(output, "Available Packs:", "Pack1 (Selected)", "Pack2 (Unselected)");
    }

    /**
     * Verifies DataCheckCommon.getConditions method.
     */
    @Test
    public void testGetConditions()
    {
        InstallData installData = Mockito.mock(InstallData.class);
        mockConditions(installData);

        String output = DataCheckCommon.getConditions(installData);

        verifyOutput(output, "Conditions:", "condition1 is true", "condition2 is false");
    }

    private void mockVariables(InstallData installData) {
        Variables variables = Mockito.mock(Variables.class);
        Mockito.when(installData.getVariables()).thenReturn(variables);
        Properties properties = new Properties();
        properties.setProperty("Variable1", "Value1");
        properties.setProperty("Variable2", "Value2");
        Mockito.when(variables.getProperties()).thenReturn(properties);
    }

    private void mockPacks(InstallData installData) {
        Pack pack1 = new Pack("Pack1", null, null, null, null, true, true, false, null, false, 0);
        Pack pack2 = new Pack("Pack2", null, null, null, null, false, true, false, null, false, 0);
        List<Pack> packList = new ArrayList<>();
        packList.add(pack1);
        packList.add(pack2);
        Mockito.when(installData.getAllPacks()).thenReturn(packList);
        List<Pack> selectedPackList = new ArrayList<>();
        selectedPackList.add(pack1);
        Mockito.when(installData.getSelectedPacks()).thenReturn(selectedPackList);
    }

    private void mockConditions(InstallData installData) {
        Set<String> conditionIds = new HashSet<>();
        conditionIds.add("condition1");
        conditionIds.add("condition2");
        RulesEngine rules = Mockito.mock(RulesEngine.class);
        Mockito.when(rules.getKnownConditionIds()).thenReturn(conditionIds);
        Condition condition1 = Mockito.mock(Condition.class);
        Mockito.when(condition1.getId()).thenReturn("condition1");
        Mockito.when(condition1.isTrue()).thenReturn(true);
        Mockito.when(rules.getCondition("condition1")).thenReturn(condition1);
        Condition condition2 = Mockito.mock(Condition.class);
        Mockito.when(condition2.getId()).thenReturn("condition2");
        Mockito.when(condition2.isTrue()).thenReturn(false);
        Mockito.when(rules.getCondition("condition2")).thenReturn(condition2);
        Mockito.when(installData.getRules()).thenReturn(rules);
    }

    public static void verifyOutput(String output, String prefix, String Variable1, String Variable2) {
        assertTrue(output.startsWith(prefix));
        assertTrue(output.contains(Variable1));
        assertTrue(output.contains(Variable2));
    }
}

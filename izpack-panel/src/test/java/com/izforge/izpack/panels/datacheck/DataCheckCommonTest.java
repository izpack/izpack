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

import static com.izforge.izpack.panels.datacheck.DataCheckCommon.getConditions;
import static com.izforge.izpack.panels.datacheck.DataCheckCommon.getInstallDataVariables;
import static com.izforge.izpack.panels.datacheck.DataCheckCommon.getMainLabel;
import static com.izforge.izpack.panels.datacheck.DataCheckCommon.getMainLabelWithDashes;
import static com.izforge.izpack.panels.datacheck.DataCheckCommon.getPackNames;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;
import java.util.*;
import org.junit.jupiter.api.Test;

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
        String output = getMainLabelWithDashes(0, "myPanel");
        verifyOutput(output, "------------------------", "0", "myPanel");
    }

    /**
     * Verifies DataCheckCommon.getMainLabel method.
     */
    @Test
    public void testGetMainLabel()
    {
        String output = getMainLabel(0, "myPanel");
        verifyOutput(output, "Data Check Panel, instance: ", "0", "myPanel");
    }

    /**
     * Verifies DataCheckCommon.getInstallDataVariables method.
     */
    @Test
    public void testGetInstallDataVariables()
    {
        InstallData installData = mock(InstallData.class);
        mockVariables(installData);

        String output = getInstallDataVariables(installData);

        verifyOutput(output, "InstallData Variables:", "Variable1", "Variable2");
    }

    /**
     * Verifies DataCheckCommon.getPackNames method.
     */
    @Test
    public void testGetPackNames()
    {
        InstallData installData = mock(InstallData.class);
        mockPacks(installData);

        String output = getPackNames(installData);

        verifyOutput(output, "Available Packs:", "Pack1 (Selected)", "Pack2 (Unselected)");
    }

    /**
     * Verifies DataCheckCommon.getConditions method.
     */
    @Test
    public void testGetConditions()
    {
        InstallData installData = mock(InstallData.class);
        mockConditions(installData);

        String output = getConditions(installData);

        verifyOutput(output, "Conditions:", "condition1 is true", "condition2 is false");
    }

    private void mockVariables(InstallData installData) {
        Variables variables = mock(Variables.class);
        when(installData.getVariables()).thenReturn(variables);
        Properties properties = new Properties();
        properties.setProperty("Variable1", "Value1");
        properties.setProperty("Variable2", "Value2");
        when(variables.getProperties()).thenReturn(properties);
    }

    private void mockPacks(InstallData installData) {
        Pack pack1 = new Pack("Pack1", null, null, null, null, true, true, false, null, false, 0);
        Pack pack2 = new Pack("Pack2", null, null, null, null, false, true, false, null, false, 0);
        List<Pack> packList = new ArrayList<>();
        packList.add(pack1);
        packList.add(pack2);
        when(installData.getAllPacks()).thenReturn(packList);
        List<Pack> selectedPackList = new ArrayList<>();
        selectedPackList.add(pack1);
        when(installData.getSelectedPacks()).thenReturn(selectedPackList);
    }

    private void mockConditions(InstallData installData) {
        Set<String> conditionIds = new HashSet<>();
        conditionIds.add("condition1");
        conditionIds.add("condition2");
        RulesEngine rules = mock(RulesEngine.class);
        when(rules.getKnownConditionIds()).thenReturn(conditionIds);
        Condition condition1 = mock(Condition.class);
        when(condition1.getId()).thenReturn("condition1");
        when(condition1.isTrue()).thenReturn(true);
        when(rules.getCondition("condition1")).thenReturn(condition1);
        Condition condition2 = mock(Condition.class);
        when(condition2.getId()).thenReturn("condition2");
        when(condition2.isTrue()).thenReturn(false);
        when(rules.getCondition("condition2")).thenReturn(condition2);
        when(installData.getRules()).thenReturn(rules);
    }

    public static void verifyOutput(String output, String prefix, String Variable1, String Variable2) {
        assertThat(output).startsWith(prefix);
        assertThat(output).contains(Variable1);
        assertThat(output).contains(Variable2);
    }
}

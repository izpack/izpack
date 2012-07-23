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

package com.izforge.izpack.panels.userinput.processorclient;

import static org.junit.Assert.assertEquals;

import java.awt.Toolkit;

import org.junit.Test;
import org.mockito.Mockito;

import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.util.Platforms;


/**
 * Tests the {@link RuleInputField}.
 *
 * @author Tim Anderson
 */
public class RuleInputFieldTest
{

    /**
     * Tests {@link RuleInputField} support for entering IP addresses.
     */
    @Test
    public void testIPAddressRuleInputField()
    {
        String layout = "N:3:3 . N:3:3 . N:3:3 . N:3:3"; // IP address format
        String set = "0:192 1:168 2:0 3:1";              // default value
        String separator = null;
        String validator = null;
        String processor = null;
        Toolkit toolkit = Mockito.mock(Toolkit.class);

        GUIInstallData installData = new GUIInstallData(new DefaultVariables(), Platforms.HP_UX);

        RuleInputField field = new RuleInputField(layout, set, separator, validator, processor,
                                                  RuleInputField.DISPLAY_FORMAT, toolkit, installData);

        assertEquals(4, field.getNumFields());

        // check default value
        assertEquals("192.168.0.1", field.getText());
        assertEquals("192", field.getFieldContents(0));
        assertEquals("168", field.getFieldContents(1));
        assertEquals("0", field.getFieldContents(2));
        assertEquals("1", field.getFieldContents(3));

        // TODO - need to provide methods to update fields and verify field formats
    }
}

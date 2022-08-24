/*
 * IzPack - Copyright 2021 Hitesh A. Bosamiya, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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
package com.izforge.izpack.com.izforge.izpack.installer.debugger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.installer.debugger.Debugger;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;


import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class DebuggerTest {

    public static void main(String[] args) {
        try {
            new DebuggerTest().testRemoveHTML();
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRemoveHTML() throws InterruptedException, InvocationTargetException {
        IconsDatabase icons = new IconsDatabase();
        Properties properties = new Properties();
        properties.setProperty("key", "value");

        InstallData installdata = mock(InstallData.class);
        Variables variables = mock(Variables.class);
        RulesEngine rules = mock(RulesEngine.class);
        Messages messages = mock(Messages.class);

        when(installdata.getVariables()).thenReturn(variables);
        when(variables.getProperties()).thenReturn(properties);
        doAnswer(ctx -> updateProperty(properties, ctx)).when(installdata).setVariable(anyString(), anyString());
        when(installdata.getMessages()).thenReturn(messages);
        when(messages.get("debug.changevariable")).thenReturn("Modify");
        when(messages.get("debug.deletevariable")).thenReturn("Delete");

        Debugger debugger = new Debugger(installdata, icons, rules, new Color(230, 230, 230));

        JFrame debugframe = new JFrame("Debug information");
        debugframe.setContentPane(debugger.getDebugPanel());
        debugframe.setSize(new Dimension(400, 400));
        debugframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        debugframe.setVisible(true);
    }

    private static Object updateProperty(Properties properties, InvocationOnMock ctx) {
        final Object[] arguments = ctx.getArguments();
        final String key = (String) arguments[0];
        final String value = (String) arguments[1];
        if (value == null) {
            System.out.println("removing: " + key);
            properties.remove(key);
        } else {
            System.out.println("updateing: " + key + " to: " + value);
            properties.setProperty(key, value);
        }
        return null;
    }
}

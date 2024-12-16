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
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.rules.process.VariableCondition;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.installer.debugger.Debugger;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class DebuggerTest
{

    public static void main(String[] args)
    {
        try
        {
            new DebuggerTest().testRemoveHTML();
        }
        catch (InterruptedException | InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testRemoveHTML() throws InterruptedException, InvocationTargetException
    {
        InstallData installdata = mock(InstallData.class, "installdata");
        Variables variables = mock(Variables.class, "variables");
        RulesEngine rules = mock(RulesEngine.class, "rules");
        Messages messages = mock(Messages.class, "messages");

        IconsDatabase icons = new IconsDatabase();
        Properties properties = new Properties();
        Map<String, Condition> conditions = new HashMap<>();
        for (int count = 0; count < 10; count++)
        {
            final String name = "izpack.test." + UUID.randomUUID();
            final String value = "value" + count;
            properties.setProperty(name, value);
            VariableCondition condition = new VariableCondition(name, value);
            final String id = "cond." + UUID.randomUUID();
            condition.setId(id);
            condition.setInstallData(installdata);
            conditions.put(id, condition);
        }

        when(installdata.getVariables()).thenReturn(variables);
        when(installdata.getVariable(anyString())).thenAnswer(ctx -> properties.getProperty((String) ctx.getArguments()[0]));
        when(variables.getProperties()).thenReturn(properties);
        when(rules.getKnownConditionIds()).thenReturn(conditions.keySet());
        when(rules.getCondition(anyString())).thenAnswer(ctx -> conditions.get(ctx.getArguments()[0]));
        doAnswer(ctx -> updateProperty(properties, ctx)).when(installdata).setVariable(anyString(), anyString());
        when(installdata.getMessages()).thenReturn(messages);
        when(messages.get("debug.changevariable")).thenReturn("Modify");
        when(messages.get("debug.deletevariable")).thenReturn("Delete");

        Debugger debugger = new Debugger(installdata, icons, rules, new Color(230, 230, 230));

        JFrame debugframe = debugger.initialize(new JFrame("Debug information"));
        debugframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        debugframe.setVisible(true);
        KeyStroke resetDebugWindow = KeyStroke.getKeyStroke(KeyEvent.VK_D,
                InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
        final JPanel contentPane = (JPanel)debugframe.getContentPane();
        contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(resetDebugWindow, "reset.debug.window");
        contentPane.getActionMap().put("reset.debug.window", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Dimension panelsize = debugger.getDefaultPanelSize();
                debugframe.setSize(panelsize);
                debugframe.setPreferredSize(panelsize);
                debugframe.setLocationRelativeTo(null);
            }
        });
    }

    private static Object updateProperty(Properties properties, InvocationOnMock ctx)
    {
        final Object[] arguments = ctx.getArguments();
        final String key = (String) arguments[0];
        final String value = (String) arguments[1];
        if (value == null)
        {
            System.out.println("removing: " + key);
            properties.remove(key);
        }
        else
        {
            System.out.println("updateing: " + key + " to: " + value);
            properties.setProperty(key, value);
        }
        return null;
    }
}

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
package com.izforge.izpack.installer.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.core.container.CdiInitializationContext;
import org.junit.Test;
import org.mockito.Mockito;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.container.DefaultContainer;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.factory.DefaultObjectFactory;
import com.izforge.izpack.core.resource.DefaultLocales;
import com.izforge.izpack.core.rules.ConditionContainer;
import com.izforge.izpack.core.rules.RulesEngineImpl;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.panel.Panels;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.Platforms;

/**
 * Tests the {@link DefaultNavigator}.
 *
 * @author Tim Anderson
 */
public class DefaultNavigatorTest
{

    /**
     * The installer frame.
     */
    private final InstallerFrame frame;

    /**
     * The installation data.
     */
    private final GUIInstallData installData;

    /**
     * Factory for creating IzPanels.
     */
    private final ObjectFactory factory;

    /**
     * The container.
     */
    private final Container container;

    /**
     * Panel id seed.
     */
    private int id;


    /**
     * Constructs a {@code DefaultNavigatorTest}.
     */
    public DefaultNavigatorTest()
    {
        frame = Mockito.mock(InstallerFrame.class);
        installData = new GUIInstallData(new DefaultVariables(), Platforms.WINDOWS);
        RulesEngine rules = new RulesEngineImpl(installData, Mockito.mock(ConditionContainer.class));
        installData.setRules(rules);
        final Resources resources = Mockito.mock(Resources.class);
        installData.setMessages(new LocaleDatabase((Messages) null, new DefaultLocales(resources)));

        container = new DefaultContainer()
        {
            @Override
            protected void fillContainer(CdiInitializationContext context)
            {
                super.fillContainer(context);
                context.addComponent(InstallerFrame.class, frame);
                context.addComponent(Resources.class, resources);
            }

            @Override
            public AutomatedInstallData create(Resources resources, Variables variables, Platform platform,
                                               Locales locales, Predicate<Pack> availablePackPredicate)
            {
                return installData;
            }
        };
        factory = new DefaultObjectFactory(container);
    }


    /**
     * Tests panel navigation.
     */
    @Test
    public void testNavigation()
    {
        Panels panels = createPanels(3);
        Navigator navigator = createNavigator(panels);

        // prior to display of first panel
        assertTrue(navigator.isNextEnabled());
        assertFalse(navigator.isPreviousEnabled());
        assertTrue(navigator.next());

        // first panel
        assertEquals(0, panels.getIndex());
        assertTrue(navigator.isNextEnabled());
        assertFalse(navigator.isPreviousEnabled());
        assertTrue(navigator.next());

        // second panel
        assertEquals(1, panels.getIndex());
        assertTrue(navigator.isNextEnabled());
        assertTrue(navigator.isPreviousEnabled());

        // make sure can navigate back
        assertTrue(navigator.previous());
        assertEquals(0, panels.getIndex());
        assertFalse(navigator.isPreviousEnabled());
        assertTrue(navigator.next());
        assertEquals(1, panels.getIndex());
        assertTrue(navigator.next());

        // third panel
        assertEquals(2, panels.getIndex());
        assertFalse(navigator.isNextEnabled());
        assertFalse(navigator.isPreviousEnabled());    // on last panel, can't navigate back
        assertFalse(navigator.next());
    }

    /**
     * Verifies that the next button can be disabled when switching panels.
     */
    @Test
    public void testDisableNextOnSwitch()
    {
        IzPanels panels = createPanels(3);
        final Navigator navigator = createNavigator(panels);

        // register a listener that disables the next button when the second panel is displayed
        panels.setListener(new IzPanelsListener()
        {
            @Override
            public void switchPanel(IzPanelView newPanel, IzPanelView oldPanel)
            {
                if (newPanel.getIndex() == 1)
                {
                    navigator.setNextEnabled(false);
                }
            }
        });

        // navigate to the second panel
        assertEquals(-1, panels.getIndex());
        assertTrue(navigator.next());
        assertEquals(0, panels.getIndex());
        assertTrue(navigator.next());
        assertEquals(1, panels.getIndex());

        // verify the next button is disabled, and that navigation is disabled
        assertFalse(navigator.isNextEnabled());
        assertEquals(1, panels.getIndex());

        // enable the next button and verify the third panel can be navigated to
        navigator.setNextEnabled(true);
        assertTrue(navigator.next());
        assertEquals(2, panels.getIndex());
    }

    /**
     * Verifies that the previous button can be disabled when switching panels.
     */
    @Test
    public void testDisablePreviousOnSwitch()
    {
        IzPanels panels = createPanels(3);
        final Navigator navigator = createNavigator(panels);

        // register a listener that disables the previous button when the second panel is displayed
        panels.setListener(new IzPanelsListener()
        {
            @Override
            public void switchPanel(IzPanelView newPanel, IzPanelView oldPanel)
            {
                if (newPanel.getIndex() == 1)
                {
                    navigator.setPreviousEnabled(false);
                }
            }
        });

        // navigate to the second panel
        assertEquals(-1, panels.getIndex());
        assertTrue(navigator.next());
        assertEquals(0, panels.getIndex());
        assertTrue(navigator.next());
        assertEquals(1, panels.getIndex());

        // verify the previous button is disabled, and that navigation is disabled
        assertFalse(navigator.isPreviousEnabled());
        assertFalse(navigator.previous());
        assertEquals(1, panels.getIndex());
    }

    /**
     * Tests {@link com.izforge.izpack.installer.gui.Navigator#quit()}.
     */
    @Test
    public void testQuit()
    {
        IzPanels panels = createPanels(5);

        // test quit with quit enabled
        InstallerFrame frame1 = Mockito.mock(InstallerFrame.class);
        Navigator navigator1 = createNavigator(panels, frame1);
        assertTrue(navigator1.isQuitEnabled());
        navigator1.quit();
        verify(frame1, times(1)).quit(); // verify InstallerFrame.quit() invoked

        // test quit with quit disabled
        InstallerFrame frame2 = Mockito.mock(InstallerFrame.class);
        Navigator navigator2 = createNavigator(panels, frame2);
        navigator2.setQuitEnabled(false);
        navigator2.quit();
        verify(frame2, never()).quit(); // verify InstallerFrame.quit() not invoked

        // now enable quit
        navigator2.setQuitEnabled(true);
        navigator2.quit();
        verify(frame2, times(1)).quit(); // verify InstallerFrame.quit() invoked
    }

    /**
     * Verifies that the next panel can be skipped.
     */
    @Test
    public void testSkipNextPanel()
    {
        IzPanels panels = createPanels(3);
        final DefaultNavigator navigator = createNavigator(panels);
        panels.setListener(new IzPanelsListener()
        {
            @Override
            public void switchPanel(IzPanelView newPanel, IzPanelView oldPanel)
            {
                if (newPanel.getIndex() == 1)
                {
                    navigator.next(false);
                }
            }
        });

        // navigate to the first panel
        assertEquals(-1, panels.getIndex());
        assertTrue(navigator.next());
        assertEquals(0, panels.getIndex());

        // navigate to the next, verifying that the second panel (index == 1) is skipped
        assertTrue(navigator.next());
        assertEquals(2, panels.getIndex());
    }

    /**
     * Creates a new {@code Navigator} for the specified panels
     *
     * @param panels the panels to navigate
     * @return a new {@code Navigator}
     */
    private DefaultNavigator createNavigator(Panels panels)
    {
        return createNavigator(panels, frame);
    }

    /**
     * Creates a new {@code Navigator} for the specified panels
     *
     * @param panels the panels to navigate
     * @param frame  the installer frame
     * @return a new {@code Navigator}
     */
    private DefaultNavigator createNavigator(Panels panels, InstallerFrame frame)
    {
        IconsDatabase icons = new IconsDatabase();
        DefaultNavigator navigator = new DefaultNavigator(panels, icons, installData);
        navigator.setInstallerFrame(frame);
        return navigator;
    }

    /**
     * Creates a {@link IzPanels} with the specified no. of panels.
     *
     * @param count the no. of panels
     * @return a new {@link IzPanels} with {@code count} panels
     */
    private IzPanels createPanels(int count)
    {
        List<IzPanelView> views = new ArrayList<>();
        for (int i = 0; i < count; ++i)
        {
            Panel panel = new Panel();
            panel.setClassName(TestIzPanel.class.getName());
            panel.setPanelId(TestIzPanel.class.getSimpleName() + ++id);

            IzPanelView panelView = new IzPanelView(panel, factory, installData);
            views.add(panelView);
        }
        IzPanels panels = new IzPanels(views, container, installData);
        panels.initialise();
        panels.setListener(new IzPanelsListener()
        {
            @Override
            public void switchPanel(IzPanelView newPanel, IzPanelView oldPanel)
            {

            }
        });
        return panels;
    }
}

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
package com.izforge.izpack.panels.test;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;

import com.izforge.izpack.api.data.InstallData;
import org.fest.swing.fixture.ContainerFixture;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.timing.Condition;
import org.fest.swing.timing.Pause;
import org.junit.After;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.base.InstallDataConfiguratorWithRules;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.gui.DefaultNavigator;
import com.izforge.izpack.installer.gui.InstallerController;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.installer.gui.IzPanelView;
import com.izforge.izpack.installer.gui.IzPanels;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.Platforms;


/**
 * Base class for panel tests.
 *
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@Container(TestGUIPanelContainer.class)
public class AbstractPanelTest
{

    /**
     * The test container.
     */
    private final com.izforge.izpack.api.container.Container container;

    /**
     * The installation data.
     */
    private GUIInstallData installData;

    /**
     * The frame test wrapper.
     */
    private FrameFixture frameFixture;

    /**
     * The resources.
     */
    private ResourceManager resourceManager;

    /**
     * The uninstallation data writer.
     */
    private UninstallDataWriter uninstallDataWriter;

    /**
     * The icons.
     */
    private final IconsDatabase icons;

    /**
     * The rules.
     */
    private final RulesEngine rules;

    /**
     * The factory for panels etc.
     */
    private final ObjectFactory factory;

    /**
     * The available locales.
     */
    private final Locales locales;

    /**
     * The panels.
     */
    private IzPanels panels;


    /**
     * Constructs a {@code AbstractPanelTest}.
     *
     * @param container           the test container
     * @param installData         the installation data
     * @param resourceManager     the resource manager
     * @param factory             the panel factory
     * @param rules               the rules
     * @param icons               the icons
     * @param uninstallDataWriter the uninstallation data writer
     * @param locales             the locales
     */
    public AbstractPanelTest(com.izforge.izpack.api.container.Container container, InstallData installData,
                             ResourceManager resourceManager, ObjectFactory factory, RulesEngine rules,
                             IconsDatabase icons, UninstallDataWriter uninstallDataWriter, Locales locales)
    {
        this.container = container;
        this.installData = (GUIInstallData) installData;
        this.resourceManager = resourceManager;
        this.factory = factory;
        this.rules = rules;
        this.icons = icons;
        this.uninstallDataWriter = uninstallDataWriter;
        this.locales = locales;
    }

    /**
     * Cleans up after the test case.
     */
    @After
    public void tearDown() throws Exception
    {
        if (frameFixture != null)
        {
            frameFixture.cleanUp();
        }
    }

    /**
     * Returns the installation data.
     *
     * @return the installation data
     */
    protected GUIInstallData getInstallData()
    {
        return installData;
    }

    /**
     * Returns the resources.
     *
     * @return the resources
     */
    protected ResourceManager getResourceManager()
    {
        return resourceManager;
    }

    /**
     * Returns the rules.
     *
     * @return the rules
     */
    protected RulesEngine getRules()
    {
        return rules;
    }

    /**
     * Returns the uninstallation data writer.
     *
     * @return the uninstallation data writer
     */
    protected UninstallDataWriter getUninstallDataWriter()
    {
        return uninstallDataWriter;
    }

    /**
     * Returns the panels.
     *
     * @return the panels
     */
    protected IzPanels getPanels()
    {
        return panels;
    }

    /**
     * Creates an installer that displays the specified panels.
     *
     * @param panelClasses the panel classes
     * @return an {@link InstallerFrame} wrapped in a {@link FrameFixture}
     * @throws IzPackException for any error
     */
    protected FrameFixture show(Class<?>... panelClasses)
    {
        List<IzPanelView> panelList = new ArrayList<>();
        for (Class<?> panelClass : panelClasses)
        {
            panelList.add(createPanelView(panelClass));
        }
        return show(panelList);
    }

    /**
     * Creates an installer that displays the specified panels.
     *
     * @param panelViews the panel views
     * @return an {@link InstallerFrame} wrapped in a {@link FrameFixture}
     * @throws IzPackException for any error
     */
    protected FrameFixture show(IzPanelView... panelViews)
    {
        return show(Arrays.asList(panelViews));
    }

    /**
     * Creates an installer that displays the specified panels.
     *
     * @param panelViews the panel views
     * @return an {@link InstallerFrame} wrapped in a {@link FrameFixture}
     * @throws IzPackException for any error
     */
    protected FrameFixture show(final List<IzPanelView> panelViews)
    {
        // create the frame in the event dispatcher thread (mostly to keep substance L&F happy, but also good practice)
        final InstallerFrame[] handle = new InstallerFrame[1];
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                @Override
                public void run()
                {
                    panels = new IzPanels(panelViews, container, installData);
                    DefaultNavigator navigator = new DefaultNavigator(panels, icons, installData);
                    InstallerFrame frame = new InstallerFrame(installData, rules,
                                                              icons, panels, uninstallDataWriter, resourceManager,
                                                              Mockito.mock(UninstallData.class),
                                                              Mockito.mock(Housekeeper.class), navigator,
                                                              Mockito.mock(Log.class), locales);
                    handle[0] = frame;
                }
            });
        }
        catch (Exception exception)
        {
            throw new IzPackException(exception);
        }
        InstallerFrame frame = handle[0];
        frameFixture = new FrameFixture(frame);
        // container.addComponent(InstallerFrame.class, frame);
        InstallDataConfiguratorWithRules configuratorWithRules = new InstallDataConfiguratorWithRules(
                installData, rules, Platforms.UNIX);
        InstallerController controller = new InstallerController(configuratorWithRules, frame);
        controller.buildInstallation();
        controller.launchInstallation();

        return frameFixture;
    }

    /**
     * Creates an {@link IzPanelView} for the specified panel class.
     *
     * @param panelClass the panel class
     * @return a new view
     */
    protected IzPanelView createPanelView(Class<?> panelClass)
    {
        Panel panel = new Panel();
        panel.setClassName(panelClass.getName());
        return createPanelView(panel);
    }

    /**
     * Helper to create an {@link IzPanelView} for a panel.
     *
     * @param panel the panel
     * @return a new {@link IzPanelView}
     */
    protected IzPanelView createPanelView(Panel panel)
    {
        return new IzPanelView(panel, factory, installData);
    }

    /**
     * Helper which waits until a specific IzPanel class is shown.
     *
     * @param panelClass The IzPanel class to wait for.
     */
    protected void waitForPanel(Class<? extends IzPanel> panelClass)
    {
        if (null == frameFixture)
        {
            throw new IzPackException("Can't wait for panel, frame fixture not available");
        }

        Pause.pause(new UntilPanelIsShowing(frameFixture, panelClass));
    }

    /**
     * Condition which waits until a specific IzPanel is shown.
     */
    protected static class UntilPanelIsShowing extends Condition
    {

        private final Class<? extends IzPanel> panelClass;
        private final ContainerFixture<?> fixture;

        /**
         * Creates a condition for the given {@code fixture} and {@code panelClass}.
         *
         * @param fixture A container fixture (needed to gain access to the robot instance).
         * @param panelClass The IzPanel class to wait for.
         */
        public UntilPanelIsShowing(ContainerFixture<?> fixture, Class<? extends IzPanel> panelClass)
        {
            super("Waiting for panel " + panelClass.getSimpleName());

            this.fixture = fixture;
            this.panelClass = panelClass;
        }

        /**
         * @return True, if a component which is an instance of the given
         *      {@code panelClass} is visible.
         */
        @Override
        public boolean test()
        {
            Component component = fixture.robot.finder().findByType(panelClass, true);
            return (null != component);
        }
    }
}


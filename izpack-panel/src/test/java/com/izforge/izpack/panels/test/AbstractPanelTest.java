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

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.factory.ObjectFactory;
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
    private final TestGUIPanelContainer container;

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
     */
    public AbstractPanelTest(TestGUIPanelContainer container, GUIInstallData installData,
                             ResourceManager resourceManager,
                             ObjectFactory factory, RulesEngine rules, IconsDatabase icons,
                             UninstallDataWriter uninstallDataWriter)
    {
        this.container = container;
        this.installData = installData;
        this.resourceManager = resourceManager;
        this.factory = factory;
        this.rules = rules;
        this.icons = icons;
        this.uninstallDataWriter = uninstallDataWriter;
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
    protected FrameFixture show(Class... panelClasses)
    {
        List<IzPanelView> panelList = new ArrayList<IzPanelView>();
        for (Class panelClass : panelClasses)
        {
            Panel panel = new Panel();
            panel.setClassName(panelClass.getName());
            panelList.add(createPanelView(panel));
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
                    InstallerFrame frame = new InstallerFrame("A title", installData, rules,
                                                              icons, panels, uninstallDataWriter, resourceManager,
                                                              Mockito.mock(UninstallData.class),
                                                              Mockito.mock(Housekeeper.class), navigator,
                                                              Mockito.mock(Log.class));
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
        container.getContainer().addComponent(frame);
        InstallDataConfiguratorWithRules configuratorWithRules = new InstallDataConfiguratorWithRules(
                installData, rules, Platforms.UNIX);
        InstallerController controller = new InstallerController(configuratorWithRules, frame);
        controller.buildInstallation();
        controller.launchInstallation();
        return frameFixture;
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

}


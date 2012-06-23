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

package com.izforge.izpack.integration;

import static org.junit.Assert.assertEquals;

import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.event.InstallerListener;
import com.izforge.izpack.compiler.container.TestInstallationContainer;
import com.izforge.izpack.installer.event.InstallerListeners;
import com.izforge.izpack.installer.gui.InstallerController;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.test.listener.TestInstallerListener;


/**
 * Tests that {@link InstallerListener}s are invoked during installation.
 *
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@Container(TestInstallationContainer.class)
public class InstallerListenerTest extends AbstractInstallationTest
{

    /**
     * The listeners,
     */
    private final InstallerListeners listeners;

    /**
     * The installer frame.
     */
    private final InstallerFrame frame;

    /**
     * The installer controller.
     */
    private final InstallerController controller;

    /**
     * Frame fixture.
     */
    private FrameFixture frameFixture;


    /**
     * Constructs an <tt>InstallerListenerTest</tt>.
     *
     * @param listeners   the installer listeners
     * @param installData the install data
     * @param frame       the installer frame
     * @param controller  the installer controller
     */
    public InstallerListenerTest(InstallerListeners listeners, AutomatedInstallData installData, InstallerFrame frame,
                                 InstallerController controller)
    {
        super(installData);
        this.listeners = listeners;
        this.frame = frame;
        this.controller = controller;
    }

    /**
     * Tears down the test case.
     */
    @After
    public void tearDown()
    {
        if (frameFixture != null)
        {
            frameFixture.cleanUp();
        }
    }

    /**
     * Verifies that {@link InstallerListener} methods are invoked the correct no. of times when registered.
     *
     * @throws Exception for any error
     */
    @Test
    @InstallFile("samples/event/customlisteners.xml")
    public void testInstallListenerInvocation() throws Exception
    {
        frameFixture = HelperTestMethod.prepareFrameFixture(frame, controller);
        frameFixture.button(GuiId.BUTTON_NEXT.id).click();
        frameFixture.requireVisible();

        HelperTestMethod.waitAndCheckInstallation(getInstallData());

        assertEquals(1, listeners.size());
        TestInstallerListener listener = (TestInstallerListener) listeners.getInstallerListeners().get(0);
        assertEquals(1, listener.getInitialiseCount());
        assertEquals(1, listener.getBeforePacksCount());
        assertEquals(3, listener.getBeforePackCount());
        assertEquals(5, listener.getBeforeDirCount());
        assertEquals(4, listener.getBeforeFileCount());

        assertEquals(listener.getBeforePacksCount(), listener.getAfterPacksCount());
        assertEquals(listener.getBeforePackCount(), listener.getAfterPackCount());
        assertEquals(listener.getBeforeDirCount(), listener.getAfterDirCount());
        assertEquals(listener.getBeforeFileCount(), listener.getAfterFileCount());
    }

}

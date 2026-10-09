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

import static com.izforge.izpack.integration.HelperTestMethod.prepareFrameFixture;
import static com.izforge.izpack.integration.HelperTestMethod.waitAndCheckInstallation;
import static org.assertj.core.api.Assertions.assertThat;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.compiler.container.TestGUIInstallationContainer;
import com.izforge.izpack.installer.event.InstallerListeners;
import com.izforge.izpack.installer.gui.InstallerController;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.listener.TestInstallerListener;
import org.fest.swing.fixture.FrameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that {@link InstallerListener}s are invoked during installation.
 *
 * @author Tim Anderson
 */
@Container(TestGUIInstallationContainer.class)
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
    @AfterEach
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
        frameFixture = prepareFrameFixture(frame, controller);
        frameFixture.button(GuiId.BUTTON_NEXT.id).click();
        frameFixture.requireVisible();

        waitAndCheckInstallation(getInstallData());

        assertThat(listeners.size()).isEqualTo(1);
        TestInstallerListener listener = (TestInstallerListener) listeners.getInstallerListeners().get(0);
        assertThat(listener.getInitialiseCount()).isEqualTo(1);
        assertThat(listener.getBeforePacksCount()).isEqualTo(1);
        assertThat(listener.getBeforePackCount()).isEqualTo(3);
        assertThat(listener.getBeforeDirCount()).isEqualTo(5);
        assertThat(listener.getBeforeFileCount()).isEqualTo(4);

        assertThat(listener.getAfterPacksCount()).isEqualTo(listener.getBeforePacksCount());
        assertThat(listener.getAfterPackCount()).isEqualTo(listener.getBeforePackCount());
        assertThat(listener.getAfterDirCount()).isEqualTo(listener.getBeforeDirCount());
        assertThat(listener.getAfterFileCount()).isEqualTo(listener.getBeforeFileCount());
    }

}

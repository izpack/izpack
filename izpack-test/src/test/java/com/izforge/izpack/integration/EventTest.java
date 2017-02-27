package com.izforge.izpack.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.izforge.izpack.compiler.container.TestGUIInstallationContainer;
import com.izforge.izpack.data.CustomData;
import com.izforge.izpack.event.RegistryInstallerListener;
import com.izforge.izpack.event.RegistryUninstallerListener;
import com.izforge.izpack.event.SummaryLoggerInstallerListener;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.event.InstallerListeners;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;

/**
 * Test for event binding.
 *
 * @author Anthonin Bonnefoy
 * @see com.izforge.izpack.installer.container.impl.CustomDataLoader
 */
@RunWith(PicoRunner.class)
@Container(TestGUIInstallationContainer.class)
public class EventTest
{

    private final InstallerListeners listeners;

    private final UninstallData uninstallData;

    public EventTest(InstallerListeners listeners, UninstallData uninstallData)
    {
        this.listeners = listeners;
        this.uninstallData = uninstallData;
    }

    @Test
    @InstallFile("samples/event/event.xml")
    public void eventInitialization() throws Exception
    {
        assertThat(listeners.size(), Is.is(2));
        assertThat(listeners.get(0), IsInstanceOf.instanceOf(SummaryLoggerInstallerListener.class));
        assertThat(listeners.get(1), IsInstanceOf.instanceOf(RegistryInstallerListener.class));

        List<CustomData> uninstallListeners = uninstallData.getUninstallerListeners();
        assertNotNull(uninstallListeners);
        assertThat(uninstallListeners.size(), Is.is(1));
        CustomData customData = uninstallListeners.get(0);
        assertEquals(RegistryUninstallerListener.class.getName(), customData.listenerName);
    }
}

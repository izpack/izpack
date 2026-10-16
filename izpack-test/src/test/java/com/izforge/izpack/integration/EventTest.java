package com.izforge.izpack.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.izforge.izpack.compiler.container.TestGUIInstallationContainer;
import com.izforge.izpack.data.CustomData;
import com.izforge.izpack.event.RegistryInstallerListener;
import com.izforge.izpack.event.RegistryUninstallerListener;
import com.izforge.izpack.event.SummaryLoggerInstallerListener;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.event.InstallerListeners;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Test for event binding.
 *
 * @author Anthonin Bonnefoy
 * @see com.izforge.izpack.installer.container.impl.CustomDataLoader
 */
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
        assertThat(listeners.size()).isEqualTo(2);
        assertThat(listeners.get(0)).isInstanceOf(SummaryLoggerInstallerListener.class);
        assertThat(listeners.get(1)).isInstanceOf(RegistryInstallerListener.class);

        List<CustomData> uninstallListeners = uninstallData.getUninstallerListeners();
        assertThat(uninstallListeners).isNotNull();
        assertThat(uninstallListeners).hasSize(1);
        CustomData customData = uninstallListeners.get(0);
        assertThat(customData.listenerName).isEqualTo(RegistryUninstallerListener.class.getName());
    }
}

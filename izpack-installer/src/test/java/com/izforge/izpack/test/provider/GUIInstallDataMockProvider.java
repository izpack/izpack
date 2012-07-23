package com.izforge.izpack.test.provider;

import java.net.URL;
import java.util.Locale;

import org.mockito.Mockito;
import org.picocontainer.injectors.Provider;

import com.izforge.izpack.api.data.GUIPrefs;
import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.util.Platforms;

/**
 * Mock provider for guiInstallData
 */
public class GUIInstallDataMockProvider implements Provider
{

    public GUIInstallData provide(Variables variables) throws Exception
    {
        final GUIInstallData guiInstallData = new GUIInstallData(variables, Platforms.MAC_OSX);
        GUIPrefs guiPrefs = new GUIPrefs();
        guiPrefs.height = 600;
        guiPrefs.width = 480;
        guiInstallData.guiPrefs = guiPrefs;

        Info info = new Info();
        guiInstallData.setInfo(info);

        URL resource = getClass().getResource("/com/izforge/izpack/bin/langpacks/installer/eng.xml");
        Messages messages = new LocaleDatabase(resource.openStream(), Mockito.mock(Locales.class));
        guiInstallData.setMessages(messages);
        guiInstallData.setLocale(Locale.getDefault());

        return guiInstallData;
    }

}

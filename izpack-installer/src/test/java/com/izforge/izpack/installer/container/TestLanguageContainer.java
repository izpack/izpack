package com.izforge.izpack.installer.container;

import static org.mockito.Mockito.when;

import java.util.Arrays;

import javax.swing.ImageIcon;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.installer.data.GUIInstallData;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.resource.DefaultLocales;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.installer.automation.AutomatedInstaller;
import com.izforge.izpack.installer.container.provider.IconsProvider;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.merge.resolve.PathResolver;
import com.izforge.izpack.test.provider.GUIInstallDataMockProvider;

/**
 * Container for test language
 *
 * @author Anthonin Bonnefoy
 */
public class TestLanguageContainer extends AbstractContainer {

    /**
     * Constructs a <tt>TestLanguageContainer</tt>.
     */
    public TestLanguageContainer() {
        initialise();
    }

    /**
     * Invoked by {@link #initialise} to fill the container.
     *
     * @throws ContainerException if initialisation fails
     */
    @Override
    protected void fillContainer() {
        addComponent(System.getProperties());

        ResourceManager resourceManager = Mockito.mock(ResourceManager.class);
        when(resourceManager.getObject("langpacks.info")).thenReturn(Arrays.asList("eng", "fra"));
        ImageIcon engFlag = new ImageIcon(getClass().getResource("/com/izforge/izpack/bin/langpacks/flags/eng.gif"));
        ImageIcon frFlag = new ImageIcon(getClass().getResource("/com/izforge/izpack/bin/langpacks/flags/fra.gif"));
        when(resourceManager.getImageIcon("flag.eng")).thenReturn(engFlag);
        when(resourceManager.getImageIcon("flag.fra")).thenReturn(frFlag);
        when(resourceManager.getInputStream("langpacks/eng.xml")).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return getClass().getResourceAsStream("/com/izforge/izpack/bin/langpacks/installer/eng.xml");
            }
        });
        when(resourceManager.getInputStream("langpacks/fra.xml")).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return getClass().getResourceAsStream("/com/izforge/izpack/bin/langpacks/installer/fra.xml");
            }
        });

        when(resourceManager.getInputStream(Resources.CUSTOM_ICONS_RESOURCE_NAME))
                .thenThrow(new IzPackException("Not available"));

        DefaultLocales locales = new DefaultLocales(resourceManager);
        addComponent(Variables.class, DefaultVariables.class);
        addComponent(resourceManager);
        addComponent(Mockito.mock(UninstallData.class));
        addComponent(Mockito.mock(UninstallDataWriter.class));
        addComponent(Mockito.mock(AutomatedInstaller.class));
        addComponent(Mockito.mock(PathResolver.class));
        addComponent(locales);
        addComponent(Container.class, this);

        addProvider(InstallData.class, GUIInstallDataMockProvider.class);
        addProvider(IconsDatabase.class, IconsProvider.class);
    }

}

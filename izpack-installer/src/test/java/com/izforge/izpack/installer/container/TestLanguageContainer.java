package com.izforge.izpack.installer.container;

import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Properties;
import java.util.function.Predicate;

import javax.swing.ImageIcon;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.core.container.CdiInitializationContext;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.installer.automation.AutomatedInstaller;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.merge.resolve.PathResolver;
import com.izforge.izpack.test.provider.GUIInstallDataMockProvider;
import com.izforge.izpack.util.Platform;

/**
 * Container for test language
 *
 * @author Anthonin Bonnefoy
 */
public class TestLanguageContainer extends AbstractContainer
{
    private Class<?> classUnderTest;

    /**
     * Constructs a <tt>TestLanguageContainer</tt>.
     */
    public TestLanguageContainer(Class<?> classUnderTest)
    {
        this.classUnderTest = classUnderTest;
        initialise();
    }

    /**
     * Invoked by {@link #initialise} to fill the container.
     *
     * @throws ContainerException if initialisation fails
     */
    @Override
    protected void fillContainer(CdiInitializationContext context)
    {
        super.fillContainer(context);
        context.addComponent(classUnderTest);

        context.addComponent(Properties.class,  System.getProperties());

        ResourceManager resourceManager = Mockito.mock(ResourceManager.class);
        when(resourceManager.getObject("langpacks.info")).thenReturn(Arrays.asList("eng", "fra"));
        ImageIcon engFlag = new ImageIcon(getClass().getResource("/com/izforge/izpack/bin/langpacks/flags/eng.gif"));
        when(resourceManager.getImageIcon("flag.eng")).thenReturn(engFlag);
        ImageIcon frFlag = new ImageIcon(getClass().getResource("/com/izforge/izpack/bin/langpacks/flags/fra.gif"));
        when(resourceManager.getImageIcon("flag.fra")).thenReturn(frFlag);
        when(resourceManager.getInputStream("langpacks/eng.xml")).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                return getClass().getResourceAsStream("/com/izforge/izpack/bin/langpacks/installer/eng.xml");
            }
        });
        when(resourceManager.getInputStream("langpacks/fra.xml")).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                return getClass().getResourceAsStream("/com/izforge/izpack/bin/langpacks/installer/fra.xml");
            }
        });

        when(resourceManager.getInputStream(Resources.CUSTOM_ICONS_RESOURCE_NAME))
                .thenThrow(new IzPackException("Not available"));

        context.addComponent(ResourceManager.class, resourceManager);
        context.addComponent(UninstallData.class, Mockito.mock(UninstallData.class));
        context.addComponent(UninstallDataWriter.class, Mockito.mock(UninstallDataWriter.class));
        context.addComponent(AutomatedInstaller.class, Mockito.mock(AutomatedInstaller.class));
        context.addComponent(PathResolver.class, Mockito.mock(PathResolver.class));

//        addComponent(DefaultLocales.class, new DefaultLocales(resourceManager));
//        addComponent(Container.class, this);
//        addComponent(DefaultVariables.class);
//        addComponent(GUIInstallDataMockProvider.class);
//        addComponent(IconsProvider.class);
    }

    @Override
    public InstallData create(Resources resources, Variables variables, Platform platform, Locales locales,
                              Predicate<Pack> availablePackPredicate) {
        return GUIInstallDataMockProvider.create(variables, locales);
    }
}

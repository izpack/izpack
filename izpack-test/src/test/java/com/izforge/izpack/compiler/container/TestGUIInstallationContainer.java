package com.izforge.izpack.compiler.container;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import org.junit.runners.model.FrameworkMethod;

import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.container.CdiInitializationContext;
import com.izforge.izpack.installer.container.impl.InstallerContainer;
import com.izforge.izpack.installer.container.provider.GUIInstallDataFactory;
import com.izforge.izpack.util.Platform;

import java.util.function.Predicate;

/**
 * Container for integration testing
 *
 * @author Anthonin Bonnefoy
 */
public class TestGUIInstallationContainer extends AbstractTestInstallationContainer
{

    public TestGUIInstallationContainer(Class<?> klass, FrameworkMethod frameworkMethod)
    {
        super(klass, frameworkMethod);
        initialise();
    }

    @Override
    protected InstallerContainer fillInstallerContainer(CdiInitializationContext container)
    {
        return new TestGUIInstallerContainer(container);
    }

    @Override
    public InstallData create(Resources resources, Variables variables, Platform platform, Locales locales,
                              Predicate<Pack> availablePackPredicate)
    {
        return GUIInstallDataFactory.create(resources, variables, platform, locales, availablePackPredicate);
    }
}

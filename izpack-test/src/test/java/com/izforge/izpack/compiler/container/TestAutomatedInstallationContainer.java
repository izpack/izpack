package com.izforge.izpack.compiler.container;

import com.izforge.izpack.installer.container.impl.InstallerContainer;
import org.junit.runners.model.FrameworkMethod;
import org.picocontainer.MutablePicoContainer;

/**
 * Container for integration testing
 */
public class TestAutomatedInstallationContainer extends AbstractTestInstallationContainer
{

    public TestAutomatedInstallationContainer(Class<?> klass, FrameworkMethod frameworkMethod)
    {
        super(klass, frameworkMethod);
        initialise();
    }

    @Override
    protected InstallerContainer fillInstallerContainer(MutablePicoContainer container)
    {
        return new TestAutomatedInstallerContainer(container);
    }

}

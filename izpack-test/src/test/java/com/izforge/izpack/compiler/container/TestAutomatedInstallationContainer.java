package com.izforge.izpack.compiler.container;

import com.izforge.izpack.installer.container.impl.InstallerContainer;
import org.junit.runners.model.FrameworkMethod;


/**
 * Container for integration testing
 */
public class TestAutomatedInstallationContainer extends AbstractTestInstallationContainer
{
    public TestAutomatedInstallationContainer(Class<?> klass, FrameworkMethod frameworkMethod)
    {
        super(klass, frameworkMethod, false);
        initialise();
    }


    @Override
    protected InstallerContainer fillInstallerContainer()
    {
        return new TestAutomatedInstallerContainer();
    }

}

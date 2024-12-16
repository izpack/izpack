package com.izforge.izpack.compiler.container;

import com.izforge.izpack.installer.container.impl.InstallerContainer;
import org.junit.runners.model.FrameworkMethod;


/**
 * Container for integration testing
 *
 * @author Anthonin Bonnefoy
 */
public class TestConsoleInstallationContainer extends AbstractTestInstallationContainer
{
    public TestConsoleInstallationContainer(Class<?> klass, FrameworkMethod frameworkMethod)
    {
        super(klass, frameworkMethod, false);
        initialise();
    }

    @Override
    protected InstallerContainer fillInstallerContainer()
    {
        return new TestConsoleInstallerContainer();
    }

}

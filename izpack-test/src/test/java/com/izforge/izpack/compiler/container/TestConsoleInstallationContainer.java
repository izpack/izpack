package com.izforge.izpack.compiler.container;

import java.lang.reflect.Method;
import org.picocontainer.MutablePicoContainer;

import com.izforge.izpack.installer.container.impl.InstallerContainer;


/**
 * Container for integration testing
 *
 * @author Anthonin Bonnefoy
 */
public class TestConsoleInstallationContainer extends AbstractTestInstallationContainer
{
    public TestConsoleInstallationContainer(Class<?> klass, Method frameworkMethod)
    {
        super(klass, frameworkMethod);
        initialise();
    }


    @Override
    protected InstallerContainer fillInstallerContainer(MutablePicoContainer container)
    {
        return new TestConsoleInstallerContainer(container);
    }

}

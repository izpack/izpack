package com.izforge.izpack.compiler.container;

import com.izforge.izpack.installer.container.impl.InstallerContainer;
import java.lang.reflect.Method;
import org.picocontainer.MutablePicoContainer;


/**
 * Container for integration testing
 */
public class TestAutomatedInstallationContainer extends AbstractTestInstallationContainer
{
    public TestAutomatedInstallationContainer(Class<?> klass, Method frameworkMethod)
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

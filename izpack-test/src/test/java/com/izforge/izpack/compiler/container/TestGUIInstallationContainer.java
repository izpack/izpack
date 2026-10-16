package com.izforge.izpack.compiler.container;

import java.lang.reflect.Method;
import org.picocontainer.MutablePicoContainer;

import com.izforge.izpack.installer.container.impl.InstallerContainer;

/**
 * Container for integration testing
 *
 * @author Anthonin Bonnefoy
 */
public class TestGUIInstallationContainer extends AbstractTestInstallationContainer
{

    public TestGUIInstallationContainer(Class klass, Method frameworkMethod)
    {
        super(klass, frameworkMethod);
        initialise();
    }

    @Override
    protected InstallerContainer fillInstallerContainer(MutablePicoContainer container)
    {
        return new TestGUIInstallerContainer(container);
    }

}

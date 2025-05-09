package com.izforge.izpack.compiler.container;

import com.izforge.izpack.installer.container.impl.InstallerContainer;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.picocontainer.MutablePicoContainer;


/**
 * Container for integration testing
 */
public class TestAutomatedInstallationContainer extends AbstractTestInstallationContainer
{
    public TestAutomatedInstallationContainer(Class<?> klass, ExtensionContext extensionContext)
    {
        super(klass, extensionContext);
        initialise();
    }

    @Override
    protected InstallerContainer fillInstallerContainer(MutablePicoContainer container)
    {
        return new TestAutomatedInstallerContainer(container);
    }

}

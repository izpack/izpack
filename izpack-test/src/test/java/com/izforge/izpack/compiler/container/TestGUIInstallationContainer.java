package com.izforge.izpack.compiler.container;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.picocontainer.MutablePicoContainer;

import com.izforge.izpack.installer.container.impl.InstallerContainer;

/**
 * Container for integration testing
 *
 * @author Anthonin Bonnefoy
 */
public class TestGUIInstallationContainer extends AbstractTestInstallationContainer
{

    public TestGUIInstallationContainer(Class klass, ExtensionContext extensionContext)
    {
        super(klass, extensionContext);
        initialise();
    }

    @Override
    protected InstallerContainer fillInstallerContainer(MutablePicoContainer container)
    {
        return new TestGUIInstallerContainer(container);
    }

}

package com.izforge.izpack.test.container;

import java.util.Properties;

import org.mockito.Mockito;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.injectors.ProviderAdapter;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.container.PlatformProvider;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.factory.DefaultObjectFactory;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.core.rules.ConditionContainer;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.automation.AutomatedInstaller;
import com.izforge.izpack.installer.base.InstallDataConfiguratorWithRules;
import com.izforge.izpack.installer.container.provider.IconsProvider;
import com.izforge.izpack.installer.container.provider.RulesProvider;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.unpacker.IUnpacker;
import com.izforge.izpack.test.provider.GUIInstallDataMockProvider;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.Platforms;


/**
 * Container for injecting mock for individual panel testing.
 */
public class TestPanelContainer extends AbstractContainer
{

    /**
     * Constructs a <tt>TestPanelContainer</tt>.
     *
     * @throws ContainerException if initialisation fails
     */
    public TestPanelContainer()
    {
        initialise();
    }

    /**
     * Returns the underlying container.
     *
     * @return the underlying container
     */
    @Override
    public MutablePicoContainer getContainer()
    {
        return super.getContainer();
    }

    /**
     * Invoked by {@link #initialise} to fill the container.
     *
     * @param container the underlying container
     * @throws ContainerException if initialisation fails
     * @throws PicoException      for any PicoContainer error
     */
    @Override
    protected void fillContainer(MutablePicoContainer container)
    {
        Properties properties = System.getProperties();
        addComponent(properties, properties);
        addComponent(Variables.class, DefaultVariables.class);
        addComponent(ResourceManager.class);
        addComponent(UninstallData.class);
        addComponent(ConditionContainer.class);
        addComponent(InstallDataConfiguratorWithRules.class);
        addComponent(UninstallDataWriter.class, Mockito.mock(UninstallDataWriter.class));
        addComponent(AutomatedInstaller.class);

        container.addComponent(new DefaultObjectFactory(this));
        addComponent(IUnpacker.class, Mockito.mock(IUnpacker.class));
        addComponent(Log.class, Mockito.mock(Log.class));
        addComponent(Housekeeper.class, Mockito.mock(Housekeeper.class));
        addComponent(Platforms.class);
        addComponent(Container.class, this);

        container.addConfig("title", "testPanel");

        container
                .addAdapter(new ProviderAdapter(new GUIInstallDataMockProvider()))
                .addAdapter(new ProviderAdapter(new IconsProvider()))
                .addAdapter(new ProviderAdapter(new RulesProvider()))
                .addAdapter(new ProviderAdapter(new PlatformProvider()));
    }

}

package com.izforge.izpack.uninstaller.container;

import java.util.Properties;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.injectors.ProviderAdapter;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.container.PlatformProvider;
import com.izforge.izpack.core.factory.DefaultObjectFactory;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.resource.DefaultLocales;
import com.izforge.izpack.core.resource.DefaultResources;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.uninstaller.Destroyer;
import com.izforge.izpack.uninstaller.gui.UninstallerFrame;
import com.izforge.izpack.uninstaller.resource.Executables;
import com.izforge.izpack.uninstaller.resource.InstallLog;
import com.izforge.izpack.uninstaller.resource.RootScripts;
import com.izforge.izpack.util.DefaultTargetPlatformFactory;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.Platforms;
import com.izforge.izpack.util.TargetFactory;


/**
 * Uninstaller container.
 *
 * @author Tim Anderson
 */
public abstract class UninstallerContainer extends AbstractContainer
{

    /**
     * Invoked by {@link #initialise} to fill the container.
     * <p/>
     *
     * @param container the underlying container
     * @throws ContainerException if initialisation fails
     * @throws PicoException      for any PicoContainer error
     */
    @Override
    protected void fillContainer(MutablePicoContainer container)
    {
        addComponent(Resources.class, DefaultResources.class);
        addComponent(Housekeeper.class);
        addComponent(Librarian.class);
        addComponent(TargetFactory.class);
        addComponent(DefaultObjectFactory.class);
        addComponent(DefaultTargetPlatformFactory.class);
        addComponent(RegistryDefaultHandler.class);
        addComponent(UninstallerFrame.class);
        addComponent(Container.class, this);
        addComponent(Properties.class);
        addComponent(ResourceManager.class);
        addComponent(DefaultLocales.class);
        addComponent(Platforms.class);
        addComponent(ObjectFactory.class, DefaultObjectFactory.class);
        addComponent(InstallLog.class);
        addComponent(Executables.class);
        addComponent(RootScripts.class);
        addComponent(Destroyer.class);

        container.addAdapter(new ProviderAdapter(new PlatformProvider()));
        container.addAdapter(new ProviderAdapter(new UninstallerListenersProvider()));
        container.addAdapter(new ProviderAdapter(new MessagesProvider()));
    }
}

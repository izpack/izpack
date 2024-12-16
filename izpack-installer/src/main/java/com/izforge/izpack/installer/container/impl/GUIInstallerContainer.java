package com.izforge.izpack.installer.container.impl;

import javax.swing.SwingUtilities;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.container.CdiInitializationContext;
import com.izforge.izpack.installer.container.provider.GUIInstallDataFactory;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.unpacker.IUnpacker;
import com.izforge.izpack.util.Platform;

import java.util.function.Predicate;

/**
 * GUI Installer container.
 */
public class GUIInstallerContainer extends InstallerContainer
{

    /**
     * Constructs a <tt>GUIInstallerContainer</tt>.
     *
     * @throws ContainerException if initialisation fails
     */
    public GUIInstallerContainer()
    {
        initialise();
    }

    /**
     * Constructs a <tt>GUIInstallerContainer</tt>.
     * <p/>
     * This constructor is provided for testing purposes.
     *
     * @param container the underlying container
     * @throws ContainerException if initialisation fails
     */
    protected GUIInstallerContainer(CdiInitializationContext container)
    {
        initialise(container, this::fillContainer);
    }

    /**
     * Registers components with the container.
     */
    @Override
    protected void fillContainer(CdiInitializationContext context)
    {
        super.fillContainer(context);
//        addComponent(GUIInstallDataProvider.class);
//        addComponent(IzPanelsProvider.class);
//        addComponent(IconsProvider.class);
//        addComponent(GUIPrompt.class);
//        addComponent(InstallerController.class);
//        addComponent(DefaultNavigator.class);
//        addComponent(InstallerFrame.class);
//        addComponent(Log.class);
//        addComponent(GUIPackResources.class);
//        addComponent(MultiVolumeUnpackerHelper.class);
//        addComponent(SplashScreen.class);
//        addComponent(LanguageDialog.class);
    }

    /**
     * Resolve components.
     */
    @Override
    protected void resolveComponents(CdiInitializationContext context)
    {
        super.resolveComponents(context);
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                @Override
                public void run()
                {
                    InstallerFrame frame = getComponent(InstallerFrame.class);
                    IUnpacker unpacker = getComponent(IUnpacker.class);
                    frame.setUnpacker(unpacker);
                }
            });
        }
        catch (Exception exception)
        {
            throw new IzPackException(exception);
        }

    }

    @Override
    public InstallData create(Resources resources, Variables variables, Platform platform, Locales locales,
                              Predicate<Pack> availablePackPredicate)
    {
        return GUIInstallDataFactory.create(resources, variables, platform, locales, availablePackPredicate);
    }
}

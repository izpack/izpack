package com.izforge.izpack.installer.container.impl;

import javax.swing.SwingUtilities;

import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.injectors.ProviderAdapter;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.gui.GUIPrompt;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.container.provider.GUIInstallDataProvider;
import com.izforge.izpack.installer.container.provider.IconsProvider;
import com.izforge.izpack.installer.container.provider.IzPanelsProvider;
import com.izforge.izpack.installer.gui.DefaultNavigator;
import com.izforge.izpack.installer.gui.InstallerController;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.SplashScreen;
import com.izforge.izpack.installer.language.LanguageDialog;
import com.izforge.izpack.installer.multiunpacker.MultiVolumeUnpackerHelper;
import com.izforge.izpack.installer.unpacker.GUIPackResources;
import com.izforge.izpack.installer.unpacker.IUnpacker;

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
    protected GUIInstallerContainer(MutablePicoContainer container)
    {
        initialise(container);
    }

    /**
     * Registers components with the container.
     *
     * @param pico the container
     */
    @Override
    protected void registerComponents(MutablePicoContainer pico)
    {
        super.registerComponents(pico);
        pico
                .addAdapter(new ProviderAdapter(new GUIInstallDataProvider()))
                .addAdapter(new ProviderAdapter(new IzPanelsProvider()))
                .addAdapter(new ProviderAdapter(new IconsProvider()));

        pico
                .addComponent(GUIPrompt.class)
                .addComponent(InstallerController.class)
                .addComponent(DefaultNavigator.class)
                .addComponent(InstallerFrame.class)
                .addComponent(Log.class)
                .addComponent(GUIPackResources.class)
                .addComponent(MultiVolumeUnpackerHelper.class)
                .addComponent(SplashScreen.class)
                .as(Characteristics.USE_NAMES).addComponent(LanguageDialog.class);
    }

    /**
     * Resolve components.
     *
     * @param pico the container
     */
    @Override
    protected void resolveComponents(final MutablePicoContainer pico)
    {
        super.resolveComponents(pico);

        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                @Override
                public void run()
                {
                    InstallerFrame frame = pico.getComponent(InstallerFrame.class);
                    IUnpacker unpacker = pico.getComponent(IUnpacker.class);
                    frame.setUnpacker(unpacker);
                }
            });
        }
        catch (Exception exception)
        {
            throw new IzPackException(exception);
        }

    }
}

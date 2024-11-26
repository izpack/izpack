package com.izforge.izpack.installer.container.impl;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.gui.GUIPrompt;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.container.provider.GUIInstallDataProvider;
import com.izforge.izpack.installer.container.provider.IconsProvider;
import com.izforge.izpack.installer.container.provider.IzPanelsProvider;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.*;
import com.izforge.izpack.installer.language.LanguageDialog;
import com.izforge.izpack.installer.multiunpacker.MultiVolumeUnpackerHelper;
import com.izforge.izpack.installer.unpacker.GUIPackResources;
import com.izforge.izpack.installer.unpacker.IUnpacker;
import com.izforge.izpack.installer.unpacker.PackResources;

import javax.swing.*;

/**
 * GUI Installer container.
 */
public class GUIInstallerContainer extends InstallerContainer {

    /**
     * Registers components with the container.
     */
    @Override
    protected void registerComponents() {
        super.registerComponents();

        addProvider(GUIInstallData.class, GUIInstallDataProvider.class);
        addProvider(InstallData.class, GUIInstallDataProvider.class);
        addProvider(IzPanels.class, IzPanelsProvider.class);
        addProvider(IconsDatabase.class, IconsProvider.class);

        addComponent(Prompt.class, GUIPrompt.class);
        addComponent(InstallerController.class);
        addComponent(DefaultNavigator.class);
        addComponent(InstallerFrame.class);
        addComponent(Log.class);
        addComponent(PackResources.class, GUIPackResources.class);
        addComponent(MultiVolumeUnpackerHelper.class);
        addComponent(SplashScreen.class);
        addComponent(LanguageDialog.class);
    }

    /**
     * Resolve components.
     */
    @Override
    protected void resolveComponents() {
        super.resolveComponents();

        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {
                        InstallerFrame frame = getComponent(InstallerFrame.class);
                        IUnpacker unpacker = getComponent(IUnpacker.class);
                        frame.setUnpacker(unpacker);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } catch (Exception exception) {
            throw new IzPackException(exception);
        }

    }
}

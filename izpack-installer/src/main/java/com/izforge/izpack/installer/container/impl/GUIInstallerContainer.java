package com.izforge.izpack.installer.container.impl;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import com.izforge.izpack.installer.gui.SplashScreen;
import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.injectors.ProviderAdapter;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.gui.GUIPrompt;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.container.provider.GUIInstallDataProvider;
import com.izforge.izpack.installer.container.provider.IconsProvider;
import com.izforge.izpack.installer.container.provider.IzPanelsProvider;
import com.izforge.izpack.installer.gui.DefaultNavigator;
import com.izforge.izpack.installer.gui.InstallerController;
import com.izforge.izpack.installer.gui.InstallerFrame;
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
    protected void resolveComponents(MutablePicoContainer pico)
    {
        super.resolveComponents(pico);
        InstallData installdata = pico.getComponent(InstallData.class);
        pico
                .addConfig("title", getTitle(installdata)) // Configuration of title parameter in InstallerFrame
                .addConfig("frame", initFrame());          // Configuration of frame parameter in languageDialog

        InstallerFrame frame = pico.getComponent(InstallerFrame.class);
        IUnpacker unpacker = pico.getComponent(IUnpacker.class);
        frame.setUnpacker(unpacker);
    }

    private JFrame initFrame()
    {
        IconsDatabase icons = getComponent(IconsDatabase.class);
        // Dummy Frame
        JFrame frame = new JFrame();
        ImageIcon imageIcon = icons.get("JFrameIcon");
        frame.setIconImage(imageIcon.getImage());

        Dimension frameSize = frame.getSize();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - frameSize.width) / 2,
                          (screenSize.height - frameSize.height) / 2 - 10);
        return frame;
    }

    private String getTitle(InstallData installData)
    {
        // Use a alternate message if defined.
        final String key = "installer.reversetitle";
        Messages messages = installData.getMessages();
        String message = messages.get(key);
        // message equal to key -> no message defined.
        if (message.equals(key))
        {
            message = messages.get("installer.title") + " " + installData.getInfo().getAppName();
        }
        else
        {
            // Attention! The alternate message has to contain the whole message including
            // $APP_NAME and may be $APP_VER.
            message = installData.getVariables().replace(message);
        }
        return message;
    }

}

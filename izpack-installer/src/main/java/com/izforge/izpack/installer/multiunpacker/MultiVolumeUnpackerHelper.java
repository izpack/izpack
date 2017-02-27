package com.izforge.izpack.installer.multiunpacker;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.core.io.VolumeLocator;
import com.izforge.izpack.installer.gui.InstallerFrame;

public class MultiVolumeUnpackerHelper implements VolumeLocator
{

    /**
     * The installation data.
     */
    private InstallData installData;

    /**
     * The installer frame.
     */
    private final InstallerFrame frame;

    /**
     * The logger.
     */
    private static final Logger LOGGER = Logger.getLogger(MultiVolumeUnpackerHelper.class.getName());

    /**
     * Constructs a <tt>MultiVolumeUnpackerHelper</tt>.
     *
     * @param installData the installation data
     * @param frame the installer frame
     */
    public MultiVolumeUnpackerHelper(InstallData installData, InstallerFrame frame)
    {
        this.installData = installData;
        this.frame = frame;
    }

    /**
     * Returns the next volume.
     *
     * @param path the expected volume path
     * @param corrupt if <tt>true</tt> the previous attempt detected a corrupt
     * or invalid volume
     * @return the next volume
     * @throws java.io.IOException if the volume cannot be found
     */
    @Override
    public File getVolume(String path, boolean corrupt) throws IOException
    {
        if (corrupt)
        {
            Messages messages = installData.getMessages();
            JOptionPane.showMessageDialog(frame, messages.get("nextmedia.corruptmedia"),
                    messages.get("nextmedia.corruptmedia.title"), JOptionPane.ERROR_MESSAGE);
        }
        LOGGER.fine("Enter next media: " + path);

        File volume = new File(path);
        NextMediaDialog nextMediaDialog;

        while (!volume.exists() || corrupt)
        {
            nextMediaDialog = new NextMediaDialog(frame, installData, path);
            nextMediaDialog.setVisible(true);
            String nextmediainput = nextMediaDialog.getNextMedia();
            if (nextmediainput != null)
            {
                volume = new File(nextmediainput);
            }
            else
            {
                LOGGER.fine("Input from NextMediaDialog was null");
                volume = new File(path);
            }
            // selection equal to last selected which was corrupt?
            if (!(path.equals(volume.getAbsolutePath()) && corrupt))
            {
                corrupt = false;
            }
        }
        return volume;
    }

}

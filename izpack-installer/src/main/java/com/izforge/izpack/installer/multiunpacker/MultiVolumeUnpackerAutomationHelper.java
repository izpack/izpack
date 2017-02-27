package com.izforge.izpack.installer.multiunpacker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.core.io.VolumeLocator;

public class MultiVolumeUnpackerAutomationHelper implements VolumeLocator
{

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * The logger.
     */
    private static final Logger LOGGER = Logger.getLogger(MultiVolumeUnpackerAutomationHelper.class.getName());

    /**
     * Constructs a <tt>MultiVolumeUnpackerAutomationHelper</tt>.
     *
     * @param installData the installation data
     */
    public MultiVolumeUnpackerAutomationHelper(InstallData installData)
    {
        this.installData = installData;
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
        Messages messages = installData.getMessages();
        if (corrupt)
        {
            System.err.println(" [ " + messages.get("nextmedia.corruptmedia.title") + " ] ");
            System.err.println(messages.get("nextmedia.corruptmedia"));
            System.err.println(messages.get("nextmedia.corruptmedia"));
        }
        LOGGER.fine("Enter next media: " + path);

        File volume = new File(path);

        while (!volume.exists() || corrupt)
        {
            System.out.println(" [ " + messages.get("nextmedia.title") + " ] ");
            System.out.println(messages.get("nextmedia.msg"));
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            String nextmediainput = null;
            try
            {
                nextmediainput = reader.readLine();
            }
            catch (IOException e)
            {
                LOGGER.log(Level.WARNING, "Error reading next media path: " + e.getMessage(), e);
                e.printStackTrace();
            }

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

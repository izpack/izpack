package com.izforge.izpack.installer;

import java.awt.Component;
import java.io.File;

import javax.swing.JOptionPane;

import com.izforge.izpack.panels.NextMediaDialog;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.Debug;

public class MultiVolumeUnpackerHelper implements IMultiVolumeUnpackerHelper
{

    private AutomatedInstallData idata;

    private AbstractUIProgressHandler handler;

    public MultiVolumeUnpackerHelper()
    {
        
    }

    public File enterNextMediaMessage(String volumename, boolean lastcorrupt)
    {
        if (lastcorrupt)
        {
            Component parent = null;
            if ((this.handler != null) && (this.handler instanceof IzPanel))
            {
                parent = ((IzPanel) this.handler).getInstallerFrame();
            }
            JOptionPane.showMessageDialog(parent, idata.langpack
                    .getString("nextmedia.corruptmedia"), idata.langpack
                    .getString("nextmedia.corruptmedia.title"), JOptionPane.ERROR_MESSAGE);
        }
        Debug.trace("Enter next media: " + volumename);

        File nextvolume = new File(volumename);
        NextMediaDialog nmd = null;

        while (!nextvolume.exists() || lastcorrupt)
        {
            if ((this.handler != null) && (this.handler instanceof IzPanel))
            {
                InstallerFrame installframe = ((IzPanel) this.handler).getInstallerFrame();
                nmd = new NextMediaDialog(installframe, idata, volumename);
            }
            else
            {
                nmd = new NextMediaDialog(null, idata, volumename);
            }
            nmd.setVisible(true);
            String nextmediainput = nmd.getNextMedia();
            if (nextmediainput != null)
            {
                nextvolume = new File(nextmediainput);
            }
            else
            {
                Debug.trace("Input from NextMediaDialog was null");
                nextvolume = new File(volumename);
            }
            // selection equal to last selected which was corrupt?
            if (!(volumename.equals(nextvolume.getAbsolutePath()) && lastcorrupt))
            {
                lastcorrupt = false;
            }
        }
        return nextvolume;
    }

    public File enterNextMediaMessage(String volumename)
    {
        return enterNextMediaMessage(volumename, false);
    }

    public void init(AutomatedInstallData idata, AbstractUIProgressHandler handler)
    {
        this.idata = idata;
        this.handler = handler;
    }
}

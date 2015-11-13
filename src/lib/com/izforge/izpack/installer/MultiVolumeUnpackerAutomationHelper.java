package com.izforge.izpack.installer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import com.izforge.izpack.panels.NextMediaDialog;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.Debug;


public class MultiVolumeUnpackerAutomationHelper implements IMultiVolumeUnpackerHelper
{   
    private AutomatedInstallData idata;
    private AbstractUIProgressHandler handler;
    
    public MultiVolumeUnpackerAutomationHelper(){
        
    }
    
    public File enterNextMediaMessage(String volumename, boolean lastcorrupt)
    {
        if (lastcorrupt)
        {
            System.err.println(" [ " + idata.langpack.getString("nextmedia.corruptmedia.title") + " ] ");
            System.err.println(idata.langpack.getString("nextmedia.corruptmedia"));                        
        }
        Debug.trace("Enter next media: " + volumename);
                
        File nextvolume = new File(volumename);
        NextMediaDialog nmd = null;

        while (!nextvolume.exists() || lastcorrupt)
        {                        
            System.out.println(" [ " + idata.langpack.getString("nextmedia.title") + " ] ");
            System.out.println(idata.langpack.getString("nextmedia.msg"));
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                        
            String nextmediainput = null;
            try
            {
                nextmediainput = reader.readLine();
            }
            catch (IOException e)
            {
                Debug.error("Error reading next media path: " + e.getMessage());
                e.printStackTrace();
            }
            
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

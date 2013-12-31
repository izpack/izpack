package com.izforge.izpack.panels;

import com.coi.tools.os.win.MSWinConstants;
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerException;
import com.izforge.izpack.installer.PanelAutomation;
import com.izforge.izpack.installer.PanelAutomationHelper;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.VariableSubstitutor;


public class InstallationTypePanelAutomationHelper extends PanelAutomationHelper implements
        PanelAutomation, AbstractUIProgressHandler, MSWinConstants
{

    public void startAction(String name, int no_of_steps)
    {
        // TODO Auto-generated method stub

    }

    public void stopAction()
    {
        // TODO Auto-generated method stub

    }

    public void nextStep(String step_name, int step_no, int no_of_substeps)
    {
        // TODO Auto-generated method stub

    }

    public void setSubStepNo(int no_of_substeps)
    {
        // TODO Auto-generated method stub

    }

    public void progress(int substep_no, String message)
    {
        // TODO Auto-generated method stub

    }

    public void makeXMLData(AutomatedInstallData installData, IXMLElement panelRoot)
    {
        IXMLElement ipath = new XMLElementImpl(InstallData.MODIFY_INSTALLATION,panelRoot);
        // check this writes even if value is the default,
        // because without the constructor, default does not get set.
        ipath.setContent(installData.getVariable(InstallData.MODIFY_INSTALLATION));

        IXMLElement prev = panelRoot.getFirstChildNamed(InstallData.MODIFY_INSTALLATION);
        if (prev != null)
        {
            panelRoot.removeChild(prev);
        }
        panelRoot.addChild(ipath);

    }

    public void runAutomated(AutomatedInstallData installData, IXMLElement panelRoot)
            throws InstallerException
    {
        IXMLElement ipath = panelRoot.getFirstChildNamed(InstallData.MODIFY_INSTALLATION);

        String modify = null;
        
        try 
        {    
            modify=ipath.getContent();
        }
        catch (Exception ex)
        {
            // assume a normal install
        }
                 
        if (modify == null || "".equals(modify.trim()))
        {
            // assume a normal install 
            installData.setVariable(InstallData.MODIFY_INSTALLATION, "false");
        }
        else
        {
            if (Boolean.parseBoolean(modify.trim()))
            {
                installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");
            }
            else
            {
                installData.setVariable(InstallData.MODIFY_INSTALLATION, "false");
            }
        }

    }

}

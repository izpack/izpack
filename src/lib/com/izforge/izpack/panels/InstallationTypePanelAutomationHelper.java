package com.izforge.izpack.panels;

import com.coi.tools.os.win.MSWinConstants;
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.InstallerException;
import com.izforge.izpack.installer.PanelAutomation;
import com.izforge.izpack.installer.PanelAutomationHelper;
import com.izforge.izpack.util.AbstractUIProgressHandler;


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
        // TODO Auto-generated method stub

    }

    public void runAutomated(AutomatedInstallData installData, IXMLElement panelRoot)
            throws InstallerException
    {
        // TODO Auto-generated method stub

    }

}

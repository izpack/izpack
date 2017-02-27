package com.izforge.izpack.panels.hello;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Overrides;
import com.izforge.izpack.installer.automation.PanelAutomation;

public class HelloPanelAutomation implements PanelAutomation
{

    public HelloPanelAutomation()
    {
    }

    /**
     * Creates an installation record for unattended installations and adds it
     * to a XML root element.
     *
     * @param installData The installation data
     * @param panelRoot The root element to add panel-specific child elements to
     */
    @Override
    public void createInstallationRecord(InstallData installData, IXMLElement panelRoot)
    {
        // we do nothing for the HelloPanel in unattended installations
    }

    /**
     * Makes the panel work in automated mode. Default is to do nothing, but any
     * panel doing something 'effective' during the installation process should
     * implement this method.
     *
     * @param installData The installation data
     * @param panelRoot The XML root element of the panels blackbox tree.
     * @throws com.izforge.izpack.api.exception.InstallerException if the
     * automated work failed critically.
     */
    @Override
    public void runAutomated(InstallData installData, IXMLElement panelRoot)
    {
        // we do nothing for the HelloPanel in unattended installations
    }

    @Override
    public void processOptions(InstallData installData, Overrides overrides)
    {
    }
}

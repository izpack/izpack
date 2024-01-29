package com.izforge.izpack.panels.installationtype;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Overrides;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.installer.automation.PanelAutomation;

public class InstallationTypePanelAutomation implements PanelAutomation
{
    public InstallationTypePanelAutomation()
    {
    }

    @Override
    public void createInstallationRecord(InstallData installData, IXMLElement rootElement)
    {
        IXMLElement element = new XMLElementImpl("modifyInstallation", rootElement);
        String modifyInstallation = installData.getVariable(InstallData.MODIFY_INSTALLATION);
        element.setContent(String.valueOf(Boolean.parseBoolean(modifyInstallation)));
        rootElement.addChild(element);
    }

    @Override
    public void runAutomated(InstallData installData, IXMLElement panelRoot) throws InstallerException
    {
        IXMLElement element = panelRoot.getFirstChildNamed("modifyInstallation");
        // installation record of prior (to this change) versions will not have this element
        if (element != null)
        {
            installData.setVariable(InstallData.MODIFY_INSTALLATION, element.getContent());
        }
    }

    @Override
    public void processOptions(InstallData installData, Overrides overrides)
    {
        String modifyInstallation = overrides.fetch(InstallData.MODIFY_INSTALLATION);
        // if not specified, we don't want to override existing value
        if (modifyInstallation != null)
        {
            installData.setVariable(InstallData.MODIFY_INSTALLATION,
                    String.valueOf(Boolean.parseBoolean(modifyInstallation)));
        }
    }
}

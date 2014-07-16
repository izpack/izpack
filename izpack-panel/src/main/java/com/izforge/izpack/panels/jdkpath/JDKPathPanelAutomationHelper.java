package com.izforge.izpack.panels.jdkpath;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.installer.automation.PanelAutomation;
import com.izforge.izpack.installer.automation.PanelAutomationHelper;

public class JDKPathPanelAutomationHelper extends PanelAutomationHelper implements PanelAutomation
{
    @Override
    public void createInstallationRecord(InstallData installData, IXMLElement rootElement)
    {
        String jdkVarName = installData.getVariable("jdkVarName");
        String jdkPathName = installData.getVariable(jdkVarName);

        IXMLElement jdkPath = new XMLElementImpl("jdkPath", rootElement);
        jdkPath.setContent(jdkPathName);
        rootElement.addChild(jdkPath);

        IXMLElement jdkVar = new XMLElementImpl("jdkVarName", rootElement);
        jdkVar.setContent(jdkVarName);
        rootElement.addChild(jdkVar);
    }

    @Override
    public void runAutomated(InstallData installData, IXMLElement panelRoot) throws InstallerException
    {
        IXMLElement jdkPathElement = panelRoot.getFirstChildNamed("jdkPath");
        String jdkPath = jdkPathElement.getContent();

        IXMLElement jdkVarNameElement = panelRoot.getFirstChildNamed("jdkVarName");
        String jdkVarName = jdkVarNameElement.getContent();

        installData.setVariable(jdkVarName, jdkPath);
    }
}
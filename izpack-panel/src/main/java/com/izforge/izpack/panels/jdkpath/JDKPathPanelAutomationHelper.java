package com.izforge.izpack.panels.jdkpath;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.installer.automation.PanelAutomation;
import com.izforge.izpack.installer.automation.PanelAutomationHelper;

public class JDKPathPanelAutomationHelper extends PanelAutomationHelper implements PanelAutomation {

    @Override
    public void createInstallationRecord(InstallData installData, IXMLElement rootElement) {
        IXMLElement varname = new XMLElementImpl("jdkVarName", rootElement);
        varname.setContent(installData.getVariable("jdkVarName"));
        rootElement.addChild(varname);

        IXMLElement jdkPath = new XMLElementImpl("jdkPath", rootElement);
        jdkPath.setContent(installData.getVariable(installData.getVariable("jdkVarName")));
        rootElement.addChild(jdkPath);
    }

    @Override
    public void runAutomated(InstallData installData, IXMLElement panelRoot) throws InstallerException {
        IXMLElement jdkPathElement = panelRoot.getFirstChildNamed("jdkPath");
        String jdkPath = jdkPathElement.getContent();

        IXMLElement jdkVarNameElement = panelRoot.getFirstChildNamed("jdkVarName");
        String jdkVarName = jdkVarNameElement.getContent();

        installData.setVariable(jdkVarName, jdkPath);
    }
}
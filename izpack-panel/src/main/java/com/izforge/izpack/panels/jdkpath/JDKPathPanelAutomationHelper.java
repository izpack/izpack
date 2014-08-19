package com.izforge.izpack.panels.jdkpath;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.installer.automation.PanelAutomation;
import com.izforge.izpack.installer.automation.PanelAutomationHelper;

public class JDKPathPanelAutomationHelper extends PanelAutomationHelper implements PanelAutomation
{
    private static final String PATH_PROMPT_KEY = "JDKPathPanelAutomationHelper.MissingPath.Prompt";
    private static final String VAR_PROMPT_KEY = "JDKPathPanelAutomationHelper.MissingVar.Prompt";

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

        if (jdkPath == null) {
            String msg = installData.getMessages().get(PATH_PROMPT_KEY);
            jdkPath = requestInput(msg);
        }

        IXMLElement jdkVarNameElement = panelRoot.getFirstChildNamed("jdkVarName");
        String jdkVarName = jdkVarNameElement.getContent();

        if (jdkVarName == null) {
            String msg = installData.getMessages().get(VAR_PROMPT_KEY);
            jdkVarName = requestInput(msg);
        }

        installData.setVariable(jdkVarName, jdkPath);
    }
}
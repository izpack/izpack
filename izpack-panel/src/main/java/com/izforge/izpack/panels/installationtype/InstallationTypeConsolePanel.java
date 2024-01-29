package com.izforge.izpack.panels.installationtype;

import java.util.Collections;
import java.util.Properties;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.config.Options;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.installer.console.AbstractConsolePanel;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.util.Console;

public class InstallationTypeConsolePanel extends AbstractConsolePanel
{
    private final InstallData installData;

    /**
     * Constructs an {@code DefaultTargetConsolePanel}.
     *
     * @param panel the parent panel/view. May be {@code null}
     * @param installData the installation data
     */
    public InstallationTypeConsolePanel(PanelView<ConsolePanel> panel, InstallData installData)
    {
        super(panel);
        this.installData = installData;
    }

    @Override
    public boolean run(InstallData installData, Properties properties)
    {
        String modifyInstall = properties.getProperty(InstallData.MODIFY_INSTALLATION);
        if (modifyInstall != null)
        {
            installData.setVariable(InstallData.MODIFY_INSTALLATION, modifyInstall);
        }
        return true;
    }

    @Override
    public boolean run(InstallData installData, Console console)
    {
        Messages messages = installData.getMessages();

        printHeadLine(installData, console);

        boolean modifyInstallation = Boolean.parseBoolean(installData.getVariable(InstallData.MODIFY_INSTALLATION));

        console.println("0 [" + (modifyInstallation ? " " : "x") + "] " + messages.get("InstallationTypePanel.normal"));
        console.println("1 [" + (modifyInstallation ? "x" : " ") + "] " + messages.get("InstallationTypePanel.modify"));

        int defaultSelection = modifyInstallation ? 1 : 0;

        int selectedInstallation = console.prompt(installData.getMessages().get("ConsoleInstaller.inputSelection"), 0, 1, defaultSelection, defaultSelection);

        installData.setVariable(InstallData.MODIFY_INSTALLATION, selectedInstallation == 1 ? "true" : "false");

        return true;
    }

    @Override
    public boolean generateOptions(InstallData installData, Options options) {
        final String name = InstallData.MODIFY_INSTALLATION;
        options.add(name, installData.getVariable(name));
        options.addEmptyLine(name);
        options.putComment(name, Collections.singletonList(getPanel().getPanelId()));
        return true;
    }

    @Override
    public void createInstallationRecord(IXMLElement rootElement) {
        new InstallationTypePanelAutomation().createInstallationRecord(installData, rootElement);
    }
}

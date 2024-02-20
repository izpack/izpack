/*
 * IzPack - Copyright 2024 Hitesh A. Bosamiya, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2024 Hitesh A. Bosamiya
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.izforge.izpack.panels.selectprinter;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.config.Options;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.installer.console.AbstractConsolePanel;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.util.Console;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.util.Collections;
import java.util.Properties;

public class SelectPrinterConsolePanel extends AbstractConsolePanel
{
    private final InstallData installData;

    /**
     * Constructs an {@code SelectPrinterConsolePanel}.
     *
     * @param panel the parent panel/view. May be {@code null}
     * @param installData the installation data
     */
    public SelectPrinterConsolePanel(PanelView<ConsolePanel> panel, InstallData installData)
    {
        super(panel);
        this.installData = installData;
    }

    @Override
    public boolean run(InstallData installData, Properties properties)
    {
        return true;
    }

    @Override
    public boolean run(InstallData installData, Console console)
    {
        printHeadLine(installData, console);

        Messages messages = installData.getMessages();

        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);

        if (printServices.length == 0)
        {
            console.println(messages.get("SelectPrinterPanel.no_printer"));
            return promptEndPanel(installData, console);
        }

        console.println(messages.get("SelectPrinterPanel.select_printer"));
        for (int i = 0; i < printServices.length; i++)
        {
            console.println(i + "  [" + (i == 0 ? "x" : " ") + "] " + printServices[i].getName());
        }
        int selectedPrinter = console.prompt(installData.getMessages().get("ConsoleInstaller.inputSelection"), 0, printServices.length, 0, 0);
        installData.setVariable("SELECTED_PRINTER", printServices[selectedPrinter].getName());
        return true;
    }

    @Override
    public boolean generateOptions(InstallData installData, Options options)
    {
        final String name = "SELECTED_PRINTER";
        options.add(name, installData.getVariable(name));
        options.addEmptyLine(name);
        options.putComment(name, Collections.singletonList(getPanel().getPanelId()));
        return true;
    }

    @Override
    public void createInstallationRecord(IXMLElement rootElement)
    {
        new SelectPrinterPanelAutomation().createInstallationRecord(installData, rootElement);
    }
}

/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil
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
package com.izforge.izpack.installer;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Properties;

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.Panel;
import com.izforge.izpack.installer.DataValidator.Status;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.OsConstraint;
import com.izforge.izpack.util.VariableSubstitutor;

/**
 * Runs the console installer
 * 
 * @author Mounir el hajj
 */
public class ConsoleInstaller extends InstallerBase
{

    private AutomatedInstallData installdata = new AutomatedInstallData();

    private boolean result = false;

    private Properties properties;

    private PrintWriter printWriter;

    public ConsoleInstaller(String langcode) throws Exception
    {
        super();
        loadInstallData(this.installdata);
        
        this.installdata.localeISO3 = langcode;
        // Fallback: choose the first listed language pack if not specified via commandline
        if (this.installdata.localeISO3 == null)
        {
            this.installdata.localeISO3 = getAvailableLangPacks().get(0);
        }
        
        InputStream in = getClass().getResourceAsStream(
                "/langpacks/" + this.installdata.localeISO3 + ".xml");
        this.installdata.langpack = new LocaleDatabase(in);
        this.installdata.setVariable(ScriptParser.ISO3_LANG, this.installdata.localeISO3);
        ResourceManager.create(this.installdata);
        loadConditions(installdata);
        loadInstallerRequirements();
        loadDynamicVariables();
        if (!checkInstallerRequirements(installdata))
        {
            Debug.log("not all installerconditions are fulfilled.");
            return;
        }
        addCustomLangpack(installdata);
    }

    protected void iterateAndPerformAction(String strAction) throws Exception
    {
        if (!checkInstallerRequirements(this.installdata))
        {
            Debug.log("not all installerconditions are fulfilled.");
            return;
        }
        Debug.log("[ Starting console installation ] " + strAction);

        try
        {
            this.result = true;
            Iterator<Panel> panelsIterator = this.installdata.panelsOrder.iterator();
            this.installdata.curPanelNumber = -1;
            VariableSubstitutor substitutor = new VariableSubstitutor(this.installdata.getVariables());
            while (panelsIterator.hasNext())
            {
                Panel p = (Panel) panelsIterator.next();
                this.installdata.curPanelNumber++;
                String praefix = "com.izforge.izpack.panels.";
                if (p.className.compareTo(".") > -1)
                {
                    praefix = "";
                }
                if (!OsConstraint.oneMatchesCurrentSystem(p.osConstraints))
                {
                    continue;
                }
                String panelClassName = p.className;
                String consoleHelperClassName = praefix + panelClassName + "ConsoleHelper";
                Class<PanelConsole> consoleHelperClass = null;
                Constructor constructor = null;
                Object[] params = {this, installdata};

                Debug.log("ConsoleHelper:" + consoleHelperClassName);
                try
                {

                    consoleHelperClass = (Class<PanelConsole>) Class
                            .forName(consoleHelperClassName);

                }
                catch (ClassNotFoundException e)
                {
                    Debug.log("ClassNotFoundException-skip :" + consoleHelperClassName);
                    continue;
                }
                PanelConsole consoleHelperInstance = null;
                if (consoleHelperClass != null)
                {
                    try
                    {
                        Debug.log("Instantiate :" + consoleHelperClassName);
                        refreshDynamicVariables(substitutor, installdata);
                        consoleHelperInstance = consoleHelperClass.newInstance();
                    }
                    catch (Exception e)
                    {
                        Debug.log("ERROR: no default constructor for " + consoleHelperClassName
                                + ", skipping...");
                        continue;
                    }
                }

                if (consoleHelperInstance != null)
                {
                    try
                    {
                        Debug.log("consoleHelperInstance." + strAction + ":"
                                + consoleHelperClassName + " entered.");
                        boolean bActionResult = true;
                        boolean bIsConditionFulfilled = true;
                        String strCondition = p.getCondition();
                        if (strCondition != null)
                        {
                            bIsConditionFulfilled = installdata.getRules().isConditionTrue(
                                    strCondition);
                        }

                        if (strAction.equals("doInstall") && bIsConditionFulfilled)
                        {
                            do
                            {
                                bActionResult = consoleHelperInstance.runConsole(this.installdata);
                            }
                            while (!validatePanel(p));
                        }
                        else if (strAction.equals("doGeneratePropertiesFile"))
                        {
                            bActionResult = consoleHelperInstance.runGeneratePropertiesFile(
                                    this.installdata, this.printWriter);
                        }
                        else if (strAction.equals("doInstallFromPropertiesFile")
                                && bIsConditionFulfilled)
                        {
                            bActionResult = consoleHelperInstance.runConsoleFromPropertiesFile(
                                    this.installdata, this.properties);
                        }
                        if (!bActionResult)
                        {
                            this.result = false;
                            return;
                        }
                        else
                        {
                            Debug.log("consoleHelperInstance." + strAction + ":"
                                    + consoleHelperClassName + " successfully done.");
                        }
                    }
                    catch (Exception e)
                    {
                        Debug.log("ERROR: console installation failed for panel " + panelClassName);
                        e.printStackTrace();
                        this.result = false;
                    }

                }

            }

            if (this.result)
            {
                System.out.println("[ Console installation done ]");
            }
            else
            {
                System.out.println("[ Console installation FAILED! ]");
            }
        }
        catch (Exception e)
        {
            this.result = false;
            System.err.println(e.toString());
            e.printStackTrace();
            System.out.println("[ Console installation FAILED! ]");
        }

    }

    protected void doInstall() throws Exception
    {
        try
        {
            iterateAndPerformAction("doInstall");
        }
        catch (Exception e)
        {
            throw e;
        }

        finally
        {
            Housekeeper.getInstance().shutDown(this.result ? 0 : 1);
        }
    }

    protected void doGeneratePropertiesFile(String strFile) throws Exception
    {
        try
        {
            this.printWriter = new PrintWriter(strFile);
            iterateAndPerformAction("doGeneratePropertiesFile");
            this.printWriter.flush();
        }
        catch (Exception e)
        {
            throw e;
        }

        finally
        {
            this.printWriter.close();
            Housekeeper.getInstance().shutDown(this.result ? 0 : 1);
        }

    }

    protected void doInstallFromPropertiesFile(String strFile) throws Exception
    {
        FileInputStream in = new FileInputStream(strFile);
        try
        {
            properties = new Properties();
            properties.load(in);
            iterateAndPerformAction("doInstallFromPropertiesFile");
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            in.close();
            Housekeeper.getInstance().shutDown(this.result ? 0 : 1);
        }
    }

    /**
     * Validate a panel.
     * 
     * @param p The panel to validate
     * @return The status of the validation - false makes the installation fail
     */
    private boolean validatePanel(final Panel p) throws InstallerException
    {
        String dataValidator = p.getValidator();
        if (dataValidator != null)
        {
            DataValidator validator = DataValidatorFactory.createDataValidator(dataValidator);
            Status validationResult = validator.validateData(installdata);
            if (validationResult != DataValidator.Status.OK)
            {
                // if defaultAnswer is true, result is ok
                if (validationResult == Status.WARNING && validator.getDefaultAnswer())
                {
                    System.out
                            .println("Configuration said, it's ok to go on, if validation is not successfull");
                }
                else
                {
                    // make installation fail instantly
                    System.out.println("Validation failed, please verify your input");
                    return false;
                }
            }
        }
        return true;
    }

    public void run(int type, String path) throws Exception
    {
        switch (type)
        {
            case Installer.CONSOLE_GEN_TEMPLATE:
                doGeneratePropertiesFile(path);
                break;

            case Installer.CONSOLE_FROM_TEMPLATE:
                doInstallFromPropertiesFile(path);
                break;
                
            default:
                doInstall();
        }
    }
}

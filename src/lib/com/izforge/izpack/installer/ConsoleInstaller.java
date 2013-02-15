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

import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;

import java.util.Iterator;
import java.util.Properties;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

import com.izforge.izpack.ExecutableFile;
import com.izforge.izpack.CustomData;
import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.Panel;
import com.izforge.izpack.rules.RulesEngine;
import com.izforge.izpack.installer.DataValidator.Status;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.OsConstraint;
import com.izforge.izpack.util.VariableSubstitutor;
import com.izforge.izpack.util.IoHelper;

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

    private ZipOutputStream outJar;

    public ConsoleInstaller(String langcode, String[] args ) throws Exception
    {
        super();
        cmdLineArgs = args;
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
        addCustomLangpack(installdata);
        //if (!checkInstallerRequirements(installdata))
        //{
        //    Debug.log("not all installerconditions are fulfilled.");
        //    return;
        //}
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

            // we get the reference to outJar now
            // since uninstallOutJar "should" not
            // return null but instead the real
            // thing
            outJar = installdata.uninstallOutJar;
            writeUninstallData();

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
    private void writeUninstallData() {
        // Show whether a separated logfile should be also written or not.
        String logfile = installdata.getVariable("InstallerFrame.logfilePath");
        BufferedWriter extLogWriter = null;
        if (logfile != null) {
            if (logfile.toLowerCase().startsWith("default")) {
                logfile = installdata.info.getUninstallerPath() + "/install.log";
            }
            logfile = IoHelper.translatePath(logfile, new VariableSubstitutor(installdata
                    .getVariables()));
            File outFile = new File(logfile);
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(outFile);
            }
            catch (FileNotFoundException e) {
                Debug.trace("Cannot create logfile!");
                Debug.error(e);
            }
            if (out != null) {
                extLogWriter = new BufferedWriter(new OutputStreamWriter(out));
            }
        }
        try {
            String condition = installdata.getVariable("UNINSTALLER_CONDITION");
            if (condition != null) {
                if (!RulesEngine.getCondition(condition).isTrue()) {
                    // condition for creating the uninstaller is not fulfilled.
                    return;
                }
            }
            // We get the data
            UninstallData udata = UninstallData.getInstance();
            List files = udata.getUninstalableFilesList();
            ZipOutputStream outJar = installdata.uninstallOutJar;

            if (outJar == null) {
                return;
            }

            // We write the files log
            outJar.putNextEntry(new ZipEntry("install.log"));
            BufferedWriter logWriter = new BufferedWriter(new OutputStreamWriter(outJar));
            logWriter.write(installdata.getInstallPath());
            logWriter.newLine();
            Iterator iter = files.iterator();
            if (extLogWriter != null) { // Write intern (in uninstaller.jar) and extern log file.
                while (iter.hasNext()) {
                    String txt = (String) iter.next();
                    logWriter.write(txt);
                    extLogWriter.write(txt);
                    if (iter.hasNext()) {
                        logWriter.newLine();
                        extLogWriter.newLine();
                    }
                }
                logWriter.flush();
                extLogWriter.flush();
                extLogWriter.close();
            } else {
                while (iter.hasNext()) {
                    logWriter.write((String) iter.next());
                    if (iter.hasNext()) {
                        logWriter.newLine();
                    }
                }
                logWriter.flush();
            }
            outJar.closeEntry();

            // We write the uninstaller jar file log
            outJar.putNextEntry(new ZipEntry("jarlocation.log"));
            logWriter = new BufferedWriter(new OutputStreamWriter(outJar));
            logWriter.write(udata.getUninstallerJarFilename());
            logWriter.newLine();
            logWriter.write(udata.getUninstallerPath());
            logWriter.flush();
            outJar.closeEntry();

            // Write out executables to execute on uninstall
            outJar.putNextEntry(new ZipEntry("executables"));
            ObjectOutputStream execStream = new ObjectOutputStream(outJar);
            iter = udata.getExecutablesList().iterator();
            execStream.writeInt(udata.getExecutablesList().size());
            while (iter.hasNext()) {
                ExecutableFile file = (ExecutableFile) iter.next();
                execStream.writeObject(file);
            }
            execStream.flush();
            outJar.closeEntry();

            // Write out additional uninstall data
            // Do not "kill" the installation if there is a problem
            // with custom uninstall data. Therefore log it to Debug,
            // but do not throw.
            Map<String, Object> additionalData = udata.getAdditionalData();
            if (additionalData != null && !additionalData.isEmpty()) {
                Iterator<String> keys = additionalData.keySet().iterator();
                HashSet<String> exist = new HashSet<String>();
                while (keys != null && keys.hasNext()) {
                    String key = keys.next();
                    Object contents = additionalData.get(key);
                    if ("__uninstallLibs__".equals(key)) {
                        Iterator nativeLibIter = ((List) contents).iterator();
                        while (nativeLibIter != null && nativeLibIter.hasNext()) {
                            String nativeLibName = (String) ((List) nativeLibIter.next()).get(0);
                            byte[] buffer = new byte[5120];
                            long bytesCopied = 0;
                            int bytesInBuffer;
                            outJar.putNextEntry(new ZipEntry("native/" + nativeLibName));
                            InputStream in = getClass().getResourceAsStream(
                                    "/native/" + nativeLibName);
                            while ((bytesInBuffer = in.read(buffer)) != -1) {
                                outJar.write(buffer, 0, bytesInBuffer);
                                bytesCopied += bytesInBuffer;
                            }
                            outJar.closeEntry();
                        }
                    } else if ("uninstallerListeners".equals(key) || "uninstallerJars".equals(key)) { // I
                        // full
                        // package paths of all needed class files.
                        // First we create a new ArrayList which contains only
                        // the full paths for the uninstall listener self; thats
                        // the first entry of each sub ArrayList.
                        ArrayList<String> subContents = new ArrayList<String>();

                        // Secound put the class into uninstaller.jar
                        Iterator listenerIter = ((List) contents).iterator();
                        while (listenerIter.hasNext()) {
                            byte[] buffer = new byte[5120];
                            long bytesCopied = 0;
                            int bytesInBuffer;
                            CustomData customData = (CustomData) listenerIter.next();
                            // First element of the list contains the listener
                            // class path;
                            // remind it for later.
                            if (customData.listenerName != null) {
                                subContents.add(customData.listenerName);
                            }
                            Iterator<String> liClaIter = customData.contents.iterator();
                            while (liClaIter.hasNext()) {
                               String contentPath = liClaIter.next();
                                if (exist.contains(contentPath)) {
                                    continue;
                                }
                                exist.add(contentPath);
                                try {
                                    outJar.putNextEntry(new ZipEntry(contentPath));
                                }
                                catch (ZipException ze) { // Ignore, or ignore not ?? May be it is a
                                    // exception because
                                    // a doubled entry was tried, then we should
                                    // ignore ...
                                    Debug.trace("ZipException in writing custom data: "
                                            + ze.getMessage());
                                    continue;
                                }
                                InputStream in = getClass().getResourceAsStream("/" + contentPath);
                                if (in != null) {
                                    while ((bytesInBuffer = in.read(buffer)) != -1) {
                                        outJar.write(buffer, 0, bytesInBuffer);
                                        bytesCopied += bytesInBuffer;
                                    }
                                } else {
                                    Debug.trace("custom data not found: " + contentPath);
                                }
                                outJar.closeEntry();

                            }
                        }
                        // Third we write the list into the
                        // uninstaller.jar
                        outJar.putNextEntry(new ZipEntry(key));
                        ObjectOutputStream objOut = new ObjectOutputStream(outJar);
                        objOut.writeObject(subContents);
                        objOut.flush();
                        outJar.closeEntry();

                    } else {
                        outJar.putNextEntry(new ZipEntry(key));
                        if (contents instanceof ByteArrayOutputStream) {
                            ((ByteArrayOutputStream) contents).writeTo(outJar);
                        } else {
                            ObjectOutputStream objOut = new ObjectOutputStream(outJar);
                            objOut.writeObject(contents);
                            objOut.flush();
                        }
                        outJar.closeEntry();
                    }
               }
            }

            // write the script files, which will
            // perform several complement and unindependend uninstall actions
            ArrayList<String> unInstallScripts = udata.getUninstallScripts();
            Iterator<String> unInstallIter = unInstallScripts.iterator();
            ObjectOutputStream rootStream;
            int idx = 0;
            while (unInstallIter.hasNext()) {
                outJar.putNextEntry(new ZipEntry(UninstallData.ROOTSCRIPT + Integer.toString(idx)));
                rootStream = new ObjectOutputStream(outJar);
                String unInstallScript = (String) unInstallIter.next();
                rootStream.writeUTF(unInstallScript);
                rootStream.flush();
                outJar.closeEntry();
                idx++;
            }

            // Cleanup
            outJar.flush();
            outJar.close();
        }
        catch (Exception err) {
            err.printStackTrace();
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

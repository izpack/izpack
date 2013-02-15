/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2003 Jonathan Halliday
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

import com.izforge.izpack.CustomData;
import com.izforge.izpack.ExecutableFile;
import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.Panel;
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.IXMLParser;
import com.izforge.izpack.adaptator.impl.XMLParser;
import com.izforge.izpack.installer.DataValidator.Status;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.OsConstraint;
import com.izforge.izpack.util.VariableSubstitutor;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

/**
 * Runs the install process in text only (no GUI) mode.
 * 
 * @author Jonathan Halliday <jonathan.halliday@arjuna.com>
 * @author Julien Ponge <julien@izforge.com>
 * @author Johannes Lehtinen <johannes.lehtinen@iki.fi>
 */
public class AutomatedInstaller extends InstallerBase
{

    // there are panels which can be instantiated multiple times
    // we therefore need to select the right XML section for each
    // instance
    private TreeMap<String, Integer> panelInstanceCount;

    /**
     * The automated installation data.
     */
    private AutomatedInstallData idata = new AutomatedInstallData();

    /**
     * The result of the installation.
     */
    private boolean result = false;

    /**
     * Constructing an instance triggers the install.
     *
     * @param inputFilename Name of the file containing the installation data.
     * @throws Exception Description of the Exception
     */
    public AutomatedInstaller(String inputFilename, String[] args) throws Exception
    {
        super();
        
        cmdLineArgs = args;
        
        File input = new File(inputFilename);

        // Loads the installation data
        loadInstallData(this.idata);

        // Loads the xml data
        this.idata.xmlData = getXMLData(input);

        // Loads the langpack
        this.idata.localeISO3 = this.idata.xmlData.getAttribute("langpack", "eng");
        InputStream in = getClass().getResourceAsStream(
                "/langpacks/" + this.idata.localeISO3 + ".xml");
        this.idata.langpack = new LocaleDatabase(in);
        this.idata.setVariable(ScriptParser.ISO3_LANG, this.idata.localeISO3);

        // create the resource manager singleton
        ResourceManager.create(this.idata);

        // Load custom langpack if exist.
        addCustomLangpack(this.idata);

        this.panelInstanceCount = new TreeMap<String, Integer>();
        // load conditions
        loadConditions(this.idata);

        // loads installer conditions
        loadInstallerRequirements();

        // load dynamic variables
        loadDynamicVariables();
    }

    /**
     * Writes the uninstalldata. <p/> Unfortunately, Java doesn't allow multiple inheritance, so
     * <code>AutomatedInstaller</code> and <code>InstallerFrame</code> can't share this code ...
     * :-/ <p/> TODO: We should try to fix this in the future.
     */
    private boolean writeUninstallData()
    {
        try
        {
            // We get the data
            UninstallData udata = UninstallData.getInstance();
            List files = udata.getUninstalableFilesList();
            ZipOutputStream outJar = this.idata.uninstallOutJar;

            if (outJar == null)
            {
                return true; // it is allowed not to have an installer
            }

            System.out.println("[ Writing the uninstaller data ... ]");

            // We write the files log
            outJar.putNextEntry(new ZipEntry("install.log"));
            BufferedWriter logWriter = new BufferedWriter(new OutputStreamWriter(outJar));
            logWriter.write(this.idata.getInstallPath());
            logWriter.newLine();
            Iterator iter = files.iterator();
            while (iter.hasNext())
            {
                logWriter.write((String) iter.next());
                if (iter.hasNext())
                {
                    logWriter.newLine();
                }
            }
            logWriter.flush();
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
            while (iter.hasNext())
            {
                ExecutableFile file = (ExecutableFile) iter.next();
                execStream.writeObject(file);
            }
            execStream.flush();
            outJar.closeEntry();

            // *** ADDED code bellow
            // Write out additional uninstall data
            // Do not "kill" the installation if there is a problem
            // with custom uninstall data. Therefore log it to Debug,
            // but do not throw.
            Map<String, Object> additionalData = udata.getAdditionalData();
            if (additionalData != null && !additionalData.isEmpty())
            {
                Iterator<String> keys = additionalData.keySet().iterator();
                HashSet<String> exist = new HashSet<String>();
                while (keys != null && keys.hasNext())
                {
                    String key = keys.next();
                    Object contents = additionalData.get(key);
                    if ("__uninstallLibs__".equals(key))
                    {
                        Iterator nativeLibIter = ((List) contents).iterator();
                        while (nativeLibIter != null && nativeLibIter.hasNext())
                        {
                            String nativeLibName = (String) ((List) nativeLibIter.next()).get(0);
                            byte[] buffer = new byte[5120];
                            long bytesCopied = 0;
                            int bytesInBuffer;
                            outJar.putNextEntry(new ZipEntry("native/" + nativeLibName));
                            InputStream in = getClass().getResourceAsStream(
                                    "/native/" + nativeLibName);
                            while ((bytesInBuffer = in.read(buffer)) != -1)
                            {
                                outJar.write(buffer, 0, bytesInBuffer);
                                bytesCopied += bytesInBuffer;
                            }
                            outJar.closeEntry();
                        }
                    }
                    else if ("uninstallerListeners".equals(key) || "uninstallerJars".equals(key))
                    { // It is a ArrayList of ArrayLists which contains the
                        // full
                        // package paths of all needed class files.
                        // First we create a new ArrayList which contains only
                        // the full paths for the uninstall listener self; thats
                        // the first entry of each sub ArrayList.
                        ArrayList<String> subContents = new ArrayList<String>();

                        // Secound put the class into uninstaller.jar
                        Iterator listenerIter = ((List) contents).iterator();
                        while (listenerIter.hasNext())
                        {
                            byte[] buffer = new byte[5120];
                            long bytesCopied = 0;
                            int bytesInBuffer;
                            CustomData customData = (CustomData) listenerIter.next();
                            // First element of the list contains the listener
                            // class path;
                            // remind it for later.
                            if (customData.listenerName != null)
                            {
                                subContents.add(customData.listenerName);
                            }
                            Iterator<String> liClaIter = customData.contents.iterator();
                            while (liClaIter.hasNext())
                            {
                                String contentPath = liClaIter.next();
                                if (exist.contains(contentPath))
                                {
                                    continue;
                                }
                                exist.add(contentPath);
                                try
                                {
                                    outJar.putNextEntry(new ZipEntry(contentPath));
                                }
                                catch (ZipException ze)
                                { // Ignore, or ignore not ?? May be it is a
                                    // exception because
                                    // a doubled entry was tried, then we should
                                    // ignore ...
                                    Debug.trace("ZipException in writing custom data: "
                                            + ze.getMessage());
                                    continue;
                                }
                                InputStream in = getClass().getResourceAsStream("/" + contentPath);
                                if (in != null)
                                {
                                    while ((bytesInBuffer = in.read(buffer)) != -1)
                                    {
                                        outJar.write(buffer, 0, bytesInBuffer);
                                        bytesCopied += bytesInBuffer;
                                    }
                                }
                                else
                                {
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

                    }
                    else
                    {
                        outJar.putNextEntry(new ZipEntry(key));
                        if (contents instanceof ByteArrayOutputStream)
                        {
                            ((ByteArrayOutputStream) contents).writeTo(outJar);
                        }
                        else
                        {
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
            while (unInstallIter.hasNext())
            {
                outJar.putNextEntry(new ZipEntry(UninstallData.ROOTSCRIPT + Integer.toString(idx)));
                rootStream = new ObjectOutputStream(outJar);
                String unInstallScript = (String) unInstallIter.next();
                rootStream.writeUTF(unInstallScript);
                rootStream.flush();
                outJar.closeEntry();
            }

            // Cleanup
            outJar.flush();
            outJar.close();
            return true;
        }
        catch (Exception err)
        {
            err.printStackTrace();
            return false;
        }
    }

    /**
     * Runs the automated installation logic for each panel in turn.
     *
     * @throws Exception
     */
    protected void doInstall() throws Exception
    {
        // check installer conditions
        if (!checkInstallerRequirements(this.idata))
        {
            Debug.log("not all installerconditions are fulfilled.");
            System.exit(-1);
            return;
        }

        // TODO: i18n
        System.out.println("[ Starting automated installation ]");
        Debug.log("[ Starting automated installation ]");

        ConsolePanelAutomationHelper uihelper = new ConsolePanelAutomationHelper(); 
        
        try
        {
            // assume that installation will succeed
            this.result = true;
            VariableSubstitutor substitutor = new VariableSubstitutor(this.idata.getVariables());
            
            // walk the panels in order
            for (Panel p : this.idata.panelsOrder)
            {
                if (p.hasCondition()
                        && !this.idata.getRules().isConditionTrue(p.getCondition(), this.idata.variables))
                {
                    Debug.log("Condition for panel " + p.getPanelid() + "is not fulfilled, skipping panel!");
                    if (this.panelInstanceCount.containsKey(p.className))
                    {
                        // get number of panel instance to process
                        this.panelInstanceCount.put(p.className, this.panelInstanceCount.get(p.className) + 1);
                    }
                    else
                    {
                        this.panelInstanceCount.put(p.className, 1);
                    }
                    continue;
                }

                if (!OsConstraint.oneMatchesCurrentSystem(p.osConstraints))
                {
                    continue;
                }

                PanelAutomation automationHelper = getPanelAutomationHelper(p);

                if (automationHelper == null)
                {
                    executePreValidateActions(p, uihelper);
                    validatePanel(p);
                    executePostValidateActions(p, uihelper);
                    continue;
                }

                IXMLElement panelRoot = updateInstanceCount(p);

                // execute the installation logic for the current panel
                installPanel(p, automationHelper, panelRoot);
                refreshDynamicVariables(substitutor, this.idata);
            }

            // this does nothing if the uninstaller was not included
            writeUninstallData();

            if (this.result)
            {
                System.out.println("[ Automated installation done ]");
            }
            else
            {
                System.out.println("[ Automated installation FAILED! ]");
            }
        }
        catch (Exception e)
        {
            this.result = false;
            System.err.println(e.toString());
            e.printStackTrace();
            System.out.println("[ Automated installation FAILED! ]");
        }
        finally
        {
            // Bye
            Housekeeper.getInstance().shutDown(this.result ? 0 : 1);
        }
    }

    /**
     * Run the installation logic for a panel.
     * @param p                   The panel to install.
     * @param automationHelper    The helper of the panel.
     * @param panelRoot           The xml element describing the panel.
     * @throws InstallerException if something went wrong while installing.
     */
    private void installPanel(Panel p, PanelAutomation automationHelper, IXMLElement panelRoot) throws InstallerException
    {
        executePreActivateActions(p, null);

        Debug.log("automationHelperInstance.runAutomated :"
                + automationHelper.getClass().getName() + " entered.");

        automationHelper.runAutomated(this.idata, panelRoot);

        Debug.log("automationHelperInstance.runAutomated :"
                + automationHelper.getClass().getName() + " successfully done.");

        executePreValidateActions(p, null);
        validatePanel(p);
        executePostValidateActions(p, null);
    }

    /**
     * Update the panelInstanceCount object with a panel.
     * @see this.panelInstanceCount
     * @param p The panel.
     * @return The xml element which describe the panel.
     */
    private IXMLElement updateInstanceCount(Panel p)
    {
        String panelClassName = p.className;

        // We get the panels root xml markup
        Vector<IXMLElement> panelRoots = this.idata.xmlData.getChildrenNamed(panelClassName);
        int panelRootNo = 0;

        if (this.panelInstanceCount.containsKey(panelClassName))
        {
            // get number of panel instance to process
            panelRootNo = this.panelInstanceCount.get(panelClassName);
        }

        IXMLElement panelRoot = panelRoots.elementAt(panelRootNo);

        this.panelInstanceCount.put(panelClassName, panelRootNo + 1);

        return panelRoot;
    }

    /**
     * Try to get the automation helper for the specified panel.
     * @param p The panel to handle.
     * @return The automation helper if possible, null otherwise.
     */
    private PanelAutomation getPanelAutomationHelper(Panel p)
    {
        Class<PanelAutomation> automationHelperClass = null;
        PanelAutomation automationHelperInstance = null;

        String praefix = "com.izforge.izpack.panels.";
        if (p.className.compareTo(".") > -1)
        // Full qualified class name
        {
            praefix = "";
        }

        String automationHelperClassName = praefix + p.className + "AutomationHelper";

        try
        {
            Debug.log("AutomationHelper:" + automationHelperClassName);
            // determine if the panel supports automated install
            automationHelperClass = (Class<PanelAutomation>) Class.forName(automationHelperClassName);
        }
        catch (ClassNotFoundException e)
        {
            // this is OK - not all panels have/need automation support.
            Debug.log("ClassNotFoundException-skip :" + automationHelperClassName);
        }

        executePreConstructActions(p, null);

        if (automationHelperClass != null)
        {
            try
            {
                // instantiate the automation logic for the panel
                Debug.log("Instantiate :" + automationHelperClassName);
                automationHelperInstance = automationHelperClass.newInstance();
            }
            catch (IllegalAccessException e)
            {
                Debug.log("ERROR: no default constructor for " + automationHelperClassName + ", skipping...");
            }
            catch (InstantiationException e)
            {
                Debug.log("ERROR: no default constructor for " + automationHelperClassName + ", skipping...");
            }
        }

        return automationHelperInstance;
    }

    /**
     * Validate a panel.
     *
     * @param p The panel to validate
     * @throws InstallerException thrown if the validation fails.
     */
    private void validatePanel(final Panel p) throws InstallerException
    {
        String dataValidator = p.getValidator();
        if (dataValidator != null)
        {
            DataValidator validator = DataValidatorFactory.createDataValidator(dataValidator);
            Status validationResult = validator.validateData(idata);
            if (validationResult != DataValidator.Status.OK)
            {
                // if defaultAnswer is true, result is ok
                if (validationResult == Status.WARNING && validator.getDefaultAnswer())
                {
                    System.out
                            .println("Configuration said, it's ok to go on, if validation is not successfull");
                    return;
                }
                // make installation fail instantly
                this.result = false;
                throw new InstallerException("Validating data for panel " + p.getPanelid() + " was not successfull");
            }
        }
    }

    /**
     * Loads the xml data for the automated mode.
     *
     * @param input The file containing the installation data.
     * @return The root of the XML file.
     * @throws IOException thrown if there are problems reading the file.
     */
    public IXMLElement getXMLData(File input) throws IOException
    {
        FileInputStream in = new FileInputStream(input);

        // Initialises the parser
        IXMLParser parser = new XMLParser();
        IXMLElement rtn = parser.parse(in,input.getAbsolutePath());
        in.close();

        return rtn;
    }

    /**
     * Get the result of the installation.
     *
     * @return True if the installation was successful.
     */
    public boolean getResult()
    {
        return this.result;
    }

    private List<PanelAction> createPanelActionsFromStringList(Panel panel, List<String> actions)
    {
        List<PanelAction> actionList = null;
        if (actions != null)
        {
            actionList = new ArrayList<PanelAction>();
            for (String actionClassName : actions)
            {
                PanelAction action = PanelActionFactory.createPanelAction(actionClassName);
                action.initialize(panel.getPanelActionConfiguration(actionClassName));
                actionList.add(action);
            }
        }
        return actionList;
    }

    private void executePreConstructActions(Panel panel, AbstractUIHandler handler)
    {
        List<PanelAction> preConstructActions = createPanelActionsFromStringList(panel, panel
                .getPreConstructionActions());
        if (preConstructActions != null)
        {
            for (PanelAction preConstructAction : preConstructActions)
            {
                preConstructAction.executeAction(idata, handler);
            }
        }
    }

    private void executePreActivateActions(Panel panel, AbstractUIHandler handler)
    {
        List<PanelAction> preActivateActions = createPanelActionsFromStringList(panel, panel
                .getPreActivationActions());
        if (preActivateActions != null)
        {
            for (PanelAction preActivateAction : preActivateActions)
            {
                preActivateAction.executeAction(idata, handler);
            }
        }
    }

    private void executePreValidateActions(Panel panel, AbstractUIHandler handler)
    {
        List<PanelAction> preValidateActions = createPanelActionsFromStringList(panel, panel
                .getPreValidationActions());
        if (preValidateActions != null)
        {
            for (PanelAction preValidateAction : preValidateActions)
            {
                preValidateAction.executeAction(idata, handler);
            }
        }
    }

    private void executePostValidateActions(Panel panel, AbstractUIHandler handler)
    {
        List<PanelAction> postValidateActions = createPanelActionsFromStringList(panel, panel
                .getPostValidationActions());
        if (postValidateActions != null)
        {
            for (PanelAction postValidateAction : postValidateActions)
            {
                postValidateAction.executeAction(idata, handler);
            }
        }
    }
}

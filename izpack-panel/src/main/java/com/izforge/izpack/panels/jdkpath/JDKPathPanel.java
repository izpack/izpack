/*
 * $Id$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2004 Klaus Bartz
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

package com.izforge.izpack.panels.jdkpath;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.coi.tools.os.win.MSWinConstants;
import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.panels.path.PathInputPanel;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.Platform;

/**
 * Panel which asks for the JDK path.
 *
 * @author Klaus Bartz
 */
public class JDKPathPanel extends PathInputPanel implements HyperlinkListener
{

    private static final long serialVersionUID = 3257006553327810104L;

    public static final String[] testFiles = new String[]{"lib" + File.separator + "tools.jar"};

    public static final String JDK_ROOT_KEY = "Software\\JavaSoft\\Java Development Kit";

    public static final String JDK_VALUE_NAME = "JavaHome";

    public static final String OSX_JDK_HOME = "/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home/";

    private static final int OK = 0;
    private static final int BAD_VERSION = 1;
    private static final int BAD_REAL_PATH = 2;
    private static final int BAD_REG_PATH = 3;

    private String detectedVersion;

    private String minVersion = null;

    private String maxVersion = null;

    private String variableName;

    private Set<String> badRegEntries = null;

    private final RegistryDefaultHandler handler;

    private final VariableSubstitutor replacer;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(JDKPathPanel.class.getName());


    /**
     * Constructs a <tt>JDKPathPanel</tt>.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent window
     * @param installData the installation data
     * @param resources   the resources
     * @param handler     the registry handler
     * @param replacer    the variable replacer
     * @param log         the log
     */
    public JDKPathPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
                        RegistryDefaultHandler handler, VariableSubstitutor replacer, Log log)
    {
        super(panel, parent, installData, resources, log);
        this.handler = handler;
        this.replacer = replacer;
        setMustExist(true);
        if (!installData.getPlatform().isA(Platform.Name.MAC_OSX))
        {
            setExistFiles(JDKPathPanel.testFiles);
        }
        setMinVersion(installData.getVariable("JDKPathPanel.minVersion"));
        setMaxVersion(installData.getVariable("JDKPathPanel.maxVersion"));
        setVariableName("JDKPath");
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e)
    {
        try
        {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
            {
                String urls = e.getURL().toExternalForm();
                if (Desktop.isDesktopSupported())
                {
                    Desktop desktop = Desktop.getDesktop();
                    desktop.browse(new URI(urls));
                }
            }
        }
        catch (Exception err)
        {
            logger.log(Level.WARNING, err.getMessage());
        }
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return Wether the panel has been validated or not.
     */
    public boolean isValidated()
    {
        boolean retval = false;
        if (super.isValidated())
        {
            switch (verifyVersionEx())
            {
                case OK:
                    this.installData.setVariable(getVariableName(), pathSelectionPanel.getPath());
                    retval = true;
                    break;
                case BAD_REG_PATH:
                    if (askQuestion(getString("installer.warning"),
                                    getString("JDKPathPanel.nonValidPathInReg"),
                                    AbstractUIHandler.CHOICES_YES_NO,
                                    AbstractUIHandler.ANSWER_NO) == AbstractUIHandler.ANSWER_YES)
                    {
                        this.installData.setVariable(getVariableName(), pathSelectionPanel.getPath());
                        retval = true;
                    }
                    break;
                case BAD_REAL_PATH:
                    break;
                case BAD_VERSION:
                    String min = getMinVersion();
                    String max = getMaxVersion();
                    StringBuilder message = new StringBuilder();
                    message.append(getString("JDKPathPanel.badVersion1")).append(
                            getDetectedVersion()).append(
                            getString("JDKPathPanel.badVersion2"));
                    if (min != null && max != null)
                    {
                        message.append(min).append(" - ").append(max);
                    }
                    else if (min != null)
                    {
                        message.append(" >= ").append(min);
                    }
                    else if (max != null)
                    {
                        message.append(" <= ").append(max);
                    }

                    message.append(getString("JDKPathPanel.badVersion3"));
                    if (askQuestion(getString("installer.warning"), message.toString(),
                                    AbstractUIHandler.CHOICES_YES_NO,
                                    AbstractUIHandler.ANSWER_NO) == AbstractUIHandler.ANSWER_YES)
                    {
                        this.installData.setVariable(getVariableName(), pathSelectionPanel.getPath());
                        retval = true;
                    }
                    break;
                default:
                    throw new RuntimeException("Internal error: unknown result of version verification.");

            }
        }
        return (retval);
    }

    /**
     * Called when the panel becomes active.
     */
    public void panelActivate()
    {
        // Resolve the default for chosenPath
        super.panelActivate();
        String chosenPath = "";

        String msg = getString("JDKPathPanel.jdkDownload");
        if (msg != null && !msg.isEmpty())
        {
            add(IzPanelLayout.createParagraphGap());
            JEditorPane textArea = new JEditorPane("text/html; charset=utf-8", replacer.substitute(msg, null));
            textArea.setCaretPosition(0);
            textArea.setEditable(false);
            textArea.addHyperlinkListener(this);
            textArea.setBackground(getBackground());

            JScrollPane scroller = new JScrollPane(textArea);
            scroller.setAlignmentX(LEFT_ALIGNMENT);
            add(scroller, NEXT_LINE);
        }

        // The variable will be exist if we enter this panel
        // second time. We would maintain the previos
        // selected path.
        if (installData.getVariable(getVariableName()) != null)
        {
            chosenPath = installData.getVariable(getVariableName());
        }
        else
        {
            if (installData.getPlatform().isA(Platform.Name.MAC_OSX))
            {
                chosenPath = OSX_JDK_HOME;
            }
            else
            {
            	String javaHome = installData.getVariable("JAVA_HOME");
            	if (javaHome != null)
            	{
            		chosenPath = new File(javaHome).getAbsolutePath();
            	}
            }
        }
        // Set the path for method pathIsValid ...
        pathSelectionPanel.setPath(chosenPath);

        if (!pathIsValid() || !verifyVersion())
        {
            chosenPath = resolveInRegistry();
            if (!pathIsValid() || !verifyVersion())
            {
                chosenPath = "";
            }
        }
        // Set the default to the path selection panel.
        pathSelectionPanel.setPath(chosenPath);
        String skipIfValid = this.installData.getVariable("JDKPathPanel.skipIfValid");
        // Should we skip this panel?
        if (chosenPath.length() > 0 && skipIfValid != null && "yes".equalsIgnoreCase(skipIfValid))
        {
            this.installData.setVariable(getVariableName(), chosenPath);
            parent.skipPanel();
        }

    }

    /**
     * Returns the path to the needed JDK if found in the registry. If there are more than one JDKs
     * registered, that one with the highest allowd version will be returned. Works only on windows.
     * On Unix an empty string returns.
     *
     * @return the path to the needed JDK if found in the windows registry
     */
    private String resolveInRegistry()
    {
        String retval = "";
        int oldVal = 0;
        RegistryHandler registryHandler = null;
        badRegEntries = new HashSet<String>();
        try
        {
            // Get the default registry handler.
            registryHandler = handler.getInstance();
            if (registryHandler == null)
            // We are on a os which has no registry or the
            // needed dll was not bound to this installation. In
            // both cases we forget the try to get the JDK path from registry.
            {
                return (retval);
            }
            oldVal = registryHandler.getRoot(); // Only for security...
            registryHandler.setRoot(MSWinConstants.HKEY_LOCAL_MACHINE);
            String[] keys = registryHandler.getSubkeys(JDK_ROOT_KEY);
            if (keys == null || keys.length == 0)
            {
                return (retval);
            }
            Arrays.sort(keys);
            int i = keys.length - 1;
            String min = getMinVersion();
            String max = getMaxVersion();
            // We search for the highest allowd version, therefore retrograde
            while (i > 0)
            {
                if (compareVersions(keys[i], max, false, 4, 4, "__NO_NOT_IDENTIFIER_"))
                { // First allowd version found, now we have to test that the min value
                    // also allows this version.
                    if (compareVersions(keys[i], min, true, 4, 4, "__NO_NOT_IDENTIFIER_"))
                    {
                        String cv = JDK_ROOT_KEY + "\\" + keys[i];
                        String path = registryHandler.getValue(cv, JDK_VALUE_NAME).getStringData();
                        // Use it only if the path is valid.
                        // Set the path for method pathIsValid ...
                        pathSelectionPanel.setPath(path);
                        if (!pathIsValid())
                        {
                            badRegEntries.add(keys[i]);
                        }
                        else if ("".equals(retval))
                        {
                            retval = path;
                        }
                        pathSelectionPanel.setPath(retval);
                    }
                }
                i--;
            }
        }
        catch (Exception e)
        { // Will only be happen if registry handler is good, but an
            // exception at performing was thrown. This is an error...
            e.printStackTrace();
        }
        finally
        {
            if (registryHandler != null && oldVal != 0)
            {
                try
                {
                    registryHandler.setRoot(MSWinConstants.HKEY_LOCAL_MACHINE);
                }
                catch (NativeLibException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return (retval);
    }

    private int verifyVersionEx()
    {
        String min = getMinVersion();
        String max = getMaxVersion();
        int retval = OK;
        // No min and max, version always ok.
        if (min == null && max == null)
        {
            return (OK);
        }

        if (!pathIsValid())
        {
            return (BAD_REAL_PATH);
        }
        // No get the version ...
        // We cannot look to the version of this vm because we should
        // test the given JDK VM.
        String[] params;
        if (installData.getPlatform().isA(Platform.Name.WINDOWS))
        {
            params = new String[]{
                    "cmd",
                    "/c",
                    pathSelectionPanel.getPath() + File.separator + "bin" + File.separator + "java",
                    "-version"
            };
        }
        else
        {
            params = new String[]{
                    pathSelectionPanel.getPath() + File.separator + "bin" + File.separator + "java",
                    "-version"
            };
        }
        String[] output = new String[2];
        FileExecutor fileExecutor = new FileExecutor();
        fileExecutor.executeCommand(params, output);
        // "My" VM writes the version on stderr :-(
        String vs = (output[0].length() > 0) ? output[0] : output[1];
        if (min != null)
        {
            if (!compareVersions(vs, min, true, 4, 4, "__NO_NOT_IDENTIFIER_"))
            {
                retval = BAD_VERSION;
            }
        }
        if (max != null)
        {
            if (!compareVersions(vs, max, false, 4, 4, "__NO_NOT_IDENTIFIER_"))
            {
                retval = BAD_VERSION;
            }
        }
        if (retval == OK && badRegEntries != null && badRegEntries.size() > 0)
        {   // Test for bad registry entry.
            if (badRegEntries.contains(getDetectedVersion()))
            {
                retval = BAD_REG_PATH;
            }
        }
        return (retval);

    }

    private boolean verifyVersion()
    {
        return (verifyVersionEx() <= 0);
    }

    private boolean compareVersions(String in, String template, boolean isMin,
                                    int assumedPlace, int halfRange, String useNotIdentifier)
    {
        StringTokenizer tokenizer = new StringTokenizer(in, " \t\n\r\f\"");
        int i;
        int currentRange = 0;
        String[] interestedEntries = new String[halfRange + halfRange];
        for (i = 0; i < assumedPlace - halfRange; ++i)
        {
            if (tokenizer.hasMoreTokens())
            {
                tokenizer.nextToken(); // Forget this entries.
            }
        }

        for (i = 0; i < halfRange + halfRange; ++i)
        { // Put the interesting Strings into an intermediaer array.
            if (tokenizer.hasMoreTokens())
            {
                interestedEntries[i] = tokenizer.nextToken();
                currentRange++;
            }
        }

        for (i = 0; i < currentRange; ++i)
        {
            if (useNotIdentifier != null && interestedEntries[i].contains(useNotIdentifier))
            {
                continue;
            }
            if (Character.getType(interestedEntries[i].charAt(0)) != Character.DECIMAL_DIGIT_NUMBER)
            {
                continue;
            }
            break;
        }
        if (i == currentRange)
        {
            detectedVersion = "<not found>";
            return (false);
        }
        detectedVersion = interestedEntries[i];
        StringTokenizer current = new StringTokenizer(interestedEntries[i], "._-");
        StringTokenizer needed = new StringTokenizer(template, "._-");
        while (needed.hasMoreTokens())
        {
            // Current can have no more tokens if needed has more
            // and if a privious token was not accepted as good version.
            // e.g. 1.4.2_02 needed, 1.4.2 current. The false return
            // will be right here. Only if e.g. needed is 1.4.2_00 the
            // return value will be false, but zero should not b e used
            // at the last version part.
            if (!current.hasMoreTokens())
            {
                return (false);
            }
            String cur = current.nextToken();
            String nee = needed.nextToken();
            int curVal;
            int neededVal;
            try
            {
                curVal = Integer.parseInt(cur);
                neededVal = Integer.parseInt(nee);
            }
            catch (NumberFormatException nfe)
            { // A number format exception will be raised if
                // there is a non numeric part in the version,
                // e.g. 1.5.0_beta. The verification runs only into
                // this deep area of version number (fourth sub place)
                // if all other are equal to the given limit. Then
                // it is right to return false because e.g.
                // the minimal needed version will be 1.5.0.2.
                return (false);
            }
            if (curVal < neededVal)
            {
                if (isMin)
                {
                    return (false);
                }
                return (true);
            }
            if (curVal > neededVal)
            {
                if (isMin)
                {
                    return (true);
                }
                return (false);
            }
        }
        return (true);
    }

    /**
     * Returns the current detected version.
     *
     * @return the current detected version
     */
    public String getDetectedVersion()
    {
        return detectedVersion;
    }

    /**
     * Returns the current used maximum version.
     *
     * @return the current used maximum version
     */
    public String getMaxVersion()
    {
        return maxVersion;
    }

    /**
     * Returns the current used minimum version.
     *
     * @return the current used minimum version
     */
    public String getMinVersion()
    {
        return minVersion;
    }

    /**
     * Sets the given value as current detected version.
     *
     * @param detectedVersion version string to be used as detected version
     */
    protected void setDetectedVersion(String detectedVersion)
    {
        this.detectedVersion = detectedVersion;
    }

    /**
     * Sets the given value as maximum for version control.
     *
     * @param maxVersion version string to be used as maximum
     */
    protected void setMaxVersion(String maxVersion)
    {
        if (maxVersion != null && maxVersion.length() > 0)
        {
            this.maxVersion = maxVersion;
        }
        else
        {
            this.maxVersion = "99.0.0";
        }
    }

    /**
     * Sets the given value as minimum for version control.
     *
     * @param minVersion version string to be used as minimum
     */
    protected void setMinVersion(String minVersion)
    {
        if (minVersion != null && minVersion.length() > 0)
        {
            this.minVersion = minVersion;
        }
        else
        {
            this.minVersion = "1.0.0";
        }
    }

    /**
     * Returns the name of the variable which should be used for the path.
     *
     * @return the name of the variable which should be used for the path
     */
    public String getVariableName()
    {
        return variableName;
    }

    /**
     * Sets the name for the variable which should be set with the path.
     *
     * @param variableName variable name to be used
     */
    public void setVariableName(String variableName)
    {
        this.variableName = variableName;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.izforge.izpack.installer.IzPanel#getSummaryBody()
     */

    public String getSummaryBody()
    {
        return (this.installData.getVariable(getVariableName()));
    }
    
    @Override
    public void makeXMLData(IXMLElement panelRoot) {
        IXMLElement ipath = new XMLElementImpl("jdkPath", panelRoot);
        ipath.setContent(pathSelectionPanel.getPath());
        panelRoot.addChild(ipath);
        
        IXMLElement varname = new XMLElementImpl("jdkVarName", panelRoot);
        varname.setContent(variableName);
        panelRoot.addChild(varname);
    }
}

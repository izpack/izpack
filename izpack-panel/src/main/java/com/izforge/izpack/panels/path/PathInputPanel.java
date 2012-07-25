/*
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

package com.izforge.izpack.panels.path;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.Platform;

/**
 * Base class for panels which asks for paths.
 *
 * @author Klaus Bartz
 */
public class PathInputPanel extends IzPanel implements ActionListener
{
    private static final long serialVersionUID = 3257566217698292531L;

    private static final transient Logger logger = Logger.getLogger(PathInputPanel.class.getName());

    /**
     * Flag whether the choosen path must exist or not
     */
    protected boolean mustExist = false;

    /**
     * Files which should be exist
     */
    protected String[] existFiles = null;

    /**
     * The path selection sub panel
     */
    protected PathSelectionPanel pathSelectionPanel;

    protected String emptyTargetMsg;

    protected String warnMsg;

    /**
     * Constructs a <tt>PathInputPanel</tt>.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent window
     * @param installData the installation data
     * @param resources   the resources
     * @param log         the log
     */
    public PathInputPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources, Log log)
    {
        super(panel, parent, installData, new IzPanelLayout(log), resources);
        // Set default values
        emptyTargetMsg = getI18nStringForClass("empty_target", "TargetPanel");
        warnMsg = getI18nStringForClass("warn", "TargetPanel");

        String introText = getI18nStringForClass("extendedIntro", "PathInputPanel");
        if (introText == null || introText.endsWith("extendedIntro")
                || introText.indexOf('$') > -1)
        {
            introText = getI18nStringForClass("intro", "PathInputPanel");
            if (introText == null || introText.endsWith("intro"))
            {
                introText = "";
            }
        }
        // Intro
        // row 0 column 0
        add(createMultiLineLabel(introText));

        add(IzPanelLayout.createParagraphGap());

        // Label for input
        // row 1 column 0.
        add(createLabel("info", "TargetPanel", "open",
                        LEFT, true), NEXT_LINE);
        // Create path selection components and add they to this panel.
        pathSelectionPanel = new PathSelectionPanel(this, installData, log);
        add(pathSelectionPanel, NEXT_LINE);
        createLayoutBottom();
        getLayoutHelper().completeLayout();
    }

    /**
     * This method does nothing. It is called from ctor of PathInputPanel, to give in a derived
     * class the possibility to add more components under the path input components.
     */
    public void createLayoutBottom()
    {
        // Derived classes implements additional elements.
    }

    /**
     * Actions-handling method.
     *
     * @param e The event.
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();
        if (source == pathSelectionPanel.getPathInputField())
        {
            parent.navigateNext();
        }

    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return Wether the panel has been validated or not.
     */
    @Override
    public boolean isValidated()
    {
        String chosenPath = pathSelectionPanel.getPath();
        boolean ok = true;

        boolean modifyinstallation = Boolean.valueOf(
                this.installData.getVariable(InstallData.MODIFY_INSTALLATION));
        if (modifyinstallation)
        {
            // installation directory has to exist in a modification installation
            mustExist = true;

            File installationinformation = new File(
                    pathSelectionPanel.getPath() + File.separator + InstallData.INSTALLATION_INFORMATION);
            if (!installationinformation.exists())
            {
                emitError(getString("installer.error"),
                          getString("PathInputPanel.required.forModificationInstallation"));

                return false;
            }
        }

        // We put a warning if the specified target is nameless
        if (chosenPath.length() == 0)
        {
            if (isMustExist())
            {
                emitError(getString("installer.error"), getString("PathInputPanel.required"));
                return false;
            }
            ok = emitWarning(getString("installer.warning"), emptyTargetMsg);
        }
        if (!ok)
        {
            return ok;
        }

        // Expand unix home reference
        if (chosenPath.startsWith("~"))
        {
            String home = System.getProperty("user.home");
            chosenPath = home + chosenPath.substring(1);
        }

        // Normalize the path
        File path = new File(chosenPath).getAbsoluteFile();
        chosenPath = path.toString();
        pathSelectionPanel.setPath(chosenPath);
        if (isMustExist())
        {
            if (!path.exists())
            {
                emitError(getString("installer.error"), getString(getI18nStringForClass("required", "PathInputPanel")));
                return false;
            }
            if (!pathIsValid())
            {
                emitError(getString("installer.error"), getString(getI18nStringForClass("notValid", "PathInputPanel")));
                return false;
            }
        }
        else
        {
            // We assume, that we would install something into this dir
            if (!isWriteable())
            {
                emitError(getString("installer.error"), getI18nStringForClass(
                        "notwritable", "TargetPanel"));
                return false;
            }
            // We put a warning if the directory exists else we warn
            // that it will be created
            if (path.exists())
            {
                int res = askQuestion(getString("installer.warning"), warnMsg,
                                      AbstractUIHandler.CHOICES_YES_NO, AbstractUIHandler.ANSWER_YES);
                ok = res == AbstractUIHandler.ANSWER_YES;
            }
            else
            {
                //if 'ShowCreateDirectoryMessage' variable set to 'false'
                // then don't show "directory will be created" dialog:
                final String vStr =
                        installData.getVariable("ShowCreateDirectoryMessage");
                if (vStr == null || Boolean.getBoolean(vStr))
                {
                    ok = this.emitNotificationFeedback(getI18nStringForClass(
                            "createdir", "TargetPanel") + "\n" + chosenPath);
                }
            }
        }
        return ok;
    }

    /**
     * Returns whether the chosen path is true or not. If existFiles are not null, the existence of
     * it under the choosen path are detected. This method can be also implemented in derived
     * classes to handle special verification of the path.
     *
     * @return true if existFiles are exist or not defined, else false
     */
    protected boolean pathIsValid()
    {
        if (existFiles == null)
        {
            return true;
        }
        for (String existFile : existFiles)
        {
            File path = new File(pathSelectionPanel.getPath(), existFile).getAbsoluteFile();
            if (!path.exists())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the must exist state.
     *
     * @return the must exist state
     */
    public boolean isMustExist()
    {
        return mustExist;
    }

    /**
     * Sets the must exist state. If it is true, the path must exist.
     *
     * @param mustExist must exist state
     */
    public void setMustExist(boolean mustExist)
    {
        this.mustExist = mustExist;
    }

    /**
     * Returns the array of strings which are described the files which must exist.
     *
     * @return paths of files which must exist
     */
    public String[] getExistFiles()
    {
        return existFiles;
    }

    /**
     * Sets the paths of files which must exist under the chosen path.
     *
     * @param strings paths of files which must exist under the chosen path
     */
    public void setExistFiles(String[] strings)
    {
        existFiles = strings;
    }

    /**
     * Loads up the "dir" resource associated with TargetPanel. Acceptable dir resource names:
     * <pre>
     * TargetPanel.dir.macosx
     * TargetPanel.dir.mac
     * TargetPanel.dir.windows
     * TargetPanel.dir.unix
     * TargetPanel.dir.xxx
     * </pre>
     * where xxx is the lower case version of System.getProperty("os.name"),
     * with any spaces replace with underscores
     * TargetPanel.dir (generic that will be applied if none of above is found)
     * As with all IzPack resources, each of the above ids should be associated with a separate
     * filename, which is set in the install.xml file at compile time.
     *
     * @param resources   the resources
     * @param installData the installation data
     * @return the default installation directory for the current installation
     */
    public static String loadDefaultInstallDir(Resources resources, InstallData installData)
    {
        String defaultInstallDir = null;
        BufferedReader br = null;
        try
        {
            InputStream in = null;
            String os = System.getProperty("os.name");
            // first try to look up by specific os name
            os = os.replace(' ', '_'); // avoid spaces in file names
            os = os.toLowerCase(); // for consistency among TargetPanel res
            // files
            try
            {
                in = resources.getInputStream("TargetPanel.dir.".concat(os));
            }
            catch (ResourceNotFoundException rnfe)
            {
            }
            if (in == null)
            {
                Platform platform = installData.getPlatform();
                if (platform.isA(Platform.Name.WINDOWS))
                {
                    try
                    {
                        in = resources.getInputStream("TargetPanel.dir.windows");
                    }
                    catch (ResourceNotFoundException rnfe)
                    {
                    }//it's usual, that the resource does not exist
                }
                else if (platform.isA(Platform.Name.MAC_OSX))
                {
                    try
                    {
                        in = resources.getInputStream("TargetPanel.dir.macosx");
                    }
                    catch (ResourceNotFoundException rnfe)
                    {
                    }//it's usual, that the resource does not exist
                }
                else
                {
                    try
                    {
                        in = resources.getInputStream("TargetPanel.dir.unix");
                    }
                    catch (ResourceNotFoundException eee)
                    {
                    }//it's usual, that the resource does not exist
                }
            }

            // if all above tests failed, there is no resource file,
            // so use system default
            if (in == null)
            {
                try
                {
                    in = resources.getInputStream("TargetPanel.dir");
                }
                catch (ResourceNotFoundException ignored)
                {
                }
            }

            if (in != null)
            {
                // now read the file, once we've identified which one to read
                InputStreamReader isr = new InputStreamReader(in);
                br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null)
                {
                    line = line.trim();
                    // use the first non-blank line
                    if (!"".equals(line))
                    {
                        break;
                    }
                }
                defaultInstallDir = line;

                defaultInstallDir = installData.getVariables().replace(defaultInstallDir);
            }
            else
            {
                defaultInstallDir = installData.getDefaultInstallPath();
                if (defaultInstallDir == null)
                {
                    // Make the  default path point to the current location
                    defaultInstallDir = System.getProperty("user.dir");
                }
            }
        }
        catch (Exception e)
        {
            //mar: what's the common way to log an exception ?
            e.printStackTrace();
            defaultInstallDir = null;
            // leave unset to take the system default set by Installer class
        }
        finally
        {
            try
            {
                if (br != null)
                {
                    br.close();
                }
            }
            catch (IOException ignored)
            {
            }
        }
        return defaultInstallDir;
    }

    /**
     * This method determines whether the chosen dir is writeable or not.
     *
     * @return whether the chosen dir is writeable or not
     */
    public boolean isWriteable()
    {
        File existParent = IoHelper.existingParent(new File(pathSelectionPanel.getPath()));
        if (existParent == null)
        {
            return false;
        }
        // On windows we cannot use canWrite because
        // it looks to the dos flags which are not valid
        // on NT or 2k XP or ...
        if (OsVersion.IS_WINDOWS)
        {
            File tmpFile;
            try
            {
                tmpFile = File.createTempFile("izWrTe", ".tmp", existParent);
                tmpFile.deleteOnExit();
            }
            catch (IOException e)
            {
                logger.log(Level.WARNING, e.toString(), e);
                return false;
            }
            return true;
        }
        return existParent.canWrite();
    }

}

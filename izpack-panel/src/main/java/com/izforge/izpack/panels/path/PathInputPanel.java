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
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;

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
     * Returns the selected path.
     *
     * @return the selected path
     */
    public String getPath()
    {
        String chosenPath = pathSelectionPanel.getPath();
        String normalizedPath = PathInputBase.normalizePath(chosenPath);
        return normalizedPath;
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
     * Indicates whether the panel has been validated or not.
     *
     * @return whether the panel has been validated or not.
     */
    @Override
    public boolean isValidated()
    {
        String path = getPath();
        String normalizedPath = PathInputBase.normalizePath(path);
        File file = new File(normalizedPath).getAbsoluteFile();

        if (normalizedPath.length() == 0 && !checkEmptyPath())
        {
            // Empty path disallowed
            return false;
        }

        pathSelectionPanel.setPath(normalizedPath);

        if (isMustExist())
        {
            if (!checkExists(file) || !pathIsValid(true) || (modifyInstallation() && !checkInstallationInformation(
                    file)))
            {
                return false;
            }
        }
        else
        {
            if (!PathInputBase.isWritable(file))
            {
                emitError(getString("installer.error"), getI18nStringForClass("notwritable", "TargetPanel"));
                return false;
            }

            // We put a warning if the directory exists else we warn that it will be created
            if (file.exists())
            {
                if (!checkOverwrite(file))
                {
                    return false;
                }
            }
            else if (!checkCreateDirectory(file))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * This method is called when the panel becomes active. Default is to do nothing : feel free to
     * implement what you need in your subclasses. A panel becomes active when the user reaches it
     * during the installation process.
     */
    @Override
    public void panelActivate()
    {
        super.panelActivate();
        if (modifyInstallation())
        {
            // installation directory has to exist if an installation is being modified
            mustExist = true;
        }
        PathInputBase.setInstallData(installData);
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
     * Verifies that the specified file exists.
     *
     * @param file the file to check
     * @return {@code true} if the file exists, otherwise {@code false}
     */
    protected boolean checkExists(File file)
    {
        if (!file.exists())
        {
            emitError(getString("installer.error"), getString(getI18nStringForClass("required", "PathInputPanel")));
            return false;
        }
        return true;
    }

    /**
     * Determines if an empty path is allowed.
     *
     * @return {@code true} if an empty path is allowed, otherwise {@code false}
     */
    protected boolean checkEmptyPath()
    {
        if (isMustExist())
        {
            emitError(getString("installer.error"), getI18nStringForClass("required", "PathInputPanel"));
            return false;
        }
        return emitWarning(getString("installer.warning"), emptyTargetMsg);
    }

    /**
     * Verifies that installation information exists in the specified path.
     *
     * @param path the path
     * @return {@code true} if installation information exists, otherwise {@code false}
     */
    protected boolean checkInstallationInformation(File path)
    {
        File info = new File(path, InstallData.INSTALLATION_INFORMATION);
        if (!info.exists())
        {
            emitError(getString("installer.error"),
                      getString("PathInputPanel.required.forModificationInstallation"));

            return false;
        }
        return true;
    }

    /**
     * Determines if required files exist relative to the specified path
     *
     * @return {@code true} if no files are required, or they exist
     */
    protected boolean checkRequiredFilesExist(String path)
    {
        if (existFiles == null || path == null || path.isEmpty())
        {
            return true;
        }
        for (String existFile : existFiles)
        {
            File file = new File(path, existFile).getAbsoluteFile();
            if (!file.exists())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines if the specified directory can be created.
     *
     * @param dir the directory
     * @return {@code true} if the directory may be created, otherwise {@code false}
     */
    protected boolean checkCreateDirectory(File dir)
    {
        boolean result = true;
        //if 'ShowCreateDirectoryMessage' variable set to 'false'
        // then don't show "directory will be created" dialog:
        String show = installData.getVariable("ShowCreateDirectoryMessage");
        if (show == null || Boolean.getBoolean(show))
        {
            result = emitNotificationFeedback(getI18nStringForClass("createdir", "TargetPanel") + "\n" + dir);
        }
        return result;
    }

    /**
     * Determines if an existing directory can be written to.
     *
     * @param dir the directory
     * @return {@code true} if the directory can be written to, otherwise {@code false}
     */
    protected boolean checkOverwrite(File dir)
    {
        return askQuestion(getString("installer.warning"), warnMsg,
                           AbstractUIHandler.CHOICES_YES_NO, AbstractUIHandler.ANSWER_YES)
                == AbstractUIHandler.ANSWER_YES;
    }

    /**
     * Determines if an existing installation is being modified.
     *
     * @return {@code true} if an installation is being modified, otherwise {@code false}
     */
    protected boolean modifyInstallation()
    {
        return Boolean.valueOf(installData.getVariable(InstallData.MODIFY_INSTALLATION));
    }

    /**
     * Same as calling {@link #pathIsValid(boolean) pathIsValid(false)}.
     */
    protected boolean pathIsValid()
    {
        return pathIsValid(false);
    }

    /**
     * Returns whether the chosen path is valid or not. If existFiles are not null, the existence of
     * it under the chosen path are detected. This method can be also implemented in derived
     * classes to handle special verification of the path.
     *
     * @return true if existFiles are exist or not defined, else false
     */
    protected boolean pathIsValid(boolean notifyUserIfInvalid)
    {
        String pathToBeChecked = getPath();
        boolean isValid = checkRequiredFilesExist(pathToBeChecked);
        if (!isValid && notifyUserIfInvalid)
        {
            String errMsg = getString(getI18nStringForClass("notValid", "PathInputPanel"));
            logger.log(Level.WARNING, String.format("%s: '%s'", errMsg, pathToBeChecked));
            emitError(getString("installer.error"), errMsg);
        }
        return isValid;
    }

}

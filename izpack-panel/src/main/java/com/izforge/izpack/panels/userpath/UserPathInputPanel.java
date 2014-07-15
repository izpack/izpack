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

package com.izforge.izpack.panels.userpath;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 * @author Jeff Gordon
 */
public class UserPathInputPanel extends IzPanel implements ActionListener
{
    private static final long serialVersionUID = 3257566217698292531L;

    private static final transient Logger logger = Logger.getLogger(UserPathInputPanel.class.getName());

    /**
     * Flag whether the choosen path must exist or not
     */
    protected boolean _mustExist = false;
    protected boolean _loadedDefaultDir = false;
    /**
     * Files which should be exist
     */
    protected String[] _existFiles = null;
    /** The path which was chosen */
    // protected String chosenPath;
    /**
     * The path selection sub panel
     */
    protected UserPathSelectionPanel _pathSelectionPanel;
    protected String _error;
    protected String _warn;
    protected String _emptyTargetMsg;
    protected String _warnMsg;
    protected String _reqMsg;
    protected String _notValidMsg;
    protected String _notWritableMsg;
    protected String _createDirMsg;
    protected String _defaultDir = null;
    protected String _thisPanel = "UserPathInputPanel";
    protected String _defaultPanelName = "TargetPanel";
    protected String _targetPanel = "UserPathPanel";
    protected String _variableName = "pathVariable";

    /**
     * Constructs an <tt>UserPathInputPanel</tt>.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent window
     * @param installData the installation data
     * @param targetPanel the target panel
     * @param resources   the resources
     * @param log         the log
     */
    public UserPathInputPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, String targetPanel,
                              Resources resources, Log log)
    {
        super(panel, parent, installData, new IzPanelLayout(log), resources);
        _targetPanel = targetPanel;
        _variableName = getString(targetPanel + ".variableName");

        String mustExist;
        if ((mustExist = panel.getConfiguration("mustExist")) != null) {
            this._mustExist = Boolean.parseBoolean(mustExist);
        }

        // Set default values
        loadMessages();
        String introText = getI18nStringForClass("extendedIntro", _thisPanel);
        if (introText == null || introText.endsWith("extendedIntro") || introText.indexOf('$') > -1)
        {
            introText = getI18nStringForClass("intro", _thisPanel);
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
        add(createLabel("info", _targetPanel, "open", LEFT, true), NEXT_LINE);
        // Create path selection components and add they to this panel.
        _pathSelectionPanel = new UserPathSelectionPanel(this, installData, _targetPanel, _variableName, log);
        add(_pathSelectionPanel, NEXT_LINE);
        createLayoutBottom();
        getLayoutHelper().completeLayout();
    }

    /**
     * This method does nothing. It is called from ctor of UserPathInputPanel, to give in a derived
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
        if (source == _pathSelectionPanel.getPathInputField())
        {
            parent.navigateNext();
        }

    }

    private void loadMessages()
    {
        _error = getString("installer.error");
        _warn = getString("installer.warning");
        _reqMsg = getMessage("required");
        _emptyTargetMsg = getMessage("empty_target");
        _warnMsg = getMessage("exists_warn");
        _notValidMsg = getMessage("notValid");
        _notWritableMsg = getMessage("notwritable");
        _createDirMsg = getMessage("createdir");
    }

    private String getMessage(String type)
    {
        String msg = null;
        msg = getI18nStringForClass(type, _targetPanel);
        if (msg == null)
        {
            msg = getI18nStringForClass(type, _defaultPanelName);
        }
        return msg;
    }

    /**
     * Indicates whether the panel has been validated or not.
     *
     * @return Whether the panel has been validated or not.
     */
    @Override
    public boolean isValidated()
    {
        String chosenPath = _pathSelectionPanel.getPath();
        boolean ok = true;
        // We put a warning if the specified target is nameless
        if (chosenPath.length() == 0)
        {
            if (isMustExist())
            {
                emitError(_error, _reqMsg);
                return false;
            }
            ok = emitWarning(_warn, _emptyTargetMsg);
        }
        if (!ok)
        {
            return ok;
        }
        // Normalize the path
        File path = new File(chosenPath).getAbsoluteFile();
        chosenPath = path.toString();
        _pathSelectionPanel.setPath(chosenPath);
        if (isMustExist())
        {
            if (!path.exists())
            {
                emitError(_error, _reqMsg);
                return false;
            }
            if (!pathIsValid())
            {
                emitError(_error, _notValidMsg);
                return false;
            }
        }
        else
        {
            // We assume, that we would install something into this dir
            if (!isWriteable())
            {
                emitError(_error, _notWritableMsg);
                return false;
            }
            // We put a warning if the directory exists else we warn
            // that it will be created
            if (path.exists())
            {
                int res = askQuestion(_warn, _warnMsg,
                                      AbstractUIHandler.CHOICES_YES_NO, AbstractUIHandler.ANSWER_YES);
                ok = res == AbstractUIHandler.ANSWER_YES;
            }
            else
            {
                ok = this.emitNotificationFeedback(_createDirMsg + "\n" + chosenPath);
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
        if (_existFiles == null)
        {
            return true;
        }
        for (String _existFile : _existFiles)
        {
            File path = new File(_pathSelectionPanel.getPath(), _existFile).getAbsoluteFile();
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
        return _mustExist;
    }

    /**
     * Sets the must exist state. If it is true, the path must exist.
     *
     * @param mustExist must exist state
     */
    public void setMustExist(boolean mustExist)
    {
        _mustExist = mustExist;
    }

    /**
     * Returns the array of strings which are described the files which must exist.
     *
     * @return paths of files which must exist
     */
    public String[] getExistFiles()
    {
        return _existFiles;
    }

    /**
     * Sets the paths of files which must exist under the chosen path.
     *
     * @param strings paths of files which must exist under the chosen path
     */
    public void setExistFiles(String[] strings)
    {
        _existFiles = strings;
    }

    /**
     * "targetPanel" is typically the class name of the implementing panel, such as
     * "UserPathPanel" or "TargetPanel" set when the class is created, but can be set
     * with setDefaultDir().
     * Loads up the "dir" resource associated with targetPanel. Acceptable dir resource names:
     * <code>
     * targetPanel.dir.macosx
     * targetPanel.dir.mac
     * targetPanel.dir.windows
     * targetPanel.dir.unix
     * targetPanel.dir.xxx,
     * where xxx is the lower case version of System.getProperty("os.name"),
     * with any spaces replace with underscores
     * targetPanel.dir (generic that will be applied if none of above is found)
     * </code>
     * As with all IzPack resources, each the above ids should be associated with a separate
     * filename, which is set in the install.xml file at compile time.
     */
    private void loadDefaultDir()
    {
        // Load only once ...
        if (!(_loadedDefaultDir))
        {
            Resources resources = getResources();
            BufferedReader reader = null;
            try
            {
                InputStream in = null;
                String os = System.getProperty("os.name");
                // first try to look up by specific os name
                os = os.replace(' ', '_'); // avoid spaces in file names
                os = os.toLowerCase(); // for consistency among targetPanel res files
                try
                {
                    in = resources.getInputStream(_targetPanel + ".dir.".concat(os));
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
                            in = resources.getInputStream(_targetPanel + ".dir.windows");
                        }
                        catch (ResourceNotFoundException rnfe)
                        {
                        }//it's usual, that the resource does not exist
                    }
                    else if (platform.isA(Platform.Name.MAC_OSX))
                    {
                        try
                        {
                            in = resources.getInputStream(_targetPanel + ".dir.mac");
                        }
                        catch (ResourceNotFoundException rnfe)
                        {
                        }//it's usual, that the resource does not exist
                    }
                    else
                    {
                        try
                        {
                            in = resources.getInputStream(_targetPanel + ".dir.unix");
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
                        in = resources.getInputStream(_targetPanel + ".dir");
                    }
                    catch (ResourceNotFoundException eee)
                    {
                    }
                }
                if (in != null)
                {
                    // now read the file, once we've identified which one to read
                    InputStreamReader isr = new InputStreamReader(in);
                    reader = new BufferedReader(isr);
                    String line;
                    while ((line = reader.readLine()) != null)
                    {
                        line = line.trim();
                        // use the first non-blank line
                        if (!"".equals(line))
                        {
                            break;
                        }
                    }
                    _defaultDir = line;
                    _defaultDir = installData.getVariables().replace(_defaultDir);
                }
            }
            catch (Exception e)
            {
                logger.log(Level.WARNING, e.getMessage(), e);
                _defaultDir = null;
                // leave unset to take the system default set by Installer class
            }
            finally
            {
                try
                {
                    if (reader != null)
                    {
                        reader.close();
                    }
                }
                catch (IOException ignored)
                {
                }
            }
        }
        _loadedDefaultDir = true;
    }

    /**
     * This method determines whether the chosen dir is writeable or not.
     *
     * @return whether the chosen dir is writeable or not
     */
    public boolean isWriteable()
    {
        File existParent = IoHelper.existingParent(new File(_pathSelectionPanel.getPath()));
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

    /**
     * Returns the default for the directory.
     *
     * @return the default for the directory
     */
    public String getDefaultDir()
    {
        if (_defaultDir == null && (!(_loadedDefaultDir)))
        {
            loadDefaultDir();
        }
        return _defaultDir;
    }

    /**
     * Sets the default for the directory to the given string.
     *
     * @param defaultDir path for default directory
     */
    public void setDefaultDir(String defaultDir)
    {
        _defaultDir = defaultDir;
    }

    /**
     * Returns the panel name extending this class.
     * Used for looking up localized text and resources.
     *
     * @return the default for the directory
     */
    public String getTargetPanel()
    {
        return _targetPanel;
    }

    /**
     * Sets the panel name extending this class.
     * Used for looking up localized text and resources.
     *
     * @param targetPanel path for default directory
     */
    public void setTargetPanel(String targetPanel)
    {
        _targetPanel = targetPanel;
    }
}

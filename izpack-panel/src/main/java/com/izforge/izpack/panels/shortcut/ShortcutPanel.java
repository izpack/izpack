/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://www.izforge.com/izpack/ http://izpack.codehaus.org/
 *
 * Copyright 2002 Elmar Grom
 * Copyright 2010 Florian Buehlmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.izforge.izpack.panels.shortcut;

import static com.izforge.izpack.util.Platform.Name.UNIX;
import static com.izforge.izpack.util.Platform.Name.WINDOWS;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.MultiLineLabel;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.event.InstallerListeners;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.PlatformModelMatcher;
import com.izforge.izpack.util.TargetFactory;
import com.izforge.izpack.util.os.Shortcut;

/**
 * This class implements a panel for the creation of shortcuts. The panel prompts the user to select
 * a program group for shortcuts, accept the creation of desktop shortcuts and actually creates the
 * shortcuts.
 * <p/>
 * Use LateShortcutInstallListener to create the Shortcuts after the Files have been installed.
 *
 * @version $Revision$
 */
public class ShortcutPanel extends IzPanel implements ActionListener, ListSelectionListener
{

    /**
     * serialVersionUID = 3256722870838112311L
     */
    private static final long serialVersionUID = 3256722870838112311L;

    /**
     * The default file name for the text file in which the shortcut information should be stored,
     * in case shortcuts can not be created on a particular target system. TEXT_FILE_NAME =
     * "Shortcuts.txt"
     */
    private static final String TEXT_FILE_NAME = "Shortcuts.txt";

    /**
     * Determines if the panel has been initialised.
     */
    private boolean initialised = false;

    private boolean isRootUser;

    /**
     * UI element to present the list of existing program groups for selection
     */
    private JList groupList;

    /**
     * UI element to present the default name for the program group and to support editing of this
     * name.
     */
    private JTextField programGroup;

    /**
     * UI element to allow the user to revert to the default name of the program group
     */
    private JButton defaultButton;

    /**
     * UI element to allow the user to save a text file with the shortcut information
     */
    private JButton saveButton;

    /**
     * UI element to allow the user to decide if shortcuts should be placed on the desktop or not.
     */
    private JCheckBox allowDesktopShortcut;

    /**
     * Checkbox to enable/disable to create ShortCuts
     */
    private JCheckBox createShortcuts;

    /**
     * UI element instruct this panel to create shortcuts for the current user only
     */
    private JRadioButton currentUser;

    /**
     * UI element instruct this panel to create shortcuts for all users
     */
    private JRadioButton allUsers;

    /**
     * The layout for this panel
     */
    private GridBagLayout layout;

    /**
     * The contraints object to use whan creating the layout
     */
    private GridBagConstraints constraints;

    private ShortcutPanelLogic shortcutPanelLogic;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(ShortcutPanel.class.getName());


    /**
     * Constructs a <tt>ShortcutPanel</tt>.
     *
     * @param panel         the panel
     * @param parent        reference to the application frame
     * @param installData   the installation data
     * @param resources     the resources
     * @param uninstallData the uninstallation data
     * @param housekeeper   the house keeper
     * @param factory       the factory for platform-specific implementations
     * @param matcher       the platform-model matcher
     */
    public ShortcutPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
                         UninstallData uninstallData, Housekeeper housekeeper, TargetFactory factory,
                         InstallerListeners listeners, PlatformModelMatcher matcher)
    {
        super(panel, parent, installData, "link16x16", resources);
        layout = (GridBagLayout) super.getLayout();
        Object con = getLayoutHelper().getDefaultConstraints();
        if (con instanceof GridBagConstraints)
        {
            constraints = (GridBagConstraints) con;
        }
        else
        {
            constraints = new GridBagConstraints();
        }
        setLayout(super.getLayout());
        try
        {
            shortcutPanelLogic = new ShortcutPanelLogic(installData, resources, uninstallData, housekeeper,
                                                        factory, listeners, matcher);
        }
        catch (Exception exception)
        {
            logger.log(Level.WARNING, "Failed to initialise shortcuts: " + exception.getMessage(), exception);
        }
    }

    /**
     * This method represents the ActionListener interface, invoked when an action occurs.
     *
     * @param event the action event.
     */

    /*--------------------------------------------------------------------------*/
    @Override
    public void actionPerformed(ActionEvent event)
    {
        Object eventSource = event.getSource();

        // ----------------------------------------------------
        // create shortcut for the current user was selected
        // refresh the list of program groups accordingly and
        // reset the program group to the default setting.
        // ----------------------------------------------------
        if (eventSource.equals(currentUser))
        {
            if (groupList != null)
            {
                groupList.setListData(shortcutPanelLogic.getProgramGroups(Shortcut.CURRENT_USER)
                                              .toArray());
            }
            programGroup.setText(shortcutPanelLogic.getSuggestedProgramGroup());
            shortcutPanelLogic.setUserType(Shortcut.CURRENT_USER);

        }

        // ----------------------------------------------------
        // create shortcut for all users was selected
        // refresh the list of program groups accordingly and
        // reset the program group to the default setting.
        // ----------------------------------------------------
        else if (eventSource.equals(allUsers))
        {
            if (groupList != null)
            {
                groupList.setListData(shortcutPanelLogic.getProgramGroups(Shortcut.ALL_USERS)
                                              .toArray());
            }
            programGroup.setText(shortcutPanelLogic.getSuggestedProgramGroup());
            shortcutPanelLogic.setUserType(Shortcut.ALL_USERS);

        }

        // ----------------------------------------------------
        // The reset button was pressed.
        // - clear the selection in the list box, because the
        // selection is no longer valid
        // - refill the program group edit control with the
        // suggested program group name
        // ----------------------------------------------------
        else if (eventSource.equals(defaultButton))
        {
            if (groupList != null && groupList.getSelectionModel() != null)
            {
                groupList.getSelectionModel().clearSelection();
            }
            programGroup.setText(shortcutPanelLogic.getSuggestedProgramGroup());
        }

        // ----------------------------------------------------
        // the save button was pressed. This is a request to
        // save shortcut information to a text file.
        // ----------------------------------------------------
        else if (eventSource.equals(saveButton))
        {
            saveToFile();
        }
        else if (eventSource.equals(createShortcuts))
        {
            boolean create = createShortcuts.isSelected();

            if (groupList != null)
            {
                groupList.setEnabled(create);
            }

            programGroup.setEnabled(create);
            currentUser.setEnabled(create);
            defaultButton.setEnabled(create);

            // ** There where no Desktop Links or not allowed, this may be null: **//
            if (allowDesktopShortcut != null)
            {
                allowDesktopShortcut.setEnabled(create);
            }

            if (isRootUser)
            {
                allUsers.setEnabled(create);
            }
        }
    }

    /**
     * Returns true when all selections have valid settings. This indicates that it is legal to
     * proceed to the next panel.
     *
     * @return true if it is legal to proceed to the next panel, otherwise false.
     */
    @Override
    public boolean isValidated()
    {
        String errorMessage = shortcutPanelLogic.verifyProgramGroup(programGroup.getText());
        if(!errorMessage.isEmpty())
        {
            emitError("Error", errorMessage);
            return false;
        }
        shortcutPanelLogic.setGroupName(programGroup.getText());
        if (allowDesktopShortcut != null)
        {
            shortcutPanelLogic.setCreateDesktopShortcuts(allowDesktopShortcut.isSelected());
        }
        shortcutPanelLogic.setCreateShortcuts(createShortcuts.isSelected());

        if (shortcutPanelLogic.isCreateShortcutsImmediately())
        {
            try
            {
                shortcutPanelLogic.createAndRegisterShortcuts();
            }
            catch (Exception e)
            {
                logger.log(Level.WARNING, e.getMessage(), e);
                // ignore exception
            }
        }
        return true;
    }

    /**
     * Called when the panel is shown to the user.
     */
    @Override
    public void panelActivate()
    {
        if (shortcutPanelLogic != null)
        {
            if (!initialised)
            {
                initialised = true;
                if (shortcutPanelLogic.isSupported())
                {
                    isRootUser = shortcutPanelLogic.initUserType();
                    buildUI(shortcutPanelLogic.getProgramsFolder(shortcutPanelLogic.getUserType()));
                }
                else if (shortcutPanelLogic.isSkipIfNotSupported())
                {
                    parent.skipPanel();
                }
                else
                {
                    buildAlternateUI();
                }
            }
        }
        else
        {
            parent.skipPanel();
        }
    }

    /**
     * This method is called by the groupList when the user makes a selection. It updates the
     * content of the programGroup with the result of the selection.
     *
     * @param event the list selection event
     */

    /*--------------------------------------------------------------------------*/
    @Override
    public void valueChanged(ListSelectionEvent event)
    {
        if (programGroup == null)
        {
            return;
        }

        String value = "";

        try
        {
            value = (String) groupList.getSelectedValue();
        }
        catch (ClassCastException exception)
        {
            // ignore
        }

        if (value == null)
        {
            value = "";
        }

        programGroup
                .setText(value + File.separator + shortcutPanelLogic.getSuggestedProgramGroup());
    }

    /**
     * This method creates the UI for this panel.
     *
     * @param programsFolder Directory containing the existing program groups.
     */
    private void buildUI(File programsFolder)
    {
        int line = 0;
        int col = 0;
        constraints.insets = new Insets(10, 10, 0, 0);

        // Add a CheckBox which enables the user to entirely suppress shortcut creation.
        createShortcuts = new JCheckBox(shortcutPanelLogic.getCreateShortcutsPrompt(), true);
        createShortcuts.setName(GuiId.SHORTCUT_CREATE_CHECK_BOX.id);
        createShortcuts.addActionListener(this);
        constraints.gridx = col;
        constraints.gridy = line + 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;

        // constraints.weightx = 0.1;
        // constraints.weighty = 0.2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;

        layout.addLayoutComponent(createShortcuts, constraints);
        add(createShortcuts);

        constraints.insets = new Insets(0, 10, 0, 0);

        // ----------------------------------------------------
        // check box to allow the user to decide if a desktop
        // shortcut should be created.
        // this should only be created if needed and requested
        // in the definition file.
        // ----------------------------------------------------
        if (shortcutPanelLogic.hasDesktopShortcuts())
        {
            boolean initialAllowedFlag = shortcutPanelLogic.isDesktopShortcutCheckboxSelected();
            allowDesktopShortcut = new JCheckBox(shortcutPanelLogic.getCreateDesktopShortcutsPrompt(),
                                                 initialAllowedFlag);
            constraints.gridx = col;
            constraints.gridy = line + 2;
            constraints.gridwidth = 1;
            constraints.gridheight = 1;

            // constraints.weighty = 0.2;
            // constraints.weighty = 1.0;
            // constraints.weighty = 0.5;
            layout.addLayoutComponent(allowDesktopShortcut, constraints);
            add(allowDesktopShortcut);
        }

        // label the list of existing program groups
        JLabel listLabel = LabelFactory.create(getString("ShortcutPanel.regular.list"), SwingConstants.LEADING);
        Platform platform = installData.getPlatform();
        shortcutPanelLogic.setPlatform(platform);
        if (platform.isA(WINDOWS))
        {
            constraints.gridx = col;
            constraints.gridy = line + 3;

            constraints.gridwidth = 1;
            constraints.gridheight = 1;

            constraints.insets = new Insets(10, 10, 0, 0);

            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.NORTHWEST;
        }
        else
        {
            constraints.gridx = col;
            constraints.gridy = line + 4;

            constraints.gridwidth = 1;
            constraints.gridheight = 1;

            constraints.insets = new Insets(10, 10, 0, 0);

            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.SOUTHWEST;
        }
        layout.addLayoutComponent(listLabel, constraints);
        add(listLabel);

        // ----------------------------------------------------
        // list box to list all of already existing folders as program groups
        // at the intended destination
        // ----------------------------------------------------
        Vector<String> dirEntries = new Vector<String>();

        File[] entries = programsFolder.listFiles();

        // Quickfix prevent NullPointer on non default compliant Linux - KDEs
        // i.e Mandrake 2005 LE stores from now also in "applnk" instead in prior "applnk-mdk":
        if (entries != null && !platform.isA(UNIX))
        {
            for (File entry : entries)
            {
                if (entry.isDirectory())
                {
                    dirEntries.add(entry.getName());
                }
            }
        }
        if (platform.isA(WINDOWS))
        {
            if (groupList == null)
            {
                groupList = new JList();
            }

            groupList = addList(dirEntries, ListSelectionModel.SINGLE_SELECTION, groupList, col,
                                line + 4, 1, 1, GridBagConstraints.BOTH);
        }

        // ----------------------------------------------------
        // radio buttons to select current user or all users.
        // ----------------------------------------------------
        if (shortcutPanelLogic.isSupportingMultipleUsers())
        {
            // if 'defaultCurrentUser' specified, default to current user:
            final boolean rUserFlag = !shortcutPanelLogic.isDefaultCurrentUserFlag() && isRootUser;

            JPanel usersPanel = new JPanel(new GridLayout(2, 1));
            ButtonGroup usersGroup = new ButtonGroup();
            currentUser = new JRadioButton(shortcutPanelLogic.getCreateForCurrentUserPrompt(), !rUserFlag);
            currentUser.addActionListener(this);
            usersGroup.add(currentUser);
            usersPanel.add(currentUser);
            allUsers = new JRadioButton(shortcutPanelLogic.getCreateForAllUsersPrompt(), rUserFlag);

            logger.fine("allUsers.setEnabled(), am I root?: " + isRootUser);

            allUsers.setEnabled(isRootUser);

            allUsers.addActionListener(this);
            usersGroup.add(allUsers);
            usersPanel.add(allUsers);

            TitledBorder border = new TitledBorder(new EmptyBorder(2, 2, 2, 2),
                                                   shortcutPanelLogic.getCreateForUserPrompt());
            usersPanel.setBorder(border);
            if (platform.isA(WINDOWS))
            {
                constraints.gridx = col + 1;
                constraints.gridy = line + 4;
                constraints.gridwidth = 1;
                constraints.gridheight = 1;
            }
            else
            {
                constraints.insets = new Insets(10, 10, 20, 0);
                constraints.gridx = col;
                constraints.gridy = line + 4;
                constraints.gridwidth = 2;
                constraints.gridheight = 1;
                constraints.anchor = GridBagConstraints.EAST;
            }

            // constraints.weighty = 1.0;
            // constraints.weightx = 1.0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            layout.addLayoutComponent(usersPanel, constraints);
            add(usersPanel);
        }

        // ----------------------------------------------------
        // edit box that contains the suggested program group
        // name, which can be modfied or substituted from the
        // list by the user
        // ----------------------------------------------------
        String suggestedProgramGroup = shortcutPanelLogic.getSuggestedProgramGroup();
        programGroup = new JTextField(suggestedProgramGroup, 40); // 40?

        constraints.gridx = col;
        constraints.gridy = line + 5;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;

        // constraints.weighty = 1.0;
        // constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        layout.addLayoutComponent(programGroup, constraints);
        add(programGroup);

        // ----------------------------------------------------
        // reset button that allows the user to revert to the
        // original suggestion for the program group
        // ----------------------------------------------------
        defaultButton = ButtonFactory.createButton(getString("ShortcutPanel.regular.default"),
                                                   installData.buttonsHColor);
        defaultButton.addActionListener(this);

        constraints.gridx = col + 1;
        constraints.gridy = line + 5;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        layout.addLayoutComponent(defaultButton, constraints);
        add(defaultButton);

        if (suggestedProgramGroup == null || "".equals(suggestedProgramGroup))
        {
            programGroup.setVisible(false);
            defaultButton.setVisible(false);
            listLabel.setVisible(false);
        }
    }

    /**
     * Adds the grouplist to the panel
     *
     * @param Entries     the entries to display
     * @param ListModel   the model to use
     * @param aJList      the JList to use
     * @param aGridx      The X position in the gridbag layout.
     * @param aGridy      The Y position in the gridbag layout.
     * @param aGridwidth  the gridwith to use in the gridbag layout.
     * @param aGridheight the gridheight to use in the gridbag layout.
     * @param aFill       the FILL to use in the gridbag layout.
     * @return the filled JList
     */
    private JList addList(Vector<String> Entries, int ListModel, JList aJList, int aGridx,
                          int aGridy, int aGridwidth, int aGridheight, int aFill)
    {
        if (aJList == null)
        {
            aJList = new JList(Entries);
        }
        else
        {
            aJList.setListData(Entries);
        }

        aJList.setSelectionMode(ListModel);
        aJList.getSelectionModel().addListSelectionListener(this);

        JScrollPane scrollPane = new JScrollPane(aJList);

        constraints.gridx = aGridx;
        constraints.gridy = aGridy;
        constraints.gridwidth = aGridwidth;
        constraints.gridheight = aGridheight;
        constraints.weightx = 2.0;
        constraints.weighty = 1.5;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = aFill;
        layout.addLayoutComponent(scrollPane, constraints);
        add(scrollPane);

        return aJList;
    }

    /**
     * This method creates an alternative UI for this panel. This UI can be used when the creation
     * of shortcuts is not supported on the target system. It displays an apology for the inability
     * to create shortcuts on this system, along with information about the intended targets. In
     * addition, there is a button that allows the user to save more complete information in a text
     * file. Based on this information the user might be able to create the necessary shortcut him
     * or herself. At least there will be information about how to launch the application.
     */
    private void buildAlternateUI()
    {
        layout = new GridBagLayout();
        constraints = new GridBagConstraints();
        setLayout(layout);

        // ----------------------------------------------------
        // static text a the top of the panel, that apologizes
        // about the fact that we can not create shortcuts on
        // this particular target OS.
        // ----------------------------------------------------
        MultiLineLabel apologyLabel = new MultiLineLabel(getString("ShortcutPanel.alternate.apology"), 0, 0);

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        layout.addLayoutComponent(apologyLabel, constraints);
        add(apologyLabel);

        // ----------------------------------------------------
        // label that explains the significance ot the list box
        // ----------------------------------------------------
        MultiLineLabel listLabel = new MultiLineLabel(getString("ShortcutPanel.alternate.targetsLabel"), 0, 0);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        layout.addLayoutComponent(listLabel, constraints);
        add(listLabel);

        // ----------------------------------------------------
        // list box to list all of the intended shortcut targets
        // ----------------------------------------------------
        Vector<String> targets = new Vector<String>();
        if (shortcutPanelLogic != null)
        {
            targets.addAll(shortcutPanelLogic.getTargets());
        }

        // list the intended shortcut targets
        JList targetList = new JList(targets);

        JScrollPane scrollPane = new JScrollPane(targetList);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.BOTH;
        layout.addLayoutComponent(scrollPane, constraints);
        add(scrollPane);

        // ----------------------------------------------------
        // static text that explains about the text file
        // ----------------------------------------------------
        MultiLineLabel fileExplanation = new MultiLineLabel(getString("ShortcutPanel.alternate.textFileExplanation"),
                                                            0, 0);

        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        layout.addLayoutComponent(fileExplanation, constraints);
        add(fileExplanation);

        // ----------------------------------------------------
        // button to save the text file
        // ----------------------------------------------------
        saveButton = ButtonFactory.createButton(getString("ShortcutPanel.alternate.saveButton"),
                                                installData.buttonsHColor);
        saveButton.addActionListener(this);

        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.CENTER;
        layout.addLayoutComponent(saveButton, constraints);
        add(saveButton);
    }

    @Override
    public Dimension getSize()
    {
        Dimension size = getParent().getSize();
        Insets insets = getInsets();
        Border border = getBorder();
        Insets borderInsets = new Insets(0, 0, 0, 0);

        if (border != null)
        {
            borderInsets = border.getBorderInsets(this);
        }

        size.height = size.height - insets.top - insets.bottom - borderInsets.top
                - borderInsets.bottom - 50;
        size.width = size.width - insets.left - insets.right - borderInsets.left
                - borderInsets.right - 50;

        return (size);
    }

    /**
     * This method saves all shortcut information to a text file.
     */
    private void saveToFile()
    {
        File file;

        // ----------------------------------------------------
        // open a file chooser dialog to get a path / file name
        // ----------------------------------------------------
        JFileChooser fileDialog = new JFileChooser(installData.getInstallPath());
        fileDialog.setSelectedFile(new File(TEXT_FILE_NAME));

        if (fileDialog.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            file = fileDialog.getSelectedFile();
        }
        else
        {
            return;
        }

        shortcutPanelLogic.saveToFile(file);
    }

    @Override
    public void createInstallationRecord(IXMLElement panelRoot)
    {
        try
        {
            new ShortcutPanelAutomationHelper(shortcutPanelLogic).createInstallationRecord(installData, panelRoot);
        }
        catch (Exception e)
        {
            logger.log(Level.WARNING, "Could generate automatic installer description for shortcuts.");
        }
    }
}

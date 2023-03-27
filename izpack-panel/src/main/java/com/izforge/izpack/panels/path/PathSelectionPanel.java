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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.IzPanelConstraints;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LayoutConstants;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.installer.gui.LayoutHelper;

/**
 * This is a sub panel which contains a text field and a browse button for path selection. This is
 * NOT an IzPanel, else it is made to use in an IzPanel for any path selection. If the IzPanel
 * parent implements ActionListener, the ActionPerformed method will be called, if
 * PathSelectionPanel.ActionPerformed was called with a source other than the browse button. This
 * can be used to perform parentFrame.navigateNext in the IzPanel parent. An example implementation
 * is done in com.izforge.izpack.panels.path.PathInputPanel.
 *
 * @author Klaus Bartz
 */
public class PathSelectionPanel extends JPanel implements ActionListener, LayoutConstants
{

    /**
     *
     */
    private static final long serialVersionUID = 3618700794577105718L;

    /**
     * The text field for the path.
     */
    private JTextField textField;

    /**
     * The 'browse' button.
     */
    private JButton browseButton;

    /**
     * IzPanel parent (not the InstallerFrame).
     */
    private IzPanel parent;

    /**
     * The installation data.
     */
    private GUIInstallData installData;

    /**
     * The target panel (panel which uses this PathSelectionPanel as sub-panel).
     */
    private String targetPanel;

    /**
     * The log.
     */
    private final Log log;

    /**
     * Constructs a <tt>PathSelectionPanel</tt>.
     *
     * @param parent       the parent panel
     * @param installData  the installation data
     * @param targetPanel  the target panel
     * @param log          the log
     */
    public PathSelectionPanel(IzPanel parent, GUIInstallData installData, String targetPanel, Log log)
    {
        super();
        this.parent = parent;
        this.installData = installData;
        this.targetPanel = targetPanel;
        this.log = log;
        createLayout();
    }

    /**
     * Creates the layout for this sub panel.
     */
    protected void createLayout()
    {
        // We would use the IzPanelLayout also in this "sub" panel.
        // In an IzPanel there is support for this layout manager in
        // more than one place, but not in this panel. So, we have
        // to make all things needed.
        // First create a layout helper.
        LayoutHelper layoutHelper = new LayoutHelper(this, installData);
        // Start the layout.
        layoutHelper.startLayout(new IzPanelLayout(log));
        // One of the rare points we need explicit a constraints.
        IzPanelConstraints ipc = IzPanelLayout.getDefaultConstraint(TEXT_CONSTRAINT);
        // The text field should be stretched.
        ipc.setXStretch(1.0);
        textField = new JTextField(50);
        textField.addActionListener(this);
        textField.setName(GuiId.PATH_SELECTION_PANEL_PATH_TEXT_FIELD.id);
        parent.setInitialFocus(textField);
        add(textField, ipc);
        // We would have place between text field and button.
        add(IzPanelLayout.createHorizontalFiller(3));
        // No explicit constraints for the button (else implicit) because
        // defaults are OK.
        String buttonText = parent.getInstallerFrame().getMessages().get(targetPanel + ".browse");
        browseButton = ButtonFactory.createButton(buttonText, parent.getInstallerFrame().getIcons().get("open"),
                                                  installData.buttonsHColor);
        browseButton.setName(GuiId.BUTTON_BROWSE.id);
        browseButton.addActionListener(this);
        add(browseButton);
    }

    // There are problems with the size if no other component needs the
    // full size. Sometimes directly, sometimes only after a back step.

    @Override
    public Dimension getMinimumSize()
    {
        Dimension preferredSize = super.getPreferredSize();
        Dimension retval = parent.getSize();
        retval.height = preferredSize.height;
        return (retval);
    }

    /**
     * Actions-handling method.
     *
     * @param e The event.
     */
    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();

        if (source == browseButton)
        {
            // The user wants to browse its filesystem

            // Prepares the file chooser
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setName(GuiId.PATH_SELECTION_PANEL_FILE_CHOOSER.id);
            fileChooser.setCurrentDirectory(new File(textField.getText()));
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.addChoosableFileFilter(fileChooser.getAcceptAllFileFilter());

            // Shows it
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                String path = fileChooser.getSelectedFile().getAbsolutePath();
                textField.setText(path);
            }

        }
        else
        {
            if (parent instanceof ActionListener)
            {
                ((ActionListener) parent).actionPerformed(e);
            }
        }
    }

    /**
     * Returns the chosen path.
     *
     * @return the chosen path
     */
    public String getPath()
    {
        return (textField.getText());
    }

    /**
     * Sets the contents of the text field to the given path.
     *
     * @param path the path to be set
     */
    public void setPath(String path)
    {
        textField.setText(path);
    }

    /**
     * Returns the text input field for the path. This methode can be used to differ in a
     * ActionPerformed method of the parent between the browse button and the text field.
     *
     * @return the text input field for the path
     */
    public JTextField getPathInputField()
    {
        return textField;
    }

    /**
     * Returns the browse button object for modification or for use with a different ActionListener.
     *
     * @return the browse button to open the JFileChooser
     */
    public JButton getBrowseButton()
    {
        return browseButton;
    }

}

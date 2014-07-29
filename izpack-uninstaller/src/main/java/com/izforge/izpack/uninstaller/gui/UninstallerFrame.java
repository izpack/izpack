/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.uninstaller.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.GUIPrompt;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.uninstaller.Destroyer;
import com.izforge.izpack.uninstaller.event.DestroyerListener;
import com.izforge.izpack.uninstaller.resource.InstallLog;
import com.izforge.izpack.util.Housekeeper;

/**
 * The uninstaller frame class.
 *
 * @author Julien Ponge
 */
public class UninstallerFrame extends JFrame
{

    /**
     * The icons database.
     */
    private final IconsDatabase icons;

    /**
     * The locale-specific messages.
     */
    private final Messages messages;

    /**
     * The target destroy checkbox.
     */
    protected JCheckBox targetDestroyCheckbox;

    /**
     * The progress bar.
     */
    private JProgressBar progressBar;

    /**
     * The destroy button.
     */
    private JButton destroyButton;

    /**
     * The quit button.
     */
    private JButton quitButton;

    /**
     * The buttons hover color.
     */
    private final Color buttonsHColor = new Color(230, 230, 230);

    /**
     * The installation log.
     */
    private final InstallLog log;

    /**
     * The destroyer.
     */
    private final Destroyer destroyer;

    /**
     * The housekeeper.
     */
    private final Housekeeper housekeeper;


    /**
     * The constructor.
     *
     * @param destroyer   the destroyer
     * @param housekeeper the housekeeper
     * @param messages    the locale-specific messages
     */
    public UninstallerFrame(Destroyer destroyer, InstallLog log, Housekeeper housekeeper, Messages messages)
            throws Exception
    {
        super("IzPack - Uninstaller");
        this.destroyer = destroyer;
        this.log = log;
        this.housekeeper = housekeeper;
        this.messages = messages;

        // Initializations
        icons = new IconsDatabase();
        loadIcons();
        UIManager.put("OptionPane.yesButtonText", this.messages.get("installer.yes"));
        UIManager.put("OptionPane.noButtonText", this.messages.get("installer.no"));
        UIManager.put("OptionPane.cancelButtonText", this.messages.get("installer.cancel"));

        // Sets the frame icon
        setIconImage(icons.get("JFrameIcon").getImage());

        destroyer.setProgressListener(new DestroyerListener()
        {
            @Override
            public void startAction(String name, int steps)
            {
                started(steps);
            }

            @Override
            public void progress(int subStep, String message)
            {
                UninstallerFrame.this.progress(subStep, message);
            }

            @Override
            public void stopAction()
            {
                finished();
            }

        });
    }

    /**
     * Initialises the frame
     *
     * @param displayForceOption If true, display to the user the option permitting to force all files deletion.
     * @param forceOptionState   If true, force deletion is activated.
     */
    public void init(boolean displayForceOption, boolean forceOptionState)
    {
        buildGUI(displayForceOption, forceOptionState);
        addWindowListener(new WindowHandler());
        pack();
        centerFrame(this);
        setResizable(false);
        setVisible(true);
    }

    /**
     * Builds the GUI.
     *
     * @param displayForceOption If true, display to the user the option permitting to force
     *                           all files deletion.
     * @param forceOptionState   If true, force deletion is activated.
     */
    private void buildGUI(boolean displayForceOption, boolean forceOptionState)
    {
        // We initialize our layout
        JPanel contentPane = (JPanel) getContentPane();
        GridBagLayout layout = new GridBagLayout();
        contentPane.setLayout(layout);
        GridBagConstraints gbConstraints = new GridBagConstraints();
        gbConstraints.insets = new Insets(5, 5, 5, 5);

        // We prepare our action handler
        ActionsHandler handler = new ActionsHandler();

        // Prepares the glass pane to block gui interaction when needed
        JPanel glassPane = (JPanel) getGlassPane();
        glassPane.addMouseListener(new MouseAdapter()
        {
        });
        glassPane.addMouseMotionListener(new MouseMotionAdapter()
        {
        });
        glassPane.addKeyListener(new KeyAdapter()
        {
        });

        // We set-up the buttons factory
        ButtonFactory.useButtonIcons();
        ButtonFactory.useHighlightButtons();

        // We put our components

        JLabel warningLabel = new JLabel(messages.get("uninstaller.warning"), icons
                .get("warning"), JLabel.TRAILING);
        buildConstraints(gbConstraints, 0, 0, 2, 1, 1.0, 0.0);
        gbConstraints.anchor = GridBagConstraints.WEST;
        gbConstraints.fill = GridBagConstraints.NONE;
        layout.addLayoutComponent(warningLabel, gbConstraints);
        contentPane.add(warningLabel);

        targetDestroyCheckbox = new JCheckBox(messages.get("uninstaller.destroytarget")
                                                      + log.getInstallPath(), forceOptionState);
        buildConstraints(gbConstraints, 0, 1, 2, 1, 1.0, 0.0);
        layout.addLayoutComponent(targetDestroyCheckbox, gbConstraints);
        if (displayForceOption)
        {
            contentPane.add(targetDestroyCheckbox);
        }
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString(messages.get("InstallPanel.begin"));
        buildConstraints(gbConstraints, 0, 2, 2, 1, 1.0, 0.0);
        layout.addLayoutComponent(progressBar, gbConstraints);
        contentPane.add(progressBar);

        destroyButton = ButtonFactory.createButton(messages.get("uninstaller.uninstall"),
                                                   icons.get("delete"), buttonsHColor);
        destroyButton.addActionListener(handler);
        buildConstraints(gbConstraints, 0, 3, 1, 1, 0.5, 0.0);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.WEST;
        layout.addLayoutComponent(destroyButton, gbConstraints);
        contentPane.add(destroyButton);

        quitButton = ButtonFactory.createButton(messages.get("installer.quit"), icons
                .get("stop"), buttonsHColor);
        quitButton.addActionListener(handler);
        buildConstraints(gbConstraints, 1, 3, 1, 1, 0.5, 0.0);
        gbConstraints.anchor = GridBagConstraints.EAST;
        layout.addLayoutComponent(quitButton, gbConstraints);
        contentPane.add(quitButton);

        // intercept error messages and display them in the progress bar
        destroyer.setPrompt(new GUIPrompt()
        {
            @Override
            public void message(Type type, String message)
            {
                super.message(type, message);
                if (type == Type.ERROR)
                {
                    progressBar.setString(message);
                }
            }
        });
    }

    /**
     * Centers a window on screen.
     *
     * @param frame The window to center.
     */
    private void centerFrame(Window frame)
    {
        Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        Dimension frameSize = frame.getSize();
        frame.setLocation(center.x - frameSize.width / 2,
                          center.y - frameSize.height / 2 - 10);
    }

    /**
     * Sets the parameters of a GridBagConstraints object.
     *
     * @param gbc The constraints object.
     * @param gx  The x coordinates.
     * @param gy  The y coordinates.
     * @param gw  The width.
     * @param wx  The x wheight.
     * @param wy  The y wheight.
     * @param gh  Description of the Parameter
     */
    private void buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh,
                                  double wx, double wy)
    {
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
    }

    /**
     * Loads the icons.
     *
     * @throws Exception Description of the Exception
     */
    private void loadIcons() throws Exception
    {
        URL url;
        ImageIcon img;

        // We load it
        url = UninstallerFrame.class.getResource("/com/izforge/izpack/img/trash.png");
        img = new ImageIcon(url);
        icons.put("delete", img);

        url = UninstallerFrame.class.getResource("/com/izforge/izpack/img/stop.png");
        img = new ImageIcon(url);
        icons.put("stop", img);

        url = UninstallerFrame.class.getResource("/com/izforge/izpack/img/flag.png");
        img = new ImageIcon(url);
        icons.put("warning", img);

        url = UninstallerFrame.class.getResource("/com/izforge/izpack/img/JFrameIcon.png");
        img = new ImageIcon(url);
        icons.put("JFrameIcon", img);
    }

    /**
     * Blocks GUI interaction.
     */
    private  void blockGUI()
    {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        getGlassPane().setVisible(true);
        getGlassPane().setEnabled(true);
    }

    /**
     * Releases GUI interaction.
     */
    private void releaseGUI()
    {
        getGlassPane().setEnabled(false);
        getGlassPane().setVisible(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Invoked when uninstallation is started.
     *
     * @param count the no. of files to delete
     */
    private void started(int count)
    {
        progressBar.setMinimum(0);
        progressBar.setMaximum(count);
        blockGUI();
    }

    /**
     * Invoked when uninstallation progresses.
     *
     * @param pos     the position
     * @param message the progress message
     */
    private void progress(int pos, String message)
    {
        progressBar.setValue(pos);
        progressBar.setString(message);
    }

    /**
     * Invoked when uninstallation is complete.
     */
    private void finished()
    {
        progressBar.setString(messages.get("InstallPanel.finished"));
        targetDestroyCheckbox.setEnabled(false);
        destroyButton.setEnabled(false);
        releaseGUI();
        List<File> failedToDelete = destroyer.getFailedToDelete();
        if (!failedToDelete.isEmpty())
        {
            StringBuilder buffer = new StringBuilder();
            for (File f : failedToDelete)
            {
                buffer.append(f.getPath());
                buffer.append('\n');
            }
            JTextArea textArea = new JTextArea();
            textArea.setText(buffer.toString());
            textArea.setRows(10);
            textArea.setColumns(72);
            textArea.setEditable(false);
            JScrollPane pane = new JScrollPane(textArea);
            BorderLayout layout = new BorderLayout();
            layout.setHgap(8);
            JPanel panel = new JPanel(layout);
            JLabel label = new JLabel("The following files and/or directories could not be removed:");
            panel.add(label, BorderLayout.NORTH);
            panel.add(pane, BorderLayout.CENTER);
            label = new JLabel("Directories cannot be removed if it is not empty. " +
                    "Administrative privileges may be required to remove files.");
            panel.add(label, BorderLayout.SOUTH);
            JOptionPane.showMessageDialog(null, panel, "Uninstallation Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * The window events handler.
     *
     * @author Julien Ponge
     */
    private final class WindowHandler extends WindowAdapter
    {

        /**
         * We can't avoid the exit here, so don't call exit elsewhere.
         *
         * @param e The event.
         */
        public void windowClosing(WindowEvent e)
        {
            housekeeper.shutDown(0);
        }
    }

    /**
     * The actions events handler.
     *
     * @author Julien Ponge
     */
    class ActionsHandler implements ActionListener
    {

        /**
         * Action handling method.
         *
         * @param e The event.
         */
        public void actionPerformed(ActionEvent e)
        {
            Object src = e.getSource();
            if (src == quitButton)
            {
                housekeeper.shutDown(0);
            }
            else if (src == destroyButton)
            {
                destroyButton.setEnabled(false);
                destroyer.setForceDelete(targetDestroyCheckbox.isSelected());
                Thread thread = new Thread(destroyer, "IzPack - Destroyer");
                thread.start();
            }
        }
    }

}

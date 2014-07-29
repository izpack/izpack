/*
 * $Id$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Jan Blok
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

package com.izforge.izpack.installer.gui;

import static com.izforge.izpack.api.GuiId.BUTTON_HELP;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.exception.ResourceException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.EtchedLineBorder;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.base.InstallerBase;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.debugger.Debugger;
import com.izforge.izpack.installer.unpacker.IUnpacker;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.Housekeeper;

/**
 * The IzPack installer frame.
 *
 * @author Julien Ponge created October 27, 2002
 * @author Fabrice Mirabile added fix for alert window on cross button, July 06 2005
 * @author Dennis Reil, added RulesEngine November 10 2006, several changes in January 2007
 */
public class InstallerFrame extends JFrame implements InstallerBase, InstallerView
{
    private static final long serialVersionUID = 3257852069162727473L;

    private static final transient Logger logger = Logger.getLogger(InstallerFrame.class.getName());

    private static final String ICON_RESOURCE = "Installer.image";

    /**
     * Name of the variable where to find an extension to the resource name of the icon resource
     */
    private static final String ICON_RESOURCE_EXT_VARIABLE_NAME = "installerimage.ext";

    /**
     * Heading icon resource name.
     */
    private static final String HEADING_ICON_RESOURCE = "Heading.image";

    /**
     * The installation data.
     */
    private GUIInstallData installdata;

    /**
     * The icons database.
     */
    private IconsDatabase icons;

    /**
     * The panels container.
     */
    protected JPanel panelsContainer;

    /**
     * The frame content pane.
     */
    protected JPanel contentPane;

    /**
     * The help button.
     */
    protected JButton helpButton = null;

    /**
     * The panel navigator.
     */
    private final DefaultNavigator navigator;

    /**
     * Registered GUICreationListener.
     */
    protected ArrayList<GUIListener> guiListener;

    /**
     * Heading major text.
     */
    protected JLabel[] headingLabels;

    /**
     * Panel which contains the heading text and/or icon
     */
    protected JPanel headingPanel;

    /**
     * The heading counter component.
     */
    protected JComponent headingCounterComponent;

    /**
     * Image
     */
    private JLabel iconLabel;

    /**
     * Count for discarded interrupt trials.
     */
    private int interruptCount = 1;

    /**
     * Maximum of discarded interrupt trials.
     */
    private static final int MAX_INTERRUPT = 3;

    /**
     * conditions
     */
    protected RulesEngine rules;

    private Debugger debugger;

    // If a heading image is defined should it be displayed on the left
    private boolean imageLeft = false;

    /**
     * The panels.
     */
    private final IzPanels panels;

    /**
     * The resources.
     */
    private ResourceManager resourceManager;

    /**
     * Manager for writing uninstall data
     */
    private UninstallDataWriter uninstallDataWriter;

    /**
     * The variables.
     */
    private Variables variables;

    private UninstallData uninstallData;

    /**
     * The unpacker.
     */
    private IUnpacker unpacker;

    /**
     * The house keeper.
     */
    private final Housekeeper housekeeper;

    /**
     * The log.
     */
    private final Log log;

    /**
     * The supported locales that contains the localised messages.
     */
    private Locales locales;

    /**
     * Constructs an <tt>InstallerFrame</tt>.
     *
     * @param title               the window title
     * @param installData         the installation data
     * @param rules               the rules engine
     * @param icons               the icons database
     * @param panels              the panels
     * @param uninstallDataWriter the uninstallation data writer
     * @param resourceManager     the resources
     * @param uninstallData       the uninstallation data
     * @param housekeeper         the house-keeper
     * @param navigator           the panel navigator
     * @param log                 the log
     */
    public InstallerFrame(String title, GUIInstallData installData, RulesEngine rules, IconsDatabase icons,
                          IzPanels panels, UninstallDataWriter uninstallDataWriter,
                          ResourceManager resourceManager, UninstallData uninstallData, Housekeeper housekeeper,
                          DefaultNavigator navigator, Log log, Locales locales)
    {
        super(title);
        guiListener = new ArrayList<GUIListener>();
        this.installdata = installData;
        this.rules = rules;
        this.resourceManager = resourceManager;
        this.uninstallDataWriter = uninstallDataWriter;
        this.uninstallData = uninstallData;
        this.panels = panels;
        this.variables = installData.getVariables();
        this.housekeeper = housekeeper;
        this.log = log;
        this.locales = locales;

        this.setIcons(icons);
        this.navigator = navigator;
        navigator.setInstallerFrame(this);

        // Sets the window events handler
        addWindowListener(new WindowHandler(navigator));
    }

    /**
     * Sets the unpacker.
     *
     * @param unpacker the unpacker
     */
    public void setUnpacker(IUnpacker unpacker)
    {
        this.unpacker = unpacker;
    }

    @Override
    public void sizeFrame()
    {
        pack();
        setSize(installdata.guiPrefs.width, installdata.guiPrefs.height);
        setPreferredSize(new Dimension(installdata.guiPrefs.width, installdata.guiPrefs.height));
        setResizable(installdata.guiPrefs.resizable);
        centerFrame(this);
    }

    public Debugger getDebugger()
    {
        return this.debugger;
    }

    /**
     * Builds the GUI.
     */
    public void buildGUI()
    {
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        ImageIcon jframeIcon = getIcons().get("JFrameIcon");
        setIconImage(jframeIcon.getImage());
        // Prepares the glass pane to block the gui interaction when needed
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
        glassPane.addFocusListener(new FocusAdapter()
        {
        });

        // We set the layout & prepare the constraint object
        contentPane = (JPanel) getContentPane();
        contentPane.setLayout(new BorderLayout()); // layout);

        // We add the panels container
        panelsContainer = new JPanel();
        panelsContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        panelsContainer.setLayout(new GridLayout(1, 1));
        contentPane.add(panelsContainer, BorderLayout.CENTER);

        logger.fine("Building GUI. The panel list to display is " + installdata.getPanels());

        Messages messages = locales.getMessages();
        navigator.updateButtonText(messages);

        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.X_AXIS));
        TitledBorder border = BorderFactory.createTitledBorder(
                new EtchedLineBorder(), messages.get("installer.madewith") + " ",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.PLAIN, 10));
        navPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8), border));

        // Add help Button to the navigation panel
        this.helpButton = ButtonFactory.createButton(messages.get("installer.help"), getIcons()
                .get("help"), installdata.buttonsHColor);
        navPanel.add(this.helpButton);
        this.helpButton.setName(BUTTON_HELP.id);
        this.helpButton.addActionListener(new HelpHandler());

        // update navigation panel and help button mnemonic shortcuts for selected language.
        ButtonFactory.clearAllMnemonics();
        ButtonFactory.reserveButtonMnemonics(new JButton[] {helpButton});
        navigator.reserveNavigatorButtonMnemonics();

        navPanel.add(Box.createHorizontalGlue());
        navPanel.add(navigator.getPrevious());
        navPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        navPanel.add(navigator.getNext());
        navPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        navPanel.add(navigator.getQuit());

        contentPane.add(navPanel, BorderLayout.SOUTH);

        // always initialize debugger
        debugger = new Debugger(installdata, getIcons(), rules);
        // this needed to fully initialize the debugger.
        JPanel debugpanel = debugger.getDebugPanel();

        // create a debug panel if TRACE is enabled
        if (Debug.isTRACE())
        {
            if (installdata.guiPrefs.modifier.containsKey("showDebugWindow")
                    && Boolean.valueOf(installdata.guiPrefs.modifier.get("showDebugWindow")))
            {
                JFrame debugframe = new JFrame("Debug information");
                debugframe.setContentPane(debugpanel);
                debugframe.setSize(new Dimension(400, 400));
                debugframe.setVisible(true);
            }
            else
            {
                debugpanel.setPreferredSize(new Dimension(200, 400));
                contentPane.add(debugpanel, BorderLayout.EAST);
            }
        }

        ImageIcon icon = loadIcon(ICON_RESOURCE, 0 + "");
        if (icon != null)
        {
            JPanel imgPanel = new JPanel();
            imgPanel.setLayout(new BorderLayout());
            imgPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));
            iconLabel = new JLabel(icon);
            iconLabel.setBorder(BorderFactory.createLoweredBevelBorder());
            imgPanel.add(iconLabel, BorderLayout.NORTH);
            contentPane.add(imgPanel, BorderLayout.WEST);
            loadAndShowImageForPanelNum(iconLabel, 0);
        }
        getRootPane().setDefaultButton(navigator.setDefaultButton());
        callGUIListener(GUIListener.GUI_BUILDED, navPanel);
        createHeading(navPanel);

        // need to initialise the panels after construction, as many of the panels require InstallerFrame
        panels.initialise();
        panels.setListener(new IzPanelsListener()
        {
            @Override
            public void switchPanel(IzPanelView newPanel, IzPanelView oldPanel)
            {
                InstallerFrame.this.switchPanel(newPanel, oldPanel);
            }

        });
    }

    /**
     * Returns the panel navigator.
     *
     * @return the panel navigator
     */
    public Navigator getNavigator()
    {
        return navigator;
    }

    private void callGUIListener(int what)
    {
        callGUIListener(what, null);
    }

    private void callGUIListener(int what, JPanel param)
    {
        for (GUIListener aGuiListener : guiListener)
        {
            aGuiListener.guiActionPerformed(what, param);
        }
    }

    /**
     * Loads icon for given panel id.
     *
     * @param resPrefix resource prefix
     * @param panelid   panel id
     * @return image icon, or {@code null} if no icon exists
     * @throws ResourceException if the resource exists but cannot be retrieved
     */
    private ImageIcon loadIcon(String resPrefix, String panelid)
    {
        ImageIcon icon = null;
        String ext = getIconResourceNameExtension();
        try
        {
            icon = resourceManager.getImageIcon(resPrefix, resPrefix + "." + panelid + ext);
        }
        catch (ResourceNotFoundException exception)
        {
            logger.fine("No icon for panel=" + panelid + ": " + exception.getMessage());
        }
        return icon;
    }

    /**
     * Returns the current set extension to icon resource names. Can be used to change the static
     * installer image based on user input
     *
     * @return a resource extension or an empty string if the variable was not set.
     */
    private String getIconResourceNameExtension()
    {
        try
        {
            String iconext = installdata.getVariable(ICON_RESOURCE_EXT_VARIABLE_NAME);
            if (iconext == null)
            {
                iconext = "";
            }
            else
            {

                if ((iconext.length() > 0) && (iconext.charAt(0) != '.'))
                {
                    iconext = "." + iconext;
                }
            }
            iconext = iconext.trim();
            return iconext;
        }
        catch (Exception e)
        {
            // in case of error, return an empty string
            return "";
        }
    }

    private void loadAndShowImageForPanelNum(JLabel jLabel, int panelNo)
    {
        loadAndShowImage(jLabel, ICON_RESOURCE, panelNo);
    }

    private void loadAndShowImageForPanelOrId(JLabel jLabel, int panelNo, String panelId)
    {
        loadAndShowImage(jLabel, ICON_RESOURCE, panelNo, panelId);
    }

    private void loadAndShowImage(JLabel jLabel, String resPrefix, int panelNo, String panelId)
    {
        ImageIcon icon = loadIcon(resPrefix, panelId);
        if (icon == null)
        {
            icon = loadIcon(resPrefix, panelNo + "");
        }
        jLabel.setVisible(false);
        jLabel.setIcon(icon);
        jLabel.setVisible(true);
    }

    private void loadAndShowImage(JLabel jLabel, String resPrefix, int panelNo)
    {
        ImageIcon icon = loadIcon(resPrefix, panelNo + "");
        if (icon == null)
        {
            icon = loadIcon(resPrefix, panelNo + "");
        }
        if (icon != null)
        {
            jLabel.setVisible(false);
            jLabel.setIcon(icon);
            jLabel.setVisible(true);
        }
    }

    /**
     * Switches the current panel.
     *
     * @param newPanel the new panel
     * @param oldPanel the old panel. May be {@code null}
     */
    protected void switchPanel(IzPanelView newPanel, IzPanelView oldPanel)
    {
        int oldIndex = (oldPanel != null) ? oldPanel.getIndex() : -1;
        logger.fine("Switching panel, old index is " + oldIndex);

        try
        {
            panelsContainer.setVisible(false);
            IzPanel newView = newPanel.getView();
            showHelpButton(newView.canShowHelp());
            if (Debug.isTRACE())
            {
                Panel panel = (oldPanel != null) ? oldPanel.getPanel() : null;
                debugger.switchPanel(newPanel.getPanel(), panel);
            }
            String oldPanelClass = (oldPanel != null) ? oldPanel.getClass().getName() : null;
            log.addDebugMessage(
                    "InstallerFrame.switchPanel: try switching newPanel from {0} to {1} ({2} to {3})",
                    new String[]{oldPanelClass, newPanel.getClass().getName(),
                            Integer.toString(oldIndex), Integer.toString(newPanel.getIndex())},
                    Log.PANEL_TRACE, null);

            // instead of writing data here which leads to duplicated entries in
            // auto-installation script (bug # 4551), let's make data only immediately before
            // writing out that script.
            // oldPanel.makeXMLData(installdata.xmlData.getChildAtIndex(oldIndex));
            // No previous button in the first visible newPanel

            // Change panels container to the current one.
            if (oldPanel != null)
            {
                IzPanel oldView = oldPanel.getView();
                panelsContainer.remove(oldView);
                oldView.panelDeactivate();
            }

            panelsContainer.add(newView);
            installdata.setCurPanelNumber(newPanel.getIndex());

            if (newView.getInitialFocus() != null)
            {
                // Initial focus hint should be performed after current newPanel
                // was added to the panels container, else the focus hint will
                // be ignored.
                // Give a hint for the initial focus to the system.
                final Component inFoc = newView.getInitialFocus();

                // On java VM version >= 1.5 it works only if
                // invoke later will be used.
                SwingUtilities.invokeLater(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        inFoc.requestFocusInWindow();
                    }
                });

                /*
                 * On editable text components position the caret to the end of the cust existent
                 * text.
                 */
                if (inFoc instanceof JTextComponent)
                {
                    JTextComponent inText = (JTextComponent) inFoc;
                    if (inText.isEditable() && inText.getDocument() != null)
                    {
                        inText.setCaretPosition(inText.getDocument().getLength());
                    }
                }
            }
            performHeading(newPanel);
            performHeadingCounter(newPanel);
            newPanel.executePreActivationActions();
            newView.panelActivate();
            panelsContainer.setVisible(true);
            if (iconLabel != null)
            {
                if (!"UNKNOWN".equals(newPanel.getPanelId()))
                {
                    loadAndShowImageForPanelOrId(iconLabel, panels.getVisibleIndex(newPanel), newPanel.getPanelId());
                }
                else
                {
                    loadAndShowImageForPanelNum(iconLabel, panels.getVisibleIndex(newPanel));
                }
            }
            callGUIListener(GUIListener.PANEL_SWITCHED);
            log.addDebugMessage("InstallerFrame.switchPanel: switched", null, Log.PANEL_TRACE, null);
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, "Error when switching panel", e);
        }
    }

    /**
     * Centers a window on screen.
     *
     * @param frame The window tp center.
     */
    public void centerFrame(Window frame)
    {
        Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        Dimension frameSize = frame.getSize();
        frame.setLocation(center.x - frameSize.width / 2, center.y - frameSize.height / 2 - 10);
    }

    /**
     * Returns the panels container size.
     *
     * @return The panels container size.
     */
    public Dimension getPanelsContainerSize()
    {
        return panelsContainer.getSize();
    }

    /**
     * Exits the installer, if quit is enabled.
     * <p/>
     * If installation is complete, this writes any uninstallation data, and shuts down.
     * If installation is incomplete, a confirmation dialog will be displayed.
     */
    public void exit()
    {
        navigator.quit();
    }

    /**
     * Quits the installer.
     * <p/>
     * If installation is complete, this writes any uninstallation data, and shuts down.
     * If installation is incomplete, a confirmation dialog will be displayed.
     */
    void quit()
    {
        // FIXME !!! Reboot handling
        if (installdata.isCanClose() || (!navigator.isNextEnabled() && !navigator.isPreviousEnabled()))
        {
            if (!writeUninstallData())
            {
                // TODO - for now just shut down. Alternative approaches include:
                // . retry
                // . revert installation - which is what wipeAborted attempts to do, but fails to handle shortcuts and
                //                         registry changes
            }
            shutdown();
        }
        else
        {
            // The installation is not over
            confirmExit();
        }
    }

    /**
     * Wipes the written files when you abort the installation.
     */
    protected void wipeAborted()
    {
        // We set interrupt to all running Unpacker and wait 40 sec for maximum.
        // If interrupt is discarded (return value false), return immediately:
        if (!unpacker.interrupt(40000))
        {
            return;
        }

        // Wipe the files that had been installed
        for (String installedFile : uninstallData.getInstalledFilesList())
        {
            File file = new File(installedFile);
            file.delete();
        }
    }

    /**
     * Launches the installation.
     *
     * @param listener The installation listener.
     */
    public void install(ProgressListener listener)
    {
        unpacker.setProgressListener(listener);
        Thread unpackerthread = new Thread(unpacker, "IzPack - Unpacker thread");
        unpackerthread.start();
    }

    /**
     * Writes the installation record to a file.
     *
     * @param out  The file to write to.
     * @throws Exception Description of the Exception
     */
    @Override
    public void writeInstallationRecord(File file, UninstallData uninstallData) throws Exception
    {
        panels.writeInstallationRecord(file, uninstallData);
    }


    /**
     * Changes the quit button text. If <tt>text</tt> is null, the default quit text is used.
     *
     * @param text text to be used for changes
     */
    public void setQuitButtonText(String text)
    {
        if (text == null)
        {
            Messages messages = locales.getMessages();
            text = messages.get("installer.quit");
        }
        navigator.setQuitText(text);
    }

    /**
     * Sets a new icon into the quit button if icons should be used, else nothing will be done.
     *
     * @param iconName name of the icon to be used
     */
    public void setQuitButtonIcon(String iconName)
    {
        String useButtonIcons = installdata.guiPrefs.modifier.get("useButtonIcons");

        if (useButtonIcons == null || "yes".equalsIgnoreCase(useButtonIcons))
        {
            navigator.getQuit().setIcon(getIcons().get(iconName));
        }
    }

    /**
     * FocusTraversalPolicy objects to handle keybord blocking; the declaration os Object allows to
     * use a pre version 1.4 VM.
     */
    private Object usualFTP = null;

    private Object blockFTP = null;

    /**
     * Blocks GUI interaction.
     */
    public void blockGUI()
    {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        getGlassPane().setVisible(true);
        getGlassPane().setEnabled(true);

        if (usualFTP == null)
        {
            usualFTP = getFocusTraversalPolicy();
        }
        if (blockFTP == null)
        {
            blockFTP = new BlockFocusTraversalPolicy();
        }
        setFocusTraversalPolicy((java.awt.FocusTraversalPolicy) blockFTP);
        getGlassPane().requestFocus();
        callGUIListener(GUIListener.GUI_BLOCKED);

    }

    /**
     * Releases GUI interaction.
     */
    public void releaseGUI()
    {
        getGlassPane().setEnabled(false);
        getGlassPane().setVisible(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        setFocusTraversalPolicy((java.awt.FocusTraversalPolicy) usualFTP);
        callGUIListener(GUIListener.GUI_RELEASED);
    }

    /**
     * Locks the 'previous' button.
     */
    @Override
    public void lockPrevButton()
    {
        navigator.setPreviousEnabled(false);
    }

    /**
     * Locks the 'next' button.
     */
    @Override
    public void lockNextButton()
    {
        navigator.setNextEnabled(false);
    }

    /**
     * Unlocks the 'previous' button.
     */
    @Override
    public void unlockPrevButton()
    {
        navigator.setPreviousEnabled(true);
    }

    /**
     * Unlocks the 'next' button.
     */
    @Override
    public void unlockNextButton()
    {
        unlockNextButton(true);
    }

    /**
     * Unlocks the 'next' button.
     *
     * @param requestFocus if <code>true</code> focus goes to <code>nextButton</code>
     */
    @Override
    public void unlockNextButton(boolean requestFocus)
    {
        navigator.setNextEnabled(true);
        if (requestFocus)
        {
            getRootPane().setDefaultButton(navigator.setDefaultButton());
            navigator.getNext().requestFocusInWindow();
            if (this.getFocusOwner() != null)
            {
                logger.fine("Current focus owner: " + this.getFocusOwner().getName());
            }
        }
    }

    /**
     * Allows a panel to ask to be skipped.
     */
    public void skipPanel()
    {
        if (panels.isBack())
        {
            navigatePrevious();
        }
        else
        {
            navigator.next(false);
        }
    }

    /**
     * This function moves to the next panel
     */
    @Override
    public void navigateNext()
    {
        navigator.next();
    }

    /**
     * Check to see if there is another panel that can be navigated to next. This checks the
     * successive panels to see if at least one can be shown based on the conditions associated with
     * the panels.
     *
     * @param startPanel  The panel to check from
     * @param visibleOnly Only check the visible panels
     * @return The panel that we can navigate to next or -1 if there is no panel that we can
     *         navigate next to
     */
    public int hasNavigateNext(int startPanel, boolean visibleOnly)
    {
        return panels.getNext(startPanel, visibleOnly);
    }

    /**
     * Check to see if there is another panel that can be navigated to previous. This checks the
     * previous panels to see if at least one can be shown based on the conditions associated with
     * the panels.
     *
     * @param endingPanel The panel to check from
     * @return The panel that we can navigate to previous or -1 if there is no panel that we can
     *         navigate previous to
     */
    public int hasNavigatePrevious(int endingPanel, boolean visibleOnly)
    {
        return panels.getPrevious(endingPanel, visibleOnly);
    }

    /**
     * This function moves to the previous panel
     */
    @Override
    public void navigatePrevious()
    {
        navigator.previous();
    }

    /**
     * Show help Window
     */
    @Override
    public void showHelp()
    {
        IzPanel izPanel = panels.getView();
        izPanel.showHelp();
    }

    /**
     * Returns the locale-specific messages.
     *
     * @return the messages
     */
    public Messages getMessages()
    {
        Messages messages = locales.getMessages();
        return messages;
    }

    /**
     * Returns the locale-specific messages.
     *
     * @return the messages
     * @deprecated use {@link #getMessages()}
     */
    @Deprecated
    public LocaleDatabase getLangpack()
    {
        Messages messages = locales.getMessages();
        return (LocaleDatabase) messages;
    }

    /**
     * Sets the locale specific messages.
     *
     * @param langpack the language pack
     * @deprecated no replacement
     */
    @Deprecated
    public void setLangpack(LocaleDatabase langpack)
    {
    }

    public IconsDatabase getIcons()
    {
        return icons;
    }

    public void setIcons(IconsDatabase icons)
    {
        this.icons = icons;
    }

    class HelpHandler implements ActionListener
    {

        /**Button
         * Actions handler.
         *
         * @param e The event.
         */
        @Override
        public void actionPerformed(ActionEvent e)
        {
            showHelp();
        }
    }

    /**
     * A FocusTraversalPolicy that only allows the block panel to have the focus
     */
    private class BlockFocusTraversalPolicy extends java.awt.DefaultFocusTraversalPolicy
    {

        private static final long serialVersionUID = 3258413928261169209L;

        /**
         * Only accepts the block panel
         *
         * @param aComp the component to check
         * @return true if aComp is the block panel
         */
        @Override
        protected boolean accept(Component aComp)
        {
            return aComp == getGlassPane();
        }
    }

    /**
     * Returns the gui creation listener list.
     *
     * @return the gui creation listener list
     */
    public List<GUIListener> getGuiListener()
    {
        return guiListener;
    }

    /**
     * Add a listener to the listener list.
     *
     * @param listener to be added as gui creation listener
     */
    public void addGuiListener(GUIListener listener)
    {
        guiListener.add(listener);
    }

    /**
     * Creates heading labels.
     *
     * @param headingLines the number of lines of heading labels
     * @param back         background color (currently not used)
     */
    private void createHeadingLabels(int headingLines, Color back)
    {
        // headingLabels are an array which contains the labels for header (0),
        // description lines and the icon (last).
        headingLabels = new JLabel[headingLines + 1];
        headingLabels[0] = new JLabel("");
        // First line ist the "main heading" which should be bold.
        headingLabels[0].setFont(headingLabels[0].getFont().deriveFont(Font.BOLD));

        // Updated by Daniel Azarov, Exadel Inc.
        // start
        Color foreground;
        if (installdata.guiPrefs.modifier.containsKey("headingForegroundColor"))
        {
            foreground = Color.decode(installdata.guiPrefs.modifier.get("headingForegroundColor"));
            headingLabels[0].setForeground(foreground);
        }
        // end

        if (installdata.guiPrefs.modifier.containsKey("headingFontSize"))
        {
            float fontSize = Float.parseFloat(installdata.guiPrefs.modifier.get("headingFontSize"));
            if (fontSize > 0.0 && fontSize <= 5.0)
            {
                float currentSize = headingLabels[0].getFont().getSize2D();
                headingLabels[0].setFont(headingLabels[0].getFont().deriveFont(
                        currentSize * fontSize));
            }
        }
        if (imageLeft)
        {
            headingLabels[0].setAlignmentX(Component.RIGHT_ALIGNMENT);
        }
        for (int i = 1; i < headingLines; ++i)
        {
            headingLabels[i] = new JLabel();
            // Minor headings should be a little bit more to the right.
            if (imageLeft)
            {
                headingLabels[i].setAlignmentX(Component.RIGHT_ALIGNMENT);
            }
            else
            {
                headingLabels[i].setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 8));
            }
        }

    }

    /**
     * Creates heading panel counter.
     *
     * @param navPanel         navi JPanel
     * @param leftHeadingPanel left heading JPanel
     */
    private void createHeadingCounter(JPanel navPanel, JPanel leftHeadingPanel)
    {
        int i;
        String counterPos = "inHeading";
        if (installdata.guiPrefs.modifier.containsKey("headingPanelCounterPos"))
        {
            counterPos = installdata.guiPrefs.modifier.get("headingPanelCounterPos");
        }
        // Do not create counter if it should be in the heading, but no heading should be used.
        if (leftHeadingPanel == null && "inHeading".equalsIgnoreCase(counterPos))
        {
            return;
        }
        if (installdata.guiPrefs.modifier.containsKey("headingPanelCounter"))
        {
            headingCounterComponent = null;
            if ("progressbar".equalsIgnoreCase(installdata.guiPrefs.modifier
                                                       .get("headingPanelCounter")))
            {
                JProgressBar headingProgressBar = new JProgressBar();
                headingProgressBar.setStringPainted(true);
                headingProgressBar.setString("");
                headingProgressBar.setValue(0);
                headingCounterComponent = headingProgressBar;
                if (imageLeft)
                {
                    headingCounterComponent.setAlignmentX(Component.RIGHT_ALIGNMENT);
                }
            }
            else
            {
                if ("text".equalsIgnoreCase(installdata.guiPrefs.modifier
                                                    .get("headingPanelCounter")))
                {
                    JLabel headingCountPanels = new JLabel(" ");
                    headingCounterComponent = headingCountPanels;
                    if (imageLeft)
                    {
                        headingCounterComponent.setAlignmentX(Component.RIGHT_ALIGNMENT);
                    }
                    else
                    {
                        headingCounterComponent.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
                    }

                    // Updated by Daniel Azarov, Exadel Inc.
                    // start
                    Color foreground;
                    if (installdata.guiPrefs.modifier.containsKey("headingForegroundColor"))
                    {
                        foreground = Color.decode(installdata.guiPrefs.modifier
                                                          .get("headingForegroundColor"));
                        headingCountPanels.setForeground(foreground);
                    }
                    // end
                }
            }
            if ("inHeading".equals(counterPos))
            {
                leftHeadingPanel.add(headingCounterComponent);
            }
            else if ("inNavigationPanel".equals(counterPos))
            {
                Component[] comps = navPanel.getComponents();
                for (i = 0; i < comps.length; ++i)
                {
                    if (comps[i].equals(navigator.getPrevious()))
                    {
                        break;
                    }
                }
                if (i <= comps.length)
                {
                    navPanel.add(Box.createHorizontalGlue(), i);
                    navPanel.add(headingCounterComponent, i);
                }

            }
        }
    }

    /**
     * Creates heading icon.
     *
     * @param back the color of background around image.
     * @return a panel with heading image.
     */
    private JPanel createHeadingIcon(Color back)
    {
        JPanel imgPanel = new JPanel();
        imgPanel.setLayout(new BoxLayout(imgPanel, BoxLayout.Y_AXIS));

        // Updated by Daniel Azarov, Exadel Inc.
        // start
        int borderSize = 8;
        if (installdata.guiPrefs.modifier.containsKey("headingImageBorderSize"))
        {
            borderSize = Integer.parseInt(installdata.guiPrefs.modifier
                                                  .get("headingImageBorderSize"));
        }
        imgPanel.setBorder(BorderFactory.createEmptyBorder(borderSize, borderSize, borderSize,
                                                           borderSize));
        // end

        if (back != null)
        {
            imgPanel.setBackground(back);
        }
        ImageIcon icon = loadIcon(HEADING_ICON_RESOURCE, 0 + "");
        if (icon != null)
        {
            JLabel iconLab = new JLabel(icon);
            if (imageLeft)
            {
                imgPanel.add(iconLab, BorderLayout.WEST);
            }
            else
            {
                imgPanel.add(iconLab, BorderLayout.EAST);
            }
            headingLabels[headingLabels.length - 1] = iconLab;
        }
        return (imgPanel);

    }

    /**
     * Creates a Heading in given Panel.
     *
     * @param navPanel a panel
     */
    private void createHeading(JPanel navPanel)
    {
        headingPanel = null;
        int headingLines = 1;
        // The number of lines can be determined in the config xml file.
        // The first is the header, additonals are descriptions for the header.
        if (installdata.guiPrefs.modifier.containsKey("headingLineCount"))
        {
            headingLines = Integer.parseInt(installdata.guiPrefs.modifier.get("headingLineCount"));
        }
        Color back = null;
        // It is possible to determine the used background color of the heading panel.
        if (installdata.guiPrefs.modifier.containsKey("headingBackgroundColor"))
        {
            back = Color.decode(installdata.guiPrefs.modifier.get("headingBackgroundColor"));
        }
        // Try to create counter if no heading should be used.
        if (!isHeading(null))
        {
            createHeadingCounter(navPanel, null);
            return;
        }
        // See if we should switch the header image to the left side
        if (installdata.guiPrefs.modifier.containsKey("headingImageOnLeft")
                && (installdata.guiPrefs.modifier.get("headingImageOnLeft").equalsIgnoreCase(
                "yes") || installdata.guiPrefs.modifier
                .get("headingImageOnLeft").equalsIgnoreCase("true")))
        {
            imageLeft = true;
        }
        // We create the text labels and the needed panels. From inner to outer.
        // Labels
        createHeadingLabels(headingLines, back);
        // Panel which contains the labels
        JPanel leftHeadingPanel = new JPanel();
        if (back != null)
        {
            leftHeadingPanel.setBackground(back);
        }
        leftHeadingPanel.setLayout(new BoxLayout(leftHeadingPanel, BoxLayout.Y_AXIS));
        if (imageLeft)
        {
            leftHeadingPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        }
        for (int i = 0; i < headingLines; ++i)
        {
            leftHeadingPanel.add(headingLabels[i]);
        }

        // HeadingPanel counter: this is a label or a progress bar which can be placed
        // in the leftHeadingPanel or in the navigation bar. It is facultative. If
        // exist, it shows the current panel number and the amount of panels.
        createHeadingCounter(navPanel, leftHeadingPanel);
        // It is possible to place an icon on the right side of the heading panel.
        JPanel imgPanel = createHeadingIcon(back);

        // The panel for text and icon.
        JPanel northPanel = new JPanel();
        if (back != null)
        {
            northPanel.setBackground(back);
        }
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.X_AXIS));
        northPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        if (imageLeft)
        {
            northPanel.add(imgPanel);
            northPanel.add(Box.createHorizontalGlue());
            northPanel.add(leftHeadingPanel);
        }
        else
        {
            northPanel.add(leftHeadingPanel);
            northPanel.add(Box.createHorizontalGlue());
            northPanel.add(imgPanel);
        }
        headingPanel = new JPanel(new BorderLayout());
        headingPanel.add(northPanel);
        headingPanel.add(new JSeparator(), BorderLayout.SOUTH);

        // contentPane.add(northPanel, BorderLayout.NORTH);
        contentPane.add(headingPanel, BorderLayout.NORTH);
    }

    /**
     * Returns whether this installer frame uses with the given panel a separated heading panel or
     * not. Be aware, this is an other heading as given by the IzPanel which will be placed in the
     * IzPanel. This heading will be placed if the gui preferences contains an modifier with the key
     * "useHeadingPanel" and the value "yes" and there is a message with the key "&lt;class
     * name&gt;.headline".
     *
     * @param caller the IzPanel for which heading should be resolved
     * @return whether an heading panel will be used or not
     */
    public boolean isHeading(IzPanel caller)
    {
        if (!installdata.guiPrefs.modifier.containsKey("useHeadingPanel")
                || !(installdata.guiPrefs.modifier.get("useHeadingPanel")).equalsIgnoreCase("yes"))
        {
            return (false);
        }
        if (caller == null)
        {
            return (true);
        }
        return (caller.getI18nStringForClass("headline") != null);

    }

    private void performHeading(IzPanelView panel)
    {
        int i;
        int headingLines = 1;
        if (installdata.guiPrefs.modifier.containsKey("headingLineCount"))
        {
            headingLines = Integer.parseInt(installdata.guiPrefs.modifier.get("headingLineCount"));
        }

        if (headingLabels == null)
        {
            return;
        }
        IzPanel view = panel.getView();
        String headline = view.getI18nStringForClass("headline");
        if (headline == null)
        {
            headingPanel.setVisible(false);
            return;
        }
        for (i = 0; i <= headingLines; ++i)
        {
            if (headingLabels[i] != null)
            {
                headingLabels[i].setVisible(false);
            }
        }
        String info;
        for (i = 0; i < headingLines - 1; ++i)
        {
            info = view.getI18nStringForClass("headinfo" + Integer.toString(i));
            if (info == null)
            {
                info = " ";
            }
            if (info.endsWith(":"))
            {
                info = info.substring(0, info.length() - 1) + ".";
            }
            headingLabels[i + 1].setText(info);
            headingLabels[i + 1].setVisible(true);
        }
        // Do not forgett the first headline.
        headingLabels[0].setText(headline);
        headingLabels[0].setVisible(true);
        int curPanelNo = panels.getVisibleIndex(panel);
        if (headingLabels[headingLines] != null)
        {
            loadAndShowImage(headingLabels[headingLines], HEADING_ICON_RESOURCE, curPanelNo);
            headingLabels[headingLines].setVisible(true);
        }
        headingPanel.setVisible(true);
    }

    private void performHeadingCounter(IzPanelView panel)
    {
        if (headingCounterComponent != null)
        {
            int curPanelNo = panels.getVisibleIndex(panel);
            int visPanelsCount = panels.getVisible();
            Messages messages = locales.getMessages();

            String message = String.format(
                    "%s %d %s %d",
                    messages.get("installer.step"), curPanelNo + 1,
                    messages.get("installer.of"), visPanelsCount
            );
            if (headingCounterComponent instanceof JProgressBar)
            {
                updateProgressBar(visPanelsCount, curPanelNo + 1, message);
            }
            else
            {
                updateProgressCounter(message);
            }
        }
    }

    public void updateProgressCounter(String message)
    {
        ((JLabel) headingCounterComponent).setText(message);
    }

    public void updateProgressBar(int maximum, int value, String message)
    {
        JProgressBar counterComponent = (JProgressBar) headingCounterComponent;
        counterComponent.setMaximum(maximum);
        counterComponent.setValue(value);
        counterComponent.setString(message);
    }

    /**
     * Shows or hides Help button depending on <code>show</code> parameter
     *
     * @param show - flag to show or hide Help button
     */
    private void showHelpButton(boolean show)
    {
        if (this.helpButton == null)
        {
            return;
        }
        this.helpButton.setVisible(show);
    }

    public void refreshDynamicVariables()
    {
        try
        {
            installdata.refreshVariables();
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, "Error when refreshing variable", e);
            logger.fine("Refreshing dynamic variables failed, asking user whether to proceed.");
            StringBuilder msg = new StringBuilder();
            msg.append("<html>");
            msg.append("The following error occured during refreshing panel contents:<br>");
            msg.append("<i>").append(e.getMessage()).append("</i><br>");
            msg.append("Are you sure you want to continue with this installation?");
            msg.append("</html>");
            JLabel label = new JLabel(msg.toString());
            label.setFont(new Font("Sans Serif", Font.PLAIN, 12));
            Object[] optionValues = {"Continue", "Exit"};
            int selectedOption = JOptionPane.showOptionDialog(null, label, "Warning",
                                                              JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                                                              null, optionValues,
                                                              optionValues[1]);
            logger.fine("Selected option: " + selectedOption);
            if (selectedOption == 0)
            {
                logger.fine("Continuing installation");
            }
            else
            {
                // TODO - shouldn't do this. Should try and clean up the installation
                logger.fine("Exiting");
                System.exit(1);
            }
        }
    }

    /**
     * Writes uninstall data if it is required.
     * <p/>
     * An error message will be displayed if the write fails.
     *
     * @return <tt>true</tt> if uninstall data was written successfully or is not required, otherwise <tt>false</tt>
     */
    private boolean writeUninstallData()
    {
        boolean result = true;
        if (uninstallDataWriter.isUninstallRequired())
        {
            result = uninstallDataWriter.write();
            if (!result)
            {
                Messages messages = locales.getMessages();
                String title = messages.get("installer.error");
                String message = messages.get("installer.uninstall.writefailed");
                JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
            }
        }
        return result;
    }

    /**
     * Shuts down the installer after successful installation.
     * <p/>
     * This may trigger a reboot.
     */
    private void shutdown()
    {
        boolean reboot = false;
        if (installdata.isRebootNecessary())
        {
            Messages messages = locales.getMessages();
            String message;
            String title;
            System.out.println("[ There are file operations pending after reboot ]");
            switch (installdata.getInfo().getRebootAction())
            {
                case Info.REBOOT_ACTION_ALWAYS:
                    reboot = true;
                    break;
                case Info.REBOOT_ACTION_ASK:
                    message = variables.replace(messages.get("installer.reboot.ask.message"));
                    title = variables.replace(messages.get("installer.reboot.ask.title"));
                    int res = JOptionPane
                            .showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION);
                    if (res == JOptionPane.YES_OPTION)
                    {
                        reboot = true;
                    }
                    break;
                case Info.REBOOT_ACTION_NOTICE:
                    message = variables.replace(messages.get("installer.reboot.notice.message"));
                    title = variables.replace(messages.get("installer.reboot.notice.title"));
                    JOptionPane.showConfirmDialog(this, message, title, JOptionPane.OK_OPTION);
                    break;
            }
            if (reboot)
            {
                System.out.println("[ Rebooting now automatically ]");
            }
        }

        housekeeper.shutDown(0, reboot);
    }


    /**
     * Confirms exit when installation is not complete.
     */
    private void confirmExit()
    {
        if (unpacker.isInterruptDisabled() && interruptCount < MAX_INTERRUPT)
        { // But we should not interrupt.
            interruptCount++;
            return;
        }

        Messages messages = locales.getMessages();

        // Use a alternate message and title if defined.
        final String mkey = "installer.quit.reversemessage";
        final String tkey = "installer.quit.reversetitle";
        String message = messages.get(mkey);
        String title = messages.get(tkey);
        // message equal to key -> no alternate message defined.
        if (message.contains(mkey))
        {
            message = messages.get("installer.quit.message");
        }
        // title equal to key -> no alternate title defined.
        if (title.contains(tkey))
        {
            title = messages.get("installer.quit.title");
        }
        // Now replace variables in message or title.
        message = variables.replace(message);
        title = variables.replace(title);

        int res = JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION)
        {
            wipeAborted();
            housekeeper.shutDown(0);
        }
    }

}

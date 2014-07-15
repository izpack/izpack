/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Marcus Wolschon
 * Copyright 2002 Jan Blok
 * Copyright 2004 Klaus Bartz
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

package com.izforge.izpack.panels.packs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.debugger.Debugger;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.installer.util.PackHelper;
import com.izforge.izpack.panels.treepacks.PackValidator;
import com.izforge.izpack.util.IoHelper;

/**
 * The base class for Packs panels. It brings the common member and methods of the different packs
 * panels together. This class handles the common logic of pack selection. The derived class should
 * be create the layout and other specific actions. There are some helper methods to simplify layout
 * creation in the derived class.
 *
 * @author Julien Ponge
 * @author Klaus Bartz
 * @author Dennis Reil
 */
public abstract class PacksPanelBase extends IzPanel implements PacksPanelInterface,
        ListSelectionListener
{
    private static final long serialVersionUID = -727171695900867059L;

    private static final String ACTIONKEY_TOGGLE = "togglePack";
    private static final String ACTIONKEY_NEXTCOLUMNCELL = "selectNextColumnCell";
    private static final String ACTIONKEY_PREVIOUSCOLUMNCELL = "selectPreviousColumnCell";

    private static final transient Logger logger = Logger.getLogger(PacksPanelBase.class.getName());

    // Common used Swing fields
    /**
     * The free space label.
     */
    protected JLabel freeSpaceLabel;

    /**
     * The space label.
     */
    protected JLabel spaceLabel;

    /**
     * The tip label.
     */
    protected JTextArea descriptionArea;

    /**
     * The dependencies label.
     */
    protected JTextArea dependencyArea;

    /**
     * The packs table.
     */
    protected JTable packsTable;

    /**
     * The packs model.
     */
    protected PacksModel packsModel;

    /**
     * The tablescroll.
     */
    protected JScrollPane tableScroller;

    // Non-GUI fields
    /**
     * Map that connects names with pack objects
     */
    private Map<String, Pack> names;

    /**
     * The bytes of the current pack.
     */
    protected long bytes = 0;

    /**
     * The free bytes of the current selected disk.
     */
    protected long freeBytes = 0;

    /**
     * Are there dependencies in the packs
     */
    protected boolean dependenciesExist = false;

    /**
     * The packs messages.
     */
    private Messages messages = null;

    /**
     * The name of the XML file that specifies the panel langpack
     */
    private static final String LANG_FILE_NAME = "packsLang.xml";

    private Debugger debugger;

    /**
     * The factory for creating {@link PackValidator} instances.
     */
    private final transient ObjectFactory factory;

    private RulesEngine rules;

    /**
     * Constructs a <tt>PacksPanelBase</tt>.
     *
     * @param panel       the panel meta-data
     * @param parent      fhe parent window
     * @param installData the installation data
     * @param resources   the resources
     * @param factory     the factory for creating {@link PackValidator} instances
     * @param rules       the rules engine
     */
    public PacksPanelBase(Panel panel, InstallerFrame parent, GUIInstallData installData,
                          Resources resources, ObjectFactory factory, RulesEngine rules)
    {
        super(panel, parent, installData, resources);
        this.rules = rules;
        this.factory = factory;
        this.debugger = parent.getDebugger();

        try
        {
            messages = installData.getMessages().newMessages(LANG_FILE_NAME);
        }
        catch (ResourceNotFoundException exception)
        {
            // no packs messages resource, so fall back to the default
            logger.info(exception.getMessage());
            messages = installData.getMessages();
        }
        // init the map
        computePacks(installData.getAvailablePacks());

        createNormalLayout();

        packsTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent event)
            {
                int row = packsTable.rowAtPoint(event.getPoint());
                int col = packsTable.columnAtPoint(event.getPoint());
                if (col == 0)
                {
                    togglePack(row);
                }
            }
        });
    }

    /**
     * The Implementation of this method should create the layout for the current class.
     */
    abstract protected void createNormalLayout();

    @Override
    public Messages getMessages()
    {
        return messages;
    }

    @Deprecated
    @Override
    public LocaleDatabase getLangpack()
    {
        return (LocaleDatabase) messages;
    }

    @Override
    public long getBytes()
    {
        return bytes;
    }

    @Override
    public void setBytes(long bytes)
    {
        this.bytes = bytes;
    }

    @Override
    public void showSpaceRequired()
    {
        if (spaceLabel != null)
        {
            spaceLabel.setText(Pack.toByteUnitsString(bytes));
        }
    }

    @Override
    public void showFreeSpace()
    {
        if (IoHelper.supported("getFreeSpace") && freeSpaceLabel != null)
        {
            String msg;
            freeBytes = IoHelper.getFreeSpace(IoHelper.existingParent(
                    new File(this.installData.getInstallPath())).getAbsolutePath());
            if (freeBytes < 0)
            {
                msg = getString("PacksPanel.notAscertainable");
            }
            else
            {
                msg = Pack.toByteUnitsString(freeBytes);
            }
            freeSpaceLabel.setText(msg);
        }
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return true if the needed space is less than the free space, else false
     */
    @Override
    public boolean isValidated()
    {
        if (IoHelper.supported("getFreeSpace") && freeBytes >= 0 && freeBytes <= bytes)
        {
            JOptionPane.showMessageDialog(this, getString("PacksPanel.notEnoughSpace"),
                                          getString("installer.error"), JOptionPane.ERROR_MESSAGE);
            return (false);
        }

        for (Pack pack : this.installData.getAvailablePacks())
        {
            for (String validator : pack.getValidators())
            {
                boolean selected = installData.getSelectedPacks().indexOf(pack) > -1;
                try
                {
                    PackValidator validatorInst = factory.create(validator, PackValidator.class);
                    if (!validatorInst.validate(this, installData, pack.getName(), selected))
                    {
                        return false;
                    }
                }
                catch (Exception e)
                {
                    logger.log(Level.WARNING, "Validator threw exception for pack " + pack.getName()
                            + ": " + e.getMessage(), e);
                    return false;
                }
            }
        }
        return (true);
    }

    /**
     * Asks to make the XML panel installDataGUI.
     *
     * @param panelRoot The XML tree to write the installDataGUI in.
     */
    @Override
    public void createInstallationRecord(IXMLElement panelRoot)
    {
        new PacksPanelAutomationHelper().createInstallationRecord(this.installData, panelRoot);
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        int selectedRow = packsTable.getSelectedRow();

        // Operations for the description
        if ((descriptionArea != null) && (selectedRow != -1))
        {
            Pack pack = this.packsModel.getPackAtRow(selectedRow);
            String desc = PackHelper.getPackDescription(pack, messages);
            desc = installData.getVariables().replace(desc);
            descriptionArea.setText(desc);
        }
        // Operation for the dependency listing
        if ((dependencyArea != null) && (selectedRow != -1))
        {
            Pack pack = this.packsModel.getPackAtRow(selectedRow);
            List<String> dep = pack.getDependencies();
            String list = "";
            if (dep != null)
            {
                list += (messages == null) ? "Dependencies: " : messages.get("PacksPanel.dependencies");
            }
            for (int j = 0; dep != null && j < dep.size(); j++)
            {
                String name = dep.get(j);
                list += getI18NPackName(names.get(name));
                if (j != dep.size() - 1)
                {
                    list += ", ";
                }
            }

            // add the list of the packs to be excluded
            String excludeslist = (messages == null) ? "Excludes: " : messages.get("PacksPanel.excludes");
            int numexcludes = 0;
            if (pack.getExcludeGroup() != null)
            {
                for (int q = 0; q < this.installData.getAvailablePacks().size(); q++)
                {
                    Pack otherpack = this.installData.getAvailablePacks().get(q);
                    String exgroup = otherpack.getExcludeGroup();
                    if (exgroup != null)
                    {
                        if (q != selectedRow && pack.getExcludeGroup().equals(exgroup))
                        {

                            excludeslist += getI18NPackName(otherpack) + ", ";
                            numexcludes++;
                        }
                    }
                }
            }
            // concatenate
            if (dep != null)
            {
                excludeslist = "    " + excludeslist;
            }
            if (numexcludes > 0)
            {
                list += excludeslist;
            }
            if (list.endsWith(", "))
            {
                list = list.substring(0, list.length() - 2);
            }

            // and checkbox the result
            dependencyArea.setText(list);
        }
    }

    /**
     * This method tries to resolve the localized name of the given pack. If this is not possible,
     * the name given in the installation description file in ELEMENT <pack> will be used.
     *
     * @param pack for which the name should be resolved
     * @return localized name of the pack
     */
    private String getI18NPackName(Pack pack)
    {
        return PackHelper.getPackName(pack, messages);
    }

    /**
     * Layout helper method:<br>
     * Creates an label with a message given by msgId and an icon given by the iconId. If layout and
     * constraints are not null, the label will be added to layout with the given constraints. The
     * label will be added to this object.
     *
     * @param msgId       identifier for the IzPack langpack
     * @param iconId      identifier for the IzPack icons
     * @param layout      layout to be used
     * @param constraints constraints to be used
     * @return the created label
     */
    protected JLabel createLabel(String msgId, String iconId, GridBagLayout layout,
                                 GridBagConstraints constraints)
    {
        JLabel label = LabelFactory.create(getString(msgId), parent.getIcons()
                .get(iconId), TRAILING);
        if (layout != null && constraints != null)
        {
            layout.addLayoutComponent(label, constraints);
        }
        add(label);
        return (label);
    }

    /**
     * Creates a panel containing a anonymous label on the left with the message for the given msgId
     * and a label on the right side with initial no text. The right label will be returned. If
     * layout and constraints are not null, the label will be added to layout with the given
     * constraints. The panel will be added to this object.
     *
     * @param msgId       identifier for the IzPack langpack
     * @param layout      layout to be used
     * @param constraints constraints to be used
     * @return the created (right) label
     */
    protected JLabel createPanelWithLabel(String msgId, GridBagLayout layout,
                                          GridBagConstraints constraints)
    {
        JPanel panel = new JPanel();
        JLabel label = new JLabel();
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(LabelFactory.create(getString(msgId)));
        panel.add(Box.createHorizontalGlue());
        panel.add(label);
        if (layout != null && constraints != null)
        {
            layout.addLayoutComponent(panel, constraints);
        }
        add(panel);
        boolean doNotShowRequiredSize = Boolean.parseBoolean(
                this.installData.guiPrefs.modifier.get("doNotShowRequiredSize"));
        panel.setVisible(!doNotShowRequiredSize);
        return (label);
    }

    /**
     * Creates a text area with standard settings and the title given by the msgId. If scroller is
     * not null, the create text area will be added to the scroller and the scroller to this object,
     * else the text area will be added directly to this object. If layout and constraints are not
     * null, the text area or scroller will be added to layout with the given constraints. The text
     * area will be returned.
     *
     * @param msgId       identifier for the IzPack langpack
     * @param scroller    the scroller to be used
     * @param layout      layout to be used
     * @param constraints constraints to be used
     * @return the created text area
     */
    protected JTextArea createTextArea(String msgId, JScrollPane scroller, GridBagLayout layout,
                                       GridBagConstraints constraints)
    {
        JTextArea area = new JTextArea();
        // area.setMargin(new Insets(2, 2, 2, 2));
        area.setAlignmentX(LEFT_ALIGNMENT);
        area.setCaretPosition(0);
        area.setEditable(false);
        area.setFocusable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createTitledBorder(getString(msgId)));
        area.setFont(getControlTextFont());

        if (layout != null && constraints != null)
        {
            if (scroller != null)
            {
                layout.addLayoutComponent(scroller, constraints);
            }
            else
            {
                layout.addLayoutComponent(area, constraints);
            }
        }
        if (scroller != null)
        {
            scroller.setViewportView(area);
            add(scroller);
        }
        else
        {
            add(area);
        }
        return (area);

    }

    /**
     * Creates the table for the packs. All parameters are required. The table will be returned.
     *
     * @param width       of the table
     * @param scroller    the scroller to be used
     * @param layout      layout to be used
     * @param constraints constraints to be used
     * @return the created table
     */
    protected JTable createPacksTable(int width, JScrollPane scroller, GridBagLayout layout,
                                      GridBagConstraints constraints)
    {
        final JTable table = new JTable();
        table.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(Color.white);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this);
        table.setShowGrid(false);

        // register an action to toggle the selected pack when SPACE is pressed in the table
        table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), ACTIONKEY_TOGGLE);
        table.getActionMap().put(ACTIONKEY_TOGGLE, new AbstractAction()
        {
            private static final long serialVersionUID = -2367549964368751438L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                int row = table.getSelectedRow();
                togglePack(row);
            }
        });

        // register an action for "selectNextColumnCell" to change selection to the next row.
        // When at the last row, move focus outside the table. This avoids the need to use Ctrl-Tab to move focus
        table.getActionMap().put(ACTIONKEY_NEXTCOLUMNCELL, new AbstractAction()
        {
            private static final long serialVersionUID = -8459710483408176528L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                int row = table.getSelectedRow();
                if (row < table.getRowCount() - 1)
                {
                    table.changeSelection(row + 1, 0, false, false);
                }
                else
                {
                    table.clearSelection();
                    table.transferFocus();
                }
            }
        });
        // register an action for "selectPreviousColumnCell" to change selection to the previous row.
        // When at the first, move focus to the prior component. This avoids the need to use Ctrl-Shift-Tab to move
        // focus
        table.getActionMap().put(ACTIONKEY_PREVIOUSCOLUMNCELL, new AbstractAction()
        {
            private static final long serialVersionUID = 5846709935309245963L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                int row = table.getSelectedRow();
                if (row == -1)
                {
                    row = table.getRowCount();
                }
                if (row > 0)
                {
                    table.changeSelection(row - 1, 0, false, false);
                }
                else
                {
                    table.clearSelection();
                    table.transferFocusBackward();
                }
            }
        });

        scroller.setViewportView(table);
        scroller.setAlignmentX(LEFT_ALIGNMENT);
        scroller.getViewport().setBackground(Color.white);
        scroller.setPreferredSize(new Dimension(width, (this.installData.guiPrefs.height / 3 + 30)));

        if (layout != null && constraints != null)
        {
            layout.addLayoutComponent(scroller, constraints);
        }
        add(scroller);
        return (table);
    }

    /**
     * Computes pack related installDataGUI like the names or the dependencies state.
     *
     * @param packs The list of packs.
     */
    private void computePacks(List<Pack> packs)
    {
        names = new HashMap<String, Pack>();
        dependenciesExist = false;
        for (Pack pack : packs)
        {
            names.put(pack.getName(), pack);
            if (pack.getDependencies() != null || pack.getExcludeGroup() != null)
            {
                dependenciesExist = true;
            }
        }
    }

    /**
     * Called when the panel becomes active. If a derived class implements this method also, it is
     * recomanded to call this method with the super operator first.
     */
    @Override
    public void panelActivate()
    {
        parent.lockNextButton();

        try
        {
            packsModel = new PacksModelGUI(this, installData, rules)
            {
                /**
                 *
                 */
                private static final long serialVersionUID = -8566131431416593277L;

                @Override
                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return false;
                }
            };
            packsTable.setModel(packsModel);
            CheckBoxRenderer packSelectedRenderer = new CheckBoxRenderer();
            packsTable.getColumnModel().getColumn(0).setCellRenderer(packSelectedRenderer);
            packsTable.getColumnModel().getColumn(0).setMaxWidth(40);

            packsTable.getColumnModel().getColumn(1).setCellRenderer(new PacksPanelTableCellRenderer());
            PacksPanelTableCellRenderer packTextColumnRenderer = new PacksPanelTableCellRenderer();
            packTextColumnRenderer.setHorizontalAlignment(RIGHT);
            if (packsTable.getColumnCount() > 2)
            {
                packsTable.getColumnModel().getColumn(2).setCellRenderer(packTextColumnRenderer);
                packsTable.getColumnModel().getColumn(2).setMaxWidth(100);
            }

            // remove header,so we don't need more strings
            tableScroller.remove(packsTable.getTableHeader());
            tableScroller.setColumnHeaderView(null);
            tableScroller.setColumnHeader(null);

            // set the JCheckBoxes to the currently selected panels. The
            // selection might have changed in another panel
            bytes = 0;
            for (Pack p : this.installData.getAvailablePacks())
            {
                if (p.isRequired())
                {
                    bytes += p.getSize();
                    continue;
                }
                if (this.installData.getSelectedPacks().contains(p))
                {
                    bytes += p.getSize();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        showSpaceRequired();
        showFreeSpace();
        packsTable.setRowSelectionInterval(0, 0);

        updateButtons();
    }

    @Override
    public String getSummaryBody()
    {
        StringBuilder retval = new StringBuilder(256);
        boolean first = true;
        for (Pack pack : this.installData.getSelectedPacks())
        {
            if (!first)
            {
                retval.append("<br>");
            }
            first = false;
            retval.append(getI18NPackName(pack));
        }
        if (packsModel.isModifyInstallation())
        {
            Map<String, Pack> installedpacks = packsModel.getInstalledPacks();
            retval.append("<br><b>");
            retval.append(messages.get("PacksPanel.installedpacks.summarycaption"));
            retval.append("</b>");
            retval.append("<br>");
            for (String key : installedpacks.keySet())
            {
                Pack pack = installedpacks.get(key);
                retval.append(getI18NPackName(pack));
                retval.append("<br>");
            }
        }
        return (retval.toString());
    }

    static class CheckBoxRenderer implements TableCellRenderer
    {
        JCheckBox checkbox = new JCheckBox();

        CheckBoxRenderer()
        {
            if (com.izforge.izpack.util.OsVersion.IS_UNIX && !com.izforge.izpack.util.OsVersion.IS_OSX)
            {
                checkbox.setIcon(new LFIndependentIcon());
                checkbox.setDisabledIcon(new LFIndependentIcon());
                checkbox.setSelectedIcon(new LFIndependentIcon());
                checkbox.setDisabledSelectedIcon(new LFIndependentIcon());
            }
            checkbox.setHorizontalAlignment(CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column)
        {
            if (isSelected)
            {
                checkbox.setForeground(table.getSelectionForeground());
                checkbox.setBackground(table.getSelectionBackground());
            }
            else
            {
                checkbox.setForeground(table.getForeground());
                checkbox.setBackground(table.getBackground());
            }

            int state = (Integer) value;
            if (state == -2)
            {
                // condition not fulfilled
                checkbox.setForeground(Color.GRAY);
            }
            if (state == -3)
            {
                checkbox.setForeground(Color.RED);
                checkbox.setSelected(true);
            }

            checkbox.setEnabled(state >= 0);
            checkbox.setSelected((value != null && Math.abs(state) == 1));
            return checkbox;
        }
    }

    public static class LFIndependentIcon implements Icon
    {
        ButtonModel buttonModel = null;

        protected int getControlSize()
        {
            return 13;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y)
        {
            ButtonModel model = ((JCheckBox) component).getModel();
            buttonModel = model;
            int controlSize = getControlSize();
            if (model.isPressed() && model.isArmed())
            {
                graphics.setColor(MetalLookAndFeel.getControlShadow());
                if (model.isEnabled())
                {
                    graphics.setColor(Color.green);
                }
                else
                {
                    graphics.setColor(Color.gray);
                }
                graphics.fillRect(x, y, controlSize - 1, controlSize - 1);
                drawPressedBorder(graphics, x, y, controlSize, controlSize, model);
            }
            else
            {
                drawBorder(graphics, x, y, controlSize, controlSize, model);
            }
            graphics.setColor(Color.green);
            if (model.isSelected())
            {
                drawCheck(graphics, x, y);
            }
        }

        private void drawBorder(Graphics graphics, int x, int y, int width, int height, ButtonModel model)
        {
            graphics.translate(x, y);

            // outer frame rectangle
            graphics.setColor(MetalLookAndFeel.getControlDarkShadow());
            if (!model.isEnabled())
            {
                graphics.setColor(new Color(0.4f, 0.4f, 0.4f));
            }
            graphics.drawRect(0, 0, width - 2, height - 2);

            // middle frame
            graphics.setColor(MetalLookAndFeel.getControlHighlight());
            if (!model.isEnabled())
            {
                graphics.setColor(new Color(0.6f, 0.6f, 0.6f));
            }
            graphics.drawRect(1, 1, width - 2, height - 2);

            // background
            if (model.isEnabled())
            {
                graphics.setColor(Color.white);
            }
            else
            {
                graphics.setColor(new Color(0.8f, 0.8f, 0.8f));
            }
            graphics.fillRect(2, 2, width - 3, height - 3);

            //some extra lines for FX
            graphics.setColor(MetalLookAndFeel.getControl());
            graphics.drawLine(0, height - 1, 1, height - 2);
            graphics.drawLine(width - 1, 0, width - 2, 1);
            graphics.translate(-x, -y);
        }

        private void drawPressedBorder(Graphics graphics, int x, int y, int width, int height, ButtonModel model)
        {
            graphics.translate(x, y);
            drawBorder(graphics, 0, 0, width, height, model);
            graphics.setColor(MetalLookAndFeel.getControlShadow());
            graphics.drawLine(1, 1, 1, height - 2);
            graphics.drawLine(1, 1, width - 2, 1);
            graphics.drawLine(2, 2, 2, height - 3);
            graphics.drawLine(2, 2, width - 3, 2);
            graphics.translate(-x, -y);
        }

        protected void drawCheck(Graphics graphics, int x, int y)
        {
            int controlSize = getControlSize();
            if (buttonModel != null)
            {
                if (buttonModel.isEnabled())
                {
                    graphics.setColor(new Color(0.0f, 0.6f, 0.0f));
                }
                else
                {
                    graphics.setColor(new Color(0.1f, 0.1f, 0.1f));
                }
            }

            graphics.drawLine(x + (controlSize - 4), y + 2, x + (controlSize - 4) - 4, y + 2 + 4);
            graphics.drawLine(x + (controlSize - 4), y + 3, x + (controlSize - 4) - 4, y + 3 + 4);
            graphics.drawLine(x + (controlSize - 4), y + 4, x + (controlSize - 4) - 4, y + 4 + 4);

            graphics.drawLine(x + (controlSize - 4) - 4, y + 2 + 4, x + (controlSize - 4) - 4 - 2, y + 2 + 4 - 2);
            graphics.drawLine(x + (controlSize - 4) - 4, y + 3 + 4, x + (controlSize - 4) - 4 - 2, y + 3 + 4 - 2);
            graphics.drawLine(x + (controlSize - 4) - 4, y + 4 + 4, x + (controlSize - 4) - 4 - 2, y + 4 + 4 - 2);
        }

        @Override
        public int getIconWidth()
        {
            return getControlSize();
        }

        @Override
        public int getIconHeight()
        {
            return getControlSize();
        }
    }

    static class PacksPanelTableCellRenderer extends DefaultTableCellRenderer
    {
        /**
         * Required (serializable)
         */
        private static final long serialVersionUID = -9089892183236584242L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column)
        {
            Component renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            int state = (Integer) table.getModel().getValueAt(row, 0);
            if (state == -2)
            {
                // condition not fulfilled
                renderer.setForeground(Color.GRAY);
                if (isSelected)
                {
                    renderer.setBackground(table.getSelectionBackground());
                }
                else
                {
                    renderer.setBackground(table.getBackground());
                }
            }
            else
            {
                if (isSelected)
                {
                    renderer.setForeground(table.getSelectionForeground());
                    renderer.setBackground(table.getSelectionBackground());
                }
                else
                {
                    renderer.setForeground(table.getForeground());
                    renderer.setBackground(table.getBackground());
                }
            }

            return renderer;
        }

    }

    @Override
    public Debugger getDebugger()
    {
        return this.debugger;
    }

    /**
     * Toggles the state of the pack at the selected row.
     *
     * @param row the row
     */
    private void togglePack(int row)
    {
        Integer checked = (Integer) packsModel.getValueAt(row, 0);
        checked = (checked <= 0) ? 1 : 0;
        packsModel.setValueAt(checked, row, 0);
        packsTable.repaint();
        packsTable.changeSelection(row, 0, false, false);
        updateButtons();
    }

    private void updateButtons()
    {
        if (installData.getSelectedPacks().isEmpty())
        {
            parent.lockNextButton();
        }
        else
        {
            parent.unlockNextButton();
        }
    }
}

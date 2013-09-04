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

package com.izforge.izpack.panels.installationgroup;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.util.PlatformModelMatcher;


/**
 * A panel which displays the available installGroups found on the packs to
 * allow the user to select a subset of the packs based on the pack
 * installGroups attribute. This panel will be skipped if there are no
 * pack elements with an installGroups attribute.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1.1.1 $
 */
public class InstallationGroupPanel extends IzPanel
        implements ListSelectionListener
{
    /**
     *
     */
    private static final long serialVersionUID = -8080249125208860785L;

    private static final transient Logger logger = Logger.getLogger(InstallationGroupPanel.class.getName());

    /**
     * The platform-model matcher.
     */
    private final PlatformModelMatcher matcher;

    /**
     * HashMap<String, Pack> of the GUIInstallData.availablePacks
     */
    private HashMap<String, Pack> packsByName;
    private TableModel groupTableModel;
    private JTextPane descriptionField;
    private JScrollPane groupScrollPane;
    private JTable groupsTable;
    private GroupData[] rows;
    private int selectedGroup = -1;


    /**
     * The constructor.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent window
     * @param installData the installation data
     * @param resources   the resources
     * @param matcher     the platform-model matcher
     */
    public InstallationGroupPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
                                  PlatformModelMatcher matcher)
    {
        super(panel, parent, installData, resources);
        this.matcher = matcher;
        buildLayout();
    }

    /**
     * If there are no packs with an installGroups attribute, this panel is
     * skipped. Otherwise, the unique installGroups are displayed in a table.
     */
    @Override
    public void panelActivate()
    {
        // Set/restore availablePacks from allPacks; consider OS constraints
        this.installData.setAvailablePacks(new ArrayList<Pack>());
        for (Pack pack : this.installData.getAllPacks())
        {
            if (matcher.matchesCurrentPlatform(pack.getOsConstraints()))
            {
                this.installData.getAvailablePacks().add(pack);
            }
        }

        logger.fine("selectedGroup=" + selectedGroup);
        // If there are no groups, skip this panel
        Map<String, GroupData> installGroups = getInstallGroups(this.installData);
        if (installGroups.size() == 0)
        {
            super.askQuestion("Skip InstallGroup selection",
                              "Skip InstallGroup selection", AbstractUIHandler.CHOICES_YES_NO);
            parent.skipPanel();
            return;
        }

        // Build the table model from the unique groups
        groupTableModel = getModel(installGroups);
        groupsTable.setModel(groupTableModel);
        TableColumnModel columnModel = groupsTable.getColumnModel();

        // renders the radio buttons and adjusts their state
        TableCellRenderer radioButtonRenderer = new TableCellRenderer()
        {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column)
            {
                if (value == null)
                {
                    return null;
                }

                int selectedRow = table.getSelectedRow();

                if (selectedRow != -1)
                {
                    JRadioButton selectedButton = (JRadioButton) table.getValueAt(selectedRow, 0);
                    if (!selectedButton.isSelected())
                    {
                        selectedButton.doClick();
                    }
                }

                JRadioButton button = (JRadioButton) value;
                button.setForeground(isSelected ?
                                             table.getSelectionForeground() : table.getForeground());
                button.setBackground(isSelected ?
                                             table.getSelectionBackground() : table.getBackground());

                // long millis = System.currentTimeMillis() % 100000;
                // System.out.printf("%1$5d: row: %2$d; isSelected: %3$5b; buttonSelected: %4$5b; selectedRow: %5$d%n", millis, row, isSelected, button.isSelected(), selectedRow);

                return button;
            }
        };
        columnModel.getColumn(0).setCellRenderer(radioButtonRenderer);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        columnModel.getColumn(1).setCellRenderer(renderer);

        //groupsTable.setColumnSelectionAllowed(false);
        //groupsTable.setRowSelectionAllowed(true);
        groupsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupsTable.getSelectionModel().addListSelectionListener(this);
        groupsTable.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        groupsTable.setIntercellSpacing(new Dimension(0, 0));
        groupsTable.setShowGrid(false);
        if (selectedGroup >= 0)
        {
            groupsTable.getSelectionModel().setSelectionInterval(selectedGroup, selectedGroup);
            descriptionField.setText(rows[selectedGroup].description);
        }
        else
        {
            descriptionField.setText(rows[0].description);
        }
    }

    /**
     * Remove all packs from the GUIInstallData availablePacks and selectedPacks
     * that do not list the selected installation group. Packs without any
     * installGroups are always included.
     */
    @Override
    public void panelDeactivate()
    {

        logger.fine("selectedGroup=" + selectedGroup);
        if (selectedGroup >= 0)
        {
            removeUnusedPacks();
            GroupData group = this.rows[selectedGroup];
            this.installData.setVariable("INSTALL_GROUP", group.name);
            logger.fine("Added variable INSTALL_GROUP=" + group.name);
        }
    }

    /**
     * There needs to be a valid selectedGroup to go to the next panel
     *
     * @return true if selectedGroup >= 0, false otherwise
     */
    @Override
    public boolean isValidated()
    {
        logger.fine("selectedGroup=" + selectedGroup);
        return selectedGroup >= 0;
    }

    /**
     * Update the current selected install group index.
     *
     * @param e
     */
    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        logger.fine("Event: " + e);
        if (!e.getValueIsAdjusting())
        {
            ListSelectionModel listSelectionModel = (ListSelectionModel) e.getSource();
            if (listSelectionModel.isSelectionEmpty())
            {
                descriptionField.setText("");
            }
            else
            {
                selectedGroup = listSelectionModel.getMinSelectionIndex();
                if (selectedGroup >= 0)
                {
                    GroupData data = rows[selectedGroup];
                    descriptionField.setText(data.description);
                    ((JRadioButton) groupTableModel.getValueAt(selectedGroup, 0)).setSelected(true);
                }
                logger.fine("selectedGroup set to: " + selectedGroup);
            }
        }
    }

    /* Add the installation group to pack mappings
    * @see com.izforge.izpack.installer.IzPanel#makeXMLData(com.izforge.izpack.api.adaptator.IXMLElement)
    */

    @Override
    public void makeXMLData(IXMLElement panelRoot)
    {
        InstallationGroupPanelAutomationHelper helper = new InstallationGroupPanelAutomationHelper();
        this.installData.setAttribute("GroupData", rows);
        this.installData.setAttribute("packsByName", packsByName);
        helper.makeXMLData(this.installData, panelRoot);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.izforge.izpack.installer.IzPanel#getSummaryBody()
     */

    @Override
    public String getSummaryBody()
    {
        return (rows[selectedGroup].name);
    }

    /**
     * Create the panel ui.
     */
    protected void buildLayout()
    {
        GridBagConstraints gridBagConstraints;

        descriptionField = new JTextPane();
        groupScrollPane = new JScrollPane();
        groupsTable = new JTable();

        setLayout(new GridBagLayout());

        descriptionField.setMargin(new Insets(2, 2, 2, 2));
        descriptionField.setAlignmentX(LEFT_ALIGNMENT);
        descriptionField.setCaretPosition(0);
        descriptionField.setEditable(false);
        descriptionField.setOpaque(false);
        descriptionField.setText("<b>Install group description text</b>");
        descriptionField.setContentType("text/html");
        descriptionField.setBorder(
                new TitledBorder(getString("PacksPanel.description")));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.3;
        add(descriptionField, gridBagConstraints);

        groupScrollPane.setBorder(new EmptyBorder(1, 1, 1, 1));
        groupScrollPane.setViewportView(groupsTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(groupScrollPane, gridBagConstraints);
    }

    protected void removeUnusedPacks()
    {
        GroupData data = rows[selectedGroup];
        logger.fine("data=" + data.name);

        // Now remove the packs not in groupPackNames
        Iterator<Pack> iter = this.installData.getAvailablePacks().iterator();
        while (iter.hasNext())
        {
            Pack pack = iter.next();

            //reverse dependencies must be reset in case the user is going
            //back and forth between the group selection panel and the packs selection panel
            pack.setDependants(null);

            if (!data.packNames.contains(pack.getName()))
            {
                iter.remove();
                logger.fine("Removed available pack: " + pack.getName());
            }
        }

        this.installData.getSelectedPacks().clear();
        if (!"no".equals(this.installData.getVariable("InstallationGroupPanel.selectPacks")))
        {
            this.installData.getSelectedPacks().addAll(this.installData.getAvailablePacks());
        }
        else
        {
            for (Pack availablePack : this.installData.getAvailablePacks())
            {
                if (availablePack.isPreselected())
                {
                    this.installData.getSelectedPacks().add(availablePack);
                }
            }
        }
    }

    protected void addDependents(Pack p, HashMap<String, Pack> packsByName, GroupData data)
    {
        data.packNames.add(p.getName());
        data.size += p.getSize();
        logger.fine("Added pack: " + p.getName());
        if (p.getDependencies() == null || p.getDependencies().size() == 0)
        {
            return;
        }

        logger.fine(p.getName() + ", dependencies: " + p.getDependencies());
        for (String dependent : p.getDependencies())
        {
            if (!data.packNames.contains(dependent))
            {
                logger.fine("Need dependent: " + dependent);
                Pack dependentPack = packsByName.get(dependent);
                addDependents(dependentPack, packsByName, data);
            }
        }
    }

    /**
     * Build the set of unique installGroups installDataGUI. The GroupData description
     * is taken from the InstallationGroupPanel.description.[name] property
     * where [name] is the installGroup name. The GroupData size is built
     * from the Pack.size sum.
     *
     * @param idata - the panel install installDataGUI
     * @return HashMap<String, GroupData> of unique install group names
     */
    protected HashMap<String, GroupData> getInstallGroups(GUIInstallData idata)
    {
        /* First create a packsByName<String, Pack> of all packs and identify
        the unique install group names.
        */
        packsByName = new HashMap<String, Pack>();
        HashMap<String, GroupData> installGroups = new HashMap<String, GroupData>();
        for (Pack pack : idata.getAvailablePacks())
        {
            packsByName.put(pack.getName(), pack);
            Set<String> groups = pack.getInstallGroups();
            logger.fine("Pack: " + pack.getName() + ", installGroups: " + groups);
            for (String group : groups)
            {
                GroupData data = installGroups.get(group);
                if (data == null)
                {
                    String description = getGroupDescription(group);
                    String sortKey = getGroupSortKey(group);
                    data = new GroupData(group, description, sortKey);
                    installGroups.put(group, data);
                }
            }
        }
        logger.fine("Found installGroups: " + installGroups.keySet());

        /* Build up a set of the packs to include in the installation by finding
        all packs in the selected group, and then include their dependencies.
        */
        for (GroupData data : installGroups.values())
        {
            logger.fine("Adding dependents for: " + data.name);
            for (Pack pack : idata.getAvailablePacks())
            {
                Set<String> groups = pack.getInstallGroups();
                if (groups.size() == 0 || groups.contains(data.name))
                {
                    // The pack may have already been added while traversing dependencies
                    if (!data.packNames.contains(pack.getName()))
                    {
                        addDependents(pack, packsByName, data);
                    }
                }
            }
            logger.fine("Completed dependents for: " + data);
        }

        return installGroups;
    }

    /**
     * Look for a key = InstallationGroupPanel.description.[group] entry:
     * first using installData.langpack.getString(key+".html")
     * next using installData.langpack.getString(key)
     * next using installData.getVariable(key)
     * lastly, defaulting to group + " installation"
     *
     * @param group - the installation group name
     * @return the group description
     */
    protected String getGroupDescription(String group)
    {
        String description = null;
        String key = "InstallationGroupPanel.description." + group;
        String htmlKey = key + ".html";
        String html = getString(htmlKey);
        // This will equal the key if there is no entry
        if (htmlKey.equalsIgnoreCase(html))
        {
            description = getString(key);
        }
        else
        {
            description = html;
        }
        if (description == null || key.equalsIgnoreCase(description))
        {
            description = this.installData.getVariable(key);
        }
        if (description == null)
        {
            description = group + " installation";
        }
        try
        {
            description = URLDecoder.decode(description, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            emitWarning("Failed to convert description", e.getMessage());
        }

        return description;
    }

    /**
     * Look for a key = InstallationGroupPanel.sortKey.[group] entry:
     * by using installData.getVariable(key)
     * if this variable is not defined, defaults to group
     *
     * @param group - the installation group name
     * @return the group sortkey
     */
    protected String getGroupSortKey(String group)
    {
        String key = "InstallationGroupPanel.sortKey." + group;
        String sortKey = this.installData.getVariable(key);
        if (sortKey == null)
        {
            sortKey = group;
        }
        try
        {
            sortKey = URLDecoder.decode(sortKey, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            emitWarning("Failed to convert sortKey", e.getMessage());
        }

        return sortKey;
    }


    /**
     * Look for a key = InstallationGroupPanel.group.[group] entry:
     * first using installData.langpackgetString(key+".html")
     * next using installData.langpack.getString(key)
     * next using installData.getVariable(key)
     * lastly, defaulting to group
     *
     * @param group - the installation group name
     * @return the localized group name
     */
    protected String getLocalizedGroupName(String group)
    {
        String gname = null;
        String key = "InstallationGroupPanel.group." + group;
        String htmlKey = key + ".html";
        String html = getString(htmlKey);
        // This will equal the key if there is no entry
        if (htmlKey.equalsIgnoreCase(html))
        {
            gname = getString(key);
        }
        else
        {
            gname = html;
        }
        if (gname == null || key.equalsIgnoreCase(gname))
        {
            gname = this.installData.getVariable(key);
        }
        if (gname == null)
        {
            gname = group;
        }
        try
        {
            gname = URLDecoder.decode(gname, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            emitWarning("Failed to convert localized group name", e.getMessage());
        }

        return gname;
    }

    protected TableModel getModel(Map<String, GroupData> groupData)
    {
        String c1 = getString("InstallationGroupPanel.colNameSelected");
        //String c2 = installData.getLangpack().getString("InstallationGroupPanel.colNameInstallType");
        String c3 = getString("InstallationGroupPanel.colNameSize");
        String[] columns = {c1, c3};
        DefaultTableModel model = new DefaultTableModel(columns, 0)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };
        rows = new GroupData[groupData.size()];
        // The name of the group to select if there is no current selection
        String defaultGroup = this.installData.getVariable("InstallationGroupPanel.defaultGroup");
        logger.fine("InstallationGroupPanel.defaultGroup=" + defaultGroup + ", selectedGroup=" + selectedGroup);
        List<GroupData> values = new ArrayList<GroupData>(groupData.values());
        Collections.sort(values, new Comparator<GroupData>()
        {
            @Override
            public int compare(GroupData g1, GroupData g2)
            {
                if (g1.sortKey == null || g2.sortKey == null)
                {
                    return 0;
                }

                return g1.sortKey.compareTo(g2.sortKey);
            }
        });

        ButtonGroup buttonGroup = new ButtonGroup();
        boolean madeSelection = false;
        int count = 0;
        for (GroupData gd : values)
        {
            rows[count] = gd;
            logger.fine("Creating button #" + count + ", group=" + gd.name);
            JRadioButton button = new JRadioButton(getLocalizedGroupName(gd.name));
            if (selectedGroup == count)
            {
                button.setSelected(true);
                logger.fine("Selected button #" + count);
            }
            else if (selectedGroup < 0 && !madeSelection)
            {
                if (defaultGroup != null)
                {
                    if (defaultGroup.equals(gd.name))
                    {
                        madeSelection = true;
                    }
                }
                else if (count == 0)
                {
                    madeSelection = true;
                }
                if (madeSelection)
                {
                    button.setSelected(true);
                    logger.fine("Selected button #" + count);
                    selectedGroup = count;
                }
            }
            else
            {
                button.setSelected(false);
            }
            buttonGroup.add(button);
            String sizeText = gd.getSizeString();
            //Object[] installDataGUI = { button, gd.description, sizeText};
            Object[] data = {button, sizeText};
            model.addRow(data);
            count++;
        }
        return model;
    }

    protected static class GroupData
    {
        static final long ONEK = 1024;
        static final long ONEM = 1024 * 1024;
        static final long ONEG = 1024 * 1024 * 1024;

        String name;
        String description;
        String sortKey;
        long size;
        HashSet<String> packNames = new HashSet<String>();

        GroupData(String name, String description, String sortKey)
        {
            this.name = name;
            this.description = description;
            this.sortKey = sortKey;
        }

        String getSizeString()
        {
            String s;
            if (size < ONEK)
            {
                s = size + " bytes";
            }
            else if (size < ONEM)
            {
                s = size / ONEK + " KB";
            }
            else if (size < ONEG)
            {
                s = size / ONEM + " MB";
            }
            else
            {
                s = size / ONEG + " GB";
            }
            return s;
        }

        @Override
        public String toString()
        {
            StringBuffer tmp = new StringBuffer("GroupData(");
            tmp.append(name);
            tmp.append("){description=");
            tmp.append(description);
            tmp.append(", sortKey=");
            tmp.append(sortKey);
            tmp.append(", size=");
            tmp.append(size);
            tmp.append(", sizeString=");
            tmp.append(getSizeString());
            tmp.append(", packNames=");
            tmp.append(packNames);
            tmp.append("}");
            return tmp.toString();
        }
    }

}

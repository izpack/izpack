/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Marcus Wolschon
 * Copyright 2002 Jan Blok
 * Copyright 2004 Gaganis Giorgos
 * Copyright 2006,2007 Dennis Reil
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackColor;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.installer.util.PackHelper;

/**
 * User: Gaganis Giorgos Date: Sep 17, 2004 Time: 8:33:21 AM
 */
public class PacksModel extends AbstractTableModel
{
    private static final long serialVersionUID = 3258128076746733110L;
    private static final transient Logger logger = Logger.getLogger(PacksModel.class.getName());

    protected List<Pack> packs;
    protected List<Pack> allPacks;
    protected List<Pack> hiddenPacks;
    protected List<Pack> packsToInstall;

    private Map<String, Pack> installedPacks;

    protected int[] checkValues;

    private Map<String, Pack> nameToPack;
    private Map<String, Integer> nameToRow;

    private InstallData installData;
    private Messages messages;
    protected RulesEngine rules;
    protected Variables variables;

    private boolean modifyInstallation;

    //Negative number represent that the checkbox is unselectable
    public final int PARTIAL_SELECTED = 2;
    public final int SELECTED = 1;
    public final int DESELECTED = 0;
    public final int REQUIRED_SELECTED = -1;
    public final int DEPENDENT_DESELECTED = -2;
    public final int REQUIRED_PARTIAL_SELECTED = -3;
    public final int REQUIRED_DESELECTED = -4;

    public PacksModel(InstallData idata)
    {
        this.installData = idata;
        this.rules = idata.getRules();
        this.messages = idata.getMessages();
        this.variables = idata.getVariables();
        this.packsToInstall = idata.getSelectedPacks();

        this.modifyInstallation = Boolean.valueOf(idata.getVariable(InstallData.MODIFY_INSTALLATION));
        this.installedPacks = loadInstallationInformation(modifyInstallation);

        this.packs = getVisiblePacks();
        this.hiddenPacks = getHiddenPacks();
        this.allPacks = idata.getAvailablePacks();
        this.nameToRow = getNametoRowMapping(packs);
        this.nameToPack = getNametoPackMapping(allPacks);

        this.packs = setPackProperties(packs, nameToPack);
        this.checkValues = initCheckValues(packs, packsToInstall);

        updateConditions(true);
        updatePacksToInstall();
    }


    /**
     * @return a list of hidden packs
     */
    private List<Pack> getHiddenPacks()
    {
        List<Pack> hiddenPacks = new ArrayList<Pack>();
        for (Pack availablePack : installData.getAvailablePacks())
        {
            if (availablePack.isHidden())
            {
                hiddenPacks.add(availablePack);
            }
        }
        return hiddenPacks;
    }

    /**
     * @return a list of visible packs
     */
    public List<Pack> getVisiblePacks()
    {
        List<Pack> visiblePacks = new ArrayList<Pack>();
        for (Pack availablePack : installData.getAvailablePacks())
        {
            if (!availablePack.isHidden())
            {
                visiblePacks.add(availablePack);
            }
        }
        return visiblePacks;
    }

    /**
     * Generate a map from a pack's name to its pack object.
     *
     * @param packs list of pack objects
     * @return map from a pack's name to its pack object.
     */
    private Map<String, Pack> getNametoPackMapping(List<Pack> packs)
    {
        Map <String, Pack> nameToPack = new HashMap<String, Pack>();
        for (Pack pack : packs)
        {
            nameToPack.put(pack.getName(), pack);
        }
        return nameToPack;
    }

    /**
     * Generate a map from a pack's name to its row number visible on the UI.
     *
     * @param packs list of pack objects
     * @return map from a pack's name to its row number visible on the UI
     */
    private Map<String, Integer> getNametoRowMapping(List<Pack> packs)
    {
        Map<String, Integer> nameToPos = new HashMap<String, Integer>();
        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = packs.get(i);
            nameToPos.put(pack.getName(), i);
        }
        return nameToPos;
    }

    /**
     * Ensure that parent packs know which packs are their children.
     * Ensure that packs who have dependants know which packs depend on them
     *
     * @param packs packs visible to the user
     * @param nameToPack mapping from pack names to pack objects
     * @return packs
     */
    private List<Pack> setPackProperties(List<Pack> packs, Map<String, Pack> nameToPack)
    {
        Pack parent;
        for (Pack pack : packs)
        {
            if (pack.hasParent())
            {
                String parentName = pack.getParent();
                parent = nameToPack.get(parentName);
                parent.addChild(pack.getName());
            }

            if (pack.hasDependencies())
            {
                for (String name : pack.getDependencies())
                {
                    parent = nameToPack.get(name);
                    parent.addDependant(pack.getName());
                }
            }
        }
        return packs;
    }

    /**
     * Helper function to retrieve a pack object based on which row it is on.
     *
     * @param row
     * @return pack on the given row
     */
    public Pack getPackAtRow(int row)
    {
        return this.packs.get(row);
    }

    public void updateConditions()
    {
        this.updateConditions(false);
    }

    /**
     * Update the conditions for dependent packages.
     * Update the conditions for optional packages.
     *
     * @param initial indicates if its the first time updating conditions.
     */
    private void updateConditions(boolean initial)
    {
        boolean changes = true;

        while (changes)
        {
            changes = false;
            for (Pack pack : packs)
            {
                String packName = pack.getName();
                int pos = getPos(packName);
                logger.fine("Conditions fulfilled for: " + packName + "?");
                if (!rules.canInstallPack(packName, variables))
                {
                    logger.fine("no");
                    if (rules.canInstallPackOptional(packName, variables))
                    {
                        logger.fine("optional");
                        logger.fine(packName + " can be installed optionally.");
                        if (initial)
                        {
                            if (checkValues[pos] != DESELECTED)
                            {
                                checkValues[pos] = DESELECTED;
                                changes = true;
                            }
                        }
                    }
                    else
                    {
                        if (checkValues[pos] != DEPENDENT_DESELECTED)
                        {
                            logger.fine("Pack" + packName + " cannot be installed");
                            checkValues[pos] = DEPENDENT_DESELECTED;
                            changes = true;
                        }
                    }
                }
            }
        }
    }

    /**
     * Ensure that the table is up to date.
     * Order does matter
     */
    public void updateTable()
    {
        updateDeps();
        updateConditions();
        updatePacksToInstall();
        fireTableDataChanged();
    }

    /**
     * Initialize the data that represented the checkbox states.
     *
     * @param packs
     * @param packsToInstall
     * @return
     */
    private int[] initCheckValues(List<Pack> packs, List<Pack> packsToInstall)
    {
        int[] checkValues = new int[packs.size()];

        // If a pack is indicated to be installed checkbox value should be SELECTED
        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = packs.get(i);
            if (packsToInstall.contains(pack))
            {
                checkValues[i] = SELECTED;
            }
        }

        // If a packs dependency cannot be resolved checkboc value should be DEPENDENT_DESELECTED
        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = packs.get(i);
            if (checkValues[i] == DESELECTED)
            {
                List<String> deps = pack.getDependants();
                for (int j = 0; deps != null && j < deps.size(); j++)
                {
                    String name = deps.get(j);
                    int pos = getPos(name);
                    checkValues[pos] = DEPENDENT_DESELECTED;
                }
            }

            // for mutual exclusion, uncheck uncompatible packs too
            // (if available in the current installGroup)
            if (checkValues[i] > 0 && pack.getExcludeGroup() != null)
            {
                for (int q = 0; q < packs.size(); q++)
                {
                    if (q != i)
                    {
                        Pack otherPack = packs.get(q);
                        if (pack.getExcludeGroup().equals(otherPack.getExcludeGroup()))
                        {
                            if (checkValues[q] == SELECTED)
                            {
                                checkValues[q] = DESELECTED;
                            }
                        }
                    }
                }
            }
        }

        // Configure required packs
        for (Pack pack : packs)
        {
            if (pack.isRequired())
            {
                checkValues = propRequirement(pack.getName(), checkValues);
            }
        }

        return checkValues;
    }

    /**
     * Configure required packs.
     * @param name
     * @param checkValues
     * @return
     */
    private int [] propRequirement(String name, int[] checkValues)
    {

        final int pos = getPos(name);
        checkValues[pos] = REQUIRED_SELECTED;
        List<String> deps = packs.get(pos).getDependencies();
        for (int i = 0; deps != null && i < deps.size(); i++)
        {
            String s = deps.get(i);
            return propRequirement(s, checkValues);
        }
        return checkValues;

    }

    /**
     * Given a map of names and Integer for position and a name it return the position of this name
     * as an int
     *
     * @return position of the name
     */
    private int getPos(String name)
    {
        return nameToRow.get(name);
    }

    /*
     * @see TableModel#getRowCount()
     */
    @Override
    public int getRowCount()
    {
        return packs.size();
    }

    /*
     * @see TableModel#getColumnCount()
     */
    @Override
    public int getColumnCount()
    {
        return 3;
    }

    /*
     * @see TableModel#getColumnClass(int)
     */
    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
        switch (columnIndex)
        {
            case 0:
                return Integer.class;

            default:
                return String.class;
        }
    }

    /*
     * @see TableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        if (checkValues[rowIndex] < 0)
        {
            return false;
        }
        else
        {
            return columnIndex == 0;
        }
    }

    /*
     * @see TableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        Pack pack = packs.get(rowIndex);
        switch (columnIndex)
        {
            case 0:
                return checkValues[rowIndex];

            case 1:
                return PackHelper.getPackName(pack, messages);

            case 2:
                return Pack.toByteUnitsString(pack.getSize());

            default:
                return null;
        }
    }


    /**
     * Toggle checkbox value from selected to deselected and vice-versa.
     * @param rowIndex
     */
    public void toggleValueAt(int rowIndex)
    {
        if  (checkValues[rowIndex] == SELECTED)
        {
            setValueAt(DESELECTED, rowIndex, 0);
        }
        else
        {
            setValueAt(SELECTED, rowIndex, 0);
        }

    }

    /*
     * @see TableModel#setValueAt(Object, int, int)
     * Update the value of some checkbox
     */
    @Override
    public void setValueAt(Object checkValue, int rowIndex, int columnIndex)
    {
        if (columnIndex != 0 || !(checkValue instanceof Integer))
        {
            return;
        }
        else
        {
            Pack pack = packs.get(rowIndex);

            boolean added;
            if ((Integer) checkValue == SELECTED)
            {
                added = true;
                String name = pack.getName();
                if (rules.canInstallPack(name, variables) || rules.canInstallPackOptional(name, variables))
                {
                    if (pack.isRequired())
                    {
                        checkValues[rowIndex] = REQUIRED_SELECTED;
                    }
                    else
                    {
                        checkValues[rowIndex] = SELECTED;
                    }
                }
            }
            else
            {
                added = false;
                checkValues[rowIndex] = DESELECTED;
            }

            updateExcludes(rowIndex);
            updateDeps();

            if (added)
            {
                onSelectionUpdate(rowIndex);
                this.packsToInstall.add(pack);    //Temporarily add
                updateConditions();
                this.packsToInstall.remove(pack); //Redo
            }
            else
            {
                onDeselectionUpdate(rowIndex);
                this.packsToInstall.remove(pack); //Temporarily remove
                updateConditions();
                this.packsToInstall.add(pack); //Redo
            }

            updatePacksToInstall();

            if (pack.hasParent())
            {
                updateParent(pack);
            }
            else if (pack.hasChildren())
            {
                updateChildren(pack);
            }

            fireTableDataChanged();
        }
    }

    /**
     * Set the value of the parent pack of the given pack to SELECTED, PARTIAL_SELECT, or DESELECTED.
     * Value of the pack is dependent of its children values.
     *
     * @param childPack
     */
    private void updateParent(Pack childPack)
    {
        String parentName = childPack.getParent();
        Pack parentPack = nameToPack.get(parentName);
        int parentPosition = nameToRow.get(parentName);

        int childrenSelected = 0;
        for (String childName : parentPack.getChildren())
        {
            int childPosition = nameToRow.get(childName);
            if (isChecked(childPosition))
            {
                childrenSelected += 1;
            }
        }

        if (parentPack.getChildren().size() == childrenSelected)
        {
            if (checkValues[parentPosition] < 0)
            {
                checkValues[parentPosition] = REQUIRED_SELECTED;
            }
            else
            {
                checkValues[parentPosition] = SELECTED;
            }
        }
        else if (childrenSelected > 0)
        {

            if (checkValues[parentPosition] < 0)
            {
                checkValues[parentPosition] = REQUIRED_PARTIAL_SELECTED;
            }
            else
            {
                checkValues[parentPosition] = PARTIAL_SELECTED;
            }
        }
        else
        {
            if (checkValues[parentPosition] < 0)
            {
                checkValues[parentPosition] = REQUIRED_DESELECTED;
            }
            else
            {
                checkValues[parentPosition] = DESELECTED;
            }
        }
    }


    /**
     * Set the value of children packs to the same value as the parent pack.
     *
     * @param parentPack
     */
    private void updateChildren(Pack parentPack)
    {
        String parentName = parentPack.getName();
        int parentPosition = nameToRow.get(parentName);
        int parentValue = checkValues[parentPosition];

        for (String childName : parentPack.getChildren())
        {
            int childPosition = nameToRow.get(childName);
            checkValues[childPosition] = parentValue;
        }
    }

    /**
     * Select/Deselect pack(s) based on packsData mapping.
     * This is related to the onSelect and onDeselect attributes for packs.
     * User is not allowed to has a required pack for onSelect and onDeselect.
     *
     * @param packsData
     */
    private void selectionUpdate(Map<String, String> packsData)
    {
        RulesEngine rules = installData.getRules();
        for (Map.Entry<String, String> packData : packsData.entrySet())
        {
            int value, packPos;
            String packName = packData.getKey();
            String condition = packData.getValue();

            if(condition != null && !rules.isConditionTrue(condition))
            {
                return; //Do nothing if condition is false
            }

            Pack pack;

            if (packName.startsWith("!"))
            {
                packName = packName.substring(1);
                pack  = nameToPack.get(packName);
                packPos = getPos(packName);
                value = DESELECTED;
            }
            else
            {
                pack  = nameToPack.get(packName);
                packPos = getPos(packName);
                value = SELECTED;
            }
            if (!pack.isRequired() && dependenciesResolved(pack))
            {
                checkValues[packPos] = value;
            }
        }
    }

    /**
     * Update checkboxes based on the onSelect attribute
     * @param index
     */
    protected void onSelectionUpdate(int index)
    {
        Pack pack = packs.get(index);
        Map<String, String> packsData = pack.getOnSelect();
        selectionUpdate(packsData);
    }

    /**
     * Update checkboxes based on the onDeselect attribute
     * @param index
     */
    protected void onDeselectionUpdate(int index)
    {
        Pack pack = packs.get(index);
        Map<String, String> packsData = pack.getOnDeselect();
        selectionUpdate(packsData);
    }

    /**
     * Update packs to installed.
     * A pack to be installed is:
     * 1. A visible pack that has its checkbox checked
     * 2. A hidden pack that condition
     * @return
     */
    public List<Pack> updatePacksToInstall()
    {
        packsToInstall.clear();
        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = packs.get(i);
            if (isChecked(i) && !installedPacks.containsKey(pack.getName()))
            {
                packsToInstall.add(pack);
            }
            else if (installedPacks.containsKey(pack.getName()))
            {
                checkValues[i] = REQUIRED_PARTIAL_SELECTED;
            }
        }

        for (Pack hiddenPack : this.hiddenPacks)
        {
            if (this.rules.canInstallPack(hiddenPack.getName(), variables))
            {
                packsToInstall.add(hiddenPack);
            }
        }

        installData.setSelectedPacks(packsToInstall);
        return packsToInstall;
    }


    /**
     * This function updates the checkboxes after a change by disabling packs that cannot be
     * installed anymore and enabling those that can after the change. This is accomplished by
     * running a search that pinpoints the packs that must be disabled by a non-fullfiled
     * dependency.
     * TODO: Look into "+2" and "-2", doesn't look safe
     */
    protected void updateDeps()
    {
        int[] statusArray = new int[packs.size()];
        for (int i = 0; i < statusArray.length; i++)
        {
            statusArray[i] = 0;
        }
        dfs(statusArray);
        for (int i = 0; i < statusArray.length; i++)
        {
            if (statusArray[i] == 0 && checkValues[i] < 0)
            {
                checkValues[i] += PARTIAL_SELECTED;
            }
            if (statusArray[i] == 1 && checkValues[i] >= 0)
            {
                checkValues[i] = DEPENDENT_DESELECTED;
            }

        }
        // The required ones must propagate their required status to all the ones that they depend on
        for (Pack pack : packs)
        {
            if (pack.isRequired())
            {
                String name = pack.getName();
                if (!(!rules.canInstallPack(name, variables) && rules.canInstallPackOptional(name, variables)))
                {
                    checkValues = propRequirement(name, checkValues);
                }
            }
        }

    }

    /*
     * Sees which packs (if any) should be unchecked and updates checkValues
     */
    protected void updateExcludes(int rowindex)
    {
        int value = checkValues[rowindex];
        Pack pack = packs.get(rowindex);
        if (value > 0 && pack.getExcludeGroup() != null)
        {
            for (int q = 0; q < packs.size(); q++)
            {
                if (rowindex != q)
                {
                    Pack otherPack = packs.get(q);
                    String name1 = otherPack.getExcludeGroup();
                    String name2 = pack.getExcludeGroup();
                    if (name2.equals(name1))
                    {
                        if (checkValues[q] == SELECTED)
                        {
                            checkValues[q] = DESELECTED;
                        }
                    }
                }
            }
        }
    }


    /**
     * We use a modified dfs graph search algorithm as described in: Thomas H. Cormen, Charles
     * Leiserson, Ronald Rivest and Clifford Stein. Introduction to algorithms 2nd Edition
     * 540-549,MIT Press, 2001
     */
    private int dfs(int[] status)
    {
        Map<String, PackColor> colours = new HashMap<String, PackColor>();
        for (int i = 0; i < packs.size(); i++)
        {
            for (Pack pack : packs)
            {
                colours.put(pack.getName(), PackColor.WHITE);
            }
            Pack pack = packs.get(i);
            boolean wipe = false;

            if (dfsVisit(pack, status, wipe, colours) != 0)
            {
                return -1;
            }

        }
        return 0;
    }

    private int dfsVisit(Pack u, int[] status, boolean wipe, Map<String, PackColor> colours)
    {
        colours.put(u.getName(), PackColor.GREY);
        int check = checkValues[getPos(u.getName())];

        if (Math.abs(check) != 1)
        {
            wipe = true;
        }
        List<String> deps = u.getDependants();
        if (deps != null)
        {
            for (String name : deps)
            {
                Pack v = nameToPack.get(name);
                if (wipe)
                {
                    status[getPos(v.getName())] = 1;
                }
                if (colours.get(v.getName()) == PackColor.WHITE)
                {
                    final int result = dfsVisit(v, status, wipe, colours);
                    if (result != 0)
                    {
                        return result;
                    }
                }
            }
        }
        colours.put(u.getName(), PackColor.BLACK);
        return 0;
    }

    /**
     * Get previously installed packs on modifying a pre-installed application
     * @return the installedPacks
     */
    public Map<String, Pack> getInstalledPacks()
    {
        return this.installedPacks;
    }

    /**
     * @return the modifyInstallation
     */
    public boolean isModifyInstallation()
    {
        return this.modifyInstallation;
    }

    /**
     * Remove pack that are already installed
     * @param selectedPacks
     */
    private void removeAlreadyInstalledPacks(List<Pack> selectedPacks)
    {
        List<Pack> removePacks = new ArrayList<Pack>();

        for (Pack selectedPack : selectedPacks)
        {
            if (installedPacks.containsKey(selectedPack.getName()))
            {
                // pack is already installed, remove it
                removePacks.add(selectedPack);
            }
        }
        for (Pack removePack : removePacks)
        {
            selectedPacks.remove(removePack);
        }
    }

    private Map<String, Pack> loadInstallationInformation(boolean modifyInstallation)
    {
        Map<String, Pack> installedpacks = new HashMap<String, Pack>();
        if (!modifyInstallation)
        {
            return installedpacks;
        }

        // installation shall be modified
        // load installation information
        ObjectInputStream oin = null;
        try
        {
            FileInputStream fin = new FileInputStream(new File(
                    installData.getInstallPath() + File.separator + InstallData.INSTALLATION_INFORMATION));
            oin = new ObjectInputStream(fin);
            List<Pack> packsinstalled = (List<Pack>) oin.readObject();
            for (Pack installedpack : packsinstalled)
            {
                installedpacks.put(installedpack.getName(), installedpack);
            }
            this.removeAlreadyInstalledPacks(installData.getSelectedPacks());
            logger.fine("Found " + packsinstalled.size() + " installed packs");

            Properties variables = (Properties) oin.readObject();

            for (Object key : variables.keySet())
            {
                installData.setVariable((String) key, (String) variables.get(key));
            }
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            if (oin != null)
            {
                try { oin.close(); }
                catch (IOException e) {}
            }
        }
        return installedpacks;
    }

    /**
     * Check if a pack's dependencies are resolved.
     * @param pack
     * @return
     */
    private boolean dependenciesResolved(Pack pack)
    {
        if(!pack.hasDependencies())
        {
            return true;
        }
        for (String dependentPackName : pack.getDependencies())
        {
            if (!isChecked(nameToRow.get(dependentPackName)))
            {
                return false;
            }
        }
        return true;
    }
    /**
     * @return mapping from pack name to pack
     */
    public Map<String, Pack> getNameToPack()
    {
        return nameToPack;
    }

    /**
     * @return mapping from pack to row position
     */
    public Map<Pack, Integer> getPacksToRowNumbers()
    {
        Map<Pack, Integer> packsToRowNumbers = new HashMap<Pack, Integer>();
        for (Map.Entry<String, Integer> entry : nameToRow.entrySet())
        {
            packsToRowNumbers.put(nameToPack.get(entry.getKey()), entry.getValue());
        }
        return packsToRowNumbers;
    }

    /**
     * @return mapping from pack name to row position
     */
    public Map<String, Integer> getNameToRow()
    {
        return nameToRow;
    }

    /**
     * @return the number of bytes that the installation requires based on selected packs
     */
    public int getTotalByteSize()
    {
        Map<Pack, Integer> packToRow = getPacksToRowNumbers();
        int row;
        int bytes = 0;
        for (Pack pack : packs)
        {
            row = packToRow.get(pack);
            if(isChecked(row))
            {
                bytes += pack.getSize();
            }
        }
        return bytes;
    }

    /**
     * Check if the checkbox is selected given its row.
     *
     * @param row
     * @return {@code true} if checkbox is selected else {@code false}
     */
    public boolean isChecked(int row)
    {
        if(checkValues[row] == SELECTED
                || checkValues[row] == REQUIRED_SELECTED
                || checkValues[row] == PARTIAL_SELECTED
                || checkValues[row] == REQUIRED_PARTIAL_SELECTED)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * @param row
     * @return {@code true} if checkbox is partially selected else {@code false}
     */
    public boolean isPartiallyChecked(int row)
    {
        if(checkValues[row] == PARTIAL_SELECTED
                || checkValues[row] == REQUIRED_PARTIAL_SELECTED)
        {
            return true;
        }
        return  false;
    }

    /**
     * @param row
     * @return {@code true} if the checkbox is selected else {@code false}
     */
    public boolean isCheckBoxSelectable(int row)
    {
        return checkValues[row] >= 0;
    }

    /**
     * @return {@code true} if any dependencies for the visible packs exists else {@code false}
     */
    public boolean dependenciesExist()
    {
        for (Pack pack : getVisiblePacks())
        {
            if (pack.hasDependencies())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @param packName
     * @return helper method to get a pack object from the pack's name
     */
    public Pack getPack(String packName)
    {
        return nameToPack.get(packName);
    }
}

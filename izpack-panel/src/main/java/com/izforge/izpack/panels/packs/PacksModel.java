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

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackColor;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.installer.util.PackHelper;

import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.logging.Logger;

/**
 * User: Gaganis Giorgos Date: Sep 17, 2004 Time: 8:33:21 AM
 */
public class PacksModel extends AbstractTableModel
{
    private static final long serialVersionUID = 3258128076746733110L;
    private static final transient Logger logger = Logger.getLogger(PacksModel.class.getName());

    protected List<Pack> packs;
    protected final List<Pack> hiddenPacks;
    private final List<Pack> packsToInstall;

    protected final transient RulesEngine rules;
    protected final transient Variables variables;
    private final transient InstallData installData;
    private transient Messages messages;

    private final Map<String, Pack> installedPacks;

    List<CbSelectionState> checkValues;

    private final Map<String, Pack> nameToPack;
    private final Map<String, Integer> nameToRow;

    private final boolean modifyInstallation;

    public PacksModel(InstallData idata)
    {
        this.installData = idata;
        this.rules = idata.getRules();
        try{
          this.messages = idata.getMessages().newMessages(Resources.PACK_TRANSLATIONS_RESOURCE_NAME);
        } catch(ResourceNotFoundException ex){
          this.messages=idata.getMessages();
        }
        this.variables = idata.getVariables();
        this.packsToInstall = idata.getSelectedPacks();

        this.modifyInstallation = Boolean.valueOf(idata.getVariable(InstallData.MODIFY_INSTALLATION));
        this.installedPacks = loadInstallationInformation(modifyInstallation);

        this.packs = getVisiblePacks();
        this.hiddenPacks = getHiddenPacks();
        this.nameToRow = getNametoRowMapping(packs);
        this.nameToPack = getNametoPackMapping(idata.getAvailablePacks());

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

    private void updateConditions()
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
                if (!rules.canInstallPack(packName, variables))
                {
                    logger.fine("Conditions for pack '" + packName + "' are not complied with");
                    if (rules.canInstallPackOptional(packName, variables))
                    {
                        logger.fine("Pack '" + packName + "' can be installed optionally.");
                        if (initial)
                        {
                            if (checkValues.get(pos) != CbSelectionState.DESELECTED)
                            {
                                checkValues.set(pos, CbSelectionState.DESELECTED);
                                changes = true;
                            }
                        }
                    }
                    else
                    {
                        if (checkValues.get(pos) != CbSelectionState.DEPENDENT_DESELECTED)
                        {
                            logger.fine("Pack '" + packName + "' cannot be installed");
                            checkValues.set(pos, CbSelectionState.DEPENDENT_DESELECTED);
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
    private List<CbSelectionState> initCheckValues(List<Pack> packs, List<Pack> packsToInstall)
    {
        CbSelectionState[] checkValues = new CbSelectionState[packs.size()];

        // If a pack is indicated to be installed checkbox value should be SELECTED
        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = packs.get(i);
            if (packsToInstall.contains(pack))
            {
                checkValues[i] = CbSelectionState.SELECTED;
            }
            else
            {
                checkValues[i] = CbSelectionState.DESELECTED;
            }
        }

        // If a packs dependency cannot be resolved checkboc value should be DEPENDENT_DESELECTED
        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = packs.get(i);
            if (checkValues[i] == CbSelectionState.DESELECTED)
            {
                List<String> deps = pack.getDependants();
                for (int j = 0; deps != null && j < deps.size(); j++)
                {
                    String name = deps.get(j);
                    int pos = getPos(name);
                    checkValues[pos] = CbSelectionState.DEPENDENT_DESELECTED;
                }
            }

            // for mutual exclusion, uncheck uncompatible packs too
            // (if available in the current installGroup)
            CbSelectionState checkState = checkValues[i];
            if (checkState != null && checkState.isFullyOrPartiallySelected() && pack.getExcludeGroup() != null)
            {
                for (int q = 0; q < packs.size(); q++)
                {
                    if (q != i)
                    {
                        Pack otherPack = packs.get(q);
                        if (pack.getExcludeGroup().equals(otherPack.getExcludeGroup()))
                        {
                            if (checkValues[q] == CbSelectionState.SELECTED)
                            {
                                checkValues[q] = CbSelectionState.DESELECTED;
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
                checkValues = propRequirement(pack.getName(), Arrays.asList(checkValues)).toArray(new CbSelectionState[checkValues.length]);
            }
        }

        return Arrays.asList(checkValues);
    }

    /**
     * Configure required packs.
     * @param name
     * @param checkValues
     * @return
     */
    private List<CbSelectionState> propRequirement(String name, List<CbSelectionState> checkValues)
    {

        final int pos = getPos(name);
        checkValues.set(pos, CbSelectionState.REQUIRED_SELECTED);
        List<String> deps = packs.get(pos).getDependencies();
        if (deps != null)
        {
            for (String s : deps)
            {
                return propRequirement(s, checkValues);
            }
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
        CbSelectionState state = checkValues.get(rowIndex);
        return state != null && state.isSelectable() && columnIndex == 0;
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
                CbSelectionState state = checkValues.get(rowIndex);
                return state != null ? checkValues.get(rowIndex) : null;

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
        CbSelectionState state = checkValues.get(rowIndex);
        if  (state != null && checkValues.get(rowIndex) == CbSelectionState.SELECTED)
        {
            setValueAt(CbSelectionState.DESELECTED, rowIndex, 0);
        }
        else
        {
            setValueAt(CbSelectionState.SELECTED, rowIndex, 0);
        }

    }

    /*
     * @see TableModel#setValueAt(Object, int, int)
     * Update the value of some checkbox
     */
    @Override
    public void setValueAt(Object checkValue, int rowIndex, int columnIndex)
    {
        if (!(columnIndex != 0 || !(checkValue instanceof CbSelectionState)))
        {
            Pack pack = packs.get(rowIndex);

            boolean added;
            if ((CbSelectionState) checkValue == CbSelectionState.SELECTED)
            {
                added = true;
                String name = pack.getName();
                if (rules.canInstallPack(name, variables) || rules.canInstallPackOptional(name, variables))
                {
                    if (pack.isRequired())
                    {
                        checkValues.set(rowIndex, CbSelectionState.REQUIRED_SELECTED);
                    }
                    else
                    {
                        checkValues.set(rowIndex, CbSelectionState.SELECTED);
                    }
                }
            }
            else
            {
                added = false;
                checkValues.set(rowIndex, CbSelectionState.DESELECTED);
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
            if (!checkValues.get(parentPosition).isSelectable())
            {
                checkValues.set(parentPosition, CbSelectionState.REQUIRED_SELECTED);
            }
            else
            {
                checkValues.set(parentPosition, CbSelectionState.SELECTED);
            }
        }
        else if (childrenSelected > 0)
        {

            if (!checkValues.get(parentPosition).isSelectable())
            {
                checkValues.set(parentPosition, CbSelectionState.REQUIRED_PARTIAL_SELECTED);
            }
            else
            {
                checkValues.set(parentPosition, CbSelectionState.PARTIAL_SELECTED);
            }
        }
        else
        {
            if (!checkValues.get(parentPosition).isSelectable())
            {
                checkValues.set(parentPosition, CbSelectionState.REQUIRED_DESELECTED);
            }
            else
            {
                checkValues.set(parentPosition, CbSelectionState.DESELECTED);
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
        CbSelectionState parentValue = checkValues.get(parentPosition);

        for (String childName : parentPack.getChildren())
        {
            int childPosition = nameToRow.get(childName);
            if (checkValues.get(childPosition).isSelectable())
            {
                checkValues.set(childPosition, parentValue);
            }
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
            CbSelectionState value;
            int packPos;
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
                value = CbSelectionState.DESELECTED;
            }
            else
            {
                pack  = nameToPack.get(packName);
                packPos = getPos(packName);
                value = CbSelectionState.SELECTED;
            }
            if (!pack.isRequired() && dependenciesResolved(pack))
            {
                checkValues.set(packPos, value);
            }
        }
    }

    /**
     * Update checkboxes based on the onSelect attribute
     * @param index
     */
    private void onSelectionUpdate(int index)
    {
        Pack pack = packs.get(index);
        Map<String, String> packsData = pack.getOnSelect();
        selectionUpdate(packsData);
    }

    /**
     * Update checkboxes based on the onDeselect attribute
     * @param index
     */
    private void onDeselectionUpdate(int index)
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
                checkValues.set(i, CbSelectionState.REQUIRED_PARTIAL_SELECTED);
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
        
        // calculate the estimated size and store in variable
        long estimatedSizeInBytes = 0;
        for (Pack p : packsToInstall)
        {
        	estimatedSizeInBytes += p.getSize();
        }
        // the size of the installed files (in KB)
        installData.setVariable(InstallData.ESTIMATED_SIZE, Long.toString(estimatedSizeInBytes / 1024));
        
        return packsToInstall;
    }


    /**
     * This function updates the checkboxes after a change by disabling packs that cannot be
     * installed anymore and enabling those that can after the change. This is accomplished by
     * running a search that pinpoints the packs that must be disabled by a non-fulfilled
     * dependency.
     * TODO: Look into "+2" and "-2", doesn't look safe
     */
    private void updateDeps()
    {
        int[] statusArray = new int[packs.size()];
        dfs(statusArray);
        for (int i = 0; i < statusArray.length; i++)
        {
            CbSelectionState cbSelectionState = checkValues.get(i);
            if (statusArray[i] == 0 && !cbSelectionState.isSelectable())
            {
                // When dependency is selected, dependent should be marked as deselected to avoid double-clicking to
                // select dependent, issue [IZPACK-1653]
                checkValues.set(i, cbSelectionState == CbSelectionState.DEPENDENT_DESELECTED ?
                        CbSelectionState.DESELECTED : CbSelectionState.PARTIAL_SELECTED);
            }
            if (statusArray[i] == 1 && cbSelectionState.isSelectable())
            {
                checkValues.set(i, CbSelectionState.DEPENDENT_DESELECTED);
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
    private void updateExcludes(int rowindex)
    {
        CbSelectionState value = checkValues.get(rowindex);
        Pack pack = packs.get(rowindex);
        if (value != null && value.isFullyOrPartiallySelected() && pack.getExcludeGroup() != null)
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
                        if (checkValues.get(q) == CbSelectionState.SELECTED)
                        {
                            checkValues.set(q, CbSelectionState.DESELECTED);
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
    private void dfs(int[] status)
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
                return;
            }

        }
    }

    private int dfsVisit(Pack u, int[] status, boolean wipe, Map<String, PackColor> colours)
    {
        colours.put(u.getName(), PackColor.GREY);
        CbSelectionState check = checkValues.get(getPos(u.getName()));

        if (!check.isSelectedOrRequiredSelected())
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
     * @param installedPacks the packs found in an existing .installationinformation file
     */
    private void removeAlreadyInstalledPacks(List<Pack> selectedPacks, Map<String, Pack> installedPacks)
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
        Map<String, Pack> readPacks = new HashMap<String, Pack>();
        if (!modifyInstallation)
        {
            return readPacks;
        }

        // installation shall be modified
        // load installation information
        ObjectInputStream oin = null;
        File installInfo = new File(installData.getInstallPath(), InstallData.INSTALLATION_INFORMATION);
        try
        {
            if (installInfo.exists())
            {
                FileInputStream fin = new FileInputStream(installInfo);
                oin = new ObjectInputStream(fin);
                //noinspection unchecked
                List<Pack> packsinstalled = (List<Pack>) oin.readObject();
                for (Pack installedpack : packsinstalled)
                {
                    readPacks.put(installedpack.getName(), installedpack);
                }
                removeAlreadyInstalledPacks(installData.getSelectedPacks(), readPacks);
                logger.fine("Found " + packsinstalled.size() + " installed packs");

                Properties variables = (Properties) oin.readObject();
                for (Object key : variables.keySet())
                {
                    installData.setVariable((String) key, (String) variables.get(key));
                }
            }
        }
        catch (Exception e)
        {
            logger.warning("Could not read installation information: " + e.getMessage());
        }
        finally
        {
            if (oin != null)
            {
                try
                {
                    oin.close();
                }
                catch (IOException ignored) {}
            }
        }
        return readPacks;
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
    public long getTotalByteSize()
    {
        Map<Pack, Integer> packToRow = getPacksToRowNumbers();
        int row;
        long bytes = 0;
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
        CbSelectionState state = checkValues.get(row);
        return state != null ? state.isChecked() : false;
    }

    /**
     * @param row
     * @return {@code true} if checkbox is partially selected else {@code false}
     */
    public boolean isPartiallyChecked(int row)
    {
        CbSelectionState state = checkValues.get(row);
        return state != null ? state.isPartiallyChecked() : false;
    }

    /**
     * @param row
     * @return {@code true} if the checkbox is selected else {@code false}
     */
    public boolean isCheckBoxSelectable(int row)
    {
        CbSelectionState state = checkValues.get(row);
        return state != null ? state.isSelectable() : false;
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

    /**
     * Enumeration of possible internal model states of a pack checkbox
     */
    public enum CbSelectionState
    {
        PARTIAL_SELECTED(2), SELECTED(1), DESELECTED(0),
        REQUIRED_SELECTED(-1), DEPENDENT_DESELECTED(-2), REQUIRED_PARTIAL_SELECTED(-3), REQUIRED_DESELECTED(-4);

        private final int value;

        CbSelectionState(int value)
        {
            this.value = value;
        }

        /**
         * Check whether the checkbox state is one of {@code SELECTED}, {@code PARTIAL_SELECTED}.
         * @return {@code true} if the above condition is met
         */
        public boolean isFullyOrPartiallySelected()
        {
            return this.value > 0;
        }

        /**
         * Check whether the checkbox state is one of {@code DESELECTED}, {@code SELECTED}, {@code PARTIAL_SELECTED}.
         * @return {@code true} if the above condition is met
         */
        public boolean isSelectable()
        {
            return this.value >= 0;
        }

        /**
         * Check whether the checkbox state is one of {@code REQUIRED_SELECTED}, {@code SELECTED}.
         * @return {@code true} if the above condition is met
         */
        public boolean isSelectedOrRequiredSelected()
        {
            final int ordinal = this.ordinal();
            return SELECTED.ordinal() == ordinal || REQUIRED_SELECTED.ordinal() == ordinal;
        }

        /**
         * Check if the checkbox state means selected or required to be selected (even partially).
         * @return {@code true} if checkbox is selected else {@code false}
         */
        public boolean isChecked()
        {
            final int ordinal = this.ordinal();
            return SELECTED.ordinal() == ordinal || REQUIRED_SELECTED.ordinal() == ordinal
                    || PARTIAL_SELECTED.ordinal() == ordinal || REQUIRED_PARTIAL_SELECTED.ordinal() == ordinal;
        }

        /**
         * Check if the checkbox state means partially selected or required to be partially selected.
         * @return {@code true} if checkbox is selected else {@code false}
         */
        public boolean isPartiallyChecked()
        {
            final int ordinal = this.ordinal();
            return PARTIAL_SELECTED.ordinal() == ordinal || REQUIRED_PARTIAL_SELECTED.ordinal() == ordinal;
        }
    }
}

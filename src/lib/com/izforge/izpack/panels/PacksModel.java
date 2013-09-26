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

package com.izforge.izpack.panels;

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.Pack;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.rules.RulesEngine;
import com.izforge.izpack.util.Debug;

import javax.swing.table.AbstractTableModel;
import java.io.*;
import java.util.*;

/**
 * User: Gaganis Giorgos Date: Sep 17, 2004 Time: 8:33:21 AM
 */
class PacksModel extends AbstractTableModel
{

    /**
     *
     */
    private static final long serialVersionUID = 3258128076746733110L;

    private static final String INITAL_PACKSELECTION = "initial.pack.selection";

    private List packs;
    private List<Pack> hiddenPacks;

    private List packsToInstall;

    private Map installedpacks;
    private boolean modifyinstallation;


    private PacksPanelInterface panel;

    private LocaleDatabase langpack;

    // This is used to represent the status of the checkbox
    private int[] checkValues;

    // Map to hold the object name relationship
    Map<String, Pack> namesObj;

    // Map to hold the object name relationship
    Map<String, Integer> namesPos;

    // reference to the RulesEngine for validating conditions
    private RulesEngine rules;

    // reference to the current variables, needed for condition validation
    private Properties variables;
    
    private InstallData idata;

    public PacksModel(PacksPanelInterface panel, InstallData idata, RulesEngine rules)
    {
        this.idata = idata;
        modifyinstallation = Boolean.valueOf(idata.getVariable(InstallData.MODIFY_INSTALLATION));
        this.installedpacks = new HashMap();

        // cannot modify
        // for update purpose
        if ( false && modifyinstallation)
        {
            // installation shall be modified
            // load installation information

            try
            {
                FileInputStream fin = new FileInputStream(new File(idata.getInstallPath() + File.separator + AutomatedInstallData.INSTALLATION_INFORMATION));
                ObjectInputStream oin = new ObjectInputStream(fin);
                List packsinstalled = (List) oin.readObject();
                for (Object aPacksinstalled : packsinstalled)
                {
                    Pack installedpack = (Pack) aPacksinstalled;
                    if ((installedpack.id != null) && (installedpack.id.length() > 0))
                    {
                        this.installedpacks.put(installedpack.id, installedpack);
                    }
                    else
                    {
                        this.installedpacks.put(installedpack.name, installedpack);
                    }
                }
                this.removeAlreadyInstalledPacks(idata.selectedPacks);
                Debug.trace("Found " + packsinstalled.size() + " installed packs");

                Properties variables = (Properties) oin.readObject();

                Iterator iter = variables.keySet().iterator();
                while (iter.hasNext())
                {
                    Object key = iter.next();
                    idata.setVariable((String) key, (String) variables.get(key));
                }
                fin.close();
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
        }
        this.rules = rules;
        
        this.packs = new ArrayList();
        this.hiddenPacks = new ArrayList<Pack>();
        for(Pack availablePack : idata.availablePacks){
            // only add a pack if not hidden
            if (!availablePack.isHidden()){
                this.packs.add(availablePack);
            }
            else {
                this.hiddenPacks.add(availablePack);
            }
        }        
        
        this.packsToInstall = idata.selectedPacks;
        this.panel = panel;
        this.variables = idata.getVariables();
        this.variables.setProperty(INITAL_PACKSELECTION, Boolean.toString(true));
        langpack = panel.getLangpack();
        checkValues = new int[packs.size()];
        reverseDeps();
        initvalues();
        this.updateConditions(true);
        refreshPacksToInstall();
        this.variables.setProperty(INITAL_PACKSELECTION, Boolean.toString(false));
    }
    
    public Pack getPackAtRow(int row){
        return (Pack) this.packs.get(row);
    }

    private void removeAlreadyInstalledPacks(List selectedpacks)
    {
        List<Pack> removepacks = new ArrayList<Pack>();

        for (Object selectedpack1 : selectedpacks)
        {
            Pack selectedpack = (Pack) selectedpack1;
            String key = "";
            if ((selectedpack.id != null) && (selectedpack.id.length() > 0))
            {
                key = selectedpack.id;
            }
            else
            {
                key = selectedpack.name;
            }
            if (installedpacks.containsKey(key))
            {
                // pack is already installed, remove it
                removepacks.add(selectedpack);
            }
        }
        for (Pack removepack : removepacks)
        {
            selectedpacks.remove(removepack);
        }
    }

    public void updateConditions()
    {
        this.updateConditions(false);
    }

    private void updateConditions(boolean initial)
    {
        boolean changes = true;

        while (changes)
        {
            changes = false;
            // look for packages,
            for (Object pack1 : packs)
            {
                Pack pack = (Pack) pack1;
                int pos = getPos(pack.name);
                Debug.trace("Conditions fulfilled for: " + pack.name + "?");
                if (!this.rules.canInstallPack(pack.id, this.variables))
                {
                    Debug.trace("no");
                    if (this.rules.canInstallPackOptional(pack.id, this.variables))
                    {
                        Debug.trace("optional");
                        Debug.trace(pack.id + " can be installed optionally.");
                        if (initial)
                        {
                            if (checkValues[pos] != 0)
                            {
                                checkValues[pos] = 0;
                                changes = true;
                                // let the process start from the beginning
                                break;
                            }
                        }
                        else
                        {
                            // just do nothing                       
                        }
                    }
                    else
                    {
                        Debug.trace(pack.id + " can not be installed.");
                        if (checkValues[pos] != -2)
                        {
                            checkValues[pos] = -2;
                            changes = true;
                            // let the process start from the beginning
                            break;
                        }
                    }
                }
            }
            refreshPacksToInstall();
        }
    }

    /**
     * Creates the reverse dependency graph
     */
    private void reverseDeps()
    {
        // name to pack map
        namesObj = new HashMap<String, Pack>();
        for (Object pack2 : packs)
        {
            Pack pack = (Pack) pack2;
            namesObj.put(pack.name, pack);
        }
        // process each pack
        for (Object pack1 : packs)
        {
            Pack pack = (Pack) pack1;
            List<String> deps = pack.dependencies;
            for (int j = 0; deps != null && j < deps.size(); j++)
            {
                String name = deps.get(j);
                Pack parent = namesObj.get(name);
                parent.addRevDep(pack.name);
            }
        }

    }

    private void initvalues()
    {
        // name to pack position map
        namesPos = new HashMap<String, Integer>();
        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = (Pack) packs.get(i);
            namesPos.put(pack.name, i);
        }
        // Init to the first values
        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = (Pack) packs.get(i);
            if (packsToInstall.contains(pack))
            {
                checkValues[i] = 1;
            }
        }

        // Check out and disable the ones that are excluded by non fullfiled
        // deps
        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = (Pack) packs.get(i);
            if (checkValues[i] == 0)
            {
                List<String> deps = pack.revDependencies;
                for (int j = 0; deps != null && j < deps.size(); j++)
                {
                    String name = deps.get(j);
                    int pos = getPos(name);
                    checkValues[pos] = -2;
                }
            }
            // for mutual exclusion, uncheck uncompatible packs too
            // (if available in the current installGroup)

            if (checkValues[i] > 0 && pack.excludeGroup != null)
            {
                for (int q = 0; q < packs.size(); q++)
                {
                    if (q != i)
                    {
                        Pack otherpack = (Pack) packs.get(q);
                        if (pack.excludeGroup.equals(otherpack.excludeGroup))
                        {
                            if (checkValues[q] == 1)
                            {
                                checkValues[q] = 0;
                            }
                        }
                    }
                }
            }
        }
        // The required ones must propagate their required status to all the
        // ones
        // that they depend on
        for (Object pack1 : packs)
        {
            Pack pack = (Pack) pack1;

            if (pack.required)
            {
                propRequirement(pack.name);
            }
        }

        refreshPacksToInstall();
    }

    private void propRequirement(String name)
    {

        final int pos = getPos(name);
        checkValues[pos] = -1;
        List<String> deps = ((Pack) packs.get(pos)).dependencies;
        for (int i = 0; deps != null && i < deps.size(); i++)
        {
            String s = deps.get(i);
            propRequirement(s);
        }

    }

    /**
     * Given a map of names and Integer for position and a name it return the position of this name
     * as an int
     *
     * @return position of the name
     */
    private int getPos(String name)
    {
        return namesPos.get(name);
    }

    /*
     * @see TableModel#getRowCount()
     */
    public int getRowCount()
    {
        return packs.size();
    }

    /*
     * @see TableModel#getColumnCount()
     */
    public int getColumnCount()
    {
        boolean doNotShowPackSize = Boolean.parseBoolean(idata.guiPrefs.modifier.get("doNotShowPackSizeColumn"));
        
        int result=0;
        if (!doNotShowPackSize){
            result = 3;
        }
        else {
            result = 2;
        }        
        return result;
    }

    /*
     * @see TableModel#getColumnClass(int)
     */
    public Class getColumnClass(int columnIndex)
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
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        Pack pack = (Pack) packs.get(rowIndex);
        switch (columnIndex)
        {
            case 0:

                return checkValues[rowIndex];

            case 1:

                Object name = null;
                if (langpack != null && pack.id != null && !pack.id.equals(""))
                {
                    name = langpack.get(pack.id);
                }
                if (name == null || "".equals(name))
                {
                    name = pack.name;
                }
                return name;

            case 2:
                return Pack.toByteUnitsString(pack.nbytes);

            default:
                return null;
        }
    }

    /*
     * @see TableModel#setValueAt(Object, int, int)
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        if (columnIndex == 0)
        {
            if (aValue instanceof Integer)
            {
                Pack pack = (Pack) packs.get(rowIndex);
                boolean packadded = false;
                if ((Integer) aValue == 1)
                {
                    packadded = true;
                    String packid = pack.id;
                    if (packid != null)
                    {
                        if (this.rules.canInstallPack(packid, this.variables) || this.rules.canInstallPackOptional(packid, this.variables))
                        {
                            if (pack.required)
                            {
                                checkValues[rowIndex] = -1;
                            }
                            else
                            {
                                checkValues[rowIndex] = 1;
                            }
                        }
                    }
                    else
                    {
                        if (pack.required)
                        {
                            checkValues[rowIndex] = -1;
                        }
                        else
                        {
                            checkValues[rowIndex] = 1;
                        }
                    }
                }
                else
                {
                    packadded = false;
                    checkValues[rowIndex] = 0;
                }
                updateExcludes(rowIndex);
                updateDeps();

                if (packadded)
                {
                    if (panel.getDebugger() != null)
                    {
                        panel.getDebugger().packSelectionChanged("after adding pack " + pack.id);
                    }
                    // temporarily add pack to packstoinstall
                    this.packsToInstall.add(pack);
                }
                else
                {
                    if (panel.getDebugger() != null)
                    {
                        panel.getDebugger().packSelectionChanged("after removing pack " + pack.id);
                    }
                    // temporarily remove pack from packstoinstall
                    this.packsToInstall.remove(pack);
                }
                updateConditions();
                if (packadded)
                {
                    // redo 
                    this.packsToInstall.remove(pack);
                }
                else
                {
                    // redo
                    this.packsToInstall.add(pack);
                }
                refreshPacksToInstall();
                updateBytes();
                fireTableDataChanged();                
                panel.showSpaceRequired();
            }
        }
    }

    private void refreshPacksToInstall()
    {

        packsToInstall.clear();
        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = (Pack) packs.get(i);
            String key = "";
            if ((pack.id != null) && (pack.id.length() > 0))
            {
                key = pack.id;
            }
            else
            {
                key = pack.name;
            }
            if ((Math.abs(checkValues[i]) == 1) && (!installedpacks.containsKey(key)))
            {
                packsToInstall.add(pack);
            }

        }

        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = (Pack) packs.get(i);

            String key = "";
            if ((pack.id != null) && (pack.id.length() > 0))
            {
                key = pack.id;
            }
            else
            {
                key = pack.name;
            }
            if (installedpacks.containsKey(key))
            {
                checkValues[i] = -3;
            }
        }
        // add hidden packs
        for (Pack hiddenpack : this.hiddenPacks){
            if (this.rules.canInstallPack(hiddenpack.id,variables)){
                packsToInstall.add(hiddenpack);
            }
        }
    }


    /**
     * This function updates the checkboxes after a change by disabling packs that cannot be
     * installed anymore and enabling those that can after the change. This is accomplished by
     * running a search that pinpoints the packs that must be disabled by a non-fullfiled
     * dependency.
     */
    private void updateDeps()
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
                checkValues[i] += 2;
            }
            if (statusArray[i] == 1 && checkValues[i] >= 0)
            {
                checkValues[i] = -2;
            }

        }
        // The required ones must propagate their required status to all the
        // ones
        // that they depend on
        for (Object pack1 : packs)
        {
            Pack pack = (Pack) pack1;
            if (pack.required)
            {
                String packid = pack.id;
                if (packid != null)
                {
                    if (!(!this.rules.canInstallPack(packid, this.variables) && this.rules.canInstallPackOptional(packid, this.variables)))
                    {
                        propRequirement(pack.name);
                    }
                }
                else
                {
                    propRequirement(pack.name);
                }
            }
        }

    }

    /*
     * Sees which packs (if any) should be unchecked and updates checkValues
     */
    private void updateExcludes(int rowindex)
    {
        int value = checkValues[rowindex];
        Pack pack = (Pack) packs.get(rowindex);
        if (value > 0 && pack.excludeGroup != null)
        {
            for (int q = 0; q < packs.size(); q++)
            {
                if (rowindex != q)
                {
                    Pack otherpack = (Pack) packs.get(q);
                    String name1 = otherpack.excludeGroup;
                    String name2 = pack.excludeGroup;
                    if (name2.equals(name1))
                    {
                        if (checkValues[q] == 1)
                        {
                            checkValues[q] = 0;
                        }
                    }
                }
            }
        }
    }

    private void updateBytes()
    {
        long bytes = 0;
        for (int q = 0; q < packs.size(); q++)
        {
            if (Math.abs(checkValues[q]) == 1)
            {
                Pack pack = (Pack) packs.get(q);
                bytes += pack.nbytes;
            }
        }
        
        // add selected hidden bytes
        for (Pack hidden : this.hiddenPacks){
            if (this.rules.canInstallPack(hidden.id, variables)){
                bytes += hidden.nbytes;
            }
        }
        panel.setBytes(bytes);
    }

    /**
     * We use a modified dfs graph search algorithm as described in: Thomas H. Cormen, Charles
     * Leiserson, Ronald Rivest and Clifford Stein. Introduction to algorithms 2nd Edition
     * 540-549,MIT Press, 2001
     */
    private int dfs(int[] status)
    {
        for (int i = 0; i < packs.size(); i++)
        {
            for (Object pack1 : packs)
            {
                ((Pack) pack1).color = Pack.WHITE;
            }
            Pack pack = (Pack) packs.get(i);
            boolean wipe = false;

            if (dfsVisit(pack, status, wipe) != 0)
            {
                return -1;
            }

        }
        return 0;
    }

    private int dfsVisit(Pack u, int[] status, boolean wipe)
    {
        u.color = Pack.GREY;
        int check = checkValues[getPos(u.name)];

        if (Math.abs(check) != 1)
        {
            wipe = true;
        }
        List<String> deps = u.revDependencies;
        if (deps != null)
        {
            for (String name : deps)
            {
                Pack v = namesObj.get(name);
                if (wipe)
                {
                    status[getPos(v.name)] = 1;
                }
                if (v.color == Pack.WHITE)
                {

                    final int result = dfsVisit(v, status, wipe);
                    if (result != 0)
                    {
                        return result;
                    }
                }
            }
        }
        u.color = Pack.BLACK;
        return 0;
    }


    /**
     * @return the installedpacks
     */
    public Map getInstalledpacks()
    {
        return this.installedpacks;
    }

    /**
     * @return the modifyinstallation
     */
    public boolean isModifyinstallation()
    {
        return this.modifyinstallation;
    }
}

/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Elmar Grom
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
package com.izforge.izpack.panels.shortcut;

/*---------------------------------------------------------------------------*/
/**
 * This class serves as a installDataGUI structure in
 * <code>{@link com.izforge.izpack.panels.shortcut.ShortcutPanel}</code>
 *
 * @author Elmar Grom
 * @version 0.0.1 / 4/1/02
 */
/*---------------------------------------------------------------------------*/
public class ShortcutData implements Cloneable
{

    public String name;

    public String description;

    public String target;

    public String commandLine;

    public int type;

    public int userType;

    public boolean addToGroup = false;

    public String subgroup;

    public String iconFile;

    public int iconIndex;

    public int initialState;

    public String workingDirectory;

    public String deskTopEntryLinuxMimeType;

    public String deskTopEntryLinuxTerminal;

    public String deskTopEntryLinuxTerminalOptions;

    public String deskTopEntryLinuxType;

    public String deskTopEntryLinuxURL;

    public String deskTopEntryLinuxEncoding;

    public String deskTopEntryLinuxXKDESubstituteUID;

    public String deskTopEntryLinuxXKDEUserName;

    /**
     * Linux Common Menu categories
     */
    public String categories;

    /**
     * Linux Common Menu tryExec
     */
    public String tryExec;

    public boolean createForAll;

    /**
     * Determines if the shortcut target should be run with administrator
     * privileges.
     */
    public boolean runAsAdministrator;

    /*--------------------------------------------------------------------------*/
    /**
     * Returns a clone (copy) of this object.
     *
     * @return a copy of this object
     * @throws OutOfMemoryError
     */
    /*--------------------------------------------------------------------------*/
    @Override
    public ShortcutData clone() throws OutOfMemoryError
    {
        ShortcutData result = new ShortcutData();

        result.type = type;
        result.userType = userType;
        result.iconIndex = iconIndex;
        result.initialState = initialState;
        result.addToGroup = addToGroup;

        result.name = cloneString(name);
        result.description = cloneString(description);
        result.target = cloneString(target);
        result.commandLine = cloneString(commandLine);
        result.subgroup = cloneString(subgroup);
        result.iconFile = cloneString(iconFile);
        result.workingDirectory = cloneString(workingDirectory);
        result.deskTopEntryLinuxMimeType = cloneString(deskTopEntryLinuxMimeType);
        result.deskTopEntryLinuxTerminal = cloneString(deskTopEntryLinuxTerminal);
        result.deskTopEntryLinuxTerminalOptions = cloneString(deskTopEntryLinuxTerminalOptions);
        result.deskTopEntryLinuxType = cloneString(deskTopEntryLinuxType);
        result.deskTopEntryLinuxURL = cloneString(deskTopEntryLinuxURL);
        result.deskTopEntryLinuxEncoding = cloneString(deskTopEntryLinuxEncoding);
        result.deskTopEntryLinuxXKDESubstituteUID = cloneString(deskTopEntryLinuxXKDESubstituteUID);
        result.deskTopEntryLinuxXKDEUserName = cloneString(deskTopEntryLinuxXKDEUserName);

        result.categories = cloneString(categories);
        result.tryExec = cloneString(tryExec);

        result.createForAll = createForAll;
        result.runAsAdministrator = runAsAdministrator;
        return (result);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Clones a <code>String</code>, that is it makes a copy of the content, not
     * of the reference. In addition, if the original is <code>null</code> then
     * an empty <code>String</code> is returned rather than <code>null</code>.
     *
     * @param original the <code>String</code> to clone
     * @return a clone of the original
     */
    /*--------------------------------------------------------------------------*/
    private String cloneString(String original)
    {
        if (original == null)
        {
            return ("");
        }
        else
        {
            return (original);
        }
    }
}
/*---------------------------------------------------------------------------*/

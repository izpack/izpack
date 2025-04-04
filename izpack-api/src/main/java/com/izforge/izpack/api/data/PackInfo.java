/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2004 Chadwick McHenry
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

package com.izforge.izpack.api.data;

import com.izforge.izpack.api.data.binding.OsModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Temporary holding place for Pack information as the Packager is built. The packager is used by
 * the compiler to collect info about an installer, and finally create the actual installer files.
 *
 * @author Chadwick McHenry
 */
public class PackInfo implements Serializable
{
    /**
     *
     */
    private static final long serialVersionUID = -3604642858885697783L;

    /**
     * The pack object serialized in the installer.
     */
    private final Pack pack;

    /**
     * The color of the node. This is used for the dependency graph algorithms
     */
    @SuppressWarnings("TransientFieldNotInitialized")
    public transient PackColor colour;

    /**
     * Files of the Pack.
     */
    private final Map<PackFile, File> files = new LinkedHashMap<PackFile, File>();

    /**
     * Parsables files in this Pack.
     */
    private final List<ParsableFile> parsables = new ArrayList<ParsableFile>();

    /**
     * Executable files in this Pack.
     */
    private final List<ExecutableFile> executables = new ArrayList<ExecutableFile>();

    /**
     * Update check specifications in this Pack.
     */
    private final List<UpdateCheck> updateChecks = new ArrayList<UpdateCheck>();

    /**
     * Constructor with required info.
     *
     * @param name         name of the pack
     * @param id           id of the pack e.g. to resolve I18N
     * @param description  description in English
     * @param required     pack is required or not
     * @param loose        files of pack should be stored separately or not
     * @param excludegroup name of the exclude group
     * @param uninstall    pack must be uninstalled
     * @param size         the size of the pack, in bytes
     */
    public PackInfo(String name, String id, String description, boolean required, boolean loose, String excludegroup,
                    boolean uninstall, long size)
    {
        boolean ispreselected = (excludegroup == null);
        pack = new Pack(name, id, description, null, null, required, ispreselected,
                loose, excludegroup, uninstall, size);
        colour = PackColor.WHITE;
    }

    public void setOsConstraints(List<OsModel> osConstraints)
    {
        pack.setOsConstraints(osConstraints);
    }

    public void setPreselected(boolean preselected)
    {
        pack.setPreselected(preselected);
    }

    /**
     * Set the pack group.
     *
     * @param group the group to associate the pack with.
     */
    public void setGroup(String group)
    {
        pack.setGroup(group);
    }

    /**
     * Add an install group to the pack.
     *
     * @param group the install group to associate the pack with.
     */
    public void addInstallGroup(String group)
    {
        pack.getInstallGroups().add(group);
    }

    public Pack getPack()
    {
        return pack;
    }


    public void setHidden(boolean hidden)
    {
        pack.setHidden(hidden);
    }

    /**
     * Add a file or directory to be installed.
     *
     * @param file       the file or basedir to be installed.
     * @param targetfile path file will be installed to.
     * @param osList     the target operation system(s) of this pack.
     * @param override   what to do if the file already exists when installing
     * @param condition  the condition to decide whether the file should be extracted
     * @throws FileNotFoundException if the file specified does not exist.
     */
    public void addFile(File baseDir, File file, String targetfile, List<OsModel> osList, OverrideType override,
                        String overrideRenameTo, Blockable blockable, Map additionals, String condition)
            throws IOException
    {
        if (!file.exists())
        {
            throw new FileNotFoundException(file.toString());
        }

        PackFile packFile = new PackFile(baseDir, file, targetfile, osList, override, overrideRenameTo, blockable,
                                         additionals);
        packFile.setLoosePackInfo(pack.isLoose());
        packFile.setCondition(condition);
        files.put(packFile, file);
    }

    /**
     * Set of PackFile objects for this Pack.
     */
    public Set<PackFile> getPackFiles()
    {
        return files.keySet();
    }

    /**
     * Provides the complete ordered map of PackFile -> File objects for being able to override entries.
     * @return the PackFile map
     */
    public Map<PackFile, File> getPackFilesMap()
    {
        return files;
    }

    /**
     * The file described by the specified PackFile. Returns <tt>null</tt> if the PackFile did not
     * come from the set returned by {@link #getPackFiles()}.
     */
    public File getFile(PackFile packFile)
    {
        return files.get(packFile);
    }

    /**
     * Parsable files have variables substituted after installation.
     */
    public void addParsable(ParsableFile parsable)
    {
        parsables.add(parsable);
    }

    /**
     * List of parsables for this Pack.
     */
    public List<ParsableFile> getParsables()
    {
        return parsables;
    }

    /**
     * Executables files have their executable flag set, may be executed, and optionally, deleted
     * when finished executing.
     */
    public void addExecutable(ExecutableFile executable)
    {
        executables.add(executable);
    }

    /**
     * List of parsables for this Pack.
     */
    public List<ExecutableFile> getExecutables()
    {
        return executables;
    }

    /**
     * Executables files have their executable flag set, may be executed, and optionally, deleted
     * when finished executing.
     */
    public void addUpdateCheck(UpdateCheck updateCheck)
    {
        updateChecks.add(updateCheck);
    }

    /**
     * List of update checks for this Pack.
     */
    public List<UpdateCheck> getUpdateChecks()
    {
        return updateChecks;
    }

    /**
     * The packs that this file depends on
     */
    public void addDependency(String dependency)
    {
        pack.addDependency(dependency);
    }

    public List<String> getDependencies()
    {
        return pack.getDependencies();
    }

    public void setParent(String p)
    {
        pack.setParent(p);
    }

    public String toString()
    {
        return pack.getName();
    }

    public void setPackImgId(String packImgId)
    {
        pack.setImageId(packImgId);
    }


    /**
     * @param condition the condition to set
     */
    public void setCondition(String condition)
    {
        this.pack.setCondition(condition);
    }

    public void addValidator(String validatorClassName)
    {
        pack.addValidator(validatorClassName);
    }

    /**
     * Determines what packs to be selected or deselected when this pack is selected.
     * Packs to be deselected should have their named pre-appended with a "!".
     * Packs to be selected should be specified by name.
     * Additionally a pack will only be selected or deselected if the condition is true.
     * The condition is based on weather some pack is selected or deselected.
     *
     * @param name name of a pack to be selected/deselected
     * @param condition select/deselect a pack when this pack is selected and condition is satisfied or null
     */
    public void addOnSelect(String name, String condition)
    {
        pack.setOnSelect(name, condition);
    }

    /**
     * Determines what packs to be selected or deselected when this pack is deselected.
     * Packs to be deselected should have their named pre-appended with a "!".
     * Packs to be selected should be specified by name.
     * Additionally a pack will only be selected or deselected if the condition is true.
     * The condition is based on weather some pack is selected or deselected.
     *
     * @param name name of a pack to be selected/deselected
     * @param condition select/deselect a pack when this pack is deselected and condition is satisfied or null
     */
    public void addOnDeselect(String name, String condition)
    {
        pack.setOnDeselect(name, condition);
    }
}

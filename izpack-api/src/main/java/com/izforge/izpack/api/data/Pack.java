/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.api.data;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.*;

import com.izforge.izpack.api.data.binding.OsModel;

/**
 * Represents a Pack.
 *
 * @author Julien Ponge
 */
public class Pack implements Serializable
{

    /**
     * The pack name. This uniquely identifies the pack.
     */
    private String name;

    /**
     * The language pack identifier, used for localising the package display. May be {@code null}.
     */
    private String langPackId;

    /**
     * If {@code true}, indicates that the pack files are stored outside of the installation packages.
     */
    private boolean loose;

    /**
     * If {@code true}, all files of the pack will be deleted during uninstallation. If {@code false} they are only
     * removed if uninstaller force delete option is activated.
     */
    private boolean uninstall;

    /**
     * An association of this pack to zero or more installation groups. An
     * installation group is just a named collection of packs to allow for
     * different pack collections to be selected, for example: minimal,
     * default, all.
     */
    private Set<String> installGroups = new HashSet<String>();

    /**
     * All packs in the same excludeGroup are mutually exclusive. The excludeGroup
     * is a string and serves are key identifying each group of mutually
     * exclusive packs.
     */
    private String excludeGroup = "";

    /**
     * The group the pack is associated with. The pack group identifies
     * packs with common functionality to allow for grouping of packs in a
     * tree in the TargetPanel for example.
     */
    private String group;

    /**
     * The pack description. May be {@code null}
     */
    private String description;

    /**
     * The target operating system of this pack. If {@code null}, indicates the pack applies to all OSes.
     */
    private List<OsModel> osConstraints;

    /**
     * Condition for this pack.
     */
    private String condition;

    /**
     * The names of the packs that this pack depends on. May be {@code null}
     */
    private List<String> dependencies;

    /**
     * The packs that are dependent on this pack. May be {@code null}
     */
    private List<String> dependants;

    /**
     * Mapping from packs to be selected/deselected and any conditions.
     */
    private Map<String, String> onSelectPacks;

    /**
     * Mapping from packs to be selected/deselected and any conditions.
     */
    private Map<String, String> onDeselectPacks;

    /**
     * True if the pack is required.
     */
    private boolean required;

    /**
     * The size of the pack. This may include reserved space, independent of the size of the files.
     */
    private long size;

    /**
     * The size of files in the pack, in bytes.
     */
    private long fileSize;

    /**
     * Determines if the pack should be preselected for installation.
     */
    private boolean preselected;

    /**
     * Parent pack name. May be {@code null}
     */
    private String parent;

    /**
     * Children pack names.
     */
    private List<String> children = new ArrayList<String>();

    /**
     * The pack's image resource identifier. May be {@code null}
     */
    private String imageId;

    /**
     * The validators.
     */
    private List<String> validators = new ArrayList<String>();

    /**
     * If {@code true}, denotes that the pack should not be displayed.
     */
    private boolean hidden;

    /**
     * Used for conversions.
     */
    private final static double KILOBYTES = 1024.0;

    /**
     * Used for conversions.
     */
    private final static double MEGABYTES = 1024.0 * 1024.0;

    /**
     * Used for conversions.
     */
    private final static double GIGABYTES = 1024.0 * 1024.0 * 1024.0;

    /**
     * Used for conversions.
     */
    private final static DecimalFormat formatter = new DecimalFormat("#,###.##");


    /**
     * The constructor.
     *
     * @param name          the pack name. Uniquely identifies the pack
     * @param langPackId    the id of the pack used for I18N. May be {@code null}.
     * @param description   the pack description. May be {@code null}
     * @param osConstraints the OS constraint. If {@code null} indicates the pack applies to all OSes
     * @param dependencies  dependencies of this pack. May be {@code null}
     * @param required      determines if the pack is required or not
     * @param preselected   if {@code true} the pack will be selected automatically for installation
     * @param loose         if {@code true} files of this pack are stored outside the installation jar file
     * @param excludeGroup  associated exclude group. May be {@code null}
     * @param uninstall     if {@code true}, the pack must be uninstalled
     * @param size          the pack size
     */
    public Pack(String name, String langPackId, String description, List<OsModel> osConstraints,
                List<String> dependencies, boolean required, boolean preselected, boolean loose, String excludeGroup,
                boolean uninstall, long size)
    {
        this.name = name;
        this.langPackId = langPackId;
        this.description = description;
        this.osConstraints = osConstraints;
        this.dependencies = dependencies;
        this.required = required;
        this.preselected = preselected;
        this.loose = loose;
        this.excludeGroup = excludeGroup;
        this.uninstall = uninstall;
        this.size = size;
        this.onSelectPacks = new HashMap<String, String>();
        this.onDeselectPacks = new HashMap<String, String>();
    }

    /**
     * Returns the pack name. This uniquely identifies the pack.
     *
     * @return the pack name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the language pack identifier, used for internationalisation.
     *
     * @param langPackId the language pack identifier. May be {@code null}
     */
    public void setLangPackId(String langPackId)
    {
        this.langPackId = langPackId;
    }

    /**
     * Returns the language pack identifier, used for internationalisation.
     *
     * @return the language pack identifier. May be {@code null}
     */
    public String getLangPackId()
    {
        return langPackId;
    }

    /**
     * Returns the pack description.
     *
     * @return the pack description. May be {@code null}
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Sets the target platforms for the pack.
     *
     * @param platforms the target platforms. If {@code null} or empty, indicates the pack applies to all platforms
     */
    public void setOsConstraints(List<OsModel> platforms)
    {
        osConstraints = platforms;
    }

    /**
     * Returns the target platforms for the pack.
     *
     * @return the target platforms. If {@code null} or empty, indicates the pack applies to all platforms
     */
    public List<OsModel> getOsConstraints()
    {
        return osConstraints;
    }

    /**
     * Sets the names of packs that this pack is dependent on.
     *
     * @param dependencies a list of pack names. May be {@code null}
     */
    public void setDependencies(List<String> dependencies)
    {
        this.dependencies = dependencies;
    }

    /**
     * Returns the pack's dependencies.
     *
     * @return a list of pack names that the pack is dependent on. May be {@code null}
     */
    public List<String> getDependencies()
    {
        return dependencies;
    }

    public boolean hasDependencies()
    {
        return dependencies != null;
    }

    /**
     * Adds a dependency on another pack.
     *
     * @param name the name of the pack this pack is dependent on
     */
    public void addDependency(String name)
    {
        if (dependencies == null)
        {
            dependencies = new ArrayList<String>();
        }
        dependencies.add(name);
    }

    /**
     * Sets the names of the packs that require this pack.
     *
     * @param dependants the dependants. May be {@code null}
     */
    public void setDependants(List<String> dependants)
    {
        this.dependants = dependants;
    }

    /**
     * Returns the names of the pack that are dependent on this pack.
     *
     * @return the dependants. May be {@code null}
     */
    public List<String> getDependants()
    {
        return dependants;
    }

    /**
     * Adds the name of a pack that is dependent on this pack.
     *
     * @param name the dependant pack name
     */
    public void addDependant(String name)
    {
        if (dependants == null)
        {
            dependants = new ArrayList<String>();
        }
        dependants.add(name);
    }


    /**
     * Determined if the pack is required.
     *
     * @return {@code true} if the pack is required; {@code false} if it is optional
     */
    public boolean isRequired()
    {
        return required;
    }

    /**
     * Determines if the pack should be preselected for installation.
     *
     * @param preselected if {@code true}, the pack should be preselected
     */
    public void setPreselected(boolean preselected)
    {
        this.preselected = preselected;
    }

    /**
     * Determines if the pack should be preselected for installation.
     *
     * @return {@code true} if the pack should be preselected
     */
    public boolean isPreselected()
    {
        return preselected;
    }

    /**
     * Determines if pack files are stored outside of the installation packages.
     *
     * @param loose if {@code true}, pack files are stored outside of the installation packages
     */
    public void setLoose(boolean loose)
    {
        this.loose = loose;
    }

    /**
     * Determines if pack files are stored outside of the installation packages.
     *
     * @return {@code true} if pack files are stored outside of the installation packages
     */
    public boolean isLoose()
    {
        return loose;
    }

    /**
     * Sets the exclude group for the pack. All packs in the same exclude group are mutually exclusive.
     *
     * @param group the exclude group. May be {@code null}
     */
    public void setExcludeGroup(String group)
    {
        excludeGroup = group;
    }

    /**
     * Returns the exclude group for the pack.
     *
     * @return the exclude group. May be {@code null}
     */
    public String getExcludeGroup()
    {
        return excludeGroup;
    }

    /**
     * Determines if the pack files are deleted at uninstallation.
     *
     * @return {@code true} if all files of the pack will be deleted during uninstallation;
     *         {@code false} if they should be retained (subject to the uninstaller force delete option)
     */
    public boolean isUninstall()
    {
        return uninstall;
    }

    /**
     * Returns the installation groups that this pack belongs to.
     * <p/>
     * An installation group is a logical collection of packs. It enables different pack collections to be selected
     * e.g. minimal, default, or all.
     *
     * @return the installation groups
     */
    public Set<String> getInstallGroups()
    {
        return installGroups;
    }

    /**
     * Sets the pack group. This enables grouping of packs with related functionality.
     *
     * @param group the group. May be {@code null}
     */
    public void setGroup(String group)
    {
        this.group = group;
    }

    /**
     * Returns the pack group.
     *
     * @return the pack group. May be {@code null}
     */
    public String getGroup()
    {
        return group;
    }

    /**
     * Sets the size of the pack.
     *
     * @param size the size of the pack, in bytes
     */
    public void setSize(long size)
    {
        this.size = size;
    }

    /**
     * Returns the size of the pack.
     *
     * @return the size of the pack, in bytes
     */
    public long getSize()
    {
        return size;
    }

    /**
     * Sets the size of the pack.
     *
     * @param size the size of the pack, in bytes
     */
    public void setFileSize(long size)
    {
        fileSize = size;
    }

    /**
     * Adds to the size of the pack.
     *
     * @param add the no. of bytes to add
     */
    public void addFileSize(long add)
    {
        fileSize += add;
    }

    /**
     * Returns the size of the pack.
     *
     * @return the size of the pack, in bytes
     */
    public long getFileSize()
    {
        return fileSize;
    }

    /**
     * Sets the parent pack name.
     *
     * @param parent the parent pack name. May be {@code null}
     */
    public void setParent(String parent)
    {
        this.parent = parent;
    }

    /**
     * Returns the parent pack name.
     *
     * @return the parent pack name. May be {@code null}
     */
    public String getParent()
    {
        return parent;
    }

    /**
     *
     * @return {@code true} if the pack has a parent otherwise {@code false}
     */
    public boolean hasParent()
    {
        return parent != null;
    }

    /**
     * Add a child to this pack.
     */
    public void addChild(String child)
    {
        this.children.add(child);
    }

    /**
     * Returns the children pack names.
     *
     * @return the names of the children for this pack.
     */
    public List<String> getChildren()
    {
        return children;
    }

    /**
     *
     * @return {@code true} if the pack has a parent otherwise {@code false}
     */
    public boolean hasChildren()
    {
        return !children.isEmpty();
    }

    /**
     * Sets the pack image resource identifier.
     *
     * @param imageId the image resource identifier. May be {@code null}
     */
    public void setImageId(String imageId)
    {
        this.imageId = imageId;

    }

    /**
     * Returns the pack image resource identifier.
     *
     * @return the resource identifier. If {@code null}, then the pack has no image
     */
    public String getImageId()
    {
        return imageId;
    }

    /**
     * Sets a condition that must be fulfilled for the pack to be installed.
     *
     * @param condition the condition to set. If {@code null}, indicates that installation is unconditional
     */
    public void setCondition(String condition)
    {
        this.condition = condition;
    }

    /**
     * Returns the condition that must be fulfilled for the pack to be installed.
     *
     * @return the condition. If {@code null}, indicates that installation is unconditional
     */
    public String getCondition()
    {
        return this.condition;
    }

    /**
     * Determines if the pack has a condition for installation.
     *
     * @return {@code true} if the pack has a condition, {@code false} if installation is unconditional
     */
    public boolean hasCondition()
    {
        return condition != null;
    }

    /**
     * Adds a pack validator.
     *
     * @param validatorClassName the pack validator class name
     */
    public void addValidator(String validatorClassName)
    {
        validators.add(validatorClassName);
    }

    /**
     * Returns the pack validators.
     *
     * @return the pack validator class names
     */
    public List<String> getValidators()
    {
        return validators;
    }

    /**
     * Determines if the pack should be hidden.
     *
     * @param hidden {@code true} if the pack should be hidden, {@code false} if it should be displayed
     */
    public void setHidden(boolean hidden)
    {
        this.hidden = hidden;
    }

    /**
     * Determines if the pack should be hidden.
     *
     * @return {@code true} if the pack should be hidden, {@code false} if it should be displayed
     */
    public boolean isHidden()
    {
        return hidden;
    }

    /**
     * To a String (usefull for JLists).
     *
     * @return the string representation of the pack
     */
    public String toString()
    {
        return name + " (" + description + ")";
    }

    /**
     * Convert bytes into appropriate measurements.
     *
     * @param bytes the bytes
     * @return the string representation
     */
    public static String toByteUnitsString(long bytes)
    {
        if (bytes < KILOBYTES)
        {
            return String.valueOf(bytes) + " bytes";
        }
        else if (bytes < (MEGABYTES))
        {
            double value = bytes / KILOBYTES;
            return formatter.format(value) + " KB";
        }
        else if (bytes < (GIGABYTES))
        {
            double value = bytes / MEGABYTES;
            return formatter.format(value) + " MB";
        }
        else
        {
            double value = bytes / GIGABYTES;
            return formatter.format(value) + " GB";
        }
    }

    /**
     * Determines what packs to be selected or deselected when this pack is selected.
     * Packs to be deselected should have their named pre-appended with a "!".
     * Packs to be selected should be specified by name.
     * Additionally a pack will only be selected or deselected if the condition is true.
     * The condition is based on weather some pack is selected or deselected.
     *
     * @param names names of a pack to be selected/deselected separated by a comma
     * @param condition select/deselect a pack when this pack is selected and condition is satisfied or null
     */
    public void setOnSelect(String names, String condition)
    {
        for (String name : names.split(","))
        {
            this.onSelectPacks.put(name, condition);
        }
    }

    public Map<String, String> getOnSelect()
    {
        return this.onSelectPacks;
    }
    /**
     * Determines what packs to be selected or deselected when this pack is deselected.
     * Packs to be deselected should have their named pre-appended with a "!".
     * Packs to be selected should be specified by name.
     * Additionally a pack will only be selected or deselected if the condition is true.
     * The condition is based on weather some pack is selected or deselected.
     *
     * @param names name of a pack to be selected/deselected separated by a comma
     * @param condition select/deselect a pack when this pack is deselected and condition is satisfied or null
     */
    public void setOnDeselect(String names, String condition)
    {
        for (String name : names.split(","))
        {
            this.onDeselectPacks.put(name, condition);
        }
    }
    public Map<String, String> getOnDeselect()
    {
        return this.onDeselectPacks;
    }
}

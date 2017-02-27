/*
 * Copyright 2016 Julien Ponge, René Krell and the IzPack team.
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

import com.izforge.izpack.api.data.binding.Help;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.handler.DefaultConfigurationHandler;

import java.io.Serializable;
import java.util.*;

/**
 * @author Jan Blok
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class Panel extends DefaultConfigurationHandler implements Serializable
{

    static final long serialVersionUID = 8886445274940938809L;

    /**
     * The panel classname.
     */
    private String className;

    /**
     * The target operation system of this panel
     */
    private List<OsModel> osConstraints = Collections.emptyList();

    /**
     * the unique id of this panel
     */
    private String panelId;

    /**
     * condition for this panel
     */
    private String condition;

    /**
     * Whether the panel including all fields is read-only if otherwise hidden.
     */
    private boolean displayHidden = false;

    /**
     * Condition whether the panel should be shown read-only in otherwise
     * hidden.
     */
    private String displayHiddenCondition;

    /**
     * Whether the panel including all fields is read-only as a whole.
     */
    private boolean readonly = false;

    /**
     * Condition whether the panel should be shown read-only.
     */
    private String readonlyCondition;

    /**
     * The list of validators for this panel
     */
    private final List<String> validators = new ArrayList<String>();

    /**
     * Set affected variable names that might be changed on this panel
     */
    private Set<String> affectedVariableNames;

    /**
     * The map of validator conditions for this panel depending on the validator
     * Condition whether the validator has to be asked for validation.
     */
    private final Map<Integer, String> validatorConditionIds = new HashMap<Integer, String>();

    /**
     * The map of validator configuration options for this panel depending on
     * the validator configuration section.
     */
    private final Map<Integer, Configurable> validatorConfiguration = new HashMap<Integer, Configurable>();

    /**
     * Whether the panel has been visited for summarizing the installation story
     */
    private transient boolean visited = false;

    /**
     * list of all pre panel construction actions
     */
    private List<PanelActionConfiguration> preConstructionActions = null;

    /**
     * list of all pre panel activation actions
     */
    private List<PanelActionConfiguration> preActivationActions = null;

    /**
     * list of all pre panel validation actions
     */
    private List<PanelActionConfiguration> preValidationActions = null;

    /**
     * list of all post panel validation actions
     */
    private List<PanelActionConfiguration> postValidationActions = null;

    /**
     * A list of URLs to help files, key should be iso3-code
     */
    private List<Help> helps = null;

    /**
     * Whether we confirm quit from this panel.
     *
     * This is set from the "allowClose" attribute in the panel specification: -
     * not specified = DYNAMIC - allowClose="true" = SILENT - allowClose="false"
     * = CONFIRM
     *
     * See CompilerConfig.addPanels for special handling to simplify usage while
     * maintaining backward compatibility.
     */
    public enum ConfirmQuitType
    {
        DYNAMIC, //confirm quit until files are copied; "classic" behavior
        CONFIRM, //always confirm quit
        SILENT
    }   //never confirm quit
    private ConfirmQuitType confirmQuitType = ConfirmQuitType.DYNAMIC;

    public String getClassName()
    {
        return this.className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public boolean hasPanelId()
    {
        return (panelId != null);
    }

    public String getPanelId()
    {
        return panelId;
    }

    public void setPanelId(String panelId)
    {
        this.panelId = panelId;
    }

    /**
     * @return the condition
     */
    public String getCondition()
    {
        return this.condition;
    }

    /**
     * @param condition the condition to set
     */
    public void setCondition(String condition)
    {
        this.condition = condition;
    }

    public boolean hasCondition()
    {
        return this.condition != null;
    }

    /**
     * Get the 'displayHiddenCondition' of this panel.
     *
     * @return the condition to set when the panel should be shown read-only in
     * otherwise hidden state
     */
    public String getDisplayHiddenCondition()
    {
        return this.displayHiddenCondition;
    }

    /**
     * Set the 'displayHiddenCondition' of this panel.
     *
     * @param condition the condition to set when the panel should be shown
     * read-only in otherwise hidden state
     */
    public void setDisplayHiddenCondition(String condition)
    {
        this.displayHiddenCondition = condition;
    }

    /**
     * Whether the 'displayHiddenCondition' is set for this panel.
     *
     * @return the condition to set when the panel should be shown read-only in
     * otherwise hidden state
     */
    public boolean hasDisplayHiddenCondition()
    {
        return this.displayHiddenCondition != null;
    }

    public boolean isDisplayHidden()
    {
        return displayHidden;
    }

    public void setDisplayHidden(boolean flag)
    {
        this.displayHidden = flag;
    }

    public boolean isReadonly()
    {
        return readonly;
    }

    public void setReadonly(boolean flag)
    {
        this.readonly = flag;
    }

    /**
     * Get the 'readonlyCondition' of this panel.
     *
     * @return the condition to set when the panel should be shown read-only
     */
    public String getReadonlyCondition()
    {
        return this.readonlyCondition;
    }

    /**
     * Set the 'readonlyCondition' of this panel.
     *
     * @param condition the condition to set when the panel should be shown
     * read-only
     */
    public void setReadonlyCondition(String condition)
    {
        this.readonlyCondition = condition;
    }

    /**
     * Whether the 'readonlyCondition' is set for this panel.
     *
     * @return the condition to set when the panel should be shown read-only
     */
    public boolean hasReadonlyCondition()
    {
        return this.readonlyCondition != null;
    }

    /**
     * Get validator and validator condition entries for this panel
     *
     * @return Returns a list of validator class names and optional conditions
     * defining whether the panel validator should be asked at all.
     */
    public List<String> getValidators()
    {
        return validators;
    }

    /**
     * Gets a validator condition
     *
     * @param index the order number of the validator within the panel
     * definition
     * @return the validator condition of a validator at the given index for
     * this panel
     */
    public String getValidatorCondition(int index)
    {
        return this.validatorConditionIds.get(index);
    }

    /**
     * Gets a validator condition
     *
     * @param index the order number of the validator within the panel
     * definition
     * @return the configuration options of a validator at the given index for
     * this panel
     */
    public Configurable getValidatorConfiguration(int index)
    {
        return this.validatorConfiguration.get(index);
    }

    /**
     * Adds a panel validator and a condition defining whether the panel
     * validator should be asked at all.
     *
     * @param validatorClassName the class name the validator
     * @param validatorConditionId the validator condition for this panel (set
     * null for no condition)
     * @param configurable configuration options assigned to the validator
     * (nested 'configuration' tag)
     */
    public void addValidator(String validatorClassName, String validatorConditionId, Configurable configurable)
    {
        this.validators.add(validatorClassName);
        if (validatorConditionId != null)
        {
            // There must be used the index in the ordered list of validators as key, because the validator
            // has no own ID and its classname might not be unique.
            this.validatorConditionIds.put(validators.size() - 1, validatorConditionId);
        }
        if (configurable != null)
        {
            // There must be used the index in the ordered list of validators as key, because the validator
            // has no own ID and its classname might not be unique.
            this.validatorConfiguration.put(validators.size() - 1, configurable);
        }
    }

    public void setHelps(List<Help> helps)
    {
        this.helps = helps;
    }

    public List<PanelActionConfiguration> getPreConstructionActions()
    {
        return preConstructionActions;
    }

    public void addPreConstructionAction(PanelActionConfiguration action)
    {
        if (this.preConstructionActions == null)
        {
            this.preConstructionActions = new ArrayList<PanelActionConfiguration>();
        }
        this.preConstructionActions.add(action);
    }

    public List<PanelActionConfiguration> getPreActivationActions()
    {
        return preActivationActions;
    }

    public void addPreActivationAction(PanelActionConfiguration action)
    {
        if (this.preActivationActions == null)
        {
            this.preActivationActions = new ArrayList<PanelActionConfiguration>();
        }
        this.preActivationActions.add(action);
    }

    public List<PanelActionConfiguration> getPreValidationActions()
    {
        return preValidationActions;
    }

    public void addPreValidationAction(PanelActionConfiguration action)
    {
        if (this.preValidationActions == null)
        {
            this.preValidationActions = new ArrayList<PanelActionConfiguration>();
        }
        this.preValidationActions.add(action);
    }

    public List<PanelActionConfiguration> getPostValidationActions()
    {
        return postValidationActions;
    }

    public void addPostValidationAction(PanelActionConfiguration action)
    {
        if (this.postValidationActions == null)
        {
            this.postValidationActions = new ArrayList<PanelActionConfiguration>();
        }
        this.postValidationActions.add(action);
    }

    public List<OsModel> getOsConstraints()
    {
        return osConstraints;
    }

    public void setOsConstraints(List<OsModel> osConstraints)
    {
        this.osConstraints = osConstraints;
    }

    public String getHelpUrl(String localeISO3)
    {
        if (helps == null)
        {
            return null;
        }
        for (Help help : helps)
        {
            if (help.getIso3().equals(localeISO3))
            {
                return help.getSrc();
            }
        }
        return null;
    }

    /**
     * @return Whether the panel has been visited for summarizing the
     * installation story
     */
    public boolean isVisited()
    {
        return visited;
    }

    /**
     * Mark panel visited for summarizing the installation story
     *
     * @param visited whether the panel has been shown to the user
     */
    public void setVisited(boolean visited)
    {
        this.visited = visited;
    }

    /**
     * Gets a unique collection of names of variables that might me affected
     * when this panel is active.
     *
     * @return Set of variable names
     */
    public Set<String> getAffectedVariableNames()
    {
        return affectedVariableNames;
    }

    /**
     * Adds or completes a collection of names of variables that might me
     * affected when this panel is active.<br><br>
     * Affected variables are blocked for further dynamic changes if there are
     * definitions as dynamic variables as soon as the user passes forward over
     * this panel
     *
     * @param names Affected variables
     */
    public void setAffectedVariableNames(Set<String> names)
    {
        affectedVariableNames = names;
    }

    /**
     * Gets the behavior when quit is pressed on this panel.
     *
     * @return the confirmation options of this panel
     */
    public ConfirmQuitType getConfirmQuitType()
    {
        return confirmQuitType;
    }

    /**
     * Sets the behavior when quit is pressed on this panel.
     *
     * @param value the confirmation options of this panel
     */
    public void setConfirmQuitType(ConfirmQuitType value)
    {
        confirmQuitType = value;
    }

    @Override
    public String toString()
    {
        return "Panel{"
                + "className='" + className + '\''
                + ", osConstraints=" + osConstraints
                + ", panelid='" + getPanelId() + '\''
                + ", condition='" + condition + '\''
                + ", validator count='" + validators.size() + '\''
                + ", helps=" + helps + '\''
                + ", affected variables count='" + affectedVariableNames.size() + '\''
                + '}';
    }
}

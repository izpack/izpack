/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
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

package com.izforge.izpack.installer.panel;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.DynamicInstallerRequirementValidator;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.data.PanelAction;
import com.izforge.izpack.data.PanelAction.ActionStage;


/**
 * Encapsulates a {@link Panel} and its user-interface representation.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPanelView<T> implements PanelView<T>
{

    /**
     * The panel.
     */
    private final Panel panel;

    /**
     * The panel user interface class.
     */
    private final Class<T> viewClass;

    /**
     * The factory for creating the view.
     */
    private final ObjectFactory factory;

    /**
     * The panel index.
     */
    private int index;

    /**
     * The panel user interface.
     */
    private T view;

    /**
     * Determines if the user interface is visible.
     */
    private boolean visible = true;

    /**
     * Ordered list of panel data validators
     */
    private List<DataValidator> validators = new ArrayList<DataValidator>();

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * Actions to invoke prior to the panel being displayed.
     */
    private final List<PanelAction> preActivationActions = new ArrayList<PanelAction>();

    /**
     * Actions to invoke prior to the panel being validated.
     */
    private final List<PanelAction> preValidationActions = new ArrayList<PanelAction>();

    /**
     * Actions to invoke after the panel being validated.
     */
    private final List<PanelAction> postValidationActions = new ArrayList<PanelAction>();

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(AbstractPanelView.class.getName());


    /**
     * Constructs a {@code PanelView}.
     *
     * @param panel       the panel
     * @param viewClass   the panel user interface class
     * @param factory     the factory for creating the view
     * @param installData the installation data
     */
    public AbstractPanelView(Panel panel, Class<T> viewClass, ObjectFactory factory, InstallData installData)
    {
        this.panel = panel;
        this.viewClass = viewClass;
        this.factory = factory;
        this.installData = installData;
    }

    /**
     * Returns the panel identifier.
     *
     * @return the panel identifier
     */
    @Override
    public String getPanelId()
    {
        return panel.getPanelId();
    }

    /**
     * Returns the panel.
     *
     * @return the panel
     */
    @Override
    public Panel getPanel()
    {
        return panel;
    }

    /**
     * Returns the panel index.
     * <br/>
     * This is the offset of the panel relative to the other panels, visible or not.
     *
     * @return the panel index
     */
    @Override
    public int getIndex()
    {
        return index;
    }

    /**
     * Sets the panel index.
     *
     * @param index the index
     */
    @Override
    public void setIndex(int index)
    {
        this.index = index;
    }

    /**
     * Returns the panel user interface.
     * <br/>
     * The view will be created if it doesn't exist.
     * <br/>
     * If the panel has a {@link DataValidator} specified, this will be constructed, with both the panel and view
     * supplied for injection into it's constructor.
     *
     * @return the panel user interface
     */
    @Override
    public T getView()
    {
        if (view == null)
        {
            executePreConstructionActions();
            view = createView(panel, viewClass);

            List<String> dataValidatorClassNames = panel.getValidators();
            for (String dataValidatorClassName : dataValidatorClassNames)
            {
                validators.add(factory.create(dataValidatorClassName, DataValidator.class, panel, view));
            }

            addActions(panel.getPreActivationActions(), preActivationActions, ActionStage.preactivate);
            addActions(panel.getPreValidationActions(), preValidationActions, ActionStage.prevalidate);
            addActions(panel.getPostValidationActions(), postValidationActions, ActionStage.postvalidate);

            initialise(view, panel, installData);
        }
        return view;
    }

    /**
     * Sets the visibility of the panel.
     *
     * @param visible if {@code true} the panel is visible, otherwise it is hidden
     */
    @Override
    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    /**
     * Determines the visibility of the panel.
     *
     * @return {@code true} if the panel is visible, {@code false} if it is hidden
     */
    @Override
    public boolean isVisible()
    {
        return visible;
    }

    /**
     * Determines if the panel is valid.
     *
     * @return {@code true} if the panel is valid
     */
    @Override
    public boolean isValid()
    {
        boolean result = false;
        installData.getVariables().refresh();
        executePreValidationActions();

        List<DynamicInstallerRequirementValidator> conditions = installData.getDynamicInstallerRequirements();
        if (conditions == null || validateDynamicConditions())
        {
            result = validators.size() == 0 || validateData();
        }

        executePostValidationActions();
        return result;
    }

    /**
     * Save the contents of the panel into install data.
     */
    @Override
    public void saveData()
    {
        //Panel specific should be overwritten my the panel
    }
    /**
     * Determines if the panel can be shown.
     *
     * @return {@code true} if the panel can be shown
     */
    @Override
    public boolean canShow()
    {
        boolean result;
        String panelId = panel.getPanelId();
        installData.refreshVariables();
        if (panel.hasCondition())
        {
            result = installData.getRules().isConditionTrue(panel.getCondition());
            logger.fine("Panel '" + getPanelId() + "' depending on condition '" + panel.getCondition() + "' " + (result?"can be shown":"will be skipped"));
        }
        else
        {
            result = installData.getRules().canShowPanel(panelId, installData.getVariables());
            logger.fine("Panel '" + getPanelId() + "' " + (result?"can be shown":"will be skipped"));
        }
        return result;
    }

    /**
     * Executes actions prior to activating the panel.
     */
    public void executePreActivationActions()
    {
        execute(preActivationActions);
    }

    /**
     * Executes actions prior to validating the panel.
     */
    public void executePreValidationActions()
    {
        execute(preValidationActions);
    }

    /**
     * Executes actions after validating the panel.
     */
    public void executePostValidationActions()
    {
        execute(postValidationActions);
    }

    /**
     * Returns a handler to prompt the user.
     *
     * @return the handler
     */
    protected abstract AbstractUIHandler getHandler();

    /**
     * Creates a new view.
     *
     * @param panel     the panel to create the view for
     * @param viewClass the view base class
     * @return the new view
     */
    protected T createView(Panel panel, Class<T> viewClass)
    {
        return factory.create(panel.getClassName(), viewClass, panel, this);
    }

    /**
     * Initialises the view.
     * <br/>
     * This implementation is a no-op.
     *
     * @param view        the view to initialise
     * @param panel       the panel the view represents
     * @param installData the installation data
     */
    protected void initialise(T view, Panel panel, InstallData installData)
    {

    }

    /**
     * Validates dynamic conditions.
     *
     * @return {@code true} if there are no conditions, or conditions validate successfully
     */
    protected boolean validateDynamicConditions()
    {
        boolean result = true;
        try
        {
            installData.refreshVariables();
            for (DynamicInstallerRequirementValidator validator : installData.getDynamicInstallerRequirements())
            {
                if (!isValid(validator, installData))
                {
                    result = false;
                    break;
                }
            }
        }
        catch (Throwable exception)
        {
            logger.log(Level.WARNING, "Panel " + getPanelId() + ": Could not validate dynamic conditions", exception);
            result = false;
        }
        return result;
    }

    /**
     * Evaluates the panel data validator.
     *
     * @return {@code true} if the validator evaluated successfully, or with a warning that the user chose to skip;
     *         otherwise {@code false}
     */
    protected boolean validateData()
    {
        boolean result = true;
        DataValidator[] validatorArray = validators.toArray(new DataValidator[] {});
        for (int i = 0; i < validatorArray.length; i++)
        {
            DataValidator validator = validatorArray[i];
            if (!isValid(validator, i, installData))
            {
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * Evaluates a validator.
     * <p/>
     * If the validator returns a warning status, then a prompt will be displayed asking the user to skip the
     * validation or not.
     *
     * @param validator   the validator to evaluate
     * @param index   the index in the list of validators for finding its appropriate validator condition
     * @param installData the installation data
     * @return {@code true} if the validator evaluated successfully, or with a warning that the user chose to skip;
     *         otherwise {@code false}
     */
    private boolean isValid(DataValidator validator, Integer index, InstallData installData)
    {
        String dataValidatorConditionId = panel.getValidatorCondition(index);
        if (dataValidatorConditionId != null)
        {
            Condition dataValidatorCondition = installData.getRules().getCondition(dataValidatorConditionId);
            if (!dataValidatorCondition.isTrue())
            {
                // Skip panel validation
                logger.fine("Panel " + getPanelId() + ": Skip validation (" + validator.getClass().getName() +")");
                return true;
            }
        }
        return isValid(validator, installData);
    }

    /**
     * Evaluates a validator.
     * <p/>
     * If the validator returns a warning status, then a prompt will be displayed asking the user to skip the
     * validation or not.
     *
     * @param validator   the validator to evaluate
     * @param index   the index in the list of validators for finding its appropriate validator condition
     * @param installData the installation data
     * @return {@code true} if the validator evaluated successfully, or with a warning that the user chose to skip;
     *         otherwise {@code false}
     */
    private boolean isValid(DataValidator validator, InstallData installData)
    {
        boolean result = false;

        DataValidator.Status status = validator.validateData(installData);
        logger.fine("Panel " + getPanelId() + ": Data validation status=" + status + " (" + validator.getClass().getName() + ")");

        if (status == DataValidator.Status.OK)
        {
            result = true;
        }
        else
        {
            if (status == DataValidator.Status.WARNING)
            {
                String message = getMessage(validator.getWarningMessageId(), true);
                if (message == null)
                {
                    logger.warning("Panel " + getPanelId() + ": No warning message for validator " + validator.getClass().getName());
                }
                result = isWarningValid(message, validator.getDefaultAnswer());
            }
            else
            {
                String message = getMessage(validator.getErrorMessageId(), true);
                if (message == null)
                {
                    logger.warning("Panel " + getPanelId() + ": No error message for validator " + validator.getClass().getName());
                    message = "Validation error";
                }
                getHandler().emitError(getMessage("data.validation.error.title"), message);
            }
        }
        return result;
    }

    /**
     * Determines the behaviour when a warning is encountered during validation.
     *
     * @param message       the validation message. May be {@code null}
     * @param defaultAnswer the default response for warnings
     * @return {@code true} if the warning doesn't invalidate the panel; {@code false} if it does
     */
    protected boolean isWarningValid(String message, boolean defaultAnswer)
    {
        boolean result = false;
        if (message != null)
        {
            if (getHandler().emitWarning(getMessage("data.validation.warning.title"), message))
            {
                result = true;
                logger.fine("Panel " + getPanelId() + ": User decided to skip validation warning");
            }
        }
        else
        {
            logger.fine("Panel " + getPanelId() + ": No warning message available, using default answer=" + defaultAnswer);
            result = defaultAnswer;
        }
        return result;
    }

    /**
     * Returns the factory.
     *
     * @return the factory
     */
    protected ObjectFactory getFactory()
    {
        return factory;
    }

    /**
     * Helper to return a localised message, given its id.
     *
     * @param id the message identifier
     * @return the corresponding message or {@code null} if none is found
     */
    protected String getMessage(String id)
    {
        return getMessage(id, false);
    }

    /**
     * Executes actions.
     *
     * @param actions the actions to execute
     */
    private void execute(List<PanelAction> actions)
    {
        AbstractUIHandler handler = getHandler();
        for (PanelAction action : actions)
        {
            action.executeAction(installData, handler);
        }
    }

    /**
     * Executes actions prior to creating the panel.
     * <br/>
     * Both the panel and view are supplied for injection into the action's constructor.
     */
    private void executePreConstructionActions()
    {
        List<PanelActionConfiguration> configurations = panel.getPreConstructionActions();
        if (configurations != null)
        {
            for (PanelActionConfiguration config : configurations)
            {
                PanelAction action = factory.create(config.getActionClassName(), PanelAction.class, panel,
                                                    ActionStage.preconstruct);
                action.initialize(config);
                action.executeAction(installData, null);
            }
        }
    }

    /**
     * Creates {@link PanelAction}s, adding them to the supplied list.
     * <br/>
     * Both the panel and view are supplied for injection into the action's constructor.
     *
     * @param configurations the action class names. May be {@code null}
     * @param actions        the actions to add to
     * @param stage          the action stage
     */
    private void addActions(List<PanelActionConfiguration> configurations, List<PanelAction> actions,
                            ActionStage stage)
    {
        if (configurations != null)
        {
            for (PanelActionConfiguration config : configurations)
            {
                PanelAction action = factory.create(config.getActionClassName(), PanelAction.class, panel, view,
                                                    stage);
                action.initialize(config);
                actions.add(action);
            }
        }
    }

    /**
     * Helper to return a localised message, given its id.
     *
     * @param id      the message identifier
     * @param replace if {@code true}, replace any variables in the message with their corresponding values
     * @return the corresponding message or {@code null} if none is found
     */
    private String getMessage(String id, boolean replace)
    {
        String message = null;
        if (id != null)
        {
            Messages messages = installData.getMessages();
            message = messages.get(id);
            if (replace)
            {
                message = installData.getVariables().replace(message);
            }
        }
        return message;
    }

    /**
     * Creates an empty root element prepared for adding auto-installation records for this panel.
     *
     * @return IXMLElement The prepared panel record XML
     */
    protected final IXMLElement createPanelRootRecord()
    {
        IXMLElement panelRoot = new XMLElementImpl(panel.getClassName(), installData.getInstallationRecord());
        String panelId = panel.getPanelId();
        if (panelId != null)
        {
            panelRoot.setAttribute(AutomatedInstallData.AUTOINSTALL_PANELROOT_ATTR_ID, panelId);
        }
        return panelRoot;
    }

}
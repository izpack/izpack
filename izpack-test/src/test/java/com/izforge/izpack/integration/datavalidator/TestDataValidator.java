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

package com.izforge.izpack.integration.datavalidator;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.installer.DataValidator;


/**
 * Test {@link DataValidator} implementation.
 * <p/>
 * This enables the result of the validation for a panel to be determined by specifying a variable named
 * <em>&lt;panelId&gt;.status</em>. E.g.:
 * <pre>
 * installData.setVariable("HelloPanel.status", "ERROR");
 * </pre>
 * A counter <em>&lt;panelId&gt;.validate</em> is incremented each time the validator is called.
 *
 * @author Tim Anderson
 */
public class TestDataValidator implements DataValidator
{

    /**
     * The panel.
     */
    private final Panel panel;

    /**
     * The installation data.
     */
    private final InstallData installData;


    /**
     * Constructs a <tt>TestDataValidator</tt>.
     *
     * @param panel       the panel
     * @param installData the installation data
     */
    public TestDataValidator(Panel panel, InstallData installData)
    {
        this.installData = installData;
        this.panel = panel;
    }

    /**
     * Returns the no. of times validation has been invoked for the specified panel.
     *
     * @param panelId     the panel identifier
     * @param installData the installation data
     * @return the no. of times validation has been invoked
     */
    public static int getValidate(String panelId, InstallData installData)
    {
        return getValue(panelId + ".validate", installData);
    }

    /**
     * Method to validate {@link InstallData}.
     *
     * @param installData the installation data
     * @return the result of the validation
     */
    @Override
    public Status validateData(InstallData installData)
    {
        String id = getPanelId();
        String status = installData.getVariable(id + ".status");
        increment(id + ".validate");
        return (status != null) ? Status.valueOf(status) : Status.ERROR;
    }

    /**
     * Returns the messageId for an error.
     *
     * @return the messageId
     */
    @Override
    public String getErrorMessageId()
    {
        return getPanelId() + ".error";
    }

    /**
     * Returns the messageId for a warning.
     *
     * @return the messageId
     */
    @Override
    public String getWarningMessageId()
    {
        return getPanelId() + ".warning";
    }

    /**
     * Determines how the installer responds to a warning when running automated.
     *
     * @return boolean
     */
    @Override
    public boolean getDefaultAnswer()
    {
        return false;
    }

    /**
     * Returns the installation data.
     *
     * @return the installation data
     */
    protected InstallData getInstallData()
    {
        return installData;
    }

    /**
     * Returns the panel id of the current panel.
     *
     * @return the panel id
     */
    protected String getPanelId()
    {
        return panel.getPanelId();
    }

    /**
     * Returns the no. of times validation has been invoked for the current panel.
     *
     * @return the no. of invocations
     */
    protected int getValidate()
    {
        return getValue(getPanelId() + ".validate");
    }

    /**
     * Returns the value of a numeric variable.
     *
     * @param variable the variable name
     * @return the variable value
     */
    protected int getValue(String variable)
    {
        return getValue(variable, installData);
    }

    /**
     * Returns the value of a numeric variable.
     *
     * @param variable    the variable name
     * @param installData the installation data
     * @return the variable value
     */
    protected static int getValue(String variable, InstallData installData)
    {
        String value = installData.getVariable(variable);
        if (value == null)
        {
            value = "0";
        }
        return Integer.valueOf(value);
    }

    /**
     * Increments a variable.
     *
     * @param variable the variable to increment
     * @return the new value
     */
    protected int increment(String variable)
    {
        int value = getValue(variable);
        value++;
        installData.setVariable(variable, Integer.toString(value));
        return value;
    }

}

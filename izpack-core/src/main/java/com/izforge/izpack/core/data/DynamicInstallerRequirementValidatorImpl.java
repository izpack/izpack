/*
 * $Id: Compiler.java 1918 2007-11-29 14:02:17Z dreil $
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2010 René Krell
 *
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
package com.izforge.izpack.core.data;

import java.io.Serializable;

import com.izforge.izpack.api.data.DynamicInstallerRequirementValidator;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.rules.RulesEngine;

/**
 * Implicit data validator for checking a set of conditions on each panel change
 */
public class DynamicInstallerRequirementValidatorImpl implements DynamicInstallerRequirementValidator, Serializable
{

    /**
     *
     */
    private static final long serialVersionUID = -3752323264590369711L;

    private String conditionId;
    private Status severity;
    private String messageId;

    public DynamicInstallerRequirementValidatorImpl(String conditionId, Status severity, String messageId)
    {
        this.conditionId = conditionId;
        this.severity = severity;
        this.messageId = messageId;
    }

    public Status validateData(InstallData idata)
    {
        RulesEngine rules = idata.getRules();
        if (!rules.isConditionTrue(conditionId))
        {
            return severity;
        }

        return Status.OK;
    }

    public String getErrorMessageId()
    {
        if (this.messageId != null)
        {
            return this.messageId;
        }

        return null;
    }

    public String getWarningMessageId()
    {
        if (this.messageId != null)
        {
            return this.messageId;
        }

        return null;
    }

    public boolean getDefaultAnswer()
    {
        return (this.severity != Status.ERROR);
    }

}

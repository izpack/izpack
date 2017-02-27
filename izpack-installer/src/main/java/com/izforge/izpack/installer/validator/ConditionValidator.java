package com.izforge.izpack.installer.validator;

import java.util.Set;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.installer.gui.IzPanel;

public class ConditionValidator implements DataValidator
{

    private static final Logger LOGGER = Logger.getLogger(IzPanel.class.getName());

    private String lastFailedConditionId;

    @Override
    public Status validateData(InstallData idata)
    {
        RulesEngine rules = idata.getRules();
        Set<String> conditionIds = rules.getKnownConditionIds();
        for (String conditionId : conditionIds)
        {
            if (conditionId.toLowerCase().startsWith(this.getClass().getSimpleName().toLowerCase() + "."))
            {
                if (!rules.getCondition(conditionId).isTrue())
                {
                    LOGGER.fine("Validation failed on condition: " + conditionId);
                    lastFailedConditionId = conditionId;
                    return Status.ERROR;
                }
            }
        }
        return Status.OK;
    }

    @Override
    public String getErrorMessageId()
    {
        return lastFailedConditionId + ".error.message";
    }

    @Override
    public String getWarningMessageId()
    {
        return null;
    }

    @Override
    public boolean getDefaultAnswer()
    {
        return false;
    }

}

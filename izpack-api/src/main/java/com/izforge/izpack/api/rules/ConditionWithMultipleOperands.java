package com.izforge.izpack.api.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ConditionWithMultipleOperands extends Condition
{
    protected List<Condition> nestedConditions = new ArrayList<Condition>();

    public List<Condition> getOperands()
    {
        return nestedConditions;
    }

    public void addOperands(Condition... operands)
    {
        Collections.addAll(nestedConditions, operands);
    }
}

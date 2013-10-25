package com.izforge.izpack.rules;


public class VariableCompareCondition extends VariableCondition
{
    /**
     * 
     */
    private static final long serialVersionUID = 9133334869593548118L;

    public boolean isTrue()
    {
        if (this.installdata != null)
        {
            String val = this.installdata.getVariable(variablename);
            String val2 = this.installdata.getVariable(value);
            if (val == null)
            {
                return false;
            }
            else
            {
                return val.equals(val2);
            }
        }
        else
        {
            return false;
        }
    }

}

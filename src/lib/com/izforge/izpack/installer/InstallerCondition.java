package com.izforge.izpack.installer;


public class InstallerCondition
{
    private String condition;
    private String message;
    
    public String getCondition()
    {
        return condition;
    }
    
    public void setCondition(String condition)
    {
        this.condition = condition;
    }
    
    public String getMessage()
    {
        return message;
    }
    
    public void setMessage(String message)
    {
        this.message = message;
    }
}

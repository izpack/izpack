package com.izforge.izpack.installer;

import java.io.Serializable;

/**
 * A requirement which has to be fulfilled to start the installer.
 * @author dennis.reil
 *
 */
public class InstallerRequirement implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 2567455022706542974L;
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

package com.coi.tools.os.win;

/**
 * This class holds a representation of MS Windows ACEs.
 *
 * @author Klaus Bartz
 */
public class AccessControlEntry implements Cloneable
{

    private String owner;

    private int accessAllowdMask;

    private int accessDeniedMask;

    /**
     * Default constructor.
     */
    public AccessControlEntry()
    {
        super();
    }

    /**
     * Creates an ACE with the given parameter.
     *
     * @param owner2 owner of the ACE
     * @param allowed access allowed mask
     * @param denied access denied mask
     */
    public AccessControlEntry(String owner2, int allowed, int denied)
    {
        owner = owner2;
        accessAllowdMask = allowed;
        accessDeniedMask = denied;
    }

    /**
     * Returns the owner.
     *
     * @return the owner
     */
    public String getOwner()
    {
        return owner;
    }

    /**
     * Sets owner to the given value.
     *
     * @param owner The owner to set.
     */
    public void setOwner(String owner)
    {
        this.owner = owner;
    }

    /**
     * Returns the accessAllowdMask.
     *
     * @return the accessAllowdMask
     */
    public int getAccessAllowdMask()
    {
        return accessAllowdMask;
    }

    /**
     * Sets accessAllowdMask to the given value.
     *
     * @param accessAllowdMask The accessAllowdMask to set.
     */
    public void setAccessAllowdMask(int accessAllowdMask)
    {
        this.accessAllowdMask = accessAllowdMask;
    }

    /**
     * Returns the accessDeniedMask.
     *
     * @return the accessDeniedMask
     */
    public int getAccessDeniedMask()
    {
        return accessDeniedMask;
    }

    /**
     * Sets accessDeniedMask to the given value.
     *
     * @param accessDeniedMask The accessDeniedMask to set.
     */
    public void setAccessDeniedMask(int accessDeniedMask)
    {
        this.accessDeniedMask = accessDeniedMask;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#clone()
     */
    public Object clone()
    {
        try
        {
            return (super.clone());
        }
        catch (CloneNotSupportedException e)
        {
            e.printStackTrace();
        }
        return (null);
    }
}

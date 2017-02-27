package com.izforge.izpack.panels.process;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Anthonin Bonnefoy
 */
class ButtonConfig
{

    private final String conditionid;
    private final boolean unlockPrev;
    private final boolean unlockNext;

    /**
     * @param conditionid
     * @param unlockPrev
     * @param unlockNext
     */
    public ButtonConfig(String conditionid, boolean unlockPrev, boolean unlockNext)
    {
        this.conditionid = conditionid;
        this.unlockPrev = unlockPrev;
        this.unlockNext = unlockNext;
    }

    /**
     * @return the unlockPrev
     */
    public boolean isUnlockPrev()
    {
        return unlockPrev;
    }

    /**
     * @return the unlockNext
     */
    public boolean isUnlockNext()
    {
        return unlockNext;
    }

    /**
     * @return the conditionid
     */
    public String getConditionid()
    {
        return conditionid;
    }
}

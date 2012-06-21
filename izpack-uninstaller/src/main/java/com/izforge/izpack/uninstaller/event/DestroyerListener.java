package com.izforge.izpack.uninstaller.event;

import com.izforge.izpack.api.event.ProgressListener;


/**
 * Destroyer progress listener.
 *
 * @author Tim Anderson
 */
public abstract class DestroyerListener implements ProgressListener
{

    /**
     * The next step starts.
     *
     * @param stepName     the name of the step which starts now.
     * @param stepNo       the number of the step.
     * @param noOfSubSteps the number of sub-steps this step consists of
     */
    @Override
    public void nextStep(String stepName, int stepNo, int noOfSubSteps)
    {
        // not used
    }

    /**
     * Set the number of substeps.
     * <p/>
     * This may be used if the number of substeps changes during an action.
     *
     * @param subSteps The number of substeps.
     */
    @Override
    public void setSubStepNo(int subSteps)
    {
        // not used
    }

    /**
     * Invoked to notify progress.
     * <p/>
     * This increments the current step.
     *
     * @param message a message describing the step
     */
    @Override
    public void progress(String message)
    {
        // no-op
    }

    /**
     * Invoked when an action restarts.
     *
     * @param name           the name of the action
     * @param overallMessage a message describing the overall progress
     * @param tip            a tip describing the current progress
     * @param steps          the number of steps the action consists of
     */
    @Override
    public void restartAction(String name, String overallMessage, String tip, int steps)
    {
        // no-op
    }
}

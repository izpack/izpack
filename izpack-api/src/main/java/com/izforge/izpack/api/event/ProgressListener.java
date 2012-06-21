package com.izforge.izpack.api.event;

/**
 * This interface is used to notify the user of some progress.
 * <p/>
 * For example, the installation progress and compilation progress are communicated to the user
 * using this interface. The interface supports a two-stage progress indication: The whole action is
 * divided into steps (for example, packs when installing) and sub-steps (for example, files of a pack).
 */
public interface ProgressListener
{

    /**
     * Invoked when an action starts.
     *
     * @param name  the name of the action
     * @param steps the number of steps the action consists of
     */
    void startAction(String name, int steps);

    /**
     * Invoked when an action finishes.
     */
    void stopAction();

    /**
     * Invoked when an action step starts.
     *
     * @param stepName the name of the step
     * @param step     the step number
     * @param subSteps the number of sub-steps the step consists of
     */
    void nextStep(String stepName, int step, int subSteps);

    /**
     * Sets the number of sub-steps.
     * <p/>
     * This may be used if the number of sub-steps changes during an action.
     *
     * @param subSteps the number of sub-steps
     */
    void setSubStepNo(int subSteps);

    /**
     * Invoked to notify progress.
     * <p/>
     * This increments the current step.
     *
     * @param message a message describing the step
     */
    void progress(String message);

    /**
     * Invoked to notify progress.
     *
     * @param subStep the sub-step which will be performed next
     * @param message an additional message describing the sub-step
     */
    void progress(int subStep, String message);

    /**
     * Invoked when an action restarts.
     *
     * @param name           the name of the action
     * @param overallMessage a message describing the overall progress
     * @param tip            a tip describing the current progress
     * @param steps          the number of steps the action consists of
     */
    void restartAction(String name, String overallMessage, String tip, int steps);

}
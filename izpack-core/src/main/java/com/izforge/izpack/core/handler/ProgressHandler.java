package com.izforge.izpack.core.handler;

import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;
import com.izforge.izpack.api.handler.Prompt;


/**
 * Implements {@link AbstractUIProgressHandler} using an {@link ProgressListener} and {@link Prompt}.
 *
 * @author Tim Anderson
 */
public class ProgressHandler extends PromptUIHandler implements AbstractUIProgressHandler
{

    /**
     * The progress listener.
     */
    private final ProgressListener listener;

    /**
     * Constructs a {@code ProgressHandler}.
     *
     * @param listener the listener to delegate to
     * @param prompt   the prompt prompt to delegate to
     */
    public ProgressHandler(ProgressListener listener, Prompt prompt)
    {
        super(prompt);
        this.listener = listener;
    }

    /**
     * Invoked when an action starts.
     *
     * @param name      the name of the action
     * @param stepCount the number of steps the action consists of
     */
    @Override
    public void startAction(String name, int stepCount)
    {
        listener.startAction(name, stepCount);
    }

    /**
     * Invoked when an action finishes.
     */
    @Override
    public void stopAction()
    {
        listener.stopAction();
    }

    /**
     * Invoked when an action step starts.
     *
     * @param stepName     the name of the step
     * @param stepNo       the step number
     * @param subStepCount the number of sub-steps the step consists of
     */
    @Override
    public void nextStep(String stepName, int stepNo, int subStepCount)
    {
        listener.nextStep(stepName, stepNo, subStepCount);
    }

    /**
     * Sets the number of sub-steps.
     * <p/>
     * This may be used if the number of sub-steps changes during an action.
     *
     * @param subSteps the number of sub-steps
     */
    @Override
    public void setSubStepNo(int subSteps)
    {
        listener.setSubStepNo(subSteps);
    }

    /**
     * Notify of progress.
     *
     * @param subStepNo the sub-step which will be performed next
     * @param message   an additional message describing the sub-step
     */
    @Override
    public void progress(int subStepNo, String message)
    {
        listener.progress(subStepNo, message);
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
        listener.progress(message);
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
        listener.restartAction(name, overallMessage, tip, steps);
    }
}

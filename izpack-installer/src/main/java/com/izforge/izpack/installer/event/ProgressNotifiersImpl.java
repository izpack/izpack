package com.izforge.izpack.installer.event;

import java.util.ArrayList;
import java.util.List;

import com.izforge.izpack.api.event.InstallerListener;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.event.ProgressNotifiers;


/**
 * Implementation of the {@link ProgressNotifiers} interface.
 *
 * @author Tim Anderson
 */
public class ProgressNotifiersImpl implements ProgressNotifiers
{

    /**
     * The listeners that may perform progress notification.
     */
    private List<InstallerListener> listeners = new ArrayList<InstallerListener>();

    /**
     * Determines if the listeners should notify the progress listener.
     */
    private boolean notifyProgress = false;

    /**
     * Adds a listener that may notify a {@link ProgressListener}.
     *
     * @param listener the listener
     */
    @Override
    public void addNotifier(InstallerListener listener)
    {
        listeners.add(listener);
    }

    /**
     * Returns the index of the specified listener.
     *
     * @param listener the listener
     * @return the index of the listener or {@code -1} if it is not registered
     */
    @Override
    public int indexOf(InstallerListener listener)
    {
        return listeners.indexOf(listener);
    }

    /**
     * Determines if listeners should notify an {@link ProgressListener}.
     *
     * @param notify if {@code true}, notify the {@link ProgressListener}
     */
    @Override
    public void setNotifyProgress(boolean notify)
    {
        this.notifyProgress = notify;
    }

    /**
     * Determines if listeners should notify an {@link ProgressListener}.
     *
     * @return {@code true} if the {@link ProgressListener} should be notified
     */
    @Override
    public boolean notifyProgress()
    {
        return notifyProgress;
    }

    /**
     * Returns the count of registered listeners that may perform notification.
     *
     * @return the count of registered listeners
     */
    @Override
    public int getNotifiers()
    {
        return listeners.size();
    }
}

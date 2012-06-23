/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

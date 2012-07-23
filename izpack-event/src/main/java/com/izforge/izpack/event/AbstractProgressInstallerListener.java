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

package com.izforge.izpack.event;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.event.AbstractInstallerListener;
import com.izforge.izpack.api.event.InstallerListener;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.event.ProgressNotifiers;


/**
 * An {@link InstallerListener} that may notify {@link ProgressListener}s.
 *
 * @author Tim Anderson
 */
public abstract class AbstractProgressInstallerListener extends AbstractInstallerListener
{

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * The notifiers. May be {@code null}.
     */
    private final ProgressNotifiers notifiers;


    /**
     * Constructs an {@code AbstractProgressInstallerListener}.
     *
     * @param installData the installation data
     */
    public AbstractProgressInstallerListener(InstallData installData)
    {
        this(installData, null);
    }

    /**
     * Constructs an {@code AbstractProgressInstallerListener}.
     *
     * @param installData the installation data
     * @param notifiers   the progress notifiers. May be {@code null}
     */
    public AbstractProgressInstallerListener(InstallData installData, ProgressNotifiers notifiers)
    {
        this.installData = installData;
        this.notifiers = notifiers;
    }

    /**
     * Determines if listeners should notify a {@link ProgressListener}.
     *
     * @return {@code true} if the {@link ProgressListener} should be notified
     */
    protected boolean notifyProgress()
    {
        return (notifiers != null && notifiers.notifyProgress());
    }

    /**
     * Returns the progress notifier id of this listener.
     *
     * @return the progress notifier id of this listener, or {@code 0} if this is not registered
     */
    protected int getProgressNotifierId()
    {
        return notifiers != null ? notifiers.indexOf(this) + 1 : 0;
    }

    /**
     * Register this listener as a progress notifier.
     */
    protected void setProgressNotifier()
    {
        if (notifiers != null)
        {
            notifiers.addNotifier(this);
        }
    }

    /**
     * Returns the progress notifiers.
     *
     * @return the progress notifiers, or {@code null} if none was supplied at construction
     */
    protected ProgressNotifiers getProgressNotifiers()
    {
        return notifiers;
    }

    /**
     * Returns the installation data.
     *
     * @return the installation data
     */
    protected InstallData getInstallData()
    {
        return installData;
    }

    /**
     * Helper to return a localised message, given its identifier.
     *
     * @param id the message identifier
     * @return the corresponding message, or {@code id} if it doesn't exist
     */
    protected String getMessage(String id)
    {
        return installData.getMessages().get(id);
    }

}

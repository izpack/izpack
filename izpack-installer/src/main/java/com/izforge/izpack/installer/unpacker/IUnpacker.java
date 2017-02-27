/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil
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
package com.izforge.izpack.installer.unpacker;

import com.izforge.izpack.api.event.ProgressListener;

public interface IUnpacker extends Runnable
{

    /**
     * Return the state of the operation.
     *
     * @return true if the operation was successful, false otherwise.
     */
    boolean getResult();

    /**
     * Sets the progress listener.
     *
     * @param listener the progress listener
     */
    void setProgressListener(ProgressListener listener);

    /**
     * Interrupts the unpacker, and waits for it to complete.
     * <p/>
     * If interrupts have been prevented ({@link #isInterruptDisabled} returns
     * <tt>true</tt>), then this returns immediately.
     *
     * @param wait the maximum time to wait, in milliseconds
     * @return true if the interrupt will be performed, false if the interrupt
     * will be discarded
     */
    boolean interrupt(long wait);

    /**
     * Determines if interrupts should be disabled.
     *
     * @param disable if <tt>true</tt> disable interrupts, otherwise enable them
     */
    void setDisableInterrupt(boolean disable);

    /**
     * Determines if interrupts have been disabled or not.
     *
     * @return <tt>true</tt> if interrupts have been disabled, otherwise
     * <tt>false</tt>
     */
    boolean isInterruptDisabled();
}

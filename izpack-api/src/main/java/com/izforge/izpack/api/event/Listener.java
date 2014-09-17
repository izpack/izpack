package com.izforge.izpack.api.event;

import com.izforge.izpack.api.exception.IzPackException;

/**
 * Parent INterface for both InstallerListener and UninstallerListener
 *
 * @author Tom Helpstone
 */
public interface Listener {


    /**
     * Initialises the listener.
     *
     * @throws IzPackException for any error
     */
    void initialise();
    
}

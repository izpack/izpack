package com.izforge.izpack.installer.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The window events handler.
 *
 * @author julien created October 27, 2002
 */
class WindowHandler extends WindowAdapter
{
    private final Navigator navigator;

    public WindowHandler(Navigator navigator)
    {
        this.navigator = navigator;
    }

    /**
     * Window close is pressed,
     *
     * @param e The event.
     */
    public void windowClosing(WindowEvent e)
    {
        // We ask for confirmation
        navigator.quit();
    }
}

package com.izforge.izpack.uninstaller.console;

import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.uninstaller.event.DestroyerListener;
import com.izforge.izpack.util.Console;

/**
 * The console destroyer progress listener.
 */
public class ConsoleDestroyerListener extends DestroyerListener
{

    /**
     * The console.
     */
    private final Console console;

    /**
     * The locale-specific messages.
     */
    private final Messages messages;


    /**
     * Constructs a {@code ConsoleDestroyerHandler}.
     *
     * @param console  the console
     * @param messages the locale-specific messages
     */
    public ConsoleDestroyerListener(Console console, Messages messages)
    {
        this.console = console;
        this.messages = messages;
    }

    /**
     * The destroyer starts.
     *
     * @param name The name of the overall action. Not used here.
     * @param max  The maximum value of the progress.
     */
    public void startAction(final String name, final int max)
    {
        console.println("Processing " + name);
    }

    /**
     * The destroyer stops.
     */
    public void stopAction()
    {
        console.println(messages.get("InstallPanel.finished"));
    }

    /**
     * The destroyer progresses.
     *
     * @param subStepNo The actual position.
     * @param message   The message.
     */
    public void progress(final int subStepNo, final String message)
    {
        console.println(message);
    }

}

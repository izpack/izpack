package com.izforge.izpack.uninstaller.gui;

import javax.swing.SwingUtilities;

import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.uninstaller.event.DestroyerListener;

/**
 * The GUI destroyer progress listener.
 *
 * @author Julien Ponge
 * @author Tino Schwarze
 */
public class GUIDestroyerListener extends DestroyerListener
{

    /**
     * The parent frame.
     */
    private UninstallerFrame uninstallerFrame;

    /**
     * The locale specific messages.
     */
    private final Messages messages;


    /**
     * Constructs a {@code GUIDestroyerHandler}.
     *
     * @param messages the locale-specific messages
     */
    public GUIDestroyerListener(Messages messages)
    {
        this.messages = messages;
    }

    /**
     * Registers the uninstaller frame.
     *
     * @param frame the frame
     */
    public void setUninstallerFrame(UninstallerFrame frame)
    {
        this.uninstallerFrame = frame;
    }

    /**
     * The destroyer starts.
     *
     * @param name The name of the overall action. Not used here.
     * @param max  The maximum value of the progress.
     */
    public void startAction(final String name, final int max)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                uninstallerFrame.progressBar.setMinimum(0);
                uninstallerFrame.progressBar.setMaximum(max);
                uninstallerFrame.blockGUI();
            }
        });
    }

    /**
     * The destroyer stops.
     */
    public void stopAction()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                uninstallerFrame.progressBar.setString(messages.get("InstallPanel.finished"));
                uninstallerFrame.targetDestroyCheckbox.setEnabled(false);
                uninstallerFrame.destroyButton.setEnabled(false);
                uninstallerFrame.releaseGUI();
            }
        });
    }

    /**
     * The destroyer progresses.
     *
     * @param pos     The actual position.
     * @param message The message.
     */
    public void progress(final int pos, final String message)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                uninstallerFrame.progressBar.setValue(pos);
                uninstallerFrame.progressBar.setString(message);
            }
        });
    }

}

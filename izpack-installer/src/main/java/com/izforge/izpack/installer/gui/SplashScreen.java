package com.izforge.izpack.installer.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;

import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.data.GUIInstallData;

/**
 * Splash screen to show before loading any other panels.
 */
public class SplashScreen
{

    private static final Logger LOGGER = Logger.getLogger(SplashScreen.class.getName());

    private final Resources resources;
    private final GUIInstallData installData;

    private JFrame frame;

    public SplashScreen(Resources resources, GUIInstallData installData)
    {
        this.installData = installData;
        this.resources = resources;
    }

    /**
     * Display the splash screen. Will only display if the user has set the
     * guipref modifier. Splash screen will display for a minimum fo X
     * milliseconds based on the user's useSplashScreen modifier's value.
     */
    public void displaySplashScreen(final Object trigger)
    {
        if (installData.guiPrefs.modifier.containsKey("useSplashScreen"))
        {
            ImageIcon splashIcon = resources.getImageIcon("/resources/Splash.image");
            if (splashIcon != null)
            {
                try
                {
                    frame = new JFrame();
                    frame.setUndecorated(true);

                    JLabel labelSplash = new JLabel(splashIcon);

                    frame.getContentPane().add(labelSplash, BorderLayout.CENTER);
                    frame.pack();
                    frame.setLocationRelativeTo(null);

                    frame.setVisible(true);
                }
                catch (Exception e)
                {
                    LOGGER.log(Level.WARNING, "Prepare and display splashScreen failed.", e);
                }
            }
            else
            {
                LOGGER.log(Level.WARNING, "No splash icon found!!!");
            }

            try
            {
                int duration = Integer.parseInt(installData.guiPrefs.modifier.get("useSplashScreen"));
                if (duration > 0)
                {
                    // we're in AWT thread already, start a swing timer that notifies the waiting thread
                    Timer timer = new Timer(duration, new ActionListener()
                    {

                        @Override
                        public void actionPerformed(ActionEvent e)
                        {

                            synchronized (trigger)
                            {
                                trigger.notifyAll();
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                }
            }
            catch (Exception e)
            {
                //Failed to sleep
                //Failed to get duration
                //Failed to get splash screen resource
            }
        }
    }

    /**
     * Remove the splash screen screen.
     */
    public void removeSplashScreen()
    {
        if (frame != null)
        {
            frame.setVisible(false);
        }
    }
}

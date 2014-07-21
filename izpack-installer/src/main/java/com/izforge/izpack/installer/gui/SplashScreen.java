package com.izforge.izpack.installer.gui;

import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.data.GUIInstallData;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ImageIcon;

/**
 * Splash screen to show before loading any other panels.
 */
public class SplashScreen extends JFrame
{

    private final Resources resources;
    private final GUIInstallData installData;

    public SplashScreen(Resources resources, GUIInstallData installData)
    {
        this.installData = installData;
        this.resources = resources;
        this.setResizable(false);
        this.setVisible(false);
    }

    /**
     * Display the splash screen.
     * Will only display if the user has set the guipref modifier.
     * Splash screen will display for a minimum fo X milliseconds based on the user's useSplashScreen modifier's value.
     */
    public void displaySplashScreen()
    {
        ImageIcon splashIcon = resources.getImageIcon("/resources/Splash.image");
        if (splashIcon != null)
        {
            this.add(new JLabel(splashIcon));
            this.setSize(splashIcon.getIconWidth(), splashIcon.getIconHeight());
            this.setLocationRelativeTo(null);
        }

        if (installData.guiPrefs.modifier.containsKey("useSplashScreen"))
        {
            this.setVisible(true);
            try
            {
                int duration = Integer.parseInt(installData.guiPrefs.modifier.get("useSplashScreen"));
                if(duration > 0)
                {
                    Thread.sleep(duration);
                }
            }
            catch (Exception e)
            {
                //Failed to sleep
                //Failed to get duration
            }
        }
    }

    /**
     * Remove the splash screen screen.
     */
    public void removeSplashScreen()
    {
        this.setVisible(false);
    }
}

package com.izforge.izpack.installer.gui;

/**
 * Interface for Installer view control
 *
 * @author Anthonin Bonnefoy
 */
public interface InstallerView
{

    void lockPrevButton();

    void lockNextButton();

    void lockQuitButton();

    void unlockPrevButton();

    void unlockNextButton();

    void unlockNextButton(boolean requestFocus);

    void unlockQuitButton();

    void navigateNext();

    void navigatePrevious();

    void showHelp();

    void sizeFrame();
}

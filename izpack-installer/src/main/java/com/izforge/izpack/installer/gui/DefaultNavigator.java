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
package com.izforge.izpack.installer.gui;


import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.panel.Panels;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.izforge.izpack.api.GuiId.*;

/**
 * Default implementation of {@link Navigator}.
 *
 * @author Tim Anderson
 */
public class DefaultNavigator implements Navigator
{
    /**
     * The parent frame.
     */
    private InstallerFrame frame;

    /**
     * The panels.
     */
    private final Panels panels;

    /**
     * The previous button.
     */
    private JButton previous;

    /**
     * The next button.
     */
    private JButton next;

    /**
     * The quit button.
     */
    private JButton quit;

    /**
     * Flag to indicate if the current panel is being switched for another.
     */
    private boolean switchPanel;

    /**
     * Determines if the next button should be configured after panel switching; {@code true} prior to switching,
     * {@code false} if the button is configured externally during the switch.
     */
    private boolean configureNext = true;

    /**
     * Determines if the previous button should be configured after panel switching; {@code true} prior to switching,
     * {@code false} if the button is configured externally during the switch.
     */
    private boolean configurePrevious = true;

    /**
     * Constructs a {@code DefaultNavigator}.
     *
     * @param panels      the panels
     * @param icons       the icons
     * @param installData the installation data
     */
    public DefaultNavigator(Panels panels, IconsDatabase icons, GUIInstallData installData)
    {
        this.panels = panels;
        Messages messages = installData.getMessages();
        previous = ButtonFactory.createButton(messages.get("installer.prev"), icons.get("stepback"),
                                              installData.buttonsHColor);
        ActionListener navHandler = new NavigationHandler();
        previous.addActionListener(navHandler);
        previous.setName(BUTTON_PREV.id);
        previous.setVisible(false);

        next = ButtonFactory.createButton(messages.get("installer.next"), icons.get("stepforward"),
                                          installData.buttonsHColor);
        next.setName(BUTTON_NEXT.id);
        next.addActionListener(navHandler);

        quit = ButtonFactory.createButton(messages.get("installer.quit"), icons.get("stop"),
                                          installData.buttonsHColor);
        quit.setName(BUTTON_QUIT.id);
        quit.addActionListener(navHandler);

        configureVisibility();
    }

    protected void updateButtonText(Messages messages)
    {

        previous.setText(messages.get("installer.prev"));
        next.setText(messages.get("installer.next"));
        quit.setText(messages.get("installer.quit"));

    }

    /**
     * Call to reserve mnemonics for the buttons used by the navigator.
     */
    public void reserveNavigatorButtonMnemonics()
    {
        JButton [] buttons = {
                quit,
                next,
                previous
        };

        ButtonFactory.reserveButtonMnemonics(buttons);
    }

    /**
     * Registers the parent installer frame.
     * <p/>
     * This should be invoked before using any other methods.
     *
     * @param frame the frame.
     */
    public void setInstallerFrame(InstallerFrame frame)
    {
        this.frame = frame;
    }

    /**
     * Determines if the next panel may be navigated to.
     *
     * @return {@code true} if the next panel may be navigated to
     */
    @Override
    public boolean isNextEnabled()
    {
        return panels.isNextEnabled();
    }

    /**
     * Determines if the next panel may be navigated to.
     *
     * @param enable if {@code true}, enable navigation, otherwise disable it
     */
    @Override
    public void setNextEnabled(boolean enable)
    {
        configureNext = !switchPanel;
        panels.setNextEnabled(enable);
        next.setEnabled(enable);
    }

    /**
     * Makes the next button visible or invisible.
     *
     * @param visible if {@code true} makes the button visible, otherwise makes it invisible.
     */
    @Override
    public void setNextVisible(boolean visible)
    {
        next.setVisible(visible);
    }

    /**
     * Sets the text for the 'next' button.
     *
     * @param text the button text. May be {@code null}
     */
    @Override
    public void setNextText(String text)
    {
        next.setText(text);
    }

    /**
     * Sets the icon for the 'next' button.
     *
     * @param icon the icon. May be {@code null}
     */
    @Override
    public void setNextIcon(Icon icon)
    {
        next.setIcon(icon);
    }

    /**
     * Determines if the previous panel may be navigated to.
     *
     * @return {@code true} if the previous panel may be navigated to
     */
    @Override
    public boolean isPreviousEnabled()
    {
        return panels.isPreviousEnabled();
    }

    /**
     * Determines if the previous panel may be navigated to.
     *
     * @param enable if {@code true}, enable navigation, otherwise disable it
     */
    @Override
    public void setPreviousEnabled(boolean enable)
    {
        configurePrevious = !switchPanel;
        panels.setPreviousEnabled(enable);
        previous.setEnabled(enable);
    }

    /**
     * Makes the previous button visible/invisible.
     *
     * @param visible if {@code true} makes the button visible, otherwise makes it invisible.
     */
    @Override
    public void setPreviousVisible(boolean visible)
    {
        previous.setVisible(visible);
    }

    /**
     * Sets the text for the 'previous' button.
     *
     * @param text the button text. May be {@code null}
     */
    @Override
    public void setPreviousText(String text)
    {
        previous.setText(text);
    }

    /**
     * Sets the icon for the 'previous' button.
     *
     * @param icon the icon. May be {@code null}
     */
    @Override
    public void setPreviousIcon(Icon icon)
    {
        previous.setIcon(icon);
    }

    /**
     * Determines if the 'quit' button is enabled.
     *
     * @return {@code true} if the 'quit' button is enabled
     */
    @Override
    public boolean isQuitEnabled()
    {
        return quit.isEnabled();
    }

    /**
     * Determines if the 'quit' button is enabled.
     *
     * @param enable if {@code true}, enable quit, otherwise disable it
     */
    @Override
    public void setQuitEnabled(boolean enable)
    {
        quit.setEnabled(enable);
    }

    /**
     * Makes the 'quit' button visible/invisible.
     *
     * @param visible if {@code true} makes the button visible, otherwise makes it invisible.
     */
    @Override
    public void setQuitVisible(boolean visible)
    {
        quit.setVisible(visible);
    }

    /**
     * Sets the text for the 'quit' button.
     *
     * @param text the button text. May be {@code null}
     */
    @Override
    public void setQuitText(String text)
    {
        quit.setText(text);
    }

    /**
     * Sets the icon for the 'quit' button.
     *
     * @param icon the icon. May be {@code null}
     */
    @Override
    public void setQuitIcon(Icon icon)
    {
        quit.setIcon(icon);
    }

    /**
     * Navigates to the next panel.
     *
     * @return {@code true} if the next panel was displayed, or {@code false} if the last panel is displayed
     */
    @Override
    public boolean next()
    {
        return next(true);
    }

    /**
     * Navigates to the next panel.
     *
     * @param validate if {@code true}, only move to the next panel if validation succeeds
     * @return {@code true} if the next panel was navigated to
     */
    public boolean next(boolean validate)
    {
        boolean result = false;
        if (panels.isNextEnabled())
        {
            try
            {
                preSwitchPanel();
                result = panels.next(validate);
            }
            finally
            {
                postSwitchPanel();
            }
            configureVisibility();
        }
        return result;
    }

    /**
     * Navigates to the previous panel.
     *
     * @return {@code true} if the previous panel was displayed, or {@code false} if the first panel is displayed
     */
    @Override
    public boolean previous()
    {
        boolean result = false;
        if (panels.isPreviousEnabled())
        {
            try
            {
                preSwitchPanel();
                result = panels.previous();
            }
            finally
            {
                postSwitchPanel();
            }
            configureVisibility();
        }
        return result;
    }

    /**
     * Quits installation, if quit is enabled, and installation is complete.
     * <p/>
     * This method does not return if the quit is accepted.
     */
    @Override
    public void quit()
    {
        if (isQuitEnabled())
        {
            frame.quit();
        }
    }

    /**
     * Returns the button to navigate to the next panel.
     *
     * @return the 'next' button
     */
    protected JButton getNext()
    {
        return next;
    }

    /**
     * Returns the button to navigate to the previous panel.
     *
     * @return the 'previous' button
     */
    protected JButton getPrevious()
    {
        return previous;
    }

    /**
     * Returns the button to quit installation.
     *
     * @return the 'quit' button
     */
    protected JButton getQuit()
    {
        return quit;
    }

    /**
     * Sets the default button.
     * <p/>
     * This sets the default to the "next" button if it is enabled. If not it sets it to the "quit" button, if it is
     * enabled.
     *
     * @return the default button, or {@code null} if the buttons aren't enabled
     */
    protected JButton setDefaultButton()
    {
        JButton result = null;
        if (next.isEnabled())
        {
            result = next;
            quit.setDefaultCapable(false);
            previous.setDefaultCapable(false);
            next.setDefaultCapable(true);
        }
        else if (quit.isEnabled())
        {
            result = quit;
            quit.setDefaultCapable(true);
            previous.setDefaultCapable(false);
            next.setDefaultCapable(false);
        }
        return result;
    }

    /**
     * Invoked prior to switching panels.
     */
    private void preSwitchPanel()
    {
        switchPanel = true;
        configureNext = true;
        configurePrevious = true;
    }

    /**
     * Invoked after switching panels.
     */
    private void postSwitchPanel()
    {
        switchPanel = false;
    }

    /**
     * Configures next/previous button visibility.
     */
    private void configureVisibility()
    {
        boolean isLastPanel = panels.getNext(true) == -1;
        if (isLastPanel)
        {
            // last panel. Disable navigation.
            setPreviousVisible(false);
            setPreviousEnabled(false);
            setNextVisible(false);
            setNextEnabled(false);
        }
        else
        {
            if (configurePrevious)
            {
                // only configure the previous button if it wasn't modified during panel switching
                boolean enablePrev = panels.getPrevious(true) != -1;
                setPreviousVisible(enablePrev);
                setPreviousEnabled(enablePrev);
            }

            if (configureNext)
            {
                // only configure the next button if it wasn't modified during panel switching
                boolean enableNext = !isLastPanel;
                setNextVisible(enableNext);
                setNextEnabled(enableNext);
            }
        }

        if (frame != null && frame.getRootPane() != null)
        {
            // With VM version >= 1.5 setting default button one time will not work.
            // Therefore we set it every newPanel switch and that also later. But in
            // the moment it seems so that the quit button will not used as default button.
            // No idea why... (Klaus Bartz, 06.09.25)
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    frame.getRootPane().setDefaultButton(setDefaultButton());
                }
            });
        }
    }

    /**
     * Handles the events from the navigation bar elements.
     *
     * @author Julien Ponge
     */
    class NavigationHandler implements ActionListener
    {

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            // Some panels activation may be slow, hence we block the GUI, spin a thread to handle navigation then
            // release the GUI.
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {

                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            frame.blockGUI();
                            navigate(e);
                            frame.releaseGUI();
                        }
                    });
                }
            }).start();
        }

        private void navigate(ActionEvent e)
        {
            Object source = e.getSource();
            if (source == previous)
            {
                previous();
            }
            else if (source == next)
            {
                next();
            }
            else if (source == quit)
            {
                quit();
            }
        }
    }

}

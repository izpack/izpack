/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyrght 2008 Patrick Zbinden.
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

import static java.awt.Desktop.getDesktop;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * Common HTML Help Window (modal)
 */
public class HelpWindow extends JDialog implements HyperlinkListener, ActionListener
{

    private static final long serialVersionUID = -357544689286217809L;

    private static final Logger LOGGER = Logger.getLogger(HelpWindow.class.getName());

    private JPanel contentPane = null;

    private JEditorPane htmlHelp = null;

    private JButton closeButton = null;

    private JScrollPane scrollPane = null;

    private String closeButtonText = "Close";

    /**
     * This is the default constructor
     *
     * @param owner - owner Frame
     * @param closeButtonText - Button Text for Close button
     */
    public HelpWindow(Frame owner, String closeButtonText)
    {
        super(owner, true);
        this.closeButtonText = closeButtonText;
        initialize();
    }

    /**
     * This method initializes Help Dialog
     */
    private void initialize()
    {
        this.setSize(600, 400);
        this.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        this.setContentPane(getJContentPane());
    }

    /**
     * This method initializes jContentPane
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane()
    {
        if (contentPane == null)
        {
            contentPane = new JPanel();
            contentPane.setLayout(new BoxLayout(getJContentPane(), BoxLayout.Y_AXIS));
            contentPane.add(getScrollPane(), null);
            contentPane.add(getCloseButton(), null);
        }
        return contentPane;
    }

    /**
     * This method initializes _htmlHelp
     *
     * @return javax.swing.JEditorPane
     */
    private JEditorPane getHtmlHelp()
    {
        if (htmlHelp == null)
        {
            try
            {
                htmlHelp = new JEditorPane();
                htmlHelp.setContentType("text/html"); // Generated
                htmlHelp.setEditable(false);
                htmlHelp.addHyperlinkListener(this);
            }
            catch (java.lang.Throwable e)
            {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
        return htmlHelp;
    }

    private JScrollPane getScrollPane()
    {
        if (scrollPane == null)
        {
            try
            {
                scrollPane = new JScrollPane(getHtmlHelp());
            }
            catch (java.lang.Throwable e)
            {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
        return scrollPane;
    }

    /**
     * This method initializes _btnClose
     *
     * @return javax.swing.JButton
     */
    private JButton getCloseButton()
    {
        if (closeButton == null)
        {
            try
            {
                closeButton = new JButton(closeButtonText);
                closeButton.setAlignmentX(CENTER_ALIGNMENT);
                closeButton.addActionListener(this);
            }
            catch (java.lang.Throwable e)
            {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
        return closeButton;
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e)
    {
        try
        {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
            {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                {
                    String url = e.getURL().toString();
                    if (url.startsWith("#"))
                    {
                        getHtmlHelp().setPage(e.getURL());
                    }
                    else
                    {
                        getDesktop().browse(e.getURL().toURI());
                    }
                }
            }
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    /**
     * displays Help Text in a modal window
     *
     * @param title
     * @param helpDocument
     */
    public void showHelp(String title, URL helpDocument)
    {
        this.setTitle(title);
        try
        {
            getHtmlHelp().setPage(helpDocument);
        }
        catch (IOException e)
        {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        // Close button pressed
        this.setVisible(false);
    }
}

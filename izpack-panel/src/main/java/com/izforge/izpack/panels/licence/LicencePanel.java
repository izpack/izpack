/*
 * IzPack - Copyright 2001-2016 The IzPack project team.
 * All Rights Reserved.
 *
 * http://izpack.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.panels.licence;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * The license panel.
 *
 * @author Julien Ponge
 */
public class LicencePanel extends AbstractLicencePanel implements ActionListener
{
    private static final long serialVersionUID = 3691043187997552948L;

    /**
     * The radio buttons.
     */
    private final JRadioButton yesRadio;
    private final JRadioButton noRadio;

    /**
     * Constructs a <tt>LicencePanel</tt>.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent window
     * @param installData the installation data
     * @param resources   the resources
     * @param log         the log
     */
    public LicencePanel(Panel panel, final InstallerFrame parent, GUIInstallData installData, Resources resources,
                        Log log)
    {
        super(panel, parent, installData, new IzPanelLayout(log), resources);

        // We put our components
        add(LabelFactory.create(getString(titleMessageKey),
                                parent.getIcons().get("history"), LEADING), NEXT_LINE);

        JTextArea textArea = new JTextArea(loadLicenceAsString());
        textArea.setName(GuiId.LICENCE_TEXT_AREA.id);
        textArea.setCaretPosition(0);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scroller = new JScrollPane(textArea);
        scroller.setAlignmentX(LEFT_ALIGNMENT);
        add(scroller, NEXT_LINE);

        // register a listener to trigger the default button if enter is pressed whilst the text area has the focus
        ActionListener fireDefault = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JButton defaultButton = parent.getRootPane().getDefaultButton();
                if (defaultButton != null && defaultButton.isEnabled())
                {
                    defaultButton.doClick();
                }
            }
        };
        textArea.registerKeyboardAction(fireDefault, null, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                                        JComponent.WHEN_FOCUSED);

        ButtonGroup group = new ButtonGroup();

        yesRadio = new JRadioButton(getString("LicencePanel.agree"), false);
        yesRadio.setName(GuiId.LICENCE_YES_RADIO.id);
        group.add(yesRadio);
        add(yesRadio, NEXT_LINE);
        yesRadio.addActionListener(this);
        yesRadio.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e)
            { 
            }
            
            @Override
            public void keyReleased(KeyEvent e)
            {
                if (yesRadio.isSelected())
                {
                    parent.unlockNextButton(false);
                }
                else
                {
                    parent.lockNextButton();
                }
            }
            
            @Override
            public void keyPressed(KeyEvent e) {
            }
        });

        noRadio = new JRadioButton(getString("LicencePanel.notagree"), true);
        noRadio.setName(GuiId.LICENCE_NO_RADIO.id);
        group.add(noRadio);
        add(noRadio, NEXT_LINE);
        noRadio.addActionListener(this);
        noRadio.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e)
            { 
            }
            
            @Override
            public void keyReleased(KeyEvent e)
            {
                if (yesRadio.isSelected())
                {
                    parent.unlockNextButton(false);
                }
                else
                {
                    parent.lockNextButton();
                }
            }
            
            @Override
            public void keyPressed(KeyEvent e) {
            }
        });

        setInitialFocus(noRadio);
        getLayoutHelper().completeLayout();
    }

    /**
     * Actions-handling method (here it allows the installation).
     *
     * @param e The event.
     */
    public void actionPerformed(ActionEvent e)
    {
        if (yesRadio.isSelected())
        {
            parent.unlockNextButton();
        }
        else
        {
            parent.lockNextButton();
        }
    }
    
    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return true if the user has agreed.
     */
    public boolean isValidated()
    {
        if (noRadio.isSelected())
        {
            parent.exit();
            return false;
        }
        return (yesRadio.isSelected());
    }

    /**
     * Called when the panel becomes active.
     */
    public void panelActivate()
    {
        if (!yesRadio.isSelected())
        {
            parent.lockNextButton();
        }
    }
}

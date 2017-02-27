/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/ http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.izforge.izpack.installer.multiunpacker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.gui.InstallerFrame;

/**
 * Dialog for choosing the next volume.
 *
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class NextMediaDialog extends JDialog implements ActionListener
{

    private static final String NEXTMEDIA_MSG_ID = "nextmedia.msg";
    private static final String NEXTMEDIA_TITLE_ID = "nextmedia.title";
    private static final String BROWSEBTN_ID = "nextmedia.browsebtn";
    private static final String OKBTN_ID = "nextmedia.okbtn";
    private static final String CANCELBTN_ID = "nextmedia.cancelbtn";

    private static final long serialVersionUID = -2551719029962051020L;

    protected JLabel msg;
    protected JTextField path;
    protected JButton browsebtn;
    protected JButton okbtn;
    protected JButton cancelbtn;

    protected String nextmedianame;
    protected String nextmediapath;
    protected String nextmediainput;
    private final Messages messages;
    protected IconsDatabase icons;

    protected Frame owner;

    /**
     * @throws HeadlessException
     */
    public NextMediaDialog(InstallerFrame main, String nextmedia) throws HeadlessException
    {
        this(null, main, nextmedia);
    }

    /**
     * @param owner
     * @throws HeadlessException
     */
    public NextMediaDialog(Frame owner, InstallerFrame main, String nextmedia) throws HeadlessException
    {
        this(owner, main.getMessages(), main.getIcons(), nextmedia);
    }

    public NextMediaDialog(Frame owner, Messages messages, IconsDatabase icons, String nextmedia)
    {
        super(owner, messages.get(NEXTMEDIA_TITLE_ID), true);
        this.owner = owner;
        this.messages = messages;
        this.icons = icons;
        this.nextmediapath = nextmedia;
        File nextmediafile = new File(this.nextmediapath);
        this.nextmedianame = nextmediafile.getName();
        this.initUI();
    }

    public NextMediaDialog(Frame owner, InstallData idata, String nextmedia)
    {
        this(owner, idata.getMessages(), null, nextmedia);
    }

    private void initUI()
    {
        if (this.icons != null)
        {
            this.msg = LabelFactory.create(messages.get(NEXTMEDIA_MSG_ID), this.icons.get("warning"),
                    JLabel.LEFT);
            this.browsebtn = ButtonFactory.createButton(messages.get(BROWSEBTN_ID), this.icons.get("open"),
                    new Color(230, 230, 230));
            this.okbtn = ButtonFactory.createButton(messages.get(OKBTN_ID), this.icons.get("ok"),
                    new Color(230, 230, 230));
            this.cancelbtn = ButtonFactory.createButton(messages.get(CANCELBTN_ID), this.icons.get("cancel"),
                    new Color(230, 230, 230));
        }
        else
        {
            this.msg = new JLabel(messages.get(NEXTMEDIA_MSG_ID), JLabel.LEFT);
            this.browsebtn = new JButton(messages.get(BROWSEBTN_ID));
            this.okbtn = new JButton(messages.get(OKBTN_ID));
            this.cancelbtn = new JButton(messages.get(CANCELBTN_ID));
        }
        this.path = new JTextField(this.nextmediapath);
        this.path.setColumns(40);

        this.browsebtn.addActionListener(this);
        this.okbtn.addActionListener(this);
        this.cancelbtn.addActionListener(this);

        JPanel mainpanel = new JPanel();
        mainpanel.setLayout(new BoxLayout(mainpanel, BoxLayout.PAGE_AXIS));
        mainpanel.add(this.msg);

        JPanel pathpanel = new JPanel();
        pathpanel.setLayout(new BoxLayout(pathpanel, BoxLayout.LINE_AXIS));
        pathpanel.add(this.path);
        pathpanel.add(this.browsebtn);
        pathpanel.add(Box.createHorizontalGlue());
        mainpanel.add(pathpanel);

        JPanel okpanel = new JPanel();
        okpanel.setLayout(new BoxLayout(okpanel, BoxLayout.LINE_AXIS));
        okpanel.add(Box.createHorizontalGlue());
        okpanel.add(this.okbtn);
        okpanel.add(this.cancelbtn);
        okpanel.add(Box.createHorizontalGlue());
        mainpanel.add(okpanel);
        mainpanel.add(Box.createVerticalGlue());

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(mainpanel, BorderLayout.CENTER);

        this.pack();
        // set location
        if (this.owner != null)
        {
            Dimension mysize = this.getSize();
            Dimension ownersize = this.owner.getSize();
            Point position = this.owner.getLocationOnScreen();
            Point centerposition = new Point();
            centerposition.setLocation(position.getX() + 0.5 * ownersize.getWidth(),
                    position.getY() + 0.5 * ownersize.getHeight());
            Point myposition = new Point();
            myposition.setLocation(centerposition.getX() - 0.5 * mysize.getWidth(),
                    centerposition.getY() - 0.5 * mysize.getHeight());
            this.setLocation(myposition);
        }
    }

    public String getNextMedia()
    {
        return this.nextmediainput;
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == this.browsebtn)
        {
            JFileChooser fileChooser;
            if (this.path.getText() != null)
            {
                fileChooser = new JFileChooser(this.path.getText());
            }
            else
            {
                fileChooser = new JFileChooser();
            }
            fileChooser.setFileFilter(new NextMediaFileFilter(this.nextmedianame, this.messages));
            fileChooser.setDialogTitle(messages.get("nextmedia.choosertitle"));
            fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                this.nextmediainput = fileChooser.getSelectedFile().getAbsolutePath();
                this.path.setText(this.nextmediainput);
            }
        }
        else if (e.getSource() == this.okbtn)
        {
            this.nextmediainput = this.path.getText();
            // close this dialog
            this.setVisible(false);
        }
        else if (e.getSource() == this.cancelbtn)
        {
            int option = JOptionPane.showConfirmDialog(this, messages.get("installer.quit.message"),
                    messages.get("installer.quit.title"),
                    JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION)
            {
                // exit
                System.exit(-1);
            }
        }
    }
}

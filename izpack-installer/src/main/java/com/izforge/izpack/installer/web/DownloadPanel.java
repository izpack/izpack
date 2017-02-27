/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Vladimir Ralev
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
package com.izforge.izpack.installer.web;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Displays progress and stats while downloading repository files.
 *
 * @author <a href="vralev@redhat.com">Vladimir Ralev</a>
 * @version $Revision: 1.1 $
 */
public class DownloadPanel extends JDialog implements ActionListener
{

    private static final long serialVersionUID = -4458769435196053866L;

    JLabel statusLabel = new JLabel("", JLabel.RIGHT);

    JLabel fileLabel = new JLabel("File", JLabel.LEFT);

    JButton button = new JButton("Cancel");

    JProgressBar progressBar = new JProgressBar();

    String statusText;

    String fileText;

    LoggedInputStream lis;

    public DownloadPanel(LoggedInputStream lis)
    {
        Dimension dialogSize = new Dimension(406, 150);
        this.setLayout(null);
        this.setMinimumSize(dialogSize);
        this.setMaximumSize(dialogSize);
        this.setPreferredSize(dialogSize);
        this.setAlwaysOnTop(true);
        this.setResizable(false);
        this.setSize(dialogSize);
        this.lis = lis;

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        JPanel contents = (JPanel) getContentPane();
        contents.setLayout(null);
        contents.setSize(dialogSize);

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        contents.add(fileLabel);
        contents.add(statusLabel);
        contents.add(progressBar);
        contents.add(button);

        button.addActionListener(this);

        fileLabel.setBounds(10, 10, 260, 20);
        statusLabel.setBounds(270, 10, 120, 20);
        progressBar.setBounds(10, 35, 380, 20);
        button.setBounds(200 - 50, 70, 100, 25);
        pack();
    }

    public void setStatusLabel(String text)
    {
        statusText = text;
        if (!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    statusLabel.setText(statusText);
                }
            });
        }

    }

    public void setFileLabel(String text)
    {
        int maxStr = 35;
        int lastSeparator = text.lastIndexOf("/");
        text = text.substring(lastSeparator + 1, text.length());
        int length = text.length();

        if (length > maxStr)
        {
            fileText = ".." + text.substring(length - maxStr, length);
        }
        else
        {
            fileText = text;
        }

        if (!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    fileLabel.setText(fileText);
                }
            });
        }

    }

    public void actionPerformed(ActionEvent e)
    {
        lis.setCancelled(true);
        this.dispose();
    }

    public void setProgressMax(int total)
    {
        progressBar.setIndeterminate(false);
        progressBar.setStringPainted(true);
        progressBar.setMaximum(total);
        progressBar.setMinimum(0);
    }

    public void setProgressCurrent(int curr)
    {
        progressBar.setValue(curr);
    }
}

/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Johannes Lehtinen
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
import java.awt.*;
import java.io.InputStream;
import java.net.*;
import java.util.Locale;

/**
 * Dialogs for password authentication and firewall specification, when needed,
 * during web installation.
 *
 * @author Chadwick McHenry
 * @author <a href="vralev@redhat.com">Vladimir Ralev</a>
 * @version 1.0
 */
public class WebAccessor
{

    private Thread openerThread = null;

    private InputStream iStream = null;

    private Exception exception = null;

    private Object soloCancelOption = null;

    private Component parent = null;

    private JDialog dialog = null;

    private boolean tryProxy = false;

    private JPanel passwordPanel = null;

    private JLabel promptLabel;

    private JTextField nameField;

    private JPasswordField passField;

    private JPanel proxyPanel = null;

    private JLabel errorLabel;

    private JTextField hostField;

    private JTextField portField;

    private String url;

    private int contentLength = -1;

    /**
     * Not yet Implemented: placeholder for headless installs.
     *
     * @throws UnsupportedOperationException
     */
    public WebAccessor()
    {
        // the class should probably be rearranged to do this.
        throw new UnsupportedOperationException();
    }

    /**
     * Create a WebAccessor that prompts for proxies and passwords using a
     * JDialog.
     *
     * @param parent determines the frame in which the dialog is displayed; if
     * the parentComponent has no Frame, a default Frame is used
     */
    public WebAccessor(Component parent)
    {
        this.parent = parent;
        Locale locale = null;
        if (parent != null)
        {
            parent.getLocale();
        }
        soloCancelOption = UIManager.get("OptionPane.cancelButtonText", locale);// TODO:
        // i18n?
        Authenticator.setDefault(new MyDialogAuthenticator());
    }

    /**
     * Opens a URL connection and returns it's InputStream for the specified
     * URL.
     *
     * @param url the url to open the stream to.
     * @return an input stream ready to read, or null on failure
     */
    public InputStream openInputStream(URL url)
    {
        setUrl(url.toExternalForm());
        OPEN_URL:
        while (true)
        {
            startOpening(url); // this starts a thread

            Thread.yield();

            // Wait a bit to see if the stream comes up
            int retry = 28;
            while (exception == null && iStream == null && retry > 0)
            {
                try
                {
                    Thread.sleep(200);
                    retry--;
                }
                catch (Exception e)
                {
                    System.out.println("In openInputStream: " + e);
                }
            }

            /* Try to find a proxy if that failed */
            // success!
            if (iStream != null)
            {
                break;
            }

            // an exception we don't expect setting a proxy to fix
            if (!tryProxy)
            {
                break;
            }

            // else (exception != null)
            // show proxy dialog until valid values or cancel
            JPanel panel = getProxyPanel();
            errorLabel.setText("Unable to connect: " + exception.getMessage());
            while (true)
            {
                int result = JOptionPane.showConfirmDialog(parent, panel, "Proxy Configuration",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (result != JOptionPane.OK_OPTION) // canceled
                {
                    break OPEN_URL;
                }

                String host = null;
                String port = null;

                try
                {
                    InetAddress addr = InetAddress.getByName(hostField.getText());
                    host = addr.getHostName();
                }
                catch (Exception x)
                {
                    errorLabel.setText("Unable to resolve Host");
                    Toolkit.getDefaultToolkit().beep();
                }

                try
                {
                    if (host != null)
                    {
                        port = Integer.valueOf(portField.getText()).toString();
                    }
                }
                catch (NumberFormatException x)
                {
                    errorLabel.setText("Invalid Port");
                    Toolkit.getDefaultToolkit().beep();
                }

                if (host != null && port != null)
                {
                    // System.err.println ("Setting http proxy: "+ host
                    // +":"+ port);
                    System.getProperties().put("proxySet", "true");
                    System.getProperties().put("proxyHost", host);
                    System.getProperties().put("proxyPort", port);
                    break;
                }
            }
        }

        if (iStream == null)
        {
            openerThread.interrupt();
        }

        return iStream;
    }

    private void startOpening(final URL url)
    {
        final WebAccessor webAccessor = this;
        openerThread = new Thread()
        {
            public void run()
            {
                iStream = null;
                try
                {
                    tryProxy = false;

                    URLConnection connection = url.openConnection();

                    if (connection instanceof HttpURLConnection)
                    {
                        HttpURLConnection htc = (HttpURLConnection) connection;
                        contentLength = htc.getContentLength();
                    }

                    //InputStream iii = echoSocket.getInputStream();
                    InputStream inputStream = connection.getInputStream();
                    iStream = new LoggedInputStream(inputStream, webAccessor); // just to make

                }
                catch (ConnectException x)
                { // could be an incorrect proxy
                    tryProxy = true;
                    exception = x;

                }
                catch (Exception x)
                {
                    // Exceptions that get here are considered cancels or
                    // missing
                    // pages, eg 401 if user finally cancels auth
                    exception = x;

                }
                finally
                {
                    // if dialog is in use, allow it to become visible /before/
                    // closing
                    // it, else on /fast/ connectinos, it may open later and
                    // hang!
                    if (dialog != null)
                    {
                        Thread.yield();
                        dialog.setVisible(false);
                    }
                }
            }
        };
        openerThread.start();
    }

    /**
     * Only to be called after an initial error has indicated a connection
     * problem
     */
    private JPanel getProxyPanel()
    {
        if (proxyPanel == null)
        {
            proxyPanel = new JPanel(new BorderLayout(5, 5));

            errorLabel = new JLabel();

            JPanel fields = new JPanel(new GridLayout(2, 2));
            String hostDefaultValue = (String) System.getProperties().get("proxyHost");
            String portDefaultValue = (String) System.getProperties().get("proxyPort");
            hostField = new JTextField(hostDefaultValue != null ? hostDefaultValue : "");
            portField = new JTextField(portDefaultValue != null ? portDefaultValue : "");
            JLabel host = new JLabel("Host: "); // TODO: i18n
            JLabel port = new JLabel("Port: "); // TODO: i18n
            fields.add(host);
            fields.add(hostField);
            fields.add(port);
            fields.add(portField);

            JLabel exampleLabel = new JLabel("e.g. host=\"gatekeeper.example.com\" port=\"80\"");

            proxyPanel.add(errorLabel, BorderLayout.NORTH);
            proxyPanel.add(fields, BorderLayout.CENTER);
            proxyPanel.add(exampleLabel, BorderLayout.SOUTH);
        }
        proxyPanel.validate();

        return proxyPanel;
    }

    private JPanel getPasswordPanel()
    {
        if (passwordPanel == null)
        {
            passwordPanel = new JPanel(new BorderLayout(5, 5));

            promptLabel = new JLabel();

            JPanel fields = new JPanel(new GridLayout(2, 2));
            nameField = new JTextField();
            passField = new JPasswordField();
            JLabel name = new JLabel("Name: "); // TODO: i18n
            JLabel pass = new JLabel("Password: "); // TODO: i18n
            fields.add(name);
            fields.add(nameField);
            fields.add(pass);
            fields.add(passField);

            passwordPanel.add(promptLabel, BorderLayout.NORTH);
            passwordPanel.add(fields, BorderLayout.CENTER);
        }
        passField.setText("");

        return passwordPanel;
    }

    /**
     * Authenticates via dialog when needed.
     */
    private class MyDialogAuthenticator extends Authenticator
    {

        public PasswordAuthentication getPasswordAuthentication()
        {
            // TODO: i18n
            JPanel panel = getPasswordPanel();
            String prompt = getRequestingPrompt();
            InetAddress addr = getRequestingSite();
            if (addr != null)
            {
                prompt += " (" + addr.getHostName() + ")";
            }
            promptLabel.setText(prompt);
            int result = JOptionPane.showConfirmDialog(parent, panel, "Enter Password", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result != JOptionPane.OK_OPTION)
            {
                return null;
            }

            return new PasswordAuthentication(nameField.getText(), passField.getPassword());
        }
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public int getContentLength()
    {
        return contentLength;
    }
}

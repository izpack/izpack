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
package com.izforge.izpack.gui;

import static com.izforge.izpack.api.handler.Prompt.Option.CANCEL;
import static com.izforge.izpack.api.handler.Prompt.Option.NO;
import static com.izforge.izpack.api.handler.Prompt.Option.OK;
import static com.izforge.izpack.api.handler.Prompt.Option.YES;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.izforge.izpack.api.handler.AbstractPrompt;

/**
 * Displays a dialog prompting users for a value or informs them of something.
 *
 * @author Tim Anderson
 */
public class GUIPrompt extends AbstractPrompt
{

    /**
     * The parent component. May be {@code null}.
     */
    private final JComponent parent;

    /**
     * Default JOptionPane OK button key.
     */
    private static final String OK_BUTTON = "OptionPane.okButtonText";

    /**
     * Default JOptionPane Cancel button key.
     */
    private static final String CANCEL_BUTTON = "OptionPane.cancelButtonText";

    /**
     * Default JOptionPane Yes button key.
     */
    private static final String YES_BUTTON = "OptionPane.yesButtonText";

    /**
     * Default JOptionPane No button key.
     */
    private static final String NO_BUTTON = "OptionPane.noButtonText";

    /**
     * Default JOptionPane Close button key.
     */
    private static final String CLOSE_BUTTON = "OptionPane.closeButtonText";

    /**
     * Default JOptionPane Show Details button key.
     */
    private static final String SHOW_DETAILS_BUTTON = "OptionPane.showDetailsButtonText";

    /**
     * Default JOptionPane Hide Details button key.
     */
    private static final String HIDE_DETAILS_BUTTON = "OptionPane.hideDetailsButtonText";

    /**
     * Default JOptionPane Copy button key.
     */
    private static final String COPY_BUTTON = "OptionPane.copyButtonText";

    /**
     * Default JOptionPane Send Report button key.
     */
    private static final String SEND_REPORT_BUTTON = "OptionPane.sendReportButtonText";

    /**
     * Default constructor.
     */
    public GUIPrompt()
    {
        this(null);
    }

    /**
     * Constructs a {@code GUIPrompt} with a parent component.
     *
     * @param parent the parent component. This determines the {@code Frame} in
     * which the dialog is displayed; if {@code null} or if the {@code parent}
     * has no {@code Frame}, a default {@code Frame} is used
     */
    public GUIPrompt(JComponent parent)
    {
        this.parent = parent;
    }

    @Override
    public void message(Type type, String title, String message, Throwable throwable)
    {
        if (title == null)
        {
            title = getTitle(type);
        }
        showMessageDialog(getMessageType(type), title, message, null, throwable);
    }

    /**
     * Displays a confirmation message.
     *
     * @param type the type of the message
     * @param title the message title. May be {@code null}
     * @param message the message
     * @param options the options which may be selected
     * @return the selected option
     */
    @Override
    public Option confirm(Type type, String title, final String message, Options options, Option defaultOption)
    {
        final int messageType = getMessageType(type);
        final int optionType;
        switch (options)
        {
            case OK_CANCEL:
                optionType = JOptionPane.OK_CANCEL_OPTION;
                break;
            case YES_NO_CANCEL:
                optionType = JOptionPane.YES_NO_CANCEL_OPTION;
                break;
            default:
                optionType = JOptionPane.YES_NO_OPTION;
                break;
        }
        if (title == null)
        {
            title = getTitle(type);
        }
        int selected;
        if (defaultOption == null)
        {
            selected = showConfirmDialog(messageType, title, message, optionType);
        }
        else
        {
            // jump through some hoops to select the default option
            List<Object> opts = new ArrayList<Object>();
            Object initialValue;
            switch (optionType)
            {
                case JOptionPane.OK_CANCEL_OPTION:
                {
                    String ok = UIManager.getString(OK_BUTTON);
                    String cancel = UIManager.getString(CANCEL_BUTTON);
                    opts.add(ok);
                    opts.add(cancel);
                    initialValue = (defaultOption == OK) ? ok : (defaultOption == CANCEL) ? cancel : null;
                    break;
                }
                case JOptionPane.YES_NO_OPTION:
                {
                    String yes = UIManager.getString(YES_BUTTON);
                    String no = UIManager.getString(NO_BUTTON);
                    opts.add(yes);
                    opts.add(no);
                    initialValue = (defaultOption == YES) ? yes : (defaultOption == NO) ? no : null;
                    break;
                }
                case JOptionPane.YES_NO_CANCEL_OPTION:
                {
                    String yes = UIManager.getString(YES_BUTTON);
                    String no = UIManager.getString(NO_BUTTON);
                    opts.add(yes);
                    opts.add(no);
                    String cancel = UIManager.getString(CANCEL_BUTTON);
                    initialValue = (defaultOption == YES) ? yes : (defaultOption == NO) ? no
                            : (defaultOption == CANCEL) ? cancel : null;
                    break;
                }
                default:
                    initialValue = null;
                    break;
            }
            selected = showOptionDialog(messageType, title, message, optionType, opts, initialValue);
        }

        return getSelected(options, selected);
    }

    /**
     * Maps a {@link Type} to a JOptionPane message type.
     *
     * @param type the message type
     * @return the JOptionPane equivalent
     */
    private int getMessageType(Type type)
    {
        int result;
        switch (type)
        {
            case INFORMATION:
                result = JOptionPane.INFORMATION_MESSAGE;
                break;
            case WARNING:
                result = JOptionPane.WARNING_MESSAGE;
                break;
            case QUESTION:
                result = JOptionPane.QUESTION_MESSAGE;
                break;
            default:
                result = JOptionPane.ERROR_MESSAGE;
                break;
        }
        return result;
    }

    /**
     * Display details about throwable in a simple modal dialog.
     *
     * @param throwable
     * @param title the title of the dialog box.
     * @param submissionURL if not null, allow the user to report the exception
     * to that URL
     * @param component the "owner" of the dialog, and may be null for
     * non-graphical applications.
     */
    public void showMessageDialog(final int type, final String title, final String message,
            final String submissionURL,
            final Throwable throwable)
    {
        final List<Object> buttons = new ArrayList<Object>();
        String throwMessage = null;
        final JButton detailsButton = new JButton(UIManager.getString(SHOW_DETAILS_BUTTON));;
        final JButton copyButton = new JButton(UIManager.getString(COPY_BUTTON));;
        if (throwable != null)
        {
            throwMessage = throwable.getMessage();
            buttons.add(detailsButton);
            buttons.add(copyButton);
        }
        final String basicMessage = ((message != null) ? message : ((throwMessage != null) ? throwMessage : UIManager.getString("installer.errorMessage")));

        Font font = UIManager.getFont("OptionPane.font");
        AffineTransform at = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(at, true, true);
        final int basicMessageWidth = (int) font.getStringBounds(basicMessage, frc).getWidth();
        final JPanel topPanel = new JPanel();
        final JLabel messageLabel = new JLabel();
        messageLabel.setText(basicMessage);
        if (basicMessageWidth > 700)
        {
            messageLabel.setText(wrapHtml(basicMessage));
            messageLabel.setSize(new Dimension(700, 10)); // add small height so that preferred size is filled correctly
            messageLabel.setPreferredSize(new Dimension(700, messageLabel.getPreferredSize().height));
        }
        else
        {
            messageLabel.setText(basicMessage);
        }
        topPanel.add(messageLabel);

        final JPanel centerPanel = new JPanel();
        centerPanel.setSize(new Dimension(420, 300));
        final JEditorPane exceptionPane = new JEditorPane();
        exceptionPane.setEditable(false);
        exceptionPane.setContentType("text/html");
        exceptionPane.setText(getHTMLDetails(throwable));
        final JScrollPane exceptionScrollPane = new JScrollPane(exceptionPane);
        exceptionScrollPane.setPreferredSize(new Dimension(470, 300));
        centerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        centerPanel.add(exceptionScrollPane);
        centerPanel.setVisible(false);

        final JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());
        jPanel.add(topPanel, BorderLayout.NORTH);
        jPanel.add(centerPanel, BorderLayout.CENTER);

        final JButton reportButton = new JButton(UIManager.getString(SEND_REPORT_BUTTON));
        if (submissionURL != null)
        {
            buttons.add(reportButton);
        }
        buttons.add(UIManager.getString(CLOSE_BUTTON));
        JOptionPane pane = new JOptionPane(jPanel, type,
                JOptionPane.YES_NO_OPTION, null,
                buttons.toArray());
        final JDialog dialog = pane.createDialog(parent, title);
        if (throwable != null)
        {
            // event handler for the Details button
            detailsButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent event)
                {
                    // Show or hide error details based on state of button
                    String label = detailsButton.getText();
                    if (label.startsWith(UIManager.getString(SHOW_DETAILS_BUTTON)))
                    {
                        if (basicMessageWidth > 700)
                        {
                            messageLabel.setText(wrapHtml(basicMessage));
                        }
                        else
                        {
                            messageLabel.setText(basicMessage);
                        }
                        centerPanel.setVisible(true);
                        detailsButton.setText(UIManager.getString(HIDE_DETAILS_BUTTON));
                        dialog.pack(); // resize dialog to fit details
                    }
                    else
                    {
                        if (basicMessageWidth > 700)
                        {
                            messageLabel.setText(wrapHtml(basicMessage));
                        }
                        else
                        {
                            messageLabel.setText(basicMessage);
                        }
                        centerPanel.setVisible(false);
                        detailsButton.setText(UIManager.getString(SHOW_DETAILS_BUTTON));
                        dialog.pack();
                    }
                }
            });
            // event handler for the Details button
            copyButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent event)
                {
                    // select and copy stacktrace to system clipboard
                    exceptionPane.selectAll();
                    exceptionPane.copy();
                }
            });
        }
        if (reportButton != null)
        {
            // Event handler for the "Report" button.
            reportButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent event)
                {
                    try
                    {
                        // Report the error, get response. See below.
                        String response = reportThrowable(throwable, submissionURL);
                        // Tell the user about the report
                        messageLabel.setText("<html>Error reported to:<pre>" + submissionURL
                                + "</pre>Server responds:<p>" + response + "</html>");
                        dialog.pack(); // Resize dialog to fit new message
                        // Don't allow it to be reported again
                        reportButton.setText("Error Reported");
                        reportButton.setEnabled(false);
                    }
                    catch (IOException e)
                    { // If error reporting fails
                        messageLabel.setText("Error not reported: " + e);
                        dialog.pack();
                    }
                }
            });
        }

        // Display the dialog modally. This method will return only when the
        // user clicks the "Exit" button of the JOptionPane.
        dialog.setVisible(true);
    }

    /**
     * Return an HTML-formatted stack trace for the specified Throwable,
     * including any exceptions chained to the exception. Note the use of the
     * Java 1.4 StackTraceElement to get stack details. The returned string
     * begins with "<html>" and is therefore suitable for display in Swing
     * components such as JLabel.
     */
    private static String getHTMLDetails(Throwable throwable)
    {
        StringBuffer b = new StringBuffer("<html>");
        int lengthOfLastTrace = 1; // initial value
        // Start with the specified throwable and loop through the chain of
        // causality for the throwable.
        while (throwable != null)
        {
            // Output Exception name and message, and begin a list
            b.append("<b>" + throwable.getClass().getName() + "</b>: " + throwable.getMessage()
                    + "<ul>");
            // Get the stack trace and output each frame.
            // Be careful not to repeat stack frames that were already reported
            // for the exception that this one caused.
            StackTraceElement[] stack = throwable.getStackTrace();
            for (int i = stack.length - lengthOfLastTrace; i >= 0; i--)
            {
                b.append("<li> in " + stack[i].getClassName() + ".<b>" + stack[i].getMethodName()
                        + "</b>() at <tt>" + stack[i].getFileName() + ":"
                        + stack[i].getLineNumber() + "</tt>");
            }
            b.append("</ul>"); // end list
            // See if there is a cause for this exception
            throwable = throwable.getCause();
            if (throwable != null)
            {
                // If so, output a header
                b.append("<i>Caused by: </i>");
                // And remember how many frames to skip in the stack trace
                // of the cause exception
                lengthOfLastTrace = stack.length;
            }
        }
        b.append("</html>");
        return b.toString();
    }

    /**
     * Serialize the specified Throwable, and use an HttpURLConnection to POST
     * it to the specified URL. Return the response of the web server.
     */
    private static String reportThrowable(Throwable throwable, String submissionURL)
            throws IOException
    {
        URL url = new URL(submissionURL); // Parse the URL
        URLConnection c = url.openConnection(); // Open unconnected Connection
        c.setDoOutput(true);
        c.setDoInput(true);
        // Tell the server what kind of data we're sending
        c.addRequestProperty("Content-type", "application/x-java-serialized-object");

        // This code might work for other URL protocols, but it is intended
        // for HTTP. We use a POST request to send data with the request.
        if (c instanceof HttpURLConnection)
        {
            ((HttpURLConnection) c).setRequestMethod("POST");
        }

        c.connect(); // Now connect to the server

        // Get a stream to write to the server from the URLConnection.
        // Wrap an ObjectOutputStream around it and serialize the Throwable.
        ObjectOutputStream out = new ObjectOutputStream(c.getOutputStream());
        out.writeObject(throwable);
        out.close();

        // Now get the response from the URLConnection. We expect it to be
        // an InputStream from which we read the server's response.
        Object response = c.getContent();
        StringBuffer message = new StringBuffer();
        if (response instanceof InputStream)
        {
            BufferedReader in = new BufferedReader(new InputStreamReader((InputStream) response));
            String line;
            while ((line = in.readLine()) != null)
            {
                message.append(line);
            }
        }
        return message.toString();
    }

    /**
     * Displays an option dialog, ensuring that it is displayed from the event
     * dispatch thread.
     *
     * @param type the dialog type
     * @param title the title
     * @param message the message
     * @param optionType the option type
     * @param opts the options
     * @param initialValue the initial value
     * @return the selected option
     */
    private int showOptionDialog(final int type, final String title, final String message, final int optionType,
            final List<Object> opts, final Object initialValue)
    {
        int selected;
        if (SwingUtilities.isEventDispatchThread())
        {
            selected = JOptionPane.showOptionDialog(parent, message, title, optionType, type, null,
                    opts.toArray(), initialValue);
        }
        else
        {
            final int[] handle = new int[1];
            try
            {
                SwingUtilities.invokeAndWait(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        handle[0] = JOptionPane.showOptionDialog(parent, message, title, optionType, type,
                                null, opts.toArray(), initialValue);
                    }
                });
            }
            catch (Throwable exception)
            {
                throw new IllegalStateException(exception);
            }
            selected = handle[0];
        }
        return selected;
    }

    /**
     * Displays an option dialog, ensuring that it is displayed from the event
     * dispatch thread.
     *
     * @param type the dialog type
     * @param title the title
     * @param message the message
     * @param optionType the option type
     * @return the selected option
     */
    private int showConfirmDialog(final int type, final String title, final String message, final int optionType)
    {
        int selected;
        if (SwingUtilities.isEventDispatchThread())
        {
            selected = JOptionPane.showConfirmDialog(parent, message, title, optionType, type);
        }
        else
        {
            final int[] handle = new int[1];
            try
            {
                SwingUtilities.invokeAndWait(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        handle[0] = JOptionPane.showConfirmDialog(parent, message, title, optionType, type);
                    }
                });
            }
            catch (Throwable exception)
            {
                throw new IllegalStateException(exception);
            }
            selected = handle[0];
        }
        return selected;
    }

    /**
     * Maps a {@code JOptionPane} selection to an {@link Option}.
     *
     * @param options the options
     * @param selected the selected value
     * @return the corresponding {@link Option}
     */
    private Option getSelected(Options options, int selected)
    {
        Option result;
        switch (selected)
        {
            case JOptionPane.YES_OPTION:
                result = (options == Options.OK_CANCEL) ? OK : YES;
                break;
            case JOptionPane.NO_OPTION:
                result = NO;
                break;
            case JOptionPane.CANCEL_OPTION:
                result = CANCEL;
                break;
            default:
                result = (options == Options.YES_NO_CANCEL) ? CANCEL : NO;
        }
        return result;
    }

    private String wrapHtml(String string)
    {
        return "<html><body><div style='float:left; width:540px;'>" + string + "</div></body></html>";
    }

    // A test program to demonstrate the class
    public static class Test
    {

        public static void main(String[] args)
        {
            UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
            UIManager.put("OptionPane.closeButtonText", "Close");
            UIManager.put("OptionPane.showDetailsButtonText", "Show Details");
            UIManager.put("OptionPane.hideDetailsButtonText", "Hide Details");
            UIManager.put("OptionPane.copyButtonText", "Copy");
            UIManager.put("OptionPane.sendReportButtonText", "Send Report");
            String url = (args.length > 0) ? args[0] : null;
            try
            {
                foo();
            }
            catch (Throwable e)
            {
                new GUIPrompt().showMessageDialog(JOptionPane.ERROR_MESSAGE, "Fatal Error", "A critical error occured", url, e);
                System.exit(1);
            }
        }

        // These methods purposely throw an exception
        public static void foo()
        {
            bar(null);
        }

        public static void bar(Object o)
        {
            try
            {
                blah(o);
            }
            catch (NullPointerException e)
            {
                // Catch the null pointer exception and throw a new exception
                // that has the NPE specified as its cause.
                throw (IllegalArgumentException) new IllegalArgumentException("null argument")
                        .initCause(e);
            }
        }

        public static void blah(Object o)
        {
            Class c = o.getClass(); // throws NPE if o is null
        }
    }
}

/*
/*
 * $Id:$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://www.izforge.com/izpack/
 * http://izpack.codehaus.org/
 *
 * Copyright 2006 Elmar Grom
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

package com.izforge.izpack.gui.log;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormatSymbols;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;

/*---------------------------------------------------------------------------*/

/**
 * This class handles informing the user about unusual events during the installation process and
 * therefore about the possibility that the installation may not have succeeded at all or may have
 * succeeded only partially.
 * <p/>
 * <h1>Usage</h1>
 * To add a message to the install log call one of the <code>addMessage()</code> methods, using
 * the appropriate message index. If the message contains placeholders for variables, provide a
 * <code>String</code> array that supplies the necessary variable text. Calling
 * <code>addMessage()</code> will add an informative message to the log. These may be used to
 * indicate actions taken by the installer throughout the progression of the install. Calling
 * <code>addWarning()</code> will not only add the requested warning message to the log but also
 * cause the user to be alerted about the fact that the install might not have succeeded completely.
 * <code>addError()</code> goes one step further, by alerting the user that the installation has
 * failed.
 * <p/>
 * <h1>Adding Messages</h1>
 * Messages are divided into three categories:
 * <ol>
 * <li>informative/general messages
 * <li>warning messages
 * <li>error messages
 * </ol>
 * To add a message, define the text resource in the language packs. Then add a new constant for the
 * message, along with a brief description of the purpose of the message. If the message contains
 * place holders for variable text add an ordered list to the description, that lists all variables
 * in proper order, so that other programmers have no difficulty to form a correct call for the
 * message. The constants are defined in the individual interfaces <code>LogMessage</code>,
 * <code>LogWarning</code> and <code>LogError</code>.
 * <p/>
 * To derive a correct integer value for the message index add a new value to either
 * <code>MESSAGE_BASE</code>, <code>WARNING_BASE</code> or <code>ERROR_BASE</code>,
 * depending on the message category. Next, increment the MAX_ constant for the message category, to
 * ensure that the add... methods actually allow the message to be added. The key for the text
 * resource must be named either <code>log.message_</code>, <code>log.warning_</code> or
 * <code>log.error_</code> in accordance with the chosen index base. In addition, the key must be
 * appended by the message index (without the base, since this implementation will automatically
 * subtract the base). Variable place holders must conform to the specification for
 * <code>java.text.MessageFormat</code>.
 * <p/>
 * <h1>Debug Messages</h1>
 * The output of debug messages is controlled through system properties. These properties may be set
 * using the -D command line option. Please note, that the -D option is a command line switch for
 * the VM, not for IzPack. In order for this to work, these options must be listed on the command
 * line before IzPack!
 * <p/>
 * <h2>Turning Debug On</h2>
 * In order to receive debug output, it is necessary to turn this feature on explicitely. This is
 * done with the command line otprion:
 * <p/>
 * <pre>
 * -DIzPack.debug=on
 * </pre>
 * <p/>
 * <h2>Selecting Debug Channels</h2>
 * Setting the list of specific debug channels to trace is accomplished with the following command
 * line option:
 * <p/>
 * <pre>
 * -DIzPack.debug.channel=&lt;channelA,channelB,...&gt;
 * </pre>
 * <p/>
 * The parameter is a comma separated list of one or more channel identifiers.
 * <p/>
 * <h2>Dumping a List of Debug Channels</h2>
 * <p/>
 * <pre>
 * -DIzPack.debug.dumpList=on
 * </pre>
 * <p/>
 * To turn debug messages on
 *
 * @author Elmar Grom
 * @version 0.0.1 / 11/20/06
 */
/*---------------------------------------------------------------------------*/

public class Log implements LogError, LogWarning, LogMessage
{

    // --------------------------------------------------------------------------
    // Constant Definitions
    // --------------------------------------------------------------------------
    /**
     * The prefix for all text resources related to this class
     */
    private static final String RESOURCE_PREFIX = "log.";

    /**
     * The formatting used for the report time stamp
     */
    private static final String DATE_FORMAT = RESOURCE_PREFIX + "timeStamp";

    /**
     * The prefix for building message keys
     */
    private static final String MESSAGE_PREFIX = RESOURCE_PREFIX + "message_";

    /**
     * The prefix for building warning message keys
     */
    private static final String WARNING_PREFIX = RESOURCE_PREFIX + "warning_";

    /**
     * The prefix for building error message keys
     */
    private static final String ERROR_PREFIX = RESOURCE_PREFIX + "error_";

    /**
     * System property to turn debug output on
     */
    private static final String DEBUG_SWITCH = "IzPack.debug";

    /**
     * System property to set the debug channels to trace
     */
    private static final String CHANNEL_SPEC = "IzPack.debug.channel";

    /**
     * System property to enable dumping of the list of debug channels that did record messages
     */
    private static final String CHANNEL_LIST = "IzPack.debug.dumpList";

    // --------------------------------------------------------------------------
    // Variable Declarations
    // --------------------------------------------------------------------------

    /**
     * The system dependent newline character sequence
     */
    private String newline = System.getProperty("line.separator");

    /**
     * Access to the installation information and the localized text resources
     */
    private InstallData installData = null;

    /**
     * The collection of installation messages
     */
    private ArrayList<Record> messages = new ArrayList<Record>();

    /**
     * The collection of warning messages
     */
    private ArrayList<Record> warnings = new ArrayList<Record>();

    /**
     * The collection of error messages
     */
    private ArrayList<Record> errors = new ArrayList<Record>();

    /**
     * The collection of debug messages
     */
    private ArrayList<Record> debug = new ArrayList<Record>();

    /**
     * The list of channels requested for debug output. A <code>Vector</code> must be used for
     * this purpose, since this is the only class that explicitly specifies that the
     * <code>equals()</code> method is used for determining if a particular object is contained.
     */
    private List<String> channels = null;

    /**
     * This map keeps track of all channels that are recorded. It is used for information purposes.
     */
    private Map<String, String> recordedChannels = null;

    /**
     * This flag signals if debug messages should be recorded
     */
    private boolean debugActive = false;

    /**
     * This flag signals that the identifiers of the recorded debug channels should be dumped
     */
    private boolean dumpChannels = false;
    /**
     * Channel name for debug message about panel tracing.
     */
    public final static String PANEL_TRACE = "PanelTrace";

    /*--------------------------------------------------------------------------*/

    /**
     * Constructs a <tt>Log</tt>.
     *
     * @param installData the installation data
     */
    public Log(InstallData installData)
    {
        this.installData = installData;

        // ----------------------------------------------------
        // get the debug setting
        // ----------------------------------------------------
        String temp = System.getProperty(DEBUG_SWITCH);
        if ((temp != null) && (temp.toUpperCase().equals("ON")))
        {
            debugActive = true;
        }

        if (debugActive)
        {
            // ----------------------------------------------------
            // get the list of debug channels requested
            // ----------------------------------------------------
            recordedChannels = new HashMap<String, String>();
            channels = new ArrayList<String>();
            temp = System.getProperty(CHANNEL_LIST);
            if ((temp != null) && (temp.toUpperCase().equals("ON")))
            {
                dumpChannels = true;
            }

            // ----------------------------------------------------
            // get the list of debug channels requested
            // ----------------------------------------------------
            temp = System.getProperty(CHANNEL_SPEC);
            if (temp != null)
            {
                String[] channelList = temp.split(",");

                channels.addAll(Arrays.asList(channelList));
            }
        }
    }

    /**
     * This method records general installation message
     *
     * @param message the numeric identifier of the message to add, as defined in
     *                {@link LogMessage <code>LogMessage</code>}
     * @param detail  a string array of variable fields that should be inserted into the message text
     */
    /*--------------------------------------------------------------------------*/
    public void addMessage(int message, String[] detail)
    {
        if ((message >= LogMessage.MESSAGE_BASE) && (message < LogMessage.MAX_MESSAGE))
        {
            messages.add(new Record(message, detail));
        }
    }

    /*--------------------------------------------------------------------------*/

    /**
     * This method records a general installation message, using a custom text template. It allows
     * cusom code to insert message text that is not defined in IzPack. IzPack internal code should
     * use the parallel version based on the message index.
     *
     * @param template the basic template of the message
     * @param detail   a string array of variable fields that should be inserted into the message
     *                 text. Each array element will be inserted into the text template, replacing a marker.
     * @see java.text.MessageFormat#format(java.lang.String, java.lang.Object[])
     */
    /*--------------------------------------------------------------------------*/
    public void addCustomMessage(String template, String[] detail)
    {
        messages.add(new Record(template, detail));
    }

    /*--------------------------------------------------------------------------*/

    /**
     * This method records a warning message to the list of messages
     *
     * @param message   the numeric identifier of the message to add, as defined in
     *                  {@link LogWarning <code>LogWarning</code>}
     * @param detail    a string array of variable fields that should be inserted into the message
     *                  text. Each array element will be inserted into the text template, replacing a marker.
     * @param exception the exception associated with the event or <code>null</code> if there was
     *                  none.
     * @see java.text.MessageFormat#format(java.lang.String, java.lang.Object[])
     */
    /*--------------------------------------------------------------------------*/
    public void addWarning(int message, String[] detail, Throwable exception)
    {
        if ((message >= LogWarning.WARNING_BASE) && (message < LogWarning.MAX_WARNING))
        {
            warnings.add(new Record(message, detail, exception));
        }
    }

    /*--------------------------------------------------------------------------*/

    /**
     * This method records a warning message, using a custom text template. It allows cusom code to
     * insert message text that is not defined in IzPack. IzPack internal code should use the
     * parallel version based on the message index.
     *
     * @param template  the basic template for the message
     * @param detail    a string array of variable fields that should be inserted into the message
     *                  text. Each array element will be inserted into the text template, replacing a marker.
     * @param exception the exception associated with the event or <code>null</code> if there was
     *                  none.
     * @see java.text.MessageFormat#format(java.lang.String, java.lang.Object[])
     */
    /*--------------------------------------------------------------------------*/
    public void addCustomWarning(String template, String[] detail, Throwable exception)
    {
        warnings.add(new Record(template, detail, exception));
    }

    /*--------------------------------------------------------------------------*/

    /**
     * This method records an error message to the list of messages
     *
     * @param message   the numeric identifier of the message to add, as defined in
     *                  {@link LogError <code>LogError</code>}
     * @param detail    a string array of variable fields that should be inserted into the message
     *                  text. Each array element will be inserted into the text template, replacing a marker.
     * @param exception the exception associated with the event or <code>null</code> if there was
     *                  none.
     * @see java.text.MessageFormat#format(java.lang.String, java.lang.Object[])
     */
    /*--------------------------------------------------------------------------*/
    public void addError(int message, String[] detail, Throwable exception)
    {
        if ((message >= LogError.ERROR_BASE) && (message < LogError.MAX_ERROR))
        {
            errors.add(new Record(message, detail, exception));
            installData.setInstallSuccess(false);
        }
    }

    /*--------------------------------------------------------------------------*/

    /**
     * This method records an error message, using a custom text template. It allows cusom code to
     * insert message text that is not defined in IzPack. IzPack internal code should use the
     * parallel version based on the message index.
     *
     * @param template  the basic template for the message
     * @param detail    a string array of variable fields that should be inserted into the message
     *                  text. Each array element will be inserted into the text template, replacing a marker.
     * @param exception the exception associated with the event or <code>null</code> if there was
     *                  none.
     * @see java.text.MessageFormat#format(java.lang.String, java.lang.Object[])
     */
    /*--------------------------------------------------------------------------*/
    public void addCustomError(String template, String[] detail, Throwable exception)
    {
        errors.add(new Record(template, detail, exception));
    }

    /*--------------------------------------------------------------------------*/

    /**
     * This method provides a channel for debugging messages in IzPack development. Note that debug
     * messages are used solely for this purpose. Adding debug messages does not trigger user
     * notification. If the user should be notified about a specific situation, please also call the
     * appropriate (message, warning, error) method. Debug messages are not localized, please use
     * English only.
     * <p/>
     * In order to prevent flooding developers with messages that are generally of no interest, each
     * message may be associated with a specific channel. A message is associated with a channel
     * simply by providing a channel identifier as call parameter. There is no need for registering
     * channels beforehand. If <code>null</code> or an empty string is used as channel identifier
     * the message will be output, regardless of the channel filter applied. Please use this option
     * sparingly, just for really impotant messages of general interest.
     * <p/>
     * To receive output for select channels, start IzPack with the command line option
     * -DIzPack.debug.channel=, followed by a comma separated list of channel identifiers.
     *
     * @param template  the basic template for the message
     * @param detail    a string array of variable fields that should be inserted into the message
     *                  text. Each array element will be inserted into the text template, replacing a marker.
     * @param channel   the debug channel the message is associated with.
     * @param exception the exception associated with the event or <code>null</code> if there was
     *                  none.
     */
    /*--------------------------------------------------------------------------*/
    public void addDebugMessage(String template, String[] detail, String channel,
                                Throwable exception)
    {
        if (debugActive)
        {
            recordedChannels.put(channel, channel);

            if ((channel == null) || (channel.length() == 0) || channels.contains(channel))
            {
                Record record = new Record(template, detail, exception, channel);

                debug.add(record);
                System.out.println(buildDebug(record));
            }
        }
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Reports if any messages have been recorded.
     *
     * @return true if any messages have been recorded
     */
    /*--------------------------------------------------------------------------*/
    public boolean messagesRecorded()
    {
        return (!messages.isEmpty());
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Reports if any warnings have been recorded.
     *
     * @return true if any warnings have been recorded
     */
    /*--------------------------------------------------------------------------*/
    public boolean warningsRecorded()
    {
        return (!warnings.isEmpty());
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Reports if any errors have been recorded.
     *
     * @return true if any errors have been recorded
     */
    /*--------------------------------------------------------------------------*/
    public boolean errorsRecorded()
    {
        return (!errors.isEmpty());
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Displays a dialog that informs the user about the fact that one or more unusual events have
     * occurred during installation. If nothing has been recorded, this method returns immediately.
     */
    /*--------------------------------------------------------------------------*/
    public void informUser()
    {
        String message;
        int messageType;

        // ----------------------------------------------------
        // fins out if there are any warnings or errors,
        // otherwise return with no action
        // ----------------------------------------------------
        if (errorsRecorded())
        {
            messageType = JOptionPane.ERROR_MESSAGE;
            message = format(RESOURCE_PREFIX + "informUserFail");
        }
        else if (warningsRecorded())
        {
            messageType = JOptionPane.WARNING_MESSAGE;
            message = format(RESOURCE_PREFIX + "informUserPartial");
        }
        else
        {
            return;
        }

        // ----------------------------------------------------
        // present the warning message
        // ----------------------------------------------------
        int userChoice = JOptionPane.showConfirmDialog(null, message, format(RESOURCE_PREFIX + "informUserTitle"),
                                                       JOptionPane.YES_NO_OPTION, messageType);

        // ----------------------------------------------------
        // if the user has elected to write the report, present
        // the file selection dialog.
        // ----------------------------------------------------
        if (userChoice == JOptionPane.OK_OPTION)
        {
            writeReport();
        }
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Presents the user with a dialog to select a file and location for the installation report and
     * writes a report contianing all messages to the user selected file.
     */
    /*--------------------------------------------------------------------------*/
    public void writeReport()
    {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(format(RESOURCE_PREFIX + "saveLogTitle"));
        fileChooser.setSelectedFile(new File(format(RESOURCE_PREFIX + "LogFileName")));
        int choice = fileChooser.showSaveDialog(null);

        if (choice == JFileChooser.APPROVE_OPTION)
        {
            writeReport(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Writes a report contianing all messages to the indicated file.
     *
     * @param file the fully qualifies name of the file to write to.
     */
    /*--------------------------------------------------------------------------*/
    public void writeReport(String file)
    {
        try
        {
            FileWriter writer = new FileWriter(file);
            String text = compileReport();

            writer.write(text, 0, text.length());

            writer.flush();
            writer.close();
        }
        catch (Throwable exception)
        {
            try
            {
                JOptionPane.showMessageDialog(null, format((RESOURCE_PREFIX + "reportWriteError"), file),
                                              format(RESOURCE_PREFIX + "reportWriteErrorTitle"),
                                              JOptionPane.ERROR_MESSAGE);
            }
            catch (Throwable exception2)
            {
                // What todo if writing log report fails?? We can ignore
                // or write it exceptionally on stderr.
                exception2.printStackTrace();
            }
        }
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Prepares an installation report from the recorded information
     *
     * @return the installation report
     */
    /*--------------------------------------------------------------------------*/
    private String compileReport()
    {
        StringBuilder report = new StringBuilder();
        String dateFormat = format(DATE_FORMAT);
        int count;

        // ----------------------------------------------------
        // insert header
        // ----------------------------------------------------
        report.append("-------------------------------------------------------------------------------");
        report.append(newline);
        report.append(format(RESOURCE_PREFIX + "reportHeading"));
        report.append(newline);
        report.append("-------------------------------------------------------------------------------");
        report.append(newline);

        // ----------------------------------------------------
        // insert a general warning message if appropriate
        // ----------------------------------------------------
        if (errorsRecorded())
        {
            report.append(newline);
            report.append(format(RESOURCE_PREFIX + "installFailed"));
            report.append(newline);
        }
        else if (warningsRecorded())
        {
            report.append(newline);
            report.append(format(RESOURCE_PREFIX + "partialInstall"));
            report.append(newline);
        }

        // ----------------------------------------------------
        // insert general information about the installation
        // ----------------------------------------------------
        report.append(newline);
        report.append(format(RESOURCE_PREFIX + "messageCount", messages.size(), warnings.size(), errors.size()));
        report.append(newline);
        report.append(newline);

        report.append(format(RESOURCE_PREFIX + "application", installData.getInfo().getAppName(),
                             installData.getInfo().getAppVersion()));
        report.append(newline);
        report.append(format(RESOURCE_PREFIX + "timePrefix",
                             new SimpleDateFormat(dateFormat, new DateFormatSymbols()).format(new Date())));

        report.append(newline);
        report.append(format(RESOURCE_PREFIX + "pathPrefix", installData.getInstallPath()));
        report.append(newline);

        // ----------------------------------------------------
        // insert the recorded mnessages
        // ----------------------------------------------------
        if (messagesRecorded())
        {
            report.append(newline);
            report.append(format(RESOURCE_PREFIX + "messageHeading"));
            report.append(newline);
            report.append(newline);

            count = messages.size();

            for (int i = 0; i < count; i++)
            {
                report.append(buildMessage(i));
            }
        }
        // ----------------------------------------------------
        // insert the recorded warning messages
        // ----------------------------------------------------
        if (warningsRecorded())
        {
            report.append(newline);
            report.append(format(RESOURCE_PREFIX + "warningHeading"));
            report.append(newline);
            report.append(newline);

            count = warnings.size();

            for (int i = 0; i < count; i++)
            {
                report.append(buildWarning(i));
            }
        }
        // ----------------------------------------------------
        // insert the recorded error messages
        // ----------------------------------------------------
        if (errorsRecorded())
        {
            report.append(newline);
            report.append(format(RESOURCE_PREFIX + "errorHeading"));
            report.append(newline);
            report.append(newline);

            count = errors.size();

            for (int i = 0; i < count; i++)
            {
                report.append(buildError(i));
            }
        }

        // ----------------------------------------------------
        // insert the debug messages
        // ----------------------------------------------------
        if (debugActive)
        {
            report.append(newline);
            report.append(format(RESOURCE_PREFIX + "debugHeading"));
            report.append(newline);
            report.append(newline);

            count = errors.size();

            for (int i = 0; i < count; i++)
            {
                report.append(buildDebug(i));
            }
        }

        report
                .append("-------------------------------------------------------------------------------");
        report.append(newline);

        return (report.toString());
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Builds a general installation message from the information contained in the indicated record
     *
     * @param index the index of the requested message entry
     */
    /*--------------------------------------------------------------------------*/
    private String buildMessage(int index)
    {
        Record record = messages.get(index);
        StringBuilder message = new StringBuilder();

        // ----------------------------------------------------
        // append the message text
        // ----------------------------------------------------
        message.append(format(RESOURCE_PREFIX + "messagePrefix", index));
        if (record.message >= 0)
        {
            message.append(format(MESSAGE_PREFIX + record.message, (Object[]) record.variables));
        }
        else
        {
            message.append(MessageFormat.format(record.template, (Object[]) record.variables));
        }

        message.append(newline);
        return (message.toString());
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Builds a warning message from the information contained in the indicated warning record
     *
     * @param index the index of the requested warning entry
     */
    /*--------------------------------------------------------------------------*/
    private String buildWarning(int index)
    {
        Record record = warnings.get(index);
        StringBuilder message = new StringBuilder();

        // ----------------------------------------------------
        // append the message text
        // ----------------------------------------------------
        message.append(format(RESOURCE_PREFIX + "warningPrefix", index));

        if (record.message >= 0)
        {
            message.append(format(WARNING_PREFIX + Integer.toString(record.message - LogWarning.WARNING_BASE),
                                  (Object[]) record.variables));
        }
        else
        {
            message.append(MessageFormat.format(record.template, (Object[]) record.variables));
        }

        // ----------------------------------------------------
        // append the exception
        // ----------------------------------------------------
        if (record.exception != null)
        {
            message.append(newline);
            message.append(format(RESOURCE_PREFIX + "exceptionPrefix", record.exception));
        }

        message.append(newline);
        return (message.toString());
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Builds an error message from the information contained in the indicated error record
     *
     * @param index the index of the requested error entry
     */
    /*--------------------------------------------------------------------------*/
    private String buildError(int index)
    {
        Record record = errors.get(index);
        StringBuilder message = new StringBuilder();

        // ----------------------------------------------------
        // append the message text
        // ----------------------------------------------------
        message.append(format(RESOURCE_PREFIX + "errorPrefix", Integer.toString(index)));

        if (record.message >= 0)
        {
            message.append(format(ERROR_PREFIX + Integer.toString(record.message - LogError.ERROR_BASE),
                                  (Object[]) record.variables));
        }
        else
        {
            message.append(MessageFormat.format(record.template, (Object[]) record.variables));
        }

        // ----------------------------------------------------
        // append the exception
        // ----------------------------------------------------
        if (record.exception != null)
        {
            message.append(newline);
            message.append(format(RESOURCE_PREFIX + "exceptionPrefix", record.exception));
        }

        message.append(newline);
        return (message.toString());
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Builds a debug message from the information contained in the indicated debug record
     *
     * @param index the index of the requested debug entry
     */
    /*--------------------------------------------------------------------------*/
    private String buildDebug(int index)
    {
        Record record = debug.get(index);

        return (buildDebug(record));
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Builds a debug message from the information contained in a specific debug record
     *
     * @param record the debug record
     */
    /*--------------------------------------------------------------------------*/
    private String buildDebug(Record record)
    {
        StringBuilder message = new StringBuilder();

        // ----------------------------------------------------
        // append the message text
        // ----------------------------------------------------
        if ((record.channel == null) || (record.channel.length() == 0))
        {
            message.append("Debug - general: ");
        }
        else
        {
            message.append("Debug - ").append(record.channel).append(": ");
        }

        message.append(MessageFormat.format(record.template, (Object[]) record.variables));

        // ----------------------------------------------------
        // append the exception
        // ----------------------------------------------------
        if (record.exception != null)
        {
            message.append(newline);
            message.append(format(RESOURCE_PREFIX + "exceptionPrefix", record.exception));
        }

        message.append(newline);
        return (message.toString());
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Dumps the list of debug channels to stdout that have recorded messages
     */
    /*--------------------------------------------------------------------------*/
    public void dumpRecordedChannels()
    {
        if (debugActive && dumpChannels)
        {
            System.out.println();
            System.out.println("The following debug channels did record messages:");
            System.out.println();

            for (String channel : recordedChannels.keySet())
            {
                System.out.println(" - " + channel);
            }

            System.out.println();
        }
    }

    /**
     * Helper to format a localised message.
     *
     * @param id   the message id
     * @param args the format arguments
     * @return
     */
    private String format(String id, Object... args)
    {
        return installData.getMessages().get(id, args);
    }

    // --------------------------------------------------------------------------
    // class definition to handle individual entries
    // --------------------------------------------------------------------------

    private class Record
    {

        String channel;

        String template;

        int message;

        String[] variables;

        Throwable exception;

        // ----------------------------------------------------
        // Constructors
        // ----------------------------------------------------

        Record(int message, String[] variables)
        {
            this.message = message;
            this.variables = variables;
        }

        Record(String template, String[] variables)
        {
            this.message = -1;
            this.template = template;
            this.variables = variables;
        }

        Record(int message, String[] variables, Throwable exception)
        {
            this.message = message;
            this.variables = variables;
            this.exception = exception;
        }

        Record(String template, String[] variables, Throwable exception)
        {
            this.message = -1;
            this.template = template;
            this.variables = variables;
            this.exception = exception;
        }

        Record(String template, String[] variables, Throwable exception, String channel)
        {
            this.message = -1;
            this.template = template;
            this.variables = variables;
            this.exception = exception;
            this.channel = channel;
        }
    }
}
/*---------------------------------------------------------------------------*/

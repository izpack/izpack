package com.izforge.izpack.util;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import jline.Terminal;
import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;
import jline.console.completer.FileNameCompleter;
import jline.internal.Log;

/**
 * I/O streams to support prompting and keyboard input from the console.
 *
 * @author Tim Anderson
 */
public class Console
{
    private static final Logger logger = Logger.getLogger(Console.class.getName());

    private  static final java.io.Console console = System.console();

    /**
     * Console reader.
     */
    private ConsoleReader consoleReader;

    /**
     * Check if consoleReader failed to load.
     */
    private boolean consoleReaderFailed = false;

    /**
     * File name completer allows for tab completion on files and directories.
     */
    private final FileNameCompleter fileNameCompleter = new FileNameCompleter();

    /**
     * Constructs a <tt>Console</tt> with <tt>System.in</tt> and <tt>System.out</tt> as the I/O streams.
     */
    public Console()
    {
        try
        {
            Log.setOutput(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException
                {
                }
            }));
            this.consoleReader = new ConsoleReader("IzPack", new FileInputStream(FileDescriptor.in), System.out, null);
            this.consoleReader.setHandleUserInterrupt(true);
            Terminal terminal = consoleReader.getTerminal();
            if (terminal == null || terminal instanceof UnsupportedTerminal)
            {
                consoleReader.shutdown();
                throw new Throwable("Terminal not initialized");
            }
        }
        catch (Throwable t)
        {
            consoleReaderFailed = true;
            logger.log(Level.WARNING, "Cannot initialize the console reader. Default to regular input stream.", t);
        }

    }

    /**
     * Reads a line of text.  A line is considered to be terminated by any one
     * of a line feed ('\\n'), a carriage return ('\\r'), or a carriage return
     * followed immediately by a linefeed.
     *
     * @return a String containing the contents of the line, not including any line-termination characters, or
     *         null if the end of the stream has been reached
     * @throws IOException if an I/O error occurs
     */
    public String readLine() throws IOException
    {
        if (consoleReaderFailed)
        {
            return readLineDefaultInput();
        }
        else
        {
            return consoleReader.readLine();
        }
    }

    /**
     * Prints a message to the console.
     *
     * @param message the message to print
     */
    public void print(String message)
    {
        if (console != null)
        {
            console.printf("%s", message);
            console.flush();
        }
        else
        {
            // Fix tests
            System.out.print(message);
        }
    }

    /**
     * Prints a new line.
     */
    public void println()
    {
        if (console != null)
        {
            console.printf("\n");
        }
        else
        {
            // Fix UserInputConsoleTest
            System.out.println();
        }
    }

    /**
     * Prints a message to the console with a new line.
     *
     * @param message the message to print
     */
    public void println(String message)
    {
        if (console != null)
        {
            console.printf("%s\n", message);
        }
        else
        {
            // Fix tests
            System.out.println(message);
        }
    }

    /**
     * Displays a prompt and waits for numeric input.
     *
     * @param prompt the prompt to display
     * @param min    the minimum allowed value
     * @param max    the maximum allowed value
     * @param eof    the value to return if end of stream is reached
     * @return a value in the range of <tt>from..to</tt>, or <tt>eof</tt> if the end of stream is reached
     */
    public int prompt(String prompt, int min, int max, int eof)
    {
        return prompt(prompt, min, max, min - 1, eof);
    }

    /**
     * Displays a prompt and waits for numeric input.
     *
     * @param prompt       the prompt to display
     * @param min          the minimum allowed value
     * @param max          the maximum allowed value
     * @param defaultValue the default value to use, if no input is entered. Use a value {@code < min} if there is no
     *                     default
     * @param eof          the value to return if end of stream is reached
     * @return a value in the range of <tt>from..to</tt>, or <tt>eof</tt> if the end of stream is reached
     */
    public int prompt(String prompt, int min, int max, int defaultValue, int eof)
    {
        int result = min - 1;
        try
        {
            do
            {
                println(prompt);
                String value = readLine();
                if (value != null)
                {
                    value = value.trim();
                    if (value.equals("") && defaultValue >= min)
                    {
                        // use the default value
                        result = defaultValue;
                        break;
                    }
                    try
                    {
                        result = Integer.valueOf(value);
                    }
                    catch (NumberFormatException ignore)
                    {
                        // loop round to try again
                    }
                }
                else
                {
                    // end of stream
                    result = eof;
                    break;
                }
            }
            while (result < min || result > max);
        }
        catch (IOException e)
        {
            logger.log(Level.WARNING, e.getMessage(), e);
            result = eof;
        }
        return result;
    }

    /**
     * Displays a prompt and waits for input.
     * Allows auto completion of files and directories.
     * Except a path to a file or directory.
     * Ensure to expand the tilde character to the user's home directory.
     * If the input ends with a file separator we will trim it to keep consistency.
     *
     * @param prompt the prompt to display
     * @param eof the value to return if end of stream is reached
     * @return the input value or <tt>eof</tt> if the end of stream is reached
     */
    public String promptLocation(String prompt, String eof)
    {
        return promptLocation(prompt, "", eof);
    }

    /**
     * Displays a prompt and waits for input.
     * Allows auto completion of files and directories.
     * Except a path to a file or directory.
     * Ensure to expand the tilde character to the user's home directory.
     * If the input ends with a file separator we will trim it to keep consistency.
     * TODO: Perhaps have file separator at the end for directories and no file separator at the end for files
     *
     * @param prompt       the prompt to display
     * @param defaultValue the default value to use, if no input is entered
     * @param eof          the value to return if end of stream is reached
     * @return the input value or {@code eof} if the end of stream is reached
     */
    public String promptLocation(String prompt, String defaultValue, String eof)
    {
        if (consoleReaderFailed)
        {
            return prompt(prompt, defaultValue, eof);
        }
        String result;
        consoleReader.addCompleter(fileNameCompleter);

        println(prompt);
        try
        {
            while ((result = consoleReader.readLine().trim()) != null)
            {
                if (result.startsWith("~"))
                {
                    result = result.replace("~", System.getProperty("user.home"));
                }
                if (result.endsWith(File.separator) && result.length() > 1)
                {
                    result = result.substring(0, result.length()-1);
                }
                if (result.isEmpty())
                {
                    result = defaultValue;
                }
                break;
            }
        }
        catch (IOException e)
        {
            result = eof;
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        finally
        {
            consoleReader.removeCompleter(fileNameCompleter);
        }

        return result;
    }

    /**
     * Displays a prompt and waits for input.
     * Expects a password, characters with be mased with the echoCharacter "*"
     * @param prompt the prompt to display
     * @param eof    the value to return if end of stream is reached
     * @return the input value or <tt>eof</tt> if the end of stream is reached
     */
    public String promptPassword(String prompt, String eof)
    {
        return promptPassword(prompt, "", eof);
    }

    /**
     * Displays a prompt and waits for input.
     * Expects a password, characters with be mased with the echoCharacter "*"
     *
     * @param prompt       the prompt to display
     * @param defaultValue the default value to use, if no input is entered
     * @param eof          the value to return if end of stream is reached
     * @return the input value or {@code eof} if the end of stream is reached
     */
    public String promptPassword(String prompt, String defaultValue, String eof)
    {
        if (consoleReaderFailed)
        {
            char[] passwd;
            try
            {
                passwd = readPasswordDefaultInput(defaultValue, "%s\n", prompt);
                return new String(passwd);
            }
            catch (IOException e)
            {
                return defaultValue;
            }
        }

        int ch;
        String result = "";

        String backspace = "\b \b";
        String echoCharacter = "*";
        StringBuilder stringBuilder = new StringBuilder();

        println(prompt);
        boolean submitted = false;
        try
        {
            while(!submitted)
            {
                switch (ch = consoleReader.readCharacter())
                {
                    case -1:
                    case '\n':
                    case '\r':
                        println("");
                        result = stringBuilder.toString();
                        submitted = true;
                        break;
                    case KeyEvent.VK_BACK_SPACE:
                    case KeyEvent.VK_DELETE:
                        if (stringBuilder.length() > 0)
                        {
                            print(backspace);
                            stringBuilder.setLength(stringBuilder.length() - 1);
                        }
                        break;
                    default:
                        print(echoCharacter);
                        stringBuilder.append((char) ch);
                }
            }
        }
        catch (IOException e)
        {
            result = eof;
            logger.log(Level.WARNING, e.getMessage(), e);
        }

        if(result.isEmpty())
        {
            result = defaultValue;
        }
        return result;
    }

    /**
     * Displays a prompt and waits for input.
     *
     * @param prompt the prompt to display
     * @param eof    the value to return if end of stream is reached
     * @return the input value or <tt>eof</tt> if the end of stream is reached
     */
    public String prompt(String prompt, String eof)
    {
        return prompt(prompt, "", eof);
    }

    /**
     * Displays a prompt and waits for input.
     *
     * @param prompt       the prompt to display
     * @param defaultValue the default value to use, if no input is entered
     * @param eof          the value to return if end of stream is reached
     * @return the input value or {@code eof} if the end of stream is reached
     */
    public String prompt(String prompt, String defaultValue, String eof)
    {
        String result;
        try
        {
            println(prompt);
            result = readLine();
            if (result == null)
            {
                result = eof;
            }
            else if (result.equals(""))
            {
                result = defaultValue;
            }
        }
        catch (IOException e)
        {
            result = eof;
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        return result;
    }

    /**
     * Prompts for a value from a set of values.
     *
     * @param prompt the prompt to display
     * @param values the valid values
     * @param eof    the value to return if end of stream is reached
     * @return the input value or <tt>eof</tt> if the end of stream is reached
     */
    public String prompt(String prompt, String[] values, String eof)
    {
        while (true)
        {
            String input = prompt(prompt, eof);
            if (input == null || input.equals(eof))
            {
                return input;
            }
            else
            {
                for (String value : values)
                {
                    if (value.equalsIgnoreCase(input))
                    {
                        return value;
                    }
                }
            }
        }
    }

    private String readLineDefaultInput() throws IOException {
        return console.readLine();
    }

    private char[] readPasswordDefaultInput(String defaultValue, String format, Object... args)
            throws IOException {
        char[] result;
        if (console != null)
        {
           result = console.readPassword(format, args);
           if (result.length == 0)
           {
               result = defaultValue.toCharArray();
           }
        }
        else
        {
            // Fix ConsolePasswordGroupFieldTest
            result = readLine().toCharArray();
        }
        return result;
    }

    public void useDefaultInput()
    {
        consoleReaderFailed = true;
    }
}

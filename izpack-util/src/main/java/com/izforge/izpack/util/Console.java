package com.izforge.izpack.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * I/O streams to support prompting and keyboard input from the console.
 *
 * @author Tim Anderson
 */
public class Console
{
    private static final Logger logger = Logger.getLogger(Console.class.getName());

    /**
     * Input stream.
     */
    private final BufferedReader in;

    /**
     * Output stream.
     */
    private final PrintWriter out;

    /**
     * Constructs a <tt>Console</tt> with <tt>System.in</tt> and <tt>System.out</tt> as the I/O streams.
     */
    public Console()
    {
        this(System.in, System.out);
    }

    /**
     * Constructs a <tt>Console</tt>.
     *
     * @param in  the input stream
     * @param out the output stream
     */
    public Console(InputStream in, OutputStream out)
    {
        this.in = new BufferedReader(new InputStreamReader(in));
        this.out = new PrintWriter(out, true);
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
        return in.readLine();
    }

    /**
     * Prints a message to the console.
     *
     * @param message the message to print
     */
    public void print(String message)
    {
        out.print(message);
        out.flush();
    }

    /**
     * Prints a new line.
     */
    public void println()
    {
        out.println();
    }

    /**
     * Prints a message to the console with a new line.
     *
     * @param message the message to print
     */
    public void println(String message)
    {
        out.println(message);
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
            print(prompt);
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

}

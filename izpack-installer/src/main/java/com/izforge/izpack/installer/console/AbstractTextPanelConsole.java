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

package com.izforge.izpack.installer.console;

import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.util.Console;

/**
 * Abstract console panel for displaying paginated text.
 *
 * @author Tim Anderson
 */
public abstract class AbstractTextPanelConsole extends AbstractPanelConsole
{

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(AbstractTextPanelConsole.class.getName());

    /**
     * Runs the panel using the supplied properties.
     *
     * @param installData the installation data
     * @param properties  the properties
     * @return <tt>true</tt>
     */
    @Override
    public boolean runConsoleFromProperties(InstallData installData, Properties properties)
    {
        return true;
    }

    /**
     * Runs the panel using the specified console.
     * <p/>
     * If there is no text to display, the panel will return <tt>false</tt>.
     *
     * @param installData the installation data
     * @param console     the console
     * @return <tt>true</tt> if the panel ran successfully, otherwise <tt>false</tt>
     */
    @Override
    public boolean runConsole(InstallData installData, Console console)
    {
        boolean result;
        String text = getText();
        if (text != null)
        {
            result = paginateText(text, console);
        }
        else
        {
            logger.warning("No text to display");
            result = false;
        }
        return result && promptEndPanel(installData, console);
    }

    /**
     * Returns the text to display.
     *
     * @return the text. A <tt>null</tt> indicates failure
     */
    protected abstract String getText();

    /**
     * Pages through the supplied text.
     *
     * @param text    the text to display
     * @param console the console to display to
     * @return <tt>true</tt> if paginated through, <tt>false</tt> if terminated
     */
    protected boolean paginateText(String text, Console console)
    {
        boolean result = true;
        int lines = 22; // the no. of lines to display at a time
        int line = 0;

        StringTokenizer tokens = new StringTokenizer(text, "\n");
        while (tokens.hasMoreTokens())
        {
            String token = tokens.nextToken();
            console.println(token);
            line++;
            if (line >= lines && tokens.hasMoreTokens())
            {
                if (!promptContinue(console))
                {
                    result = false;
                    break;
                }
                line = 0;
            }
        }
        return result;
    }

    /**
     * Displays a prompt to continue, providing the option to terminate installation.
     *
     * @param console the console to perform I/O
     * @return <tt>true</tt> if the installation should continue, <tt>false</tt> if it should terminate
     */
    protected boolean promptContinue(Console console)
    {
        String value = console.prompt("\nPress Enter to continue, X to exit", "x");
        console.println();
        return !value.equalsIgnoreCase("x");
    }

    /**
     * Helper to strip HTML from text.
     * From code originally developed by Jan Blok.
     *
     * @param text the text. May be {@code null}
     * @return the text with HTML removed
     */
    protected String removeHTML(String text)
    {
        String result = "";

        if (text != null)
        {
            // chose to keep newline (\n) instead of carriage return (\r) for line breaks.

            // Replace line breaks with space
            result = text.replaceAll("\r", " ");
            // Remove step-formatting
            result = result.replaceAll("\t", "");
            // Remove repeating spaces because browsers ignore them

            result = result.replaceAll("( )+", " ");


            result = result.replaceAll("<( )*head([^>])*>", "<head>");
            result = result.replaceAll("(<( )*(/)( )*head( )*>)", "</head>");
            result = result.replaceAll("(<head>).*(</head>)", "");
            result = result.replaceAll("<( )*script([^>])*>", "<script>");
            result = result.replaceAll("(<( )*(/)( )*script( )*>)", "</script>");
            result = result.replaceAll("(<script>).*(</script>)", "");

            // remove all styles (prepare first by clearing attributes)
            result = result.replaceAll("<( )*style([^>])*>", "<style>");
            result = result.replaceAll("(<( )*(/)( )*style( )*>)", "</style>");
            result = result.replaceAll("(<style>).*(</style>)", "");

            result = result.replaceAll("(<( )*(/)( )*sup( )*>)", "</sup>");
            result = result.replaceAll("<( )*sup([^>])*>", "<sup>");
            result = result.replaceAll("(<sup>).*(</sup>)", "");

            // insert tabs in spaces of <td> tags
            result = result.replaceAll("<( )*td([^>])*>", "\t");

            // insert line breaks in places of <BR> and <LI> tags
            result = result.replaceAll("<( )*br( )*>", "\r");
            result = result.replaceAll("<( )*li( )*>", "\r");

            // insert line paragraphs (double line breaks) in place
            // if <P>, <DIV> and <TR> tags
            result = result.replaceAll("<( )*div([^>])*>", "\r\r");
            result = result.replaceAll("<( )*tr([^>])*>", "\r\r");

            result = result.replaceAll("(<) h (\\w+) >", "\r");
            result = result.replaceAll("(\\b) (</) h (\\w+) (>) (\\b)", "");
            result = result.replaceAll("<( )*p([^>])*>", "\r\r");

            // Remove remaining tags like <a>, links, images,
            // comments etc - anything that's enclosed inside < >
            result = result.replaceAll("<[^>]*>", "");


            result = result.replaceAll("&bull;", " * ");
            result = result.replaceAll("&lsaquo;", "<");
            result = result.replaceAll("&rsaquo;", ">");
            result = result.replaceAll("&trade;", "(tm)");
            result = result.replaceAll("&frasl;", "/");
            result = result.replaceAll("&lt;", "<");
            result = result.replaceAll("&gt;", ">");

            result = result.replaceAll("&copy;", "(c)");
            result = result.replaceAll("&reg;", "(r)");
            result = result.replaceAll("&(.{2,6});", "");

            // Remove extra line breaks and tabs:
            // replace over 2 breaks with 2 and over 4 tabs with 4.
            // Prepare first to remove any whitespaces in between
            // the escaped characters and remove redundant tabs in between line breaks
            result = result.replaceAll("(\r)( )+(\r)", "\r\r");
            result = result.replaceAll("(\t)( )+(\t)", "\t\t");
            result = result.replaceAll("(\t)( )+(\r)", "\t\r");
            result = result.replaceAll("(\r)( )+(\t)", "\r\t");
            result = result.replaceAll("(\r)(\t)+(\\r)", "\r\r");
            result = result.replaceAll("(\r)(\t)+", "\r\t");
        }
        return result;
    }
}

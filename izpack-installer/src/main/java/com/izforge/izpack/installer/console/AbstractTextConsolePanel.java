/*
 * Copyright 2016 Julien Ponge, René Krell and the IzPack team.
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

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.installer.util.PanelHelper;
import com.izforge.izpack.util.Console;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Abstract console panel for displaying paginated text.
 *
 * @author Tim Anderson
 */
public abstract class AbstractTextConsolePanel extends AbstractConsolePanel
{

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(AbstractTextConsolePanel.class.getName());

    /**
     * Constructs an {@code AbstractTextConsolePanel}.
     *
     * @param panel the parent panel/view. May be {@code null}
     */
    public AbstractTextConsolePanel(PanelView<ConsolePanel> panel)
    {
        super(panel);
    }

    /**
     * Runs the panel using the supplied properties.
     *
     * @param installData the installation data
     * @param properties  the properties
     * @return <tt>true</tt>
     */
    @Override
    public boolean run(InstallData installData, Properties properties)
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
    public boolean run(InstallData installData, Console console)
    {
        printHeadLine(installData, console);

        String panelLabel = getPanelLabel(installData);
        if (panelLabel != null)
        {
            console.println(panelLabel);
        }

        String text = getText();
        if (substituteVariables()) {
            text = installData.getVariables().replace(text);
        }
        if (text != null)
        {
            Panel panel = getPanel();
            RulesEngine rules = installData.getRules();
            boolean paging = Boolean.parseBoolean(panel.getConfigurationOptionValue("console-text-paging", rules));
            boolean wordwrap = Boolean.parseBoolean(panel.getConfigurationOptionValue("console-text-wordwrap", rules));

            try
            {
                console.printMultiLine(text, wordwrap, paging);
            }
            catch (IOException e)
            {
                logger.warning("Displaying multiline text failed: " + e.getMessage());
            }
        }
        else
        {
            logger.warning("No text to display");
        }
        return promptEndPanel(installData, console);
    }

    /**
     * Returns the panel label to display.
     *
     * @param installData the installation data
     * @return the panel label. A <tt>null</tt> indicates no panel label to display
     */
    protected String getPanelLabel(InstallData installData) {
        String titleMessageKey = PanelHelper.getPanelTitleMessageKey(getPanel(), "info", installData);
        return installData.getMessages().get(titleMessageKey);
    }

    /**
     * Returns the text to display.
     *
     * @return the text. A <tt>null</tt> indicates failure
     */
    protected abstract String getText();

    /**
     * Returns true if variables are to be substituted in the text or else false.
     *
     * @return true, the default implementation
     */
    protected boolean substituteVariables() {
        return true;
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

            // we will remove child elements of head except title
            result = removeHTMLTag(result, "base");
            result = removeHTMLTag(result, "link");
            result = removeHTMLTag(result, "meta");
            result = removeHTMLTag(result, "noscript");
            result = removeHTMLTag(result, "script");
            result = removeHTMLTag(result, "style");
            result = removeHTMLTag(result, "template");

            result = result.replaceAll("[ \\t]*<( )*title([^>])*>[ \\t]*", "<title>");
            result = result.replaceAll("([ \\t]*<( )*(/)( )*title( )*>[ \\t]*)", "</title>");

            result = removeHTMLTag(result, "sup");

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
            result = result.replaceAll("\\r", "\n");
            result = result.replaceAll("[\\t ]+\\n", "\n");
            result = result.replaceAll("\\n\\n+\\n", "\n\n").trim();
        }
        return result;
    }

    private String removeHTMLTag(String text, String tag)
    {
        String result = text.replaceAll("[ \\t]*<( )*" + tag + "([^>])*>[ \\t]*", "<" + tag + ">");
        result = result.replaceAll("([ \\t]*<( )*(/)( )*" + tag + "( )*>[ \\t]*)", "</" + tag + ">");
        return result.replaceAll("(<" + tag + ">)(.|\n)*(</" + tag + ">)", "");
    }
}

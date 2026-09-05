/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Jan Blok
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

package com.izforge.izpack.panels.jdkpath;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.installer.console.AbstractConsolePanel;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.panels.path.PathInputBase;
import com.izforge.izpack.util.Console;
import com.izforge.izpack.api.config.Options;

import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.izforge.izpack.panels.jdkpath.JDKPathPanel.PANEL_NAME;

/**
 * The JDKPathPanel panel console helper class.
 *
 * @author Mounir El Hajj
 */
public class JDKPathConsolePanel extends AbstractConsolePanel
{
    private static final Logger LOGGER = Logger.getLogger(JDKPathConsolePanel.class.getName());
    private static final Pattern ANSWER_PATTERN = Pattern.compile("\\[(?<yes>\\w)/(?<no>\\w)] \\[(?<default>\\w)]$");

    private final VariableSubstitutor variableSubstitutor;
    private final RegistryDefaultHandler handler;
    private final InstallData installData;

    /**
     * Constructs a <tt>JDKPathConsolePanelHelper</tt>.
     *
     * @param variableSubstitutor the variable substituter
     * @param handler             the registry handler
     * @param panel               the parent panel/view. May be {@code null}
     */
    public JDKPathConsolePanel(VariableSubstitutor variableSubstitutor, RegistryDefaultHandler handler,
                               PanelView<ConsolePanel> panel, InstallData installData)
    {
        super(panel);
        this.handler = handler;
        this.installData = installData;
        this.variableSubstitutor = variableSubstitutor;
        JDKPathPanelHelper.initialize(installData);
    }

    public boolean run(InstallData installData, Properties properties)
    {
        String strJDKPath = properties.getProperty(JDKPathPanelHelper.JDK_PATH, "");
        if (strJDKPath.isBlank())
        {
            LOGGER.severe("Missing mandatory JDK path!");
            return false;
        }
        else
        {
            try
            {
            	strJDKPath = variableSubstitutor.substitute(strJDKPath);
            }
            catch (Exception e)
            {
                // ignore
            }
            installData.setVariable(JDKPathPanelHelper.JDK_PATH, strJDKPath);
            return true;
        }
    }

    /**
     * Runs the panel using the specified console.
     *
     * @param installData the installation data
     * @param console     the console
     * @return <tt>true</tt> if the panel ran successfully, otherwise <tt>false</tt>
     */
    @Override
    public boolean run(InstallData installData, Console console)
    {
        printHeadLine(installData, console);

        String introText = getI18nStringForClass("intro", PANEL_NAME, installData);
        if (introText != null)
        {
            console.println(introText);
            console.println();
        }

        String defaultValue = JDKPathPanelHelper.getDefaultJavaPath(installData, handler);

        if (JDKPathPanelHelper.skipPanel(installData, defaultValue))
        {
            return true;
        }
        String strPath;
        boolean bKeepAsking = true;
        while (bKeepAsking)
        {
            Messages messages = installData.getMessages();
            strPath = console.promptLocation(messages.get("JDKPathPanel.info") + " [" + defaultValue + "] ", defaultValue);
            if (strPath == null)
            {
                return false;
            }
            strPath = strPath.trim();

            strPath = PathInputBase.normalizePath(strPath);
            String detectedJavaVersion = JDKPathPanelHelper.getCurrentJavaVersion(strPath, installData.getPlatform());

            String errorMessage = JDKPathPanelHelper.validate(strPath, detectedJavaVersion, messages);
            if (!errorMessage.isEmpty())
            {
                if (errorMessage.endsWith("?"))
                {
                    if (promptForContinuation(console, messages, errorMessage))
                    {
                        bKeepAsking = false;
                    }
                    else
                    {
                        return false;
                    }
                }
                else
                {
                    console.println(messages.get("JDKPathPanel.notValid"));
                }
            }
            else
            {
                bKeepAsking = false;
            }
            installData.setVariable(JDKPathPanelHelper.JDK_PATH, strPath);
        }

        return promptEndPanel(installData, console);
    }

    static boolean promptForContinuation(Console console, Messages messages, String errorMessage) {
        // Ex: JDKPathPanel.badVersion4= "Continue anyway? [y/n] [n]"
        final String yesNoPromptPattern = messages.get("JDKPathPanel.badVersion4");
        final Matcher answerMatcher = ANSWER_PATTERN.matcher(yesNoPromptPattern);
        final String defaultAnswer;
        final String yesAnswer;
        final String noAnswer;
        if (answerMatcher.find())
        {
            defaultAnswer = Objects.requireNonNullElse(answerMatcher.group("default"), "n");
            yesAnswer = Objects.requireNonNullElse(answerMatcher.group("yes"), "y");
            noAnswer = Objects.requireNonNullElse(answerMatcher.group("no"), "n");
        }
        else
        {
            defaultAnswer = "n";
            yesAnswer = "y";
            noAnswer = "n";
        }
        errorMessage += "\n" + yesNoPromptPattern;
        final String userAnswer = console.prompt(errorMessage, noAnswer);
        final String userAnswerChar;
        if (userAnswer.isBlank())
        {
            userAnswerChar = defaultAnswer;
        }
        else
        {
            userAnswerChar = userAnswer.substring(0, 1);
        }
        return yesAnswer.equalsIgnoreCase(userAnswerChar) || "y".equalsIgnoreCase(userAnswerChar);
    }

    @Override
    public boolean generateOptions(InstallData installData, Options options)
    {
        final String name =JDKPathPanelHelper.JDK_PATH;
        options.add(name, installData.getVariable(name));
        options.addEmptyLine(name);
        options.putComment(name, List.of(getPanel().getPanelId()));
        return true;
    }

    @Override
    public void createInstallationRecord(IXMLElement panelRoot)
    {
        new JDKPathPanelAutomationHelper().createInstallationRecord(installData, panelRoot);
    }
}

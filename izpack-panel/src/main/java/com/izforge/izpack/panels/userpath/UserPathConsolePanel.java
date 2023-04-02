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
package com.izforge.izpack.panels.userpath;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.config.Options;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.installer.console.AbstractConsolePanel;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.util.Console;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import static com.izforge.izpack.panels.userpath.UserPathPanel.PANEL_NAME;
import static com.izforge.izpack.panels.userpath.UserPathPanel.PATH_VARIABLE_NAME;

/**
 * The UserPath panel console helper class.
 * Based on the Target panel console helper
 *
 * @author Mounir El Hajj
 * @author Dustin Kut Moy Cheung
 */
public class UserPathConsolePanel extends AbstractConsolePanel
{
    public static final String USER_PATH_INFO = "UserPathPanel.info";
    public static final String USER_PATH_NO_DIR = "UserPathPanel.nodir";
    public static final String USER_PATH_EXISTS = "UserPathPanel.exists_warn";

    private static final String EMPTY = "";

    private Messages messages;
    private final InstallData installData;

    /**
     * Constructs an {@code UserPathConsolePanel}.
     *
     * @param panel the parent panel/view. May be {@code null}
     */
    public UserPathConsolePanel(PanelView<ConsolePanel> panel, InstallData installData)
    {
        super(panel);
        this.installData = installData;
    }

    private void loadLangpack(InstallData installData)
    {
        messages = installData.getMessages();
    }

    private String getTranslation(String id)
    {
        return messages.get(id);
    }

    public boolean generateOptions(InstallData installData, Options options)
    {
        final String name = PATH_VARIABLE_NAME;
        options.add(name, installData.getVariable(name));
        options.addEmptyLine(name);
        options.putComment(name, Arrays.asList(getPanel().getPanelId()));
        return true;
    }

    public boolean run(InstallData installData, Properties properties)
    {
        String userPath = properties.getProperty(PATH_VARIABLE_NAME);
        if (userPath == null || userPath.trim().isEmpty())
        {
            System.err.println("Missing mandatory " + PATH_VARIABLE_NAME + "!");
            return false;
        }
        userPath = installData.getVariables().replace(userPath);
        installData.setVariable(PATH_VARIABLE_NAME, userPath);
        return true;
    }

    public boolean run(InstallData installData, Console console)
    {
        printHeadLine(installData, console);

        loadLangpack(installData);

        String introText = getI18nStringForClass("intro", PANEL_NAME, installData);
        if (introText != null)
        {
            console.println(introText);
            console.println();
        }
        VariableSubstitutor vs = new VariableSubstitutorImpl(installData.getVariables());
        String pathMessage = getTranslation(USER_PATH_INFO);
        String defaultUserPath = installData.getVariable(PATH_VARIABLE_NAME);

        defaultUserPath = defaultUserPath == null ? EMPTY : vs.substitute(defaultUserPath, null);

        String userPath = console.promptLocation(pathMessage + " [" + defaultUserPath + "]", defaultUserPath);

        // check what the userPath value should be
        if (userPath == null)
        {
            return false;
        }
        else if (EMPTY.equals(userPath))
        {
            if (EMPTY.equals(defaultUserPath))
            {
                out("Error: Path is empty! Enter a valid path");
                return run(installData, console);
            }
            else
            {
                userPath = defaultUserPath;
            }
        }
        else
        {
            userPath = vs.substitute(userPath, null);
        }
        if (!isPathAFile(userPath))
        {
            if (doesPathExists(userPath) && !isPathEmpty(userPath))
            {
                out(getTranslation(USER_PATH_EXISTS));

                if (!promptEndPanel(installData, console))
                {
                    return false;
                }
            }
        }
        else
        {
            out(getTranslation(USER_PATH_NO_DIR));
            return run(installData, console);
        }
        // If you reached here, all data validation done!
        // ask the user if he wants to proceed to the next
        if (promptEndPanel(installData, console))
        {
            installData.setVariable(PATH_VARIABLE_NAME, userPath);
            return true;
        }
        else
        {
            return false;
        }
    }

    private static boolean doesPathExists(String path)
    {
        File file = new File(path);
        return file.exists();
    }

    private static boolean isPathAFile(String path)
    {
        File file = new File(path);
        return file.isFile();
    }

    private static boolean isPathEmpty(String path)
    {
        File file = new File(path);
        return (file.list().length == 0);
    }

    private static void out(String out)
    {
        System.out.println(out);
    }

    @Override
    public void createInstallationRecord(IXMLElement panelRoot)
    {
        //TODO: Check if skip
        new UserPathPanelAutomationHelper().createInstallationRecord(installData, panelRoot);
    }
}

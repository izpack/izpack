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

package com.izforge.izpack.panels.path;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.installer.console.AbstractConsolePanel;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.util.Console;

import java.io.File;
import java.util.Properties;

import static com.izforge.izpack.api.handler.Prompt.Option.OK;
import static com.izforge.izpack.api.handler.Prompt.Options.OK_CANCEL;
import static com.izforge.izpack.api.handler.Prompt.Type.WARNING;

public class PathInputConsolePanel extends AbstractConsolePanel
{
    protected String targetPanel;
    private final Prompt prompt;
    private InstallData installData;
    /**
     * Constructs an {@code PathInputConsolePanel}.
     *
     * @param panel the parent panel/view. May be {@code null}
     * @param installData the install data
     * @param prompt the console prompt
     */
    public PathInputConsolePanel(PanelView<ConsolePanel> panel, String targetPanel, InstallData installData, Prompt prompt)
    {
        super(panel);
        this.targetPanel = targetPanel;
        this.installData = installData;
        this.prompt = prompt;
    }

    /**
     * Helper to return a language resource string.
     *
     * @param subkey the search subkey in targetPanel
     * @return the corresponding string, or {@code <targetPanel>.<subkey>} if the string is not found
     */
    protected String getMessage(String subkey)
    {
        String msg = getI18nStringForClass(subkey, targetPanel, installData);
        if (msg == null)
        {
            msg = targetPanel + "." + subkey;
        }
        return msg;
    }

    @Override
    public boolean run(InstallData installData, Properties properties)
    {
        this.installData = installData;
        return false;
    }

    /**
     * Determines if the specified directory can be created.
     *
     * @param dir the directory
     * @return {@code true} if the directory may be created, otherwise {@code false}
     */
    protected boolean checkCreateDirectory(File dir, Console console)
    {
        boolean result = true;
        // if 'ShowCreateDirectoryMessage' configuration option set 'false' then don't show
        // then don't show "directory will be created" dialog:
        String show = getPanel().getConfigurationOptionValue(PathInputBase.SHOWCREATEDIRECTORYMESSAGE, installData.getRules());
        if (show == null || Boolean.parseBoolean(show))
        {
            Messages messages = installData.getMessages();
            result = (OK == prompt.confirm(WARNING,
                    messages.get("installer.Message"),
                    getMessage("createdir") + "\n" + dir,
                    OK_CANCEL, OK));
        }
        return result;
    }

    /**
     * Determines if an existing directory can be written to.
     *
     * @param dir the directory
     * @return {@code true} if the directory can be written to, otherwise {@code false}
     */
    protected boolean checkOverwrite(File dir, Console console)
    {
        boolean result = true;
        // if 'ShowExistingDirectoryWarning' configuration option set 'false' then don't show
        // "The directory already exists! Are you sure you want to install here and possibly overwrite existing files?"
        // warning dialog:
        String show = getPanel().getConfigurationOptionValue(PathInputBase.SHOWEXISTINGDIRECTORYWARNING, installData.getRules());
        if ((show == null || Boolean.parseBoolean(show)) && dir.isDirectory() && dir.list().length > 0)
        {
            Messages messages = installData.getMessages();
            result = askUser(messages.get("installer.warning"), getMessage("exists_warn"), Prompt.Option.NO);
        }
        return result;
    }

    /**
     * Helper method to read the input of user
     * Method returns true if user types "y", "yes" or <Enter>·
     *
     * @return boolean  - true if condition above satisfied. Otherwise false
     */
    private boolean askUser(String title, String message, Prompt.Option defaultOption)
    {
        return Prompt.Option.YES == prompt.confirm(Prompt.Type.QUESTION, title, message, Prompt.Options.YES_NO, defaultOption);
    }
}

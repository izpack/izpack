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

package com.izforge.izpack.panels.hello;

import java.util.ArrayList;
import java.util.Properties;

import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.installer.console.AbstractConsolePanel;
import com.izforge.izpack.util.Console;

/**
 * Hello Panel console helper
 *
 * @author Mounir el hajj
 */
public class HelloConsolePanel extends AbstractConsolePanel
{

    public boolean run(InstallData installData, Properties properties)
    {
        return true;
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
        display(installData, console);
        return promptEndPanel(installData, console);
    }

    /**
     * Displays the panel.
     *
     * @param installData the installation data
     * @param console     the console
     */
    protected void display(InstallData installData, Console console)
    {
        Messages messages = installData.getMessages();
        Info info = installData.getInfo();
        String welcomeText = messages.get("HelloPanel.welcome1") + info.getAppName() + " "
                + info.getAppVersion() + messages.get("HelloPanel.welcome2");
        console.println(welcomeText);
        ArrayList<Info.Author> authors = info.getAuthors();
        if (!authors.isEmpty())
        {
            console.println(messages.get("HelloPanel.authors"));

            for (Info.Author author : authors)
            {
                String email = (author.getEmail() != null && author.getEmail().length() > 0) ? (" <"
                        + author.getEmail() + ">") : "";
                console.println(" - " + author.getName() + email);
            }
        }

        if (info.getAppURL() != null)
        {
            String urlText = messages.get("HelloPanel.url") + info.getAppURL();
            console.println(urlText);
        }
    }
}

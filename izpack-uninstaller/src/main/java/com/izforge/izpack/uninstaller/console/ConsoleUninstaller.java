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
package com.izforge.izpack.uninstaller.console;

import java.io.File;

import com.izforge.izpack.uninstaller.Destroyer;
import com.izforge.izpack.uninstaller.event.DestroyerListener;
import com.izforge.izpack.util.Console;

/**
 * Console uninstaller.
 *
 * @author Tim Anderson
 */
public class ConsoleUninstaller
{
    /**
     * The destroyer.
     */
    private final Destroyer destroyer;

    /**
     * The console.
     */
    private final Console console;

    /**
     * Constructs a {@code ConsoleUninstaller}.
     *
     * @param destroyer the destroyer
     * @param listener  the listener
     * @param console   the console
     */
    public ConsoleUninstaller(Destroyer destroyer, DestroyerListener listener, Console console)
    {
        this.destroyer = destroyer;
        this.console = console;
        destroyer.setProgressListener(listener);
    }

    /**
     * Performs uninstallation.
     *
     * @param force if {@code true}, force deletion of remaining files
     */
    public void uninstall(boolean force)
    {
        console.println("Force deletion: " + force);
        destroyer.setForceDelete(force);
        destroyer.run();
        if (!destroyer.getFailedToDelete().isEmpty())
        {
            console.println("WARNING: The following files could not be removed: ");
            for (File file : destroyer.getFailedToDelete())
            {
                console.println(file.getPath());
            }
        }
    }
}

/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Elmar Grom
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

package com.izforge.izpack.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;

/*---------------------------------------------------------------------------*/

/**
 * This class performs housekeeping and cleanup tasks.
 * <br/>
 * It is VERY important to perform pre-shutdown cleanup operations through this class. Do NOT rely
 * on operations like <code>deleteOnExit()</code> shutdown hooks or <code>finalize()</code>for
 * cleanup. Because <code>shutDown()</code> uses <code>System.exit()</code> to terminate, these
 * methods will not work at all or will not work reliably.
 *
 * @author Elmar Grom
 * @version 0.0.1 / 2/9/02
 */
/*---------------------------------------------------------------------------*/
@ApplicationScoped
public class Housekeeper
{

    private List<CleanupClient> cleanupClients = new ArrayList<>();

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(Housekeeper.class.getName());

    /*--------------------------------------------------------------------------*/

    /**
     * Default constructor.
     */
    public Housekeeper()
    {
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Use to register objects that need to perform cleanup operations before the application shuts
     * down.
     *
     * @param client reference of to an object that needs to perform cleanup operations.
     */
    /*--------------------------------------------------------------------------*/
    public void registerForCleanup(CleanupClient client)
    {
        // IZPACK-276:
        // if the client is an instance of Librarian hold it at a special place to call it at the
        // very last time
        if (client instanceof Librarian)
        {
            cleanupClients.add(0, client);
        }
        else
        {
            cleanupClients.add(client);
        }
    }

    /*--------------------------------------------------------------------------*/

    /**
     * This methods shuts the application down. First, it will call all clients that have registered
     * for cleanup operations. Once this has been accomplished, the application will be forceably
     * terminated. <br>
     * <br>
     * <b>THIS METHOD DOES NOT RETURN!</b>
     *
     * @param exitCode the exit code that should be returned to the calling process.
     */
    /*--------------------------------------------------------------------------*/
    public void shutDown(int exitCode)
    {
        shutDown(exitCode, false);
    }

    public void shutDown(int exitCode, boolean reboot)
    {
        // IZPACK-276
        // Do the cleanup of the last registered client at the fist time (first in last out)
        for (int i = cleanupClients.size() - 1; i >= 0; i--)
        {
            try
            {
                (cleanupClients.get(i)).cleanUp();
            }
            catch (Throwable exception)
            {
                // At this point we can not afford to treat exceptions. Cleanup that can not be completed might
                // unfortunately leave some garbage behind.
                logger.log(Level.WARNING, exception.getMessage(), exception);
            }
        }

        terminate(exitCode, reboot);
    }

    protected void terminate(int exitCode, boolean reboot)
    {
        if (reboot)
        {
            try
            {
                systemReboot();
            }
            catch (IOException exception)
            {
                // Do nothing at the moment
                logger.log(Level.WARNING, exception.getMessage(), exception);
            }
        }

        System.exit(exitCode);
    }

    private void systemReboot() throws IOException
    {
        final int waitseconds = 2;

        if (OsVersion.IS_UNIX)
        {
            Runtime.getRuntime().exec("sudo /sbin/shutdown -r -t " + waitseconds + " now");
        }
        else if (OsVersion.IS_WINDOWS)
        {
            Runtime.getRuntime().exec("shutdown /r /f /t " + waitseconds);
        }
        else
        {
            throw new IOException("Reboot not implemented for your OS");
        }
    }
}
/*---------------------------------------------------------------------------*/

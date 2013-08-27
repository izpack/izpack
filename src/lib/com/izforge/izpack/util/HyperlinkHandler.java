/*
 * IzPack - Copyright 2001-2009 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Elmar Grom
 * Copyright 2009 Dennis Reil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.izforge.izpack.util;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * This class can be used to listen at hyperlinks click
 * and open open the link in a browser
 *
 * @author Mathieu ANCELIN
 *
 */
public class HyperlinkHandler implements HyperlinkListener
{
    /**
     * Handle an event on the link
     * @param HyperlinkEvent the event on the link
     */
    public void hyperlinkUpdate(HyperlinkEvent e)
    {
        try
        {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
            {
                String urls = e.getURL().toExternalForm();
                if (com.izforge.izpack.util.OsVersion.IS_OSX)
                {
                    //Runtime.getRuntime().exec("open " + urls);
                    ProcessBuilder pbOSX = new ProcessBuilder(new String [] {"open", urls});
                    pbOSX.start();
                }
                else if (com.izforge.izpack.util.OsVersion.IS_UNIX)
                {
                    String[] launchers = { "htmlview QqzURL", "xdg-open QqzURL",
                            "gnome-open QqzURL", "kfmclient openURL QqzURL", "call-browser QqzURL",
                            "firefox QqzURL", "opera QqzURL", "konqueror QqzURL",
                            "epiphany QqzURL", "mozilla QqzURL", "netscape QqzURL"};
                    for (String launcher : launchers)
                    {
                        try
                        {
                            // not windows platform
                            // http://www.oracle.com/technetwork/java/javase/7u25-relnotes-1955741.html#jruntime
                            // TODO
                            Runtime.getRuntime().exec(launcher.replaceAll("QqzURL", urls));
                            System.out.println("OK");
                            break;
                        }
                        catch (Exception ignore)
                        {
                            System.out.println(launcher + " NOT OK");
                        }
                    }
                }
                else
                // windows
                {
                    //Runtime.getRuntime().exec("cmd /C start " + urls);
                    ProcessBuilder pbWIn = new ProcessBuilder(new String[] {"cmd.exe", "/C", "start", urls});
                    pbWIn.start ();
                }
            }
        }
        catch (Exception err)
        {
            err.printStackTrace();
        }
    }
}


/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/ http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil
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
package com.izforge.izpack.installer.multiunpacker;

import java.awt.HeadlessException;
import java.io.File;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.installer.bootstrap.Installer;
import com.izforge.izpack.util.SelfModifier;

/**
 * Main class, for starting the installer if it was build to support more than
 * one volume.
 *
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 * @deprecated see {@link Installer}
 */
@Deprecated
public class MultiVolumeInstaller
{

    private static final Logger LOGGER = Logger.getLogger(MultiVolumeInstaller.class.getName());

    // where is the installer looking for media files
    protected static String mediadirectory;

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        ProgressDialog progressDialog = null;

        try
        {
            progressDialog = new ProgressDialog();
            progressDialog.startProgress();
        }
        catch (HeadlessException ex)
        {
            // this exception is expected if we're running in console or
            // auto installation mode
            LOGGER.warning("Progress will not be shown. No display found.");
        }
        // default is to look in the current directory
        MultiVolumeInstaller.setMediadirectory(new File(".").getParent());
        if ((args.length > 0) && ("-direct".equals(args[0])))
        {
            String[] newargs;
            if (args.length > 1)
            {
                // cut out the direct parameter
                newargs = new String[args.length - 1];
                System.arraycopy(args, 1, newargs, 0, args.length - 1);
            }
            else
            {
                // set arguments to empty string array
                newargs = new String[0];
            }
            MultiVolumeInstaller.install(newargs);
        }
        else
        {
            try
            {
                long maxmem = Runtime.getRuntime().maxMemory() / (1024 * 1024);
                LOGGER.fine("Currently using maximum memory of " + maxmem + "m");
                // just use a static fraction of memory for perm gen size.
                long maxpermgensize = maxmem / 4;

                Class<MultiVolumeInstaller> clazz = MultiVolumeInstaller.class;
                Method target = clazz.getMethod("install", new Class[]
                {
                    String[].class
                });
                String[] newargs = new String[args.length + 2];
                System.arraycopy(args, 0, newargs, 2, args.length);

                // try to find the directory, where the jar file is located, this class was loaded
                // from
//                newargs[0] = "-Xmx" + maxmem + "m";
//                newargs[1] = "-XX:MaxPermSize=" + maxpermgensize + "m";
                newargs[0] = "-mediadir";
                newargs[1] = SelfModifier.findJarFile(clazz).getParent();
                System.out.println("Setting mediadir: " + newargs[1]);
                MultiVolumeInstaller.setMediadirectory(newargs[1]);
                new SelfModifier(target, maxmem, maxpermgensize).invoke(newargs);

            }
            catch (Exception e)
            {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
        if (progressDialog != null)
        {
            progressDialog.stopProgress();
        }
    }

    public static String getMediadirectory()
    {
        return MultiVolumeInstaller.mediadirectory;
    }

    public static void setMediadirectory(String mediadirectory)
    {
        MultiVolumeInstaller.mediadirectory = mediadirectory;
    }

    public static void install(String[] args)
    {
        if ((args.length >= 2) && ("-mediadir".equals(args[0])))
        {
            // mediadir option given
            MultiVolumeInstaller.setMediadirectory(args[1]);
            if (args.length > 2)
            {
                // cut out this option
                String[] newargs = new String[args.length - 2];
                System.arraycopy(args, 2, newargs, 0, args.length - 2);
                args = newargs;
            }
            else
            {
                // put in an empty string array
                args = new String[0];
            }
        }
        // just call the izpack installer
        Installer.main(args);
    }
}

/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.uninstaller;

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.installer.PrivilegedRunner;
import com.izforge.izpack.util.OsVersion;

import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * The uninstaller class.
 *
 * @author Julien Ponge
 */
public class Uninstaller
{

    /**
     * The main method (program entry point).
     *
     * @param args The arguments passed on the command line.
     */
    public static void main(String[] args)
    {
        checkForPrivilegedExecution(args);

        boolean cmduninstall = false;
        for (String arg : args)
        {
            if (arg.equals("-c"))
            {
                cmduninstall = true;
            }
            else if (arg.equals("-console"))
            {
                cmduninstall = true;
            }
        }
        if (GraphicsEnvironment.isHeadless())
        {
            cmduninstall = true;
        }
//        if (cmduninstall)
//        {
//            System.out.println("Command line uninstaller.\n");
//        }
        try
        {
            Class<Uninstaller> clazz = Uninstaller.class;
            Method target;
            if (cmduninstall)
            {
                //target = clazz.getMethod("cmduninstall", new Class[]{String[].class});
                //new SelfModifier(target).invoke(prepareToLaunchConsole (args));
                
                // void :(
                cmduninstall(prepareToLaunchConsole (args));
                
            }
            else
            {
                target = clazz.getMethod("uninstall", new Class[]{String[].class});
                new SelfModifier(target).invoke(args);
            }
        }
        catch (Exception ioeOrTypo)
        {
            System.err.println(ioeOrTypo.getMessage());
            ioeOrTypo.printStackTrace();
            System.err.println("Unable to exec java as a subprocess.");
            System.err.println("The uninstall may not fully complete.");
            uninstall(args);
        }
    }
    
    private static String[] prepareToLaunchConsole (String[] args)
    {
        try
        {
            List<String> params = new ArrayList<String>();
            
            
            LocaleDatabase langpack = new LocaleDatabase(Uninstaller.class.getResourceAsStream("/langpack.xml"));
            InputStream is = Uninstaller.class.getResourceAsStream("/customlangpack.xml");
            if (is != null) {
                langpack.add(is);
            }
            is.close();
    
            
            UninstallerConsole uco = new UninstallerConsole();
            boolean force = false;
            boolean quiet = false;
            for (String arg : args)
            {
                if (arg.equals("-f"))
                {
                    force = true;
                } else if (arg.equals("-force"))
                {
                    force = true;
                } else if (arg.equals("-q"))
                {
                    quiet = true;
                } else if (arg.equals("-quiet"))
                {
                    quiet = true;
                }
            }
            
            if (!quiet)
            {
                String title = langpack.getString("uninstaller.title");
                if (null == title || "uninstaller.title".equals(title))
                {
                    title = "IzPack - Uninstaller";
                }
    
                System.out.println(title);
                System.out.println();
                System.out.println(langpack.getString("uninstaller.warning"));
                System.out.println();
                
                if (!force)
                {
                    System.out.println(langpack.getString("uninstaller.destroytarget")+" "+getInstallPath().getAbsolutePath()+" ?");
                    System.out.println();
                    
                    try
                    {
                        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                        while (true)
                        {
                            System.out.println(langpack.getString("consolehelper.askyesno"));
                            String strIn = br.readLine();
                            if (strIn.equals("1"))
                            {
                                force=true;
                                params.add("-f");
                                break;
                            }
                            else if (strIn.equals("2"))
                            {
                                force=false;
                                break;
                            }
                            else if (strIn.equals("3")) { System.exit (0) ; }
                        }
    
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        System.exit (0);
                    }
                }
                
                System.out.println();
                System.out.println(langpack.getString("SummaryPanel.headline"));
                
                try
                {
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    while (true)
                    {
                        System.out.println();
                        System.out.println(((force)?"[X]":"[ ]")+langpack.getString("uninstaller.destroytarget")+" "+getInstallPath().getAbsolutePath());
                        System.out.println();
                        System.out.println(langpack.getString("consolehelper.askredisplay"));
                        String strIn = br.readLine();
                        if (strIn.equals("1"))
                        {
                            break;
                        }
                        else if (strIn.equals("2"))
                        {
                            System.exit (0);
                        }
                    }
    
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    System.exit (0);
                }
                
                
            }
            
            System.out.println();
            
            
            
            // console mode always true
            params.add("-c");
            
            String[] CmdLineArgs = params.toArray(new String[params.size()]); 
            return CmdLineArgs;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        return args;
    }
    
    /**
     * Gets the installation path from the log file.
     *
     * @throws Exception Description of the Exception
     */
    private static File getInstallPath() {
        try {
            InputStream in = Uninstaller.class.getResourceAsStream("/install.log");
            InputStreamReader inReader = new InputStreamReader(in);
            BufferedReader reader = new BufferedReader(inReader);
            String installPath = reader.readLine();
            reader.close();
            return new File(installPath);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    private static void checkForPrivilegedExecution(String[] args)
    {
        if (PrivilegedRunner.isPrivilegedMode())
        {
            // We have been launched through a privileged execution, so stop the checkings here!
            return;
        }

        if (elevationShouldBeInvestigated())
        {
            PrivilegedRunner runner = new PrivilegedRunner();
            if (runner.isPlatformSupported() && isElevationNeeded())
            {
                try
                {
                    if (runner.relaunchWithElevatedRights(args) == 0)
                    {
                        System.exit(0);
                    }
                    else
                    {
                        throw new RuntimeException("Launching an uninstaller with elevated permissions failed.");
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "The uninstaller could not launch itself with administrator permissions.\n" +
                        "The uninstallation will still continue but you may encounter problems due to insufficient permissions.");
                }
            }
            else if (!runner.isPlatformSupported())
            {
                if (GraphicsEnvironment.isHeadless())
                {
                    System.out.println("This uninstaller should be run by an administrator.\n" +
                    "The uninstallation will still continue but you may encounter problems due to insufficient permissions.");
                }
                else
                {
                    JOptionPane.showMessageDialog(null, "This uninstaller should be run by an administrator.\n" +
                    "The uninstallation will still continue but you may encounter problems due to insufficient permissions.");
    //                JOptionPane.showMessageDialog(null, "This uninstaller should be run by an administrator.\n");
    //                System.exit(1);
                }
                
            }
        }
    }

    private static boolean elevationShouldBeInvestigated()
    {
        return (Uninstaller.class.getResource("/exec-admin") != null) ||
                (OsVersion.IS_WINDOWS && !(new PrivilegedRunner().canWriteToProgramFiles()));
    }

    public static void cmduninstall(String[] args)
    {
        try
        {
            
            UninstallerConsole uco = new UninstallerConsole();
            boolean force = false;
            boolean quiet = false;
            for (String arg : args)
            {
                if (arg.equals("-f"))
                {
                    force = true;
                } else if (arg.equals("-force"))
                {
                    force = true;
                } else if (arg.equals("-q"))
                {
                    quiet = true;
                } else if (arg.equals("-quiet"))
                {
                    quiet = true;
                }
            }
            
            //System.out.println("Force deletion: " + force);
            uco.runUninstall(force);
        }
        catch (Exception err)
        {
            System.err.println("- Error -");
            err.printStackTrace();
            Housekeeper.getInstance().shutDown(0);
        }
    }

    
    public static void uninstall(final String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    boolean displayForceOption = true;
                    boolean forceOptionState = false;

                    for (String arg : args)
                    {
                        if (arg.equals("-f"))
                        {
                            forceOptionState = true;
                        }
                        else if (arg.equals("-x"))
                        {
                            displayForceOption = false;
                        }
                    }

                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    new UninstallerFrame(displayForceOption, forceOptionState);
                }
                catch (Exception err)
                {
                    System.err.println("- Error -");
                    err.printStackTrace();
                    Housekeeper.getInstance().shutDown(0);
                }
            }
        });
    }

    private static boolean isElevationNeeded() {
        // well if we told exec-admin in install.xml then  i think we should elevate à uninstall too !
        // issue #1016
        return (Uninstaller.class.getResource("/exec-admin") != null)|| (!getInstallPath().canWrite());
    }
}

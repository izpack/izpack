/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.installer.base;

import javax.swing.JOptionPane;

import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.util.FileUtil;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.PrivilegedRunner;

/**
 * Configure rules engine and install data after initialization
 *
 * @author Anthonin Bonnefoy
 */
public class InstallDataConfiguratorWithRules
{

    /**
     * The installation data.
     */
    private InstallData installData;

    /**
     * The rules.
     */
    private RulesEngine rules;

    /**
     * The current platform.
     */
    private Platform platform;


    /**
     * Constructs an <tt>InstallDataConfiguratorWithRules</tt>.
     *
     * @param installData the installation data
     * @param rules       the rules
     * @param platform    the current platform
     */
    public InstallDataConfiguratorWithRules(InstallData installData, RulesEngine rules, Platform platform)
    {
        this.installData = installData;
        this.rules = rules;
        this.platform = platform;
    }


    public void configureInstallData()
    {
        checkForPrivilegedExecution(installData.getInfo());
        checkForRebootAction(installData.getInfo());
    }


    private void checkForPrivilegedExecution(Info info)
    {
        if (info.isPrivilegedExecutionRequired())
        {
            boolean shouldElevate = true;
            String conditionId = info.getPrivilegedExecutionConditionID();
            if (conditionId != null)
            {
                // only elevate permissions when condition is true
                shouldElevate = rules.getCondition(conditionId).isTrue();
            }
            if (shouldElevate)
            {
                elevate();
            }
        }
    }

    /**
     * Elevate permissions if required.
     */
    private void elevate()
    {
        PrivilegedRunner runner = new PrivilegedRunner(platform);
        if (runner.isPlatformSupported() && runner.isElevationNeeded())
        {
            try
            {
                FileUtil.getLockFile(installData.getInfo().getAppName()).delete();
                if (runner.relaunchWithElevatedRights() == 0)
                {
                    System.exit(0);
                }
                else
                {
                    throw new IzPackException("Launching an installer with elevated permissions failed.");
                }
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(null,
                                              "Administrative privileges required.\n" +
                                                      "Please re-run the installer with administrative privileges.");
            }
            System.exit(0);
        }
        else if (!runner.isPlatformSupported())
        {
            JOptionPane.showMessageDialog(null, "This installer should be run by an administrator.\n" +
                    "The installation will still continue but you may encounter problems due to insufficient permissions.");
        }
    }


    private void checkForRebootAction(Info info)
    {
        final String conditionId = info.getRebootActionConditionID();
        if (conditionId != null)
        {
            if (!rules.getCondition(conditionId).isTrue())
            {
                info.setRebootAction(Info.REBOOT_ACTION_IGNORE);
            }
        }
    }

}

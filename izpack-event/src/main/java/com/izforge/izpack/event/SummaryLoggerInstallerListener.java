/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2004 Klaus Bartz
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

package com.izforge.izpack.event;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.util.SummaryProcessor;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.file.FileUtils;

/**
 * Installer listener which writes the summary of all panels into the logfile which is defined by
 * info.summarylogfilepath. Default is $INSTALL_PATH/Uninstaller/InstallSummary.htm
 *
 * @author Klaus Bartz
 */
public class SummaryLoggerInstallerListener extends AbstractInstallerListener
{

    /**
     * Constructs a <tt>SummaryLoggerInstallerListener</tt>.
     *
     * @param installData the installation data
     */
    public SummaryLoggerInstallerListener(InstallData installData)
    {
        super(installData);
    }

    /**
     * Invoked after packs are installed.
     *
     * @param packs    the installed packs
     * @param listener the progress listener
     * @throws com.izforge.izpack.api.exception.IzPackException
     *          for any error
     */
    @Override
    public void afterPacks(List<Pack> packs, ProgressListener listener)
    {
        if (getInstallData() instanceof GUIInstallData)
        {
            GUIInstallData installData = (GUIInstallData) getInstallData();
            if (!installData.isInstallSuccess())
            {
                return;
            }
            // No logfile at automated installation because panels are not
            // involved.
            if (installData.getPanels().isEmpty())
            {
                return;
            }
            String path = installData.getInfo().getSummaryLogFilePath();
            if (path == null)
            {
                return;
            }
            path = IoHelper.translatePath(path, installData.getVariables());
            File parent = new File(path).getParentFile();

            if (!parent.exists())
            {
                parent.mkdirs();
            }

            String summary = SummaryProcessor.getSummary(installData);
            OutputStream out = null;
            try
            {
                out = new FileOutputStream(path);
                out.write(summary.getBytes("utf-8"));
            }
            catch (IOException exception)
            {
                throw new IzPackException("Failed to write summary to path: " + path, exception);
            }
            finally
            {
                FileUtils.close(out);
            }

        }
    }

}

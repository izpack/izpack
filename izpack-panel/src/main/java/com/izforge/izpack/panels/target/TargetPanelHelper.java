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
package com.izforge.izpack.panels.target;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Target panel helper methods.
 *
 * @author Tim Anderson
 */
public class TargetPanelHelper
{
    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(TargetPanelHelper.class.getName());

    /**
     * Determines if there is IzPack installation information at the specified path that is incompatible with the
     * current version of IzPack.
     * <p/>
     * To be incompatible, the file {@link InstallData#INSTALLATION_INFORMATION} must exist in the supplied directory,
     * and not contain recognised {@link Pack} instances.
     *
     * @param dir the path to check
     * @param readInstallationInformation check .installationinformation file or skip it
     * @return {@code true} if there is incompatible installation information,
     *         {@code false} if there is no installation info, or it is compatible
     */
    @SuppressWarnings("unchecked")
    public static boolean isIncompatibleInstallation(String dir, Boolean readInstallationInformation)
    {
        boolean result = false;
        File file = new File(dir, InstallData.INSTALLATION_INFORMATION);
        if (file.exists() && readInstallationInformation)
        {
            FileInputStream input = null;
            ObjectInputStream objectInput = null;
            try
            {
                input = new FileInputStream(file);
                objectInput = new ObjectInputStream(input);
                List<Object> packs = (List<Object>) objectInput.readObject();
                for (Object pack : packs)
                {
                    if (!(pack instanceof Pack))
                    {
                        return true;
                    }
                }
            }
            catch (Throwable exception)
            {
                logger.log(Level.FINE, "Installation information at path=" + file.getPath()
                        + " failed to deserialize", exception);
                result = true;
            }
            finally
            {
                IOUtils.closeQuietly(objectInput);
                IOUtils.closeQuietly(input);
            }
        }

        return result;
    }
}

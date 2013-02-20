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
package com.izforge.izpack.panels.target;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collections;

import com.izforge.izpack.api.data.InstallData;

/**
 * Helper for {@link TargetPanel} tests.
 *
 * @author Tim Anderson
 */
class TargetPanelTestHelper
{

    /**
     * Creates an ".installationinformation" file, in the specified directory.
     *
     * @param dir the directory to write to
     * @throws java.io.IOException for any I/O error
     */
    public static void createInstallationInfo(File dir) throws IOException
    {
        File info = new File(dir, InstallData.INSTALLATION_INFORMATION);
        ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(info));
        stream.writeObject(Collections.emptyList());
        stream.close();
    }

    /**
     * Creates a bad ".installationinformation" file, in the specified directory.
     *
     * @param dir the directory to write to
     * @throws java.io.IOException for any I/O error
     */
    public static void createBadInstallationInfo(File dir) throws IOException
    {
        File info = new File(dir, InstallData.INSTALLATION_INFORMATION);
        ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(info));
        stream.writeBoolean(false);
        stream.close();
    }

    /**
     * Helper to return the locale specific message for <em>TargetPanel.incompatibleInstallation</em>.
     *
     * @param installData the installation data
     * @return the message
     */
    public static String getIncompatibleInstallationMessage(InstallData installData)
    {
        String messageId = "TargetPanel.incompatibleInstallation";
        String result = installData.getMessages().get(messageId);
        assertNotNull(result); // expect the message to exist
        assertTrue(!messageId.equals(result));
        return result;
    }

}

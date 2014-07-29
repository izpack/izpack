/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2003 Jonathan Halliday
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

import com.izforge.izpack.installer.data.UninstallData;

import java.io.File;

/**
 * Interface that all installe types must adhere to.
 * This includes ConsoleInstaller, AutomatedInstaller and InstallerFrame.
 *
 * @author Jonathan Halliday
 * @author Julien Ponge
 */
public interface InstallerBase
{
    /**
     * Write the automatic installation
     *
     * @param file
     * @param uninstallData
     * @throws Exception
     */
    public void writeInstallationRecord(File file, UninstallData uninstallData) throws Exception;
}

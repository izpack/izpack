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

package com.izforge.izpack.integration;

import java.io.File;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.uninstaller.Destroyer;


/**
 * Base class for integration tests invoking the {@link Destroyer}.
 *
 * @author Tim Anderson
 */
public class AbstractDestroyerTest extends AbstractInstallationTest
{

    /**
     * Constructs an <tt>AbstractDestroyerTest</tt>
     *
     * @param installData the install data
     */
    public AbstractDestroyerTest(InstallData installData)
    {
        super(installData);
    }

    /**
     * Runs the {@link Destroyer} in the supplied uninstall jar.
     * <p/>
     * The Destroyer is launched in an isolated class loader as it locates resources using its class loader
     *
     * @param uninstallJar the uninstaller jar
     * @throws Exception for any error
     */
    protected void runDestroyer(File uninstallJar) throws Exception
    {
        UninstallHelper.consoleUninstall(uninstallJar);
    }

    /**
     * Returns the uninstaller jar file.
     *
     * @return the uninstaller jar file
     */
    protected File getUninstallerJar()
    {
        return UninstallHelper.getUninstallerJar(getInstallData());
    }
}
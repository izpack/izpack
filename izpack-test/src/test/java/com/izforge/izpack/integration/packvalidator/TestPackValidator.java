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

package com.izforge.izpack.integration.packvalidator;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.panels.treepacks.PackValidator;


/**
 * Test {@link PackValidator} implementation.
 *
 * @author Tim Anderson
 */
public class TestPackValidator implements PackValidator
{

    /**
     * Marks a pack valid/invalid.
     *
     * @param pack        the pack name
     * @param valid       determines if the pack is valid or not
     * @param installData the installation data
     */
    public static void setValid(String pack, boolean valid, InstallData installData)
    {
        installData.setVariable(pack + ".valid", Boolean.toString(valid));
    }

    /**
     * Validates the selected pack.
     * <p/>
     * This returns the value of a <em>&lt;pack&gt;</em>.valid variable.
     *
     * @param handler     the handler
     * @param installData the installation data
     * @param packName    the pack name
     * @param isSelected  determines if the pack is selected
     * @return <tt>true</tt> if the pack is valid, otherwise <tt>false</tt>
     */
    @Override
    public boolean validate(AbstractUIHandler handler, GUIInstallData installData, String packName, boolean isSelected)
    {
        String name = packName + ".valid";
        String value = installData.getVariable(name);
        return Boolean.valueOf(value);
    }
}

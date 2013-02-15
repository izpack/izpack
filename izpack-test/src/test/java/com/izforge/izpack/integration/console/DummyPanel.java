/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2013 Tim Anderson
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
package com.izforge.izpack.integration.console;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;

/**
 * Dummy panel, used to verify that console installation fails if there is no corresponding console implementation.
 *
 * @author Tim Anderson
 */
public class DummyPanel extends IzPanel
{

    /**
     * Constructs a {@code DummyPanel}.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent IzPack installer frame
     * @param installData the installation data
     * @param resources   the resources
     */
    public DummyPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources)
    {
        super(panel, parent, installData, resources);
    }
}

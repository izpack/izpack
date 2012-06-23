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

package com.izforge.izpack.integration.panelaction;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;


/**
 * Pre-activation panel action.
 *
 * @author Tim Anderson
 */
public class PreActivatePanelAction extends TestPanelAction
{
    /**
     * Constructs a <tt>PreActivatePanelAction</tt>.
     *
     * @param panel       the panel
     * @param installData the installation data
     */
    public PreActivatePanelAction(Panel panel, InstallData installData)
    {
        super(panel, ActionStage.preactivate, installData);
    }

}

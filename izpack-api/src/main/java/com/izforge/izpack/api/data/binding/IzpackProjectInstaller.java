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

package com.izforge.izpack.api.data.binding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.izforge.izpack.api.data.Panel;

/**
 * Global model that will contain all xml information
 *
 * @author Anthonin Bonnefoy
 */
public class IzpackProjectInstaller implements Serializable
{

    private List<Listener> listeners = new ArrayList<Listener>();

    private List<Panel> panels;

    public void add(Listener listener)
    {
        this.listeners.add(listener);
    }


    public List<Listener> getListeners()
    {
        return listeners;
    }

    public List<Panel> getPanels()
    {
        return panels;
    }

    public void fillWithDefault()
    {
        if (listeners == null)
        {
            listeners = Collections.emptyList();
        }
    }
}

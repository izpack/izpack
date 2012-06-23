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

package com.izforge.izpack.api.panels;

/**
 * Created by IntelliJ IDEA. User: sora Date: Dec 7, 2009 Time: 7:48:08 AM To change this template
 * use File | Settings | File Templates.
 */
public interface IShortcutPanelLogic
{

    /**
     * Creates the shortcuts at a specified time. Before this function can be called a ShortcutPanel
     * must be used to initialize the logic properly.
     * 
     * @throws Exception
     */
    public void createAndRegisterShortcuts() throws Exception;

    /**
     * Tell the ShortcutPaneld to not create the shortcuts immediately after clicking next.
     * 
     * @param createShortcutsImmediately
     */
    public void setCreateShortcutsImmediately(boolean createShortcutsImmediately);

    /**
     * @return <code>true</code> it the shortcuts will be created after clicking next, otherwise
     * <code>false</code>
     */
    public boolean isCreateShortcutsImmediately();
}

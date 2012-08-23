/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2010 Rene Krell
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class contains data and 'perform' logic for configuration action listeners.
 *
 * @author Rene Krell
 */
public class ConfigurationAction extends ActionBase
{
    private static final long serialVersionUID = 3258131345250005557L;

    private static final transient Logger logger = Logger.getLogger(ConfigurationAction.class.getName());

    private List<ConfigurationActionTask> actionTasks = null;

    /**
     * Default constructor
     */
    public ConfigurationAction()
    {
        super();
    }

    /**
     * Performs all defined install actions.
     *
     * @throws Exception
     */
    public void performInstallAction() throws Exception
    {
        logger.fine("Found " + actionTasks.size() + " configuration tasks");
        for (ConfigurationActionTask task : actionTasks)
        {
            task.execute();
        }
    }

    public List<ConfigurationActionTask> getActionTasks()
    {
        return actionTasks;
    }

    public void setActionTasks(List<ConfigurationActionTask> configtasks)
    {
        this.actionTasks = configtasks;
    }

    public void addActionTasks(List<ConfigurationActionTask> configtasks)
    {
        if (configtasks == null)
        {
            configtasks = new ArrayList<ConfigurationActionTask>();
        }
        this.actionTasks.addAll(configtasks);
    }
}

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

package com.izforge.izpack.installer.requirement;

import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.InstallerRequirement;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.installer.RequirementChecker;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;

/**
 * Evaluates each {@link InstallerRequirement} returned by {@link InstallData#getInstallerRequirements()}
 * to determine if installation should proceed.
 *
 * @author Tim Anderson
 */
public class InstallerRequirementChecker implements RequirementChecker
{

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * The rules engine.
     */
    private final RulesEngine rules;

    /**
     * The prompt.
     */
    private final Prompt prompt;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(InstallerRequirementChecker.class.getName());


    /**
     * Constructs a <tt>InstallerRequirementChecker</tt>.
     *
     * @param installData the installation data.
     * @param rules       the rules engine
     * @param prompt      the prompt
     */
    public InstallerRequirementChecker(InstallData installData, RulesEngine rules, Prompt prompt)
    {
        this.installData = installData;
        this.rules = rules;
        this.prompt = prompt;
    }

    /**
     * Determines if installation requirements are met.
     *
     * @return <tt>true</tt> if requirements are met, otherwise <tt>false</tt>
     */
    @Override
    public boolean check()
    {
        boolean fail = false;
        Messages messages = installData.getMessages();

        for (InstallerRequirement requirement : installData.getInstallerRequirements())
        {
            String id = requirement.getCondition();
            Condition condition = rules.getCondition(id);
            if (condition == null)
            {
                fail = true;
                logger.warning(id + " is not a valid condition.");
                break;
            }
            if (!condition.isTrue())
            {
                fail = true;
                String message = requirement.getMessage();
                if (message != null)
                {
                    String localizedMessage = messages.get(message);
                    prompt.message(Prompt.Type.ERROR, localizedMessage);
                }
                break;
            }
        }
        return !fail;
    }

}

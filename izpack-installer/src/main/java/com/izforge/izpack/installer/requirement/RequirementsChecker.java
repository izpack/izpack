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

import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.installer.RequirementChecker;


/**
 * Verifies all installation requirements are met. This should be used prior to installation commencing.
 *
 * @author Tim Anderson
 */
public class RequirementsChecker implements RequirementChecker
{
    /**
     * The variables.
     */
    private final Variables variables;

    /**
     * The language pack checker.
     */
    private final LangPackChecker langChecker;

    /**
     * The version checker.
     */
    private final JavaVersionChecker versionChecker;

    /**
     * The JDK checker.
     */
    private final JDKChecker jdkChecker;

    /**
     * The lock file checker.
     */
    private final LockFileChecker lockChecker;

    /**
     * The installer requirement checker.
     */
    private final InstallerRequirementChecker installerRequirementChecker;

    /**
     * Constructs a <tt>RequirementsChecker</tt>.
     *
     * @param variables                   the variables. These are refreshed prior to checking requirements
     * @param langChecker                 the language pack checker
     * @param versionChecker              the java version checker
     * @param jdkChecker                  the JDK checker
     * @param lockChecker                 the lock file checker
     * @param installerRequirementChecker the installer requirement checker
     */
    public RequirementsChecker(Variables variables, LangPackChecker langChecker, JavaVersionChecker versionChecker,
                               JDKChecker jdkChecker, LockFileChecker lockChecker,
                               InstallerRequirementChecker installerRequirementChecker)
    {
        this.variables = variables;
        this.versionChecker = versionChecker;
        this.jdkChecker = jdkChecker;
        this.lockChecker = lockChecker;
        this.langChecker = langChecker;
        this.installerRequirementChecker = installerRequirementChecker;
    }

    /**
     * Determines if installation requirements are met.
     *
     * @return <tt>true</tt> if requirements are met, otherwise <tt>false</tt>
     */
    @Override
    public boolean check()
    {
        variables.refresh();
        return langChecker.check() && versionChecker.check() && jdkChecker.check() && lockChecker.check() &&
                installerRequirementChecker.check();
    }
}

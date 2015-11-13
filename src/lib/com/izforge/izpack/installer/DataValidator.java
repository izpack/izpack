/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2008 Patrick Zbinden.
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
package com.izforge.izpack.installer;

public interface DataValidator
{

    public enum Status {
        OK, WARNING, ERROR
    }

    /**
     * tag-name of the datavalidator
     */
    public static final String DATA_VALIDATOR_TAG = "validator";

    /**
     * attribute for class to use
     */
    public static final String DATA_VALIDATOR_CLASSNAME_TAG = "classname";

    /**
     * Method to validate on {@link AutomatedInstallData}
     * 
     * @param adata
     * @return {@link Status} the result of the validation
     */
    public Status validateData(final AutomatedInstallData adata);

    /**
     * Returns the string with messageId for an error
     * 
     * @return String the messageId
     */
    public String getErrorMessageId();

    /**
     * Returns the string with messageId for a warning
     * 
     * @return String the messageId
     */
    public String getWarningMessageId();

    /**
     * if Installer is run in automated mode, and validator returns a warning, this method is asked,
     * how to go on
     * 
     * @return boolean
     */

    public boolean getDefaultAnswer();
}

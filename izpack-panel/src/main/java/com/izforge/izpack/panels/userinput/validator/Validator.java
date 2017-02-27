/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Elmar Grom
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
package com.izforge.izpack.panels.userinput.validator;

import com.izforge.izpack.panels.userinput.processorclient.ProcessingClient;

/*---------------------------------------------------------------------------*/
/**
 * Interface for classes that provide rule validation services.
 *
 * @author Elmar Grom
 * @version 0.0.1 / 10/26/02
 */
/*---------------------------------------------------------------------------*/
public interface Validator
{

    /*--------------------------------------------------------------------------*/
    /**
     * Validates the contend of an input field.
     *
     * @param client the client object using the services of this validator.
     * @return <code>true</code> if the validation passes, otherwise
     * <code>false</code>.
     */
    /*--------------------------------------------------------------------------*/
    public boolean validate(ProcessingClient client);
}
/*---------------------------------------------------------------------------*/

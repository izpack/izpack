/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2003 Elmar Grom
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
package com.izforge.izpack.panels.userinput.processorclient;

import java.util.Map;

/*---------------------------------------------------------------------------*/
/**
 * Implement this interface in any class that wants to use processing or
 * validation services.
 *
 * @author Elmar Grom
 * @version 0.0.1 / 2/22/03
 * @see com.izforge.izpack.panels.userinput.processor.Processor
 * @see com.izforge.izpack.panels.userinput.validator.Validator
 */
/*---------------------------------------------------------------------------*/
public interface ProcessingClient
{

    /*--------------------------------------------------------------------------*/
    /**
     * Returns the number of sub-fields.
     *
     * @return the number of sub-fields
     */
    /*--------------------------------------------------------------------------*/
    public int getNumFields();

    /*--------------------------------------------------------------------------*/
    /**
     * Returns the contents of the field indicated by <code>index</code>.
     *
     * @param index the index of the sub-field from which the contents is
     * requested.
     * @return the contents of the indicated sub-field.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    /*--------------------------------------------------------------------------*/
    public String getFieldContents(int index);

// These newly added fields are similar to the functionality provided
// by the multiple validator support using the validator container.

    /*---------------------------------------------------------------------------*/
    /**
     * Returns the field contents.
     *
     * @return the field contents
     */
    /*--------------------------------------------------------------------------*/
    public String getText();

    /*--------------------------------------------------------------------------*/
    /**
     * @return true if this instance has any parameters to pass to the Validator
     * instance.
     */
    /*--------------------------------------------------------------------------*/
    public boolean hasParams();

    /*--------------------------------------------------------------------------*/
    /**
     * Returns the validator parameters, if any. The caller should check for the
     * existence of validator parameters via the <code>hasParams()</code> method
     * prior to invoking this method.
     *
     * @return a java.util.Map containing the validator parameters.
     */
    /*--------------------------------------------------------------------------*/
    public Map<String, String> getValidatorParams();

}
/*---------------------------------------------------------------------------*/

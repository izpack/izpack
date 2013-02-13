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

package com.izforge.izpack.panels.userinput.field.check;

import com.izforge.izpack.panels.userinput.field.FieldConfig;


/**
 * Configuration for 'check' fields.
 *
 * @author Tim Anderson
 */
public interface CheckFieldConfig extends FieldConfig
{

    /**
     * Returns the value to assign to the associated variable when the checkbox is selected (i.e. is 'true').
     *
     * @return the 'true' value. May be {@code null}
     */
    String getTrueValue();

    /**
     * Returns the value to assign to the associated variable when the checkbox is not selected (i.e. is 'false').
     *
     * @return the 'false' value
     */
    String getFalseValue();
}

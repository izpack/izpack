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

package com.izforge.izpack.panels.userinput.field;

import com.izforge.izpack.api.adaptator.IXMLElement;


/**
 * A field reader where the 'variable' is optional, and the 'spec' element ignored.
 *
 * @author Tim Anderson
 */
public class SimpleFieldReader extends FieldReader
{
    /**
     * Constructs a {@code SimpleFieldReader}.
     *
     * @param field  the field
     * @param config the configuration
     */
    public SimpleFieldReader(IXMLElement field, Config config)
    {
        super(field, config);
    }

    /**
     * Returns the variable that the field reads and updates.
     *
     * @return the 'variable' attribute, or {@code null} if the variable is not present
     */
    @Override
    public String getVariable()
    {
        return getConfig().getString(getField(), VARIABLE, null);
    }

    /**
     * Returns the 'spec' element.
     *
     * @param field  the parent field element
     * @param config the configuration
     * @return {@code null}
     */
    @Override
    protected IXMLElement getSpec(IXMLElement field, Config config)
    {
        return null;
    }

}

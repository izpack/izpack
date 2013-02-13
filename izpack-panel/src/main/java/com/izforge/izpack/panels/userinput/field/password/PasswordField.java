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

package com.izforge.izpack.panels.userinput.field.password;


/**
 * Password field.
 *
 * @author Tim Anderson
 */
public class PasswordField
{
    /**
     * The password field label.
     */
    private final String label;

    /**
     * The password field size.
     */
    private int size;

    /**
     * The initial value of the field.
     */
    private final String set;

    /**
     * Constructs a {@code PasswordField}.
     *
     * @param label the password field label may be {@code null}
     * @param size the password field size
     * @param set the initial value of the field. May be {@code null}
     */
    public PasswordField(String label, int size, String set)
    {
        this.label = label;
        this.size = size;
        this.set = set;
    }

    /**
     * Returns the password field label.
     *
     * @return the password field label. May be {@code null}
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * Returns the password field size.
     *
     * @return the password field size
     */
    public int getSize()
    {
        return size;
    }

    /**
     * Returns the initial value.
     *
     * @return the initial value. May be {@code null}
     */
    public String getSet()
    {
        return set;
    }
}

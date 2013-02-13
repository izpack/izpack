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

package com.izforge.izpack.panels.userinput.field;


/**
 * Status of running a {@link FieldValidator}.
 *
 * @author Tim Anderson
 */
public class ValidationStatus
{

    /**
     * Determines if the field is valid.
     */
    private final boolean valid;

    /**
     * Tbe field values.
     */
    private final String[] values;

    /**
     * The validation error message.
     */
    private final String message;

    /**
     * Constructs a {@code ValidationStatus}.
     *
     * @param valid   indicates if validation was successful or not
     * @param values  the field values
     * @param message the validation error message, or {@code null} if validation was successful
     */
    public ValidationStatus(boolean valid, String[] values, String message)
    {
        this.valid = valid;
        this.values = values;
        this.message = message;
    }

    /**
     * Creates a {@link ValidationStatus} indicating that validation was successful.
     *
     * @param values the field values
     * @return a new validation status
     */
    public static ValidationStatus success(String[] values)
    {
        return new ValidationStatus(true, values, null);
    }

    /**
     * Creates a {@link ValidationStatus} indicating that validation failed.
     *
     * @param message the validation error message
     * @return a new validation status
     */
    public static ValidationStatus failed(String message)
    {
        return new ValidationStatus(false, null, message);
    }

    /**
     * Determines if validation was successful.
     *
     * @return {@code true} if validation was successful
     */
    public boolean isValid()
    {
        return valid;
    }

    /**
     * Returns the field values.
     *
     * @return the field values
     */
    public String[] getValues()
    {
        return values;
    }

    /**
     * The validation error message.
     *
     * @return the validation error message, {@code null} if validation was successful
     */
    public String getMessage()
    {
        return message;
    }
}

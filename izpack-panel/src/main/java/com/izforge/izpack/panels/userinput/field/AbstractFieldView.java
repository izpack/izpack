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
 * Abstract implementation of {@link FieldView}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractFieldView implements FieldView
{
    /**
     * The field.
     */
    private final Field field;

    /**
     * Determines if the view is being displayed.
     */
    private boolean displayed = false;


    /**
     * Constructs an {@link AbstractFieldView}.
     *
     * @param field the field
     */
    public AbstractFieldView(Field field)
    {
        this.field = field;
    }

    /**
     * Returns the field.
     *
     * @return the field
     */
    @Override
    public Field getField()
    {
        return field;
    }

    /**
     * Returns the variable associated with the field.
     *
     * @return the variable, or {@code null} if the field doesn't update a variable
     */
    public String getVariable()
    {
        return field.getVariable();
    }

    public void setVariable(String newVariableName)
    {
        field.setVariable(newVariableName);
    }

    /**
     * Returns the summary key  associated with the field.
     *
     * @return the summary key, or {@code null} if the field doesn't update a summary
     */
    public String getSummaryKey()
    {
        return field.getSummaryKey();
    }

    /**
     * Determines if the view is being displayed.
     *
     * @return {@code true} if the view is being displayed
     */
    @Override
    public boolean isDisplayed()
    {
        return displayed;
    }

    /**
     * Determines if the view is being displayed.
     *
     * @param displayed {@code true} if the view is being displayed
     */
    @Override
    public void setDisplayed(boolean displayed)
    {
        this.displayed = displayed;
    }
}

/*
 * Copyright  2000-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.izforge.izpack.util.file.types;

/**
 * Base class for those classes that can appear inside the build file as stand
 * alone data types.
 * <p/>
 * <p>
 * This class handles the common description attribute and provides a default
 * implementation for reference handling and checking for circular references
 * that is appropriate for types that can not be nested inside elements of the
 * same type (i.e. &lt;patternset&gt; but not &lt;path&gt;).</p>
 */
public abstract class DataType
{

    /**
     * The description the user has set.
     */
    private String description;

    /**
     * Are we sure we don't hold circular references?
     * <p/>
     * <p>
     * Subclasses are responsible for setting this value to false if we'd need
     * to investigate this condition (usually because a child element has been
     * added that is a subclass of DataType).</p>
     */
    private boolean checked = true;

    /**
     * Sets a description of the current data type. It will be useful in
     * commenting what we are doing.
     */
    public void setDescription(final String desc)
    {
        description = desc;
    }

    /**
     * Return the description for the current data type.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Creates an exception that indicates that refid has to be the only
     * attribute if it is set.
     */
    protected Exception tooManyAttributes()
    {
        return new Exception("You must not specify more than one "
                + "attribute when using refid");
    }

    /**
     * Creates an exception that indicates that this XML element must not have
     * child elements if the refid attribute is set.
     */
    protected Exception noChildrenAllowed()
    {
        return new Exception("You must not specify nested elements "
                + "when using refid");
    }

    protected boolean isChecked()
    {
        return checked;
    }

    protected void setChecked(final boolean checked)
    {
        this.checked = checked;
    }
}

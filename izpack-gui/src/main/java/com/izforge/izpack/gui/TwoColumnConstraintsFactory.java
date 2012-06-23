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

package com.izforge.izpack.gui;

import com.izforge.izpack.api.adaptator.IXMLElement;


public class TwoColumnConstraintsFactory
{

    private static final String ALIGNMENT = "align";

    private static final String LABEL_ALIGNMENT = "label_align";

    private static final String CONTROL_ALIGNMENT = "control_align";

    private static final String LABEL_POSITION = "label_position";

    private static final String CONTROL_POSITION = "control_position";

    private static final String LABEL_INDENT = "label_indent";

    private static final String CONTROL_INDENT = "control_indent";

    private static final String LEFT = "left";

    private static final String CENTER = "center";

    private static final String RIGHT = "right";

    private static final String WEST = "west";

    private static final String WESTONLY = "westonly";

    private static final String EAST = "east";

    private static final String EASTONLY = "eastonly";

    private static final String BOTH = "both";

    private static final String TRUE = "true";

    private static final String FALSE = "false";

    public static TwoColumnConstraints createTextConstraint(IXMLElement field)
    {
        return createTextConstraint(field, TwoColumnConstraints.BOTH, false, false);
    }

    public static TwoColumnConstraints createLabelConstraint(IXMLElement field)
    {
        return createLabelConstraint(field, TwoColumnConstraints.WEST, false, false);
    }

    public static TwoColumnConstraints createControlConstraint(IXMLElement field)
    {
        return createControlConstraint(field, TwoColumnConstraints.EAST, false, false);
    }

    public static TwoColumnConstraints createTextConstraint(IXMLElement field, int position, boolean indent,
                                                            boolean stretch)
    {
        TwoColumnConstraints constraint = new TwoColumnConstraints();
        constraint.position = position;
        constraint.indent = indent;
        constraint.stretch = stretch;
        constraint.align = TwoColumnConstraints.LEFT;
        overrideTextDefaults(constraint, field);
        return constraint;
    }

    public static TwoColumnConstraints createControlConstraint(IXMLElement field, int position, boolean indent,
                                                               boolean stretch)
    {
        TwoColumnConstraints constraint = new TwoColumnConstraints();
        constraint.position = position;
        constraint.indent = indent;
        constraint.stretch = stretch;
        constraint.align = TwoColumnConstraints.LEFT;
        overrideControlDefaults(constraint, field);
        return constraint;
    }

    public static TwoColumnConstraints createLabelConstraint(IXMLElement field, int position, boolean indent,
                                                             boolean stretch)
    {
        TwoColumnConstraints constraint = new TwoColumnConstraints();
        constraint.position = position;
        constraint.indent = indent;
        constraint.stretch = stretch;
        constraint.align = TwoColumnConstraints.LEFT;
        overrideLabelDefaults(constraint, field);
        return constraint;
    }

    private static void overrideTextDefaults(TwoColumnConstraints constraint, IXMLElement field)
    {
        if (field != null)
        {
            overrideAlignment(constraint, field.getAttribute(ALIGNMENT));
            overrideAlignment(constraint, field.getAttribute(LABEL_ALIGNMENT));
        }
    }

    private static void overrideLabelDefaults(TwoColumnConstraints constraint, IXMLElement field)
    {
        if (field != null)
        {
            overrideAlignment(constraint, field.getAttribute(LABEL_ALIGNMENT));
            overridePosition(constraint, field.getAttribute(LABEL_POSITION));
            overrideIndent(constraint, field.getAttribute(LABEL_INDENT));
        }
    }

    private static void overrideControlDefaults(TwoColumnConstraints constraint, IXMLElement field)
    {
        if (field != null)
        {
            overrideAlignment(constraint, field.getAttribute(CONTROL_ALIGNMENT));
            overridePosition(constraint, field.getAttribute(CONTROL_POSITION));
            overrideIndent(constraint, field.getAttribute(CONTROL_INDENT));
        }
    }

    private static void overrideAlignment(TwoColumnConstraints constraint, String value)
    {
        if (value != null)
        {
            if (value.equalsIgnoreCase(LEFT))
            {
                constraint.align = TwoColumnConstraints.LEFT;
            }
            else if (value.equalsIgnoreCase(CENTER))
            {
                constraint.align = TwoColumnConstraints.CENTER;
            }
            else if (value.equalsIgnoreCase(RIGHT))
            {
                constraint.align = TwoColumnConstraints.RIGHT;
            }
        }
    }

    private static void overridePosition(TwoColumnConstraints constraint, String value)
    {
        if (value != null)
        {
            if (value.equalsIgnoreCase(WEST))
            {
                constraint.position = TwoColumnConstraints.WEST;
            }
            else if (value.equalsIgnoreCase(EAST))
            {
                constraint.position = TwoColumnConstraints.EAST;
            }
            else if (value.equalsIgnoreCase(BOTH))
            {
                constraint.position = TwoColumnConstraints.BOTH;
            }
            else if (value.equalsIgnoreCase(WESTONLY))
            {
                constraint.position = TwoColumnConstraints.WESTONLY;
            }
            else if (value.equalsIgnoreCase(EASTONLY))
            {
                constraint.position = TwoColumnConstraints.EASTONLY;
            }
        }
    }

    private static void overrideIndent(TwoColumnConstraints constraint, String value)
    {
        if (value != null)
        {
            if (value.equalsIgnoreCase(TRUE))
            {
                constraint.indent = true;
            }
            else if (value.equalsIgnoreCase(FALSE))
            {
                constraint.indent = false;
            }
        }
    }
}

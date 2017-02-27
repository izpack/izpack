/*
 * Copyright  2002-2005 The Apache Software Foundation
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
package com.izforge.izpack.util.file.types.selectors;

import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.util.file.types.Parameter;

/**
 * Selector that filters files based on their size.
 */
public class SizeSelector extends BaseExtendSelector
{

    private long size = -1;
    private long multiplier = 1;
    private long sizelimit = -1;
    private SizeComparisons cmp = SizeComparisons.EQUAL;
    /**
     * Used for parameterized custom selector
     */
    public static final String SIZE_KEY = "value";
    /**
     * Used for parameterized custom selector
     */
    public static final String UNITS_KEY = "units";
    /**
     * Used for parameterized custom selector
     */
    public static final String WHEN_KEY = "when";

    /**
     * Creates a new <code>SizeSelector</code> instance.
     */
    public SizeSelector()
    {
    }

    /**
     * Returns a <code>String</code> object representing the specified
     * SizeSelector. This is "{sizeselector value: " + <"compare",
     * "less", "more", "equal"> + "}".
     *
     * @return a string describing this object
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer("{sizeselector value: ");
        buf.append(sizelimit);
        buf.append("compare: ");
        buf.append(cmp.getAttribute());
        buf.append("}");
        return buf.toString();
    }

    /**
     * A size selector needs to know what size to base its selecting on. This
     * will be further modified by the multiplier to get an actual size limit.
     *
     * @param size the size to select against expressed in units.
     */
    public void setValue(long size)
    {
        this.size = size;
        if ((multiplier != 0) && (size > -1))
        {
            sizelimit = size * multiplier;
        }
    }

    /**
     * Sets the units to use for the comparison. This is a little complicated
     * because common usage has created standards that play havoc with
     * capitalization rules. Thus, some people will use "K" for indicating
     * 1000's, when the SI standard calls for "k". Others have tried to
     * introduce "K" as a multiple of 1024, but that falls down when you reach
     * "M", since "m" is already defined as 0.001.
     * <p/>
     * To get around this complexity, a number of standards bodies have proposed
     * the 2^10 standard, and at least one has adopted it. But we are still left
     * with a populace that isn't clear on how capitalization should work.
     * <p/>
     * We therefore ignore capitalization as much as possible. Completely mixed
     * case is not possible, but all upper and lower forms are accepted for all
     * long and short forms. Since we have no need to work with the 0.001 case,
     * this practice works here.
     * <p/>
     * This function translates all the long and short forms that a unit prefix
     * can occur in and translates them into a single multiplier.
     *
     * @param units The units to compare the size to, using an
     * EnumeratedAttribute.
     */
    public void setUnits(ByteUnits units)
    {
        multiplier = 0;

        switch (units)
        {
            case K:
            case k:
            case kilo:
            case KILO:
                multiplier = 1000;
                break;

            case Ki:
            case KI:
            case ki:
            case kibi:
            case KIBI:
                multiplier = 1024;
                break;

            case M:
            case m:
            case mega:
            case MEGA:
                multiplier = 1000000;
                break;

            case Mi:
            case MI:
            case mi:
            case mebi:
            case MEBI:
                multiplier = 1048576;
                break;

            case G:
            case g:
            case giga:
            case GIGA:
                multiplier = 1000000000L;
                break;

            case Gi:
            case GI:
            case gi:
            case gibi:
            case GIBI:
                multiplier = 1073741824L;
                break;

            case T:
            case t:
            case tera:
            case TERA:
                multiplier = 1000000000000L;
                break;

            case Ti:
            case TI:
            case ti:
            case tebi:
            case TEBI:
                multiplier = 1099511627776L;
                break;

            default:
                break;
        }

        if ((multiplier > 0) && (size > -1))
        {
            sizelimit = size * multiplier;
        }
    }

    /**
     * This specifies when the file should be selected, whether it be when the
     * file matches a particular size, when it is smaller, or whether it is
     * larger.
     *
     * @param scmp The comparison to perform, an EnumeratedAttribute.
     */
    public void setWhen(SizeComparisons scmp)
    {
        this.cmp = scmp;
    }

    /**
     * When using this as a custom selector, this method will be called. It
     * translates each parameter into the appropriate setXXX() call.
     *
     * @param parameters the complete set of parameters for this selector.
     */
    public void setParameters(Parameter[] parameters)
    {
        super.setParameters(parameters);
        if (parameters != null)
        {
            for (Parameter parameter : parameters)
            {
                String paramname = parameter.getName();
                if (SIZE_KEY.equalsIgnoreCase(paramname))
                {
                    try
                    {
                        setValue(new Long(parameter.getValue()
                        ).longValue());
                    }
                    catch (NumberFormatException nfe)
                    {
                        setError("Invalid size setting " + parameter.getValue());
                    }
                }
                else if (UNITS_KEY.equalsIgnoreCase(paramname))
                {
                    ByteUnits units = ByteUnits.getFromAttribute(parameter.getValue());
                    if (units != null)
                    {
                        setUnits(units);
                    }
                    else
                    {
                        setError("Invalid " + UNITS_KEY + " setting " + parameter.getValue());
                    }
                }
                else if (WHEN_KEY.equalsIgnoreCase(paramname))
                {
                    SizeComparisons cmp = SizeComparisons.getFromAttribute(parameter.getValue());
                    if (cmp != null)
                    {
                        setWhen(cmp);
                    }
                    else
                    {
                        setError("Invalid " + WHEN_KEY + " setting " + parameter.getValue());
                    }
                }
                else
                {
                    setError("Invalid parameter " + paramname);
                }
            }
        }
    }

    /**
     * <p>
     * Checks to make sure all settings are kosher. In this case, it means that
     * the size attribute has been set (to a positive value), that the
     * multiplier has a valid setting, and that the size limit is valid. Since
     * the latter is a calculated value, this can only fail due to a programming
     * error.
     * </p>
     * <p>
     * If a problem is detected, the setError() method is called.
     * </p>
     */
    public void verifySettings()
    {
        if (size < 0)
        {
            setError("The value attribute is required, and must be positive");
        }
        else if (multiplier < 1)
        {
            setError("Invalid Units supplied, must be K,Ki,M,Mi,G,Gi,T,or Ti");
        }
        else if (sizelimit < 0)
        {
            setError("Internal error: Code is not setting sizelimit correctly");
        }
    }

    /**
     * The heart of the matter. This is where the selector gets to decide on the
     * inclusion of a file in a particular fileset.
     *
     * @param basedir A java.io.File object for the base directory.
     * @param filename The name of the file to check.
     * @param file A File object for this filename.
     * @return whether the file should be selected or not.
     */
    public boolean isSelected(InstallData idata, File basedir, String filename, File file)
            throws Exception
    {
        // throw Exception on error
        validate();

        // Directory size never selected for
        if (file.isDirectory())
        {
            return true;
        }
        switch (cmp)
        {
            case LESS:
                return (file.length() < sizelimit);

            case MORE:
                return (file.length() > sizelimit);

            default:
                return (file.length() == sizelimit);
        }
    }

    /**
     * Enumerated attribute with the values for units.
     * <p/>
     * This treats the standard SI units as representing powers of ten, as they
     * should. If you want the powers of 2 that approximate the SI units, use
     * the first two characters followed by a <code>bi</code>. So 1024 (2^10)
     * becomes <code>kibi</code>, 1048576 (2^20) becomes <code>mebi</code>,
     * 1073741824 (2^30) becomes <code>gibi</code>, and so on. The symbols are
     * also accepted, and these are the first letter capitalized followed by an
     * <code>i</code>. <code>Ki</code>, <code>Mi</code>, <code>Gi</code>, and so
     * on. Capitalization variations on these are also accepted.
     * <p/>
     * This binary prefix system is approved by the IEC and appears on its way
     * for approval by other agencies, but it is not an SI standard. It
     * disambiguates things for us, though.
     */
    public enum ByteUnits
    {
        K("K"), k("k"), kilo("kilo"), KILO("KILO"),
        Ki("Ki"), KI("KI"), ki("ki"), kibi("kibi"), KIBI("KIBI"),
        M("M"), m("m"), mega("mega"), MEGA("MEGA"),
        Mi("Mi"), MI("MI"), mi("mi"), mebi("mebi"), MEBI("MEBI"),
        G("G"), g("g"), giga("giga"), GIGA("GIGA"),
        Gi("Gi"), GI("GI"), gi("gi"), gibi("gibi"), GIBI("GIBI"),
        T("T"), t("t"), tera("tera"), TERA("TERA"),
        Ti("Ti"), TI("TI"), ti("ti"), tebi("tebi"), TEBI("TEBI");

        private static Map<String, ByteUnits> lookup;

        private String attribute;

        ByteUnits(String attribute)
        {
            this.attribute = attribute;
        }

        static
        {
            lookup = new HashMap<String, ByteUnits>();
            for (ByteUnits mapperType : EnumSet.allOf(ByteUnits.class))
            {
                lookup.put(mapperType.getAttribute(), mapperType);
            }
        }

        public String getAttribute()
        {
            return attribute;
        }

        public static ByteUnits getFromAttribute(String attribute)
        {
            if (attribute != null && lookup.containsKey(attribute))
            {
                return lookup.get(attribute);
            }
            return null;
        }
    }

    public enum SizeComparisons
    {
        LESS("less"), MORE("more"), EQUAL("equal");

        private static Map<String, SizeComparisons> lookup;

        private String attribute;

        SizeComparisons(String attribute)
        {
            this.attribute = attribute;
        }

        static
        {
            lookup = new HashMap<String, SizeComparisons>();
            for (SizeComparisons mapperType : EnumSet.allOf(SizeComparisons.class))
            {
                lookup.put(mapperType.getAttribute(), mapperType);
            }
        }

        public String getAttribute()
        {
            return attribute;
        }

        public static SizeComparisons getFromAttribute(String attribute)
        {
            if (attribute != null && lookup.containsKey(attribute))
            {
                return lookup.get(attribute);
            }
            return null;
        }
    }
}

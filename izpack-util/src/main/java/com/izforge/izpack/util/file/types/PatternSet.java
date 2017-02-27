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

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Named collection of include/exclude tags.
 * <p/>
 * <p>
 * Moved out of MatchingTask to make it a standalone object that could be
 * referenced (by scripts for example).
 */
public class PatternSet extends DataType implements Cloneable
{

    private Vector<NameEntry> includeList = new Vector<NameEntry>();
    private Vector<NameEntry> excludeList = new Vector<NameEntry>();

    /**
     * inner class to hold a name on list. "If" and "Unless" attributes may be
     * used to invalidate the entry based on the existence of a property
     * (typically set thru the use of the Available task).
     */
    public class NameEntry
    {

        private String name;
        private String ifCond;
        private String unlessCond;

        /**
         * Sets the name pattern.
         *
         * @param name The pattern string.
         */
        public void setName(String name)
        {
            this.name = name;
        }

        /**
         * Sets the if attribute. This attribute and the "unless" attribute are
         * used to validate the name, based in the existence of the property.
         *
         * @param cond A property name. If this property is not present, the
         * name is invalid.
         */
        public void setIf(String cond)
        {
            ifCond = cond;
        }

        /**
         * Sets the unless attribute. This attribute and the "if" attribute are
         * used to validate the name, based in the existence of the property.
         *
         * @param cond A property name. If this property is present, the name is
         * invalid.
         */
        public void setUnless(String cond)
        {
            unlessCond = cond;
        }

        /**
         * @return the name attribute.
         */
        public String getName()
        {
            return name;
        }

        /**
         * @return a printable form of this object.
         */
        public String toString()
        {
            if (name == null)
            {
                throw new RuntimeException(
                        "Missing attribute \"name\" for a pattern");
            }
            StringBuffer buf = new StringBuffer(name);
            if ((ifCond != null) || (unlessCond != null))
            {
                buf.append(":");
                String connector = "";

                if (ifCond != null)
                {
                    buf.append("if->");
                    buf.append(ifCond);
                    connector = ";";
                }
                if (unlessCond != null)
                {
                    buf.append(connector);
                    buf.append("unless->");
                    buf.append(unlessCond);
                }
            }

            return buf.toString();
        }
    }

    /**
     * Creates a new <code>PatternSet</code> instance.
     */
    public PatternSet()
    {
        super();
    }

    /**
     * This is a patternset nested element.
     *
     * @param p a configured patternset nested element.
     */
    public void addConfiguredPatternset(PatternSet p)
    {
        String[] nestedIncludes = p.getIncludePatterns();
        String[] nestedExcludes = p.getExcludePatterns();

        if (nestedIncludes != null)
        {
            for (String nestedInclude : nestedIncludes)
            {
                createInclude().setName(nestedInclude);
            }
        }

        if (nestedExcludes != null)
        {
            for (String nestedExclude : nestedExcludes)
            {
                createExclude().setName(nestedExclude);
            }
        }
    }

    /**
     * add a name entry on the include list
     *
     * @return a nested include element to be configured.
     */
    public NameEntry createInclude()
    {
        return addPatternToList(includeList);
    }

    /**
     * add a name entry on the exclude list
     *
     * @return a nested exclude element to be configured.
     */
    public NameEntry createExclude()
    {
        return addPatternToList(excludeList);
    }

    /**
     * Appends <code>includes</code> to the current list of include patterns.
     * Patterns may be separated by a comma or a space.
     *
     * @param includes the string containing the include patterns
     */
    public void setIncludes(String includes)
    {
        if (includes != null && includes.length() > 0)
        {
            StringTokenizer tok = new StringTokenizer(includes, ", ", false);
            while (tok.hasMoreTokens())
            {
                createInclude().setName(tok.nextToken());
            }
        }
    }

    /**
     * Appends <code>excludes</code> to the current list of exclude patterns.
     * Patterns may be separated by a comma or a space.
     *
     * @param excludes the string containing the exclude patterns
     */
    public void setExcludes(String excludes)
    {
        if (excludes != null && excludes.length() > 0)
        {
            StringTokenizer tok = new StringTokenizer(excludes, ", ", false);
            while (tok.hasMoreTokens())
            {
                createExclude().setName(tok.nextToken());
            }
        }
    }

    /**
     * add a name entry to the given list
     */
    private NameEntry addPatternToList(Vector<NameEntry> list)
    {
        NameEntry result = new NameEntry();
        list.addElement(result);
        return result;
    }

    /**
     * Adds the patterns of the other instance to this set.
     *
     * @param other the other PatternSet instance.
     * @param p the current project.
     */
    public void append(PatternSet other/*, Project p*/)
    {
        String[] incl = other.getIncludePatterns(/*p*/);
        if (incl != null)
        {
            for (String anIncl : incl)
            {
                createInclude().setName(anIncl);
            }
        }

        String[] excl = other.getExcludePatterns(/*p*/);
        if (excl != null)
        {
            for (String anExcl : excl)
            {
                createExclude().setName(anExcl);
            }
        }
    }

    /**
     * Returns the filtered include patterns.
     *
     * @param p the current project.
     * @return the filtered included patterns.
     */
    public String[] getIncludePatterns()
    {
        return makeArray(includeList);
    }

    /**
     * Returns the filtered include patterns.
     *
     * @param p the current project.
     * @return the filtered excluded patterns.
     */
    public String[] getExcludePatterns()
    {
        return makeArray(excludeList);
    }

    /**
     * helper for FileSet.
     */
    boolean hasPatterns()
    {
        return includeList.size() > 0 || excludeList.size() > 0;
    }

    /**
     * Convert a vector of NameEntry elements into an array of Strings.
     */
    private String[] makeArray(Vector<NameEntry> list)
    {
        if (list.size() == 0)
        {
            return null;
        }

        Vector<String> tmpNames = new Vector<String>();
        for (NameEntry ne : list)
        {
            String pattern = ne.getName();
            if (pattern != null && pattern.length() > 0)
            {
                tmpNames.addElement(pattern);
            }
        }

        String[] result = new String[tmpNames.size()];
        tmpNames.copyInto(result);
        return result;
    }

    /**
     * @return a printable form of this object.
     */
    public String toString()
    {
        return "patternSet{ includes: " + includeList
                + " excludes: " + excludeList + " }";
    }

}

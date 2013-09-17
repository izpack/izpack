/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2010 Rene Krell
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

package com.izforge.izpack.util.regex;

import java.util.Enumeration;
import java.util.Vector;

/**
 * *************************************************************************
 * Regular Expression utilities
 *
 * @author <a href='mailto:mattinger@yahoo.com'>Matthew Inger</a> (original Ant Contrib implementation part)
 * @author René Krell - changes against the original implementation in Apache Ant 1.6.5 and ant-contrib 1.0b3
 *         **************************************************************************
 * @see <a href='http://ant.apache.org'>Apache Ant</a>
 * @see <a href='http://ant-contrib.sourceforge.net'>Ant Contrib project</a>
 */

public class RegexUtil
{
    /**
     * An abritrary node in a select expression
     */
    private static interface SelectNode
    {
        /**
         * Select the value based on the groups
         *
         * @param groups The groups found in the match
         */
        public String select(Vector<String> groups);
    }

    /**
     * A group node in a select expression
     */
    private static class GroupSelectNode
            implements SelectNode
    {
        private int groupNumber;

        public GroupSelectNode(int groupNumber)
        {
            this.groupNumber = groupNumber;
        }

        public String select(Vector<String> groups)
        {
            if (groupNumber < groups.size())
            {
                return groups.elementAt(groupNumber);
            }
            else
            {
                return "\\" + groupNumber;
            }
        }

        public String toString()
        {
            return "group: " + groupNumber;
        }
    }

    /**
     * An abritrary node in a select expression
     */
    private static class StringSelectNode
            implements SelectNode
    {
        private String text;

        public StringSelectNode(String text)
        {
            this.text = text;
        }

        public String select(Vector<String> groups)
        {
            return text;
        }

        public String toString()
        {
            return "string: " + text;
        }
    }

    /**
     * Parses a select string into a List of SelectNode objects.
     * These objects can then be merged with a group list to produce
     * an output string (using the "select" method)
     *
     * @param input The select string
     * @return a List of SelectNode objects
     */
    private static Vector<SelectNode> parseSelectString(String input)
    {
        Vector<SelectNode> nodes = new Vector<SelectNode>();
        StringBuffer buf = new StringBuffer();
        char c[] = input.toCharArray();
        for (int i = 0; i < c.length; i++)
        {
            if (c[i] == '\\')
            {
                if (buf.length() > 0)
                {
                    nodes.addElement(new StringSelectNode(buf.toString()));
                    buf.setLength(0);
                }

                while (i + 1 < c.length && Character.isDigit(c[i + 1]))
                {
                    buf.append(c[i + 1]);
                    i++;
                }

                int groupNum = Integer.parseInt(buf.toString());
                buf.setLength(0);
                nodes.addElement(new GroupSelectNode(groupNum));
            }
            else
            {
                buf.append(c[i]);
            }
        }


        if (buf.length() > 0)
        {
            nodes.addElement(new StringSelectNode(buf.toString()));
            buf.setLength(0);
        }

        return nodes;
    }

    /**
     * Parse a select string, and merge it with a match groups
     * vector to produce an output string.  Each group placeholder
     * in the select string is replaced with the group at the
     * corresponding index in the match groups vector
     *
     * @param select The select string
     * @param groups The match groups
     * @return The output string with the merged selection
     */
    public static String select(String select, Vector<String> groups)
    {
        Vector<SelectNode> nodes = parseSelectString(select);

        StringBuffer buf = new StringBuffer();
        Enumeration<SelectNode> e = nodes.elements();
        SelectNode node = null;
        while (e.hasMoreElements())
        {
            node = e.nextElement();
            buf.append(node.select(groups));
        }
        return buf.toString();
    }

    /**
     * Check the options has a particular flag set.
     *
     * @param options an <code>int</code> value
     * @param flag    an <code>int</code> value
     * @return true if the flag is set
     */
    public static boolean hasFlag(int options, int flag)
    {
        return ((options & flag) > 0);
    }

    /**
     * Remove a particular flag from an int value contains the option flags.
     *
     * @param options an <code>int</code> value
     * @param flag    an <code>int</code> value
     * @return the options with the flag unset
     */
    public static int removeFlag(int options, int flag)
    {
        return (options & (0xFFFFFFFF - flag));
    }
    
    /**
     * convert regex option flag characters to regex options
     * <dl>
     *   <li>g -  Regexp.REPLACE_ALL</li>
     *   <li>i -  RegexpMatcher.MATCH_CASE_INSENSITIVE</li>
     *   <li>m -  RegexpMatcher.MATCH_MULTILINE</li>
     *   <li>s -  RegexpMatcher.MATCH_SINGLELINE</li>
     * </dl>
     * @param flags the string containing the flags
     * @return the Regexp option bits
     * @since Ant 1.8.2
     */
    public static int asOptions(String flags) {
        int options = RegexpMatcher.MATCH_DEFAULT;
        if (flags != null) {
            options = asOptions(flags.indexOf('i') == -1,
                                flags.indexOf('m') != -1,
                                flags.indexOf('s') != -1);
            if (flags.indexOf('g') != -1) {
                options |= Regexp.REPLACE_ALL;
            }
        }
        return options;
    }

    /**
     * Convert flag to regex options.
     *
     * @param caseSensitive opposite of RegexpMatcher.MATCH_CASE_INSENSITIVE
     * @return the Regexp option bits
     * @since Ant 1.8.2
     */
    public static int asOptions(boolean caseSensitive) {
        return asOptions(caseSensitive, false, false);
    }

    /**
     * Convert flags to regex options.
     *
     * @param caseSensitive opposite of RegexpMatcher.MATCH_CASE_INSENSITIVE
     * @param multiLine RegexpMatcher.MATCH_MULTILINE
     * @param singleLine RegexpMatcher.MATCH_SINGLELINE
     * @return the Regexp option bits
     * @since Ant 1.8.2
     */
    public static int asOptions(boolean caseSensitive, boolean multiLine,
                                boolean singleLine) {
        int options = RegexpMatcher.MATCH_DEFAULT;
        if (!caseSensitive) {
            options = options | RegexpMatcher.MATCH_CASE_INSENSITIVE;
        }
        if (multiLine) {
            options = options | RegexpMatcher.MATCH_MULTILINE;
        }
        if (singleLine) {
            options = options | RegexpMatcher.MATCH_SINGLELINE;
        }
        return options;
    }
}

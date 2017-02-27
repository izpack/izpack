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

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Implementation of RegexpMatcher for the built-in regexp matcher of JDK 1.4.
 * UNIX_LINES option is enabled as a default.
 *
 * @author René Krell - changes against the original implementation in Apache
 * Ant 1.6.5
 * @see <a href='http://ant.apache.org'>Apache Ant</a>
 */
public class JavaRegexpMatcher implements RegexpMatcher
{

    private String pattern;

    /**
     * Constructor for JakartaOroRegexp
     */
    public JavaRegexpMatcher()
    {
    }

    /**
     * Set the regexp pattern from the String description.
     *
     * @param pattern the pattern to match
     */
    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    /**
     * Get a String representation of the regexp pattern
     *
     * @return the pattern
     * @throws BuildException on error
     */
    public String getPattern()
    {
        return pattern;
    }

    /**
     * Get a compiled representation of the regexp pattern
     *
     * @param options the options
     * @return the compiled pattern
     * @throws BuildException on error
     */
    protected Pattern getCompiledPattern(int options)
            throws RuntimeException
    {
        int cOptions = getCompilerOptions(options);
        try
        {
            Pattern p = Pattern.compile(this.pattern, cOptions);
            return p;
        }
        catch (PatternSyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Does the given argument match the pattern using default options?
     *
     * @param argument the string to match against
     * @return true if the pattern matches
     * @throws BuildException on error
     */
    public boolean matches(String argument) throws RuntimeException
    {
        return matches(argument, MATCH_DEFAULT);
    }

    /**
     * Does the given argument match the pattern?
     *
     * @param input the string to match against
     * @param options the regex options to use
     * @return true if the pattern matches
     * @throws BuildException on error
     */
    public boolean matches(String input, int options)
            throws RuntimeException
    {
        try
        {
            Pattern p = getCompiledPattern(options);
            return p.matcher(input).find();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a Vector of matched groups found in the argument using default
     * options.
     * <p/>
     * <p>
     * Group 0 will be the full match, the rest are the parenthesized
     * subexpressions</p>.
     *
     * @param argument the string to match against
     * @return the vector of groups
     * @throws BuildException on error
     */
    public Vector<String> getGroups(String argument) throws RuntimeException
    {
        return getGroups(argument, MATCH_DEFAULT);
    }

    /**
     * Returns a Vector of matched groups found in the argument.
     * <p/>
     * <p>
     * Group 0 will be the full match, the rest are the parenthesized
     * subexpressions</p>.
     *
     * @param input the string to match against
     * @param options the regex options to use
     * @return the vector of groups
     * @throws BuildException on error
     */
    public Vector<String> getGroups(String input, int options) throws RuntimeException
    {
        Pattern p = getCompiledPattern(options);
        Matcher matcher = p.matcher(input);
        if (!matcher.find())
        {
            return null;
        }
        Vector<String> v = new Vector<String>();
        int cnt = matcher.groupCount();
        for (int i = 0; i <= cnt; i++)
        {
            String match = matcher.group(i);
            // treat non-matching groups as empty matches
            if (match == null)
            {
                match = "";
            }
            v.addElement(match);
        }
        return v;
    }

    /**
     * Convert the generic options to the regex compiler specific options.
     *
     * @param options the generic options
     * @return the specific options
     */
    protected int getCompilerOptions(int options)
    {
        // be strict about line separator
        int cOptions = Pattern.UNIX_LINES;

        if (RegexUtil.hasFlag(options, MATCH_CASE_INSENSITIVE))
        {
            cOptions |= Pattern.CASE_INSENSITIVE;
        }
        if (RegexUtil.hasFlag(options, MATCH_MULTILINE))
        {
            cOptions |= Pattern.MULTILINE;
        }
        if (RegexUtil.hasFlag(options, MATCH_SINGLELINE))
        {
            cOptions |= Pattern.DOTALL;
        }

        return cOptions;
    }

}

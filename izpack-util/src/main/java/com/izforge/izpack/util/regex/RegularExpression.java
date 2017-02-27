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

/**
 * A regular expression datatype. Keeps an instance of the compiled expression
 * for speed purposes. This compiled expression is lazily evaluated (it is
 * compiled the first time it is needed). The syntax is the dependent on which
 * regular expression type you are using. The system property
 * "izpack.regexp.regexpimpl" will be the classname of the implementation that
 * will be used.
 * <p/>
 * <
 * pre>
 * For jdk &lt;= 1.3, there are two available implementations:
 * org.apache.tools.ant.util.regexp.JakartaOroRegexp (the default) Based on the
 * jakarta-oro package
 * <p/>
 * org.apache.tools.ant.util.regexp.JakartaRegexpRegexp Based on the
 * jakarta-regexp package
 * <p/>
 * For jdk &gt;= 1.4 an additional implementation is available:
 * org.apache.tools.ant.util.regexp.Jdk14RegexpRegexp Based on the jdk 1.4 built
 * in regular expression package.
 * </pre>
 * <p/>
 * <
 * pre>
 * &lt;regexp [ [id="id"] pattern="expression" | refid="id" ] /&gt;
 * </pre>
 *
 * @author René Krell - changes against the original implementation in Apache
 * Ant 1.6.5
 * @see org.apache.oro.text.regex.Perl5Compiler
 * @see org.apache.regexp.RE
 * @see java.util.regex.Pattern
 * @see org.apache.tools.ant.util.regexp.Regexp
 * @see <a href='http://ant.apache.org'>Apache Ant</a>
 */
public class RegularExpression
{

    private boolean alreadyInit = false;

    // The regular expression factory
    private static final RegexpFactory FACTORY = new RegexpFactory();

    private Regexp regexp = null;
    // temporary variable
    private String myPattern;
    private boolean setPatternPending = false;

    private void init()
    {
        if (!alreadyInit)
        {
            this.regexp = FACTORY.newRegexp();
            alreadyInit = true;
        }
    }

    private void setPattern()
    {
        if (setPatternPending)
        {
            regexp.setPattern(myPattern);
            setPatternPending = false;
        }
    }

    /**
     * sets the regular expression pattern
     *
     * @param pattern regular expression pattern
     */
    public void setPattern(String pattern)
    {
        if (regexp == null)
        {
            myPattern = pattern;
            setPatternPending = true;
        }
        else
        {
            regexp.setPattern(pattern);
        }
    }

    /**
     * Gets the pattern string for this RegularExpression in the given project.
     *
     * @return pattern
     */
    public String getPattern()
    {
        init();
        setPattern();
        return regexp.getPattern();
    }

    /**
     * provides a reference to the Regexp contained in this
     *
     * @return Regexp instance associated with this RegularExpression instance
     */
    public Regexp getRegexp()
    {
        init();
        setPattern();
        return this.regexp;
    }
}

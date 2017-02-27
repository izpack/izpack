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
 * Regular expression factory, which will create Regexp objects. The actual
 * implementation class depends on the System or Ant Property:
 * <code>izpack.regexp.regexpimpl</code>.
 *
 * @author René Krell - changes against the original implementation in Apache
 * Ant 1.6.5
 * @see <a href='http://ant.apache.org'>Apache Ant</a>
 */
public class RegexpFactory extends RegexpMatcherFactory
{

    /**
     * Create a new regular expression matcher instance.
     *
     * @return the matcher instance
     * @throws BuildException on error
     */
    public Regexp newRegexp() throws RuntimeException
    {
        String systemDefault = System.getProperty("izpack.regexp.regexpimpl");
        if (systemDefault != null)
        {
            return createRegexpInstance(systemDefault);
            // XXX     should we silently catch possible exceptions and try to
            //         load a different implementation?
        }

        try
        {
            testAvailability("java.util.regex.Matcher");
            return createRegexpInstance("com.izforge.izpack.util.regex.JavaRegexp");
        }
        catch (Exception be)
        {
            // ignore
        }

        /*
        try {
            testAvailability("org.apache.oro.text.regex.Pattern");
            return createRegexpInstance("com.izforge.izpack.util.regex.JakartaOroRegexp");
        } catch (Exception be) {
            // ignore
        }
         */

 /*
        try {
            testAvailability("org.apache.regexp.RE");
            return createRegexpInstance("com.izforge.izpack.util.regex.JakartaRegexpRegexp");
        } catch (Exception be) {
            // ignore
        }
         */
        throw new RuntimeException("No supported regular expression matcher found");
    }

    /**
     * Wrapper over RegexpMatcherFactory.createInstance that ensures that we are
     * dealing with a Regexp implementation.
     *
     * @see RegexpMatcherFactory#createInstance(String)
     * @since 1.3
     */
    protected Regexp createRegexpInstance(String classname)
            throws RuntimeException
    {
        RegexpMatcher m = createInstance(classname);
        if (m instanceof Regexp)
        {
            return (Regexp) m;
        }
        else
        {
            throw new RuntimeException(classname + " doesn't implement the Regexp interface");
        }
    }

}

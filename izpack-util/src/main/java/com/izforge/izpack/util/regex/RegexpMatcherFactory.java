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
 * Simple Factory Class that produces an implementation of RegexpMatcher based
 * on the system property <code>izpack.regexp.matcherimpl</code> and the classes
 * available.
 * <p/>
 * <p>
 * In a more general framework this class would be abstract and have a static
 * newInstance method.</p>
 *
 * @author René Krell - changes against the original implementation in Apache
 * Ant 1.6.5
 * @see <a href='http://ant.apache.org'>Apache Ant</a>
 */
public class RegexpMatcherFactory
{

    /**
     * Create a new regular expression instance.
     *
     * @return the matcher
     * @throws BuildException on error
     */
    public RegexpMatcher newRegexpMatcher() throws Exception
    {
        String systemDefault = System.getProperty("izpack.regexp.matcherimpl");
        if (systemDefault != null)
        {
            return createInstance(systemDefault);
        }

        try
        {
            testAvailability("java.util.regex.Matcher");
            return createInstance("com.izforge.izpack.util.regex.JavaRegexpMatcher");
        }
        catch (Exception be)
        {
            // ignore
        }

        /*        try {
            testAvailability("org.apache.oro.text.regex.Pattern");
            return createInstance("com.izforge.izpack.util.regex.JakartaOroMatcher");
        } catch (Exception be) {
            // ignore
        }

        try {
            testAvailability("org.apache.regexp.RE");
            return createInstance("com.izforge.izpack.util.regex.JakartaRegexpMatcher");
        } catch (Exception be) {
            // ignore
        }
         */
        throw new Exception("No supported regular expression matcher found");
    }

    /**
     * Create an instance of a matcher from a classname.
     *
     * @param className a <code>String</code> value
     * @return a <code>RegexpMatcher</code> value
     * @throws BuildException if an error occurs
     */
    protected RegexpMatcher createInstance(String className)
            throws RuntimeException
    {
        try
        {
            Class<?> implClass = Class.forName(className);
            return (RegexpMatcher) implClass.newInstance();
        }
        catch (Throwable t)
        {
            throw new RuntimeException(t);
        }
    }

    /**
     * Test if a particular class is available to be used.
     *
     * @param className a <code>String</code> value
     * @throws BuildException if an error occurs
     */
    protected void testAvailability(String className) throws Exception
    {
        try
        {
            Class.forName(className);
        }
        catch (Throwable t)
        {
            throw new Exception(t);
        }
    }
}

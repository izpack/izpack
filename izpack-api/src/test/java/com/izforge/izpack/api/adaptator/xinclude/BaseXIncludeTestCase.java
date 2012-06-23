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

package com.izforge.izpack.api.adaptator.xinclude;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace;

import java.net.URL;
import java.util.List;

import org.junit.Test;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.IXMLParser;
import com.izforge.izpack.api.adaptator.impl.XMLParser;

/**
 * Base class for xinclude tests.
 */
public abstract class BaseXIncludeTestCase
{

    /**
     * This method takes the fileBase name and attempts to find two files
     * called &lt;fileBase&gt;-input.xml and &lt;fileBase&gt;-expected.xml
     *
     * @param fileBase the base of the test file names
     * @throws Exception
     */
    public abstract void doTest(String fileBase) throws Exception;

    /**
     * This method is used to ensure that the contents of the specified file
     * (when having "-input.xml" appended) cause the parser to fail
     *
     * @param fileBase the base name of the input file.
     * @throws Exception
     */
    public void ensureFailure(String fileBase) throws Exception
    {
        try
        {
            URL baseURL = getClass().getResource(fileBase + "-input.xml");
            // set up a new parser to parse the input xml (with includes)
            IXMLParser parser = new XMLParser();
            parser.parse(baseURL);
            fail("an exception should have been thrown");
        }
        catch (Throwable t)
        {
            // success
        }
    }

    /**
     * Perform a deep equality check on the two nodes.
     */
    public void deepEqual(IXMLElement a, IXMLElement b)
    {

        assertEquals("element names ", a.getName(), b.getName());
//        assertEquals("element attributes for " + a.getName(),
//                a.getAttributes(), b.getAttributes());
        if (null != b.getContent() && null != a.getContent())
        {
            assertThat(a.getContent(), equalToIgnoringWhiteSpace(b.getContent()));
        }
        assertEquals("equal number of children " + a.getName(),
                a.getChildrenCount(), b.getChildrenCount());

        List<IXMLElement> aChildren = a.getChildren();
        List<IXMLElement> bChildren = b.getChildren();
        for (int i = 0; i < bChildren.size(); i++)
        {
            IXMLElement aChild = aChildren.get(i);
            IXMLElement bChild = bChildren.get(i);
            deepEqual(aChild, bChild);
        }

    }

    /**
     * Test Empty document with include
     *
     * @throws Exception
     */
    @Test
    public void testIncludeOnly() throws Exception
    {
        doTest("include-only");
    }

    /**
     * Test Empty document with include
     *
     * @throws Exception
     */
    @Test
    public void testIncludeSubdirectoryOnly() throws Exception
    {
        doTest("include-subdirectory-only");
    }

    /**
     * Test that a fragment included as the root node does not have the
     * "fragment" element removed
     *
     * @throws Exception
     */
    @Test
    public void testIncludeFragmentOnly() throws Exception
    {
        doTest("include-fragment-only");
    }

    /**
     * Test to ensure that content is correctly included when the include
     * element is not the root element
     *
     * @throws Exception
     */
    @Test
    public void testIncludeInElement() throws Exception
    {
        doTest("include-in-element");
    }

    /**
     * Test to ensure that content is correctly included when the include
     * element is not the root element
     *
     * @throws Exception
     */
    @Test
    public void testIncludeFragmentInElement() throws Exception
    {
        doTest("include-fragment-in-element");
    }

    /**
     * Test text inclusion
     *
     * @throws Exception
     */
    @Test
    public void _testIncludeTextInElement() throws Exception
    {
        doTest("include-fragment-in-element");
    }

    /**
     * Ensure that the parse attribute accepts "text" and treats it like text
     *
     * @throws Exception
     */
    @Test
    public void testParseAttributeText() throws Exception
    {
        doTest("include-xml-as-text");
    }

    /**
     * Ensure that the parse attribute accepts "xml" and treats like xml
     * (most other tests do not explicitly set the parse parameter and let it
     * default to "xml"
     *
     * @throws Exception
     */
    @Test
    public void testParseAttributeXML() throws Exception
    {
        doTest("include-xml-as-xml");
    }

    /**
     * Make sure that a failure occurs for a parse valid that is not "xml"
     * or "text"
     *
     * @throws Exception
     */
    @Test
    public void testParseInvalidAttribute() throws Exception
    {
        ensureFailure("invalid-parse-attrib");
    }

    /**
     * Ensure fallbacks work correctly
     *
     * @throws Exception
     */
    @Test
    public void testFallback() throws Exception
    {
        doTest("fallback");
    }

    /**
     * Test that an empty fallback just removes the include and fallback
     * elements
     *
     * @throws Exception
     */
    @Test
    public void testEmptyFallback() throws Exception
    {
        doTest("empty-fallback");
    }

    /**
     * Ensure that two includes in the same element both get included
     *
     * @throws Exception
     */
    @Test
    public void testMultipleIncludes() throws Exception
    {
        doTest("multiple-include");
    }
}

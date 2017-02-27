/*
* IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
*
* http://izpack.org/
* http://izpack.codehaus.org/
*
* Copyright (c) 2008, 2009 Anthonin Bonnefoy
* Copyright (c) 2008, 2009 David Duponchel
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
package com.izforge.izpack.api.adaptator;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.izforge.izpack.api.adaptator.impl.XMLParser;

/**
 * Test on the XMLElement
 *
 * @author Anthonin Bonnefoy
 * @author David Duponchel
 */
public class XMLParserTest
{

    private static final String FILENAME = "shortcutSpec.xml";
    private static final String SHORT_FILENAME = "short.xml";
    private static final String LN_FILENAME = "linenumber/linenumber.xml";
    private static final String XLN_FILENAME = "linenumber/xinclude-linenumber.xml";
    private static final String PARSE_ERROR_FILENAME = "notvalid.xml";
    private static final String PARSE_ERROR_XINCLUDE_FILENAME = "xinclude-notvalid.xml";

    @Test
    public void testParseFile() throws Exception
    {
        InputStream input;
        IXMLElement spec;
        input = XMLParserTest.class.getResourceAsStream(SHORT_FILENAME);

        IXMLParser parser = new XMLParser();
        spec = parser.parse(input);
        Assert.assertEquals("izpack:shortcuts", spec.getName());
    }

    @Test
    public void testParseString() throws Exception
    {
        IXMLElement spec;
        String substitutedSpec = FileUtils.readFileToString(
                new File(XMLParserTest.class.getResource(FILENAME).toURI()));
        IXMLParser parser = new XMLParser(false);
        spec = parser.parse(substitutedSpec);
        Assert.assertEquals("izpack:shortcuts", spec.getName());
    }

    private void checkEltLN(IXMLElement elt)
    {
        assertEquals(Integer.parseInt(elt.getAttribute("ln")), elt.getLineNr());
        for (IXMLElement child : elt.getChildren())
        {
            checkEltLN(child);
        }
    }

    @Test
    public void testLineNumber() throws SAXException, ParserConfigurationException, IOException, TransformerException
    {
        InputStream input = XMLParserTest.class.getResourceAsStream(LN_FILENAME);
        IXMLElement elt;

        IXMLParser parser = new XMLParser(false);
        elt = parser.parse(input);

        checkEltLN(elt);
    }

    @Test
    public void testXincludeLineNumber()
            throws SAXException, ParserConfigurationException, IOException, TransformerException
    {
        URL url = XMLParserTest.class.getResource(XLN_FILENAME);

        IXMLParser parser = new XMLParser(false);
        IXMLElement elt = parser.parse(url);

        checkEltLN(elt);
    }

    @Test(expected = XMLException.class)
    public void testXMLExceptionThrown()
    {
        InputStream input = XMLParserTest.class.getResourceAsStream(PARSE_ERROR_FILENAME);
        IXMLParser parser = new XMLParser();
        parser.parse(input, PARSE_ERROR_FILENAME);
    }

    @Test(expected = XMLException.class)
    public void testXMLExceptionThrownXInclude()
    {
        InputStream input = XMLParserTest.class.getResourceAsStream(PARSE_ERROR_XINCLUDE_FILENAME);
        IXMLParser parser = new XMLParser();
        parser.parse(input, PARSE_ERROR_XINCLUDE_FILENAME);
    }

    @Test(expected = NullPointerException.class)
    public void testNPE()
    {
        IXMLParser parser = new XMLParser();
        parser.parse((InputStream) null);
    }

    @Test(expected = NullPointerException.class)
    public void testWithSystemIdNPE()
    {
        IXMLParser parser = new XMLParser();
        parser.parse(null, "bla");
    }

}

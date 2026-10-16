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

import static java.lang.Integer.parseInt;
import static java.nio.charset.Charset.defaultCharset;
import static java.nio.file.Files.readString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.izforge.izpack.api.adaptator.impl.XMLParser;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

/**
 * Test on the XMLElement
 *
 * @author Anthonin Bonnefoy
 * @author David Duponchel
 */
public class XMLParserTest
{

    private static final String filename = "shortcutSpec.xml";
    private static final String shortFilename = "short.xml";
    private static final String lnFilename = "linenumber/linenumber.xml";
    private static final String xlnFilename = "linenumber/xinclude-linenumber.xml";
    private static final String parseErrorFilename = "notvalid.xml";
    private static final String parseErrorXincludeFilename = "xinclude-notvalid.xml";


    @Test
    public void testParseFile() throws Exception
    {
        InputStream input;
        IXMLElement spec;
        input = XMLParserTest.class.getResourceAsStream(shortFilename);

        IXMLParser parser = new XMLParser();
        spec = parser.parse(input);
        assertThat(spec.getName()).isEqualTo("izpack:shortcuts");
    }

    @Test
    public void testParseString() throws Exception
    {
        IXMLElement spec;
        String substitutedSpec = readString(
                Path.of(XMLParserTest.class.getResource(filename).toURI()),
                defaultCharset());
        IXMLParser parser = new XMLParser(false);
        spec = parser.parse(substitutedSpec);
        assertThat(spec.getName()).isEqualTo("izpack:shortcuts");
    }

    private void checkEltLN(IXMLElement elt)
    {
        assertThat(elt.getLineNr()).isEqualTo(parseInt(elt.getAttribute("ln")));
        for (IXMLElement child : elt.getChildren())
        {
            checkEltLN(child);
        }
    }

    @Test
    public void testLineNumber() throws SAXException, ParserConfigurationException, IOException, TransformerException
    {
        InputStream input = XMLParserTest.class.getResourceAsStream(lnFilename);
        IXMLElement elt;

        IXMLParser parser = new XMLParser(false);
        elt = parser.parse(input);

        checkEltLN(elt);
    }

    @Test
    public void testXincludeLineNumber()
            throws SAXException, ParserConfigurationException, IOException, TransformerException
    {
        URL url = XMLParserTest.class.getResource(xlnFilename);

        IXMLParser parser = new XMLParser(false);
        IXMLElement elt = parser.parse(url);

        checkEltLN(elt);
    }

    @Test
    public void testXMLExceptionThrown()
    {
        assertThatThrownBy(() -> {
            InputStream input = XMLParserTest.class.getResourceAsStream(parseErrorFilename);
            IXMLParser parser = new XMLParser();
            parser.parse(input, parseErrorFilename);
        }).isInstanceOf(XMLException.class);
    }

    @Test
    public void testXMLExceptionThrownXInclude()
    {
        assertThatThrownBy(() -> {
            InputStream input = XMLParserTest.class.getResourceAsStream(parseErrorXincludeFilename);
            IXMLParser parser = new XMLParser();
            parser.parse(input, parseErrorXincludeFilename);
        }).isInstanceOf(XMLException.class);
    }

    @Test
    public void testNPE()
    {
        assertThatThrownBy(() -> {
            IXMLParser parser = new XMLParser();
            parser.parse((InputStream) null);
        }).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testWithSystemIdNPE()
    {
        assertThatThrownBy(() -> {
            IXMLParser parser = new XMLParser();
            parser.parse(null, "bla");
        }).isInstanceOf(NullPointerException.class);
    }

}
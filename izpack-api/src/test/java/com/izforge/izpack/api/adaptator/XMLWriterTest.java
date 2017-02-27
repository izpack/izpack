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

import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.adaptator.impl.XMLWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;

/**
 * Test the writer implementation
 *
 * @author Anthonin Bonnefoy
 */
public class XMLWriterTest
{

    private static final String FILENAME = "partial.xml";
    private static final String OUTPUT = "output.xml";
    private IXMLParser parser;
    private IXMLElement root;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() throws FileNotFoundException
    {
        parser = new XMLParser();
        root = parser.parse(XMLWriterTest.class.getResourceAsStream(FILENAME));
    }

    /**
     * Try to write a file with an outputStream
     *
     * @throws FileNotFoundException
     */
    @Test
    public void testWriteFile() throws IOException
    {
        IXMLWriter writer = new XMLWriter();
        File file = tempFolder.newFile(OUTPUT);
        FileOutputStream out = new FileOutputStream(file);
        writer.setOutput(out);
        writer.write(root);
        root = parser.parse(XMLWriterTest.class.getResourceAsStream(FILENAME));
        IXMLElement element = parser.parse(new FileInputStream(file));
        Assert.assertEquals(root.getName(), element.getName());
    }

    /**
     * Try to write a file with an Url to a resource
     *
     * @throws java.io.IOException
     */
    @Test
    public void testWriteURL() throws IOException
    {
        IXMLWriter writer = new XMLWriter();
        File file = tempFolder.newFile(OUTPUT);
        FileOutputStream out = new FileOutputStream(file);
        writer.setOutput(out);
        writer.write(root);
        root = parser.parse(XMLWriterTest.class.getResourceAsStream(FILENAME));
        IXMLElement element = parser.parse(new FileInputStream(file));
        Assert.assertEquals(root.getName(), element.getName());
    }

    @Test(expected = XMLException.class)
    public void testFail()
    {
        // TODO : don't use XMLElementImpl !
        IXMLElement elt = new XMLElementImpl("root");
        IXMLWriter writer = new XMLWriter();
        writer.setOutput(""); // will take the current directory, which is not a file !
        writer.write(elt);
    }
}

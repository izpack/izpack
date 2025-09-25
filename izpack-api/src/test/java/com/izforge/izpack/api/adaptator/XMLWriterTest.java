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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.adaptator.impl.XMLWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;

/**
 * Test the writer implementation
 *
 * @author Anthonin Bonnefoy
 */
public class XMLWriterTest
{

    private static final String filename = "partial.xml";
    private static final String output = "output.xml";
    private IXMLParser parser;
    private IXMLElement root;


    @TempDir
    static Path tempDir;

    @BeforeEach
    public void setUp() throws FileNotFoundException
    {
        parser = new XMLParser();
        root = parser.parse(XMLWriterTest.class.getResourceAsStream(filename));
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
        File file = tempDir.resolve(output).toFile();
        FileOutputStream out = new FileOutputStream(file);
        writer.setOutput(out);
        writer.write(root);
        root = parser.parse(XMLWriterTest.class.getResourceAsStream(filename));
        IXMLElement element = parser.parse(new FileInputStream(file));
        assertEquals(root.getName(), element.getName());
    }


    /**
     * Try to write a file with an Url to a resource
     *
     * @throws IOException
     */
    @Test
    public void testWriteURL() throws IOException
    {
        IXMLWriter writer = new XMLWriter();
        File file = tempDir.resolve(output).toFile();
        FileOutputStream out = new FileOutputStream(file);
        writer.setOutput(out);
        writer.write(root);
        root = parser.parse(XMLWriterTest.class.getResourceAsStream(filename));
        IXMLElement element = parser.parse(new FileInputStream(file));
        assertEquals(root.getName(), element.getName());
    }

    @Test
    public void testFail()
    {
        assertThrows(XMLException.class, () -> {
            // TODO : don't use XMLElementImpl !
            IXMLElement elt = new XMLElementImpl("root");
            IXMLWriter writer = new XMLWriter();
            writer.setOutput(""); // will take the current directory, which is not a file !
            writer.write(elt);
        });
    }
}

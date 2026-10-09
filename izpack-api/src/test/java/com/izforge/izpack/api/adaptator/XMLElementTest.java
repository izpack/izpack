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

import static org.assertj.core.api.Assertions.assertThat;

import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.adaptator.impl.XMLParser;
import java.io.FileNotFoundException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test on the XMLElement
 *
 * @author Anthonin Bonnefoy
 * @author David Duponchel
 */
public class XMLElementTest
{
    private static final String filename = "partial.xml";

    private IXMLElement root;

    @BeforeEach
    public void setUp() throws FileNotFoundException
    {
        /* méthode DOM */
        IXMLParser parser = new XMLParser();
        root = parser.parse(XMLElementTest.class.getResourceAsStream(filename));
    }

    @Test
    public void testGetName()
    {
        assertThat(root.getName()).isEqualTo("izpack:installation");
        assertThat(root.getChildAtIndex(0).getName()).isEqualTo("info");
    }

    @Test
    public void testAddChild() throws NoSuchMethodException
    {
        IXMLElement element = new XMLElementImpl("child", root);
        root.addChild(element);
        element = root.getChildAtIndex(root.getChildrenCount() - 1);
        assertThat(element.getName()).isEqualTo("child");
    }

    @Test
    public void testAddChildToDifferentDocument() throws NoSuchMethodException
    {
        IXMLElement element = new XMLElementImpl("child");
        root.addChild(element);
        element = root.getChildAtIndex(root.getChildrenCount() - 1);
        assertThat(element.getName()).isEqualTo("child");
    }

    @Test
    public void testRemoveChild() throws NoSuchMethodException
    {
        IXMLElement element = new XMLElementImpl("child", root);
        root.addChild(element);
        element = root.getChildAtIndex(root.getChildrenCount() - 1);
        root.removeChild(element);
        assertThat(root.getChildrenNamed("child").size()).isZero();
    }

    @Test
    public void testHasChildrenIfTrue()
    {
        assertThat(root.hasChildren()).isTrue();
    }

    @Test
    public void testHasChildrenIfFalse()
    {
        IXMLElement element = new XMLElementImpl("test");
        assertThat(element.hasChildren()).isFalse();
    }

    @Test
    public void testGetChildrenCount()
    {
        IXMLElement element = root.getChildAtIndex(0);
        assertThat(element.getChildrenCount()).isEqualTo(9);
    }

    @Test
    public void testGetChildAtIndex()
    {
        IXMLElement element = root.getChildAtIndex(1);
        assertThat(element.getName()).isEqualTo("variables");
    }

    @Test
    public void testGetFirstChildNamed()
    {
        IXMLElement element = root.getFirstChildNamed("locale");
        assertThat(element.getName()).isEqualTo("locale");
    }

    @Test
    public void testGetChildrenNamed()
    {
        IXMLElement element = root.getChildAtIndex(2);
        List<IXMLElement> list = element.getChildrenNamed("modifier");
        assertThat(list).hasSize(7);
    }
}
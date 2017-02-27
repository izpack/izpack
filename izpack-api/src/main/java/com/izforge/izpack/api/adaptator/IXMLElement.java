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

import org.w3c.dom.Node;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * Interface of the adaptator between the methods of nanoXml and javax
 *
 * @author Anthonin Bonnefoy
 * @author David Duponchel
 */
public interface IXMLElement extends Serializable
{

    /**
     * No line number defined.
     */
    int NO_LINE = -1;

    /**
     * Returns the name of the element.
     *
     * @return the name, or null if the element only contains #PCDATA.
     */
    String getName();

    /**
     * Adds a child element.
     *
     * @param child the non-null child to add.
     */
    void addChild(IXMLElement child);

    /**
     * Removes a child element.
     *
     * @param child the non-null child to remove.
     */
    void removeChild(IXMLElement child);

    /**
     * Returns whether the element has children.
     *
     * @return true if the element has children.
     */
    boolean hasChildren();

    /**
     * Returns the number of children.
     *
     * @return the count.
     */
    int getChildrenCount();

    /**
     * Returns a list containing all the child elements.
     *
     * @return the list.
     */
    List<IXMLElement> getChildren();

    /**
     * Returns the child at a specific index.
     *
     * @param index Index of the child
     * @return the non-null child
     * @throws java.lang.ArrayIndexOutOfBoundsException if the index is out of
     * bounds.
     */
    IXMLElement getChildAtIndex(int index) throws ArrayIndexOutOfBoundsException;

    /**
     * Searches a child element.
     *
     * @param name the name of the child to search for.
     * @return the child element, or null if no such child was found.
     */
    IXMLElement getFirstChildNamed(String name);

    /**
     * Returns a list of all child elements named <I>name</I>.
     *
     * @param name the name of the children to search for.
     * @return the non-null list of child elements.
     */
    List<IXMLElement> getChildrenNamed(String name);

    /**
     * Returns the value of an attribute.
     *
     * @param name the non-null name of the attribute.
     * @return the value, or null if the attribute does not exist.
     */
    String getAttribute(String name);

    /**
     * Returns the value of an attribute.
     *
     * @param name the non-null name of the attribute.
     * @param defaultValue the default value of the attribute.
     * @return the value, or defaultValue if the attribute does not exist.
     */
    String getAttribute(String name, String defaultValue);

    /**
     * Sets an attribute.
     *
     * @param name the non-null name of the attribute.
     * @param value the non-null value of the attribute.
     */
    void setAttribute(String name, String value);

    /**
     * Removes an attribute.
     *
     * @param name the non-null name of the attribute.
     */
    void removeAttribute(String name);

    /**
     * Returns an enumeration of all attribute names.
     *
     * @return the non-null enumeration.
     */
    Enumeration<String> enumerateAttributeNames();

    /**
     * Returns whether an attribute exists.
     *
     * @param name The name of the attribute
     * @return true if the attribute exists.
     */
    boolean hasAttribute(String name);

    /**
     * Returns all attributes as a Properties object.
     *
     * @return the non-null set.
     */
    Properties getAttributes();

    /**
     * Returns the line number in the data where the element started.
     *
     * @return the line number, or NO_LINE if unknown.
     * @see #NO_LINE
     */
    int getLineNr();

    /**
     * Return the #PCDATA content of the element. If the element has a
     * combination of #PCDATA content and child elements, the #PCDATA sections
     * can be retrieved as unnamed child objects. In this case, this method
     * returns null.
     *
     * @return the content.
     */
    String getContent();

    /**
     * Sets the #PCDATA content. It is an error to call this method with a
     * non-null value if there are child objects.
     *
     * @param content the (possibly null) content.
     */
    void setContent(String content);

    /**
     * Get the embeded node of the XmlElement
     *
     * @return Embedded node
     */
    Node getElement();
}

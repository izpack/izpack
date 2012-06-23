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

package com.izforge.izpack.api.adaptator.impl;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;


/**
 * A custom SAX XML filter, used to add line numbers to a DOM document.
 * This filter stores line numbers while parsing, and the applyLN method set
 * line numbers on the result.
 * Line numbers are stored in the user data of the Element,
 * so require Java 5 (DOM 3) or higher.
 *
 * @author Anthonin Bonnefoy
 * @author David Duponchel
 */
public class LineNumberFilter extends XMLFilterImpl
{

    /**
     * a queue to store line numbers while parsing.
     */
    private Queue<Integer> lnQueue;

    /**
     * The locator given while parsing.
     */
    private Locator locator;

    public LineNumberFilter(XMLReader xmlReader)
    {
        super(xmlReader);
    }

    @Override
    public void startDocument() throws SAXException
    {
        super.startDocument();
        lnQueue = new LinkedList<Integer>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        super.startElement(uri, localName, qName, atts);
        lnQueue.add(locator.getLineNumber());
    }

    @Override
    public void setDocumentLocator(Locator locator)
    {
        super.setDocumentLocator(locator);
        this.locator = locator;
    }

    /**
     * Return the locator on the current position.
     *
     * @return the current locator.
     */
    public Locator getDocumentLocator()
    {
        return locator;
    }

    /**
     * Return the first element found from the given Node.
     *
     * @param elt The Node start point.
     * @return The first Element found, or null.
     */
    private Element getFirstFoundElement(Node elt)
    {
        while (elt != null && elt.getNodeType() != Node.ELEMENT_NODE)
        {
            elt = elt.getNextSibling();
        }
        return (Element) elt;
    }

    /**
     * Return the next element sibling found from the given Node.
     *
     * @param elt The Node start point.
     * @return The next sibling Element, or null if nto found.
     */
    private Element getNextSibling(Node elt)
    {
        return getFirstFoundElement(elt.getNextSibling());
    }

    /**
     * Return the first element child found from the given Node.
     *
     * @param elt The Node start point.
     * @return The first child Element found, or null.
     */
    private Element getFirstChild(Node elt)
    {
        return getFirstFoundElement(elt.getFirstChild());
    }

    /**
     * Returns whether the given node has any element children.
     *
     * @param elt The node to check.
     * @return Returns <code>true</code> if this node has any children,
     *         <code>false</code> otherwise.
     */
    private boolean hasChildElements(Node elt)
    {
        return getFirstChild(elt) != null;
    }

    /**
     * Apply a line number on the given element.
     * We assume that the line number queue has been correctly filled, and that
     * the current DOM tree correspond to the parsed XML.
     *
     * @param elt the element to apply the line number.
     */
    private void applyLN(Element elt)
    {
        elt.setUserData("ln", lnQueue.poll(), null);
    }

    /**
     * Apply line numbers stored by a parse using this object on the xml elements.
     *
     * @param result The result of the parse.
     */
    public void applyLN(DOMResult result)
    {
        Element elt = getFirstChild(result.getNode());
        boolean end = false;
        Stack<Element> stack = new Stack<Element>();

        /*
         * We collected the line numbers, now we walk through the xml tree to apply them.
         */
        while (!end)
        {
            if (hasChildElements(elt))
            { // not a leaf
                stack.push(elt);
                applyLN(elt);
                elt = getFirstChild(elt); // go down
            }
            else
            { // a leaf
                applyLN(elt);
                Element sibling = getNextSibling(elt);
                if (sibling != null)
                { // has a sibling
                    elt = sibling;
                }
                else
                { // no sibling
                    do
                    {
                        if (stack.isEmpty())
                        {
                            end = true;
                        }
                        else
                        {
                            elt = stack.pop();
                            elt = getNextSibling(elt);
                        }
                    }
                    while (!end && elt == null);
                }
            }
        }
    }
}
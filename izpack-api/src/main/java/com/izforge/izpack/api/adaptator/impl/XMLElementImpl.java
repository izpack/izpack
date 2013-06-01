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

package com.izforge.izpack.api.adaptator.impl;

import com.izforge.izpack.api.adaptator.IXMLElement;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Implementation of the adaptator between nanoXml and javax
 *
 * @author Anthonin Bonnefoy
 * @author David Duponchel
 */
public class XMLElementImpl implements IXMLElement
{

    /**
     * The dom element embedded by the XMLElement
     */
    private Element element;

    /**
     * A flag to notice any changement made to the element.
     * It is used to generate the childrenList.
     */
    private boolean hasChanged = true;

    /**
     * List of the children elements.
     * It is generated as it is called.
     */
    private List<IXMLElement> childrenList;

    /**
     * Create a new root element in a new document.
     *
     * @param name Name of the root element
     */
    public XMLElementImpl(String name)
    {
        Document document;
        try
        {
            // Création d'un nouveau DOM
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder constructeur = documentFactory.newDocumentBuilder();
            document = constructeur.newDocument();
            // Propriétés du DOM
            document.setXmlVersion("1.0");
            element = document.createElement(name);
            document.appendChild(element);
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Constructor which create a root element in the given document
     *
     * @param name       Name of the root element
     * @param inDocument The document in which to create the xml
     */
    public XMLElementImpl(String name, Document inDocument)
    {
        element = inDocument.createElement(name);
    }

    /**
     * Create a element in the same document of the given element
     *
     * @param name             Name of the element
     * @param elementReference Reference of an existing xml. It is used to generate an xmlElement on the same document.
     */
    public XMLElementImpl(String name, IXMLElement elementReference)
    {
        element = elementReference.getElement().getOwnerDocument().createElement(name);
    }

    /**
     * Constructor saving the passed node
     *
     * @param node Node to save inside the XMLElement
     */
    public XMLElementImpl(Node node)
    {
        if (!(node instanceof Element))
        {
            throw new IllegalArgumentException("The node should be an instance of Element");
        }
        this.element = (Element) node;
    }

    @Override
    public String getName()
    {
        return element.getNodeName();
    }

    @Override
    public void addChild(IXMLElement child)
    {
        hasChanged = true;
        Document targetDoc = element.getOwnerDocument();
        Document sourceDoc = child.getElement().getOwnerDocument();
        if (targetDoc.equals(sourceDoc)) {
            element.appendChild(child.getElement());
        } else {
            Node firstDocImportedNode = element.getOwnerDocument().importNode(child.getElement(), true);
            element.appendChild(firstDocImportedNode );
        }
    }

    @Override
    public void removeChild(IXMLElement child)
    {
        hasChanged = true;
        element.removeChild(child.getElement());
    }

    @Override
    public boolean hasChildren()
    {
        for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling())
        {
            if (child.getNodeType() == Node.ELEMENT_NODE)
            {
                return true;
            }
        }
        return false;
    }

    private void initChildrenList()
    {
        if (hasChanged)
        {
            hasChanged = false;
            childrenList = new ArrayList<IXMLElement>();
            for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling())
            {
                if (child.getNodeType() == Node.ELEMENT_NODE)
                {
                    childrenList.add(new XMLElementImpl(child));
                }
            }
        }
    }

    @Override
    public int getChildrenCount()
    {
        initChildrenList();
        return childrenList.size();
    }

    @Override
    public List<IXMLElement> getChildren()
    {
        initChildrenList();
        return childrenList;
    }

    @Override
    public IXMLElement getChildAtIndex(int index)
    {
        initChildrenList();
        return childrenList.get(index);
    }

    @Override
    public IXMLElement getFirstChildNamed(String name)
    {
        XMLElementImpl res = null;
        NodeList nodeList = element.getElementsByTagName(name);
        if (nodeList.getLength() > 0)
        {
            res = new XMLElementImpl(nodeList.item(0));
        }
        return res;
    }

    @Override
    public List<IXMLElement> getChildrenNamed(String name)
    {
        List<IXMLElement> res = new ArrayList<IXMLElement>();
        for (IXMLElement child : getChildren())
        {
            if (child.getName() != null && child.getName().equals(name))
            {
                res.add(new XMLElementImpl(child.getElement()));
            }
        }
        return res;
    }

    @Override
    public String getAttribute(String name)
    {
        return this.getAttribute(name, null);
    }

    @Override
    public String getAttribute(String name, String defaultValue)
    {
        Node attribute = element.getAttributes().getNamedItem(name);
        if (attribute != null)
        {
            return attribute.getNodeValue();
        }
        return defaultValue;
    }

    @Override
    public void setAttribute(String name, String value)
    {
        NamedNodeMap attributes = element.getAttributes();
        Attr attribute = element.getOwnerDocument().createAttribute(name);
        attribute.setValue(value);
        attributes.setNamedItem(attribute);
    }

    @Override
    public void removeAttribute(String name)
    {
        this.element.getAttributes().removeNamedItem(name);
    }

    @Override
    public Enumeration enumerateAttributeNames()
    {
        NamedNodeMap namedNodeMap = element.getAttributes();
        Properties properties = new Properties();
        for (int i = 0; i < namedNodeMap.getLength(); i++)
        {
            Node node = namedNodeMap.item(i);
            properties.put(node.getNodeName(), node.getNodeValue());
        }
        return properties.keys();
    }

    @Override
    public boolean hasAttribute(String name)
    {
        return (this.element.getAttributes().getNamedItem(name) != null);
    }

    @Override
    public Properties getAttributes()
    {
        Properties properties = new Properties();
        NamedNodeMap namedNodeMap = this.element.getAttributes();
        for (int i = 0; i < namedNodeMap.getLength(); ++i)
        {
            properties.put(namedNodeMap.item(i).getNodeName(), namedNodeMap.item(i).getNodeValue());
        }
        return properties;
    }

    @Override
    public int getLineNr()
    {
        Object ln = element.getUserData("ln");
        if (ln == null)
        {
            return NO_LINE;
        }
        try
        {
            return (Integer) element.getUserData("ln");
        }
        catch (ClassCastException e)
        {
            return NO_LINE;
        }
    }

    @Override
    public String getContent()
    {
        StringBuilder builder = new StringBuilder();
        String content;
        Node child = this.element.getFirstChild();

        // no error if there are children
        boolean err = (child == null);

        // pattern : only whitespace characters
        Pattern pattern = Pattern.compile("^\\s+$");

        while (!err && child != null)
        {
            content = child.getNodeValue();
            if (child.getNodeType() == Node.TEXT_NODE)
            {
                // text node : nanoXML ignores it if it's only whitespace characters.
                if (content != null && !pattern.matcher(content).matches())
                {
                    builder.append(content);
                }
            }
            else if (child.getNodeType() == Node.CDATA_SECTION_NODE)
            {
                builder.append(content);
            }
            // neither CDATA nor text : real nested element !
            else
            {
                err = true;
            }
            child = child.getNextSibling();
        }
        return (err) ? null : builder.toString().trim();
    }

    @Override
    public void setContent(String content)
    {
        Node child;
        while ((child = this.element.getFirstChild()) != null)
        {
            this.element.removeChild(child);
        }
        element.appendChild(element.getOwnerDocument().createTextNode(content));
    }

    @Override
    public Node getElement()
    {
        return element;
    }

    @Override
    public String toString()
    {
        return element.getNodeName() + " " + element.getNodeValue();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof IXMLElement)
        {
            IXMLElement o = (IXMLElement) obj;
            Element elem = (Element) o.getElement();
            Node child2 = elem.getFirstChild();
            for (Node child = element.getFirstChild(); child != null && child2 != null; child = child.getNextSibling())
            {
                if (!child.equals(child2))
                {
                    return false;
                }
                child2.getNextSibling();
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hashCode = 0;
        for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling())
        {
            hashCode += child.hashCode();
        }
        return hashCode;
    }
}

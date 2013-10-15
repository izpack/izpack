/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2009 Laurent Bovet, Alex Mathey
 * Copyright 2010, 2012 René Krell
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

package com.izforge.izpack.util.xmlmerge.action;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.jdom2.Element;

import com.izforge.izpack.util.xmlmerge.AbstractXmlMergeException;
import com.izforge.izpack.util.xmlmerge.Action;
import com.izforge.izpack.util.xmlmerge.DocumentException;
import com.izforge.izpack.util.xmlmerge.ElementException;
import com.wutka.dtd.DTD;
import com.wutka.dtd.DTDAny;
import com.wutka.dtd.DTDContainer;
import com.wutka.dtd.DTDElement;
import com.wutka.dtd.DTDItem;
import com.wutka.dtd.DTDName;
import com.wutka.dtd.DTDParser;

/**
 * Copy the patch element in the output parent with the correct position according to the DTD
 * declared in doctype.
 *
 * @author Laurent Bovet (LBO)
 * @author Alex Mathey (AMA)
 */
public class DtdInsertAction implements Action
{
    private static final Logger logger = Logger.getLogger(DtdInsertAction.class.getName());

    /**
     * Map containing (ID, DTD) pairs, where ID represents the system ID of a DTD, and DTD
     * represents the corresponding DTD.
     */
    static Map<String, DTD> s_dtdMap = new Hashtable<String, DTD>();

    @Override
    public void perform(Element originalElement, Element patchElement, Element outputParentElement)
            throws AbstractXmlMergeException
    {

        Element element;

        if (originalElement != null)
        {
            element = (Element) originalElement.clone();
        }
        else
        {
            element = (Element) patchElement.clone();
        }

        DTD dtd = getDTD(outputParentElement);

        List<DTDElement> dtdElements = dtd.getItemsByType(DTDElement.class);

        // Find the corresponding element
        DTDElement parentDtdElement = null;
        for (DTDElement dtdElement : dtdElements)
        {
            if (dtdElement.getName().equals(outputParentElement.getName()))
            {
                parentDtdElement = dtdElement;
            }
        }

        if (parentDtdElement == null)
        {
            throw new ElementException(element, "Element " + outputParentElement.getName()
                    + " not defined in DTD");
        }
        else
        {

            DTDItem item = parentDtdElement.getContent();

            if (item instanceof DTDAny)
            {
                // the parent element accepts anything in any order
                outputParentElement.addContent(element);
            }
            else if (item instanceof DTDContainer)
            {

                // List existing elements in output parent element
                List<Element> existingChildren = outputParentElement.getChildren();

                if (existingChildren.size() == 0)
                {
                    // This is the first child
                    outputParentElement.addContent(element);
                }
                else
                {

                    List<String> orderedDtdElements = getOrderedDtdElements((DTDContainer) item);

                    int indexOfNewElementInDtd = orderedDtdElements.indexOf(element.getName());
                    logger.fine("index of element " + element.getName() + ": "
                            + indexOfNewElementInDtd);

                    int pos = existingChildren.size();

                    // Calculate the position in the parent where we insert the
                    // element
                    for (int i = 0; i < existingChildren.size(); i++)
                    {
                        String elementName = (existingChildren.get(i)).getName();
                        logger.fine("index of child " + elementName + ": "
                                + orderedDtdElements.indexOf(elementName));
                        if (orderedDtdElements.indexOf(elementName) > indexOfNewElementInDtd)
                        {
                            pos = i;
                            break;
                        }
                    }

                    logger.fine("adding element " + element.getName() + " add in pos " + pos);
                    outputParentElement.addContent(pos, element);

                }

            }

        }

    }

    /**
     * Gets the DTD declared in the doctype of the element's owning document.
     *
     * @param element The element for which the DTD will be retrieved
     * @return The DTD declared in the doctype of the element's owning document
     * @throws DocumentException If an error occurred during DTD retrieval
     */
    public DTD getDTD(Element element) throws DocumentException
    {

        if (element.getDocument().getDocType() != null)
        {

            String systemId = element.getDocument().getDocType().getSystemID();

            DTD dtd = s_dtdMap.get(systemId);

            // if not in cache, create the DTD and put it in cache
            if (dtd == null)
            {
                Reader reader;
                URL url;

                // lookup URL of DTD
                try
                {
                    url = new URL(systemId);
                    reader = new InputStreamReader(url.openStream());
                }
                catch (MalformedURLException e)
                {
                    throw new DocumentException(element.getDocument(), e);
                }
                catch (IOException ioe)
                {
                    throw new DocumentException(element.getDocument(), ioe);
                }

                try
                {
                    dtd = new DTDParser(reader).parse();
                }
                catch (IOException ioe)
                {
                    throw new DocumentException(element.getDocument(), ioe);
                }

                s_dtdMap.put(systemId, dtd);
            }

            return dtd;

        }
        else
        {
            throw new DocumentException(element.getDocument(), "No DTD specified in document "
                    + element.getDocument());
        }
    }

    /**
     * Retrieves a list containing the DTD elements of a given DTD container.
     *
     * @param container A DTD container.
     * @return A list containing the DTD elements of a given DTD container
     */
    public List<String> getOrderedDtdElements(DTDContainer container)
    {
        List<String> result = new ArrayList<String>();

        DTDItem[] items = container.getItems();

        for (DTDItem item : items)
        {
            if (item instanceof DTDContainer)
            {
                // recursively add container children
                result.addAll(getOrderedDtdElements((DTDContainer) item));
            }
            else if (item instanceof DTDName)
            {
                result.add(((DTDName) item).getValue());
            }
        }

        return result;

    }

}

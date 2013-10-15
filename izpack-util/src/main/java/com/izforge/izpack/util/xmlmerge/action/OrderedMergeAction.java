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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import org.jdom2.Attribute;
import org.jdom2.Comment;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Text;

import com.izforge.izpack.util.xmlmerge.AbstractXmlMergeException;
import com.izforge.izpack.util.xmlmerge.Action;
import com.izforge.izpack.util.xmlmerge.DocumentException;
import com.izforge.izpack.util.xmlmerge.Mapper;
import com.izforge.izpack.util.xmlmerge.Matcher;

/**
 * Merge implementation traversing parallelly both element contents. Works when contents are in the
 * same order in both elements.
 *
 * @author Laurent Bovet (LBO)
 * @author Alex Mathey (AMA)
 */
public class OrderedMergeAction extends AbstractMergeAction
{
    private static final Logger logger = Logger.getLogger(OrderedMergeAction.class.getName());

    @Override
    public void perform(Element originalElement, Element patchElement, Element outputParentElement)
            throws AbstractXmlMergeException
    {

        logger.fine("Merging: " + originalElement + " (original) and " + patchElement + "(patch)");

        Mapper mapper = (Mapper) m_mapperFactory.getOperation(originalElement, patchElement);

        if (originalElement == null)
        {
            outputParentElement.addContent(mapper.map(patchElement));
        }
        else if (patchElement == null)
        {
            outputParentElement.addContent((Content) originalElement.clone());
        }
        else
        {

            Element workingElement = new Element(originalElement.getName(), originalElement
                    .getNamespacePrefix(), originalElement.getNamespaceURI());
            addAttributes(workingElement, originalElement);

            logger.fine("Adding " + workingElement);
            outputParentElement.addContent(workingElement);

            doIt(workingElement, originalElement, patchElement);
        }

    }

    /**
     * Performs the actual merge between two source elements.
     *
     * @param parentOut The merged element
     * @param parentIn1 The first source element
     * @param parentIn2 The second source element
     * @throws AbstractXmlMergeException If an error occurred during the merge
     */
    private void doIt(Element parentOut, Element parentIn1, Element parentIn2)
            throws AbstractXmlMergeException
    {

        addAttributes(parentOut, parentIn2);

        Content[] list1 = (Content[]) parentIn1.getContent().toArray(new Content[] {});
        Content[] list2 = (Content[]) parentIn2.getContent().toArray(new Content[] {});

        int offsetTreated1 = 0;
        int offsetTreated2 = 0;

        for (Content content1 : list1)
        {

            logger.fine("List 1: " + content1);

            if (content1 instanceof Comment || content1 instanceof Text)
            {
                parentOut.addContent((Content) content1.clone());
                offsetTreated1++;
            }
            else if (!(content1 instanceof Element))
            {
                throw new DocumentException(content1.getDocument(), "Contents of type "
                        + content1.getClass().getName() + " not supported");
            }
            else
            {
                Element e1 = (Element) content1;

                // does e1 exist on list2 and has not yet been treated
                int posInList2 = -1;
                for (int j = offsetTreated2; j < list2.length; j++)
                {

                    logger.fine("List 2: " + list2[j]);

                    if (list2[j] instanceof Element)
                    {

                        if (((Matcher) m_matcherFactory.getOperation(e1, (Element) list2[j]))
                                .matches(e1, (Element) list2[j]))
                        {
                            logger.fine("Match found: " + e1 + " and " + list2[j]);
                            posInList2 = j;
                            break;
                        }
                    }
                    else if (list2[j] instanceof Comment || list2[j] instanceof Text)
                    {
                        // skip
                    }
                    else
                    {
                        throw new DocumentException(list2[j].getDocument(), "Contents of type "
                                + list2[j].getClass().getName() + " not supported");
                    }
                }

                // element found in second list, but there is some elements to
                // be treated before in second list
                while (posInList2 != -1 && offsetTreated2 < posInList2)
                {
                    Content contentToAdd;
                    if (list2[offsetTreated2] instanceof Element)
                    {
                        applyAction(parentOut, null, (Element) list2[offsetTreated2]);
                    }
                    else
                    {
                        // FIXME prevent double comments in output by enhancing applyAction() to
                        // Content type instead of Element
                        // Workaround: Add only comments from original document (List1)
                        if (!(list2[offsetTreated2] instanceof Comment))
                        {
                            contentToAdd = (Content) list2[offsetTreated2].clone();
                            parentOut.addContent(contentToAdd);
                        }
                    }

                    offsetTreated2++;
                }

                // element found in all lists
                if (posInList2 != -1)
                {

                    applyAction(parentOut, (Element) list1[offsetTreated1],
                            (Element) list2[offsetTreated2]);

                    offsetTreated1++;
                    offsetTreated2++;
                }
                else
                {
                    // element not found in second list
                    applyAction(parentOut, (Element) list1[offsetTreated1], null);
                    offsetTreated1++;
                }
            }
        }

        // at the end of list1, are there some elements in list2 which must be still treated?
        while (offsetTreated2 < list2.length)
        {
            Content contentToAdd;
            if (list2[offsetTreated2] instanceof Element)
            {
                applyAction(parentOut, null, (Element) list2[offsetTreated2]);
            }
            else
            {
                // FIXME prevent double comments in output by enhancing applyAction() to Content
                // type instead of Element
                // Workaround: Add only comments from original document (List1)
                if (!(list2[offsetTreated2] instanceof Comment))
                {
                    contentToAdd = (Content) list2[offsetTreated2].clone();
                    parentOut.addContent(contentToAdd);
                }
            }

            offsetTreated2++;
        }

    }

    /**
     * Applies the action which performs the merge between two source elements.
     *
     * @param workingParent Output parent element
     * @param originalElement Original element
     * @param patchElement Patch element
     * @throws AbstractXmlMergeException if an error occurred during the merge
     */
    private void applyAction(Element workingParent, Element originalElement, Element patchElement)
            throws AbstractXmlMergeException
    {
        Action action = (Action) m_actionFactory.getOperation(originalElement, patchElement);
        Mapper mapper = (Mapper) m_mapperFactory.getOperation(originalElement, patchElement);

        action.perform(originalElement, mapper.map(patchElement), workingParent);
    }

    /**
     * Adds attributes from in element to out element.
     *
     * @param out out element
     * @param in in element
     */
    private void addAttributes(Element out, Element in)
    {

        LinkedHashMap<String, Attribute> allAttributes = new LinkedHashMap<String, Attribute>();

        List<Attribute> outAttributes = new ArrayList<Attribute>(out.getAttributes());
        List<Attribute> inAttributes = new ArrayList<Attribute>(in.getAttributes());

        for (Attribute attr : outAttributes)
        {
            attr.detach();
            allAttributes.put(attr.getQualifiedName(), attr);
            logger.fine("adding attr from out:" + attr);
        }

        for (Attribute attr : inAttributes)
        {
            attr.detach();
            allAttributes.put(attr.getQualifiedName(), attr);
            logger.fine("adding attr from in:" + attr);
        }

        out.setAttributes(new ArrayList<Attribute>(allAttributes.values()));
    }

}

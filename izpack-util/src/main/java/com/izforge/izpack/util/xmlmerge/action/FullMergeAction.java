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
 * Merge implementation traversing element contents undependend of their order.
 * This is an enhancement of {@link OrderedMergeAction}.
 *
 * @author René Krell
 */
public class FullMergeAction extends AbstractMergeAction
{
    private static final Logger logger = Logger.getLogger(FullMergeAction.class.getName());

    @Override
    public void perform(Element originalElement, Element patchElement, Element outputParentElement)
            throws AbstractXmlMergeException
    {

        logger.fine("Merging: " + originalElement + " (original) and " + patchElement + " (patch)");

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
     * @param origElement The first source element
     * @param patchElement The second source element
     * @throws AbstractXmlMergeException If an error occurred during the merge
     */
    private void doIt(Element parentOut, Element origElement, Element patchElement)
            throws AbstractXmlMergeException
    {
        addAttributes(parentOut, patchElement);

        List<Content> origContentList = origElement.getContent();
        List<Content> patchContentList = patchElement.getContent();
        List<Content> unmatchedPatchContentList = new ArrayList<Content>();
        List<Content> matchedPatchContentList = new ArrayList<Content>();

        for (Content origContent : origContentList)
        {
            logger.fine("Checking original content: " + origContent + " for matching patch contents");
            if (origContent instanceof Element)
            {
                boolean patchMatched = false;

                for (Content patchContent : patchContentList)
                {
                    logger.fine("Checking patch content: " + patchContent);

                    if (patchContent instanceof Comment || patchContent instanceof Text)
                    {
                        // skip and leave original comment or text
                        logger.fine("Skipped patch content: " + patchContent);
                    }
                    else if (!(patchContent instanceof Element))
                    {
                        throw new DocumentException(patchContent.getDocument(), "Contents of type "
                                + patchContent.getClass().getName() + " in patch document not supported");
                    }
                    else
                    {
                        if (((Matcher) m_matcherFactory.getOperation((Element) patchContent, (Element) origContent))
                                .matches((Element) patchContent, (Element) origContent))
                        {
                            logger.fine("Apply matching patch: " + patchContent + " -> " + origContent);
                            applyAction(parentOut, (Element) origContent, (Element) patchContent);
                            patchMatched = true;
                            if (!matchedPatchContentList.contains(patchContent))
                            {
                                matchedPatchContentList.add(patchContent);
                            }
                        }
                        else
                        {
                            if (!unmatchedPatchContentList.contains(patchContent))
                            {
                                unmatchedPatchContentList.add(patchContent);
                            }
                        }
                        // Continue searching here for finding multiple matches
                    }
                }

                if (!patchMatched)
                {
                    logger.fine("Apply original: "+ origContent);
                    applyAction(parentOut, (Element) origContent, null);
                }
            }
            else if (origContent instanceof Comment || origContent instanceof Text)
            {
                // leave original comment or text
                parentOut.addContent((Content) origContent.clone());
            }
            else
            {
                throw new DocumentException(origContent.getDocument(), "Contents of type "
                        + origContent.getClass().getName() + " in original document not supported");
            }
        }

        for (Content unmatchedPatchContent : unmatchedPatchContentList)
        {
            if (!matchedPatchContentList.contains(unmatchedPatchContent))
            {
                logger.fine("Apply unmatching patch: "+ unmatchedPatchContent);
                applyAction(parentOut, null, (Element) unmatchedPatchContent);
            }
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

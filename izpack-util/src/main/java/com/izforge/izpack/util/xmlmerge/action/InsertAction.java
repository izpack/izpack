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
import java.util.Iterator;
import java.util.List;

import org.jdom2.Content;
import org.jdom2.Element;

import com.izforge.izpack.util.xmlmerge.Action;

/**
 * Copies the patch element into the output by inserting it after already existing elements of the
 * same name. Usually applied with the {@link com.izforge.izpack.util.xmlmerge.matcher.SkipMatcher}.
 *
 * @author Laurent Bovet (LBO)
 * @author Alex Mathey (AMA)
 */
public class InsertAction implements Action
{

    @Override
    public void perform(Element originalElement, Element patchElement, Element outputParentElement)
    {

        if (patchElement == null && originalElement != null)
        {
            outputParentElement.addContent((Element) originalElement.clone());

        }
        else
        {
            List<Content> outputContent = outputParentElement.getContent();

            Iterator<Content> it = outputContent.iterator();

            int lastIndex = outputContent.size();

            while (it.hasNext())
            {
                Content content = it.next();

                if (content instanceof Element)
                {
                    Element element = (Element) content;

                    if (element.getQualifiedName().equals(patchElement.getQualifiedName()))
                    {
                        lastIndex = outputParentElement.indexOf(element);
                    }
                }
            }

            List<Content> toAdd = new ArrayList<Content>();
            toAdd.add(patchElement);
            outputContent.addAll(Math.min(lastIndex + 1, outputContent.size()), toAdd);
        }
    }

}

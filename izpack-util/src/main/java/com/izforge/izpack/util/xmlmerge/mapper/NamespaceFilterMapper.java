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

package com.izforge.izpack.util.xmlmerge.mapper;

import java.util.Iterator;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

import com.izforge.izpack.util.xmlmerge.Mapper;

/**
 * Filters out elements and attributes with a specified namespace.
 *
 * @author Laurent Bovet (LBO)
 * @author Alex Mathey (AMA)
 */
public class NamespaceFilterMapper implements Mapper
{

    /**
     * The namespace defining the elements and attributes to be filtered out.
     */
    Namespace m_namespace;

    /**
     * Creates a new NamespaceFilterMapper.
     *
     * @param filteredNamespace String representing the namespace defining the elements and
     * attributes to be filtered out
     */
    public NamespaceFilterMapper(String filteredNamespace)
    {
        this.m_namespace = Namespace.getNamespace(filteredNamespace);
    }

    @Override
    public Element map(Element patchElement)
    {
        if (patchElement == null) { return null; }
        if (patchElement.getNamespace().equals(m_namespace))
        {
            return null;
        }
        else
        {
            return filterAttributes(patchElement);
        }
    }

    /**
     * Filters an element's attributes.
     *
     * @param element An element whose attributes will be filtered
     * @return The input element whose attributes have been filtered
     */
    private Element filterAttributes(Element element)
    {
        Element result = (Element) element.clone();

        List<Attribute> attributes = result.getAttributes();
        Iterator<Attribute> it = attributes.iterator();

        while (it.hasNext())
        {
            Attribute attr = it.next();

            if (attr.getNamespace().equals(m_namespace))
            {
                it.remove();
            }
        }

        return result;
    }

    /**
     * Sets the namespace defining the elements and attributes to be filtered out.
     *
     * @param namespace The namespace defining the elements and attributes to be filtered out.
     */
    public void setFilteredNamespace(Namespace namespace)
    {
        this.m_namespace = namespace;
    }

}

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

package com.izforge.izpack.util.xmlmerge.factory;

import org.jdom2.Element;
import org.jdom2.Namespace;

import com.izforge.izpack.util.xmlmerge.AbstractXmlMergeException;
import com.izforge.izpack.util.xmlmerge.Operation;
import com.izforge.izpack.util.xmlmerge.OperationFactory;

/**
 * Creates operations by inspecting keywords passed as attributes in patch elements.
 *
 * @author Laurent Bovet (LBO)
 * @author Alex Mathey (AMA)
 */
public class AttributeOperationFactory implements OperationFactory
{

    /**
     * Default operation.
     */
    private Operation m_defaultOperation;

    /**
     * Namespace describing the operations to apply.
     */
    private Namespace m_namespace;

    /**
     * Keyword.
     */
    private String m_keyword;

    /**
     * Operation resolver.
     */
    private OperationResolver m_resolver;

    /**
     * Creates a new AttributeOperationFactory.
     *
     * @param defaultOperation The factory's default operation
     * @param resolver The factory's operation resolver
     * @param keyword The name of the attribute representing the factory's operation
     * @param namespace The namespace describing the operations to apply
     */
    public AttributeOperationFactory(Operation defaultOperation, OperationResolver resolver,
            String keyword, String namespace)
    {
        this.m_defaultOperation = defaultOperation;
        this.m_keyword = keyword;
        this.m_resolver = resolver;
        this.m_namespace = Namespace.getNamespace(namespace);
    }

    @Override
    public Operation getOperation(Element originalElement, Element modifiedElement)
            throws AbstractXmlMergeException
    {

        if (modifiedElement == null) { return m_defaultOperation; }

        String operationString = modifiedElement.getAttributeValue(m_keyword, m_namespace);

        if (operationString != null)
        {
            return m_resolver.resolve(operationString);
        }
        else
        {
            return m_defaultOperation;
        }

    }
}

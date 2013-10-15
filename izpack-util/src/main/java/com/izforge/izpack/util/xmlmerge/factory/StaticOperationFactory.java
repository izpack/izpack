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

import com.izforge.izpack.util.xmlmerge.Operation;
import com.izforge.izpack.util.xmlmerge.OperationFactory;

/**
 * An operation factory returning always the same operation whatever the specified elements.
 *
 * @author Laurent Bovet (LBO)
 * @author Alex Mathey (AMA)
 */
public class StaticOperationFactory implements OperationFactory
{

    /**
     * The operation operation returned by this factory.
     */
    Operation m_operation;

    /**
     * Creates a StaticOperationFactory returning the given operation.
     *
     * @param operation The operation operation returned by this factory.
     */
    public StaticOperationFactory(Operation operation)
    {
        this.m_operation = operation;
    }

    @Override
    public Operation getOperation(Element originalElement, Element modifiedElement)
    {
        return m_operation;
    }

}

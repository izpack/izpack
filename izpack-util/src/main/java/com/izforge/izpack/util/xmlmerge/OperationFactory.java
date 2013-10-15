/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2009 Laurent Bovet, Alex Mathey
 * Copyright 2010 Rene Krell
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

package com.izforge.izpack.util.xmlmerge;

import org.jdom2.Element;

/**
 * Creates operation (action, mapper and matcher) instances corresponding to a pair of elements from
 * the original and patch DOMs.
 *
 * @author Laurent Bovet (LBO)
 * @author Alex Mathey (AMA)
 */
public interface OperationFactory
{

    /**
     * Creates operation (action, mapper and matcher) instances corresponding to a pair of elements
     * from the original and patch DOMs.
     *
     * @param originalElement Original element
     * @param modifiedElement Modified element
     * @return The operation (action, mapper or matcher) for the given element pair
     * @throws AbstractXmlMergeException If an error occurs during operation creation
     */
    public Operation getOperation(Element originalElement, Element modifiedElement)
            throws AbstractXmlMergeException;

}

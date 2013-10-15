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
 * Transforms an element to another element by changing some nested content or attributes.
 *
 * @author Laurent Bovet (LBO)
 * @author Alex Mathey (AMA)
 */
public interface Mapper extends Operation
{

    /**
     * Transforms the given element to another element by changing some nested content or
     * attributes.
     *
     * @param patchElement the element to transform
     * @return the transformed element
     */
    public Element map(Element patchElement);
}

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

import com.izforge.izpack.util.xmlmerge.AbstractXmlMergeException;
import com.izforge.izpack.util.xmlmerge.Operation;
import com.izforge.izpack.util.xmlmerge.OperationFactory;

/**
 * An operation factory delegating to other operation factories according to the existence of the
 * original and patch element.
 *
 * @author Laurent Bovet (LBO)
 * @author Alex Mathey (AMA)
 */
public class DiffOperationFactory implements OperationFactory
{

    /**
     * OperationFactory this factory delegates to if only the original element exists.
     */
    OperationFactory m_onlyInOriginalOperationFactory;

    /**
     * OperationFactory this factory delegates to if only the patch element exists.
     */
    OperationFactory m_onlyInPatchOperationFactory;

    /**
     * OperationFactory this factory delegates to if the original and patch elements exist.
     */
    OperationFactory m_inBothOperationFactory;

    /**
     * Sets the operation factory this factory delegates to if the original and patch elements
     * exist.
     *
     * @param inBothOperationFactory the operation factory this factory delegates to if the original
     * and patch elements exist.
     */
    public void setInBothOperationFactory(OperationFactory inBothOperationFactory)
    {
        this.m_inBothOperationFactory = inBothOperationFactory;
    }

    /**
     * Sets the operation factory this factory delegates to if only the original element exists.
     *
     * @param onlyInOriginalOperationFactory factory this factory delegates to if only the original
     * element exists
     */
    public void setOnlyInOriginalOperationFactory(OperationFactory onlyInOriginalOperationFactory)
    {
        this.m_onlyInOriginalOperationFactory = onlyInOriginalOperationFactory;
    }

    /**
     * Sets the operation factory this factory delegates to if only the patch element exists.
     *
     * @param onlyInPatchOperationFactory factory this factory delegates to if only the patch
     * element exists
     */
    public void setOnlyInPatchOperationFactory(OperationFactory onlyInPatchOperationFactory)
    {
        this.m_onlyInPatchOperationFactory = onlyInPatchOperationFactory;
    }

    @Override
    public Operation getOperation(Element originalElement, Element patchElement)
            throws AbstractXmlMergeException
    {

        if (originalElement != null && patchElement == null) { return m_onlyInOriginalOperationFactory
                .getOperation(originalElement, null); }

        if (originalElement == null && patchElement != null) { return m_onlyInPatchOperationFactory
                .getOperation(null, patchElement); }

        if (originalElement != null && patchElement != null) { return m_inBothOperationFactory
                .getOperation(originalElement, patchElement); }

        throw new IllegalArgumentException();
    }

}

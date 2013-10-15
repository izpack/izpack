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

package com.izforge.izpack.util.xmlmerge.matcher;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Elements match if their name and a certain attribute value are the same.
 */
public abstract class AbstractAttributeMatcher extends AbstractTagMatcher
{
    protected abstract boolean ignoreCaseAttributeName();
    protected abstract boolean ignoreCaseAttributeValue();
    protected abstract String getAttributeName();

    @Override
    public boolean matches(Element originalElement, Element patchElement)
    {
        if (super.matches(originalElement, patchElement))
        {
            List<Attribute> origAttList = originalElement.getAttributes();
            List<Attribute> patchAttList = patchElement.getAttributes();

            if (origAttList.size() == 0 && patchAttList.size() == 0)
            {
                return true;
            }

            if (origAttList.size() != patchAttList.size())
            {
                return false;
            }

            for (Attribute origAttribute : origAttList)
            {
                if (getAttributeName() == null ||
                        (getAttributeName() != null &&
                         equalsString(origAttribute.getQualifiedName(), getAttributeName(),
                                ignoreCaseAttributeName())))
                {
                    for (Attribute patchAttribute : patchAttList)
                    {
                        if (ignoreCaseAttributeName())
                        {
                            if (origAttribute.getQualifiedName().equalsIgnoreCase(patchAttribute.getQualifiedName()))
                            {
                                return equalsString(origAttribute.getValue(), patchAttribute.getValue(), ignoreCaseAttributeValue());
                            }
                        }
                        else
                        {
                            if (origAttribute.getQualifiedName().equals(patchAttribute.getQualifiedName()))
                            {
                                return equalsString(origAttribute.getValue(), patchAttribute.getValue(), ignoreCaseAttributeValue());
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}

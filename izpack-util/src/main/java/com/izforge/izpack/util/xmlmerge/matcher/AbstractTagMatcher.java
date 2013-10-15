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

import org.jdom2.Element;

import com.izforge.izpack.util.xmlmerge.Matcher;

/**
 * Compares the qualified name of elements.
 */
public abstract class AbstractTagMatcher implements Matcher
{

    protected abstract boolean ignoreCaseElementName();

    @Override
    public boolean matches(Element originalElement, Element patchElement)
    {
        return equalsString(originalElement.getQualifiedName(), patchElement.getQualifiedName(),
                ignoreCaseElementName());
    }

    protected static boolean equalsString(String s1, String s2, boolean ignoreCase)
    {
        if (ignoreCase)
        {
            return s1.equalsIgnoreCase(s2);
        }
        else
        {
            return s1.equals(s2);
        }
    }
}

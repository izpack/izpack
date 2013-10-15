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
 * Thrown when something is wrong in the matching process.
 *
 * @author Laurent Bovet (LBO)
 * @author Alex Mathey (AMA)
 */
public class MatchException extends AbstractXmlMergeException
{

    /**
     * Element which caused the exception.
     */
    Element m_element;

    /**
     * Constructor with message.
     *
     * @param element Element which caused the exception
     * @param message Exception message
     */
    public MatchException(Element element, String message)
    {
        super(message);
        this.m_element = element;
    }

    /**
     * Constructor with cause.
     *
     * @param element Element which caused the exception
     * @param cause Exception cause
     */
    public MatchException(Element element, Throwable cause)
    {
        super(cause);
        this.m_element = element;
    }

    /**
     * @return Returns the element.
     */
    public Element getElement()
    {
        return m_element;
    }

    /**
     * @param element Is the element to set.
     */
    public void setElement(Element element)
    {
        m_element = element;
    }

}

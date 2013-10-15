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

import org.jdom2.Document;

/**
 * Thrown when something is wrong with a source or output document.
 *
 * @author Laurent Bovet (LBO)
 * @author Alex Mathey (AMA)
 */
public class DocumentException extends AbstractXmlMergeException
{

    /**
     * A document instance.
     */
    Document m_document;

    /**
     * Constructor with message.
     *
     * @param document Document which caused the exception
     * @param message Exception message
     */
    public DocumentException(Document document, String message)
    {
        super(message);
        m_document = document;
    }

    /**
     * Constructor with cause.
     *
     * @param document Document which caused the exception
     * @param cause Exception cause
     */
    public DocumentException(Document document, Throwable cause)
    {
        super(makeMessage(document), cause);
        m_document = document;
    }

    /**
     * Announces that there is a problem with the given document.
     *
     * @param document A given document
     * @return String announcing that there is a problem with the given document
     */
    private static String makeMessage(Document document)
    {
        return "Problem with document " + document;
    }

    /**
     * @return Returns the document.
     */
    public Document getDocument()
    {
        return m_document;
    }

    /**
     * @param document Is the document to set.
     */
    public void setDocument(Document document)
    {
        m_document = document;
    }

}

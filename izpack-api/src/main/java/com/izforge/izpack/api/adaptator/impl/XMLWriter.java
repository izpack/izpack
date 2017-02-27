/*
* IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
*
* http://izpack.org/
* http://izpack.codehaus.org/
*
* Copyright (c) 2008, 2009 Anthonin Bonnefoy
* Copyright (c) 2008, 2009 David Duponchel
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
package com.izforge.izpack.api.adaptator.impl;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.IXMLWriter;
import com.izforge.izpack.api.adaptator.XMLException;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;

/**
 * @author Anthonin Bonnefoy
 * @author David Duponchel
 */
public class XMLWriter implements IXMLWriter
{

    /**
     * OutputStream of the xml
     */
    private OutputStream outputStream;

    /**
     * Url of the output
     */
    private String systemId;

    /**
     * Default constructor
     */
    public XMLWriter()
    {
    }

    /**
     * Constructor with parameter
     *
     * @param outputStream outputStream to use
     */
    public XMLWriter(OutputStream outputStream)
    {
        this.outputStream = outputStream;
    }

    public void write(IXMLElement element)
    {
        try
        {
            Source source = new DOMSource(element.getElement().getOwnerDocument());
            TransformerFactory fabrique = TransformerFactory.newInstance();
            Transformer transformer = fabrique.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            Result result;
            if (outputStream != null)
            {
                result = new StreamResult(outputStream);
            }
            else
            {
                result = new StreamResult(systemId);
            }
            transformer.transform(source, result);
        }
        catch (TransformerException e)
        {
            throw new XMLException(e);
        }
    }

    public void setOutput(OutputStream outputStream)
    {
        this.outputStream = outputStream;
    }

    public void setOutput(String systemId)
    {
        this.systemId = systemId;
    }
}

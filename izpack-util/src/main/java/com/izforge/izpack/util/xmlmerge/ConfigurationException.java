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

/**
 * Thrown when something wrong happens in the XmlMerge configuration.
 *
 * @author Laurent Bovet (LBO)
 * @author Alex Mathey (AMA)
 */
public class ConfigurationException extends AbstractXmlMergeException
{

    private static final long serialVersionUID = -6132220150764644855L;

    /**
     * Default constructor.
     */
    public ConfigurationException()
    {
        super();
    }

    /**
     * Constructor with message.
     *
     * @param message Exception message
     */
    public ConfigurationException(String message)
    {
        super(message);
    }

    /**
     * Constructor with message and cause.
     *
     * @param message Exception message
     * @param cause Exception cause
     */
    public ConfigurationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor with cause.
     *
     * @param cause Exception cause
     */
    public ConfigurationException(Throwable cause)
    {
        super(cause);
    }

}

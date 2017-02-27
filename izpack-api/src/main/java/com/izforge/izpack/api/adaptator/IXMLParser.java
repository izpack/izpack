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
package com.izforge.izpack.api.adaptator;

import java.io.InputStream;
import java.net.URL;

/**
 * Interface for the adaptator of the javax Xml parser
 *
 * @author Anthonin Bonnefoy
 * @author David Duponchel
 */
public interface IXMLParser
{

    /**
     * Path to the xsl style sheet, used to handle xinclude leftovers. The path
     * is relative to the IXMLParser class
     */
    public static final String XSL_FILE_NAME = "styleSheet.xsl";

    /**
     * Parse the given stream to a XML
     *
     * @param inputStream Stream to parse. Must be not null.
     * @return Root element of the parsed xml
     * @throws XMLException if something went wrong.
     */
    IXMLElement parse(InputStream inputStream);

    /**
     * Parse the given stream to a XML and set the systemId to the inputSource.
     * It is useful for the xinclude feature, as the path of the resolution
     * depends on the systemId.
     *
     * @param inputStream Stream to parse. Must be not null.
     * @param systemId System id of the file parsed
     * @return Root element of the parsed XML
     * @throws XMLException if something went wrong.
     */
    IXMLElement parse(InputStream inputStream, String systemId);

    /**
     * Parse the given text as an xml
     *
     * @param inputString Xml written in a string
     * @return Root element of the parsed xml
     * @throws XMLException if something went wrong.
     */
    IXMLElement parse(String inputString);

    /**
     * Parse the resource at the url specified
     *
     * @param inputURL Url of the resource
     * @return Root element of the parsed xml
     * @throws XMLException
     */
    IXMLElement parse(URL inputURL);
}

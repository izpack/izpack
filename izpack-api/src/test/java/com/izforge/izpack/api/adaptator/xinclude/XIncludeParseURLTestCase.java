/*
 * Copyright 2007 Volantis Systems Ltd., All Rights Reserved.
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
package com.izforge.izpack.api.adaptator.xinclude;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.IXMLParser;
import com.izforge.izpack.api.adaptator.impl.XMLParser;

import java.net.URL;

/**
 * Test the XInclude style functionality. Use the URL as input for the parser
 */
public class XIncludeParseURLTestCase extends BaseXIncludeTestCase
{

    @Override
    public void doTest(String fileBase) throws Exception
    {
        URL inputURL = getClass().getResource(fileBase + "-input.xml");
        URL expectURL = getClass().getResource(fileBase + "-expect.xml");
        // set up a new parser to parse the input xml (with includes)
        IXMLParser parser = new XMLParser(false);
        IXMLElement inputElement = parser.parse(inputURL);
        IXMLElement expectedElement = parser.parse(expectURL);
        deepEqual(expectedElement, inputElement);
    }
}

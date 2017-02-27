/*
 * IzPack - Copyright 2001-2015 Julien Ponge, René Krell, All Rights Reserved.
 *
 * http://izpack.org/
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
package com.izforge.izpack.compiler.xml;

import com.izforge.izpack.api.adaptator.impl.XMLParser;

/**
 * XML parser for the IzPack langpack resources with activated schema validation
 * using the according built-in XSD.
 */
public class LangPackXmlParser extends XMLParser
{

    public LangPackXmlParser()
    {
        super(true, XMLSchemaDefinition.LANGPACK.createStreamSources());
    }
}

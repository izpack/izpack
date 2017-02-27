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

import javax.xml.transform.stream.StreamSource;

import static com.izforge.izpack.compiler.data.CompilerData.VERSION;

public enum XMLSchemaDefinition
{

    ANTACTIONS("antactions"),
    BSFACTIONS("bsfactions"),
    COMPILATION("compilation"),
    CONDITIONS("conditions"),
    CONFIGURATIONACTIONS("configurationactions"),
    ICONS("icons"),
    INSTALLATION("installation"),
    LANGPACK("langpack"),
    PROCESSING("processing"),
    REGISTRY("registry"),
    SHORTCUTS("shortcuts"),
    USERINPUT("userinput");

    private final String[] resources;
    private final String[] systemIds;

    XMLSchemaDefinition(final String shortName)
    {
        this.resources = new String[]
        {
            "/schema/" + VERSION + "/izpack-types-" + VERSION + ".xsd",
            "/schema/" + VERSION + "/izpack-" + shortName + "-" + VERSION + ".xsd"
        };
        this.systemIds = new String[]
        {
            "http://izpack.org/schema/types",
            "http://izpack.org/schema/" + shortName
        };
    }

    private String[] resources()
    {
        return this.resources;
    }

    private String[] systemIds()
    {
        return this.systemIds;
    }

    StreamSource[] createStreamSources()
    {
        final String[] resources = resources();
        final String[] systemIds = systemIds();
        StreamSource[] sources = new StreamSource[resources.length];
        for (int i = 0; i < resources.length; i++)
        {
            sources[i] = new StreamSource(XMLSchemaDefinition.class.getResourceAsStream(resources[i]), systemIds[i]);
        }
        return sources;
    }

;
}

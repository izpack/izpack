/*
 * IzPack - Copyright 2001-2005 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2004 Klaus Bartz
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

package com.izforge.izpack.compiler.listener;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.exception.CompilerException;
import com.izforge.izpack.compiler.listener.SimpleCompilerListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * <p>CompilerListener for file and directory permissions.</p>
 *
 * @author Klaus Bartz
 */
public class ChmodCompilerListener extends SimpleCompilerListener
{


    /* (non-Javadoc)
    * @see com.izforge.izpack.compiler.listener.CompilerListener#reviseAdditionalDataMap(java.util.Map, net.n3.nanoxml.XMLElement)
    */

    public Map reviseAdditionalDataMap(Map existentDataMap, IXMLElement element)
            throws CompilerException
    {
        Map retval = existentDataMap != null ?
                existentDataMap : new HashMap();
        List<IXMLElement> dataList = element.getChildrenNamed("additionaldata");
        if (dataList == null || dataList.size() == 0)
        {
            return (existentDataMap);
        }
        for (IXMLElement data : dataList)
        {
            String[] relevantKeys = {"permission.dir", "permission.file"};
            for (String relevantKey : relevantKeys)
            {
                String key = data.getAttribute("key");
                if (key.equalsIgnoreCase(relevantKey))
                {
                    String value = data.getAttribute("value");
                    if (value == null || value.length() == 0)
                    {
                        continue;
                    }
                    try
                    {
                        int radix = value.startsWith("0") ? 8 : 10;
                        retval.put(key, Integer.valueOf(value, radix));
                    }
                    catch (NumberFormatException x)
                    {
                        throw new CompilerException("'" + relevantKey + "' must be an integer");
                    }
                }
            }
        }
        return retval;
    }
}

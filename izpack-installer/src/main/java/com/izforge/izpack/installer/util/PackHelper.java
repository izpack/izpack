/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
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
package com.izforge.izpack.installer.util;


import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.resource.Messages;

/**
 * Pack helper methods.
 *
 * @author Tim Anderson
 */
public class PackHelper
{
    /**
     * The name of the XML file that specifies the panel langpack
     */
    public static final String LANG_FILE_NAME = "packsLang.xml";

    /**
     * Returns a localised name for a pack.
     * <p/>
     * This uses {@link Pack#getLangPackId()} to locate the localised name for the pack.
     * <p/>
     * If no localised name exists, {@link Pack#getName()} will be returned.
     *
     * @param pack     the pack
     * @param messages the messages. May be {@code null}
     * @return the pack name
     */
    public static String getPackName(Pack pack, Messages messages)
    {
        String name = null;
        if (messages != null)
        {
            name = getMessage(pack.getLangPackId(), messages);
        }
        if (name == null || "".equals(name))
        {
            name = pack.getName();
        }
        return name;
    }

    /**
     * Returns a localised description for a pack.
     * <p/>
     * This uses {@code "<langPackId>.description"} to locate the localised name for the pack.
     * <p/>
     * If no localised description exists, {@link Pack#getDescription()} wil be returned.
     *
     * @param pack     the pack
     * @param messages the messages. May be {@code null}
     * @return the pack description. May be {@code null}
     */
    public static String getPackDescription(Pack pack, Messages messages)
    {
        String result = null;
        if (messages != null && pack.getLangPackId() != null)
        {
            result = getMessage(pack.getLangPackId() + ".description", messages);
        }
        if (result == null)
        {
            result = pack.getDescription();
        }
        return result;
    }

    /**
     * Helper to return a localised message.
     *
     * @param key      the message key
     * @param messages the messages
     * @return the message corresponding to {@code key}, or {@code null} if none exists
     */
    private static String getMessage(String key, Messages messages)
    {
        String result = null;
        if (key != null && !key.equals(""))
        {
            result = messages.get(key);
            if (key.equals(result) || "".equals(result))
            {
                result = null;
            }
        }
        return result;
    }

}

/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005,2009 Ivan SZKIBA
 * Copyright 2010,2011 Rene Krell
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
package com.izforge.izpack.util.config.base;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public interface Registry extends Profile
{
    enum Hive
    {
        HKEY_CLASSES_ROOT,
        HKEY_CURRENT_CONFIG,
        HKEY_CURRENT_USER,
        HKEY_LOCAL_MACHINE,
        HKEY_USERS;
    }

    // TODO handle delete operations with special Type
    enum Type
    {
        REG_NONE("hex(0)"),
        REG_SZ(""),
        REG_EXPAND_SZ("hex(2)"),
        REG_BINARY("hex"),
        REG_DWORD("dword"),
        REG_DWORD_4("hex(4)"),
        REG_DWORD_BIG_ENDIAN("hex(5)"),
        REG_LINK("hex(6)"),
        REG_MULTI_SZ("hex(7)"),
        REG_RESOURCE_LIST("hex(8)"),
        REG_FULL_RESOURCE_DESCRIPTOR("hex(9)"),
        REG_RESOURCE_REQUIREMENTS_LIST("hex(a)"),
        REG_QWORD("hex(b)");
        private static final Map<String, Type> MAPPING;

        static
        {
            MAPPING = new HashMap<String, Type>();
            for (Type t : values())
            {
                MAPPING.put(t.toString(), t);
            }
        }

        public static final char SEPARATOR_CHAR = ':';
        public static final String SEPARATOR = String.valueOf(SEPARATOR_CHAR);
        public static final char REMOVE_CHAR = '-';
        public static final String REMOVE = String.valueOf(REMOVE_CHAR);
        private final String _prefix;

        private Type(String prefix)
        {
            _prefix = prefix;
        }

        public static Type fromString(String str)
        {
            return MAPPING.get(str);
        }

        @Override public String toString()
        {
            return _prefix;
        }
    }

    char ESCAPE_CHAR = '\\';
    Charset FILE_ENCODING = Charset.forName("UnicodeLittle");
    char KEY_SEPARATOR = '\\';
    String LINE_SEPARATOR = "\r\n";
    char TYPE_SEPARATOR = ':';
    String VERSION = "Windows Registry Editor Version 5.00";

    String getVersion();

    void setVersion(String value);

    @Override Key get(Object key);

    @Override Key get(Object key, int index);

    @Override Key put(String key, Section value);

    @Override Key put(String key, Section value, int index);

    @Override Key remove(Object key);

    @Override Key remove(Object key, int index);

    interface Key extends Section
    {
        String DEFAULT_NAME = "@";

        @Override Key getChild(String key);

        @Override Key getParent();

        Type getType(Object key);

        Type getType(Object key, Type defaulType);

        @Override Key addChild(String key);

        @Override Key lookup(String... path);

        Type putType(String key, Type type);

        Type removeType(Object key);
    }
}

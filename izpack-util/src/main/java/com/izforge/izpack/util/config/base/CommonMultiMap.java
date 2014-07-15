/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005,2009 Ivan SZKIBA
 * Copyright 2010,2014 René Krell
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

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class CommonMultiMap<K, V> extends BasicMultiMap<K, V> implements CommentedMap<K, V>
{
    private static final long serialVersionUID = -861700824946477984L;

    private static final String SEPARATOR = ";#;";
    private static final String FIRST_CATEGORY = "";
    private static final String LAST_CATEGORY = "zzzzzzzzzzzzzzzzzzzzzz";
    private static final String META_COMMENT = "comment";
    private static final String META_NEWLINE_COUNT = "newline";
    private SortedMap<String, Object> _meta;

    @Override public int getNewLineCount(Object key)
    {
        Integer emptyLines = (Integer) getMeta(META_NEWLINE_COUNT, key);
        return emptyLines==null ? 0 : emptyLines.intValue();
    }

    @Override public Integer addEmptyLine(K key)
    {
        Integer newLines = (Integer) getMeta(META_NEWLINE_COUNT, key);
        if (newLines == null)
        {
            newLines = Integer.valueOf(0);
        }
        return (Integer) putMeta(META_NEWLINE_COUNT, key, Integer.valueOf(newLines.intValue()+1));
    }

    @SuppressWarnings("unchecked")
    @Override public List<String> getComment(Object key)
    {
        return (List<String>) getMeta(META_COMMENT, key);
    }

    @Override public void clear()
    {
        super.clear();
        if (_meta != null)
        {
            _meta.clear();
        }
    }

    @SuppressWarnings("unchecked")
    @Override public void putAll(Map<? extends K, ? extends V> map)
    {
        super.putAll(map);
        if (map instanceof CommonMultiMap)
        {
            @SuppressWarnings("rawtypes")
            Map<String, String> meta = ((CommonMultiMap) map)._meta;

            if (meta != null)
            {
                meta().putAll(meta);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override public List<String> putComment(K key, List<String> comment)
    {
        return (List<String>) putMeta(META_COMMENT, key, comment);
    }

    @Override public V remove(Object key)
    {
        V ret = super.remove(key);

        removeMeta(key);

        return ret;
    }

    @Override public V remove(Object key, int index)
    {
        V ret = super.remove(key, index);

        if (length(key) == 0)
        {
            removeMeta(key);
        }

        return ret;
    }

    @SuppressWarnings("unchecked")
    @Override public List<String> removeComment(Object key)
    {
        return (List<String>) removeMeta(META_COMMENT, key);
    }

    Object getMeta(String category, Object key)
    {
        return (_meta == null) ? null : _meta.get(makeKey(category, key));
    }

    Object putMeta(String category, K key, Object value)
    {
        return meta().put(makeKey(category, key), value);
    }

    void removeMeta(Object key)
    {
        if (_meta != null)
        {
            _meta.subMap(makeKey(FIRST_CATEGORY, key), makeKey(LAST_CATEGORY, key)).clear();
        }
    }

    Object removeMeta(String category, Object key)
    {
        return (_meta == null) ? null : _meta.remove(makeKey(category, key));
    }

    private String makeKey(String category, Object key)
    {
        StringBuilder buff = new StringBuilder();

        buff.append(String.valueOf(key));
        buff.append(SEPARATOR);
        buff.append(category);

        return buff.toString();
    }

    private Map<String, Object> meta()
    {
        if (_meta == null)
        {
            _meta = new TreeMap<String, Object>();
        }

        return _meta;
    }
}

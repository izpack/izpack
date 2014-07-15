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

import java.lang.reflect.Array;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.izforge.izpack.util.config.base.spi.AbstractBeanInvocationHandler;
import com.izforge.izpack.util.config.base.spi.BeanTool;
import com.izforge.izpack.util.config.base.spi.IniHandler;

public class BasicProfile extends CommonMultiMap<String, Profile.Section> implements Profile
{
    private static final String SECTION_SYSTEM_PROPERTIES = "@prop";
    private static final String SECTION_ENVIRONMENT = "@env";
    private static final Pattern EXPRESSION = Pattern.compile(
            "(?<!\\\\)\\$\\{(([^\\[\\}]+)(\\[([0-9]+)\\])?/)?([^\\[^/\\}]+)(\\[(([0-9]+))\\])?\\}");
    private static final int G_SECTION = 2;
    private static final int G_SECTION_IDX = 4;
    private static final int G_OPTION = 5;
    private static final int G_OPTION_IDX = 7;
    private static final long serialVersionUID = -1817521505004015256L;
    private List<String> _comment;
    private List<String> _footerComment;
    private final boolean _propertyFirstUpper;
    private final boolean _treeMode;

    public BasicProfile()
    {
        this(false, false);
    }

    public BasicProfile(boolean treeMode, boolean propertyFirstUpper)
    {
        _treeMode = treeMode;
        _propertyFirstUpper = propertyFirstUpper;
    }

    @Override public List<String> getHeaderComment()
    {
        return _comment;
    }

    @Override public void setHeaderComment(List<String> value)
    {
        _comment = value;
    }

    @Override public List<String> getFooterComment()
    {
        return _footerComment;
    }

    @Override public void setFooterComment(List<String> value)
    {
        _footerComment = value;
    }

    @Override public Section add(String name)
    {
        if (isTreeMode())
        {
            int idx = name.lastIndexOf(getPathSeparator());

            if (idx > 0)
            {
                String parent = name.substring(0, idx);

                if (!containsKey(parent))
                {
                    add(parent);
                }
            }
        }

        Section section = newSection(name);

        add(name, section);

        return section;
    }

    @Override public void add(String section, String option, Object value)
    {
        getOrAdd(section).add(option, value);
    }

    @Override public <T> T as(Class<T> clazz)
    {
        return as(clazz, null);
    }

    @Override public <T> T as(Class<T> clazz, String prefix)
    {
        return clazz.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { clazz },
                    new BeanInvocationHandler(prefix)));
    }

    @Override public String fetch(Object sectionName, Object optionName)
    {
        Section sec = get(sectionName);

        return (sec == null) ? null : sec.fetch(optionName);
    }

    @Override public <T> T fetch(Object sectionName, Object optionName, Class<T> clazz)
    {
        Section sec = get(sectionName);

        return (sec == null) ? BeanTool.getInstance().zero(clazz) : sec.fetch(optionName, clazz);
    }

    @Override public String get(Object sectionName, Object optionName)
    {
        Section sec = get(sectionName);

        return (sec == null) ? null : sec.get(optionName);
    }

    @Override public <T> T get(Object sectionName, Object optionName, Class<T> clazz)
    {
        Section sec = get(sectionName);

        return (sec == null) ? BeanTool.getInstance().zero(clazz) : sec.get(optionName, clazz);
    }

    @Override public String put(String sectionName, String optionName, Object value)
    {
        return getOrAdd(sectionName).put(optionName, value);
    }

    @Override public Section remove(Section section)
    {
        return remove((Object) section.getName());
    }

    @Override public String remove(Object sectionName, Object optionName)
    {
        Section sec = get(sectionName);

        return (sec == null) ? null : sec.remove(optionName);
    }

    boolean isTreeMode()
    {
        return _treeMode;
    }

    char getPathSeparator()
    {
        return PATH_SEPARATOR;
    }

    boolean isPropertyFirstUpper()
    {
        return _propertyFirstUpper;
    }

    Section newSection(String name)
    {
        return new BasicProfileSection(this, name);
    }

    void resolve(StringBuilder buffer, Section owner)
    {
        Matcher m = EXPRESSION.matcher(buffer);

        while (m.find())
        {
            String sectionName = m.group(G_SECTION);
            String optionName = m.group(G_OPTION);
            int optionIndex = parseOptionIndex(m);
            Section section = parseSection(m, owner);
            String value = null;

            if (SECTION_ENVIRONMENT.equals(sectionName))
            {
                value = Config.getEnvironment(optionName);
            }
            else if (SECTION_SYSTEM_PROPERTIES.equals(sectionName))
            {
                value = Config.getSystemProperty(optionName);
            }
            else if (section != null)
            {
                value = (optionIndex == -1) ? section.fetch(optionName) : section.fetch(optionName, optionIndex);
            }

            if (value != null)
            {
                buffer.replace(m.start(), m.end(), value);
                m.reset(buffer);
            }
        }
    }

    void store(IniHandler formatter)
    {
        formatter.startIni();
        store(formatter, getHeaderComment());
        for (Ini.Section s : values())
        {
            store(formatter, s);
        }
        if (_footerComment != null)
        {
            store(formatter, _footerComment);
        }

        formatter.endIni();
    }

    void store(IniHandler formatter, Section s)
    {
        store(formatter, getComment(s.getName()));
        formatter.startSection(s.getName());
        for (String name : s.keySet())
        {
            store(formatter, s, name);
        }

        formatter.endSection();
    }

    void store(IniHandler formatter, List<String> comment)
    {
        formatter.handleComment(comment);
    }

    void store(IniHandler formatter, Section section, String option)
    {
        store(formatter, section.getComment(option));
        int n = section.length(option);

        for (int i = 0; i < n; i++)
        {
            store(formatter, section, option, i);
        }
    }

    void store(IniHandler formatter, Section section, String option, int index)
    {
        formatter.handleOption(option, section.get(option, index));
    }

    private Section getOrAdd(String sectionName)
    {
        Section section = get(sectionName);

        return ((section == null)) ? add(sectionName) : section;
    }

    private int parseOptionIndex(Matcher m)
    {
        return (m.group(G_OPTION_IDX) == null) ? -1 : Integer.parseInt(m.group(G_OPTION_IDX));
    }

    private Section parseSection(Matcher m, Section owner)
    {
        String sectionName = m.group(G_SECTION);
        int sectionIndex = parseSectionIndex(m);

        return (sectionName == null) ? owner : ((sectionIndex == -1) ? get(sectionName) : get(sectionName, sectionIndex));
    }

    private int parseSectionIndex(Matcher m)
    {
        return (m.group(G_SECTION_IDX) == null) ? -1 : Integer.parseInt(m.group(G_SECTION_IDX));
    }

    private final class BeanInvocationHandler extends AbstractBeanInvocationHandler
    {
        private final String _prefix;

        private BeanInvocationHandler(String prefix)
        {
            _prefix = prefix;
        }

        @Override protected Object getPropertySpi(String property, Class<?> clazz)
        {
            String key = transform(property);
            Object o = null;

            if (containsKey(key))
            {
                if (clazz.isArray())
                {
                    o = Array.newInstance(clazz.getComponentType(), length(key));
                    for (int i = 0; i < length(key); i++)
                    {
                        Array.set(o, i, get(key, i).as(clazz.getComponentType()));
                    }
                }
                else
                {
                    o = get(key).as(clazz);
                }
            }

            return o;
        }

        @Override protected void setPropertySpi(String property, Object value, Class<?> clazz)
        {
            String key = transform(property);

            remove(key);
            if (value != null)
            {
                if (clazz.isArray())
                {
                    for (int i = 0; i < Array.getLength(value); i++)
                    {
                        Section sec = add(key);

                        sec.from(Array.get(value, i));
                    }
                }
                else
                {
                    Section sec = add(key);

                    sec.from(value);
                }
            }
        }

        @Override protected boolean hasPropertySpi(String property)
        {
            return containsKey(transform(property));
        }

        String transform(String property)
        {
            String ret = (_prefix == null) ? property : (_prefix + property);

            if (isPropertyFirstUpper())
            {
                StringBuilder buff = new StringBuilder();

                buff.append(Character.toUpperCase(property.charAt(0)));
                buff.append(property.substring(1));
                ret = buff.toString();
            }

            return ret;
        }
    }
}

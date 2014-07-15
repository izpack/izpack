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


public interface Profile extends MultiMap<String, Profile.Section>, CommentedMap<String, Profile.Section>
{
    char PATH_SEPARATOR = '/';

    List<String> getHeaderComment();

    void setHeaderComment(List<String> value);

    List<String> getFooterComment();

    void setFooterComment(List<String> value);

    Section add(String sectionName);

    void add(String sectionName, String optionName, Object value);

    <T> T as(Class<T> clazz);

    <T> T as(Class<T> clazz, String prefix);

    String fetch(Object sectionName, Object optionName);

    <T> T fetch(Object sectionName, Object optionName, Class<T> clazz);

    String get(Object sectionName, Object optionName);

    <T> T get(Object sectionName, Object optionName, Class<T> clazz);

    String put(String sectionName, String optionName, Object value);

    Section remove(Profile.Section section);

    String remove(Object sectionName, Object optionName);

    interface Section extends OptionMap
    {
        Section getChild(String key);

        String getName();

        Section getParent();

        String getSimpleName();

        Section addChild(String key);

        String[] childrenNames();

        Section lookup(String... path);

        void removeChild(String key);
    }
}

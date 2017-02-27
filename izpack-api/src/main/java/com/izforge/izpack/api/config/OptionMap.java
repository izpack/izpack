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
package com.izforge.izpack.api.config;

public interface OptionMap extends MultiMap<String, String>, CommentedMap<String, String>
{

    <T> T getAll(Object key, Class<T> clazz);

    void add(String key, Object value);

    void add(String key, Object value, int index);

    <T> T as(Class<T> clazz);

    <T> T as(Class<T> clazz, String keyPrefix);

    String fetch(Object key);

    String fetch(Object key, String defaultValue);

    String fetch(Object key, int index);

    <T> T fetch(Object key, Class<T> clazz);

    <T> T fetch(Object key, Class<T> clazz, T defaultValue);

    <T> T fetch(Object key, int index, Class<T> clazz);

    <T> T fetchAll(Object key, Class<T> clazz);

    void from(Object bean);

    void from(Object bean, String keyPrefix);

    String get(Object key, String defaultValue);

    <T> T get(Object key, Class<T> clazz);

    <T> T get(Object key, Class<T> clazz, T defaultValue);

    <T> T get(Object key, int index, Class<T> clazz);

    String put(String key, Object value);

    String put(String key, Object value, int index);

    void putAll(String key, Object value);

    void to(Object bean);

    void to(Object bean, String keyPrefix);
}

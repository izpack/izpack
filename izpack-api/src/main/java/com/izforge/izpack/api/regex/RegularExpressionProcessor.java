/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2010 Rene Krell
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

package com.izforge.izpack.api.regex;

/**
 * Regular expression utility adapted from and inspired by the PropertyRegEx Ant task
 * (project Ant Contrib)
 *
 * @author René Krell - changes against the original implementation ant-contrib 1.0b3
 * @see <a href='http://ant-contrib.sourceforge.net'>Ant Contrib project</a>
 */
public interface RegularExpressionProcessor
{
    public void setInput(String input);

    public void setDefaultValue(String defaultValue);

    public void setRegexp(String regex) throws RuntimeException;

    public void setReplace(String replace);

    public void setSelect(String select);

    public void setCaseSensitive(boolean caseSensitive);

    public void setGlobal(boolean global);

    public String execute();
}

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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

class BasicProfileSection extends BasicOptionMap implements Profile.Section
{

    private static final long serialVersionUID = 985800697957194374L;
    private static final String[] EMPTY_STRING_ARRAY =
    {
    };
    private static final char REGEXP_ESCAPE_CHAR = '\\';
    private final Pattern childPattern;
    private final String name;
    private final BasicProfile profile;

    protected BasicProfileSection(BasicProfile profile, String name)
    {
        this.profile = profile;
        this.name = name;
        this.childPattern = newChildPattern(name);
    }

    @Override
    public Profile.Section getChild(String key)
    {
        return this.profile.get(childName(key));
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public Profile.Section getParent()
    {
        Profile.Section ret = null;
        int idx = this.name.lastIndexOf(this.profile.getPathSeparator());

        if (idx >= 0)
        {
            String name = this.name.substring(0, idx);

            ret = this.profile.get(name);
        }

        return ret;
    }

    @Override
    public String getSimpleName()
    {
        int idx = this.name.lastIndexOf(this.profile.getPathSeparator());

        return (idx < 0) ? this.name : this.name.substring(idx + 1);
    }

    @Override
    public Profile.Section addChild(String key)
    {
        String name = childName(key);

        return this.profile.add(name);
    }

    @Override
    public String[] childrenNames()
    {
        List<String> names = new ArrayList<String>();

        for (String key : this.profile.keySet())
        {
            if (this.childPattern.matcher(key).matches())
            {
                names.add(key.substring(this.name.length() + 1));
            }
        }

        return names.toArray(EMPTY_STRING_ARRAY);
    }

    @Override
    public Profile.Section lookup(String... parts)
    {
        StringBuilder buff = new StringBuilder();

        for (String part : parts)
        {
            if (buff.length() != 0)
            {
                buff.append(this.profile.getPathSeparator());
            }

            buff.append(part);
        }

        return this.profile.get(childName(buff.toString()));
    }

    @Override
    public void removeChild(String key)
    {
        String name = childName(key);

        this.profile.remove(name);
    }

    @Override
    boolean isPropertyFirstUpper()
    {
        return this.profile.isPropertyFirstUpper();
    }

    @Override
    void resolve(StringBuilder buffer)
    {
        this.profile.resolve(buffer, this);
    }

    private String childName(String key)
    {
        StringBuilder buff = new StringBuilder(this.name);

        buff.append(this.profile.getPathSeparator());
        buff.append(key);

        return buff.toString();
    }

    private Pattern newChildPattern(String name)
    {
        StringBuilder buff = new StringBuilder();

        buff.append('^');
        buff.append(Pattern.quote(name));
        buff.append(REGEXP_ESCAPE_CHAR);
        buff.append(this.profile.getPathSeparator());
        buff.append("[^");
        buff.append(REGEXP_ESCAPE_CHAR);
        buff.append(this.profile.getPathSeparator());
        buff.append("]+$");

        return Pattern.compile(buff.toString());
    }
}

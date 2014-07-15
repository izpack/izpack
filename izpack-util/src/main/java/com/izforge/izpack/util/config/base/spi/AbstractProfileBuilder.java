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

package com.izforge.izpack.util.config.base.spi;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.izforge.izpack.util.config.base.CommentedMap;
import com.izforge.izpack.util.config.base.Config;
import com.izforge.izpack.util.config.base.Ini;
import com.izforge.izpack.util.config.base.Profile;

abstract class AbstractProfileBuilder implements IniHandler
{
    private Profile.Section _currentSection;
    private List<String> lastComments = new ArrayList<String>();

    @Override public void endIni()
    {
        setFooterComment();
    }

    @Override public void endSection()
    {
        _currentSection = null;
    }

    @Override public void handleComment(List<String> comment)
    {
        lastComments.addAll(comment);
    }

    @Override public void handleEmptyLine()
    {
        // Trick to add intermediate comments separated by new line before a property and not to loose them
        lastComments.add("\0");
    }

    @Override public void handleOption(String name, String value)
    {
        if (getConfig().isMultiOption())
        {
            _currentSection.add(name, value);
        }
        else
        {
            _currentSection.put(name, value);
        }

        putComment(_currentSection, name);
    }

    @Override public void startIni()
    {
        lastComments.clear();
    }

    @Override public void startSection(String sectionName)
    {
        if (getConfig().isMultiSection())
        {
            _currentSection = getProfile().add(sectionName);
        }
        else
        {
            Ini.Section s = getProfile().get(sectionName);

            _currentSection = (s == null) ? getProfile().add(sectionName) : s;
        }

       putComment(getProfile(), sectionName);
    }

    abstract Config getConfig();

    abstract Profile getProfile();

    Profile.Section getCurrentSection()
    {
        return _currentSection;
    }

    private void setFooterComment()
    {
        if (getConfig().isComment() &&  !lastComments.isEmpty())
        {
            getProfile().setFooterComment((List<String>)lastComments);
        }
    }


    private void putComment(CommentedMap<String, ?> map, String key)
    {
        if (getConfig().isComment() &&  !lastComments.isEmpty())
        {
            // TODO Handle comments between multi-options
            // (currently, the last one appeared replaces the others)
            map.putComment(key, (List<String>)lastComments);
            lastComments = new LinkedList<String>();
        }
    }
}

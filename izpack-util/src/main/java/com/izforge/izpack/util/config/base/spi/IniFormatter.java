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

import java.io.PrintWriter;
import java.io.Writer;

import com.izforge.izpack.util.config.base.Config;

public class IniFormatter extends AbstractFormatter implements IniHandler
{
    public static IniFormatter newInstance(Writer out, Config config)
    {
        IniFormatter instance = newInstance();

        instance.setOutput((out instanceof PrintWriter) ? (PrintWriter) out : new PrintWriter(out));
        instance.setConfig(config);

        return instance;
    }

    @Override public void endIni()
    {
        getOutput().flush();
    }

    @Override public void endSection()
    {
        getOutput().print(getConfig().getLineSeparator());
    }

    @Override public void startIni()
    {
        assert true;
    }

    @Override public void startSection(String sectionName)
    {
        if (!getConfig().isGlobalSection() || !sectionName.equals(getConfig().getGlobalSectionName()))
        {
            getOutput().print(IniParser.SECTION_BEGIN);
            getOutput().print(escapeFilter(sectionName));
            getOutput().print(IniParser.SECTION_END);
            getOutput().print(getConfig().getLineSeparator());
        }
    }

    private static IniFormatter newInstance()
    {
        return ServiceFinder.findService(IniFormatter.class);
    }
}

/*
 * Copyright 2016 Julien Ponge, René Krell and the IzPack team.
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
package com.izforge.izpack.core.data;

import com.izforge.izpack.api.config.Config;
import com.izforge.izpack.api.config.Options;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Overrides;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.data.VariableMapper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class DefaultOverrides extends Options implements Overrides
{

    public DefaultOverrides(File file) throws IOException
    {
        Config config = Config.getGlobal().clone();
        config.setFileEncoding(Charset.forName("UTF-8"));
        config.setInclude(true);
        config.setComment(true);
        setConfig(config);
        setFile(file);
        //load();
    }

    @Override
    public String fetch(String name)
    {
        return processMapping(name, super.fetch(name));
    }

    @Override
    public String fetch(String name, String defaultValue)
    {
        return processMapping(name, super.fetch(name, defaultValue));
    }

    @Override
    public boolean containsKey(String name)
    {
        return super.containsKey(name);
    }

    @Override
    public String remove(String name)
    {
        return super.remove(name);
    }

    @Override
    public void setInstallData(InstallData installData)
    {
        getConfig().setInstallData(installData);
    }

    private String processMapping(String name, String value)
    {
        boolean isMappingMode = false;
        if (value != null)
        {
            List<String> comments = getComment(name);
            if (comments != null)
            {
                for (String comment : comments)
                {
                    if (comment != null)
                    {
                        comment = comment.trim();
                        String entries[] = comment.split("(\\r?\\n)|;|,");
                        for (String entry : entries)
                        {
                            entry = entry.trim();
                            if (!entry.isEmpty())
                            {
                                if (entry.equals("BEGIN MAP"))
                                {
                                    isMappingMode = true;
                                    continue;
                                }
                                else if (entry.equals("END MAP"))
                                {
                                    isMappingMode = false;
                                    continue;
                                }
                                if (isMappingMode)
                                {
                                    Class<?> mapperClass = null;
                                    VariableMapper instance = null;
                                    try
                                    {
                                        mapperClass = Class.forName(entry);
                                        instance = (VariableMapper) mapperClass.newInstance();
                                        value = instance.map(value);
                                    }
                                    catch (Exception e)
                                    {
                                        throw new IzPackException(
                                                "Failure mapping override of variable '" + name
                                                + "' defined in file " + getFile(), e);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return value;
    }

}

/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.compiler.container;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.core.container.CdiInitializationContext;

/**
 * Fill container with resolver dependencies.
 *
 * @author Anthonin Bonnefoy
 */
public class ResolverContainerFiller
{
    public void fillContainer(CdiInitializationContext context)
    {
//        Properties properties = context.getComponent(Properties.class);
//        for (Map.Entry<Object, Object> entry : getPanelDependencies().entrySet())
//        {
//            properties.put(entry.getKey(), entry.getValue());
//        }

//        context.addComponent(DefaultClassNameMapper.class);
//        context.addComponent(CompilerClassLoader.class);
//        context.addComponent(CompilerPathResolver.class);
//        context.addComponent(MergeableResolver.class);
    }

    private Properties getPanelDependencies()
    {
        Properties properties = new Properties();
        try (InputStream inStream = getClass().getResourceAsStream("panelDependencies.properties"))
        {
            properties.load(inStream);
        }
        catch (IOException e)
        {
            throw new IzPackException(e);
        }
        return properties;
    }
}

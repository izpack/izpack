/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
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
package com.izforge.izpack.core.handler;

import com.izforge.izpack.api.handler.AbstractPrompt;

/**
 * Automated implementation of {@link Prompt}.
 */
public class AutomatedPrompt extends AbstractPrompt
{

    @Override
    public void message(Type type, String title, String message, Throwable throwable)
    {
        if (title != null)
        {
            System.out.println(title + ":");
        }
        System.out.println(message);
        if (throwable != null)
        {
            System.out.println(getDetails(throwable));
        }

    }

    @Override
    public Option confirm(Type type, String title, String message, Options options, Option defaultOption)
    {
        return null;
    }
}

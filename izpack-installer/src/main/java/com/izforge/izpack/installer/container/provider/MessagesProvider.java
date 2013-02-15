/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2013 Tim Anderson
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

package com.izforge.izpack.installer.container.provider;

import org.picocontainer.injectors.Provider;

import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Messages;


/**
 * Provides an {@link Messages} from the current locale.
 *
 * @author Tim Anderson
 */
public class MessagesProvider implements Provider
{

    /**
     * Provides an {@link Messages}.
     *
     * @param locales the locales
     * @return the messages from the current locale
     */
    public Messages provide(Locales locales)
    {
        return locales.getMessages();
    }
}

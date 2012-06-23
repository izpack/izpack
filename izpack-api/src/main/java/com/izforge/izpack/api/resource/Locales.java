/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.api.resource;


import java.util.List;
import java.util.Locale;

import com.izforge.izpack.api.exception.ResourceNotFoundException;


/**
 * Supported locales.
 *
 * @author Tim Anderson
 */
public interface Locales
{

    /**
     * Returns the current locale.
     *
     * @return the current locale. May be <tt>null</tt>
     */
    Locale getLocale();

    /**
     * Sets the current locale.
     *
     * @param locale the locale. May be <tt>null</tt>
     */
    void setLocale(Locale locale);

    /**
     * Returns the locale corresponding to the supplied ISO2/ISO3 code.
     *
     * @param code the 2 or 3 character ISO code
     * @return the corresponding locale, or {@code null} if the locale isn't supported
     */
    Locale getLocale(String code);

    /**
     * Returns the supported locales.
     *
     * @return the supported locales
     */
    List<Locale> getLocales();

    /**
     * Returns messages for the current locale.
     *
     * @return messages for the current locale
     * @throws ResourceNotFoundException if the messages resource cannot be found
     */
    Messages getMessages();

    /**
     * Returns the named messages for the current locale.
     *
     * @param name the message resource name
     * @return messages for the current locale
     * @throws ResourceNotFoundException if the named resource cannot be found
     */
    Messages getMessages(String name);

}

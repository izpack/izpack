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
 * <p/>
 * IzPack uses 3 character ISO language and country codes to identify locale-specific resources rather than
 * the convention used by Java to identify resources bundles.
 * <p/>
 * This has the following restrictions:
 * <ul>
 * <li>resources can either be associated with a particular country code or language code, but not both</li>
 * <li>country codes should be specified as lower-case</li>
 * <li>country codes are used in preference to language codes. This is to handle the case where a language
 * variation is spoken for which there is no ISO language code.
 * <p/>
 * e.g Portuguese and Brazilian Portuguese.
 * <p/>
 * For resources bundles, these would typically be specified as "pt" and "pt_BR". In IzPack,
 * they must be specified using "prt" and "bra" respectively.
 * </li>
 * </ul>
 *
 * @author Tim Anderson
 */
public interface Locales
{

    /**
     * Returns the current locale.
     *
     * @return the current locale. May be {@code null}
     */
    Locale getLocale();

    /**
     * Sets the current locale.
     *
     * @param code the ISO code
     * @throws ResourceNotFoundException if the locale isn't supported
     */
    void setLocale(String code);

    /**
     * Returns the current locale's 3 character ISO code.
     * <p/>
     * This is the code that was used to select the locale's messages and may be a country or language code.
     *
     * @return the current locale's ISO code, or {@code null} if there is no current locale
     */
    String getISOCode();

    /**
     * Returns the locale corresponding to the supplied ISO2/ISO3 code.
     *
     * @param code the ISO code. May be a country or language code
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
     * Returns the 3 character ISO codes of the supported locales.
     * <p/>
     * For backward compatibility:
     * <ol>
     * <li>these should all be lowercase; and</li>
     * <li>may be a mix of language and country codes</li>
     * </ol>
     *
     * @return the ISO codes
     */
    List<String> getISOCodes();

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

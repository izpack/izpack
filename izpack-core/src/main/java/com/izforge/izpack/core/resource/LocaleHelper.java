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

package com.izforge.izpack.core.resource;

import java.util.Locale;
import java.util.MissingResourceException;

/**
 * Locale helper methods.
 *
 * @author Tim Anderson
 */
class LocaleHelper
{

    /**
     * Returns the ISO3 country code for a locale.
     *
     * @param locale the locale
     * @return the locale's 3 character country code, or {@code null} if it doesn't exist
     */
    public static String getISO3Country(Locale locale)
    {
        String result;
        try
        {
            result = locale.getISO3Country();
        }
        catch (MissingResourceException ignore)
        {
            result = null;
        }
        return result;
    }

    /**
     * Returns the ISO3 language code for a locale.
     *
     * @param locale the locale
     * @return the locale's 3 character language code, or {@code null} if it doesn't exist
     */
    public static String getISO3Language(Locale locale)
    {
        String result;
        try
        {
            result = locale.getISO3Language();
        }
        catch (MissingResourceException ignore)
        {
            result = null;
        }
        return result;
    }

}

/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.core;

import java.io.File;
import java.io.FileInputStream;
import java.util.stream.Stream;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.resource.Locales;

/**
 * A JUnit TestCase to check completeness of the all the language packs
 *
 * @author Hans Aikema
 */
@Disabled
public class Bin_Langpacks_InstallerTest
{
    private final static String referencePack = "eng.xml";
    private final static String basePath = "." + File.separator +
            "bin" + File.separator +
            "langpacks" + File.separator +
            "installer" + File.separator;
    private static LocaleDatabase reference;
    private LocaleDatabase check;


    public SoftAssertions collector = new SoftAssertions();

    public static Stream<Arguments> testLangs() {
        return Stream.of(
            Arguments.arguments("cat.xml"),
            Arguments.arguments("chn.xml"),
            Arguments.arguments("ces.xml"),
            Arguments.arguments("dan.xml"),
            Arguments.arguments("deu.xml"),
            Arguments.arguments("ell.xml"),
            Arguments.arguments("eng.xml"),
            Arguments.arguments("fas.xml"),
            Arguments.arguments("fin.xml"),
            Arguments.arguments("fra.xml"),
            Arguments.arguments("hun.xml"),
            Arguments.arguments("idn.xml"),
            Arguments.arguments("ita.xml"),
            Arguments.arguments("jpn.xml"),
            Arguments.arguments("kor.xml"),
            Arguments.arguments("msa.xml"),
            Arguments.arguments("nld.xml"),
            Arguments.arguments("nor.xml"),
            Arguments.arguments("pol.xml"),
            Arguments.arguments("bra.xml"),
            Arguments.arguments("ron.xml"),
            Arguments.arguments("rus.xml"),
            Arguments.arguments("srp.xml"),
            Arguments.arguments("spa.xml"),
            Arguments.arguments("slk.xml"),
            Arguments.arguments("swe.xml"),
            Arguments.arguments("tur.xml"),
            Arguments.arguments("ukr.xml")
            );
    }

    /**
     * Checks all language pack for missing / superfluous translations
     *
     * @param lang The lang pack
     * @throws Exception
     */
    @ParameterizedTest
    @MethodSource
    public void testLangs(String lang) throws Exception
    {
        Bin_Langpacks_InstallerTest.reference = new LocaleDatabase(new FileInputStream(basePath + referencePack),
                                                                   Mockito.mock(Locales.class));
        this.checkLangpack(lang);
    }

    private void checkLangpack(String langpack) throws Exception
    {
        this.check = new LocaleDatabase(new FileInputStream(basePath + langpack), Mockito.mock(Locales.class));
        // all keys in the English langpack should be present in the foreign langpack
        for (String id : reference.keySet())
        {
            if (this.check.containsKey(id))
            {
                collector.collectAssertionError(new AssertionError("Missing translation for id:" + id));
            }
        }
        // there should be no keys in the foreign langpack which don't exist in the 
        // english langpack
        for (String id : this.check.keySet())
        {
            if (reference.containsKey(id))
            {
                collector.collectAssertionError(new AssertionError("Superfluous translation for id:" + id));
            }
        }
    }

}

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

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.resource.Locales;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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

    private final List<Executable> checks = new ArrayList<>();

    public static String[] langs = {"cat.xml",
            "chn.xml",
            "ces.xml",
            "dan.xml",
            "deu.xml",
            "ell.xml",
            "eng.xml",
            "fas.xml"
            , "fin.xml"
            , "fra.xml"
            , "hun.xml"
            , "idn.xml"
            , "ita.xml"
            , "jpn.xml"
            , "kor.xml"
            , "msa.xml"
            , "nld.xml"
            , "nor.xml"
            , "pol.xml"
            , "bra.xml"
            , "ron.xml"
            , "rus.xml"
            , "srp.xml"
            , "spa.xml"
            , "slk.xml"
            , "swe.xml"
            , "tur.xml"
            , "ukr.xml"
    };

    /**
     * Checks all language pack for missing / superfluous translations
     *
     * @param lang The lang pack
     * @throws Exception
     */
    @ParameterizedTest
    @MethodSource("languages")
    public void testLangs(String lang) throws Exception
    {
        Bin_Langpacks_InstallerTest.reference = new LocaleDatabase(new FileInputStream(basePath + referencePack),
                                                                   mock(Locales.class));
        this.checkLangpack(lang);
        assertAll(checks);
    }

    public static String[] languages()
    {
        return langs;
    }

    private void checkLangpack(String langpack) throws Exception
    {
        this.check = new LocaleDatabase(new FileInputStream(basePath + langpack), mock(Locales.class));
        // all keys in the English langpack should be present in the foreign langpack
        for (String id : reference.keySet())
        {
            if (this.check.containsKey(id))
            {
                checks.add(() -> fail("Missing translation for id:" + id));
            }
        }
        // there should be no keys in the foreign langpack which don't exist in the
        // english langpack
        for (String id : this.check.keySet())
        {
            if (reference.containsKey(id))
            {
                checks.add(() -> fail("Superfluous translation for id:" + id));
            }
        }
    }

}

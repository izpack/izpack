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

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.resource.Locales;

/**
 * A JUnit TestCase to check completeness of the all the language packs
 *
 * @author Hans Aikema
 */
@Ignore
@RunWith(Theories.class)
public class Bin_Langpacks_InstallerTest
{
    private final static String referencePack = "eng.xml";
    private final static String basePath = "." + File.separator +
            "bin" + File.separator +
            "langpacks" + File.separator +
            "installer" + File.separator;
    private static LocaleDatabase reference;
    private LocaleDatabase check;

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @DataPoints
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
    @Theory
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
                collector.addError(new Throwable("Missing translation for id:" + id));
            }
        }
        // there should be no keys in the foreign langpack which don't exist in the 
        // english langpack
        for (String id : this.check.keySet())
        {
            if (reference.containsKey(id))
            {
                collector.addError(new Throwable("Superfluous translation for id:" + id));
            }
        }
    }

}

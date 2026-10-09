/*
 * IzPack - Copyright 2001-2009 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2009 Ari Voutilainen
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

package com.izforge.izpack.core.substitutor;

import static org.assertj.core.api.Assertions.assertThat;

import com.izforge.izpack.api.substitutor.SubstitutionType;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.data.DefaultVariables;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * This will test some characters as UTF-8.
 *
 * @author Ari Voutilainen, ari.voutilainen (at) iki.fi
 * @note This test will create VariableSubstitutor object with null parameter!
 * There won't be Properties object available!
 */
public class SubstitutorTest
{
    private final String umlautString = "ÄöäÖüÜ";
    private final String cyrillicString = "Закончить";
    private final String japanesString = "ƒCƒ“ƒXƒg[ƒ‹‚Ì’†Ž~";

    protected VariableSubstitutor subst = new VariableSubstitutorImpl(new DefaultVariables());


    /**
     * (non-Javadoc)
     *
     * Initializes the fixture.
     */
    @BeforeEach
    protected void setUp() throws Exception
    {

    }

    /**
     * (non-Javadoc)
     *
     * Releases the fixture.
     */
    @AfterEach
    protected void tearDown() throws Exception
    {

    }


    /**
     * Tests return value of substitute for string containing umlaut characters.
     */
    @Test
    public void testUmlautString()
    {
        String returnStr = umlautString;
        try
        {
            returnStr = subst.substitute(umlautString, SubstitutionType.TYPE_PLAIN);
        }
        catch (Exception e)
        {
            // ignore
        }
        assertThat(returnStr).isEqualTo(umlautString);
    }

    /**
     * Tests return value of substitute for Cyrillic string.
     */
    @Test
    public void testCyrillicString()
    {
        String returnStr = cyrillicString;
        try
        {
            returnStr = subst.substitute(cyrillicString, SubstitutionType.TYPE_PLAIN);
        }
        catch (Exception e)
        {
            // ignore
        }
        assertThat(returnStr).isEqualTo(cyrillicString);
    }

    /**
     * Tests return value of substitute for Japanese string.
     */
    @Test
    public void testJapaneseString()
    {
        String returnStr = japanesString;
        try
        {
            returnStr = subst.substitute(japanesString, SubstitutionType.TYPE_PLAIN);
        }
        catch (Exception e)
        {
            // ignore
        }
        assertThat(returnStr).isEqualTo(japanesString);
    }
}

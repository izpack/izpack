/*
 * IzPack - Copyright 2021, Hitesh A. Bosamiya, All Rights Reserved.
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

package com.izforge.izpack.core.substitutor;

import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.substitutor.SubstitutionType;
import com.izforge.izpack.core.data.DefaultVariables;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Unit tests of VariableSubstitutorInputStream
 *
 * @author Hitesh A. Bosamiya
 */
public class VariableSubstitutorInputStreamTest
{
    private Variables variables;

    @Before
    public void setupVariableSubstitutorInputStream()
    {
        Properties properties = new Properties(System.getProperties());
        properties.put("PHRASE", "वसुधैव कुटुम्बकम्");
        properties.put("MEANING", "The world is a family");
        variables = new DefaultVariables(properties);
    }

    @Test
    public void shouldReturnUTF8EncodedBytes() throws Exception
    {
        byte[] expectedValues = ("A nice Sanskrit phrase is \"वसुधैव कुटुम्बकम्\"," +
                " meaning in English is \"The world is a family\".").getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream bais =
                new ByteArrayInputStream("A nice Sanskrit phrase is \"$PHRASE\", meaning in English is \"$MEANING\"."
                        .getBytes());
        VariableSubstitutorInputStream inputStream = new VariableSubstitutorInputStream(bais, variables,
                SubstitutionType.TYPE_XML, false);
        assertThat(inputStream.getEncoding(), Is.is("UTF-8"));
        int res;
        int index = 0;
        while ((res = inputStream.read()) != -1)
        {
            assertThat(res, Is.is(expectedValues[index++] & 0xff));
        }
        assertThat(index, Is.is(expectedValues.length));
    }

    @Test
    public void shouldReturnISO_8859_1EncodedBytes() throws Exception
    {
        byte[] expectedValues = ("A nice Sanskrit phrase is \"वसुधैव कुटुम्बकम्\"," +
                " meaning in English is \"The world is a family\".").getBytes(StandardCharsets.ISO_8859_1);
        ByteArrayInputStream bais =
                new ByteArrayInputStream("A nice Sanskrit phrase is \"$PHRASE\", meaning in English is \"$MEANING\"."
                        .getBytes());
        VariableSubstitutorInputStream inputStream = new VariableSubstitutorInputStream(bais, variables,
                SubstitutionType.TYPE_JAVA_PROPERTIES, false);
        assertThat(inputStream.getEncoding(), Is.is("ISO-8859-1"));
        int res;
        int index = 0;
        while ((res = inputStream.read()) != -1)
        {
            assertThat(res, Is.is(expectedValues[index++] & 0xff));
        }
        assertThat(index, Is.is(expectedValues.length));
    }
}

/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
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

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Properties;

import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.core.data.DefaultVariables;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import com.izforge.izpack.api.substitutor.SubstitutionType;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;


/**
 * Unit tests of substitutor features
 *
 * @author Anthonin Bonnefoy
 */
public class VariableSubstitutorImplTest
{

    private VariableSubstitutor variableSubstitutor;

    @Before
    public void setupVariableSubstitutor()
    {
        Properties properties = new Properties(System.getProperties());
        properties.put("MY_PROP", "one");
        properties.put("MY_PROP2", "two");
        properties.put("PHRASE", "वसुधैव कुटुम्बकम्");
        properties.put("MEANING", "The world is a family");
        Variables variables = new DefaultVariables(properties);
        variableSubstitutor = new VariableSubstitutorImpl(variables);
    }

    @Test
    public void shouldNotSubstitute() throws Exception
    {
        String res = variableSubstitutor.substitute("string not substitute", SubstitutionType.TYPE_PLAIN);
        assertThat(res, Is.is("string not substitute"));
        res = variableSubstitutor.substitute("string not ${substitute}", SubstitutionType.TYPE_PLAIN);
        assertThat(res, Is.is("string not ${substitute}"));
    }

    @Test
    public void shouldSubstitutePlainText() throws Exception
    {
        assertThat(
                variableSubstitutor.substitute("Variable ${MY_PROP} and ${MY_PROP2}", SubstitutionType.TYPE_PLAIN),
                Is.is("Variable one and two"));
        assertThat(
                variableSubstitutor.substitute("$MY_PROP2$MY_PROP", SubstitutionType.TYPE_PLAIN),
                Is.is("twoone"));
        assertThat(
                variableSubstitutor.substitute("$MY_PROP2$MY_PRO", SubstitutionType.TYPE_PLAIN),
                Is.is("two$MY_PRO"));
        assertThat(
                variableSubstitutor.substitute("$$$MY_PROP2$MY_PRO", SubstitutionType.TYPE_PLAIN),
                Is.is("$$two$MY_PRO"));
        assertThat(
                variableSubstitutor.substitute("A nice Sanskrit phrase is \"$PHRASE\", meaning in English is \"$MEANING\".", SubstitutionType.TYPE_PLAIN),
                Is.is("A nice Sanskrit phrase is \"वसुधैव कुटुम्बकम्\", meaning in English is \"The world is a family\"."));
    }

    @Test
    public void shouldSubstituteAntType() throws Exception
    {
        assertThat(
                variableSubstitutor.substitute("@MY_PROP@@MY_PROP2@", SubstitutionType.TYPE_ANT),
                Is.is("onetwo"));
        assertThat(
                variableSubstitutor.substitute("@{MY_PROP}", SubstitutionType.TYPE_ANT),
                Is.is("@{MY_PROP}"));
        assertThat(
                variableSubstitutor.substitute("Variable @{MY_PROP}@ and @MY_PROP2@", SubstitutionType.TYPE_ANT),
                Is.is("Variable one and two"));
        assertThat(
                variableSubstitutor.substitute("A nice Sanskrit phrase is \"@PHRASE@\", meaning in English is \"@MEANING@\".", SubstitutionType.TYPE_ANT),
                Is.is("A nice Sanskrit phrase is \"वसुधैव कुटुम्बकम्\", meaning in English is \"The world is a family\"."));
    }

    @Test
    public void shouldSubstituteShellType() throws Exception
    {
        assertThat(
                variableSubstitutor.substitute("%MY_PROP%MY_PROP2", SubstitutionType.TYPE_SHELL),
                Is.is("onetwo"));
    }

    @Test
    public void testSystemPropertiesSubstition() throws Exception
    {
        String substituted = variableSubstitutor.substitute("${SYSTEM[user.dir]}");
        assertNotNull(substituted);
        if (substituted.trim().isEmpty() || substituted.startsWith("${SYSTEM["))
        {
            fail("The system variable resolution of ${SYSTEM[user.dir]} resulted in an invalid string '" + substituted + "\"");
        }
        // TODO: This is just for backward compatibility, remove in future
        substituted = variableSubstitutor.substitute("${SYSTEM_user_dir}");
        assertNotNull(substituted);
        if (substituted.trim().isEmpty() || substituted.startsWith("${SYSTEM_"))
        {
            fail("The system variable resolution of ${SYSTEM_user_dir} resulted in an invalid string '" + substituted + "\"");
        }
    }
}

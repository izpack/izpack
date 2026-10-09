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

package com.izforge.izpack.core.rules;

import static com.izforge.izpack.core.rules.logic.NotCondition.createFromCondition;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.IXMLParser;
import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.container.DefaultContainer;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.rules.logic.AndCondition;
import com.izforge.izpack.core.rules.logic.NotCondition;
import com.izforge.izpack.core.rules.logic.OrCondition;
import com.izforge.izpack.core.rules.logic.XorCondition;
import com.izforge.izpack.core.rules.process.*;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.Platforms;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RulesEngineImplTest
{
    private RulesEngine engine = null;

    /**
     * AIX install condition identifier.
     */
    private static final String AIX_INSTALL = "izpack.aixinstall";

    /**
     * Windows install condition identifier.
     */
    private static final String WINDOWS_INSTALL = "izpack.windowsinstall";

    /**
     * Windows XP install condition identifier.
     */
    private static final String WINDOWS_XP_INSTALL = "izpack.windowsinstall.xp";

    /**
     * Windows 2003 install condition identifier.
     */
    private static final String WINDOWS_2003_INSTALL = "izpack.windowsinstall.2003";

    /**
     * Windows Vista install condition identifier.
     */
    private static final String WINDOWS_VISTA_INSTALL = "izpack.windowsinstall.vista";

    /**
     * Windows 7 install condition identifier.
     */
    private static final String WINDOWS_7_INSTALL = "izpack.windowsinstall.7";

    /**
     * Windows 8 install condition identifier.
     */
    private static final String WINDOWS_8_INSTALL = "izpack.windowsinstall.8";

    /**
     * Windows 10 install condition identifier.
     */
    private static final String WINDOWS_10_INSTALL = "izpack.windowsinstall.10";

    /**
     * Linux install condition identifier.
     */
    private static final String LINUX_INSTALL = "izpack.linuxinstall";

    /**
     * Solaris install condition identifier.
     */
    private static final String SOLARIS_INSTALL = "izpack.solarisinstall";

    /**
     * Solaris x86 install condition identifier.
     */
    private static final String SOLARIS_X86_INSTALL = "izpack.solarisinstall.x86";

    /**
     * Solaris Sparc install condition identifier.
     */
    private static final String SOLARIS_SPARC_INSTALL = "izpack.solarisinstall.sparc";

    /**
     * Mac install condition identifier.
     */
    private static final String MAC_INSTALL = "izpack.macinstall";

    /**
     * OSX install condition identifier.
     */
    private static final String MAC_OSX_INSTALL = "izpack.macinstall.osx";

    /**
     * All install condition identifiers.
     */
    private static final String INSTALL_CONDITIONS[] = {AIX_INSTALL, WINDOWS_INSTALL, WINDOWS_XP_INSTALL,
            WINDOWS_2003_INSTALL, WINDOWS_VISTA_INSTALL, WINDOWS_7_INSTALL, WINDOWS_8_INSTALL, WINDOWS_10_INSTALL,
            LINUX_INSTALL, SOLARIS_INSTALL, SOLARIS_X86_INSTALL, SOLARIS_SPARC_INSTALL, MAC_INSTALL, MAC_OSX_INSTALL};

    @BeforeEach
    public void setUp() throws Exception
    {
        DefaultVariables variables = new DefaultVariables();
        Platform linux = Platforms.LINUX;
        engine = new RulesEngineImpl(new AutomatedInstallData(variables, linux), null, linux);
        variables.setRules(engine);

        Map<String, Condition> conditions = new HashMap<String, Condition>();
        Condition alwaysFalse = new JavaCondition();
        conditions.put("false", alwaysFalse);

        Condition alwaysTrue = createFromCondition(alwaysFalse, engine);
        conditions.put("true", alwaysTrue);

        engine.readConditionMap(conditions);
    }

    @Test
    @SuppressWarnings("PointlessBooleanExpression")
    public void testSimpleNot() throws Exception
    {
        Condition condition;

        condition = engine.getCondition("@!false");
        assertThat(condition.isTrue()).isEqualTo(!false);

        condition = engine.getCondition("@!true");
        assertThat(condition.isTrue()).isEqualTo(!true);
    }

    @Test
    @SuppressWarnings({ "PointlessBooleanExpression", "unused"})
    public void testSimpleAnd() throws Exception
    {
        Condition condition;

        condition = engine.getCondition("@false && false");
        assertThat(condition.isTrue()).isEqualTo(false && false);

        condition = engine.getCondition("@false && true");
        assertThat(condition.isTrue()).isEqualTo(false && true);

        condition = engine.getCondition("@true && false");
        assertThat(condition.isTrue()).isEqualTo(true && false);

        condition = engine.getCondition("@true && true");
        assertThat(condition.isTrue()).isEqualTo(true && true);
    }

    @Test
    @SuppressWarnings({ "PointlessBooleanExpression", "unused"})
    public void testSimpleOr() throws Exception
    {
        Condition condition;

        condition = engine.getCondition("@false || false");
        assertThat(condition.isTrue()).isEqualTo(false || false);

        condition = engine.getCondition("@false || true");
        assertThat(condition.isTrue()).isEqualTo(false || true);

        condition = engine.getCondition("@true || false");
        assertThat(condition.isTrue()).isEqualTo(true || false);

        condition = engine.getCondition("@true || true");
        assertThat(condition.isTrue()).isEqualTo(true || true);
    }

    @Test
    @SuppressWarnings("PointlessBooleanExpression")
    public void testSimpleXor() throws Exception
    {
        Condition condition;

        condition = engine.getCondition("@false ^ false");
        assertThat(condition.isTrue()).isEqualTo(false ^ false);

        condition = engine.getCondition("@false ^ true");
        assertThat(condition.isTrue()).isEqualTo(false ^ true);

        condition = engine.getCondition("@true ^ false");
        assertThat(condition.isTrue()).isEqualTo(true ^ false);

        condition = engine.getCondition("@true ^ true");
        assertThat(condition.isTrue()).isEqualTo(true ^ true);
    }


    @Test
    @SuppressWarnings({ "PointlessBooleanExpression", "unused"})
    public void testComplexNot() throws Exception
    {
        Condition condition;

        condition = engine.getCondition("@!false || false");
        assertThat(condition.isTrue()).isEqualTo(!false || false);

        condition = engine.getCondition("@!true || false");
        assertThat(condition.isTrue()).isEqualTo(!true || false);

        condition = engine.getCondition("@false || !false");
        assertThat(condition.isTrue()).isEqualTo(false || !false);

        condition = engine.getCondition("@true || !false");
        assertThat(condition.isTrue()).isEqualTo(true || !false);

        condition = engine.getCondition("@!false && true");
        assertThat(condition.isTrue()).isEqualTo(!false && true);

        condition = engine.getCondition("@true && !false");
        assertThat(condition.isTrue()).isEqualTo(true && !false);

    }

    @Test
    @SuppressWarnings({ "PointlessBooleanExpression", "unused"})
    public void testComplexAnd() throws Exception
    {
        Condition condition;

        condition = engine.getCondition("@false || false && false || false");
        assertThat(condition.isTrue()).isEqualTo(false || false && false || false);

        condition = engine.getCondition("@false || false && false || true");
        assertThat(condition.isTrue()).isEqualTo(false || false && false || true);

        condition = engine.getCondition("@false || false && true || false");
        assertThat(condition.isTrue()).isEqualTo(false || false && true || false);

        condition = engine.getCondition("@false || false && true || true");
        assertThat(condition.isTrue()).isEqualTo(false || false && true || true);

        condition = engine.getCondition("@false || true && false || false");
        assertThat(condition.isTrue()).isEqualTo(false || true && false || false);

        condition = engine.getCondition("@false || true && false || false");
        assertThat(condition.isTrue()).isEqualTo(false || true && false || false);

        condition = engine.getCondition("@false || true && false || true");
        assertThat(condition.isTrue()).isEqualTo(false || true && false || true);

        condition = engine.getCondition("@false || true && true || false");
        assertThat(condition.isTrue()).isEqualTo(false || true && true || false);

        condition = engine.getCondition("@false || true && true || true");
        assertThat(condition.isTrue()).isEqualTo(false || true && true || true);

        condition = engine.getCondition("@true || false && false || false");
        assertThat(condition.isTrue()).isEqualTo(true || false && false || false);

        condition = engine.getCondition("@true || false && false || true");
        assertThat(condition.isTrue()).isEqualTo(true || false && false || true);

        condition = engine.getCondition("@true || false && true || false");
        assertThat(condition.isTrue()).isEqualTo(true || false && true || false);

        condition = engine.getCondition("@true || false && true || true");
        assertThat(condition.isTrue()).isEqualTo(true || false && true || true);

        condition = engine.getCondition("@true || true && false || false");
        assertThat(condition.isTrue()).isEqualTo(true || true && false || false);

        condition = engine.getCondition("@true || true && false || false");
        assertThat(condition.isTrue()).isEqualTo(true || true && false || false);

        condition = engine.getCondition("@true || true && false || true");
        assertThat(condition.isTrue()).isEqualTo(true || true && false || true);

        condition = engine.getCondition("@true || true && true || false");
        assertThat(condition.isTrue()).isEqualTo(true || true && true || false);

        condition = engine.getCondition("@true || true && true || true");
        assertThat(condition.isTrue()).isEqualTo(true || true && true || true);

    }

    @Test
    @SuppressWarnings({ "PointlessBooleanExpression", "unused"})
    public void testComplexOr() throws Exception
    {
        Condition condition;

        condition = engine.getCondition("@false && false || false && false");
        assertThat(condition.isTrue()).isEqualTo(false && false || false && false);

        condition = engine.getCondition("@false && false || false && true");
        assertThat(condition.isTrue()).isEqualTo(false && false || false && true);

        condition = engine.getCondition("@false && false || true && false");
        assertThat(condition.isTrue()).isEqualTo(false && false || true && false);

        condition = engine.getCondition("@false && false || true && true");
        assertThat(condition.isTrue()).isEqualTo(false && false || true && true);

        condition = engine.getCondition("@false && true || false && false");
        assertThat(condition.isTrue()).isEqualTo(false && true || false && false);

        condition = engine.getCondition("@false && true || false && false");
        assertThat(condition.isTrue()).isEqualTo(false && true || false && false);

        condition = engine.getCondition("@false && true || false && true");
        assertThat(condition.isTrue()).isEqualTo(false && true || false && true);

        condition = engine.getCondition("@false && true || true && false");
        assertThat(condition.isTrue()).isEqualTo(false && true || true && false);

        condition = engine.getCondition("@false && true || true && true");
        assertThat(condition.isTrue()).isEqualTo(false && true || true && true);

        condition = engine.getCondition("@true && false || false && false");
        assertThat(condition.isTrue()).isEqualTo(true && false || false && false);

        condition = engine.getCondition("@true && false || false && true");
        assertThat(condition.isTrue()).isEqualTo(true && false || false && true);

        condition = engine.getCondition("@true && false || true && false");
        assertThat(condition.isTrue()).isEqualTo(true && false || true && false);

        condition = engine.getCondition("@true && false || true && true");
        assertThat(condition.isTrue()).isEqualTo(true && false || true && true);

        condition = engine.getCondition("@true && true || false && false");
        assertThat(condition.isTrue()).isEqualTo(true && true || false && false);

        condition = engine.getCondition("@true && true || false && false");
        assertThat(condition.isTrue()).isEqualTo(true && true || false && false);

        condition = engine.getCondition("@true && true || false && true");
        assertThat(condition.isTrue()).isEqualTo(true && true || false && true);

        condition = engine.getCondition("@true && true || true && false");
        assertThat(condition.isTrue()).isEqualTo(true && true || true && false);

        condition = engine.getCondition("@true && true || true && true");
        assertThat(condition.isTrue()).isEqualTo(true && true || true && true);
    }

    @Test
    @SuppressWarnings({ "PointlessBooleanExpression", "unused"})
    public void testComplexXor() throws Exception
    {
        Condition condition;

        condition = engine.getCondition("@false && false ^ false && false");
        assertThat(condition.isTrue()).isEqualTo(false && false ^ false && false);

        condition = engine.getCondition("@false && false ^ false && true");
        assertThat(condition.isTrue()).isEqualTo(false && false ^ false && true);

        condition = engine.getCondition("@false && false ^ true && false");
        assertThat(condition.isTrue()).isEqualTo(false && false ^ true && false);

        condition = engine.getCondition("@false && false ^ true && true");
        assertThat(condition.isTrue()).isEqualTo(false && false ^ true && true);

        condition = engine.getCondition("@false && true ^ false && false");
        assertThat(condition.isTrue()).isEqualTo(false && true ^ false && false);

        condition = engine.getCondition("@false && true ^ false && false");
        assertThat(condition.isTrue()).isEqualTo(false && true ^ false && false);

        condition = engine.getCondition("@false && true ^ false && true");
        assertThat(condition.isTrue()).isEqualTo(false && true ^ false && true);

        condition = engine.getCondition("@false && true ^ true && false");
        assertThat(condition.isTrue()).isEqualTo(false && true ^ true && false);

        condition = engine.getCondition("@false && true ^ true && true");
        assertThat(condition.isTrue()).isEqualTo(false && true ^ true && true);

        condition = engine.getCondition("@true && false ^ false && false");
        assertThat(condition.isTrue()).isEqualTo(true && false ^ false && false);

        condition = engine.getCondition("@true && false ^ false && true");
        assertThat(condition.isTrue()).isEqualTo(true && false ^ false && true);

        condition = engine.getCondition("@true && false ^ true && false");
        assertThat(condition.isTrue()).isEqualTo(true && false ^ true && false);

        condition = engine.getCondition("@true && false ^ true && true");
        assertThat(condition.isTrue()).isEqualTo(true && false ^ true && true);

        condition = engine.getCondition("@true && true ^ false && false");
        assertThat(condition.isTrue()).isEqualTo(true && true ^ false && false);

        condition = engine.getCondition("@true && true ^ false && false");
        assertThat(condition.isTrue()).isEqualTo(true && true ^ false && false);

        condition = engine.getCondition("@true && true ^ false && true");
        assertThat(condition.isTrue()).isEqualTo(true && true ^ false && true);

        condition = engine.getCondition("@true && true ^ true && false");
        assertThat(condition.isTrue()).isEqualTo(true && true ^ true && false);

        condition = engine.getCondition("@true && true ^ true && true");
        assertThat(condition.isTrue()).isEqualTo(true && true ^ true && true);

        condition = engine.getCondition("@false ^ false && false ^ false");
        assertThat(condition.isTrue()).isEqualTo(false ^ false && false ^ false);

        condition = engine.getCondition("@false ^ false && false ^ true");
        assertThat(condition.isTrue()).isEqualTo(false ^ false && false ^ true);

        condition = engine.getCondition("@false ^ false && true ^ false");
        assertThat(condition.isTrue()).isEqualTo(false ^ false && true ^ false);

        condition = engine.getCondition("@false ^ false && true ^ true");
        assertThat(condition.isTrue()).isEqualTo(false ^ false && true ^ true);

        condition = engine.getCondition("@false ^ true && false ^ false");
        assertThat(condition.isTrue()).isEqualTo(false ^ true && false ^ false);

        condition = engine.getCondition("@false ^ true && false ^ false");
        assertThat(condition.isTrue()).isEqualTo(false ^ true && false ^ false);

        condition = engine.getCondition("@false ^ true && false ^ true");
        assertThat(condition.isTrue()).isEqualTo(false ^ true && false ^ true);

        condition = engine.getCondition("@false ^ true && true ^ false");
        assertThat(condition.isTrue()).isEqualTo(false ^ true && true ^ false);

        condition = engine.getCondition("@false ^ true && true ^ true");
        assertThat(condition.isTrue()).isEqualTo(false ^ true && true ^ true);

        condition = engine.getCondition("@true ^ false && false ^ false");
        assertThat(condition.isTrue()).isEqualTo(true ^ false && false ^ false);

        condition = engine.getCondition("@true ^ false && false ^ true");
        assertThat(condition.isTrue()).isEqualTo(true ^ false && false ^ true);

        condition = engine.getCondition("@true ^ false && true ^ false");
        assertThat(condition.isTrue()).isEqualTo(true ^ false && true ^ false);

        condition = engine.getCondition("@true ^ false && true ^ true");
        assertThat(condition.isTrue()).isEqualTo(true ^ false && true ^ true);

        condition = engine.getCondition("@true ^ true && false ^ false");
        assertThat(condition.isTrue()).isEqualTo(true ^ true && false ^ false);

        condition = engine.getCondition("@true ^ true && false ^ false");
        assertThat(condition.isTrue()).isEqualTo(true ^ true && false ^ false);

        condition = engine.getCondition("@true ^ true && false ^ true");
        assertThat(condition.isTrue()).isEqualTo(true ^ true && false ^ true);

        condition = engine.getCondition("@true ^ true && true ^ false");
        assertThat(condition.isTrue()).isEqualTo(true ^ true && true ^ false);

        condition = engine.getCondition("@true ^ true && true ^ true");
        assertThat(condition.isTrue()).isEqualTo(true ^ true && true ^ true);
    }

    /**
     * Verifies that conditions read from a <tt>conditions.xml</tt> have the expected type.
     */
    @Test
    public void testReadConditionTypes()
    {
        RulesEngine rules = createRulesEngine(new AutomatedInstallData(new DefaultVariables(), Platforms.UNIX));
        IXMLParser parser = new XMLParser();
        IXMLElement conditions = parser.parse(getClass().getResourceAsStream("conditions.xml"));
        rules.analyzeXml(conditions);

        assertThat(rules.getCondition("and1") instanceof AndCondition).isTrue();
        assertThat(rules.getCondition("not1") instanceof NotCondition).isTrue();
        assertThat(rules.getCondition("or1") instanceof OrCondition).isTrue();
        assertThat(rules.getCondition("xor1") instanceof XorCondition).isTrue();
        assertThat(rules.getCondition("variable1") instanceof VariableCondition).isTrue();
        assertThat(rules.getCondition("comparenumerics1") instanceof CompareNumericsCondition).isTrue();
        assertThat(rules.getCondition("compareversions1") instanceof CompareVersionsCondition).isTrue();
        assertThat(rules.getCondition("compareversionsmajor1") instanceof CompareVersionsMajorCondition).isTrue();
        assertThat(rules.getCondition("empty1") instanceof EmptyCondition).isTrue();
        assertThat(rules.getCondition("exists1") instanceof ExistsCondition).isTrue();
        assertThat(rules.getCondition("java1") instanceof JavaCondition).isTrue();
        assertThat(rules.getCondition("packselection1") instanceof PackSelectionCondition).isTrue();
        assertThat(rules.getCondition("ref1") instanceof RefCondition).isTrue();
        assertThat(rules.getCondition("user1") instanceof UserCondition).isTrue();
        assertThat(rules.getCondition("linuxInstallOrUpdate") instanceof AndCondition).isTrue();
    }

    /**
     * Verifies that exception is thrown when reading a poorly defined not condition from <tt>poorly_defined_not_condition.xml</tt>.
     */
    @Test
    public void testPoorlyDefinedNotCondition()
    {
        RulesEngine rules = createRulesEngine(new AutomatedInstallData(new DefaultVariables(), Platforms.UNIX));
        IXMLParser parser = new XMLParser();
        IXMLElement conditions = parser.parse(getClass().getResourceAsStream("poorly_defined_not_condition.xml"));
        Throwable failure = assertThrows(Throwable.class, () -> {
            rules.analyzeXml(conditions);
            rules.getCondition("poorlydefinednot");
        });
        assertThat(failure.getMessage().contains("Missing attribute \"refid\" in condition")).isTrue();
    }

    /**
     * Verifies that exception is thrown when reading a poorly defined not condition from <tt>poorly_defined_and_condition.xml</tt>.
     */
    @Test
    public void testPoorlyDefinedAndConditions()
    {
        RulesEngine rules = createRulesEngine(new AutomatedInstallData(new DefaultVariables(), Platforms.UNIX));
        IXMLParser parser = new XMLParser();
        IXMLElement conditions = parser.parse(getClass().getResourceAsStream("poorly_defined_and_condition.xml"));
        Throwable failure = assertThrows(Throwable.class, () -> {
            rules.analyzeXml(conditions);
            rules.getCondition("poorlydefinedand");
        });
        assertThat(failure.getMessage().contains("Incorrect element specified in condition \"poorlydefinedand\"")).isTrue();
    }

    /**
     * Verifies that the pre-defined platform conditions:
     * <ul>
     * <li>izpack.aixinstall
     * <li>izpack.windowsinstall
     * <li>izpack.windowsinstall.xp
     * <li>izpack.windowsinstall.2003
     * <li>izpack.windowsinstall.vista
     * <li>izpack.windowsinstall.7
     * <li>izpack.windowsinstall.8
     * <li>izpack.windowsinstall.10
     * <li>izpack.linuxinstall
     * <li>izpack.solarisinstall
     * <li>izpack.solarisinstall.x86
     * <li>izpack.solarisinstall.sparc
     * <li>izpack.macinstall
     * <li>izpack.macinstall.osx
     * </ul>
     * evaluate correctly for a range of platforms
     */
    @Test
    public void testPlatformConditions()
    {
        checkPlatformCondition(Platforms.AIX, AIX_INSTALL);
        checkPlatformCondition(Platforms.WINDOWS, WINDOWS_INSTALL);
        checkPlatformCondition(Platforms.WINDOWS_XP, WINDOWS_XP_INSTALL, WINDOWS_INSTALL);
        checkPlatformCondition(Platforms.WINDOWS_2003, WINDOWS_2003_INSTALL, WINDOWS_INSTALL);
        checkPlatformCondition(Platforms.WINDOWS_VISTA, WINDOWS_VISTA_INSTALL, WINDOWS_INSTALL);
        checkPlatformCondition(Platforms.WINDOWS_7, WINDOWS_7_INSTALL, WINDOWS_INSTALL);
        checkPlatformCondition(Platforms.WINDOWS_8, WINDOWS_8_INSTALL, WINDOWS_INSTALL);
        checkPlatformCondition(Platforms.WINDOWS_10, WINDOWS_10_INSTALL, WINDOWS_INSTALL);
        checkPlatformCondition(Platforms.LINUX, LINUX_INSTALL);
        checkPlatformCondition(Platforms.SUNOS, SOLARIS_INSTALL);
        checkPlatformCondition(Platforms.SUNOS_X86, SOLARIS_X86_INSTALL, SOLARIS_INSTALL);
        checkPlatformCondition(Platforms.SUNOS_SPARC, SOLARIS_SPARC_INSTALL, SOLARIS_INSTALL);
        checkPlatformCondition(Platforms.MAC, MAC_INSTALL);
        checkPlatformCondition(Platforms.MAC_OSX, MAC_OSX_INSTALL, MAC_INSTALL);
    }

    /**
     * Verifies that conditions can be serialized and deserialized.
     * <p/>
     * Any serialized built-in conditions should be ignored on deserialization.
     *
     * @throws Exception for any error
     */
    @Test
    public void testSerialization() throws Exception
    {
        // create rules for Windows platform
        InstallData installData1 = new AutomatedInstallData(new DefaultVariables(), Platforms.WINDOWS);
        RulesEngine rules1 = createRulesEngine(installData1);
        IXMLParser parser = new XMLParser();

        // load the conditions
        IXMLElement conditions = parser.parse(getClass().getResourceAsStream("conditions.xml"));
        rules1.analyzeXml(conditions);
        rules1.resolveConditions();

        // verify the conditions evaluate as expected
        checkConditions(rules1, installData1);
        assertThat(rules1.isConditionTrue("izpack.windowsinstall")).isTrue();
        assertThat(rules1.isConditionTrue("izpack.macinstall.osx")).isFalse();

        // serialize the conditions. This includes built-in conditions which should be excluded when read back in.
        Map<String, Condition> read = serializeConditions(rules1);

        // create rules for OSX platform, and populate with the serialized conditions
        InstallData installData2 = new AutomatedInstallData(new DefaultVariables(), Platforms.MAC_OSX);
        RulesEngine rules2 = createRulesEngine(installData2);
        rules2.readConditionMap(read);

        // verify the conditions evaluate as expected
        checkConditions(rules2, installData2);
        assertThat(rules2.isConditionTrue("izpack.windowsinstall")).isFalse();
        assertThat(rules2.isConditionTrue("izpack.macinstall.osx")).isTrue();
    }

    /**
     * Verifies that when conditions are deserialized, any built-in conditions are replaced with those held by the
     * rules engine.
     *
     * @throws Exception for any error
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testSerializeBuiltinConditions() throws Exception
    {
        // create rules for Windows platform
        InstallData installData1 = new AutomatedInstallData(new DefaultVariables(), Platforms.WINDOWS_XP);
        RulesEngine rules1 = createRulesEngine(installData1);
        IXMLParser parser = new XMLParser();

        // load the conditions
        IXMLElement conditions = parser.parse(getClass().getResourceAsStream("builtin_conditions.xml"));
        rules1.analyzeXml(conditions);
        rules1.resolveConditions();

        // verify the conditions evaluate as expected
        assertThat(rules1.isConditionTrue("izpack.windowsinstall.nt5")).isTrue();
        assertThat(rules1.isConditionTrue("izpack.windowsinstall.nt6")).isFalse();
        assertThat(rules1.isConditionTrue("izpack.windowsinstall.nt5OrHigher")).isTrue();

        // serialize the conditions. This includes built-in conditions which should be excluded when read back in.
        Map<String, Condition> read = serializeConditions(rules1);

        // create rules for Windows 7 platform, and populate with the serialized conditions
        InstallData installData2 = new AutomatedInstallData(new DefaultVariables(), Platforms.WINDOWS_7);
        RulesEngine rules2 = createRulesEngine(installData2);
        rules2.readConditionMap(read);

        // verify the conditions evaluate as expected
        assertThat(rules2.isConditionTrue("izpack.windowsinstall.nt5")).isFalse();
        assertThat(rules2.isConditionTrue("izpack.windowsinstall.nt6")).isTrue();
        assertThat(rules2.isConditionTrue("izpack.windowsinstall.nt5OrHigher")).isTrue();
    }

    /**
     * Checks conditions read from the test <em>conditions.xml</em> file.
     *
     * @param rules       the rules
     * @param installData the installation data
     */
    private void checkConditions(RulesEngine rules, InstallData installData)
    {
        installData.setVariable("setup.type", "standard");
        assertThat(rules.isConditionTrue("variable1")).isTrue();    // variable1 = setup.type == standard
        assertThat(rules.isConditionTrue("variable2")).isFalse();   // variable2 = setup.type == expert
        assertThat(rules.isConditionTrue("and1")).isFalse();        // and1 = variable1 && variable2
        assertThat(rules.isConditionTrue("not1")).isFalse();        // not1 = !variable1
        assertThat(rules.isConditionTrue("or1")).isTrue();          // or1 = variable1 || variable2
        assertThat(rules.isConditionTrue("xor1")).isTrue();         // xor1 = variable1 ^ variable2
        assertThat(rules.isConditionTrue("ref1")).isTrue();         // ref1 = variable1

        installData.setVariable("setup.type", "expert");
        assertThat(rules.isConditionTrue("variable1")).isFalse();
        assertThat(rules.isConditionTrue("variable2")).isTrue();
        assertThat(rules.isConditionTrue("and1")).isFalse();
        assertThat(rules.isConditionTrue("not1")).isTrue();
        assertThat(rules.isConditionTrue("or1")).isTrue();
        assertThat(rules.isConditionTrue("xor1")).isTrue();
        assertThat(rules.isConditionTrue("ref1")).isFalse();

        assertThat(rules.isConditionTrue("comparenumerics1")).isTrue();  // comparenumerics1 = 1 < 2
        assertThat(rules.isConditionTrue("compareversions1")).isTrue();  // compareversions1 = 1 < 2
        assertThat(rules.isConditionTrue("compareversionsmajor1")).isTrue();  // compareversions1 = 1.8 eq 1.8.0_72
    }

    /**
     * Verifies that the specified conditions evaluate {@code true} for the specified platform.
     * <p/>
     * Platform conditions not specified will be evaluated to ensure they evaluate {@code false}
     *
     * @param platform   the 'current' platform
     * @param conditions the condition identifiers
     */
    private void checkPlatformCondition(Platform platform, String... conditions)
    {
        DefaultContainer parent = new DefaultContainer();
        RulesEngine rules = new RulesEngineImpl(new AutomatedInstallData(new DefaultVariables(), platform),
                                                new ConditionContainer(parent), platform);
        for (String condition : conditions)
        {
            assertThat(rules.isConditionTrue(condition)).as("Expected " + condition + " to be true").isTrue();
        }
        List<String> falseConditions = new ArrayList<String>(asList(INSTALL_CONDITIONS));
        falseConditions.removeAll(asList(conditions));
        for (String falseCondition : falseConditions)
        {
            assertThat(rules.isConditionTrue(falseCondition)).as("Expected " + falseCondition + " to be false").isFalse();
        }
    }

    /**
     * Helper to serialize and deserialize conditions held by the supplied {@link RulesEngine}.
     *
     * @param rules the rules
     * @return the deserialized conditions
     * @throws IOException            for any I/O error
     * @throws ClassNotFoundException if the class of a serialized object cannot be found
     */
    @SuppressWarnings("unchecked")
    private Map<String, Condition> serializeConditions(RulesEngine rules) throws IOException, ClassNotFoundException
    {
        Map<String, Condition> map = new HashMap<String, Condition>();
        for (String id : rules.getKnownConditionIds())
        {
            map.put(id, rules.getCondition(id));
        }

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
        objectOut.writeObject(map);
        objectOut.close();

        // deserialize the conditions
        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream objectIn = new ObjectInputStream(byteIn);

        return (Map<String, Condition>) objectIn.readObject();
    }

    /**
     * Creates a new {@link RulesEngine}.
     *
     * @param installData the installation data
     * @return a new rules engine
     */
    private RulesEngine createRulesEngine(InstallData installData)
    {
        DefaultContainer parent = new DefaultContainer();
        RulesEngine rules = new RulesEngineImpl(installData, new ConditionContainer(parent), installData.getPlatform());
        parent.addComponent(RulesEngine.class, rules);
        return rules;
    }

}



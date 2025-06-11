/*
 * IzPack - Copyright 2001-2006 Julien Ponge, All Rights Reserved.
 *
 * http://www.izforge.com/izpack/ http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.izforge.izpack.test;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Matcher;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.rules.logic.AndCondition;
import com.izforge.izpack.core.rules.logic.NotCondition;
import com.izforge.izpack.core.rules.logic.OrCondition;
import com.izforge.izpack.core.rules.process.ExistsCondition;
import com.izforge.izpack.core.rules.process.RefCondition;
import com.izforge.izpack.core.rules.process.VariableCondition;
import com.izforge.izpack.test.junit.PicoRunner;

import jakarta.inject.Inject;


/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
@RunWith(PicoRunner.class)
@Container(TestConditionContainer.class)
public class ConditionTest
{
    private static final Matcher<? super Boolean> IS_TRUE = Is.is(true);
    private static final Matcher<? super Boolean> IS_FALSE = Is.is(false);
    private static final Matcher<Object> IS_NULL = IsNull.nullValue();
    private static final Matcher<Object> IS_NOT_NULL = IsNull.notNullValue();
    
    @Inject
    private RulesEngine rules;
    @Inject
    private InstallData idata;

    @Before
    public void setUp() throws Exception
    {
        IXMLElement conditions = new XMLElementImpl("conditions");
        Document ownerDocument = conditions.getElement().getOwnerDocument();
        conditions.addChild(createVariableCondition("test.true", "TEST", "true", ownerDocument));
        conditions.addChild(createRefCondition("test.true2", "test.true", ownerDocument));
//        conditions.addChild(createNotCondition("test.not.true", createVariableCondition("test.true", "TEST", "true", ownerDocument), ownerDocument));
        conditions.addChild(
                createNotCondition("test.not.true", createRefCondition("test.true", ownerDocument), ownerDocument));

        conditions.addChild(createVariableExistsCondition("haveInstallPath", "INSTALL_PATH", ownerDocument));
        conditions.addChild(createVariableCondition("isNewVersion", "previous.version", "UNKNOWN", ownerDocument));
        conditions.addChild(createNotCondition("isUpgradeVersion", createRefCondition("isNewVersion", ownerDocument),
                                               ownerDocument));
        conditions.addChild(createAndCondition("isNew", "isNewVersion", "haveInstallPath", ownerDocument));
        conditions.addChild(createAndCondition("isUpgrade", "isUpgradeVersion", "haveInstallPath", ownerDocument));

        conditions.addChild(createOrCondition("newOrUpgrade", "isNew", "isUpgrade", ownerDocument));

        rules.analyzeXml(conditions);
        rules.resolveConditions();
    }

    @Test
    public void testNotCondition()
    {
        assertThat(rules.getCondition("test.not"), IS_NULL);
        assertThat(rules.getCondition("test.not.true"), IS_NOT_NULL);
        assertThat(rules.isConditionTrue("test.not.true", idata), IS_TRUE);

        assertThat(rules.getCondition("!test.not.true"), IS_NOT_NULL);

        assertThat(rules.isConditionTrue("!test.not.true", idata), IS_FALSE);
    }

    @Test
    public void testVariableCondition()
    {
        assertThat(rules.getCondition("test.true"), IS_NOT_NULL);
        assertThat(rules.getCondition("test.true2"), IS_NOT_NULL);

        assertThat(rules.isConditionTrue("test.true", idata), IS_FALSE);
        assertThat(rules.isConditionTrue("test.true2", idata), IS_FALSE);

        idata.setVariable("TEST", "true");

        assertThat(rules.isConditionTrue("test.true", idata), IS_TRUE);
        assertThat(rules.isConditionTrue("test.true2", idata), IS_TRUE);

        assertThat(rules.isConditionTrue("!test.true", idata), IS_FALSE);
        assertThat(rules.isConditionTrue("!test.true2", idata), IS_FALSE);

        assertThat(rules.isConditionTrue("test.true+test.true2", idata), IS_TRUE);
        assertThat(rules.isConditionTrue("test.true2+test.true", idata), IS_TRUE);

        assertThat(rules.isConditionTrue("!test.true2+test.true", idata), IS_FALSE);

        assertThat(rules.isConditionTrue("test.true2|test.true", idata), IS_TRUE);

        assertThat(rules.isConditionTrue("test.true2\\test.true", idata), IS_FALSE);
    }

    @Test
    public void testNestedReferenceConditions()
    {
        assertThat(rules.isConditionTrue("newOrUpgrade", idata), IS_FALSE);

        idata.setVariable("INSTALL_PATH", "/usr/local/my_app");
        assertThat(rules.isConditionTrue("haveInstallPath", idata), IS_TRUE);

        idata.setVariable("previous.version", "UNKNOWN");
        assertThat(rules.isConditionTrue("isNewVersion", idata), IS_TRUE);
        assertThat(rules.isConditionTrue("isUpgradeVersion", idata), IS_FALSE);
        assertThat(rules.isConditionTrue("isNew", idata), IS_TRUE);
        assertThat(rules.isConditionTrue("isUpgrade", idata), IS_FALSE);

        idata.setVariable("previous.version", "1.0");
        assertThat(rules.isConditionTrue("isNewVersion", idata), IS_FALSE);
        assertThat(rules.isConditionTrue("isUpgradeVersion", idata), IS_TRUE);
        assertThat(rules.isConditionTrue("isNew", idata), IS_FALSE);
        assertThat(rules.isConditionTrue("isUpgrade", idata), IS_TRUE);

        assertThat(rules.isConditionTrue("newOrUpgrade", idata), IS_TRUE);
    }

    /**
     * Creates xml for a {@link NotCondition}.
     *
     * @param id            the condition identifier
     * @param condition     the condition to negate
     * @param ownerDocument the parent document
     * @return a new condition element
     */
    private IXMLElement createNotCondition(String id, IXMLElement condition, Document ownerDocument)
    {
        IXMLElement result = new XMLElementImpl("condition", ownerDocument);
        result.setAttribute("type", "not");
        result.setAttribute("id", id);
        result.addChild(condition);
        return result;
    }

    /**
     * Creates xml for a {@link VariableCondition}.
     *
     * @param id            the condition identifier
     * @param variable      variable
     * @param expression    the variable expression
     * @param ownerDocument the parent document
     * @return a new condition element
     */
    private IXMLElement createVariableCondition(String id, String variable, String expression, Document ownerDocument)
    {
        IXMLElement result = new XMLElementImpl("condition", ownerDocument);
        result.setAttribute("type", "variable");
        result.setAttribute("id", id);
        IXMLElement name = new XMLElementImpl("name", ownerDocument);
        name.setContent(variable);
        IXMLElement value = new XMLElementImpl("value", ownerDocument);
        value.setContent(expression);

        result.addChild(name);
        result.addChild(value);

        return result;
    }

    /**
     * Creates xml for a {@link RefCondition}.
     *
     * @param refid         the reference identifier
     * @param ownerDocument the parent document
     * @return a new condition element
     */
    private IXMLElement createRefCondition(String refid, Document ownerDocument)
    {
        return createRefCondition(null, refid, ownerDocument);
    }

    /**
     * Creates xml for a {@link RefCondition}.
     *
     * @param id            the condition  identifier. May be <tt>null</tt>
     * @param refid         the reference identifier
     * @param ownerDocument the parent document
     * @return a new condition element
     */
    private IXMLElement createRefCondition(String id, String refid, Document ownerDocument)
    {
        IXMLElement result = new XMLElementImpl("condition", ownerDocument);
        result.setAttribute("type", "ref");
        result.setAttribute("refid", refid);
        if (id != null)
        {
            // ID on ref conditions is optional per definition
            result.setAttribute("id", id);
        }

        return result;
    }

    /**
     * Creates xml for an {@link AndCondition}.
     *
     * @param id            the condition identifier
     * @param refid1        the left hand condition reference
     * @param refid2        the right hand condition reference
     * @param ownerDocument the parent document
     * @return a new condition element
     */
    private IXMLElement createAndCondition(String id, String refid1, String refid2, Document ownerDocument)
    {
        IXMLElement result = new XMLElementImpl("condition", ownerDocument);
        result.setAttribute("type", "and");
        result.setAttribute("id", id);
        IXMLElement ref1 = createRefCondition(refid1, ownerDocument);
        IXMLElement ref2 = createRefCondition(refid2, ownerDocument);

        result.addChild(ref1);
        result.addChild(ref2);

        return result;
    }

    /**
     * Creates xml for an {@link OrCondition}.
     *
     * @param id            the condition identifier
     * @param refid1        the left hand condition reference
     * @param refid2        the right hand condition reference
     * @param ownerDocument the parent document
     * @return a new condition element
     */
    private IXMLElement createOrCondition(String id, String refid1, String refid2, Document ownerDocument)
    {
        IXMLElement result = new XMLElementImpl("condition", ownerDocument);
        result.setAttribute("type", "or");
        result.setAttribute("id", id);
        IXMLElement ref1 = createRefCondition(refid1, ownerDocument);
        IXMLElement ref2 = createRefCondition(refid2, ownerDocument);
        result.addChild(ref1);
        result.addChild(ref2);
        return result;
    }

    /**
     * Creates xml for an {@link ExistsCondition}.
     *
     * @param id            the condition identifier
     * @param variable      the variable
     * @param ownerDocument the parent document
     * @return a new condition element
     */
    private IXMLElement createVariableExistsCondition(String id, String variable, Document ownerDocument)
    {
        IXMLElement result = new XMLElementImpl("condition", ownerDocument);
        result.setAttribute("type", "exists");
        result.setAttribute("id", id);
        IXMLElement name = new XMLElementImpl("variable", ownerDocument);
        name.setContent(variable);
        result.addChild(name);
        return result;
    }

}
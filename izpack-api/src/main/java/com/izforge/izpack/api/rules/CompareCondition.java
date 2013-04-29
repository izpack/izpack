/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007-2009 Dennis Reil
 * Copyright 2010-2011 Rene Krell
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

package com.izforge.izpack.api.rules;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;

public abstract class CompareCondition extends Condition
{
    private static final long serialVersionUID = 5449799513620657114L;

    protected String operand1;
    protected String operand2;
    protected ComparisonOperator operator = ComparisonOperator.EQUAL;

    public CompareCondition(String op1, String op2)
    {
        super();
        operand1 = op1;
        operand2 = op2;
    }

    public CompareCondition()
    {
        super();
    }

    public String getLeftOperand()
    {
        return operand1;
    }

    public void setLeftOperand(String value)
    {
        operand1 = value;
    }

    public String getRightOperand()
    {
        return operand2;
    }

    public void setRightOperand(String value)
    {
        operand2 = value;
    }

    public ComparisonOperator getOperator()
    {
        return operator;
    }


    public void setOperator(ComparisonOperator operator)
    {
        this.operator = operator;
    }

    @Override
    public void readFromXML(IXMLElement xmlcondition) throws Exception
    {
        try
        {
            this.operand1 = xmlcondition.getFirstChildNamed("arg1").getContent();
            this.operand2 = xmlcondition.getFirstChildNamed("arg2").getContent();
            String operatorAttr = xmlcondition.getFirstChildNamed("operator").getContent();
            if (operatorAttr != null)
            {
                operator = ComparisonOperator.getComparisonOperatorFromAttribute(operatorAttr);
            }
        }
        catch (Exception e)
        {
            throw new Exception("missing element in <condition type=\"" + getClass().getSimpleName() + "\"/>");
        }

    }

    @Override
    public String getDependenciesDetails()
    {
        StringBuffer details = new StringBuffer();
        details.append(getId());
        details.append(" depends on the values <b>");
        details.append(this.operand1);
        details.append("</b> and <b>");
        details.append(this.operand2);
        details.append("</b>");
        details.append("This value has to be <b>" + this.operator);
        details.append("</b><br/>");
        return details.toString();
    }


    @Override
    public void makeXMLData(IXMLElement conditionRoot)
    {
        XMLElementImpl nameXml = new XMLElementImpl("arg1", conditionRoot);
        nameXml.setContent(this.operand1);
        conditionRoot.addChild(nameXml);
        XMLElementImpl valueXml = new XMLElementImpl("arg2", conditionRoot);
        valueXml.setContent(this.operand2);
        conditionRoot.addChild(valueXml);
        XMLElementImpl opXml = new XMLElementImpl("operator", conditionRoot);
        opXml.setContent(this.operator.getAttribute());
        conditionRoot.addChild(opXml);
    }
}

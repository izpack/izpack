/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007-2009 Dennis Reil
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
package com.izforge.izpack.rules;

import java.util.HashMap;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.impl.XMLElementImpl;

import com.izforge.izpack.util.Debug;

/**
 * @author Dennis Reil, <izpack@reil-online.de>
 */
public class CompareNumericsCondition extends Condition
{       
    private static final long serialVersionUID = 5631805710151645907L;

    protected String variablename;
    protected String value;
    protected String operator;    
    
    public CompareNumericsCondition(String variablename, String value, HashMap packstoremove)
    {
        super();
        this.variablename = variablename;
        this.value = value;
        this.operator = "eq";
    }

    public CompareNumericsCondition(String variablename, String value)
    {
        super();
        this.variablename = variablename;
        this.value = value;
        this.operator = "eq";
    }

    public CompareNumericsCondition()
    {
        super();
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getVariablename()
    {
        return variablename;
    }

    public void setVariablename(String variablename)
    {
        this.variablename = variablename;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.reddot.installer.rules.Condition#readFromXML(com.izforge.izpack.adaptator.IXMLElement)
     */
    public void readFromXML(IXMLElement xmlcondition)
    {
        try
        {
            this.variablename = xmlcondition.getFirstChildNamed("name").getContent();
            this.value = xmlcondition.getFirstChildNamed("value").getContent();
            this.operator = xmlcondition.getFirstChildNamed("operator").getContent();
        }
        catch (Exception e)
        {
            Debug.log("missing element in <condition type=\"variable\"/>");
        }

    }

    public boolean isTrue()
    {
        boolean result = false;
        if (this.installdata != null) {
            String val = this.installdata.getVariable(variablename);
            if (val != null){
                if (operator == null){
                    operator = "eq";                    
                }
                try {
                    //int currentValue = new Integer(val);
                    //int comparisonValue = new Integer(value);
                    float currentValue = new Float(val);
                    float comparisonValue = new Float(value);
                    if ("eq".equalsIgnoreCase(operator)){
                        result = currentValue == comparisonValue;
                    }
                    else if ("gt".equalsIgnoreCase(operator)){
                        result = currentValue > comparisonValue;
                    }
                    else if ("lt".equalsIgnoreCase(operator)){
                        result = currentValue < comparisonValue;
                    }
                    else if ("leq".equalsIgnoreCase(operator)){
                        result = currentValue <= comparisonValue;
                    }
                    else if ("geq".equalsIgnoreCase(operator)){
                        result = currentValue >= comparisonValue;
                    }                                                          
                }
                catch (NumberFormatException nfe){
                    Debug.log("The value of the associated variable is not a numeric value or the value which should be compared is not a number.");                    
                }            
            }
        }                    
        return result;
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.rules.Condition#getDependenciesDetails()
     */
    public String getDependenciesDetails()
    {
        StringBuffer details = new StringBuffer();
        details.append(this.id);
        details.append(" depends on a value of <b>");        
        details.append(this.value);
        details.append("</b> on variable <b>");
        details.append(this.variablename);
        details.append(" (current value: ");
        details.append(this.installdata.getVariable(variablename));
        details.append(")");
        details.append("This value has to be " + this.operator);
        details.append("</b><br/>");
        return details.toString();
    }

    
    public String getOperator()
    {
        return operator;
    }

    
    public void setOperator(String operator)
    {
        this.operator = operator;
    }

    @Override
    public void makeXMLData(IXMLElement conditionRoot)
    {
        XMLElementImpl nameXml = new XMLElementImpl("name",conditionRoot);
        nameXml.setContent(this.variablename);
        conditionRoot.addChild(nameXml);
        XMLElementImpl valueXml = new XMLElementImpl("value",conditionRoot);
        valueXml.setContent(this.value);
        conditionRoot.addChild(valueXml);
        XMLElementImpl opXml = new XMLElementImpl("op",conditionRoot);
        opXml.setContent(this.operator);
        conditionRoot.addChild(opXml);
    }
}
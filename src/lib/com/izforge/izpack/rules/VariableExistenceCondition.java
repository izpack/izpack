/*
 * IzPack - Copyright 2001-2009 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2009 Dennis Reil
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

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.util.Debug;

/**
 * This condition checks if a certain variable has a value. If it is not
 * in the current list of variables it will evaluate to false.
 * 
 * @author Dennis Reil,<izpack@reil-online.de>
 */
public class VariableExistenceCondition extends Condition
{
    private static final long serialVersionUID = -7424383017678759732L;
    
    private String variable;
    
    public VariableExistenceCondition(){
        this.variable = "default.variable";        
    }
    
    
    @Override
    public boolean isTrue()
    {
        boolean result = false;
        String value = this.installdata.getVariable(this.variable);
        if (value != null){
            result = true;
        }
        return result;
    }

    @Override
    public void readFromXML(IXMLElement xmlcondition)
    {
        if (xmlcondition != null){
            IXMLElement variableElement = xmlcondition.getFirstChildNamed("variable");
            if (variableElement != null){
                this.variable = variableElement.getContent();
            }
            else {
                Debug.error("VariableExistenceCondition needs a variable element in its spec.");
            }            
        }
    }


    
    public String getVariable()
    {
        return variable;
    }


    
    public void setVariable(String variable)
    {
        this.variable = variable;
    }


    @Override
    public void makeXMLData(IXMLElement conditionRoot)
    {
        XMLElementImpl variableEl = new XMLElementImpl("variable",conditionRoot);
        variableEl.setContent(this.variable);
        conditionRoot.addChild(variableEl);        
    }

}

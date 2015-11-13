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

import com.izforge.izpack.util.Debug;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.impl.XMLElementImpl;

/**
 * Checks to see whether the user who is running the installer is the same as the user who should be
 * running the installer.
 * 
 * @author J. Chris Folsom <jchrisfolsom@gmail.com>
 * @author Dennis Reil <izpack@reil-online.de>
 */
public class UserCondition extends Condition
{    
    private static final long serialVersionUID = -2076347348048202718L;
    private String requiredUsername;
    
    @Override
    public boolean isTrue()
    {
        boolean result = false;
        if (this.requiredUsername == null)
        {
            Debug.log("Expected user name not set in user condition. Condition will return false.");            
        }
        else
        {
            String actualUsername = System.getProperty("user.name");
            if ((actualUsername != null) && (actualUsername.length() >= 0)){
                result = this.requiredUsername.equals(actualUsername);
            }            
            else {
                Debug.log("No user.name found in system properties. Condition will return false.");
            }
        }
        return result;
    }

    @Override
    public void readFromXML(IXMLElement xmlcondition)
    {
        IXMLElement userElement = xmlcondition.getFirstChildNamed("requiredusername");

        if (userElement == null)
        {
            Debug.log("Condition or type \"user\" requires child element: user");
        }
        else
        {
            this.requiredUsername = userElement.getContent();
        }
    }

    @Override
    public void makeXMLData(IXMLElement conditionRoot)
    {
        XMLElementImpl requiredUserEl = new XMLElementImpl("requiredusername",conditionRoot);
        requiredUserEl.setContent(this.requiredUsername);
        conditionRoot.addChild(requiredUserEl);
        
    }

}

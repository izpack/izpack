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

package com.izforge.izpack.core.rules.process;

import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.rules.Condition;

/**
 * Checks to see whether the user who is running the installer is the same as the user who should be
 * running the installer.
 *
 * @author J. Chris Folsom <jchrisfolsom@gmail.com>
 * @author Dennis Reil <izpack@reil-online.de>
 */
public class UserCondition extends Condition
{
    private static final long serialVersionUID = -8494645616275891103L;

    private static final transient Logger logger = Logger.getLogger(UserCondition.class.getName());

    private String requiredUsername;

    public UserCondition()
    {
        this(null);
    }

    public UserCondition(String requiredUsername)
    {
        this.requiredUsername = requiredUsername;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTrue()
    {
        boolean result = false;
        if (this.requiredUsername == null)
        {
            logger.warning("Condition \"" + getId() + "\": Expected user name not set, condition will return false");
        }
        else
        {
            String actualUsername = System.getProperty("user.name");
            if (actualUsername != null &&  !actualUsername.isEmpty())
            {
                result = this.requiredUsername.equals(actualUsername);
            }
            else
            {
                logger.warning("Condition \"" + getId() + "\": Non-existing or empty system property user.name, condition will return false.");
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readFromXML(IXMLElement xmlcondition) throws Exception
    {
        IXMLElement userElement = xmlcondition.getFirstChildNamed("requiredusername");

        if (userElement == null)
        {
            throw new Exception("Missing \"requiredusername\" element in condition \"" +  getId() + "\"");
        }
        else
        {
            this.requiredUsername = userElement.getContent();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void makeXMLData(IXMLElement conditionRoot)
    {
        XMLElementImpl requiredUserEl = new XMLElementImpl("requiredusername", conditionRoot);
        requiredUserEl.setContent(this.requiredUsername);
        conditionRoot.addChild(requiredUserEl);

    }

}

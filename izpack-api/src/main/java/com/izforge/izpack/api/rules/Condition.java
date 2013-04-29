/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007-2009 Dennis Reil
 * Copyright 2010-2012 René Krell
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

import java.io.Serializable;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;

/**
 * Abstract base class for all conditions. Implementations of custom conditions
 * have to derive from this class.
 *
 * @author Dennis Reil <izpack@reil-online.de>
 */
public abstract class Condition implements Serializable
{
    private static final long serialVersionUID = -1294594086621662171L;

    private String id;

    private transient InstallData installData;

    public Condition()
    {
        this.setId("UNKNOWN");
    }

    /**
     * @return the id
     */
    public String getId()
    {
        return this.id;
    }


    /**
     * @param id the id to set
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * Parse and initialize this condition from parsed values. An exception
     * should be thrown if the condition description has not the expected
     * XML format, something missing or obviously bad values.
     *
     * @param xmlcondition the root element to parse from
     * @throws Exception on a parse error
     */
    public abstract void readFromXML(IXMLElement xmlcondition) throws Exception;

    public abstract boolean isTrue();

    public InstallData getInstallData()
    {
        return installData;
    }


    public void setInstallData(InstallData installData)
    {
        this.installData = installData;
    }

    public String getDependenciesDetails()
    {
        return "No dependencies for this condition.";
    }

    /**
     * This element will be called by the RulesEngine to serialize the configuration
     * of a condition into XML.
     *
     * @param conditionRoot the root element for this condition
     */
    public abstract void makeXMLData(IXMLElement conditionRoot);
}

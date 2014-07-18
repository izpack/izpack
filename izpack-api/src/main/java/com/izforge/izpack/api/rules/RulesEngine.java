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

package com.izforge.izpack.api.rules;

import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.Variables;

/**
 * Interface of rulesEngine
 */
public interface RulesEngine
{
    Set<String> getKnownConditionIds();

    boolean isConditionTrue(String id, InstallData installData);

    boolean isConditionTrue(Condition cond, InstallData installData);

    boolean isConditionTrue(String id);

    boolean isConditionTrue(Condition cond);

    boolean canShowPanel(String panelId, Variables variables);

    void addPanelCondition(Panel panel, Condition newCondition);

    boolean canInstallPack(String packid, Variables variables);

    boolean canInstallPackOptional(String packid, Variables variables);

    void addCondition(Condition condition);

    void writeRulesXML(OutputStream out);

    Condition getCondition(String id);

    void readConditionMap(Map<String, Condition> rules);

    void analyzeXml(IXMLElement conditionsspec);

    @Deprecated
    Condition instantiateCondition(IXMLElement condition);

    /**
     * Creates a condition given its XML specification.
     *
     * @param condition the condition XML specification
     * @return a new  condition
     */
    Condition createCondition(IXMLElement condition);

    /**
     * Check whether references condition exist This must be done after all conditions have been
     * read, to not depend on order of their definition in the XML
     */
    void resolveConditions() throws Exception;

    IXMLElement createConditionElement(Condition condition, IXMLElement root);
}

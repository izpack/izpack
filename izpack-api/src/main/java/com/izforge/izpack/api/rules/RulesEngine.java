package com.izforge.izpack.api.rules;

import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
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

    boolean canShowPanel(String panelid, Variables variables);

    boolean canInstallPack(String packid, Variables variables);

    boolean canInstallPackOptional(String packid, Variables variables);

    void addCondition(Condition condition);

    void writeRulesXML(OutputStream out);

    Condition getCondition(String id);

    void readConditionMap(Map<String, Condition> rules);

    void analyzeXml(IXMLElement conditionsspec);

    @Deprecated
    Condition instanciateCondition(IXMLElement condition);

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

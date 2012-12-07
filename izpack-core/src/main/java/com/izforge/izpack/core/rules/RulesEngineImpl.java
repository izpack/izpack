/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil
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

import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.XMLException;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.adaptator.impl.XMLWriter;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.ConditionReference;
import com.izforge.izpack.api.rules.ConditionWithMultipleOperands;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.rules.logic.AndCondition;
import com.izforge.izpack.core.rules.logic.NotCondition;
import com.izforge.izpack.core.rules.logic.OrCondition;
import com.izforge.izpack.core.rules.logic.XorCondition;
import com.izforge.izpack.core.rules.process.CompareNumericsCondition;
import com.izforge.izpack.core.rules.process.CompareVersionsCondition;
import com.izforge.izpack.core.rules.process.EmptyCondition;
import com.izforge.izpack.core.rules.process.ExistsCondition;
import com.izforge.izpack.core.rules.process.JavaCondition;
import com.izforge.izpack.core.rules.process.PackSelectionCondition;
import com.izforge.izpack.core.rules.process.RefCondition;
import com.izforge.izpack.core.rules.process.UserCondition;
import com.izforge.izpack.core.rules.process.VariableCondition;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.Platforms;


/**
 * The rules engine class is the central point for checking conditions
 *
 * @author Dennis Reil, <Dennis.Reil@reddot.de> created: 09.11.2006, 13:48:39
 */
public class RulesEngineImpl implements RulesEngine
{

    private final Map<String, String> panelConditions = new HashMap<String, String>();

    private final Map<String, String> packConditions = new HashMap<String, String>();

    private final Map<String, String> optionalPackConditions = new HashMap<String, String>();

    private final Map<String, Condition> conditionsMap = new HashMap<String, Condition>();

    private final Set<ConditionReference> refConditions = new HashSet<ConditionReference>();

    private final InstallData installData;

    private final ConditionContainer container;

    private static final Logger logger = Logger.getLogger(RulesEngineImpl.class.getName());

    /**
     * The built-in condition types, with their corresponding class names.
     */
    private static final Map<String, String> TYPE_CLASS_NAMES = new HashMap<String, String>();

    static
    {
        TYPE_CLASS_NAMES.put("and", AndCondition.class.getName());
        TYPE_CLASS_NAMES.put("not", NotCondition.class.getName());
        TYPE_CLASS_NAMES.put("or", OrCondition.class.getName());
        TYPE_CLASS_NAMES.put("xor", XorCondition.class.getName());
        TYPE_CLASS_NAMES.put("comparenumerics", CompareNumericsCondition.class.getName());
        TYPE_CLASS_NAMES.put("compareversions", CompareVersionsCondition.class.getName());
        TYPE_CLASS_NAMES.put("empty", EmptyCondition.class.getName());
        TYPE_CLASS_NAMES.put("exists", ExistsCondition.class.getName());
        TYPE_CLASS_NAMES.put("java", JavaCondition.class.getName());
        TYPE_CLASS_NAMES.put("packselection", PackSelectionCondition.class.getName());
        TYPE_CLASS_NAMES.put("ref", RefCondition.class.getName());
        TYPE_CLASS_NAMES.put("user", UserCondition.class.getName());
        TYPE_CLASS_NAMES.put("variable", VariableCondition.class.getName());
    }

    public RulesEngineImpl(ConditionContainer container, Platform platform)
    {
        this.installData = null;
        this.container = container;
        initStandardConditions(platform);
    }

    public RulesEngineImpl(InstallData installData, ConditionContainer container, Platform platform)
    {
        this.installData = installData;
        this.container = container;
        if (installData != null)
        {
            initStandardConditions(platform);
        }
    }

    @Override
    public void readConditionMap(Map<String, Condition> rules)
    {
        for (Map.Entry<String, Condition> entry : rules.entrySet())
        {
            Condition condition = entry.getValue();
            // skip BuiltinConditions - these must be created by initStandardConditions().
            if (!(condition instanceof BuiltinCondition))
            {
                conditionsMap.put(entry.getKey(), condition);
                condition.setInstallData(installData);
                resolveBuiltinConditions(condition);
            }
        }
    }

    /**
     * Returns the current known condition ids.
     *
     * @return the known condition ids
     */
    @Override
    public Set<String> getKnownConditionIds()
    {
        return conditionsMap.keySet();
    }

    @Override
    @Deprecated
    public Condition instanciateCondition(IXMLElement condition)
    {
        return createCondition(condition);
    }

    /**
     * Creates a condition given its XML specification.
     *
     * @param condition the condition XML specification
     * @return a new  condition
     */
    @Override
    public Condition createCondition(IXMLElement condition)
    {
        String id = condition.getAttribute("id");
        String type = condition.getAttribute("type");
        Condition result = null;
        if (type != null)
        {
            String className = getClassName(type);
            Class<Condition> conditionClass = container.getClass(className, Condition.class);
            try
            {
                if (id == null || id.isEmpty() || "UNKNOWN".equals(id))
                {
                    id = className + "-" + UUID.randomUUID().toString();
                    logger.fine("Random condition id " + id + " generated");
                }
                container.addComponent(id, conditionClass);
                result = (Condition) container.getComponent(id);
                result.setId(id);
                result.setInstallData(installData);
                result.readFromXML(condition);
                conditionsMap.put(id, result);
                if (result instanceof ConditionReference)
                {
                    refConditions.add((ConditionReference) result);
                }
            }
            catch (Exception e)
            {
                throw new IzPackException(e);
            }
        }
        return result;
    }

    @Override
    public void resolveConditions() throws Exception
    {
        for (ConditionReference refCondition : refConditions)
        {
            refCondition.resolveReference();
        }
    }

    /**
     * Read the specification for the conditions.
     *
     * @param conditionsSpec the conditions specification
     */
    @Override
    public void analyzeXml(IXMLElement conditionsSpec)
    {
        if (conditionsSpec == null)
        {
            logger.fine("No conditions specification found");
            return;
        }
        if (conditionsSpec.hasChildren())
        {
            // read in the condition specs
            List<IXMLElement> childs = conditionsSpec.getChildrenNamed("condition");

            for (IXMLElement condition : childs)
            {
                Condition cond = createCondition(condition);
                if (cond != null && !(cond instanceof BuiltinCondition))
                {
                    // this.conditionslist.add(cond);
                    String condid = cond.getId();
                    cond.setInstallData(installData);
                    if ((condid != null) && !("UNKNOWN".equals(condid)))
                    {
                        resolveBuiltinConditions(cond);
                        conditionsMap.put(condid, cond);
                    }
                }
            }

            List<IXMLElement> panelconditionels = conditionsSpec
                    .getChildrenNamed("panelcondition");
            for (IXMLElement panelel : panelconditionels)
            {
                String panelid = panelel.getAttribute("panelid");
                String conditionid = panelel.getAttribute("conditionid");
                this.panelConditions.put(panelid, conditionid);
            }

            List<IXMLElement> packconditionels = conditionsSpec
                    .getChildrenNamed("packcondition");
            for (IXMLElement panelel : packconditionels)
            {
                String panelid = panelel.getAttribute("packid");
                String conditionid = panelel.getAttribute("conditionid");
                this.packConditions.put(panelid, conditionid);
                // optional install allowed, if condition is not met?
                String optional = panelel.getAttribute("optional");
                if (optional != null)
                {
                    boolean optionalinstall = Boolean.valueOf(optional);
                    if (optionalinstall)
                    {
                        // optional installation is allowed
                        this.optionalPackConditions.put(panelid, conditionid);
                    }
                }
            }
        }
    }

    /**
     * Gets the condition for the requested id.
     * The id may be one of the following:
     * A condition ID as defined in the install.xml
     * A simple expression with !,+,|,\
     * A complex expression with !,&&,||,\\ - must begin with char @
     *
     * @param id
     * @return the condition. May be <tt>null</tt>
     */
    @Override
    public Condition getCondition(String id)
    {
        Condition result = conditionsMap.get(id);
        if (result == null)
        {
            if (id.startsWith("@"))
            {
                result = parseComplexCondition(id.substring(1));
            }
            else
            {
                result = getConditionByExpr(new StringBuffer(id));
            }
        }
        return result;
    }

    @Override
    public boolean isConditionTrue(String id, InstallData installData)
    {
        Condition cond = getCondition(id);
        if (cond != null)
        {
            return isConditionTrue(cond, installData);
        }
        logger.warning("Condition " + id + " not found");
        return false;
    }

    @Override
    public boolean isConditionTrue(Condition cond, InstallData installData)
    {
        if (cond != null)
        {
            if (installData != null)
            {
                cond.setInstallData(installData);
            }
            return isConditionTrue(cond);
        }
        return false;
    }

    @Override
    public boolean isConditionTrue(String id)
    {
        Condition cond = getCondition(id);
        if (cond != null)
        {
            return isConditionTrue(cond);
        }
        logger.warning("Condition " + id + " not found");
        return false;
    }

    @Override
    public boolean isConditionTrue(Condition cond)
    {
        if (cond.getInstallData() == null)
        {
            cond.setInstallData(this.installData);
        }
        boolean value = cond.isTrue();
        logger.fine("Condition " + cond.getId() + ": " + Boolean.toString(value));
        return value;
    }

    /**
     * Can a panel be shown?
     *
     * @param panelid   - id of the panel, which should be shown
     * @param variables - the variables
     * @return true - there is no condition or condition is met false - there is a condition and the
     *         condition was not met
     */
    @Override
    public boolean canShowPanel(String panelid, Variables variables)
    {
        if (!this.panelConditions.containsKey(panelid))
        {
            logger.fine("Panel " + panelid + " unconditionally activated");
            return true;
        }
        Condition condition = getCondition(this.panelConditions.get(panelid));
        boolean b = condition.isTrue();
        logger.fine("Panel " + panelid + ": activation depends on condition "
                            + condition.getId() + " -> " + b);
        return b;
    }

    /**
     * Is the installation of a pack possible?
     *
     * @param packid
     * @param variables
     * @return true - there is no condition or condition is met false - there is a condition and the
     *         condition was not met
     */
    @Override
    public boolean canInstallPack(String packid, Variables variables)
    {
        if (packid == null)
        {
            return true;
        }
        if (!this.packConditions.containsKey(packid))
        {
            logger.fine("Package " + packid + " unconditionally installable");
            return true;
        }
        Condition condition = getCondition(this.packConditions.get(packid));
        boolean b = condition.isTrue();
        logger.fine("Package " + packid + ": installation depends on condition "
                            + condition.getId() + " -> " + b);
        return b;
    }

    /**
     * Is an optional installation of a pack possible if the condition is not met?
     *
     * @param packid
     * @param variables
     * @return
     */
    @Override
    public boolean canInstallPackOptional(String packid, Variables variables)
    {
        if (!this.optionalPackConditions.containsKey(packid))
        {
            logger.fine("Package " + packid + " unconditionally installable");
            return false;
        }
        else
        {
            logger.fine("Package " + packid + " optional installation possible");
            return true;
        }
    }

    /**
     * @param condition
     */
    @Override
    public void addCondition(Condition condition)
    {
        if (condition != null)
        {
            String id = condition.getId();
            if (conditionsMap.containsKey(id))
            {
                logger.warning("Condition " + id + " already registered");
            }
            else
            {
                conditionsMap.put(id, condition);
            }
        }
        else
        {
            logger.warning("Could not add condition, was null");
        }
    }

    @Override
    public void writeRulesXML(OutputStream out)
    {
        XMLWriter xmlOut = new XMLWriter();
        xmlOut.setOutput(out);
        XMLElementImpl conditionsel = new XMLElementImpl("conditions");
        for (Condition condition : conditionsMap.values())
        {
            IXMLElement conditionEl = createConditionElement(condition, conditionsel);
            condition.makeXMLData(conditionEl);
            conditionsel.addChild(conditionEl);
        }
        logger.fine("Writing generated conditions specification");
        try
        {
            xmlOut.write(conditionsel);
        }
        catch (XMLException e)
        {
            throw new IzPackException(e);
        }
    }

    @Override
    public IXMLElement createConditionElement(Condition condition, IXMLElement root)
    {
        XMLElementImpl xml = new XMLElementImpl("condition", root);
        xml.setAttribute("id", condition.getId());
        xml.setAttribute("type", condition.getClass().getCanonicalName());
        return xml;
    }

    /**
     * initializes built-in conditions like os conditions and package conditions.
     *
     * @param platform the current platform
     */
    private void initStandardConditions(Platform platform)
    {
        logger.fine("Initializing built-in conditions");
        initOsConditions(platform);
        if ((installData != null) && (installData.getAllPacks() != null))
        {
            logger.fine("Initializing built-in conditions for packs");
            for (Pack pack : installData.getAllPacks())
            {
                // automatically add packselection condition
                PackSelectionCondition selectionCondition = new PackSelectionCondition();
                selectionCondition.setInstallData(installData);
                selectionCondition.setId("izpack.selected." + pack.getName());
                selectionCondition.setPack(pack.getName());
                conditionsMap.put(selectionCondition.getId(), selectionCondition);

                String condition = pack.getCondition();
                if (condition != null && !condition.isEmpty())
                {
                    logger.fine("Adding pack condition \"" + condition + "\" for pack \"" + pack.getName() + "\"");
                    packConditions.put(pack.getName(), condition);
                }
            }
        }
    }

    /**
     * Initialises the pre-defined OS conditions.
     *
     * @param platform the current platform
     */
    private void initOsConditions(Platform platform)
    {
        createPlatformCondition("izpack.aixinstall", platform, Platforms.AIX);
        createPlatformCondition("izpack.windowsinstall", platform, Platforms.WINDOWS);
        createPlatformCondition("izpack.windowsinstall.xp", platform, Platforms.WINDOWS_XP);
        createPlatformCondition("izpack.windowsinstall.2003", platform, Platforms.WINDOWS_2003);
        createPlatformCondition("izpack.windowsinstall.vista", platform, Platforms.WINDOWS_VISTA);
        createPlatformCondition("izpack.windowsinstall.7", platform, Platforms.WINDOWS_7);
        createPlatformCondition("izpack.windowsinstall.8", platform, Platforms.WINDOWS_8);
        createPlatformCondition("izpack.linuxinstall", platform, Platforms.LINUX);
        createPlatformCondition("izpack.solarisinstall", platform, Platforms.SUNOS);
        createPlatformCondition("izpack.macinstall", platform, Platforms.MAC);
        createPlatformCondition("izpack.macinstall.osx", platform, Platforms.MAC_OSX);
        createPlatformCondition("izpack.solarisinstall.x86", platform, Platforms.SUNOS_X86);
        createPlatformCondition("izpack.solarisinstall.sparc", platform, Platforms.SUNOS_SPARC);
    }

    /**
     * Creates a condition to determine if the current platform is that specified.
     *
     * @param conditionId the condition identifier
     * @param current     the current platform
     * @param platform    the platform to compare against
     */
    private void createPlatformCondition(String conditionId, Platform current, Platform platform)
    {
        boolean isA = current.isA(platform);
        // create a condition that simply returns the isA value. This condition doesn't need to be serializable
        Condition condition = new StaticCondition(isA);
        condition.setInstallData(installData);
        condition.setId(conditionId);
        conditionsMap.put(condition.getId(), condition);
    }

    /**
     * Parses the given complex expression into a condition.
     * Understands the boolean operations && (AND), || (OR)
     * and ! (NOT).
     * <p/>
     * Precedence is:
     * NOT is evaluated first.
     * AND is evaluated after NOT, but before OR.
     * OR is evaluated last.
     * <p/>
     * Parentheses may be added at a later time.
     *
     * @param expression
     * @return
     */
    private Condition parseComplexCondition(String expression)
    {
        Condition result = null;

        if (expression.contains("||"))
        {
            result = parseComplexOrCondition(expression);
        }
        else if (expression.contains("&&"))
        {
            result = parseComplexAndCondition(expression);
        }
        else if (expression.contains("^"))
        {
            result = parseComplexXorCondition(expression);
        }
        else if (expression.contains("!"))
        {
            result = parseComplexNotCondition(expression);
        }
        else
        {
            result = conditionsMap.get(expression);
        }

        result.setInstallData(installData);

        return result;
    }

    /**
     * Creates an OR condition from the given complex expression.
     * Uses the substring up to the first || delimiter as first operand and
     * the rest as second operand.
     *
     * @param expression
     * @return OrCondition
     */
    private Condition parseComplexOrCondition(String expression)
    {
        String[] parts = expression.split("\\|\\|", 2);
        OrCondition orCondition = new OrCondition(this);
        orCondition.addOperands(parseComplexCondition(parts[0].trim()), parseComplexCondition(parts[1].trim()));

        return orCondition;
    }

    /**
     * Creates a XOR condition from the given complex expression
     *
     * @param expression
     * @return
     */
    private Condition parseComplexXorCondition(String expression)
    {
        String[] parts = expression.split("\\^", 2);
        XorCondition xorCondition = new XorCondition(this);
        xorCondition.addOperands(parseComplexCondition(parts[0].trim()), parseComplexCondition(parts[1].trim()));

        return xorCondition;
    }

    /**
     * Creates an AND condition from the given complex expression.
     * Uses the expression up to the first && delimiter as first operand and
     * the rest as second operand.
     *
     * @param expression
     * @return AndCondition
     */
    private Condition parseComplexAndCondition(String expression)
    {
        String[] parts = expression.split("\\&\\&", 2);
        AndCondition andCondition = new AndCondition(this);
        andCondition.addOperands(parseComplexCondition(parts[0].trim()), parseComplexCondition(parts[1].trim()));

        return andCondition;
    }

    /**
     * Creates a NOT condition from the given complex expression.
     * Negates the result of the whole expression!
     *
     * @param expression
     * @return NotCondtion
     */
    private Condition parseComplexNotCondition(String expression)
    {
        Condition result = null;
        result = NotCondition.createFromCondition(
                parseComplexCondition(expression.substring(1).trim()),
                this);
        return result;
    }

    private Condition getConditionByExpr(StringBuffer conditionexpr)
    {
        Condition result = null;
        int index = 0;
        while (index < conditionexpr.length())
        {
            char currentchar = conditionexpr.charAt(index);
            switch (currentchar)
            {
                case '+':
                    // and-condition
                    Condition op1 = conditionsMap.get(conditionexpr.substring(0, index));
                    conditionexpr.delete(0, index + 1);
                    result = new AndCondition(this);
                    ((ConditionWithMultipleOperands) result).addOperands(op1, getConditionByExpr(conditionexpr));
                    break;
                case '|':
                    // or-condition
                    op1 = conditionsMap.get(conditionexpr.substring(0, index));
                    conditionexpr.delete(0, index + 1);
                    result = new OrCondition(this);
                    ((ConditionWithMultipleOperands) result).addOperands(op1, getConditionByExpr(conditionexpr));

                    break;
                case '\\':
                    // xor-condition
                    op1 = conditionsMap.get(conditionexpr.substring(0, index));
                    conditionexpr.delete(0, index + 1);
                    result = new XorCondition(this);
                    ((ConditionWithMultipleOperands) result).addOperands(op1, getConditionByExpr(conditionexpr));
                    break;
                case '!':
                    // not-condition
                    if (index > 0)
                    {
                        logger.warning("! operator only allowed at position 0");
                    }
                    else
                    {
                        // delete not symbol
                        conditionexpr.deleteCharAt(index);
                        result = NotCondition.createFromCondition(
                                getConditionByExpr(conditionexpr),
                                this);
                    }
                    break;
                default:
                    // do nothing
            }
            index++;
        }
        if (conditionexpr.length() > 0)
        {
            result = conditionsMap.get(conditionexpr.toString());
            if (result != null)
            {
                result.setInstallData(installData);
                conditionexpr.delete(0, conditionexpr.length());
            }
        }
        return result;
    }

    /**
     * Returns the class name implementing a condition type.
     *
     * @param type the condition type
     * @return the class name
     */
    private String getClassName(String type)
    {
        String result;
        if (type.indexOf('.') != -1)
        {
            // fully qualified class name
            result = type;
        }
        else
        {
            result = TYPE_CLASS_NAMES.get(type);
            if (result == null)
            {
                // probably a bad type...
                result = type;
            }
        }
        return result;
    }

    /**
     * Recursively replaces any built-in conditions referenced by the supplied condition with those held by this.
     *
     * @param condition the condition
     */
    private void resolveBuiltinConditions(Condition condition)
    {
        if (condition instanceof ConditionReference)
        {
            ConditionReference not = (ConditionReference) condition;
            if (not.getReferencedCondition() instanceof StaticCondition)
            {
                not.setReferencedCondition(conditionsMap.get(not.getReferencedCondition().getId()));
            }
            else
            {
                resolveBuiltinConditions(not.getReferencedCondition());
            }
        }
        else if (condition instanceof ConditionWithMultipleOperands)
        {
            ConditionWithMultipleOperands c = (ConditionWithMultipleOperands) condition;
            List<Condition> operands = c.getOperands();
            for (int i = 0; i < operands.size(); ++i)
            {
                Condition operand = operands.get(i);
                if (operand instanceof StaticCondition)
                {
                    operands.set(i, conditionsMap.get(operand.getId()));
                }
                else
                {
                    resolveBuiltinConditions(operand);
                }
            }
        }
    }

    /**
     * A built-in condition, created by the RulesEngine. These are not intended to be serialized - the RulesEngine
     * will replace any instance of a built in condition with its own version.
     */
    private static abstract class BuiltinCondition extends Condition
    {
        @Override
        public void readFromXML(IXMLElement condition) throws Exception
        {
        }

        @Override
        public void makeXMLData(IXMLElement conditionRoot)
        {
        }
    }

    /**
     * A pre-evaluated condition.
     */
    private static class StaticCondition extends BuiltinCondition
    {
        private final boolean result;

        public StaticCondition(boolean result)
        {
            this.result = result;
        }

        @Override
        public boolean isTrue()
        {
            return result;
        }

    }
}

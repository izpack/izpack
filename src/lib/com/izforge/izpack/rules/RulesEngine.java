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
package com.izforge.izpack.rules;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import com.izforge.izpack.Pack;
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.XMLException;
import com.izforge.izpack.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.adaptator.impl.XMLWriter;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.Debug;

/**
 * The rules engine class is the central point for checking conditions
 * 
 * @author Dennis Reil, <Dennis.Reil@reddot.de> created: 09.11.2006, 13:48:39
 */
public class RulesEngine implements Serializable
{

    private static final long serialVersionUID = 3966346766966632860L;

    protected Map<String, String> panelconditions;

    protected Map<String, String> packconditions;

    protected Map<String, String> optionalpackconditions;

    protected IXMLElement conditionsspec;

    protected static Map<String, Condition> conditionsmap = new HashMap<String, Condition>();

    protected static AutomatedInstallData installdata;

    static
    {
        loadStaticConditions();
    }

    private static void loadStaticConditions()
    {
        createBuiltinOsCondition("IS_X86", "izpack.x86install");
        createBuiltinOsCondition("IS_X86_64", "izpack.x86_64install");
        createBuiltinOsCondition("IS_PPC", "izpack.ppcinstall");
        createBuiltinOsCondition("IS_SPARC", "izpack.sparcinstall");
        createBuiltinOsCondition("IS_FREEBSD", "izpack.freebsdinstall");
        createBuiltinOsCondition("IS_LINUX", "izpack.linuxinstall");
        createBuiltinOsCondition("IS_HPUX", "izpack.hpuxinstall");
        createBuiltinOsCondition("IS_AIX", "izpack.aixinstall");
        createBuiltinOsCondition("IS_SUNOS", "izpack.solarisinstall");
        createBuiltinOsCondition("IS_SUNOS_X86", "izpack.solarisinstall.x86");
        createBuiltinOsCondition("IS_SUNOS_SPARC", "izpack.solarisinstall.sparc");
        createBuiltinOsCondition("IS_OS2", "izpack.os2install");
        createBuiltinOsCondition("IS_MAC", "izpack.macinstall");
        createBuiltinOsCondition("IS_OSX", "izpack.osxinstall");
        
        createBuiltinOsCondition("IS_WINDOWS", "izpack.windowsinstall");
        createBuiltinOsCondition("IS_WINDOWS_2000", "izpack.windowsinstall.200");
        createBuiltinOsCondition("IS_WINDOWS_XP", "izpack.windowsinstall.xp");
        createBuiltinOsCondition("IS_WINDOWS_2003", "izpack.windowsinstall.2003");
        createBuiltinOsCondition("IS_WINDOWS_VISTA", "izpack.windowsinstall.vista");
        createBuiltinOsCondition("IS_WINDOWS_2008", "izpack.windowsinstall.2008");
        createBuiltinOsCondition("IS_WINDOWS_2008R2", "izpack.windowsinstall.2008R2");
        createBuiltinOsCondition("IS_WINDOWS_7", "izpack.windowsinstall.7");
        createBuiltinOsCondition("IS_WINDOWS_8", "izpack.windowsinstall.8");

        createBuiltinOsCondition("IS_UNIX", "izpack.unixinstall");
        createBuiltinOsCondition("IS_REDHAT_LINUX", "izpack.linuxinstall.redhat");
        createBuiltinOsCondition("IS_FEDORA_LINUX", "izpack.linuxinstall.fedora");
        createBuiltinOsCondition("IS_UBUNTU_LINUX", "izpack.linuxinstall.ubuntu");
        createBuiltinOsCondition("IS_MANDRAKE_LINUX", "izpack.linuxinstall.mandrake");
        createBuiltinOsCondition("IS_MANDRIVA_LINUX", "izpack.linuxinstall.mandriva");
        createBuiltinOsCondition("IS_SUSE_LINUX", "izpack.linuxinstall.suse");
        createBuiltinOsCondition("IS_DEBIAN_LINUX", "izpack.linuxinstall.debian");
        createBuiltinOsCondition("IS_ORACLE_LINUX", "izpack.linuxinstall.oracle");
        createBuiltinOsCondition("IS_CENTOS_LINUX", "izpack.linuxinstall.centos");
        createBuiltinOsCondition("IS_AMI_LINUX", "izpack.linuxinstall.ami");
        
    }

    private RulesEngine()
    {
        conditionsmap = new Hashtable<String, Condition>();
        this.panelconditions = new Hashtable<String, String>();
        this.packconditions = new Hashtable<String, String>();
        this.optionalpackconditions = new Hashtable<String, String>();
    }

    /**
     * initializes builtin conditions
     */
    private void init()
    {
        Debug.trace("RulesEngine.init()");

        loadStaticConditions();

        if ((installdata != null) && (installdata.allPacks != null))
        {
            Debug.trace("Initializing builtin conditions for packs.");
            for (Pack pack : installdata.allPacks)
            {
                if (pack.id != null)
                {
                    // automatically add packselection condition
                    PackselectionCondition packselcond = new PackselectionCondition();
                    packselcond.setInstalldata(installdata);
                    packselcond.id = "izpack.selected." + pack.id;
                    packselcond.packid = pack.id;
                    conditionsmap.put(packselcond.id, packselcond);

                    Debug.trace("Pack.getCondition(): " + pack.getCondition() + " for pack "
                            + pack.id);
                    if ((pack.getCondition() != null) && pack.getCondition().length() > 0)
                    {
                        Debug.trace("Adding pack condition " + pack.getCondition() + " for pack "
                                + pack.id);
                        packconditions.put(pack.id, pack.getCondition());
                    }
                }

            }
        }
    }

    private static void createBuiltinOsCondition(String osVersionField, String conditionId)
    {
        JavaCondition condition = new JavaCondition();
        condition.setInstalldata(installdata);
        condition.id = conditionId;
        condition.classname = "com.izforge.izpack.util.OsVersion";
        condition.fieldname = osVersionField;
        condition.returnvalue = "true";
        condition.returnvaluetype = "boolean";
        condition.complete = true;
        conditionsmap.put(condition.id, condition);
    }

    /**
     *
     */
    public RulesEngine(IXMLElement conditionsspecxml, AutomatedInstallData installdata)
    {
        this();
        this.conditionsspec = conditionsspecxml;
        RulesEngine.installdata = installdata;
        this.readConditions();
        init();
    }

    public RulesEngine(Map<String, Condition> rules, AutomatedInstallData installdata)
    {
        this();
        Debug.trace("Initializing RulesEngine");
        RulesEngine.installdata = installdata;
        conditionsmap = rules;
        Iterator<String> keyiter = conditionsmap.keySet().iterator();
        while (keyiter.hasNext())
        {
            String key = keyiter.next();
            Condition condition = conditionsmap.get(key);
            condition.setInstalldata(installdata);
        }
        init();
    }

    /**
     * Returns the current known condition ids.
     * 
     * @return
     */
    public String[] getKnownConditionIds()
    {
        String[] conditionids = conditionsmap.keySet().toArray(
                new String[this.conditionsmap.size()]);
        Arrays.sort(conditionids);
        return conditionids;
    }

    /**
     * Checks if an attribute for an xmlelement is set.
     * 
     * @param val value of attribute to check
     * @param attribute the attribute which is checked
     * @param element the element
     * @return true value was set false no value was set
     */
    protected boolean checkAttribute(String val, String attribute, String element)
    {
        if ((val != null) && (val.length() > 0))
        {
            return true;
        }
        else
        {
            Debug.trace("Element " + element + " has to specify an attribute " + attribute);
            return false;
        }
    }

    public static Condition analyzeCondition(IXMLElement condition)
    {
        String condid = condition.getAttribute("id");
        String condtype = condition.getAttribute("type");
        Condition result = null;
        if (condtype != null)
        {
            String conditionclassname = "";
            if (condtype.indexOf('.') > -1)
            {
                conditionclassname = condtype;
            }
            else
            {
                String conditiontype = condtype; //condtype.toLowerCase();
                conditionclassname = "com.izforge.izpack.rules."
                        + conditiontype.substring(0, 1).toUpperCase()
                        + conditiontype.substring(1, conditiontype.length());
                conditionclassname += "Condition";
            }

            try
            {
                Class<Condition> conditionClass = getConditionClass(conditionclassname);
                if (conditionClass != null){
                    result = conditionClass.newInstance();
                    result.readFromXML(condition);
                    if (condid != null)
                    {
                        result.setId(condid);
                    }
                    result.setInstalldata(RulesEngine.installdata);    
                }                
            }
            catch (InstantiationException e)
            {
                Debug.trace(conditionclassname + " couldn't be instantiated.");
            }
            catch (IllegalAccessException e)
            {
                Debug.trace("Illegal access to " + conditionclassname);
            }
        }
        return result;
    }

    private static Class<Condition> getConditionClass(String conditionClassName)
    {
        ClassLoader loader = Thread.currentThread().getContextClassLoader(); // 
        Class<Condition> conditionclass = null;
        try
        {
            conditionclass = (Class<Condition>) loader.loadClass(conditionClassName);
        }
        catch (ClassNotFoundException e)
        {
            Debug.trace(conditionClassName + " not found in context class loader.");
        }
        try
        {
            // try with a different classloader
            loader = RulesEngine.class.getClassLoader();
            conditionclass = (Class<Condition>) loader.loadClass(conditionClassName);
        }
        catch (ClassNotFoundException e)
        {
            Debug.trace(conditionClassName + " not found.");
        }
        return conditionclass;
    }

    /**
     * Read the spec for the conditions
     */
    protected void readConditions()
    {
        if (this.conditionsspec == null)
        {
            Debug.trace("No specification for conditions found.");
            return;
        }
        try
        {
            if (this.conditionsspec.hasChildren())
            {
                // read in the condition specs
                Vector<IXMLElement> childs = this.conditionsspec.getChildrenNamed("condition");

                for (IXMLElement condition : childs)
                {
                    Condition cond = analyzeCondition(condition);
                    if (cond != null)
                    {
                        // this.conditionslist.add(cond);
                        String condid = cond.getId();
                        cond.setInstalldata(RulesEngine.installdata);
                        if ((condid != null) && !("UNKNOWN".equals(condid)))
                        {
                            conditionsmap.put(condid, cond);
                        }
                    }
                }

                Vector<IXMLElement> panelconditionels = this.conditionsspec
                        .getChildrenNamed("panelcondition");
                for (IXMLElement panelel : panelconditionels)
                {
                    String panelid = panelel.getAttribute("panelid");
                    String conditionid = panelel.getAttribute("conditionid");
                    this.panelconditions.put(panelid, conditionid);
                }

                Vector<IXMLElement> packconditionels = this.conditionsspec
                        .getChildrenNamed("packcondition");
                for (IXMLElement panelel : packconditionels)
                {
                    String panelid = panelel.getAttribute("packid");
                    String conditionid = panelel.getAttribute("conditionid");
                    this.packconditions.put(panelid, conditionid);
                    // optional install allowed, if condition is not met?
                    String optional = panelel.getAttribute("optional");
                    if (optional != null)
                    {
                        boolean optionalinstall = Boolean.valueOf(optional);
                        if (optionalinstall)
                        {
                            // optional installation is allowed
                            this.optionalpackconditions.put(panelid, conditionid);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static Condition getCondition(String id)
    {
        Condition result = conditionsmap.get(id);
        if (result == null)
        {
            result = getConditionByExpr(new StringBuffer(id));
        }
        return result;
    }

    protected static Condition getConditionByExpr(StringBuffer conditionexpr)
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
                Condition op1 = conditionsmap.get(conditionexpr.substring(0, index));
                conditionexpr.delete(0, index + 1);
                result = new AndCondition(op1, getConditionByExpr(conditionexpr), RulesEngine.installdata);
                break;
            case '|':
                // or-condition
                op1 = conditionsmap.get(conditionexpr.substring(0, index));
                conditionexpr.delete(0, index + 1);
                result = new OrCondition(op1, getConditionByExpr(conditionexpr), RulesEngine.installdata);
                break;
            case '\\':
                // xor-condition
                op1 = conditionsmap.get(conditionexpr.substring(0, index));
                conditionexpr.delete(0, index + 1);
                result = new XorCondition(op1, getConditionByExpr(conditionexpr), RulesEngine.installdata);
                break;
            case '!':
                // not-condition
                if (index > 0)
                {
                    Debug.trace("error: ! operator only allowed at position 0");
                }
                else
                {
                    // delete not symbol
                    conditionexpr.deleteCharAt(index);
                    result = new NotCondition(getConditionByExpr(conditionexpr), RulesEngine.installdata);
                }
                break;
            default:
                // do nothing
            }
            index++;
        }
        if (conditionexpr.length() > 0)
        {
            result = conditionsmap.get(conditionexpr.toString());
            if (result != null)
            {
                result.setInstalldata(RulesEngine.installdata);
                conditionexpr.delete(0, conditionexpr.length());
            }
        }
        return result;
    }

    public boolean isConditionTrue(String id, Properties variables)
    {
        Condition cond = getCondition(id);
        if (cond == null)
        {
            Debug.trace("Condition (" + id + ") not found.");
            return true;
        }
        else
        {
            Debug.trace("Checking condition");
            if (cond.getInstalldata() == null)
            {
                cond.setInstalldata(installdata);
            }
            try
            {
                return cond.isTrue();
            }
            catch (NullPointerException npe)
            {
                Debug.error("Nullpointerexception checking condition: " + id);
                return false;
            }
        }
    }

    public boolean isConditionTrue(Condition cond, Properties variables)
    {
        if (cond == null)
        {
            Debug.trace("Condition not found.");
            return true;
        }
        else
        {
            Debug.trace("Checking condition");
            return cond.isTrue();
        }
    }

    public boolean isConditionTrue(String id)
    {
        Condition cond = RulesEngine.getCondition(id);
        if (cond != null)
        {
            if (cond.getInstalldata() == null){
                cond.setInstalldata(RulesEngine.installdata);
            }
            return this.isConditionTrue(cond);
        }
        else
        {
            return false;
        }
    }

    public boolean isConditionTrue(Condition cond)
    {
        if (cond.getInstalldata() == null){
            cond.setInstalldata(RulesEngine.installdata);
        }
        return cond.isTrue();
    }

    /**
     * Can a panel be shown?
     * 
     * @param panelid - id of the panel, which should be shown
     * @param variables - the variables
     * @return true - there is no condition or condition is met false - there is a condition and the
     * condition was not met
     */
    public boolean canShowPanel(String panelid, Properties variables)
    {
        Debug.trace("can show panel with id " + panelid + " ?");
        if (!this.panelconditions.containsKey(panelid))
        {
            Debug.trace("no condition, show panel");
            return true;
        }
        Debug.trace("there is a condition");
        Condition condition = getCondition(this.panelconditions.get(panelid));
        if (condition != null) { return condition.isTrue(); }
        return false;
    }

    /**
     * Is the installation of a pack possible?
     * 
     * @param packid
     * @param variables
     * @return true - there is no condition or condition is met false - there is a condition and the
     * condition was not met
     */
    public boolean canInstallPack(String packid, Properties variables)
    {
        if (packid == null) { return true; }
        Debug.trace("can install pack with id " + packid + "?");
        if (!this.packconditions.containsKey(packid))
        {
            Debug.trace("no condition, can install pack");
            return true;
        }
        Debug.trace("there is a condition");
        Condition condition = getCondition(this.packconditions.get(packid));
        if (condition != null) { return condition.isTrue(); }
        return false;
    }

    /**
     * Is an optional installation of a pack possible if the condition is not met?
     * 
     * @param packid
     * @param variables
     * @return
     */
    public boolean canInstallPackOptional(String packid, Properties variables)
    {
        Debug.trace("can install pack optional with id " + packid + "?");
        if (!this.optionalpackconditions.containsKey(packid))
        {
            Debug.trace("not in optionalpackconditions.");
            return false;
        }
        else
        {
            Debug.trace("optional install possible");
            return true;
        }
    }

    /**
     * 
     * @param condition
     */
    public void addCondition(Condition condition)
    {
        if (condition != null)
        {
            if (conditionsmap.containsKey(condition.id))
            {
                //Debug.error("Condition already registered.");
            }
            else
            {
                conditionsmap.put(condition.id, condition);
            }
        }
        else
        {
            Debug.error("Cannot add condition. Condition was null.");
        }
    }

    public void writeRulesXML(OutputStream out)
    {
        XMLWriter xmlOut = new XMLWriter();
        xmlOut.setOutput(out);
        if (conditionsspec != null)
        {
            Debug.trace("Writing original conditions specification.");
            try
            {
                xmlOut.write(conditionsspec);
            }
            catch (XMLException e)
            {
                Debug.error("Error writing condition specification: " + e);
            }
        }
        else
        {
            XMLElementImpl conditionsel = new XMLElementImpl("conditions");
            for (Condition condition : conditionsmap.values())
            {
                IXMLElement conditionEl = createConditionElement(condition, conditionsel);
                condition.makeXMLData(conditionEl);
                conditionsel.addChild(conditionEl);
            }
            Debug.trace("Writing generated conditions specification.");
            try
            {
                xmlOut.write(conditionsel);
            }
            catch (XMLException e)
            {
                Debug.error("Error writing condition specification: " + e);
            }
        }
    }

    public static IXMLElement createConditionElement(Condition condition, IXMLElement root)
    {
        XMLElementImpl xml = new XMLElementImpl("condition", root);
        xml.setAttribute("id", condition.getId());
        xml.setAttribute("type", condition.getClass().getCanonicalName());
        return xml;
    }
}

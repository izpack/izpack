/*
 * IzPack - Copyright 2001-2009 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2009 Matthew Inger
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

package com.izforge.izpack.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.IzPackException;

/**
 * Action which executes a BSF-supported script, which can specify the
 * appropriate interface methods from the InstallerListener,
 * and UninstallerListener as BSF methods.
 *
 * @author minger
 */
public class BSFAction extends ActionBase
{
    private static final long serialVersionUID = 3258131345250005557L;

    public static final String BSFACTIONS = "bsfactions";
    public static final String BSFACTION = "bsfaction";

    // PHASES NOT DEFINED IN ActionBase
    public static final String BEFOREFILE = "beforefile";
    public static final String AFTERFILE = "afterfile";
    public static final String BEFOREDIR = "beforedir";
    public static final String AFTERDIR = "afterdir";
    public static final String BEFOREDELETE = "beforedelete";
    public static final String AFTERDELETE = "afterdelete";

    public static final String BEFOREDELETION = "beforedeletion";
    public static final String AFTERDELETION = "afterdeletion";

    private String script = null;
    private String language = null;
    private String scriptName = null;

    private transient BSFManager manager = null;
    private transient BSFEngine engine = null;
    private static Map<String, MethodDescriptor> orderMethodMap = null;

    private Properties variables = new Properties();

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(BSFAction.class.getName());

    private static class MethodDescriptor
    {
        private String name;
        private String argNames[];

        public MethodDescriptor(String name, String... argNames)
        {
            super();
            this.name = name;
            this.argNames = argNames;
        }
    }

    private static interface MethodExistenceChecker
    {
        boolean isMethodDefined(String method, String scriptName, BSFEngine engine, BSFManager manager)
                throws BSFException;
    }

    private static Map<String, MethodExistenceChecker> langToMethodCheckerMap = new HashMap<String, MethodExistenceChecker>();

    static
    {
        orderMethodMap = new HashMap<String, MethodDescriptor>();

        // UninstallerListener Methods
        orderMethodMap.put(BSFAction.BEFOREDELETION, new MethodDescriptor("beforeDeletion", "files"));
        orderMethodMap.put(BSFAction.AFTERDELETION, new MethodDescriptor("afterDeletion", "files"));
        orderMethodMap.put(BSFAction.BEFOREDELETE, new MethodDescriptor("beforeDelete", "file"));
        orderMethodMap.put(BSFAction.AFTERDELETE, new MethodDescriptor("afterDelete", "file"));

        // InstallerListener Methods
        orderMethodMap.put(BSFAction.BEFOREDIR, new MethodDescriptor("beforeDir", "file", "pack"));
        orderMethodMap.put(BSFAction.AFTERDIR, new MethodDescriptor("afterDir", "file", "pack"));
        orderMethodMap.put(BSFAction.BEFOREFILE, new MethodDescriptor("beforeFile", "file", "pack"));
        orderMethodMap.put(BSFAction.AFTERFILE, new MethodDescriptor("afterFile", "file", "pack"));
        orderMethodMap.put(BEFOREPACKS, new MethodDescriptor("beforePacks", "packs", "npacks"));
        // npacks required for backward compatibility with 4.x
        orderMethodMap.put(AFTERPACKS, new MethodDescriptor("afterPacks", "packs"));
        orderMethodMap.put(BEFOREPACK, new MethodDescriptor("beforePack", "pack", "i"));
        orderMethodMap.put(AFTERPACK, new MethodDescriptor("afterPack", "pack", "i"));

        langToMethodCheckerMap.put("beanshell",
                                   new MethodExistenceChecker()
                                   {
                                       public boolean isMethodDefined(String method, String scriptName,
                                                                      BSFEngine engine, BSFManager manager)
                                               throws BSFException
                                       {
                                           String script = "this.namespace.getMethod(\"" + method + "\", new Class[0])";
                                           Object res = engine.eval(scriptName, 1, 1, script);
                                           return res != null;
                                       }
                                   }
        );

    }

    public BSFAction()
    {
        super();
    }

    public void setScript(String script)
    {
        this.script = script;
    }

    public String getScript()
    {
        return script;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    public void init()
    {
        if (manager == null)
        {
            manager = new BSFManager();
        }

        if (engine == null)
        {
            try
            {
                engine = manager.loadScriptingEngine(language);
                scriptName = "script." + language;
                engine.exec(scriptName, 1, 1, script);
            }
            catch (BSFException exception)
            {
                throw new IzPackException("Failed to initialise BSF", exception);
            }
        }
    }

    public void destroy()
    {
        if (engine != null)
        {
            engine.terminate();
            engine = null;
        }

        if (manager != null)
        {
            manager.terminate();
            manager = null;
        }
    }

    public void executeUninstall(String order, Object... params)
    {
        MethodDescriptor desc = orderMethodMap.get(order);

        if (desc != null)
        {
            try
            {
                for (int i = 0; i < desc.argNames.length; i++)
                {
                    if (params[i] != null)
                    {
                        manager.declareBean(desc.argNames[i], params[i], params[i].getClass());
                    }
                }

                manager.declareBean("variables", variables, Properties.class);

                MethodExistenceChecker checker = langToMethodCheckerMap.get(language);
                if (checker != null)
                {
                    if (!checker.isMethodDefined(desc.name, scriptName, engine, manager))
                    {
                        return;
                    }
                }
                else
                {
                    engine.eval(scriptName, 1, 1, desc.name);
                }

                engine.exec(scriptName, 1, 1, desc.name + "()");
            }
            catch (BSFException exception)
            {
                throw new IzPackException("Failed to execute BSF action: " + desc.name, exception);
            }
            finally
            {
                undeclareBeans(desc, "variables");
            }
        }
    }

    public void execute(String order, Object[] params, InstallData installData)
    {
        MethodDescriptor desc = orderMethodMap.get(order);
        if (desc != null)
        {
            try
            {
                for (int i = 0; i < desc.argNames.length; i++)
                {
                    if (params[i] != null)
                    {
                        manager.declareBean(desc.argNames[i], params[i], params[i].getClass());
                    }
                }
                manager.declareBean("installData", installData, InstallData.class);
                manager.declareBean("idata", installData, InstallData.class); // for backward compatibility with 4.x

                MethodExistenceChecker checker = langToMethodCheckerMap.get(language);
                if (checker != null)
                {
                    if (!checker.isMethodDefined(desc.name, scriptName, engine, manager))
                    {
                        return;
                    }
                }
                else
                {
                    engine.eval(scriptName, 1, 1, desc.name);
                }

                engine.exec(scriptName, 1, 1, desc.name + "()");
            }
            catch (BSFException exception)
            {
                throw new IzPackException("Failed to execute BSF action: " + desc.name, exception);
            }
            finally
            {
                undeclareBeans(desc, "installData", "idata");
            }
        }

    }

    /**
     * Helper to undeclare beans.
     *
     * @param desc the method descriptor
     * @param names additional bean names to undeclare
     */
    private void undeclareBeans(MethodDescriptor desc, String... names)
    {
        try
        {
            for (int i = 0; i < desc.argNames.length; i++)
            {
                manager.undeclareBean(desc.argNames[i]);
            }
            for (String name : names)
            {
                manager.undeclareBean(name);
            }
        }
        catch (BSFException exception)
        {
            logger.log(Level.INFO, "Failed to undeclare beans: " + exception.getMessage(), exception);
        }
    }
}

/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2010 Rene Krell
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

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.DynamicVariable;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.event.ProgressNotifiers;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.data.DynamicVariableImpl;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.core.variable.ConfigFileValue;
import com.izforge.izpack.core.variable.EnvironmentValue;
import com.izforge.izpack.core.variable.ExecValue;
import com.izforge.izpack.core.variable.JarEntryConfigValue;
import com.izforge.izpack.core.variable.PlainConfigFileValue;
import com.izforge.izpack.core.variable.PlainValue;
import com.izforge.izpack.core.variable.RegistryValue;
import com.izforge.izpack.core.variable.ZipEntryConfigFileValue;
import com.izforge.izpack.core.variable.filters.LocationFilter;
import com.izforge.izpack.core.variable.filters.RegularExpressionFilter;
import com.izforge.izpack.util.FileUtil;
import com.izforge.izpack.util.config.ConfigFileTask;
import com.izforge.izpack.util.config.ConfigurableFileCopyTask;
import com.izforge.izpack.util.config.ConfigurableTask;
import com.izforge.izpack.util.config.IniFileCopyTask;
import com.izforge.izpack.util.config.OptionFileCopyTask;
import com.izforge.izpack.util.config.RegistryTask;
import com.izforge.izpack.util.config.SingleConfigurableTask;
import com.izforge.izpack.util.config.SingleConfigurableTask.Entry;
import com.izforge.izpack.util.config.SingleConfigurableTask.Entry.LookupType;
import com.izforge.izpack.util.config.SingleConfigurableTask.Entry.Operation;
import com.izforge.izpack.util.config.SingleConfigurableTask.Entry.Type;
import com.izforge.izpack.util.config.SingleConfigurableTask.Unit;
import com.izforge.izpack.util.config.SingleIniFileTask;
import com.izforge.izpack.util.config.SingleOptionFileTask;
import com.izforge.izpack.util.config.SingleXmlFileMergeTask;
import com.izforge.izpack.util.file.FileNameMapper;
import com.izforge.izpack.util.file.GlobPatternMapper;
import com.izforge.izpack.util.file.types.FileSet;
import com.izforge.izpack.util.file.types.Mapper;
import com.izforge.izpack.util.helper.SpecHelper;


public class ConfigurationInstallerListener extends AbstractProgressInstallerListener
{
    private static final Logger logger = Logger.getLogger(ConfigurationInstallerListener.class.getName());

    /**
     * Name of the specification file
     */
    public static final String SPEC_FILE_NAME = "ConfigurationActionsSpec.xml";

    private static final String ERRMSG_CONFIGACTION_BADATTR = "Bad attribute value in configuration action: {0}=\"{1}\" not allowed";

    /**
     * The configuration actions.
     */
    private final Map<String, Map<Object, List<ConfigurationAction>>> actions
            = new HashMap<String, Map<Object, List<ConfigurationAction>>>();

    /**
     * The resources.
     */
    private final Resources resources;

    /**
     * The variable replacer.
     */
    private final VariableSubstitutor replacer;

    /**
     * The specification helper.
     */
    private SpecHelper spec;

    private VariableSubstitutor substlocal;


    /**
     * Constructs a <tt>ConfigurationInstallerListener</tt>.
     *
     * @param installData the installation data
     * @param resources   the resources
     * @param replacer    the variable replacer
     * @param notifiers   the progress notifiers
     */
    public ConfigurationInstallerListener(InstallData installData, Resources resources,
                                          VariableSubstitutor replacer, ProgressNotifiers notifiers)
    {
        super(installData, notifiers);
        this.resources = resources;
        this.replacer = replacer;
    }

    /**
     * Returns the actions map.
     *
     * @return the actions map
     */
    public Map<String, Map<Object, List<ConfigurationAction>>> getActions()
    {
        return actions;
    }

    /**
     * Initialises the listener.
     *
     * @throws IzPackException for any error
     */
    @Override
    public void initialise()
    {
        spec = new SpecHelper(resources);
        try
        {
            spec.readSpec(SPEC_FILE_NAME, replacer);
        }
        catch (Exception exception)
        {
            throw new IzPackException("Failed to read: " + SPEC_FILE_NAME, exception);
        }
    }

    /**
     * Invoked before packs are installed.
     *
     * @param packs the packs to be installed
     * @throws IzPackException for any error
     */
    @Override
    public void beforePacks(List<Pack> packs)
    {
        if (spec.getSpec() == null)
        {
            return;
        }

        for (Pack p : packs)
        {
            logger.fine("Entering beforepacks configuration action for pack " + p.getName());

            // Resolve data for current pack.
            IXMLElement pack = spec.getPackForName(p.getName());
            if (pack == null)
            {
                continue;
            }

            logger.fine("Found configuration action descriptor for pack " + p.getName());
            // Prepare the action cache
            Map<Object, List<ConfigurationAction>> packActions = new HashMap<Object, List<ConfigurationAction>>();
            packActions.put(ActionBase.BEFOREPACK, new ArrayList<ConfigurationAction>());
            packActions.put(ActionBase.AFTERPACK, new ArrayList<ConfigurationAction>());
            packActions.put(ActionBase.BEFOREPACKS, new ArrayList<ConfigurationAction>());
            packActions.put(ActionBase.AFTERPACKS, new ArrayList<ConfigurationAction>());

            // Get all entries for antcalls.
            List<IXMLElement> configActionEntries = pack.getChildrenNamed("configurationaction");
            if (configActionEntries != null)
            {
                logger.fine("Found " + configActionEntries.size() + " configuration actions");
                if (configActionEntries.size() >= 1)
                {
                    Iterator<IXMLElement> entriesIter = configActionEntries.iterator();
                    while (entriesIter != null && entriesIter.hasNext())
                    {
                        ConfigurationAction act = readConfigAction(entriesIter.next());
                        if (act != null)
                        {
                            logger.fine("Adding " + act.getOrder() + "configuration action with "
                                                + act.getActionTasks().size() + " tasks");
                            (packActions.get(act.getOrder())).add(act);
                        }
                    }
                    // Set for progress bar interaction.
                    if ((packActions.get(ActionBase.AFTERPACKS)).size() > 0)
                    {
                        setProgressNotifier();
                    }
                }
                // Set for progress bar interaction.
                if ((packActions.get(ActionBase.AFTERPACKS)).size() > 0)
                {
                    this.setProgressNotifier();
                }
            }

            actions.put(p.getName(), packActions);
        }
        for (Pack p : packs)
        {
            String currentPack = p.getName();
            performAllActions(currentPack, ActionBase.BEFOREPACKS, null);
        }
    }

    /**
     * Invoked before a pack is installed.
     *
     * @param pack the pack
     * @param i    the pack number
     * @throws IzPackException for any error
     */
    @Override
    public void beforePack(Pack pack, int i)
    {
        performAllActions(pack.getName(), ActionBase.BEFOREPACK, null);
    }


    /**
     * Invoked after a pack is installed.
     *
     * @param pack the pack
     * @param i    the pack number
     * @throws IzPackException for any error
     */
    @Override
    public void afterPack(Pack pack, int i)
    {
        performAllActions(pack.getName(), ActionBase.AFTERPACK, null);
    }

    /**
     * Invoked after packs are installed.
     *
     * @param packs    the installed packs
     * @param listener the progress listener
     * @throws IzPackException for any error
     */
    @Override
    public void afterPacks(List<Pack> packs, ProgressListener listener)
    {
        if (notifyProgress())
        {
            listener.nextStep(getMessage("ConfigurationAction.pack"), getProgressNotifierId(),
                              getActionCount(packs, ActionBase.AFTERPACKS));
        }
        for (Pack pack : packs)
        {
            String currentPack = pack.getName();
            performAllActions(currentPack, ActionBase.AFTERPACKS, listener);
        }
    }

    private int getActionCount(List<Pack> packs, String order)
    {
        int retval = 0;
        for (Pack pack : packs)
        {
            String currentPack = pack.getName();
            List<ConfigurationAction> actList = getActions(currentPack, order);
            if (actList != null)
            {
                retval += actList.size();
            }
        }
        return (retval);
    }

    /**
     * Returns the defined actions for the given pack in the requested order.
     *
     * @param packName name of the pack for which the actions should be returned
     * @param order    order to be used; valid are <i>beforepack</i> and <i>afterpack</i>
     * @return a list which contains all defined actions for the given pack and order
     */
    // -------------------------------------------------------
    protected List<ConfigurationAction> getActions(String packName, String order)
    {
        if (actions == null)
        {
            return null;
        }

        Map<Object, List<ConfigurationAction>> packActions = actions.get(packName);
        if (packActions == null || packActions.size() == 0)
        {
            return null;
        }

        return packActions.get(order);
    }

    /**
     * Performs all actions which are defined for the given pack and order.
     *
     * @param packName name of the pack for which the actions should be performed
     * @param order    order to be used; valid are <i>beforepack</i> and <i>afterpack</i>
     * @throws InstallerException
     */
    private void performAllActions(String packName, String order, ProgressListener listener)
            throws InstallerException
    {
        List<ConfigurationAction> actList = getActions(packName, order);
        if (actList == null || actList.size() == 0)
        {
            return;
        }

        logger.fine("Executing all " + order + " configuration actions for " + packName + " ...");
        for (ConfigurationAction act : actList)
        {
            // Inform progress bar if needed. Works only on AFTER_PACKS
            if (notifyProgress() && order.equals(ActionBase.AFTERPACKS))
            {
                listener.progress((act.getMessageID() != null) ? getMessage(act.getMessageID()) : "");
            }
            else
            {
                try
                {
                    act.performInstallAction();
                }
                catch (Exception e)
                {
                    throw new InstallerException(e);
                }
            }
        }
    }

    /**
     * Returns an ant call which is defined in the given XML element.
     *
     * @param el XML element which contains the description of an ant call
     * @return an ant call which is defined in the given XML element
     * @throws InstallerException
     */
    private ConfigurationAction readConfigAction(IXMLElement el) throws InstallerException
    {
        if (el == null)
        {
            return null;
        }
        ConfigurationAction act = new ConfigurationAction();
        try
        {
            act.setOrder(spec.getRequiredAttribute(el, ActionBase.ORDER));
        }
        catch (Exception e)
        {
            throw new InstallerException(e);
        }

        // Read specific attributes and nested elements
        substlocal = new VariableSubstitutorImpl(readVariables(el));
        act.setActionTasks(readConfigurables(el));
        act.addActionTasks(readConfigurableSets(el));

        return act;
    }

    private String substituteVariables(String name)
    {
        try
        {
            name = replacer.substitute(name);
        }
        catch (Exception exception)
        {
            logger.log(Level.WARNING, "Failed to substitute: " + name, exception);
        }
        if (substlocal != null)
        {
            try
            {
                name = substlocal.substitute(name);
            }
            catch (Exception exception)
            {
                logger.log(Level.WARNING, "Failed to substitute: " + name, exception);
            }
        }
        return name;
    }

    protected List<ConfigurationActionTask> readConfigurableSets(IXMLElement parent) throws InstallerException
    {
        List<ConfigurationActionTask> configtasks = new ArrayList<ConfigurationActionTask>();
        for (IXMLElement el : parent.getChildrenNamed("configurableset"))
        {
            String attrib = requireAttribute(el, "type");
            ConfigType configType;
            if (attrib != null)
            {
                configType = ConfigType.getFromAttribute(attrib);
                if (configType == null)
                {
                    throw new InstallerException("Unknown configurableset type '" + attrib + "'");
                }
            }
            else
            {
                throw new InstallerException("Missing configurableset type");
            }

            ConfigurableTask task;
            switch (configType)
            {
                case OPTIONS:
                    task = new OptionFileCopyTask();
                    readConfigurableSetCommonAttributes(el, (ConfigurableFileCopyTask) task);
                    break;

                case INI:
                    task = new IniFileCopyTask();
                    readConfigurableSetCommonAttributes(el, (ConfigurableFileCopyTask) task);
                    break;
                default:
                    throw new InstallerException(
                            "Type '" + configType.getAttribute() + "' currently not allowed for ConfigurableSet");
            }

            configtasks.add(new ConfigurationActionTask(task, getAttribute(el, "condition"),
                                                        getInstallData().getRules()));
        }
        return configtasks;
    }

    private void readConfigurableSetCommonAttributes(IXMLElement el, ConfigurableFileCopyTask task)
            throws InstallerException
    {
        InstallData idata = getInstallData();
        task.setToDir(FileUtil.getAbsoluteFile(getAttribute(el, "todir"), idata.getInstallPath()));
        task.setToFile(FileUtil.getAbsoluteFile(getAttribute(el, "tofile"), idata.getInstallPath()));
        task.setFile(FileUtil.getAbsoluteFile(getAttribute(el, "fromfile"), idata.getInstallPath()));
        String boolattr = getAttribute(el, "keepOldKeys");
        if (boolattr != null)
        {
            task.setPatchPreserveEntries(Boolean.parseBoolean(boolattr));
        }
        boolattr = getAttribute(el, "keepOldValues");
        if (boolattr != null)
        {
            task.setPatchPreserveValues(Boolean.parseBoolean(boolattr));
        }
        boolattr = getAttribute(el, "resolveExpressions");
        if (boolattr != null)
        {
            task.setPatchResolveExpressions(Boolean.parseBoolean(boolattr));
        }
        boolattr = getAttribute(el, "failonerror");
        if (boolattr != null)
        {
            task.setFailOnError(Boolean.parseBoolean(boolattr));
        }
        boolattr = getAttribute(el, "includeemptydirs");
        if (boolattr != null)
        {
            task.setIncludeEmptyDirs(Boolean.parseBoolean(boolattr));
        }
        boolattr = getAttribute(el, "overwrite");
        if (boolattr != null)
        {
            task.setOverwrite(Boolean.parseBoolean(boolattr));
        }
        boolattr = getAttribute(el, "preservelastmodified");
        if (boolattr != null)
        {
            task.setPreserveLastModified(Boolean.parseBoolean(boolattr));
        }
        boolattr = getAttribute(el, "enablemultiplemappings");
        if (boolattr != null)
        {
            task.setEnableMultipleMappings(Boolean.parseBoolean(boolattr));
        }
        boolattr = getAttribute(el, "cleanup");
        if (boolattr != null)
        {
            task.setCleanup(Boolean.parseBoolean(boolattr));
        }
        for (FileSet fs : readFileSets(el))
        {
            task.addFileSet(fs);
        }
        try
        {
            for (FileNameMapper mapper : readMapper(el))
            {
                task.add(mapper);
            }
        }
        catch (Exception e)
        {
            throw new InstallerException(e.getMessage());
        }
    }

    private void readSingleConfigurableTaskCommonAttributes(IXMLElement el, SingleConfigurableTask task)
            throws InstallerException
    {
        String attr = getAttribute(el, "create");
        if (attr != null)
        {
            task.setCreate(Boolean.parseBoolean(attr));
        }
        attr = getAttribute(el, "keepOldKeys");
        if (attr != null)
        {
            task.setPatchPreserveEntries(Boolean.parseBoolean(attr));
        }
        attr = getAttribute(el, "keepOldValues");
        if (attr != null)
        {
            task.setPatchPreserveValues(Boolean.parseBoolean(attr));
        }
        attr = getAttribute(el, "resolveExpressions");
        if (attr != null)
        {
            task.setPatchResolveVariables(Boolean.parseBoolean(attr));
        }
        attr = getAttribute(el, "escape");
        if (attr != null)
        {
            task.setEscape(Boolean.parseBoolean(attr));
        }
        attr = getAttribute(el, "escapeNewLine");
        if (attr != null)
        {
            task.setEscapeNewLine(Boolean.parseBoolean(attr));
        }
        attr = getAttribute(el, "headerComment");
        if (attr != null)
        {
            task.setHeaderComment(Boolean.parseBoolean(attr));
        }
        attr = getAttribute(el, "emptyLines");
        if (attr != null)
        {
            task.setEmptyLines(Boolean.parseBoolean(attr));
        }
        attr = getAttribute(el, "operator");
        if (attr != null)
        {
            task.setOperator(attr);
        }
    }

    private void readConfigFileTaskCommonAttributes(InstallData idata,
                                                    IXMLElement el, ConfigFileTask task)
            throws InstallerException
    {
        File tofile = FileUtil.getAbsoluteFile(requireAttribute(el, "tofile"), idata.getInstallPath());
        task.setToFile(tofile);
        task.setOldFile(FileUtil.getAbsoluteFile(getAttribute(el, "patchfile"), idata.getInstallPath()));
        File newfile = FileUtil.getAbsoluteFile(getAttribute(el, "originalfile"), idata.getInstallPath());
        task.setNewFile(newfile);
        String boolattr = getAttribute(el, "cleanup");
        if (boolattr != null)
        {
            task.setCleanup(Boolean.parseBoolean(boolattr));
        }
    }

    protected List<ConfigurationActionTask> readConfigurables(IXMLElement parent) throws InstallerException
    {
        List<ConfigurationActionTask> configtasks = new ArrayList<ConfigurationActionTask>();
        InstallData idata = getInstallData();
        for (IXMLElement el : parent.getChildrenNamed("configurable"))
        {
            String attrib = requireAttribute(el, "type");
            ConfigType configType;
            if (attrib != null)
            {
                configType = ConfigType.getFromAttribute(attrib);
                if (configType == null)
                {
                    throw new InstallerException("Unknown configurable type '" + attrib + "'");
                }
            }
            else
            {
                throw new InstallerException("Missing configurable type");
            }

            ConfigurableTask task;
            switch (configType)
            {
                case OPTIONS:
                    task = new SingleOptionFileTask();
                    readConfigFileTaskCommonAttributes(idata, el, (ConfigFileTask) task);
                    readSingleConfigurableTaskCommonAttributes(el, (SingleConfigurableTask) task);
                    readAndAddEntries(el, (SingleConfigurableTask) task);
                    break;

                case INI:
                    task = new SingleIniFileTask();
                    readConfigFileTaskCommonAttributes(idata, el, (ConfigFileTask) task);
                    readSingleConfigurableTaskCommonAttributes(el, (SingleConfigurableTask) task);
                    readAndAddEntries(el, (SingleConfigurableTask) task);
                    break;

                case XML:
                    task = new SingleXmlFileMergeTask();
                    File tofile = FileUtil.getAbsoluteFile(requireAttribute(el, "tofile"), idata.getInstallPath());
                    ((SingleXmlFileMergeTask) task).setToFile(tofile);
                    ((SingleXmlFileMergeTask) task).setPatchFile(
                            FileUtil.getAbsoluteFile(getAttribute(el, "patchfile"), idata.getInstallPath()));
                    File originalfile = FileUtil.getAbsoluteFile(getAttribute(el, "originalfile"),
                                                                 idata.getInstallPath());
                    if (originalfile == null)
                    {
                        originalfile = tofile;
                    }
                    ((SingleXmlFileMergeTask) task).setOriginalFile(originalfile);
                    ((SingleXmlFileMergeTask) task).setConfigFile(
                            FileUtil.getAbsoluteFile(getAttribute(el, "configfile"), idata.getInstallPath()));
                    String boolattr = getAttribute(el, "cleanup");
                    if (boolattr != null)
                    {
                        ((SingleXmlFileMergeTask) task).setCleanup(Boolean.parseBoolean(boolattr));
                    }
                    List<FileSet> fslist = readFileSets(el);
                    for (FileSet fs : fslist)
                    {
                        ((SingleXmlFileMergeTask) task).addFileSet(fs);
                    }
                    readAndAddXPathProperties(el, (SingleXmlFileMergeTask) task);
                    break;

                case REGISTRY:
                    task = new RegistryTask();
                    ((RegistryTask) task).setFromKey(requireAttribute(el, "fromkey"));
                    ((RegistryTask) task).setKey(requireAttribute(el, "tokey"));
                    readSingleConfigurableTaskCommonAttributes(el, (SingleConfigurableTask) task);
                    break;

                default:
                    // This should never happen
                    throw new InstallerException(
                            "Type '" + configType.getAttribute() + "' currently not allowed for Configurable");
            }

            configtasks.add(new ConfigurationActionTask(task, getAttribute(el, "condition"),
                                                        getInstallData().getRules()));
        }
        return configtasks;
    }


    public void readAndAddEntries(IXMLElement parent, SingleConfigurableTask task) throws InstallerException
    {
        for (IXMLElement el : parent.getChildrenNamed("entry"))
        {
            Entry entry = createEntryFromXML(el);
            if (task instanceof SingleOptionFileTask)
            {
                entry.setKey(el.getAttribute("key"));
                entry.setValue(getAttribute(el, "value"));
            }
            else if (task instanceof SingleIniFileTask)
            {
                entry.setSection(el.getAttribute("section"));
                entry.setKey(el.getAttribute("key"));
                entry.setValue(getAttribute(el, "value"));
            }
            else if (task instanceof RegistryTask)
            {
                entry.setSection(el.getAttribute("key"));
                entry.setKey(el.getAttribute("value"));
                entry.setValue(getAttribute(el, "data"));
            }
            task.addEntry(entry);
        }
    }

    private Entry createEntryFromXML(IXMLElement parent) throws InstallerException
    {
        Entry e = new Entry();
        String attrib = parent.getAttribute("dataType");
        if (attrib != null)
        {
            Type type = Type.getFromAttribute(attrib);
            if (type == null)
            {
                // TODO Inform about misconfigured configuration actions during
                // compilation
                throw new InstallerException(MessageFormat.format(ERRMSG_CONFIGACTION_BADATTR,
                        "dataType", attrib));
            }
            e.setType(type);
        }
        attrib = parent.getAttribute("lookupType");
        if (attrib != null)
        {
            LookupType lookupType = LookupType.getFromAttribute(attrib);
            if (lookupType == null)
            {
                // TODO Inform about misconfigured configuration actions during compilation
                throw new InstallerException(MessageFormat.format(ERRMSG_CONFIGACTION_BADATTR,
                        "lookupType", attrib));
            }
            e.setLookupType(lookupType);
        }
        attrib = parent.getAttribute("operation");
        if (attrib != null)
        {
            Operation operation = Operation.getFromAttribute(attrib);
            if (operation == null)
            {
              // TODO Inform about misconfigured configuration actions during compilation
              throw new InstallerException(
                  MessageFormat.format(
                      ERRMSG_CONFIGACTION_BADATTR,
                      "operation", attrib)
                  );
            }
            e.setOperation(operation);
        }
        attrib = parent.getAttribute("unit");
        if (attrib != null)
        {
            Unit unit = Unit.getFromAttribute(attrib);
            if (unit == null)
            {
                // TODO Inform about misconfigured configuration actions during compilation
                throw new InstallerException(MessageFormat.format(ERRMSG_CONFIGACTION_BADATTR,
                        "unit", attrib));
            }
            e.setUnit(unit);
        }
        e.setDefault(parent.getAttribute("default"));
        e.setPattern(parent.getAttribute("pattern"));
        //FIXME remove?
        //filterEntryFromXML(parent, e);
        return e;
    }


    private void readAndAddXPathProperties(IXMLElement parent, SingleXmlFileMergeTask task)
            throws InstallerException
    {
        for (IXMLElement f : parent.getChildrenNamed("xpathproperty"))
        {
            task.addProperty(requireAttribute(f, "key"), requireAttribute(f, "value"));
        }
    }

    private List<FileSet> readFileSets(IXMLElement parent)
            throws InstallerException
    {
        Iterator<IXMLElement> iter = parent.getChildrenNamed("fileset").iterator();
        List<FileSet> fslist = new ArrayList<FileSet>();
        try
        {
            String installPath = getInstallData().getInstallPath();
            while (iter.hasNext())
            {
                IXMLElement f = iter.next();


                FileSet fs = new FileSet();

                String strattr = getAttribute(f, "dir");
                if (strattr != null)
                {
                    fs.setDir(FileUtil.getAbsoluteFile(strattr, installPath));
                }

                strattr = getAttribute(f, "file");
                if (strattr != null)
                {
                    fs.setFile(FileUtil.getAbsoluteFile(strattr, installPath));
                }
                else
                {
                    if (fs.getDir() == null)
                    {
                        throw new InstallerException(
                                "At least one of both attributes, 'dir' or 'file' required in fileset");
                    }
                }

                strattr = getAttribute(f, "includes");
                if (strattr != null)
                {
                    fs.setIncludes(strattr);
                }

                strattr = getAttribute(f, "excludes");
                if (strattr != null)
                {
                    fs.setExcludes(strattr);
                }

                String boolval = getAttribute(f, "casesensitive");
                if (boolval != null)
                {
                    fs.setCaseSensitive(Boolean.parseBoolean(boolval));
                }

                boolval = getAttribute(f, "defaultexcludes");
                if (boolval != null)
                {
                    fs.setDefaultexcludes(Boolean.parseBoolean(boolval));
                }

                boolval = getAttribute(f, "followsymlinks");
                if (boolval != null)
                {
                    fs.setFollowSymlinks(Boolean.parseBoolean(boolval));
                }

                readAndAddIncludes(f, fs);
                readAndAddExcludes(f, fs);

                fslist.add(fs);
            }
        }
        catch (Exception e)
        {
            throw new InstallerException(e);
        }
        return fslist;
    }

    private List<FileNameMapper> readMapper(IXMLElement parent)
            throws InstallerException
    {
        Iterator<IXMLElement> iter = parent.getChildrenNamed("mapper").iterator();
        List<FileNameMapper> mappers = new ArrayList<FileNameMapper>();
        try
        {
            while (iter.hasNext())
            {
                IXMLElement f = iter.next();

                String attrib = requireAttribute(f, "type");
                Mapper.MapperType mappertype;
                if (attrib != null)
                {
                    mappertype = Mapper.MapperType.getFromAttribute(attrib);
                    if (mappertype == null)
                    {
                        throw new InstallerException("Unknown filename mapper type '" + attrib + "'");
                    }
                }
                else
                {
                    throw new InstallerException("Missing filename mapper type");
                }
                FileNameMapper mapper = (FileNameMapper) Class.forName(mappertype.getImplementation()).newInstance();
                if (mapper instanceof GlobPatternMapper)
                {
                    String boolval = getAttribute(f, "casesensitive");
                    if (boolval != null)
                    {
                        ((GlobPatternMapper) mapper).setCaseSensitive(Boolean.parseBoolean(boolval));
                    }
                    mapper.setFrom(requireAttribute(f, "from"));
                    mapper.setTo(requireAttribute(f, "to"));
                }
                else
                {
                    throw new InstallerException("Filename mapper type \"" + "\" currently not supported");
                }

                mappers.add(mapper);
            }
        }
        catch (Exception e)
        {
            throw new InstallerException(e);
        }
        return mappers;
    }

    private void readAndAddIncludes(IXMLElement parent, FileSet fileset)
            throws InstallerException
    {
        for (IXMLElement f : parent.getChildrenNamed("include"))
        {
            fileset.createInclude().setName(requireAttribute(f, "name"));
        }
    }

    private void readAndAddExcludes(IXMLElement parent, FileSet fileset)
            throws InstallerException
    {
        for (IXMLElement f : parent.getChildrenNamed("exclude"))
        {
            fileset.createExclude().setName(requireAttribute(f, "name"));
        }
    }

    private int getConfigFileType(String varname, String type)
            throws InstallerException
    {
        int filetype = ConfigFileValue.CONFIGFILE_TYPE_OPTIONS;
        if (type != null)
        {
            if (type.equalsIgnoreCase("options"))
            {
                filetype = ConfigFileValue.CONFIGFILE_TYPE_OPTIONS;
            }
            else if (type.equalsIgnoreCase("xml"))
            {
                filetype = ConfigFileValue.CONFIGFILE_TYPE_XML;
            }
            else if (type.equalsIgnoreCase("ini"))
            {
                filetype = ConfigFileValue.CONFIGFILE_TYPE_INI;
            }
            else
            {
                parseError("Error in definition of dynamic variable " + varname + ": Unknown entry type " + type);
            }
        }
        return filetype;
    }

    protected Properties readVariables(IXMLElement parent) throws InstallerException
    {
        List<DynamicVariable> dynamicVariables = null;

        IXMLElement vars = parent.getFirstChildNamed("variables");
        if (vars != null)
        {
            logger.fine("Reading variables for configuration action");
            dynamicVariables = new LinkedList<DynamicVariable>();

            for (IXMLElement var : vars.getChildrenNamed("variable"))
            {
                String name = requireAttribute(var, "name");
                logger.fine("Reading variable '" + name +"'");

                DynamicVariable dynamicVariable = new DynamicVariableImpl();
                dynamicVariable.setName(name);

                // Check for plain value
                String value = getAttribute(var, "value");
                if (value != null)
                {
                    dynamicVariable.setValue(new PlainValue(value));
                }
                else
                {
                    IXMLElement valueElement = var.getFirstChildNamed("value");
                    if (valueElement != null)
                    {
                        value = valueElement.getContent();
                        if (value == null)
                        {
                            parseError("Empty value element for dynamic variable " + name);
                        }
                        dynamicVariable.setValue(new PlainValue(value));
                    }
                }
                // Check for environment variable value
                value = getAttribute(var, "environment");
                if (value != null)
                {
                    if (dynamicVariable.getValue() == null)
                    {
                        dynamicVariable.setValue(new EnvironmentValue(value));
                    }
                    else
                    {
                        parseError("Ambiguous environment value definition for dynamic variable " + name);
                    }
                }
                // Check for registry value
                value = getAttribute(var, "regkey");
                if (value != null)
                {
                    String regroot = getAttribute(var, "regroot");
                    String regvalue = getAttribute(var, "regvalue");
                    if (dynamicVariable.getValue() == null)
                    {
                        dynamicVariable.setValue(
                                new RegistryValue(regroot, value, regvalue));
                    }
                    else
                    {
                        parseError("Ambiguous registry value definition for dynamic variable " + name);
                    }
                }
                // Check for value from plain config file
                value = var.getAttribute("file");
                if (value != null)
                {
                    String stype = var.getAttribute("type");
                    String filesection = var.getAttribute("section");
                    String filekey = requireAttribute(var, "key");
                    String escapeVal = var.getAttribute("escape");
                    boolean escape = true;
                    if (escapeVal != null)
                    {
                        escape = Boolean.parseBoolean(escapeVal);
                    }
                    if (dynamicVariable.getValue() == null)
                    {
                        dynamicVariable.setValue(new PlainConfigFileValue(value, getConfigFileType(
                                name, stype), filesection, filekey, escape));
                    }
                    else
                    {
                        // unexpected combination of variable attributes
                        parseError("Ambiguous file value definition for dynamic variable " + name);
                    }
                }
                // Check for value from config file entry in a zip file
                value = var.getAttribute("zipfile");
                if (value != null)
                {
                    String entryname = requireAttribute(var, "entry");
                    String stype = var.getAttribute("type");
                    String filesection = var.getAttribute("section");
                    String filekey = requireAttribute(var, "key");
                    String escapeVal = var.getAttribute("escape");
                    boolean escape = true;
                    if (escapeVal != null)
                    {
                        escape = Boolean.parseBoolean(escapeVal);
                    }
                    if (dynamicVariable.getValue() == null)
                    {
                        dynamicVariable.setValue(new ZipEntryConfigFileValue(value, entryname,
                                                                             getConfigFileType(name, stype), filesection,
                                                                             filekey, escape));
                    }
                    else
                    {
                        // unexpected combination of variable attributes
                        parseError("Ambiguous file value definition for dynamic variable " + name);
                    }
                }
                // Check for value from config file entry in a jar file
                value = var.getAttribute("jarfile");
                if (value != null)
                {
                    String entryname = requireAttribute(var, "entry");
                    String stype = var.getAttribute("type");
                    String filesection = var.getAttribute("section");
                    String filekey = requireAttribute(var, "key");
                    String escapeVal = var.getAttribute("escape");
                    boolean escape = true;
                    if (escapeVal != null)
                    {
                        escape = Boolean.parseBoolean(escapeVal);
                    }
                    if (dynamicVariable.getValue() == null)
                    {
                        dynamicVariable.setValue(new JarEntryConfigValue(value, entryname,
                                                                         getConfigFileType(name, stype), filesection,
                                                                         filekey, escape));
                    }
                    else
                    {
                        // unexpected combination of variable attributes
                        parseError("Ambiguous file value definition for dynamic variable " + name);
                    }
                }
                // Check for config file value
                value = getAttribute(var, "executable");
                if (value != null)
                {
                    if (dynamicVariable.getValue() == null)
                    {
                        String dir = var.getAttribute("dir");
                        String exectype = var.getAttribute("type");
                        String boolval = var.getAttribute("stderr");
                        boolean stderr = false;
                        if (boolval != null)
                        {
                            stderr = Boolean.parseBoolean(boolval);
                        }

                        if (value.length() <= 0)
                        {
                            parseError("No command given in definition of dynamic variable " + name);
                        }
                        Vector<String> cmd = new Vector<String>();
                        cmd.add(value);
                        List<IXMLElement> args = var.getChildrenNamed("arg");
                        if (args != null)
                        {
                            for (IXMLElement arg : args)
                            {
                                String content = arg.getContent();
                                if (content != null)
                                {
                                    cmd.add(content);
                                }
                            }
                        }
                        String[] cmdarr = new String[cmd.size()];
                        if (exectype.equalsIgnoreCase("process") || exectype == null)
                        {
                            dynamicVariable.setValue(new ExecValue(cmd.toArray(cmdarr), dir, false, stderr));
                        }
                        else if (exectype.equalsIgnoreCase("shell"))
                        {
                            dynamicVariable.setValue(new ExecValue(cmd.toArray(cmdarr), dir, true, stderr));
                        }
                        else
                        {
                            parseError("Bad execution type " + exectype + " given for dynamic variable " + name);
                        }
                    }
                    else
                    {
                        parseError("Ambiguous execution output value definition for dynamic variable " + name);
                    }
                }

                if (dynamicVariable.getValue() == null)
                {
                    parseError("No value specified at all for dynamic variable " + name);
                }

                // Check whether dynamic variable has to be evaluated only once during installation
                value = getAttribute(var, "checkonce");
                if (value != null)
                {
                    dynamicVariable.setCheckonce(Boolean.valueOf(value));
                }

                // Check whether evaluation failures of the dynamic variable should be ignored
                value = getAttribute(var, "ignorefailure");
                if (value != null)
                {
                    dynamicVariable.setIgnoreFailure(Boolean.valueOf(value));
                }

                IXMLElement filters = var.getFirstChildNamed("filters");
                if (filters != null)
                {
                    List<IXMLElement> filterList = filters.getChildren();
                    for (IXMLElement filterElement : filterList)
                    {
                        if (filterElement.getName().equals("regex"))
                        {
                            String expression = filterElement.getAttribute("regexp");
                            String selectexpr = filterElement.getAttribute("select");
                            String replaceexpr = filterElement.getAttribute("replace");
                            String defaultvalue = filterElement.getAttribute("defaultvalue");
                            String scasesensitive = filterElement.getAttribute("casesensitive");
                            String sglobal = filterElement.getAttribute("global");
                            dynamicVariable.addFilter(
                                    new RegularExpressionFilter(
                                            expression, selectexpr,
                                            replaceexpr, defaultvalue,
                                            Boolean.valueOf(scasesensitive != null ? scasesensitive : "true"),
                                            Boolean.valueOf(sglobal != null ? sglobal : "false")));
                        }
                        else if (filterElement.getName().equals("location"))
                        {
                            String basedir = filterElement.getAttribute("basedir");
                            dynamicVariable.addFilter(new LocationFilter(basedir));
                        }
                    }
                }
                try
                {
                    dynamicVariable.validate();
                }
                catch (Exception e)
                {
                    parseError("Error in definition of dynamic variable " + name + ": "
                                       + e.getMessage());
                }

                String conditionid = getAttribute(var, "condition");
                dynamicVariable.setConditionid(conditionid);

                dynamicVariables.add(dynamicVariable);
            }
        }

        return evaluateDynamicVariables(dynamicVariables);
    }

    private Properties evaluateDynamicVariables(List<DynamicVariable> dynamicvariables)
            throws InstallerException
    {
        // FIXME change DynamicVariableSubstitutor constructor interface
        //DynamicVariableSubstitutor dynsubst = new DynamicVariableSubstitutor((List)dynamicvariables, installdata.getRules());
        Properties props = new Properties();

        if (dynamicvariables != null)
        {
            logger.fine("Evaluating configuration variables");
            RulesEngine rules = getInstallData().getRules();
            for (DynamicVariable dynvar : dynamicvariables)
            {
                String name = dynvar.getName();
                logger.fine("Evaluating configuration variable: " + name);
                boolean refresh = false;
                String conditionid = dynvar.getConditionid();
                logger.fine("Configuration variable condition: " + conditionid);
                if ((conditionid != null) && (conditionid.length() > 0))
                {
                    if ((rules != null) && rules.isConditionTrue(conditionid))
                    {
                        logger.fine("Refresh configuration variable \"" + name
                                            + "\" based on global condition \" " + conditionid + "\"");
                        // condition for this rule is true
                        refresh = true;
                    }
                }
                else
                {
                    logger.fine("Refresh configuration variable \"" + name
                                        + "\" based on local condition \" " + conditionid + "\"");
                    // empty condition
                    refresh = true;
                }
                if (refresh)
                {
                    try
                    {
                        if (!(dynvar.isCheckonce() && dynvar.isChecked()))
                        {
                            String newValue = dynvar.evaluate(replacer);
                            if (newValue != null)
                            {
                                logger.fine("Configuration variable " + name + ": " + newValue);
                                props.setProperty(name, newValue);
                            }
                            else
                            {
                                logger.fine("Configuration variable " + name + " unchanged: " + dynvar.getValue());
                            }
                        }
                        dynvar.setChecked();
                    }
                    catch (Exception e)
                    {
                        throw new InstallerException(e);
                    }
                }
            }
        }

        return props;
    }

    /**
     * Call getAttribute on an element, producing a meaningful error message if not present, or
     * empty. It is an error for 'element' or 'attribute' to be null.
     *
     * @param element   The element to get the attribute value of
     * @param attribute The name of the attribute to get
     */
    protected String requireAttribute(IXMLElement element, String attribute)
            throws InstallerException
    {
        String value = getAttribute(element, attribute);
        if (value == null)
        {
            parseError(element, "<" + element.getName() + "> requires attribute '" + attribute + "'");
        }
        return value;
    }

    protected String getAttribute(IXMLElement element, String attribute)
            throws InstallerException
    {
        String value = element.getAttribute(attribute);
        if (value != null)
        {
            return substituteVariables(value);
        }
        return value;
    }

    /**
     * Create parse error with consistent messages. Includes file name.
     *
     * @param message Brief message explaining error
     */
    protected void parseError(String message) throws InstallerException
    {
        throw new InstallerException(SPEC_FILE_NAME + ":" + message);
    }

    /**
     * Create parse error with consistent messages. Includes file name and line # of parent. It is
     * an error for 'parent' to be null.     *
     *
     * @param parent  The element in which the error occured
     * @param message Brief message explaining error
     */
    protected void parseError(IXMLElement parent, String message) throws InstallerException
    {
        throw new InstallerException(SPEC_FILE_NAME + ":" + parent.getLineNr() + ": " + message);
    }

    /**
     * Create a parse warning with consistent messages. Includes file name and line # of parent.     *
     *
     * @param parent  The element in which the warning occured
     * @param message Warning message
     */
    protected void parseWarn(IXMLElement parent, String message)
    {
        System.out.println("Warning: " + SPEC_FILE_NAME + ":" + parent.getLineNr() + ": " + message);
    }

    public enum ConfigType
    {
        OPTIONS("options"), INI("ini"), XML("xml"), REGISTRY("registry");

        private static Map<String, ConfigType> lookup;

        private String attribute;

        ConfigType(String attribute)
        {
            this.attribute = attribute;
        }

        static
        {
            lookup = new HashMap<String, ConfigType>();
            for (ConfigType operation : EnumSet.allOf(ConfigType.class))
            {
                lookup.put(operation.getAttribute(), operation);
            }
        }

        public String getAttribute()
        {
            return attribute;
        }

        public static ConfigType getFromAttribute(String attribute)
        {
            if (attribute != null && lookup.containsKey(attribute))
            {
                return lookup.get(attribute);
            }
            return null;
        }
    }

}

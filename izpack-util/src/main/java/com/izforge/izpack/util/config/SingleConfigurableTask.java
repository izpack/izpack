/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005,2009 Ivan SZKIBA
 * Copyright 2010,2011 Rene Krell
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

package com.izforge.izpack.util.config;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import com.izforge.izpack.util.config.SingleConfigurableTask.Entry.LookupType;
import com.izforge.izpack.util.config.base.BasicProfile;
import com.izforge.izpack.util.config.base.Config;
import com.izforge.izpack.util.config.base.Configurable;
import com.izforge.izpack.util.config.base.Ini;
import com.izforge.izpack.util.config.base.OptionMap;
import com.izforge.izpack.util.config.base.Options;
import com.izforge.izpack.util.config.base.Reg;

public abstract class SingleConfigurableTask implements ConfigurableTask
{
    private static final Logger logger = Logger.getLogger(SingleConfigurableTask.class.getName());

    private boolean patchPreserveEntries = true;

    private boolean patchPreserveValues = true;

    private boolean patchResolveVariables = false;

    protected boolean createConfigurable = true;

    /*
     * ini4j-like settings
     */
    private boolean escape = Config.getGlobal().isEscape();
    private boolean escapeNewLine = Config.getGlobal().isEscapeNewline();
    private boolean headerComment = false;
    private boolean emptyLines = true;
    private boolean autoNumbering = true;
    private String operator = Config.getGlobal().getOperator();
    private Charset encoding = Config.getGlobal().getFileEncoding();


    /*
     * Internal variables.
     */

    protected Configurable configurable;

    protected Configurable fromConfigurable;

    private Vector<Entry> entries = new Vector<Entry>();

    /**
     * Whether to preserve equal entries but not necessarily their values from an old configuration,
     * if they can be found (default: true).
     *
     * @param preserveEntries - true to preserve equal entries from an old configuration
     */
    public void setPatchPreserveEntries(boolean preserveEntries)
    {
        this.patchPreserveEntries = preserveEntries;
    }

    /**
     * Whether to preserve the values of equal entries from an old configuration, if they can be
     * found (default: true). Set false to overwrite old configuration values by default with the
     * new ones, regardless whether they have been already set in an old configuration. Values from
     * an old configuration can only be preserved, if the appropriate entries exist in an old
     * configuration.
     *
     * @param preserveValues - true to preserve the values of equal entries from an old
     * configuration
     */
    public void setPatchPreserveValues(boolean preserveValues)
    {
        patchPreserveValues = preserveValues;
    }

    /**
     * Whether variables should be resolved during patching.
     *
     * @param resolve - true to resolve in-value variables
     */
    public void setPatchResolveVariables(boolean resolve)
    {
        patchResolveVariables = resolve;
    }

    /**
     * Whether to accept escape sequences
     *
     * @param escape - true to resolve escape sequences
     */
    public void setEscape(boolean escape)
    {
        this.escape = escape;
    }

    /**
     * Whether to interpret escape at the end of line for joining lines
     *
     * @param escape - true to interpret escape at the end of line for joining lines
     */
    public void setEscapeNewLine(boolean escapeNewLine)
    {
        this.escapeNewLine = escapeNewLine;
    }

    /**
     * Whether to use header comments
     *
     * @param headerComment - true to use header comments
     */
    public void setHeaderComment(boolean headerComment)
    {
        this.headerComment = headerComment;
    }

    /**
     * Whether to preserve empty lines
     *
     * @param emptyLines - true to preserve empty lines
     */
    public void setEmptyLines(boolean emptyLines)
    {
        this.emptyLines = emptyLines;
    }

    /**
     * Whether to use property auto numbering (for property names
     * with a trailing '.')
     *
     * @param autoNumbering - true to use property auto numbering
     */
    public void setAutoNumbering(boolean autoNumbering)
    {
        this.autoNumbering = autoNumbering;
    }

    /**
     * The operator to use for separating name and value
     *
     * @param operator - an operator string
     */
    public void setOperator(String operator)
    {
        this.operator = operator;
    }

    /**
     * The encoding to be used for loading and saving configuration files
     *
     * @param encoding - an encoding string
     */
    public void setEncoding(Charset encoding)
    {
        this.encoding = encoding;
    }

    /**
     * Whether the configuration file or registry root entry should be created if it doesn't already
     * exist (default: true).
     *
     * @param create - whether to create a new configuration file or registry root entry
     */
    public void setCreate(boolean create)
    {
        createConfigurable = create;
    }

    @Override
    public void execute() throws Exception
    {
        Config.getGlobal().setHeaderComment(headerComment);
        Config.getGlobal().setEmptyLines(emptyLines);
        Config.getGlobal().setAutoNumbering(autoNumbering);
        Config.getGlobal().setEscape(escape);
        Config.getGlobal().setEscapeNewline(escapeNewLine);
        Config.getGlobal().setOperator(operator);
        Config.getGlobal().setFileEncoding(encoding);
        checkAttributes();
        readConfigurable();
        readSourceConfigurable();
        patchConfigurable();
        executeNestedEntries();
        writeConfigurable();
    }

    private String getValueFromOptionMap(OptionMap map, String key, int index)
    {
        return (patchResolveVariables ?
                map.fetch(key, index)
                : map.get(key, index));
    }

    private void keepOptions(String key, String fromValue, String lookupValue, LookupType lookupType)
    {
        int found = 0;

        for (int i = 0; i < ((Options) configurable).length(key); i++)
        {
            if (lookupValue != null)
            {
                String origValue = getValueFromOptionMap((OptionMap) configurable, key, i);

                if (origValue != null)
                {
                    switch (lookupType)
                    {
                        case REGEXP:
                            if (origValue.matches(lookupValue))
                            {
                                // found in patch target and in patch using reqexp value lookup;
                                // overwrite in each case at the original position
                                logger.fine("Preserve option file entry \"" + key + "\"");
                                ((Options) configurable).put(key, fromValue, i);
                                found++;
                            }
                            break;

                        default:
                            if (origValue.equals(lookupValue))
                            {
                                // found in patch target and in patch using plain value lookup;
                                // overwrite in each case at the original position
                                ((Options) configurable).put(key, fromValue, i);
                                found++;
                            }
                            break;
                    }
                }
            }
            else
            {
                // found in patch target and in patch;
                // not looked up by value - overwrite in each case at the original position
                ((Options) configurable).put(key, fromValue, i);
                found++;
            }
        }

        logger.fine("Patched " + found + " option file entries for key \"" + key + "\" found in original: " + fromValue);

        if (found == 0)
        {
            // nothing existing to patch found in patch target
            // but force preserving of patch entry
            logger.fine("Add option file entry for \"" + key + "\": " + fromValue);
            ((Options) configurable).add(key, fromValue);
        }
    }

    private void deleteOptions(String key, String lookupValue, LookupType lookupType)
    {
        for (int i = 0; i < ((Options) configurable).length(key); i++)
        {
            if (lookupValue == null)
            {
                String origValue = getValueFromOptionMap((OptionMap) configurable, key, i);

                if (origValue != null)
                {
                    switch (lookupType)
                    {
                        case REGEXP:
                            if (origValue.matches(lookupValue))
                            {
                                logger.fine("Remove option key \"" + key + "\"");
                                ((Options) configurable).remove(key, i);
                                i--;
                            }
                            break;

                        default:
                            if (origValue.equals(lookupValue))
                            {
                                logger.fine("Remove option key \"" + key + "\"");
                                ((Options) configurable).remove(key, i);
                                i--;
                            }
                            break;
                    }
                }
            }
            else
            {
                logger.fine("Remove option key \"" + key + "\"");
                ((Options) configurable).remove(key);
                i--;
            }
        }
    }

    private void deleteConfigurableEntry(String section, String key,
            String lookupValue, LookupType lookupType)
    throws Exception
    {
        if (configurable instanceof Options)
        {
            deleteOptions(key, lookupValue, lookupType);
        }
        else if (configurable instanceof Ini)
        {
            ((Ini) configurable).remove(section, key);
        }
        else if (configurable instanceof Reg)
        {
            ((Reg) configurable).remove(section, key);
        }
        else
        {
            throw new Exception("Unknown configurable type class: "
                    + configurable.getClass().getName());
        }
    }

    private void keepConfigurableValue(String section, String key,
            String lookupValue, LookupType lookupType)
    throws Exception
    {
        if (fromConfigurable != null)
        {
            if (configurable instanceof Options)
            {
                for (int i = 0; i < ((Options) fromConfigurable).length(key); i++)
                {
                    String fromValue = getValueFromOptionMap((OptionMap) fromConfigurable, key, i);
                    if (fromValue != null)
                    {
                        if (lookupValue != null)
                        {
                            switch (lookupType)
                            {
                                case REGEXP:
                                    if (fromValue.matches(lookupValue))
                                    {
                                        keepOptions(key, fromValue, lookupValue, lookupType);
                                    }
                                    break;

                                default:
                                    if (!fromValue.equals(lookupValue))
                                    {
                                        keepOptions(key, fromValue, lookupValue, lookupType);
                                    }
                                    break;
                            }
                        }
                        else
                        {
                            keepOptions(key, fromValue, lookupValue, lookupType);
                        }
                    }
                }
            }
            else if (configurable instanceof Ini)
            {
                Ini.Section fromSection = ((Ini) fromConfigurable).get(section);
                Ini.Section toSection = ((Ini) configurable).get(section);
                if (fromSection != null)
                {
                    if (toSection == null)
                    {
                        logger.fine("Adding new INI section [" + section + "]");
                        toSection = ((Ini) configurable).add(section);
                    }
                    if (toSection != null)
                    {
                        String fromValue = (patchResolveVariables ? fromSection
                                .fetch(key) : fromSection.get(key));
                        if (!toSection.containsKey(key))
                        {
                            logger.fine("Preserve INI file entry \"" + key
                                    + "\" in section [" + section + "]: " + fromValue);
                            toSection.add(key, fromValue);
                        }
                        else
                        {
                            logger.fine("Preserve INI file entry value for key \"" + key
                                    + "\" in section [" + section + "]: " + fromValue);
                            toSection.put(key, fromValue);
                        }
                    }
                }
            }
            else if (configurable instanceof Reg)
            {
                Reg.Key fromRegKey = ((Reg) fromConfigurable).get(section);
                Reg.Key toRegKey = ((Reg) configurable).get(section);
                if (fromRegKey != null)
                {
                    if (toRegKey == null)
                    {
                        logger.fine("Adding new registry root key " + section);
                        toRegKey = ((Reg) configurable).add(section);
                    }
                    if (toRegKey != null)
                    {
                        String fromValue = (patchResolveVariables ? fromRegKey
                                .fetch(key) : fromRegKey.get(key));
                        if (!toRegKey.containsKey(key))
                        {
                            logger.fine("Preserve registry value " + key + " under root key "
                                    + section + ": " + fromValue);
                            toRegKey.add(key, fromValue);
                        }
                        else
                        {
                            logger.fine("Preserve registry data for value " + key
                                    + " in root key " + section + ": " + fromValue);
                            toRegKey.put(key, fromValue);
                        }
                    }
                }
            }
            else
            {
                throw new Exception("Unknown configurable type class: "
                        + configurable.getClass().getName());
            }
        }
    }

    private void patchConfigurable() throws Exception
    {
        if (fromConfigurable != null)
        {
            Set<String> toKeySet;
            Set<String> fromKeySet;
            if (configurable instanceof Options)
            {
                toKeySet = ((Options) configurable).keySet();
                fromKeySet = ((Options) fromConfigurable).keySet();
                for (String key : fromKeySet)
                {
                    String fromValue = (patchResolveVariables ? ((Options) fromConfigurable)
                            .fetch(key)
                            : ((Options) fromConfigurable).get(key));
                    if (patchPreserveEntries && !toKeySet.contains(key))
                    {
                        logger.fine("Preserve option file entry \"" + key + "\"");
                        ((Options) configurable).add(key, fromValue);
                    }
                    else if (patchPreserveValues && ((Options) configurable).keySet().contains(key))
                    {
                        logger.fine("Preserve option value for key \"" + key + "\": \"" + fromValue
                                + "\"");
                        ((Options) configurable).put(key, fromValue);
                    }
                }
            }
            else if (configurable instanceof Ini)
            {
                Set<String> sectionKeySet = ((Ini) configurable).keySet();
                Set<String> fromSectionKeySet = ((Ini) fromConfigurable).keySet();
                for (String fromSectionKey : fromSectionKeySet)
                {
                    if (sectionKeySet.contains(fromSectionKey))
                    {
                        Ini.Section fromSection = ((Ini) fromConfigurable)
                                .get(fromSectionKey);
                        Ini.Section toSection = ((Ini) configurable)
                                .get(fromSectionKey);
                        fromKeySet = fromSection.keySet();
                        toKeySet = null;
                        if (toSection != null) toKeySet = toSection.keySet();
                        for (String fromKey : fromKeySet)
                        {
                            if (toSection == null)
                            {
                                logger.fine("Adding new INI section [" + fromSectionKey + "]");
                                toSection = ((Ini) configurable).add(fromSectionKey);
                            }
                            String fromValue = (patchResolveVariables ? fromSection
                                    .fetch(fromKey) : fromSection.get(fromKey));
                            if (patchPreserveEntries && !toKeySet.contains(fromKey))
                            {
                                logger.fine("Preserve INI file entry \"" + fromKey
                                        + "\" in section [" + fromSectionKey + "]: " + fromValue);
                                toSection.add(fromKey, fromValue);
                            }
                            else if (patchPreserveValues && toKeySet.contains(fromKey))
                            {
                                logger.fine("Preserve INI file entry value for key \"" + fromKey
                                        + "\" in section [" + fromSectionKey + "]: " + fromValue);
                                toSection.put(fromKey, fromValue);
                            }
                        }
                    }
                }
            }
            else if (configurable instanceof Reg)
            {
                Set<String> rootKeySet = ((Reg) configurable).keySet();
                Set<String> fromRootKeySet = ((Reg) fromConfigurable).keySet();
                for (String fromRootKey : fromRootKeySet)
                {
                    if (rootKeySet.contains(fromRootKey))
                    {
                        Reg.Key fromRegKey = ((Reg) fromConfigurable).get(fromRootKey);
                        Reg.Key toRegKey = ((Reg) configurable).get(fromRootKey);
                        fromKeySet = fromRegKey.keySet();
                        toKeySet = null;
                        if (toRegKey != null) toKeySet = toRegKey.keySet();
                        for (String fromKey : fromKeySet)
                        {
                            if (toRegKey == null)
                            {
                                logger.fine("Adding new registry root key " + fromRootKey);
                                toRegKey = ((Reg) configurable).add(fromRootKey);
                            }
                            String fromValue = (patchResolveVariables ? fromRegKey
                                    .fetch(fromKey) : fromRegKey.get(fromKey));
                            if (patchPreserveEntries && !toKeySet.contains(fromKey))
                            {
                                logger.fine("Preserve registry value " + fromKey + " under root key "
                                        + fromRootKey + ": " + fromValue);
                                toRegKey.add(fromKey, fromValue);
                            }
                            else if (patchPreserveValues && toKeySet.contains(fromKey))
                            {
                                logger.fine("Preserve registry data for value " + fromKey
                                        + " in root key " + fromRootKey + ": " + fromValue);
                                toRegKey.put(fromKey, fromValue);
                            }
                        }
                    }
                }
            }
            else
            {
                throw new Exception("Unknown configurable type class: "
                        + configurable.getClass().getName());
            }
        }
    }

    private void executeNestedEntries() throws Exception
    {
        for (Entry entry : entries)
        {
            switch (entry.getOperation())
            {
            case REMOVE:
                deleteConfigurableEntry(entry.getSection(), entry.getKey(), entry.getValue(), entry.getLookupType());
                break;
            case KEEP:
                keepConfigurableValue(entry.getSection(), entry.getKey(), entry.getValue(), entry.getLookupType());
                break;
            default:
                entry.executeOn(configurable);
            }
        }
    }

    protected abstract void checkAttributes() throws Exception;

    protected abstract void readSourceConfigurable() throws Exception;

    protected abstract void readConfigurable() throws Exception;

    protected abstract void writeConfigurable() throws Exception;

    public void addEntry(Entry entry)
    {
        entries.addElement(entry);
    }

    /**
     * Instance of this class represents nested elements of a task configuration file.
     */
    public static class Entry
    {

        private static final int DEFAULT_INT_VALUE = 0;

        private static final String DEFAULT_DATE_VALUE = "now";

        private static final String DEFAULT_STRING_VALUE = "";

        protected String section = null;

        protected String key = null;

        protected String value = null;

        private boolean resolveVariables = false;

        private LookupType lookupType = LookupType.PLAIN;

        private Type type = Type.STRING;

        private Operation operation = Operation.SET;

        private String defaultValue = null;

        private String pattern = null;

        private Unit unit = Unit.DAY;


        public String getSection()
        {
            return section;
        }

        /**
         * Name of s INI File section
         */
        public void setSection(String section)
        {
            this.section = section;
        }

        public String getKey()
        {
            return key;
        }

        /**
         * Name of the key/value pair
         */
        public void setKey(String value)
        {
            this.key = value;
        }

        /**
         * Value to set (=), to add (+) or subtract (-)
         */
        public void setValue(String value)
        {
            this.value = value;
        }

        /**
         * Whether variables should be resolved during manipulating with explicit modifiers nested
         * in the Ant task.
         *
         * @param resolve - true to resolve variables in explicit modifiers
         */
        public void setResolveVariables(boolean resolve)
        {
            this.resolveVariables = resolve;
        }

        public String getValue()
        {
            return value;
        }

        public LookupType getLookupType()
        {
            return lookupType;
        }


        public Type getType()
        {
            return type;
        }

        public Operation getOperation()
        {
            return operation;
        }

        /**
         * operation to apply. &quot;+&quot; or &quot;=&quot; (default) for all datatypes;
         * &quot;-&quot; for date and int only)\.
         */
        public void setOperation(Operation operation)
        {
            this.operation = operation;
        }

        /**
         * Regard the value as : int, date or string (default)
         */
        public void setType(Type type)
        {
            this.type = type;
        }

        /**
         * Regard the value as : regexp | plain (default)
         */
        public void setLookupType(LookupType lookupType)
        {
            this.lookupType = lookupType;
        }

        /**
         * Initial value to set for a key if it is not already defined in the configuration file.
         * For type date, an additional keyword is allowed: &quot;now&quot;
         */

        public void setDefault(String value)
        {
            this.defaultValue = value;
        }

        /**
         * For int and date type only. If present, Values will be parsed and formatted accordingly.
         */
        public void setPattern(String value)
        {
            this.pattern = value;
        }

        /**
         * The unit of the value to be applied to date +/- operations. Valid Values are:
         * <ul>
         * <li>millisecond</li>
         * <li>second</li>
         * <li>minute</li>
         * <li>hour</li>
         * <li>day (default)</li>
         * <li>week</li>
         * <li>month</li>
         * <li>year</li>
         * </ul>
         * This only applies to date types using a +/- operation.
         */
        public void setUnit(Unit unit)
        {
            this.unit = unit;
        }

        private void executeOnOptions(Options configurable) throws Exception
        {
            List<String> values = configurable.getAll(key);
            String newValue = null;
            boolean contains = false;
            if (values != null)
            {
                for (int i = 0; i < values.toArray().length; i++)
                {
                    String origValue = getValueFromOptions(configurable, i);
                    newValue = execute(origValue);

                    if (origValue != null && value != null)
                    {
                        switch (lookupType)
                        {
                            case REGEXP:
                                if (origValue.matches(value))
                                {
                                    logger.fine("Set option value for key \"" + key + "\": \""
                                            + newValue + "\"");
                                    configurable.put(key, newValue, i);
                                    contains = true;
                                }
                                break;

                            default:
                                if (origValue.equals(value))
                                {
                                    logger.fine("Set option value for key \"" + key + "\": \""
                                            + newValue + "\"");
                                    configurable.put(key, newValue, i);
                                    contains = true;
                                }
                                break;
                        }
                    }
                }
            }
            if (!contains)
            {
                logger.fine("Set option value for key \"" + key + "\": \"" + newValue + "\"");
                configurable.put(key, newValue);
            }

        }

        private void executeOnProfile(BasicProfile profile) throws Exception
        {
            String oldValue = getValueFromProfile(profile);
            profile.put(section, key, execute(oldValue));
        }

        private String getValueFromOptions(OptionMap map, int index)
        {
            return resolveVariables ? map.fetch(key, index) : map.get(key, index);
        }

        private String getValueFromProfile(BasicProfile profile)
        {
            return resolveVariables ? profile.fetch(section, key) : profile.get(section, key);
        }

        private String execute(String oldValue) throws Exception
        {
            String newValue = null;

            switch (type)
            {
            case INTEGER:
                newValue = executeInteger(oldValue);
                break;
            case DATE:
                newValue = executeDate(oldValue);
                break;
            case STRING:
                newValue = executeString(oldValue);
                break;
            default:
                throw new Exception("Unknown operation type: " + type);
            }

            if (newValue == null)
            {
                newValue = "";
            }

            return newValue;
        }

        protected void executeOn(Configurable configurable) throws Exception
        {
            checkParameters();

            if (configurable instanceof Options)
            {
                executeOnOptions((Options) configurable);
            }
            else if (configurable instanceof Ini)
            {
                executeOnProfile((BasicProfile) configurable);
            }
            else if (configurable instanceof Reg)
            {
                executeOnProfile((BasicProfile) configurable);
            }
            else
            {
                throw new Exception("Unknown configurable type class: "
                        + configurable.getClass().getName());
            }
        }

        /**
         * Handle operations for type <code>date</code>.
         *
         * @param oldValue the current value read from the configuration file or <code>null</code>
         * if the <code>key</code> was not contained in the configuration file.
         */
        private String executeDate(String oldValue) throws Exception
        {
            Calendar currentValue = Calendar.getInstance();

            if (pattern == null)
            {
                pattern = "yyyy/MM/dd HH:mm";
            }
            DateFormat fmt = new SimpleDateFormat(pattern);

            String currentStringValue = getCurrentValue(oldValue);
            if (currentStringValue == null)
            {
                currentStringValue = DEFAULT_DATE_VALUE;
            }

            if ("now".equals(currentStringValue))
            {
                currentValue.setTime(new Date());
            }
            else
            {
                try
                {
                    currentValue.setTime(fmt.parse(currentStringValue));
                }
                catch (ParseException pe)
                {
                    // swallow
                }
            }

            if (operation != Operation.SET)
            {
                int offset = 0;
                try
                {
                    offset = Integer.parseInt(value);
                    if (operation == Operation.DECREMENT)
                    {
                        offset = -1 * offset;
                    }
                }
                catch (NumberFormatException e)
                {
                    throw new Exception("Value not an integer on " + key);
                }
                currentValue.add(unit.getCalendarField(), offset);
            }

            return fmt.format(currentValue.getTime());
        }

        /**
         * Handle operations for type <code>int</code>.
         *
         * @param oldValue the current value read from the configuration file or <code>null</code>
         * if the <code>key</code> was not contained in the configuration file.
         */
        private String executeInteger(String oldValue) throws Exception
        {
            int currentValue = DEFAULT_INT_VALUE;
            int newValue = DEFAULT_INT_VALUE;

            DecimalFormat fmt = (pattern != null) ? new DecimalFormat(pattern)
                    : new DecimalFormat();
            try
            {
                String curval = getCurrentValue(oldValue);
                if (curval != null)
                {
                    currentValue = fmt.parse(curval).intValue();
                }
                else
                {
                    currentValue = 0;
                }
            }
            catch (NumberFormatException nfe)
            {
                // swallow
            }
            catch (ParseException pe)
            {
                // swallow
            }

            if (operation == Operation.SET)
            {
                newValue = currentValue;
            }
            else
            {
                int operationValue = 1;
                if (value != null)
                {
                    try
                    {
                        operationValue = fmt.parse(value).intValue();
                    }
                    catch (NumberFormatException nfe)
                    {
                        // swallow
                    }
                    catch (ParseException pe)
                    {
                        // swallow
                    }
                }

                if (operation == Operation.INCREMENT)
                {
                    newValue = currentValue + operationValue;
                }
                else if (operation == Operation.DECREMENT)
                {
                    newValue = currentValue - operationValue;
                }
            }

            return fmt.format(newValue);
        }

        /**
         * Handle operations for type <code>string</code>.
         *
         * @param oldValue the current value read from the configuration file or <code>null</code>
         * if the <code>key</code> was not contained in the configuration file.
         */
        private String executeString(String oldValue) throws Exception
        {
            String newValue = DEFAULT_STRING_VALUE;

            String currentValue = getCurrentValue(oldValue);

            if (currentValue == null)
            {
                currentValue = DEFAULT_STRING_VALUE;
            }

            if (operation == Operation.SET)
            {
                newValue = currentValue;
            }
            else if (operation == Operation.INCREMENT)
            {
                newValue = currentValue + value;
            }

            return newValue;
        }

        /**
         * Check if parameter combinations can be supported
         *
         * @todo make sure the 'unit' attribute is only specified on date fields
         */
        private void checkParameters() throws Exception
        {
            if (type == Type.STRING && operation == Operation.DECREMENT) { throw new Exception(
                    "- is not supported for string " + "properties (key: " + key + ")"); }
            if (value == null && defaultValue == null) { throw new Exception(
                    "\"value\" and/or \"default\" " + "attribute must be specified (key: " + key
                            + ")"); }
            if (key == null) { throw new Exception("key is mandatory"); }
            if (type == Type.STRING && pattern != null) { throw new Exception(
                    "pattern is not supported for string " + "properties (key: " + key + ")"); }
        }

        private String getCurrentValue(String oldValue)
        {
            String ret = null;
            if (operation == Operation.SET)
            {
                // If only value is specified, the value is set to it
                // regardless of its previous value.
                if (value != null && defaultValue == null)
                {
                    ret = value;
                }

                // If only default is specified and the value previously
                // existed in the configuration file, it is unchanged.
                if (value == null && defaultValue != null && oldValue != null)
                {
                    ret = oldValue;
                }

                // If only default is specified and the value did not
                // exist in the configuration file, the value is set to default.
                if (value == null && defaultValue != null && oldValue == null)
                {
                    ret = defaultValue;
                }

                // If value and default are both specified and the value
                // previously existed in the configuration file, the value
                // is set to value.
                if (value != null && defaultValue != null && oldValue != null)
                {
                    ret = value;
                }

                // If value and default are both specified and the value
                // did not exist in the configuration file, the value is set
                // to default.
                if (value != null && defaultValue != null && oldValue == null)
                {
                    ret = defaultValue;
                }
            }
            else
            {
                ret = (oldValue == null) ? defaultValue : oldValue;
            }

            return ret;
        }

        public enum Operation
        {
            INCREMENT("+"), DECREMENT("-"), SET("="), REMOVE("remove"), KEEP("keep");

            private static Map<String, Operation> lookup;

            private String attribute;

            Operation(String attribute)
            {
                this.attribute = attribute;
            }

            static
            {
                lookup = new HashMap<String, Operation>();
                for (Operation operation : EnumSet.allOf(Operation.class))
                {
                    lookup.put(operation.getAttribute(), operation);
                }
            }

            public String getAttribute()
            {
                return attribute;
            }

            public static Operation getFromAttribute(String attribute)
            {
                if (attribute != null && lookup.containsKey(attribute))
                {
                    return lookup.get(attribute);
                }
                return null;
            }
        }

        public enum Type
        {
            INTEGER("int"), DATE("date"), STRING("string");

            private static Map<String, Type> lookup;

            private String attribute;

            Type(String attribute)
            {
                this.attribute = attribute;
            }

            static
            {
                lookup = new HashMap<String, Type>();
                for (Type type : EnumSet.allOf(Type.class))
                {
                    lookup.put(type.getAttribute(), type);
                }
            }

            public String getAttribute()
            {
                return attribute;
            }

            public static Type getFromAttribute(String attribute)
            {
                if (attribute != null && lookup.containsKey(attribute))
                {
                    return lookup.get(attribute);
                }
                return null;
            }
        }

        public enum LookupType
        {
            PLAIN("plain"), REGEXP("regexp");

            private static Map<String, LookupType> lookup;

            private String attribute;

            LookupType(String attribute)
            {
                this.attribute = attribute;
            }

            static
            {
                lookup = new HashMap<String, LookupType>();
                for (LookupType type : EnumSet.allOf(LookupType.class))
                {
                    lookup.put(type.getAttribute(), type);
                }
            }

            public String getAttribute()
            {
                return attribute;
            }

            public static LookupType getFromAttribute(String attribute)
            {
                if (attribute != null && lookup.containsKey(attribute))
                {
                    return lookup.get(attribute);
                }
                return null;
            }
        }
    }

    public enum Unit
    {
        MILLISECOND("millisecond"), SECOND("second"), MINUTE("minute"), HOUR("hour"),
        DAY("day"), WEEK("week"), MONTH("month"), YEAR("year");

        private static Map<String, Unit> lookup;
        private static Hashtable<Unit, Integer> calendarFields;

        private String attribute;

        Unit(String attribute)
        {
            this.attribute = attribute;
        }

        static
        {
            lookup = new HashMap<String, Unit>();
            for (Unit unit : EnumSet.allOf(Unit.class))
            {
                lookup.put(unit.getAttribute(), unit);
            }
            calendarFields = new Hashtable<Unit, Integer>();
            calendarFields.put(MILLISECOND, new Integer(Calendar.MILLISECOND));
            calendarFields.put(SECOND, new Integer(Calendar.SECOND));
            calendarFields.put(MINUTE, new Integer(Calendar.MINUTE));
            calendarFields.put(HOUR, new Integer(Calendar.HOUR_OF_DAY));
            calendarFields.put(DAY, new Integer(Calendar.DATE));
            calendarFields.put(WEEK, new Integer(Calendar.WEEK_OF_YEAR));
            calendarFields.put(MONTH, new Integer(Calendar.MONTH));
            calendarFields.put(YEAR, new Integer(Calendar.YEAR));
        }

        public String getAttribute()
        {
            return attribute;
        }

        public static Unit getFromAttribute(String attribute)
        {
            if (attribute != null && lookup.containsKey(attribute))
            {
                return lookup.get(attribute);
            }
            return null;
        }

        public int getCalendarField()
        {
            return calendarFields.get(this);
        }

    }
}

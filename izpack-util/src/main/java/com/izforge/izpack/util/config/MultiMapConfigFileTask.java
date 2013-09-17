/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005,2009 Ivan SZKIBA
 * Copyright 2010,2011 Rene Krell
 * Copyright 2012 Daniel Abson
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

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Logger;

import com.izforge.izpack.util.config.base.Config;
import com.izforge.izpack.util.config.base.MultiMap;

public abstract class MultiMapConfigFileTask<K, V> extends ConfigFileTask
{
    private static final Logger logger = Logger.getLogger(MultiMapConfigFileTask.class.getName());

    protected boolean patchPreserveEntries = true;

    protected boolean patchPreserveValues = true;

    protected boolean patchResolveVariables = false;

    /*
     * ini4j settings
     */
    protected boolean escape = Config.getGlobal().isEscape();
    protected boolean escapeNewLine = Config.getGlobal().isEscapeNewline();
    protected boolean useHeaderComment = false;
    protected boolean emptyLines = true;
    protected boolean autoNumbering = true;
    protected String operator = Config.getGlobal().getOperator();

    protected Vector<MultiMapConfigEntry> entries = new Vector<MultiMapConfigEntry>();

    
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
     * Whether ini4j-style variables should be resolved during patching.
     *
     * @param resolve true to resolve in-value variables
     */
    public void setPatchResolveVariables(boolean resolve)
    {
        patchResolveVariables = resolve;
    }

    /**
     * Whether to accept escape sequences
     *
     * @param escape true to resolve escape sequences
     */
    public void setEscape(boolean escape)
    {
        this.escape = escape;
    }

    /**
     * Whether to interpret escape at the end of line for joining lines
     *
     * @param escapeNewLine true to interpret escape at the end of line for joining lines
     */
    public void setEscapeNewLine(boolean escapeNewLine)
    {
        this.escapeNewLine = escapeNewLine;
    }

    /**
     * Whether to use header comments.
     *
     * @param useHeaderComment true to use header comments
     */
    public void setHeaderComment(boolean useHeaderComment)
    {
        this.useHeaderComment = useHeaderComment;
    }

    /**
     * Whether to preserve empty lines.
     *
     * @param emptyLines true to preserve empty lines
     */
    public void setEmptyLines(boolean emptyLines)
    {
        this.emptyLines = emptyLines;
    }

    /**
     * Whether to use property auto numbering (for property names
     * with a trailing '.')
     *
     * @param autoNumbering true to use property auto numbering
     */
    public void setAutoNumbering(boolean autoNumbering)
    {
        this.autoNumbering = autoNumbering;
    }

    /**
     * The operator to use for separating name and value.
     *
     * @param operator an operator string
     */
    public void setOperator(String operator)
    {
        this.operator = operator;
    }

    @Override
    public void doFileOperation(File srcFile, File patchFile, File destFile) throws Exception
    {
    	//TODO: use a unique Config instance to make thread-safe
        Config.getGlobal().setHeaderComment(useHeaderComment);
        Config.getGlobal().setEmptyLines(emptyLines);
        Config.getGlobal().setAutoNumbering(autoNumbering);
        Config.getGlobal().setEscape(escape);
        Config.getGlobal().setEscapeNewline(escapeNewLine);
        Config.getGlobal().setOperator(operator);
        
        MultiMap<K, V> srcConfig = readFromFile(srcFile);
        MultiMap<K, V> patchConfig = readFromFile(patchFile);
        patch(srcConfig, patchConfig);
        for (MultiMapConfigEntry entry : entries)
        {
        	processEntry(entry, srcConfig, patchConfig);
        }
        writeToFile(patchConfig, destFile);
    }

    /**
     * Delete an entry from the specified MultiMap configuration.
     * 
     * @param entry the entry to delete
     * @param config the configuration to change
     * @throws Exception in the event of any error
     */
    protected abstract void deleteEntry(MultiMapConfigEntry entry, MultiMap<K, V> config) throws Exception;

    /**
     * Preserve an entry from one MultiMap configuration to another.
     * 
     * @param entry the entry to patch
     * @param srcConfig the source config to patch from
     * @param targetConfig the target config to patch to
     * @throws Exception in the event of any error
     */
    protected abstract void keepEntry(MultiMapConfigEntry entry, MultiMap<K, V> srcConfig, MultiMap<K, V> targetConfig) throws Exception;
    
    /**
     * Insert an entry into a MultiMap configuration.
     * 
     * @param entry the entry to insert
     * @param config the configuration to update
     */
    protected abstract void insertEntry(MultiMapConfigEntry entry, MultiMap<K, V> config) throws Exception;
    
    /**
     * Patches one config from another, based on configured task attributes.
     * 
     * @param srcConfig the original config from which to preserve data
     * @param patchConfig the new config to which the old data should be 
     * merged
     */
    protected abstract void patch(MultiMap<K, V> srcConfig, MultiMap<K, V> patchConfig);

    /**
     * 
     * 
     * @param entry
     * @param srcConfig
     * @param targetConfig
     * @throws NullPointerException if srcConfig is null and entry processing
     * would require it (i.e. {@link MultiMapConfigEntry.Operation#KEEP})
     * @throws Exception
     */
    protected void processEntry(MultiMapConfigEntry entry, MultiMap<K, V> srcConfig, MultiMap<K, V> targetConfig) throws Exception
    {
        switch (entry.getOperation())
        {
        case REMOVE:
            deleteEntry(entry, targetConfig);
            break;
        case KEEP:
        	if (srcConfig == null)
        	{
        		throw new NullPointerException("Invalid null reference for srcConfig with entry operation '" + entry.getOperation() + "'");
        	}
            keepEntry(entry, srcConfig, targetConfig);
            break;
        default:
            insertEntry(entry, targetConfig);
        }
    }

    /**
     * Construct a Configurable representation by parsing a file.
     * 
     * @param configFile the config file to parse
     * @return the Configurable representation of the config file
     * @throws IOException in the event of an error parsing the file
     */
    protected abstract MultiMap<K, V> readFromFile(File configFile) throws IOException;

    /**
     * Write a Configurable representation to a formatted config file.
     * 
     * @param config the Configurable representation to write
     * @param destFile the file to which to write the formatted config
     * @throws IOException in the event of an error writing the config file
     */
    protected abstract void writeToFile(MultiMap<K, V> config, File destFile) throws IOException;

    public void addEntry(MultiMapConfigEntry entry)
    {
        entries.addElement(entry);
    }
}

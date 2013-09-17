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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.util.config.base.MultiMap;
import com.izforge.izpack.util.config.base.Reg;
import com.izforge.izpack.util.config.base.Profile.Section;
import com.izforge.izpack.util.config.base.Registry;

public class RegistryConfigTask extends IniFileConfigTask
{
    private static final Logger logger = Logger.getLogger(RegistryConfigTask.class.getName());

    /*
     * Instance variables.
     */

    protected String toKey;
    protected String srcKey;
    

    /**
     * Location of the registry key to be updated or created; required for 
     * making changes to Windows registry. If this is not specified, the task
     * will fallback to the default file patching behaviour, and assume that 
     * you are only working with exported Windows Registry Editor files.
     */
    public void setToKey(String toKey)
    {
        this.toKey = toKey;
    }

    /**
     * Location of the registry key to be patched from; optional.
     */
    public void setSrcKey(String srcKey)
    {
        this.srcKey = srcKey;
    }
    
    /**
     * Reads a key from the Windows registry, and builds a {@link MultiMap} 
     * representation of it. If the specified key coulnd't be found, a new, 
     * empty key can optionally be created.
     * 
     * @param key the key to read
     * @param forceCreate whether the named key should be create if it doesn't exist
     * @return a representation of the named key
     * @throws IOException in the event of any error exporting or loading the 
     * registry data
     */
    protected Reg readFromRegistry(String key, boolean forceCreate) throws IOException
    {
    	Reg reg;
        if (key != null)
        {
            logger.fine("Loading from registry: " + key);
            reg = new Reg(key, forceCreate);
        }    	
        else
        {
        	reg = new Reg();
        }
        return reg;
    }
    
    /**
     * Writes the contents of {@code reg} to the Windows registry, overwriting
     * existing values and keys.
     * 
     * @param reg the data to write to the registry
     * @throws IOException in the event of any error writing or importing the
     * registry data
     */
    protected void writeToRegistry(Reg reg) throws IOException
    {
        reg.write();    	
    }
    
    /**
     * {@inheritDoc}
     * 
     * Calls {@link ConfigFileTask#execute()} to operate only on exported
     * Registry Editor files, unless {@link #setToKey(String) toKey} is 
     * specified.
     * 
     * @see #setToKey
     */
    @Override
    public void execute() throws Exception
    {
   		validateRegAttributes();
    	
		if (toKey == null)
		{
			super.execute();
		}
		else
		{
			try
			{
		        Reg key = readFromRegistry(srcKey, false);
		        Reg destKey = readFromRegistry(toKey, create);
		        patch(key, destKey);
				for (MultiMapConfigEntry entry : entries)
				{
					processEntry(entry, key, destKey);
				}
		        writeToRegistry(destKey);
			}
	    	catch (Exception e)
	    	{
	    		String errMsg = "Failed to update Windows registry";
	    		if (failonerror)
	    		{
	    			throw new Exception(errMsg, e);
	    		}
	    		else
	    		{
	    			logger.log(Level.SEVERE, errMsg, e);
	    		}
	    	}
    	}
    }
    
    @Override
    protected void insertEntry(MultiMapConfigEntry entry, MultiMap<String, Section> config) throws Exception
    {
    	super.insertEntry(setEntryKey(entry), config);
    }
    
    @Override
    protected void keepEntry(MultiMapConfigEntry entry, MultiMap<String, Section> srcConfig, MultiMap<String, Section> targetConfig) throws Exception
    {
    	super.keepEntry(setEntryKey(entry), srcConfig, targetConfig);    	
    }

    @Override
    protected void deleteEntry(MultiMapConfigEntry entry, MultiMap<String, Section> config) throws Exception
    {
    	super.deleteEntry(setEntryKey(entry), config);    	
    }

    protected MultiMapConfigEntry setEntryKey(MultiMapConfigEntry entry)
    {
    	MultiMapConfigEntry newEntry = entry.clone();
    	if (entry.getSection() == null || entry.getSection().equals(""))
    	{
    		newEntry.setSection(toKey);
    	}
    	else
    	{
    		newEntry.setSection(toKey + Registry.KEY_SEPARATOR + entry.getSection());
    	}
    	return newEntry;
    }
    
    //TODO: different registry datatypes

    /**
     * Check that all settings and attributes are valid in combination.
     * 
     * @throws Exception if a validation error occurs
     */
	protected void validateRegAttributes() throws Exception
	{
    	if (file != null || destDir != null)
    	{
    		if (toKey != null)
    		{
    			throw new Exception("Only one of tokey and tofile/todir may be set.");    			
    		}
    		else
    		{
    			super.validateAttributes();
    		}
    	}
    	else
    	{
    		if (toKey == null)
    		{
    			throw new Exception("One of tokey and tofile/todir must be set.");
    		}
    		else
    		{
    			if (filesets.size() > 0)
    			{
    				throw new Exception("Fileset cannot be used as a source when tokey is set");
    			}
    			for (MultiMapConfigEntry entry : entries)
    			{
    				entry.validateAttributes();
    			}
    		}
    	}
	}
    
    @Override
    protected MultiMap<String, Section> readFromFile(File configFile) throws IOException
    {
    	MultiMap<String, Section> config;
        if (configFile != null && configFile.exists())
        {
            logger.fine("Loading registry editor file: " + configFile.getAbsolutePath());
            config = new Reg(configFile);
        }
        else
        {
        	config = new Reg();
        }
        return config;
    }

	/**
	 * {@inheritDoc}
	 * 
	 * @throws ClassCastException if {@code config} cannot be cast to
	 * {@link Reg}
	 */
	@Override
	protected void writeToFile(MultiMap<String, Section> config, File destFile) throws IOException
	{
        Reg reg = (Reg) config;
        reg.setFile(destFile);
        reg.store();		
	}
}

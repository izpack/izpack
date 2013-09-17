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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.izforge.izpack.util.config.base.MultiMap;
import com.izforge.izpack.util.config.base.OptionMap;
import com.izforge.izpack.util.config.base.Options;

public class PropertyFileConfigTask extends MultiMapConfigFileTask<String, String>
{
    private static final Logger logger = Logger.getLogger(PropertyFileConfigTask.class.getName());

    @Override
    protected MultiMap<String, String> readFromFile(File configFile) throws IOException
    {
    	MultiMap<String, String> config;
        if (configFile != null && configFile.exists())
        {
            logger.fine("Loading options file: " + configFile.getAbsolutePath());
            config = new Options(configFile);
        }
        else
        {
        	config = new Options();
        }
        return config;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws ClassCastException if {@code config} cannot be cast to 
     * {@link Options} 
     */
    @Override
    protected void writeToFile(MultiMap<String, String> config, File destFile) throws IOException
    {
        Options opts = (Options) config;
        opts.setFile(destFile);
        opts.setComment(headerComment);
        opts.store();
    }

    /**
     * {@inheritDoc}
     * 
     * @throws ClassCastException if {@link #patchResolveVariables} is 
     * {@code true}, and {@code srcConfig} cannot be cast to {@link OptionMap}
     */
	@Override
	protected void patch(MultiMap<String, String> srcConfig, MultiMap<String, String> patchConfig)
	{
        for (String key : srcConfig.keySet())
        {
            String fromValue = (patchResolveVariables ? ((OptionMap)srcConfig).fetch(key) : srcConfig.get(key));
            boolean containsKey = patchConfig.keySet().contains(key);
            if (patchPreserveValues && containsKey)
            {
                logger.fine("Preserve value for key \"" + key + "\": \"" + fromValue + "\"");
                patchConfig.put(key, fromValue);
            }
            if (patchPreserveEntries && !containsKey)
            {
                logger.fine("Preserve key \"" + key + "\"");
                patchConfig.add(key, fromValue);
            }
        }
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws ClassCastException if {@code config} is not a type of {@link OptionMap}
	 */
	@Override
	protected void deleteEntry(MultiMapConfigEntry entry, MultiMap<String, String> config) throws Exception {
		if (entry.getValue() == null)
		{
            logger.fine("Remove option key \"" + entry.getKey() + "\"");
            config.remove(entry.getKey());			
		}
		else
		{
            String[] values = getValuesFromOptionMap((OptionMap) config, entry.getKey());	
            Map<Integer, String> matchedEntries = findMatchingValues(values, entry);
            for (int i : matchedEntries.keySet())
            {
            	config.remove(entry.getKey(), i);
            }
		}
	}

	@Override
	protected void keepEntry(MultiMapConfigEntry entry, MultiMap<String, String> src, MultiMap<String, String> target) throws Exception
	{
		if (entry.getValue() == null)
		{
			//No lookup required: replace target key with source key
			target.putAll(entry.getKey(), src.getAll(entry.getKey()));
		}
		else
		{
			//Do lookup: replace all matching target values with matching source values
			String[] srcValues = getValuesFromOptionMap((OptionMap)src, entry.getKey());
			String[] targetValues = getValuesFromOptionMap((OptionMap)target, entry.getKey());
			Map<Integer, String> srcMatched = findMatchingValues(srcValues, entry);
			Map<Integer, String> targetMatched = findMatchingValues(targetValues, entry);
		
			if (srcMatched.size() > 0)
			{
				int idx;
				ArrayList<Integer> srcMatchedIdx = new ArrayList<Integer>(srcMatched.keySet());
				ArrayList<Integer> targetMatchedIdx = new ArrayList<Integer>(targetMatched.keySet());
				Collections.sort(srcMatchedIdx);
				Collections.sort(targetMatchedIdx);
				
				//Put source values in original target positions where possible
				for (idx = 0; idx < srcMatchedIdx.size() && idx < targetMatchedIdx.size(); idx++)
				{
					target.put(entry.getKey(), srcMatched.get(srcMatchedIdx.get(idx)), targetMatchedIdx.get(idx));
				}
				//There are more matched source values than target values: add remaining source values 
				while (idx < srcMatchedIdx.size())
				{
					target.add(entry.getKey(), srcMatched.get(srcMatchedIdx.get(idx)));
					idx++;
				}
				//There are more matched target values than source values: remove additional target values
				while (idx < targetMatchedIdx.size())
				{
					target.remove(entry.getKey(), targetMatchedIdx.get(idx));
					idx++;
				}
				
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws ClassCastException if config is not of type {@link OptionMap}
	 */
	@Override
	protected void insertEntry(MultiMapConfigEntry entry, MultiMap<String, String> config) throws Exception 
	{
        String[] values = getValuesFromOptionMap((OptionMap)config, entry.getKey());        
        Map<Integer, String> matchedEntries = findMatchingValues(values, entry);
        
        if (matchedEntries.isEmpty())
        {
            //Key doesn't exist - add new key
            logger.fine("Add entry for \"" + entry.getKey() + "\": " + entry.getValue());
            config.put(entry.getKey(), entry.getCurrentValue(null));
        }
        else
    	{
            //One or more values exist for key - replace matching entries with new value
        	for (Map.Entry<Integer, String> matchedEntry : matchedEntries.entrySet())
	        {
	        	String newValue = entry.calculateValue(matchedEntry.getValue());
	        	logger.fine("Set value for entry \"" + entry.getKey() + "\": " + matchedEntry.getValue() + " -> " + newValue);
	            config.put(entry.getKey(), newValue, matchedEntry.getKey());
	        }
        }
    }
	
	/**
	 * Helper method for lookup-by-value operations, returns the index and value of matches
	 * from specified {@code values}.
	 * 
	 * @param values list of values to check for matches
	 * @param entry lookup key/value details
	 * @return all matching values, keyed on value's original index in {@code values}
	 * @see #getValuesFromOptionMap 
	 */
	protected Map<Integer, String> findMatchingValues(String[] values, MultiMapConfigEntry entry)
	{
		Map<Integer, String> matches = new HashMap<Integer, String>();
		
		for (int i=0; i < values.length; i++)
		{
			if (values[i] != null)
			{
                switch (entry.getLookupType())
                {
                    case REGEXP:
                        if (values[i].matches(entry.getValue()))
                        {
                            matches.put(i, values[i]);
                        }
                        break;

                    default:
                        if (values[i].equals(entry.getValue()))
                        {
                            matches.put(i, values[i]);                            
                        }
                        break;
                }
			}
		}		
		return matches;
	}

    /**
     * Resolves the value of the {@code key} in {@code map} with the specified
     * index. The value of {@code key} may contain ini4j variables if 
     * {@link #patchResolveVariables} is {@code true}.
     * 
     * @param map the map to search
     * @param key the key to resolve
     * @param index the index of the given key
     * @return the resolved value
     */
    protected String getValueFromOptionMap(OptionMap map, String key, int index)
    {
        return patchResolveVariables ?
                map.fetch(key, index)
                : map.get(key, index);
    }
    
    /**
     * Returns all values for the {@code key} in {@code map}. The value of
     * {@code key} may contain ini4j variables if 
     * {@link #patchResolveVariables} is {@code true}.
     * 
     * @param map the map to search
     * @param key the key to resolve
     * @return all values for the specified key
     */
    protected String[] getValuesFromOptionMap(OptionMap map, String key)
    {
        return patchResolveVariables ?
                map.fetchAll(key, String[].class)
                : map.getAll(key, String[].class);
    }
}

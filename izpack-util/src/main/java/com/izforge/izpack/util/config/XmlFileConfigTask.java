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
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import com.izforge.izpack.util.xmlmerge.XmlMerge;
import com.izforge.izpack.util.xmlmerge.config.ConfigurableXmlMerge;
import com.izforge.izpack.util.xmlmerge.config.PropertyXPathConfigurer;

public class XmlFileConfigTask extends MergeableConfigFileTask
{
    private static final Logger logger = Logger.getLogger(XmlFileConfigTask.class.getName());

    protected File origfile;
    protected File patchfile;
    protected File tofile;
    protected File confFile;
    protected boolean cleanup;

    protected Properties confProps = new Properties();


    public void setConfigFile(File confFile)
    {
        this.confFile = confFile;
    }


    /**
     * Adds an XML merge configuration property (XPath expression).
     * 
     * @param key the property key
     * @param value the property value
     */
    public void addProperty(String key, String value) {
        confProps.setProperty(key, value);
    }

   
    @Override
    public void validateAttributes() throws Exception {
        if (!confProps.isEmpty() && confFile != null) {
            throw new Exception("Using both XML merge configuration file and explicit merge properties not allowed");
        }
        super.validateAttributes();
    }

    @Override
    public void execute() throws Exception {
    	try
    	{
	        if (confFile != null)
	        {
	            InputStream configIn = null;
	            try
	            {
	                configIn = new FileInputStream(confFile);
	                confProps.load(configIn);
	            } 
	            finally 
	            {
	                if (configIn != null) 
	                {
	                	configIn.close();
	                }
	            }
	        }
    	}
    	catch (Exception e)
    	{
            if (failonerror)
            {
                throw e;
            }
            else
            {
                logger.warning(e.getMessage());
                return; //skip task execution
            }    		
    	}
    	
    	super.execute();
    }

	@Override
	protected void doFileMergeOperation() throws Exception {
        // Create the XmlMerge instance and execute the merge
        XmlMerge xmlMerge;
        xmlMerge = new ConfigurableXmlMerge(new PropertyXPathConfigurer(confProps));
        xmlMerge.merge(filesToMerge.toArray(new File[filesToMerge.size()]), tofile);
	}
}

/*
 * IzPack - Copyright 2001-2009 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/ http://izpack.codehaus.org/
 * 
 * Copyright 2009 Dennis Reil
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.izforge.izpack.installer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PanelActionConfiguration implements Serializable
{
    private Map<String,String> properties;  
    
    public PanelActionConfiguration(){
        this.properties = new HashMap<String, String>();
    }
    
    public void addProperty(String key, String value){
        this.properties.put(key, value);
    }
    
    public String getProperty(String key){
        return this.properties.get(key);
    }
    
    public String getProperty(String key, String defaultValue){
        String result = getProperty(key);
        if (result == null){
            result = defaultValue;
        }
        return result;
    }
    
    public Map<String, String> getProperties()
    {
        return properties;
    }

    
    public void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }
}

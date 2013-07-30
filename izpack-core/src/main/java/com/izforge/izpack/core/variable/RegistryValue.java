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

package com.izforge.izpack.core.variable;

import java.io.Serializable;

import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.config.base.Reg;


public class RegistryValue extends ValueImpl implements Serializable
{
    /**
     *
     */
    private static final long serialVersionUID = 97879516787269847L;

    public String root; // optional
    public String key; // mandatory
    public String value; // optional; if null -> use default value
    private String resolvedValue;

    public RegistryValue(String root, String key, String value)
    {
        super();
        this.root = root;
        this.key = key;
        this.value = value;
    }

    public String getRoot()
    {
        return root;
    }

    public void setRoot(String root)
    {
        this.root = root;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getValue()
    {
        return this.value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    @Override
    public void validate() throws Exception
    {
        if ((this.root == null && this.key == null) ||
                ((this.root != null && this.root.length() <= 0) &&
                        (this.key != null && this.key.length() <= 0)))
        {
            throw new Exception("No or empty registry key path");
        }
    }
    
    @Override
    public String toString() {
    	StringBuilder str = new StringBuilder();
    	if( root != null ) {
    		str.append("root: ").append(root).append(", ");
    	}
    	if( key != null ) {
    		str.append("key: ").append(key).append(", ");
    	}
    	if( value != null ) {
    		str.append("value: ").append(value).append(", ");
    	}
    	if( resolvedValue != null ) {
    		str.append("resolved: ").append(resolvedValue);
    	}
    	return str.toString();
    }

    @Override
    public String resolve() throws Exception
    {
        if (!OsVersion.IS_WINDOWS)
        {
            throw new Exception("Registry access allowed only on Windows OS");
        }

        Reg reg = null;
        Reg.Key regkey = null;
        if (root != null)
        {
            reg = new Reg(root);
        }
        if (key != null)
        {
            if (reg == null)
            {
            	// If the regRoot is not provided, load the portion of the registry indicated by regKey
                reg = new Reg(key);
            }
            regkey = reg.get(key);
        }
        if (regkey != null)
        {
            return regkey.get(value);
        }

        return null;
    }

    @Override
    public String resolve(VariableSubstitutor... substitutors) throws Exception
    {
        if (!OsVersion.IS_WINDOWS)
        {
            throw new Exception("Registry access allowed only on Windows OS");
        }

        Reg reg = null;
        Reg.Key regkey = null;
        if (root != null)
        {
            String _root_ = root;
            for (VariableSubstitutor substitutor : substitutors)
            {
                _root_ = substitutor.substitute(_root_);
            }
            reg = new Reg(_root_);
        }
        if (key != null)
        {
        	String _key_ = key;
            for (VariableSubstitutor substitutor : substitutors)
            {
                _key_ = substitutor.substitute(_key_);
            }
            
            if (reg == null)
            {
            	// If the regRoot is not provided, load the portion of the registry indicated by regKey
                reg = new Reg(_key_);
            }        
            regkey = reg.get(_key_);            
        }
        if (regkey != null)
        {
            String _value_ = value;
            for (VariableSubstitutor substitutor : substitutors)
            {
                _value_ = substitutor.substitute(_value_);
            }
            return regkey.get(_value_);
        }

        return null;
    }
}

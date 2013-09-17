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
        return resolve((VariableSubstitutor[])null);
    }

    @Override
    public String resolve(VariableSubstitutor... substitutors) throws Exception
    {
        if (!OsVersion.IS_WINDOWS)
        {
            throw new Exception("Registry access allowed only on Windows OS");
        }
        
        String _root_ = root;
        String _key_ = key;
        String _value_ = value;
        if (substitutors != null)
        {
            for (VariableSubstitutor substitutor : substitutors)
            {
                _root_ = substitutor.substitute(_root_);
                _key_ = substitutor.substitute(_key_);
                _value_ = substitutor.substitute(_value_);
            }
        }

        Reg reg = null;
        Reg.Key regkey = null;
        if (_root_ != null)
        {
            reg = new Reg(_root_, false);
            if (!_key_.startsWith(_root_))
            {
                //keys in Reg object are stored with full path - prepend root if _key_ is relative
                _key_ = _root_ + "\\" + _key_;
            }
        }
        if (_key_ != null)
        {
            if (reg == null)
            {
                // If the regRoot is not provided, load the portion of the registry indicated by regKey
                reg = new Reg(_key_, false);
            }
            regkey = reg.get(_key_);
        }
        if (regkey != null)
        {
            resolvedValue=regkey.get(_value_);
            return resolvedValue;
        }

        return null;
    }
}

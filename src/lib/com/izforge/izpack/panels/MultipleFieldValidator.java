/*
 * IzPack - Copyright 2001-2009 Julien Ponge, All Rights Reserved.
 * 
 * Copyright 2009 Dennis Reil
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
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
package com.izforge.izpack.panels;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.InstallData;

/**
 * Validator helper used for validating a group of related fields, e.g. a password group in console
 * based mode.
 * 
 * @author Sergiy Shyrkov
 */
public class MultipleFieldValidator implements ProcessingClient
{
    private List<String> inputs;

    private List<ValidatorContainer> validators;

    private ValidatorContainer currentValidator;
    
    private String message;
    private AutomatedInstallData idata;
    

    public MultipleFieldValidator(AutomatedInstallData idata, List<String> inputs, List<ValidatorContainer> validators)
    {
        this.inputs = inputs;
        this.validators = validators;
        this.idata = idata;
        
    }

    public String getFieldContents(int index)
    {
        return inputs.get(index);
    }

    public int getNumFields()
    {
        return inputs.size();
    }

    public String getText()
    {
        return getFieldContents(0);
    }

    public Map<String, String> getValidatorParams()
    {        
        return (currentValidator != null) ? currentValidator.getValidatorParams() : new HashMap<String,String>();
    }

    public boolean hasParams()
    {
        return (currentValidator != null) ? currentValidator.hasParams() : false;
    }

    public boolean validate()
    {
        boolean success = true;
        
        if (validators != null){
            for (ValidatorContainer validator : validators)
            {
                currentValidator = validator;
                Validator validatorInstance = currentValidator.getValidator();
                if (validatorInstance != null){
                    success = validatorInstance.validate(this, idata);
                    if (!success){
                        message = currentValidator.getMessage();
                        break;
                    }
                }
                
            }    
        }
        
        return success;
    }
    
    public String getValidationMessage(){
        return message;
    }
}

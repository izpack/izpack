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

import com.izforge.izpack.installer.InstallData;

public class StringInputProcessingClient implements ProcessingClient
{

    private String input;

    private List<ValidatorContainer> validators;

    private ValidatorContainer currentValidator;
    
    private String message;
    
    private InstallData idata = null;
    

    public StringInputProcessingClient(InstallData idata, String input, List<ValidatorContainer> validators)
    {
        this.input = input;
        this.validators = validators;
        this.idata = idata;
    }

    public String getFieldContents(int index)
    {
        return input;
    }

    public int getNumFields()
    {
        return 1;
    }

    public String getText()
    {
        return this.input;
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

/**
 * 
 */
package com.izforge.izpack.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.installer.DataValidator.Status;
import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;
import com.mongodb.MongoClient;



public class NodeIdentifierValidator implements Validator
{
    NodeIdentifierDataValidator validator = new NodeIdentifierDataValidator();

    public boolean validate(ProcessingClient client)
    {
        // find app_name
        String appname = client.getValidatorParams().get("APP_NAME");
        
        // then validate
        String nodename = client.getText();
        
        return (validator.validate(nodename, appname)==Status.OK);
    }


}


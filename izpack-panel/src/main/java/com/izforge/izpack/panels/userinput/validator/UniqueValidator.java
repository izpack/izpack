package com.izforge.izpack.panels.userinput.validator;

import com.izforge.izpack.panels.userinput.processorclient.ProcessingClient;

import java.util.HashSet;
import java.util.Set;

/**
 * Ensure that all values are unique.
 * Expects a comma separated string of values.
 */
public class UniqueValidator implements Validator
{
    @Override
    public boolean validate(ProcessingClient client)
    {
        Set<String> set = new HashSet<String>();
        for(String value : client.getText().split(","))
        {
            if(set.contains(value))
            {
                return false;
            }
            set.add(value);
        }
        return true;
    }
}

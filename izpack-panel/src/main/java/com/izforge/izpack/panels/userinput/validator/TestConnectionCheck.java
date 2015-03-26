package com.izforge.izpack.panels.userinput.validator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.izforge.izpack.api.substitutor.VariableSubstitutor;

public class TestConnectionCheck extends AbstractValidator
{
	private VariableSubstitutor variableSubstitutor;
	
    public TestConnectionCheck(VariableSubstitutor variableSubstitutor)
    {
        this.variableSubstitutor = variableSubstitutor;
    }
	public boolean validate(String[] values, Map<String, String> parameters)
    {

		Map<String, String> params = getParams(parameters);
	
		//STEP 1: DB details Configuration
        
        String dbtype;
		String dbname;
		String dbhost;
		String dbport;
		String dbusername;
		String dbpassword;

	
          dbtype = params.get("dbtype");
 		  dbhost = params.get("dbhost");
 		  dbport = params.get("dbport");
 		  dbname = params.get("dbname");
 		  dbusername = params.get("dbusername");
 		  dbpassword = params.get("dbpassword");	
 		  
         if (dbpassword == null)
         {
        	 dbpassword = getPassword(values);
            
         }
         else if (dbpassword.equalsIgnoreCase(""))
         {
        	 dbpassword = getPassword(values);
             
         }
      
   		 // JDBC driver name and database URL
   		 String JDBC_DRIVER = null;
   		 // Database credentials
   		 String USER = dbusername;
   		 String PASS = dbpassword;
   		 
	   	 Connection conn = null;
   		 	 
   		 if(dbtype.equals("postgresql"))
   		     JDBC_DRIVER = "org.postgresql.Driver";
   		 if(dbtype.equals("mysql"))
   			 JDBC_DRIVER = "com.mysql.jdbc.Driver";  
   		 
   		 String DB_URL = "jdbc:"+dbtype+"://"+dbhost+":"+dbport+"/"+dbname;
         
   		//System.out.println("******************** *****************************  ******************"+DB_URL+"::"+USER+"::"+PASS);
   		
   	//STEP 2: Register JDBC driver
	   	 try {
	   		Class.forName(JDBC_DRIVER);
	   		try {
	   			//STEP 3: Open a connection
	   			conn = DriverManager.getConnection(DB_URL,USER,PASS);
	   		} catch (SQLException e) {
	   			//System.out.println(e.getMessage());
	   			return false;
	   		}
	   	} catch (ClassNotFoundException e) {
	   		//System.out.println(e.getMessage());
	   		return false;
	   	}
	   	 return true;
    }

	private Map<String, String> getParams(Map<String, String> parameters)
	{
	Map<String, String> result = new HashMap<String, String>();
	for (String key : parameters.keySet())
	{
	    // Feed parameter values through vs
	    String value = variableSubstitutor.substitute(parameters.get(key));
	    result.put(key, value);
	}
	return result;
	}
	
	 private String getPassword(String[] values)
	    {
	        // ----------------------------------------------------
	        // We assume that if there is more than one field an equality validation
	        // was already performed.
	        // ----------------------------------------------------
	        return (values.length > 0) ? values[0] : null;
	    }


}




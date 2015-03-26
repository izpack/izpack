package com.izforge.izpack.panels.userinput.field.button;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.panels.userinput.action.ButtonAction;
import com.izforge.izpack.util.Console;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;



public class DBConnectionCheck extends ButtonAction
{
		private static final String ERROR = "error";
	
		public DBConnectionCheck(InstallData installData){
			super(installData);
		}
		
		@Override
		public boolean execute()
		{
			
				//STEP 1: DB details Configuration
		        
		        String dbtype;
				String dbname;
				String dbhost;
				String dbport;
				String dbusername;
				String dbpassword;

				dbtype=installData.getVariable("dbtype");
				dbhost=installData.getVariable("dbhost");
				dbport=installData.getVariable("dbport");
				dbname=installData.getVariable("dbname");
				dbusername=installData.getVariable("dbusername");
				dbpassword=installData.getVariable("dbpassword");
				
				
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
		
		@Override
		public boolean execute(Console console)
		{
			if(!execute())
			{
			console.println(messages.get(ERROR));
			return false;
			}
			return true;
		}
		
		
		@Override
		public boolean execute(Prompt prompt)
		{
			if(!execute())
			{
			prompt.warn(messages.get(ERROR));
			return false;
			}
			return true;
		}
		
}
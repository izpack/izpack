package com.izforge.izpack.util;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.panels.ProcessingClient;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.ServerAddress;

import java.util.Arrays;
import java.util.List;

public class MongoDBValidator implements com.izforge.izpack.installer.DataValidator 
{

    public Status validateData(AutomatedInstallData adata)
    {
        Status bReturn = Status.ERROR;
        try
        {
        
            String userName = adata.getVariable("mongodb.url.username");
            String passWord = adata.getVariable("mongodb.url.password");
            String hostName = adata.getVariable("mongodb.service.hostname");
            String hostPort = adata.getVariable("mongodb.service.port");
            
            MongoClient mongoClient = new MongoClient( hostName , Integer.parseInt(hostPort) );
            mongoClient.getVersion();
            
            bReturn = Status.OK; 

            // test if syracuse db already exists
            List<String> lstDb = mongoClient.getDatabaseNames();
            
            for (String dbb : lstDb)
            {
                if (dbb.equals("syracuse"))
                {
                    bReturn = Status.WARNING;
                }
            }
            

        }
        catch (Exception ex)
        {
            Debug.trace(ex.getMessage());
            bReturn = Status.ERROR; 
        }

        return bReturn;
    }

    public String getErrorMessageId()
    {
        // TODO Auto-generated method stub
        return "mongodbtesterror";
    }

    public String getWarningMessageId()
    {
        // TODO Auto-generated method stub
        return "mongodbtestwarn";
    }

    public boolean getDefaultAnswer()
    {
        // TODO Auto-generated method stub
        return false;
    }

}

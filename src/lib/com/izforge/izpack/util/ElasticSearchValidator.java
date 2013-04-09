package com.izforge.izpack.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.installer.DataValidator.Status;


public class ElasticSearchValidator implements DataValidator
{

    public Status validateData(AutomatedInstallData adata)
    {
        Status bReturn = Status.ERROR;
        try
        {
        
            String hostName = adata.getVariable("elasticsearch.url.hostname");
            String hostPort = adata.getVariable("elasticsearch.url.port");
            
            String strurl = "http://"+hostName+":"+hostPort+"/_nodes";
            
            String strHttpResult = getHTML(strurl);
            
            if (strHttpResult.startsWith("{\"ok\":true,"))  bReturn = Status.OK; 

        }
        catch (Exception ex)
        {
            Debug.trace(ex.getMessage());
        }

        return bReturn;
    }

    public String getHTML(String urlToRead) {
        URL url;
        HttpURLConnection conn;
        BufferedReader rd;
        String line;
        String result = "";
        try {
           url = new URL(urlToRead);
           conn = (HttpURLConnection) url.openConnection();
           conn.setRequestMethod("GET");
           rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
           while ((line = rd.readLine()) != null) {
              result += line;
           }
           rd.close();
        } catch (Exception e) {
           e.printStackTrace();
        }
        return result;
     }    
    
    public String getErrorMessageId()
    {
        // TODO Auto-generated method stub
        return "elasticsearchtesterror";
    }

    public String getWarningMessageId()
    {
        // TODO Auto-generated method stub
        return "elasticsearchtesterror";
    }

    public boolean getDefaultAnswer()
    {
        // TODO Auto-generated method stub
        return false;
    }

}

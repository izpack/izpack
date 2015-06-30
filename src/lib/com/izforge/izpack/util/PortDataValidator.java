package com.izforge.izpack.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.izforge.izpack.Pack;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;


public class PortDataValidator implements DataValidator
{

    public Status validateData(AutomatedInstallData adata)
    {
        InetAddress inet = null;
        String host = "localhost";
        Status retValue = Status.ERROR;

        String value = adata.getVariable("mongodb.service.port");
        
        // if update mode
        // load old value
        
        boolean updatemode = ("true".equalsIgnoreCase(adata.getVariable("MODIFY.IZPACK.INSTALL")));

        if (updatemode)
        {
            // load old installadata
            try
            {
                FileInputStream fin = new FileInputStream(new File(adata.getInstallPath() + File.separator + AutomatedInstallData.INSTALLATION_INFORMATION));
                ObjectInputStream oin = new ObjectInputStream(fin);
                List packsinstalled = (List) oin.readObject();
                Properties variables = (Properties) oin.readObject();
                fin.close();
                
                String oldPort = variables.getProperty("mongodb.service.port");
                
                if (value.equals(oldPort)) return Status.OK;
                
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                return Status.ERROR;
            }
            
        }

        try
        {
            inet = InetAddress.getByName(host);
            ServerSocket socket = new ServerSocket(Integer.parseInt(value), 0, inet);
            if (socket.getLocalPort() > 0)
            {
                socket.close();
                return Status.OK;
            }
            else
            {
                return Status.WARNING;
            }
        }
        catch (Exception ex)
        {
            retValue = Status.ERROR;
        }

        return retValue;

    }

    public String getErrorMessageId()
    {
        return "portvalidatorerror";
    }

    public String getWarningMessageId()
    {
        return "portvalidatoralreadyinuse";
    }

    public boolean getDefaultAnswer()
    {
        //by default if updating ourself then the port is already in use
        return true;
    }

}

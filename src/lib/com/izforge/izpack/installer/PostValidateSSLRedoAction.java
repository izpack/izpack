package com.izforge.izpack.installer;

import com.izforge.izpack.util.AbstractUIHandler;


public class PostValidateSSLRedoAction implements PanelAction
{

    public void executeAction(AutomatedInstallData adata, AbstractUIHandler handler)
    {
        String strRedoSSL = adata.getVariable("MONGODB.SSL.REDO");
        String strUpdate = adata.getVariable("MODIFY.IZPACK.INSTALL");
        String strSSLAlreadyDone = adata.getVariable("mongodb.ssl.alreadydone");
        
        if ("true".equalsIgnoreCase(strUpdate) && "true".equalsIgnoreCase(strRedoSSL) && "true".equalsIgnoreCase(strSSLAlreadyDone))
        {
            // we want to redo ssl configuration
            // set mongodb.ssl.alreadydone to false
            
            adata.setVariable("mongodb.ssl.alreadydone","false");
            
        }
    }

    public void initialize(PanelActionConfiguration configuration)
    {
        // nothing to initialize

    }

}

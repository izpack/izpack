package com.izforge.izpack.event;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import com.izforge.izpack.Pack;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerException;
import com.izforge.izpack.installer.ResourceManager;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.CleanupClient;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.Log;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.SpecHelper;
import com.izforge.izpack.util.VariableSubstitutor;


public class UpdateListener extends SimpleInstallerListener implements CleanupClient
{
    
    public static final String BEFORE_UPDATE_SCRIPT = "BeforeUpdateScript"; 
    public static final String AFTER_UPDATE_SCRIPT = "AfterUpdateScript"; 
    public static final String PLATFORM = OsVersion.IS_UNIX ? "unix":"windows";


    public void cleanUp()
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.event.SimpleInstallerListener#afterPacks(com.izforge.izpack.installer.AutomatedInstallData, com.izforge.izpack.util.AbstractUIProgressHandler)
     */
    @Override
    public void afterPacks(AutomatedInstallData idata, AbstractUIProgressHandler handler)
            throws Exception
    {
        super.afterPacks(idata, handler);

        if (Boolean.valueOf(idata.getVariable(InstallData.MODIFY_INSTALLATION)))
        {
            // at the top i imagine a first general action script
            // let says beforeUpdate Script
            
            fetchAndExcuteResource(AFTER_UPDATE_SCRIPT+"_"+PLATFORM, idata);
            
            
            // we can call the update before/after script for each deleted packs
            // ???
            
        }
    
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.event.SimpleInstallerListener#afterPack(com.izforge.izpack.Pack, java.lang.Integer, com.izforge.izpack.util.AbstractUIProgressHandler)
     */
    @Override
    public void afterPack(Pack pack, Integer i, AbstractUIProgressHandler handler) throws Exception
    {
        super.afterPack(pack, i, handler);

        if (Boolean.valueOf(getInstalldata().getVariable(InstallData.MODIFY_INSTALLATION)))
        {
            fetchAndExcuteResource(pack.id+"_"+AFTER_UPDATE_SCRIPT+"_"+PLATFORM, getInstalldata());
        }
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.event.SimpleInstallerListener#beforePacks(com.izforge.izpack.installer.AutomatedInstallData, java.lang.Integer, com.izforge.izpack.util.AbstractUIProgressHandler)
     */
    @Override
    public void beforePacks(AutomatedInstallData idata, Integer npacks,
            AbstractUIProgressHandler handler) throws Exception
    {
        super.beforePacks(idata, npacks, handler);
        
        if (Boolean.valueOf(idata.getVariable(InstallData.MODIFY_INSTALLATION)))
        {
            // at the top i imagine a first general action script
            // let says beforeUpdate Script
            
            fetchAndExcuteResource(BEFORE_UPDATE_SCRIPT+"_"+PLATFORM, idata);
            
            
            // we can call the update before/after script for each deleted packs
            // ???
            
        }
        
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.event.SimpleInstallerListener#beforePack(com.izforge.izpack.Pack, java.lang.Integer, com.izforge.izpack.util.AbstractUIProgressHandler)
     */
    @Override
    public void beforePack(Pack pack, Integer i, AbstractUIProgressHandler handler)
            throws Exception
    {
        super.beforePack(pack, i, handler);
        
        if (Boolean.valueOf(getInstalldata().getVariable(InstallData.MODIFY_INSTALLATION)))
        {
            fetchAndExcuteResource(pack.id+"_"+BEFORE_UPDATE_SCRIPT+"_"+PLATFORM, getInstalldata());
        }
        
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.event.SimpleInstallerListener#afterInstallerInitialization(com.izforge.izpack.installer.AutomatedInstallData)
     */
    @Override
    public void afterInstallerInitialization(AutomatedInstallData data)
    {
        super.afterInstallerInitialization(data);
        
        // do we need to initialize something ?
        // don't know for now but ??
        
    }
    
    public void fetchAndExcuteResource (String resource, AutomatedInstallData idata)
            throws Exception
    {
        SpecHelper spechelper = new SpecHelper();
        String ext = OsVersion.IS_UNIX ? ".sh":".cmd";
        InputStream stream = spechelper.getResource(resource);
        if (stream != null)
        {
            InputStream substitutedStream = spechelper.substituteVariables(stream, new VariableSubstitutor(idata.getVariables()));

            File tempFile = File.createTempFile(resource, ext);
            FileOutputStream fos = null;
            tempFile.deleteOnExit();
            fos = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = substitutedStream.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            
            substitutedStream.close();
            fos.flush();
            fos.close();
            
            // ok now we have to execute
            ProcessBuilder procBuilder = null;
            
            if (OsVersion.IS_UNIX)
            {
                procBuilder= new ProcessBuilder(System.getenv("SHELL"), tempFile.getAbsolutePath());
            }
            else
            {
                procBuilder= new ProcessBuilder("cmd.exe", "/C", tempFile.getAbsolutePath());
            }
            
            Debug.log("launching "+tempFile.getAbsolutePath());
            Process p = procBuilder.start();
            InputStream errorOutput = new BufferedInputStream(p.getErrorStream(), 10000);
            InputStream consoleOutput = new BufferedInputStream(p.getInputStream(), 10000);
            
            Debug.log("errorOutput:");
            
            BufferedReader br = new BufferedReader(new InputStreamReader(errorOutput));
            String read = br.readLine();
            while (read!=null)
            {
                Debug.log(read);
                read=br.readLine();
            }
            
            Debug.log("consoleOutput:");

            BufferedReader br2 = new BufferedReader(new InputStreamReader(consoleOutput));
            String read2 = br2.readLine();
            while (read2!=null)
            {
                Debug.log(read2);
                read2=br2.readLine();
            }

            int exitCode = p.waitFor();

            Debug.log("exitCode: "+ exitCode);

            if (exitCode != 0)
            {
                // script doesn't return 0 = SUCCESS
                // throw an exception
                
                throw new InstallerException(resource + " return code is " + exitCode + " !");
            }
        }

    }
    
    
    

    
    
}

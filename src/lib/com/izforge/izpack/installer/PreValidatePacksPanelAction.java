package com.izforge.izpack.installer;

import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.Pack;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.util.Debug;

import java.io.*;
import java.util.*;


public class PreValidatePacksPanelAction implements PanelAction
{
    
    private Map installedpacks = null;

    public void executeAction(AutomatedInstallData adata, AbstractUIHandler handler)
    {
        // well if in modify mode
        // then we must select already installed packs
        
        Boolean modifyinstallation = Boolean.valueOf(adata.getVariable(InstallData.MODIFY_INSTALLATION));
        installedpacks = new HashMap();

        if (modifyinstallation)
        {
            // installation shall be modified
            // load installation information
            Debug.trace("Update mode, loading installed packs ...");

            try
            {
                FileInputStream fin = new FileInputStream(new File(adata.getInstallPath() + File.separator + AutomatedInstallData.INSTALLATION_INFORMATION));
                ObjectInputStream oin = new ObjectInputStream(fin);
                List packsinstalled = (List) oin.readObject();
                for (Object aPacksinstalled : packsinstalled)
                {
                    Pack installedpack = (Pack) aPacksinstalled;
                    if ((installedpack.id != null) && (installedpack.id.length() > 0))
                    {
                        installedpacks.put(installedpack.id, installedpack);
                        Debug.trace("Found " +installedpack.id);
                    }
                    else
                    {
                        installedpacks.put(installedpack.name, installedpack);
                        Debug.trace("Found " +installedpack.name);
                    }
                }
                
                this.removeAlreadyInstalledPacks(adata.selectedPacks);
                adata.installedPacks = packsinstalled;
                
                for (Object aPack : adata.availablePacks)
                {
                    if (installedpacks.containsKey( ((Pack)aPack).id ) || installedpacks.containsKey( ((Pack)aPack).name ) )
                    {
                        adata.selectedPacks.add((Pack)aPack);
                        ((Pack)aPack).preselected = true;
                        ((Pack)aPack).required = true;
                
                    }
                }
                
                //adata.availablePacks.
                
                Debug.trace("Found " + packsinstalled.size() + " installed packs");
                Debug.trace("Loading properties ...");

                Properties variables = (Properties) oin.readObject();
                
                Iterator iter = variables.keySet().iterator();
                while (iter.hasNext())
                {
                    String key = (String) iter.next();
                    if (Character.isLowerCase(key.charAt(0)) || "UNINSTALL_NAME".equals(key))
                    {
                        adata.setVariable( key, (String) variables.get(key));
                        Debug.trace((String) key+"="+ (String) variables.get(key));
                    }
                    
                    // TODO : verrue !
                    if (key.equals("syracuse.winservice.username"))
                    {
                        adata.setVariable( "syracuse.winservice.username.oldvalue", (String) variables.get(key));
                    }
                }
                
                
                
                fin.close();
                
                
            }
            catch (FileNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (ClassNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        

    }

    public void initialize(PanelActionConfiguration configuration)
    {
        // nothing to do really

    }
    
    private void removeAlreadyInstalledPacks(List selectedpacks)
    {
        List<Pack> removepacks = new ArrayList<Pack>();

        for (Object selectedpack1 : selectedpacks)
        {
            Pack selectedpack = (Pack) selectedpack1;
            String key = "";
            if ((selectedpack.id != null) && (selectedpack.id.length() > 0))
            {
                key = selectedpack.id;
            }
            else
            {
                key = selectedpack.name;
            }
            if (installedpacks.containsKey(key))
            {
                // pack is already installed, remove it
                removepacks.add(selectedpack);
            }
        }
        for (Pack removepack : removepacks)
        {
            selectedpacks.remove(removepack);
        }
    }
    

}

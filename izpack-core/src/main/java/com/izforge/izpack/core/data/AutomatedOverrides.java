package com.izforge.izpack.core.data;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Overrides;

import java.io.File;
import java.io.IOException;

public class AutomatedOverrides implements Overrides
{
    private InstallData installData;

    public AutomatedOverrides(InstallData installData)
    {
        setInstallData(installData);
    }

    @Override
    public String fetch(String name)
    {
        if (name.equals(InstallData.INSTALL_PATH))
        {
            String installPath = installData.getInstallPath();
            if (installPath == null)
            {
                installPath = installData.getDefaultInstallPath();
            }
            return installPath;
        }
        return null;
    }

    @Override
    public String fetch(String name, String defaultValue)
    {
        return null;
    }

    @Override
    public boolean containsKey(String name)
    {
        return false;
    }

    @Override
    public String remove(String name)
    {
        return null;
    }

    @Override
    public int size()
    {
        return 0;
    }

    @Override
    public void setInstallData(InstallData installData)
    {
        this.installData = installData;
    }

    @Override
    public File getFile()
    {
        return null;
    }

    @Override
    public void load() throws IOException
    {
    }
}

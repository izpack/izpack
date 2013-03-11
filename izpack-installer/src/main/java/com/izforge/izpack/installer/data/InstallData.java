package com.izforge.izpack.installer.data;

import static com.izforge.izpack.util.Platform.Name.WINDOWS;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.util.Platform;


public class InstallData extends AutomatedInstallData
{
    public InstallData(Variables variables, Platform platform)
    {
        super(variables, platform);
    }

    @Override
    public void setInstallPath(String path)
    {
        super.setInstallPath(path);
        setInstallDriveFromPath(path, INSTALL_DRIVE);
    }

    @Override
    public void setDefaultInstallPath(String path)
    {
        super.setDefaultInstallPath(path);
        setInstallDriveFromPath(path, DEFAULT_INSTALL_DRIVE);
    }

    @Override
    public String getDefaultInstallPath()
    {
        return getVariable(DEFAULT_INSTALL_PATH);
    }

    private void setInstallDriveFromPath(String path, String variable)
    {
        if (getPlatform().isA(WINDOWS))
        {
            String[] parts = path.trim().split(":", 2);
            if (parts.length > 0 && parts[0].length() == 1)
            {
                setVariable(variable, parts[0] + ":");
            }
        }
    }

}

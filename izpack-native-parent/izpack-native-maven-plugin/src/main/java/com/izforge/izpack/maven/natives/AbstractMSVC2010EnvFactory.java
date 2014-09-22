package com.izforge.izpack.maven.natives;

import org.codehaus.mojo.natives.NativeBuildException;
import org.codehaus.mojo.natives.msvc.AbstractMSVCEnvFactory;
import org.codehaus.mojo.natives.msvc.RegQuery;
import org.codehaus.mojo.natives.util.EnvUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Abstract factory for Microsoft Visual C++ 2010 environment variables.
 * <p/>
 * This makes the following assumptions:
 * <ul>
 * <li>.NET 4.0 is being used</li>
 * <li>Windows SDK 7.1 is being used</li>
 * </ul>
 *
 * @author Tim Anderson
 */
public abstract class AbstractMSVC2010EnvFactory extends AbstractMSVCEnvFactory
{
    /**
     * Visual Studio 10 tool dir environment variable.
     */
    private static final String VS100COMNTOOLS_ENV_KEY = "VS100COMNTOOLS";

    /**
     * The logger. We don't have access to Plexus logging.
     */
    private Logger log = Logger.getLogger(AbstractMSVC2010EnvFactory.class.getName());

    /**
     * Returns the Visual C++ 2010 environment variables.
     * This overrides the parent which would otherwise cache the results in a singleton - useless when building
     * for multiple platforms in the one JVM.
     *
     * @return the Visual C++ 2010 environment variables
     * @throws NativeBuildException for any error
     */
    @Override
    public Map<String,String> getEnvironmentVariables() throws NativeBuildException
    {
        return createEnvs();
    }

    /**
     * Returns the Visual C++ 2010 environment variables.
     *
     * @return the Visual C++ 2010 environment variables
     * @throws NativeBuildException
     */
    protected Map<String, String> createEnvs() throws NativeBuildException
    {
        Map<String, String> envs = new HashMap<String,String>();

        File vsCommonToolDir = getCommonToolsDirectory();

        File vsInstallDir = getVisualStudioInstallDirectory(vsCommonToolDir);

        if (!vsInstallDir.isDirectory())
        {
            throw new NativeBuildException(vsInstallDir.getPath() + " is not a directory.");
        }
        if (log.isLoggable(Level.INFO))
        {
            log.info("Visual Studio install dir=" + vsInstallDir);
        }

        File vcInstallDir = getVCInstallDir(vsInstallDir);
        if (!vcInstallDir.isDirectory())
        {
            throw new NativeBuildException(vcInstallDir.getPath() + " is not a directory.");
        }
        if (log.isLoggable(Level.INFO))
        {
            log.info("VC install dir=" + vcInstallDir);
        }

        File frameworkDir = getFrameworkDir();

        File windowsSDKDir = getWindowsSDKDir();
        // envs.put("WindowsSDKDir", windowsSDKDir.getPath());

        String[] frameworkVersion = getFrameworkVersion();
        // envs.put("FrameworkVersion", frameworkVersion[0]);

        //setup new PATH
        String currentPathEnv = System.getProperty("java.library.path");

        String newPathEnv = getPath(frameworkDir, frameworkVersion, vsCommonToolDir, vcInstallDir, windowsSDKDir);
        newPathEnv += ";" + currentPathEnv;

        envs.put("Path", newPathEnv);

        //setup new INCLUDE PATH
        String currentIncludeEnv = EnvUtil.getEnv("INCLUDE");
        String newIncludeEnv = vcInstallDir.getPath() + "\\ATLMFC\\INCLUDE;" + vcInstallDir.getPath() + "\\INCLUDE;"
                + windowsSDKDir.getPath() + "\\INCLUDE;" + currentIncludeEnv;

        envs.put("INCLUDE", newIncludeEnv);

        //
        //setup new LIB PATH
        //
        String currentLibEnv = EnvUtil.getEnv("LIB");
        String newLibEnv = getLibEnv(vcInstallDir, windowsSDKDir) + ";" + currentLibEnv;
        envs.put("LIB", newLibEnv);

        String currentLibPathEnv = EnvUtil.getEnv("LIBPATH");
        String newLibPathEnv = getLibPathEnv(frameworkDir, frameworkVersion, vcInstallDir, windowsSDKDir) + ";"
                + currentLibPathEnv;
        envs.put("LIBPATH", newLibPathEnv);

        if (log.isLoggable(Level.INFO))
        {
            for (Object key : envs.keySet())
            {
                log.log(Level.INFO, "VC 2010 Environment: " + key + " = " + envs.get(key));
            }
        }
        return envs;
    }

    /**
     * Returns the PATH environment variable path.
     *
     * @param frameworkDir      the .NET Framework dir
     * @param frameworkVersions the .NET Framework versions
     * @param vsCommonToolDir   the Visual Studio tools dir
     * @param vcInstallDir      the Visual C++ install dir
     * @param windowsSDKDir     the Windows SDK install dir
     * @return the PATH environment variable path
     */
    protected String getPath(File frameworkDir, String[] frameworkVersions, File vsCommonToolDir, File vcInstallDir,
                             File windowsSDKDir)
    {
        StringBuilder result = new StringBuilder();
        result.append(getFrameworkPaths(frameworkDir, frameworkVersions));
        result.append(vsCommonToolDir).append("\\..\\IDE;");
        result.append(vsCommonToolDir).append(';');
        result.append(getVCBinDir(vcInstallDir)).append(';');
        result.append(vcInstallDir).append("\\Bin\\").append("VCPackages;");
        result.append(windowsSDKDir).append("\\Bin;");
        return result.toString();
    }

    /**
     * Returns the .NET Framework versions
     *
     * @return the .NET Framework versions, most recent first.
     */
    protected String[] getFrameworkVersion()
    {
        return new String[]{"v4.0.30319", "v3.5"};
    }

    /**
     * Returns the Microsoft Windows SDK dir.
     * <p/>
     * Note that if multiple versions of the Windows SDK have been installed, this may return the wrong directory.
     * <p/>
     * It will return <tt>c:\\Program Files\\Microsoft SDKs\\Windows\\v7.1</tt> if it exists, in preference to any
     * other.
     *
     * @return the Microsoft Windows SDK dir
     */
    protected File getWindowsSDKDir()
    {
        File windowsSDKDir = new File(getProgramFiles(), "\\Microsoft SDKs\\Windows\\v7.1");
        if (!windowsSDKDir.exists())
        {
            // RegQuery doesn't handle multiple returned keys.
            String value = RegQuery.getValue("REG_SZ", "HKLM\\SOFTWARE\\Microsoft\\Microsoft SDKs\\Windows",
                    "CurrentInstallFolder");
            if (value != null)
            {
                windowsSDKDir = new File(value);
            }
        }
        return windowsSDKDir;
    }

    /**
     * Returns the Microsoft Visual Studio Common Tools directory.
     *
     * @return the value of the {@link #VS100COMNTOOLS_ENV_KEY} environment variable
     * @throws NativeBuildException if the {@link #VS100COMNTOOLS_ENV_KEY} is not set
     */
    protected File getCommonToolsDirectory() throws NativeBuildException
    {
        String envValue = System.getenv(VS100COMNTOOLS_ENV_KEY);
        if (envValue == null)
        {
            throw new NativeBuildException("Environment variable: " + VS100COMNTOOLS_ENV_KEY + " not available.");
        }

        return new File(envValue);
    }


    /**
     * Returns the Visual Studio Install directory.
     * <p/>
     * E.g. <pre>c:\\Program Files\\Microsoft Visual Studio\\10.0</pre>
     *
     * @param commonToolsDir the tools dir
     * @return the Visual Studio install directory
     * @throws NativeBuildException if the directory cannot be determined
     */
    protected File getVisualStudioInstallDirectory(File commonToolsDir)
            throws NativeBuildException
    {
        try
        {
            return new File(commonToolsDir, "../..").getCanonicalFile();
        }
        catch (IOException e)
        {
            throw new NativeBuildException("Unable to construct Visual Studio install directory using: "
                    + commonToolsDir, e);
        }
    }

    /**
     * Returns the .NET Framework dir.
     * <p/>
     * E.g.: <pre>c:\\Program Files\\Microsoft.NET\\Framework</pre>
     *
     * @return the .NET framework dir
     */
    protected File getFrameworkDir()
    {
        return new File(getSystemRoot() + "\\Microsoft.NET\\Framework");
    }

    /**
     * Returns the Visual C++ install dir.
     * <p/>
     * E.g.: <pre>c:\\Program Files\\Microsoft Visual Studio\\10.0\\VC</pre>
     *
     * @param vsInstallDir the Visual Studio install dir
     * @return the Visual C++ install dir
     */
    protected File getVCInstallDir(File vsInstallDir)
    {
        return new File(vsInstallDir.getPath() + "\\VC");
    }

    /**
     * Returns the Visual C++ bin dir.
     * <p/>
     * E.g.: <pre>c:\\Program Files\\Microsoft Visual Studio\\10.0\\VC\\bin</pre>
     *
     * @param vcInstallDir the Visual C++ install dir
     * @return the Visual C++ bin dir
     */
    protected String getVCBinDir(File vcInstallDir)
    {
        return vcInstallDir.getPath() + "\\bin";
    }

    /**
     * Returns the LIB environment variable path.
     * <p/>
     * E.g.:
     * <pre>
     * c:\\Program Files\\Microsoft Visual Studio\\10.0\\VC\\ATLMFC\\lib;
     * c:\\Program Files\\Microsoft Visual Studio\\10.0\\VC\\lib;
     * c:\\Program Files\\Microsoft SDKs\\Windows\\7.1</pre>
     *
     * @param vcInstallDir  the Visual C++ install dir
     * @param windowsSDKDir the Windows SDK dir
     * @return the LIB environment variable path
     */
    protected String getLibEnv(File vcInstallDir, File windowsSDKDir)
    {
        return vcInstallDir.getPath() + "\\ATLMFC\\lib;" + vcInstallDir.getPath() + "\\lib;"
                + windowsSDKDir.getPath() + "\\lib";
    }

    /**
     * Returns the LIBPATH environment variable path.
     * <p/>
     * E.g.:
     * <pre>
     * c:\\Program Files\\Microsoft Visual Studio\\10.0\\VC\\ATLMFC\\lib;
     * c:\\Program Files\\Microsoft Visual Studio\\10.0\\VC\\lib;
     * c:\\Program Files\\Microsoft SDKs\\Windows\\7.1</pre>
     *
     * @param frameworkDir      the .NET Framework directory
     * @param frameworkVersions the .NET Framework versions
     * @param vcInstallDir      the Visual C++ install dir
     * @param windowsSDKDir     the Windows SDK dir
     * @return the LIBPATH environment variable path
     */
    protected String getLibPathEnv(File frameworkDir, String[] frameworkVersions, File vcInstallDir, File windowsSDKDir)
    {
        StringBuilder result = new StringBuilder();
        result.append(getFrameworkPaths(frameworkDir, frameworkVersions));
        result.append(vcInstallDir.getPath()).append("\\ATLMFC\\lib;");
        result.append(vcInstallDir.getPath()).append("\\lib");
        return result.toString();
    }

    /**
     * Creates a path for .NET framework versions.
     *
     * @param frameworkDir      the base .NET Framework directory
     * @param frameworkVersions the .NET Framework versions
     * @return the paths
     */
    protected String getFrameworkPaths(File frameworkDir, String[] frameworkVersions)
    {
        StringBuilder result = new StringBuilder();
        for (String version : frameworkVersions)
        {
            result.append(frameworkDir).append('\\').append(version).append(';');
        }
        return result.toString();
    }

}

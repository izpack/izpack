package com.izforge.izpack.panels.jdkpath;

import com.coi.tools.os.win.MSWinConstants;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.panels.path.PathInputBase;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.Platform;

import java.io.File;
import java.util.Arrays;
import java.util.StringTokenizer;

public class JDKPathPanelHelper
{
    public static final String[] testFiles = new String[]{"lib" + File.separator + "tools.jar"};
    public static final String JDK_VALUE_NAME = "JavaHome";
    public static final String JDK_ROOT_KEY = "Software\\JavaSoft\\Java Development Kit";
    public static final String OSX_JDK_HOME = "/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home/";

    public final static String JDK_PATH = "jdkPath";
    public final static String JDK_VAR_NAME = "jdkVarName";

    private static String minVersion = null;
    private static String maxVersion = null;

    /**
     * MUST always be called in constructor of JDKPathConsolePanel and JDKPathPanel
     * @param installData
     */
    public static void initialize(InstallData installData)
    {
        minVersion = installData.getVariable("JDKPathPanel.minVersion");
        maxVersion = installData.getVariable("JDKPathPanel.maxVersion");
        installData.setVariable(JDK_VAR_NAME, JDK_PATH);
    }

    /**
     * Obtain the default java path
     * @param installData
     * @param handler
     * @return
     */
    public static String getDefaultJavaPath(InstallData installData, RegistryDefaultHandler handler)
    {
        String detectedJavaVersion = "";

        String defaultValue = installData.getVariable(JDK_PATH);

        if(defaultValue != null)
        {
            return defaultValue;
        }

        if (OsVersion.IS_OSX)
        {
            defaultValue = OSX_JDK_HOME;
        }
        else
        {
            defaultValue = (new File(installData.getVariable("JAVA_HOME"))).getParent();
        }

        //See if java from currently running jre is valid, otherwise check the registry.
        //If java is still not found set path ot JAVA_HOME to an empty string.
        Platform platform = installData.getPlatform();
        detectedJavaVersion = JDKPathPanelHelper.getCurrentJavaVersion(defaultValue, platform);
        if (!JDKPathPanelHelper.pathIsValid(defaultValue) || !JDKPathPanelHelper.verifyVersion(detectedJavaVersion))
        {
            defaultValue = JDKPathPanelHelper.getJavaHomeFromRegistry(handler);
            detectedJavaVersion = JDKPathPanelHelper.getCurrentJavaVersion(defaultValue, platform);
            if (!JDKPathPanelHelper.pathIsValid(defaultValue) || !JDKPathPanelHelper.verifyVersion(detectedJavaVersion))
            {
                defaultValue = "";
            }
        }

        return PathInputBase.normalizePath(defaultValue);
    }

    /**
     * Returns the path to the needed JDK if found in the registry. If there are more than one JDKs
     * registered, that one with the highest allowed version will be returned. Works only on windows.
     * On Unix an empty string returns.
     *
     * @return the path to the needed JDK if found in the windows registry
     */
    public static String getJavaHomeFromRegistry(RegistryDefaultHandler handler)
    {
        String javaHome = "";
        int oldVal = 0;
        RegistryHandler registryHandler = null;

        try
        {
            // Get the default registry handler.
            registryHandler = handler.getInstance();
            if (registryHandler == null)
            {
                // We are on a os which has no registry or the
                // needed dll was not bound to this installation. In
                // both cases we forget the try to get the JDK path from registry.
                return javaHome;
            }

            oldVal = registryHandler.getRoot(); // Only for security...
            registryHandler.setRoot(MSWinConstants.HKEY_LOCAL_MACHINE);
            String[] keys = registryHandler.getSubkeys(JDK_ROOT_KEY);
            if (keys == null || keys.length == 0)
            {
                return javaHome;
            }
            Arrays.sort(keys);
            int i = keys.length - 1;

            // We search for the highest allowed version, therefore retrograde
            while (i > 0)
            {
                String javaVersion = extractJavaVersion(keys[i]);
                if (maxVersion == null || compareVersions(javaVersion, maxVersion, false))
                {
                    // First allowed version found, now we have to test that the min value
                    // also allows this version.
                    if (minVersion == null || compareVersions(javaVersion, minVersion, true))
                    {
                        String cv = JDK_ROOT_KEY + "\\" + keys[i];
                        String path = registryHandler.getValue(cv, JDK_VALUE_NAME).getStringData();
                        // Use it only if the path is valid.
                        // Set the path for method JDKPathPanelHelper.pathIsValid ...
                        if (JDKPathPanelHelper.pathIsValid(path))
                        {
                            javaHome = path;
                            break;
                        }
                    }
                }
                i--;
            }
        }
        catch (Exception e)
        { // Will only be happen if registry handler is good, but an
            // exception at performing was thrown. This is an error...
            e.printStackTrace();
        }
        finally
        {
            if (registryHandler != null && oldVal != 0)
            {
                try
                {
                    registryHandler.setRoot(MSWinConstants.HKEY_LOCAL_MACHINE);
                }
                catch (NativeLibException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return javaHome;
    }

    /**
     * Returns whether the chosen path is true or not. If existFiles are not null, the existence of
     * it under the chosen path are detected. This method can be also implemented in derived
     * classes to handle special verification of the path.
     *
     * @return true if existFiles are exist or not defined, else false
     */
    private static boolean pathIsValid(String strPath)
    {
        for (String existFile : testFiles)
        {
            File path = new File(strPath, existFile).getAbsoluteFile();
            if (!path.exists())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Validate that the given javaVersion meets meets the minimum and maximum java version requirements.
     *
     * @param javaVersion
     * @return
     */
    private static boolean verifyVersion(String javaVersion)
    {
        boolean valid = true;

        // No min and max, version always ok.
        if (minVersion == null && maxVersion == null)
        {
            return true;
        }

        if (minVersion != null)
        {
            if (!compareVersions(javaVersion, minVersion, true))
            {
                valid = false;
            }
        }
        if (maxVersion != null)
        {
            if (!compareVersions(javaVersion, maxVersion, false))
            {
                valid = false;
            }
        }
        return valid;
    }

    /**
     * Run the java binary from the JAVA_HOME directory specified from the user.
     * We do this to figure out what version of java the user has specified
     *
     * @param path JAVA_HOME
     * @param platform specifies which platform user is running installation on
     * @return string representation of the java version
     */
    public static String getCurrentJavaVersion(String path, Platform platform)
    {
        String[] params;
        if (platform.isA(Platform.Name.WINDOWS))
        {
            params = new String[]{
                    "cmd",
                    "/c",
                    path + File.separator + "bin" + File.separator + "java",
                    "-version"
            };
        }
        else
        {
            params = new String[]{
                    path + File.separator + "bin" + File.separator + "java",
                    "-version"
            };
        }

        String[] output = new String[2];
        FileExecutor fe = new FileExecutor();
        fe.executeCommand(params, output);

        // Get version information from stdout or stderr, may vary across machines
        String versionInformation = (output[0].length() > 0) ? output[0] : output[1];
        return extractJavaVersion(versionInformation);
    }

    /**
     * Given a 'dirty' string representing the javaVersion.
     * Extract the actual java version and strip away any extra information.
     *
     * @param javaVersion
     * @return
     */
     public static String extractJavaVersion(String javaVersion)
     {
         //Were originally parameters
         int assumedPlace = 4;
         int halfRange = 4;
         String useNotIdentifier = "__NO_NOT_IDENTIFIER_";

         StringTokenizer tokenizer = new StringTokenizer(javaVersion, " \t\n\r\f\"");
         int i;
         int currentRange = 0;
         String[] interestedEntries = new String[halfRange + halfRange];
         for (i = 0; i < assumedPlace - halfRange; ++i)
         {
             if (tokenizer.hasMoreTokens())
             {
                 tokenizer.nextToken(); // Forget this entries.
             }
         }

         for (i = 0; i < halfRange + halfRange; ++i)
         { // Put the interesting Strings into an intermediaer array.
             if (tokenizer.hasMoreTokens())
             {
                 interestedEntries[i] = tokenizer.nextToken();
                 currentRange++;
             }
         }

         for (i = 0; i < currentRange; ++i)
         {
             if (useNotIdentifier != null && interestedEntries[i].contains(useNotIdentifier))
             {
                 continue;
             }
             if (Character.getType(interestedEntries[i].charAt(0)) != Character.DECIMAL_DIGIT_NUMBER)
             {
                 continue;
             }
             break;
         }
         if (i == currentRange)
         {
             return "<not found>";
         }
         return interestedEntries[i];
     }

    /**
     * Validate that the given javaVersion meets meets the minimum and maximum java version requirements.
     *
     * @param currentVersion
     * @param template
     * @param isMin
     * @return
     */
     private static boolean compareVersions(String currentVersion, String template, boolean isMin)
     {
        StringTokenizer currentTokenizer = new StringTokenizer(currentVersion, "._-");
        StringTokenizer neededTokenizer = new StringTokenizer(template, "._-");
        while (neededTokenizer.hasMoreTokens())
        {
            // Current can have no more tokens if needed has more
            // and if a privious token was not accepted as good version.
            // e.g. 1.4.2_02 needed, 1.4.2 current. The false return
            // will be right here. Only if e.g. needed is 1.4.2_00 the
            // return value will be false, but zero should not b e used
            // at the last version part.
            if (!currentTokenizer.hasMoreTokens())
            {
                return (false);
            }
            String current = currentTokenizer.nextToken();
            String needed = neededTokenizer.nextToken();
            int currentValue;
            int neededValue;
            try
            {
                currentValue = Integer.parseInt(current);
                neededValue = Integer.parseInt(needed);
            }
            catch (NumberFormatException nfe)
            { // A number format exception will be raised if
                // there is a non numeric part in the version,
                // e.g. 1.5.0_beta. The verification runs only into
                // this deep area of version number (fourth sub place)
                // if all other are equal to the given limit. Then
                // it is right to return false because e.g.
                // the minimal needed version will be 1.5.0.2.
                return (false);
            }
            if (currentValue < neededValue)
            {
                if (isMin)
                {
                    return false;
                }
                return true;
            }
            else if (currentValue > neededValue)
            {
                if (isMin)
                {
                    return true;
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Central validation of java path.
     * Also gives allowance to central strings.
     *
     * @param javaHome JAVA_HOME path to test
     * @param javaVersion the java version being queried
     * @param messages available messages
     * @return error message if validation failed, otherwise an empty string
     */
    public static String validate(String javaHome, String javaVersion, Messages messages)
    {
        StringBuilder message = new StringBuilder();

        if(!pathIsValid(javaHome))
        {
            message.append(messages.get("PathInputPanel.notValid"));
        }
        else if (!verifyVersion(javaVersion))
        {
            message.append(messages.get("JDKPathPanel.badVersion1"))
                    .append(javaVersion)
                    .append(messages.get("JDKPathPanel.badVersion2"));
            if (minVersion != null && maxVersion != null)
            {
                message.append(minVersion).append(" - ").append(maxVersion);
            }
            else if (minVersion != null)
            {
                message.append(" >= ").append(minVersion);
            }
            else if (maxVersion != null)
            {
                message.append(" <= ").append(maxVersion);
            }
            message.append(messages.get("JDKPathPanel.badVersion3"));
        }

        return message.toString();
    }
    /**
     * Check if JDK panel should be skipped.
     * Return true if panel should be skipped otherwise false.
     *
     * @param installData
     * @param path
     * @return
     */
    public static boolean skipPanel(InstallData installData, String path)
    {
        String skipIfValid = installData.getVariable("JDKPathPanel.skipIfValid");

        if (pathIsValid(path) && skipIfValid != null &&
                ("yes".equalsIgnoreCase(skipIfValid) || "true".equalsIgnoreCase(skipIfValid)))
        {
            installData.setVariable(JDK_PATH, path);
            return true;
        }
        return false;
    }
}

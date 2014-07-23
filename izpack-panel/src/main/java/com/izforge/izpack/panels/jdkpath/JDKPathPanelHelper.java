package com.izforge.izpack.panels.jdkpath;

import com.coi.tools.os.win.MSWinConstants;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.Platform;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class JDKPathPanelHelper
{

    /**
     * Returns the path to the needed JDK if found in the registry. If there are more than one JDKs
     * registered, that one with the highest allowed version will be returned. Works only on windows.
     * On Unix an empty string returns.
     *
     * @return the path to the needed JDK if found in the windows registry
     */
    public static String getJavaHomeFromRegistry(RegistryDefaultHandler handler, String min, String max)
    {
        String javaHome = "";
        int oldVal = 0;
        RegistryHandler registryHandler = null;
        Set<String> badRegEntries = new HashSet<String>();
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
            String[] keys = registryHandler.getSubkeys(JDKPathPanel.JDK_ROOT_KEY);
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
                if (max == null || compareVersions(javaVersion, max, false))
                {
                    // First allowed version found, now we have to test that the min value
                    // also allows this version.
                    if (min == null || compareVersions(javaVersion, min, true))
                    {
                        String cv = JDKPathPanel.JDK_ROOT_KEY + "\\" + keys[i];
                        String path = registryHandler.getValue(cv, JDKPathPanel.JDK_VALUE_NAME).getStringData();
                        // Use it only if the path is valid.
                        // Set the path for method JDKPathPanelHelper.pathIsValid ...
                        if (!JDKPathPanelHelper.pathIsValid(path))
                        {
                            badRegEntries.add(keys[i]);
                        }
                        else
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
    public static boolean pathIsValid(String strPath)
    {
        for (String existFile : JDKPathPanel.testFiles)
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
     * @param min
     * @param max
     * @return
     */
    public static boolean verifyVersion(String javaVersion, String min, String max)
    {
        boolean valid = true;

        // No min and max, version always ok.
        if (min == null && max == null)
        {
            return true;
        }

        if (min != null)
        {
            if (!compareVersions(javaVersion, min, true))
            {
                valid = false;
            }
        }
        if (max != null)
        {
            if (!compareVersions(javaVersion, max, false))
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
}

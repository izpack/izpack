package com.izforge.izpack.panels.jdkpath;

import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.Platform;

import java.io.File;
import java.util.StringTokenizer;

public class JDKPathPanelHelper
{
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

    public static boolean verifyVersion(String vs, String min, String max)
    {
        boolean retval = true;
        // No min and max, version always ok.
        if (min == null && max == null)
        {
            return (true);
        }

        if (min != null)
        {
            if (!compareVersions(vs, min, true))
            {
                retval = false;
            }
        }
        if (max != null)
        {
            if (!compareVersions(vs, max, false))
            {
                retval = false;
            }
        }
        return retval;
    }

    public static String getCurrentJavaVersion(String path, Platform platform)
    {
        // Now get the version ...
        // We cannot look to the version of this vm because we should
        // test the given JDK VM.
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

        // "My" VM writes the version on stderr :-(
        String vs = (output[0].length() > 0) ? output[0] : output[1];
        String javaVersion = getCurrentJavaVersion(vs, 4, 4, "__NO_NOT_IDENTIFIER_");
        return javaVersion;
    }

     public static String getCurrentJavaVersion(String in, int assumedPlace, int halfRange, String useNotIdentifier)
     {
         StringTokenizer tokenizer = new StringTokenizer(in, " \t\n\r\f\"");
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

     public static boolean compareVersions(String currentVersion, String template, boolean isMin)
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
                    return (false);
                }
                return (true);
            }
            if (currentValue > neededValue)
            {
                if (isMin)
                {
                    return (true);
                }
                return (false);
            }
        }
        return (true);
    }
}

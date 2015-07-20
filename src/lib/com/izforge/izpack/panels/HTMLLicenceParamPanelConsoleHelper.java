package com.izforge.izpack.panels;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.StringTokenizer;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
import com.izforge.izpack.installer.ResourceManager;


public class HTMLLicenceParamPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole
{
    
    public static final String strLicenceId = "licenceid";
    
    public boolean runGeneratePropertiesFile(AutomatedInstallData installData,PrintWriter printWriter)
    {
        return true;
    }

    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p)
    {
        return true;
    }

    public boolean runConsole(AutomatedInstallData idata)
    {

        String license = null;
        String resNamePrefix = idata.getVariable(strLicenceId);
        try
        {
            // We read it
             license = ResourceManager.getInstance().getTextResource(resNamePrefix);
        }
        catch (Exception err)
        {
            license = "Error : could not load the licence text for defined resource " + resNamePrefix;
            System.out.println(license);
            return false;
        }
        
        // controls # of lines to display at a time, to allow simulated scrolling down
        int lines=25;
        int l = 0;

        
        String strippedHTML = this.removeHTML(license);
        
        StringTokenizer st = new StringTokenizer(strippedHTML, "\n");
        while (st.hasMoreTokens())
        {
             String token = st.nextToken();
             System.out.println(token);
             l++;
             if (l >= lines) {
                 if (! doContinue()) {
                     return false;
                 }
                 l=0;
             }             

        }
   
        int i = askToAcceptLicense();

        if (i == 1)
        {
            return true;
        }
        else if (i == 2)
        {
            return false;
        }
        else
        {
            return runConsole(idata);
        }

    }
    
    private boolean doContinue()
    {
        try
        {
            System.out.println("\r");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true)
            {
                System.out.println("press Enter to continue, X to exit");
                String strIn = br.readLine();
                if (strIn.equalsIgnoreCase("x"))
                {
                    return false;
                }

                return true;
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }
    
    private int askToAcceptLicense()
    {
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true)
            {
                System.out.println("press 1 to accept, 2 to reject, 3 to redisplay");
                String strIn = br.readLine();
                if (strIn.equals("1"))
                {
                    return 1;
                }
                else if (strIn.equals("2"))
                {
                    return 2;
                }
                else if (strIn.equals("3")) { return 3; }
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return 2;
    }
    
    private String removeHTML(String source)
    {
        String result = "";
        try
        {
            // chose to keep newline (\n) instead of carriage return (\r) for line breaks.

             // Replace line breaks with space
            result = source.replaceAll("\r", " ");
            // Remove step-formatting
            result = result.replaceAll("\t", "");
            // Remove repeating spaces because browsers ignore them
            
            result = result.replaceAll("( )+", " ");

            
            result = result.replaceAll("<( )*head([^>])*>","<head>");
            result = result.replaceAll("(<( )*(/)( )*head( )*>)","</head>");
            result = result.replaceAll("(<head>).*(</head>)", "");
            result = result.replaceAll("<( )*script([^>])*>","<script>");
            result = result.replaceAll("(<( )*(/)( )*script( )*>)","</script>");
            result = result.replaceAll("(<script>).*(</script>)","");
            
         // remove all styles (prepare first by clearing attributes)
            result = result.replaceAll("<( )*style([^>])*>","<style>");
            result = result.replaceAll("(<( )*(/)( )*style( )*>)","</style>");
            result = result.replaceAll("(<style>).*(</style>)","");
            
            result = result.replaceAll("(<( )*(/)( )*sup( )*>)","</sup>");
            result = result.replaceAll("<( )*sup([^>])*>","<sup>");
            result = result.replaceAll("(<sup>).*(</sup>)", "");
            
         // insert tabs in spaces of <td> tags
            result = result.replaceAll("<( )*td([^>])*>","\t");
            
         // insert line breaks in places of <BR> and <LI> tags
            result = result.replaceAll("<( )*br( )*>","\r");
            result = result.replaceAll("<( )*li( )*>","\r");
            
            // insert line paragraphs (double line breaks) in place
            // if <P>, <DIV> and <TR> tags
            result = result.replaceAll("<( )*div([^>])*>","\r\r");
            result = result.replaceAll("<( )*tr([^>])*>","\r\r");
            
            result = result.replaceAll("(<) h (\\w+) >","\r");
            result = result.replaceAll("(\\b) (</) h (\\w+) (>) (\\b)","");
            result = result.replaceAll("<( )*p([^>])*>","\r\r");
            
            // Remove remaining tags like <a>, links, images,
            // comments etc - anything that's enclosed inside < >
            result = result.replaceAll("<[^>]*>","");
            
            
            result = result.replaceAll("&bull;"," * ");
            result = result.replaceAll("&lsaquo;","<");
            result = result.replaceAll("&rsaquo;",">");
            result = result.replaceAll("&trade;","(tm)");
            result = result.replaceAll("&frasl;","/");
            result = result.replaceAll("&lt;","<");
            result = result.replaceAll("&gt;",">");

            result = result.replaceAll("&copy;","(c)");
            result = result.replaceAll("&reg;","(r)");
            result = result.replaceAll("&(.{2,6});","");
 
            // Remove extra line breaks and tabs:
            // replace over 2 breaks with 2 and over 4 tabs with 4.
            // Prepare first to remove any whitespaces in between
            // the escaped characters and remove redundant tabs in between line breaks
            result = result.replaceAll("(\r)( )+(\r)","\r\r");
            result = result.replaceAll("(\t)( )+(\t)","\t\t");
            result = result.replaceAll("(\t)( )+(\r)","\t\r");
            result = result.replaceAll("(\r)( )+(\t)","\r\t");
            result = result.replaceAll("(\r)(\t)+(\\r)","\r\r");
            result = result.replaceAll("(\r)(\t)+","\r\t");
 
        } catch (Exception e)
        {
            e.printStackTrace();
           
        }
        
        return result;
    }
    

}

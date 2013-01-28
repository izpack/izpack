/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2005 Klaus Bartz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.util;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.IzPanel;

import java.util.Iterator;
import java.util.Map;

/**
 * A helper class which creates a summary from all panels. This class calls all declared panels for
 * a summary To differ between caption and message, HTML is used to draw caption in bold and indent
 * messaged a little bit.
 *
 * @author Klaus Bartz
 */
public class SummaryProcessor
{

    private static String HTML_HEADER;

    private static String HTML_FOOTER = "</body>\n</html>\n";

    private static String BODY_START = "<div class=\"body\">";

    private static String BODY_END = "</div>";

    private static String HEAD_START = "<h1>";

    private static String HEAD_END = "</h1>\n";

    static
    {
        // Initialize HTML header and footer.
        StringBuffer sb = new StringBuffer(256);
        sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n").append(
                "<html>\n" + "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">" +
                        "<head>\n<STYLE TYPE=\"text/css\" media=screen,print>\n").append(
                "h1{\n  font-size: 100%;\n  margin: 1em 0 0 0;\n  padding: 0;\n}\n").append(
                "div.body {\n  font-size: 100%;\n  margin: 0mm 2mm 0  8mm;\n  padding: 0;\n}\n")
                .append("</STYLE>\n</head>\n<body>\n");
        HTML_HEADER = sb.toString();
    }

    /**
     * Returns a HTML formated string which contains the summary of all panels. To get the summary,
     * the methods * {@link IzPanel#getSummaryCaption} and {@link IzPanel#getSummaryBody()} of all
     * panels are called.
     *
     * @param idata AutomatedInstallData which contains the panel references
     * @return a HTML formated string with the summary of all panels
     */
    public static String getSummary(AutomatedInstallData idata)
    {
        Iterator<IzPanel> iter = idata.panels.iterator();
        StringBuffer sb = new StringBuffer(2048);
        sb.append(HTML_HEADER);
        while (iter.hasNext())
        {
            IzPanel panel = iter.next();
            String caption = panel.getSummaryCaption();
            String msg = panel.getSummaryBody();
            // If no caption or/and message, ignore it.
            if (caption == null || msg == null)
            {
                continue;
            }
            sb.append(HEAD_START).append(caption).append(HEAD_END);
            sb.append(BODY_START).append(msg).append(BODY_END);
        }
        sb.append(HTML_FOOTER);
        return (sb.toString());
    }

    public static void processSummaryText(AutomatedInstallData idata)
    {
        for (Map.Entry<String, java.util.List> entry : idata.summaryText.entrySet())
        {
            System.out.println(" ");
            System.out.println(entry.getKey() + " :");
            System.out.println(" ");
            Iterator iter = entry.getValue().iterator();
            while (iter.hasNext())
            {
                System.out.println((String) iter.next());
            }
        }

    }
    
    
}

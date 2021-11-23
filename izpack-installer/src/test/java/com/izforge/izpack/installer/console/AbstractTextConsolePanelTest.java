/*
 * IzPack - Copyright 2021 Hitesh A. Bosamiya, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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
package com.izforge.izpack.installer.console;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link AbstractTextConsolePanel} class.
 *
 * @author Hitesh A. Bosamiya
 */
public class AbstractTextConsolePanelTest
{
    private AbstractTextConsolePanel abstractTextConsolePanel;

    @Before
    public void setup()
    {
        abstractTextConsolePanel = new AbstractTextConsolePanel(null)
        {

            @Override
            protected String getText()
            {
                return null;
            }
        };
    }

    @Test
    public void testRemoveHTML()
    {
        String html = "<  html   >\n" +
                "<head>\n" +
                "    \t<title>  \tWelcome   </title>   \n" +
                "    <style>\n" +
                "        * { font-family: Calibri, Candara, Segoe, \"Segoe UI\", Optima, Arial, sans-serif; }\n" +
                "        h1 { font-size: 20pt; }\n" +
                "        pan.text { font-size: 14pt; }\n" +
                "        span.footer { display: block; font-size: 12pt; }\n" +
                "        body { background-color: #EDEDED; }\n" +
                "    </style>\n" +
                "</head  >\n" +
                "<body>\n" +
                "<table width=\"100%\" cellpadding=10 border=0>\n" +
                "<tr>\n" +
                "<td align=\"right\" valign=\"top\">\n" +
                "<h1>Welcome to IzPack</h1>\n" +
                "</td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "</body>\n" +
                "</html>\n";
        String expected =  "Welcome\n\nWelcome to IzPack";

        assertEquals(expected, abstractTextConsolePanel.removeHTML(html));
    }

    @Test
    public void testRemoveHTMLForNull()
    {
        assertEquals("", abstractTextConsolePanel.removeHTML(null));
    }
}

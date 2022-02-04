/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.panels.htmlinfo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.text.Document;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.installer.util.PanelHelper;
import com.izforge.izpack.util.HyperlinkHandler;

/**
 * The HTML info panel.
 *
 * @author Julien Ponge
 */
public class HTMLInfoPanel extends IzPanel
{
    private static final long serialVersionUID = 3257008769514025270L;

    /**
     * Resource name for panel content.
     */
    private final String panelResourceName;

    /**
     * The text area.
     */
    private JEditorPane textArea;

    /**
     * Constructs an <tt>HTMLInfoPanel</tt>.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent window
     * @param installData the installation data
     * @param resources   the resources
     * @param log         the log
     */
    public HTMLInfoPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources, Log log)
    {
        this(panel, parent, installData, true, resources, log);
    }

    /**
     * Constructs an <tt>HTMLInfoPanel</tt>.
     *
     * @param panel             the panel  meta-data
     * @param parent            the parent window
     * @param installData       the installation data
     * @param showInfoLabelFlag true to show "please read..." label above content
     * @param resources         the resources
     * @param log               the log
     */
    public HTMLInfoPanel(Panel panel, InstallerFrame parent, GUIInstallData installData,
                         boolean showInfoLabelFlag, Resources resources, Log log)
    {
        super(panel, parent, installData, new IzPanelLayout(log), resources);
        panelResourceName = PanelHelper.getPanelResourceName(panel, "info", resources);

        // We add the components
        if (showInfoLabelFlag)
        {  //flag is set; add label above content
            add(LabelFactory.create(getString(panelResourceName), parent.getIcons().get("edit"), LEADING), NEXT_LINE);
        }
        try
        {
            textArea = new JEditorPane()
            {       //override get-stream method to parse variable
                private static final long serialVersionUID = 1L;

                // declarations in HTML content:
                protected InputStream getStream(URL urlObj)
                        throws IOException
                {                  //get original stream contents:
                    final InputStream inStm = super.getStream(urlObj);
                    final ByteArrayOutputStream btArrOutStm =
                            new ByteArrayOutputStream();
                    int b;         //copy contents to output stream:
                    final byte[] buff = new byte[2048];
                    while ((b = inStm.read(buff, 0, buff.length)) > 0)
                    {
                        btArrOutStm.write(buff, 0, b);
                    }
                    //convert to string and parse variables:
                    final String parsedStr =
                            parseText(btArrOutStm.toString());
                    //return input stream to parsed string:
                    return new ByteArrayInputStream(
                            parsedStr.getBytes());
                }
            };
            textArea.setName(GuiId.HTML_INFO_PANEL_TEXT.id);
            textArea.setContentType("text/html; charset=utf-8");
            textArea.setEditable(false);
            textArea.addHyperlinkListener(new HyperlinkHandler());
            JScrollPane scroller = new JScrollPane(textArea);
            //set caret so beginning of file is displayed:
            textArea.setCaretPosition(0);
            add(scroller, NEXT_LINE);
        }
        catch (Exception err)
        {
            err.printStackTrace();
        }
        getLayoutHelper().completeLayout();
    }

    /*
     * loads the content of the info resource as text so that it can be parsed afterwards
     */
    private URL loadHTMLInfoContent()
    {
        try
        {
            return getResources().getURL(panelResourceName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Indicates whether the panel has been validated or not.
     *
     * @return Always true.
     */
    public boolean isValidated()
    {
        return true;
    }

    public void panelActivate()
    {
        // Clear this property to get the document to reload and perform variable substitution.
        // See JEditorPane.setPage javadoc.
        textArea.getDocument().putProperty(Document.StreamDescriptionProperty, null);
        try
        {
            textArea.setPage(loadHTMLInfoContent());
            textArea.setCaretPosition(0);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}


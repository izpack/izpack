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

package com.izforge.izpack.panels.summary;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.installer.util.SummaryProcessor;

/**
 * Summary panel to use before InstallPanel. This panel calls the {@link SummaryProcessor} which
 * calls all declared panels for a summary and shows the given captiond and messaged in a
 * <code>JEditorPane</code>.
 *
 * @author Klaus Bartz
 */
public class SummaryPanel extends IzPanel
{
    private static final long serialVersionUID = 3832626166401282361L;

    /**
     * The text area.
     */
    private JEditorPane textArea;

    /**
     * Constructs a <tt>SummaryPanel</tt>.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent window
     * @param installData the installation data
     * @param resources   the resources
     * @param log         the log
     */
    public SummaryPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources, Log log)
    {
        super(panel, parent, installData, new IzPanelLayout(log), resources);
        add(LabelFactory.createMultiLineLabel(getString("SummaryPanel.info")));
        try
        {
            textArea = new JEditorPane();
            textArea.setContentType("text/html");
            textArea.setEditable(false);
            JScrollPane scroller = new JScrollPane(textArea);
            add(scroller, NEXT_LINE);
        }
        catch (Exception err)
        {
            err.printStackTrace();
        }
        getLayoutHelper().completeLayout();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.izforge.izpack.installer.IzPanel#panelActivate()
     */

    public void panelActivate()
    {
        super.panelActivate();
        String nextText = getString("SummaryPanel.next");
        if (nextText == null)
        {
            nextText = getString("installer.next");
        }
        parent.getNavigator().setNextText(nextText);
        textArea.setText(SummaryProcessor.getSummary(this.installData));
        textArea.setCaretPosition(0);
    }

    public void panelDeactivate()
    {
        super.panelDeactivate();
        parent.getNavigator().setNextText(getString("installer.next"));
    }
}

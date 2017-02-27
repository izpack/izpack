/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Jan Blok
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
package com.izforge.izpack.panels.hello;

import java.awt.LayoutManager2;
import java.util.ArrayList;

import javax.swing.JLabel;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.LayoutConstants;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;

/**
 * The Hello panel class.
 *
 * @author Julien Ponge
 */
public class HelloPanel extends IzPanel
{

    /**
     * Serialization version identifier.
     */
    private static final long serialVersionUID = 3257848774955905587L;

    /**
     * Constructs an <tt>HelloPanel</tt>.
     *
     * @param panel the panel meta-data
     * @param parent the parent window
     * @param installData the installation data
     * @param resources the resources
     * @param log the log
     */
    public HelloPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources, Log log)
    {
        this(panel, parent, installData, new IzPanelLayout(log), resources);
    }

    /**
     * Constructs an <tt>HelloPanel</tt> with the given layout manager.
     * <p/>
     * Valid layout manager are the {@link IzPanelLayout} and
     * <tt>GridBagLayout</tt>. New panels should be use IzPanelLayout. If
     * layoutManager is null, no layout manager will be created or initialized.
     *
     * @param panel the panel meta-data
     * @param parent The parent IzPack installer frame.
     * @param installData The installer internal installDataGUI.
     * @param layout layout manager to be used with this IzPanel
     */
    public HelloPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, LayoutManager2 layout,
            Resources resources)
    {
        // Layout handling. This panel was changed from a mixed layout handling
        // with GridBagLayout and BoxLayout to IzPanelLayout. It can be used as an
        // example how to use the IzPanelLayout. For this there are some comments
        // which are excrescent for a "normal" panel.
        // Set a IzPanelLayout as layout for this panel.
        // This have to be the first line during layout if IzPanelLayout will be used.
        super(panel, parent, installData, layout, resources);
        // We create and put the labels
        String welcomeText = getString("HelloPanel.welcome1") + installData.getInfo().getAppName() + " "
                + installData.getInfo().getAppVersion() + getString("HelloPanel.welcome2");
        JLabel welcomeLabel = LabelFactory.create(welcomeText, parent.getIcons().get("host"), LEADING);
        welcomeLabel.setName(GuiId.HELLO_PANEL_LABEL.id);
        // IzPanelLayout is a constraint orientated layout manager. But if no constraint is
        // given, a default will be used. It starts in the first line.
        // NEXT_LINE have to insert also in the first line!!
        add(welcomeLabel, NEXT_LINE);
        // Yes, there exist also a strut for the IzPanelLayout.
        // But the strut will be only used for one cell. A vertical strut will be use
        // NEXT_ROW, a horizontal NEXT_COLUMN. For more information see the java doc.
        // add(IzPanelLayout.createVerticalStrut(20));
        // But for a strut you have to define a fixed height. Alternative it is possible
        // to create a paragraph gap which is configurable.
        add(IzPanelLayout.createParagraphGap());

        ArrayList<Info.Author> authors = installData.getInfo().getAuthors();

        if (!authors.isEmpty())
        {
            String authorText = getString("HelloPanel.authors");
            JLabel appAuthorsLabel = LabelFactory.create(authorText, parent.getIcons()
                    .get("information"), LEADING);
            // If nothing will be sad to the IzPanelLayout the position of an add will be
            // determined in the default constraint. For labels it is CURRENT_ROW, NEXT_COLUMN.
            // But at this point we would place the label in the next row. It is possible
            // to create an IzPanelConstraint with this options, but it is also possible to
            // use simple the NEXT_LINE object as constraint. Attention!! Do not use
            // LayoutConstants.NEXT_ROW else LayoutConstants.NEXT_LINE because NEXT_ROW is an
            // int and with it an other add method will be used without any warning (there the
            // parameter will be used as position of the component in the panel, not the
            // layout manager.
            add(appAuthorsLabel, LayoutConstants.NEXT_LINE);

            JLabel label;
            for (Info.Author author : authors)
            {
                String email = (author.getEmail() != null && author.getEmail().length() > 0) ? (" <"
                        + author.getEmail() + ">") : "";
                label = LabelFactory.create(" - " + author.getName() + email, parent.getIcons()
                        .get("empty"), LEADING);
                add(label, NEXT_LINE);
            }
            add(IzPanelLayout.createParagraphGap());
        }

        if (installData.getInfo().getAppURL() != null)
        {
            String urlText = getString("HelloPanel.url") + installData.getInfo().getAppURL();
            JLabel appURLLabel = LabelFactory.create(urlText, parent.getIcons().get("bookmark"), LEADING);
            add(appURLLabel, LayoutConstants.NEXT_LINE);
        }
        // At end of layouting we should call the completeLayout method also they do nothing.
        getLayoutHelper().completeLayout();
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return Always true.
     */
    public boolean isValidated()
    {
        return true;
    }
}

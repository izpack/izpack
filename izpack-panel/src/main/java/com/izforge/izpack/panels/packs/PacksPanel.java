/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Marcus Wolschon
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

package com.izforge.izpack.panels.packs;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.panels.treepacks.PackValidator;
import com.izforge.izpack.util.IoHelper;

/**
 * The packs selection panel class. This class handles only the layout. Common stuff are handled by
 * the base class.
 *
 * @author Julien Ponge
 * @author Jan Blok
 * @author Klaus Bartz
 */
public class PacksPanel extends PacksPanelBase
{

    /**
     *
     */
    private static final long serialVersionUID = 4051327842505668403L;

    /**
     * Constructs a <tt>PacksPanel</tt>.
     *
     * @param panel       the panel meta-data
     * @param parent      fhe parent window
     * @param installData the installation data
     * @param resources   the resources
     * @param factory     the factory for creating {@link PackValidator} instances
     * @param rules       the rules engine
     */
    public PacksPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
                      ObjectFactory factory, RulesEngine rules)
    {
        super(panel, parent, installData, resources, factory, rules);
    }

    @Override
    protected void createNormalLayout()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        createLabel("PacksPanel.info", "preferences", null, null);
        add(Box.createRigidArea(new Dimension(0, 3)));
        createLabel("PacksPanel.tip", "tip", null, null);
        add(Box.createRigidArea(new Dimension(0, 5)));
        tableScroller = new JScrollPane();
        packsTable = createPacksTable(300, tableScroller, null, null);
        if (dependenciesExist)
        {
            dependencyArea = createTextArea("PacksPanel.dependencyList", null, null, null);
        }
        descriptionArea = createTextArea("PacksPanel.description", null, null, null);
        spaceLabel = createPanelWithLabel("PacksPanel.space", null, null);

        if (IoHelper.supported("getFreeSpace"))
        {
            add(Box.createRigidArea(new Dimension(0, 3)));
            freeSpaceLabel = createPanelWithLabel("PacksPanel.freespace", null, null);
        }
    }

}

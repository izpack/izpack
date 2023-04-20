/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2004 Volker Friedritz
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

package com.izforge.izpack.panels.imgpacks;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;

import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.LayoutHelper;
import com.izforge.izpack.panels.packs.PacksPanelBase;
import com.izforge.izpack.panels.packs.PackValidator;
import com.izforge.izpack.util.IoHelper;

/**
 * The ImgPacks panel class. Allows the packages selection with a small picture displayed for every
 * pack. This new version combines the old PacksPanel and the ImgPacksPanel so that the positive
 * characteristics of both are combined. This class handles only the layout and some related stuff.
 * Common stuff are handled by the base class.
 *
 * @author Julien Ponge
 * @author Volker Friedritz
 * @author Klaus Bartz
 */
public class ImgPacksPanel extends PacksPanelBase
{

    /**
     *
     */
    private static final long serialVersionUID = 3977858492633659444L;

    /**
     * The images to display.
     */
    private HashMap<String, ImageIcon> images;

    /**
     * The img label.
     */
    private JLabel imgLabel;

    /**
     * Constructs a <tt>ImgPacksPanel</tt>.
     *
     * @param panel       the panel meta-data
     * @param parent      fhe parent window
     * @param installData the installation data
     * @param resources   the resources
     * @param factory     the factory for creating {@link PackValidator} instances
     */
    public ImgPacksPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
                         ObjectFactory factory)
    {
        super(panel, parent, installData, resources, factory);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.izforge.izpack.panels.packs.PacksPanelBase#createNormalLayout()
     */

    protected void createNormalLayout()
    {
        preLoadImages();
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gbConstraints = new GridBagConstraints();
        setLayout(layout);

        // Create constraint for first component as standard constraint.
        LayoutHelper.buildConstraints(gbConstraints, 0, 0, 1, 1, 0.25, 0.0);
        gbConstraints.insets = new Insets(5, 5, 5, 5);
        gbConstraints.anchor = GridBagConstraints.WEST;
        // Create the info label.
        createLabel("PacksPanel.info", "preferences", layout, gbConstraints);

        // Create the snap label.
        LayoutHelper.buildConstraints(gbConstraints, 1, 0, 1, 1, 0.50, 0.0);
        createLabel("ImgPacksPanel.snap", "tip", layout, gbConstraints);

        // Create packs table with a scroller.
        tableScroller = new JScrollPane();
        LayoutHelper.buildConstraints(gbConstraints, 0, 1, 1, 2, 0.50, 0.0);
        gbConstraints.fill = GridBagConstraints.BOTH;
        packsTable = createPacksTable(250, tableScroller, layout, gbConstraints);

        // Create the image label with a scroller.
        // Use the image of the first pack having an image as initial image
        Iterator<Pack> pack_it = this.installData.getAvailablePacks().iterator();
        Pack firstImgPack = null;
        boolean imgFound = false;
        while (!imgFound && pack_it.hasNext())
        {
            firstImgPack = pack_it.next();
            imgFound = firstImgPack.getImageId() != null;
        }
        if (imgFound)
        {
            imgLabel = new JLabel(images.get(firstImgPack.getImageId()));
        }
        else
        {
            imgLabel = new JLabel();
        }
        JScrollPane imgScroller = new JScrollPane(imgLabel);
        imgScroller.setPreferredSize(getPreferredSizeFromImages());
        LayoutHelper.buildConstraints(gbConstraints, 1, 1, 1, 1, 0.5, 1.0);
        layout.addLayoutComponent(imgScroller, gbConstraints);
        add(imgScroller);

        // Create a vertical strut.

        Component strut = Box.createVerticalStrut(20);
        LayoutHelper.buildConstraints(gbConstraints, 1, 2, 1, 3, 0.0, 0.0);
        layout.addLayoutComponent(strut, gbConstraints);
        add(strut);

        // Create the dependency area with a scroller.
        if (dependenciesExist)
        {
            JScrollPane depScroller = new JScrollPane();
            depScroller.setPreferredSize(new Dimension(250, 40));
            LayoutHelper.buildConstraints(gbConstraints, 0, 3, 1, 1, 0.50, 0.50);
            dependencyArea = createTextArea("ImgPacksPanel.dependencyList", depScroller, layout,
                                            gbConstraints);
        }

        // Create the description area with a scroller.
        JScrollPane descriptionScroller = new JScrollPane();
        descriptionScroller.setPreferredSize(new Dimension(200, 60));
        descriptionScroller.setBorder(BorderFactory.createEmptyBorder());

        LayoutHelper.buildConstraints(gbConstraints, 1, 3, 1, 1, 0.50, 0.50);
        descriptionArea = createTextArea("PacksPanel.description", descriptionScroller, layout,
                                         gbConstraints);
        // Create the tip label.
        LayoutHelper.buildConstraints(gbConstraints, 0, 4, 2, 1, 0.0, 0.0);
        createLabel("PacksPanel.tip", "tip", layout, gbConstraints);
        // Create the space label.
        LayoutHelper.buildConstraints(gbConstraints, 0, 5, 2, 1, 0.0, 0.0);
        spaceLabel = createPanelWithLabel("PacksPanel.space", layout, gbConstraints);
        if (IoHelper.supported("getFreeSpace"))
        { // Create the free space label only if free space is supported.
            LayoutHelper.buildConstraints(gbConstraints, 0, 6, 2, 1, 0.0, 0.0);
            freeSpaceLabel = createPanelWithLabel("PacksPanel.freespace", layout, gbConstraints);
        }

    }

    /**
     * Pre-loads the images.
     */
    private void preLoadImages()
    {
        int size = this.installData.getAvailablePacks().size();
        images = new HashMap<String, ImageIcon>(size);
        Resources resources = getResources();
        for (Pack pack : this.installData.getAvailablePacks())
        {
            String imageId = pack.getImageId();
            if (imageId != null)
            {
                try
                {
                    ImageIcon img = resources.getImageIcon(imageId);
                    images.put(imageId, img);
                }
                catch (Exception err)
                {
                    err.printStackTrace();
                }
            }
        }
    }

    /**
     * Try to find a good preferredSize for imgScroller by checking all loaded images' width and
     * height.
     */
    private Dimension getPreferredSizeFromImages()
    {
        int maxWidth = 80;
        int maxHeight = 60;
        ImageIcon icon;

        for (ImageIcon imageIcon : images.values())
        {
            icon = imageIcon;
            maxWidth = Math.max(maxWidth, icon.getIconWidth());
            maxHeight = Math.max(maxHeight, icon.getIconHeight());
        }

        maxWidth = Math.min(maxWidth + 20, this.installData.guiPrefs.width - 150);
        maxHeight = Math.min(maxHeight + 20, this.installData.guiPrefs.height - 150);

        return new Dimension(maxWidth, maxHeight);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */

    public void valueChanged(ListSelectionEvent e)
    {
        // this MUST be called before calling the super's valueChanged() since
        // that method refreshes the tablemodel and thus deselects the
        // just selected row
        int i = packsTable.getSelectedRow();
        super.valueChanged(e);
        if (i < 0)
        {
            return;
        }
        if (i >= 0)
        {
            Pack pack = this.installData.getAvailablePacks().get(i);
            imgLabel.setIcon(images.get(pack.getImageId()));
        }
    }

}

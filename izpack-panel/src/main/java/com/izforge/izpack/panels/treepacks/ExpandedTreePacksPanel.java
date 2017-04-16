package com.izforge.izpack.panels.treepacks;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JScrollPane;
import javax.swing.JTree;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;

/**
 * @author Mark Heinze
 */
public class ExpandedTreePacksPanel extends TreePacksPanel
{
    private static final long serialVersionUID = 5684716698930628263L;

    /**
     * The constructor.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent window
     * @param installData the installation data
     * @param resources   the resources
     * @param locales     the supported locales
     * @param rules       the rules
     */
    public ExpandedTreePacksPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
                          Locales locales, RulesEngine rules)
    {
        super(panel, parent, installData, resources, locales, rules);
    }

    @Override
    protected JTree createPacksTree(int width, JScrollPane scroller, GridBagLayout layout,
                                    GridBagConstraints constraints)
    {
        JTree tree = super.createPacksTree(width, scroller, layout, constraints);
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
        return (tree);
    }
}

package com.izforge.izpack.panels.treepacks;

import com.izforge.izpack.api.data.Pack;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

/**
 * The renderer model for individual checkbox nodes in a JTree. It renders the
 * checkbox and a label for the pack size.
 *
 * @author <a href="vralev@redhat.com">Vladimir Ralev</a>
 * @version $Revision: 1.1 $
 */
class CheckBoxNodeRenderer implements TreeCellRenderer
{

    private static final JPanel RENDERER_PANEL = new JPanel();
    private static final JLabel PACK_SIZE_LABEL = new JLabel();
    private static final JCheckBox CHECK_BOX = new JCheckBox();
    private static final JCheckBox NORMAL_CHECK_BOX = new JCheckBox();
    private static final java.awt.Font NORMAL_FONT = new JCheckBox().getFont();
    private static final java.awt.Font BOLD_FONT = new java.awt.Font(NORMAL_FONT.getFontName(),
            java.awt.Font.BOLD,
            NORMAL_FONT.getSize());
    private static final java.awt.Font PLAIN_FONT = new java.awt.Font(NORMAL_FONT.getFontName(),
            java.awt.Font.PLAIN,
            NORMAL_FONT.getSize());
    private static final Color ANNOTATION_COLOR = new Color(0, 0, 120); // red
    private static final Color CHANGED_COLOR = new Color(200, 0, 0);

    private static Color selectionForeground, selectionBackground,
            textForeground, textBackground;

    TreePacksPanel treePacksPanel;

    public CheckBoxNodeRenderer(TreePacksPanel t)
    {
        selectionForeground = UIManager.getColor("Tree.selectionForeground");
        selectionBackground = UIManager.getColor("Tree.selectionBackground");
        textForeground = UIManager.getColor("Tree.textForeground");
        textBackground = UIManager.getColor("Tree.textBackground");
        treePacksPanel = t;

        int treeWidth = t.getTree().getPreferredSize().width;
        int height = CHECK_BOX.getPreferredSize().height;
        int cellWidth = treeWidth - treeWidth / 4;

        //Don't touch, it fixes various layout bugs in swing/awt
        RENDERER_PANEL.setLayout(new BorderLayout(0, 0));
        RENDERER_PANEL.setBackground(textBackground);
        RENDERER_PANEL.add(BorderLayout.WEST, CHECK_BOX);

        RENDERER_PANEL.setAlignmentX((float) 0);
        RENDERER_PANEL.setAlignmentY((float) 0);
        RENDERER_PANEL.add(BorderLayout.EAST, PACK_SIZE_LABEL);

        RENDERER_PANEL.setMinimumSize(new Dimension(cellWidth, height));
        RENDERER_PANEL.setPreferredSize(new Dimension(cellWidth, height));
        RENDERER_PANEL.setSize(new Dimension(cellWidth, height));

        RENDERER_PANEL.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row,
            boolean hasFocus)
    {
        if (selected)
        {
            CHECK_BOX.setForeground(selectionForeground);
            CHECK_BOX.setBackground(selectionBackground);
            RENDERER_PANEL.setForeground(selectionForeground);
            RENDERER_PANEL.setBackground(selectionBackground);
            PACK_SIZE_LABEL.setBackground(selectionBackground);
        }
        else
        {
            CHECK_BOX.setForeground(textForeground);
            CHECK_BOX.setBackground(textBackground);
            RENDERER_PANEL.setForeground(textForeground);
            RENDERER_PANEL.setBackground(textBackground);
            PACK_SIZE_LABEL.setBackground(textBackground);
        }

        if ((value != null) && (value instanceof CheckBoxNode))
        {
            CheckBoxNode node = (CheckBoxNode) value;

            if (node.isTotalSizeChanged())
            {
                PACK_SIZE_LABEL.setForeground(CHANGED_COLOR);
            }
            else
            {
                if (selected)
                {
                    PACK_SIZE_LABEL.setForeground(selectionForeground);
                }
                else
                {
                    PACK_SIZE_LABEL.setForeground(ANNOTATION_COLOR);
                }
            }

            CHECK_BOX.setText(node.getTranslatedText());

            PACK_SIZE_LABEL.setText(Pack.toByteUnitsString(node.getTotalSize()));

            if (node.isPartial())
            {
                CHECK_BOX.setSelected(false);
            }
            else
            {
                CHECK_BOX.setSelected(node.isSelected());
            }

            CHECK_BOX.setEnabled(node.isEnabled());
            PACK_SIZE_LABEL.setEnabled(node.isEnabled());

            if (node.getChildCount() > 0)
            {
                CHECK_BOX.setFont(BOLD_FONT);
                PACK_SIZE_LABEL.setFont(BOLD_FONT);
            }
            else
            {
                CHECK_BOX.setFont(NORMAL_FONT);
                PACK_SIZE_LABEL.setFont(PLAIN_FONT);
            }

            if (node.isPartial())
            {
                CHECK_BOX.setIcon(new PartialIcon(node.isEnabled()));
            }
            else
            {
                CHECK_BOX.setIcon(NORMAL_CHECK_BOX.getIcon());
            }
        }
        return RENDERER_PANEL;
    }

    public Component getCheckRenderer()
    {
        return RENDERER_PANEL;
    }

}

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
    private static final JPanel rendererPanel = new JPanel();
    private static final JLabel packSizeLabel = new JLabel();
    private static final JCheckBox checkbox = new JCheckBox();
    private static final JCheckBox normalCheckBox = new JCheckBox();
    private static final java.awt.Font normalFont = new JCheckBox().getFont();
    private static final java.awt.Font boldFont = new java.awt.Font(normalFont.getFontName(),
            java.awt.Font.BOLD,
            normalFont.getSize());
    private static final java.awt.Font plainFont = new java.awt.Font(normalFont.getFontName(),
            java.awt.Font.PLAIN,
            normalFont.getSize());
    private static final Color annotationColor = new Color(0, 0, 120); // red
    private static final Color changedColor = new Color(200, 0, 0);

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
        int height = checkbox.getPreferredSize().height;
        int cellWidth = treeWidth - treeWidth / 4;

        //Don't touch, it fixes various layout bugs in swing/awt
        rendererPanel.setLayout(new BorderLayout(0, 0));
        rendererPanel.setBackground(textBackground);
        rendererPanel.add(BorderLayout.WEST, checkbox);

        rendererPanel.setAlignmentX((float) 0);
        rendererPanel.setAlignmentY((float) 0);
        rendererPanel.add(BorderLayout.EAST, packSizeLabel);

        rendererPanel.setMinimumSize(new Dimension(cellWidth, height));
        rendererPanel.setPreferredSize(new Dimension(cellWidth, height));
        rendererPanel.setSize(new Dimension(cellWidth, height));

        rendererPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean selected, boolean expanded, boolean leaf, int row,
                                                  boolean hasFocus)
    {
        treePacksPanel.updateViewFromModel(tree);

        if (selected)
        {
            checkbox.setForeground(selectionForeground);
            checkbox.setBackground(selectionBackground);
            rendererPanel.setForeground(selectionForeground);
            rendererPanel.setBackground(selectionBackground);
            packSizeLabel.setBackground(selectionBackground);
        }
        else
        {
            checkbox.setForeground(textForeground);
            checkbox.setBackground(textBackground);
            rendererPanel.setForeground(textForeground);
            rendererPanel.setBackground(textBackground);
            packSizeLabel.setBackground(textBackground);
        }

        if ((value != null) && (value instanceof CheckBoxNode))
        {
            CheckBoxNode node = (CheckBoxNode) value;

            if (node.isTotalSizeChanged())
            {
                packSizeLabel.setForeground(changedColor);
            }
            else
            {
                if (selected)
                {
                    packSizeLabel.setForeground(selectionForeground);
                }
                else
                {
                    packSizeLabel.setForeground(annotationColor);
                }
            }

            checkbox.setText(node.getTranslatedText());

            packSizeLabel.setText(Pack.toByteUnitsString(node.getTotalSize()));

            if (node.isPartial())
            {
                checkbox.setSelected(false);
            }
            else
            {
                checkbox.setSelected(node.isSelected());
            }

            checkbox.setEnabled(node.isEnabled());
            packSizeLabel.setEnabled(node.isEnabled());

            if (node.getChildCount() > 0)
            {
                checkbox.setFont(boldFont);
                packSizeLabel.setFont(boldFont);
            }
            else
            {
                checkbox.setFont(normalFont);
                packSizeLabel.setFont(plainFont);
            }

            if (node.isPartial())
            {
                checkbox.setIcon(new PartialIcon(node.isEnabled()));
            }
            else
            {
                checkbox.setIcon(normalCheckBox.getIcon());
            }
        }
        return rendererPanel;
    }

    public Component getCheckRenderer()
    {
        return rendererPanel;
    }

}

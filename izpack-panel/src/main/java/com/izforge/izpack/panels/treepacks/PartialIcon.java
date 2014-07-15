package com.izforge.izpack.panels.treepacks;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;

/**
 * Special checkbox icon which shows partially selected nodes.
 *
 * @author <a href="vralev@redhat.com">Vladimir Ralev</a>
 * @version $Revision: 1.1 $
 */
class PartialIcon implements Icon
{
    Color color;
    public PartialIcon(boolean enabled)
    {
        if(enabled)
        {
            color = Color.green;
        }
        else
        {
            color = Color.gray;
        }
    }
    protected int getControlSize()
    {
        return 13;
    }

    public void paintIcon(Component component, Graphics graphics, int x, int y)
    {
        int controlSize = getControlSize();
        graphics.setColor(MetalLookAndFeel.getControlShadow());
        graphics.fillRect(x, y, controlSize - 1, controlSize - 1);
        drawBorder(graphics, x, y, controlSize, controlSize);

        graphics.setColor(this.color);
        drawCheck(component, graphics, x, y);
    }

    private void drawBorder(Graphics graphics, int x, int y, int width, int height)
    {
        graphics.translate(x, y);

        // outer frame rectangle
        graphics.setColor(MetalLookAndFeel.getControlDarkShadow());
        graphics.setColor(new Color(0.4f, 0.4f, 0.4f));
        graphics.drawRect(0, 0, width - 2, height - 2);

        // middle frame
        graphics.setColor(MetalLookAndFeel.getControlHighlight());
        graphics.setColor(new Color(0.6f, 0.6f, 0.6f));
        graphics.drawRect(1, 1, width - 2, height - 2);

        // background
        graphics.setColor(new Color(0.99f, 0.99f, 0.99f));
        graphics.fillRect(2, 2, width - 3, height - 3);

        //some extra lines for FX
        graphics.setColor(MetalLookAndFeel.getControl());
        graphics.drawLine(0, height - 1, 1, height - 2);
        graphics.drawLine(width - 1, 0, width - 2, 1);
        graphics.translate(-x, -y);
    }

    protected void drawCheck(Component component, Graphics graphics, int x, int y)
    {
        int controlSize = getControlSize();
        graphics.setColor(new Color(0.0f, 0.7f, 0.0f));

        graphics.fillOval(x + controlSize / 2 - 2, y + controlSize / 2 - 2, 6, 6);
    }

    public int getIconWidth()
    {
        return getControlSize();
    }

    public int getIconHeight()
    {
        return getControlSize();
    }
}

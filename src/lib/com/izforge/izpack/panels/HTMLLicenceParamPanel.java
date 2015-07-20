package com.izforge.izpack.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ButtonGroup;
import javax.swing.JEditorPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.installer.ResourceManager;


public class HTMLLicenceParamPanel extends IzPanel implements ActionListener, HyperlinkListener
{
    
    /**
     * The text area.
     */
    private JEditorPane textArea;

    /**
     * The radio buttons.
     */
    private JRadioButton yesRadio;
    private JRadioButton noRadio;

    public static final String strLicenceId = "licenceid";
    /**
     * The constructor.
     *
     * @param idata  The installation data.
     * @param parent Description of the Parameter
     */
    public HTMLLicenceParamPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata, new IzPanelLayout());
        // We load the licence
        loadLicence(idata);

        // We put our components

        add(LabelFactory.create(parent.langpack.getString("LicencePanel.info"),
                parent.icons.getImageIcon("history"), LEADING), NEXT_LINE);
        try
        {
            textArea = new JEditorPane();
            textArea.setEditable(false);
            textArea.addHyperlinkListener(this);
            JScrollPane scroller = new JScrollPane(textArea);
            textArea.setPage(loadLicence(idata));
            add(scroller, NEXT_LINE);
        }
        catch (Exception err)
        {
            err.printStackTrace();
        }

        ButtonGroup group = new ButtonGroup();

        yesRadio = new JRadioButton(parent.langpack.getString("LicencePanel.agree"), false);
        group.add(yesRadio);
        add(yesRadio, NEXT_LINE);
        yesRadio.addActionListener(this);

        noRadio = new JRadioButton(parent.langpack.getString("LicencePanel.notagree"), true);
        group.add(noRadio);
        add(noRadio, NEXT_LINE);
        noRadio.addActionListener(this);
        setInitialFocus(textArea);
        getLayoutHelper().completeLayout();
    }
    

    /**
     * Loads the license text.
     *
     * @return The license text URL.
     */
    private URL loadLicence(InstallData idata)
    {
        String resNamePrifix = idata.getVariable(strLicenceId);
        try
        {
            return ResourceManager.getInstance().getURL(resNamePrifix);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Actions-handling method (here it launches the installation).
     *
     * @param e The event.
     */
    public void actionPerformed(ActionEvent e)
    {
        if (yesRadio.isSelected())
        {
            parent.unlockNextButton();
        }
        else
        {
            parent.lockNextButton();
        }
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return true if the user agrees with the license, false otherwise.
     */
    public boolean isValidated()
    {
        if (noRadio.isSelected())
        {
            parent.exit();
            return false;
        }
        return (yesRadio.isSelected());
    }

    /**
     * Hyperlink events handler.
     *
     * @param e The event.
     */
    public void hyperlinkUpdate(HyperlinkEvent e)
    {
        try
        {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
            {
                textArea.setPage(e.getURL());
            }
        }
        catch (Exception err)
        {
            // TODO: Extend exception handling.
        }
    }

    /**
     * Called when the panel becomes active.
     */
    public void panelActivate()
    {
        if (!yesRadio.isSelected())
        {
            parent.lockNextButton();
        }
    }

}

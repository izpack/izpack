package com.izforge.izpack.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.installer.ResourceManager;


public class LicenceParamPanel extends IzPanel implements ActionListener
{
    public static final String strLicenceId = "licenceid";

    /**
     * The license text.
     */
    private String licence;

    /**
     * The radio buttons.
     */
    private JRadioButton yesRadio;
    private JRadioButton noRadio;

    /**
     * The constructor.
     *
     * @param parent The parent window.
     * @param idata  The installation data.
     */
    public LicenceParamPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata, new IzPanelLayout());
        // We load the licence
        loadLicence(idata);

        // We put our components

        add(LabelFactory.create(parent.langpack.getString("LicencePanel.info"),
                parent.icons.getImageIcon("history"), LEADING), NEXT_LINE);
        JTextArea textArea = new JTextArea(licence);
        textArea.setCaretPosition(0);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scroller = new JScrollPane(textArea);
        scroller.setAlignmentX(LEFT_ALIGNMENT);
        add(scroller, NEXT_LINE);

        ButtonGroup group = new ButtonGroup();

        String label = parent.langpack.getString("LicencePanel.agree");
        yesRadio = new JRadioButton(label, false);
        group.add(yesRadio);
        add(yesRadio, NEXT_LINE);
        yesRadio.addActionListener(this);
        char mnemonics = 0;
        if (label != null && label.length() > 0) {
            mnemonics = label.toLowerCase().charAt(0);
            yesRadio.setMnemonic(mnemonics);
        }

        label = parent.langpack.getString("LicencePanel.notagree");
        noRadio = new JRadioButton(label, true);
        group.add(noRadio);
        add(noRadio, NEXT_LINE);
        noRadio.addActionListener(this);
        if (label != null && label.length() > 0) {
            char noMnemonics = label.toLowerCase().charAt(0);
            if (mnemonics == noMnemonics) {
                if (label.indexOf(" ") != -1 && label.indexOf(" ") < label.length() - 2) {
                    noMnemonics = label.toLowerCase().charAt(label.indexOf(" ") + 1);
                } else if (label.length() > 0) {
                    noMnemonics = label.toLowerCase().charAt(1);
                }
            }
            if (noMnemonics > 0) {
                noRadio.setMnemonic(noMnemonics);
            }
        }

        setInitialFocus(noRadio);
        getLayoutHelper().completeLayout();
    }

    /**
     * Loads the licence text.
     */
    private void loadLicence(InstallData idata)
    {
        try
        {
            // We read it
            String resNamePrifix = idata.getVariable(strLicenceId);
            licence = ResourceManager.getInstance().getTextResource(resNamePrifix);
        }
        catch (Exception err)
        {
            licence = "Error : could not load the licence text !";
        }
    }

    /**
     * Actions-handling method (here it allows the installation).
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
     * @return true if the user has agreed.
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

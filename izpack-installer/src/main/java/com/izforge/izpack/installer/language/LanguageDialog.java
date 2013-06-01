/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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
package com.izforge.izpack.installer.language;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.exception.ResourceException;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.requirement.RequirementsChecker;

/**
 * Used to prompt the user for the language. Languages can be displayed in iso3 or the native
 * notation or the notation of the default locale. Revising to native notation is based on code
 * from Christian Murphy (patch #395).
 *
 * @author Julien Ponge
 * @author Christian Murphy
 * @author Klaus Bartz
 */
public class LanguageDialog extends JDialog
{
    private static final long serialVersionUID = 3256443616359887667L;

    /**
     * The parent frame.
     */
    private final JFrame frame;

    /**
     * The installation data.
     */
    private final GUIInstallData installData;

    /**
     * The resources.
     */
    private final Resources resources;

    /**
     * The supported locales.
     */
    private final Locales locales;

    /**
     * Installation requirements checker.
     */
    private final RequirementsChecker requirements;

    /**
     * Maps ISO3 codes to the corresponding language display values.
     */
    private Map<String, String> displayNames = new HashMap<String, String>();

    /**
     * The combo box.
     */
    private JComboBox comboBox;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(LanguageDialog.class.getName());


    /**
     * Constructs a {@code LanguageDialog}.
     *
     * @param frame        the parent frame
     * @param resources    the resources
     * @param locales      the locales
     * @param installData  the installation data
     * @param requirements the installation requirements
     */
    public LanguageDialog(JFrame frame, Resources resources, Locales locales, GUIInstallData installData,
                          RequirementsChecker requirements)
    {
        super(frame);
        this.frame = frame;
        this.resources = resources;
        this.locales = locales;
        this.installData = installData;
        this.requirements = requirements;
        this.setName(GuiId.DIALOG_PICKER.id);
        initialise();
    }

    /**
     * Displays the dialog.
     *
     * @throws Exception for any error
     */
    public void initLangPack() throws Exception
    {
        // Loads the suitable langpack
        switch (locales.getLocales().size()) {
        case 0:
            // nothing to do at this point
            break;
        case 1:
            // propagate the specified language 
            String codeOfUniqueLanguage = displayNames.keySet().iterator().next();
            propagateLocale(codeOfUniqueLanguage);
            break;
        default:
            frame.setVisible(false);
            setVisible(true);
        }

        // check installer conditions
        if (!requirements.check())
        {
            logger.info("Not all installer requirements are fulfilled.");
            System.exit(-1);
        }
    }

    /**
     * Initialises the dialog.
     */
    private void initialise()
    {
        JPanel contentPane = (JPanel) getContentPane();
        Languages languages = new Languages(locales, installData, contentPane.getFont());

        // We build the GUI
        addWindowListener(new WindowHandler());
        setTitle("Language Selection");
        GridBagLayout layout = new GridBagLayout();
        contentPane.setLayout(layout);
        GridBagConstraints gbConstraints = new GridBagConstraints();
        gbConstraints.anchor = GridBagConstraints.CENTER;
        gbConstraints.insets = new Insets(5, 5, 5, 5);
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.gridx = 0;
        gbConstraints.weightx = 1.0;
        gbConstraints.weighty = 1.0;
        gbConstraints.ipadx = 0;
        gbConstraints.ipady = 6;

        ImageIcon img = getImage();
        JLabel imgLabel = new JLabel(img);
        gbConstraints.gridy = 0;
        contentPane.add(imgLabel);

        String firstMessage = "Please select your language";
        if (languages.getDisplayType() == Languages.DisplayType.ISO3)
        {
            // TODO - Not sure why this is specific to ISO3. Should be localised too.
            firstMessage = "Please select your language below";
        }

        JLabel label1 = new JLabel(firstMessage, SwingConstants.LEADING);
        gbConstraints.gridy = 1;
        gbConstraints.insets = new Insets(15, 5, 5, 5);
        layout.addLayoutComponent(label1, gbConstraints);
        contentPane.add(label1);

        gbConstraints.insets = new Insets(5, 5, 5, 5);
        displayNames = languages.getDisplayNames();

        comboBox = new JComboBox(displayNames.keySet().toArray());
        comboBox.setName(GuiId.COMBO_BOX_LANG_FLAG.id);
        if (useFlags())
        {
            comboBox.setRenderer(new FlagRenderer());
        }
        else
        {
            comboBox.setRenderer(new LanguageRenderer());
        }

        gbConstraints.gridy = 3;
        layout.addLayoutComponent(comboBox, gbConstraints);
        contentPane.add(comboBox);

        gbConstraints.insets = new Insets(15, 5, 15, 5);
        JButton okButton = new JButton("OK");
        okButton.setName(GuiId.BUTTON_LANG_OK.id);
        okButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                onOK();
            }
        });
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.gridy = 4;
        gbConstraints.anchor = GridBagConstraints.CENTER;
        layout.addLayoutComponent(okButton, gbConstraints);
        contentPane.add(okButton);
        getRootPane().setDefaultButton(okButton);

        // Packs and centers
        // Fix for bug "Installer won't show anything on OSX"
        if (System.getProperty("mrj.version") == null)
        {
            pack();
        }
        setSize(getPreferredSize());

        Dimension frameSize = getSize();
        Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        setLocation(center.x - frameSize.width / 2, center.y - frameSize.height / 2 - 10);
        setResizable(true);
        comboBox.setSelectedItem(Locale.getDefault().getISO3Language().toLowerCase());
        setModal(true);
        toFront();
    }

    /**
     * Loads an image.
     *
     * @return The image icon.
     */
    private ImageIcon getImage()
    {
        ImageIcon img;
        try
        {
            img = resources.getImageIcon("installer.langsel.img");
        }
        catch (Exception err)
        {
            img = null;
        }
        return img;
    }

    private void onOK()
    {
        String selectedPack = (String) comboBox.getSelectedItem();
        if (selectedPack == null)
        {
            throw new RuntimeException("installation canceled");
        }

        propagateLocale(selectedPack);
        
        dispose();
    }

    /**
     * The window events handler.
     *
     * @author Julien Ponge
     */
    private class WindowHandler extends WindowAdapter
    {

        /**
         * We can't avoid the exit here, so don't call exit anywhere else.
         *
         * @param e the event.
         */
        @Override
        public void windowClosing(WindowEvent e)
        {
            System.exit(0);
        }
    }

    /**
     * Returns whether flags should be used in the language selection dialog or not.
     *
     * @return whether flags should be used in the language selection dialog or not
     */
    private boolean useFlags()
    {
        if (installData.guiPrefs.modifier.containsKey("useFlags")
                && "no".equalsIgnoreCase(installData.guiPrefs.modifier.get("useFlags")))
        {
            return (false);
        }
        return (true);
    }


    /**
     * Sets the selected locale on the installation data.
     *
     * @param code the locale ISO code
     */
    private void propagateLocale(String code)
    {
        try
        {
            locales.setLocale(code);
            Locale newLocale = locales.getLocale();
            Locale.setDefault(newLocale);

            JComponent.setDefaultLocale(newLocale);
            SwingUtilities.updateComponentTreeUI(this);

            installData.setLocale(locales.getLocale(), locales.getISOCode());
            installData.setMessages(locales.getMessages());

            // Configure buttons after locale has been loaded
            installData.configureGuiButtons();
        }
        catch (Exception exception)
        {
            logger.log(Level.SEVERE, exception.getMessage(), exception);
        }
    }

    /**
     * A list cell renderer to display the language given is ISO3 code.
     */
    private class LanguageRenderer extends JLabel implements ListCellRenderer
    {

        /**
         * Return a component that has been configured to display the specified value.
         *
         * @param list         the list
         * @param value        the value to display
         * @param index        the cells index.
         * @param isSelected   true if the specified cell was selected.
         * @param cellHasFocus true if the specified cell has the focus
         * @return a component to render the value
         */
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus)
        {
            String code = (String) value;
            setText(displayNames.get(code));
            if (isSelected)
            {
                setForeground(list.getSelectionForeground());
                setBackground(list.getSelectionBackground());
            }
            else
            {
                setForeground(list.getForeground());
                setBackground(list.getBackground());
            }
            return this;
        }
    }

    /**
     * A list cell renderer that adds the flags on the display.
     *
     * @author Julien Ponge
     */
    private class FlagRenderer extends LanguageRenderer
    {

        /**
         * Icons cache.
         */
        private TreeMap<String, ImageIcon> icons = new TreeMap<String, ImageIcon>();

        /**
         * Grayed icons cache.
         */
        private TreeMap<String, ImageIcon> grayIcons = new TreeMap<String, ImageIcon>();

        /**
         * Default constructor.
         */
        public FlagRenderer()
        {
            setOpaque(true);
        }

        /**
         * Returns a suitable cell.
         *
         * @param list         The list.
         * @param value        The object.
         * @param index        The index.
         * @param isSelected   true if it is selected.
         * @param cellHasFocus Description of the Parameter
         * @return The cell.
         */
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus)
        {
            Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            String code = (String) value;
            if (!icons.containsKey(code))
            {
                try
                {
                    ImageIcon icon;
                    icon = resources.getImageIcon("flag." + code);
                    icons.put(code, icon);
                    icon = new ImageIcon(GrayFilter.createDisabledImage(icon.getImage()));
                    grayIcons.put(code, icon);
                }
                catch (ResourceException exception)
                {
                    logger.log(Level.WARNING, exception.getMessage(), exception);
                }
            }
            if (isSelected || index == -1)
            {
                setIcon(icons.get(code));
            }
            else
            {
                setIcon(grayIcons.get(code));
            }
            return result;
        }
    }

}

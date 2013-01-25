/*
 * $Id$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.installer;

import com.izforge.izpack.GUIPrefs;
import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.VariableSubstitutor;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * The IzPack graphical installer class.
 *
 * @author Julien Ponge
 */
public class GUIInstaller extends InstallerBase
{

    /**
     * The installation data.
     */
    private InstallData installdata;

    /**
     * The L&F.
     */
    protected String lnf;

    /**
     * defined modifier for language display type.
     */
    private static final String[] LANGUAGE_DISPLAY_TYPES = {"iso3", "native", "default"};

    private static final String[][] LANG_CODES = {{"cat", "ca"}, {"chn", "zh"}, {"cze", "cs"},
            {"dan", "da"}, {"deu", "de"}, {"eng", "en"}, {"fin", "fi"}, {"fra", "fr"},
            {"hun", "hu"}, {"ita", "it"}, {"jpn", "ja"}, {"mys", "ms"}, {"ned", "nl"},
            {"nor", "no"}, {"pol", "pl"}, {"por", "pt"}, {"rom", "or"}, {"rus", "ru"},
            {"spa", "es"}, {"svk", "sk"}, {"swe", "sv"}, {"tur", "tr"}, {"ukr", "uk"}};

    /**
     * holds language to ISO-3 language code translation
     */
    private static HashMap isoTable;

    /**
     * The constructor.
     *
     * @throws Exception Description of the Exception
     */
    public GUIInstaller() throws Exception
    {
        try {
            init();
        } catch (Exception e) {
            showFatalError(e);
            throw e;
        } catch (Error e) {
            showFatalError(e);
            throw e;
        }
    }

    private void showFatalError(Throwable e)
    {
        try {
            JOptionPane.showMessageDialog (null, "Error: "+e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    private void init() throws Exception
    {

        this.installdata = new InstallData();

        // Loads the installation data
        loadInstallData(installdata);

        // add the GUI install data
        loadGUIInstallData();

        // Sets up the GUI L&F
        loadLookAndFeel();
        
//        // set Font Size
//        // try this APO
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements())
        {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource)
            {
                UIManager.put(key, new javax.swing.plaf.FontUIResource (((javax.swing.plaf.FontUIResource) value).getFontName(), ((javax.swing.plaf.FontUIResource) value).getStyle(), 14));
            }
        }
            
        // Checks the Java version
        checkJavaVersion();
        checkJDKAvailable();

        // Check for already running instance
        checkLockFile();

//      Loads the suitable langpack
        SwingUtilities.invokeAndWait(new Runnable()
        {

            public void run()
            {
                try
                {
                    loadLangPack();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        // create the resource manager (after the language selection!)
        ResourceManager.create(this.installdata);

//      load conditions
        loadConditions(installdata);

        // loads installer conditions
        loadInstallerRequirements();

        // load dynamic variables
        loadDynamicVariables();

        // Load custom langpack if exist.
        addCustomLangpack(installdata);

        // check installer conditions
        if (!checkInstallerRequirements(installdata))
        {
            Debug.log("not all installerconditions are fulfilled.");
            System.exit(-1);
            return;
        }

        // We launch the installer GUI
        SwingUtilities.invokeLater(new Runnable()
        {

            public void run()
            {
                try
                {
                    loadGUI();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    public void showMissingRequirementMessage(String message){
        JOptionPane.showMessageDialog(null, message);
    }

    /**
     * Load GUI preference information.
     *
     * @throws Exception
     */
    public void loadGUIInstallData() throws Exception
    {
        InputStream in = GUIInstaller.class.getResourceAsStream("/GUIPrefs");
        ObjectInputStream objIn = new ObjectInputStream(in);
        this.installdata.guiPrefs = (GUIPrefs) objIn.readObject();
        objIn.close();
    }

    /**
     * Sets a lock file. Not using java.nio.channels.FileLock to prevent
     * the installer from accidentally keeping a lock on a file if the install
     * fails or is killed.
     *
     * @throws Exception Description of the Exception
     */
    private void checkLockFile() throws Exception
    {
        String tempDir = System.getProperty("java.io.tmpdir");
        String appName = this.installdata.info.getAppName();
        String fileName = "iz-" + appName + ".tmp";
        Debug.trace("Making temp file: " + fileName);
        Debug.trace("In temp directory: " + tempDir);
        File file = new File(tempDir, fileName);
        if (file.exists())
        {
            // Ask user if they want to proceed.
            Debug.trace("Lock File Exists, asking user for permission to proceed.");
            StringBuffer msg = new StringBuffer();
            msg.append("<html>");
            msg.append("The " + appName + " installer you are attempting to run seems to have a copy already running.<br><br>");
            msg.append("This could be from a previous failed installation attempt or you may have accidentally launched <br>");
            msg.append("the installer twice. <b>The recommended action is to select 'Exit'</b> and wait for the other copy of <br>");
            msg.append("the installer to start. If you are sure there is no other copy of the installer running, click <br>");
            msg.append("the 'Continue' button to allow this installer to run. <br><br>");
            msg.append("Are you sure you want to continue with this installation?");
            msg.append("</html>");
            JLabel label = new JLabel(msg.toString());
            label.setFont(new Font("Sans Serif", Font.PLAIN, 12));
            Object[] optionValues = {"Continue", "Exit"};
            int selectedOption = JOptionPane.showOptionDialog(null, label, "Warning",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, optionValues,
                    optionValues[1]);
            Debug.trace("Selected option: " + selectedOption);
            if (selectedOption == 0)
            {
                // Take control of the file so it gets deleted after this installer instance exits.
                Debug.trace("Setting temp file to delete on exit");
                file.deleteOnExit();
            }
            else
            {
                // Leave the file as it is.
                Debug.trace("Leaving temp file alone and exiting");
                System.exit(1);
            }
        }
        else
        {
            try
            {
                // Create the new lock file
                if (file.createNewFile())
                {
                    Debug.trace("Temp file created");
                    file.deleteOnExit();
                }
                else
                {
                    Debug.trace("Temp file could not be created");
                    Debug.trace("*** Multiple instances of installer will be allowed ***");
                }
            }
            catch (Exception e)
            {
                Debug.trace("Temp file could not be created: " + e);
                Debug.trace("*** Multiple instances of installer will be allowed ***");
            }
        }
    }

    /**
     * Checks the Java version.
     *
     * @throws Exception Description of the Exception
     */
    private void checkJavaVersion() throws Exception
    {
        String version = System.getProperty("java.version");
        String required = this.installdata.info.getJavaVersion();
        if (version.compareTo(required) < 0)
        {
            StringBuffer msg = new StringBuffer();
            msg.append("The application that you are trying to install requires a ");
            msg.append(required);
            msg.append(" version or later of the Java platform.\n");
            msg.append("You are running a ");
            msg.append(version);
            msg.append(" version of the Java platform.\n");
            msg.append("Please upgrade to a newer version.");

            System.out.println(msg.toString());
            JOptionPane.showMessageDialog(null, msg.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    /**
     * Checks if a JDK is available.
     */
    private void checkJDKAvailable()
    {
        if (!this.installdata.info.isJdkRequired())
        {
            return;
        }

        FileExecutor exec = new FileExecutor();
        String[] output = new String[2];
        String[] params = {"javac", "-help"};
        if (exec.executeCommand(params, output) != 0)
        {
            String[] message = {
                    "It looks like your system does not have a Java Development Kit (JDK) available.",
                    "The software that you plan to install requires a JDK for both its installation and execution.",
                    "\n",
                    "Do you still want to proceed with the installation process?"
            };
            int status = JOptionPane.showConfirmDialog(null, message, "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (status == JOptionPane.NO_OPTION)
            {
                System.exit(1);
            }
        }
    }

    /**
     * Loads the suitable langpack.
     *
     * @throws Exception Description of the Exception
     */
    private void loadLangPack() throws Exception
    {
        // Initialisations
        List availableLangPacks = getAvailableLangPacks();
        int npacks = availableLangPacks.size();
        if (npacks == 0)
        {
            throw new Exception("no language pack available");
        }
        String selectedPack;

        // Dummy Frame
        JFrame frame = new JFrame();

        ImageIcon img;
        try
        {
            img = new ImageIcon(this.getClass().getResource("/res/installer.langsel.icon"));
        }
        catch (NullPointerException err)
        {
            img = new ImageIcon(this.getClass().getResource("/img/JFrameIcon.png"));
        }
        frame.setIconImage(img.getImage());

        Dimension frameSize = frame.getSize();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - frameSize.width) / 2,
                (screenSize.height - frameSize.height) / 2 - 10);

        // We get the langpack name
        if (npacks != 1)
        {
            LanguageDialog picker = new LanguageDialog(frame, availableLangPacks.toArray());
            picker.setSelection(Locale.getDefault().getISO3Language().toLowerCase());
            picker.setModal(true);
            picker.toFront();
            // frame.setVisible(true);
            frame.setVisible(false);
            picker.setVisible(true);

            selectedPack = (String) picker.getSelection();
            if (selectedPack == null)
            {
                throw new Exception("installation canceled");
            }
        }
        else
        {
            selectedPack = (String) availableLangPacks.get(0);
        }

        // We add an xml data information
        this.installdata.xmlData.setAttribute("langpack", selectedPack);

        // We load the langpack
        installdata.localeISO3 = selectedPack;
        installdata.setVariable(ScriptParser.ISO3_LANG, installdata.localeISO3);
        InputStream in = getClass().getResourceAsStream("/langpacks/" + selectedPack + ".xml");
        this.installdata.langpack = new LocaleDatabase(in);

    }

    /**
     * Loads the suitable L&F.
     *
     * @throws Exception Description of the Exception
     */
    protected void loadLookAndFeel() throws Exception
    {
        // Do we have any preference for this OS ?
        String syskey = "unix";
        if (OsVersion.IS_WINDOWS)
        {
            syskey = "windows";
        }
        else if (OsVersion.IS_OSX)
        {
            syskey = "mac";
        }
        String laf = null;
        if (installdata.guiPrefs.lookAndFeelMapping.containsKey(syskey))
        {
            laf = installdata.guiPrefs.lookAndFeelMapping.get(syskey);
        }

        // Let's use the system LAF
        // Resolve whether button icons should be used or not.
        boolean useButtonIcons = true;
        if (installdata.guiPrefs.modifier.containsKey("useButtonIcons")
                && "no".equalsIgnoreCase(installdata.guiPrefs.modifier
                .get("useButtonIcons")))
        {
            useButtonIcons = false;
        }
        ButtonFactory.useButtonIcons(useButtonIcons);
        boolean useLabelIcons = true;
        if (installdata.guiPrefs.modifier.containsKey("useLabelIcons")
                && "no".equalsIgnoreCase(installdata.guiPrefs.modifier
                .get("useLabelIcons")))
        {
            useLabelIcons = false;
        }
        LabelFactory.setUseLabelIcons(useLabelIcons);
        if (installdata.guiPrefs.modifier.containsKey("labelFontSize"))
        {  //'labelFontSize' modifier found in 'guiprefs'
            final String valStr =
                         installdata.guiPrefs.modifier.get("labelFontSize");
            try
            {      //parse value and enter as label-font-size multiplier:
                LabelFactory.setLabelFontSize(Float.parseFloat(valStr));
            }
            catch (NumberFormatException ex)
            {      //error parsing value; log message
                Debug.log("Error parsing guiprefs 'labelFontSize' value (" +
                                                              valStr + ')');
            }
        }
        if (laf == null)
        {
            if (!"mac".equals(syskey))
            {
                // In Linux we will use the English locale, because of a bug in
                // JRE6. In Korean, Persian, Chinese, japanese and some other
                // locales the installer throws and exception and doesn't load
                // at all. See http://jira.jboss.com/jira/browse/JBINSTALL-232.
                // This is a workaround until this bug gets fixed.
                if ("unix".equals(syskey))
                {
                    Locale.setDefault(Locale.ENGLISH);
                }
                String syslaf = UIManager.getSystemLookAndFeelClassName();
                UIManager.setLookAndFeel(syslaf);
                if (UIManager.getLookAndFeel() instanceof MetalLookAndFeel)
                {
                    ButtonFactory.useButtonIcons(useButtonIcons);
                }
            }
            lnf = "swing";
            return;
        }

        // Kunststoff (http://www.incors.org/)
        if ("kunststoff".equals(laf))
        {
            ButtonFactory.useHighlightButtons();
            // Reset the use button icons state because useHighlightButtons
            // make it always true.
            ButtonFactory.useButtonIcons(useButtonIcons);
            installdata.buttonsHColor = new Color(255, 255, 255);
            Class<LookAndFeel> lafClass = (Class<LookAndFeel>) Class.forName("com.incors.plaf.kunststoff.KunststoffLookAndFeel");
            Class mtheme = Class.forName("javax.swing.plaf.metal.MetalTheme");
            Class[] params = {mtheme};
            Class<MetalTheme> theme = (Class<MetalTheme>) Class.forName("com.izforge.izpack.gui.IzPackKMetalTheme");
            Method setCurrentThemeMethod = lafClass.getMethod("setCurrentTheme", params);

            // We invoke and place Kunststoff as our L&F
            LookAndFeel kunststoff = lafClass.newInstance();
            MetalTheme ktheme = theme.newInstance();
            Object[] kparams = {ktheme};
            UIManager.setLookAndFeel(kunststoff);
            setCurrentThemeMethod.invoke(kunststoff, kparams);

            lnf = "kunststoff";
            return;
        }

        // Liquid (http://liquidlnf.sourceforge.net/)
        if ("liquid".equals(laf))
        {
            UIManager.setLookAndFeel("com.birosoft.liquid.LiquidLookAndFeel");
            lnf = "liquid";

            Map<String, String> params = installdata.guiPrefs.lookAndFeelParams.get(laf);
            if (params.containsKey("decorate.frames"))
            {
                String value = params.get("decorate.frames");
                if ("yes".equals(value))
                {
                    JFrame.setDefaultLookAndFeelDecorated(true);
                }
            }
            if (params.containsKey("decorate.dialogs"))
            {
                String value = params.get("decorate.dialogs");
                if ("yes".equals(value))
                {
                    JDialog.setDefaultLookAndFeelDecorated(true);
                }
            }

            return;
        }

        // Metouia (http://mlf.sourceforge.net/)
        if ("metouia".equals(laf))
        {
            UIManager.setLookAndFeel("net.sourceforge.mlf.metouia.MetouiaLookAndFeel");
            lnf = "metouia";
            return;
        }

        // Nimbus (http://nimbus.dev.java.net/)
        if ("nimbus".equals(laf))
        {
            UIManager.setLookAndFeel("org.jdesktop.swingx.plaf.nimbus.NimbusLookAndFeel");
            return;
        }

        // JGoodies Looks (http://looks.dev.java.net/)
        if ("looks".equals(laf))
        {
            Map<String, String> variants = new TreeMap<String, String>();
            variants.put("windows", "com.jgoodies.looks.windows.WindowsLookAndFeel");
            variants.put("plastic", "com.jgoodies.looks.plastic.PlasticLookAndFeel");
            variants.put("plastic3D", "com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
            variants.put("plasticXP", "com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
            String variant = variants.get("plasticXP");

            Map<String, String> params = installdata.guiPrefs.lookAndFeelParams.get(laf);
            if (params.containsKey("variant"))
            {
                String param = params.get("variant");
                if (variants.containsKey(param))
                {
                    variant = variants.get(param);
                }
            }

            UIManager.setLookAndFeel(variant);
            return;
        }

        // Substance (http://substance.dev.java.net/)
        if ("substance".equals(laf))
        {
            Map<String, String> variants = new TreeMap<String, String>();
            variants.put("default", "org.jvnet.substance.SubstanceLookAndFeel"); // Ugly!!!
            variants.put("business", "org.jvnet.substance.skin.SubstanceBusinessLookAndFeel");
            variants.put("business-blue", "org.jvnet.substance.skin.SubstanceBusinessBlueSteelLookAndFeel");
            variants.put("business-black", "org.jvnet.substance.skin.SubstanceBusinessBlackSteelLookAndFeel");
            variants.put("creme", "org.jvnet.substance.skin.SubstanceCremeLookAndFeel");
            variants.put("sahara", "org.jvnet.substance.skin.SubstanceSaharaLookAndFeel");
            variants.put("moderate", "org.jvnet.substance.skin.SubstanceModerateLookAndFeel");
            variants.put("officesilver", "org.jvnet.substance.skin.SubstanceOfficeSilver2007LookAndFeel");
            String variant = variants.get("default");

            Map<String, String> params = installdata.guiPrefs.lookAndFeelParams.get(laf);
            if (params.containsKey("variant"))
            {
                String param = params.get("variant");
                if (variants.containsKey(param))
                {
                    variant = variants.get(param);
                }
            }

            UIManager.setLookAndFeel(variant);
        }
    }

    /**
     * Loads the GUI.
     *
     * @throws Exception Description of the Exception
     */
    private void loadGUI() throws Exception
    {
        UIManager.put("OptionPane.yesButtonText", installdata.langpack.getString("installer.yes"));
        UIManager.put("OptionPane.noButtonText", installdata.langpack.getString("installer.no"));
        UIManager.put("OptionPane.cancelButtonText", installdata.langpack
                .getString("installer.cancel"));
        String title;
        // Use a alternate message if defined.
        final String key = "installer.reversetitle";
        String message = installdata.langpack.getString(key);
        // message equal to key -> no message defined.
        if (message.indexOf(key) > -1)
        {
            title = installdata.langpack.getString("installer.title")
                    + installdata.info.getAppName();
        }
        else
        { // Attention! The alternate message has to contain the whole message including
            // $APP_NAME and may be $APP_VER.
            VariableSubstitutor vs = new VariableSubstitutor(installdata.getVariables());
            title = vs.substitute(message, null);
        }
        new InstallerFrame(title, this.installdata,this);
    }

    /**
     * Returns whether flags should be used in the language selection dialog or not.
     *
     * @return whether flags should be used in the language selection dialog or not
     */
    protected boolean useFlags()
    {
        if (installdata.guiPrefs.modifier.containsKey("useFlags")
                && "no".equalsIgnoreCase(installdata.guiPrefs.modifier.get("useFlags")))
        {
            return (false);
        }
        return (true);
    }

    /**
     * Returns the type in which the language should be displayed in the language selction dialog.
     * Possible are "iso3", "native" and "usingDefault".
     *
     * @return language display type
     */
    protected String getLangType()
    {
        if (installdata.guiPrefs.modifier.containsKey("langDisplayType"))
        {
            String val = installdata.guiPrefs.modifier.get("langDisplayType");
            val = val.toLowerCase();
            // Verify that the value is valid, else return the default.
            for (String aLANGUAGE_DISPLAY_TYPES : LANGUAGE_DISPLAY_TYPES)
            {
                if (val.equalsIgnoreCase(aLANGUAGE_DISPLAY_TYPES))
                {
                    return (val);
                }
            }
            Debug.trace("Value for language display type not valid; value: " + val);
        }
        return (LANGUAGE_DISPLAY_TYPES[0]);
    }

    /**
     * Used to prompt the user for the language. Languages can be displayed in iso3 or the native
     * notation or the notation of the default locale. Revising to native notation is based on code
     * from Christian Murphy (patch #395).
     *
     * @author Julien Ponge
     * @author Christian Murphy
     * @author Klaus Bartz
     */
    private final class LanguageDialog extends JDialog implements ActionListener
    {

        private static final long serialVersionUID = 3256443616359887667L;

        /**
         * The combo box.
         */
        private JComboBox comboBox;

        /**
         * The ISO3 to ISO2 HashMap
         */
        private HashMap<String, String> iso3Toiso2 = null;

        /**
         * iso3Toiso2 expanded ?
         */
        private boolean isoMapExpanded = false;

        /**
         * The constructor.
         *
         * @param items The items to display in the box.
         */
        public LanguageDialog(JFrame frame, Object[] items)
        {
            super(frame);

            try
            {
                loadLookAndFeel();
            }
            catch (Exception err)
            {
                err.printStackTrace();
            }

            // We build the GUI
            addWindowListener(new WindowHandler());
            JPanel contentPane = (JPanel) getContentPane();
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
            if (getLangType().equals(LANGUAGE_DISPLAY_TYPES[0]))
            // iso3
            {
                firstMessage = "Please select your language below";
            }

            JLabel label1 = new JLabel(firstMessage, SwingConstants.LEADING);
            gbConstraints.gridy = 1;
            gbConstraints.insets = new Insets(15, 5, 5, 5);
            layout.addLayoutComponent(label1, gbConstraints);
            contentPane.add(label1);

            gbConstraints.insets = new Insets(5, 5, 5, 5);
            items = reviseItems(items);

            comboBox = new JComboBox(items);
            if (useFlags())
            {
                comboBox.setRenderer(new FlagRenderer());
            }
            gbConstraints.gridy = 3;
            layout.addLayoutComponent(comboBox, gbConstraints);
            contentPane.add(comboBox);

            gbConstraints.insets = new Insets(15, 5, 15, 5);
            JButton okButton = new JButton("OK");
            okButton.addActionListener(this);
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
        }

        /**
         * Revises iso3 language items depending on the language display type.
         *
         * @param items item array to be revised
         * @return the revised array
         */
        private Object[] reviseItems(Object[] items)
        {
            String langType = getLangType();
            // iso3: nothing todo.
            if (langType.equals(LANGUAGE_DISPLAY_TYPES[0]))
            {
                return (items);
            }
            // native: get the names as they are written in that language.
            if (langType.equals(LANGUAGE_DISPLAY_TYPES[1]))
            {
                return (expandItems(items, (new JComboBox()).getFont()));
            }
            // default: get the names as they are written in the default
            // language.
            if (langType.equals(LANGUAGE_DISPLAY_TYPES[2]))
            {
                return (expandItems(items, null));
            }
            // Should never be.
            return (items);
        }

        /**
         * Expands the given iso3 codes to language names. If a testFont is given, the codes are
         * tested whether they can displayed or not. If not, or no font given, the language name
         * will be returned as written in the default language of this VM.
         *
         * @param items    item array to be expanded to the language name
         * @param testFont font to test wheter a name is displayable
         * @return aray of expanded items
         */
        private Object[] expandItems(Object[] items, Font testFont)
        {
            int i;
            if (iso3Toiso2 == null)
            { // Loasd predefined langs into HashMap.
                iso3Toiso2 = new HashMap<String, String>(32);
                isoTable = new HashMap();
                for (i = 0; i < LANG_CODES.length; ++i)
                {
                    iso3Toiso2.put(LANG_CODES[i][0], LANG_CODES[i][1]);
                }
            }
            for (i = 0; i < items.length; i++)
            {
                Object it = expandItem(items[i], testFont);
                isoTable.put(it, items[i]);
                items[i] = it;
            }
            return items;
        }

        /**
         * Expands the given iso3 code to a language name. If a testFont is given, the code will be
         * tested whether it is displayable or not. If not, or no font given, the language name will
         * be returned as written in the default language of this VM.
         *
         * @param item     item to be expanded to the language name
         * @param testFont font to test wheter the name is displayable
         * @return expanded item
         */
        private Object expandItem(Object item, Font testFont)
        {
            Object iso2Str = iso3Toiso2.get(item);
            int i;
            if (iso2Str == null && !isoMapExpanded)
            { // Expand iso3toiso2 only if needed because it needs some time.
                isoMapExpanded = true;
                Locale[] loc = Locale.getAvailableLocales();
                for (i = 0; i < loc.length; ++i)
                {
                    iso3Toiso2.put(loc[i].getISO3Language(), loc[i].getLanguage());
                }
                iso2Str = iso3Toiso2.get(item);
            }
            if (iso2Str == null)
            // Unknown item, return it self.
            {
                return (item);
            }
            Locale locale = new Locale((String) iso2Str);
            if (testFont == null)
            // Return the language name in the spelling of the default locale.
            {
                return (locale.getDisplayLanguage());
            }
            // Get the language name in the spelling of that language.
            String str = locale.getDisplayLanguage(locale);
            int cdut = testFont.canDisplayUpTo(str);
            if (cdut > -1)
            // Test font cannot render it;
            // use language name in the spelling of the default locale.
            {
                str = locale.getDisplayLanguage();
            }
            return (str);
        }

        /**
         * Loads an image.
         *
         * @return The image icon.
         */
        public ImageIcon getImage()
        {
            ImageIcon img;
            try
            {
                img = new ImageIcon(LanguageDialog.class.getResource("/res/installer.langsel.img"));
            }
            catch (NullPointerException err)
            {
                img = null;
            }
            return img;
        }

        /**
         * Gets the selected object.
         *
         * @return The selected item.
         */
        public Object getSelection()
        {
            Object retval = null;
            if (isoTable != null)
            {
                retval = isoTable.get(comboBox.getSelectedItem());
            }
            return (retval != null) ? retval : comboBox.getSelectedItem();
        }

        /**
         * Sets the selection.
         *
         * @param item The item to be selected.
         */
        public void setSelection(Object item)
        {
            Object mapped = null;
            if (isoTable != null)
            {
                Iterator iter = isoTable.keySet().iterator();
                while (iter.hasNext())
                {
                    Object key = iter.next();
                    if (isoTable.get(key).equals(item))
                    {
                        mapped = key;
                        break;
                    }
                }
            }
            if (mapped == null)
            {
                mapped = item;
            }
            comboBox.setSelectedItem(mapped);
        }

        /**
         * Closer.
         *
         * @param e The event.
         */
        public void actionPerformed(ActionEvent e)
        {
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
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        }
    }

    /**
     * A list cell renderer that adds the flags on the display.
     *
     * @author Julien Ponge
     */
    private static class FlagRenderer extends JLabel implements ListCellRenderer
    {

        private static final long serialVersionUID = 3832899961942782769L;

        /**
         * Icons cache.
         */
        private TreeMap<String, ImageIcon> icons = new TreeMap<String, ImageIcon>();

        /**
         * Grayed icons cache.
         */
        private TreeMap<String, ImageIcon> grayIcons = new TreeMap<String, ImageIcon>();

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
        public Component getListCellRendererComponent(JList list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus)
        {
            // We put the label
            String iso3 = (String) value;
            setText(iso3);
            if (isoTable != null)
            {
                iso3 = (String) isoTable.get(iso3);
            }
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
            // We put the icon

            if (!icons.containsKey(iso3))
            {
                ImageIcon icon;
                icon = new ImageIcon(this.getClass().getResource("/res/flag." + iso3));
                icons.put(iso3, icon);
                icon = new ImageIcon(GrayFilter.createDisabledImage(icon.getImage()));
                grayIcons.put(iso3, icon);
            }
            if (isSelected || index == -1)
            {
                setIcon(icons.get(iso3));
            }
            else
            {
                setIcon(grayIcons.get(iso3));
            }

            // We return
            return this;
        }
    }
}

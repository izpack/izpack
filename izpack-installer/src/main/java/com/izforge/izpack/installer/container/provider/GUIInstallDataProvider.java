package com.izforge.izpack.installer.container.provider;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;

import com.izforge.izpack.api.data.GUIPrefs;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.PlatformModelMatcher;

/**
 * Provide installData for GUI :
 * Load install data with l&f and GUIPrefs
 */
public class GUIInstallDataProvider extends AbstractInstallDataProvider
{
    private static final Logger logger = Logger.getLogger(GUIInstallDataProvider.class.getName());

    public static final String MODIFIER_USE_BUTTON_ICONS = "useButtonIcons";
    public static final String MODIFIER_USE_LABEL_ICONS = "useLabelIcons";
    public static final String MODIFIER_LABEL_FONT_SIZE = "labelFontSize";

    private static Map<String, String> substanceVariants = new HashMap<String, String>();
    private static Map<String, String> looksVariants = new HashMap<String, String>();

    static
    {
        substanceVariants.put("default", "org.pushingpixels.substance.api.skin.SubstanceBusinessLookAndFeel");
        substanceVariants.put("business", "org.pushingpixels.substance.api.skin.SubstanceBusinessLookAndFeel");
        substanceVariants.put("business-blue",
                              "org.pushingpixels.substance.api.skin.SubstanceBusinessBlueSteelLookAndFeel");
        substanceVariants.put("business-black",
                              "org.pushingpixels.substance.api.skin.SubstanceBusinessBlackSteelLookAndFeel");
        substanceVariants.put("creme", "org.pushingpixels.substance.api.skin.SubstanceCremeLookAndFeel");
        substanceVariants.put("creme-coffee", "org.pushingpixels.substance.api.skin.SubstanceCremeCoffeeLookAndFeel");
        substanceVariants.put("sahara", "org.pushingpixels.substance.api.skin.SubstanceSaharaLookAndFeel");
        substanceVariants.put("graphite", "org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel");
        substanceVariants.put("moderate", "org.pushingpixels.substance.api.skin.SubstanceModerateLookAndFeel");
        substanceVariants.put("nebula", "org.pushingpixels.substance.api.skin.SubstanceNebulaLookAndFeel");
        substanceVariants.put("nebula-brick-wall",
                              "org.pushingpixels.substance.api.skin.SubstanceNebulaBrickWallLookAndFeel");
        substanceVariants.put("autumn", "org.pushingpixels.substance.api.skin.SubstanceAutumnLookAndFeel");
        substanceVariants.put("mist-silver", "org.pushingpixels.substance.api.skin.SubstanceMistSilverLookAndFeel");
        substanceVariants.put("mist-aqua", "org.pushingpixels.substance.api.skin.SubstanceMistAquaLookAndFeel");
        substanceVariants.put("dust", "org.pushingpixels.substance.api.skin.SubstanceDustLookAndFeel");
        substanceVariants.put("dust-coffee", "org.pushingpixels.substance.api.skin.SubstanceDustCoffeeLookAndFeel");
        substanceVariants.put("gemini", "org.pushingpixels.substance.api.skin.SubstanceGeminiLookAndFeel");
        substanceVariants.put("mariner", "org.pushingpixels.substance.api.skin.SubstanceMarinerLookAndFeel");
        substanceVariants.put("officesilver",
                              "org.pushingpixels.substance.api.skin.SubstanceOfficeSilver2007LookAndFeel");
        substanceVariants.put("officeblue", "org.pushingpixels.substance.api.skin.SubstanceOfficeBlue2007LookAndFeel");
        substanceVariants.put("officeblack",
                              "org.pushingpixels.substance.api.skin.SubstanceOfficeBlack2007LookAndFeel");

        looksVariants.put("windows", "com.jgoodies.looks.windows.WindowsLookAndFeel");
        looksVariants.put("plastic", "com.jgoodies.looks.plastic.PlasticLookAndFeel");
        looksVariants.put("plastic3D", "com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
        looksVariants.put("plasticXP", "com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
    }


    public GUIInstallData provide(Resources resources, Locales locales, DefaultVariables variables,
                                  Housekeeper housekeeper, PlatformModelMatcher matcher)
            throws Exception
    {
        final GUIInstallData guiInstallData = new GUIInstallData(variables, matcher.getCurrentPlatform());
        // Loads the installation data
        loadInstallData(guiInstallData, resources, matcher, housekeeper);
        loadGUIInstallData(guiInstallData, resources);
        loadInstallerRequirements(guiInstallData, resources);
        loadDynamicVariables(variables, guiInstallData, resources);
        loadDynamicConditions(guiInstallData, resources);
        loadDefaultLocale(guiInstallData, locales);
        // Load custom langpack if exist.
        addCustomLangpack(guiInstallData, locales);
        loadLookAndFeel(guiInstallData);
        if (UIManager.getColor("Button.background") != null)
        {
            guiInstallData.buttonsHColor = UIManager.getColor("Button.background");
        }
        return guiInstallData;
    }

    /**
     * Loads the suitable L&F.
     *
     * @param installData the installation data
     * @throws Exception Description of the Exception
     */
    protected void loadLookAndFeel(final GUIInstallData installData) throws Exception
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
        String lookAndFeelName = null;
        if (installData.guiPrefs.lookAndFeelMapping.containsKey(syskey))
        {
            lookAndFeelName = installData.guiPrefs.lookAndFeelMapping.get(syskey);
        }

        // Let's use the system LAF
        // Resolve whether button icons should be used or not.
        boolean useButtonIcons = true;
        if (installData.guiPrefs.modifier.containsKey(MODIFIER_USE_BUTTON_ICONS)
                && "no".equalsIgnoreCase(installData.guiPrefs.modifier
                        .get(MODIFIER_USE_BUTTON_ICONS)))
        {
            useButtonIcons = false;
        }
        ButtonFactory.useButtonIcons(useButtonIcons);
        boolean useLabelIcons = true;
        if (installData.guiPrefs.modifier.containsKey(MODIFIER_USE_LABEL_ICONS)
                && "no".equalsIgnoreCase(installData.guiPrefs.modifier
                                                 .get(MODIFIER_USE_LABEL_ICONS)))
        {
            useLabelIcons = false;
        }
        LabelFactory.setUseLabelIcons(useLabelIcons);
        if (installData.guiPrefs.modifier.containsKey(MODIFIER_LABEL_FONT_SIZE))
        {  //'labelFontSize' modifier found in 'guiprefs'
            final String valStr =
                    installData.guiPrefs.modifier.get(MODIFIER_LABEL_FONT_SIZE);
            try
            {      //parse value and enter as label-font-size multiplier:
                LabelFactory.setLabelFontSize(Float.parseFloat(valStr));
            }
            catch (NumberFormatException ex)
            {      //error parsing value; log message
                logger.warning("Error parsing guiprefs '"+MODIFIER_LABEL_FONT_SIZE+"' value (" +
                                       valStr + ')');
            }
        }

        if (lookAndFeelName == null)
        {
            if (!"mac".equals(syskey))
            {
                String syslaf = UIManager.getSystemLookAndFeelClassName();
                UIManager.setLookAndFeel(syslaf);
                if (UIManager.getLookAndFeel() instanceof MetalLookAndFeel)
                {
                    ButtonFactory.useButtonIcons(useButtonIcons);
                }
            }
            return;
        }

        // Kunststoff (http://www.incors.org/)
        if ("kunststoff".equals(lookAndFeelName))
        {
            ButtonFactory.useHighlightButtons();
            // Reset the use button icons state because useHighlightButtons
            // make it always true.
            ButtonFactory.useButtonIcons(useButtonIcons);
            installData.buttonsHColor = new Color(255, 255, 255);
            Class<LookAndFeel> lafClass = (Class<LookAndFeel>) Class.forName(
                    "com.incors.plaf.kunststoff.KunststoffLookAndFeel");
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
            return;
        }

        // Liquid (http://liquidlnf.sourceforge.net/)
        if ("liquid".equals(lookAndFeelName))
        {
            UIManager.setLookAndFeel("com.birosoft.liquid.LiquidLookAndFeel");

            Map<String, String> params = installData.guiPrefs.lookAndFeelParams.get(lookAndFeelName);
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
        if ("metouia".equals(lookAndFeelName))
        {
            UIManager.setLookAndFeel("net.sourceforge.mlf.metouia.MetouiaLookAndFeel");
            return;
        }

        // Nimbus
        if ("nimbus".equals(lookAndFeelName))
        {
            // Nimbus was included in JDK 6u10 but the packaging changed in JDK 7. Iterate to locate it
            // See http://docs.oracle.com/javase/tutorial/uiswing/lookandfeel/nimbus.html for more details
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
            {
                if ("Nimbus".equals(info.getName()))
                {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            return;
        }

        // JGoodies Looks (http://looks.dev.java.net/)
        if ("looks".equals(lookAndFeelName))
        {
            String variant = looksVariants.get("plasticXP");

            Map<String, String> params = installData.guiPrefs.lookAndFeelParams.get(lookAndFeelName);
            if (params.containsKey("variant"))
            {
                String param = params.get("variant");
                if (looksVariants.containsKey(param))
                {
                    variant = looksVariants.get(param);
                }
            }

            UIManager.setLookAndFeel(variant);
            return;
        }

        // Substance (http://substance.dev.java.net/)
        if ("substance".equals(lookAndFeelName))
        {
            final String variant;
            Map<String, String> params = installData.guiPrefs.lookAndFeelParams.get(lookAndFeelName);
            if (params.containsKey("variant"))
            {
                String param = params.get("variant");
                if (substanceVariants.containsKey(param))
                {
                    variant = substanceVariants.get(param);
                }
                else
                {
                    variant = substanceVariants.get("default");
                }
            }
            else
            {
                variant = substanceVariants.get("default");
            }
            logger.info("Using laf " + variant);
            UIManager.setLookAndFeel(variant);
            UIManager.getLookAndFeelDefaults().put("ClassLoader", JPanel.class.getClassLoader());

            checkSubstanceLafLoaded();
        }
    }

    private void checkSubstanceLafLoaded() throws ClassNotFoundException
    {
        UIDefaults defaults = UIManager.getDefaults();
        String uiClassName = (String) defaults.get("PanelUI");
        ClassLoader cl = (ClassLoader) defaults.get("ClassLoader");
        ClassLoader classLoader = (cl != null) ? cl : JPanel.class.getClassLoader();
        Class aClass = (Class) defaults.get(uiClassName);

        logger.info("PanelUI : " + uiClassName);
        logger.info("ClassLoader : " + classLoader);
        logger.info("Cached class : " + aClass);
        if (aClass != null)
        {
            return;
        }

        if (classLoader == null)
        {
            logger.info("Using system loader to load " + uiClassName);
            aClass = Class.forName(uiClassName, true, Thread.currentThread().getContextClassLoader());
            logger.info("Done loading");
        }
        else
        {
            logger.info("Using custom loader to load " + uiClassName);
            aClass = classLoader.loadClass(uiClassName);
            logger.info("Done loading");
        }
        if (aClass != null)
        {
            logger.info("Loaded class : " + aClass.getName());
        }
        else
        {
            logger.info("Couldn't load the class");
        }
    }

    /**
     * Load GUI preference information.
     *
     * @param installData the installation data
     * @throws Exception
     */
    private void loadGUIInstallData(GUIInstallData installData, Resources resources) throws Exception
    {
        installData.guiPrefs = (GUIPrefs) resources.getObject("GUIPrefs");
    }


}

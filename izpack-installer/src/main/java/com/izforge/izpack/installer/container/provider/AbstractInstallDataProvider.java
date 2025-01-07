package com.izforge.izpack.installer.container.provider;

import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.*;
import com.izforge.izpack.api.data.Info.TempDir;
import com.izforge.izpack.api.exception.ResourceException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.util.*;
import org.apache.commons.io.IOUtils;
import org.picocontainer.injectors.Provider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract base class for providers of {@link InstallData}.
 */
public abstract class AbstractInstallDataProvider implements Provider
{
    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(AbstractInstallDataProvider.class.getName());


    /**
     * Loads the installation data. Also sets environment variables to <code>installdata</code>.
     * All system properties are available as $SYSTEM_<variable> where <variable> is the actual
     * name _BUT_ with all separators replaced by '_'. Properties with null values are never stored.
     * Example: $SYSTEM_java_version or $SYSTEM_os_name
     *
     * @param installData the installation data to populate
     * @param resources   the resources
     * @param matcher     the platform-model matcher
     * @param housekeeper the housekeeper for cleaning up temporary files
     * @throws IOException            for any I/O error
     * @throws ClassNotFoundException if a serialized object's class cannot be found
     * @throws ResourceException      for any resource error
     */
    @SuppressWarnings("unchecked")
    protected void loadInstallData(AutomatedInstallData installData, Resources resources,
                                   PlatformModelMatcher matcher, Housekeeper housekeeper)
            throws IOException, ClassNotFoundException
    {
        // We load the Info data
        Info info = (Info) resources.getObject("info");

        // We put the Info data as variables
        installData.setVariable(ScriptParserConstant.APP_NAME, info.getAppName());
        if (info.getAppURL() != null)
        {
            installData.setVariable(ScriptParserConstant.APP_URL, info.getAppURL());
        }
        installData.setVariable(ScriptParserConstant.APP_VER, info.getAppVersion());
        if (info.getUninstallerCondition() != null)
        {
            installData.setVariable("UNINSTALLER_CONDITION", info.getUninstallerCondition());
        }

        installData.setInfo(info);
        // Set the installation path in a default manner
        String dir = getDir(resources);
        String installPath = dir + info.getAppName();
        if (info.getInstallationSubPath() != null)
        { // A sub-path was defined, use it.
            installPath = IoHelper.translatePath(dir + info.getInstallationSubPath(), installData.getVariables());
        }

        installData.setDefaultInstallPath(installPath);
        // Pre-set install path from a system property,
        // for instance in unattended installations
        installPath = System.getProperty(InstallData.INSTALL_PATH);
        if (installPath != null)
        {
            installData.setInstallPath(installPath);
        }

        // We read the panels order data
        List<Panel> panelsOrder = (List<Panel>) resources.getObject("panelsOrder");

        // We read the packs data
        InputStream in = resources.getInputStream("packs.info");
        ObjectInputStream objIn = new ObjectInputStream(in);
        List<PackInfo> packs;
        try
        {
            packs = (List<PackInfo>) objIn.readObject();
        }
        finally
        {
            IOUtils.closeQuietly(objIn);
        }

        List<Pack> availablePacks = new ArrayList<Pack>();
        List<Pack> allPacks = new ArrayList<Pack>();

        for (PackInfo packInfo : packs)
        {
            Pack pack = packInfo.getPack();
            allPacks.add(pack);
            if (matcher.matchesCurrentPlatform(pack.getOsConstraints()))
            {
                availablePacks.add(pack);
            }
        }
        setStandardVariables(installData, dir);

        // We load the user variables
        Properties properties = (Properties) resources.getObject("vars");
        if (properties != null)
        {
            Set<String> vars = properties.stringPropertyNames();
            for (String varName : vars)
            {
                installData.setVariable(varName, properties.getProperty(varName));
            }
        }

        installData.setPanelsOrder(panelsOrder);
        installData.setAvailablePacks(availablePacks);
        installData.setAllPacks(allPacks);

        // get list of preselected packs
        for (Pack availablePack : availablePacks)
        {
            if (availablePack.isPreselected())
            {
                installData.getSelectedPacks().add(availablePack);
            }
        }

        // Create any temp directories
        Set<TempDir> tempDirs = info.getTempDirs();
        if (null != tempDirs && tempDirs.size() > 0)
        {
            for (TempDir tempDir : tempDirs)
            {
                TemporaryDirectory directory = new TemporaryDirectory(tempDir, installData, housekeeper);
                directory.create();
                directory.deleteOnExit();
            }
        }
    }

    protected void setStandardVariables(AutomatedInstallData installData, String dir)
    {
        // Determine the hostname and IP address
        String hostname;
        String canonicalHostname;
        String IPAddress;

        try
        {
            InetAddress localHost = InetAddress.getLocalHost();
            IPAddress = localHost.getHostAddress();
            hostname = localHost.getHostName();
            canonicalHostname = localHost.getCanonicalHostName();
        }
        catch (Exception exception)
        {
            logger.log(Level.WARNING, "Failed to determine hostname and IP address", exception);
            hostname = "";
            canonicalHostname = "";
            IPAddress = "";
        }

        installData.setVariable("APPLICATIONS_DEFAULT_ROOT", dir);
        installData.setVariable(ScriptParserConstant.JAVA_HOME, System.getProperty("java.home"));
        installData.setVariable(ScriptParserConstant.CLASS_PATH, System.getProperty("java.class.path"));
        installData.setVariable(ScriptParserConstant.USER_HOME, System.getProperty("user.home"));
        installData.setVariable(ScriptParserConstant.USER_NAME, System.getProperty("user.name"));
        installData.setVariable(ScriptParserConstant.IP_ADDRESS, IPAddress);
        installData.setVariable(ScriptParserConstant.HOST_NAME, hostname);
        installData.setVariable(ScriptParserConstant.CANONICAL_HOST_NAME, canonicalHostname);
        installData.setVariable(ScriptParserConstant.FILE_SEPARATOR, File.separator);
    }

    /**
     * Add the contents of a custom langpack to the default langpack, if it exists.
     *
     * @param installData the install data to be used
     */
    public static void addCustomLangpack(AutomatedInstallData installData, Locales locales)
    {
        addLangpack(Resources.CUSTOM_TRANSLATIONS_RESOURCE_NAME, "custom", installData, locales);
    }

    /**
     * Add the contents of a custom langpack to the default langpack, if it exists.
     *
     * @param installData the install data to be used
     */
    public static void addUserInputLangpack(AutomatedInstallData installData, Locales locales)
    {
        addLangpack(Resources.USER_INPUT_TRANSLATIONS_RESOURCE_NAME, "user input", installData, locales);
    }

    private static void addLangpack(String resName, String langPackName, AutomatedInstallData installData, Locales locales)
    {
        // We try to load and add langpack.
        try
        {
            installData.getMessages().add(locales.getMessages(resName));
            logger.fine("Found " + langPackName + " langpack for " + installData.getLocaleISO3());
        }
        catch (ResourceNotFoundException exception)
        {
            logger.fine("No " + langPackName + " langpack for " + installData.getLocaleISO3() + " available");
        }
    }

    private String getDir(Resources resources)
    {
        // We determine the operating system and the initial installation path
        String dir;
        if (OsVersion.IS_WINDOWS)
        {
            dir = buildWindowsDefaultPath(resources);
        }
        else if (OsVersion.IS_OSX)
        {
            dir = "/Applications/";
        }
        else
        {
            if (new File("/usr/local/").canWrite())
            {
                dir = "/usr/local/";
            }
            else
            {
                dir = System.getProperty("user.home") + File.separatorChar;
            }
        }
        return dir;
    }

    /**
     * Get the default path for Windows (i.e Program Files/...).
     * Windows has a Setting for this in the environment and in the registry.
     * Just try to use the setting in the environment. If it fails for whatever reason, we take the former solution (buildWindowsDefaultPathFromProps).
     *
     * @param resources the resources
     * @return The Windows default installation path for applications.
     */
    private String buildWindowsDefaultPath(Resources resources)
    {
        try
        {
            //get value from environment...
            String prgFilesPath = System.getenv("ProgramFiles");
            if (prgFilesPath != null && prgFilesPath.length() > 0)
            {
                return prgFilesPath + File.separatorChar;
            }
            else
            {
                return buildWindowsDefaultPathFromProps(resources);
            }
        }
        catch (Exception exception)
        {
            logger.log(Level.WARNING, exception.getMessage(), exception);
            return buildWindowsDefaultPathFromProps(resources);
        }
    }

    /**
     * just plain wrong in case the programfiles are not stored where the developer expects them.
     * E.g. in custom installations of large companies or if used internationalized version of windows with a language pack.
     *
     * @return the program files path
     */
    private String buildWindowsDefaultPathFromProps(Resources resources)
    {
        StringBuilder result = new StringBuilder("");
        try
        {
            // We load the properties
            Properties props = new Properties();
            props.load(resources.getInputStream("/com/izforge/izpack/installer/win32-defaultpaths.properties"));

            // We look for the drive mapping
            String drive = System.getProperty("user.home");
            if (drive.length() > 3)
            {
                drive = drive.substring(0, 3);
            }

            // Now we have it :-)
            result.append(drive);

            // Ensure that we have a trailing backslash (in case drive was
            // something
            // like "C:")
            if (drive.length() == 2)
            {
                result.append("\\");
            }

            String language = Locale.getDefault().getLanguage();
            String country = Locale.getDefault().getCountry();
            String language_country = language + "_" + country;

            // Try the most specific combination first
            if (null != props.getProperty(language_country))
            {
                result.append(props.getProperty(language_country));
            }
            else if (null != props.getProperty(language))
            {
                result.append(props.getProperty(language));
            }
            else
            {
                result.append(props.getProperty(Locale.ENGLISH.getLanguage()));
            }
        }
        catch (Exception err)
        {
            result = new StringBuilder("C:\\Program Files");
        }

        return result.toString();
    }

    /**
     * Loads Dynamic Variables.
     *
     * @param variables   the collection to added variables to
     * @param installData the installation data
     */
    @SuppressWarnings("unchecked")
    protected void loadDynamicVariables(Variables variables, InstallData installData, Resources resources)
    {
        try
        {
            List<DynamicVariable> dynamicVariables = (List<DynamicVariable>) resources.getObject("dynvariables");
            for (DynamicVariable dynamic : dynamicVariables)
            {
                Value value = dynamic.getValue();
                value.setInstallData(installData);
                variables.add(dynamic);
            }
        }
        catch (Exception e)
        {
            logger.log(Level.WARNING, "Cannot find optional dynamic variables", e);
        }
    }

    /**
     * Loads dynamic conditions.
     *
     * @param installData the installation data
     * @param resources   the resources
     */
    @SuppressWarnings("unchecked")
    protected void loadDynamicConditions(AutomatedInstallData installData, Resources resources)
    {
        try
        {
            List<DynamicInstallerRequirementValidator> conditions
                    = (List<DynamicInstallerRequirementValidator>) resources.getObject("dynconditions");
            installData.setDynamicInstallerRequirements(conditions);
        }
        catch (Exception e)
        {
            logger.log(Level.WARNING, "Cannot find optional dynamic conditions", e);
        }
    }

    /**
     * Load installer conditions.
     *
     * @param installData the installation data
     * @throws IOException               for any I/O error
     * @throws ClassNotFoundException    if a serialized object's class cannot be found
     * @throws ResourceNotFoundException if the resource cannot be found
     */
    @SuppressWarnings("unchecked")
    protected void loadInstallerRequirements(AutomatedInstallData installData, Resources resources)
    {
        List<InstallerRequirement> requirements =
                (List<InstallerRequirement>) resources.getObject("installerrequirements");
        installData.setInstallerRequirements(requirements);
    }

    /**
     * Load a default locale in the installData
     *
     * @param installData the installation data
     * @param locales     the supported locales
     * @throws IOException for any I/O error
     */
    protected void loadDefaultLocale(AutomatedInstallData installData, Locales locales)
    {
        Locale locale = locales.getLocale();
        if (locale != null)
        {
            installData.setInstallationRecord(new XMLElementImpl("AutomatedInstallation"));
            installData.setLocale(locale, locales.getISOCode());
            installData.setMessages(locales.getMessages());
        }
    }

}

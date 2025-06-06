/*
 * Copyright 2016 Julien Ponge, René Krell and the IzPack team.
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

package com.izforge.izpack.compiler;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.IXMLParser;
import com.izforge.izpack.api.adaptator.IXMLWriter;
import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.adaptator.impl.XMLWriter;
import com.izforge.izpack.api.data.*;
import com.izforge.izpack.api.data.GUIPrefs.LookAndFeel;
import com.izforge.izpack.api.data.Info.TempDir;
import com.izforge.izpack.api.data.binding.Help;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.data.binding.Stage;
import com.izforge.izpack.api.exception.CompilerException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.handler.DefaultConfigurationHandler;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.api.installer.DataValidator.Status;
import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.SubstitutionType;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.data.PropertyManager;
import com.izforge.izpack.compiler.helper.AssertionHelper;
import com.izforge.izpack.compiler.helper.TargetFileSet;
import com.izforge.izpack.compiler.helper.XmlCompilerHelper;
import com.izforge.izpack.compiler.listener.CompilerListener;
import com.izforge.izpack.compiler.merge.CompilerPathResolver;
import com.izforge.izpack.compiler.packager.IPackager;
import com.izforge.izpack.compiler.resource.ResourceFinder;
import com.izforge.izpack.compiler.util.AntPathMatcher;
import com.izforge.izpack.compiler.util.CompilerClassLoader;
import com.izforge.izpack.compiler.util.compress.ArchiveStreamFactory;
import com.izforge.izpack.compiler.xml.*;
import com.izforge.izpack.core.data.DynamicInstallerRequirementValidatorImpl;
import com.izforge.izpack.core.data.DynamicVariableImpl;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.core.rules.process.PackSelectionCondition;
import com.izforge.izpack.core.variable.*;
import com.izforge.izpack.core.variable.filters.CaseStyleFilter;
import com.izforge.izpack.core.variable.filters.LocationFilter;
import com.izforge.izpack.core.variable.filters.RegularExpressionFilter;
import com.izforge.izpack.data.CustomData;
import com.izforge.izpack.data.DefaultConfigurationHandlerAdapter;
import com.izforge.izpack.data.PanelAction;
import com.izforge.izpack.event.AntAction;
import com.izforge.izpack.event.AntActionInstallerListener;
import com.izforge.izpack.event.ConfigurationInstallerListener;
import com.izforge.izpack.event.RegistryInstallerListener;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.installer.unpacker.IUnpacker;
import com.izforge.izpack.logging.FileFormatter;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.panels.packs.PackValidator;
import com.izforge.izpack.panels.process.ProcessPanelWorker;
import com.izforge.izpack.panels.shortcut.ShortcutConstants;
import com.izforge.izpack.panels.userinput.UserInputPanel;
import com.izforge.izpack.panels.userinput.field.FieldReader;
import com.izforge.izpack.panels.userinput.field.SimpleChoiceReader;
import com.izforge.izpack.panels.userinput.field.UserInputPanelSpec;
import com.izforge.izpack.panels.userinput.field.button.ButtonFieldReader;
import com.izforge.izpack.util.FileUtil;
import com.izforge.izpack.util.JavaVersion;
import com.izforge.izpack.util.OsConstraintHelper;
import com.izforge.izpack.util.PlatformModelMatcher;
import com.izforge.izpack.util.file.DirectoryScanner;
import com.izforge.izpack.util.helper.SpecHelper;
import jakarta.inject.Inject;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.java.util.jar.Pack200;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;
import java.util.logging.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.izforge.izpack.api.data.Info.EXPIRE_DATE_FORMAT;

/**
 * A parser for the installer xml configuration. This parses a document conforming to the
 * installation.dtd and populates a Compiler instance to perform the install compilation.
 *
 * @author Scott Stark
 * @version $Revision$
 */
public class CompilerConfig
{
    private static Logger logger;

    private static final File TEMP_DIR = FileUtils.getTempDirectory();

    /**
     * Constant for checking attributes.
     */
    private static final boolean YES = Boolean.TRUE;

    /**
     * Constant for checking attributes.
     */
    private static final Boolean NO = Boolean.FALSE;

    /**
     * Destination resources root path for native libraries in the installer jar
     */
    private static final String NATIVES_PATH = "com/izforge/izpack/bin/native/";

    /**
     * The installer packager compiler
     */
    private final Compiler compiler;

    /**
     * Installer data
     */
    private final CompilerData compilerData;

    /**
     * List of CompilerListeners which should be called at packaging
     */
    private final List<CompilerListener> compilerListeners = new ArrayList<CompilerListener>();

    /**
     * Maps condition IDs to XML elements referring to them for checking at the end of compilation
     * whether referenced conditions exist for all elements.
     */
    private final Map<String, List<IXMLElement>> referencedConditions = new HashMap<String, List<IXMLElement>>();

    /**
     * Maps condition IDs to XML elements in the UserInputSpec resource referring to them for checking at the end of
     * compilation whether referenced conditions exist for all elements.
     */
    private final Map<String, List<IXMLElement>> referencedConditionsUserInputSpec = new HashMap<String, List<IXMLElement>>();

    /**
     * Maps condition IDs to XML elements in the AntActionSpec resource referring to them for checking at the end of
     * compilation whether referenced conditions exist for all elements.
     */
    private final Map<String, List<IXMLElement>> referencedConditionsAntActionSpec = new HashMap<String, List<IXMLElement>>();

    /**
     * Maps condition pack names to XML elements in the AntActionSpec resource referring to them for checking at the end of
     * compilation whether referenced packs exist for all elements.
     */
    private final Map<String, IXMLElement> referencedPacksAntActionSpec = new HashMap<String, IXMLElement>();

    /**
     * Maps condition IDs to XML elements in the ConfigurationActionSpec resource referring to them for checking at the end of
     * compilation whether referenced conditions exist for all elements.
     */
    private final Map<String, List<IXMLElement>> referencedConditionsConfigurationActionSpec = new HashMap<String, List<IXMLElement>>();

    /**
     * Maps condition pack names to XML elements in the ConfigurationActionSpec resource referring to them for checking at the end of
     * compilation whether referenced packs exist for all elements.
     */
    private final Map<String, IXMLElement> referencedPacksConfigurationActionSpec = new HashMap<String, IXMLElement>();

    /**
     * UserInputPanel IDs for cross check whether given user input panel
     * referred in the installation descriptor are really defined
     */
    private Set<String> userInputPanelIds;

    private boolean optionalPacks = false;
    private String unpackerClassname = "com.izforge.izpack.installer.unpacker.Unpacker";
    private String packagerClassname = "com.izforge.izpack.compiler.packager.impl.Packager";
    private final CompilerPathResolver pathResolver;
    private final VariableSubstitutor variableSubstitutor;
    private final XmlCompilerHelper xmlCompilerHelper;
    private final PropertyManager propertyManager;
    private IPackager packager;
    private final ResourceFinder resourceFinder;
    private final MergeManager mergeManager;
    private final AssertionHelper assertionHelper;
    private final RulesEngine rules;

    /**
     * The factory for {@link CompilerListener} instances.
     */
    private final ObjectFactory factory;

    /**
     * The OS constraints.
     */
    private final PlatformModelMatcher constraints;

    /**
     * The class loader.
     */
    private final CompilerClassLoader classLoader;

    private static final String TEMP_DIR_ELEMENT_NAME = "tempdir";
    private static final String TEMP_DIR_PREFIX_ATTRIBUTE = "prefix";
    private static final String DEFAULT_TEMP_DIR_PREFIX = "IzPack";
    private static final String TEMP_DIR_SUFFIX_ATTRIBUTE = "suffix";
    private static final String DEFAULT_TEMP_DIR_SUFFIX = "Install";
    private static final String TEMP_DIR_VARIABLE_NAME_ATTRIBUTE = "variablename";
    private static final String TEMP_DIR_DEFAULT_PROPERTY_NAME = "TEMP_DIRECTORY";
    private static final String ISO3_ATTRIBUTE = "iso3";
    private static final String SRC_ATTRIBUTE = "src";
    private static final String DIR_ATTRIBUTE = "dir";

    /**
     * Help information.
     */
    private static final String HELP_TAG = "help";

    /**
     * Constructor
     *
     * @param compilerData Object containing all information found in command line
     */
    @Inject
    public CompilerConfig(CompilerData compilerData, VariableSubstitutor variableSubstitutor,
                          Compiler compiler, PropertyManager propertyManager, MergeManager mergeManager,
                          RulesEngine rules, CompilerPathResolver pathResolver,ResourceFinder resourceFinder,
                          ObjectFactory factory, PlatformModelMatcher constraints,
                          CompilerClassLoader classLoader, Handler handler)
    {
        this.assertionHelper = new AssertionHelper(compilerData.getInstallFile());
        this.rules = rules;
        this.compilerData = compilerData;
        this.variableSubstitutor = variableSubstitutor;
        this.compiler = compiler;
        this.xmlCompilerHelper = new XmlCompilerHelper(assertionHelper);
        this.propertyManager = propertyManager;
        this.mergeManager = mergeManager;
        this.pathResolver = pathResolver;
        this.resourceFinder = resourceFinder;
        this.factory = factory;
        this.constraints = constraints;
        this.classLoader = classLoader;

        Logger rootLogger = Logger.getLogger("com.izforge.izpack");
        rootLogger.setUseParentHandlers(false);

        if (handler != null)
        {
            boolean found = false;
            for (Handler rootHandler : rootLogger.getHandlers())
            {
                if (rootHandler.equals(handler))
                {
                    found = true;
                    break;
                }
                else
                {
                    rootLogger.removeHandler(rootHandler);
                }
            }
            if (!found)
            {
                rootLogger.addHandler(handler);
            }
            rootLogger.setLevel(handler.getLevel());
        }

        logger = Logger.getLogger(CompilerConfig.class.getName());
        logger.info("Logging initialized at level '" + logger.getParent().getLevel() + "'");
    }

    /**
     * Compiles the installation.
     *
     * @throws Exception Description of the Exception
     */
    public void executeCompiler() throws Exception
    {
        // normalize and test: TODO: may allow failure if we require write
        // access
        File base = new File(compilerData.getBasedir()).getAbsoluteFile();
        if (!base.canRead() || !base.isDirectory())
        {
            throw new CompilerException("Invalid base directory: " + base);
        }

        // add izpack built in property
        propertyManager.setProperty("basedir", base.toString());

        // We get the XML data tree
        IXMLParser parser = new InstallationXmlParser();
        IXMLElement data = resourceFinder.getXMLTree(parser);

        // construct compiler listeners to receive all further compiler events
        addCompilerListeners(data);

        // loads the specified packager
        loadPackagingInformation(data);

        // Read the properties and perform replacement on the rest of the tree
        substituteProperties(data);

        // We add all the information
        addNativeLibraries(data);
        addInfoStrings(data);
        addJars(data);
        addVariables(data);
        addConditions(data);
        addDynamicVariables(data);
        addDynamicInstallerRequirement(data);
        addInfoConditional(data);
        addConsolePrefs(data);
        addGUIPrefs(data);
        addLangpacks(data);
        addLogging(data);
        addResources(data);
        addPanelJars(data);
        addListenerJars(data);
        addPanels(data);
        addListeners(data);
        addPacks(data);
        resolveConditions(); // this should happen after add packs as it adds "izpack.selected.<pack name>" conditions
        addInstallerRequirement(data);
        checkReferencedConditions();
        checkReferencedPacks();

        // We ask the packager to create the installer
        compiler.createInstaller();
    }

    /**
     * Sets the packager.
     *
     * @param packager the packager
     */
    protected void setPackager(IPackager packager)
    {
        this.packager = packager;
    }

    private void addInstallerRequirement(IXMLElement data) throws CompilerException
    {
        notifyCompilerListener("addInstallerRequirement", CompilerListener.BEGIN, data);
        IXMLElement root = data.getFirstChildNamed("installerrequirements");
        List<InstallerRequirement> installerrequirements = new ArrayList<InstallerRequirement>();

        if (root != null)
        {
            List<IXMLElement> installerrequirementsels = root
                    .getChildrenNamed("installerrequirement");
            for (IXMLElement installerrequirement : installerrequirementsels)
            {
                InstallerRequirement basicInstallerCondition = new InstallerRequirement();
                String conditionId = parseConditionAttribute(installerrequirement);
                if (conditionId == null)
                {
                  assertionHelper.parseError(installerrequirement, "Missing condition attribute");
                }
                basicInstallerCondition.setCondition(conditionId);
                String message = installerrequirement.getAttribute("message");
                if (message == null)
                {
                  assertionHelper.parseError(installerrequirement, "Missing message attribute");
                }
                basicInstallerCondition.setMessage(message);
                installerrequirements.add(basicInstallerCondition);
            }
        }
        packager.addInstallerRequirements(installerrequirements);
        notifyCompilerListener("addInstallerRequirement", CompilerListener.END, data);
    }

    private void loadPackagingInformation(IXMLElement data) throws CompilerException
    {
        notifyCompilerListener("loadPackager", CompilerListener.BEGIN, data);
        // Initialisation
        // REFACTOR : Moved packager initialisation to provider
        IXMLElement root = data.getFirstChildNamed("packaging");
        IXMLElement packagerElement = null;
        if (root != null)
        {
            packagerElement = root.getFirstChildNamed("packager");

            if (packagerElement != null)
            {
                Class<IPackager> packagerClass = classLoader.loadClass(
                        xmlCompilerHelper.requireAttribute(packagerElement, "class"), IPackager.class);
                packagerClassname = packagerClass.getName();
            }

            IXMLElement unpacker = root.getFirstChildNamed("unpacker");

            if (unpacker != null)
            {
                Class<IUnpacker> unpackerClass = classLoader.loadClass(
                        xmlCompilerHelper.requireAttribute(unpacker, "class"), IUnpacker.class);
                unpackerClassname = unpackerClass.getName();
            }
        }
        packager = factory.create(packagerClassname, IPackager.class);
        if (packagerElement != null)
        {
            IXMLElement options = packagerElement.getFirstChildNamed("options");
            if (options != null)
            {
                packager.addConfigurationInformation(options);
            }
        }
        compiler.setPackager(packager);
        propertyManager.addProperty("UNPACKER_CLASS", unpackerClassname);
        notifyCompilerListener("loadPackager", CompilerListener.END, data);
    }

    public boolean wasSuccessful()
    {
        return compiler.wasSuccessful();
    }

    /**
     * Returns the ConsolePrefs.
     *
     * @param data The XML data.
     * @throws CompilerException Description of the Exception
     */
    private void addConsolePrefs(IXMLElement data) throws CompilerException
    {
        notifyCompilerListener("addConsolePrefs", CompilerListener.BEGIN, data);
        // We get the IXMLElement & the attributes
        IXMLElement consolePrefsElement = data.getFirstChildNamed("consoleprefs");
        ConsolePrefs prefs = new ConsolePrefs();
        if (consolePrefsElement != null)
        {
            IXMLElement detectTerminalTag = consolePrefsElement.getFirstChildNamed("detectTerminal");
            if (detectTerminalTag != null)
            {
                prefs.enableConsoleReader = Boolean.parseBoolean(xmlCompilerHelper.requireContent(detectTerminalTag));
            }
        }
        packager.setConsolePrefs(prefs);
        notifyCompilerListener("addConsolePrefs", CompilerListener.END, data);
    }

    /**
     * Returns the GUIPrefs.
     *
     * @param data The XML data.
     * @throws CompilerException Description of the Exception
     */
    private void addGUIPrefs(IXMLElement data) throws CompilerException
    {
        notifyCompilerListener("addGUIPrefs", CompilerListener.BEGIN, data);
        // We get the IXMLElement & the attributes
        IXMLElement guiPrefsElement = data.getFirstChildNamed("guiprefs");
        GUIPrefs prefs = new GUIPrefs();
        if (guiPrefsElement != null)
        {
            prefs.resizable = xmlCompilerHelper.requireYesNoAttribute(guiPrefsElement, "resizable");
            prefs.width = xmlCompilerHelper.requireIntAttribute(guiPrefsElement, "width");
            prefs.height = xmlCompilerHelper.requireIntAttribute(guiPrefsElement, "height");

            // Look and feel mappings
            for (IXMLElement lafNode : guiPrefsElement.getChildrenNamed("laf"))
            {
                String lafName = xmlCompilerHelper.requireAttribute(lafNode, "name");
                xmlCompilerHelper.requireChildNamed(lafNode, "os");

                LookAndFeel lookAndFeel = new LookAndFeel(lafName);

                for (IXMLElement parameterNode : lafNode.getChildrenNamed("param"))
                {
                    String name = xmlCompilerHelper.requireAttribute(parameterNode, "name");
                    String value = xmlCompilerHelper.requireAttribute(parameterNode, "value");
                    lookAndFeel.setParameter(name, value);
                }

                for (IXMLElement osNode : lafNode.getChildrenNamed("os"))
                {
                    String osName = xmlCompilerHelper.requireAttribute(osNode, "family");
                    prefs.lookAndFeelMapping.put(osName, lookAndFeel);
                }
            }
            // Load modifier
            for (IXMLElement ixmlElement : guiPrefsElement.getChildrenNamed("modifier"))
            {
                String key = xmlCompilerHelper.requireAttribute(ixmlElement, "key");
                String value = xmlCompilerHelper.requireAttribute(ixmlElement, "value");
                prefs.modifier.put(key, value);

            }
            for (LookAndFeel lookAndFeel : prefs.lookAndFeelMapping.values())
            {
                String lafName = lookAndFeel.getName();
                LookAndFeels feels = LookAndFeels.lookup(lafName);
                if (feels != null)
                {
                    List<Mergeable> mergeableList = Collections.emptyList();
                    switch (feels)
                    {
                        case KUNSTSTOFF:
                            mergeableList = pathResolver.getMergeableFromPackageName("com/incors/plaf");
                            break;
                        case LOOKS:
                            mergeableList = pathResolver.getMergeableFromPackageName("com/jgoodies");
                            break;
                        case SUBSTANCE:

                            if (getMinJavaVersion() > 8 )
                            {
                                // Add RADIANCE only
                                mergeableList = pathResolver.getMergeableJarFromPackageName("org/pushingpixels/radiance");
                            }
                            else if (compilerData.getExternalInfo().getJavaVersionStrict())
                            {
                                // Add SUBSTANCE only
                                mergeableList = pathResolver.getMergeableJarFromPackageName("org/pushingpixels/substance");
                            }
                            else
                            {
                                // Add SUBSTANCE and RADIANCE
                                mergeableList = pathResolver.getMergeableJarFromPackageName("org/pushingpixels");
                            }

                            break;

                        case NIMBUS:
                            // Nimbus was included in JDK 6u10, and in JDK7 changed packages.
                            // mergeableList = pathResolver.getMergeableFromPackageName("com/sun/java/swing/plaf/nimbus");
                            break;
                        default:
                            assertionHelper.parseError(guiPrefsElement, "Unrecognized Look and Feel: " + lafName);
                    }
                    for (Mergeable mergeable : mergeableList)
                    {
                        mergeManager.addResourceToMerge(mergeable);
                    }
                }
                else
                {
                    assertionHelper.parseError(guiPrefsElement, "Unrecognized Look and Feel: " + lafName);
                }
            }
        }
        packager.setGUIPrefs(prefs);
        notifyCompilerListener("addGUIPrefs", CompilerListener.END, data);
    }

    /**
     * Adds jars specified by {@code <jar src=.... />}.
     *
     * @param data the XML install data
     * @throws CompilerException if a required attribute is not present
     * @throws IOException       if the jar cannot be read
     */
    private void addJars(IXMLElement data) throws IOException
    {
        notifyCompilerListener("addJars", CompilerListener.BEGIN, data);
        final String minimalJavaVersion = compilerData.getExternalInfo().getJavaVersion();
        final boolean javaVersionStrict = compilerData.getExternalInfo().getJavaVersionStrict();
        for (IXMLElement ixmlElement : data.getChildrenNamed("jar"))
        {
            String src = getSrcSubstitutedAttributeValue(ixmlElement);

            // all external jars contents regardless of stage type are merged into the installer
            // but we keep a copy of jar entries that user want to merge into uninstaller
            // as "customData", where the installer will get them into uninstaller.jar at the end of installation
            // note if stage is empty or null, it is the same at 'install'
            String stage = ixmlElement.getAttribute("stage");
            URL url = resourceFinder.findProjectResource(src, "Jar file", ixmlElement);
            boolean uninstaller = "both".equalsIgnoreCase(stage) || "uninstall".equalsIgnoreCase(stage);
            compiler.checkJarVersions(FileUtil.convertUrlToFile(url), minimalJavaVersion);
            if (!compiler.getJavaVersionCorrect())
            {
                if (javaVersionStrict)
                {
                    throw new CompilerException(url.getFile() + " does not meet the minimal version requirements."
                            + "\nRequired minimal target Java version: " + minimalJavaVersion
                            + "\nFound class target Java version: 1." + compiler.getJavaVersionExpected());
                }
                else
                {
                    logger.warning(url.getFile() + " does not meet the minimal version requirements which may cause issues during runtime."
                            + "\nRequired minimal target Java version: " + minimalJavaVersion
                            + "\nFound class target Java version: 1." + compiler.getJavaVersionExpected());
                }
            }
            compiler.addJar(url, uninstaller);
        }
        notifyCompilerListener("addJars", CompilerListener.END, data);
    }

    /**
     * Adds jars specified by {@code <panel jar=.../>;}
     *
     * @param data the XML install data
     * @throws IOException if the jar cannot be read
     */
    private void addPanelJars(IXMLElement data) throws IOException
    {
        notifyCompilerListener("addPanelJars", CompilerListener.BEGIN, data);

        IXMLElement panels = xmlCompilerHelper.requireChildNamed(data, "panels");
        for (IXMLElement panel : panels.getChildrenNamed("panel"))
        {
            URL url = getPanelJarURL(panel);
            if (url != null)
            {
                compiler.addJar(url, false);
            }
        }
        notifyCompilerListener("addPanelJars", CompilerListener.END, data);
    }

    /**
     * Returns the URL for a panel jar, given the panel configuration.
     *
     * @param panel the panel configuration
     * @return the panel jar URL, or <tt>null</tt> if there is none
     * @throws CompilerException if a jar is specified but cannot be found
     */
    private URL getPanelJarURL(IXMLElement panel) throws CompilerException
    {
        return getJarResourceURL(panel, "Panel jar file");
    }

    /**
     * Returns the URL for a listener jar, given the listener configuration.
     *
     * @param listener the listener configuration
     * @return the listener jar URL, or <tt>null</tt> if there is none
     * @throws CompilerException if a jar is specified but cannot be found
     */
    private URL getListenerJarURL(IXMLElement listener) throws CompilerException
    {
        return getJarResourceURL(listener, "Listener jar file");
    }

    /**
     * Helper to return a resource URL given the XML configuration and resource attribute name.
     *
     * @param element     the element
     * @param description a description of the resource, for error reporting purposes
     * @return the resource URL, or <tt>null</tt> if the attribute is not set
     * @throws CompilerException if an attribute value exists, but the corresponding resource cannot be found
     */
    private URL getJarResourceURL(IXMLElement element, String description) throws CompilerException
    {
        String value = element.getAttribute("jar");
        if (!StringUtils.isEmpty(value))
        {
            return resourceFinder.findIzPackResource(value, description, element, false);
        }
        return null;
    }

    /**
     * Adds jars specified by {@code <listener jar=.../>;}
     *
     * @param data the XML install data
     * @throws com.izforge.izpack.api.exception.CompilerException
     *                     if the jar cannot be found
     * @throws IOException if the jar cannot be read
     */
    private void addListenerJars(IXMLElement data) throws IOException
    {
        notifyCompilerListener("addListenerJars", CompilerListener.BEGIN, data);
        IXMLElement listeners = data.getFirstChildNamed("listeners");
        if (listeners != null)
        {
            for (IXMLElement listener : listeners.getChildrenNamed("listener"))
            {
                Stage stage = Stage.valueOf(xmlCompilerHelper.requireAttribute(listener, "stage"));
                if (Stage.isInInstaller(stage))
                {
                    URL url = getListenerJarURL(listener);
                    if (url != null)
                    {
                        compiler.addJar(url, stage == Stage.uninstall);
                    }
                }
            }
        }
        notifyCompilerListener("addListenerJars", CompilerListener.END, data);
    }

    /**
     * Add native libraries to the installer.
     *
     * @param data The XML data.
     */
    private void addNativeLibraries(IXMLElement data)
    {
        notifyCompilerListener("addNativeLibraries", CompilerListener.BEGIN, data);
        IXMLElement nativesElement = data.getFirstChildNamed("natives");
        if (nativesElement == null)
        {
            return;
        }
        for (IXMLElement ixmlElement : nativesElement.getChildrenNamed("native"))
        {
            String type = xmlCompilerHelper.requireAttribute(ixmlElement, "type");
            String name = xmlCompilerHelper.requireAttribute(ixmlElement, "name");
            String path = ixmlElement.getAttribute(SRC_ATTRIBUTE);
            if (path == null)
            {
                path = NATIVES_PATH + type + "/" + name;
            }
            String destination = NATIVES_PATH + type + "/" + name;
            mergeManager.addResourceToMerge(path, destination);

            // Additionally marking a native lib to be also used in the uninstaller.
            // The lib will be copied from the installer into the uninstaller if needed.
            // Therefore the lib should be in the installer also it is used only
            // from the uninstaller. This is the reason why the stage wiil be only
            // observed for the uninstaller.
            // TODO Remove this in future
            @Deprecated String stage = ixmlElement.getAttribute("stage");
            if (stage != null)
            {
                assertionHelper.parseWarn(ixmlElement,
                        "The 'stage' attribute is deprecated here and might be removed in future."
                        + " Use the new attribute 'uninstaller' as replacement."
                );
            }

            if (Boolean.parseBoolean(ixmlElement.getAttribute("uninstaller", Boolean.FALSE.toString()))
                /*deprecated:*/ || "both".equalsIgnoreCase(stage) || "uninstall".equalsIgnoreCase(stage))
            {
                List<OsModel> constraints = OsConstraintHelper.getOsList(ixmlElement);
                List<String> contents = new ArrayList<String>();
                contents.add(destination);
                CustomData customData = new CustomData(null, contents, constraints, CustomData.UNINSTALLER_LIB);
                packager.addNativeUninstallerLibrary(customData);
            }
        }
        notifyCompilerListener("addNativeLibraries", CompilerListener.END, data);
    }

    /**
     * Add packs and their contents to the installer.
     *
     * @param data The XML data.
     */
    private void addPacks(IXMLElement data) throws CompilerException
    {
        notifyCompilerListener("addPacks", CompilerListener.BEGIN, data);

        // the actual adding is delegated to addPacksSingle to enable recursive
        // parsing of refpack package definitions
        addPacksSingle(data, new File(compilerData.getBasedir()));

        compiler.checkDependencies();
        compiler.checkExcludes();

        checkOptionalPacks();
        notifyCompilerListener("addPacks", CompilerListener.END, data);
    }

    private void checkOptionalPacks() {
        if (optionalPacks) {
            List<Panel> panels = packager.getPanelList();
            boolean packsPanelSpecified = false;
            for (Panel panel : panels)
            {
                String className = panel.getClassName();
                if (className.equals("com.izforge.izpack.panels.imgpacks.ImgPacksPanel") ||
                        className.equals("com.izforge.izpack.panels.packs.PacksPanel") ||
                        className.equals("com.izforge.izpack.panels.treepacks.TreePacksPanel"))
                {
                    packsPanelSpecified = true;
                    break;
                }
            }
            if (!packsPanelSpecified)
            {
                // if there are optional packs and no ImgPacksPanel, PacksPanel or TreePacksPanel specified then we just
                // inform the user as there maybe custom panel providing similar functionality
                logger.info("There are optional packs specified but none of the ImgPacksPanel, PacksPanel and TreePacksPanel defined.");
            }
        }
    }

    /**
     * Add packs and their contents to the installer without checking the dependencies and includes.
     * <p/>
     * Helper method to recursively add more packs from refpack XML packs definitions
     *
     * @param data The XML data
     * @param baseDir the base directory of the pack
     * @throws CompilerException an error occurred during compiling
     */
    private void addPacksSingle(IXMLElement data, File baseDir) throws CompilerException
    {
        notifyCompilerListener("addPacksSingle", CompilerListener.BEGIN, data);
        // Initialisation
        IXMLElement root = xmlCompilerHelper.requireChildNamed(data, "packs");

        // at least one pack is required
        List<IXMLElement> packElements = root.getChildrenNamed("pack");
        List<IXMLElement> refPackElements = root.getChildrenNamed("refpack");
        List<IXMLElement> refPackSets = root.getChildrenNamed("refpackset");
        if (packElements.isEmpty() && refPackElements.isEmpty() && refPackSets.isEmpty())
        {
            assertionHelper.parseError(root, "<packs> requires a <pack>, <refpack> or <refpackset>");
        }

        for (IXMLElement packElement : packElements)
        {

            // Trivial initialisations
            String name = xmlCompilerHelper.requireAttribute(packElement, "name");
            String id = packElement.getAttribute("id");
            String packImgId = packElement.getAttribute("packImgId");

            boolean loose = Boolean.parseBoolean(packElement.getAttribute("loose", "false"));
            String description = xmlCompilerHelper.requireChildNamed(packElement, "description").getContent();
            boolean hidden = Boolean.parseBoolean(packElement.getAttribute("hidden", "false"));
            boolean required = hidden ? true : xmlCompilerHelper.requireYesNoAttribute(packElement, "required");
            if (!required)
            {
                optionalPacks = true;
            }
            String group = packElement.getAttribute("group");
            String installGroups = packElement.getAttribute("installGroups");
            String excludeGroup = packElement.getAttribute("excludeGroup");
            boolean uninstall = xmlCompilerHelper.validateYesNoAttribute(packElement, "uninstall", YES);
            long size = xmlCompilerHelper.getLong(packElement, "size", 0);
            String parent = packElement.getAttribute("parent");

            String conditionId = parseConditionAttribute(packElement);

            if (required && excludeGroup != null)
            {
                assertionHelper.parseError(packElement, "Pack, which has excludeGroup can not be required.",
                                           new Exception(
                                                   "Pack, which has excludeGroup can not be required."));
            }

            PackInfo pack = new PackInfo(name, id, description, required, loose, excludeGroup,
                                         uninstall, size);
            pack.setOsConstraints(OsConstraintHelper.getOsList(packElement)); // TODO:
            pack.setParent(parent);
            if (conditionId != null)
            {
                pack.setCondition(conditionId);
            }
            pack.setHidden(hidden);

            // unverified
            // if the pack belongs to an excludeGroup it's not preselected by default
            if (excludeGroup == null)
            {
                pack.setPreselected(xmlCompilerHelper.validateYesNoAttribute(packElement, "preselected", YES));
            }
            else
            {
                pack.setPreselected(xmlCompilerHelper.validateYesNoAttribute(packElement, "preselected", NO));
            }

            // Set the pack group if specified
            if (group != null)
            {
                pack.setGroup(group);
            }
            // Set the pack install groups if specified
            if (installGroups != null)
            {
                StringTokenizer st = new StringTokenizer(installGroups, ",");
                while (st.hasMoreTokens())
                {
                    String igroup = st.nextToken();
                    pack.addInstallGroup(igroup);
                }
            }

            // Set the packImgId if specified
            if (packImgId != null)
            {
                pack.setPackImgId(packImgId);
            }

            processFileChildren(baseDir, packElement, pack);

            processSingleFileChildren(baseDir, packElement, pack);

            processFileSetChildren(baseDir, packElement, pack);

            processUpdateCheckChildren(packElement, pack);

            processOnSelect(packElement, pack);

            processOnDeselect(packElement, pack);

            List<IXMLElement> parsableChildren = packElement.getChildrenNamed("parsable");
            processParsableChildren(pack, parsableChildren);

            List<IXMLElement> executableChildren = packElement.getChildrenNamed("executable");
            processExecutableChildren(pack, executableChildren);

            // We get the dependencies
            for (IXMLElement dependsNode : packElement.getChildrenNamed("depends"))
            {
                String depName = xmlCompilerHelper.requireAttribute(dependsNode, "packname");
                pack.addDependency(depName);

            }

            for (IXMLElement validator : packElement.getChildrenNamed("validator"))
            {
                Class<PackValidator> type = classLoader.loadClass(xmlCompilerHelper.requireContent(validator),
                                                                  PackValidator.class);
                pack.addValidator(type.getName());
            }

            PackSelectionCondition selectionCondition = new PackSelectionCondition();
            selectionCondition.setId("izpack.selected." + name);
            selectionCondition.setPack(name);
            rules.addCondition(selectionCondition);

            logAddingPack(pack);

            // We add the pack
            packager.addPack(pack);
        }

        for (IXMLElement refPackElement : refPackElements)
        {

            // get the name of reference xml file
            String refFileName = xmlCompilerHelper.requireAttribute(refPackElement, "file");
            String selfcontained = refPackElement.getAttribute("selfcontained");
            boolean isselfcontained = Boolean.parseBoolean(selfcontained);

            // parsing ref-pack- file
            logger.info("Reading refpack from " + refFileName + " in dir " + baseDir);
            IXMLElement refXMLData = this.readRefPackData(baseDir, refFileName, isselfcontained);

            // Recursively call myself to add all packs and refpacks from the reference XML
            addPacksSingle(refXMLData, baseDir);
        }

        for (IXMLElement refPackSet : refPackSets)
        {

            // the directory to scan
            String dir_attr = xmlCompilerHelper.requireAttribute(refPackSet, DIR_ATTRIBUTE);

            File dir = new File(dir_attr);
            if (!dir.isAbsolute())
            {
                dir = new File(baseDir, dir_attr);
            }
            if (!dir.isDirectory()) // also tests '.exists()'
            {
                assertionHelper.parseError(refPackSet, "Invalid refpackset directory 'dir': " + dir_attr);
            }

            // include pattern
            String includeString = xmlCompilerHelper.requireAttribute(refPackSet, "includes");
            String[] includes = includeString.split(", ");

            // scan for refpack files
            DirectoryScanner ds = new DirectoryScanner();
            ds.setIncludes(includes);
            ds.setBasedir(dir);
            ds.setCaseSensitive(true);

            // loop through all found files and handle them as normal refpack files
            String[] files;
            try
            {
                ds.scan();

                files = ds.getIncludedFiles();
                for (String file : files)
                {
                    File refFile = new File(dir, file);
                    File packDir;
                    if (refFile.isAbsolute()) {
                        packDir = refFile.getParentFile();
                    } else {
                        packDir = new File(baseDir, refFile.getParent());
                    }

                    // parsing ref-pack-set file
                    IXMLElement refXMLData = this.readRefPackData(packDir, refFile.getName(), false);

                    // Recursively call myself to add all packs and refpacks from the reference XML
                    addPacksSingle(refXMLData, packDir);
                }
            }
            catch (Exception e)
            {
                throw new CompilerException(getMessage(e) + " while processing refpackset " + refPackSet.getName());
            }
        }

        notifyCompilerListener("addPacksSingle", CompilerListener.END, data);
    }

    private void processUpdateCheckChildren(IXMLElement packElement, PackInfo pack) throws CompilerException
    {
        for (IXMLElement updateNode : packElement.getChildrenNamed("updatecheck"))
        {
            // get includes and excludes
            ArrayList<String> includesList = new ArrayList<String>();
            ArrayList<String> excludesList = new ArrayList<String>();

            // get includes and excludes
            for (IXMLElement ixmlElement1 : updateNode.getChildrenNamed("include"))
            {
                includesList.add(xmlCompilerHelper.requireAttribute(ixmlElement1, "name"));
            }

            for (IXMLElement ixmlElement : updateNode.getChildrenNamed("exclude"))
            {
                excludesList.add(xmlCompilerHelper.requireAttribute(ixmlElement, "name"));
            }

            pack.addUpdateCheck(new UpdateCheck(includesList, excludesList));
        }
    }

    private void processFileSetChildren(File baseDir, IXMLElement packElement, PackInfo pack) throws CompilerException
    {
        try
        {
            for (TargetFileSet fs : readFileSets(packElement, baseDir))
            {
                processFileSetChildren(fs, baseDir, null, pack);
            }
        }
        catch (Exception e)
        {
            assertionHelper.parseError(packElement, e.getMessage(), e);
        }
    }

    private void processFileSetChildren(TargetFileSet fs, File baseDir, List<OsModel> parentOsList, PackInfo pack) throws Exception
    {
        String[][] includedFilesAndDirs = new String[][]{
                fs.getDirectoryScanner().getIncludedDirectories(),
                fs.getDirectoryScanner().getIncludedFiles()
        };
        for (String[] filesOrDirs : includedFilesAndDirs)
        {
            if (filesOrDirs != null)
            {
                for (String filePath : filesOrDirs)
                {
                    if (!filePath.isEmpty()) // not the basedir itself
                    {
                        File file = new File(fs.getDir(), filePath);
                        String target = new File(fs.getTargetDir(), filePath).getPath();
                        List<OsModel> osList = fs.getOsList();
                        
                        if (parentOsList != null && !parentOsList.isEmpty())
                        {
                            // get list of OS constraints safisfiying both parent's and fs's
                            try
                            {
                                osList = OsConstraintHelper.commonOsList(parentOsList, fs.getOsList());
                                logCombineOsLists(parentOsList, fs.getOsList(), osList);
                            }
                            catch (OsConstraintHelper.UnsatisfiableOsConstraintsException ex)
                            {
                                throw new CompilerException(ex.getMessage());
                            }
                        }
                        
                        logAddingFile(file.toString(), target);
                        pack.addFile(baseDir, file, target, osList,
                                     fs.getOverride(), fs.getOverrideRenameTo(),
                                     fs.getBlockable(), fs.getAdditionals(), fs.getCondition());
                    }
                }
            }
        }
    }

    /**
     * Process onSelect tags within pack tags
     * @param packElement pack XML element
     * @param pack object holding pack information
     */
    private void processOnSelect(IXMLElement packElement, PackInfo pack)
    {
        for (IXMLElement selectNode : packElement.getChildrenNamed("onSelect"))
        {
            String name = xmlCompilerHelper.requireAttribute(selectNode, "name");
            String conditionId = parseConditionAttribute(selectNode);
            pack.addOnSelect(name, conditionId);
        }
    }

    /**
     * Process onDeselect tags within pack tags
     * @param packElement pack XML element
     * @param pack object holding pack information
     */
    private void processOnDeselect(IXMLElement packElement, PackInfo pack)
    {
        for (IXMLElement deselectNode : packElement.getChildrenNamed("onDeselect"))
        {
            String name = xmlCompilerHelper.requireAttribute(deselectNode, "name");
            String condition = parseConditionAttribute(deselectNode);
            pack.addOnDeselect(name, condition);
        }
    }

    private void processSingleFileChildren(File baseDir, IXMLElement packElement, PackInfo pack)
            throws CompilerException
    {
        for (IXMLElement singleFileNode : packElement.getChildrenNamed("singlefile"))
        {
            String src = getSrcSubstitutedAttributeValue(singleFileNode);
            String target = xmlCompilerHelper.requireAttribute(singleFileNode, "target");
            List<OsModel> osList = OsConstraintHelper.getOsList(singleFileNode); // TODO: unverified
            OverrideType override = getOverrideValue(singleFileNode);
            String overrideRenameTo = getOverrideRenameToValue(singleFileNode);
            Blockable blockable = getBlockableValue(singleFileNode, osList);
            Map<String, ?> additionals = getAdditionals(singleFileNode);
            String conditionId = parseConditionAttribute(singleFileNode);
           
            File file = new File(src);
            if (!file.isAbsolute())
            {
                file = new File(baseDir, src);
            }

            if (!file.exists())
            {
                assertionHelper.parseWarn(singleFileNode, "Source file " + src + " (" + file + ") not found");
                // next existance checking appears in pack.addFile
            }

            try
            {
                logAddingFile(file.toString(), target);
                pack.addFile(baseDir, file, target, osList, override, overrideRenameTo, blockable,
                             additionals, conditionId);
            }
            catch (IOException x)
            {
                assertionHelper.parseError(singleFileNode, x.getMessage(), x);
            }
        }
    }

    private void processFileChildren(File baseDir, IXMLElement packElement, PackInfo pack) throws CompilerException
    {
        for (IXMLElement fileNode : packElement.getChildrenNamed("file"))
        {
            String src = getSrcSubstitutedAttributeValue(fileNode);
            boolean unpack = Boolean.parseBoolean(fileNode.getAttribute("unpack"));

            TargetFileSet fs = new TargetFileSet();
            try
            {
                File relsrcfile = new File(src);
                File abssrcfile = FileUtil.getAbsoluteFile(src, baseDir.getAbsolutePath());
                if (!abssrcfile.exists())
                {
                    throw new FileNotFoundException("Source file " + relsrcfile + " (" + abssrcfile + ") not found");
                }
                if (relsrcfile.isDirectory())
                {
                    fs.setDir(abssrcfile.getParentFile());
                    fs.createInclude().setName(relsrcfile.getName() + "/**");
                }
                else
                {
                    fs.setFile(abssrcfile);
                }

                fs.setTargetDir(fileNode.getAttribute("targetdir", "${INSTALL_PATH}"));
                List<OsModel> osList = OsConstraintHelper.getOsList(fileNode); // TODO: unverified
                fs.setOsList(osList);
                fs.setOverride(getOverrideValue(fileNode));
                fs.setOverrideRenameTo(getOverrideRenameToValue(fileNode));
                fs.setBlockable(getBlockableValue(fileNode, osList));
                fs.setAdditionals(getAdditionals(fileNode));
                fs.setCondition(parseConditionAttribute(fileNode));

                String boolval = fileNode.getAttribute("casesensitive");
                if (boolval != null)
                {
                    fs.setCaseSensitive(Boolean.parseBoolean(boolval));
                }

                boolval = fileNode.getAttribute("defaultexcludes");
                if (boolval != null)
                {
                    fs.setDefaultexcludes(Boolean.parseBoolean(boolval));
                }

                boolval = fileNode.getAttribute("followsymlinks");
                if (boolval != null)
                {
                    fs.setFollowSymlinks(Boolean.parseBoolean(boolval));
                }

                LinkedList<String> srcfiles = new LinkedList<String>();
                Collections.addAll(srcfiles, fs.getDirectoryScanner().getIncludedDirectories());
                Collections.addAll(srcfiles, fs.getDirectoryScanner().getIncludedFiles());
                for (String filePath : srcfiles)
                {
                    if (!filePath.isEmpty())
                    {
                        abssrcfile = new File(fs.getDir(), filePath);
                        if (unpack)
                        {
                            logger.info("Adding content from archive: " + abssrcfile);
                            addArchiveContent(fileNode, baseDir, abssrcfile, fs.getTargetDir(),
                                              fs.getOsList(), fs.getOverride(), fs.getOverrideRenameTo(),
                                              fs.getBlockable(), pack, fs.getAdditionals(), fs.getCondition());
                        }
                        else
                        {
                            String target = fs.getTargetDir() + "/" + filePath;
                            logAddingFile(abssrcfile.toString(), target);
                            pack.addFile(baseDir, abssrcfile, target, fs.getOsList(),
                                         fs.getOverride(), fs.getOverrideRenameTo(), fs.getBlockable(),
                                         fs.getAdditionals(), fs.getCondition());
                        }
                    }
                }
            }
            catch (Exception e)
            {
                throw new CompilerException(getMessage(e) + " while processing " + src, e);
            }
        }
    }

    private void addNotNullAttribute(Map<String, String> map, String key, IXMLElement element, String attrName)
    {
        String attr = element.getAttribute(attrName);
        if (attr != null)
        {
            map.put(key, attr);
        }
    }

    private void addNotNullAttributeIfTrue(Map<String, String> map, String key, String value, IXMLElement element, String attrName)
    {
        String attr = element.getAttribute(attrName);
        if (attr != null)
        {
            if (Boolean.parseBoolean(attr))
            {
                map.put(key, value);
            }
        }
    }

    private void processExecutableChildren(PackInfo pack, List<IXMLElement> childrenNamed) throws CompilerException
    {
        for (IXMLElement executableNode : childrenNamed)
        {
            String target = executableNode.getAttribute("targetfile");
            String conditionId = parseConditionAttribute(executableNode);
            List<OsModel> osList = OsConstraintHelper.getOsList(executableNode); // TODO: unverified
            int executionStage = ExecutableFile.NEVER, type = ExecutableFile.BIN, onFailure = ExecutableFile.ASK;
            boolean keepFile;

            String val = executableNode.getAttribute("stage", "never");
            if ("postinstall".equalsIgnoreCase(val))
            {
                executionStage = ExecutableFile.POSTINSTALL;
            }
            else if ("uninstall".equalsIgnoreCase(val))
            {
                executionStage = ExecutableFile.UNINSTALL;
            }

            // type of this executable
            val = executableNode.getAttribute("type", "bin");
            String mainClass = executableNode.getAttribute("class"); // executable class
            if ("jar".equalsIgnoreCase(val))
            {
                type = ExecutableFile.JAR;
                if (mainClass == null || mainClass.isEmpty())
                {
                    throw new CompilerException("Attribute 'class' mandatory and must not be empty for type 'jar'");
                }
            }
            else
            {
                if (mainClass != null)
                {
                    throw new CompilerException("Attribute 'class' allowed for type 'jar' only");
                }
            }

            // what to do if execution fails
            val = executableNode.getAttribute("failure", "ask");
            if ("abort".equalsIgnoreCase(val))
            {
                onFailure = ExecutableFile.ABORT;
            }
            else if ("warn".equalsIgnoreCase(val))
            {
                onFailure = ExecutableFile.WARN;
            }
            else if ("ignore".equalsIgnoreCase(val))
            {
                onFailure = ExecutableFile.IGNORE;
            }

            // whether to keep the executable after executing it
            val = executableNode.getAttribute("keep");
            keepFile = Boolean.parseBoolean(val);

            // get arguments for this executable
            IXMLElement args = executableNode.getFirstChildNamed("args");
            List<String> argsList = new ArrayList<String>();
            if (null != args)
            {
                for (IXMLElement ixmlElement : args.getChildrenNamed("arg"))
                {
                    argsList.add(xmlCompilerHelper.requireAttribute(ixmlElement, "value"));
                }
            }

            if (target != null)
            {
                addNewExecutableFile(pack, target, conditionId, osList, executionStage, type, mainClass,
                        onFailure, keepFile, argsList);
                logMarkFileExecutable(target);
            }
            for (IXMLElement fileSetElement : executableNode.getChildrenNamed("fileset"))
            {
                String targetdir = fileSetElement.getAttribute("targetdir", "${INSTALL_PATH}");
                Set<String> includedFiles = getFilesetIncludedFiles(pack, fileSetElement, targetdir);
                for (String filePath : includedFiles)
                {
                    addNewExecutableFile(pack, filePath, conditionId, osList, executionStage, type, mainClass,
                            onFailure, keepFile, argsList);
                    logMarkFileExecutable(filePath);
                }
            }
        }
    }

    private void addNewExecutableFile(PackInfo pack, String target, String condition, List<OsModel> osList,
            int executionStage, int type, String mainClass, int onFailure, boolean keepFile, List<String> argsList
            ) throws CompilerException
    {
        ExecutableFile executable = new ExecutableFile();
        executable.path = target;
        executable.setCondition(condition);
        executable.osList = osList;
        executable.executionStage = executionStage;
        executable.type = type;
        executable.mainClass = mainClass;
        executable.onFailure = onFailure;
        executable.keepFile = keepFile;
        for (String arg : argsList)
        {
            executable.argList.add(arg);
        }

        pack.addExecutable(executable);
    }

    private void processParsableChildren(PackInfo pack, List<IXMLElement> parsableChildren) throws CompilerException
    {
        for (IXMLElement parsableNode : parsableChildren)
        {
            String target = parsableNode.getAttribute("targetfile");
            SubstitutionType type = SubstitutionType.lookup(parsableNode.getAttribute("type", "plain"));
            String encoding = parsableNode.getAttribute("encoding", null);
            List<OsModel> osList = OsConstraintHelper.getOsList(parsableNode); // TODO: unverified
            String conditionId = parseConditionAttribute(parsableNode);
            if (target != null)
            {
                ParsableFile parsable = new ParsableFile(target, type, encoding, osList);
                if (conditionId != null)
                {
                    parsable.setCondition(conditionId);
                }
                pack.addParsable(parsable);
                logMarkFileParsable(target);
            }
            for (IXMLElement fileSetElement : parsableNode.getChildrenNamed("fileset"))
            {
                String targetdir = fileSetElement.getAttribute("targetdir", "${INSTALL_PATH}");
                Set<String> includedFiles = getFilesetIncludedFiles(pack, fileSetElement, targetdir);
                for (String filePath : includedFiles)
                {
                    ParsableFile parsable = new ParsableFile(filePath, type, encoding, osList);
                    if (conditionId != null)
                    {
                        parsable.setCondition(conditionId);
                    }
                    pack.addParsable(parsable);
                    logMarkFileParsable(filePath);
                }
            }
        }
    }

    private Set<String> getFilesetIncludedFiles(PackInfo info, IXMLElement fileSetElement, String targetDir)
    throws CompilerException
    {
        boolean casesensitive = xmlCompilerHelper.validateYesNoAttribute(fileSetElement, "casesensitive", YES);

        // get includes and excludes
        List<IXMLElement> xcludesList;
        String[] includes = null;
        xcludesList = fileSetElement.getChildrenNamed("include");
        if (!xcludesList.isEmpty())
        {
            includes = new String[xcludesList.size()];
            for (int j = 0; j < xcludesList.size(); j++)
            {
                IXMLElement xclude = xcludesList.get(j);
                includes[j] = xmlCompilerHelper.requireAttribute(xclude, "name");
            }
        }
        String[] excludes = null;
        xcludesList = fileSetElement.getChildrenNamed("exclude");
        if (!xcludesList.isEmpty())
        {
            excludes = new String[xcludesList.size()];
            for (int j = 0; j < xcludesList.size(); j++)
            {
                IXMLElement xclude = xcludesList.get(j);
                excludes[j] = xmlCompilerHelper.requireAttribute(xclude, "name");
            }
        }

        // parse additional fileset attributes "includes" and "excludes"
        String[] toDo = new String[]{"includes", "excludes"};
        // use the existing containers filled from include and exclude
        // and add the includes and excludes to it
        String[][] containers = new String[][]{includes, excludes};
        for (int j = 0; j < toDo.length; ++j)
        {
            String inex = fileSetElement.getAttribute(toDo[j]);
            if (inex != null && inex.length() > 0)
            { // This is the same "splitting" as ant PatternSet do ...
                StringTokenizer tokenizer = new StringTokenizer(inex, ", ", false);
                int newSize = tokenizer.countTokens();
                String[] nCont = null;
                if (containers[j] != null && containers[j].length > 0)
                {   // old container exist; create a new which can hold
                    // all values and copy the old stuff to the front
                    newSize += containers[j].length;
                    nCont = new String[newSize];
                    System.arraycopy(containers[j], 0, nCont, 0, containers[j].length);
                }
                if (nCont == null) // No container for old values created, create a new one.
                {
                    nCont = new String[newSize];
                }
                for (int k = 0; k < newSize; ++k)
                // Fill the new one or expand the existent container
                {
                    nCont[k] = tokenizer.nextToken();
                }
                containers[j] = nCont;
            }
        }
        includes = containers[0]; // push the new includes to the
        // local var
        excludes = containers[1]; // push the new excludes to the
        // local var


        HashSet<String> matches = new HashSet<String>();
        AntPathMatcher matcher = new AntPathMatcher();

        if (includes == null || includes.length == 0)
        {
            throw new CompilerException("At least one included file required in a fileset");
        }

        logger.fine("Fileset (targetDir=\""+targetDir+"\"");
        for (String include : includes)
        {
            logger.fine("Processing include: \"" + include+"\"");
            for (PackFile s:info.getPackFiles()) {
                String targetPath = s.getTargetPath();
                if (matcher.match(targetDir + "/" + include, targetPath, casesensitive))
                {
                    matches.add(targetPath);
                }
            }
        }

        if (excludes != null)
        {
            for (String exclude : excludes)
            {
                for (PackFile s : info.getPackFiles())
                {
                    String targetPath = s.getTargetPath();
                    if (matcher.match(exclude, targetPath, casesensitive))
                    {
                        matches.remove(targetPath);
                    }
                }
            }
        }

        return matches;
    }

    private IXMLElement readRefPackData(File baseDir, String refFileName, boolean isselfcontained)
            throws CompilerException
    {
        File refXMLFile = new File(refFileName);
        if (!refXMLFile.isAbsolute())
        {
            refXMLFile = new File(baseDir, refFileName);
        }
        if (!refXMLFile.canRead())
        {
            throw new CompilerException("Invalid file: " + refXMLFile);
        }

        InputStream specin;

        if (isselfcontained)
        {
            if (!refXMLFile.getAbsolutePath().endsWith(".zip"))
            {
                throw new CompilerException(
                        "Invalid file: " + refXMLFile
                                + ". Selfcontained files can only be of type zip.");
            }
            ZipFile zip;
            try
            {
                zip = new ZipFile(refXMLFile, ZipFile.OPEN_READ);
                ZipEntry specentry = zip.getEntry("META-INF/izpack.xml");
                specin = zip.getInputStream(specentry);
            }
            catch (IOException e)
            {
                throw new CompilerException("Error reading META-INF/izpack.xml in " + refXMLFile);
            }
        }
        else
        {
            try
            {
                specin = new FileInputStream(refXMLFile.getAbsolutePath());
            }
            catch (FileNotFoundException e)
            {
                throw new CompilerException(
                        "FileNotFoundException exception while reading refXMLFile");
            }
        }

        IXMLElement refXMLData = new InstallationXmlParser().parse(specin, refXMLFile.getAbsolutePath());

        // Now checked the loaded XML file for basic syntax
        if (! (  "izpack:installation".equalsIgnoreCase(refXMLData.getName())  // normally with the namespace prefix
            || "installation".equalsIgnoreCase(refXMLData.getName())))       // optional without
        {
            assertionHelper.parseError(refXMLData, "this is not an IzPack XML installation file");
        }
        if (!CompilerData.VERSION.equalsIgnoreCase(xmlCompilerHelper.requireAttribute(refXMLData, "version")))
        {
            assertionHelper.parseError(refXMLData, "the file version is different from the compiler version");
        }

        // Read the properties and perform replacement on the rest of the tree
        substituteProperties(refXMLData);

        // call addResources to add the referenced XML resources to this installation
        addResources(refXMLData, baseDir);

        try
        {
            specin.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return refXMLData;
    }

    /**
     * Add files in an archive to a pack
     *
     * @param archive     the archive file to unpack
     * @param targetDir   the target directory where the content of the archive will be installed
     * @param osList      The target OS constraints.
     * @param override    Overriding behaviour.
     * @param pack        Pack to be packed into
     * @param additionals Map which contains additional data
     * @param condition   condition that must evaluate {@code} true for the file to be installed. May be {@code null}
     */
    private void addArchiveContent(IXMLElement fileNode, File baseDir, File archive, String targetDir,
                                   List<OsModel> osList, OverrideType override, String overrideRenameTo,
                                   Blockable blockable, PackInfo pack, Map<String, ?> additionals,
                                   String condition) throws Exception
    {
        String archiveName = archive.getName();

        InputStream originalInputStream = IOUtils.buffer(FileUtils.openInputStream(archive));

        InputStream uncompressedInputStream;
        try
        {
            uncompressedInputStream = IOUtils.buffer(new CompressorStreamFactory().createCompressorInputStream(originalInputStream));
            // file is compressed, may be a compressed archive
        }
        catch (CompressorException e)
        {
            // file is not a single compressed file, may be an uncompressed archive
            uncompressedInputStream = originalInputStream;
        }

        List<IXMLElement> filesetNodes = fileNode.getChildrenNamed("archivefileset");
        final boolean hasNoFileSet = (filesetNodes == null || filesetNodes.isEmpty());

        ArchiveInputStream archiveInputStream = null;
        File baseTempDir = null;
        try
        {
            archiveInputStream = new ArchiveStreamFactory().createArchiveInputStream(archive, uncompressedInputStream);

            // file is an archive (incl. ZIP archive) - unpack recursively
            baseTempDir = com.izforge.izpack.util.file.FileUtils.createTempDirectory("izpack", TEMP_DIR);

            while (true)
            {
                ArchiveEntry entry = archiveInputStream.getNextEntry();
                if (entry == null)
                {
                    break;
                }
                String entryName = entry.getName();
                if (entry.isDirectory())
                {
                    String dName = FilenameUtils.normalizeNoEndSeparator(entryName);
                    File tempDir = new File(baseTempDir, dName);
                    FileUtils.forceMkdir(tempDir);
                    updateLastModifiedDate(tempDir, entry);
                    if (hasNoFileSet)
                    {
                        String target = targetDir + "/" + dName;
                        logAddingFile(dName + " (" + archiveName + ")", target);
                        pack.addFile(baseTempDir, tempDir, target, osList, override, overrideRenameTo, blockable, additionals, condition);
                    }
                }
                else
                {
                    FileOutputStream tempFileStream = null;
                    try
                    {
                        File tempFile = new File(baseTempDir, entryName);
                        tempFileStream = FileUtils.openOutputStream(tempFile);
                        IOUtils.copy(archiveInputStream, tempFileStream);
                        tempFileStream.close();
                        updateLastModifiedDate(tempFile, entry);
                        if (hasNoFileSet)
                        {
                            String target = targetDir + "/" + entryName;
                            logAddingFile(entryName + " (" + archiveName + ")", target);
                            pack.addFile(baseTempDir, tempFile, target, osList, override, overrideRenameTo, blockable, additionals, condition);
                        }
                    }
                    finally
                    {
                        IOUtils.closeQuietly(tempFileStream);
                    }
                }
            }

            if (!hasNoFileSet)
            {
                for (IXMLElement fileSetNode : filesetNodes)
                {
                    processFileSetChildren(readArchiveFileSet(fileSetNode, baseTempDir, targetDir), baseTempDir, osList, pack);
                }
            }
        }
        catch (ArchiveException e)
        {
            if (baseTempDir != null)
            {
                FileUtils.deleteDirectory(baseTempDir);
            }

            if (uncompressedInputStream == originalInputStream)
            {
                throw new Exception("No compression or archiving format detected for file " + archive + " marked to be unpacked");
            }

            if (!hasNoFileSet)
            {
                throw new Exception("Nested archive filesets not applicable because " + archive + " is not an archive file");
            }

            // uncompressed file is not an archive
            File temp = File.createTempFile("izpack", null, TEMP_DIR);
            FileUtils.forceDeleteOnExit(temp);
            FileUtils.copyInputStreamToFile(uncompressedInputStream, temp);

            String uncompressedArchiveName = FilenameUtils.getBaseName(archiveName);
            String target = targetDir + "/" + uncompressedArchiveName;
            logAddingFile(uncompressedArchiveName + " (" + archiveName + ")", target);
            pack.addFile(baseDir, temp, target, osList, override, overrideRenameTo, blockable, additionals, condition);
        }
        finally
        {
            IOUtils.closeQuietly(archiveInputStream);
            IOUtils.closeQuietly(uncompressedInputStream);
            IOUtils.closeQuietly(originalInputStream);
            if (baseTempDir != null)
            {
                FileUtils.forceDeleteOnExit(baseTempDir);
            }
        }
    }

    private void updateLastModifiedDate(File target, ArchiveEntry entry)
    {
        target.setLastModified(entry.getLastModifiedDate().getTime());
    }

    /**
     * Parse panels and their parameters, locate the panels resources and add to the Packager.
     *
     * @param data The XML data.
     * @throws CompilerException Description of the Exception
     */
    private void addPanels(IXMLElement data)
    {
        notifyCompilerListener("addPanels", CompilerListener.BEGIN, data);
        IXMLElement root = xmlCompilerHelper.requireChildNamed(data, "panels");

        // at least one panel is required
        List<IXMLElement> panels = root.getChildrenNamed("panel");
        if (panels.isEmpty())
        {
            assertionHelper.parseError(root, "<panels> requires a <panel>");
        }

        // We process each panel markup
        // We need a panel counter to build unique panel dependent resource names
        int panelCounter = 0;
        for (IXMLElement panelElement : panels)
        {
            panelCounter++;

            // create the serialized Panel data
            Panel panel = new Panel();
            panel.setOsConstraints(OsConstraintHelper.getOsList(panelElement));
            String className = xmlCompilerHelper.requireAttribute(panelElement, "classname");

            // add an id
            String idAttr = panelElement.getAttribute("id");
            String panelId = idAttr;
            if (idAttr == null)
            {
                panelId = className + "_" + (panelCounter - 1);
            }
            panel.setPanelId(panelId);
            String conditionId = parseConditionAttribute(panelElement);
            if (conditionId != null)
            {
                panel.setCondition(conditionId);
            }

            String allowCloseStr = panelElement.getAttribute("allowClose");
            if (allowCloseStr != null)
            {
              boolean allowClose = Boolean.parseBoolean(allowCloseStr);
              if (allowClose)
                panel.setConfirmQuitType(Panel.ConfirmQuitType.SILENT);
              else
                panel.setConfirmQuitType(Panel.ConfirmQuitType.CONFIRM);
              // Make all previous panels CONFIRM if they're currently DYNAMIC.
              // This simplifies usage while maintaining backward compatibility
              // (user only has to specify allowClose="true" on last panel for
              //  probably the most common desired behavior).
              // Note: the new panel is not in the list yet (so we don't have to
              //       manually exclude it)
              List<Panel> previousPanels = packager.getPanelList();
              for (Panel previousPanel: previousPanels)
                if (previousPanel.getConfirmQuitType() == Panel.ConfirmQuitType.DYNAMIC)
                  previousPanel.setConfirmQuitType(Panel.ConfirmQuitType.CONFIRM);
            }
            // note - all jars must be added to the classpath prior to invoking this
            Class<IzPanel> type = classLoader.loadClass(className, IzPanel.class);
            if (type.equals(UserInputPanel.class))
            {
                if (userInputPanelIds == null || !userInputPanelIds.contains(panelId))
                {
                    assertionHelper.parseError(panelElement, "Referred user input panel '" + panelId
                            + "' has not been defined in resource "
                            + UserInputPanelSpec.SPEC_FILE_NAME);
                }
            }
            panel.setClassName(type.getName());
            panel.readParameters(panelElement);

            // adding validator
            List<IXMLElement> validatorElements = panelElement.getChildrenNamed(DataValidator.DATA_VALIDATOR_TAG);
            for (IXMLElement validatorElement : validatorElements)
            {
                String validator = validatorElement.getAttribute(DataValidator.DATA_VALIDATOR_CLASSNAME_ATTR);
                if (validator != null && !validator.isEmpty())
                {
                    String validatorCondition = validatorElement.getAttribute(DataValidator.DATA_VALIDATOR_CONDITION_ATTR);
                    Class<DataValidator> validatorType = classLoader.loadClass(validator, DataValidator.class);
                    DefaultConfigurationHandler configurable = null;
                    if (PanelValidator.class.isAssignableFrom(validatorType))
                    {
                        configurable = new DefaultConfigurationHandlerAdapter();
                        configurable.readParameters(validatorElement);
                        logger.finer("Validator " + validatorType.getName()
                                + " extends the " + PanelValidator.class.getSimpleName()
                                + "interface and adds "
                                + (configurable.getNames()!=null?configurable.getNames().size():"no")
                                + " parameters");
                    }
                    logger.fine("Adding validator '" + validator + "' to panel '" + panel.getPanelId() + "'");
                    panel.addValidator(validatorType.getName(), validatorCondition, configurable);
                }
            }

            // adding helps
            List<IXMLElement> helpSpecs = panelElement.getChildrenNamed(HELP_TAG);
            if (helpSpecs != null) // TODO : remove this condition, getChildrenNamed always return a list
            {
                List<Help> helps = new ArrayList<Help>();
                for (IXMLElement help : helpSpecs)
                {
                    String iso3 = help.getAttribute(ISO3_ATTRIBUTE);
                    String resourceId;
                    if (idAttr == null)
                    {
                        resourceId = className + "_" + panelCounter + "_help.html_" + iso3;
                    }
                    else
                    {
                        resourceId = idAttr + "_" + panelCounter + "_help.html_" + iso3;
                    }
                    helps.add(new Help(iso3, resourceId));
                    URL originalUrl = resourceFinder.findProjectResource(help.getAttribute(SRC_ATTRIBUTE),
                                                                         "Help", help);
                    packager.addResource(resourceId, originalUrl);
                }
                panel.setHelps(helps);
            }
            // add actions
            addPanelActions(panelElement, panel);

            // insert into the packager
            packager.addPanel(panel);
        }
        notifyCompilerListener("addPanels", CompilerListener.END, data);
    }

    /**
     * Parse and add logging configuration
     *
     * @param data the XML root tag of the installer descriptor
     * @throws CompilerException an error ocurred during compiling
     */
    private void addLogging(IXMLElement data) throws CompilerException
    {
        notifyCompilerListener("addLogging", CompilerListener.BEGIN, data);

        IXMLElement loggingElement = data.getFirstChildNamed("logging");
        if (loggingElement == null)
        {
            return;
        }

        // check if we can load the logging properties from an external file...
        List<IXMLElement> configFiles = loggingElement.getChildrenNamed("configuration-file");
        if (configFiles != null)
        {
            if (configFiles.size() > 1)
            {
                assertionHelper.parseError(loggingElement, "Logging configuration by external file may only be used once");
            }
            else if (configFiles.size() == 1)
            {
                IXMLElement configFileElement = configFiles.get(0);
                String fileName = xmlCompilerHelper.requireAttribute(configFileElement, "file");
                URL url = resourceFinder.findProjectResource(fileName, "Logging configuration from file", configFileElement);
                packager.addResource(ResourceManager.DEFAULT_INSTALL_LOGGING_CONFIGURATION_RES, url);
                return;
            }
        }

        // ... and if not, create the logging properties ourselves
        Properties logConfig = null;
        final String globalLevel = loggingElement.getAttribute("level", "INFO");
        if (globalLevel != null)
        {
            logConfig = new Properties();
            //logConfig.setProperty(".level", globalLevel);
            logConfig.setProperty(ConsoleHandler.class.getName() + ".level", globalLevel);
            if (Level.parse(globalLevel).intValue() > Level.INFO.intValue())
            {
                logConfig.setProperty("java.awt.level", globalLevel);
                logConfig.setProperty("javax.swing.level", globalLevel);
                logConfig.setProperty("sun.awt.level", globalLevel);
                logConfig.setProperty("sun.awt.X11.level", globalLevel);
            }
        }

        List<IXMLElement> logFiles = loggingElement.getChildrenNamed("log-file");
        if (logFiles != null)
        {
            if (configFiles != null && !configFiles.isEmpty() && !logFiles.isEmpty())
            {
                assertionHelper.parseError(loggingElement, "Logging configuration by external file and log file specification cannot be mixed");
            }
            for (IXMLElement configFileElement : logFiles)
            {
                final String cname = FileHandler.class.getName();
                if (logConfig == null)
                {
                    logConfig = new Properties();
                }
                logConfig.setProperty("handlers", cname);
                final String pattern = configFileElement.getAttribute("pattern");
                if (pattern != null)
                {
                    logConfig.setProperty(cname + ".pattern", pattern);
                }
                final String level = configFileElement.getAttribute("level", "INFO");
                if (level != null)
                {
                    logConfig.setProperty(cname + ".level", level);
                }
                final String filter = configFileElement.getAttribute("filter");
                if (filter != null)
                {
                    logConfig.setProperty(cname + ".filter", filter);
                }
                final String encoding = configFileElement.getAttribute("encoding");
                if (encoding != null)
                {
                    logConfig.setProperty(cname + ".encoding", encoding);
                }
                String limit = configFileElement.getAttribute("limit");
                if (limit != null)
                {
                    logConfig.setProperty(cname + ".limit", limit);
                }
                final String count = configFileElement.getAttribute("count");
                if (count != null)
                {
                    logConfig.setProperty(cname + ".count", count);
                }
                final String append = configFileElement.getAttribute("append");
                if (append != null)
                {
                    logConfig.setProperty(cname + ".append", append);
                }
                final String mkdirs = configFileElement.getAttribute("mkdirs");
                if (mkdirs != null)
                {
                    logConfig.setProperty(cname + ".mkdirs", Boolean.valueOf(mkdirs).toString());
                }
                logConfig.setProperty(cname + ".formatter", FileFormatter.class.getName());
            }
            if (logConfig != null)
            {
                FileOutputStream os = null;
                File temp;
                try
                {
                    temp = File.createTempFile("install_logging", ".properties", TEMP_DIR);
                    temp.deleteOnExit();
                    os = FileUtils.openOutputStream(temp);
                    logConfig.store(os, null);
                    packager.addResource(ResourceManager.DEFAULT_INSTALL_LOGGING_CONFIGURATION_RES, temp.toURI().toURL());
                }
                catch (IOException e)
                {
                    throw new CompilerException("Unable to handle temporary resource file: " + e.getMessage(), e);
                }
                finally
                {
                    IOUtils.closeQuietly(os);
                }
            }
        }

        notifyCompilerListener("addLogging", CompilerListener.END, data);
    }


    /**
     * Adds the resources.
     *
     * @param data The XML data.
     * @throws CompilerException Description of the Exception
     */
    private void addResources(IXMLElement data) throws CompilerException
    {
        addResources(data, new File(compilerData.getBasedir()));
    }

    /**
     * Adds the resources.
     *
     * @param data The XML data.
     * @param baseDir the base directory which resources should be relatively loaded from
     * @throws CompilerException Description of the Exception
     */
    private void addResources(IXMLElement data, File baseDir) throws CompilerException
    {
        notifyCompilerListener("addResources", CompilerListener.BEGIN, data);

        // A list of packsLang-files that were defined by the user in the resource-section The key of
        // this map is an packsLang-file identifier, e.g. <code>packsLang.xml_eng</code>, the values
        // are lists of {@link URL} pointing to the concrete packsLang-files.         *
        final Map<String, List<URL>> packsLangUrlMap = new HashMap<>();

        IXMLElement root = data.getFirstChildNamed("resources");
        if (root == null)
        {
            return;
        }

        // We process each res markup
        for (IXMLElement resNode : root.getChildrenNamed("res"))
        {
            String id = xmlCompilerHelper.requireAttribute(resNode, "id");
            String src = getSrcSubstitutedAttributeValue(resNode);
            // the parse attribute causes substitution to occur
            boolean substitute = xmlCompilerHelper.validateYesNoAttribute(resNode, "parse", NO);
            // the parsexml attribute causes the xml document to be parsed
            boolean parsexml = xmlCompilerHelper.validateYesNoAttribute(resNode, "parsexml", NO);

            Charset charset = getCharset(resNode);

            // basedir is not prepended if src is already an absolute path
            URL originalUrl = resourceFinder.findProjectResource(baseDir, src, "Resource", resNode);
            URL url = originalUrl;

            InputStream is = null;
            OutputStream os = null;
            try
            {
                if (parsexml || charset != null || (substitute && !packager.getVariables().isEmpty()))
                {
                    // make the substitutions into a temp file
                    File parsedFile = File.createTempFile("izpp", null, TEMP_DIR);
                    parsedFile.deleteOnExit();
                    os = FileUtils.openOutputStream(parsedFile);
                    // and specify the substituted file to be added to the
                    // packager
                    url = parsedFile.toURI().toURL();
                }

                if (charset != null)
                {
                    File recodedFile = File.createTempFile("izenc", null, TEMP_DIR);
                    recodedFile.deleteOnExit();

                    InputStreamReader reader = new InputStreamReader(originalUrl.openStream(), charset);
                    OutputStreamWriter writer = new OutputStreamWriter(
                            new FileOutputStream(recodedFile), StandardCharsets.UTF_8);

                    char[] buffer = new char[1024];
                    int read;
                    while ((read = reader.read(buffer)) != -1)
                    {
                        writer.write(buffer, 0, read);
                    }
                    reader.close();
                    writer.close();
                    if (parsexml)
                    {
                        originalUrl = recodedFile.toURI().toURL();
                    }
                    else
                    {
                        url = recodedFile.toURI().toURL();
                    }
                }

                if (parsexml)
                {
                    IXMLParser parser = new XMLParser(false);
                    // this constructor will open the specified url (this is
                    // why the InputStream is not handled in a similar manner
                    // to the OutputStream)

                    IXMLElement xml = parser.parse(originalUrl);
                    IXMLWriter writer = new XMLWriter();
                    if (substitute && !packager.getVariables().isEmpty())
                    {
                        // if we are also performing substitutions on the file
                        // then create an in-memory copy to pass to the
                        // substitutor
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        writer.setOutput(baos);
                        is = new ByteArrayInputStream(baos.toByteArray());
                    }
                    else
                    {
                        // otherwise write direct to the temp file
                        writer.setOutput(os);
                    }
                    writer.write(xml);
                }

                // substitute variable values in the resource if parsed
                if (substitute)
                {
                    if (packager.getVariables().isEmpty())
                    {
                        // reset url to original.
                        url = originalUrl;
                        assertionHelper.parseWarn(resNode, "No variables defined. " + url.getPath() + " not parsed.");
                    }
                    else
                    {
                        SubstitutionType type = SubstitutionType.lookup(resNode.getAttribute("type"));

                        // if the xml parser did not open the url
                        // ('parsexml' was not enabled)
                        if (null == is)
                        {
                            is = new BufferedInputStream(originalUrl.openStream());
                        }
                        // VariableSubstitutor vs = new
                        // VariableSubstitutorImpl(compiler.getVariables());
                        variableSubstitutor.substitute(is, os, type, "UTF-8");
                    }
                }

            }
            catch (Exception e)
            {
                assertionHelper.parseError(resNode, e.getMessage(), e);
            }
            finally
            {
                if (null != os)
                {
                    try
                    {
                        os.close();
                    }
                    catch (IOException e)
                    {
                        // ignore as there is nothing we can realistically do
                        // so lets at least try to close the input stream
                    }
                }
                if (null != is)
                {
                    try
                    {
                        is.close();
                    }
                    catch (IOException e)
                    {
                        // ignore as there is nothing we can realistically do
                    }
                }
            }

            IXMLElement userInputSpec = null, antActionSpec = null, configurationSpec = null;

            // Just validate to avoid XML parser errors during installation later
            if (id.startsWith(Resources.CUSTOM_TRANSLATIONS_RESOURCE_NAME)
                    || id.startsWith(Resources.USER_INPUT_TRANSLATIONS_RESOURCE_NAME)
                    || id.startsWith(Resources.PACK_TRANSLATIONS_RESOURCE_NAME))
            {
                new LangPackXmlParser().parse(url);
            }
            else if (id.endsWith(ShortcutConstants.SPEC_FILE_NAME))
            {
                new ShortcutSpecXmlParser().parse(url);
            }
            else if (id.equals(Resources.CUSTOM_ICONS_RESOURCE_NAME))
            {
                new IconsSpecXmlParser().parse(url);
            }
            else if (id.startsWith(UserInputPanelSpec.SPEC_FILE_NAME))
            {
                userInputSpec = new UserInputSpecXmlParser().parse(url);
            }
            else if (id.equals(AntActionInstallerListener.SPEC_FILE_NAME))
            {
                antActionSpec = new AntActionSpecXmlParser().parse(url);
            }
            else if (id.equals(ConfigurationInstallerListener.SPEC_FILE_NAME))
            {
                configurationSpec = new ConfigurationActionSpecXmlParser().parse(url);
            }
            else if (id.equals(RegistryInstallerListener.SPEC_FILE_NAME))
            {
                new RegistrySpecXmlParser().parse(url);
            }
            else if (id.equals(ProcessPanelWorker.SPEC_RESOURCE_NAME))
            {
                new ProcessingSpecXmlParser().parse(url);
            }

            // remembering references to all added packsLang.xml files
            if (id.startsWith(Resources.PACK_TRANSLATIONS_RESOURCE_NAME))
            {
                List<URL> packsLangURLs;
                if (packsLangUrlMap.containsKey(id))
                {
                    packsLangURLs = packsLangUrlMap.get(id);
                }
                else
                {
                    packsLangURLs = new ArrayList<URL>();
                    packsLangUrlMap.put(id, packsLangURLs);
                }
                packsLangURLs.add(url);
                // Do not add resource to packager here to prevent adding it multiple times later.
                // Languages are merged into one file per language and added by {@link addMergedTranslationResources()} later.
            }
            else
            {
                packager.addResource(id, url);

                if (id.startsWith(UserInputPanelSpec.SPEC_FILE_NAME))
                {
                    // Check user input panel definitions
                    if (userInputSpec == null)
                    {
                        // Parse only if not validating for avoiding parsing twice
                        userInputSpec = new XMLParser(false).parse(url);
                    }
                    for (IXMLElement userPanelDef : userInputSpec.getChildrenNamed(UserInputPanelSpec.PANEL))
                    {
                        String userPanelId = xmlCompilerHelper.requireAttribute(userPanelDef, "id");
                        if (userInputPanelIds == null)
                        {
                            userInputPanelIds = new HashSet<String>();
                        }
                        if (!userInputPanelIds.add(userPanelId))
                        {
                            assertionHelper.parseError(userInputSpec, "Resource " + UserInputPanelSpec.SPEC_FILE_NAME
                                    + ": Duplicate user input panel identifier '"
                                    + userPanelId + "'");
                        }
                        // Collect referenced conditions in UserInputPanelSpec for checking them later
                        for (IXMLElement fieldDef : userPanelDef.getChildrenNamed(UserInputPanelSpec.FIELD))
                        {
                            String fieldConditionId = fieldDef.getAttribute("conditionid");
                            if (fieldConditionId != null)
                            {
                                List<IXMLElement> elList = referencedConditionsUserInputSpec.get(fieldConditionId);
                                if (elList == null)
                                {
                                    elList = new ArrayList<IXMLElement>();
                                    referencedConditionsUserInputSpec.put(fieldConditionId, elList);
                                }
                                elList.add(fieldDef);
                            }
                            for (IXMLElement fieldSpecDef : fieldDef.getChildrenNamed(FieldReader.SPEC))
                            {
                                for (IXMLElement choiceDef : fieldSpecDef.getChildrenNamed(SimpleChoiceReader.CHOICE))
                                {
                                    String choiceConditionId = choiceDef.getAttribute("conditionid");
                                    if (choiceConditionId != null)
                                    {
                                        List<IXMLElement> elList = referencedConditionsUserInputSpec.get(choiceConditionId);
                                        if (elList == null)
                                        {
                                            elList = new ArrayList<IXMLElement>();
                                            referencedConditionsUserInputSpec.put(choiceConditionId, elList);
                                        }
                                        elList.add(choiceDef);
                                    }
                                }
                                // Check whether button field run class can be loaded
                                for (IXMLElement runDef : fieldSpecDef.getChildrenNamed(ButtonFieldReader.RUN_ELEMENT))
                                {
                                    String actionClassName = runDef.getAttribute(ButtonFieldReader.RUN_ELEMENT_CLASS_ATTR);
                                    if (actionClassName != null)
                                    {
                                        try
                                        {
                                            Class.forName(actionClassName);
                                        }
                                        catch (ClassNotFoundException e)
                                        {
                                            assertionHelper.parseError(userInputSpec, "Resource " + UserInputPanelSpec.SPEC_FILE_NAME
                                                    + ": Button action class '" + actionClassName + "' cannot be loaded");
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (id.equals(AntActionInstallerListener.SPEC_FILE_NAME))
                {
                    if (antActionSpec == null)
                    {
                        // Parse only if not validating for avoiding parsing twice
                        antActionSpec = new XMLParser(false).parse(url);
                    }
                    for (IXMLElement packDef : antActionSpec.getChildrenNamed(SpecHelper.PACK_KEY))
                    {
                        String packName = xmlCompilerHelper.requireAttribute(packDef, SpecHelper.PACK_NAME);
                        // Collect referenced packs in AntActionSpec for checking them later
                        if (referencedPacksAntActionSpec.put(packName, packDef) != null)
                        {
                            assertionHelper.parseError(antActionSpec, "Resource " + AntActionInstallerListener.SPEC_FILE_NAME
                                    + ": Duplicate pack identifier '"
                                    + packName + "'");
                        }
                        for (IXMLElement antCallSpecDef : packDef.getChildrenNamed(AntAction.ANTCALL))
                        {
                            String antCallConditionId = antCallSpecDef.getAttribute(AntAction.CONDITIONID_ATTR);
                            if (antCallConditionId != null)
                            {
                                List<IXMLElement> elList = referencedConditionsAntActionSpec.get(antCallConditionId);
                                if (elList == null)
                                {
                                    elList = new ArrayList<IXMLElement>();
                                    referencedConditionsAntActionSpec.put(antCallConditionId, elList);
                                }
                                elList.add(antCallSpecDef);
                            }
                        }
                    }
                } else if (id.equals(ConfigurationInstallerListener.SPEC_FILE_NAME))
                {
                    if (configurationSpec == null)
                    {
                        // Parse only if not validating for avoiding parsing twice
                        configurationSpec = new XMLParser(false).parse(url);
                    }
                    for (IXMLElement packDef : configurationSpec.getChildrenNamed(SpecHelper.PACK_KEY))
                    {
                        String packName = xmlCompilerHelper.requireAttribute(packDef, SpecHelper.PACK_NAME);
                        // Collect referenced packs in ConfigurationActionSpec for checking them later
                        if (referencedPacksConfigurationActionSpec.put(packName, packDef) != null)
                        {
                            assertionHelper.parseError(configurationSpec, "Resource " + ConfigurationInstallerListener.SPEC_FILE_NAME
                                    + ": Duplicate pack identifier '"
                                    + packName + "'");
                        }
                        for (IXMLElement configurationactionDef : packDef.getChildrenNamed(ConfigurationInstallerListener.CONFIGURATIONACTION_ATTR))
                        {
                            for (IXMLElement configurableDef : configurationactionDef.getChildrenNamed(ConfigurationInstallerListener.CONFIGURABLE_ATTR))
                            {
                                String configurationConditionId = configurableDef.getAttribute(ConfigurationInstallerListener.CONDITION_ATTR);
                                if (configurationConditionId != null)
                                {
                                    List<IXMLElement> elList = referencedConditionsConfigurationActionSpec.get(configurationConditionId);
                                    if (elList == null)
                                    {
                                        elList = new ArrayList<IXMLElement>();
                                        referencedConditionsConfigurationActionSpec.put(configurationConditionId, elList);
                                    }
                                    elList.add(configurableDef);
                                }
                            }
                            for (IXMLElement configurablesetDef : configurationactionDef.getChildrenNamed(ConfigurationInstallerListener.CONFIGURABLESET_ATTR))
                            {
                                String configurationConditionId = configurablesetDef.getAttribute(ConfigurationInstallerListener.CONDITION_ATTR);
                                if (configurationConditionId != null)
                                {
                                    List<IXMLElement> elList = referencedConditionsConfigurationActionSpec.get(configurationConditionId);
                                    if (elList == null)
                                    {
                                        elList = new ArrayList<IXMLElement>();
                                        referencedConditionsConfigurationActionSpec.put(configurationConditionId, elList);
                                    }
                                    elList.add(configurablesetDef);
                                }
                            }
                        }
                    }
                }
            }
        }
        addMergedTranslationResources(packsLangUrlMap);
        notifyCompilerListener("addResources", CompilerListener.END, data);
    }

    private static Charset getCharset(IXMLElement resNode) {
        final String encoding = resNode.getAttribute("encoding");
        if (encoding != null && !encoding.isEmpty() )
        {
            return Charset.forName(encoding);
        }
        return null;
    }

    /**
     * Adds the ISO3 codes of the langpacks and associated resources.
     *
     * @param data The XML data.
     * @throws CompilerException Description of the Exception
     */
    private void addLangpacks(IXMLElement data) throws CompilerException
    {
        notifyCompilerListener("addLangpacks", CompilerListener.BEGIN, data);
        IXMLElement root = xmlCompilerHelper.requireChildNamed(data, "locale");

        // at least one langpack is required
        List<IXMLElement> locals = root.getChildrenNamed("langpack");
        if (locals.isEmpty())
        {
            assertionHelper.parseError(root, "<locale> requires a <langpack>");
        }

        // We process each langpack markup
        for (IXMLElement localNode : locals)
        {
            String iso3 = xmlCompilerHelper.requireAttribute(localNode, "iso3");
            String path;

            path = "com/izforge/izpack/bin/langpacks/installer/" + iso3 + ".xml";
            URL iso3xmlURL = resourceFinder.findIzPackResource(path, "ISO3 file", localNode);

            path = "com/izforge/izpack/bin/langpacks/flags/" + iso3 + ".gif";
            URL iso3FlagURL = resourceFinder.findIzPackResource(path, "ISO3 flag image", localNode);

            packager.addLangPack(iso3, iso3xmlURL, iso3FlagURL);
        }
        notifyCompilerListener("addLangpacks", CompilerListener.END, data);
    }
    
    /**
     * Builds the Info class from the XML tree (part that sets just strings).
     *
     * @param data The XML data. return The Info.
     * @throws CompilerException an error occured during compiling
     */
    private void addInfoStrings(IXMLElement data)
    {
        notifyCompilerListener("addInfoStrings", CompilerListener.BEGIN, data);
        // Initialisation
        IXMLElement root = xmlCompilerHelper.requireChildNamed(data, "info");
        
        Info info = compilerData.getExternalInfo();
        info.setAppName(xmlCompilerHelper.requireContent(xmlCompilerHelper.requireChildNamed(root, "appname")));
        info.setAppVersion(xmlCompilerHelper.requireContent(xmlCompilerHelper.requireChildNamed(root, "appversion")));
        // We get the installation subpath
        IXMLElement subpath = root.getFirstChildNamed("appsubpath");
        if (subpath != null)
        {
            info.setInstallationSubPath(xmlCompilerHelper.requireContent(subpath));
        }

        // validate and insert app URL
        final IXMLElement URLElem = root.getFirstChildNamed("url");
        if (URLElem != null)
        {
            URL appURL = xmlCompilerHelper.requireURLContent(URLElem);
            info.setAppURL(appURL.toString());
        }
        
        // We get the authors list
        IXMLElement authors = root.getFirstChildNamed("authors");
        if (authors != null)
        {
            for (IXMLElement authorNode : authors.getChildrenNamed("author"))
            {
                String name = xmlCompilerHelper.requireAttribute(authorNode, "name");
                String email = xmlCompilerHelper.requireAttribute(authorNode, "email");
                info.addAuthor(new Info.Author(name, email));
            }
        }

        // We get the java version required
        IXMLElement javaVersion = root.getFirstChildNamed("javaversion");
        if (javaVersion != null)
        {
            info.setJavaVersion(xmlCompilerHelper.requireContent(javaVersion));
            if (xmlCompilerHelper.validateYesNoAttribute(javaVersion, "strict", YES))
            {
                info.setJavaVersionStrict(true);
            }
        }

        // Is a JDK required?
        IXMLElement jdkRequired = root.getFirstChildNamed("requiresjdk");
        if (jdkRequired != null)
        {
            info.setJdkRequired("yes".equals(jdkRequired.getContent()));
        }

        // Does the installer expire?
        IXMLElement expiresDate = root.getFirstChildNamed("expiresdate");
        if (expiresDate != null)
        {
            try
            {
                info.setExpiresDate(expiresDate.getContent());
            }
            catch (ParseException e)
            {
                throw new CompilerException(
                        "expiresdate must be in format '" + EXPIRE_DATE_FORMAT + "'",
                        e);
            }
        }

        // validate and insert (and require if -web kind) web dir
        IXMLElement webDirURL = root.getFirstChildNamed("webdir");
        if (webDirURL != null)
        {
            info.setWebDirURL(xmlCompilerHelper.requireURLContent(webDirURL).toString());
        }
        String kind = compilerData.getKind();
        if (kind != null)
        {
            if (kind.equalsIgnoreCase(CompilerData.WEB) && webDirURL == null)
            {
                assertionHelper.parseError(root, "<webdir> required when \"WEB\" installer requested");
            }
            else if (kind.equalsIgnoreCase(CompilerData.STANDARD) && webDirURL != null)
            {
                // Need a Warning? parseWarn(webDirURL, "Not creating web
                // installer.");
                info.setWebDirURL(null);
            }
        }

        String compressionName = compilerData.getComprFormat();
        IXMLElement compressionElement = root.getFirstChildNamed("pack-compression-format");
        if (compressionElement != null)
        {
            compressionName = xmlCompilerHelper.requireContent(compressionElement);
        }
        if (compressionName != null)
        {
            PackCompression compression = PackCompression.byName(compressionName);
            if (compression == null)
            {
                throw new CompilerException("Unknown compression format: " + compressionName);
            }
            info.setCompressionFormat(compression);
            logger.info("Pack compression method: " + compression.toName());
        }

        // Add the path for the summary log file if specified
        IXMLElement slfPath = root.getFirstChildNamed("summarylogfilepath");
        if (slfPath != null)
        {
            info.setSummaryLogFilePath(xmlCompilerHelper.requireContent(slfPath));
        }

        IXMLElement writeInstallInfo = root.getFirstChildNamed("writeinstallationinformation");
        if (writeInstallInfo != null)
        {
            String writeInstallInfoString = xmlCompilerHelper.requireContent(writeInstallInfo);
            info.setWriteInstallationInformation(validateYesNo(writeInstallInfoString));
        }
        
        IXMLElement readInstallInfo = root.getFirstChildNamed("readinstallationinformation");
        if (readInstallInfo != null)
        {
            String readInstallInfoString = xmlCompilerHelper.requireContent(readInstallInfo);
            info.setReadInstallationInformation(validateYesNo(readInstallInfoString));
        }

        IXMLElement isSingleInstance = root.getFirstChildNamed("singleinstance");
        if (isSingleInstance != null)
        {
            String isSingleInstanceString = xmlCompilerHelper.requireContent(isSingleInstance);
            info.setSingleInstance(validateYesNo(isSingleInstanceString));
        }
        
         // Check if any temp directories have been specified
        List<IXMLElement> tempdirs = root.getChildrenNamed(TEMP_DIR_ELEMENT_NAME);
        if (null != tempdirs && tempdirs.size() > 0)
        {
            Set<String> tempDirAttributeNames = new HashSet<String>();
            for (IXMLElement tempdir : tempdirs)
            {
                final String prefix;
                if (tempdir.hasAttribute(TEMP_DIR_PREFIX_ATTRIBUTE))
                {
                    prefix = tempdir.getAttribute("prefix");
                }
                else
                {
                    prefix = DEFAULT_TEMP_DIR_PREFIX;
                }
                final String suffix;
                if (tempdir.hasAttribute(TEMP_DIR_SUFFIX_ATTRIBUTE))
                {
                    suffix = tempdir.getAttribute(TEMP_DIR_SUFFIX_ATTRIBUTE);
                }
                else
                {
                    suffix = DEFAULT_TEMP_DIR_SUFFIX;
                }
                final String variableName;
                if (tempdir.hasAttribute(TEMP_DIR_VARIABLE_NAME_ATTRIBUTE))
                {
                    variableName = tempdir.getAttribute(TEMP_DIR_VARIABLE_NAME_ATTRIBUTE);
                }
                else
                {
                    if (tempDirAttributeNames.contains(TEMP_DIR_DEFAULT_PROPERTY_NAME))
                    {
                        throw new CompilerException(
                                "Only one temporary directory may be specified without a " + TEMP_DIR_VARIABLE_NAME_ATTRIBUTE
                                        + " attribute. (Line: " + tempdir.getLineNr() + ").");
                    }
                    variableName = TEMP_DIR_DEFAULT_PROPERTY_NAME;
                }
                if (tempDirAttributeNames.contains(variableName))
                {
                    throw new CompilerException("Temporary directory variable names must be unique, the name "
                                                        + variableName + " is used more than once. (Line: " + tempdir.getLineNr() + ").");
                }
                tempDirAttributeNames.add(variableName);
                info.addTempDir(new TempDir(variableName, prefix, suffix));
            }
        }

        packager.setInfo(info);
        notifyCompilerListener("addInfoStrings", CompilerListener.END, data);
    }
    

    /**
     * Builds the Info class from the XML tree (part that sets conditions).
     *
     * @param data The XML data. return The Info.
     */
    private void addInfoConditional(IXMLElement data)
    {
        notifyCompilerListener("addInfoConditional", CompilerListener.BEGIN, data);
        // Initialisation
        IXMLElement root = xmlCompilerHelper.requireChildNamed(data, "info");

        Info info = compilerData.getExternalInfo();

        // Privileged execution
        IXMLElement privileged = root.getFirstChildNamed("run-privileged");
        info.setRequirePrivilegedExecution(privileged != null);
        if (privileged != null && privileged.hasAttribute("condition"))
        {
            info.setPrivilegedExecutionConditionID(parseConditionAttribute(privileged));
        }

        // Reboot if necessary
        IXMLElement reboot = root.getFirstChildNamed("rebootaction");
        if (reboot != null)
        {
            String content = reboot.getContent();
            if ("ignore".equalsIgnoreCase(content))
            {
                info.setRebootAction(Info.REBOOT_ACTION_IGNORE);
            }
            else if ("notice".equalsIgnoreCase(content))
            {
                info.setRebootAction(Info.REBOOT_ACTION_NOTICE);
            }
            else if ("ask".equalsIgnoreCase(content))
            {
                info.setRebootAction(Info.REBOOT_ACTION_ASK);
            }
            else if ("always".equalsIgnoreCase(content))
            {
                info.setRebootAction(Info.REBOOT_ACTION_ALWAYS);
            }
            else
            {
                throw new CompilerException("Invalid value ''" + content + "'' of element ''reboot''");
            }

            String conditionId = parseConditionAttribute(reboot);
            if (conditionId != null)
            {
                info.setRebootActionConditionID(conditionId);
            }
        }

        // Add the uninstaller as a resource if specified
        IXMLElement uninstallInfo = root.getFirstChildNamed("uninstaller");
        if (xmlCompilerHelper.validateYesNoAttribute(uninstallInfo, "write", YES))
        {
            logger.info("Adding uninstaller");

            // Write the manifest first so that it is not overwritten
            try {
                writeManifest();
            } catch (IOException e) {
                throw new CompilerException("Error while writing manifest", e);
            }
            //REFACTOR Change the way uninstaller is created
            mergeManager.addResourceToMerge("com/izforge/izpack/uninstaller/");
            mergeManager.addResourceToMerge("uninstaller-META-INF/");

            if (privileged != null)
            {
                // default behavior for uninstaller elevation: elevate if installer has to be elevated too
                info.setRequirePrivilegedExecutionUninstaller(
                        xmlCompilerHelper.validateYesNoAttribute(privileged,"uninstaller", YES));
            }

            if (uninstallInfo != null)
            {
                String uninstallerName = uninstallInfo.getAttribute("name");
                if (uninstallerName != null && uninstallerName.length() > ".jar".length())
                {
                    info.setUninstallerName(uninstallerName);
                }
                String uninstallerPath = uninstallInfo.getAttribute("path");
                if (uninstallerPath != null)
                {
                    info.setUninstallerPath(uninstallerPath);
                }

                boolean hideForceOption = Boolean.parseBoolean(uninstallInfo.getAttribute("hide-force-option"));
                info.setHideForceOption(hideForceOption);

                String conditionId = parseConditionAttribute(uninstallInfo);
                if (conditionId != null)
                {
                  // there's a condition for uninstaller
                  info.setUninstallerCondition(conditionId);
                }
            }
        }
        else
        {
            logger.info("Disable uninstaller");
            info.setUninstallerPath(null);
        }

        // look for an unpacker class
        String unpackerclass = propertyManager.getProperty("UNPACKER_CLASS");
        info.setUnpackerClassName(unpackerclass);

        packager.setInfo(info);
        notifyCompilerListener("addInfoConditional", CompilerListener.END, data);
    }

    /**
     * Write manifest in the installer jar for uninstaller.
     *
     * @throws IOException for any I/O error
     */
    private void writeManifest() throws IOException
    {
        final InputStream inputStream = CompilerConfig.class.getResourceAsStream("/uninstaller-META-INF/MANIFEST.MF");
        mergeManager.addResourceToMerge(compilerData.getTempManifestFileWithAdditionalEntries(inputStream), "uninstaller-META-INF/MANIFEST.MF");
    }

    /**
     * Variable declaration is a fragment of the xml file. For example:
     * <p/>
     * <pre>
     * &lt;p/&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     *        &lt;variables&gt;
     *          &lt;variable name=&quot;nom&quot; value=&quot;value&quot;/&gt;
     *          &lt;variable name=&quot;foo&quot; value=&quot;pippo&quot;/&gt;
     *        &lt;/variables&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     * </pre>
     * <p/>
     * variable declared in this can be referred to in parsable files.
     *
     * @param data The XML data.
     * @throws CompilerException Description of the Exception
     */
    protected void addVariables(IXMLElement data) throws CompilerException
    {
        notifyCompilerListener("addVariables", CompilerListener.BEGIN, data);
        // We get the varible list
        IXMLElement root = data.getFirstChildNamed("variables");
        if (root == null)
        {
            return;
        }

        Properties variables = packager.getVariables();

        for (IXMLElement variableNode : root.getChildrenNamed("variable"))
        {
            String name = xmlCompilerHelper.requireAttribute(variableNode, "name");
            String value = xmlCompilerHelper.requireAttribute(variableNode, "value");
            if (variables.contains(name))
            {
                assertionHelper.parseWarn(variableNode, "Variable '" + name + "' being overwritten");
            }
            variables.setProperty(name, value);

            // Preserve default values for dynamic variables if the same variable names are used to define
            // static values in <variables>. This way dynamic variables are prevented from being unset.
            DynamicVariable dynamicVariable = new DynamicVariableImpl(name, value);
            dynamicVariable.setCheckonce(true);
            addDynamicVariable(variableNode, 0, name, dynamicVariable);
        }
        notifyCompilerListener("addVariables", CompilerListener.END, data);
    }

    private int getConfigFileType(String varname, String type) throws CompilerException
    {
        int filetype = ConfigFileValue.CONFIGFILE_TYPE_OPTIONS;
        if (type != null)
        {
            if (type.equalsIgnoreCase("options"))
            {
                filetype = ConfigFileValue.CONFIGFILE_TYPE_OPTIONS;
            }
            else if (type.equalsIgnoreCase("xml"))
            {
                filetype = ConfigFileValue.CONFIGFILE_TYPE_XML;
            }
            else if (type.equalsIgnoreCase("ini"))
            {
                filetype = ConfigFileValue.CONFIGFILE_TYPE_INI;
            }
            else
            {
                assertionHelper.parseError(
                        "Error in definition of dynamic variable " + varname + ": Unknown entry type " + type);
            }
        }
        return filetype;
    }

    protected void addDynamicVariables(IXMLElement data) throws CompilerException
    {
        notifyCompilerListener("addDynamicVariables", CompilerListener.BEGIN, data);
        // We get the dynamic variable list
        IXMLElement root = data.getFirstChildNamed("dynamicvariables");
        if (root == null)
        {
            return;
        }

        for (IXMLElement var : root.getChildrenNamed("variable"))
        {
            String name = xmlCompilerHelper.requireAttribute(var, "name");

            DynamicVariable dynamicVariable = new DynamicVariableImpl();
            dynamicVariable.setName(name);

            // Check for plain value
            String value = var.getAttribute("value");
            if (value != null)
            {
                dynamicVariable.setValue(new PlainValue(value));
            }
            else
            {
                IXMLElement valueElement = var.getFirstChildNamed("value");
                if (valueElement != null)
                {
                    value = valueElement.getContent();
                    if (value == null)
                    {
                        assertionHelper.parseError("Empty value element for dynamic variable " + name);
                    }
                    dynamicVariable.setValue(new PlainValue(value));
                }
            }
            // Check for environment variable value
            value = var.getAttribute("environment");
            if (value != null)
            {
                if (dynamicVariable.getValue() == null)
                {
                    dynamicVariable.setValue(new EnvironmentValue(value));
                }
                else
                {
                    // unexpected combination of variable attributes
                    assertionHelper.parseError("Ambiguous environment value definition for dynamic variable " + name);
                }
            }
            // Check for registry value
            value = var.getAttribute("regkey");
            if (value != null)
            {
                String regvalue = var.getAttribute("regvalue");
                if (dynamicVariable.getValue() == null)
                {
                    dynamicVariable.setValue(new RegistryValue(value, regvalue));
                }
                else
                {
                    // unexpected combination of variable attributes
                    assertionHelper.parseError("Ambiguous registry value definition for dynamic variable " + name);
                }
            }
            // Check for value from plain config file
            value = var.getAttribute("file");
            if (value != null)
            {
                String stype = var.getAttribute("type");
                String filesection = var.getAttribute("section");
                String filekey = xmlCompilerHelper.requireAttribute(var, "key");
                String escapeVal = var.getAttribute("escape");
                boolean escape = true;
                if (escapeVal != null)
                {
                    escape = Boolean.parseBoolean(escapeVal);
                }
                if (dynamicVariable.getValue() == null)
                {
                    dynamicVariable.setValue(new PlainConfigFileValue(value, getConfigFileType(
                            name, stype), filesection, filekey, escape));
                }
                else
                {
                    // unexpected combination of variable attributes
                    assertionHelper.parseError("Ambiguous file value definition for dynamic variable " + name);
                }
            }
            // Check for value from config file entry in a zip file
            value = var.getAttribute("zipfile");
            if (value != null)
            {
                String entryname = xmlCompilerHelper.requireAttribute(var, "entry");
                String stype = var.getAttribute("type");
                String filesection = var.getAttribute("section");
                String filekey = xmlCompilerHelper.requireAttribute(var, "key");
                String escapeVal = var.getAttribute("escape");
                boolean escape = true;
                if (escapeVal != null)
                {
                    escape = Boolean.parseBoolean(escapeVal);
                }
                if (dynamicVariable.getValue() == null)
                {
                    dynamicVariable.setValue(new ZipEntryConfigFileValue(value, entryname,
                                                                         getConfigFileType(name, stype), filesection,
                                                                         filekey, escape));
                }
                else
                {
                    // unexpected combination of variable attributes
                    assertionHelper.parseError("Ambiguous file value definition for dynamic variable " + name);
                }
            }
            // Check for value from config file entry in a jar file
            value = var.getAttribute("jarfile");
            if (value != null)
            {
                String entryname = xmlCompilerHelper.requireAttribute(var, "entry");
                String stype = var.getAttribute("type");
                String filesection = var.getAttribute("section");
                String filekey = xmlCompilerHelper.requireAttribute(var, "key");
                String escapeVal = var.getAttribute("escape");
                boolean escape = true;
                if (escapeVal != null)
                {
                    escape = Boolean.parseBoolean(escapeVal);
                }
                if (dynamicVariable.getValue() == null)
                {
                    dynamicVariable.setValue(new JarEntryConfigValue(value, entryname,
                                                                     getConfigFileType(name, stype), filesection,
                                                                     filekey, escape));
                }
                else
                {
                    // unexpected combination of variable attributes
                    assertionHelper.parseError("Ambiguous file value definition for dynamic variable " + name);
                }
            }
            // Check for result of execution
            value = var.getAttribute("executable");
            if (value != null)
            {
                if (dynamicVariable.getValue() == null)
                {
                    String dir = var.getAttribute(DIR_ATTRIBUTE);
                    String exectype = var.getAttribute("type");
                    String boolval = var.getAttribute("stderr");
                    boolean stderr = true;
                    if (boolval != null)
                    {
                        stderr = Boolean.parseBoolean(boolval);
                    }

                    if (value.length() <= 0)
                    {
                        assertionHelper.parseError("No command given in definition of dynamic variable " + name);
                    }
                    Vector<String> cmd = new Vector<String>();
                    cmd.add(value);
                    List<IXMLElement> args = var.getChildrenNamed("arg");
                    if (args != null)
                    {
                        for (IXMLElement arg : args)
                        {
                            String content = arg.getContent();
                            if (content != null)
                            {
                                cmd.add(content);
                            }
                        }
                    }
                    String[] cmdarr = new String[cmd.size()];
                    if (exectype == null || exectype.equalsIgnoreCase("process"))
                    {
                        dynamicVariable.setValue(new ExecValue(cmd.toArray(cmdarr), dir, false, stderr));
                    }
                    else if (exectype.equalsIgnoreCase("shell"))
                    {
                        dynamicVariable.setValue(new ExecValue(cmd.toArray(cmdarr), dir, true, stderr));
                    }
                    else
                    {
                        assertionHelper.parseError(
                                "Bad execution type " + exectype + " given for dynamic variable " + name);
                    }
                }
                else
                {
                    // unexpected combination of variable attributes
                    assertionHelper.parseError(
                            "Ambiguous execution output value definition for dynamic variable " + name);
                }
            }

            if (dynamicVariable.getValue() == null)
            {
                assertionHelper.parseError("No value specified at all for dynamic variable " + name);
            }

            // Check whether dynamic variable has to be evaluated only once during installation
            value = var.getAttribute("checkonce");
            if (value != null)
            {
                dynamicVariable.setCheckonce(Boolean.valueOf(value));
            }

            // Check whether dynamic variable should be automatically unset if its condition is not met
            value = var.getAttribute("unset");
            if (value != null)
            {
                dynamicVariable.setAutoUnset(Boolean.valueOf(value));
            }

            // Check whether evaluation failures of the dynamic variable should be ignored
            value = var.getAttribute("ignorefailure");
            if (value != null)
            {
                dynamicVariable.setIgnoreFailure(Boolean.valueOf(value));
            }

            // Nested value filters
            IXMLElement filters = var.getFirstChildNamed("filters");
            if (filters != null)
            {
                List<IXMLElement> filterList = filters.getChildren();
                for (IXMLElement filterElement : filterList)
                {
                    String filterName = filterElement.getName();
                    if (filterName.equals("regex"))
                    {
                        String expression = filterElement.getAttribute("regexp");
                        String selectexpr = filterElement.getAttribute("select");
                        String replaceexpr = filterElement.getAttribute("replace");
                        String defaultvalue = filterElement.getAttribute("defaultvalue");
                        String scasesensitive = filterElement.getAttribute("casesensitive");
                        String sglobal = filterElement.getAttribute("global");
                        dynamicVariable.addFilter(
                                new RegularExpressionFilter(
                                        expression, selectexpr,
                                        replaceexpr, defaultvalue,
                                        Boolean.valueOf(scasesensitive != null ? scasesensitive : "true"),
                                        Boolean.valueOf(sglobal != null ? sglobal : "false")));
                    }
                    else if (filterName.equals("location"))
                    {
                        String basedir = filterElement.getAttribute("basedir");
                        dynamicVariable.addFilter(new LocationFilter(basedir));
                    }
                    else if (filterName.equals("case"))
                    {
                        String style = filterElement.getAttribute("style");
                        dynamicVariable.addFilter(new CaseStyleFilter(style));
                    }
                    else
                    {
                        assertionHelper.parseError(String.format("Unknown filter '%s'", filterName));
                    }
                }
            }
            try
            {
                dynamicVariable.validate();
            }
            catch (Exception e)
            {
                assertionHelper.parseError(
                        "Error in definition of dynamic variable " + name + ": " + e.getMessage());
            }

            String conditionId = parseConditionAttribute(var);
            if (conditionId != null)
            {
                dynamicVariable.setConditionid(conditionId);
            }

            addDynamicVariable(var, name, dynamicVariable);
        }
        notifyCompilerListener("addDynamicVariables", CompilerListener.END, data);
    }

    private void addDynamicVariable(IXMLElement varXml, int index, String name, DynamicVariable dynamicVariable)
    {
        Map<String, List<DynamicVariable>> dynamicvariables = packager.getDynamicVariables();
        List<DynamicVariable> dynamicValues;

        if (dynamicvariables.containsKey(name))
        {
            dynamicValues = dynamicvariables.get(name);
        }
        else
        {
            dynamicValues = new ArrayList<DynamicVariable>();
            dynamicvariables.put(name, dynamicValues);
        }

        if (dynamicValues.remove(dynamicVariable))
        {
            assertionHelper.parseWarn(varXml, "Variable definition '" + dynamicVariable.toString() + "' will be overwritten");
        }
        if (index < 0)
        {
            dynamicValues.add(dynamicVariable);
        }
        else
        {
            dynamicValues.add(index, dynamicVariable);
        }
    }

    private void addDynamicVariable(IXMLElement varXml, String name, DynamicVariable dynamicVariable)
    {
        addDynamicVariable(varXml, -1, name, dynamicVariable);
    }

    private void addDynamicInstallerRequirement(IXMLElement data) throws CompilerException
    {
        notifyCompilerListener("addDynamicInstallerRequirements", CompilerListener.BEGIN, data);
        // We get the dynamic variable list
        IXMLElement root = data.getFirstChildNamed("dynamicinstallerrequirements");
        List<DynamicInstallerRequirementValidator> dynamicReq = packager.getDynamicInstallerRequirements();

        if (root != null)
        {
            List<IXMLElement> installerRequirementList = root
                    .getChildrenNamed("installerrequirement");
            for (IXMLElement installerrequirement : installerRequirementList)
            {
                Status severity = Status.valueOf(xmlCompilerHelper.requireAttribute(installerrequirement, "severity"));
                if (severity == Status.OK)
                {
                    assertionHelper.parseError(installerrequirement, "invalid value for attribute \"severity\"");
                }

                dynamicReq.add(new DynamicInstallerRequirementValidatorImpl(
                        xmlCompilerHelper.requireAttribute(installerrequirement, "condition"),
                        severity,
                        xmlCompilerHelper.requireAttribute(installerrequirement, "messageid")));
            }
        }

        notifyCompilerListener("addDynamicInstallerRequirements", CompilerListener.END, data);
    }

    /**
     * Parse conditions and add them to the compiler.
     *
     * @param data the conditions configuration
     * @throws CompilerException an error occured during compiling
     */
    private void addConditions(IXMLElement data) throws CompilerException
    {
        notifyCompilerListener("addConditions", CompilerListener.BEGIN, data);
        // We get the condition list
        IXMLElement root = data.getFirstChildNamed("conditions");
        Map<String, Condition> conditions = packager.getRules();
        if (root != null)
        {
            for (IXMLElement conditionNode : root.getChildrenNamed("condition"))
            {
                try
                {
                    // Workaround for reading user-defined conditions with fully defined class name
                    // from compile-time classpath
                    String className = rules.getClassName(conditionNode.getAttribute("type"));

                    Class<Condition> conditionClass = classLoader.loadClass(className, Condition.class);
                    Condition condition = rules.createCondition(conditionNode, conditionClass);
                    if (condition != null)
                    {
                        String conditionid = condition.getId();
                        if (conditions.put(conditionid, condition) != null)
                        {
                            assertionHelper.parseWarn(conditionNode,
                                                      "Condition with id '" + conditionid
                                                              + "' has been overwritten");
                        }
                    }
                    else
                    {
                        assertionHelper.parseError(conditionNode, "Error instantiating condition");
                    }
                }
                catch (Exception e)
                {
                    throw new CompilerException("Error reading condition at line "
                                                        + conditionNode.getLineNr() + ": "
                                                        + e.getMessage(), e);
                }
            }
        }
        notifyCompilerListener("addConditions", CompilerListener.END, data);
    }

    private void resolveConditions()
    {
        try
        {
            rules.resolveConditions();
        }
        catch (Exception e)
        {
            throw new CompilerException("Conditions check failed: "
                    + e.getMessage(), e);
        }
    }

    /**
     * Properties declaration is a fragment of the xml file. For example: <p/>
     * <p/>
     * <pre>
     * &lt;p/&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     *        &lt;properties&gt;
     *          &lt;property name=&quot;app.name&quot; value=&quot;Property Laden Installer&quot;/&gt;
     *          &lt;!-- Ant styles 'location' and 'refid' are not yet supported --&gt;
     *          &lt;property file=&quot;filename-relative-to-install?&quot;/&gt;
     *          &lt;property file=&quot;filename-relative-to-install?&quot; prefix=&quot;prefix&quot;/&gt;
     *          &lt;!-- Ant style 'url' and 'resource' are not yet supported --&gt;
     *          &lt;property environment=&quot;prefix&quot;/&gt;
     *        &lt;/properties&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     * </pre>
     * <p/>
     * variable declared in this can be referred to in parsable files.
     *
     * @param data The XML data.
     * @throws CompilerException Description of the Exception
     */
    private void substituteProperties(IXMLElement data) throws CompilerException
    {
        notifyCompilerListener("substituteProperties", CompilerListener.BEGIN, data);

        IXMLElement root = data.getFirstChildNamed("properties");
        if (root != null)
        {
            // add individual properties
            for (IXMLElement propertyNode : root.getChildrenNamed("property"))
            {
                propertyManager.execute(propertyNode);
            }
        }

        // temporarily remove the 'properties' branch, replace all properties in
        // the remaining DOM, and replace properties branch.
        // TODO: enhance IXMLElement with an "indexOf(IXMLElement)" method
        // and addChild(IXMLElement, int) so returns to the same place.
        if (root != null)
        {
            data.removeChild(root);
        }

        substituteAllProperties(data);
        if (root != null)
        {
            data.addChild(root);
        }

        notifyCompilerListener("substituteProperties", CompilerListener.END, data);
    }

    /**
     * Perform recursive substitution on all properties
     */
    private void substituteAllProperties(IXMLElement element) throws CompilerException
    {
        Enumeration<String> attributes = element.enumerateAttributeNames();
        while (attributes.hasMoreElements())
        {
            String name = attributes.nextElement();
            try
            {
                String value = variableSubstitutor.substitute(element.getAttribute(name), SubstitutionType.TYPE_AT);
                element.setAttribute(name, value);
            }
            catch (Exception e)
            {
                assertionHelper.parseWarn(element, "Value of attribute \"" + name + "\" could not be substituted ("
                        + e.getMessage() + ")");
            }
        }

        String content = element.getContent();
        if (content != null)
        {
            try
            {
                element.setContent(variableSubstitutor.substitute(content, SubstitutionType.TYPE_AT));
            }
            catch (Exception e)
            {
                assertionHelper.parseWarn(element, "Embedded content could not be substituted ("
                        + e.getMessage() + ")");
            }
        }

        for (int i = 0; i < element.getChildren().size(); i++)
        {
            IXMLElement child = element.getChildren().get(i);
            substituteAllProperties(child);
        }
    }

    private OverrideType getOverrideValue(IXMLElement fileElement) throws CompilerException
    {
        String override_val = fileElement.getAttribute("override");
        if (override_val == null)
        {
            return OverrideType.OVERRIDE_UPDATE;
        }

        OverrideType override = OverrideType.getOverrideTypeFromAttribute(override_val);
        if (override == null)
        {
            assertionHelper.parseError(fileElement, "invalid value for attribute \"override\"");
        }

        return override;
    }

    private String getOverrideRenameToValue(IXMLElement f) throws CompilerException
    {
        String override_val = f.getAttribute("override");
        String overrideRenameTo = f.getAttribute("overrideRenameTo");

        if (overrideRenameTo != null && override_val == null)
        {
            assertionHelper.parseError(f, "Attribute \"overrideRenameTo\" requires attribute \"override\" to be set");
        }

        return overrideRenameTo;
    }

    /**
     * Parses the blockable element value and adds automatically the OS constraint
     * family=windows if not already se in the given constraint list.
     * Throws a parsing warning if the constraint list was implicitely modified.
     *
     * @param blockableElement the blockable XML element to parse
     * @param osList           constraint list to maintain and return
     * @throws CompilerException an error occured during compiling
     */
    private Blockable getBlockableValue(IXMLElement blockableElement, List<OsModel> osList) throws CompilerException
    {
        String blockable_val = blockableElement.getAttribute("blockable");
        if (blockable_val == null)
        {
            return Blockable.BLOCKABLE_NONE;
        }
        Blockable blockable = Blockable.getBlockableFromAttribute(blockable_val);
        if (blockable == null)
        {
            assertionHelper.parseError(blockableElement, "invalid value for attribute \"blockable\"");
        }

        if (blockable != Blockable.BLOCKABLE_NONE)
        {
            boolean found = false;
            for (OsModel anOsList : osList)
            {
                if ("windows".equals(anOsList.getFamily()))
                {
                    found = true;
                }
            }

            if (!found)
            {
                // We cannot add this constraint here explicitly, because it the copied files might be multi-platform.
                // Print out a warning to inform the user about this fact.
                //osList.add(new OsModel("windows", null, null, null));
                assertionHelper.parseWarn(blockableElement, "'blockable' will apply only on Windows target systems");
            }
        }
        return blockable;
    }

    private boolean validateYesNo(String value)
    {
        boolean result;
        if ("yes".equalsIgnoreCase(value))
        {
            result = true;
        }
        else if ("no".equalsIgnoreCase(value))
        {
            result = false;
        }
        else
        {
            result = Boolean.valueOf(value);
        }
        return result;
    }

    /**
     * Adds installer and uninstaller listeners.
     *
     * @param data the XML data
     * @throws CompilerException if listeners cannot be added
     */
    private void addListeners(IXMLElement data) throws CompilerException
    {
        notifyCompilerListener("addListeners", CompilerListener.BEGIN, data);
        IXMLElement listeners = data.getFirstChildNamed("listeners");
        if (listeners != null)
        {
            for (IXMLElement listener : listeners.getChildrenNamed("listener"))
            {
                String className = xmlCompilerHelper.requireAttribute(listener, "classname");
                Stage stage = Stage.valueOf(xmlCompilerHelper.requireAttribute(listener, "stage"));
                if (Stage.isInInstaller(stage))
                {
                    List<OsModel> constraints = OsConstraintHelper.getOsList(listener);
                    compiler.addListener(className, stage, constraints);
                }
            }
        }
        notifyCompilerListener("addListeners", CompilerListener.END, data);
    }

    /**
     * Register compiler listeners to be notified during compilation.
     */
    private void addCompilerListeners(IXMLElement data) throws CompilerException
    {
        IXMLElement listeners = data.getFirstChildNamed("listeners");
        if (listeners != null)
        {
            for (IXMLElement listener : listeners.getChildrenNamed("listener"))
            {
                String className = xmlCompilerHelper.requireAttribute(listener, "classname");
                Stage stage = Stage.valueOf(xmlCompilerHelper.requireAttribute(listener, "stage"));
                // only process specs for stage="compile" listeners
                if (Stage.compile.equals(stage))
                {
                    // check <os/> specs to see if we need to instantiate and notify this listener
                    List<OsModel> osConstraints = OsConstraintHelper.getOsList(listener);
                    boolean matchesCurrentSystem = false;
                    if (osConstraints.isEmpty())
                    {
                        // assume listener required if no <os/> specs are present in the install file
                        matchesCurrentSystem = true;
                    }
                    else
                    {
                        if (constraints.matchesCurrentPlatform(osConstraints))
                        {
                            matchesCurrentSystem = true;
                        }
                    }
                    // instantiate an instance of the listener only if we're on a system of the specified type
                    if (matchesCurrentSystem)
                    {
                        Class<CompilerListener> clazz = classLoader.loadClass(className, CompilerListener.class);
                        CompilerListener l = factory.create(clazz.getName(), CompilerListener.class);
                        compilerListeners.add(l);
                    }
                }
            }
        }
    }

    /**
     * Calls all defined compile listeners notify method with the given data
     *
     * @param callerName name of the calling method as string
     * @param state      CompileListener.BEGIN or END
     * @param data       current install data
     */
    private void notifyCompilerListener(String callerName, int state, IXMLElement data)
    {
        for (CompilerListener compilerListener : compilerListeners)
        {
            compilerListener.notify(callerName, state, data, packager);
        }

    }

    /**
     * Calls the reviseAdditionalDataMap method of all registered CompilerListener's.
     *
     * @param fileElement file releated XML node
     * @return a map with the additional attributes
     */
    private Map<String, ?> getAdditionals(IXMLElement fileElement) throws CompilerException
    {
        Map<String,?> retval = null;
        try
        {
            for (CompilerListener compilerListener : compilerListeners)
            {
                retval = compilerListener.reviseAdditionalDataMap(retval, fileElement);
            }
        }
        catch (CompilerException ce)
        {
            assertionHelper.parseError(fileElement, ce.getMessage());
        }
        return (retval);
    }

    /**
     * A function to merge multiple packsLang-files into a single file for each identifier, e.g. two
     * resource files
     * <p/>
     * <pre>
     *    &lt;res src=&quot;./packsLang01.xml&quot; id=&quot;packsLang.xml&quot;/&gt;
     *    &lt;res src=&quot;./packsLang02.xml&quot; id=&quot;packsLang.xml&quot;/&gt;
     * </pre>
     * <p/>
     * are merged into a single temp-file to act as if the user had defined:
     * <p/>
     * <pre>
     *    &lt;res src=&quot;/tmp/izpp47881.tmp&quot; id=&quot;packsLang.xml&quot;/&gt;
     * </pre>
     *
     * @throws CompilerException an error occured during compiling
     */
    private void addMergedTranslationResources(Map<String, List<URL>> resourceUrlMap) throws CompilerException
    {
        // just one packslang file. nothing to do here
        if (resourceUrlMap.size() <= 0)
        {
            return;
        }

        OutputStream os = null;
        try
        {
            // loop through all packsLang resources, e.g. packsLang.xml_eng, packsLang.xml_deu, ...
            for (String id : resourceUrlMap.keySet())
            {
                URL mergedPackLangFileURL;

                List<URL> packsLangURLs = resourceUrlMap.get(id);
                if (packsLangURLs.size() == 0)
                {
                    continue;
                } // should not occur

                if (packsLangURLs.size() == 1)
                {
                    // no need to merge files. just use the first URL
                    mergedPackLangFileURL = packsLangURLs.get(0);
                }
                else
                {
                    IXMLElement mergedPacksLang = null;

                    // loop through all that belong to the given identifier
                    for (URL packslangURL : packsLangURLs)
                    {
                        // parsing xml
                        IXMLElement xml = new XMLParser(false).parse(packslangURL);
                        if (mergedPacksLang == null)
                        {
                            // just keep the first file
                            mergedPacksLang = xml;
                        }
                        else
                        {
                            // append data of all xml-docs into the first document
                            List<IXMLElement> langStrings = xml.getChildrenNamed("str");
                            for (IXMLElement langString : langStrings)
                            {
                                mergedPacksLang.addChild(langString);
                            }
                        }
                    }

                    // writing merged strings to a new file
                    File mergedPackLangFile = File.createTempFile("izpp", null, TEMP_DIR);
                    mergedPackLangFile.deleteOnExit();
                    os = FileUtils.openOutputStream(mergedPackLangFile);
                    IXMLWriter xmlWriter = new XMLWriter(os);
                    xmlWriter.write(mergedPacksLang);
                    os.close();
                    os = null;

                    // getting the URL to the new merged file
                    mergedPackLangFileURL = mergedPackLangFile.toURI().toURL();
                }

                packager.addResource(id, mergedPackLangFileURL);
            }
        }
        catch (Exception e)
        {
            throw new CompilerException("Unable to merge multiple " + Resources.PACK_TRANSLATIONS_RESOURCE_NAME + " files: "
                                                + e.getMessage(), e);
        }
        finally
        {
            if (null != os)
            {
                try
                {
                    os.close();
                }
                catch (IOException e)
                {
                    // ignore as there is nothing we can realistically do
                    // so lets at least try to close the input stream
                }
            }
        }
    }

    /**
     * Adds panel actions configured in an XML element to a panel.
     *
     * @param xmlPanel the panel XML element
     * @param panel    the panel
     * @throws CompilerException an error occured during compiling
     */
    private void addPanelActions(IXMLElement xmlPanel, Panel panel) throws CompilerException
    {
        IXMLElement xmlActions = xmlPanel.getFirstChildNamed(PanelAction.PANEL_ACTIONS_TAG);
        if (xmlActions != null)
        {
            List<IXMLElement> actionList = xmlActions.getChildrenNamed(PanelAction.PANEL_ACTION_TAG);
            if (actionList != null)
            {
                for (IXMLElement action : actionList)
                {
                    String stage = xmlCompilerHelper.requireAttribute(action, PanelAction.PANEL_ACTION_STAGE_TAG);
                    String actionName = xmlCompilerHelper.requireAttribute(action,
                                                                           PanelAction.PANEL_ACTION_CLASSNAME_TAG);
                    Class<PanelAction> actionType = classLoader.loadClass(actionName, PanelAction.class);

                    List<IXMLElement> params = action.getChildrenNamed("param");
                    PanelActionConfiguration config = new PanelActionConfiguration(actionType.getName());

                    for (IXMLElement param : params)
                    {
                        String name = xmlCompilerHelper.requireAttribute(param, "name");
                        String value = xmlCompilerHelper.requireAttribute(param, "value");
                        logger.fine("Adding configuration property " + name + " with value "
                                            + value + " for action " + actionName);
                        config.addProperty(name, value);
                    }
                    try
                    {
                        PanelAction.ActionStage actionStage = PanelAction.ActionStage.valueOf(stage);
                        switch (actionStage)
                        {
                            case preconstruct:
                                panel.addPreConstructionAction(config);
                                break;
                            case preactivate:
                                panel.addPreActivationAction(config);
                                break;
                            case prevalidate:
                                panel.addPreValidationAction(config);
                                break;
                            case postvalidate:
                                panel.addPostValidationAction(config);
                                break;
                        }
                    }
                    catch (IllegalArgumentException e)
                    {
                        assertionHelper.parseError(action, "Invalid value [" + stage + "] for attribute : "
                                + PanelAction.PANEL_ACTION_STAGE_TAG);
                    }
                }
            }
            else
            {
                assertionHelper.parseError(xmlActions, "<" + PanelAction.PANEL_ACTIONS_TAG + "> requires a <"
                        + PanelAction.PANEL_ACTION_TAG + ">");
            }
        }
    }

    private List<TargetFileSet> readFileSets(IXMLElement parent, File baseDir) throws CompilerException
    {
        List<TargetFileSet> fslist = new ArrayList<TargetFileSet>();
        for (IXMLElement fileSetNode : parent.getChildrenNamed("fileset"))
        {
            try
            {
                fslist.add(readFileSet(fileSetNode, baseDir));
            }
            catch (Exception e)
            {
                throw new CompilerException(getMessage(e) + " while processing fileset " + fileSetNode.getName());
            }
        }
        return fslist;
    }

    private TargetFileSet readFileSet(IXMLElement fileSetNode, File baseDir) throws CompilerException
    {
        String dir_attr = getDirSubstitutedAttributeValue(fileSetNode);
        File extractedBaseDir = baseDir;
        if (dir_attr != null)
        {
            extractedBaseDir = FileUtil.getAbsoluteFile(dir_attr, baseDir.getAbsolutePath());
        }
        String targetDir = fileSetNode.getAttribute("targetdir", "${INSTALL_PATH}");

        return readFileSet(fileSetNode, extractedBaseDir, targetDir);
    }

    private TargetFileSet readArchiveFileSet(IXMLElement fileSetNode, File baseDir, String targetDir) throws CompilerException
    {
        String dir_attr = getDirSubstitutedAttributeValue(fileSetNode);
        File extractedBaseDir = baseDir;
        if (dir_attr != null)
        {
            extractedBaseDir = new File(baseDir, dir_attr);
            if (!extractedBaseDir.exists()) {
                assertionHelper.parseError(fileSetNode, "Archive does not contain a base directory " + dir_attr);
            }
        }

        return readFileSet(fileSetNode, extractedBaseDir, targetDir);
    }


    private TargetFileSet readFileSet(IXMLElement fileSetNode, File baseDir, String targetDir) throws CompilerException
    {
        TargetFileSet fs = new TargetFileSet();

        fs.setTargetDir(targetDir);
        List<OsModel> osList = OsConstraintHelper.getOsList(fileSetNode);
        fs.setOsList(osList);
        fs.setOverride(getOverrideValue(fileSetNode));
        fs.setOverrideRenameTo(getOverrideRenameToValue(fileSetNode));
        fs.setBlockable(getBlockableValue(fileSetNode, osList));
        fs.setAdditionals(getAdditionals(fileSetNode));
        String conditionId = parseConditionAttribute(fileSetNode);
        if (conditionId != null)
        {
            fs.setCondition(conditionId);
        }

        try
        {
            fs.setDir(baseDir);
        }
        catch (Exception e)
        {
            throw new CompilerException(getMessage(e) + " while processing fileset " + fileSetNode.getName());
        }

        String attr = fileSetNode.getAttribute("includes");
        if (attr != null)
        {
            fs.setIncludes(attr);
        }

        attr = fileSetNode.getAttribute("excludes");
        if (attr != null)
        {
            fs.setExcludes(attr);
        }

        String boolval = fileSetNode.getAttribute("casesensitive");
        if (boolval != null)
        {
            fs.setCaseSensitive(Boolean.parseBoolean(boolval));
        }

        boolval = fileSetNode.getAttribute("defaultexcludes");
        if (boolval != null)
        {
            fs.setDefaultexcludes(Boolean.parseBoolean(boolval));
        }

        boolval = fileSetNode.getAttribute("followsymlinks");
        if (boolval != null)
        {
            fs.setFollowSymlinks(Boolean.parseBoolean(boolval));
        }

        readAndAddIncludes(fileSetNode, fs);
        readAndAddExcludes(fileSetNode, fs);

        return fs;
    }

    private String getMessage(Exception e)
    {
        String message = e.getMessage();
        if (message == null) {
            message = e.getClass().getName();
        }
        return message;
    }

    private void readAndAddIncludes(IXMLElement parent, TargetFileSet fileset)
            throws CompilerException
    {
        for (IXMLElement f : parent.getChildrenNamed("include"))
        {
            fileset.createInclude().setName(
                    variableSubstitutor.substitute(
                            xmlCompilerHelper.requireAttribute(f, "name")));
        }
    }

    private void readAndAddExcludes(IXMLElement parent, TargetFileSet fileset)
            throws CompilerException
    {
        for (IXMLElement f : parent.getChildrenNamed("exclude"))
        {
            fileset.createExclude().setName(
                    variableSubstitutor.substitute(
                            xmlCompilerHelper.requireAttribute(f, "name")));
        }
    }

    private String parseConditionAttribute(IXMLElement element)
    {
        String conditionId = element.getAttribute("condition");
        if (conditionId != null)
        {
            List<IXMLElement> elList = referencedConditions.get(conditionId);
            if (elList == null)
            {
                elList = new ArrayList<IXMLElement>();
                referencedConditions.put(conditionId, elList);
            }
            elList.add(element);
        }
        return conditionId;
    }

    private boolean checkReferencedConditions(Map<String, List<IXMLElement>> referringElements, AssertionHelper assertionHelper)
    {
        boolean failure = false;
        for (String conditionId : referringElements.keySet())
        {
            if (rules.getCondition(conditionId) == null)
            {
                List<IXMLElement> elList = referringElements.get(conditionId);
                for (IXMLElement element : elList)
                {
                    assertionHelper.parseWarn(element,
                            "Expression '" + conditionId + "' contains reference(s) to undefined condition(s)");
                    failure = true;
                }
            }
        }
        return failure;
    }

    private void checkReferencedConditions()
    {
        boolean failure = checkReferencedConditions(referencedConditions, assertionHelper);
        failure |= checkReferencedConditions(referencedConditionsUserInputSpec,
                new AssertionHelper("Resource " + UserInputPanelSpec.SPEC_FILE_NAME));
        failure |= checkReferencedConditions(referencedConditionsAntActionSpec,
                new AssertionHelper("Resource " + AntActionInstallerListener.SPEC_FILE_NAME));
        failure |= checkReferencedConditions(referencedConditionsConfigurationActionSpec,
                new AssertionHelper("Resource " + ConfigurationInstallerListener.SPEC_FILE_NAME));
        if (failure)
        {
            throw new CompilerException("Cannot recover from reference(s) to undefined condition(s) listed above");
        }
    }

    private void checkReferencedPacks()
    {
        AssertionHelper antActionSpecAssertionHelper
                = new AssertionHelper("Resource " + AntActionInstallerListener.SPEC_FILE_NAME);
        AssertionHelper configurationSpecAssertionHelper
                = new AssertionHelper("Resource " + ConfigurationInstallerListener.SPEC_FILE_NAME);
        List<PackInfo> packs = packager.getPacksList();
        Set<String> definedPackNames = new HashSet<String>(packs.size());
        for (PackInfo packInfo:packs)
        {
            definedPackNames.add(packInfo.getPack().getName());
        }
        for (String packName : referencedPacksAntActionSpec.keySet())
        {
            if (!definedPackNames.contains(packName))
            {
                IXMLElement element = referencedPacksAntActionSpec.get(packName);
                antActionSpecAssertionHelper.parseError(element,
                        "Expression '" + packName + "' refers to undefined pack");
            }
        }
        for (String packName : referencedPacksConfigurationActionSpec.keySet())
        {
            if (!definedPackNames.contains(packName))
            {
                IXMLElement element = referencedPacksConfigurationActionSpec.get(packName);
                configurationSpecAssertionHelper.parseError(element,
                        "Expression '" + packName + "' refers to undefined pack");
            }
        }
    }
    
    /**
     * Retrieves the substituted value of the given node's "src" attribute.
     * 
     * @param node the node you want the "src" attribute value from
     * @return the substituted value of node's "src" attribute
     */
    private String getSrcSubstitutedAttributeValue(IXMLElement node)
    {
        return getSubstitutedAttributeValue(node, SRC_ATTRIBUTE);
    }

    /**
     * Retrieves the substituted value of the given node's "dir" attribute.
     * 
     * @param node the node you want the "dir" attribute value from
     * @return the substituted value of node's "dir" attribute
     */
    private String getDirSubstitutedAttributeValue(IXMLElement node)
    {
        return getSubstitutedAttributeValue(node, DIR_ATTRIBUTE);
    }
    
    /**
     * Retrieves the substituted value of the given node's attribute.
     * 
     * @param node the XML node you want the attribute value from
     * @param attribute the attribute you want the value from
     * @return the substituted value
     */
    private String getSubstitutedAttributeValue(IXMLElement node, String attribute)
    {
        String attributeValue = xmlCompilerHelper.requireAttribute(node, attribute);
        return variableSubstitutor.substitute(attributeValue);
    }

    private int getMinJavaVersion()
    {
        String javaVersion = compilerData.getExternalInfo().getJavaVersion();
        int minJavaVersion = 1;
        if (javaVersion != null && !javaVersion.isEmpty())
        {
            minJavaVersion = JavaVersion.parse(javaVersion).feature();
        }
        return minJavaVersion;
    }

    // Logging helper methods

    private void logAddingFile(String source, String target)
    {
        logger.log(Level.FINE, "Adding file {0} => {1}",
                new String[]{source, target.replaceFirst("\\$\\{*INSTALL_PATH}*[/\\\\]+", "")});
    }

    private void logMarkFileExecutable(String target)
    {
        logger.log(Level.INFO, "Marked target file executable: {0}",
                new String[]{target.replaceFirst("\\$\\{*INSTALL_PATH}*[/\\\\]+", "")});
    }

    private void logMarkFileParsable(String target)
    {
        logger.log(Level.INFO, "Marked target file parsable: {0}",
                new String[]{target.replaceFirst("\\$\\{*INSTALL_PATH}*[/\\\\]+", "")});
    }

    private void logAddingPack(PackInfo packInfo)
    {
        logger.log(Level.INFO, "Adding pack {0} containing {1} files",
                new String[]{packInfo.getPack().getName(), Integer.toString(packInfo.getPackFiles().size())});
    }
    
    private void logCombineOsLists(List<OsModel> parentOsList, List<OsModel> osList, List<OsModel> commonOsList)
    {
        logger.log(Level.INFO, "Combined parent''s OS constraints:\n\t{0}\nwith node''s:\n\t{1}\ninto:\n\t{2}",
                new String[]{
                    OsConstraintHelper.toOsContraintsString(parentOsList),
                    OsConstraintHelper.toOsContraintsString(osList),
                    OsConstraintHelper.toOsContraintsString(commonOsList)
                }
        );
    }

}

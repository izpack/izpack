/*
 * $Id$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2001 Johannes Lehtinen
 * Copyright 2002 Paul Wilkinson
 * Copyright 2004 Gaganis Giorgos
 * Copyright 2007 Syed Khadeer / Hans Aikema
 *
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
import com.izforge.izpack.api.container.BindeableContainer;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Blockable;
import com.izforge.izpack.api.data.DynamicInstallerRequirementValidator;
import com.izforge.izpack.api.data.DynamicVariable;
import com.izforge.izpack.api.data.GUIPrefs;
import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.Info.TempDir;
import com.izforge.izpack.api.data.InstallerRequirement;
import com.izforge.izpack.api.data.LookAndFeels;
import com.izforge.izpack.api.data.OverrideType;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.data.binding.IzpackProjectInstaller;
import com.izforge.izpack.api.data.binding.Listener;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.data.binding.Stage;
import com.izforge.izpack.api.exception.CompilerException;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.api.installer.DataValidator.Status;
import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.SubstitutionType;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.data.PropertyManager;
import com.izforge.izpack.compiler.helper.AssertionHelper;
import com.izforge.izpack.compiler.helper.CompilerHelper;
import com.izforge.izpack.compiler.helper.TargetFileSet;
import com.izforge.izpack.compiler.helper.XmlCompilerHelper;
import com.izforge.izpack.compiler.listener.CompilerListener;
import com.izforge.izpack.compiler.packager.IPackager;
import com.izforge.izpack.compiler.resource.ResourceFinder;
import com.izforge.izpack.core.data.DynamicInstallerRequirementValidatorImpl;
import com.izforge.izpack.core.data.DynamicVariableImpl;
import com.izforge.izpack.core.regex.RegularExpressionFilterImpl;
import com.izforge.izpack.core.variable.ConfigFileValue;
import com.izforge.izpack.core.variable.EnvironmentValue;
import com.izforge.izpack.core.variable.ExecValue;
import com.izforge.izpack.core.variable.JarEntryConfigValue;
import com.izforge.izpack.core.variable.PlainConfigFileValue;
import com.izforge.izpack.core.variable.PlainValue;
import com.izforge.izpack.core.variable.RegistryValue;
import com.izforge.izpack.core.variable.ZipEntryConfigFileValue;
import com.izforge.izpack.data.CustomData;
import com.izforge.izpack.data.ExecutableFile;
import com.izforge.izpack.data.PackInfo;
import com.izforge.izpack.data.PanelAction;
import com.izforge.izpack.data.ParsableFile;
import com.izforge.izpack.data.UpdateCheck;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.resolve.ClassPathCrawler;
import com.izforge.izpack.merge.resolve.PathResolver;
import com.izforge.izpack.util.ClassUtils;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.FileUtil;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.OsConstraintHelper;
import com.izforge.izpack.util.file.DirectoryScanner;
import com.izforge.izpack.util.file.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * A parser for the installer xml configuration. This parses a document conforming to the
 * installation.dtd and populates a Compiler instance to perform the install compilation.
 *
 * @author Scott Stark
 * @version $Revision$
 */
public class CompilerConfig extends Thread
{

    /**
     * Constant for checking attributes.
     */
    private static boolean YES = true;

    /**
     * Constant for checking attributes.
     */
    private static boolean NO = false;

    /**
     * The installer packager compiler
     */
    private Compiler compiler;

    /**
     * Installer data
     */
    private CompilerData compilerData;

    /**
     * Compiler helper
     */
    private CompilerHelper compilerHelper;

    /**
     * List of CompilerListeners which should be called at packaging
     */
    private List<CompilerListener> compilerListeners = new ArrayList<CompilerListener>();

    /**
     * A list of packsLang-files that were defined by the user in the resource-section The key of
     * this map is an packsLang-file identifier, e.g. <code>packsLang.xml_eng</code>, the values
     * are lists of {@link URL} pointing to the concrete packsLang-files.
     *
     * @see #mergePacksLangFiles()
     */
    private Map<String, List<URL>> packsLangUrlMap = new HashMap<String, List<URL>>();
    private String unpackerClassname = "com.izforge.izpack.installer.unpacker.Unpacker";
    private String packagerClassname = "com.izforge.izpack.compiler.packager.impl.Packager";
    private PathResolver pathResolver;
    private VariableSubstitutor variableSubstitutor;
    private XmlCompilerHelper xmlCompilerHelper;
    private PropertyManager propertyManager;
    private IPackager packager;
    private ResourceFinder resourceFinder;
    private MergeManager mergeManager;
    private IzpackProjectInstaller izpackProjectInstaller;
    private AssertionHelper assertionHelper;
    private BindeableContainer compilerContainer;
    private ClassPathCrawler classPathCrawler;
    private RulesEngine rules;

    private static final String TEMP_DIR_ELEMENT_NAME = "tempdir";

    private static final String TEMP_DIR_PREFIX_ATTRIBUTE = "prefix";

    private static final String DEFAULT_TEMP_DIR_PREFIX = "IzPack";

    private static final String TEMP_DIR_SUFFIX_ATTRIBUTE = "suffix";

    private static final String DEFAULT_TEMP_DIR_SUFFIX = "Install";

    private static final String TEMP_DIR_VARIABLE_NAME_ATTRIBUTE = "variablename";

    private static final String TEMP_DIR_DEFAULT_PROPERTY_NAME = "TEMP_DIRECTORY";


    /**
     * Constructor
     *
     * @param compilerData Object containing all informations found in command line
     */
    public CompilerConfig(CompilerData compilerData, VariableSubstitutor variableSubstitutor, Compiler compiler, CompilerHelper compilerHelper, XmlCompilerHelper xmlCompilerHelper, PropertyManager propertyManager, IPackager packager, MergeManager mergeManager, IzpackProjectInstaller izpackProjectInstaller, AssertionHelper assertionHelper, CompilerContainer compilerContainer, ClassPathCrawler classPathCrawler, RulesEngine rules, PathResolver pathResolver, ResourceFinder resourceFinder)
    {
        this.assertionHelper = assertionHelper;
        this.rules = rules;
        this.compilerData = compilerData;
        this.variableSubstitutor = variableSubstitutor;
        this.compiler = compiler;
        this.compilerHelper = compilerHelper;
        this.xmlCompilerHelper = xmlCompilerHelper;
        this.propertyManager = propertyManager;
        this.packager = packager;
        this.mergeManager = mergeManager;
        this.izpackProjectInstaller = izpackProjectInstaller;
        this.compilerContainer = compilerContainer;
        this.classPathCrawler = classPathCrawler;
        this.pathResolver = pathResolver;
        this.resourceFinder = resourceFinder;
    }

    /**
     * The run() method.
     */
    public void run()
    {
        try
        {
            executeCompiler();
        }
        catch (CompilerException ce)
        {
            System.out.println(ce.getMessage() + "\n");
        }
        catch (Exception e)
        {
            if (Debug.stackTracing())
            {
                e.printStackTrace();
            }
            else
            {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
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
            throw new CompilerException(
                    "Invalid base directory: " + base);
        }

        // add izpack built in property
        propertyManager.setProperty("basedir", base.toString());

        addCompilerListeners();

        // We get the XML data tree
        IXMLElement data = resourceFinder.getXMLTree();
        // loads the specified packager
        loadPackagingInformation(data);

        // Read the properties and perform replacement on the rest of the tree
        substituteProperties(data);

        // We add all the information
        addVariables(data);
        addDynamicVariables(data);
        addDynamicInstallerRequirement(data);
        addConditions(data);
        addInfo(data);
        addGUIPrefs(data);
        addLangpacks(data);
        addResources(data);
        addNativeLibraries(data);
        addJars(data);
        addPanels(data);
        addListeners(data);
        addPacks(data);
        addInstallerRequirement(data);

        // merge multiple packlang.xml files
        mergePacksLangFiles();

        // We ask the packager to create the installer
        compiler.createInstaller();
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
                String condition = installerrequirement.getAttribute("condition");
                basicInstallerCondition.setCondition(condition);
                String message = installerrequirement.getAttribute("message");
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
                packagerClassname = xmlCompilerHelper.requireAttribute(packagerElement, "class");
            }

            IXMLElement unpacker = root.getFirstChildNamed("unpacker");

            if (unpacker != null)
            {
                unpackerClassname = xmlCompilerHelper.requireAttribute(unpacker, "class");
            }
        }
        if (packagerElement != null)
        {
            IXMLElement options = packagerElement.getFirstChildNamed("options");
            if (options != null)
            {
                packager.addConfigurationInformation(options);
            }
        }
        propertyManager.addProperty("UNPACKER_CLASS", unpackerClassname);
        notifyCompilerListener("loadPackager", CompilerListener.END, data);
    }

    public boolean wasSuccessful()
    {
        return compiler.wasSuccessful();
    }

    /**
     * Returns the GUIPrefs.
     *
     * @param data The XML data.
     * @throws CompilerException Description of the Exception
     */
    protected void addGUIPrefs(IXMLElement data) throws CompilerException
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

                for (IXMLElement osNode : lafNode.getChildrenNamed("os"))
                {
                    String osName = xmlCompilerHelper.requireAttribute(osNode, "family");
                    prefs.lookAndFeelMapping.put(osName, lafName);
                }

                Map<String, String> params = new TreeMap<String, String>();
                for (IXMLElement parameterNode : lafNode.getChildrenNamed("param"))
                {
                    String name = xmlCompilerHelper.requireAttribute(parameterNode, "name");
                    String value = xmlCompilerHelper.requireAttribute(parameterNode, "value");
                    params.put(name, value);
                }
                prefs.lookAndFeelParams.put(lafName, params);
            }
            // Load modifier
            for (IXMLElement ixmlElement : guiPrefsElement.getChildrenNamed("modifier"))
            {
                String key = xmlCompilerHelper.requireAttribute(ixmlElement, "key");
                String value = xmlCompilerHelper.requireAttribute(ixmlElement, "value");
                prefs.modifier.put(key, value);

            }
            for (String s : prefs.lookAndFeelMapping.keySet())
            {
                String lafName = prefs.lookAndFeelMapping.get(s);
                LookAndFeels feels = LookAndFeels.lookup(lafName);
                List<Mergeable> mergeableList = Collections.emptyList();
                switch (feels)
                {
                    case KUNSTSTOFF:
                        mergeableList = pathResolver.getMergeableFromPackageName("com/incors/plaf");
                        break;
                    case LIQUID:
                        mergeableList = pathResolver.getMergeableFromPackageName("com/birosoft/liquid/");
                        break;
                    case LOOKS:
                        mergeableList = pathResolver.getMergeableFromPackageName("com/jgoodies/looks");
                        break;
                    case SUBSTANCE:
                        mergeableList = pathResolver.getMergeableJarFromPackageName("org/pushingpixels");
                        mergeableList.addAll(pathResolver.getMergeableFromPackageName("nanoxml"));
                        break;
                    case NIMBUS:
                        mergeableList = pathResolver.getMergeableFromPackageName("com/sun/java/swing/plaf/nimbus");
                        break;
                    default:
                        assertionHelper.parseError(guiPrefsElement, "Unrecognized Look and Feel: " + lafName);
                }
                for (Mergeable mergeable : mergeableList)
                {
                    mergeManager.addResourceToMerge(mergeable);
                }
            }
        }
        packager.setGUIPrefs(prefs);
        notifyCompilerListener("addGUIPrefs", CompilerListener.END, data);
    }

    /**
     * Add project specific external jar files to the installer.
     *
     * @param data The XML data.
     */
    protected void addJars(IXMLElement data) throws Exception
    {
        notifyCompilerListener("addJars", CompilerListener.BEGIN, data);
        for (IXMLElement ixmlElement : data.getChildrenNamed("jar"))
        {
            String src = xmlCompilerHelper.requireAttribute(ixmlElement, "src");

            //all external jars contents regardless of stage type are merged into the installer
            // but we keep a copy of jar entries that user want to merge into uninstaller
            // as "customData", where the installer will get them into uninstaller.jar at the end of installation
            // note if stage is empty or null, it is the same at 'install'
            String stage = ixmlElement.getAttribute("stage");
            URL url = resourceFinder.findProjectResource(src, "Jar file", ixmlElement);
            CustomData customData = null;
            if ("both".equalsIgnoreCase(stage) || "uninstall".equalsIgnoreCase(stage))
            {
                customData = new CustomData(null, compilerHelper.getContainedFilePaths(url), null,
                        CustomData.UNINSTALLER_JAR);
            }
            packager.addCustomJar(customData, url);
            ClassUtils.loadJarInSystemClassLoader(FileUtil.convertUrlToFile(url));

        }
        notifyCompilerListener("addJars", CompilerListener.END, data);
    }

    /**
     * Add native libraries to the installer.
     *
     * @param data The XML data.
     */
    protected void addNativeLibraries(IXMLElement data) throws Exception
    {
        boolean needAddOns = false;
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
            String path = ixmlElement.getAttribute("src");
            if (path == null)
            {
                path = "com/izforge/izpack/bin/native/" + type + "/" + name;
            }
            String destination = "com/izforge/izpack/bin/native/" + name;
            mergeManager.addResourceToMerge(path, destination);

            // Additionals for mark a native lib also used in the uninstaller
            // The lib will be copied from the installer into the uninstaller if
            // needed.
            // Therefore the lib should be in the installer also it is used only
            // from
            // the uninstaller. This is the reason why the stage wiil be only
            // observed
            // for the uninstaller.
            String stage = ixmlElement.getAttribute("stage");
            List<OsModel> constraints = OsConstraintHelper.getOsList(ixmlElement);
            if ("both".equalsIgnoreCase(stage) || "uninstall".equalsIgnoreCase(stage))
            {
                List<String> contents = new ArrayList<String>();
                contents.add(destination);
                CustomData customData = new CustomData(null, contents, constraints, CustomData.UNINSTALLER_LIB);
                packager.addNativeUninstallerLibrary(customData);
                needAddOns = true;
            }

        }
        if (needAddOns)
        {
            // Add the uninstaller extensions as a resource if specified
            IXMLElement root = xmlCompilerHelper.requireChildNamed(data, "info");
            IXMLElement uninstallInfo = root.getFirstChildNamed("uninstaller");
            if (xmlCompilerHelper.validateYesNoAttribute(uninstallInfo, "write", YES))
            {
                //REFACTOR Change the way uninstaller are created
                // Do we still need it on compile time?
//                URL url = findIzPackResource(propertyManager.getProperty("uninstaller-ext"), "Uninstaller extensions", root);
//                packager.addResource("IzPack.uninstaller-ext", url);
            }

        }
        notifyCompilerListener("addNativeLibraries", CompilerListener.END, data);
    }

    /**
     * Add packs and their contents to the installer.
     *
     * @param data The XML data.
     */
    protected void addPacks(IXMLElement data) throws CompilerException
    {
        notifyCompilerListener("addPacks", CompilerListener.BEGIN, data);

        // the actual adding is delegated to addPacksSingle to enable recursive
        // parsing of refpack package definitions
        addPacksSingle(data);

        compiler.checkDependencies();
        compiler.checkExcludes();

        notifyCompilerListener("addPacks", CompilerListener.END, data);
    }

    /**
     * Add packs and their contents to the installer without checking the dependencies and includes.
     * <p/> Helper method to recursively add more packs from refpack XML packs definitions
     *
     * @param data The XML data
     * @throws CompilerException
     */
    private void addPacksSingle(IXMLElement data) throws CompilerException
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

        File baseDir = new File(compilerData.getBasedir());

        for (IXMLElement packElement : packElements)
        {

            // Trivial initialisations
            String name = xmlCompilerHelper.requireAttribute(packElement, "name");
            String id = packElement.getAttribute("id");
            String packImgId = packElement.getAttribute("packImgId");

            boolean loose = "true".equalsIgnoreCase(packElement.getAttribute("loose", "false"));
            String description = xmlCompilerHelper.requireChildNamed(packElement, "description").getContent();
            boolean required = xmlCompilerHelper.requireYesNoAttribute(packElement, "required");
            String group = packElement.getAttribute("group");
            String installGroups = packElement.getAttribute("installGroups");
            String excludeGroup = packElement.getAttribute("excludeGroup");
            boolean uninstall = "yes".equalsIgnoreCase(packElement.getAttribute("uninstall", "yes"));
            String parent = packElement.getAttribute("parent");
            boolean hidden = "true".equalsIgnoreCase(packElement.getAttribute("hidden", "false"));

            String conditionid = packElement.getAttribute("condition");

            if (required && excludeGroup != null)
            {
                assertionHelper.parseError(packElement, "Pack, which has excludeGroup can not be required.", new Exception(
                        "Pack, which has excludeGroup can not be required."));
            }

            PackInfo pack = new PackInfo(name, id, description, required, loose, excludeGroup,
                    uninstall);
            pack.setOsConstraints(OsConstraintHelper.getOsList(packElement)); // TODO:
            pack.setParent(parent);
            pack.setCondition(conditionid);
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

            List<IXMLElement> parsableChildren = packElement.getChildrenNamed("parsable");
            processParsableChildren(pack, parsableChildren);

            List<IXMLElement> executableChildren = packElement.getChildrenNamed("executable");
            processExecutableChildren(pack, executableChildren);

            processFileChildren(baseDir, packElement, pack);

            processSingleFileChildren(baseDir, packElement, pack);

            processFileSetChildren(baseDir, packElement, pack);

            processUpdateCheckChildren(packElement, pack);

            // We get the dependencies
            for (IXMLElement dependsNode : packElement.getChildrenNamed("depends"))
            {
                String depName = xmlCompilerHelper.requireAttribute(dependsNode, "packname");
                pack.addDependency(depName);

            }

            for (IXMLElement validatorNode : packElement.getChildrenNamed("validator"))
            {
                pack.addValidator(xmlCompilerHelper.requireContent(validatorNode));
            }

            // We add the pack
            packager.addPack(pack);
        }

        for (IXMLElement refPackElement : refPackElements)
        {

            // get the name of reference xml file
            String refFileName = xmlCompilerHelper.requireAttribute(refPackElement, "file");
            String selfcontained = refPackElement.getAttribute("selfcontained");
            boolean isselfcontained = Boolean.valueOf(selfcontained);

            // parsing ref-pack-set file
            IXMLElement refXMLData = this.readRefPackData(refFileName, isselfcontained);

            Debug.log("Reading refpack from " + refFileName);
            // Recursively call myself to add all packs and refpacks from the reference XML
            addPacksSingle(refXMLData);
        }

        for (IXMLElement refPackSet : refPackSets)
        {

            // the directory to scan
            String dir_attr = xmlCompilerHelper.requireAttribute(refPackSet, "dir");

            File dir = new File(dir_attr);
            if (!dir.isAbsolute())
            {
                dir = new File(compilerData.getBasedir(), dir_attr);
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

            // loop through all found fils and handle them as normal refpack files
            String[] files;
            try
            {
                ds.scan();

                files = ds.getIncludedFiles();
                for (String file : files)
                {
                    String refFileName = new File(dir, file).toString();

                    // parsing ref-pack-set file
                    IXMLElement refXMLData = this.readRefPackData(refFileName, false);

                    // Recursively call myself to add all packs and refpacks from the reference XML
                    addPacksSingle(refXMLData);
                }
            }
            catch (Exception e)
            {
                throw new CompilerException(e.getMessage());
            }
        }

        notifyCompilerListener("addPacksSingle", CompilerListener.END, data);
    }

    private void processUpdateCheckChildren(IXMLElement packElement, PackInfo pack) throws CompilerException
    {
        for (IXMLElement updateNode : packElement.getChildrenNamed("updatecheck"))
        {

            String casesensitive = updateNode.getAttribute("casesensitive");

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

            pack.addUpdateCheck(new UpdateCheck(includesList, excludesList, casesensitive));
        }
    }

    private void processFileSetChildren(File baseDir, IXMLElement packElement, PackInfo pack) throws CompilerException
    {
        for (TargetFileSet fs : readFileSets(packElement))
        {
            try
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
                            try
                            {
                                File file = new File(fs.getDir(), filePath);
                                String target = new File(fs.getTargetDir(), filePath).getPath();
                                pack.addFile(baseDir, file, target, fs.getOsList(), fs
                                        .getOverride(), fs.getOverrideRenameTo(), fs.getBlockable(), fs.getAdditionals(), fs
                                        .getCondition());
                            }
                            catch (FileNotFoundException x)
                            {
                                assertionHelper.parseError(packElement, x.getMessage(), x);
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                throw new CompilerException(e.getMessage());
            }
        }
    }

    private void processSingleFileChildren(File baseDir, IXMLElement packElement, PackInfo pack) throws CompilerException
    {
        for (IXMLElement singleFileNode : packElement.getChildrenNamed("singlefile"))
        {
            String src = xmlCompilerHelper.requireAttribute(singleFileNode, "src");
            String target = xmlCompilerHelper.requireAttribute(singleFileNode, "target");
            List<OsModel> osList = OsConstraintHelper.getOsList(singleFileNode); // TODO: unverified
            OverrideType override = getOverrideValue(singleFileNode);
            String overrideRenameTo = getOverrideRenameToValue(singleFileNode);
            Blockable blockable = getBlockableValue(singleFileNode, osList);
            Map additionals = getAdditionals(singleFileNode);
            String condition = singleFileNode.getAttribute("condition");
            File file = new File(src);
            if (!file.isAbsolute())
            {
                file = new File(compilerData.getBasedir(), src);
            }

            // if the path does not exist, maybe it contains variables
            if (!file.exists())
            {
                try
                {
                    file = new File(variableSubstitutor.substitute(file.getAbsolutePath()));
                }
                catch (Exception e)
                {
                    assertionHelper.parseWarn(singleFileNode, e.getMessage());
                }
                // next existance checking appears in pack.addFile
            }

            try
            {
                pack.addFile(baseDir, file, target, osList, override, overrideRenameTo, blockable, additionals, condition);
            }
            catch (FileNotFoundException x)
            {
                assertionHelper.parseError(singleFileNode, x.getMessage(), x);
            }
        }
    }

    private void processFileChildren(File baseDir, IXMLElement packElement, PackInfo pack) throws CompilerException
    {
        for (IXMLElement fileNode : packElement.getChildrenNamed("file"))
        {
            String src = xmlCompilerHelper.requireAttribute(fileNode, "src");
            boolean unpack = "true".equalsIgnoreCase(fileNode.getAttribute("unpack"));

            TargetFileSet fs = new TargetFileSet();
            try
            {
                File relsrcfile = new File(src);
                File abssrcfile = FileUtil.getAbsoluteFile(src, compilerData.getBasedir());
                if (relsrcfile.isDirectory())
                {
                    fs.setDir(abssrcfile.getParentFile());
                    fs.createInclude().setName(relsrcfile.getName() + "/**");
                }
                else
                {
                    fs.setFile(abssrcfile);
                }
                fs.setTargetDir(xmlCompilerHelper.requireAttribute(fileNode, "targetdir"));
                List<OsModel> osList = OsConstraintHelper.getOsList(fileNode); // TODO: unverified
                fs.setOsList(osList);
                fs.setOverride(getOverrideValue(fileNode));
                fs.setOverrideRenameTo(getOverrideRenameToValue(fileNode));
                fs.setBlockable(getBlockableValue(fileNode, osList));
                fs.setAdditionals(getAdditionals(fileNode));
                fs.setCondition(fileNode.getAttribute("condition"));

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
                for (String filePath : fs.getDirectoryScanner().getIncludedDirectories())
                {
                    srcfiles.add(filePath);
                }
                for (String filePath : fs.getDirectoryScanner().getIncludedFiles())
                {
                    srcfiles.add(filePath);
                }
                for (String filePath : srcfiles)
                {
                    try
                    {
                        abssrcfile = new File(fs.getDir(), filePath);
                        if (unpack)
                        {
                            addArchiveContent(baseDir, abssrcfile, fs.getTargetDir(), fs
                                    .getOsList(), fs.getOverride(), fs.getOverrideRenameTo(), fs.getBlockable(), pack, fs
                                    .getAdditionals(), fs.getCondition());
                        }
                        else
                        {
                            pack.addFile(baseDir, abssrcfile, fs.getTargetDir() + "/" + filePath, fs.getOsList(),
                                    fs.getOverride(), fs.getOverrideRenameTo(), fs.getBlockable(), fs.getAdditionals(),
                                    fs.getCondition());
                        }

                    }
                    catch (FileNotFoundException x)
                    {
                        assertionHelper.parseError(packElement, x.getMessage(), x);
                    }
                }
            }
            catch (Exception e)
            {
                throw new CompilerException(e.getMessage());
            }
        }
    }

    private void processExecutableChildren(PackInfo pack, List<IXMLElement> childrenNamed) throws CompilerException
    {
        for (IXMLElement executableNode : childrenNamed)
        {
            ExecutableFile executable = new ExecutableFile();
            String val; // temp value
            String condition = executableNode.getAttribute("condition");
            executable.setCondition(condition);
            executable.path = xmlCompilerHelper.requireAttribute(executableNode, "targetfile");

            // when to execute this executable
            val = executableNode.getAttribute("stage", "never");
            if ("postinstall".equalsIgnoreCase(val))
            {
                executable.executionStage = ExecutableFile.POSTINSTALL;
            }
            else if ("uninstall".equalsIgnoreCase(val))
            {
                executable.executionStage = ExecutableFile.UNINSTALL;
            }

            // type of this executable
            val = executableNode.getAttribute("type", "bin");
            if ("jar".equalsIgnoreCase(val))
            {
                executable.type = ExecutableFile.JAR;
                executable.mainClass = executableNode.getAttribute("class"); // executable
                // class
            }

            // what to do if execution fails
            val = executableNode.getAttribute("failure", "ask");
            if ("abort".equalsIgnoreCase(val))
            {
                executable.onFailure = ExecutableFile.ABORT;
            }
            else if ("warn".equalsIgnoreCase(val))
            {
                executable.onFailure = ExecutableFile.WARN;
            }
            else if ("ignore".equalsIgnoreCase(val))
            {
                executable.onFailure = ExecutableFile.IGNORE;
            }

            // whether to keep the executable after executing it
            val = executableNode.getAttribute("keep");
            executable.keepFile = "true".equalsIgnoreCase(val);

            // get arguments for this executable
            IXMLElement args = executableNode.getFirstChildNamed("args");
            if (null != args)
            {
                for (IXMLElement ixmlElement : args.getChildrenNamed("arg"))
                {
                    executable.argList.add(xmlCompilerHelper.requireAttribute(ixmlElement, "value"));
                }
            }

            executable.osList = OsConstraintHelper.getOsList(executableNode); // TODO:
            // unverified

            pack.addExecutable(executable);
        }
    }

    private void processParsableChildren(PackInfo pack, List<IXMLElement> parsableChildren) throws CompilerException
    {
        for (IXMLElement parsableNode : parsableChildren)
        {
            String target = xmlCompilerHelper.requireAttribute(parsableNode, "targetfile");
            SubstitutionType type = SubstitutionType.lookup(parsableNode.getAttribute("type", "plain"));
            String encoding = parsableNode.getAttribute("encoding", null);
            List<OsModel> osList = OsConstraintHelper.getOsList(parsableNode); // TODO: unverified
            String condition = parsableNode.getAttribute("condition");
            if (target != null)
            {
                ParsableFile parsable = new ParsableFile(target, type, encoding, osList);
                parsable.setCondition(condition);
                pack.addParsable(parsable);
            }
            for (IXMLElement fileSetElement : parsableNode.getChildrenNamed("fileset"))
            {
                String targetdir = xmlCompilerHelper.requireAttribute(fileSetElement, "targetdir");
                String dir_attr = xmlCompilerHelper.requireAttribute(fileSetElement, "dir");
                File dir = new File(dir_attr);
                if (!dir.isAbsolute())
                {
                    dir = new File(compilerData.getBasedir(), dir_attr);
                }
                if (!dir.isDirectory()) // also tests '.exists()'
                {
                    assertionHelper.parseError(fileSetElement, "Invalid directory 'dir': " + dir_attr);
                }
                String[] includedFiles = getFilesetIncludedFiles(fileSetElement);
                if (includedFiles != null)
                {
                    for (String filePath : includedFiles)
                    {
                        File file = new File(dir, filePath);
                        if (file.exists() && file.isFile())
                        {
                            String targetFile = new File(targetdir, filePath).getPath().replace(File.separatorChar, '/');
                            ParsableFile parsable = new ParsableFile(targetFile, type, encoding, osList);
                            parsable.setCondition(condition);
                            pack.addParsable(parsable);
                        }
                    }
                }
            }
        }
    }

    private String[] getFilesetIncludedFiles(IXMLElement fileSetElement) throws CompilerException
    {
        List<String> includedFiles = new ArrayList<String>();
        String dir_attr = xmlCompilerHelper.requireAttribute(fileSetElement, "dir");

        File dir = new File(dir_attr);
        if (!dir.isAbsolute())
        {
            dir = new File(compilerData.getBasedir(), dir_attr);
        }
        if (!dir.isDirectory()) // also tests '.exists()'
        {
            assertionHelper.parseError(fileSetElement, "Invalid directory 'dir': " + dir_attr);
        }

        boolean casesensitive = xmlCompilerHelper.validateYesNoAttribute(fileSetElement, "casesensitive", YES);
        boolean defexcludes = xmlCompilerHelper.validateYesNoAttribute(fileSetElement, "defaultexcludes", YES);

        // get includes and excludes
        List<IXMLElement> xcludesList = null;
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
                { // old container exist; create a new which can hold
                    // all values
                    // and copy the old stuff to the front
                    newSize += containers[j].length;
                    nCont = new String[newSize];
                    for (int k = 0; k < containers[j].length; ++k)
                    {
                        nCont[k] = containers[j][k];
                    }
                }
                if (nCont == null) // No container for old values
                // created,
                // create a new one.
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

        // scan and add fileset
        DirectoryScanner directoryScanner = new DirectoryScanner();
        directoryScanner.setIncludes(includes);
        directoryScanner.setExcludes(excludes);
        if (defexcludes)
        {
            directoryScanner.addDefaultExcludes();
        }
        directoryScanner.setBasedir(dir);
        directoryScanner.setCaseSensitive(casesensitive);
        try
        {
            directoryScanner.scan();

            String[] files = directoryScanner.getIncludedFiles();
            String[] dirs = directoryScanner.getIncludedDirectories();
            // Directory scanner has done recursion, add files and
            // directories
            for (String file : files)
            {
                includedFiles.add(file);
            }
            for (String dir1 : dirs)
            {
                includedFiles.add(dir1);
            }
        }
        catch (Exception e)
        {
            throw new CompilerException(e.getMessage());
        }

        return includedFiles.toArray(new String[]{});
    }

    private IXMLElement readRefPackData(String refFileName, boolean isselfcontained)
            throws CompilerException
    {
        File refXMLFile = new File(refFileName);
        if (!refXMLFile.isAbsolute())
        {
            refXMLFile = new File(compilerData.getBasedir(), refFileName);
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

        IXMLParser refXMLParser = new XMLParser();
        // We get it
        IXMLElement refXMLData = refXMLParser.parse(specin, refXMLFile.getAbsolutePath());

        // Now checked the loaded XML file for basic syntax
        // We check it
        if (!"installation".equalsIgnoreCase(refXMLData.getName()))
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
        addResources(refXMLData);

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
     * @param targetdir   the target directory where the content of the archive will be installed
     * @param osList      The target OS constraints.
     * @param override    Overriding behaviour.
     * @param pack        Pack to be packed into
     * @param additionals Map which contains additional data
     * @param condition
     */
    protected void addArchiveContent(File baseDir, File archive, String targetdir,
                                     List<OsModel> osList, OverrideType override, String overrideRenameTo,
                                     Blockable blockable, PackInfo pack, Map additionals,
                                     String condition) throws IOException
    {

        FileInputStream fin = new FileInputStream(archive);
        ZipInputStream zin = new ZipInputStream(fin);
        List<String> allDirList = new ArrayList<String>();
        while (true)
        {
            ZipEntry zentry = zin.getNextEntry();
            if (zentry == null)
            {
                break;
            }
            if (zentry.isDirectory())
            {
                // add to all dir listing/empty dir needs to be handle
                String dName = zentry.getName().substring(0, zentry.getName().length() - 1);
                allDirList.add(dName);
                continue;
            }

            try
            {
                File temp = FileUtils.createTempFile("izpack", null);
                temp.deleteOnExit();

                FileOutputStream out = new FileOutputStream(temp);
                IoHelper.copyStream(zin, out);
                out.close();

                pack.addFile(baseDir, temp, targetdir + "/" + zentry.getName(), osList, override,
                        overrideRenameTo, blockable, additionals, condition);
            }
            catch (IOException e)
            {
                throw new IOException("Couldn't create temporary file for " + zentry.getName()
                        + " in archive " + archive + " (" + e.getMessage() + ")");
            }

        }

        for (String dirName : allDirList)
        {
            File tmp = new File(dirName);
            tmp.mkdirs();
            tmp.deleteOnExit();
            pack.addFile(baseDir, tmp, targetdir + "/" + dirName, osList,
                    override, overrideRenameTo, blockable, additionals, condition);
        }
        fin.close();
    }

    /**
     * Recursive method to add files in a pack.
     *
     * @param file        The file to add.
     * @param targetdir   The relative path to the parent.
     * @param osList      The target OS constraints.
     * @param override    Overriding behaviour.
     * @param pack        Pack to be packed into
     * @param additionals Map which contains additional data
     * @param condition
     * @throws FileNotFoundException if the file does not exist
     */
    protected void addRecursively(File baseDir, File file, String targetdir,
                                  List<OsModel> osList, OverrideType override, String overrideRenameTo,
                                  Blockable blockable, PackInfo pack, Map additionals, String condition) throws IOException
    {
        String targetfile = targetdir + "/" + file.getName();
        if (!file.isDirectory())
        {
            pack.addFile(baseDir, file, targetfile, osList, override, overrideRenameTo, blockable, additionals, condition);
        }
        else
        {
            File[] files = file.listFiles();
            if (files.length == 0) // The directory is empty so must be added
            {
                pack.addFile(baseDir, file, targetfile, osList, override, overrideRenameTo, blockable, additionals, condition);
            }
            else
            {
                // new targetdir = targetfile;
                for (File file1 : files)
                {
                    addRecursively(baseDir, file1, targetfile, osList, override, overrideRenameTo, blockable,
                            pack, additionals, condition);
                }
            }
        }
    }

    /**
     * Parse panels and their paramters, locate the panels resources and add to the Packager.
     *
     * @param data The XML data.
     * @throws CompilerException Description of the Exception
     */
    protected void addPanels(IXMLElement data) throws IOException
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
        // We need a panel counter to build unique panel dependet resource names
        int panelCounter = 0;
        for (IXMLElement panelElement : panels)
        {
            panelCounter++;

            // create the serialized Panel data
            Panel panel = new Panel();
            panel.setOsConstraints(OsConstraintHelper.getOsList(panelElement));
            String className = panelElement.getAttribute("classname");

            // add an id
            String panelid = panelElement.getAttribute("id");
            panel.setPanelid(panelid);
            String condition = panelElement.getAttribute("condition");
            panel.setCondition(condition);

            // Panel files come in jars packaged w/ IzPack, or they can be
            // specified via a jar attribute on the panel element
            String jarPath = panelElement.getAttribute("jar");
            if (StringUtils.isNotBlank(jarPath))
            {
                URL jarUrl = resourceFinder.findIzPackResource(jarPath, "Panel jar file", panelElement, true);
                panel.className = compilerHelper.getFullClassName(jarUrl, className);
                packager.addJarContent(jarUrl);
            }
            else
            {
                //Assume it is merged with <jar> tag
                panel.className = className;
            }


            IXMLElement configurationElement = panelElement.getFirstChildNamed("configuration");
            if (configurationElement != null)
            {
                Debug.trace("found a configuration for this panel.");
                List<IXMLElement> params = configurationElement.getChildrenNamed("param");
                if (params != null)
                {
                    for (IXMLElement param : params)
                    {
                        IXMLElement keyElement = param.getFirstChildNamed("key");
                        IXMLElement valueElement = param.getFirstChildNamed("value");
                        if ((keyElement != null) && (valueElement != null))
                        {
                            panel.addConfiguration(keyElement.getContent(), valueElement.getContent());
                        }
                    }
                }
            }

            // adding validator
            IXMLElement validatorElement = panelElement
                    .getFirstChildNamed(DataValidator.DATA_VALIDATOR_TAG);
            if (validatorElement != null)
            {
                String validator = validatorElement
                        .getAttribute(DataValidator.DATA_VALIDATOR_CLASSNAME_TAG);
                if (!"".equals(validator))
                {
                    panel.setValidator(validator);
                }
            }
//            // adding helps
            List<IXMLElement> helps = panelElement.getChildrenNamed(AutomatedInstallData.HELP_TAG);
            if (helps != null) // TODO : remove this condition, getChildrenNamed always return a list
            {
                for (IXMLElement help : helps)
                {
                    String iso3 = help.getAttribute(AutomatedInstallData.ISO3_ATTRIBUTE);
                    String resourceId;
                    if (panelid == null)
                    {
                        resourceId = className + "_" + panelCounter + "_help_" + iso3 + ".html";
                    }
                    else
                    {
                        resourceId = panelid + "_" + panelCounter + "_help_" + iso3 + ".html";
                    }
//                    panel.addHelp(iso3, resourceId);
                    URL originalUrl = resourceFinder.findProjectResource(help
                            .getAttribute(AutomatedInstallData.SRC_ATTRIBUTE), "Help", help);
                    packager.addResource(resourceId, originalUrl);
                }
            }
            // adding actions
            addPanelActions(panelElement, panel);
            // insert into the packager

            packager.addPanel(panel);
        }
        notifyCompilerListener("addPanels", CompilerListener.END, data);
    }

    /**
     * Adds the resources.
     *
     * @param data The XML data.
     * @throws CompilerException Description of the Exception
     */
    protected void addResources(IXMLElement data) throws CompilerException
    {
        notifyCompilerListener("addResources", CompilerListener.BEGIN, data);
        IXMLElement root = data.getFirstChildNamed("resources");
        if (root == null)
        {
            return;
        }

        // We process each res markup
        for (IXMLElement resNode : root.getChildrenNamed("res"))
        {
            String id = xmlCompilerHelper.requireAttribute(resNode, "id");
            String src = xmlCompilerHelper.requireAttribute(resNode, "src");
            // the parse attribute causes substitution to occur
            boolean substitute = xmlCompilerHelper.validateYesNoAttribute(resNode, "parse", NO);
            // the parsexml attribute causes the xml document to be parsed
            boolean parsexml = xmlCompilerHelper.validateYesNoAttribute(resNode, "parsexml", NO);

            String encoding = resNode.getAttribute("encoding");
            if (encoding == null)
            {
                encoding = "";
            }

            // basedir is not prepended if src is already an absolute path
            URL originalUrl = resourceFinder.findProjectResource(src, "Resource", resNode);
            URL url = originalUrl;

            InputStream is = null;
            OutputStream os = null;
            try
            {
                if (parsexml || (!"".equals(encoding)) || (substitute && !packager.getVariables().isEmpty()))
                {
                    // make the substitutions into a temp file
                    File parsedFile = FileUtils.createTempFile("izpp", null);
                    parsedFile.deleteOnExit();
                    FileOutputStream outFile = new FileOutputStream(parsedFile);
                    os = new BufferedOutputStream(outFile);
                    // and specify the substituted file to be added to the
                    // packager
                    url = parsedFile.toURI().toURL();
                }

                if (!"".equals(encoding))
                {
                    File recodedFile = FileUtils.createTempFile("izenc", null);
                    recodedFile.deleteOnExit();

                    InputStreamReader reader = new InputStreamReader(originalUrl.openStream(), encoding);
                    OutputStreamWriter writer = new OutputStreamWriter(
                            new FileOutputStream(recodedFile), "UTF-8");

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
                    IXMLParser parser = new XMLParser();
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
//                        VariableSubstitutor vs = new VariableSubstitutorImpl(compiler.getVariables());
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

            packager.addResource(id, url);

            // remembering references to all added packsLang.xml files
            if (id.startsWith("packsLang.xml"))
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
            }

        }
        notifyCompilerListener("addResources", CompilerListener.END, data);
    }

    /**
     * Adds the ISO3 codes of the langpacks and associated resources.
     *
     * @param data The XML data.
     * @throws CompilerException Description of the Exception
     */
    protected void addLangpacks(IXMLElement data) throws CompilerException
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
     * Builds the Info class from the XML tree.
     *
     * @param data The XML data. return The Info.
     * @throws Exception Description of the Exception
     */
    protected void addInfo(IXMLElement data) throws Exception
    {
        notifyCompilerListener("addInfo", CompilerListener.BEGIN, data);
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
        }

        // Is a JDK required?
        IXMLElement jdkRequired = root.getFirstChildNamed("requiresjdk");
        if (jdkRequired != null)
        {
            info.setJdkRequired("yes".equals(jdkRequired.getContent()));
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

        // Pack200 support
        IXMLElement pack200 = root.getFirstChildNamed("pack200");
        info.setPack200Compression(pack200 != null);

        // Privileged execution
        IXMLElement privileged = root.getFirstChildNamed("run-privileged");
        info.setRequirePrivilegedExecution(privileged != null);
        if (privileged != null && privileged.hasAttribute("condition"))
        {
            info.setPrivilegedExecutionConditionID(privileged.getAttribute("condition"));
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

            if (reboot.hasAttribute("condition"))
            {
                info.setRebootActionConditionID(reboot.getAttribute("condition"));
            }
        }

        // Add the uninstaller as a resource if specified
        IXMLElement uninstallInfo = root.getFirstChildNamed("uninstaller");
        if (xmlCompilerHelper.validateYesNoAttribute(uninstallInfo, "write", YES))
        {
            //REFACTOR Change the way uninstaller is created
            mergeManager.addResourceToMerge("com/izforge/izpack/uninstaller/");
            mergeManager.addResourceToMerge("uninstaller-META-INF/");

            if (privileged != null)
            {
                // default behavior for uninstaller elevation: elevate if installer has to be elevated too
                info.setRequirePrivilegedExecutionUninstaller(xmlCompilerHelper.validateYesNoAttribute(privileged,
                        "uninstaller", YES));
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
                if (uninstallInfo.hasAttribute("condition"))
                {
                    // there's a condition for uninstaller
                    String uninstallerCondition = uninstallInfo.getAttribute("condition");
                    info.setUninstallerCondition(uninstallerCondition);
                }
            }
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

        // look for an unpacker class
        String unpackerclass = propertyManager.getProperty("UNPACKER_CLASS");
        info.setUnpackerClassName(unpackerclass);

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
                        throw new CompilerException("Only one temporary directory may be specified without a " + TEMP_DIR_VARIABLE_NAME_ATTRIBUTE
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
        notifyCompilerListener("addInfo", CompilerListener.END, data);
    }

    /**
     * Variable declaration is a fragment of the xml file. For example: <p/>
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
     * <p/> variable declared in this can be referred to in parsable files.
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
        }
        notifyCompilerListener("addVariables", CompilerListener.END, data);
    }

    private int getConfigFileType(String varname, String type)
            throws CompilerException
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
                assertionHelper.parseError("Error in definition of dynamic variable " + varname + ": Unknown entry type " + type);
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

        Map<String, List<DynamicVariable>> dynamicvariables = packager.getDynamicVariables();

        Iterator<IXMLElement> iter = root.getChildrenNamed("variable").iterator();
        while (iter.hasNext())
        {
            IXMLElement var = iter.next();
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
                    try
                    {
                        dynamicVariable.validate();
                    }
                    catch (Exception e)
                    {
                        assertionHelper.parseError("Error in definition of dynamic variable " + name + ": " + e.getMessage());
                    }
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
                String regroot = var.getAttribute("regroot");
                String regvalue = var.getAttribute("regvalue");
                if (dynamicVariable.getValue() == null)
                {
                    dynamicVariable.setValue(
                            new RegistryValue(regroot, value, regvalue));
                    try
                    {
                        dynamicVariable.validate();
                    }
                    catch (Exception e)
                    {
                        assertionHelper.parseError("Error in definition of dynamic variable " + name + ": " + e.getMessage());
                    }
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
                if (dynamicVariable.getValue() == null)
                {
                    dynamicVariable.setValue(new PlainConfigFileValue(value,
                            getConfigFileType(name, stype), filesection, filekey));
                    try
                    {
                        dynamicVariable.validate();
                    }
                    catch (Exception e)
                    {
                        assertionHelper.parseError("Error in definition of dynamic variable " + name + ": " + e.getMessage());
                    }
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
                if (dynamicVariable.getValue() == null)
                {
                    dynamicVariable.setValue(new ZipEntryConfigFileValue(value, entryname,
                            getConfigFileType(name, stype), filesection, filekey));
                    try
                    {
                        dynamicVariable.validate();
                    }
                    catch (Exception e)
                    {
                        assertionHelper.parseError("Error in definition of dynamic variable " + name + ": " + e.getMessage());
                    }
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
                if (dynamicVariable.getValue() == null)
                {
                    dynamicVariable.setValue(new JarEntryConfigValue(value, entryname,
                            getConfigFileType(name, stype), filesection, filekey));
                    try
                    {
                        dynamicVariable.validate();
                    }
                    catch (Exception e)
                    {
                        assertionHelper.parseError("Error in definition of dynamic variable " + name + ": " + e.getMessage());
                    }
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
                    String dir = var.getAttribute("dir");
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
                    if (exectype.equalsIgnoreCase("process") || exectype == null)
                    {
                        dynamicVariable.setValue(new ExecValue(cmd.toArray(cmdarr), dir, false, stderr));
                    }
                    else if (exectype.equalsIgnoreCase("shell"))
                    {
                        dynamicVariable.setValue(new ExecValue(cmd.toArray(cmdarr), dir, true, stderr));
                    }
                    else
                    {
                        assertionHelper.parseError("Bad execution type " + exectype + " given for dynamic variable " + name);
                    }
                    try
                    {
                        dynamicVariable.validate();
                    }
                    catch (Exception e)
                    {
                        assertionHelper.parseError("Error in definition of dynamic variable " + name + ": " + e.getMessage());
                    }
                }
                else
                {
                    // unexpected combination of variable attributes
                    assertionHelper.parseError("Ambiguous execution output value definition for dynamic variable " + name);
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

            // Check whether evaluation failures of the dynamic variable should be ignored
            value = var.getAttribute("ignorefailure");
            if (value != null)
            {
                dynamicVariable.setIgnoreFailure(Boolean.valueOf(value));
            }

            // Nested regular expression filter
            IXMLElement regexElement = var.getFirstChildNamed("regex");
            if (regexElement != null)
            {
                String expression = regexElement.getAttribute("regexp");
                String selectexpr = regexElement.getAttribute("select");
                String replaceexpr = regexElement.getAttribute("replace");
                String defaultvalue = regexElement.getAttribute("defaultvalue");
                String scasesensitive = regexElement.getAttribute("casesensitive");
                String sglobal = regexElement.getAttribute("global");
                if (dynamicVariable.getRegularExpression() == null)
                {
                    dynamicVariable.setRegularExpression(
                            new RegularExpressionFilterImpl(expression,
                                    selectexpr,
                                    replaceexpr,
                                    defaultvalue,
                                    Boolean.valueOf(scasesensitive != null ? scasesensitive : "true"),
                                    Boolean.valueOf(sglobal != null ? sglobal : "false")));
                    try
                    {
                        dynamicVariable.validate();
                    }
                    catch (Exception e)
                    {
                        assertionHelper.parseError("Error in definition of dynamic variable " + name + ": " + e.getMessage());
                    }
                }
                else
                {
                    assertionHelper.parseError("Ambiguous regular expression filter definition for dynamic variable " + name);
                }
            }

            List<DynamicVariable> dynamicValues = new ArrayList<DynamicVariable>();
            if (dynamicvariables.containsKey(name))
            {
                dynamicValues = dynamicvariables.get(name);
            }
            else
            {
                dynamicvariables.put(name, dynamicValues);
            }

            String conditionid = var.getAttribute("condition");
            dynamicVariable.setConditionid(conditionid);
            if (dynamicValues.remove(dynamicVariable))
            {
                assertionHelper.parseWarn(var, "Dynamic Variable '" + name + "' will be overwritten");
            }
            dynamicValues.add(dynamicVariable);
        }
        notifyCompilerListener("addDynamicVariables", CompilerListener.END, data);
    }

    protected void addDynamicInstallerRequirement(IXMLElement data) throws CompilerException
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
                if (severity == null || severity == Status.OK)
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
     * @param data
     * @throws CompilerException
     */
    protected void addConditions(IXMLElement data) throws CompilerException
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
                    Condition condition = rules.instanciateCondition(conditionNode);
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
                catch (Exception e) {
                    throw new CompilerException("Error reading condition at line "
                    + conditionNode.getLineNr() + ": "
                    + e.getMessage(), e);
                }
            }
            try
            {
                rules.checkConditions();
            }
            catch (Exception e)
            {
                throw new CompilerException("Conditions check failed: "
                        + e.getMessage(), e);
            }
        }
        notifyCompilerListener("addConditions", CompilerListener.END, data);
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
     * <p/> variable declared in this can be referred to in parsable files.
     *
     * @param data The XML data.
     * @throws CompilerException Description of the Exception
     */
    protected void substituteProperties(IXMLElement data) throws CompilerException
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
    protected void substituteAllProperties(IXMLElement element) throws CompilerException
    {
        Enumeration attributes = element.enumerateAttributeNames();
        while (attributes.hasMoreElements())
        {
            String name = (String) attributes.nextElement();
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

    protected OverrideType getOverrideValue(IXMLElement fileElement) throws CompilerException
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

    protected String getOverrideRenameToValue(IXMLElement f) throws CompilerException
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
     * @return blockable level
     * @throws CompilerException
     */
    protected Blockable getBlockableValue(IXMLElement blockableElement, List<OsModel> osList) throws CompilerException
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
                // We cannot add this constraint here explicitely, because it
                // the copied files might be multi-platform.
                // Print out a warning to inform the user about this fact.
                //osList.add(new OsModel("windows", null, null, null));
                assertionHelper.parseWarn(blockableElement, "'blockable' will implicitely apply only on Windows target systems");
            }
        }
        return blockable;
    }

    protected boolean validateYesNo(String value)
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
            Debug.trace("yes/no not found. trying true/false");
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
                    String jar = listener.getAttribute("jar");
                    URL url = (jar != null) ? resourceFinder.findProjectResource(jar, "Jar file", listener) : null;
                    List<OsModel> constraints = OsConstraintHelper.getOsList(listener);
                    compiler.addCustomListener(className, url, stage, constraints);
                }
            }
        }
        notifyCompilerListener("addListeners", CompilerListener.END, data);
    }

    /**
     * Register compiler listeners to be notified during compilation.
     */
    private void addCompilerListeners()
    {
        compilerListeners.addAll(instantiateCompilerListeners());
    }

    /**
     * Load and instanciate compiler listeners defined by the installer file
     *
     * @return instance of the defined compiler listener
     */
    private List<CompilerListener> instantiateCompilerListeners()
    {
        ArrayList<CompilerListener> result = new ArrayList<CompilerListener>();
        for (Listener listener : izpackProjectInstaller.getListeners())
        {
            if (Stage.compiler.equals(listener.getStage()))
            {
                listener.getOs();
                Class<? extends CompilerListener> clazz = classPathCrawler.searchClassInClassPath(listener.getClassname());
                if (clazz != null)
                {
                    compilerContainer.addComponent(clazz);
                    result.add(compilerContainer.getComponent(clazz));
                }
            }
        }
        return result;
    }

    /**
     * Calls all defined compile listeners notify method with the given data
     *
     * @param callerName name of the calling method as string
     * @param state      CompileListener.BEGIN or END
     * @param data       current install data
     * @throws CompilerException
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
    private Map getAdditionals(IXMLElement fileElement) throws CompilerException
    {
        Map retval = null;
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
     * @throws CompilerException
     */
    private void mergePacksLangFiles() throws CompilerException
    {
        // just one packslang file. nothing to do here
        if (packsLangUrlMap.size() <= 0)
        {
            return;
        }

        OutputStream os = null;
        try
        {
            IXMLParser parser = new XMLParser();

            // loop through all packsLang resources, e.g. packsLang.xml_eng, packsLang.xml_deu, ...
            for (String id : packsLangUrlMap.keySet())
            {
                URL mergedPackLangFileURL;

                List<URL> packsLangURLs = packsLangUrlMap.get(id);
                if (packsLangURLs.size() == 0)
                {
                    continue;
                } // should not occure

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
                        IXMLElement xml = parser.parse(packslangURL);
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
                    File mergedPackLangFile = FileUtils.createTempFile("izpp", null);
                    mergedPackLangFile.deleteOnExit();

                    FileOutputStream outFile = new FileOutputStream(mergedPackLangFile);
                    os = new BufferedOutputStream(outFile);

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
            throw new CompilerException("Unable to merge multiple packsLang.xml files: "
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
     * @param xmlPanel
     * @param panel
     * @throws CompilerException
     */
    private void addPanelActions(IXMLElement xmlPanel, Panel panel) throws CompilerException
    {
        IXMLElement xmlActions = xmlPanel.getFirstChildNamed(PanelAction.PANEL_ACTIONS_TAG);
        if (xmlActions != null)
        {
            List<IXMLElement> actionList = xmlActions
                    .getChildrenNamed(PanelAction.PANEL_ACTION_TAG);
            if (actionList != null)
            {
                for (IXMLElement action : actionList)
                {
                    String stage = action.getAttribute(PanelAction.PANEL_ACTION_STAGE_TAG);
                    String actionName = action.getAttribute(PanelAction.PANEL_ACTION_CLASSNAME_TAG);
                    if (actionName != null)
                    {
                        List<IXMLElement> params = action.getChildrenNamed("param");
                        PanelActionConfiguration config = new PanelActionConfiguration();

                        for (IXMLElement param : params)
                        {
                            IXMLElement keyElement = param.getFirstChildNamed("key");
                            IXMLElement valueElement = param.getFirstChildNamed("value");
                            if ((keyElement != null) && (valueElement != null))
                            {
                                Debug.trace("Adding configuration property " + keyElement.getContent() + " with value " + valueElement.getContent() + " for action " + actionName);
                                config.addProperty(keyElement.getContent(), valueElement.getContent());
                            }
                        }
                        panel.putPanelActionConfiguration(actionName, config);
                    }
                    try
                    {
                        PanelAction.ActionStage actionStage = PanelAction.ActionStage.valueOf(stage);
                        switch (actionStage)
                        {
                            case preconstruct:
                                panel.addPreConstructionActions(actionName);
                                break;
                            case preactivate:
                                panel.addPreActivationAction(actionName);
                                break;
                            case prevalidate:
                                panel.addPreValidationAction(actionName);
                                break;
                            case postvalidate:
                                panel.addPostValidationAction(actionName);
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

    private List<TargetFileSet> readFileSets(IXMLElement parent) throws CompilerException
    {
        List<TargetFileSet> fslist = new ArrayList<TargetFileSet>();
        for (IXMLElement fileSetNode : parent.getChildrenNamed("fileset"))
        {
            try
            {
                fslist.add(readFileSet(fileSetNode));
            }
            catch (Exception e)
            {
                throw new CompilerException(e.getMessage());
            }
        }
        return fslist;
    }

    private TargetFileSet readFileSet(IXMLElement fileSetNode) throws CompilerException
    {
        TargetFileSet fs = new TargetFileSet();

        fs.setTargetDir(xmlCompilerHelper.requireAttribute(fileSetNode, "targetdir"));
        List<OsModel> osList = OsConstraintHelper.getOsList(fileSetNode); // TODO: unverified
        fs.setOsList(osList);
        fs.setOverride(getOverrideValue(fileSetNode));
        fs.setOverrideRenameTo(getOverrideRenameToValue(fileSetNode));
        fs.setBlockable(getBlockableValue(fileSetNode, osList));
        fs.setAdditionals(getAdditionals(fileSetNode));
        fs.setCondition(fileSetNode.getAttribute("condition"));

        String dir_attr = xmlCompilerHelper.requireAttribute(fileSetNode, "dir");
        try
        {
            if (dir_attr != null)
            {
                fs.setDir(FileUtil.getAbsoluteFile(dir_attr, compilerData.getBasedir()));
            }

            dir_attr = fileSetNode.getAttribute("file");
            if (dir_attr != null)
            {
                fs.setFile(FileUtil.getAbsoluteFile(dir_attr, compilerData.getBasedir()));
            }
            else
            {
                if (fs.getDir() == null)
                {
                    throw new CompilerException("At least one of both attributes, 'dir' or 'file' required in fileset");
                }
            }
        }
        catch (Exception e)
        {
            throw new CompilerException(e.getMessage());
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

    private void readAndAddIncludes(IXMLElement parent, TargetFileSet fileset)
            throws CompilerException
    {
        Iterator<IXMLElement> iter = parent.getChildrenNamed("include").iterator();
        while (iter.hasNext())
        {
            IXMLElement f = iter.next();
            fileset.createInclude().setName(
                    variableSubstitutor.substitute(
                            xmlCompilerHelper.requireAttribute(f, "name")));
        }
    }

    private void readAndAddExcludes(IXMLElement parent, TargetFileSet fileset)
            throws CompilerException
    {
        Iterator<IXMLElement> iter = parent.getChildrenNamed("exclude").iterator();
        while (iter.hasNext())
        {
            IXMLElement f = iter.next();
            fileset.createExclude().setName(
                    variableSubstitutor.substitute(
                            xmlCompilerHelper.requireAttribute(f, "name")));
        }
    }

}
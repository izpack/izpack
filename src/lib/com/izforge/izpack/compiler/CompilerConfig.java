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

import com.izforge.izpack.*;
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.IXMLParser;
import com.izforge.izpack.adaptator.IXMLWriter;
import com.izforge.izpack.adaptator.impl.XMLParser;
import com.izforge.izpack.adaptator.impl.XMLWriter;
import com.izforge.izpack.compiler.Compiler.CmdlinePackagerListener;
import com.izforge.izpack.event.CompilerListener;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.installer.InstallerRequirement;
import com.izforge.izpack.installer.PanelAction;
import com.izforge.izpack.installer.PanelAction.ActionStage;
import com.izforge.izpack.installer.PanelActionConfiguration;
import com.izforge.izpack.panels.HelpWindow;
import com.izforge.izpack.rules.Condition;
import com.izforge.izpack.rules.RulesEngine;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.OsConstraint;
import com.izforge.izpack.util.VariableSubstitutor;
import org.apache.tools.ant.DirectoryScanner;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarInputStream;
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
public class CompilerConfig extends Thread {

    /**
     * The compiler version.
     */
    public final static String VERSION = "1.0";

    /**
     * Standard installer.
     */
    public final static String STANDARD = "standard";

    /**
     * Web installer.
     */
    public final static String WEB = "web";

    /**
     * Constant for checking attributes.
     */
    private static boolean YES = true;

    /**
     * Constant for checking attributes.
     */
    private static boolean NO = false;

    private final static String IZ_TEST_FILE = "ShellLink.dll";

    private final static String IZ_TEST_SUBDIR = "bin" + File.separator + "native" + File.separator
            + "izpack";

    /**
     * The xml install file
     */
    private String filename;

    /**
     * The xml install configuration text
     */
    private String installText;

    /**
     * The base directory.
     */
    protected String basedir;

    /**
     * The installer packager compiler
     */
    private Compiler compiler;

    /**
     * List of CompilerListeners which should be called at packaging
     */
    protected List<CompilerListener> compilerListeners = new ArrayList<CompilerListener>();

    /**
     * A list of packsLang-files that were defined by the user in the resource-section The key of
     * this map is an packsLang-file identifier, e.g. <code>packsLang.xml_eng</code>, the values
     * are lists of {@link URL} pointing to the concrete packsLang-files.
     *
     * @see #mergePacksLangFiles()
     */
    private HashMap<String, List<URL>> packsLangUrlMap = new HashMap<String, List<URL>>();

    /**
     * Set the IzPack home directory
     *
     * @param izHome - the izpack home directory
     */
    public static void setIzpackHome(String izHome) {
        Compiler.setIzpackHome(izHome);
    }

    /**
     * The constructor.
     *
     * @param filename The XML filename.
     * @param basedir  The base directory.
     * @param kind     The installer kind.
     * @param output   The installer filename.
     *
     * @throws CompilerException
     */
    public CompilerConfig(String filename, String basedir, String kind, String output)
            throws CompilerException {
        this(filename, basedir, kind, output, null);
    }

    /**
     * The constructor.
     *
     * @param filename The XML filename.
     * @param basedir  The base directory.
     * @param kind     The installer kind.
     * @param output   The installer filename.
     * @param listener The PackagerListener.
     *
     * @throws CompilerException
     */
    public CompilerConfig(String filename, String basedir, String kind, String output,
                          PackagerListener listener) throws CompilerException {
        this(filename, basedir, kind, output, "default", listener);
    }

    /**
     * @param filename     The XML filename.
     * @param kind         The installer kind.
     * @param output       The installer filename.
     * @param compr_format The compression format to be used for packs.
     * @param listener     The PackagerListener.
     *
     * @throws CompilerException
     */
    public CompilerConfig(String filename, String base, String kind, String output,
                          String compr_format, PackagerListener listener) throws CompilerException {
        this(filename, base, kind, output, compr_format, listener, null);
    }

    /**
     * @param basedir     The base directory.
     * @param kind        The installer kind.
     * @param output      The installer filename.
     * @param listener    The PackagerListener.
     * @param installText The install xml configuration text
     *
     * @throws CompilerException
     */
    public CompilerConfig(String basedir, String kind, String output, PackagerListener listener,
                          String installText) throws CompilerException {
        this(null, basedir, kind, output, "default", listener, installText);
    }

    /**
     * @param filename     The XML filename.
     * @param basedir      The base directory.
     * @param kind         The installer kind.
     * @param output       The installer filename.
     * @param compr_format The compression format to be used for packs.
     * @param listener     The PackagerListener.
     * @param installText  The install xml configuration text
     *
     * @throws CompilerException
     */
    public CompilerConfig(String filename, String basedir, String kind, String output,
                          String compr_format, PackagerListener listener, String installText)
            throws CompilerException {
        this(filename, basedir, kind, output, compr_format, -1, listener, installText);
    }

    /**
     * @param filename     The XML filename.
     * @param basedir      The base directory.
     * @param kind         The installer kind.
     * @param output       The installer filename.
     * @param compr_format The compression format to be used for packs.
     * @param compr_level  Compression level to be used if supported.
     * @param listener     The PackagerListener.
     * @param installText  The install xml configuration text
     *
     * @throws CompilerException
     */
    public CompilerConfig(String filename, String basedir, String kind, String output,
                          String compr_format, int compr_level, PackagerListener listener, String installText)
            throws CompilerException {
        this.filename = filename;
        this.installText = installText;
        this.basedir = basedir;
        this.compiler = new Compiler(basedir, kind, output, compr_format, compr_level);
        compiler.setPackagerListener(listener);
    }

    /**
     * Add a name value pair to the project property set. It is <i>not</i> replaced it is already
     * in the set of properties.
     *
     * @param name  the name of the property
     * @param value the value to set
     *
     * @return true if the property was not already set
     */
    public boolean addProperty(String name, String value) {
        return compiler.addProperty(name, value);
    }

    /**
     * Access the install compiler
     *
     * @return the install compiler
     */
    public Compiler getCompiler() {
        return compiler;
    }

    /**
     * Retrieves the packager listener
     */
    public PackagerListener getPackagerListener() {
        return compiler.getPackagerListener();
    }

    /**
     * Compile the installation
     */
    public void compile() {
        start();
    }

    /**
     * The run() method.
     */
    public void run() {
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
    public void executeCompiler() throws Exception {
        // normalize and test: TODO: may allow failure if we require write
        // access
        File base = new File(basedir).getAbsoluteFile();
        if (!base.canRead() || !base.isDirectory())
        {
            throw new CompilerException(
                    "Invalid base directory: " + base);
        }

        // add izpack built in property
        compiler.setProperty("basedir", base.toString());

        // We get the XML data tree
        IXMLElement data = getXMLTree();
        // loads the specified packager
        loadPackagingInformation(data);

        // Listeners to various events
        addCustomListeners(data);

        // Read the properties and perform replacement on the rest of the tree
        substituteProperties(data);

        // We add all the information
        addVariables(data);
        addDynamicVariables(data);
        addConditions(data);
        addInfo(data);
        addGUIPrefs(data);
        addLangpacks(data);
        addResources(data);
        addNativeLibraries(data);
        addJars(data);
        addPanels(data);
        addPacks(data);
        addInstallerRequirement(data);

        // merge multiple packlang.xml files
        mergePacksLangFiles();

        // We ask the packager to create the installer
        compiler.createInstaller();
    }

    private void addInstallerRequirement(IXMLElement data) throws CompilerException {
        notifyCompilerListener("addInstallerRequirement", CompilerListener.BEGIN, data);
        IXMLElement root = data.getFirstChildNamed("installerrequirements");
        List<InstallerRequirement> installerrequirements = new ArrayList<InstallerRequirement>();

        if (root != null)
        {
            Vector<IXMLElement> installerrequirementsels = root
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
        compiler.addInstallerRequirement(installerrequirements);
        notifyCompilerListener("addInstallerRequirement", CompilerListener.END, data);
    }

    private void loadPackagingInformation(IXMLElement data) throws CompilerException {
        notifyCompilerListener("loadPackager", CompilerListener.BEGIN, data);
        // Initialisation
        IXMLElement root = data.getFirstChildNamed("packaging");
        String packagerclassname = "com.izforge.izpack.compiler.Packager";
        String unpackerclassname = "com.izforge.izpack.installer.Unpacker";
        IXMLElement packager = null;
        if (root != null)
        {
            packager = root.getFirstChildNamed("packager");

            if (packager != null)
            {
                packagerclassname = requireAttribute(packager, "class");
            }

            IXMLElement unpacker = root.getFirstChildNamed("unpacker");

            if (unpacker != null)
            {
                unpackerclassname = requireAttribute(unpacker, "class");
            }
        }
        compiler.initPackager(packagerclassname);
        if (packager != null)
        {
            IXMLElement options = packager.getFirstChildNamed("options");
            if (options != null)
            {
                compiler.getPackager().addConfigurationInformation(options);
            }
        }
        compiler.addProperty("UNPACKER_CLASS", unpackerclassname);
        notifyCompilerListener("loadPackager", CompilerListener.END, data);
    }

    public boolean wasSuccessful() {
        return compiler.wasSuccessful();
    }

    /**
     * Returns the GUIPrefs.
     *
     * @param data The XML data.
     *
     * @throws CompilerException Description of the Exception
     */
    protected void addGUIPrefs(IXMLElement data) throws CompilerException {
        notifyCompilerListener("addGUIPrefs", CompilerListener.BEGIN, data);
        // We get the IXMLElement & the attributes
        IXMLElement gp = data.getFirstChildNamed("guiprefs");
        GUIPrefs prefs = new GUIPrefs();
        if (gp != null)
        {
            prefs.resizable = requireYesNoAttribute(gp, "resizable");
            prefs.width = requireIntAttribute(gp, "width");
            prefs.height = requireIntAttribute(gp, "height");

            // Look and feel mappings
            Iterator<IXMLElement> it = gp.getChildrenNamed("laf").iterator();
            while (it.hasNext())
            {
                IXMLElement laf = it.next();
                String lafName = requireAttribute(laf, "name");
                requireChildNamed(laf, "os");

                Iterator<IXMLElement> oit = laf.getChildrenNamed("os").iterator();
                while (oit.hasNext())
                {
                    IXMLElement os = oit.next();
                    String osName = requireAttribute(os, "family");
                    prefs.lookAndFeelMapping.put(osName, lafName);
                }

                Iterator<IXMLElement> pit = laf.getChildrenNamed("param").iterator();
                Map<String, String> params = new TreeMap<String, String>();
                while (pit.hasNext())
                {
                    IXMLElement param = pit.next();
                    String name = requireAttribute(param, "name");
                    String value = requireAttribute(param, "value");
                    params.put(name, value);
                }
                prefs.lookAndFeelParams.put(lafName, params);
            }
            // Load modifier
            it = gp.getChildrenNamed("modifier").iterator();
            while (it.hasNext())
            {
                IXMLElement curentModifier = it.next();
                String key = requireAttribute(curentModifier, "key");
                String value = requireAttribute(curentModifier, "value");
                prefs.modifier.put(key, value);

            }
            // make sure jar contents of each are available in installer
            // map is easier to read/modify than if tree
            HashMap<String, String> lafMap = new HashMap<String, String>();
            lafMap.put("liquid", "liquidlnf.jar");
            lafMap.put("kunststoff", "kunststoff.jar");
            lafMap.put("metouia", "metouia.jar");
            lafMap.put("looks", "looks.jar");
            lafMap.put("substance", "substance.jar");
            lafMap.put("nimbus", "nimbus.jar");

            // is this really what we want? a double loop? needed, since above,
            // it's
            // the /last/ lnf for an os which is used, so can't add during
            // initial
            // loop
            Iterator<String> kit = prefs.lookAndFeelMapping.keySet().iterator();
            while (kit.hasNext())
            {
                String lafName = prefs.lookAndFeelMapping.get(kit.next());
                String lafJarName = lafMap.get(lafName);
                if (lafJarName == null)
                {
                    parseError(gp, "Unrecognized Look and Feel: " + lafName);
                }

                URL lafJarURL = findIzPackResource("lib/" + lafJarName, "Look and Feel Jar file",
                        gp);
                compiler.addJarContent(lafJarURL);
            }
        }
        compiler.setGUIPrefs(prefs);
        notifyCompilerListener("addGUIPrefs", CompilerListener.END, data);
    }

    /**
     * Add project specific external jar files to the installer.
     *
     * @param data The XML data.
     */
    protected void addJars(IXMLElement data) throws Exception {
        notifyCompilerListener("addJars", CompilerListener.BEGIN, data);
        Iterator<IXMLElement> iter = data.getChildrenNamed("jar").iterator();
        while (iter.hasNext())
        {
            IXMLElement el = iter.next();
            String src = requireAttribute(el, "src");
            URL url = findProjectResource(src, "Jar file", el);
            compiler.addJarContent(url);
            // Additionals for mark a jar file also used in the uninstaller.
            // The contained files will be copied from the installer into the
            // uninstaller if needed.
            // Therefore the contained files of the jar should be in the
            // installer also
            // they are used only from the uninstaller. This is the reason why
            // the stage
            // wiil be only observed for the uninstaller.
            String stage = el.getAttribute("stage");
            if (stage != null
                    && ("both".equalsIgnoreCase(stage) || "uninstall".equalsIgnoreCase(stage)))
            {
                CustomData ca = new CustomData(null, getContainedFilePaths(url), null,
                        CustomData.UNINSTALLER_JAR);
                compiler.addCustomJar(ca, url);
            }
        }
        notifyCompilerListener("addJars", CompilerListener.END, data);
    }

    /**
     * Add native libraries to the installer.
     *
     * @param data The XML data.
     */
    protected void addNativeLibraries(IXMLElement data) throws Exception {
        boolean needAddOns = false;
        notifyCompilerListener("addNativeLibraries", CompilerListener.BEGIN, data);
        Iterator<IXMLElement> iter = data.getChildrenNamed("native").iterator();
        while (iter.hasNext())
        {
            IXMLElement el = iter.next();
            String type = requireAttribute(el, "type");
            String name = requireAttribute(el, "name");
            String path = el.getAttribute("src");
            if (path == null)
            {
                path = "bin/native/" + type + "/" + name;
            }
            URL url = findIzPackResource(path, "Native Library", el);
            compiler.addNativeLibrary(name, url);
            // Additionals for mark a native lib also used in the uninstaller
            // The lib will be copied from the installer into the uninstaller if
            // needed.
            // Therefore the lib should be in the installer also it is used only
            // from
            // the uninstaller. This is the reason why the stage wiil be only
            // observed
            // for the uninstaller.
            String stage = el.getAttribute("stage");
            List<OsConstraint> constraints = OsConstraint.getOsList(el);
            if (stage != null
                    && ("both".equalsIgnoreCase(stage) || "uninstall".equalsIgnoreCase(stage)))
            {
                ArrayList<String> al = new ArrayList<String>();
                al.add(name);
                CustomData cad = new CustomData(null, al, constraints, CustomData.UNINSTALLER_LIB);
                compiler.addNativeUninstallerLibrary(cad);
                needAddOns = true;
            }

        }
        if (needAddOns)
        {
            // Add the uninstaller extensions as a resource if specified
            IXMLElement root = requireChildNamed(data, "info");
            IXMLElement uninstallInfo = root.getFirstChildNamed("uninstaller");
            if (validateYesNoAttribute(uninstallInfo, "write", YES))
            {
                URL url = findIzPackResource("lib/uninstaller-ext.jar", "Uninstaller extensions",
                        root);
                compiler.addResource("IzPack.uninstaller-ext", url);
            }

        }
        notifyCompilerListener("addNativeLibraries", CompilerListener.END, data);
    }

    /**
     * Add packs and their contents to the installer.
     *
     * @param data The XML data.
     */
    protected void addPacks(IXMLElement data) throws CompilerException {
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
     *
     * @throws CompilerException
     */
    private void addPacksSingle(IXMLElement data) throws CompilerException {
        notifyCompilerListener("addPacksSingle", CompilerListener.BEGIN, data);
        // Initialisation
        IXMLElement root = requireChildNamed(data, "packs");

        // at least one pack is required
        Vector<IXMLElement> packElements = root.getChildrenNamed("pack");
        Vector<IXMLElement> refPackElements = root.getChildrenNamed("refpack");
        Vector<IXMLElement> refPackSets = root.getChildrenNamed("refpackset");
        if (packElements.isEmpty() && refPackElements.isEmpty() && refPackSets.isEmpty())
        {
            parseError(root, "<packs> requires a <pack>, <refpack> or <refpackset>");
        }

        File baseDir = new File(basedir);

        Iterator<IXMLElement> packIter = packElements.iterator();
        while (packIter.hasNext())
        {
            IXMLElement el = packIter.next();

            // Trivial initialisations
            String name = requireAttribute(el, "name");
            String id = el.getAttribute("id");
            String packImgId = el.getAttribute("packImgId");

            boolean loose = "true".equalsIgnoreCase(el.getAttribute("loose", "false"));
            String description = requireChildNamed(el, "description").getContent();
            boolean required = requireYesNoAttribute(el, "required");
            String group = el.getAttribute("group");
            String installGroups = el.getAttribute("installGroups");
            String excludeGroup = el.getAttribute("excludeGroup");
            boolean uninstall = "yes".equalsIgnoreCase(el.getAttribute("uninstall", "yes"));
            String parent = el.getAttribute("parent");
            boolean hidden = "true".equalsIgnoreCase(el.getAttribute("hidden","false"));
            
            String conditionid = el.getAttribute("condition");

            if (required && excludeGroup != null)
            {
                parseError(el, "Pack, which has excludeGroup can not be required.", new Exception(
                        "Pack, which has excludeGroup can not be required."));
            }

            PackInfo pack = new PackInfo(name, id, description, required, loose, excludeGroup,
                    uninstall);
            pack.setOsConstraints(OsConstraint.getOsList(el)); // TODO:
            pack.setParent(parent);
            pack.setCondition(conditionid);
            pack.setHidden(hidden);
            VariableSubstitutor varsubst = new VariableSubstitutor(compiler.getVariables());

            // unverified
            // if the pack belongs to an excludeGroup it's not preselected by default
            if (excludeGroup == null)
            {
                pack.setPreselected(validateYesNoAttribute(el, "preselected", YES));
            }
            else
            {
                pack.setPreselected(validateYesNoAttribute(el, "preselected", NO));
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

            // We get the parsables list
            Iterator<IXMLElement> iter = el.getChildrenNamed("parsable").iterator();
            while (iter.hasNext())
            {
                IXMLElement p = iter.next();
                String target = requireAttribute(p, "targetfile");
                String type = p.getAttribute("type", "plain");
                String encoding = p.getAttribute("encoding", null);
                List<OsConstraint> osList = OsConstraint.getOsList(p); // TODO: unverified
                String condition = p.getAttribute("condition");
                ParsableFile parsable = new ParsableFile(target, type, encoding, osList);
                parsable.setCondition(condition);
                pack.addParsable(parsable);
            }

            // We get the executables list
            iter = el.getChildrenNamed("executable").iterator();
            while (iter.hasNext())
            {
                IXMLElement e = iter.next();
                ExecutableFile executable = new ExecutableFile();
                String val; // temp value
                String condition = e.getAttribute("condition");
                executable.setCondition(condition);
                executable.path = requireAttribute(e, "targetfile");

                // when to execute this executable
                val = e.getAttribute("stage", "never");
                if ("postinstall".equalsIgnoreCase(val))
                {
                    executable.executionStage = ExecutableFile.POSTINSTALL;
                }
                else if ("uninstall".equalsIgnoreCase(val))
                {
                    executable.executionStage = ExecutableFile.UNINSTALL;
                }

                // type of this executable
                val = e.getAttribute("type", "bin");
                if ("jar".equalsIgnoreCase(val))
                {
                    executable.type = ExecutableFile.JAR;
                    executable.mainClass = e.getAttribute("class"); // executable
                    // class
                }

                // what to do if execution fails
                val = e.getAttribute("failure", "ask");
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
                val = e.getAttribute("keep");
                executable.keepFile = "true".equalsIgnoreCase(val);

                // get arguments for this executable
                IXMLElement args = e.getFirstChildNamed("args");
                if (null != args)
                {
                    Iterator<IXMLElement> argIterator = args.getChildrenNamed("arg").iterator();
                    while (argIterator.hasNext())
                    {
                        IXMLElement arg = argIterator.next();
                        executable.argList.add(requireAttribute(arg, "value"));
                    }
                }

                executable.osList = OsConstraint.getOsList(e); // TODO:
                // unverified

                pack.addExecutable(executable);
            }

            // We get the files list
            iter = el.getChildrenNamed("file").iterator();
            while (iter.hasNext())
            {
                IXMLElement f = iter.next();
                String src = requireAttribute(f, "src");
                String targetdir = requireAttribute(f, "targetdir");
                List<OsConstraint> osList = OsConstraint.getOsList(f); // TODO: unverified
                int override = getOverrideValue(f);
                Map additionals = getAdditionals(f);
                boolean unpack = "true".equalsIgnoreCase(f.getAttribute("unpack"));
                String condition = f.getAttribute("condition");

                File file = new File(src);
                if (!file.isAbsolute())
                {
                    file = new File(basedir, src);
                }
                
                // if the path does not exist, maybe it contains variables 
                if (!file.exists()) 
                {
                    file = new File(varsubst.substitute(file.getAbsolutePath(), null));
                    // next existance checking appears in pack.addFile
                }

                try
                {
                    if (unpack)
                    {
                        addArchiveContent(baseDir, file, targetdir, osList, override, pack,
                                additionals, condition);
                    }
                    else
                    {
                        addRecursively(baseDir, file, targetdir, osList, override, pack,
                                additionals, condition);
                    }
                }
                catch (Exception x)
                {
                    parseError(f, x.getMessage(), x);
                }
            }

            // We get the singlefiles list
            iter = el.getChildrenNamed("singlefile").iterator();
            while (iter.hasNext())
            {
                IXMLElement f = iter.next();
                String src = requireAttribute(f, "src");
                String target = requireAttribute(f, "target");
                List<OsConstraint> osList = OsConstraint.getOsList(f); // TODO: unverified
                int override = getOverrideValue(f);
                Map additionals = getAdditionals(f);
                String condition = f.getAttribute("condition");
                File file = new File(src);
                if (!file.isAbsolute())
                {
                    file = new File(basedir, src);
                }

                // if the path does not exist, maybe it contains variables 
                if (!file.exists()) 
                {
                    file = new File(varsubst.substitute(file.getAbsolutePath(), null));
                    // next existance checking appears in pack.addFile
                }

                try
                {
                    pack.addFile(baseDir, file, target, osList, override, additionals, condition);
                }
                catch (FileNotFoundException x)
                {
                    parseError(f, x.getMessage(), x);
                }
            }

            // We get the fileset list
            iter = el.getChildrenNamed("fileset").iterator();
            while (iter.hasNext())
            {
                IXMLElement f = iter.next();
                String dir_attr = requireAttribute(f, "dir");

                File dir = new File(dir_attr);
                if (!dir.isAbsolute())
                {
                    dir = new File(basedir, dir_attr);
                }
                if (!dir.isDirectory()) // also tests '.exists()'
                {
                    parseError(f, "Invalid directory 'dir': " + dir_attr);
                }

                boolean casesensitive = validateYesNoAttribute(f, "casesensitive", YES);
                boolean defexcludes = validateYesNoAttribute(f, "defaultexcludes", YES);
                String targetdir = requireAttribute(f, "targetdir");
                List<OsConstraint> osList = OsConstraint.getOsList(f); // TODO: unverified
                int override = getOverrideValue(f);
                Map additionals = getAdditionals(f);
                String condition = f.getAttribute("condition");

                // get includes and excludes
                Vector<IXMLElement> xcludesList = null;
                String[] includes = null;
                xcludesList = f.getChildrenNamed("include");
                if (!xcludesList.isEmpty())
                {
                    includes = new String[xcludesList.size()];
                    for (int j = 0; j < xcludesList.size(); j++)
                    {
                        IXMLElement xclude = xcludesList.get(j);
                        includes[j] = requireAttribute(xclude, "name");
                    }
                }
                String[] excludes = null;
                xcludesList = f.getChildrenNamed("exclude");
                if (!xcludesList.isEmpty())
                {
                    excludes = new String[xcludesList.size()];
                    for (int j = 0; j < xcludesList.size(); j++)
                    {
                        IXMLElement xclude = xcludesList.get(j);
                        excludes[j] = requireAttribute(xclude, "name");
                    }
                }

                // parse additional fileset attributes "includes" and "excludes"
                String[] toDo = new String[]{"includes", "excludes"};
                // use the existing containers filled from include and exclude
                // and add the includes and excludes to it
                String[][] containers = new String[][]{includes, excludes};
                for (int j = 0; j < toDo.length; ++j)
                {
                    String inex = f.getAttribute(toDo[j]);
                    if (inex != null && inex.length() > 0)
                    { // This is the same "splitting" as ant PatternSet do ...
                        StringTokenizer tok = new StringTokenizer(inex, ", ", false);
                        int newSize = tok.countTokens();
                        int k = 0;
                        String[] nCont = null;
                        if (containers[j] != null && containers[j].length > 0)
                        { // old container exist; create a new which can hold
                            // all values
                            // and copy the old stuff to the front
                            newSize += containers[j].length;
                            nCont = new String[newSize];
                            for (; k < containers[j].length; ++k)
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
                        for (; k < newSize; ++k)
                        // Fill the new one or expand the existent container
                        {
                            nCont[k] = tok.nextToken();
                        }
                        containers[j] = nCont;
                    }
                }
                includes = containers[0]; // push the new includes to the
                // local var
                excludes = containers[1]; // push the new excludes to the
                // local var

                // scan and add fileset
                DirectoryScanner ds = new DirectoryScanner();
                ds.setIncludes(includes);
                ds.setExcludes(excludes);
                if (defexcludes)
                {
                    ds.addDefaultExcludes();
                }
                ds.setBasedir(dir);
                ds.setCaseSensitive(casesensitive);
                ds.scan();

                String[] files = ds.getIncludedFiles();
                String[] dirs = ds.getIncludedDirectories();

                // Directory scanner has done recursion, add files and
                // directories
                for (String file : files)
                {
                    try
                    {
                        String target = new File(targetdir, file).getPath();
                        pack.addFile(baseDir, new File(dir, file), target, osList, override,
                                additionals, condition);
                    }
                    catch (FileNotFoundException x)
                    {
                        parseError(f, x.getMessage(), x);
                    }
                }
                for (String dir1 : dirs)
                {
                    try
                    {
                        String target = new File(targetdir, dir1).getPath();
                        pack.addFile(baseDir, new File(dir, dir1), target, osList, override,
                                additionals, condition);
                    }
                    catch (FileNotFoundException x)
                    {
                        parseError(f, x.getMessage(), x);
                    }
                }
            }

            // get the updatechecks list
            iter = el.getChildrenNamed("updatecheck").iterator();
            while (iter.hasNext())
            {
                IXMLElement f = iter.next();

                String casesensitive = f.getAttribute("casesensitive");

                // get includes and excludes
                ArrayList<String> includesList = new ArrayList<String>();
                ArrayList<String> excludesList = new ArrayList<String>();

                // get includes and excludes
                Iterator<IXMLElement> include_it = f.getChildrenNamed("include").iterator();
                while (include_it.hasNext())
                {
                    IXMLElement inc_el = include_it.next();
                    includesList.add(requireAttribute(inc_el, "name"));
                }

                Iterator<IXMLElement> exclude_it = f.getChildrenNamed("exclude").iterator();
                while (exclude_it.hasNext())
                {
                    IXMLElement excl_el = exclude_it.next();
                    excludesList.add(requireAttribute(excl_el, "name"));
                }

                pack.addUpdateCheck(new UpdateCheck(includesList, excludesList, casesensitive));
            }
            // We get the dependencies
            iter = el.getChildrenNamed("depends").iterator();
            while (iter.hasNext())
            {
                IXMLElement dep = iter.next();
                String depName = requireAttribute(dep, "packname");
                pack.addDependency(depName);

            }

            iter = el.getChildrenNamed("validator").iterator();
            while (iter.hasNext())
            {
                IXMLElement validator = iter.next();
                pack.addValidator(requireContent(validator));
            }

            // We add the pack
            compiler.addPack(pack);
        }

        Iterator<IXMLElement> refPackIter = refPackElements.iterator();
        while (refPackIter.hasNext())
        {
            IXMLElement el = refPackIter.next();

            // get the name of reference xml file
            String refFileName = requireAttribute(el, "file");
            String selfcontained = el.getAttribute("selfcontained");
            boolean isselfcontained = Boolean.valueOf(selfcontained);

            // parsing ref-pack-set file
            IXMLElement refXMLData = this.readRefPackData(refFileName, isselfcontained);

            Debug.log("Reading refpack from " + refFileName);
            // Recursively call myself to add all packs and refpacks from the reference XML
            addPacksSingle(refXMLData);
        }

        Iterator<IXMLElement> refPackSetIter = refPackSets.iterator();
        while (refPackSetIter.hasNext())
        {
            IXMLElement el = refPackSetIter.next();

            // the directory to scan
            String dir_attr = this.requireAttribute(el, "dir");

            File dir = new File(dir_attr);
            if (!dir.isAbsolute())
            {
                dir = new File(basedir, dir_attr);
            }
            if (!dir.isDirectory()) // also tests '.exists()'
            {
                parseError(el, "Invalid refpackset directory 'dir': " + dir_attr);
            }

            // include pattern
            String includeString = this.requireAttribute(el, "includes");
            String[] includes = includeString.split(", ");

            // scan for refpack files
            DirectoryScanner ds = new DirectoryScanner();
            ds.setIncludes(includes);
            ds.setBasedir(dir);
            ds.setCaseSensitive(true);
            ds.scan();

            // loop through all found fils and handle them as normal refpack files
            String[] files = ds.getIncludedFiles();
            for (int i = 0; i < files.length; i++)
            {
                String refFileName = new File(dir, files[i]).toString();

                // parsing ref-pack-set file
                IXMLElement refXMLData = this.readRefPackData(refFileName, false);

                // Recursively call myself to add all packs and refpacks from the reference XML
                addPacksSingle(refXMLData);
            }
        }

        notifyCompilerListener("addPacksSingle", CompilerListener.END, data);
    }

    private IXMLElement readRefPackData(String refFileName, boolean isselfcontained)
            throws CompilerException {
        File refXMLFile = new File(refFileName);
        if (!refXMLFile.isAbsolute())
        {
            refXMLFile = new File(basedir, refFileName);
        }
        if (!refXMLFile.canRead())
        {
            throw new CompilerException("Invalid file: " + refXMLFile);
        }

        InputStream specin = null;

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
        IXMLElement refXMLData = refXMLParser.parse(specin,refXMLFile.getAbsolutePath());

        // Now checked the loaded XML file for basic syntax
        // We check it
        if (!"installation".equalsIgnoreCase(refXMLData.getName()))
        {
            parseError(refXMLData, "this is not an IzPack XML installation file");
        }
        if (!VERSION.equalsIgnoreCase(requireAttribute(refXMLData, "version")))
        {
            parseError(refXMLData, "the file version is different from the compiler version");
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
     * Checks whether the dependencies stated in the configuration file are correct. Specifically it
     * checks that no pack point to a non existent pack and also that there are no circular
     * dependencies in the packs.
     */
    public void checkDependencies(List<PackInfo> packs) throws CompilerException {
        // Because we use package names in the configuration file we assosiate
        // the names with the objects
        Map<String, PackInfo> names = new HashMap<String, PackInfo>();
        for (PackInfo pack : packs)
        {
            names.put(pack.getPack().name, pack);
        }
        int result = dfs(packs, names);
        // @todo More informative messages to include the source of the error
        if (result == -2)
        {
            parseError("Circular dependency detected");
        }
        else if (result == -1)
        {
            parseError("A dependency doesn't exist");
        }
    }

    /**
     * We use the dfs graph search algorithm to check whether the graph is acyclic as described in:
     * Thomas H. Cormen, Charles Leiserson, Ronald Rivest and Clifford Stein. Introduction to
     * algorithms 2nd Edition 540-549,MIT Press, 2001
     *
     * @param packs The graph
     * @param names The name map
     */
    private int dfs(List<PackInfo> packs, Map<String, PackInfo> names) {
        Map<Edge, Integer> edges = new HashMap<Edge, Integer>();
        for (PackInfo pack : packs)
        {
            if (pack.colour == PackInfo.WHITE)
            {
                if (dfsVisit(pack, names, edges) != 0)
                {
                    return -1;
                }
            }

        }
        return checkBackEdges(edges);
    }

    /**
     * This function checks for the existence of back edges.
     */
    private int checkBackEdges(Map<Edge, Integer> edges) {
        Set<Edge> keys = edges.keySet();
        for (final Edge key : keys)
        {
            int color = edges.get(key);
            if (color == PackInfo.GREY)
            {
                return -2;
            }
        }
        return 0;

    }

    /**
     * This class is used for the classification of the edges
     */
    private class Edge {

        PackInfo u;

        PackInfo v;

        Edge(PackInfo u, PackInfo v) {
            this.u = u;
            this.v = v;
        }
    }

    private int dfsVisit(PackInfo u, Map<String, PackInfo> names, Map<Edge, Integer> edges) {
        u.colour = PackInfo.GREY;
        List<String> deps = u.getDependencies();
        if (deps != null)
        {
            for (String name : deps)
            {
                PackInfo v = names.get(name);
                if (v == null)
                {
                    System.out.println("Failed to find dependency: " + name);
                    return -1;
                }
                Edge edge = new Edge(u, v);
                if (edges.get(edge) == null)
                {
                    edges.put(edge, v.colour);
                }

                if (v.colour == PackInfo.WHITE)
                {

                    final int result = dfsVisit(v, names, edges);
                    if (result != 0)
                    {
                        return result;
                    }
                }
            }
        }
        u.colour = PackInfo.BLACK;
        return 0;
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
                                     List<OsConstraint> osList, int override, PackInfo pack, Map additionals,
                                     String condition) throws IOException {

        FileInputStream fin = new FileInputStream(archive);
        ZipInputStream zin = new ZipInputStream(fin);
        while (true)
        {
            ZipEntry zentry = zin.getNextEntry();
            if (zentry == null)
            {
                break;
            }
            if (zentry.isDirectory())
            {
                continue;
            }
            
            //System.out.println (zentry.getName());

            try
            {
                File temp = File.createTempFile("izpack", null);
                temp.deleteOnExit();

                FileOutputStream out = new FileOutputStream(temp);
                PackagerHelper.copyStream(zin, out);
                out.close();

                pack.addFile(baseDir, temp, targetdir + "/" + zentry.getName(), osList, override,
                        additionals, condition);
            }
            catch (IOException e)
            {
                throw new IOException("Couldn't create temporary file for " + zentry.getName()
                        + " in archive " + archive + " (" + e.getMessage() + ")");
            }

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
     *
     * @throws FileNotFoundException if the file does not exist
     */
    protected void addRecursively(File baseDir, File file, String targetdir,
                                  List<OsConstraint> osList, int override, PackInfo pack, Map additionals,
                                  String condition) throws IOException {
        String targetfile = targetdir + "/" + file.getName();
        if (!file.isDirectory())
        {
            pack.addFile(baseDir, file, targetfile, osList, override, additionals, condition);
        }
        else
        {
            File[] files = file.listFiles();
            if (files.length == 0) // The directory is empty so must be added
            {
                pack.addFile(baseDir, file, targetfile, osList, override, additionals, condition);
            }
            else
            {
                // new targetdir = targetfile;
                for (File file1 : files)
                {
                    addRecursively(baseDir, file1, targetfile, osList, override, pack, additionals,
                            condition);
                }
            }
        }
    }

    /**
     * Parse panels and their paramters, locate the panels resources and add to the Packager.
     *
     * @param data The XML data.
     *
     * @throws CompilerException Description of the Exception
     */
    protected void addPanels(IXMLElement data) throws CompilerException {
        notifyCompilerListener("addPanels", CompilerListener.BEGIN, data);
        IXMLElement root = requireChildNamed(data, "panels");

        // at least one panel is required
        Vector<IXMLElement> panels = root.getChildrenNamed("panel");
        if (panels.isEmpty())
        {
            parseError(root, "<panels> requires a <panel>");
        }

        // We process each panel markup
        Iterator<IXMLElement> iter = panels.iterator();
        // We need a panel counter to build unique panel dependet resource names
        int panelCounter = 0;
        while (iter.hasNext())
        {
            IXMLElement xmlPanel = iter.next();
            panelCounter++;

            // create the serialized Panel data
            Panel panel = new Panel();
            panel.osConstraints = OsConstraint.getOsList(xmlPanel);
            String className = xmlPanel.getAttribute("classname");
            // add an id
            String panelid = xmlPanel.getAttribute("id");
            panel.setPanelid(panelid);
            String condition = xmlPanel.getAttribute("condition");
            panel.setCondition(condition);

            // Panel files come in jars packaged w/ IzPack, or they can be
            // specified via a jar attribute on the panel element
            String jarPath = xmlPanel.getAttribute("jar");
            if (jarPath == null)
            {
                jarPath = "bin/panels/" + className + ".jar";
            }
            URL url = null;
            // jar="" may be used to suppress the warning message ("Panel jar
            // file not found")
            if (!jarPath.equals(""))
            {
                url = findIzPackResource(jarPath, "Panel jar file", xmlPanel, true);
            }

            // when the expected panel jar file is not found, it is assumed that
            // user will do the jar merge themselves via <jar> tag

            String fullClassName = null;
            if (url == null)
            {
                fullClassName = className;
            }
            else
            {
                try
                {
                    fullClassName = getFullClassName(url, className);
                }
                catch (IOException e)
                {
                }
            }

            if (fullClassName != null)
            {
                panel.className = fullClassName;
            }
            else
            {
                panel.className = className;
            }
            IXMLElement configurationElement = xmlPanel.getFirstChildNamed("configuration");
            if (configurationElement != null){
                Debug.trace("found a configuration for this panel.");
                Vector<IXMLElement> params = configurationElement.getChildrenNamed("param");
                if (params != null){
                    for(IXMLElement param : params){
                        IXMLElement keyElement = param.getFirstChildNamed("key");
                        IXMLElement valueElement = param.getFirstChildNamed("value");
                        if ((keyElement != null) && (valueElement != null)){
                            panel.addConfiguration(keyElement.getContent(), valueElement.getContent());
                        }
                    }    
                }                
            }
            
            // adding validator
            IXMLElement validatorElement = xmlPanel
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
            // adding helps
            Vector helps = xmlPanel.getChildrenNamed(HelpWindow.HELP_TAG);
            if (helps != null)
            {
                for (int helpIndex = 0; helpIndex < helps.size(); helpIndex++)
                {
                    IXMLElement help = (IXMLElement) helps.get(helpIndex);
                    String iso3 = help.getAttribute(HelpWindow.ISO3_ATTRIBUTE);
                    String resourceId;
                    if (panelid == null)
                    {
                        resourceId = className + "_" + panelCounter + "_help_" + iso3 + ".html";
                    }
                    else
                    {
                        resourceId = panelid + "_" + panelCounter + "_help_" + iso3 + ".html";
                    }
                    panel.addHelp(iso3, resourceId);

                    URL originalUrl = findProjectResource(help
                            .getAttribute(HelpWindow.SRC_ATTRIBUTE), "Help", help);
                    URL helpUrl = originalUrl;
                    compiler.addResource(resourceId, helpUrl);
                }
            }
            // adding actions
            addPanelActions(xmlPanel, panel);
            // insert into the packager
            compiler.addPanelJar(panel, url);
        }
        notifyCompilerListener("addPanels", CompilerListener.END, data);
    }

    /**
     * Adds the resources.
     *
     * @param data The XML data.
     *
     * @throws CompilerException Description of the Exception
     */
    protected void addResources(IXMLElement data) throws CompilerException {
        notifyCompilerListener("addResources", CompilerListener.BEGIN, data);
        IXMLElement root = data.getFirstChildNamed("resources");
        if (root == null)
        {
            return;
        }

        // We process each res markup
        Iterator<IXMLElement> iter = root.getChildrenNamed("res").iterator();
        while (iter.hasNext())
        {
            IXMLElement res = iter.next();
            String id = requireAttribute(res, "id");
            String src = requireAttribute(res, "src");
            // the parse attribute causes substitution to occur
            boolean substitute = validateYesNoAttribute(res, "parse", NO);
            // the parsexml attribute causes the xml document to be parsed
            boolean parsexml = validateYesNoAttribute(res, "parsexml", NO);

            String encoding = res.getAttribute("encoding");
            if (encoding == null)
            {
                encoding = "";
            }

            // basedir is not prepended if src is already an absolute path
            URL originalUrl = findProjectResource(src, "Resource", res);
            URL url = originalUrl;

            InputStream is = null;
            OutputStream os = null;
            try
            {
                if (parsexml || (!"".equals(encoding)) || (substitute && !compiler.getVariables().isEmpty()))
                {
                    // make the substitutions into a temp file
                    File parsedFile = File.createTempFile("izpp", null);
                    parsedFile.deleteOnExit();
                    FileOutputStream outFile = new FileOutputStream(parsedFile);
                    os = new BufferedOutputStream(outFile);
                    // and specify the substituted file to be added to the
                    // packager
                    url = parsedFile.toURL();
                }
                
                if (!"".equals(encoding))
                {
                    File recodedFile = File.createTempFile("izenc", null);
                    recodedFile.deleteOnExit();

                    InputStreamReader reader = new InputStreamReader(originalUrl.openStream(), encoding);
                    OutputStreamWriter writer = new OutputStreamWriter(
                            new FileOutputStream(recodedFile), "UTF-8");

                    char[] buffer = new char[1024];
                    int read = 0;
                    while ((read = reader.read(buffer)) != -1)
                    {
                        writer.write(buffer, 0, read);
                    }
                    reader.close();
                    writer.close();

                    if (parsexml)
                    {
                        originalUrl = recodedFile.toURL();
                    } else
                    {
                        url = recodedFile.toURL();
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
                    if (substitute && !compiler.getVariables().isEmpty())
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
                    if (compiler.getVariables().isEmpty())
                    {
                        // reset url to original.
                        url = originalUrl;
                        parseWarn(res, "No variables defined. " + url.getPath() + " not parsed.");
                    }
                    else
                    {
                        String type = res.getAttribute("type");

                        // if the xml parser did not open the url
                        // ('parsexml' was not enabled)
                        if (null == is)
                        {
                            is = new BufferedInputStream(originalUrl.openStream());
                        }
                        VariableSubstitutor vs = new VariableSubstitutor(compiler.getVariables());
                        vs.substitute(is, os, type, "UTF-8");
                    }
                }

            }
            catch (Exception e)
            {
                parseError(res, e.getMessage(), e);
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

            compiler.addResource(id, url);

            // remembering references to all added packsLang.xml files
            if (id.startsWith("packsLang.xml"))
            {
                List<URL> packsLangURLs = null;
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
     *
     * @throws CompilerException Description of the Exception
     */
    protected void addLangpacks(IXMLElement data) throws CompilerException {
        notifyCompilerListener("addLangpacks", CompilerListener.BEGIN, data);
        IXMLElement root = requireChildNamed(data, "locale");

        // at least one langpack is required
        Vector<IXMLElement> locals = root.getChildrenNamed("langpack");
        if (locals.isEmpty())
        {
            parseError(root, "<locale> requires a <langpack>");
        }

        // We process each langpack markup
        Iterator<IXMLElement> iter = locals.iterator();
        while (iter.hasNext())
        {
            IXMLElement el = iter.next();
            String iso3 = requireAttribute(el, "iso3");
            String path;

            path = "bin/langpacks/installer/" + iso3 + ".xml";
            URL iso3xmlURL = findIzPackResource(path, "ISO3 file", el);

            path = "bin/langpacks/flags/" + iso3 + ".gif";
            URL iso3FlagURL = findIzPackResource(path, "ISO3 flag image", el);

            compiler.addLangPack(iso3, iso3xmlURL, iso3FlagURL);
        }
        notifyCompilerListener("addLangpacks", CompilerListener.END, data);
    }

    /**
     * Builds the Info class from the XML tree.
     *
     * @param data The XML data. return The Info.
     *
     * @throws Exception Description of the Exception
     */
    protected void addInfo(IXMLElement data) throws Exception {
        notifyCompilerListener("addInfo", CompilerListener.BEGIN, data);
        // Initialisation
        IXMLElement root = requireChildNamed(data, "info");

        Info info = new Info();
        info.setAppName(requireContent(requireChildNamed(root, "appname")));
        info.setAppVersion(requireContent(requireChildNamed(root, "appversion")));
        // We get the installation subpath
        IXMLElement subpath = root.getFirstChildNamed("appsubpath");
        if (subpath != null)
        {
            info.setInstallationSubPath(requireContent(subpath));
        }

        // validate and insert app URL
        final IXMLElement URLElem = root.getFirstChildNamed("url");
        if (URLElem != null)
        {
            URL appURL = requireURLContent(URLElem);
            info.setAppURL(appURL.toString());
        }

        // We get the authors list
        IXMLElement authors = root.getFirstChildNamed("authors");
        if (authors != null)
        {
            Iterator<IXMLElement> iter = authors.getChildrenNamed("author").iterator();
            while (iter.hasNext())
            {
                IXMLElement author = iter.next();
                String name = requireAttribute(author, "name");
                String email = requireAttribute(author, "email");
                info.addAuthor(new Info.Author(name, email));
            }
        }

        // We get the java version required
        IXMLElement javaVersion = root.getFirstChildNamed("javaversion");
        if (javaVersion != null)
        {
            info.setJavaVersion(requireContent(javaVersion));
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
            info.setWebDirURL(requireURLContent(webDirURL).toString());
        }
        String kind = compiler.getKind();
        if (kind != null)
        {
            if (kind.equalsIgnoreCase(WEB) && webDirURL == null)
            {
                parseError(root, "<webdir> required when \"WEB\" installer requested");
            }
            else if (kind.equalsIgnoreCase(STANDARD) && webDirURL != null)
            {
                // Need a Warning? parseWarn(webDirURL, "Not creating web
                // installer.");
                info.setWebDirURL(null);
            }
        }

        // Pack200 support
        IXMLElement pack200 = root.getFirstChildNamed("pack200");
        info.setPack200Compression(pack200 != null);
        
        // ISAdxAdmin
        IXMLElement isAdxAdmin = root.getFirstChildNamed("is-adxadmin");
        info.setIsAdxAdmin(isAdxAdmin != null);
        

        // Privileged execution
        IXMLElement privileged = root.getFirstChildNamed("run-privileged");
        info.setRequirePrivilegedExecution(privileged != null);
        if (privileged != null && privileged.hasAttribute("condition"))
        {
            info.setPrivilegedExecutionConditionID(privileged.getAttribute("condition"));
        }

        // Add the uninstaller as a resource if specified
        IXMLElement uninstallInfo = root.getFirstChildNamed("uninstaller");
        if (validateYesNoAttribute(uninstallInfo, "write", YES))
        {
            URL url = findIzPackResource("lib/uninstaller.jar", "Uninstaller", root);
            compiler.addResource("IzPack.uninstaller", url);
            
            if (privileged != null)
            {
                // default behavior for uninstaller elevation: elevate if installer has to be elevated too
                info.setRequirePrivilegedExecutionUninstaller(validateYesNoAttribute(privileged,
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
            info.setSummaryLogFilePath(requireContent(slfPath));
        }

        IXMLElement writeInstallInfo = root.getFirstChildNamed("writeinstallationinformation");
        if (writeInstallInfo != null)
        {
            String writeInstallInfoString = requireContent(writeInstallInfo);
            info.setWriteInstallationInformation(validateYesNo(writeInstallInfoString));
        }

        // look for an unpacker class
        String unpackerclass = compiler.getProperty("UNPACKER_CLASS");
        info.setUnpackerClassName(unpackerclass);
        compiler.setInfo(info);
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
     *
     * @throws CompilerException Description of the Exception
     */
    protected void addVariables(IXMLElement data) throws CompilerException {
        notifyCompilerListener("addVariables", CompilerListener.BEGIN, data);
        // We get the varible list
        IXMLElement root = data.getFirstChildNamed("variables");
        if (root == null)
        {
            return;
        }

        Properties variables = compiler.getVariables();

        Iterator<IXMLElement> iter = root.getChildrenNamed("variable").iterator();
        while (iter.hasNext())
        {
            IXMLElement var = iter.next();
            String name = requireAttribute(var, "name");
            String value = requireAttribute(var, "value");
            if (variables.contains(name))
            {
                parseWarn(var, "Variable '" + name + "' being overwritten");
            }
            variables.setProperty(name, value);
        }
        notifyCompilerListener("addVariables", CompilerListener.END, data);
    }

    protected void addDynamicVariables(IXMLElement data) throws CompilerException {
        notifyCompilerListener("addDynamicVariables", CompilerListener.BEGIN, data);
        // We get the dynamic variable list
        IXMLElement root = data.getFirstChildNamed("dynamicvariables");
        if (root == null)
        {
            return;
        }

        Map<String, List<DynamicVariable>> dynamicvariables = compiler.getDynamicVariables();

        Iterator<IXMLElement> iter = root.getChildrenNamed("variable").iterator();
        while (iter.hasNext())
        {
            IXMLElement var = iter.next();
            String name = requireAttribute(var, "name");
            String value = var.getAttribute("value");
            if (value==null){
                IXMLElement valueElement = var.getFirstChildNamed("value");
                if (valueElement != null){
                    value = valueElement.getContent();
                    if (value == null){
                       parseError("A dynamic variable needs either a value attribute or a value element.");
                    }                       
                }
                else {
                    parseError("A dynamic variable needs either a value attribute or a value element. Variable name: " + name);    
                }                
            }
            String conditionid = var.getAttribute("condition");

            List<DynamicVariable> dynamicValues = new ArrayList<DynamicVariable>();
            if (dynamicvariables.containsKey(name))
            {
                dynamicValues = dynamicvariables.get(name);
            }
            else
            {
                dynamicvariables.put(name, dynamicValues);
            }

            DynamicVariable dynamicVariable = new DynamicVariable();
            dynamicVariable.setName(name);
            dynamicVariable.setValue(value);
            dynamicVariable.setConditionid(conditionid);
            if (dynamicValues.remove(dynamicVariable))
            {
                parseWarn(var, "Dynamic Variable '" + name + "' will be overwritten");
            }
            dynamicValues.add(dynamicVariable);
        }
        notifyCompilerListener("addDynamicVariables", CompilerListener.END, data);
    }

    /**
     * Parse conditions and add them to the compiler.
     *
     * @param data
     *
     * @throws CompilerException
     */
    protected void addConditions(IXMLElement data) throws CompilerException {
        notifyCompilerListener("addConditions", CompilerListener.BEGIN, data);
        // We get the condition list
        IXMLElement root = data.getFirstChildNamed("conditions");
        Map<String, Condition> conditions = compiler.getConditions();
        if (root != null)
        {
            Iterator<IXMLElement> iter = root.getChildrenNamed("condition").iterator();
            while (iter.hasNext())
            {
                IXMLElement conditionel = iter.next();
                Condition condition = RulesEngine.analyzeCondition(conditionel);
                if (condition != null)
                {
                    String conditionid = condition.getId();
                    if (conditions.containsKey(conditionid))
                    {
                        parseWarn(conditionel, "Condition with id '" + conditionid
                                + "' will be overwritten");
                    }
                    conditions.put(conditionid, condition);

                }
                else
                {
                    parseWarn(conditionel, "Condition couldn't be instantiated.");
                }
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
     *
     * @throws CompilerException Description of the Exception
     */
    protected void substituteProperties(IXMLElement data) throws CompilerException {
        notifyCompilerListener("substituteProperties", CompilerListener.BEGIN, data);

        IXMLElement root = data.getFirstChildNamed("properties");
        if (root != null)
        {
            // add individual properties
            Iterator<IXMLElement> iter = root.getChildrenNamed("property").iterator();
            while (iter.hasNext())
            {
                IXMLElement prop = iter.next();
                Property property = new Property(prop, this);
                property.execute();
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
    protected void substituteAllProperties(IXMLElement element) throws CompilerException {
        Enumeration attributes = element.enumerateAttributeNames();
        while (attributes.hasMoreElements())
        {
            String name = (String) attributes.nextElement();
            String value = compiler.replaceProperties(element.getAttribute(name));
            element.setAttribute(name, value);
        }

        String content = element.getContent();
        if (content != null)
        {
            element.setContent(compiler.replaceProperties(content));
        }

        for (int i = 0; i < element.getChildren().size(); i++)
        {
            IXMLElement child = (IXMLElement) element.getChildren().elementAt(i);
            substituteAllProperties(child);
        }
    }

    /**
     * Checks whether a File instance is a regular file, exists and is readable. Throws appropriate
     * CompilerException to report violations of these conditions.
     *
     * @throws CompilerException if the file is either not existing, not a regular file or not
     *                           readable.
     */
    private void assertIsNormalReadableFile(File fileToCheck, String fileDescription)
            throws CompilerException {
        if (fileToCheck != null)
        {
            if (!fileToCheck.exists())
            {
                throw new CompilerException(fileDescription
                        + " does not exist: " + fileToCheck);
            }
            if (!fileToCheck.isFile())
            {
                throw new CompilerException(fileDescription
                        + " is not a regular file: " + fileToCheck);
            }
            if (!fileToCheck.canRead())
            {
                throw new CompilerException(fileDescription
                        + " is not readable by application: " + fileToCheck);
            }
        }
    }

    /**
     * Returns the IXMLElement representing the installation XML file.
     *
     * @return The XML tree.
     *
     * @throws CompilerException For problems with the installation file
     * @throws IOException       for errors reading the installation file
     */
    protected IXMLElement getXMLTree() throws CompilerException, IOException {
        IXMLParser parser = new XMLParser();
        IXMLElement data = null;
        if (filename != null)
        {
            File file = new File(filename).getAbsoluteFile();
            assertIsNormalReadableFile(file, "Configuration file");
            data = parser.parse(new FileInputStream(filename),file.getAbsolutePath());
            // add izpack built in property
            compiler.setProperty("izpack.file", file.toString());
        }
        else if (installText != null)
        {
            data = parser.parse(installText);
        }
        else
        {
            throw new CompilerException("Neither install file nor text specified");
        }
        // We check it
        if (!"installation".equalsIgnoreCase(data.getName()))
        {
            parseError(data, "this is not an IzPack XML installation file");
        }
        if (!VERSION.equalsIgnoreCase(requireAttribute(data, "version")))
        {
            parseError(data, "the file version is different from the compiler version");
        }

        // We finally return the tree
        return data;
    }

    protected int getOverrideValue(IXMLElement f) throws CompilerException {
        int override = PackFile.OVERRIDE_UPDATE;

        String override_val = f.getAttribute("override");
        if (override_val != null)
        {
            if ("true".equalsIgnoreCase(override_val))
            {
                override = PackFile.OVERRIDE_TRUE;
            }
            else if ("false".equalsIgnoreCase(override_val))
            {
                override = PackFile.OVERRIDE_FALSE;
            }
            else if ("asktrue".equalsIgnoreCase(override_val))
            {
                override = PackFile.OVERRIDE_ASK_TRUE;
            }
            else if ("askfalse".equalsIgnoreCase(override_val))
            {
                override = PackFile.OVERRIDE_ASK_FALSE;
            }
            else if ("update".equalsIgnoreCase(override_val))
            {
                override = PackFile.OVERRIDE_UPDATE;
            }
            else
            {
                parseError(f, "invalid value for attribute \"override\"");
            }
        }

        return override;
    }

    /**
     * Look for a project specified resources, which, if not absolute, are sought relative to the
     * projects basedir. The path should use '/' as the fileSeparator. If the resource is not found,
     * a CompilerException is thrown indicating fault in the parent element.
     *
     * @param path   the relative path (using '/' as separator) to the resource.
     * @param desc   the description of the resource used to report errors
     * @param parent the IXMLElement the resource is specified in, used to report errors
     *
     * @return a URL to the resource.
     */
    private URL findProjectResource(String path, String desc, IXMLElement parent)
            throws CompilerException {
        URL url = null;
        File resource = new File(path);
        if (!resource.isAbsolute())
        {
            resource = new File(basedir, path);
        }

        if (!resource.exists()) // fatal
        {
            parseError(parent, desc + " not found: " + resource);
        }

        try
        {
            url = resource.toURL();
        }
        catch (MalformedURLException how)
        {
            parseError(parent, desc + "(" + resource + ")", how);
        }

        return url;
    }

    private URL findIzPackResource(String path, String desc, IXMLElement parent)
            throws CompilerException {
        return findIzPackResource(path, desc, parent, false);
    }

    /**
     * Look for an IzPack resource either in the compiler jar, or within IZPACK_HOME. The path must
     * not be absolute. The path must use '/' as the fileSeparator (it's used to access the jar
     * file). If the resource is not found, take appropriate action base on ignoreWhenNotFound flag.
     *
     * @param path               the relative path (using '/' as separator) to the resource.
     * @param desc               the description of the resource used to report errors
     * @param parent             the IXMLElement the resource is specified in, used to report errors
     * @param ignoreWhenNotFound when false, throws a CompilerException indicating
     *                           fault in the parent element when resource not found.
     *
     * @return a URL to the resource.
     */
    private URL findIzPackResource(String path, String desc, IXMLElement parent, boolean ignoreWhenNotFound)
            throws CompilerException {
        URL url = getClass().getResource("/" + path);
        if (url == null)
        {
            File resource = new File(path);

            if (!resource.isAbsolute())
            {
                resource = new File(Compiler.IZPACK_HOME, path);
            }

            if (resource.exists())
            {
                try
                {
                    url = resource.toURL();
                }
                catch (MalformedURLException how)
                {
                    parseError(parent, desc + "(" + resource + ")", how);
                }
            }
            else
            {
                if (ignoreWhenNotFound)
                {
                    parseWarn(parent, desc + " not found: " + resource);
                }
                else
                {
                    parseError(parent, desc + " not found: " + resource);
                }
            }

        }

        return url;
    }

    /**
     * Create parse error with consistent messages. Includes file name. For use When parent is
     * unknown.
     *
     * @param message Brief message explaining error
     */
    protected void parseError(String message) throws CompilerException {
        throw new CompilerException(filename + ":" + message);
    }

    /**
     * Create parse error with consistent messages. Includes file name and line # of parent. It is
     * an error for 'parent' to be null.
     *
     * @param parent  The element in which the error occured
     * @param message Brief message explaining error
     */
    protected void parseError(IXMLElement parent, String message) throws CompilerException {
        throw new CompilerException(filename + ":" + parent.getLineNr() + ": " + message);
    }

    /**
     * Create a chained parse error with consistent messages. Includes file name and line # of
     * parent. It is an error for 'parent' to be null.
     *
     * @param parent  The element in which the error occured
     * @param message Brief message explaining error
     */
    protected void parseError(IXMLElement parent, String message, Throwable cause)
            throws CompilerException {
        throw new CompilerException(filename + ":" + parent.getLineNr() + ": " + message, cause);
    }

    /**
     * Create a parse warning with consistent messages. Includes file name and line # of parent. It
     * is an error for 'parent' to be null.
     *
     * @param parent  The element in which the warning occured
     * @param message Warning message
     */
    protected void parseWarn(IXMLElement parent, String message) {
        System.out.println("Warning: " + filename + ":" + parent.getLineNr() + ": " + message);
    }

    /**
     * Call getFirstChildNamed on the parent, producing a meaningful error message on failure. It is
     * an error for 'parent' to be null.
     *
     * @param parent The element to search for a child
     * @param name   Name of the child element to get
     */
    protected IXMLElement requireChildNamed(IXMLElement parent, String name) throws CompilerException {
        IXMLElement child = parent.getFirstChildNamed(name);
        if (child == null)
        {
            parseError(parent, "<" + parent.getName() + "> requires child <" + name + ">");
        }
        return child;
    }

    /**
     * Call getContent on an element, producing a meaningful error message if not present, or empty,
     * or a valid URL. It is an error for 'element' to be null.
     *
     * @param element The element to get content of
     */
    protected URL requireURLContent(IXMLElement element) throws CompilerException {
        URL url = null;
        try
        {
            url = new URL(requireContent(element));
        }
        catch (MalformedURLException x)
        {
            parseError(element, "<" + element.getName() + "> requires valid URL", x);
        }
        return url;
    }

    /**
     * Call getContent on an element, producing a meaningful error message if not present, or empty.
     * It is an error for 'element' to be null.
     *
     * @param element The element to get content of
     */
    protected String requireContent(IXMLElement element) throws CompilerException {
        String content = element.getContent();
        if (content == null || content.length() == 0)
        {
            parseError(element, "<" + element.getName() + "> requires content");
        }
        return content;
    }

    protected boolean validateYesNo(String value) {
        boolean result = false;
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
     * Call getAttribute on an element, producing a meaningful error message if not present, or
     * empty. It is an error for 'element' or 'attribute' to be null.
     *
     * @param element   The element to get the attribute value of
     * @param attribute The name of the attribute to get
     */
    protected String requireAttribute(IXMLElement element, String attribute)
            throws CompilerException {
        String value = element.getAttribute(attribute);
        if (value == null)
        {
            parseError(element, "<" + element.getName() + "> requires attribute '" + attribute
                    + "'");
        }
        return value;
    }

    /**
     * Get a required attribute of an element, ensuring it is an integer. A meaningful error message
     * is generated as a CompilerException if not present or parseable as an int. It is an error for
     * 'element' or 'attribute' to be null.
     *
     * @param element   The element to get the attribute value of
     * @param attribute The name of the attribute to get
     */
    protected int requireIntAttribute(IXMLElement element, String attribute)
            throws CompilerException {
        String value = element.getAttribute(attribute);
        if (value == null || value.length() == 0)
        {
            parseError(element, "<" + element.getName() + "> requires attribute '" + attribute
                    + "'");
        }
        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException x)
        {
            parseError(element, "'" + attribute + "' must be an integer");
        }
        return 0; // never happens
    }

    /**
     * Call getAttribute on an element, producing a meaningful error message if not present, or one
     * of "yes" or "no". It is an error for 'element' or 'attribute' to be null.
     *
     * @param element   The element to get the attribute value of
     * @param attribute The name of the attribute to get
     */
    protected boolean requireYesNoAttribute(IXMLElement element, String attribute)
            throws CompilerException {
        String value = requireAttribute(element, attribute);
        if ("yes".equalsIgnoreCase(value))
        {
            return true;
        }
        if ("no".equalsIgnoreCase(value))
        {
            return false;
        }

        parseError(element, "<" + element.getName() + "> invalid attribute '" + attribute
                + "': Expected (yes|no)");

        return false; // never happens
    }

    /**
     * Call getAttribute on an element, producing a meaningful warning if not "yes" or "no". If the
     * 'element' or 'attribute' are null, the default value is returned.
     *
     * @param element      The element to get the attribute value of
     * @param attribute    The name of the attribute to get
     * @param defaultValue Value returned if attribute not present or invalid
     */
    protected boolean validateYesNoAttribute(IXMLElement element, String attribute,
                                             boolean defaultValue) {
        if (element == null)
        {
            return defaultValue;
        }

        String value = element.getAttribute(attribute, (defaultValue ? "yes" : "no"));
        if ("yes".equalsIgnoreCase(value))
        {
            return true;
        }
        if ("no".equalsIgnoreCase(value))
        {
            return false;
        }

        // TODO: should this be an error if it's present but "none of the
        // above"?
        parseWarn(element, "<" + element.getName() + "> invalid attribute '" + attribute
                + "': Expected (yes|no) if present");

        return defaultValue;
    }

    /**
     * The main method if the compiler is invoked by a command-line call.
     *
     * @param args The arguments passed on the command-line.
     */
    public static void main(String[] args) {
        // Outputs some informations
        System.out.println("");
        System.out.println(".::  IzPack - Version " + Compiler.IZPACK_VERSION + " ::.");
        System.out.println("");
        System.out.println("< compiler specifications version: " + VERSION + " >");
        System.out.println("");
        System.out.println("- Copyright (c) 2001-2008 Julien Ponge");
        System.out.println("- Visit http://izpack.org/ for the latest releases");
        System.out
                .println("- Released under the terms of the Apache Software License version 2.0.");
        System.out.println("");

        // exit code 1 means: error
        int exitCode = 1;
        String home = ".";

        // We get the IzPack home directory
        String izHome = System.getProperty("izpack.home");
        if (izHome != null)
        {
            home = izHome;
        }
        else
        {
            izHome = System.getenv("IZPACK_HOME");
            if (izHome != null)
            {
                home = izHome;
            }
        }

        // We analyse the command line parameters
        try
        {
            // Our arguments
            String filename;
            String base = ".";
            String kind = "standard";
            String output;
            String compr_format = "default";
            int compr_level = -1;

            // First check
            int nArgs = args.length;
            if (nArgs < 1)
            {
                throw new Exception("no arguments given");
            }

            // The users wants to know the command line parameters
            if ("-?".equalsIgnoreCase(args[0]))
            {
                System.out.println("-> Command line parameters are : (xml file) [args]");
                System.out.println("   (xml file): the xml file describing the installation");
                System.out
                        .println("   -h (IzPack home) : the root path of IzPack. This will be needed");
                System.out
                        .println("               if the compiler is not called in the root directory  of IzPack.");
                System.out
                        .println("               Do not forget quotations if there are blanks in the path.");
                System.out
                        .println("   -b (base) : indicates the base path that the compiler will use for filenames");
                System.out
                        .println("               of sources. Default is the current path. Attend to -h.");
                System.out.println("   -k (kind) : indicates the kind of installer to generate");
                System.out.println("               default is standard");
                System.out.println("   -o (out)  : indicates the output file name");
                System.out.println("               default is the xml file name\n");
                System.out
                        .println("   -c (compression)  : indicates the compression format to be used for packs");
                System.out.println("               default is the internal deflate compression\n");
                System.out
                        .println("   -l (compression-level)  : indicates the level for the used compression format");
                System.out.println("                if supported. Only integer are valid\n");

                System.out
                        .println("   When using vm option -DSTACKTRACE=true there is all kind of debug info ");
                System.out.println("");
                exitCode = 0;
            }
            else
            {
                // We can parse the other parameters & try to compile the
                // installation

                // We get the input file name and we initialize the output file
                // name
                filename = args[0];
                // default jar files names are based on input file name
                output = filename.substring(0, filename.length() - 3) + "jar";

                // We parse the other ones
                int pos = 1;
                while (pos < nArgs)
                {
                    if ((args[pos].startsWith("-")) && (args[pos].length() == 2))
                    {
                        switch (args[pos].toLowerCase().charAt(1))
                        {
                            case 'b':
                                if ((pos + 1) < nArgs)
                                {
                                    pos++;
                                    base = args[pos];
                                }
                                else
                                {
                                    throw new Exception("base argument missing");
                                }
                                break;
                            case 'k':
                                if ((pos + 1) < nArgs)
                                {
                                    pos++;
                                    kind = args[pos];
                                }
                                else
                                {
                                    throw new Exception("kind argument missing");
                                }
                                break;
                            case 'o':
                                if ((pos + 1) < nArgs)
                                {
                                    pos++;
                                    output = args[pos];
                                }
                                else
                                {
                                    throw new Exception("output argument missing");
                                }
                                break;
                            case 'c':
                                if ((pos + 1) < nArgs)
                                {
                                    pos++;
                                    compr_format = args[pos];
                                }
                                else
                                {
                                    throw new Exception("compression format argument missing");
                                }
                                break;
                            case 'l':
                                if ((pos + 1) < nArgs)
                                {
                                    pos++;
                                    compr_level = Integer.parseInt(args[pos]);
                                }
                                else
                                {
                                    throw new Exception("compression level argument missing");
                                }
                                break;
                            case 'h':
                                if ((pos + 1) < nArgs)
                                {
                                    pos++;
                                    home = args[pos];
                                }
                                else
                                {
                                    throw new Exception("IzPack home path argument missing");
                                }
                                break;
                            default:
                                throw new Exception("unknown argument");
                        }
                        pos++;
                    }
                    else
                    {
                        throw new Exception("bad argument");
                    }
                }

                home = resolveIzPackHome(home);
                // Outputs what we are going to do
                System.out.println("-> Processing  : " + filename);
                System.out.println("-> Output      : " + output);
                System.out.println("-> Base path   : " + base);
                System.out.println("-> Kind        : " + kind);
                System.out.println("-> Compression : " + compr_format);
                System.out.println("-> Compr. level: " + compr_level);
                System.out.println("-> IzPack home : " + home);
                System.out.println("");

                Compiler.setIzpackHome(home);

                // Calls the compiler
                CmdlinePackagerListener listener = new CmdlinePackagerListener();
                CompilerConfig compiler = new CompilerConfig(filename, base, kind, output,
                        compr_format, compr_level, listener, null);
                compiler.executeCompiler();

                // Waits
                while (compiler.isAlive())
                {
                    Thread.sleep(100);
                }

                if (compiler.wasSuccessful())
                {
                    exitCode = 0;
                }

                System.out.println("Build time: " + new Date());
            }
        }
        catch (Exception err)
        {
            // Something bad has happened
            System.err.println("-> Fatal error :");
            System.err.println("   " + err.getMessage());
            err.printStackTrace();
            System.err.println("");
            System.err.println("(tip : use -? to get the commmand line parameters)");
        }

        // Closes the JVM
        System.exit(exitCode);
    }

    private static String resolveIzPackHome(String home) {
        File test = new File(home, IZ_TEST_SUBDIR + File.separator + IZ_TEST_FILE);
        if (test.exists())
        {
            return (home);
        }
        // Try to resolve the path using compiler.jar which also should be under
        // IZPACK_HOME.
        String self = Compiler.class.getName();
        self = self.replace('.', '/');
        self = "/" + self + ".class";
        URL url = Compiler.class.getResource(self);
        String np = url.getFile();
        int start = np.indexOf(self);
        np = np.substring(0, start);
        if (np.endsWith("!"))
        { // Where shut IZPACK_HOME at the standalone-compiler be??
            // No idea.
            if (np.endsWith("standalone-compiler.jar!")
                    || np.endsWith("standalone-compiler-4.0.0.jar!")
                    || np.matches("standalone-compiler-[\\d\\.]+.jar!"))
            {
                return (".");
            }
            np = np.substring(0, np.length() - 1);
        }
        File root = null;
        if (URI.create(np).isAbsolute())
        {
            root = new File(URI.create(np));
        }
        else
        {
            root = new File(np);
        }
        while (true)
        {
            if (root == null)
            {
                throw new IllegalArgumentException(
                        "No valid IzPack home directory found");
            }
            test = new File(root, IZ_TEST_SUBDIR + File.separator + IZ_TEST_FILE);
            if (test.exists())
            {
                return (root.getAbsolutePath());
            }
            root = root.getParentFile();
        }
    }

    // -------------------------------------------------------------------------
    // ------------- Listener stuff ------------------------- START ------------

    /**
     * This method parses install.xml for defined listeners and put them in the right position. If
     * posible, the listeners will be validated. Listener declaration is a fragmention in
     * install.xml like : <listeners> <listener compiler="PermissionCompilerListener"
     * installer="PermissionInstallerListener"/> </<listeners>
     *
     * @param data the XML data
     *
     * @throws Exception Description of the Exception
     */
    private void addCustomListeners(IXMLElement data) throws Exception {
        // We get the listeners
        IXMLElement root = data.getFirstChildNamed("listeners");
        if (root == null)
        {
            return;
        }
        Iterator<IXMLElement> iter = root.getChildrenNamed("listener").iterator();
        while (iter.hasNext())
        {
            IXMLElement xmlAction = iter.next();
            Object[] listener = getCompilerListenerInstance(xmlAction);
            if (listener != null)
            {
                addCompilerListener((CompilerListener) listener[0]);
            }
            String[] typeNames = new String[]{"installer", "uninstaller"};
            int[] types = new int[]{CustomData.INSTALLER_LISTENER,
                    CustomData.UNINSTALLER_LISTENER};
            for (int i = 0; i < typeNames.length; ++i)
            {
                String className = xmlAction.getAttribute(typeNames[i]);
                if (className != null)
                {
                    // Check for a jar attribute on the listener
                    String jarPath = xmlAction.getAttribute("jar");
                    jarPath = compiler.replaceProperties(jarPath);
                    if (jarPath == null)
                    {
                        jarPath = "bin/customActions/" + className + ".jar";
                    }
                    List<OsConstraint> constraints = OsConstraint.getOsList(xmlAction);
                    compiler.addCustomListener(types[i], className, jarPath, constraints);
                }
            }
        }

    }

    /**
     * Returns a list which contains the pathes of all files which are included in the given url.
     * This method expects as the url param a jar.
     *
     * @param url url of the jar file
     *
     * @return full qualified paths of the contained files
     *
     * @throws Exception
     */
    private List<String> getContainedFilePaths(URL url) throws Exception {
        JarInputStream jis = new JarInputStream(url.openStream());
        ZipEntry zentry = null;
        ArrayList<String> fullNames = new ArrayList<String>();
        while ((zentry = jis.getNextEntry()) != null)
        {
            String name = zentry.getName();
            // Add only files, no directory entries.
            if (!zentry.isDirectory())
            {
                fullNames.add(name);
            }
        }
        jis.close();
        return (fullNames);
    }

    /**
     * Returns the qualified class name for the given class. This method expects as the url param a
     * jar file which contains the given class. It scans the zip entries of the jar file.
     *
     * @param url       url of the jar file which contains the class
     * @param className short name of the class for which the full name should be resolved
     *
     * @return full qualified class name
     *
     * @throws IOException
     */
    private String getFullClassName(URL url, String className) throws IOException // throws
    // Exception
    {
        JarInputStream jis = new JarInputStream(url.openStream());
        ZipEntry zentry = null;
        while ((zentry = jis.getNextEntry()) != null)
        {
            String name = zentry.getName();
            int lastPos = name.lastIndexOf(".class");
            if (lastPos < 0)
            {
                continue; // No class file.
            }
            name = name.replace('/', '.');
            int pos = -1;
            int nonCasePos = -1;
            if (className != null)
            {
                pos = name.indexOf(className);
                nonCasePos = name.toLowerCase().indexOf(className.toLowerCase());
            }
            if (pos != -1 && name.length() == pos + className.length() + 6) // "Main" class found
            {
                jis.close();
                return (name.substring(0, lastPos));
            }

            if (nonCasePos != -1 && name.length() == nonCasePos + className.length() + 6)
            // "Main" class with different case found
            {
                throw new IllegalArgumentException(
                        "Fatal error! The declared panel name in the xml file (" + className
                                + ") differs in case to the founded class file (" + name + ").");
            }
        }
        jis.close();
        return (null);
    }

    /**
     * Returns the compiler listener which is defined in the xml element. As xml element a "listner"
     * node will be expected. Additional it is expected, that either "findIzPackResource" returns an
     * url based on "bin/customActions/[className].jar", or that the listener element has a jar
     * attribute specifying the listener jar path. The class will be loaded via an URLClassLoader.
     *
     * @param var the xml element of the "listener" node
     *
     * @return instance of the defined compiler listener
     *
     * @throws Exception
     */
    private Object[] getCompilerListenerInstance(IXMLElement var) throws Exception {
        String className = var.getAttribute("compiler");
        Class listener = null;
        Object instance = null;
        if (className == null)
        {
            return (null);
        }

        // CustomAction files come in jars packaged IzPack, or they can be
        // specified via a jar attribute on the listener
        String jarPath = var.getAttribute("jar");
        jarPath = compiler.replaceProperties(jarPath);
        if (jarPath == null)
        {
            jarPath = "bin/customActions/" + className + ".jar";
        }
        URL url = findIzPackResource(jarPath, "CustomAction jar file", var);
        String fullName = getFullClassName(url, className);
        if (fullName == null)
        {
            // class not found
            return null;
        }
        if (url != null)
        {
            if (getClass().getResource("/" + jarPath) != null)
            { // Oops, standalone, URLClassLoader will not work ...
                // Write the jar to a temp file.
                InputStream in = null;
                FileOutputStream outFile = null;
                byte[] buffer = new byte[5120];
                File tf = null;
                try
                {
                    tf = File.createTempFile("izpj", ".jar");
                    tf.deleteOnExit();
                    outFile = new FileOutputStream(tf);
                    in = getClass().getResourceAsStream("/" + jarPath);
                    long bytesCopied = 0;
                    int bytesInBuffer;
                    while ((bytesInBuffer = in.read(buffer)) != -1)
                    {
                        outFile.write(buffer, 0, bytesInBuffer);
                        bytesCopied += bytesInBuffer;
                    }
                }
                finally
                {
                    if (in != null)
                    {
                        in.close();
                    }
                    if (outFile != null)
                    {
                        outFile.close();
                    }
                }
                url = tf.toURL();

            }
            // Use the class loader of the interface as parent, else
            // compile will fail at using it via an Ant task.
            URLClassLoader ucl = new URLClassLoader(new URL[]{url}, CompilerListener.class
                    .getClassLoader());
            listener = ucl.loadClass(fullName);
        }
        if (listener != null)
        {
            instance = listener.newInstance();
        }
        else
        {
            parseError(var, "Cannot find defined compiler listener " + className);
        }
        if (!CompilerListener.class.isInstance(instance))
        {
            parseError(var, "'" + className + "' must be implemented "
                    + CompilerListener.class.toString());
        }
        List<OsConstraint> constraints = OsConstraint.getOsList(var);
        return (new Object[]{instance, className, constraints});
    }

    /**
     * Add a CompilerListener. A registered CompilerListener will be called at every enhancmend
     * point of compiling.
     *
     * @param pe CompilerListener which should be added
     */
    private void addCompilerListener(CompilerListener pe) {
        compilerListeners.add(pe);
    }

    /**
     * Calls all defined compile listeners notify method with the given data
     *
     * @param callerName name of the calling method as string
     * @param state      CompileListener.BEGIN or END
     * @param data       current install data
     *
     * @throws CompilerException
     */
    private void notifyCompilerListener(String callerName, int state, IXMLElement data)
            throws CompilerException {
        Iterator<CompilerListener> i = compilerListeners.iterator();
        IPackager packager = compiler.getPackager();
        while (i != null && i.hasNext())
        {
            CompilerListener listener = i.next();
            listener.notify(callerName, state, data, packager);
        }

    }

    /**
     * Calls the reviseAdditionalDataMap method of all registered CompilerListener's.
     *
     * @param f file releated XML node
     *
     * @return a map with the additional attributes
     */
    private Map getAdditionals(IXMLElement f) throws CompilerException {
        Iterator<CompilerListener> i = compilerListeners.iterator();
        Map retval = null;
        try
        {
            while (i != null && i.hasNext())
            {
                retval = (i.next()).reviseAdditionalDataMap(retval, f);
            }
        }
        catch (CompilerException ce)
        {
            parseError(f, ce.getMessage());
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
    private void mergePacksLangFiles() throws CompilerException {
        // just one packslang file. nothing to do here
        if (packsLangUrlMap.size() <= 0) return;

        OutputStream os = null;
        try
        {
            IXMLParser parser = new XMLParser();

            // loop through all packsLang resources, e.g. packsLang.xml_eng, packsLang.xml_deu, ...
            for (String id : packsLangUrlMap.keySet())
            {
                URL mergedPackLangFileURL = null;

                List<URL> packsLangURLs = packsLangUrlMap.get(id);
                if (packsLangURLs.size() == 0) continue; // should not occure

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
                            Vector<IXMLElement> langStrings = xml.getChildrenNamed("str");
                            for (IXMLElement langString : langStrings)
                            {
                                mergedPacksLang.addChild(langString);
                            }
                        }
                    }

                    // writing merged strings to a new file
                    File mergedPackLangFile = File.createTempFile("izpp", null);
                    mergedPackLangFile.deleteOnExit();

                    FileOutputStream outFile = new FileOutputStream(mergedPackLangFile);
                    os = new BufferedOutputStream(outFile);

                    IXMLWriter xmlWriter = new XMLWriter(os);
                    xmlWriter.write(mergedPacksLang);
                    os.close();
                    os = null;

                    // getting the URL to the new merged file
                    mergedPackLangFileURL = mergedPackLangFile.toURL();
                }

                compiler.addResource(id, mergedPackLangFileURL);
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
            Vector<IXMLElement> actionList = xmlActions
                    .getChildrenNamed(PanelAction.PANEL_ACTION_TAG);
            if (actionList != null)
            {
                for (IXMLElement action : actionList){
                    String stage = action.getAttribute(PanelAction.PANEL_ACTION_STAGE_TAG);
                    String actionName = action.getAttribute(PanelAction.PANEL_ACTION_CLASSNAME_TAG);
                    if (actionName != null){
                        Vector<IXMLElement> params = action.getChildrenNamed("param");
                        PanelActionConfiguration config = new PanelActionConfiguration();
                        
                        for(IXMLElement param : params){                            
                            IXMLElement keyElement = param.getFirstChildNamed("key");
                            IXMLElement valueElement = param.getFirstChildNamed("value");
                            if ((keyElement != null) && (valueElement != null)){
                                Debug.trace("Adding configuration property " + keyElement.getContent() + " with value " + valueElement.getContent() + " for action " + actionName);
                                config.addProperty(keyElement.getContent(),valueElement.getContent());
                            }                            
                        }                                                
                        panel.putPanelActionConfiguration(actionName, config);
                    }
                    try
                    {
                        ActionStage actionStage = ActionStage.valueOf(stage);
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
                        parseError(action, "Invalid value [" + stage + "] for attribute : "
                                + PanelAction.PANEL_ACTION_STAGE_TAG);
                    }
                }                   
            }
            else
            {
                parseError(xmlActions, "<" + PanelAction.PANEL_ACTIONS_TAG + "> requires a <"
                        + PanelAction.PANEL_ACTION_TAG + ">");
            }
        }
         }

}

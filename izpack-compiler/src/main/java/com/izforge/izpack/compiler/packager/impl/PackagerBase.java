/*
 * $Id:$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Klaus Bartz
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

package com.izforge.izpack.compiler.packager.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
import java.util.zip.ZipInputStream;

import com.izforge.izpack.api.data.DynamicInstallerRequirementValidator;
import com.izforge.izpack.api.data.DynamicVariable;
import com.izforge.izpack.api.data.GUIPrefs;
import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.InstallerRequirement;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.compiler.compressor.PackCompressor;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.listener.PackagerListener;
import com.izforge.izpack.compiler.merge.CompilerPathResolver;
import com.izforge.izpack.compiler.merge.PanelMerge;
import com.izforge.izpack.compiler.packager.IPackager;
import com.izforge.izpack.compiler.stream.JarOutputStream;
import com.izforge.izpack.data.CustomData;
import com.izforge.izpack.data.PackInfo;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.util.FileUtil;
import com.izforge.izpack.util.IoHelper;

/**
 * The packager base class. The packager interface <code>IPackager</code> is used by the compiler to put files into an installer, and
 * create the actual installer files. The packager implementation depends on different requirements (e.g. normal packager versus multi volume packager).
 * This class implements the common used method which can also be overload as needed.
 *
 * @author Klaus Bartz
 */
public abstract class PackagerBase implements IPackager
{

    /**
     * Path to resources in jar
     */
    public static final String RESOURCES_PATH = "resources/";

    /**
     * Variables.
     */
    private final Properties properties;

    /**
     * The listeners.
     */
    private final PackagerListener listener;

    /**
     * Executable zipped output stream. First to open, last to close.
     * Attention! This is our own JarOutputStream, not the java standard!
     */
    private final JarOutputStream installerJar;

    /**
     * The merge manager.
     */
    private final MergeManager mergeManager;

    /**
     * The path resolver.
     */
    private final CompilerPathResolver pathResolver;

    /**
     * The mergeable resolver.
     */
    private final MergeableResolver mergeableResolver;

    /**
     * The compression format to be used for pack compression.
     */
    private final PackCompressor compressor;

    /**
     * The compiler data.
     */
    private final CompilerData compilerData;

    /**
     * Installer requirements.
     */
    private List<InstallerRequirement> installerRequirements;

    /**
     * Basic installer info.
     */
    private Info info;

    /**
     * GUI preferences.
     */
    private GUIPrefs guiPrefs;

    /**
     * The ordered panels.
     */
    protected List<Panel> panelList = new ArrayList<Panel>();

    /**
     * The ordered pack information.
     */
    private final List<PackInfo> packsList = new ArrayList<PackInfo>();

    /**
     * The ordered language pack locale names.
     */
    private List<String> langpackNameList = new ArrayList<String>();

    /**
     * The ordered custom actions information.
     */
    private List<CustomData> customDataList = new ArrayList<CustomData>();

    /**
     * The language pack URLs keyed by locale name (e.g. de_CH).
     */
    private final Map<String, URL> installerResourceURLMap = new HashMap<String, URL>();

    /**
     * The conditions.
     */
    private final Map<String, Condition> rules = new HashMap<String, Condition>();

    /**
     * Dynamic variables.
     */
    private final Map<String, List<DynamicVariable>> dynamicVariables = new HashMap<String, List<DynamicVariable>>();

    /**
     * Dynamic conditions.
     */
    private List<DynamicInstallerRequirementValidator> dynamicInstallerRequirements =
            new ArrayList<DynamicInstallerRequirementValidator>();

    /**
     * Jar file URLs who's contents will be copied into the installer.
     */
    private Set<Object[]> includedJarURLs = new HashSet<Object[]>();

    /**
     * Tracks files which are already written into the container file.
     */
    private Map<FilterOutputStream, Set<String>> alreadyWrittenFiles = new HashMap<FilterOutputStream, Set<String>>();


    /**
     * Constructs a <tt>PackagerBase</tt>.
     *
     * @param properties        the properties
     * @param listener          the packager listener
     * @param installerJar      the installer jar output stream
     * @param mergeManager      the merge manager
     * @param pathResolver      the path resolver
     * @param mergeableResolver the mergeable resolver
     * @param compressor        the pack compressor
     * @param compilerData      the compiler data
     */
    public PackagerBase(Properties properties, PackagerListener listener, JarOutputStream installerJar,
                        MergeManager mergeManager, CompilerPathResolver pathResolver,
                        MergeableResolver mergeableResolver, PackCompressor compressor, CompilerData compilerData)
    {
        this.properties = properties;
        this.listener = listener;
        this.installerJar = installerJar;
        this.mergeManager = mergeManager;
        this.pathResolver = pathResolver;
        this.mergeableResolver = mergeableResolver;
        this.compressor = compressor;
        this.compilerData = compilerData;
    }

    @Override
    public void addCustomJar(CustomData ca, URL url)
    {
        if (ca != null)
        {
            customDataList.add(ca); // serialized to keep order/variables correct
        }

        if (url != null)
        {
            addJarContent(url); // each included once, no matter how many times added
        }
    }

    @Override
    public void addJarContent(URL jarURL)
    {
        sendMsg("Adding content of jar: " + jarURL.getFile(), PackagerListener.MSG_VERBOSE);
        mergeManager.addResourceToMerge(mergeableResolver.getMergeableFromURL(jarURL));
    }

    @Override
    public void addLangPack(String iso3, URL xmlURL, URL flagURL)
    {
        sendMsg("Adding langpack: " + iso3, PackagerListener.MSG_VERBOSE);
        // put data & flag as entries in installer, and keep array of iso3's
        // names
        langpackNameList.add(iso3);
        addResource("flag." + iso3, flagURL);
        installerResourceURLMap.put("langpacks/" + iso3 + ".xml", xmlURL);
    }

    @Override
    public void addNativeLibrary(String name, URL url)
    {
        sendMsg("Adding native library: " + name, PackagerListener.MSG_VERBOSE);
        installerResourceURLMap.put("native/" + name, url);
    }


    @Override
    public void addNativeUninstallerLibrary(CustomData data)
    {
        customDataList.add(data); // serialized to keep order/variables
        // correct

    }

    @Override
    public void addPack(PackInfo pack)
    {
        packsList.add(pack);
    }

    @Override
    public void addPanel(Panel panel)
    {
        sendMsg("Adding panel: " + panel.getPanelId() + " :: Classname : " + panel.getClassName());
        panelList.add(panel); // serialized to keep order/variables correct
        PanelMerge mergeable = pathResolver.getPanelMerge(panel.getClassName());
        mergeManager.addResourceToMerge(mergeable);
    }

    @Override
    public void addResource(String resId, URL url)
    {
        sendMsg("Adding resource: " + resId, PackagerListener.MSG_VERBOSE);
        installerResourceURLMap.put(resId, url);
    }

    @Override
    public List<PackInfo> getPacksList()
    {
        return packsList;
    }

    @Override
    public Properties getVariables()
    {
        return properties;
    }

    @Override
    public void setGUIPrefs(GUIPrefs prefs)
    {
        sendMsg("Setting the GUI preferences", PackagerListener.MSG_VERBOSE);
        guiPrefs = prefs;
    }

    @Override
    public void setInfo(Info info)
    {
        sendMsg("Setting the installer information", PackagerListener.MSG_VERBOSE);
        this.info = info;

        if (!compressor.useStandardCompression() && compressor.getDecoderMapperName() != null)
        {
            this.info.setPackDecoderClassName(compressor.getDecoderMapperName());
        }
    }

    public Info getInfo()
    {
        return info;
    }

    /**
     * @return the rules
     */
    @Override
    public Map<String, Condition> getRules()
    {
        return this.rules;
    }

    /**
     * @return the dynamic variables
     */
    @Override
    public Map<String, List<DynamicVariable>> getDynamicVariables()
    {
        return dynamicVariables;
    }

    /**
     * @return the dynamic conditions
     */
    @Override
    public List<DynamicInstallerRequirementValidator> getDynamicInstallerRequirements()
    {
        return dynamicInstallerRequirements;
    }

    @Override
    public void addInstallerRequirements(List<InstallerRequirement> conditions)
    {
        this.installerRequirements = conditions;
    }

    @Override
    public void createInstaller() throws Exception
    {
        // preliminary work
        info.setInstallerBase(compilerData.getOutput().replaceAll(".jar", ""));

        sendStart();

        writeInstaller();

        // Finish up. closeAlways is a hack for pack compressions other than
        // default. Some of it (e.g. BZip2) closes the slave of it also.
        // But this should not be because the jar stream should be open
        // for the next pack. Therefore an own JarOutputStream will be used
        // which close method will be blocked.
        getInstallerJar().closeAlways();

        sendStop();
    }

    /**
     * Determines if each pack is to be included in a separate jar.
     *
     * @return <tt>true</tt> if {@link Info#getWebDirURL()} is non-null
     */
    protected boolean packSeparateJars()
    {
        return info != null && info.getWebDirURL() != null;
    }

    /**
     * Writes the installer.
     *
     * @throws IOException for any I/O error
     */
    protected void writeInstaller() throws IOException
    {
        // write the installer jar. MUST be first so manifest is not overwritten by an included jar
        writeManifest();
        writeSkeletonInstaller();

        writeInstallerObject("info", info);
        writeInstallerObject("vars", properties);
        writeInstallerObject("GUIPrefs", guiPrefs);
        writeInstallerObject("panelsOrder", panelList);
        writeInstallerObject("customData", customDataList);
        writeInstallerObject("langpacks.info", langpackNameList);
        writeInstallerObject("rules", rules);
        writeInstallerObject("dynvariables", dynamicVariables);
        writeInstallerObject("dynconditions", dynamicInstallerRequirements);
        writeInstallerObject("installerrequirements", installerRequirements);

        writeInstallerResources();
        writeIncludedJars();

        // Pack File Data may be written to separate jars
        writePacks();
    }

    /**
     * Write manifest in the install jar.
     *
     * @throws IOException for any I/O error
     */
    protected void writeManifest() throws IOException
    {
        Manifest manifest = new Manifest(PackagerBase.class.getResourceAsStream("MANIFEST.MF"));
        File tempManifest = com.izforge.izpack.util.file.FileUtils.createTempFile("MANIFEST", ".MF");
        manifest.write(new FileOutputStream(tempManifest));
        mergeManager.addResourceToMerge(tempManifest.getAbsolutePath(), "META-INF/MANIFEST.MF");
    }

    /**
     * Write skeleton installer to the installer jar.
     *
     * @throws IOException for any I/O error
     */
    protected void writeSkeletonInstaller() throws IOException
    {
        sendMsg("Copying the skeleton installer", PackagerListener.MSG_VERBOSE);
        mergeManager.addResourceToMerge("com/izforge/izpack/installer/");
        mergeManager.addResourceToMerge("org/picocontainer/");
        mergeManager.addResourceToMerge("com/izforge/izpack/img/");
        mergeManager.addResourceToMerge("com/izforge/izpack/bin/");
        mergeManager.addResourceToMerge("com/izforge/izpack/api/");
        mergeManager.addResourceToMerge("com/izforge/izpack/event/");
        mergeManager.addResourceToMerge("com/izforge/izpack/core/");
        mergeManager.addResourceToMerge("com/izforge/izpack/data/");
        mergeManager.addResourceToMerge("com/izforge/izpack/gui/");
        mergeManager.addResourceToMerge("com/izforge/izpack/merge/");
        mergeManager.addResourceToMerge("com/izforge/izpack/util/");
        mergeManager.addResourceToMerge("org/apache/regexp/");
        mergeManager.addResourceToMerge("com/coi/tools/");
        mergeManager.addResourceToMerge("org/apache/tools/zip/");
        mergeManager.addResourceToMerge("org/apache/commons/io/FilenameUtils.class");
        mergeManager.addResourceToMerge("jline/");
        mergeManager.addResourceToMerge("org/fusesource/jansi/");
        mergeManager.addResourceToMerge("META-INF/native/");
        mergeManager.merge(installerJar);
    }

    /**
     * Write an arbitrary object to installer jar.
     *
     * @throws IOException for any I/O error
     */
    protected void writeInstallerObject(String entryName, Object object) throws IOException
    {
        installerJar.putNextEntry(new org.apache.tools.zip.ZipEntry(RESOURCES_PATH + entryName));
        ObjectOutputStream out = new ObjectOutputStream(installerJar);
        try
        {
            out.writeObject(object);
        }
        catch (IOException e)
        {
            throw new IOException("Error serializing instance of " + object.getClass().getName()
                                          + " as entry \"" + entryName + "\"", e);
        }
        finally
        {
            out.flush();
            installerJar.closeEntry();
        }
    }

    /**
     * Write the data referenced by URL to installer jar.
     *
     * @throws IOException for any I/O error
     */
    protected void writeInstallerResources() throws IOException
    {
        sendMsg("Copying " + installerResourceURLMap.size() + " files into installer");

        for (Map.Entry<String, URL> stringURLEntry : installerResourceURLMap.entrySet())
        {
            URL url = stringURLEntry.getValue();
            InputStream in = url.openStream();

            org.apache.tools.zip.ZipEntry newEntry = new org.apache.tools.zip.ZipEntry(
                    RESOURCES_PATH + stringURLEntry.getKey());
            long dateTime = FileUtil.getFileDateTime(url);
            if (dateTime != -1)
            {
                newEntry.setTime(dateTime);
            }
            installerJar.putNextEntry(newEntry);

            IoHelper.copyStream(in, installerJar);
            installerJar.closeEntry();
            in.close();
        }
    }

    /**
     * Copy included jars to installer jar.
     *
     * @throws IOException for any I/O error
     */
    protected void writeIncludedJars() throws IOException
    {
        sendMsg("Merging " + includedJarURLs.size() + " jars into installer");

        for (Object[] includedJarURL : includedJarURLs)
        {
            InputStream is = ((URL) includedJarURL[0]).openStream();
            ZipInputStream inJarStream = new ZipInputStream(is);
            IoHelper.copyZip(inJarStream, installerJar, (List<String>) includedJarURL[1], alreadyWrittenFiles);
        }
    }

    /**
     * Write packs to the installer jar, or each to a separate jar.
     *
     * @throws IOException for any I/O error
     */
    protected abstract void writePacks() throws IOException;

    /**
     * Returns the installer jar stream.
     *
     * @return the installer jar stream
     */
    protected JarOutputStream getInstallerJar()
    {
        return installerJar;
    }

    /**
     * Returns the pack compressor.
     *
     * @return the pack compressor
     */
    protected PackCompressor getCompressor()
    {
        return compressor;
    }

    /**
     * Dispatches a message to the listeners.
     *
     * @param job the job description.
     */
    protected void sendMsg(String job)
    {
        sendMsg(job, PackagerListener.MSG_INFO);
    }

    /**
     * Dispatches a message to the listeners at specified priority.
     *
     * @param job      the job description.
     * @param priority the message priority.
     */
    protected void sendMsg(String job, int priority)
    {
        if (listener != null)
        {
            listener.packagerMsg(job, priority);
        }
    }

    /**
     * Dispatches a start event to the listeners.
     */
    protected void sendStart()
    {
        if (listener != null)
        {
            listener.packagerStart();
        }
    }

    /**
     * Dispatches a stop event to the listeners.
     */
    protected void sendStop()
    {
        if (listener != null)
        {
            listener.packagerStop();
        }
    }

}

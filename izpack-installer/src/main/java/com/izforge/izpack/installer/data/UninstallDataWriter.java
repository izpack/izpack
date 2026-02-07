package com.izforge.izpack.installer.data;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.merge.MergeTarget;
import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.data.CustomData;
import com.izforge.izpack.api.data.ExecutableFile;
import com.izforge.izpack.merge.resolve.PathResolver;
import com.izforge.izpack.util.IoHelper;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

/**
 * Writes uninstall data to an executable jar file.
 */
public class UninstallDataWriter
{
    /**
     * Uninstall data.
     */
    private final UninstallData uninstallData;

    /**
     * Install data.
     */
    private final AutomatedInstallData installData;

    /**
     * The path resolver.
     */
    private final PathResolver pathResolver;

    /**
     * The jar to write to.
     */
    private JarOutputStream jar;
    private MergeTarget mergeTarget;

    /**
     * The underlying jar file stream.
     */
    private FileOutputStream jarStream;

    /**
     * The rules engine.
     */
    private final RulesEngine rules;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(UninstallDataWriter.class.getName());

    /**
     * Log file path variable name.
     */
    private static final String LOGFILE_PATH = "InstallerFrame.logfilePath";

    /**
     * Constructs an <tt>UninstallDataWriter</tt>.
     *
     * @param uninstallData the uninstall data
     * @param installData   the install data
     * @param pathResolver  the path resolver
     * @param rules         the rules engine
     */
    public UninstallDataWriter(UninstallData uninstallData, AutomatedInstallData installData, PathResolver pathResolver,
                               RulesEngine rules)
    {
        this.uninstallData = uninstallData;
        this.installData = installData;
        this.pathResolver = pathResolver;
        this.rules = rules;
    }

    /**
     * Determines if uninstall data should be written.
     * <p/>
     * Uninstall data should be written if {@link com.izforge.izpack.api.data.Info#getUninstallerCondition()} is
     * empty, or evaluates <tt>true</tt>.
     *
     * @return <tt>true</tt> if uninstall data should be written, otherwise <tt>false</tt>
     */
    public boolean isUninstallRequired()
    {
        // if install path is not available then license panel could be the first panel and quit button was pressed or
        // in any case when the install-path is not available we can't write uninstall data
        if (installData.getInstallPath() == null)
        {
            return false;
        }
        String condition = installData.getInfo().getUninstallerCondition();

        return (installData.getInfo().getUninstallerPath() != null)
                && (condition == null || condition.isEmpty() || rules.isConditionTrue(condition)
        );
    }

    /**
     * Writes the uninstall data.
     *
     * @return <tt>true</tt> if uninstall data was successfully written, otherwise <tt>false</tt>
     */
    public boolean write()
    {
        boolean result = false;
        try
        {
            BufferedWriter extLogWriter = getExternalLogFile();
            createOutputJar();

            System.out.println("[ Writing the uninstaller data ... ]");

            writeJarSkeleton();
            writeFilesLog(extLogWriter);
            writeUninstallerJarFileLog();
            writeExecutables();
            writeUninstallerListeners();
            writeJars();
            writeNativeLibraries();
            writeAdditionalUninstallData();
            writeScriptFiles();
            writeHideForceOption();

            jar.close();
            result = true;
        }
        catch (Throwable t)
        {
            logger.log(Level.SEVERE, t.getMessage(), t);
            destroyJar(); // don't keep the jar - it may be incomplete or corrupted
        }
        return result;
    }

    /**
     * Determines if an external log file should be written.
     *
     * @return the external log file writer, or <tt>null</tt> if none should be written or the file couldn't be created
     */
    private BufferedWriter getExternalLogFile()
    {
        String logfile = installData.getVariable(LOGFILE_PATH);
        BufferedWriter writer = null;
        if (logfile != null)
        {
            if (logfile.toLowerCase().startsWith("default"))
            {
                logfile = installData.getInfo().getUninstallerPath() + "/install.log";
            }
            logfile = IoHelper.translatePath(logfile, installData.getVariables());
            File outFile = new File(logfile);
            if (!outFile.getParentFile().exists())
            {
                if (!outFile.getParentFile().mkdirs())
                {
                    logger.warning("Failed to create directory: " + outFile.getParentFile().getPath());
                }
            }
            FileOutputStream out = null;
            try
            {
                out = new FileOutputStream(outFile);
            }
            catch (FileNotFoundException e)
            {
                logger.log(Level.WARNING, "Cannot create logfile", e);
            }
            if (out != null)
            {
                writer = new BufferedWriter(new OutputStreamWriter(out));
            }
        }

        return writer;
    }

    /**
     * Writes the uninstaller skeleton.
     *
     * @throws IOException for any I/O error
     * @throws com.izforge.izpack.api.exception.IzPackException
     *                     for any IzPack error
     */
    private void writeJarSkeleton() throws IOException
    {
        List<Mergeable> uninstallerMerge = pathResolver.getMergeableFromPath("com/izforge/izpack/uninstaller/");
        uninstallerMerge.addAll(pathResolver.getMergeableFromPath("uninstaller-META-INF/", "META-INF/"));
        uninstallerMerge.addAll(pathResolver.getMergeableFromPath("com/izforge/izpack/api/"));
        uninstallerMerge.addAll(pathResolver.getMergeableFromPath("com/izforge/izpack/data/"));
        uninstallerMerge.addAll(pathResolver.getMergeableFromPath("com/izforge/izpack/core/"));
        uninstallerMerge.addAll(pathResolver.getMergeableFromPath("com/izforge/izpack/util/"));
        uninstallerMerge.addAll(pathResolver.getMergeableFromPath("com/izforge/izpack/logging/"));
        uninstallerMerge.addAll(pathResolver.getMergeableFromPath("com/izforge/izpack/gui/"));
        uninstallerMerge.addAll(pathResolver.getMergeableFromPath("com/izforge/izpack/img/"));
        uninstallerMerge.addAll(pathResolver.getMergeableFromPath("org/picocontainer/"));
        uninstallerMerge.addAll(pathResolver.getMergeableFromPath("org/apache/commons/io/"));

        //required by console uninstaller
        uninstallerMerge.addAll(pathResolver.getMergeableFromPath("jline/"));
        uninstallerMerge.addAll(pathResolver.getMergeableFromPath("org/fusesource/"));

        if (!uninstallData.getUninstallerListeners().isEmpty())
        {
            uninstallerMerge.addAll(pathResolver.getMergeableFromPath("com/izforge/izpack/event/"));
        }
        if (rules.isConditionTrue("izpack.windowsinstall"))
        {
            uninstallerMerge.addAll(pathResolver.getMergeableFromPath("com/izforge/izpack/core/os/"));
            uninstallerMerge.addAll(pathResolver.getMergeableFromPath("com/coi/tools/os/"));
        }

        for (Mergeable mergeable : uninstallerMerge)
        {
            mergeable.merge(mergeTarget);
        }

        if (installData.getInfo().isPrivilegedExecutionRequiredUninstaller())
        {
            boolean shouldElevate = true;
            String conditionId = installData.getInfo().getPrivilegedExecutionConditionID();
            if (conditionId != null)
            {
                // only elevate permissions when condition is true
                shouldElevate = rules.isConditionTrue(conditionId);
            }
            if (shouldElevate)
            {
                // Add resources required to elevate privileges
                mergeTarget.offer("exec-admin", -1, this::noOp);

                if (rules.isConditionTrue("izpack.windowsinstall"))
                {
                    writeResource("com/izforge/izpack/util/windows/elevate.js");
                }
                if (rules.isConditionTrue("izpack.macinstall"))
                {
                    writeResource("com/izforge/izpack/util/mac/run-with-privileges-on-osx");
                }
            }
        }

        // We put the langpack
        List<Mergeable> langPack = pathResolver.getMergeableFromPath("resources/langpacks/"
                                                                             + installData.getLocaleISO3() + ".xml",
                                                                     "langpack.xml");
        for (Mergeable mergeable : langPack)
        {
            mergeable.merge(mergeTarget);
        }
    }

    private void noOp(OutputStream outputStream)
    {
        // does nothing
    }

    /**
     * Writes the file log.
     *
     * @param extLogWriter the external log writer. May be <tt>null</tt>
     * @throws IOException for any I/O error
     */
    private void writeFilesLog(BufferedWriter extLogWriter) throws IOException
    {
        List<String> files = uninstallData.getUninstalableFilesList();

        mergeTarget.offer("install.log", -1, out ->
        {
            BufferedWriter logWriter = new BufferedWriter(new OutputStreamWriter(out));
            logWriter.write(installData.getInstallPath());
            logWriter.newLine();
            Iterator<String> iter = files.iterator();
            if (extLogWriter != null)
            {
                // Write intern (in uninstaller.jar) and extern log file.
                while (iter.hasNext())
                {
                    String txt = iter.next();
                    logWriter.write(txt);
                    extLogWriter.write(txt);
                    if (iter.hasNext())
                    {
                        logWriter.newLine();
                        extLogWriter.newLine();
                    }
                }
                logWriter.flush();
                extLogWriter.flush();
                extLogWriter.close();
            }
            else
            {
                while (iter.hasNext())
                {
                    String txt = iter.next();
                    logWriter.write(txt);
                    if (iter.hasNext())
                    {
                        logWriter.newLine();
                    }
                }
                logWriter.flush();
            }
        });
    }

    /**
     * Writes executables to execute on uninstall.
     *
     * @throws IOException for any I/O error
     */
    private void writeExecutables() throws IOException
    {
        mergeTarget.offer("executables", -1, out ->
        {
            ObjectOutputStream execStream = new ObjectOutputStream(out);
            execStream.writeInt(uninstallData.getExecutablesList().size());
            for (ExecutableFile file : uninstallData.getExecutablesList())
            {
                execStream.writeObject(file);
            }
            execStream.flush();
        });
    }

    /**
     * Writes the uinstaller jar file log.
     *
     * @throws IOException for any I/O error
     */
    private void writeUninstallerJarFileLog() throws IOException
    {
        mergeTarget.offer("jarlocation.log", 1, out ->
        {
            BufferedWriter logWriter = new BufferedWriter(new OutputStreamWriter(out));
            logWriter.write(uninstallData.getUninstallerJarFilename());
            logWriter.newLine();
            logWriter.write(uninstallData.getUninstallerPath());
            logWriter.flush();
        });
    }

    /**
     * Writes uninstaller listeners.
     *
     * @throws IOException for any I/O error
     */
    private void writeUninstallerListeners() throws IOException
    {
        mergeTarget.offer("uninstallerListeners", -1, out ->
        {
            ArrayList<String> listeners = new ArrayList<>();
            writeCustomDataResources(uninstallData.getUninstallerListeners());
            for (CustomData data : uninstallData.getUninstallerListeners())
            {
                if (data.listenerName != null)
                {
                    listeners.add(data.listenerName);
                }
            }
            ObjectOutputStream stream = new ObjectOutputStream(out);
            stream.writeObject(listeners);
            stream.flush();
        });
    }

    /**
     * Writes uninstaller jars.
     *
     * @throws IOException for any I/O error
     */
    private void writeJars() throws IOException
    {
        writeCustomDataResources(uninstallData.getJars());
    }

    /**
     * Writes native libraries.
     */
    private void writeNativeLibraries()
    {
        for (String path : uninstallData.getNativeLibraries())
        {
            writeResource(path);
        }
    }

    /**
     * Writes data from {@link com.izforge.izpack.installer.data.UninstallData#getAdditionalData()}.
     * <p/>
     * This silently ignores failures to locate custom resources, logging it instead.
     *
     * @throws IOException for any I/O error
     */
    private void writeAdditionalUninstallData() throws IOException
    {
        Map<String, Object> additionalData = uninstallData.getAdditionalData();
        if (additionalData != null && !additionalData.isEmpty())
        {
            for (Entry<String, Object> entry : additionalData.entrySet())
            {
                writeContent(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Writes uninstall scripts.
     *
     * @throws IOException for any I/O error
     */
    private void writeScriptFiles() throws IOException
    {
        ArrayList<String> unInstallScripts = uninstallData.getUninstallScripts();
        int idx = 0;
        for (String unInstallScript : unInstallScripts)
        {
            mergeTarget.offer(UninstallData.ROOTSCRIPT + Integer.toString(idx), -1, out ->
            {
                ObjectOutputStream rootStream = new ObjectOutputStream(out);
                rootStream.writeUTF(unInstallScript);
                rootStream.flush();
            });
            idx++;
        }
    }

    /**
     * Writes hide-force-option entry if specified.
     *
     * @throws IOException for any I/O error
     */
    private void writeHideForceOption() throws IOException
    {
        if (installData.getInfo().getHideForceOption())
        {
            // we add this entry only if user wants hide force option
            // just entry is enough for us to check whether the user has specified
            mergeTarget.offer("hide-force-option", -1, this::noOp);
        }
    }

    /**
     * Writes the resources referenced by {@link CustomData#contents}.
     *
     * @param customData the custom data to write
     */
    private void writeCustomDataResources(List<CustomData> customData)
    {
        for (CustomData data : customData)
        {
            if (data.contents != null)
            {
                for (String content : data.contents)
                {
                    writeResource(content);
                }
            }
        }
    }

    /**
     * Writes a resource to the jar, if it is not already present.
     *
     * @param path the resource path
     */
    private void writeResource(String path)
    {
        for (Mergeable mergeable : pathResolver.getMergeableFromPath(path))
        {
            mergeable.merge(mergeTarget);
        }
    }

    /**
     * Writes content to the jar.
     *
     * @param path    the path to write to
     * @param content the content to write. May be an object or a <tt>ByteArrayOutputStream</tt>. No idea why for the latter. TODO
     * @throws IOException for any I/O error
     */
    private void writeContent(String path, Object content) throws IOException
    {
        mergeTarget.offer(path, -1, out ->
        {
            if (content instanceof ByteArrayOutputStream)
            {
                ((ByteArrayOutputStream) content).writeTo(out);
            }
            else
            {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
                objectOutputStream.writeObject(content);
                objectOutputStream.flush();
            }
        });
    }

    /**
     * Creates the uninstaller jar file.
     *
     * @throws IOException for any I/O error
     */
    private void createOutputJar() throws IOException
    {
        // Create the uninstaller directory
        String dirPath = IoHelper.translatePath(installData.getInfo().getUninstallerPath(), installData.getVariables());
        String jarPath = dirPath + File.separator + installData.getInfo().getUninstallerName();
        File dir = new File(dirPath);
        if (!dir.exists() && !dir.mkdirs())
        {
            throw new IOException("Failed to create output path: " + dir);
        }

        // Log the uninstaller deletion information
        uninstallData.setUninstallerJarFilename(jarPath);
        uninstallData.setUninstallerPath(dirPath);

        // Create the jar file
        jarStream = new FileOutputStream(jarPath);

        final Set<String> entries = new HashSet<>();
        jar = new JarOutputStream(new BufferedOutputStream(jarStream)) {
            @Override
            public void putNextEntry(ZipEntry ze) throws IOException {
                if (entries.add(ze.getName())) {
                    super.putNextEntry(ze);
                    return;
                }
                throw new IOException("Duplicate entry: " + ze.getName());
            }
        };
        mergeTarget = IoHelper.mergeTarget(jar);
        jar.setLevel(9);
        uninstallData.addFile(jarPath, true);
    }

    /**
     * Destroys the uninstaller jar when it cannot be written.
     */
    private void destroyJar()
    {
        IOUtils.closeQuietly(jar);
        IOUtils.closeQuietly(jarStream); // if jar cannot be closed, then need to close underlying stream
        String path = uninstallData.getUninstallerJarFilename();
        if (path != null)
        {
            File file = new File(path);
            if (file.exists() && !file.delete())
            {
                logger.warning("Failed to delete incomplete uninstall information: " + path);
            }
        }
    }

}

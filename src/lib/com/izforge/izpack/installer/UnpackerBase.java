/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil
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

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.Pack;
import com.izforge.izpack.PackFile;
import com.izforge.izpack.UpdateCheck;
import com.izforge.izpack.event.InstallerListener;
import com.izforge.izpack.rules.RulesEngine;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.VariableSubstitutor;
import org.apache.regexp.RE;
import org.apache.regexp.RECompiler;
import org.apache.regexp.RESyntaxException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Abstract base class for all unpacker implementations.
 *
 * @author Dennis Reil, <izpack@reil-online.de>
 */
public abstract class UnpackerBase implements IUnpacker
{
    /**
     * The installdata.
     */
    protected AutomatedInstallData idata;

    /**
     * The installer listener.
     */
    protected AbstractUIProgressHandler handler;

    /**
     * The uninstallation data.
     */
    protected UninstallData udata;

    /**
     * The variables substitutor.
     */
    protected VariableSubstitutor vs;

    /**
     * The absolute path of the installation. (NOT the canonical!)
     */
    protected File absolute_installpath;

    /**
     * The absolute path of the source installation jar.
     */
    private File absolutInstallSource;

    /**
     * The packs locale database.
     */
    protected LocaleDatabase langpack = null;

    /**
     * The result of the operation.
     */
    protected boolean result = true;

    /**
     * The instances of the unpacker objects.
     */
    protected static HashMap<Object, String> instances = new HashMap<Object, String>();

    /**
     * Interrupt flag if global interrupt is desired.
     */
    protected static boolean interruptDesired = false;

    /**
     * Do not perform a interrupt call.
     */
    protected static boolean discardInterrupt = false;

    /**
     * The name of the XML file that specifies the panel langpack
     */
    protected static final String LANG_FILE_NAME = "packsLang.xml";

    public static final String ALIVE = "alive";

    public static final String INTERRUPT = "doInterrupt";

    public static final String INTERRUPTED = "interruppted";

    protected RulesEngine rules;

    /**
     * The constructor.
     *
     * @param idata   The installation data.
     * @param handler The installation progress handler.
     */
    public UnpackerBase(AutomatedInstallData idata, AbstractUIProgressHandler handler)
    {
        try
        {
            String resource = LANG_FILE_NAME + "_" + idata.localeISO3;
            this.langpack = new LocaleDatabase(ResourceManager.getInstance().getInputStream(resource));
        }
        catch (Throwable exception)
        {
        }

        this.idata = idata;
        this.handler = handler;

        // Initialize the variable substitutor
        vs = new VariableSubstitutor(idata.getVariables());
    }

    public void setRules(RulesEngine rules)
    {
        this.rules = rules;
    }

    /**
     * Returns a copy of the active unpacker instances.
     *
     * @return a copy of active unpacker instances
     */
    public static HashMap getRunningInstances()
    {
        synchronized (instances)
        { // Return a shallow copy to prevent a
            // ConcurrentModificationException.
            return (HashMap) (instances.clone());
        }
    }

    /**
     * Adds this to the map of all existent instances of Unpacker.
     */
    protected void addToInstances()
    {
        synchronized (instances)
        {
            instances.put(this, ALIVE);
        }
    }

    /**
     * Removes this from the map of all existent instances of Unpacker.
     */
    protected void removeFromInstances()
    {
        synchronized (instances)
        {
            instances.remove(this);
        }
    }

    /**
     * Initiate interrupt of all alive Unpacker. This method does not interrupt the Unpacker objects
     * else it sets only the interrupt flag for the Unpacker objects. The dispatching of interrupt
     * will be performed by the Unpacker objects self.
     */
    private static void setInterruptAll()
    {
        synchronized (instances)
        {
            Iterator iter = instances.keySet().iterator();
            while (iter.hasNext())
            {
                Object key = iter.next();
                if (instances.get(key).equals(ALIVE))
                {
                    instances.put(key, INTERRUPT);
                }
            }
            // Set global flag to allow detection of it in other classes.
            // Do not set it to thread because an exec will then be stoped.
            setInterruptDesired(true);
        }
    }

    /**
     * Initiate interrupt of all alive Unpacker and waits until all Unpacker are interrupted or the
     * wait time has arrived. If the doNotInterrupt flag in InstallerListener is set to true, the
     * interrupt will be discarded.
     *
     * @param waitTime wait time in millisecounds
     * @return true if the interrupt will be performed, false if the interrupt will be discarded
     */
    public static boolean interruptAll(long waitTime)
    {
        long t0 = System.currentTimeMillis();
        if (isDiscardInterrupt())
        {
            return (false);
        }
        setInterruptAll();
        while (!isInterruptReady())
        {
            if (System.currentTimeMillis() - t0 > waitTime)
            {
                return (true);
            }
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
            }
        }
        return (true);
    }

    private static boolean isInterruptReady()
    {
        synchronized (instances)
        {
            Iterator iter = instances.keySet().iterator();
            while (iter.hasNext())
            {
                Object key = iter.next();
                if (!instances.get(key).equals(INTERRUPTED))
                {
                    return (false);
                }
            }
            return (true);
        }

    }

    /**
     * Sets the interrupt flag for this Unpacker to INTERRUPTED if the previos state was INTERRUPT
     * or INTERRUPTED and returns whether interrupt was initiate or not.
     *
     * @return whether interrupt was initiate or not
     */
    protected boolean performInterrupted()
    {
        synchronized (instances)
        {
            Object doIt = instances.get(this);
            if (doIt != null && (doIt.equals(INTERRUPT) || doIt.equals(INTERRUPTED)))
            {
                instances.put(this, INTERRUPTED);
                this.result = false;
                return (true);
            }
            return (false);
        }
    }

    /**
     * Returns whether interrupt was initiate or not for this Unpacker.
     *
     * @return whether interrupt was initiate or not
     */
    private boolean shouldInterrupt()
    {
        synchronized (instances)
        {
            Object doIt = instances.get(this);
            if (doIt != null && (doIt.equals(INTERRUPT) || doIt.equals(INTERRUPTED)))
            {
                return (true);
            }
            return (false);
        }

    }

    /**
     * Return the state of the operation.
     *
     * @return true if the operation was successful, false otherwise.
     */
    public boolean getResult()
    {
        return this.result;
    }

    /**
     * @param filename
     * @param patterns
     * @return true if the file matched one pattern, false if it did not
     */
    private boolean fileMatchesOnePattern(String filename, ArrayList<RE> patterns)
    {
        // first check whether any include matches
        for (RE pattern : patterns)
        {
            if (pattern.match(filename))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * @param list       A list of file name patterns (in ant fileset syntax)
     * @param recompiler The regular expression compiler (used to speed up RE compiling).
     * @return List of org.apache.regexp.RE
     */
    private List<RE> preparePatterns(ArrayList<String> list, RECompiler recompiler)
    {
        ArrayList<RE> result = new ArrayList<RE>();

        for (String element : list)
        {
            if ((element != null) && (element.length() > 0))
            {
                // substitute variables in the pattern
                element = this.vs.substitute(element, "plain");

                // check whether the pattern is absolute or relative
                File f = new File(element);

                // if it is relative, make it absolute and prepend the
                // installation path
                // (this is a bit dangerous...)
                if (!f.isAbsolute())
                {
                    element = new File(this.absolute_installpath, element).toString();
                }

                // now parse the element and construct a regular expression from
                // it
                // (we have to parse it one character after the next because
                // every
                // character should only be processed once - it's not possible
                // to get this
                // correct using regular expression replacing)
                StringBuffer element_re = new StringBuffer();

                int lookahead = -1;

                int pos = 0;

                while (pos < element.length())
                {
                    char c;

                    if (lookahead != -1)
                    {
                        c = (char) lookahead;
                        lookahead = -1;
                    }
                    else
                    {
                        c = element.charAt(pos++);
                    }

                    switch (c)
                    {
                        case '/':
                        {
                            element_re.append(File.separator);
                            break;
                        }
                        // escape backslash and dot
                        case '\\':
                        case '.':
                        {
                            element_re.append("\\");
                            element_re.append(c);
                            break;
                        }
                        case '*':
                        {
                            if (pos == element.length())
                            {
                                element_re.append("[^").append(File.separator).append("]*");
                                break;
                            }

                            lookahead = element.charAt(pos++);

                            // check for "**"
                            if (lookahead == '*')
                            {
                                element_re.append(".*");
                                // consume second star
                                lookahead = -1;
                            }
                            else
                            {
                                element_re.append("[^").append(File.separator).append("]*");
                                // lookahead stays there
                            }
                            break;
                        }
                        default:
                        {
                            element_re.append(c);
                            break;
                        }
                    } // switch

                }

                // make sure that the whole expression is matched
                element_re.append('$');

                // replace \ by \\ and create a RE from the result
                try
                {
                    result.add(new RE(recompiler.compile(element_re.toString())));
                }
                catch (RESyntaxException e)
                {
                    this.handler.emitNotification("internal error: pattern \"" + element
                            + "\" produced invalid RE \"" + f.getPath() + "\"");
                }

            }
        }

        return result;
    }

    // CUSTOM ACTION STUFF -------------- start -----------------

    /**
     * Informs all listeners which would be informed at the given action type.
     *
     * @param customActions array of lists with the custom action objects
     * @param action        identifier for which callback should be called
     * @param firstParam    first parameter for the call
     * @param secondParam   second parameter for the call
     * @param thirdParam    third parameter for the call
     */
    protected void informListeners(List[] customActions, int action, Object firstParam,
                                   Object secondParam, Object thirdParam) throws Exception
    {
        List listener = null;
        // select the right action list.
        switch (action)
        {
            case InstallerListener.BEFORE_FILE:
            case InstallerListener.AFTER_FILE:
            case InstallerListener.BEFORE_DIR:
            case InstallerListener.AFTER_DIR:
                listener = customActions[customActions.length - 1];
                break;
            default:
                listener = customActions[0];
                break;
        }
        if (listener == null)
        {
            return;
        }
        // Iterate the action list.
        Iterator iter = listener.iterator();
        while (iter.hasNext())
        {
            if (shouldInterrupt())
            {
                return;
            }
            InstallerListener il = (InstallerListener) iter.next();
            switch (action)
            {
                case InstallerListener.BEFORE_FILE:
                    il.beforeFile((File) firstParam, (PackFile) secondParam);
                    break;
                case InstallerListener.AFTER_FILE:
                    il.afterFile((File) firstParam, (PackFile) secondParam);
                    break;
                case InstallerListener.BEFORE_DIR:
                    il.beforeDir((File) firstParam, (PackFile) secondParam);
                    break;
                case InstallerListener.AFTER_DIR:
                    il.afterDir((File) firstParam, (PackFile) secondParam);
                    break;
                case InstallerListener.BEFORE_PACK:
                    il.beforePack((Pack) firstParam, (Integer) secondParam,
                            (AbstractUIProgressHandler) thirdParam);
                    break;
                case InstallerListener.AFTER_PACK:
                    il.afterPack((Pack) firstParam, (Integer) secondParam,
                            (AbstractUIProgressHandler) thirdParam);
                    break;
                case InstallerListener.BEFORE_PACKS:
                    il.beforePacks((AutomatedInstallData) firstParam, (Integer) secondParam,
                            (AbstractUIProgressHandler) thirdParam);
                    break;
                case InstallerListener.AFTER_PACKS:
                    il.afterPacks((AutomatedInstallData) firstParam,
                            (AbstractUIProgressHandler) secondParam);
                    break;

            }
        }
    }

    /**
     * Returns the defined custom actions split into types including a constructed type for the file
     * related installer listeners.
     *
     * @return array of lists of custom action data like listeners
     */
    protected List[] getCustomActions()
    {
        String[] listenerNames = AutomatedInstallData.CUSTOM_ACTION_TYPES;
        List[] retval = new List[listenerNames.length + 1];
        int i;
        for (i = 0; i < listenerNames.length; ++i)
        {
            retval[i] = idata.customData.get(listenerNames[i]);
            if (retval[i] == null)
            // Make a dummy list, then iterator is ever callable.
            {
                retval[i] = new ArrayList();
            }
        }
        if (retval[AutomatedInstallData.INSTALLER_LISTENER_INDEX].size() > 0)
        { // Installer listeners exist
            // Create file related installer listener list in the last
            // element of custom action array.
            i = retval.length - 1; // Should be so, but safe is safe ...
            retval[i] = new ArrayList();
            Iterator iter = retval[AutomatedInstallData.INSTALLER_LISTENER_INDEX]
                    .iterator();
            while (iter.hasNext())
            {
                // If we get a class cast exception many is wrong and
                // we must fix it.
                InstallerListener li = (InstallerListener) iter.next();
                if (li.isFileListener())
                {
                    retval[i].add(li);
                }
            }

        }
        return (retval);
    }

    // This method is only used if a file related custom action exist.
    /**
     * Creates the given directory recursive and calls the method "afterDir" of each listener with
     * the current file object and the pack file object. On error an exception is raised.
     *
     * @param dest          the directory which should be created
     * @param pf            current pack file object
     * @param customActions all defined custom actions
     * @return false on error, true else
     * @throws Exception
     */

    protected boolean mkDirsWithEnhancement(File dest, PackFile pf, List[] customActions)
            throws Exception
    {
        String path = "unknown";
        if (dest != null)
        {
            path = dest.getAbsolutePath();
        }
        if (dest != null && !dest.exists() && dest.getParentFile() != null)
        {
            if (dest.getParentFile().exists())
            {
                informListeners(customActions, InstallerListener.BEFORE_DIR, dest, pf, null);
            }
            if (!dest.mkdir())
            {
                mkDirsWithEnhancement(dest.getParentFile(), pf, customActions);
                if (!dest.mkdir())
                {
                    dest = null;
                }
            }
            informListeners(customActions, InstallerListener.AFTER_DIR, dest, pf, null);
        }
        if (dest == null)
        {
            handler.emitError("Error creating directories", "Could not create directory\n" + path);
            handler.stopAction();
            return (false);
        }
        return (true);
    }

    // CUSTOM ACTION STUFF -------------- end -----------------

    /**
     * Returns whether an interrupt request should be discarded or not.
     *
     * @return Returns the discard interrupt flag
     */
    public static synchronized boolean isDiscardInterrupt()
    {
        return discardInterrupt;
    }

    /**
     * Sets the discard interrupt flag.
     *
     * @param di the discard interrupt flag to set
     */
    public static synchronized void setDiscardInterrupt(boolean di)
    {
        discardInterrupt = di;
        setInterruptDesired(false);
    }

    /**
     * Returns the interrupt desired state.
     *
     * @return the interrupt desired state
     */
    public static boolean isInterruptDesired()
    {
        return interruptDesired;
    }

    /**
     * @param interruptDesired The interrupt desired flag to set
     */
    private static void setInterruptDesired(boolean interruptDesired)
    {
        UnpackerBase.interruptDesired = interruptDesired;
    }

    /**
     * Puts the uninstaller.
     *
     * @throws Exception Description of the Exception
     */
    protected void putUninstaller() throws Exception
    {
        String uninstallerCondition = idata.info.getUninstallerCondition();
        if ((uninstallerCondition != null) &&
             (uninstallerCondition.length() > 0) &&
             !this.rules.isConditionTrue(uninstallerCondition)){
            Debug.log("Uninstaller has a condition (" + uninstallerCondition  + ") which is not fulfilled.");
            Debug.log("Skipping creation of uninstaller.");
            return;
        }
        // get the uninstaller base, returning if not found so that
        // idata.uninstallOutJar remains null
        InputStream[] in = new InputStream[2];
        in[0] = UnpackerBase.class.getResourceAsStream("/res/IzPack.uninstaller");
        if (in[0] == null)
        {
            return;
        }
        // The uninstaller extension is facultative; it will be exist only
        // if a native library was marked for uninstallation.
        in[1] = UnpackerBase.class.getResourceAsStream("/res/IzPack.uninstaller-ext");

        // Me make the .uninstaller directory
        String dest = IoHelper.translatePath(idata.info.getUninstallerPath(), vs);
        String jar = dest + File.separator + idata.info.getUninstallerName();
        File pathMaker = new File(dest);
        pathMaker.mkdirs();

        // We log the uninstaller deletion information
        udata.setUninstallerJarFilename(jar);
        udata.setUninstallerPath(dest);

        // We open our final jar file
        FileOutputStream out = new FileOutputStream(jar);
        // Intersect a buffer else byte for byte will be written to the file.
        BufferedOutputStream bos = new BufferedOutputStream(out);
        ZipOutputStream outJar = new ZipOutputStream(bos);
        idata.uninstallOutJar = outJar;
        outJar.setLevel(9);
        udata.addFile(jar, true);

        // We copy the uninstallers
        HashSet<String> doubles = new HashSet<String>();

        for (InputStream anIn : in)
        {
            if (anIn == null)
            {
                continue;
            }
            ZipInputStream inRes = new ZipInputStream(anIn);
            ZipEntry zentry = inRes.getNextEntry();
            while (zentry != null)
            {
                // Puts a new entry, but not twice like META-INF
                if (!doubles.contains(zentry.getName()))
                {
                    doubles.add(zentry.getName());
                    outJar.putNextEntry(new ZipEntry(zentry.getName()));

                    // Byte to byte copy
                    int unc = inRes.read();
                    while (unc != -1)
                    {
                        outJar.write(unc);
                        unc = inRes.read();
                    }

                    // Next one please
                    inRes.closeEntry();
                    outJar.closeEntry();
                }
                zentry = inRes.getNextEntry();
            }
            inRes.close();
        }

        // is it an adxadmin package ?
        // we must know in order to check uninstall
        if (idata.info.isAdxAdmin())
        {
            outJar.putNextEntry(new ZipEntry("is-adxadmin"));
            outJar.closeEntry();
        }
        
        
        // Should we relaunch the uninstaller with privileges?
        if (idata.info.isPrivilegedExecutionRequiredUninstaller())
        {
            outJar.putNextEntry(new ZipEntry("exec-admin"));
            outJar.closeEntry();
        }

        // We put the langpack
        InputStream in2 = Unpacker.class.getResourceAsStream("/langpacks/" + idata.localeISO3 + ".xml");
	if (in2 != null)
        {
            outJar.putNextEntry(new ZipEntry("langpack.xml"));
            int read = in2.read();
            while (read != -1)
            {
                outJar.write(read);
                read = in2.read();
            }
            outJar.closeEntry();
            in2.close();
	}

        // We put the custom langpack if available
        in2 = Unpacker.class.getResourceAsStream("/res/CustomLangpack.xml_" + idata.localeISO3);
	if (in2 != null)
        {
            outJar.putNextEntry(new ZipEntry("customlangpack.xml"));
            int read = in2.read();
            while (read != -1)
            {
                outJar.write(read);
                read = in2.read();
            }
            outJar.closeEntry();
            in2.close();
	}
    }

    /**
     * Adds additional unistall data to the uninstall data object.
     *
     * @param udata      unistall data
     * @param customData array of lists of custom action data like uninstaller listeners
     */
    protected void handleAdditionalUninstallData(UninstallData udata, List[] customData)
    {
        // Handle uninstall libs
        udata.addAdditionalData("__uninstallLibs__", customData[AutomatedInstallData.UNINSTALLER_LIBS_INDEX]);
        // Handle uninstaller listeners
        udata.addAdditionalData("uninstallerListeners", customData[AutomatedInstallData.UNINSTALLER_LISTENER_INDEX]);
        // Handle uninstaller jars
        udata.addAdditionalData("uninstallerJars", customData[AutomatedInstallData.UNINSTALLER_JARS_INDEX]);
    }

    public abstract void run();

    /**
     * @param updatechecks
     */
    protected void performUpdateChecks(ArrayList<UpdateCheck> updatechecks)
    {
        ArrayList<RE> include_patterns = new ArrayList<RE>();
        ArrayList<RE> exclude_patterns = new ArrayList<RE>();

        RECompiler recompiler = new RECompiler();

        this.absolute_installpath = new File(idata.getInstallPath()).getAbsoluteFile();

        // at first, collect all patterns
        for (UpdateCheck uc : updatechecks)
        {
            if (uc.includesList != null)
            {
                include_patterns.addAll(preparePatterns(uc.includesList, recompiler));
            }

            if (uc.excludesList != null)
            {
                exclude_patterns.addAll(preparePatterns(uc.excludesList, recompiler));
            }
        }

        // do nothing if no update checks were specified
        if (include_patterns.size() == 0)
        {
            return;
        }

        // now collect all files in the installation directory and figure
        // out files to check for deletion

        // use a treeset for fast access
        TreeSet<String> installed_files = new TreeSet<String>();

        for (String fname : this.udata.getInstalledFilesList())
        {
            File f = new File(fname);

            if (!f.isAbsolute())
            {
                f = new File(this.absolute_installpath, fname);
            }

            installed_files.add(f.getAbsolutePath());
        }

        // now scan installation directory (breadth first), contains Files of
        // directories to scan
        // (note: we'll recurse infinitely if there are circular links or
        // similar nasty things)
        Stack<File> scanstack = new Stack<File>();

        // contains File objects determined for deletion
        ArrayList<File> files_to_delete = new ArrayList<File>();

        try
        {
            scanstack.add(absolute_installpath);

            while (!scanstack.empty())
            {
                File f = scanstack.pop();

                File[] files = f.listFiles();

                if (files == null)
                {
                    throw new IOException(f.getPath() + "is not a directory!");
                }

                for (File newf : files)
                {
                    String newfname = newf.getPath();

                    // skip files we just installed
                    if (installed_files.contains(newfname))
                    {
                        continue;
                    }

                    if (fileMatchesOnePattern(newfname, include_patterns)
                            && (!fileMatchesOnePattern(newfname, exclude_patterns)))
                    {
                        files_to_delete.add(newf);
                    }

                    if (newf.isDirectory() && !fileMatchesOnePattern(newfname, exclude_patterns))
                    {
                        scanstack.push(newf);
                    }

                }
            }
        }
        catch (IOException e)
        {
            this.handler.emitError("error while performing update checks", e.toString());
        }

        for (File f : files_to_delete)
        {
            if (!f.isDirectory())
            // skip directories - they cannot be removed safely yet
            {
//                this.handler.emitNotification("deleting " + f.getPath());
                f.delete();
            }

        }
    }

    /**
     * Writes information about the installed packs and the variables at
     * installation time.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void writeInstallationInformation() throws IOException, ClassNotFoundException
    {
        if (!idata.info.isWriteInstallationInformation())
        {
            Debug.trace("skip writing installation information");
            return;
        }
        Debug.trace("writing installation information");
        String installdir = idata.getInstallPath();

        List installedpacks = new ArrayList(idata.selectedPacks);

        File installationinfo = new File(installdir + File.separator + AutomatedInstallData.INSTALLATION_INFORMATION);
        if (!installationinfo.exists())
        {
            Debug.trace("creating info file" + installationinfo.getAbsolutePath());
            installationinfo.createNewFile();
        }
        else
        {
            Debug.trace("installation information found");
            // read in old information and update
            FileInputStream fin = new FileInputStream(installationinfo);
            ObjectInputStream oin = new ObjectInputStream(fin);

            List packs = (List) oin.readObject();
            for (Object pack1 : packs)
            {
                Pack pack = (Pack) pack1;
                installedpacks.add(pack);
            }
            oin.close();
            fin.close();

        }

        FileOutputStream fout = new FileOutputStream(installationinfo);
        ObjectOutputStream oout = new ObjectOutputStream(fout);
        oout.writeObject(installedpacks);
        /*
        int selectedpackscount = idata.selectedPacks.size();
        for (int i = 0; i < selectedpackscount; i++)
        {
            Pack pack = (Pack) idata.selectedPacks.get(i);
            oout.writeObject(pack);
        }
        */
        oout.writeObject(idata.variables);
        Debug.trace("done.");
        oout.close();
        fout.close();
    }

    protected File getAbsolutInstallSource() throws Exception
    {
        if (absolutInstallSource == null)
        {
            URI uri = getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            if (!"file".equals(uri.getScheme()))
            {
                throw new Exception ("Unexpected scheme in JAR file URI: "+uri);
            }
            absolutInstallSource = new File(uri.getSchemeSpecificPart()).getAbsoluteFile();
            if (absolutInstallSource.getName().endsWith(".jar"))
            {
                absolutInstallSource = absolutInstallSource.getParentFile();
            }
        }
        return absolutInstallSource;
    }
}


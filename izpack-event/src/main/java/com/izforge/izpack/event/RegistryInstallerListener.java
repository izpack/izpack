/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2004 Klaus Bartz
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

package com.izforge.izpack.event;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.exception.*;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.unpacker.IUnpacker;
import com.izforge.izpack.util.CleanupClient;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.helper.SpecHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Installer custom action for handling registry entries on Windows. On Unix nothing will be done.
 * The actions which should be performed are defined in a resource file named "RegistrySpec.xml".
 * This resource should be declared in the installation definition file (install.xml), else an
 * exception will be raised during execution of this custom action. The related DTD is
 * appl/install/IzPack/resources/registry.dtd.
 *
 * @author Klaus Bartz
 */
public class RegistryInstallerListener extends AbstractProgressInstallerListener implements CleanupClient
{
    private static final Logger logger = Logger.getLogger(RegistryInstallerListener.class.getName());

    /**
     * The name of the XML file that specifies the registry entries.
     */
    public static final String SPEC_FILE_NAME = "RegistrySpec.xml";

    private static final String REG_KEY = "key";

    private static final String REG_VALUE = "value";

    private static final String REG_ROOT = "root";

    private static final String REG_ROOT_FALLBACK = "rootFallback";

    private static final String REG_BASENAME = "name";

    private static final String REG_KEYPATH = "keypath";

    private static final String REG_DWORD = "dword";

    private static final String REG_STRING = "string";

    private static final String REG_MULTI = "multi";

    private static final String REG_BIN = "bin";

    private static final String REG_DATA = "data";

    private static final String REG_OVERRIDE = "override";

    private static final String SAVE_PREVIOUS = "saveprevious";


    private List registryModificationLog;

    /**
     * The unpacker.
     */
    private final IUnpacker unpacker;

    /**
     * The variable substituter.
     */
    private final VariableSubstitutor substitutor;

    /**
     * The uninstallation data.
     */
    private final UninstallData uninstallData;

    /**
     * The resources.
     */
    private final Resources resources;

    /**
     * The rules.
     */
    private final RulesEngine rules;

    /**
     * The house-keeper.
     */
    private final Housekeeper housekeeper;

    /**
     * The registry handler. May be <tt>null</tt>
     */
    private final RegistryHandler registry;

    /**
     * The specification helper.
     */
    private final SpecHelper spec;

    /**
     * The uninstaller icon.
     */
    static final String UNINSTALLER_ICON = "UninstallerIcon";


    /**
     * Constructs a <tt>RegistryInstallerListener</tt>.
     *
     * @param unpacker      the unpacker
     * @param substitutor   the variable substituter
     * @param installData   the installation data
     * @param uninstallData the uninstallation data
     * @param rules         the rules
     * @param resources     the resources
     * @param housekeeper   the housekeeper
     * @param handler       the registry handler reference
     */
    public RegistryInstallerListener(IUnpacker unpacker, VariableSubstitutor substitutor,
                                     InstallData installData, UninstallData uninstallData,
                                     Resources resources, RulesEngine rules, Housekeeper housekeeper,
                                     RegistryDefaultHandler handler)
    {
        super(installData);
        this.substitutor = substitutor;
        this.unpacker = unpacker;
        this.uninstallData = uninstallData;
        this.resources = resources;
        this.rules = rules;
        this.housekeeper = housekeeper;
        this.registry = handler.getInstance();
        spec = new SpecHelper(resources);
    }

    /**
     * Initialises the listener.
     *
     * @throws IzPackException for any error
     */
    @Override
    public void initialise()
    {
        if (registry != null)
        {
            String uninstallName = getUninstallName();
            registry.setUninstallName(uninstallName);
        }
    }

    /**
     * Invoked after packs are installed.
     *
     * @param packs    the installed packs
     * @param listener the progress listener
     * @throws IzPackException for any error
     */
    @Override
    public void afterPacks(List<Pack> packs, ProgressListener listener)
    {
        if (registry != null)
        {
            try
            {
                // need to read the spec now rather than in initialise(), in order to do variable replacement
                spec.readSpec(SPEC_FILE_NAME);
            }
            catch (Exception exception)
            {
                throw new IzPackException("Failed to read: " + SPEC_FILE_NAME, exception);
            }

            try
            {
                afterPacks(packs);
            }
            catch (NativeLibException e)
            {
                throw new WrappedNativeLibException(e, getInstallData().getMessages());
            }
        }
    }

    /**
     * Remove all registry entries on failed installation.
     */

    public void cleanUp()
    {
        if (registry != null)
        {
            InstallData installData = getInstallData();
            if (!installData.isInstallSuccess() && registryModificationLog != null
                    && !registryModificationLog.isEmpty())
            {
                // installation was not successful so rewind all registry changes
                try
                {
                    registry.activateLogging();
                    registry.setLoggingInfo(registryModificationLog);
                    registry.rewind();
                }
                catch (Exception e)
                {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Returns the uninstall name, used to initialise the {@link RegistryHandler#setUninstallName(String)}.
     * <p/>
     * This implementation returns a concatenation of the <em>APP_NAME</em> and <em>APP_VER</em> variables,
     * separated by a space.
     *
     * @return the uninstall name
     */
    protected String getUninstallName()
    {
        Variables variables = getInstallData().getVariables();
        return variables.get("APP_NAME") + " " + variables.get("APP_VER");
    }

   private void afterPacks(List<Pack> packs) throws NativeLibException, InstallerException
    {
        // Register for cleanup
        housekeeper.registerForCleanup(this);

        InstallData installData = getInstallData();
        Info installInfo = installData.getInfo();
        
        // Start logging
        IXMLElement uninstallerPack = null;
        // No interrupt desired after writing registry entries.
        unpacker.setDisableInterrupt(true);
        registry.activateLogging();

        if (spec.getSpec() != null)
        {
            // Get the special pack "UninstallStuff" which contains values
            // for the uninstaller entry.
            uninstallerPack = spec.getPackForName("UninstallStuff");
            performPack(uninstallerPack);

            // Now perform the selected packs.
            for (Pack selectedPack : packs)
            {
                // Resolve data for current pack.
                IXMLElement pack = spec.getPackForName(selectedPack.getName());
                performPack(pack);

            }
        }
        String uninstallSuffix = installData.getVariable("UninstallKeySuffix");
        if (uninstallSuffix != null)
        {
            registry.setUninstallName(registry.getUninstallName() + " " + uninstallSuffix);
        }
        // Generate uninstaller key automatically if not defined in spec only if the uninstaller path is set
        // (this is not the case if write="false").
        if (uninstallerPack == null && installInfo != null && installInfo.getUninstallerPath() != null)
        {
            registerUninstallKey();
        }
        // Get the logging info from the registry class and put it into
        // the uninstaller. The RegistryUninstallerListener loads that data
        // and rewind the made entries.
        // This is the common way to transport informations from an
        // installer CustomAction to the corresponding uninstaller
        // CustomAction.
        List<Object> info = registry.getLoggingInfo();
        if (info != null)
        {
            uninstallData.addAdditionalData("registryEntries", info);
        }
        // Remember all registry info to rewind registry modifications in case of failed installation
        registryModificationLog = info;
    }

    /**
     * Performs the registry settings for the given pack.
     *
     * @param pack XML element which contains the registry settings for one pack
     * @throws InstallerException if a required attribute is missing
     * @throws NativeLibException for any native library error
     */
    private void performPack(IXMLElement pack) throws InstallerException, NativeLibException
    {
        if (pack == null)
        {
            return;
        }
        String packCondition = pack.getAttribute("condition");
        if (packCondition != null)
        {
            logger.fine("Condition \"" + packCondition + "\" found for pack of registry entries");
            if (!rules.isConditionTrue(packCondition))
            {
                // condition not fulfilled, continue with next element.
                logger.fine("Condition \"" + packCondition + "\" not true");
                return;
            }
        }

        // Get all entries for registry settings.
        List<IXMLElement> regEntries = pack.getChildren();
        if (regEntries == null)
        {
            return;
        }
        for (IXMLElement regEntry : regEntries)
        {
            String condition = regEntry.getAttribute("condition");
            if (condition != null)
            {
                logger.fine("Condition " + condition + " found for registry entry");
                if (!rules.isConditionTrue(condition))
                {
                    // condition not fulfilled, continue with next element.
                    logger.fine("Condition \"" + condition + "\" not true");
                    continue;
                }
            }

            // Perform one registry entry.
            String type = regEntry.getName();
            if (type.equalsIgnoreCase(REG_KEY))
            {
                try
                {
                    performKeySetting(regEntry, false);
                }
                catch (NativeLibException exception)
                {
                    performKeySetting(regEntry, true);
                }
            }
            else if (type.equalsIgnoreCase(REG_VALUE))
            {
                try
                {
                    performValueSetting(regEntry, false);
                }
                catch (NativeLibException exception)
                {
                    performValueSetting(regEntry, true);
                }
            }
            else
            {
                // No valid type.
                spec.parseError(regEntry, "Non-valid type of entry; only 'key' and 'value' are allowed.");
            }

        }

    }

    /**
     * Perform the setting of one value.
     *
     * @param regEntry element which contains the description of the value to be set
     * @throws InstallerException if a required attribute is missing
     * @throws NativeLibException for any native library error
     */
    private void performValueSetting(IXMLElement regEntry, boolean useRootFallback) throws InstallerException, NativeLibException
    {
        String name = spec.getRequiredAttribute(regEntry, REG_BASENAME);
        name = substitutor.substitute(name);
        String keypath = spec.getRequiredAttribute(regEntry, REG_KEYPATH);
        keypath = substitutor.substitute(keypath);

        if (useRootFallback)
        {
            String rootFallback = spec.getOptionalAttribute(regEntry, REG_ROOT_FALLBACK);
            if (null == rootFallback)
            {
                throw new InstallerException("Error writing to registry");
            }
            int rootId = resolveRoot(regEntry, rootFallback);

            registry.setRoot(rootId);
        }
        else
        {
            String root = spec.getRequiredAttribute(regEntry, REG_ROOT);
            int rootId = resolveRoot(regEntry, root);

            registry.setRoot(rootId);
        }

        String override = regEntry.getAttribute(REG_OVERRIDE, "true");
        if (!"true".equalsIgnoreCase(override))
        { // Do not set value if override is not true and the value exist.

            if (registry.getValue(keypath, name, null) != null)
            {
                return;
            }
        }

        //set flag for logging previous contents if "saveprevious"
        // attribute not specified or specified as 'true':
        registry.setLogPrevSetValueFlag("true".equalsIgnoreCase(regEntry.getAttribute(SAVE_PREVIOUS, "true")));

        String value = regEntry.getAttribute(REG_DWORD);
        if (value != null)
        { // Value type is DWord; placeholder possible.
            value = substitutor.substitute(value);
            registry.setValue(keypath, name, Long.parseLong(value));
            return;
        }
        value = regEntry.getAttribute(REG_STRING);
        if (value != null)
        { // Value type is string; placeholder possible.
            value = substitutor.substitute(value);
            registry.setValue(keypath, name, value);
            return;
        }
        List<IXMLElement> values = regEntry.getChildrenNamed(REG_MULTI);
        if (values != null && !values.isEmpty())
        { // Value type is REG_MULTI_SZ; placeholder possible.
            Iterator<IXMLElement> multiIter = values.iterator();
            String[] multiString = new String[values.size()];
            for (int i = 0; multiIter.hasNext(); ++i)
            {
                IXMLElement element = multiIter.next();
                multiString[i] = spec.getRequiredAttribute(element, REG_DATA);
                multiString[i] = substitutor.substitute(multiString[i]);
            }
            registry.setValue(keypath, name, multiString);
            return;
        }
        values = regEntry.getChildrenNamed(REG_BIN);
        if (values != null && !values.isEmpty())
        { // Value type is REG_BINARY; placeholder possible or not ??? why not
            // ...
            Iterator<IXMLElement> multiIter = values.iterator();

            StringBuilder buf = new StringBuilder();
            while (multiIter.hasNext())
            {
                IXMLElement element = multiIter.next();
                String tmp = spec.getRequiredAttribute(element, REG_DATA);
                buf.append(tmp);
                if (!tmp.endsWith(",") && multiIter.hasNext())
                {
                    buf.append(",");
                }
            }
            byte[] bytes = extractBytes(regEntry, substitutor.substitute(buf.toString()));
            registry.setValue(keypath, name, bytes);
            return;
        }
        spec.parseError(regEntry, "No data found.");

    }

    private byte[] extractBytes(IXMLElement element, String byteString) throws InstallerException
    {
        StringTokenizer st = new StringTokenizer(byteString, ",");
        byte[] retval = new byte[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens())
        {
            byte value = 0;
            String token = st.nextToken().trim();
            try
            {
                // Unfortunately byte is signed ...
                int tval = Integer.parseInt(token, 16);
                if (tval < 0 || tval > 0xff)
                {
                    throw new InstallerException("Byte value out of range: " + tval);
                }
                if (tval > 0x7f)
                {
                    tval -= 0x100;
                }
                value = (byte) tval;
            }
            catch (NumberFormatException nfe)
            {
                spec.parseError(element, "Bad entry for REG_BINARY; a byte should be written as 2 digit hexvalue"
                        + "followed by a ','.");
            }
            retval[i++] = value;
        }
        return (retval);

    }

    /**
     * Perform the setting of one key.
     *
     * @param regEntry element which contains the description of the key to be created
     * @throws InstallerException if a required attribute is missing
     * @throws NativeLibException for any native library error
     */
    private void performKeySetting(IXMLElement regEntry, boolean useRootFallback) throws InstallerException, NativeLibException
    {
        String path = spec.getRequiredAttribute(regEntry, REG_KEYPATH);
        path = substitutor.substitute(path);

        if (useRootFallback)
        {
            String rootFallback = spec.getOptionalAttribute(regEntry, REG_ROOT_FALLBACK);
            if (null == rootFallback)
            {
                throw new InstallerException("Error writing to registry");
            }
            int rootId = resolveRoot(regEntry, rootFallback);

            registry.setRoot(rootId);
        }
        else
        {
            String root = spec.getRequiredAttribute(regEntry, REG_ROOT);
            int rootId = resolveRoot(regEntry, root);

            registry.setRoot(rootId);
        }

        if (!registry.keyExist(path))
        {
            registry.createKey(path);
        }
    }

    private int resolveRoot(IXMLElement regEntry, String root)
    {
        String root1 = substitutor.substitute(root);
        Integer tmp = RegistryHandler.ROOT_KEY_MAP.get(root1);
        if (tmp != null)
        {
            return (tmp);
        }
        spec.parseError(regEntry, "Unknown value (" + root1 + ") for registry root.");
        return 0;
    }

    /**
     * Registers the uninstaller.
     *
     * @throws NativeLibException for any native library exception
     * @throws InstallerException for any other error
     */
    private void registerUninstallKey() throws NativeLibException
    {
        String uninstallName = registry.getUninstallName();
        if (uninstallName == null)
        {
            return;
        }
        InstallData installData = getInstallData();
        String keyName = RegistryHandler.UNINSTALL_ROOT + uninstallName;
        String uninstallerPath = IoHelper.translatePath(installData.getInfo().getUninstallerPath(),
                                                        installData.getVariables());
        String cmd = "\"" + installData.getVariable("JAVA_HOME") + "\\bin\\javaw.exe\" -jar \""
                + uninstallerPath + "\\" + installData.getInfo().getUninstallerName() + "\"";
        String appVersion = installData.getVariable("APP_VER");
        String appUrl = installData.getVariable("APP_URL");
        
        try
        {
            registry.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
            registry.setValue(keyName, "DisplayName", uninstallName);
        }
        catch (NativeLibException exception)
        { // Users without administrative rights should be able to install the app for themselves
            logger.warning(
                    "Failed to register uninstaller in HKEY_LOCAL_MACHINE hive, trying HKEY_CURRENT_USER: " + exception.getMessage());
            registry.setRoot(RegistryHandler.HKEY_CURRENT_USER);
            registry.setValue(keyName, "DisplayName", uninstallName);
        }
        registry.setValue(keyName, "UninstallString", cmd);
        registry.setValue(keyName, "DisplayVersion", appVersion);
        if (appUrl != null && appUrl.length() > 0)
        {
            registry.setValue(keyName, "HelpLink", appUrl);
        }
        
        // add the estimated size
        String estimatedSize = installData.getVariable(InstallData.ESTIMATED_SIZE);
        if (estimatedSize != null && !estimatedSize.isEmpty()) 
        {
	        long estimatedSizeInBytes = Long.parseLong(estimatedSize);
	        registry.setValue(keyName, "EstimatedSize", estimatedSizeInBytes);
        }
        
        // Try to write the uninstaller icon out.
        InputStream in = null;
        FileOutputStream out = null;
        try
        {
            in = resources.getInputStream(UNINSTALLER_ICON);
            String iconPath = installData.getVariable("INSTALL_PATH") + File.separator
                    + "Uninstaller" + File.separator + "UninstallerIcon.ico";
            
            // make sure the 'Uninstaller' directory exists
            File uninstallerIcon = new File(iconPath);
            FileUtils.touch(uninstallerIcon);

            out = new FileOutputStream(uninstallerIcon);
            IOUtils.copy(in, out);
            out.flush();
            out.close();
            registry.setValue(keyName, "DisplayIcon", iconPath);
        }
        catch (ResourceNotFoundException exception)
        {
            // No icon resource defined; ignore it
            logger.warning("The configured uninstaller icon was not found: " + exception.getMessage());
        }
        catch (IOException exception)
        {
            throw new InstallerException(exception);
        }
        finally
        {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }


}

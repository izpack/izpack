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

import com.coi.tools.os.win.NativeLibException;
import com.izforge.izpack.Pack;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.UninstallData;
import com.izforge.izpack.installer.Unpacker;
import com.izforge.izpack.rules.RulesEngine;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.CleanupClient;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.SpecHelper;
import com.izforge.izpack.util.VariableSubstitutor;
import com.izforge.izpack.util.os.RegistryDefaultHandler;
import com.izforge.izpack.util.os.RegistryHandler;
import com.izforge.izpack.util.os.WrappedNativeLibException;
import com.izforge.izpack.adaptator.IXMLElement;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Installer custom action for handling registry entries on Windows. On Unix nothing will be done.
 * The actions which should be performed are defined in a resource file named "RegistrySpec.xml".
 * This resource should be declared in the installation definition file (install.xml), else an
 * exception will be raised during execution of this custom action. The related DTD is
 * appl/install/IzPack/resources/registry.dtd.
 *
 * @author Klaus Bartz
 */
public class RegistryInstallerListener extends NativeInstallerListener implements CleanupClient
{

    /**
     * The name of the XML file that specifies the registry entries.
     */
    private static final String SPEC_FILE_NAME = "RegistrySpec.xml";

    private static final String REG_KEY = "key";

    private static final String REG_VALUE = "value";

    private static final String REG_ROOT = "root";

    private static final String REG_BASENAME = "name";

    private static final String REG_KEYPATH = "keypath";

    private static final String REG_DWORD = "dword";

    private static final String REG_STRING = "string";

    private static final String REG_MULTI = "multi";

    private static final String REG_BIN = "bin";

    private static final String REG_DATA = "data";

    private static final String REG_OVERRIDE = "override";

    private static final String SAVE_PREVIOUS = "saveprevious";

    private RulesEngine rules;

	private List registryModificationLog;
    
    /**
     * Default constructor.
     */
    public RegistryInstallerListener()
    {
        super(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.compiler.InstallerListener#beforePacks(com.izforge.izpack.installer.AutomatedInstallData,
     * int, com.izforge.izpack.util.AbstractUIProgressHandler)
     */
    public void beforePacks(AutomatedInstallData idata, Integer npacks,
                            AbstractUIProgressHandler handler) throws Exception
    {
        super.beforePacks(idata, npacks, handler);
        rules = idata.getRules();
        initializeRegistryHandler(idata);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.compiler.InstallerListener#afterPacks(com.izforge.izpack.installer.AutomatedInstallData,
     * com.izforge.izpack.util.AbstractUIProgressHandler)
     */
    public void afterPacks(AutomatedInstallData idata, AbstractUIProgressHandler handler)
            throws Exception
    {
        try
        {
        	// Register for cleanup
        	Housekeeper.getInstance().registerForCleanup(this);
        	
            // Start logging
            RegistryHandler rh = RegistryDefaultHandler.getInstance();
            if (rh == null)
            {
                return;
            }
            IXMLElement uninstallerPack = null;
            // No interrupt desired after writing registry entries.
            Unpacker.setDiscardInterrupt(true);
            rh.activateLogging();

            if (getSpecHelper().getSpec() != null)
            {
                VariableSubstitutor substitutor = new VariableSubstitutor(idata.getVariables());
                Iterator iter = idata.selectedPacks.iterator();
                // Get the special pack "UninstallStuff" which contains values
                // for the uninstaller entry.
                uninstallerPack = getSpecHelper().getPackForName("UninstallStuff");
                performPack(uninstallerPack, substitutor);

                // Now perform the selected packs.
                while (iter != null && iter.hasNext())
                {
                    // Resolve data for current pack.
                    IXMLElement pack = getSpecHelper().getPackForName(((Pack) iter.next()).name);
                    performPack(pack, substitutor);

                }
            }
            String uninstallSuffix = idata.getVariable("UninstallKeySuffix");
            if (uninstallSuffix != null)
            {
                rh.setUninstallName(rh.getUninstallName() + " " + uninstallSuffix);
            }
            // Generate uninstaller key automatically if not defined in spec.
            if (uninstallerPack == null)
            {
                rh.registerUninstallKey();
            }
            // Get the logging info from the registry class and put it into
            // the uninstaller. The RegistryUninstallerListener loads that data
            // and rewind the made entries.
            // This is the common way to transport informations from an
            // installer CustomAction to the corresponding uninstaller
            // CustomAction.
            List<Object> info = rh.getLoggingInfo();
            if (info != null)
            {
                UninstallData.getInstance().addAdditionalData("registryEntries", info);
            }
            // Remember all registry info to rewind registry modifications in case of failed installation
            registryModificationLog = info;
        }
        catch (Exception e)
        {
            if (e instanceof NativeLibException)
            {
                throw new WrappedNativeLibException(e);
            }
            else
            {
                throw e;
            }
        }
    }
    
    

    /**
     * Remove all registry entries on failed installation
     */
    public void cleanUp()
    {
		// installation was not successful now rewind all registry changes
        if (AutomatedInstallData.getInstance().installSuccess || registryModificationLog == null || registryModificationLog.size() < 1)
        {
            return;
        }
        RegistryHandler registryHandler = RegistryDefaultHandler.getInstance();
        try
        {
            if (registryHandler == null)
            {
                return;
            }

            registryHandler.activateLogging();
            registryHandler.setLoggingInfo(registryModificationLog);
            registryHandler.rewind();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

	/**
     * Performs the registry settings for the given pack.
     *
     * @param pack XML elemtent which contains the registry settings for one pack
     * @throws Exception
     */
    private void performPack(IXMLElement pack, VariableSubstitutor substitutor) throws Exception
    {
        if (pack == null)
        {
            return;
        }
        
        String packcondition = pack.getAttribute("condition");
        if (packcondition != null){
            Debug.trace("condition " + packcondition + " found for pack of registry entries.");
            if (!rules.isConditionTrue(packcondition)){
                // condition not fulfilled, continue with next element.
                Debug.trace("not fulfilled.");
                return;
            }
        }
        
        // Get all entries for registry settings.
        Vector regEntries = pack.getChildren();
        if (regEntries == null)
        {
            return;
        }
        Iterator entriesIter = regEntries.iterator();
        while (entriesIter != null && entriesIter.hasNext())
        {
            IXMLElement regEntry = (IXMLElement) entriesIter.next();
            String condition = regEntry.getAttribute("condition");
            if (condition != null){
                Debug.trace("condition " + condition + " found for registry entry.");
                if (!rules.isConditionTrue(condition)){
                    // condition not fulfilled, continue with next element.
                    Debug.trace("not fulfilled.");
                    continue;
                }
            }
            
            // Perform one registry entry.
            String type = regEntry.getName();
            if (type.equalsIgnoreCase(REG_KEY))
            {
                performKeySetting(regEntry, substitutor);
            }
            else if (type.equalsIgnoreCase(REG_VALUE))
            {
                performValueSetting(regEntry, substitutor);
            }
            else
            // No valid type.
            {
                getSpecHelper().parseError(regEntry,
                        "Non-valid type of entry; only 'key' and 'value' are allowed.");
            }

        }

    }

    /**
     * Perform the setting of one value.
     *
     * @param regEntry    element which contains the description of the value to be set
     * @param substitutor variable substitutor to be used for revising the regEntry contents
     */
    private void performValueSetting(IXMLElement regEntry, VariableSubstitutor substitutor)
            throws Exception
    {
        SpecHelper specHelper = getSpecHelper();
        String name = specHelper.getRequiredAttribute(regEntry, REG_BASENAME);
        name = substitutor.substitute(name, null);
        String keypath = specHelper.getRequiredAttribute(regEntry, REG_KEYPATH);
        keypath = substitutor.substitute(keypath, null);
        String root = specHelper.getRequiredAttribute(regEntry, REG_ROOT);
        int rootId = resolveRoot(regEntry, root, substitutor);

        RegistryHandler rh = RegistryDefaultHandler.getInstance();
        if (rh == null)
        {
            return;
        }

        rh.setRoot(rootId);

        String override = regEntry.getAttribute(REG_OVERRIDE, "true");
        if (!"true".equalsIgnoreCase(override))
        { // Do not set value if override is not true and the value exist.

            if (rh.getValue(keypath, name, null) != null)
            {
                return;
            }
        }

              //set flag for logging previous contents if "saveprevious"
              // attribute not specified or specified as 'true':
        rh.setLogPrevSetValueFlag("true".equalsIgnoreCase(
                              regEntry.getAttribute(SAVE_PREVIOUS,"true")));

        String value = regEntry.getAttribute(REG_DWORD);
        if (value != null)
        { // Value type is DWord; placeholder possible.
            value = substitutor.substitute(value, null);
            rh.setValue(keypath, name, Long.parseLong(value));
            return;
        }
        value = regEntry.getAttribute(REG_STRING);
        if (value != null)
        { // Value type is string; placeholder possible.
            value = substitutor.substitute(value, null);
            rh.setValue(keypath, name, value);
            return;
        }
        Vector<IXMLElement> values = regEntry.getChildrenNamed(REG_MULTI);
        if (values != null && !values.isEmpty())
        { // Value type is REG_MULTI_SZ; placeholder possible.
            Iterator<IXMLElement> multiIter = values.iterator();
            String[] multiString = new String[values.size()];
            for (int i = 0; multiIter.hasNext(); ++i)
            {
                IXMLElement element = multiIter.next();
                multiString[i] = specHelper.getRequiredAttribute(element, REG_DATA);
                multiString[i] = substitutor.substitute(multiString[i], null);
            }
            rh.setValue(keypath, name, multiString);
            return;
        }
        values = regEntry.getChildrenNamed(REG_BIN);
        if (values != null && !values.isEmpty())
        { // Value type is REG_BINARY; placeholder possible or not ??? why not
            // ...
            Iterator<IXMLElement> multiIter = values.iterator();

            StringBuffer buf = new StringBuffer();
            for (int i = 0; multiIter.hasNext(); ++i)
            {
                IXMLElement element = multiIter.next();
                String tmp = specHelper.getRequiredAttribute(element, REG_DATA);
                buf.append(tmp);
                if (!tmp.endsWith(",") && multiIter.hasNext())
                {
                    buf.append(",");
                }
            }
            byte[] bytes = extractBytes(regEntry, substitutor.substitute(buf.toString(), null));
            rh.setValue(keypath, name, bytes);
            return;
        }
        specHelper.parseError(regEntry, "No data found.");

    }

    private byte[] extractBytes(IXMLElement element, String byteString) throws Exception
    {
        StringTokenizer st = new StringTokenizer(byteString, ",");
        byte[] retval = new byte[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens())
        {
            byte value = 0;
            String token = st.nextToken().trim();
            try
            { // Unfortenly byte is signed ...
                int tval = Integer.parseInt(token, 16);
                if (tval < 0 || tval > 0xff)
                {
                    throw new NumberFormatException("Value out of range.");
                }
                if (tval > 0x7f)
                {
                    tval -= 0x100;
                }
                value = (byte) tval;
            }
            catch (NumberFormatException nfe)
            {
                getSpecHelper()
                        .parseError(element,
                                "Bad entry for REG_BINARY; a byte should be written as 2 digit hexvalue followed by a ','.");
            }
            retval[i++] = value;
        }
        return (retval);

    }

    /**
     * Perform the setting of one key.
     *
     * @param regEntry    element which contains the description of the key to be created
     * @param substitutor variable substitutor to be used for revising the regEntry contents
     */
    private void performKeySetting(IXMLElement regEntry, VariableSubstitutor substitutor)
            throws Exception
    {
        String keypath = getSpecHelper().getRequiredAttribute(regEntry, REG_KEYPATH);
        keypath = substitutor.substitute(keypath, null);
        String root = getSpecHelper().getRequiredAttribute(regEntry, REG_ROOT);
        int rootId = resolveRoot(regEntry, root, substitutor);
        RegistryHandler rh = RegistryDefaultHandler.getInstance();
        if (rh == null)
        {
            return;
        }
        rh.setRoot(rootId);
        if (!rh.keyExist(keypath))
        {
            rh.createKey(keypath);
        }
    }

    private int resolveRoot(IXMLElement regEntry, String root, VariableSubstitutor substitutor)
            throws Exception
    {
        String root1 = substitutor.substitute(root, null);
        Integer tmp = RegistryHandler.ROOT_KEY_MAP.get(root1);
        if (tmp != null)
        {
            return (tmp);
        }
        getSpecHelper().parseError(regEntry, "Unknown value (" + root1 + ")for registry root.");
        return 0;
    }

    private void initializeRegistryHandler(AutomatedInstallData idata) throws Exception
    {
        RegistryHandler rh = RegistryDefaultHandler.getInstance();
        if (rh == null)
        {
            return;
        }
        rh.verify(idata);
        getSpecHelper().readSpec(SPEC_FILE_NAME);
    }

}

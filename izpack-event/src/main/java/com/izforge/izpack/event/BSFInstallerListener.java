/*
 * IzPack - Copyright 2001-2009 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2009 Matthew Inger
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.event.ProgressNotifiers;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.util.file.FileUtils;
import com.izforge.izpack.util.helper.SpecHelper;


public class BSFInstallerListener extends AbstractInstallerListener
{

    public static final String SPEC_FILE_NAME = "BSFActionsSpec.xml";

    /**
     * The BSF actions, keyed on pack name.
     */
    private final Map<String, List<BSFAction>> actions = new HashMap<String, List<BSFAction>>();

    /**
     * The BSF uninstallation actions.
     */
    private final List<BSFAction> uninstActions = new ArrayList<BSFAction>();

    /**
     * Used to replace variables in the BSF specification.
     */
    private final VariableSubstitutor replacer;

    /**
     * The uninstallation data.
     */
    private UninstallData uninstallData;

    /**
     * The resources.
     */
    private final Resources resources;

    /**
     * The specification helper.
     */
    private SpecHelper spec;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(BSFInstallerListener.class.getName());

    /**
     * Constructs a <tt>BSFFInstallerListener</tt>.
     *
     * @param installData   the installation data
     * @param replacer      the variable replacer
     * @param resources     the resources
     * @param uninstallData the uninstallation data
     * @param notifiers     the progress notifiers
     */
    public BSFInstallerListener(InstallData installData, VariableSubstitutor replacer, Resources resources,
                                UninstallData uninstallData, ProgressNotifiers notifiers)
    {
        super(installData, notifiers);
        this.replacer = replacer;
        this.uninstallData = uninstallData;
        this.resources = resources;
        spec = new SpecHelper(resources);
    }

    /**
     * Initialises the listener.
     *
     * @throws IzPackException if the BSF actions specification cannot be read
     */
    @Override
    public void initialise()
    {
        try
        {
            spec.readSpec(SPEC_FILE_NAME, replacer);
        }
        catch (Exception exception)
        {
            throw new IzPackException("Failed to read: " + SPEC_FILE_NAME, exception);
        }
    }

    /**
     * Invoked before packs are installed.
     *
     * @param packs the packs to be installed
     * @throws IzPackException for any error
     */
    @Override
    public void beforePacks(List<Pack> packs)
    {
        if (spec == null)
        {
            return;
        }

        for (Pack pack : packs)
        {
            IXMLElement packElement = spec.getPackForName(pack.getName());
            if (packElement == null)
            {
                continue;
            }

            List<BSFAction> packActions = new ArrayList<BSFAction>();

            List<IXMLElement> scriptEntries = packElement.getChildrenNamed("script");
            if (scriptEntries != null && !scriptEntries.isEmpty())
            {
                for (IXMLElement scriptEntry : scriptEntries)
                {
                    BSFAction action = readAction(scriptEntry);
                    if (action != null)
                    {
                        packActions.add(action);
                        String script = action.getScript().toLowerCase();
                        if (script.contains(BSFAction.BEFOREDELETE) ||
                                script.contains(BSFAction.AFTERDELETE) ||
                                script.contains(BSFAction.BEFOREDELETION) ||
                                script.contains(BSFAction.AFTERDELETION))
                        {
                            uninstActions.add(action);
                        }
                    }
                }

                if (!packActions.isEmpty())
                {
                    setProgressNotifier();
                }
            }

            actions.put(pack.getName(), packActions);
        }

        for (Pack pack : packs)
        {
            performAllActions(pack, ActionBase.BEFOREPACKS, null, packs, packs.size());
            // NOTE: packs.size() is passed to BSF as "npacks" and is required for backward compatibility with 4.x
        }
    }

    /**
     * Invoked before a pack is installed.
     *
     * @param pack the pack
     * @param i    the pack number
     * @throws IzPackException for any error
     */
    @Override
    public void beforePack(Pack pack, int i)
    {
        performAllActions(pack, ActionBase.BEFOREPACK, null, pack, i);
    }

    /**
     * Invoked after a pack is installed.
     *
     * @param pack the pack
     * @param i    the pack number
     * @throws IzPackException for any error
     */
    @Override
    public void afterPack(Pack pack, int i)
    {
        performAllActions(pack, ActionBase.AFTERPACK, null, pack, i);
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
        if (notifyProgress())
        {
            listener.nextStep(getMessage("BSFAction.pack"), getProgressNotifierId(), getActionCount(packs));
        }
        for (Pack pack : packs)
        {
            performAllActions(pack, ActionBase.AFTERPACKS, listener, packs);
        }
        if (!uninstActions.isEmpty())
        {
            uninstallData.addAdditionalData("bsfActions", uninstActions);
        }
    }

    /**
     * Determines if the listener should be notified of every file and directory installation.
     *
     * @return {@code true}
     */
    @Override
    public boolean isFileListener()
    {
        return true;
    }

    /**
     * Invoked before a directory is created.
     *
     * @param dir      the directory
     * @param packFile the corresponding pack file
     * @param pack     the pack that {@code packFile} comes from
     * @throws IzPackException for any error
     */
    @Override
    public void beforeDir(File dir, PackFile packFile, Pack pack)
    {
        performAllActions(pack, BSFAction.BEFOREDIR, null, dir, packFile);
    }

    /**
     * Invoked after a directory is created.
     *
     * @param dir      the directory
     * @param packFile the corresponding pack file
     * @param pack     the pack that {@code packFile} comes from
     * @throws IzPackException for any error
     */
    @Override
    public void afterDir(File dir, PackFile packFile, Pack pack)
    {
        performAllActions(pack, BSFAction.AFTERDIR, null, dir, packFile);
    }

    /**
     * Invoked before a file is installed.
     *
     * @param file     the file
     * @param packFile the corresponding pack file
     * @param pack     the pack that {@code packFile} comes from
     * @throws IzPackException for any error
     */
    @Override
    public void beforeFile(File file, PackFile packFile, Pack pack)
    {
        performAllActions(pack, BSFAction.BEFOREFILE, null, file, packFile);
    }

    /**
     * Invoked after a file is installed.
     *
     * @param file     the file
     * @param packFile the corresponding pack file
     * @param pack     the pack that {@code packFile} comes from
     * @throws IzPackException for any error
     */
    @Override
    public void afterFile(File file, PackFile packFile, Pack pack)
    {
        performAllActions(pack, BSFAction.AFTERFILE, null, file, packFile);
    }

    private int getActionCount(List<Pack> packs)
    {
        int count = 0;
        for (Pack pack : packs)
        {
            List<BSFAction> actList = actions.get(pack.getName());
            if (actList != null)
            {
                count += actList.size();
            }
        }
        return (count);
    }

    private void performAllActions(Pack pack, String order, ProgressListener listener, Object... args)
    {
        String packName = pack.getName();
        List<BSFAction> actList = actions.get(packName);
        if (actList == null || actList.isEmpty())
        {
            return;
        }

        logger.fine("Executing all " + order + " BSF actions of pack " + packName + " ...");
        for (BSFAction act : actList)
        {
            // Inform progress bar if needed. Works only on AFTER_PACKS
            if (notifyProgress() && order.equals(ActionBase.AFTERPACKS))
            {
                listener.progress((act.getMessageID() != null) ? getMessage(act.getMessageID()) : "");
            }

            if (ActionBase.BEFOREPACKS.equalsIgnoreCase(order))
            {
                act.init();
            }
            act.execute(order, args, getInstallData());
            if (ActionBase.AFTERPACKS.equalsIgnoreCase(order))
            {
                act.destroy();
            }
        }
    }

    private BSFAction readAction(IXMLElement element)
    {
        BSFAction action = new BSFAction();
        String src = element.getAttribute("src");
        if (src != null)
        {
            InputStream is = null;
            InputStream subis = null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try
            {
                byte buf[] = new byte[10 * 1024];
                int read;
                is = resources.getInputStream(src);
                subis = new SpecHelper(resources).substituteVariables(is, replacer);

                while ((read = subis.read(buf)) != -1)
                {
                    baos.write(buf, 0, read);
                }

                action.setScript(new String(baos.toByteArray()));
            }
            catch (IzPackException exception)
            {
                throw exception;
            }
            catch (Exception exception)
            {
                throw new InstallerException(exception);
            }
            finally
            {
                FileUtils.close(subis);
                FileUtils.close(is);
            }
        }
        else
        {
            String script = element.getContent();
            if (script == null)
            {
                script = "";
            }
            action.setScript(script);
        }
        String language = element.getAttribute("language");
        action.setLanguage(language);
        return action;
    }
}

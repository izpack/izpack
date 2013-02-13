/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
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
package com.izforge.izpack.compiler.util;

import java.util.HashMap;
import java.util.Map;

import com.izforge.izpack.event.AntActionInstallerListener;
import com.izforge.izpack.event.AntActionUninstallerListener;
import com.izforge.izpack.event.BSFInstallerListener;
import com.izforge.izpack.event.BSFUninstallerListener;
import com.izforge.izpack.event.ConfigurationInstallerListener;
import com.izforge.izpack.event.ProgressBarInstallerListener;
import com.izforge.izpack.event.RegistryInstallerListener;
import com.izforge.izpack.event.RegistryUninstallerListener;
import com.izforge.izpack.event.SummaryLoggerInstallerListener;
import com.izforge.izpack.installer.web.DownloadPanel;
import com.izforge.izpack.panels.checkedhello.CheckedHelloPanel;
import com.izforge.izpack.panels.compile.CompilePanel;
import com.izforge.izpack.panels.datacheck.DataCheckPanel;
import com.izforge.izpack.panels.defaulttarget.DefaultTargetPanel;
import com.izforge.izpack.panels.extendedinstall.ExtendedInstallPanel;
import com.izforge.izpack.panels.finish.FinishPanel;
import com.izforge.izpack.panels.hello.HelloPanel;
import com.izforge.izpack.panels.htmlhello.HTMLHelloPanel;
import com.izforge.izpack.panels.htmlinfo.HTMLInfoPanel;
import com.izforge.izpack.panels.htmllicence.HTMLLicencePanel;
import com.izforge.izpack.panels.imgpacks.ImgPacksPanel;
import com.izforge.izpack.panels.info.InfoPanel;
import com.izforge.izpack.panels.install.InstallPanel;
import com.izforge.izpack.panels.installationgroup.InstallationGroupPanel;
import com.izforge.izpack.panels.installationtype.InstallationTypePanel;
import com.izforge.izpack.panels.jdkpath.JDKPathPanel;
import com.izforge.izpack.panels.licence.LicencePanel;
import com.izforge.izpack.panels.packs.PacksPanel;
import com.izforge.izpack.panels.process.ProcessPanel;
import com.izforge.izpack.panels.selectprinter.SelectPrinterPanel;
import com.izforge.izpack.panels.shortcut.ShortcutPanel;
import com.izforge.izpack.panels.simplefinish.SimpleFinishPanel;
import com.izforge.izpack.panels.sudo.SudoPanel;
import com.izforge.izpack.panels.summary.SummaryPanel;
import com.izforge.izpack.panels.target.TargetPanel;
import com.izforge.izpack.panels.treepacks.TreePacksPanel;
import com.izforge.izpack.panels.userinput.UserInputPanel;
import com.izforge.izpack.panels.userinput.processor.PasswordEncryptionProcessor;
import com.izforge.izpack.panels.userinput.processor.PortProcessor;
import com.izforge.izpack.panels.userinput.processor.UnixGroupProcessor;
import com.izforge.izpack.panels.userinput.processor.UnixUserProcessor;
import com.izforge.izpack.panels.userinput.validator.HostAddressValidator;
import com.izforge.izpack.panels.userinput.validator.IsPortValidator;
import com.izforge.izpack.panels.userinput.validator.NotEmptyValidator;
import com.izforge.izpack.panels.userinput.validator.PasswordEqualityValidator;
import com.izforge.izpack.panels.userinput.validator.PortValidator;
import com.izforge.izpack.panels.userinput.validator.RegularExpressionValidator;
import com.izforge.izpack.panels.userpath.UserPathPanel;
import com.izforge.izpack.panels.xinfo.XInfoPanel;

/**
 * Maps unqualified IzPack class names to their fully qualified names.
 *
 * @author Tim Anderson
 */
public class DefaultClassNameMapper implements ClassNameMapper
{

    /**
     * Map of class simple names to their fully qualified names.
     */
    private final Map<String, String> mappings = new HashMap<String, String>();


    /**
     * Default constructor.
     */
    public DefaultClassNameMapper()
    {
        // add mappings for InstallerListeners
        addMapping(AntActionInstallerListener.class, BSFInstallerListener.class,
                   ConfigurationInstallerListener.class, ProgressBarInstallerListener.class,
                   RegistryInstallerListener.class, SummaryLoggerInstallerListener.class);

        // add mappings for UninstallerListeners
        addMapping(AntActionUninstallerListener.class, BSFUninstallerListener.class,
                   RegistryUninstallerListener.class);

        // add mappings for IzPanels
        addMapping(CheckedHelloPanel.class, CompilePanel.class, DataCheckPanel.class,
                   DefaultTargetPanel.class, DownloadPanel.class, ExtendedInstallPanel.class, FinishPanel.class,
                   HTMLHelloPanel.class, HTMLInfoPanel.class, HTMLLicencePanel.class, HelloPanel.class,
                   ImgPacksPanel.class, InfoPanel.class, InstallationGroupPanel.class, InstallationTypePanel.class,
                   InstallPanel.class, JDKPathPanel.class, LicencePanel.class, PacksPanel.class, ProcessPanel.class,
                   SelectPrinterPanel.class, ShortcutPanel.class, SimpleFinishPanel.class, SudoPanel.class,
                   SummaryPanel.class, TargetPanel.class, TreePacksPanel.class, UserInputPanel.class,
                   UserPathPanel.class, XInfoPanel.class);

        // add mappings for Validators
        addMapping(HostAddressValidator.class, IsPortValidator.class, NotEmptyValidator.class,
                   PasswordEqualityValidator.class, PortValidator.class, RegularExpressionValidator.class);

        // add mappings for Processors
        addMapping(PasswordEncryptionProcessor.class, PortProcessor.class, UnixGroupProcessor.class,
                   UnixUserProcessor.class);
    }

    /**
     * Maps an unqualified class name to its fully qualified name.
     *
     * @param className the class name to map
     * @return the fully qualified class name, or {@code null} if no mapping exists
     */
    @Override
    public String map(String className)
    {
        return mappings.get(className);
    }

    /**
     * Adds mappings for the specified types.
     *
     * @param types the types
     * @throws IllegalStateException if a mapping already exists
     */
    private void addMapping(Class... types)
    {
        for (Class type : types)
        {
            if (mappings.put(type.getSimpleName(), type.getName()) != null)
            {
                throw new IllegalStateException("A mapping already exists for " + type.getSimpleName());
            }
        }
    }

}

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

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
 * Tests the {@link DefaultClassNameMapper}.
 *
 * @author Tim Anderson
 */
public class DefaultClassNameMapperTest
{

    /**
     * The mapper.
     */
    private ClassNameMapper mapper;


    /**
     * Default constructor.
     */
    public DefaultClassNameMapperTest()
    {
        mapper = new DefaultClassNameMapper();
    }

    /**
     * Tests the mapping of installer listener simple names to their fully qualified names.
     */
    @Test
    public void testInstallerListeners()
    {
        assertEquals(AntActionInstallerListener.class.getName(), mapper.map("AntActionInstallerListener"));
        assertEquals(BSFInstallerListener.class.getName(), mapper.map("BSFInstallerListener"));
        assertEquals(ConfigurationInstallerListener.class.getName(), mapper.map("ConfigurationInstallerListener"));
        assertEquals(ProgressBarInstallerListener.class.getName(), mapper.map("ProgressBarInstallerListener"));
        assertEquals(RegistryInstallerListener.class.getName(), mapper.map("RegistryInstallerListener"));
        assertEquals(SummaryLoggerInstallerListener.class.getName(), mapper.map("SummaryLoggerInstallerListener"));
    }

    /**
     * Tests the mapping of uninstaller listener simple names to their fully qualified names.
     */
    @Test
    public void testUninstallerListeners()
    {
        assertEquals(AntActionUninstallerListener.class.getName(), mapper.map("AntActionUninstallerListener"));
        assertEquals(BSFUninstallerListener.class.getName(), mapper.map("BSFUninstallerListener"));
        assertEquals(RegistryUninstallerListener.class.getName(), mapper.map("RegistryUninstallerListener"));
    }

    /**
     * Tests the mapping of validator simple names to their fully qualified names.
     */
    @Test
    public void testValidators()
    {
        assertEquals(HostAddressValidator.class.getName(), mapper.map("HostAddressValidator"));
        assertEquals(IsPortValidator.class.getName(), mapper.map("IsPortValidator"));
        assertEquals(NotEmptyValidator.class.getName(), mapper.map("NotEmptyValidator"));
        assertEquals(PasswordEqualityValidator.class.getName(), mapper.map("PasswordEqualityValidator"));
        assertEquals(PortValidator.class.getName(), mapper.map("PortValidator"));
        assertEquals(RegularExpressionValidator.class.getName(), mapper.map("RegularExpressionValidator"));
    }

    /**
     * Tests the mapping of processor simple names to their fully qualified names.
     */
    @Test
    public void testProcessors()
    {
        assertEquals(PasswordEncryptionProcessor.class.getName(), mapper.map("PasswordEncryptionProcessor"));
        assertEquals(PortProcessor.class.getName(), mapper.map("PortProcessor"));
        assertEquals(UnixGroupProcessor.class.getName(), mapper.map("UnixGroupProcessor"));
        assertEquals(UnixUserProcessor.class.getName(), mapper.map("UnixUserProcessor"));
    }

    /**
     * Tests the mapping of panel simple names to their fully qualified names.
     */
    @Test
    public void testIzPanels()
    {
        assertEquals(CheckedHelloPanel.class.getName(), mapper.map("CheckedHelloPanel"));
        assertEquals(CompilePanel.class.getName(), mapper.map("CompilePanel"));
        assertEquals(DataCheckPanel.class.getName(), mapper.map("DataCheckPanel"));
        assertEquals(DefaultTargetPanel.class.getName(), mapper.map("DefaultTargetPanel"));
        assertEquals(DownloadPanel.class.getName(), mapper.map("DownloadPanel"));
        assertEquals(ExtendedInstallPanel.class.getName(), mapper.map("ExtendedInstallPanel"));
        assertEquals(FinishPanel.class.getName(), mapper.map("FinishPanel"));
        assertEquals(HTMLHelloPanel.class.getName(), mapper.map("HTMLHelloPanel"));
        assertEquals(HTMLInfoPanel.class.getName(), mapper.map("HTMLInfoPanel"));
        assertEquals(HTMLLicencePanel.class.getName(), mapper.map("HTMLLicencePanel"));
        assertEquals(HelloPanel.class.getName(), mapper.map("HelloPanel"));
        assertEquals(ImgPacksPanel.class.getName(), mapper.map("ImgPacksPanel"));
        assertEquals(InfoPanel.class.getName(), mapper.map("InfoPanel"));
        assertEquals(InstallationGroupPanel.class.getName(), mapper.map("InstallationGroupPanel"));
        assertEquals(InstallationTypePanel.class.getName(), mapper.map("InstallationTypePanel"));
        assertEquals(InstallPanel.class.getName(), mapper.map("InstallPanel"));
        assertEquals(JDKPathPanel.class.getName(), mapper.map("JDKPathPanel"));
        assertEquals(LicencePanel.class.getName(), mapper.map("LicencePanel"));
        assertEquals(PacksPanel.class.getName(), mapper.map("PacksPanel"));
        assertEquals(ProcessPanel.class.getName(), mapper.map("ProcessPanel"));
        assertEquals(SelectPrinterPanel.class.getName(), mapper.map("SelectPrinterPanel"));
        assertEquals(ShortcutPanel.class.getName(), mapper.map("ShortcutPanel"));
        assertEquals(SimpleFinishPanel.class.getName(), mapper.map("SimpleFinishPanel"));
        assertEquals(SudoPanel.class.getName(), mapper.map("SudoPanel"));
        assertEquals(SummaryPanel.class.getName(), mapper.map("SummaryPanel"));
        assertEquals(TargetPanel.class.getName(), mapper.map("TargetPanel"));
        assertEquals(TreePacksPanel.class.getName(), mapper.map("TreePacksPanel"));
        assertEquals(UserInputPanel.class.getName(), mapper.map("UserInputPanel"));
        assertEquals(UserPathPanel.class.getName(), mapper.map("UserPathPanel"));
        assertEquals(XInfoPanel.class.getName(), mapper.map("XInfoPanel"));
    }

    /**
     * Verifies that null is returned if no mapping exists.
     */
    @Test
    public void testNoMapping()
    {
        assertNull(mapper.map("NoMapping"));
        assertNull(mapper.map(HelloPanel.class.getName()));
    }
}

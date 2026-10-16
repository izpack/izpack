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

import static org.assertj.core.api.Assertions.assertThat;

import com.izforge.izpack.event.*;
import com.izforge.izpack.installer.web.DownloadPanel;
import com.izforge.izpack.panels.checkedhello.CheckedHelloPanel;
import com.izforge.izpack.panels.compile.CompilePanel;
import com.izforge.izpack.panels.datacheck.DataCheckPanel;
import com.izforge.izpack.panels.defaulttarget.DefaultTargetPanel;
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
import com.izforge.izpack.panels.userinput.validator.*;
import com.izforge.izpack.panels.userpath.UserPathPanel;
import com.izforge.izpack.panels.xinfo.XInfoPanel;
import org.junit.jupiter.api.Test;

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
        assertThat(mapper.map("AntActionInstallerListener")).isEqualTo(AntActionInstallerListener.class.getName());
        assertThat(mapper.map("BSFInstallerListener")).isEqualTo(BSFInstallerListener.class.getName());
        assertThat(mapper.map("ConfigurationInstallerListener")).isEqualTo(ConfigurationInstallerListener.class.getName());
        assertThat(mapper.map("ProgressBarInstallerListener")).isEqualTo(ProgressBarInstallerListener.class.getName());
        assertThat(mapper.map("RegistryInstallerListener")).isEqualTo(RegistryInstallerListener.class.getName());
        assertThat(mapper.map("SummaryLoggerInstallerListener")).isEqualTo(SummaryLoggerInstallerListener.class.getName());
    }

    /**
     * Tests the mapping of uninstaller listener simple names to their fully qualified names.
     */
    @Test
    public void testUninstallerListeners()
    {
        assertThat(mapper.map("AntActionUninstallerListener")).isEqualTo(AntActionUninstallerListener.class.getName());
        assertThat(mapper.map("BSFUninstallerListener")).isEqualTo(BSFUninstallerListener.class.getName());
        assertThat(mapper.map("RegistryUninstallerListener")).isEqualTo(RegistryUninstallerListener.class.getName());
    }

    /**
     * Tests the mapping of validator simple names to their fully qualified names.
     */
    @Test
    public void testValidators()
    {
        assertThat(mapper.map("HostAddressValidator")).isEqualTo(HostAddressValidator.class.getName());
        assertThat(mapper.map("IsPortValidator")).isEqualTo(IsPortValidator.class.getName());
        assertThat(mapper.map("NotEmptyValidator")).isEqualTo(NotEmptyValidator.class.getName());
        assertThat(mapper.map("PasswordEqualityValidator")).isEqualTo(PasswordEqualityValidator.class.getName());
        assertThat(mapper.map("PortValidator")).isEqualTo(PortValidator.class.getName());
        assertThat(mapper.map("RegularExpressionValidator")).isEqualTo(RegularExpressionValidator.class.getName());
    }

    /**
     * Tests the mapping of processor simple names to their fully qualified names.
     */
    @Test
    public void testProcessors()
    {
        assertThat(mapper.map("PasswordEncryptionProcessor")).isEqualTo(PasswordEncryptionProcessor.class.getName());
        assertThat(mapper.map("PortProcessor")).isEqualTo(PortProcessor.class.getName());
        assertThat(mapper.map("UnixGroupProcessor")).isEqualTo(UnixGroupProcessor.class.getName());
        assertThat(mapper.map("UnixUserProcessor")).isEqualTo(UnixUserProcessor.class.getName());
    }

    /**
     * Tests the mapping of panel simple names to their fully qualified names.
     */
    @Test
    public void testIzPanels()
    {
        assertThat(mapper.map("CheckedHelloPanel")).isEqualTo(CheckedHelloPanel.class.getName());
        assertThat(mapper.map("CompilePanel")).isEqualTo(CompilePanel.class.getName());
        assertThat(mapper.map("DataCheckPanel")).isEqualTo(DataCheckPanel.class.getName());
        assertThat(mapper.map("DefaultTargetPanel")).isEqualTo(DefaultTargetPanel.class.getName());
        assertThat(mapper.map("DownloadPanel")).isEqualTo(DownloadPanel.class.getName());
        assertThat(mapper.map("FinishPanel")).isEqualTo(FinishPanel.class.getName());
        assertThat(mapper.map("HTMLHelloPanel")).isEqualTo(HTMLHelloPanel.class.getName());
        assertThat(mapper.map("HTMLInfoPanel")).isEqualTo(HTMLInfoPanel.class.getName());
        assertThat(mapper.map("HTMLLicencePanel")).isEqualTo(HTMLLicencePanel.class.getName());
        assertThat(mapper.map("HelloPanel")).isEqualTo(HelloPanel.class.getName());
        assertThat(mapper.map("ImgPacksPanel")).isEqualTo(ImgPacksPanel.class.getName());
        assertThat(mapper.map("InfoPanel")).isEqualTo(InfoPanel.class.getName());
        assertThat(mapper.map("InstallationGroupPanel")).isEqualTo(InstallationGroupPanel.class.getName());
        assertThat(mapper.map("InstallationTypePanel")).isEqualTo(InstallationTypePanel.class.getName());
        assertThat(mapper.map("InstallPanel")).isEqualTo(InstallPanel.class.getName());
        assertThat(mapper.map("JDKPathPanel")).isEqualTo(JDKPathPanel.class.getName());
        assertThat(mapper.map("LicencePanel")).isEqualTo(LicencePanel.class.getName());
        assertThat(mapper.map("PacksPanel")).isEqualTo(PacksPanel.class.getName());
        assertThat(mapper.map("ProcessPanel")).isEqualTo(ProcessPanel.class.getName());
        assertThat(mapper.map("SelectPrinterPanel")).isEqualTo(SelectPrinterPanel.class.getName());
        assertThat(mapper.map("ShortcutPanel")).isEqualTo(ShortcutPanel.class.getName());
        assertThat(mapper.map("SimpleFinishPanel")).isEqualTo(SimpleFinishPanel.class.getName());
        assertThat(mapper.map("SudoPanel")).isEqualTo(SudoPanel.class.getName());
        assertThat(mapper.map("SummaryPanel")).isEqualTo(SummaryPanel.class.getName());
        assertThat(mapper.map("TargetPanel")).isEqualTo(TargetPanel.class.getName());
        assertThat(mapper.map("TreePacksPanel")).isEqualTo(TreePacksPanel.class.getName());
        assertThat(mapper.map("UserInputPanel")).isEqualTo(UserInputPanel.class.getName());
        assertThat(mapper.map("UserPathPanel")).isEqualTo(UserPathPanel.class.getName());
        assertThat(mapper.map("XInfoPanel")).isEqualTo(XInfoPanel.class.getName());
    }

    /**
     * Verifies that null is returned if no mapping exists.
     */
    @Test
    public void testNoMapping()
    {
        assertThat(mapper.map("NoMapping")).isNull();
        assertThat(mapper.map(HelloPanel.class.getName())).isNull();
    }
}

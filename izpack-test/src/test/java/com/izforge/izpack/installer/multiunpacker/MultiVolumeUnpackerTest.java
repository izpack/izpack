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

package com.izforge.izpack.installer.multiunpacker;

import com.izforge.izpack.api.data.*;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.listener.PackagerListener;
import com.izforge.izpack.compiler.merge.CompilerPathResolver;
import com.izforge.izpack.compiler.packager.impl.MultiVolumePackager;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.io.VolumeLocator;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.event.InstallerListeners;
import com.izforge.izpack.installer.unpacker.ConsolePackResources;
import com.izforge.izpack.installer.unpacker.FileQueueFactory;
import com.izforge.izpack.installer.unpacker.PackResources;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.test.util.TestHelper;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.PlatformModelMatcher;
import com.izforge.izpack.util.Platforms;
import org.apache.commons.io.FilenameUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.izforge.izpack.test.util.TestHelper.assertFileEquals;
import static com.izforge.izpack.test.util.TestHelper.assertFileNotExists;
import static org.junit.Assert.*;

/**
 * Tests the {@link MultiVolumeUnpacker}.
 *
 * @author Tim Anderson
 */
public class MultiVolumeUnpackerTest
{
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();


    /**
     * Tests unpacking of multiple volume installation.
     *
     * @throws Exception for any error
     */
    @Test
    public void testUnpack() throws Exception
    {
        File baseDir = temporaryFolder.getRoot();
        File packageDir = new File(baseDir, "package");
        File installerJar = new File(packageDir, "installer.jar");
        File installDir = new File(baseDir, "install");
        assertTrue(packageDir.mkdir());

        // create some packs
        File file1 = createFile(baseDir, "file1.dat", 1024);
        File file2 = createFile(baseDir, "file2.dat", 2048);
        File file3 = createFile(baseDir, "file3.dat", 4096);
        PackInfo base = createPack("base", baseDir, file1, file2, file3);

        File file4 = createFile(baseDir, "file4.dat", 8192);
        File file5 = createFile(baseDir, "file5.dat", 16384);
        File file6 = createFile(baseDir, "file6.dat", 32768);
        PackInfo pack1 = createPack("pack1", baseDir, file4, file5, file6);

        File file7 = createFile(baseDir, "file7.dat", 65536);
        File file8 = createFile(baseDir, "file8.dat", 131072);
        File file9 = createFile(baseDir, "file9.dat", 262144);
        PackInfo pack2 = createPack("pack2", baseDir, file7, file8, file9);

        // pack3 is loose - i.e. content should not be stored in the volumes
        File file10 = createFile(baseDir, "file10.dat", 100);
        PackInfo pack3 = createPack("pack3", baseDir, file10);
        pack3.getPack().setLoose(true);

        MultiVolumePackager packager = createPackager(baseDir, installerJar);

        long firstVolumeSize = 40000;
        long maxVolumeSize = 100000;
        packager.setMaxFirstVolumeSize(firstVolumeSize);
        packager.setMaxVolumeSize(maxVolumeSize);

        packager.addPack(base);
        packager.addPack(pack1);
        packager.addPack(pack2);
        packager.addPack(pack3);

        // package the installer
        packager.createInstaller();

        // verify the installer exists
        assertTrue(installerJar.exists());

        // verify the installer volumes have been created
        Resources resources = createResources(installerJar);
        checkVolumes(packageDir, resources, firstVolumeSize, maxVolumeSize);

        // verify the loose pack files are present
        assertTrue(new File(packageDir, file10.getName()).exists());

        // unpack the installer
        AutomatedInstallData installData = createInstallData(packageDir, installDir, resources);
        setSelectedPacks(installData, "base", "pack2", "pack3");  // exclude pack1 from installation
        MultiVolumeUnpacker unpacker = createUnpacker(resources, installData);
        unpacker.unpack();

        // verify the expected files exists in the installation directory
        checkInstalled(installDir, file1);
        checkInstalled(installDir, file2);
        checkInstalled(installDir, file3);
        checkInstalled(installDir, file7);
        checkInstalled(installDir, file8);
        checkInstalled(installDir, file9);
        checkInstalled(installDir, file10); // loose pack file

        // verify the pack1 files are not installed
        assertFileNotExists(installDir, file4.getName());
        assertFileNotExists(installDir, file5.getName());
        assertFileNotExists(installDir, file6.getName());
    }
    
    /**
     * Tests unpacking of multiple volume installation with executables in pack.
     *
     * @throws Exception for any error
     */
    @Test
    public void testUnpackWithExecutables() throws Exception
    {
        File baseDir = temporaryFolder.getRoot();
        File packageDir = new File(baseDir, "package");
        File installerJar = new File(packageDir, "installer.jar");
        File installDir = new File(baseDir, "install");
        assertTrue(packageDir.mkdir());

        // create some packs
        File file1 = createFile(baseDir, "file1.dat", 1024);
        File file2 = createFile(baseDir, "file2.dat", 2048);
        File file3 = createFile(baseDir, "file3.dat", 4096);
        // this is an executable
        File file3e = createFile(baseDir, "file3.exe", 1024);
        PackInfo base = createPack("base", baseDir, file1, file2, file3, file3e);

        File file4 = createFile(baseDir, "file4.dat", 8192);
        File file5 = createFile(baseDir, "file5.dat", 16384);
        File file6 = createFile(baseDir, "file6.dat", 32768);
        // this is an executable
        File file6e = createFile(baseDir, "file6.exe", 1024);
        PackInfo pack1 = createPack("pack1", baseDir, file4, file5, file6, file6e);

        File file7 = createFile(baseDir, "file7.dat", 65536);
        File file8 = createFile(baseDir, "file8.dat", 131072);
        File file9 = createFile(baseDir, "file9.dat", 262144);
        // this is an executable
        File file9e = createFile(baseDir, "file9.exe", 1024);
        PackInfo pack2 = createPack("pack2", baseDir, file7, file8, file9, file9e);

        // pack3 is loose - i.e. content should not be stored in the volumes
        File file10 = createFile(baseDir, "file10.dat", 100);
        PackInfo pack3 = createPack("pack3", baseDir, file10);
        pack3.getPack().setLoose(true);

        MultiVolumePackager packager = createPackager(baseDir, installerJar);

        long firstVolumeSize = 40000;
        long maxVolumeSize = 100000;
        packager.setMaxFirstVolumeSize(firstVolumeSize);
        packager.setMaxVolumeSize(maxVolumeSize);

        packager.addPack(base);
        packager.addPack(pack1);
        packager.addPack(pack2);
        packager.addPack(pack3);

        // package the installer
        packager.createInstaller();

        // verify the installer exists
        assertTrue(installerJar.exists());

        // verify the installer volumes have been created
        Resources resources = createResources(installerJar);
        checkVolumes(packageDir, resources, firstVolumeSize, maxVolumeSize);

        // verify the loose pack files are present
        assertTrue(new File(packageDir, file10.getName()).exists());

        // unpack the installer
        AutomatedInstallData installData = createInstallData(packageDir, installDir, resources);
        setSelectedPacks(installData, "base", "pack2", "pack3");  // exclude pack1 from installation
        TestMultiVolumeUnpacker unpacker = createUnpacker(resources, installData);

        TestMultiVolumeUnpacker spy = Mockito.spy(unpacker);
        spy.unpack();
        
        Mockito.verify(spy, Mockito.times(3)).readExecutableFiles(Mockito.any(PackInfo.class), Mockito.anyList());

        // verify the expected files exists in the installation directory
        checkInstalled(installDir, file1);
        checkInstalled(installDir, file2);
        checkInstalled(installDir, file3);
        checkInstalled(installDir, file7);
        checkInstalled(installDir, file8);
        checkInstalled(installDir, file9);
        checkInstalled(installDir, file10); // loose pack file

        // verify the pack1 files are not installed
        assertFileNotExists(installDir, file3e.getName());
        assertFileNotExists(installDir, file4.getName());
        assertFileNotExists(installDir, file5.getName());
        assertFileNotExists(installDir, file6.getName());
        assertFileNotExists(installDir, file6e.getName());
        assertFileNotExists(installDir, file9e.getName());
    }


    /**
     * Helper to set the selected packs.
     *
     * @param installData the installation data
     * @param names       the names of the packs to select
     */
    private void setSelectedPacks(AutomatedInstallData installData, String... names)
    {
        for (String name : names)
        {
            for (Pack pack : installData.getAvailablePacks())
            {
                if (pack.getName().equals(name))
                {
                    installData.getSelectedPacks().add(pack);
                    break;
                }
            }
        }
        assertEquals(names.length, installData.getSelectedPacks().size());
    }

    /**
     * Creates a new pack.
     *
     * @param name the pack name
     * @return a new pack
     */
    private PackInfo createPack(String name, File baseDir, File... files) throws IOException
    {
        PackInfo pack = new PackInfo(name, name, "The " + name + " package", false, false, null, true, 0);
        addFiles(pack, baseDir, files);
        return pack;
    }

    /**
     * Verifies that there are multiple volumes of the expected size.
     *
     * @param dir                the directory to find the volumes in
     * @param resources          the resources used to determine the volume name and count
     * @param maxFirstVolumeSize the maximum size of the first volume
     * @param maxVolumeSize      the maximum volume size for subsequent volumes
     * @throws IOException for any I/O error
     */
    private void checkVolumes(File dir, Resources resources, long maxFirstVolumeSize, long maxVolumeSize)
            throws IOException
    {
        // get the volume information
        ObjectInputStream info = new ObjectInputStream(resources.getInputStream(MultiVolumeUnpacker.VOLUMES_INFO));
        int count = info.readInt();
        String name = info.readUTF();
        info.close();
        assertTrue(count >= 1);

        // verify the primary volume exists, with the expected size
        File volume = new File(dir, name);
        assertTrue(volume.exists());
        assertEquals(maxFirstVolumeSize, volume.length());

        // check the existence and size of the remaining volumes
        for (int i = 1; i < count; ++i)
        {
            volume = new File(dir, name + "." + i);
            assertTrue(volume.exists());
            if (i < count - 1)
            {
                // can't check the size of the last volume
                assertEquals(maxVolumeSize, volume.length());
            }
        }
    }

    /**
     * Creates a new unpacker.
     *
     * @param resources   the resources
     * @param installData the installation data
     * @return a new unpacker
     */
    private TestMultiVolumeUnpacker createUnpacker(Resources resources, AutomatedInstallData installData)
    {
        VariableSubstitutor replacer = new VariableSubstitutorImpl(installData.getVariables());
        Housekeeper housekeeper = Mockito.mock(Housekeeper.class);
        RulesEngine rules = Mockito.mock(RulesEngine.class);
        UninstallData uninstallData = new UninstallData();
        Librarian librarian = Mockito.mock(Librarian.class);
        VolumeLocator locator = Mockito.mock(VolumeLocator.class);
        PackResources packResources = new ConsolePackResources(resources, installData);
        FileQueueFactory queue = new FileQueueFactory(Platforms.WINDOWS, librarian);
        Prompt prompt = Mockito.mock(Prompt.class);
        InstallerListeners listeners = new InstallerListeners(installData, prompt);
        PlatformModelMatcher matcher = new PlatformModelMatcher(new Platforms(), Platforms.WINDOWS);
        TestMultiVolumeUnpacker unpacker = new TestMultiVolumeUnpacker(installData, packResources, rules, replacer,
                                                               uninstallData, queue, housekeeper,
                                                               listeners, prompt, locator, matcher);
        unpacker.setProgressListener(Mockito.mock(ProgressListener.class));
        return unpacker;
    }
    
    /**
     * This unpacker has a validation of the executables.
     *
     */
    private static class TestMultiVolumeUnpacker extends MultiVolumeUnpacker {

		public TestMultiVolumeUnpacker(com.izforge.izpack.api.data.InstallData installData, PackResources resources,
                                       RulesEngine rules, VariableSubstitutor variableSubstitutor, UninstallData uninstallData,
                                       FileQueueFactory queue, Housekeeper housekeeper, InstallerListeners listeners, Prompt prompt,
                                       VolumeLocator locator, PlatformModelMatcher matcher) {
			super(installData, resources, rules, variableSubstitutor, uninstallData, queue, housekeeper, listeners, prompt, locator,
					matcher);
		}

		@Override
		protected void readExecutableFiles(PackInfo packInfo, List<ExecutableFile> executables)
        {
			super.readExecutableFiles(packInfo, executables);
			
			assertTrue(executables.size() < 2);
		}
    }

    /**
     * Creates the installation data.
     *
     * @param mediaDir   the directory to locate volumes
     * @param installDir the installation directory
     * @param resources  the resources
     * @return the installation data
     * @throws IOException for any I/O error
     * @throws ClassNotFoundException a class has not been found
     */
    private AutomatedInstallData createInstallData(File mediaDir, File installDir, Resources resources)
            throws IOException, ClassNotFoundException
    {
        AutomatedInstallData installData = new AutomatedInstallData(new DefaultVariables(), Platforms.LINUX);

        installData.setInstallPath(installDir.getPath());
        installData.setMediaPath(mediaDir.getPath());
        installData.setInfo(new Info());
        InputStream langPack = getClass().getResourceAsStream("/com/izforge/izpack/bin/langpacks/installer/eng.xml");
        assertNotNull(langPack);
        installData.setMessages(new LocaleDatabase(langPack, Mockito.mock(Locales.class)));
        getPacks(resources).forEach(installData.getAllPacks()::add);
        installData.updateAvailablePacks(pack -> true);
        return installData;
    }

    /**
     * Creates a new {@link Resources} that reads resources from the supplied jar.
     *
     * @param installerJar the installer jar.
     * @return a new resource manager
     * @throws IOException for any I/O error
     */
    private Resources createResources(File installerJar) throws IOException
    {
        final URLClassLoader loader = new URLClassLoader(new URL[]{installerJar.toURI().toURL()},
                                                         getClass().getClassLoader());
        return new ResourceManager(loader);
    }

    /**
     * Creates a {@link MultiVolumePackager}.
     *
     * @param baseDir      the base directory
     * @param installerJar the jar to create
     * @return a new packager
     * @throws IOException for any I/O error
     */
    private MultiVolumePackager createPackager(File baseDir, File installerJar) throws IOException
    {
        Properties properties = new Properties();
        PackagerListener packagerListener = Mockito.mock(PackagerListener.class);
        MergeManager mergeManager = Mockito.mock(MergeManager.class);
        CompilerPathResolver resolver = Mockito.mock(CompilerPathResolver.class);
        MergeableResolver mergeableResolver = Mockito.mock(MergeableResolver.class);
        CompilerData data = new CompilerData(null, baseDir.getPath(), installerJar.getPath(), true);
        RulesEngine rulesEngine = Mockito.mock(RulesEngine.class);
        MultiVolumePackager packager = new MultiVolumePackager(properties, packagerListener, mergeManager,
                                                               resolver, mergeableResolver, data, rulesEngine);
        packager.setInfo(new Info());
        return packager;
    }
    
    
    /**
     * Helper to add files to a pack.
     *
     * @param pack    the pack to add the files to
     * @param baseDir the base directory
     * @param files   the files to add
     * @throws IOException for any I/O error
     */
    private void addFiles(PackInfo pack, File baseDir, File... files) throws IOException
    {
        for (File file : files)
        {
        	if ("exe".equals(FilenameUtils.getExtension(file.getName()))) {
                ExecutableFile executable = new ExecutableFile();
                executable.path = file.getPath();
                executable.executionStage = ExecutableFile.UNINSTALL;
                
        		pack.addExecutable(executable);
        	}
        	else {
	            pack.addFile(baseDir, file, "$INSTALL_PATH/" + file.getName(), null, OverrideType.OVERRIDE_FALSE, null,
	                         Blockable.BLOCKABLE_NONE, null, null);
        	}
        }
    }

    /**
     * Helper to read the pack meta-data.
     *
     * @param resources the resources
     * @return the pack meta-data
     * @throws IOException            for any I/O error
     * @throws ClassNotFoundException if the class of a serialized object cannot be found
     */
    private List<Pack> getPacks(Resources resources) throws IOException, ClassNotFoundException
    {
        // We read the packs data
        InputStream in = resources.getInputStream("packs.info");
        ObjectInputStream objIn = new ObjectInputStream(in);
        List<PackInfo> packsInfo = (List<PackInfo>) objIn.readObject();
        objIn.close();
        List<Pack> packs = new ArrayList<Pack>();
        for (PackInfo packInfo : packsInfo)
        {
            Pack pack = packInfo.getPack();
            packs.add(pack);
        }
        return packs;
    }

    /**
     * Helper to create a file with the specified size containing random data.
     *
     * @param baseDir the base directory
     * @param name    the file name
     * @param size    the file size
     * @return a new file
     * @throws IOException for any I/O error
     */
    private File createFile(File baseDir, String name, int size) throws IOException
    {
        return TestHelper.createFile(new File(baseDir, name), size);
    }

    /**
     * Verifies a file has been installed.
     *
     * @param installDir the installation directory
     * @param expected   the file that should be installed
     */
    private void checkInstalled(File installDir, File expected)
    {
        File file = new File(installDir, expected.getName());
        assertFileEquals(expected, file);
    }

}

/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Daniel Abson
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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.event.ProgressNotifiers;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.test.RunOn;
import com.izforge.izpack.test.junit.PlatformRunner;
import com.izforge.izpack.util.FileUtil;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.Platforms;
import com.izforge.izpack.util.file.DirectoryScanner;
import com.izforge.izpack.util.file.FileNameMapper;
import com.izforge.izpack.util.file.types.FileSet;

@RunWith(PlatformRunner.class)
public class ConfigurationInstallerListenerTest {
	
	private static InstallData installData;
	private static Resources resources;
	private static UninstallData uninstallData;
	private static VariableSubstitutor replacer;
	private static ProgressNotifiers notifiers;
	
	private static File tempDir;
	private static File foofile;
	private static File barfile;
	private static File bazfile;


	private IXMLElement testXml;
	private ConfigurationInstallerListener listener;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {		
        Variables variables = new DefaultVariables(new Properties());
        replacer = new VariableSubstitutorImpl(variables);

        AutomatedInstallData data = new AutomatedInstallData(variables, Platforms.DEBIAN_LINUX);
        installData = data;
        tempDir = createTempDir("izpack-test");
        installData.setInstallPath(tempDir.getPath());        

        resources = Mockito.mock(Resources.class);
        uninstallData = new UninstallData();
        notifiers = Mockito.mock(ProgressNotifiers.class);
        
    	foofile = File.createTempFile("foofile", "", tempDir);
    	barfile = File.createTempFile("barfile", "", tempDir);
    	bazfile = File.createTempFile("bazfile", "", tempDir);
        
        //workaround for XML debugging
        //http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6982772
        //com.sun.org.apache.xml.internal.security.Init.init();
	}

	@Before
	public void setUp() throws Exception
	{
		testXml = new XMLElementImpl("test");
		testXml.setAttribute("attr1", "value1");
		listener = new ConfigurationInstallerListener(installData, uninstallData, resources, replacer, notifiers);
	}
	
	@Test
	public void testGetAttribute() 
	{
		String result = null;
		result = listener.getAttribute(testXml, "attr1");
		assertNotNull(result);
		assertEquals("value1", result);
		
		assertNull(listener.getAttribute(testXml, "attr2"));
	}

	@Test
	public void testRequireAttribute() throws Exception 
	{
		String result = null;
		result = listener.requireAttribute(testXml, "attr1");
		assertNotNull(result);
		assertEquals("value1", result);

		try
		{
			listener.requireAttribute(testXml, "attr2");
			fail("Expected requireAttribute to throw exception for missing 'attr2' attribute");
		}
		catch (InstallerException e)
		{
			//success
		}
	}

	@Test
	public void testGetBooleanAttribute() {
		testXml.setAttribute("bool1", "true");
		testXml.setAttribute("bool1a", "TRUE");
		testXml.setAttribute("bool1b", "tRuE");
		testXml.setAttribute("bool2", "1");
		testXml.setAttribute("bool3", "on");
		testXml.setAttribute("bool4", "yes");
		testXml.setAttribute("bool5", "false");
		testXml.setAttribute("bool6", "0");

		// XSD true is "true" or "1"
		assertTrue(listener.getBooleanAttribute(testXml, "bool1"));
		assertTrue(listener.getBooleanAttribute(testXml, "bool1a"));
		assertTrue(listener.getBooleanAttribute(testXml, "bool1b"));
		assertTrue(listener.getBooleanAttribute(testXml, "bool2"));
		
		// XSD false is anything else, including "on" or "yes"
		assertFalse(listener.getBooleanAttribute(testXml, "bool3"));
		assertFalse(listener.getBooleanAttribute(testXml, "bool4"));
		assertFalse(listener.getBooleanAttribute(testXml, "bool5"));
		assertFalse(listener.getBooleanAttribute(testXml, "bool6"));
		assertFalse(listener.getBooleanAttribute(testXml, "attr1"));
		
		// should return null if attribute not found
		assertNull(listener.getBooleanAttribute(testXml, "attr2"));
	}

	@Test
	public void testReadFileSets() throws Exception 
	{
		List<FileSet> filesets = null;
		String[] files = null;
		DirectoryScanner scanner = null;
		List<IXMLElement> filesetsSpec = new ArrayList<IXMLElement>(6);
		
		//test 1 file in default directory, with all default settings
		IXMLElement fs1 = buildFileSetXml(foofile.getName(), null, null, null, null, null, null);
		filesetsSpec.add(fs1);
		filesets = listener.readFileSets(filesetsSpec);
		assertNotNull(filesets);
		assertEquals(1, filesets.size());
		scanner = filesets.get(0).getDirectoryScanner();
		assertNotNull(scanner);
		files = scanner.getIncludedFiles();
		assertNotNull(files);
		assertEquals(1, files.length);
		assertEquals(foofile.getName(), files[0]);
		assertTrue(filesets.get(0).getDefaultexcludes());
		assertTrue(filesets.get(0).isFollowSymlinks());
		assertTrue(scanner.isCaseSensitive());			
		
		//test exclude 1 file in default directory, with non-default settings
		IXMLElement fs2 = buildFileSetXml(null, ".", null, bazfile.getName(), "false", "false", "false");
		filesetsSpec.set(0, fs2);
		filesets = listener.readFileSets(filesetsSpec);
		assertNotNull(filesets);
		assertEquals(1, filesets.size());		
		testIncludesFooBarFiles(filesets.get(0));
		assertFalse(filesets.get(0).getDefaultexcludes());
		assertFalse(filesets.get(0).isFollowSymlinks());
		assertFalse(filesets.get(0).getDirectoryScanner().isCaseSensitive());
		
		//test exclude 1 file in default directory using nested exclude
		IXMLElement fs3 = buildFileSetXml(null, ".", null, null, null, null, null);
		addNestedExcludes(fs3, bazfile.getName());
		filesetsSpec.set(0, fs3);
		filesets = listener.readFileSets(filesetsSpec);
		assertNotNull(filesets);
		assertEquals(1, filesets.size());		
		testIncludesFooBarFiles(filesets.get(0));		
		
		//test include 2 files in default directory
		IXMLElement fs4 = buildFileSetXml(null, ".", foofile.getName() + " " + barfile.getName(), null, null, null, null);
		filesetsSpec.set(0, fs4);
		filesets = listener.readFileSets(filesetsSpec);
		assertNotNull(filesets);
		assertEquals(1, filesets.size());		
		testIncludesFooBarFiles(filesets.get(0));
		
		//test include 2 files in default directory using nested includes
		IXMLElement fs5 = buildFileSetXml(null, ".", null, null, null, null, null);
		addNestedIncludes(fs5, foofile.getName(), barfile.getName());
		filesetsSpec.set(0, fs5);
		filesets = listener.readFileSets(filesetsSpec);
		assertNotNull(filesets);
		assertEquals(1, filesets.size());		
		testIncludesFooBarFiles(filesets.get(0));
		
		//test include 1 file in non-default absolute directory
		File foofile2 = File.createTempFile("foofile", "", tempDir.getParentFile());
		IXMLElement fs6 = buildFileSetXml(null, tempDir.getParent(), foofile2.getName(), null, null, null, null);
		filesetsSpec.set(0, fs6);
		filesets = listener.readFileSets(filesetsSpec);
		assertNotNull(filesets);
		assertEquals(1, filesets.size());		
		scanner = filesets.get(0).getDirectoryScanner();
		assertNotNull(scanner);
		files = scanner.getIncludedFiles();
		assertNotNull(files);
		assertEquals(1, files.length);
		assertEquals(foofile2.getName(), files[0]);
		
		//test specify invalid file/directory combinations (both null, both specified)
		filesetsSpec.set(0, buildFileSetXml(null, null, null, null, null, null, null));
		try
		{
			filesets = listener.readFileSets(filesetsSpec);
			fail("Expected readFileSets to fail where 'dir' and 'file' are both null");
		}
		catch (InstallerException e)
		{
			//success
		}
		filesetsSpec.set(0, buildFileSetXml(foofile.getName(), ".", null, null, null, null, null));
		try
		{
			filesets = listener.readFileSets(filesetsSpec);
			fail("Expected readFileSets to fail where 'dir' and 'file' are both specified");
		}
		catch (InstallerException e)
		{
			//success
		}
		
		//test specify all filesets
		filesetsSpec.set(0, fs1);
		filesetsSpec.add(fs2);
		filesetsSpec.add(fs3);
		filesetsSpec.add(fs4);
		filesetsSpec.add(fs5);
		filesetsSpec.add(fs6);
		filesets = listener.readFileSets(filesetsSpec);
		assertNotNull(filesets);
		assertEquals(6, filesets.size());		
	}
	
	/* NON-JAVADOC
	 * Convenience method - checks that fileset contains only foofile and barfile.
	 */
	private void testIncludesFooBarFiles(FileSet fileset) throws Exception
	{
		DirectoryScanner scanner = fileset.getDirectoryScanner();
		assertNotNull(scanner);
		String[] files = scanner.getIncludedFiles();
		assertNotNull(files);
		assertEquals(2, files.length);
		Arrays.sort(files);
		assertTrue("File " + foofile.getName() + " not found in fileset", Arrays.binarySearch(files, foofile.getName()) >= 0);
		assertTrue("File " + barfile.getName() + " not found in fileset", Arrays.binarySearch(files, barfile.getName()) >= 0);		
	}

	
	/* NON-JAVADOC
	 * Constructs an XML element representing a fileset, with the specified parameters
	 */
	private IXMLElement buildFileSetXml(String file, String dir, String includes, String excludes, 
			String isCaseSensitive, String useDefaultExcludes, String followSymLinks) throws Exception
	{
		IXMLElement xml = new XMLElementImpl("fileset");
		xml.setAttribute("file", file);
		xml.setAttribute("dir", dir);
		xml.setAttribute("casesensitive", isCaseSensitive);
		xml.setAttribute("defaultexcludes", useDefaultExcludes);
		xml.setAttribute("followsymlinks", followSymLinks);
		xml.setAttribute("includes" , includes);
		xml.setAttribute("excludes", excludes);
		return xml;
	}
	
	/* NON-JAVADOC
	 * Adds <include> children to fileset XML. 
	 */
	private void addNestedIncludes(IXMLElement fs, String... nestedIncludes)
	{
		for (String include : nestedIncludes)
		{
			IXMLElement xml = new XMLElementImpl("include", fs);
			xml.setAttribute("name", include);
			fs.addChild(xml);
		}
	}
	
	/* NON-JAVADOC
	 * Adds <include> children to fileset XML. 
	 */
	private void addNestedExcludes(IXMLElement fs, String... nestedExcludes)
	{
		for (String include : nestedExcludes)
		{
			IXMLElement xml = new XMLElementImpl("exclude", fs);
			xml.setAttribute("name", include);
			fs.addChild(xml);
		}
	}
	
	/* NON-JAVADOC
	 * Creates a directory with the specified name prefix in the JMV's temporary directory.
	 * An additional, pseudo-random string is appended to guarantee the full name to be unique
	 * in this invocation of the JVM.
	 */
	private static File createTempDir(String prefix) throws IOException
	{
		File tempDir = File.createTempFile(prefix, "");
        if (!tempDir.delete())
        {
        	throw new IOException("Failed to delete temporary file " + tempDir);
        }
        if (!tempDir.mkdir())
        {
        	throw new IOException("Failed to create temporary directory " + tempDir);
        }
        return tempDir;
	}
	
	@Test
	public void testReadMappers() 
	{
		List<IXMLElement> mappersSpecs = new ArrayList<IXMLElement>();
		List<FileNameMapper> mappers = null;
		String[] results = null;
		
		//test combinations of casesensitive/handledirsep with supported mappers
		testOptionalBooleansOnMapper("identity", false);
		testOptionalBooleansOnMapper("flatten", false);
		testOptionalBooleansOnMapper("merge", false);
		testOptionalBooleansOnMapper("glob", true);
		testOptionalBooleansOnMapper("regexp", true);

		/* Test correct parsing of from/to attributes by expected behaviour */  
		//test identity
		mappersSpecs.add(buildMapperXml("identity", "qux" + File.separator + "foo*.bar", "bar*.foo", null, null));
		mappers = listener.readMappers(mappersSpecs);
		assertNotNull(mappers);
		results = mappers.get(0).mapFileName("qux" + File.separator + "foo-baz.bar");
		assertNotNull(results);
		assertEquals(1, results.length);
		assertEquals("qux" + File.separator + "foo-baz.bar", results[0]);
		//test flatten
		mappersSpecs.set(0, buildMapperXml("flatten", "qux" + File.separator + "foo*.bar", "bar*.foo", null, null));
		mappers = listener.readMappers(mappersSpecs);
		assertNotNull(mappers);
		results = mappers.get(0).mapFileName("qux" + File.separator + "foo-baz.bar");
		assertNotNull(results);
		assertEquals(1, results.length);
		assertEquals("foo-baz.bar", results[0]);
		//test merge
		mappersSpecs.set(0, buildMapperXml("merge", "qux" + File.separator + "foo*.bar", "bar*.foo", null, null));
		mappers = listener.readMappers(mappersSpecs);
		assertNotNull(mappers);
		results = mappers.get(0).mapFileName("qux" + File.separator + "foo-baz.bar");
		assertNotNull(results);
		assertEquals(1, results.length);
		assertEquals("bar*.foo", results[0]);
		results = mappers.get(0).mapFileName("quux" + File.separator + "baz-bar.foo");
		assertNotNull(results);
		assertEquals(1, results.length);
		assertEquals("bar*.foo", results[0]);
		//test glob
		mappersSpecs.set(0, buildMapperXml("glob", "qux" + File.separator + "foo*.bar", "bar*.foo", null, null));
		mappers = listener.readMappers(mappersSpecs);
		assertNotNull(mappers);
		results = mappers.get(0).mapFileName("qux" + File.separator + "foo-baz.bar");
		assertNotNull(results);
		assertEquals(1, results.length);
		assertEquals("bar-baz.foo", results[0]);
		//test regexp
		mappersSpecs.set(0, buildMapperXml("regexp", "qux[\\\\/]foo(.*)\\.bar", "bar\\1\\.foo", null, null));
		mappers = listener.readMappers(mappersSpecs);
		assertNotNull(mappers);
		results = mappers.get(0).mapFileName("qux" + File.separator + "foo-baz.bar");
		assertNotNull(results);
		assertEquals(1, results.length);
		assertEquals("bar-baz.foo", results[0]);
	}
	
	/* NON-JAVADOC
	 * Convenience method to test all combinations of casesensitive/handledirsep booleans on a
	 * specified mapper type, which may or may not support them.
	 */
	private void testOptionalBooleansOnMapper(String type, boolean supportsOptionalBooleans)
	{
		List<IXMLElement> mappersSpecs = new ArrayList<IXMLElement>();
		//when testing false, use empty string if type doesn't support setting at all
		String falseAttr = (supportsOptionalBooleans ? "false" : "");
		
		mappersSpecs.add(buildMapperXml(type, "foo", "bar", falseAttr, falseAttr));
		testCreateMapper(mappersSpecs, true); //should always work when optional booleans not used
		mappersSpecs.set(0, buildMapperXml(type, "foo", "bar", "true", falseAttr));
		testCreateMapper(mappersSpecs, supportsOptionalBooleans);
		mappersSpecs.set(0, buildMapperXml(type, "foo", "bar", falseAttr, "true"));
		testCreateMapper(mappersSpecs, supportsOptionalBooleans);		
		mappersSpecs.set(0, buildMapperXml(type, "foo", "bar", "true", "true"));
		testCreateMapper(mappersSpecs, supportsOptionalBooleans);
	}
	
	/* NON-JAVADOC
	 * Convenience method to invoke readMappers with a try-catch wrapper to handle expected to
	 * failures. Setting shouldWork=false asserts that readMappers should throw an exception.  
	 */
	private void testCreateMapper(List<IXMLElement> mapperSpecs, boolean shouldWork)
	{
		List<FileNameMapper> mappers = null;
		try
		{
			mappers = listener.readMappers(mapperSpecs);
			if (!shouldWork)
			{
				fail("Expected readMappers to fail");
			}
		}
		catch (InstallerException e)
		{
			if (shouldWork)
			{
				e.printStackTrace(new PrintStream(System.out));
				fail("Expected readMappers to succeed (" + e.getMessage() + ")");
			}
		}		
		if (shouldWork)
		{
			assertNotNull(mappers);
		}
	}
	
	/* NON-JAVADOC
	 * Convenience method to construct a <mapper> element.
	 */
	private IXMLElement buildMapperXml(String type, String from, String to, String casesensitive, String handledirsep)
	{
		IXMLElement xml = new XMLElementImpl("mapper");
		xml.setAttribute("type", type);
		xml.setAttribute("from", from);
		xml.setAttribute("to", to);
		xml.setAttribute("casesensitive", casesensitive);
		xml.setAttribute("handledirsep", handledirsep);
		return xml;
	}
	
	@Test
	public void testReadVariablesValidation()
	{
		IXMLElement variables = new XMLElementImpl("variables");
		IXMLElement variable = new XMLElementImpl("variable", variables);
		variables.addChild(variable);
				
		/* Test basic validation failures */
		verifyInvalidVariableAttributes(variables, variable, "name"); 
		verifyInvalidVariableAttributes(variables, variable, "value");
		//test ambiguous value definition
		verifyInvalidVariableAttributes(variables, variable, "name", "value", "environment");
		verifyInvalidVariableAttributes(variables, variable, "name", "value", "regkey");
		verifyInvalidVariableAttributes(variables, variable, "name", "value", "file");
		verifyInvalidVariableAttributes(variables, variable, "name", "value", "zipfile");
		verifyInvalidVariableAttributes(variables, variable, "name", "value", "jarfile");
		verifyInvalidVariableAttributes(variables, variable, "name", "value", "executable");
		//test invalid file
		verifyInvalidVariableAttributes(variables, variable, "name", "file"); 
		verifyInvalidVariableAttributes(variables, variable, "name", "file", "key"); 
		verifyInvalidVariableAttributes(variables, variable, "name", "file", "type"); 
		//test invalid zipfile
		verifyInvalidVariableAttributes(variables, variable, "name", "zipfile", "key", "type"); 
		verifyInvalidVariableAttributes(variables, variable, "name", "zipfile", "key", "entry"); 
		verifyInvalidVariableAttributes(variables, variable, "name", "zipfile", "entry", "type"); 
		//test invalid jarfile
		verifyInvalidVariableAttributes(variables, variable, "name", "jarfile", "key", "type"); 
		verifyInvalidVariableAttributes(variables, variable, "name", "jarfile", "key", "entry"); 
		verifyInvalidVariableAttributes(variables, variable, "name", "jarfile", "entry", "type"); 
		//test invalid execution type (must be process|shell)
		verifyInvalidVariableAttributes(variables, variable, "name", "executable", "type"); 		
	}

	@Test
	public void testReadVariablesEvaluation()
	{
		Properties properties = null;
		IXMLElement variables = new XMLElementImpl("variables");
		IXMLElement variable = new XMLElementImpl("variable", variables);
		variables.addChild(variable);
		variable.setAttribute("name", "variable1");
		
		//verify plain value variable
		variable.setAttribute("value", "plainValue1");
		properties = listener.readVariables(variables);
		assertNotNull(properties);
		assertEquals("plainValue1", properties.get("variable1"));
		variable.setAttribute("value", null);
		
		//verify environment variable
		variable.setAttribute("environment", "PATH");
		properties = listener.readVariables(variables);
		assertNotNull(properties);
		assertEquals(System.getenv("PATH"), properties.get("variable1"));
		variable.setAttribute("environment", null);
		
		//verify file variable
		String testFilePath = null;
		//test properties file
		testFilePath = getTestFilePath("test.properties");
		assertNotNull(testFilePath);
		variable.setAttribute("file", testFilePath);
		variable.setAttribute("type", "options");
		variable.setAttribute("key", "property3");
		properties = listener.readVariables(variables);
		assertNotNull(properties);
		assertEquals("value3", properties.get("variable1"));
		//test ini file
		testFilePath = getTestFilePath("test.ini");
		assertNotNull(testFilePath);
		variable.setAttribute("file", testFilePath);
		variable.setAttribute("type", "ini");
		variable.setAttribute("section", "section2");
		variable.setAttribute("key", "property3a");
		properties = listener.readVariables(variables);
		assertNotNull(properties);
		assertEquals("value3a", properties.get("variable1"));
		variable.setAttribute("file", null);
		variable.setAttribute("section", null);
		//TODO: test XML file
		
		//verify zipfile variable
		testFilePath = getTestFilePath("test.zip"); 
		//test properties in zip file
		variable.setAttribute("zipfile", testFilePath);
		variable.setAttribute("entry", "folderB/test.properties");
		variable.setAttribute("type", "options");
		variable.setAttribute("key", "propertyY");
		properties = listener.readVariables(variables);
		assertNotNull(properties);
		assertEquals("valueY", properties.get("variable1"));
		//test ini in zip file
		variable.setAttribute("entry", "folderC/test.ini");
		variable.setAttribute("type", "ini");
		variable.setAttribute("section", "sectionX");
		variable.setAttribute("key", "propertyZ");
		properties = listener.readVariables(variables);
		assertNotNull(properties);
		assertEquals("valueZ", properties.get("variable1"));
		variable.setAttribute("zipfile", null);
		variable.setAttribute("section", null);
		
		//verify jarfile variable
		testFilePath = getTestFilePath("test.jar"); 
		//test properties in jar file
		variable.setAttribute("jarfile", testFilePath);
		variable.setAttribute("entry", "folderB/test.properties");
		variable.setAttribute("type", "options");
		variable.setAttribute("key", "propertyY");
		properties = listener.readVariables(variables);
		assertNotNull(properties);
		assertEquals("valueY", properties.get("variable1"));
		//test ini in jar file
		variable.setAttribute("entry", "folderC/test.ini");
		variable.setAttribute("type", "ini");
		variable.setAttribute("section", "sectionX");
		variable.setAttribute("key", "propertyZ");
		properties = listener.readVariables(variables);
		assertNotNull(properties);
		assertEquals("valueZ", properties.get("variable1"));		
		variable.setAttribute("entry", null);
		variable.setAttribute("type", null);
		variable.setAttribute("key", null);
		variable.setAttribute("jarfile", null);
		
		//verify executable variable
		variable.setAttribute("executable", System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
		IXMLElement cpTag = buildTextNode(variable, "arg", "-cp");
		IXMLElement cpValueTag = buildTextNode(variable, "arg", System.getProperty("java.class.path"));
		IXMLElement classTag = buildTextNode(variable, "arg", "com.izforge.izpack.event.ConfigurationInstallerListenerTest");
		IXMLElement argTag = buildTextNode(variable, "arg", "--stdout");
		variable.addChild(cpTag);
		variable.addChild(cpValueTag);
		variable.addChild(classTag);
		variable.addChild(argTag);
		properties = listener.readVariables(variables);
		assertNotNull(properties);
		assertEquals("exeValueStdOut", properties.get("variable1").toString().trim());
		//verify using stderr attribute
		variable.setAttribute("stderr", "true");
		argTag.setContent("--stderr");
		properties = listener.readVariables(variables);
		assertNotNull(properties);
		assertEquals("exeValueStdErr", properties.get("variable1").toString().trim());
		
		//TODO test <filter> elements
	}
	
	private IXMLElement buildTextNode(IXMLElement parent, String name, String content)
	{
		IXMLElement newTag = new XMLElementImpl(name, parent);
		newTag.setContent(content);		
		return newTag;
	}
	
	private String getTestFilePath(String name)
	{
		return FileUtil.convertUrlToFilePath(this.getClass().getResource("config/" + name));
	}

	@Test
	@RunOn(Platform.Name.WINDOWS)
	public void testReadVariablesRegistry()
	{
		Properties properties = null;
		IXMLElement variables = new XMLElementImpl("variables");
		IXMLElement variable = new XMLElementImpl("variable", variables);
		variables.addChild(variable);

		//test invalid regkey
		verifyInvalidVariableAttributes(variables, variable, "name", "regkey");
		
		//verify registry variable
		variable.setAttribute("name", "regVariable");
		variable.setAttribute("regvalue", "CurrentVersion");
		variable.setAttribute("regkey", "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion");
		properties = listener.readVariables(variables);
		assertNotNull(properties);
		assertEquals(System.getProperty("os.version"), properties.getProperty("regVariable"));
		
		//verify using regroot attribute (absolute key)
		variable.setAttribute("regroot", "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows NT");
		variable.setAttribute("regkey", "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion");
		properties = listener.readVariables(variables);
		assertNotNull(properties);
		assertEquals(System.getProperty("os.version"), properties.getProperty("regVariable"));
		variable.setAttribute("regroot", null);
		
		//verify using regroot attribute (relative key)
		variable.setAttribute("regroot", "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows NT");
		variable.setAttribute("regkey", "CurrentVersion");
		properties = listener.readVariables(variables);
		assertNotNull(properties);
		assertEquals(System.getProperty("os.version"), properties.getProperty("regVariable"));
		variable.setAttribute("regroot", null);
	}
	
	/* NON-JAVADOC
	 * Convenience method: expects that variable is a child of variables. Sets all specified 
	 * attribute names to null, and fails if readVariables succeeds. Resets all specified 
	 * attributes to null after test.
	 */
	private void verifyInvalidVariableAttributes(IXMLElement variables, IXMLElement variable, String... attributes)
	{
		for (String attribute : attributes)
		{
			variable.setAttribute(attribute, "foo");
		}
		try
		{
			listener.readVariables(variables);
			fail("Expected readVariables to fail with attributes " + attributes);
		}
		catch (InstallerException e)
		{
			//success
		}
		for (String attribute : attributes)
		{
			variable.setAttribute(attribute, null);
		}
	}
	
	// TODO: implement
	/*
	@Test
	public void testReadConfigurables() {
		fail("Not yet implemented"); 
	}
	*/
	
	public static void main (String[] args)
	{
		if (args[0].equals("--stdout"))
		{
			System.out.print("exeValueStdOut");
		}
		else if (args[0].equals("--stderr"))
		{
			System.err.print("exeValueStdErr");
		}
	}
}

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

package com.izforge.izpack.util.config;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.izforge.izpack.util.FileUtil;
import com.izforge.izpack.util.config.MultiMapConfigEntry.LookupType;
import com.izforge.izpack.util.config.MultiMapConfigEntry.Operation;
import com.izforge.izpack.util.config.base.MultiMap;
import com.izforge.izpack.util.file.types.FileSet;

public class PropertyFileConfigTaskTest {
	
	private static File testProps;
	private static File testNewProps;
	
	private PropertyFileConfigTask task;
	private MultiMap<String, String> config;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//set up two test .properties file resources
        URL url = PropertyFileConfigTaskTest.class.getResource("test.properties");
        assertNotNull(url);
        testProps= new File(FileUtil.convertUrlToFilePath(url));
        
        url = PropertyFileConfigTaskTest.class.getResource("testNew.properties");
        assertNotNull(url);
        testNewProps= new File(FileUtil.convertUrlToFilePath(url));		
	}

	@Before
	public void setUp() throws Exception {
		task = new PropertyFileConfigTask();
		config = task.readFromFile(testProps);
	}

	@Test
	public void testReadFromFileFile() throws Exception
	{
		verifyTestProps(config);
		MultiMap<String, String> patchConfig = task.readFromFile(testNewProps);
		verifyTestNewProps(patchConfig);
	}
	
	private void verifyTestProps(MultiMap<String, String> config)
	{
		verifyProperty(config, "property1", "value1");
		verifyProperty(config, "property1a", "value1a");
		verifyProperty(config, "property2", "value2");
		verifyProperty(config, "property3", "value3");
		verifyProperty(config, "property3a", "value3a");
		verifyProperty(config, "property4", "value4");
		verifyProperty(config, "propertyY", "valueY");
		verifyProperty(config, "propertyZ", "valueZ");		
	}
	
	private void verifyTestNewProps(MultiMap<String, String> config)
	{
		verifyProperty(config, "property1", "valueNew1");
		verifyProperty(config, "property2", "valueNew2");
		verifyProperty(config, "property3", "valueNew3");
		verifyProperty(config, "property4", "valueNew4");
		verifyProperty(config, "property5", "value5");
		verifyProperty(config, "property6", "value6");		
	}
	
	private void verifyProperty(MultiMap<String, String> config, String name, String value)
	{
		assertNotNull("Property " + name + " should exist!", config.get(name));
		assertEquals(value, config.get(name));
	}

	@Test
	public void testWriteToFile() throws Exception
	{
		//test write unmodified config to file and read back to verify 
		File out = File.createTempFile("izpack-config-test", ".ini");
		assertTrue(out.exists());
		task.writeToFile(config, out);
		MultiMap<String, String> configCopy = task.readFromFile(out);
		verifyTestProps(configCopy);
	}

	@Test
	public void testPatch() throws Exception
	{
		//test variations on patching testNew from test
		
		//test preserve entries, preserve values
		MultiMap<String, String> patchConfig1 = task.readFromFile(testNewProps);
		task.patchPreserveEntries = true;
		task.patchPreserveValues  = true;
		task.patch(config, patchConfig1);
		verifyTestProps(patchConfig1);
		verifyProperty(patchConfig1, "property5", "value5");
		verifyProperty(patchConfig1, "property6", "value6");		
		
		//test preserve entries, replace values
		MultiMap<String, String> patchConfig2 = task.readFromFile(testNewProps);
		task.patchPreserveEntries = true;
		task.patchPreserveValues  = false;
		task.patch(config, patchConfig2);
		verifyTestNewProps(patchConfig2);
		verifyProperty(patchConfig2, "property1a", "value1a");
		verifyProperty(patchConfig2, "property3a", "value3a");
		verifyProperty(patchConfig2, "propertyY", "valueY");
		verifyProperty(patchConfig2, "propertyZ", "valueZ");
		
		//test ignore old entries, preserve values
		MultiMap<String, String> patchConfig3 = task.readFromFile(testNewProps);
		task.patchPreserveEntries = false;
		task.patchPreserveValues  = true;
		task.patch(config, patchConfig3);
		verifyProperty(patchConfig3, "property1", "value1");
		verifyProperty(patchConfig3, "property2", "value2");
		verifyProperty(patchConfig3, "property3", "value3");
		verifyProperty(patchConfig3, "property4", "value4");
		verifyProperty(patchConfig3, "property5", "value5");
		verifyProperty(patchConfig3, "property6", "value6");
		verifyNullTestPropsEntries(patchConfig3);
		
		//test ignore old entries, replace old values
		MultiMap<String, String> patchConfig4 = task.readFromFile(testNewProps);
		task.patchPreserveEntries = false;
		task.patchPreserveValues  = false;
		task.patch(config, patchConfig4);
		verifyTestNewProps(patchConfig4);
		verifyNullTestPropsEntries(patchConfig4);
	}
	
	private void verifyNullTestPropsEntries(MultiMap<String, String> config)
	{
		assertNull(config.get("propertyY"));
		assertNull(config.get("propertyZ"));		
	}

	@Test
	public void testDeleteEntry() throws Exception
	{
		//test delete key
		MultiMapConfigEntry entry1 = new MultiMapConfigEntry();
		entry1.setKey("property1a");

		//test do not delete non-matching value
		MultiMapConfigEntry entry2 = new MultiMapConfigEntry();
		entry2.setKey("property2");
		entry2.setValue("valueFoo");
		
		//test delete matching regex value
		MultiMapConfigEntry entry3 = new MultiMapConfigEntry();
		entry3.setLookupType(LookupType.REGEXP);
		entry3.setKey("property3");
		entry3.setValue("value[0-9]?");
		
		task.deleteEntry(entry1, config);
		task.deleteEntry(entry2, config);
		task.deleteEntry(entry3, config);
		verifyProperty(config, "property1", "value1");
		assertNull(config.get("property1a"));
		verifyProperty(config, "property2", "value2");
		assertNull(config.get("property3"));
		verifyProperty(config, "property3a", "value3a");
		verifyProperty(config, "property4", "value4");
		verifyProperty(config, "propertyY", "valueY");
		verifyProperty(config, "propertyZ", "valueZ");	
	}

	@Test
	public void testKeepEntry() throws Exception
	{
		MultiMap<String, String> patchConfig = task.readFromFile(testNewProps);

		//test keep key value
		MultiMapConfigEntry entry1 = new MultiMapConfigEntry();
		entry1.setKey("property1");
		
		//test keep unique key
		MultiMapConfigEntry entry2 = new MultiMapConfigEntry();
		entry2.setKey("property3a");
		
		//test keep key value matching regexp lookup
		MultiMapConfigEntry entry3 = new MultiMapConfigEntry();
		entry3.setLookupType(LookupType.REGEXP);
		entry3.setKey("property4");
		entry3.setValue("value[0-9]?");
	
		//ignore all old keys and values in initial patching
		task.patchPreserveEntries = false;
		task.patchPreserveValues  = false;
		task.patch(config, patchConfig);
		task.keepEntry(entry1, config, patchConfig);
		task.keepEntry(entry2, config, patchConfig);
		task.keepEntry(entry3, config, patchConfig);
		verifyProperty(patchConfig, "property1", "value1");
		assertNull(patchConfig.get("property1a"));
		verifyProperty(patchConfig, "property2", "valueNew2");
		verifyProperty(patchConfig, "property3", "valueNew3");
		verifyProperty(patchConfig, "property3a", "value3a");
		verifyProperty(patchConfig, "property4", "value4");		
		verifyProperty(patchConfig, "property5", "value5");
		verifyProperty(patchConfig, "property6", "value6");		
		assertNull(patchConfig.get("propertyY"));
		assertNull(patchConfig.get("propertyZ"));
	}

	@Test
	public void testInsertEntry() throws Exception
	{
		MultiMapConfigEntry entry1 = new MultiMapConfigEntry();
		entry1.setKey("property2a");
		entry1.setValue("value2a");

		task.insertEntry(entry1, config);
		verifyTestProps(config);
		verifyProperty(config, "property2a", "value2a");
	}
	
    /**
     * Skeleton functional test for batch patching, to test for an issue reported on the dev
     * mailing list.
     * 
     * @throws Exception for any error
     */
	@Test
	public void testExecute_KeepEntry() throws Exception
	{
	    task.setSrcFile(testProps);
	    task.setFile(testNewProps);
	    File output = File.createTempFile("patched", ".properties");
	    output.deleteOnExit();
	    task.setTargetFile(output);
        task.setPatchPreserveEntries(false);
        task.setPatchPreserveValues(false);
        
        MultiMapConfigEntry entry1 = new MultiMapConfigEntry();
        entry1.setKey("property2");
        entry1.setOperation(Operation.KEEP);
        task.addEntry(entry1);
        task.execute();
        
        MultiMap<String, String> patchConfig1 = task.readFromFile(output);
        verifyProperty(patchConfig1, "property2", "value2");	        
	}
	
	/**
	 * Skeleton functional test for batch patching, to test for an issue reported on the dev
	 * mailing list.
	 * 
	 * @throws Exception for any error
	 */
	@Test
	public void testExecute_Fileset() throws Exception
	{
	    FileSet srcFiles = new FileSet();
	    srcFiles.setDir(new File(PropertyFileConfigTaskTest.class.getResource("testFrom").getFile()));
	    srcFiles.setIncludes("*.properties");
	    srcFiles.setExcludes("test3*");
	    task.addFileSet(srcFiles);
	    task.setToDir(new File(PropertyFileConfigTaskTest.class.getResource("testTo").getFile()));
	    task.setPatchPreserveEntries(true);
	    task.setPatchPreserveValues(true);
	    task.execute();
	    
	    MultiMap<String, String> patchConfig1 = task.readFromFile(new File(PropertyFileConfigTaskTest.class.getResource("testTo/test1.properties").getFile()));
	    verifyProperty(patchConfig1, "1property1", "1value1");
	    verifyProperty(patchConfig1, "1propertyX", "1valueX");
        verifyProperty(patchConfig1, "1propertyZ", "1valueZ");
        MultiMap<String, String> patchConfig2 = task.readFromFile(new File(PropertyFileConfigTaskTest.class.getResource("testTo/test2.properties").getFile()));
        verifyProperty(patchConfig2, "2property1", "2value1");
        verifyProperty(patchConfig2, "2propertyY", "2valueY");
        verifyProperty(patchConfig2, "2propertyZ", "2valueZ");        
        MultiMap<String, String> patchConfig3 = task.readFromFile(new File(PropertyFileConfigTaskTest.class.getResource("testTo/test3.properties").getFile()));
        verifyProperty(patchConfig3, "3property1", "3value1a");
        assertNull("3propertyY should not exist", patchConfig3.get("3propertyY"));
        verifyProperty(patchConfig3, "3propertyZ", "3valueZ");        
	}
}

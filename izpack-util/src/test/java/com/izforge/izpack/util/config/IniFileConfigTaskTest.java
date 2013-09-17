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
import com.izforge.izpack.util.config.base.MultiMap;
import com.izforge.izpack.util.config.base.Profile.Section;

public class IniFileConfigTaskTest { 
	
	private static File testIni;
	private static File testNewIni;
	
	private IniFileConfigTask task;
	private MultiMap<String, Section> config;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		//set up two test .ini file resources
        URL url = IniFileConfigTaskTest.class.getResource("test.ini");
        assertNotNull(url);
        testIni= new File(FileUtil.convertUrlToFilePath(url));
        
        url = IniFileConfigTaskTest.class.getResource("testNew.ini");
        assertNotNull(url);
        testNewIni= new File(FileUtil.convertUrlToFilePath(url));		
	}

	@Before
	public void setUp() throws Exception
	{
		//reinitialise task and MultiMap config
		task = new IniFileConfigTask();
		config = task.readFromFile(testIni);
	}

	@Test
	public void testReadFromFile() throws Exception 
	{
		//verify read and parse test .ini files
		verifyTestIni(config);
		MultiMap<String, Section> patchConfig = task.readFromFile(testNewIni);
		verifyTestNewIni(patchConfig);
	}

	@Test
	public void testWriteToFile() throws Exception 
	{
		//test write unmodified config to file and read back to verify 
		File out = File.createTempFile("izpack-config-test", ".ini");
		assertTrue(out.exists());
		task.writeToFile(config, out);
		MultiMap<String, Section> configCopy = task.readFromFile(out);
		verifyTestIni(configCopy);
	}

	/* NON-JAVADOC
	 * Verify unmodified test.ini
	 */
	private void verifyTestIni(MultiMap<String, Section> config)
	{
		Section section1 = config.get("section1");
		assertNotNull(section1);
		assertEquals("value1", section1.get("property1"));
		assertEquals("value1a", section1.get("property1a"));
		assertEquals("value2", section1.get("property2"));
		Section section2 = config.get("section2");
		assertNotNull(section2);
		assertEquals("value3", section2.get("property3"));
		assertEquals("value3a", section2.get("property3a"));
		assertEquals("value4", section2.get("property4"));
		Section sectionX = config.get("sectionX");
		assertNotNull(sectionX);
		assertEquals("valueY", sectionX.get("propertyY"));
		assertEquals("valueZ", sectionX.get("propertyZ"));
	}

	@Test
	public void testPatch() throws Exception 
	{	
		//test variations on patching testNew from test
		
		//test preserve entries, preserve values
		MultiMap<String, Section> patchConfig1 = task.readFromFile(testNewIni);
		task.patchPreserveEntries = true;
		task.patchPreserveValues  = true;
		task.patch(config, patchConfig1);
		verifyTestIni(patchConfig1);
		Section section3 = patchConfig1.get("section3");
		assertNotNull(section3);
		assertEquals("value5", section3.get("property5"));
		assertEquals("value6", section3.get("property6"));		
		
		//test preserve entries, replace values
		MultiMap<String, Section> patchConfig2 = task.readFromFile(testNewIni);
		task.patchPreserveEntries = true;
		task.patchPreserveValues  = false;
		task.patch(config, patchConfig2);
		verifyTestNewIni(patchConfig2);
		assertEquals("value1a", patchConfig2.get("section1").get("property1a"));
		assertEquals("value3a", patchConfig2.get("section2").get("property3a"));
		Section sectionX = patchConfig2.get("sectionX");
		assertNotNull(sectionX);
		assertEquals("valueY", sectionX.get("propertyY"));
		assertEquals("valueZ", sectionX.get("propertyZ"));
		
		//test ignore old entries, preserve values
		MultiMap<String, Section> patchConfig3 = task.readFromFile(testNewIni);
		task.patchPreserveEntries = false;
		task.patchPreserveValues  = true;
		task.patch(config, patchConfig3);
		Section section1 = patchConfig3.get("section1");
		assertNotNull(section1);
		assertEquals("value1", section1.get("property1"));
		assertEquals("value2", section1.get("property2"));
		Section section2 = patchConfig3.get("section2");
		assertNotNull(section2);
		assertEquals("value3", section2.get("property3"));
		assertEquals("value4", section2.get("property4"));
		section3 = patchConfig3.get("section3");
		assertNotNull(section3);
		assertEquals("value5", section3.get("property5"));
		assertEquals("value6", section3.get("property6"));
		verifyNullTestIniEntries(patchConfig3);
		
		//test ignore old entries, replace old values
		MultiMap<String, Section> patchConfig4 = task.readFromFile(testNewIni);
		task.patchPreserveEntries = false;
		task.patchPreserveValues  = false;
		task.patch(config, patchConfig4);
		verifyTestNewIni(patchConfig4);
		verifyNullTestIniEntries(patchConfig4);
	}
	
	/* NON-JAVADOC
	 * Verify unmodified testNew.ini
	 */
	private void verifyTestNewIni(MultiMap<String, Section> config)
	{
		Section section1 = config.get("section1");
		assertNotNull(section1);
		assertEquals("valueNew1", section1.get("property1"));
		assertEquals("valueNew2", section1.get("property2"));
		Section section2 = config.get("section2");
		assertNotNull(section2);
		assertEquals("valueNew3", section2.get("property3"));
		assertEquals("valueNew4", section2.get("property4"));		
		Section section3 = config.get("section3");
		assertNotNull(section3);
		assertEquals("value5", section3.get("property5"));
		assertEquals("value6", section3.get("property6"));
	}
	
	/* NON-JAVADOC
	 * Verify test.ini unique section & keys null. 
	 */
	private void verifyNullTestIniEntries(MultiMap<String, Section>config)
	{
		assertNull(config.get("sectionX"));
		assertNull(config.get("section1").get("property1a"));
		assertNull(config.get("section2").get("property2a"));
	}

	@Test
	public void testDeleteEntry() throws Exception 
	{
		//test delete key
		MultiMapConfigEntry entry1 = new MultiMapConfigEntry();
		entry1.setSection("section1");
		entry1.setKey("property1a");
		
		//test delete section
		MultiMapConfigEntry entry2 = new MultiMapConfigEntry();
		entry2.setSection("sectionX");
		
		task.deleteEntry(entry1, config);
		task.deleteEntry(entry2, config);
		Section section1 = config.get("section1");
		assertNotNull(section1);
		assertNull(section1.get("property1a"));
		assertEquals("value1", section1.get("property1"));
		assertEquals("value2", section1.get("property2"));
		Section section2 = config.get("section2");
		assertNotNull(section2);
		assertEquals("value3", section2.get("property3"));
		assertEquals("value3a", section2.get("property3a"));
		assertEquals("value4", section2.get("property4"));
		assertNull(config.get("sectionX"));
	}

	@Test
	public void testKeepEntry() throws Exception 
	{
		MultiMap<String, Section> patchConfig1 = task.readFromFile(testNewIni);

		//test keep key
		MultiMapConfigEntry entry1 = new MultiMapConfigEntry();
		entry1.setSection("section1");
		entry1.setKey("property1");

		//test keep unique section
		MultiMapConfigEntry entry2 = new MultiMapConfigEntry();
		entry2.setSection("sectionX");

		//test keep keys/values in common section
		MultiMapConfigEntry entry3 = new MultiMapConfigEntry();
		entry3.setSection("section2");
		
		//ignore all keys and values in initial patching
		task.patchPreserveEntries = false;
		task.patchPreserveValues  = false;
		task.patch(config, patchConfig1);
		task.keepEntry(entry1, config, patchConfig1);
		task.keepEntry(entry2, config, patchConfig1);
		task.keepEntry(entry3, config, patchConfig1);
		Section section1 = patchConfig1.get("section1");
		assertNotNull(section1);
		assertEquals("value1", section1.get("property1"));
		assertNull(section1.get("property1a"));
		assertEquals("valueNew2", section1.get("property2"));
		Section section2 = patchConfig1.get("section2");
		assertNotNull(section2);
		assertEquals("value3", section2.get("property3"));
		assertEquals("value3a", section2.get("property3a"));
		assertEquals("value4", section2.get("property4"));		
		Section section3 = patchConfig1.get("section3");
		assertNotNull(section3);
		assertEquals("value5", section3.get("property5"));
		assertEquals("value6", section3.get("property6"));		
		Section sectionX = patchConfig1.get("sectionX");
		assertNotNull(sectionX);
		assertEquals("valueY", sectionX.get("propertyY"));
		assertEquals("valueZ", sectionX.get("propertyZ"));
	}

	@Test
	public void testInsertEntry() throws Exception 
	{
		//test new key in existing section
		MultiMapConfigEntry entry1 = new MultiMapConfigEntry();
		entry1.setSection("section1");
		entry1.setKey("property2a");
		entry1.setValue("value2a");

		//test new key in new section
		MultiMapConfigEntry entry2 = new MultiMapConfigEntry();
		entry2.setSection("sectionA");
		entry2.setKey("propertyB");
		entry2.setValue("valueB");
		
		task.insertEntry(entry1, config);
		task.insertEntry(entry2, config);
		verifyTestIni(config);
		Section section1 = config.get("section1");
		assertNotNull(section1);
		assertEquals("value2a", section1.get("property2a"));
		Section sectionA = config.get("sectionA");
		assertNotNull(sectionA);
		assertEquals("valueB", sectionA.get("propertyB"));
	}
}

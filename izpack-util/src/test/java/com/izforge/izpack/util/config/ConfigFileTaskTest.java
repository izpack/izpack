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

import org.junit.Before;
import org.junit.Test;

public class ConfigFileTaskTest 
{
	TestConfigFileTask task;

	@Before
	public void setUp() throws Exception 
	{
		task = new TestConfigFileTask();
	}

	@Test
	public void testValidateAttributes() 
	{
		File fooFile = new File("foo.bar");
		File barFile = new File("bar.foo");
		
		//test invalid: no toFile
		tryValidation(false, "Expected validation to fail when no toFile set");
		
		// test valid: only toFile set
		task.setToFile(fooFile);
		tryValidation(true, "Unexpected validation fail (only toFile set)");
		assertEquals(fooFile, task.getDestFile());
		assertTrue(fooFile.exists());
		fooFile.deleteOnExit();
		
		//test valid: toFile and targetFile set
		task.setTargetFile(barFile);
		tryValidation(true, "Unexpected validation fail (toFile and targetFile set)");
		assertEquals(barFile, task.getDestFile());
		
		//test valid: toFile, targetFile, and srcFile set
		task.setSrcFile(fooFile);
		tryValidation(true, "Unexpected validation fail (srcFile set to same as toFile)");
		assertNull("srcFile should be null when equal to toFile", task.srcFile);
		
		
		//test valid combinations of forceOverwrite and/or create
		tryValidation(true, "Unexpected validation fail (overwrite and create should be true by default)");
		task.setOverwrite(true);
		task.setCreate(false);
		tryValidation(true, "Unexpected validation fail (overwrite should default to true, create is false)");
		task.setOverwrite(false);
		task.setCreate(true);
		tryValidation(true, "Unexpected validation fail (overwrite is false, create is true)");		
		//test invalid combination of !forceOverwrite and !create
        task.setCreate(false);
        tryValidation(false, "Expected validation to fail when overwrite and create are false");

	}
	
	/* NON-JAVADOC
	 * Wrap validateAttributes in try/catch, and fail if exception-is-null does 
	 * not match value of shouldValidate (null exception mean successful validation).
	 */
	private void tryValidation(boolean shouldValidate, String errMessage)
	{
		Exception exception = null;
		try
		{
			task.validateAttributes();
		}
		catch (Exception e)
		{
			exception = e;
		}
		if (!(exception == null == shouldValidate))
		{
			fail(errMessage + (exception == null ? "" : ": " + exception.getMessage()));
		}
	}

	/**
	 * A dummy implementation of ConfigFileTask, for testing purposes. 
	 * 
	 * @author daniela
	 *
	 */
	protected class TestConfigFileTask extends ConfigFileTask
	{

		@Override
		protected void doFileOperation(File srcFile, File patchFile, File destFile) throws Exception 
		{
			// do nothing
		}
		
		/**
		 * Return protected {@code FileCopyTask.destFile} member for testing purposes
		 * 
		 * @return protected {@code FileCopyTask.destFile} member for testing purposes
		 */
		protected File getDestFile()
		{
			return destFile;
		}
	}

}

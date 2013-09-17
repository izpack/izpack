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

public class XmlFileConfigTaskTest {

	private XmlFileConfigTask task;
	
	@Before
	public void setUp() throws Exception {
		task = new XmlFileConfigTask();
	}

	@Test
	public void testValidateAttributes() {
		File fooFile = new File("foo.bar");
		task.setToFile(fooFile);
		
		//Test valid and invalid combinations of confProps and configFile
		task.setConfigFile(fooFile);
		tryValidation(true, "Unexpected validation fail (only configFile set)");
		task.addProperty("foo", "bar");
		tryValidation(false, "Expected validation to fail (confProp and configFile set)");
		task.setConfigFile(null);
		tryValidation(true, "Unexpected validation fail (only confProp set)");		
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
}

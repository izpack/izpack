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
package com.izforge.izpack.panels.target;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link TargetPanelHelper} class.
 *
 * @author Tim Anderson
 */
public class TargetPanelHelperTest
{
	private String orgUserDir;
	
	@Before
	public void initialize() {
		orgUserDir = System.getProperty("user.dir");
	}
	
	@After
	public void cleanup() {
		System.setProperty("user.dir", orgUserDir);
	}
	
    /**
     * Tests the {@link TargetPanelHelper#isIncompatibleInstallation(String, Boolean)} method.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testIsIncompatibleInstallation() throws IOException
    {
        File dir = File.createTempFile("junit", "");
        FileUtils.deleteQuietly(dir);

        // verify that the method returns false for non-existent directory
        assertFalse(dir.exists());
        assertFalse(TargetPanelHelper.isIncompatibleInstallation(dir.getPath(), true));

        // verify that the method returns false for existing directory
        assertTrue(dir.mkdir());
        assertFalse(TargetPanelHelper.isIncompatibleInstallation(dir.getPath(), true));

        // verify that the method returns false for valid data
        File file = new File(dir, InstallData.INSTALLATION_INFORMATION);
        FileOutputStream stream = new FileOutputStream(file);
        ObjectOutputStream objStream = new ObjectOutputStream(stream);
        objStream.writeObject(new ArrayList<Pack>());
        objStream.close();
        assertFalse(TargetPanelHelper.isIncompatibleInstallation(dir.getPath(), true));

        // verify that the method returns true for invalid data
        assertTrue(file.delete());
        stream = new FileOutputStream(file);
        objStream = new ObjectOutputStream(stream);
        objStream.writeObject(new Integer(1));
        objStream.close();
        assertTrue(TargetPanelHelper.isIncompatibleInstallation(dir.getPath(), true));
    }
}

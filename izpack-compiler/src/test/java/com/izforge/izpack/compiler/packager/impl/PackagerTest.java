/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.compiler.packager.impl;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.izforge.izpack.api.data.GUIPrefs;
import com.izforge.izpack.merge.MergeManager;

public class PackagerTest
{
    /**
     * The merge manager.
     */
    private MergeManager mergeManager;

    @Before
    public void setUp()
    {
        mergeManager = mock(MergeManager.class);
    }

    @Test
    public void noSplash() throws IOException
    {
        final Packager packager = new Packager(null, null, null, null, null, mergeManager, null, null, null);
        packager.setSplashScreenImage(null);
        packager.writeManifest();

        verify(mergeManager).addResourceToMerge(anyString(), eq("META-INF/MANIFEST.MF"));
    }

    @Test
    public void guiPrefsWithSplash() throws IOException
    {
        final File splashImage = new File("image.png");
        final Packager packager = new Packager(null, null, null, null, null, mergeManager, null, null, null);
        packager.setGUIPrefs(new GUIPrefs());
        packager.setSplashScreenImage(splashImage);
        packager.writeManifest();

        verify(mergeManager, times(1)).addResourceToMerge(anyString(), eq("META-INF/image.png"));
        verify(mergeManager, times(1)).addResourceToMerge(anyString(), eq("META-INF/MANIFEST.MF"));
    }

    @Test
    public void noGuiPrefs() throws IOException
    {
        final Packager packager = new Packager(null, null, null, null, null, mergeManager, null, null, null);

        packager.writeManifest();

        verify(mergeManager).addResourceToMerge(anyString(), anyString());

    }
}

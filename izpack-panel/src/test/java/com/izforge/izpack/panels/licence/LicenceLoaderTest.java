/*
 * IzPack - Copyright 2001-2017 The IzPack project team.
 * All Rights Reserved.
 *
 * http://izpack.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.panels.licence;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.ResourceException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.resource.ResourceManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.net.URL;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Michael Aichler
 */
public class LicenceLoaderTest {

    @Rule
    public final ExpectedException thrownException = ExpectedException.none();

    private URL defaultUrl;
    private URL specificUrl;
    private Resources resources;

    @Before
    public void setUp() throws Exception
    {
        defaultUrl = new URL("file://default");
        specificUrl = new URL("file://specific");
        resources = Mockito.mock(Resources.class);
    }

    @Test
    public void asUrlShouldThrowExceptionIfResourcesNotFound()
    {
        when(resources.getString("LicencePanel.somePanelId", null)).thenReturn(null);
        when(resources.getURL("LicencePanel.licence")).thenThrow(new ResourceNotFoundException(""));

        Panel panel = createPanel("somePanelId");
        LicenceLoader loader = createFor(panel);

        thrownException.expect(ResourceException.class);
        thrownException.expectMessage(startsWith("Could not open license document for the resource id"));

        loader.asURL();
    }

    @Test
    public void asUrlShouldUseIzPanelClassAsPrefix()
    {
        when(resources.getURL("LicencePanel.licence")).thenReturn(defaultUrl);

        Panel panel = createPanel(null);
        URL url = createFor(panel).asURL();

        assertThat(url, equalTo(defaultUrl));
    }

    @Test
    public void asUrlShouldPreferSpecificResource()
    {
        when(resources.getString("LicencePanel.somePanelId", null)).thenReturn("document content");
        when(resources.getURL("LicencePanel.somePanelId")).thenReturn(specificUrl);

        Panel panel = createPanel("somePanelId");
        URL result = createFor(panel).asURL();

        assertThat(result, equalTo(specificUrl));
    }

    @Test
    public void asUrlShouldFallbackToDefaultResource()
    {
        when(resources.getString("LicencePanel.somePanelId", null)).thenReturn(null);
        when(resources.getURL("LicencePanel.licence")).thenReturn(defaultUrl);

        Panel panel = createPanel("somePanelId");
        URL result = createFor(panel).asURL();

        assertThat(result, equalTo(defaultUrl));
    }

    @Test
    public void asStringShouldLoadResource()
    {
        ResourceManager rm = new ResourceManager();
        rm.setResourceBasePath("/com/izforge/izpack/panels/licence/");

        Panel panel = createPanel(null);
        LicenceLoader loader = new LicenceLoader(panel, rm);

        String result = loader.asString();
        assertThat(result, equalTo("This is a licence panel"));
    }

    /**
     * Helper which creates a licence loader for the given {@code clazz} and a
     * mocked instance of {@link Resources}.
     *
     * @return A newly created licence loader.
     */
    private LicenceLoader createFor(Panel panel)
    {
        return new LicenceLoader(panel, resources);
    }

    private Panel createPanel(String id)
    {
        Panel panel = new Panel();
        panel.setPanelId(id);
        panel.setClassName("LicencePanel");
        return panel;
    }
}
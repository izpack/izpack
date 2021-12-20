/*
 * IzPack - Copyright 2021, Hitesh A. Bosamiya, All Rights Reserved.
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

package com.izforge.izpack.installer.util;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.substitutor.SubstitutionType;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.substitutor.VariableSubstitutorInputStream;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

/**
 * Unit tests of PanelHelper
 *
 * @author Hitesh A. Bosamiya
 */
public class PanelHelperTest
{
    private Resources resources;
    private Panel panel;
    private InstallData installData;
    private Map<String, String> map;

    @Before
    public void setUp() throws Exception
    {
        resources = Mockito.mock(Resources.class);
        panel = Mockito.mock(Panel.class);
        installData = Mockito.mock(InstallData.class);
        Messages messages = Mockito.mock(Messages.class);
        map = Mockito.mock(Map.class);
        when(installData.getMessages()).thenReturn(messages);
        when(messages.getMessages()).thenReturn(map);
    }

    @Test
    public void resourceNameShouldBeWithDefaultSuffix()
    {
        when(resources.getString("HTMLInfoPanel.somePanelId", null)).thenReturn(null);
        when(panel.getPanelId()).thenReturn("somePanelId");
        when(panel.getClassName()).thenReturn("com.izforge.izpack.panels.htmlinfo.HTMLInfoPanel");

        String result = PanelHelper.getPanelResourceName(panel, "info", resources);

        Assert.assertThat(result, equalTo("HTMLInfoPanel.info"));
    }

    @Test
    public void resourceNameShouldBeWithDefaultSuffixForNullPanelId()
    {
        when(resources.getString("HTMLInfoPanel.somePanelId", null)).thenReturn(null);
        when(panel.getPanelId()).thenReturn(null);
        when(panel.getClassName()).thenReturn("com.izforge.izpack.panels.htmlinfo.HTMLInfoPanel");

        String result = PanelHelper.getPanelResourceName(panel, "info", resources);

        Assert.assertThat(result, equalTo("HTMLInfoPanel.info"));
    }

    @Test
    public void resourceNameShouldBeWithDefaultSuffixForConsolePanel()
    {
        when(resources.getString("HTMLInfoPanel.somePanelId", null)).thenReturn(null);
        when(panel.getPanelId()).thenReturn("somePanelId");
        when(panel.getClassName()).thenReturn("com.izforge.izpack.panels.htmlinfo.HTMLInfoConsolePanel");

        String result = PanelHelper.getPanelResourceName(panel, "info", resources);

        Assert.assertThat(result, equalTo("HTMLInfoPanel.info"));
    }

    @Test
    public void resourceNameShouldBeWithDefaultSuffixForConsolePanelForNullPanelId()
    {
        when(resources.getString("HTMLInfoPanel.somePanelId", null)).thenReturn(null);
        when(panel.getPanelId()).thenReturn(null);
        when(panel.getClassName()).thenReturn("com.izforge.izpack.panels.htmlinfo.HTMLInfoConsolePanel");

        String result = PanelHelper.getPanelResourceName(panel, "info", resources);

        Assert.assertThat(result, equalTo("HTMLInfoPanel.info"));
    }

    @Test
    public void resourceNameShouldBeWithPanelIdSuffix()
    {
        when(resources.getString("HTMLInfoPanel.somePanelId", null)).thenReturn("some content");
        when(panel.getPanelId()).thenReturn("somePanelId");
        when(panel.getClassName()).thenReturn("com.izforge.izpack.panels.htmlinfo.HTMLInfoPanel");

        String result = PanelHelper.getPanelResourceName(panel, "info", resources);

        Assert.assertThat(result, equalTo("HTMLInfoPanel.somePanelId"));
    }

    @Test
    public void resourceNameShouldBeWithPanelIdSuffixForConsolePanel()
    {
        when(resources.getString("HTMLInfoPanel.somePanelId", null)).thenReturn("some content");
        when(panel.getPanelId()).thenReturn("somePanelId");
        when(panel.getClassName()).thenReturn("com.izforge.izpack.panels.htmlinfo.HTMLInfoConsolePanel");

        String result = PanelHelper.getPanelResourceName(panel, "info", resources);

        Assert.assertThat(result, equalTo("HTMLInfoPanel.somePanelId"));
    }

    @Test
    public void titleMessageKeyShouldBeWithDefaultSuffix()
    {
        when(map.containsKey("HTMLInfoPanel.somePanelId")).thenReturn(false);
        when(panel.getPanelId()).thenReturn("somePanelId");
        when(panel.getClassName()).thenReturn("com.izforge.izpack.panels.htmlinfo.HTMLInfoPanel");

        String result = PanelHelper.getPanelTitleMessageKey(panel, "info", installData);

        Assert.assertThat(result, equalTo("HTMLInfoPanel.info"));
    }

    @Test
    public void titleMessageKeyShouldBeWithDefaultSuffixForNullPanelId()
    {
        when(map.containsKey("HTMLInfoPanel.somePanelId")).thenReturn(false);
        when(panel.getPanelId()).thenReturn(null);
        when(panel.getClassName()).thenReturn("com.izforge.izpack.panels.htmlinfo.HTMLInfoPanel");

        String result = PanelHelper.getPanelTitleMessageKey(panel, "info", installData);

        Assert.assertThat(result, equalTo("HTMLInfoPanel.info"));
    }

    @Test
    public void titleMessageKeyShouldBeWithDefaultSuffixForConsolePanel()
    {
        when(map.containsKey("HTMLInfoPanel.somePanelId")).thenReturn(false);
        when(panel.getPanelId()).thenReturn("somePanelId");
        when(panel.getClassName()).thenReturn("com.izforge.izpack.panels.htmlinfo.HTMLInfoConsolePanel");

        String result = PanelHelper.getPanelTitleMessageKey(panel, "info", installData);

        Assert.assertThat(result, equalTo("HTMLInfoPanel.info"));
    }

    @Test
    public void titleMessageKeyShouldBeWithDefaultSuffixForConsolePanelForNullPanelId()
    {
        when(map.containsKey("HTMLInfoPanel.somePanelId")).thenReturn(false);
        when(panel.getPanelId()).thenReturn(null);
        when(panel.getClassName()).thenReturn("com.izforge.izpack.panels.htmlinfo.HTMLInfoConsolePanel");

        String result = PanelHelper.getPanelTitleMessageKey(panel, "info", installData);

        Assert.assertThat(result, equalTo("HTMLInfoPanel.info"));
    }

    @Test
    public void titleMessageKeyShouldBeWithPanelIdSuffix()
    {
        when(map.containsKey("HTMLInfoPanel.somePanelId")).thenReturn(true);
        when(panel.getPanelId()).thenReturn("somePanelId");
        when(panel.getClassName()).thenReturn("com.izforge.izpack.panels.htmlinfo.HTMLInfoPanel");

        String result = PanelHelper.getPanelTitleMessageKey(panel, "info", installData);

        Assert.assertThat(result, equalTo("HTMLInfoPanel.somePanelId"));
    }

    @Test
    public void titleMessageKeyShouldBeWithPanelIdSuffixForConsolePanel()
    {
        when(map.containsKey("HTMLInfoPanel.somePanelId")).thenReturn(true);
        when(panel.getPanelId()).thenReturn("somePanelId");
        when(panel.getClassName()).thenReturn("com.izforge.izpack.panels.htmlinfo.HTMLInfoConsolePanel");

        String result = PanelHelper.getPanelTitleMessageKey(panel, "info", installData);

        Assert.assertThat(result, equalTo("HTMLInfoPanel.somePanelId"));
    }
}

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

package com.izforge.izpack.core.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.izforge.izpack.api.exception.ResourceNotFoundException;
import javax.swing.ImageIcon;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link ResourceManager}.
 *
 * @author Tim Anderson
 */
public class ResourceManagerTest
{
    /**
     * Verifies images can be retrieved for each of the supported countries and languages.
     */
    @Test
    public void testImages()
    {
        ResourceManager resources = new ResourceManager()
        {
            @Override
            public Object getObject(String name)
            {
                if (name.equals("langpacks.info"))
                {
                    return DefaultLocalesTest.ISO_CODES;

                }
                return super.getObject(name);
            }
        };
        resources.setResourceBasePath("/com/izforge/izpack/bin/langpacks/flags/");
        for (String code : DefaultLocalesTest.ISO_CODES)
        {
            assertThat(resources.getImageIcon(code + ".gif")).isNotNull();
        }
    }

    @Test
    public void testBmpImage()
    {
        ResourceManager resources = new ResourceManager();
        resources.setResourceBasePath("/com/izforge/izpack/core/resource/");

        ImageIcon icon = resources.getImageIcon("testbmp.bmp");
        assertThat(icon).isNotNull();

        assertThat(icon.getIconWidth()).isEqualTo(20);
        assertThat(icon.getIconHeight()).isEqualTo(20);
    }

    @Test
    public void testInvalidImageName()
    {
        assertThatThrownBy(() -> {
            ResourceManager resources = new ResourceManager();
            resources.setResourceBasePath("/com/izforge/izpack/core/resource/");

            // this resource does not exist
            resources.getImageIcon("testbmp.bmpx");
        }).isInstanceOf(ResourceNotFoundException.class);
    }
}

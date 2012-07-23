/*
 * IzPack - Copyright 2001-2011 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.izforge.izpack.util;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.binding.OsModel;

/**
 * Tests {@link OsConstraintHelper}.
 *
 * @author Tim Anderson
 */
public class OsConstraintHelperTest
{

    /**
     * Tests the {@link OsConstraintHelper#getOsList} method.
     */
    @Test
    public void testGetOsList()
    {
        // create a root element with 2 child <os/> elements
        XMLElementImpl root = new XMLElementImpl("root");
        XMLElementImpl os1 = new XMLElementImpl("os", root);
        os1.setAttribute("arch", "arch1");
        os1.setAttribute("family", "family1");
        os1.setAttribute("jre", "jre1");
        os1.setAttribute("name", "name1");
        os1.setAttribute("version", "version1");

        XMLElementImpl os2 = new XMLElementImpl("os", root);
        os2.setAttribute("arch", "arch2");
        os2.setAttribute("family", "family2");
        os2.setAttribute("jre", "jre2");
        os2.setAttribute("name", "name2");
        os2.setAttribute("version", "version2");

        root.addChild(os1);
        root.addChild(os2);

        // now set the "os" attribute on the root element, to check backward-compatible support
        // NOTE: probably wouldn't mix old and new config approaches in reality, but the code supports it...
        root.setAttribute("os", "unix");

        List<OsModel> models = OsConstraintHelper.getOsList(root);
        assertEquals(3, models.size());

        checkModel(models.get(0), "arch1", "family1", "jre1", "name1", "version1");
        checkModel(models.get(1), "arch2", "family2", "jre2", "name2", "version2");

        // check that the old-style "os" attribute gets populated to OsModel.getFamily().
        checkModel(models.get(2), null, "unix", null, null, null);
    }

    /**
     * Verifies a model matches that expected.
     *
     * @param model   the model to check
     * @param arch    the expected architecture
     * @param family  the expected family
     * @param jre     the expected JRE
     * @param name    the expected name
     * @param version the expected version
     */
    private void checkModel(OsModel model, String arch, String family, String jre, String name, String version)
    {
        assertEquals(arch, model.getArch());
        assertEquals(family, model.getFamily());
        assertEquals(jre, model.getJre());
        assertEquals(name, model.getName());
        assertEquals(version, model.getVersion());
    }

}

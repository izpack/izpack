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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.binding.OsModel;
import java.util.Arrays;

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
    
    /**
     * Tests the {@link OsConstraintHelper#getOsList} method.
     */
    @Test
    public void testCommonOsList()
    {
        OsModel x64 = new OsModel("x64", null, null, null, null);
        OsModel mac = new OsModel(null, "mac", null, null, null);
        OsModel unix = new OsModel(null, "unix", null, null, null);
        OsModel win = new OsModel(null, "windows", null, null, null);
        OsModel x64mac = new OsModel("x64", "mac", null, null, null);
        OsModel x64unix = new OsModel("x64", "unix", null, null, null);
        OsModel x64win = new OsModel("x64", "windows", null, null, null);
        OsModel x86unix = new OsModel("x86", "unix", null, null, null);
        OsModel x86win = new OsModel("x86", "windows", null, null, null);
        
        List<OsModel> anyList = Collections.emptyList();
        List<OsModel> x64List = Collections.singletonList(x64);
        List<OsModel> macList = Collections.singletonList(mac);
        List<OsModel> winList = Collections.singletonList(win);
        List<OsModel> unixX64winList = Arrays.asList(unix, x64win); // unix or (x64 and win)
        List<OsModel> macX86unixWinList = Arrays.asList(mac, x86unix, win); // mac or (x86 and unix) or win
        
        // [] and []
        checkCommonOsList(anyList, anyList, true, x86win, x64mac, x64unix);
        
        // [] and [win]
        checkCommonOsList(anyList, winList, true, x86win, x64win);
        checkCommonOsList(anyList, winList, false, x64mac, x64unix);
        
        // [win] and []
        checkCommonOsList(winList, anyList, true, x86win, x64win);
        checkCommonOsList(winList, anyList, false, x64mac, x64unix);
        
        // [win] and [win]
        checkCommonOsList(winList, winList, true, x86win, x64win);
        checkCommonOsList(winList, winList, false, x64mac, x64unix);
        
        // [win] and [x64]
        checkCommonOsList(winList, x64List, true, x64win);
        checkCommonOsList(winList, x64List, false, x86win, x64mac, x86unix, x64unix);
        
        // [unix or (x64 and win)] and [x64]
        checkCommonOsList(unixX64winList, x64List, true, x64win, x64unix);
        checkCommonOsList(unixX64winList, x64List, false, x86win, x86unix, x64mac);
        
        // [unix or (x64 and win)] and [mac] is unsatisfiable
        try
        {
            OsConstraintHelper.commonOsList(unixX64winList, macList);
            fail();
        }
        catch (OsConstraintHelper.UnsatisfiableOsConstraintsException ex) {
            // expected
        }
        
        // [unix or (x64 and win)] and [mac or (x86 and unix) or win]
        checkCommonOsList(unixX64winList, macX86unixWinList, true, x86unix, x64win);
        checkCommonOsList(unixX64winList, macX86unixWinList, false, x86win, x64unix, x64mac);
    }
    
    /**
     * Computes and verifies list of common OS constraints of lists
     * {@code osList} and {@code otherOsList}.
     * <p>
     * <b>Note:</b> It is expected that the combination of {@code osList} and
     * {@code otherOsList} is satisfiable.
     * 
     * @param osList list of OS constraints
     * @param otherOsList other list of OS constraints
     * @param result expected verification result
     * @param matches tested OS instances
     */
    private void checkCommonOsList(List<OsModel> osList, List<OsModel> otherOsList,
            boolean result, OsModel... matches)
    {
        List<OsModel> commonOsList;
        try
        {
            commonOsList = OsConstraintHelper.commonOsList(osList, otherOsList);
        }
        catch (OsConstraintHelper.UnsatisfiableOsConstraintsException ex)
        {
            fail(ex.getMessage());
            return;
        }
        
        if (commonOsList.isEmpty())
        {
            // all matches match []
            assertTrue(result);
            return;
        }
        
        for (OsModel match : matches)
        {
            assertTrue(match(commonOsList, match) == result);
        }
    }
    
    private boolean match(List<OsModel> osList, OsModel os)
    {
        for (OsModel constraints : osList)
        {
            boolean match = true;
            
            if (constraints.getArch() != null && os.getArch() != null)
            {
                match = constraints.getArch().equals(os.getArch());
            }
            if (match && constraints.getFamily() != null && os.getFamily() != null)
            {
                match = constraints.getFamily().equals(os.getFamily());
            }
            if (match && constraints.getJre() != null && os.getJre() != null)
            {
                match = constraints.getJre().equals(os.getJre());
            }
            if (match && constraints.getName() != null && os.getName() != null)
            {
                match = constraints.getName().equals(os.getName());
            }
            if (match && constraints.getVersion() != null && os.getVersion() != null)
            {
                match = constraints.getVersion().equals(os.getVersion());
            }
            
            if (match)
            {
                return true;
            }
        }
        
        return false;
    }
    
}

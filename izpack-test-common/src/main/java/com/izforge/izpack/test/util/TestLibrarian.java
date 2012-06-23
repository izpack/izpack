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

package com.izforge.izpack.test.util;

import java.net.URL;

import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.TargetFactory;


/**
 * Test implementation of {@link Librarian} that can find COIOSHelper.dll.
 *
 * @author Tim Anderson
 */
public class TestLibrarian extends Librarian
{

    /**
     * Constructs a <tt>TestLibrarian</tt>.
     *
     * @param factory     the factory
     * @param housekeeper the house keeper
     */
    public TestLibrarian(TargetFactory factory, Housekeeper housekeeper)
    {
        super(factory, housekeeper);
    }

    /**
     * Returns the resource URL for the named library.
     *
     * @param name the library name
     * @return the library's resource URL, or <tt>null</tt> if it is not found
     */
    @Override
    protected URL getResourcePath(String name)
    {
        if (name.startsWith("COIOSHelper"))
        {
            String resource = "/com/izforge/izpack/bin/native/3rdparty/" + name + ".dll";
            return getClass().getResource(resource);
        }
        return super.getResourcePath(name);
    }
}

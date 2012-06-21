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

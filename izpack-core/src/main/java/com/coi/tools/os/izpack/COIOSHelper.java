/*
 * $Id$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005 Klaus Bartz
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
package com.coi.tools.os.izpack;

import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.NativeLibraryClient;

/**
 * Base class to handle multiple native methods of multiple classes in one
 * shared library. This is a singelton class.
 *
 * @author Klaus Bartz
 */
public class COIOSHelper
{

    private int used = 0;

    private boolean destroyed = false;

    private final Librarian librarian;

    /**
     * This method is used to free the library at the end of progam execution.
     * After this call, any instance of this class will not be usable any more!
     *
     * @param name the name of the library to free. Use only the name and
     * extension but not the path.
     */
    private native void FreeLibrary(String name);

    /**
     * Constructs a <tt>COIOSHelper</tt>.
     *
     * @param librarian the librarian
     */
    public COIOSHelper(Librarian librarian)
    {
        this.librarian = librarian;
    }

    /**
     * This method is used to free the library at the end of progam execution.
     * This is the method of the helper class which will be called from other
     * objects. After this call, any instance of this class will not be usable
     * any more! <b><i><u>Note that this method does NOT return </u>
     * at the first call, but at any other </i> </b> <br>
     * <br>
     * <b>DO NOT CALL THIS METHOD DIRECTLY! </b> <br>
     * It is used by the librarian to free the native library before physically
     * deleting it from its temporary loaction. A call to this method will
     * freeze the application irrecoverably!
     *
     * @param name the name of the library to free. Use only the name and
     * extension but not the path.
     * @param name
     * @see com.izforge.izpack.util.NativeLibraryClient#freeLibrary
     */
    /*--------------------------------------------------------------------------*/
    /**
     * @param name
     */
    public synchronized void freeLibrary(String name)
    {
        used--;
        if (!destroyed)
        {
            FreeLibrary(name);
            destroyed = true;
        }
    }

    /**
     * Add a NativeLibraryClient as dependant to this object. The method tries
     * to load the shared library COIOSHelper which should contain native
     * methods for the dependant.
     *
     * @param dependant to be added
     * @throws UnsatisfiedLinkError if the library cannot be loaded
     */
    public synchronized void addDependant(NativeLibraryClient dependant)
    {
        used++;
        librarian.loadLibrary("COIOSHelper", dependant);
    }

}

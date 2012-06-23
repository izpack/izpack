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

package com.izforge.izpack.installer.unpacker;


import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.os.FileQueue;


/**
 * Factory for {@link FileQueue} instances, if queuing is supported by the current platform.
 *
 * @author Tim Anderson
 */
public class FileQueueFactory
{

    /**
     * Determines if the current platform supports queuing.
     */
    private final boolean supportsQueue;

    /**
     * The librarian.
     */
    private final Librarian librarian;


    /**
     * Constructs a {@code FileQueueFactory}.
     *
     * @param platform  the current platform
     * @param librarian the librarian
     */
    public FileQueueFactory(Platform platform, Librarian librarian)
    {
        supportsQueue = platform.isA(Platform.Name.WINDOWS);
        this.librarian = librarian;
    }

    /**
     * Determines if queuing is supported.
     *
     * @return {@code true} if queuing is supported
     */
    public boolean isSupported()
    {
        return supportsQueue;
    }

    /**
     * Creates a new {@link FileQueue}.
     *
     * @return a new queue or {@code null} if queuing is not supported
     */
    public FileQueue create()
    {
        return supportsQueue ? new FileQueue(librarian) : null;
    }

}

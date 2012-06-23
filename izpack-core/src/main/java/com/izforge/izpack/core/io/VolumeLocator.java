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

package com.izforge.izpack.core.io;


import java.io.File;
import java.io.IOException;

/**
 * Locates the next volume for an {@link FileSpanningInputStream}.
 *
 * @author Tim Anderson
 */
public interface VolumeLocator
{

    /**
     * Returns the next volume.
     *
     * @param path    the expected volume path
     * @param corrupt if <tt>true</tt> the previous attempt detected a corrupt or invalid volume
     * @return the next volume
     * @throws IOException if the volume cannot be found
     */
    File getVolume(String path, boolean corrupt) throws IOException;
}
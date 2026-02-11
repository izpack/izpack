/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.api.merge;

import java.io.IOException;
import java.io.OutputStream;

@FunctionalInterface
public interface MergeTarget
{
    /**
     * Offers the content represented by the given name and file time and input stream.
     * If the offered entry name is being accepted, the implementation is using the provided input stream to get
     * the input stream for reading the data.
     *
     * @param name         the target entry name
     * @param timestamp    the file time stamp
     * @param wrtiteActipm the write action called for the data
     * @return {@code true} if the entry is used, {@code false} if the enty has been added previously
     * @throws IOException if the write operation fails
     */
    boolean offer(String name, long timestamp, WriteAction wrtiteActipm) throws IOException;

    @FunctionalInterface
    interface WriteAction {
        void accept(OutputStream outputStream) throws IOException;
    }
}

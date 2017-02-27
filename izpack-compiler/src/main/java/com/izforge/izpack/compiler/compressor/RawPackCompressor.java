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
package com.izforge.izpack.compiler.compressor;

import java.io.BufferedOutputStream;
import java.io.OutputStream;

/**
 * IzPack will be able to support different compression methods for the packs
 * included in the installation jar file. This class implements the
 * PackCompressor for the compression format "raw".
 *
 * @author Klaus Bartz
 */
public class RawPackCompressor extends PackCompressorBase
{

    private static final String[] THIS_FORMAT_NAMES =
    {
        "raw", "uncompressed"
    };

    /**
     *
     */
    public RawPackCompressor()
    {
        formatNames = THIS_FORMAT_NAMES;
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#getOutputStream(java.io.OutputStream)
     */
    public OutputStream getOutputStream(OutputStream os)
    {
        // In this pack compressor we must not use reflection because
        // the neede class will be always present (a base class of the VM).
        return (new BufferedOutputStream(os));
    }
}

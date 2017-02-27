/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://www.izforge.com/izpack/ http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil
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
package com.izforge.izpack.core.io;

import java.io.IOException;

/**
 * Exception indicating a corrupt volume.
 *
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class CorruptVolumeException extends IOException
{

    private static final long serialVersionUID = -1659572038393604549L;
    //  name of the corrupt volume
    private String volumename;

    /**
     *
     */
    public CorruptVolumeException()
    {
    }

    /**
     * @param msg
     */
    public CorruptVolumeException(String msg, String volumename)
    {
        super(msg);
        this.volumename = volumename;
    }

    public String getVolumename()
    {
        return volumename;
    }

    public void setVolumename(String volumename)
    {
        this.volumename = volumename;
    }
}

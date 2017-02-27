/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright (c) 2008, 2009 Anthonin Bonnefoy
 * Copyright (c) 2008, 2009 David Duponchel
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
package com.izforge.izpack.api.adaptator;

import com.izforge.izpack.api.exception.IzPackException;

/**
 * @author David Duponchel
 */
public class XMLException extends IzPackException
{

    private static final long serialVersionUID = 5028962140189818891L;

    public XMLException(String message)
    {
        super(message);
    }

    public XMLException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public XMLException(Throwable cause)
    {
        super(cause);
    }
}

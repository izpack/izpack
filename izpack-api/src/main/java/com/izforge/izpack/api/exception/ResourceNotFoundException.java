/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Marcus Stursberg
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
package com.izforge.izpack.api.exception;

/**
 * Describes that a resource could not be found.
 *
 * @author Marcus Stursberg
 */
public class ResourceNotFoundException extends ResourceException
{

    private static final long serialVersionUID = 3258688827575906353L;

    /**
     * Constructs a <tt>ResourceNotFoundException</tt>.
     *
     * @param message the error message
     */
    public ResourceNotFoundException(String message)
    {
        super(message);
    }

    /**
     * Constructs a <tt>ResourceNotFoundException</tt>.
     *
     * @param message the error message
     * @param cause the cause
     */
    public ResourceNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }
}

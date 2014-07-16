/*
 * IzPack - Copyright 2001-2014 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2014 René Krell
 * Copyright 2009-2011 FuseSource Corp.
 * Copyright 2004, 2008 IBM Corporation and others.
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

package org.fusesource.hawtjni.runtime;

/**
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public enum ArgFlag {

    /**
     * Indicate that a native method parameter is an out only variable.
     * This only makes sense if the parameter is a structure or an array
     * of primitives. It is an optimization to avoid copying the java
     * memory to C memory on the way in.
     */
    NO_IN,

    /**
     * Indicate that a native method parameter is an in only variable.
     * This only makes sense if the parameter is a structure or an array
     * of primitives. It is an optimization to avoid copying the C memory
     * from java memory on the way out.
     */
    NO_OUT,

    /**
     * Indicate that GetPrimitiveArrayCritical() should be used instead
     * of Get<PrimitiveType>ArrayElements() when transferring array of
     * primitives from/to C. This is an optimization to avoid copying
     * memory and must be used carefully. It is ok to be used in
     * MoveMemory() and memmove() natives.
     */
    CRITICAL,

    /**
     * Indicate that the associated C local variable for a native method
     * parameter should be initialized with zeros.
     */
    INIT,

    /**
     * Indicate that the parameter is a pointer.
     */
    POINTER_ARG,

    /**
     * Indicate that a structure parameter should be passed by value
     * instead of by reference. This dereferences the parameter by
     * prepending *. The parameter must not be NULL.
     */
    BY_VALUE,

    /**
     * Indicate that GetStringChars()should be used instead of
     * GetStringUTFChars() to get the characters of a java.lang.String
     * passed as a parameter to native methods.
     */
    UNICODE,

    /**
     * Indicate that the parameter of a native method is the sentinel
     * (last parameter of a variable argument C function). The generated
     * code is always the literal NULL. Some compilers expect the sentinel
     * to be the literal NULL and output a warning if otherwise.
     */
    SENTINEL,

    /**
     * Indicate that the native parameter is a C# managed object.
     */
    CS_OBJECT,

}
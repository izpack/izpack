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
public enum ClassFlag {
    /**
     * Indicate that the item should not be generated. For example,
     * custom natives are coded by hand.
     */
    CLASS_SKIP,

    /**
     * Indicate that the platform source is in C++
     */
    CPP,

    /**
     * Indicate that this class will define a structure
     */
    STRUCT,

    /**
     * Indicate that structure name is a typedef (It should
     * not be prefixed with 'struct' to reference it.)
     */
    TYPEDEF,

    /**
     * Indicate that the struct should get zeroed out before
     * setting any of it's fields.  Comes in handy when
     * you don't map all the struct fields to java fields but
     * still want the fields that are not mapped initialized.
     */
    ZERO_OUT,
}
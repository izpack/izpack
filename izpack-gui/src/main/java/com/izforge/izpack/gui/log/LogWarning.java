/*
 * $Id:$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://www.izforge.com/izpack/
 * http://izpack.codehaus.org/
 *
 * Copyright 2006 Elmar Grom
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
package com.izforge.izpack.gui.log;

/**
 * Interface for handle warnings logging with IzPack Log class.
 *
 * @author Elmar Grom
 */
public interface LogWarning
{

    /**
     * First index for warning messages
     */
    static final int WARNING_BASE = 1000;

    /**
     * The highest legal error message number is less than this value.
     */
    static final int MAX_WARNING = WARNING_BASE;
}

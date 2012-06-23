/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2003 Tino Schwarze
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

package com.izforge.izpack.api.handler;

import com.izforge.izpack.api.event.ProgressListener;

/**
 * This interface is used by functions which need to notify the user of some progress.
 * <p/>
 * For example, the installation progress and compilation progress are communicated to the user
 * using this interface. The interface supports a two-stage progress indication: The whole action is
 * divided into steps (for example, packs when installing) and sub-steps (for example, files of a
 * pack).
 *
 * @deprecated use {@link ProgressListener}. This interface will be removed in IzPack 6.0
 */
@Deprecated
public interface AbstractUIProgressHandler extends AbstractUIHandler, ProgressListener
{

}

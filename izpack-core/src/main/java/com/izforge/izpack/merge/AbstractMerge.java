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

package com.izforge.izpack.merge;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.izforge.izpack.api.merge.Mergeable;

/**
 * Abstract classes for all mergeable element.
 * Contains helper methods to managed the mergeContent map.
 *
 * @author Anthonin Bonnefoy
 */
public abstract class AbstractMerge implements Mergeable
{
    protected Map<OutputStream, List<String>> mergeContent;

    protected List<String> getMergeList(OutputStream outputStream)
    {
        if (!mergeContent.containsKey(outputStream))
        {
            mergeContent.put(outputStream, new ArrayList<String>());
        }
        return mergeContent.get(outputStream);
    }
}

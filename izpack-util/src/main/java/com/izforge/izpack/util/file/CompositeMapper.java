/*
 * Copyright 2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.izforge.izpack.util.file;

import java.util.Arrays;
import java.util.HashSet;

/**
 * A <CODE>ContainerMapper</CODE> that unites the results of its constituent
 * <CODE>FileNameMapper</CODE>s into a single set of result filenames.
 */
public class CompositeMapper extends ContainerMapper
{

    public String[] mapFileName(String sourceFileName)
    {
        HashSet<String> results = new HashSet<String>();

        for (FileNameMapper mapper : getMappers())
        {
            if (mapper != null)
            {
                String[] mapped = mapper.mapFileName(sourceFileName);
                if (mapped != null)
                {
                    results.addAll(Arrays.asList(mapped));
                }
            }
        }

        return (results.size() == 0) ? null : results
                .toArray(new String[results.size()]);
    }

}

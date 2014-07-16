/*
 * IzPack - Copyright 2001-2014 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright (c) 2002-2012, the original authors of the JLine project
 * Copyright 2014 René Krell
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

package jline.console;

/**
 * This exception is thrown by {@link ConsoleReader#readLine} when
 * user interrupt handling is enabled and the user types the
 * interrupt character (ctrl-C). The partially entered line is
 * available via the {@link #getPartialLine()} method.
 */
public class UserInterruptException
    extends RuntimeException
{
    /**
     * 
     */
    private static final long serialVersionUID = -7461311188692852849L;
    private final String partialLine;

    public UserInterruptException(String partialLine)
    {
        this.partialLine = partialLine;
    }

    /**
     * @return the partially entered line when ctrl-C was pressed
     */
    public String getPartialLine()
    {
        return partialLine;
    }
}

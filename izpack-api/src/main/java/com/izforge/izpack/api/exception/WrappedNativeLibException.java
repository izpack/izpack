/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2005 Klaus Bartz
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

import com.izforge.izpack.api.resource.Messages;

/**
 * This class allows it to define error messages for <code>NativeLibException</code> s in the
 * IzPack locale files.
 *
 * @author Klaus Bartz
 */
public class WrappedNativeLibException extends IzPackException
{

    /**
     * The messages.
     */
    private final Messages messages;

    /**
     * Constructs a {@code WrappedNativeLibException}.
     *
     * @param cause    the cause of the exceptions
     * @param messages messages to localise the exception
     */
    public WrappedNativeLibException(Throwable cause, Messages messages)
    {
        super(cause);
        this.messages = messages;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Throwable#getMessage()
     */
    public String getMessage()
    {
        if (getCause() instanceof NativeLibException)
        {
            StringBuilder result = new StringBuilder();
            NativeLibException nle = (NativeLibException) getCause();
            if (nle.getLibMessage() != null)
            {
                String val = messages.get("NativeLibException." + nle.getLibMessage());
                result.append(val);
            }
            else if (nle.getLibErr() != 0)
            {
                String val = messages.get("NativeLibException.libErrNumber." + nle.getLibErr());
                result.append(val);
            }
            if (nle.getOsErr() != 0)
            {
                String val = messages.get("NativeLibException.libInternal.OsErrNumPraefix") + nle.getOsErr();
                if (result.length() != 0)
                {
                    result.append("\n");
                }
                result.append(val);
            }
            if (nle.getOsMessage() != null)
            {
                String val = messages.get("NativeLibException.libInternal.OsErrStringPraefix") + nle.getOsMessage();
                if (result.length() != 0)
                {
                    result.append("\n");
                }
                result.append(val);
            }
            if (result.length() > 0)
            {
                return (nle.reviseMsgWithArgs(result.toString()));
            }
            else
            {
                return (nle.getMessage());
            }
        }
        else
        {
            return (super.getMessage());
        }
    }

}

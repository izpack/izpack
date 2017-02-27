/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2013 Tim Anderson
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

import com.izforge.izpack.api.exception.IzPackException;

/**
 * Abstract implementation of {@link Prompt}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPrompt implements Prompt
{

    @Override
    public void message(Throwable throwable)
    {
        if (throwable instanceof IzPackException)
        {
            IzPackException ize = (IzPackException) throwable;
            switch (ize.getPromptType())
            {
                case ERROR:
                    error(throwable);
                    break;

                case WARNING:
                    warn(throwable);
                    break;

                default:
                    message(Type.INFORMATION, throwable.getMessage());
                    break;
            }
        }
        else
        {
            error(throwable);
        }
    }

    @Override
    public void message(Type type, String message)
    {
        message(type, null, message);
    }

    @Override
    public void message(Type type, String title, String message)
    {
        message(type, title, message, null);
    }

    @Override
    public void warn(Throwable throwable)
    {
        String message = getThrowableMessage(throwable);
        warn((message != null ? message : "An error occured"));
    }

    @Override
    public void warn(String message)
    {
        warn(null, message);
    }

    @Override
    public void warn(String title, String message)
    {
        message(Type.WARNING, title, message, null);
    }

    @Override
    public void error(Throwable throwable)
    {
        String message = getThrowableMessage(throwable);
        error(null, (message != null ? message : "An error occured"), throwable);
    }

    @Override
    public void error(String message)
    {
        error(null, message);
    }

    @Override
    public void error(String message, Throwable throwable)
    {
        error(null, message, throwable);
    }

    @Override
    public void error(String title, String message)
    {
        message(Type.ERROR, title, message, null);
    }

    @Override
    public void error(String title, String message, Throwable throwable)
    {
        message(Type.ERROR, title, message, throwable);
    }

    /**
     * Displays a confirmation message.
     *
     * @param type the type of the message
     * @param message the message
     * @param options the options which may be selected
     * @return the selected option
     */
    @Override
    public Option confirm(Type type, String message, Options options)
    {
        return confirm(type, message, options, null);
    }

    /**
     * Displays a confirmation message.
     *
     * @param type the type of the message
     * @param message the message
     * @param options the options which may be selected
     * @param defaultOption the default option to select. May be {@code null}
     * @return the selected option
     */
    @Override
    public Option confirm(Type type, String message, Options options, Option defaultOption)
    {
        return confirm(type, null, message, options, defaultOption);
    }

    /**
     * Displays a confirmation message.
     *
     * @param type the type of the message
     * @param title the message title. May be {@code null}
     * @param message the message
     * @param options the options which may be selected
     * @return the selected option
     */
    @Override
    public Option confirm(Type type, String title, String message, Options options)
    {
        return confirm(type, title, message, options, null);
    }

    /**
     * Returns the dialog title based on the type of the message.
     *
     * @param type the message type
     * @return the title
     */
    protected String getTitle(Type type)
    {
        String result;
        switch (type)
        {
            case INFORMATION:
                result = "Info";
                break;
            case QUESTION:
                result = "Question";
                break;
            case WARNING:
                result = "Warning";
                break;
            default:
                result = "Error";
        }
        return result;
    }

    /**
     * Try to extract the uppermost message from a Throwable's stacktrace.
     *
     * @param throwable the Throwable to examine including its causes
     * @return the message
     */
    public static String getThrowableMessage(Throwable throwable)
    {
        String message = null;

        while (throwable != null)
        {
            message = throwable.getMessage();
            throwable = throwable.getCause();
        }
        return message;
    }

    protected static String getDetails(Throwable throwable)
    {
        StringBuffer b = new StringBuffer();
        int lengthOfLastTrace = 1; // initial value
        // Start with the specified throwable and loop through the chain of
        // causality for the throwable.
        while (throwable != null)
        {
            // Output Exception name and message, and begin a list
            b.append(throwable.getClass().getName() + ": " + throwable.getMessage());
            // Get the stack trace and output each frame.
            // Be careful not to repeat stack frames that were already reported
            // for the exception that this one caused.
            StackTraceElement[] stack = throwable.getStackTrace();
            for (int i = stack.length - lengthOfLastTrace; i >= 0; i--)
            {
                b.append("- in " + stack[i].getClassName() + "." + stack[i].getMethodName()
                        + "() at " + stack[i].getFileName() + ":"
                        + stack[i].getLineNumber() + "\n");
            }
            // See if there is a cause for this exception
            throwable = throwable.getCause();
            if (throwable != null)
            {
                // If so, output a header
                b.append("Caused by: ");
                // And remember how many frames to skip in the stack trace
                // of the cause exception
                lengthOfLastTrace = stack.length;
            }
        }
        return b.toString();
    }

}

/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
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
package com.izforge.izpack.panels.compile;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * This class describes the result of the compilation.
 * <p/>
 * This class is here because error handling is not straight-forward with regard
 * to compilation.
 * <p/>
 * The error condition consists of an error message, the full command line which
 * failed to execute plus it's stdout and stderr. The reason for this class to
 * exist is that there are three possible reactions to the error (chosen by the
 * user).
 * <ol>
 * <li>abort</li>
 * <li>ignore (continue anyway)</li>
 * <li>reconfigure</li>
 * </ol>
 *
 * @author Tino Schwarze
 */
public class CompileResult
{

    // -------- public constants ---------------
    // arbitrary values
    public final static int SUCCESS = 42;

    public final static int FAILED = 23;

    public final static int ACTION_ABORT = 27;

    public final static int ACTION_CONTINUE = 39;

    public final static int ACTION_RECONFIGURE = 31;

    // -------- private variables ---------------
    // we're optimistic...
    private int status = SUCCESS;

    // here we're pessimistic
    private int action = ACTION_ABORT;

    /**
     * the error message
     */
    private String message = null;

    /**
     * the command line
     */
    private List<String> cmdline = null;

    /**
     * the stdout of the command
     */
    private String stdout = null;

    /**
     * the stderr of the command
     */
    private String stderr = null;

    /**
     * constructor, create a new successful result
     */
    public CompileResult()
    {
        this.status = SUCCESS;
        this.action = ACTION_CONTINUE;
    }

    /**
     * Creates a result because of exception.
     *
     * @param anException The exception.
     */
    public CompileResult(Exception anException)
    {
        StringWriter writer = new StringWriter();
        anException.printStackTrace(new PrintWriter(writer));
        this.message = writer.toString();
        this.action = ACTION_ABORT;
        this.status = FAILED;
    }

    /**
     * creates a new CompileResult with status FAILED
     *
     * @param message description of the exception
     * @param cmdline full command line of failed command
     * @param stdout standard output of failed command
     * @param stderr standard error of failed command
     */
    public CompileResult(String message, List<String> cmdline, String stdout, String stderr)
    {
        this.message = message;
        this.status = FAILED;
        this.cmdline = cmdline;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    public void setStatus(int status)
    {
        if ((status == SUCCESS) || (status == FAILED))
        {
            this.status = status;
        }
    }

    public int getStatus()
    {
        return this.status;
    }

    public void setAction(int action)
    {
        if ((action == ACTION_ABORT) || (action == ACTION_CONTINUE)
                || (action == ACTION_RECONFIGURE))
        {
            this.action = action;
        }

    }

    public int getAction()
    {
        return this.action;
    }

    /**
     * check for success (convenience function)
     */
    public boolean isSuccess()
    {
        return (this.status == SUCCESS);
    }

    /**
     * check whether to abort (convenience function)
     */
    public boolean isAbort()
    {
        return ((this.status == FAILED) && (this.action == ACTION_ABORT));
    }

    /**
     * check whether to continue (convenience function)
     *
     * @return true if status is SUCCESS or action is CONTINUE
     */
    public boolean isContinue()
    {
        return ((this.status == SUCCESS) || (this.action == ACTION_CONTINUE));
    }

    /**
     * check whether to reconfigure (convenience function)
     */
    public boolean isReconfigure()
    {
        return ((this.status == FAILED) && (this.action == ACTION_RECONFIGURE));
    }

    /**
     * return error message
     *
     * @return the error message describing the action that failed (might be
     * null)
     */
    public String getMessage()
    {
        return this.message;
    }

    /**
     * get command line of failed command as a string
     *
     * @return command line of failed command
     */
    public String getCmdline()
    {
        StringBuffer stringBuffer = new StringBuffer();
        for (String aCmdline : this.cmdline)
        {
            if (stringBuffer.length() > 0)
            {
                stringBuffer.append(' ');
            }
            stringBuffer.append(aCmdline);
        }
        return stringBuffer.toString();
    }

    public String getStdout()
    {
        return this.stdout;
    }

    public String getStderr()
    {
        return this.stderr;
    }

}

/*
 * Copyright  2002-2004 The Apache Software Foundation
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
package com.izforge.izpack.util.file.types.selectors;

import java.io.File;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.util.file.types.DataType;

/**
 * A convenience base class that you can subclass Selectors from. It provides
 * some helpful common behaviour. Note that there is no need for Selectors to
 * inherit from this class, it is only necessary that they implement
 * FileSelector.
 */
public abstract class BaseSelector extends DataType implements FileSelector
{

    private String errmsg = null;

    /**
     * Do nothing constructor.
     */
    public BaseSelector()
    {
    }

    /**
     * Allows all selectors to indicate a setup error. Note that only the first
     * error message is recorded.
     *
     * @param msg The error message any BuildException should throw.
     */
    public void setError(String msg)
    {
        if (errmsg == null)
        {
            errmsg = msg;
        }
    }

    /**
     * Returns any error messages that have been set.
     *
     * @return the error condition
     */
    public String getError()
    {
        return errmsg;
    }

    /**
     * <p>
     * Subclasses can override this method to provide checking of their state.
     * So long as they call validate() from isSelected(), this will be called
     * automatically (unless they override validate()).</p>
     * <p>
     * Implementations should check for incorrect settings and call setError()
     * as necessary.</p>
     *
     * @throws Exception
     */
    public void verifySettings() throws Exception
    {
    }

    /**
     * Subclasses can use this to throw the requisite exception in isSelected()
     * in the case of an error condition.
     */
    public void validate() throws Exception
    {
        if (getError() == null)
        {
            verifySettings();
        }
        if (getError() != null)
        {
            throw new Exception(errmsg);
        }
    }

    /**
     * Method that each selector will implement to create their selection
     * behaviour. If there is a problem with the setup of a selector, it can
     * throw a BuildException to indicate the problem.
     *
     * @param basedir A java.io.File object for the base directory
     * @param filename The name of the file to check
     * @param file A File object for this filename
     * @return whether the file should be selected or not
     * @throws Exception
     */
    public abstract boolean isSelected(InstallData idata,
            File basedir, String filename, File file) throws Exception;

}

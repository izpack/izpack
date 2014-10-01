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

import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.handler.AbstractUIHandler;

/**
 * Interface for monitoring compilation progress.
 * <p/>
 * This is used by <code>CompilePanel</code>, <code>CompileWorker</code> and
 * <code>CompilePanelAutomationHelper</code> to display the progress of the compilation.
 *
 * @author Tino Schwarze
 * @see ProgressListener
 */
public interface CompileHandler extends ProgressListener, AbstractUIHandler
{

    /**
     * An error was encountered.
     * <p/>
     * This method should notify the user of the error and request a choice whether to continue,
     * abort or reconfigure. It should alter the error accordingly.
     * <p/>
     * Although a CompileResult is passed in, the method is only called if something failed.
     *
     * @param error the error to handle
     */
    public void handleCompileError(CompileResult error);

}

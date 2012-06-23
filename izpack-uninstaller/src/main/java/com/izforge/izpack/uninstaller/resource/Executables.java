/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
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

package com.izforge.izpack.uninstaller.resource;

import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.handler.PromptUIHandler;
import com.izforge.izpack.data.ExecutableFile;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.OsConstraintHelper;


/**
 * The uninstaller {@link ExecutableFile}s.
 *
 * @author Tim Anderson
 */
public class Executables
{

    /**
     * The executables.
     */
    private final List<ExecutableFile> executables;

    /**
     * The prompt for reporting errors.
     */
    private final Prompt prompt;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(Executables.class.getName());


    /**
     * Constructs an <tt>Executables</tt>.
     *
     * @param resources used to locate the <em>executables</em> resource
     * @param prompt   the prompt for reporting errors
     * @throws IzPackException if the executables cannot be read
     */
    public Executables(Resources resources, Prompt prompt)
    {
        this.prompt = prompt;
        executables = read(resources);
    }

    /**
     * Runs the {@link ExecutableFile}s.
     * <p/>
     * TODO - should this update the uninstall progress?
     *
     * @return <tt>true</tt> if they were run successfully
     */
    public boolean run()
    {
        for (ExecutableFile file : executables)
        {
            if (file.executionStage == ExecutableFile.UNINSTALL
                    && OsConstraintHelper.oneMatchesCurrentSystem(file.osList))
            {
                if (!run(file))
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Runs an executable file.
     *
     * @param file the file to execute
     * @return <tt>true</tt> if it ran successfully, otherwise <tt>false</tt>
     */
    protected boolean run(ExecutableFile file)
    {
        FileExecutor executor = new FileExecutor(Arrays.asList(file));
        int status = executor.executeFiles(ExecutableFile.UNINSTALL, new PromptUIHandler(prompt));
        if (status != 0)
        {
            logger.severe("Executable=" + file.path + " exited with status=" + status);
            return false;
        }
        return true;
    }

    /**
     * Reads the executables.
     *
     * @return the executables
     * @throws IzPackException if the executables cannot be read
     */
    private List<ExecutableFile> read(Resources resources)
    {
        List<ExecutableFile> executables = new ArrayList<ExecutableFile>();
        try
        {
            ObjectInputStream in = new ObjectInputStream(resources.getInputStream("executables"));
            int count = in.readInt();
            for (int i = 0; i < count; i++)
            {
                ExecutableFile file = (ExecutableFile) in.readObject();
                executables.add(file);
            }
        }
        catch (Exception exception)
        {
            throw new IzPackException("Failed to read executable resources", exception);
        }
        return executables;
    }

}

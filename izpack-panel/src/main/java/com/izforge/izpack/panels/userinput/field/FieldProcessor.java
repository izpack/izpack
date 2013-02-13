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

package com.izforge.izpack.panels.userinput.field;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.panels.userinput.processor.Processor;
import com.izforge.izpack.panels.userinput.processorclient.ValuesProcessingClient;


/**
 * FieldProcessor is a wrapper around a {@link Processor}.
 *
 * @author Tim Anderson
 */
public class FieldProcessor
{

    /**
     * The configuration.
     */
    private final Config config;

    /**
     * The processor class name.
     */
    private final String className;

    /**
     * The cached processor instance.
     */
    private Processor processor;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(FieldProcessor.class.getName());


    /**
     * Constructs a {@code FieldProcessor}.
     *
     * @param processor the processor element
     * @param config    the configuration
     */
    public FieldProcessor(IXMLElement processor, Config config)
    {
        className = config.getAttribute(processor, "class");
        this.config = config;
    }

    /**
     * Processes a set of values.
     *
     * @param values the values to process
     * @return the result of the processing
     * @throws IzPackException if processing fails
     */
    public String process(String[] values)
    {
        String result;
        try
        {
            if (processor == null)
            {
                processor = config.getFactory().create(className, Processor.class);
            }
            result = processor.process(new ValuesProcessingClient(values));
        }
        catch (Throwable exception)
        {
            logger.log(Level.WARNING, "Processing using " + className + " failed: " + exception.getMessage(),
                       exception);
            if (exception instanceof IzPackException)
            {
                throw (IzPackException) exception;
            }
            throw new IzPackException("Processing using " + className + " failed: " + exception.getMessage(),
                                      exception);
        }
        return result;
    }

}

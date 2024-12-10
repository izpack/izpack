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
package com.izforge.izpack.compiler.util;

import java.net.URL;
import java.net.URLClassLoader;

import com.izforge.izpack.api.exception.IzPackClassNotFoundException;
import jakarta.inject.Inject;

/**
 * Class loader for the compiler.
 * <p/>
 * This enables:
 * <ul>
 * <li>jars to be dynamically added</li>
 * <li>unqualified class names to be mapped to their fully qualified names</li>
 * </ul>
 *
 * @author Tim Anderson
 */
public class CompilerClassLoader extends URLClassLoader
{

    /**
     * The class name mapper.
     */
    private final ClassNameMapper mapper;

    /**
     * Constructs a {@code CompilerClassLoader} using this class' class loader.
     *
     * @param mapper the class name mapper
     */
    @Inject
    public CompilerClassLoader(ClassNameMapper mapper)
    {
        this(CompilerClassLoader.class.getClassLoader(), mapper);
    }

    /**
     * Constructs a {@code CompilerClassLoader}.
     *
     * @param parent the parent class loader
     * @param mapper the class name mapper
     */
    public CompilerClassLoader(ClassLoader parent, ClassNameMapper mapper)
    {
        super(new URL[0], parent);
        this.mapper = mapper;
    }

    /**
     * Appends the specified URL to the list of URLs to search for classes and resources.
     *
     * @param url the URL to be added to the search path of URLs
     */
    @Override
    public void addURL(URL url)
    {
        super.addURL(url);
    }


    /**
     * Loads the class with the specified name, ensuring it is of the specified type.
     * <p/>
     * This uses the {@link ClassNameMapper} to try and map it to a known class.
     *
     * @param name the class name
     * @param type the expected type
     * @return the corresponding class
     * @throws ClassCastException           if the class isn't of the specified type
     * @throws IzPackClassNotFoundException if the class cannot be found
     */
    @SuppressWarnings("unchecked")
    public <T> Class<T> loadClass(String name, Class<T> type)
    {
        Class<T> result;
        try
        {
            Class<?> loaded = loadClass(name);
            if (!type.isAssignableFrom(loaded))
            {
                throw new ClassCastException("Class " + loaded.getName() + " is not a " + type.getName());
            }
            else
            {
                result = (Class<T>) loaded;
            }
        }
        catch (ClassNotFoundException exception)
        {
            throw new IzPackClassNotFoundException(name, exception);
        }
        return result;
    }


    /**
     * Finds the class with the specified name.
     * <p/>
     * This uses the {@link ClassNameMapper} to try and map it to a known class.
     *
     * @param name the name of the class
     * @return the corresponding {@code Class} object
     * @throws ClassNotFoundException if the class could not be found
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        Class<?> result;
        String mapping = mapper.map(name);
        if (mapping != null)
        {
            result = loadClass(mapping);
        }
        else
        {
            result = super.findClass(name);
        }
        return result;
    }

}

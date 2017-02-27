/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2004 Klaus Bartz
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
package com.izforge.izpack.compiler.listener;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.exception.CompilerException;
import com.izforge.izpack.compiler.packager.IPackager;
import com.izforge.izpack.data.PackInfo;

import java.util.Map;

/**
 * <p>
 * This class implements all methods of interface CompilerListener, but do not
 * do anything else. It can be used as base class to save implementation of
 * unneeded methods.
 * </p>
 *
 * @author Klaus Bartz
 */
public class SimpleCompilerListener implements CompilerListener
{

    /**
     * Creates a newly object.
     */
    public SimpleCompilerListener()
    {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.izforge.izpack.compiler.listener.CompilerListener#reviseAttributSetFile(java.util.Map,
     * com.izforge.izpack.api.adaptator.IXMLElement)
     */
    public Map<String, ?> reviseAdditionalDataMap(Map<String, ?> existentDataMap, IXMLElement element)
            throws CompilerException
    {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.izforge.izpack.compiler.listener.CompilerListener#AfterPack(com.izforge.izpack.compiler.Compiler.Pack,
     * int, com.izforge.izpack.compiler.packager.impl.Packager)
     */
    public void afterPack(PackInfo pack, int packNumber, IPackager packager)
    {
    }

    /*
     * (non-Javadoc)
     *
     * @see com.izforge.izpack.compiler.listener.CompilerListener#BeforePack(com.izforge.izpack.compiler.Compiler.Pack,
     * int, com.izforge.izpack.compiler.packager.impl.Packager)
     */
    public void beforePack(PackInfo pack, int packNumber, IPackager packager)
    {
    }

    /*
     * (non-Javadoc)
     *
     * @see com.izforge.izpack.compiler.listener.CompilerListener#notify(java.lang.String, int,
     * com.izforge.izpack.api.adaptator.IXMLElement, com.izforge.izpack.compiler.packager.impl.Packager)
     */
    public void notify(String position, int state, IXMLElement data, IPackager packager)
    {
    }

}

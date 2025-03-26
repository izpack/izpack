/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.compiler.resource;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.IXMLParser;
import com.izforge.izpack.api.exception.CompilerException;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.data.PropertyManager;
import com.izforge.izpack.compiler.helper.AssertionHelper;
import com.izforge.izpack.compiler.helper.XmlCompilerHelper;
import jakarta.inject.Inject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Anthonin Bonnefoy
 */
public class ResourceFinder
{
    private final CompilerData compilerData;
    private final AssertionHelper assertionHelper;
    private final PropertyManager propertyManager;
    private final XmlCompilerHelper xmlCompilerHelper;

    @Inject
    public ResourceFinder(CompilerData compilerData, PropertyManager propertyManager)
    {
        this.compilerData = compilerData;
        this.assertionHelper = new AssertionHelper(compilerData.getInstallFile());
        this.propertyManager = propertyManager;
        this.xmlCompilerHelper = new XmlCompilerHelper(assertionHelper);
    }

    /**
     * Look for a project specified resources, which, if not absolute, are sought relative to the
     * projects basedir. The path should use '/' as the fileSeparator. If the resource is not found,
     * a CompilerException is thrown indicating fault in the parent element.
     *
     * @param path   the relative path (using '/' as separator) to the resource.
     * @param desc   the description of the resource used to report errors
     * @param parent the IXMLElement the resource is specified in, used to report errors
     * @return a URL to the resource.
     */
    public URL findProjectResource(String path, String desc, IXMLElement parent) throws CompilerException
    {
        return findProjectResource(new File(compilerData.getBasedir()), path, desc, parent);
    }

    /**
     * Look for a project specified resources, which, if not absolute, are sought relative to the
     * projects basedir. The path should use '/' as the fileSeparator. If the resource is not found,
     * a CompilerException is thrown indicating fault in the parent element.
     *
     * @param basedir the base dir which relative paths will be resolved from
     * @param path   the relative path (using '/' as separator) to the resource.
     * @param desc   the description of the resource used to report errors
     * @param parent the IXMLElement the resource is specified in, used to report errors
     * @return a URL to the resource.
     */
    public URL findProjectResource(File basedir, String path, String desc, IXMLElement parent) throws CompilerException
    {
        URL url = null;
        File resource = new File(path);
        if (!resource.isAbsolute())
        {
            resource = new File(basedir, path);
        }

        if (!resource.exists()) // fatal
        {
            assertionHelper.parseError(parent, desc + " not found: " + resource);
        }

        try
        {
            url = resource.toURI().toURL();
        }
        catch (MalformedURLException how)
        {
            assertionHelper.parseError(parent, desc + "(" + resource + ")", how);
        }

        return url;
    }

    /**
     * Look for an IzPack resource either in the compiler jar, or within IZPACK_HOME. The path must
     * not be absolute. The path must use '/' as the fileSeparator (it's used to access the jar
     * file). If the resource is not found, take appropriate action base on ignoreWhenNotFound flag.
     *
     * @param path               the relative path (using '/' as separator) to the resource.
     * @param desc               the description of the resource used to report errors
     * @param parent             the IXMLElement the resource is specified in, used to report errors
     * @param ignoreWhenNotFound when false, throws a CompilerException indicating
     *                           fault in the parent element when resource not found.
     */
    public URL findIzPackResource(String path, String desc, IXMLElement parent, boolean ignoreWhenNotFound)
            throws CompilerException
    {
        URL url = getClass().getResource("/" + path);
        if (url == null)
        {
            File resource = new File(path);

            if (!resource.isAbsolute())
            {
                resource = new File(CompilerData.IZPACK_HOME, path);
            }

            if (resource.exists())
            {
                try
                {
                    url = resource.toURI().toURL();
                }
                catch (MalformedURLException how)
                {
                    assertionHelper.parseError(parent, desc + "(" + resource + ")", how);
                }
            }
            else
            {
                if (ignoreWhenNotFound)
                {
                    assertionHelper.parseWarn(parent, desc + " not found: " + resource);
                }
                else
                {
                    assertionHelper.parseError(parent, desc + " not found: " + resource);
                }
            }

        }

        return url;
    }

    public URL findIzPackResource(String path, String desc, IXMLElement parent)
            throws CompilerException
    {
        return findIzPackResource(path, desc, parent, false);
    }

    /**
     * Returns the IXMLElement representing the installation XML file.
     *
     * @return The XML tree.
     * @throws com.izforge.izpack.api.exception.CompilerException
     *                             For problems with the installation file
     * @throws java.io.IOException for errors reading the installation file
     */
    public IXMLElement getXMLTree(IXMLParser parser) throws IOException
    {
        IXMLElement data;
        if (compilerData.getInstallFile() != null)
        {
            File file = new File(compilerData.getInstallFile()).getAbsoluteFile();
            assertionHelper.assertIsNormalReadableFile(file, "Configuration file");
            FileInputStream inputStream = new FileInputStream(compilerData.getInstallFile());
            data = parser.parse(inputStream, file.getAbsolutePath());
            inputStream.close();
            // add izpack built in property
            propertyManager.setProperty("izpack.file", file.toString());
        }
        else if (compilerData.getInstallText() != null)
        {
            data = parser.parse(compilerData.getInstallText());
        }
        else
        {
            throw new CompilerException("Neither install file nor text specified");
        }
        // We check it
        if (!"installation".equalsIgnoreCase(data.getElement().getLocalName()))
        {
            assertionHelper.parseError(data, "this is not an IzPack XML installation file");
        }
        if (!CompilerData.VERSION.equalsIgnoreCase(xmlCompilerHelper.requireAttribute(data, "version")))
        {
            assertionHelper.parseError(data, "the file version is different from the compiler version");
        }

        // We finally return the tree
        return data;
    }
}

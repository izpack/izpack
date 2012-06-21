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

package com.izforge.izpack.util.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.IXMLParser;
import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;

/**
 * This class contains some helper methods to simplify handling of xml specification files.
 *
 * @author Klaus Bartz
 */
public class SpecHelper
{
    public static final String YES = "yes";

    public static final String NO = "no";

    private String specFilename;

    private IXMLElement spec;

    private boolean _haveSpec;

    /**
     * The resources.
     */
    private final Resources resources;

    private static final String PACK_KEY = "pack";

    private static final String PACK_NAME = "name";

    /**
     * Constructs a <tt>SpecHelper</tt>.
     *
     * @param resources the resources
     */
    public SpecHelper(Resources resources)
    {
        super();
        this.resources = resources;
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Reads the XML specification given by the file name. The result is stored in spec.
     *
     * @throws Exception for any problems in reading the specification
     */
    /*--------------------------------------------------------------------------*/
    public void readSpec(String specFileName) throws Exception
    {
        readSpec(specFileName, null);
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Reads the XML specification given by the file name. The result is stored in spec.
     *
     * @throws Exception for any problems in reading the specification
     */
    /*--------------------------------------------------------------------------*/
    public void readSpec(String specFileName, VariableSubstitutor substitutor) throws Exception
    {
        // open an input stream
        InputStream input = null;
        try
        {
            input = getResource(specFileName);
        }
        catch (Exception exception)
        {
            _haveSpec = false;
            return;
        }
        if (input == null)
        {
            _haveSpec = false;
            return;
        }

        readSpec(input, substitutor);

        // close the stream
        input.close();
        this.specFilename = specFileName;
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Reads the XML specification given by the input stream. The result is stored in spec.
     *
     * @throws Exception for any problems in reading the specification
     */
    /*--------------------------------------------------------------------------*/
    public void readSpec(InputStream input) throws Exception
    {
        readSpec(input, null);
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Reads the XML specification given by the input stream. The result is stored in spec.
     *
     * @throws Exception for any problems in reading the specification
     */
    /*--------------------------------------------------------------------------*/
    public void readSpec(InputStream input, VariableSubstitutor substitutor) throws Exception
    {
        // first try to substitute the variables
        if (substitutor != null)
        {
            input = substituteVariables(input, substitutor);
        }

        // initialize the parser
        IXMLParser parser = new XMLParser();

        // get the data
        spec = parser.parse(input);
        _haveSpec = true;
    }

    /**
     * Gets the stream to a resource.
     *
     * @param res The resource id.
     * @return The resource value, null if not found
     */
    public InputStream getResource(String res)
    {
        try
        {
            // System.out.println ("retrieving resource " + res);
            return resources.getInputStream(res);
        }
        catch (ResourceNotFoundException exception)
        {
            return null;
        }
    }

    /**
     * Returns a XML element which represents the pack for the given name.
     *
     * @param packDestName name of the pack which should be returned
     * @return a XML element which represents the pack for the given name
     */
    public IXMLElement getPackForName(String packDestName)
    {
        List<IXMLElement> packs = getSpec().getChildrenNamed(PACK_KEY);
        if (packs == null)
        {
            return (null);
        }
        for (IXMLElement pack : packs)
        {
            String packName = pack.getAttribute(PACK_NAME);
            if (packName.equals(packDestName))
            {
                return (pack);
            }
        }
        return (null);

    }

    /**
     * Create parse error with consistent messages. Includes file name and line # of parent. It is
     * an error for 'parent' to be null.
     *
     * @param parent  The element in which the error occured
     * @param message Brief message explaining error
     */
    public void parseError(IXMLElement parent, String message) throws InstallerException
    {
        throw new InstallerException(specFilename + ":" + parent.getLineNr() + ": " + message);
    }

    /**
     * Returns true if a specification exist, else false.
     *
     * @return true if a specification exist, else false
     */
    public boolean haveSpec()
    {
        return _haveSpec;
    }

    /**
     * Returns the specification.
     *
     * @return the specification
     */
    public IXMLElement getSpec()
    {
        return spec;
    }

    /**
     * Sets the specifaction to the given XML element.
     *
     * @param element
     */
    public void setSpec(IXMLElement element)
    {
        spec = element;
    }

    /**
     * Returns a Vector with all leafs of the tree which is described with childdef.
     *
     * @param root     the IXMLElement which is the current root for the search
     * @param childdef a String array which describes the tree; the last element contains the leaf
     *                 name
     * @return a Vector of XMLElements of all leafs founded under root
     */
    public List<IXMLElement> getAllSubChildren(IXMLElement root, String[] childdef)
    {
        return (getSubChildren(root, childdef, 0));
    }

    /**
     * Returns a Vector with all leafs of the tree which is described with childdef beginning at the
     * given depth.
     *
     * @param root     the IXMLElement which is the current root for the search
     * @param childdef a String array which describes the tree; the last element contains the leaf
     *                 name
     * @param depth    depth to start in childdef
     * @return a Vector of XMLElements of all leafs founded under root
     */
    private List<IXMLElement> getSubChildren(IXMLElement root, String[] childdef, int depth)
    {
        List<IXMLElement> retval = null;
        List<IXMLElement> retval2 = null;
        List<IXMLElement> children = root != null ? root.getChildrenNamed(childdef[depth]) : null;
        if (children == null)
        {
            return (null);
        }
        if (depth < childdef.length - 1)
        {
            for (IXMLElement child : children)
            {
                retval2 = getSubChildren(child, childdef, depth + 1);
                if (retval2 != null)
                {
                    if (retval == null)
                    {
                        retval = new ArrayList<IXMLElement>();
                    }
                    retval.addAll(retval2);
                }
            }
        }
        else
        {
            return (children);
        }
        return (retval);
    }

    /**
     * Creates an temp file in to the substitutor the substituted contents of input writes; close it
     * and (re)open it as FileInputStream. The file will be deleted on exit.
     *
     * @param input       the opened input stream which contents should be substituted
     * @param substitutor substitutor which should substitute the contents of input
     * @return a file input stream of the created temporary file
     * @throws Exception
     */
    public InputStream substituteVariables(InputStream input, VariableSubstitutor substitutor)
            throws Exception
    {
        File tempFile = File.createTempFile("izpacksubs", "");
        FileOutputStream fos = null;
        tempFile.deleteOnExit();
        try
        {
            fos = new FileOutputStream(tempFile);
            substitutor.substitute(input, fos, null, "UTF-8");
        }
        finally
        {
            if (fos != null)
            {
                fos.close();
            }
        }
        return new FileInputStream(tempFile);
    }

    /**
     * Returns whether the value to the given attribute is "yes" or not. If the attribute does not
     * exist, or the value is not "yes" and not "no", the default value is returned.
     *
     * @param element      the XML element which contains the attribute
     * @param attribute    the name of the attribute
     * @param defaultValue the default value
     * @return whether the value to the given attribute is "yes" or not
     */
    public boolean isAttributeYes(IXMLElement element, String attribute, boolean defaultValue)
    {
        String value = element.getAttribute(attribute, (defaultValue ? YES : NO));
        if (value.equalsIgnoreCase(YES))
        {
            return true;
        }
        if (value.equalsIgnoreCase(NO))
        {
            return false;
        }

        return defaultValue;
    }

    /**
     * Returns the attribute for the given attribute name. If no attribute exist, an
     * InstallerException with a detail message is thrown.
     *
     * @param element  XML element which should contain the attribute
     * @param attrName key of the attribute
     * @return the attribute as string
     * @throws InstallerException
     */
    public String getRequiredAttribute(IXMLElement element, String attrName)
            throws InstallerException
    {
        String attr = element.getAttribute(attrName);
        if (attr == null)
        {
            parseError(element, "<" + element.getName() + "> requires attribute '" + attrName
                    + "'.");
        }
        return (attr);
    }
}

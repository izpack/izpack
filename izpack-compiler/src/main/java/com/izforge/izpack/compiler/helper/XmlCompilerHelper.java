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

package com.izforge.izpack.compiler.helper;

import java.net.MalformedURLException;
import java.net.URL;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.exception.CompilerException;

/**
 * Helper methods for compiler
 *
 * @author Anthonin Bonnefoy
 */
public class XmlCompilerHelper
{
    private AssertionHelper assertionHelper;

    public XmlCompilerHelper(AssertionHelper assertionHelper)
    {
        this.assertionHelper = assertionHelper;
    }

    /**
     * Call getContent on an element, producing a meaningful error message if not present, or empty.
     * It is an error for 'element' to be null.
     *
     * @param element The element to get content of
     */
    public String requireContent(IXMLElement element) throws CompilerException
    {
        String content = element.getContent();
        if (content == null || content.length() == 0)
        {
            assertionHelper.parseError(element, "<" + element.getName() + "> requires content");
        }
        return content;
    }

    /**
     * Call getContent on an element, producing a meaningful error message if not present, or empty,
     * or a valid URL. It is an error for 'element' to be null.
     *
     * @param element The element to get content of
     */
    public URL requireURLContent(IXMLElement element) throws CompilerException
    {
        URL url = null;
        try
        {
            url = new URL(requireContent(element).replace(" ", "%20"));
        }
        catch (MalformedURLException x)
        {
            assertionHelper.parseError(element, "<" + element.getName() + "> requires valid URL", x);
        }
        return url;
    }

    /**
     * Call getFirstChildNamed on the parent, producing a meaningful error message on failure. It is
     * an error for 'parent' to be null.
     *
     * @param parent The element to search for a child
     * @param name   Name of the child element to get
     */
    public IXMLElement requireChildNamed(IXMLElement parent, String name) throws CompilerException
    {
        IXMLElement child = parent.getFirstChildNamed(name);
        if (child == null)
        {
            assertionHelper.parseError(parent, "<" + parent.getName() + "> requires child <" + name + ">");
        }
        return child;
    }

    /**
     * Get a required attribute of an element, ensuring it is an integer. A meaningful error message
     * is generated as a CompilerException if not present or parseable as an int. It is an error for
     * 'element' or 'attribute' to be null.
     *
     * @param element   The element to get the attribute value of
     * @param attribute The name of the attribute to get
     */
    public int requireIntAttribute(IXMLElement element, String attribute)
            throws CompilerException
    {
        String value = element.getAttribute(attribute);
        if (value == null || value.length() == 0)
        {
            assertionHelper.parseError(element, "<" + element.getName() + "> requires attribute '" + attribute
                    + "'");
        }
        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException x)
        {
            assertionHelper.parseError(element, "'" + attribute + "' must be an integer");
        }
        return 0; // never happens
    }

    /**
     * Call getAttribute on an element, producing a meaningful error message if not present, or one
     * of "yes" or "no". It is an error for 'element' or 'attribute' to be null.
     *
     * @param element   The element to get the attribute value of
     * @param attribute The name of the attribute to get
     */
    public boolean requireYesNoAttribute(IXMLElement element, String attribute)
            throws CompilerException
    {
        return validateYesNoAttribute(element, attribute, null);
    }

    /**
     * Call getAttribute on an element, producing a meaningful warning if not "yes" or "no". If the
     * 'element' or 'attribute' are null, the default value is returned.
     *
     * @param element      The element to get the attribute value of
     * @param attribute    The name of the attribute to get
     * @param defaultValue Value returned if attribute not present or invalid
     */
    public boolean validateYesNoAttribute(IXMLElement element, String attribute,
            Boolean defaultValue) throws CompilerException
    {
        if (element != null)
        {
            String value = element.getAttribute(attribute);

            if (value == null)
            {
                if (defaultValue == null)
                {
                    assertionHelper.parseError(element, "<" + element.getName() + "> undefined value of attribute '" + attribute + "' which is not associated with a default value");
                }
            }
            else
            {
                if ("yes".equalsIgnoreCase(value.trim()) || "true".equalsIgnoreCase(value.trim()))
                {
                    return true;
                }
                else
                {
                    if ("no".equalsIgnoreCase(value.trim()) || "false".equalsIgnoreCase(value.trim()))
                    {
                        return false;
                    }
                }

                final String msg = "<" + element.getName() + "> invalid value \"" + value + "\" for attribute '" + attribute
                        + "': Expected (yes|no|true|false)";

                if (defaultValue == null)
                {
                    assertionHelper.parseError(element, msg + " and no default value specified");
                }
                else
                {
                    assertionHelper.parseWarn(element, msg);
                }
            }
        }

        return defaultValue.booleanValue();
    }

    /**
     * Call getAttribute on an element, producing a meaningful error message if not present, or
     * empty. It is an error for 'element' or 'attribute' to be null.
     *
     * @param element   The element to get the attribute value of
     * @param attribute The name of the attribute to get
     */
    public String requireAttribute(IXMLElement element, String attribute)
            throws CompilerException
    {
        String value = element.getAttribute(attribute);
        if (value == null)
        {
            assertionHelper.parseError(element, "<" + element.getName() + "> requires attribute '" + attribute
                    + "'");
        }
        return value;
    }
}

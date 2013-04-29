/*
 * IzPack - Copyright 2001-2009 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2009 Dennis Reil
 * Copyright 2010 Rene Krell
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

package com.izforge.izpack.core.rules.process;

import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.rules.Condition;

/**
 * This condition checks if a certain type is empty
 */
public class EmptyCondition extends Condition
{
    private static final long serialVersionUID = -5036558553194497000L;

    private static final transient Logger logger = Logger.getLogger(EmptyCondition.class.getName());

    private ContentType contentType;
    private String content;

    public EmptyCondition() {}

    @Override
    public boolean isTrue()
    {
        boolean result = false;
        Variables variables = getInstallData().getVariables();
        switch (contentType)
        {
            case STRING:
                if (this.content == null)
                {
                    return true;
                }
                String s = variables.replace(this.content);
                if (s != null && s.length() == 0)
                {
                    result = true;
                }
                break;

            case VARIABLE:
                if (this.content != null)
                {
                    String value = this.getInstallData().getVariable(this.content);
                    if (value != null && value.length() == 0)
                    {
                        result = true;
                    }
                }
                break;

            case FILE:
                if (this.content != null)
                {
                    File file = new File(variables.replace(this.content));
                    if (!file.exists() && file.length() == 0)
                    {
                        result = true;
                    }
                }
                break;

            case DIR:
                if (this.content != null)
                {
                    File file = new File(variables.replace(this.content));
                    if (!file.exists() || file.isDirectory() && file.listFiles().length == 0)
                    {
                        result = true;
                    }
                }
                break;

            default:
                logger.warning("Illegal content type '" + contentType.getAttribute() + "' of ExistsCondition");
                break;
        }
        return result;
    }

    @Override
    public void readFromXML(IXMLElement xmlcondition) throws Exception
    {
        if (xmlcondition != null)
        {
            if (xmlcondition.getChildrenCount() != 1)
            {
                throw new Exception("Condition \"" + getId() + "\" needs exactly one nested element");
            }
            IXMLElement child = xmlcondition.getChildAtIndex(0);
            this.contentType = ContentType.getFromAttribute(child.getName());
            if (this.contentType != null)
            {
                this.content = child.getContent();
            }
            else
            {
                throw new Exception(
                        "Unknown nested element '" + child.getName() + "' to condition \"" + getId() + "\"");
            }
            if (this.content == null || this.content.length() == 0)
            {
                throw new Exception("Condition \"" + getId() + "\" has a nested element without valid contents");
            }
        }
    }

    public ContentType getContentType()
    {
        return contentType;
    }


    public void setContentType(ContentType contentType)
    {
        this.contentType = contentType;
    }


    public String getContent()
    {
        return content;
    }


    public void setContent(String content)
    {
        this.content = content;
    }

    @Override
    public void makeXMLData(IXMLElement conditionRoot)
    {
        XMLElementImpl el = new XMLElementImpl(this.contentType.getAttribute(), conditionRoot);
        el.setContent(this.content);
        conditionRoot.addChild(el);
    }

    public enum ContentType
    {
        VARIABLE("variable"), STRING("string"), FILE("file"), DIR("dir");

        private static Map<String, ContentType> lookup;

        private String attribute;

        ContentType(String attribute)
        {
            this.attribute = attribute;
        }

        static
        {
            lookup = new HashMap<String, ContentType>();
            for (ContentType operation : EnumSet.allOf(ContentType.class))
            {
                lookup.put(operation.getAttribute(), operation);
            }
        }

        public String getAttribute()
        {
            return attribute;
        }

        public static ContentType getFromAttribute(String attribute)
        {
            if (attribute != null && lookup.containsKey(attribute))
            {
                return lookup.get(attribute);
            }
            return null;
        }
    }
}

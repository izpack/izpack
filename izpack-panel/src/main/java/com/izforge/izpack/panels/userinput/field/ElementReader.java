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

import java.util.ArrayList;
import java.util.List;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.util.OsConstraintHelper;


/**
 * Configuration element reader.
 *
 * @author Tim Anderson
 */
public class ElementReader
{

    /**
     * The configuration.
     */
    private final Config config;

    /**
     * Constructs a {@code ElementReader}.
     *
     * @param config the configuration to read from
     */
    public ElementReader(Config config)
    {
        this.config = config;
    }

    /**
     * Returns the pack names associated with an element.
     *
     * @param element the element
     * @return the 'name' attributes from the "createForPack" elements
     */
    public List<String> getPacks(IXMLElement element)
    {
        List<IXMLElement> elements = element.getChildrenNamed("createForPack");
        return getNames(elements);
    }

    /**
     * Returns the unselected pack names associated with an element.
     *
     * @param element the element
     * @return the 'name' attributes from the "createForUnselectedPack" elements
     */
    public List<String> getUnselectedPacks(IXMLElement element)
    {
        List<IXMLElement> elements = element.getChildrenNamed("createForUnselectedPack");
        return getNames(elements);
    }

    /**
     * Returns the OS models associated with an element.
     *
     * @param element the element
     * @return the OSes
     */
    public List<OsModel> getOsModels(IXMLElement element)
    {
        return OsConstraintHelper.getOsList(element);
    }

    /**
     * Returns the configuration.
     *
     * @return the configuration
     */
    protected Config getConfig()
    {
        return config;
    }

    /**
     * Returns the names associated with the supplied elements.
     *
     * @param elements the elements
     * @return the "name" attributes
     */
    private List<String> getNames(List<IXMLElement> elements)
    {
        List<String> result = new ArrayList<String>();
        for (IXMLElement element : elements)
        {
            result.add(config.getAttribute(element, "name"));
        }
        return result;
    }

}

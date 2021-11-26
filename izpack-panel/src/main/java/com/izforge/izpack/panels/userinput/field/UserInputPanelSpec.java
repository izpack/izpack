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

package com.izforge.izpack.panels.userinput.field;


import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.util.PlatformModelMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * The user input panel specification.
 *
 * @author Tim Anderson
 */
public class UserInputPanelSpec
{

    /**
     * The name of the XML file that specifies the panel layout.
     */
    public static final String SPEC_FILE_NAME = "userInputSpec.xml";

    /**
     * Panel element name.
     */
    public static final String PANEL = "panel";

    /**
     * Field element name.
     */
    public static final String FIELD = "field";

    /**
     * Provides access to the XML configuration.
     */
    private final Config config;

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * The platform-model matcher.
     */
    private final PlatformModelMatcher matcher;

    /**
     * The panel identifier attribute name
     */
    private static final String PANEL_IDENTIFIER = "id";

    /**
     * Constructs a {@code UserInputPanelSpec}.
     *
     * @param resources   the resources
     * @param installData the installation data
     * @param factory     the factory
     * @param matcher     the platform-model matcher
     */
    public UserInputPanelSpec(Resources resources, InstallData installData, ObjectFactory factory,
                              PlatformModelMatcher matcher)
    {
        config = new Config(SPEC_FILE_NAME, resources, installData, factory);
        this.installData = installData;
        this.matcher = matcher;
    }

    /**
     * Returns the specification for the supplied panel.
     *
     * @param panel the panel
     * @return the corresponding specification
     * @throws IzPackException if there is no specification for the panel
     */
    public IXMLElement getPanelSpec(Panel panel)
    {
        String panelId = panel.getPanelId();
        List<IXMLElement> panels = config.getRoot().getChildrenNamed(PANEL);
        IXMLElement result = null;
        for (IXMLElement spec : panels)
        {
            String id = spec.getAttribute(PANEL_IDENTIFIER);

            if (id != null && panelId != null && panelId.equals(id))
            {
                // use the current element as spec
                result = spec;
                break;
            }
        }
        if (result == null)
        {
            throw new IzPackException("No user input specification with " + PANEL_IDENTIFIER + "=" + panelId);
        }
        return result;
    }

    /**
     * Creates fields for each field configuration in the panel.
     *
     * @param panel the panel configuration
     * @return the fields
     */
    public List<Field> createFields(IXMLElement panel)
    {
        List<Field> result = new ArrayList<Field>();
        List<IXMLElement> elements = panel.getChildrenNamed(FIELD);
        FieldFactory factory = new FieldFactory(config, installData, matcher);

        for (IXMLElement element : elements)
        {
            Field field = factory.create(element);
            result.add(field);
        }
        return result;
    }

    /**
     * Returns the configuration.
     *
     * @return the configuration
     */
    public Config getConfig()
    {
        return config;
    }

}

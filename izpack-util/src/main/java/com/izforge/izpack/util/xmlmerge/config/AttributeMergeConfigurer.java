/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2009 Laurent Bovet, Alex Mathey
 * Copyright 2010, 2012 René Krell
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

package com.izforge.izpack.util.xmlmerge.config;

import com.izforge.izpack.util.xmlmerge.ConfigurationException;
import com.izforge.izpack.util.xmlmerge.Configurer;
import com.izforge.izpack.util.xmlmerge.Matcher;
import com.izforge.izpack.util.xmlmerge.MergeAction;
import com.izforge.izpack.util.xmlmerge.OperationFactory;
import com.izforge.izpack.util.xmlmerge.XmlMerge;
import com.izforge.izpack.util.xmlmerge.action.FullMergeAction;
import com.izforge.izpack.util.xmlmerge.action.StandardActions;
import com.izforge.izpack.util.xmlmerge.factory.AttributeOperationFactory;
import com.izforge.izpack.util.xmlmerge.factory.OperationResolver;
import com.izforge.izpack.util.xmlmerge.factory.StaticOperationFactory;
import com.izforge.izpack.util.xmlmerge.mapper.NamespaceFilterMapper;
import com.izforge.izpack.util.xmlmerge.matcher.AttributeMatcher;
import com.izforge.izpack.util.xmlmerge.matcher.StandardMatchers;

/**
 * Configure to apply actions declared as attributes in the patch DOM.
 *
 * @author Laurent Bovet (LBO)
 * @author Alex Mathey (AMA)
 */
public class AttributeMergeConfigurer implements Configurer
{

    /**
     * Attribute namespace.
     */
    public static final String ATTRIBUTE_NAMESPACE = "http://izpack.org";

    /**
     * Action attribute.
     */
    public static final String ACTION_ATTRIBUTE = "action";

    /**
     * Matcher attribute.
     */
    public static final String MATCHER_ATTRIBUTE = "matcher";

    @Override
    public void configure(XmlMerge xmlMerge) throws ConfigurationException
    {

        MergeAction defaultMergeAction = new FullMergeAction();

        OperationFactory mapperFactory = new StaticOperationFactory(new NamespaceFilterMapper(ATTRIBUTE_NAMESPACE));
        defaultMergeAction.setMapperFactory(mapperFactory);

        // Configure the action factory
        OperationResolver actionResolver = new OperationResolver(StandardActions.class);

        OperationFactory actionFactory = new AttributeOperationFactory(defaultMergeAction,
                actionResolver, ACTION_ATTRIBUTE, ATTRIBUTE_NAMESPACE);
        defaultMergeAction.setActionFactory(actionFactory);

        // Configure the matcher factory
        Matcher defaultMatcher = new AttributeMatcher();

        OperationResolver matcherResolver = new OperationResolver(StandardMatchers.class);

        defaultMergeAction.setMatcherFactory(new AttributeOperationFactory(defaultMatcher,
                matcherResolver, MATCHER_ATTRIBUTE, ATTRIBUTE_NAMESPACE));

        xmlMerge.setRootMergeMapperFactory(mapperFactory);
        xmlMerge.setRootMergeActionFactory(actionFactory);
    }

}

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

import java.util.LinkedHashMap;
import java.util.Map;

import com.izforge.izpack.util.xmlmerge.Action;
import com.izforge.izpack.util.xmlmerge.ConfigurationException;
import com.izforge.izpack.util.xmlmerge.Configurer;
import com.izforge.izpack.util.xmlmerge.Mapper;
import com.izforge.izpack.util.xmlmerge.Matcher;
import com.izforge.izpack.util.xmlmerge.MergeAction;
import com.izforge.izpack.util.xmlmerge.Operation;
import com.izforge.izpack.util.xmlmerge.XmlMerge;
import com.izforge.izpack.util.xmlmerge.action.FullMergeAction;
import com.izforge.izpack.util.xmlmerge.action.StandardActions;
import com.izforge.izpack.util.xmlmerge.factory.OperationResolver;
import com.izforge.izpack.util.xmlmerge.factory.XPathOperationFactory;
import com.izforge.izpack.util.xmlmerge.mapper.IdentityMapper;
import com.izforge.izpack.util.xmlmerge.mapper.StandardMappers;
import com.izforge.izpack.util.xmlmerge.matcher.AttributeMatcher;
import com.izforge.izpack.util.xmlmerge.matcher.StandardMatchers;

/**
 * Superclass for configurers using XPathOperationFactory.
 *
 * @author Laurent Bovet (LBO)
 * @author Alex Mathey (AMA)
 */
public abstract class AbstractXPathConfigurer implements Configurer
{

    /**
     * Matcher resolver.
     */
    OperationResolver matcherResolver = new OperationResolver(StandardMatchers.class);

    /**
     * Action resolver.
     */
    OperationResolver actionResolver = new OperationResolver(StandardActions.class);

    /**
     * Mapper resolver.
     */
    OperationResolver mapperResolver = new OperationResolver(StandardMappers.class);

    /**
     * Root merge action.
     */
    MergeAction rootMergeAction = new FullMergeAction();

    /**
     * Default matcher.
     */
    Matcher defaultMatcher = new AttributeMatcher();

    /**
     * Default mapper.
     */
    Mapper defaultMapper = new IdentityMapper();

    /**
     * Default action.
     */
    Action defaultAction = new FullMergeAction();

    /**
     * Map associating XPath expressions with matchers.
     */
    Map<String, Operation> matchers = new LinkedHashMap<String, Operation>();

    /**
     * Map associating XPath expressions with actions.
     */
    Map<String, Operation> actions = new LinkedHashMap<String, Operation>();

    /**
     * Map associating XPath expressions with mappers.
     */
    Map<String, Operation> mappers = new LinkedHashMap<String, Operation>();

    /**
     * Sets the configurer's default matcher.
     *
     * @param matcherName The name of the default matcher
     * @throws ConfigurationException If an error occurred during configuration
     */
    protected final void setDefaultMatcher(String matcherName) throws ConfigurationException
    {
        defaultMatcher = (Matcher) matcherResolver.resolve(matcherName);
    }

    /**
     * Sets the configurer's default mapper.
     *
     * @param mapperName The name of the default mapper
     * @throws ConfigurationException If an error occurred during configuration
     */
    protected final void setDefaultMapper(String mapperName) throws ConfigurationException
    {
        defaultMapper = (Mapper) mapperResolver.resolve(mapperName);
    }

    /**
     * Sets the configurer's default action.
     *
     * @param actionName The name of the default action
     * @throws ConfigurationException If an error occurred during configuration
     */
    protected final void setDefaultAction(String actionName) throws ConfigurationException
    {
        defaultAction = (Action) actionResolver.resolve(actionName);
    }

    /**
     * Sets the configurer's root merge action.
     *
     * @param actionName The name of the root merge action
     * @throws ConfigurationException If an error occurred during configuration
     */
    protected final void setRootMergeAction(String actionName) throws ConfigurationException
    {
        rootMergeAction = (MergeAction) actionResolver.resolve(actionName);
    }

    /**
     * Adds a matcher for a given XPath expression.
     *
     * @param xPath An XPath expression
     * @param matcherName The name of the matcher to add
     * @throws ConfigurationException If an error occurred during configuration
     */
    protected final void addMatcher(String xPath, String matcherName) throws ConfigurationException
    {
        matchers.put(xPath, matcherResolver.resolve(matcherName));
    }

    /**
     * Adds an action for a given XPath expression.
     *
     * @param xPath An XPath expression
     * @param actionName The name of the action to add
     * @throws ConfigurationException If an error occurred during configuration
     */
    protected final void addAction(String xPath, String actionName) throws ConfigurationException
    {
        actions.put(xPath, actionResolver.resolve(actionName));
    }

    /**
     * Adds an mapper for a given XPath expression.
     *
     * @param xPath An XPath expression
     * @param mapperName The name of the mapper to add
     * @throws ConfigurationException If an error occurred during configuration
     */
    protected final void addMapper(String xPath, String mapperName) throws ConfigurationException
    {
        mappers.put(xPath, mapperResolver.resolve(mapperName));
    }

    @Override
    public final void configure(XmlMerge xmlMerge) throws ConfigurationException
    {
        readConfiguration();

        XPathOperationFactory matcherFactory = new XPathOperationFactory();
        matcherFactory.setDefaultOperation(defaultMatcher);
        matcherFactory.setOperationMap(matchers);
        rootMergeAction.setMatcherFactory(matcherFactory);

        XPathOperationFactory mapperFactory = new XPathOperationFactory();
        mapperFactory.setDefaultOperation(defaultMapper);
        mapperFactory.setOperationMap(mappers);
        rootMergeAction.setMapperFactory(mapperFactory);

        XPathOperationFactory actionFactory = new XPathOperationFactory();
        actionFactory.setDefaultOperation(defaultAction);
        actionFactory.setOperationMap(actions);
        rootMergeAction.setActionFactory(actionFactory);

        xmlMerge.setRootMergeActionFactory(actionFactory);
        xmlMerge.setRootMergeMapperFactory(mapperFactory);
        xmlMerge.setRootMergeMatcherFactory(matcherFactory);
    }

    /**
     * Reads the configuration used to configure an XmlMerge.
     *
     * @throws ConfigurationException If an error occurred during the read
     */
    protected abstract void readConfiguration() throws ConfigurationException;

    /**
     * Sets the configurer's action resolver.
     *
     * @param actionResolver The action resolver to set
     */
    public void setActionResolver(OperationResolver actionResolver)
    {
        this.actionResolver = actionResolver;
    }

    /**
     * Sets the configurer's mapper resolver.
     *
     * @param mapperResolver The mapper resolver to set
     */
    public void setMapperResolver(OperationResolver mapperResolver)
    {
        this.mapperResolver = mapperResolver;
    }

    /**
     * Sets the configurer's matcher resolver.
     *
     * @param matcherResolver the matcher resolver to set
     */
    public void setMatcherResolver(OperationResolver matcherResolver)
    {
        this.matcherResolver = matcherResolver;
    }

}

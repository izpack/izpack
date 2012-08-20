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

package com.izforge.izpack.util.xmlmerge.action;

import com.izforge.izpack.util.xmlmerge.MergeAction;
import com.izforge.izpack.util.xmlmerge.OperationFactory;
import com.izforge.izpack.util.xmlmerge.factory.StaticOperationFactory;
import com.izforge.izpack.util.xmlmerge.mapper.IdentityMapper;
import com.izforge.izpack.util.xmlmerge.matcher.AttributeMatcher;

/**
 * Gathers the operation factory-related behaviour and a default configuration.
 *
 * @author Laurent Bovet (LBO)
 * @author Alex Mathey (AMA)
 */
public abstract class AbstractMergeAction implements MergeAction
{

    /**
     * Action factory.
     */
    protected OperationFactory m_actionFactory = new StaticOperationFactory(this);

    /**
     * Mapper factory.
     */
    protected OperationFactory m_mapperFactory = new StaticOperationFactory(new IdentityMapper());

    /**
     * Matcher factory.
     */
    protected OperationFactory m_matcherFactory = new StaticOperationFactory(new AttributeMatcher());

    @Override
    public void setMapperFactory(OperationFactory factory)
    {
        m_mapperFactory = factory;
    }

    @Override
    public void setMatcherFactory(OperationFactory factory)
    {
        m_matcherFactory = factory;

    }

    @Override
    public void setActionFactory(OperationFactory factory)
    {
        m_actionFactory = factory;
    }

}

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

package com.izforge.izpack.compiler.container;

import java.util.Properties;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.injectors.ProviderAdapter;
import org.picocontainer.parameters.ComponentParameter;

import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.compiler.Compiler;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.cli.CliAnalyzer;
import com.izforge.izpack.compiler.container.provider.CompilerDataProvider;
import com.izforge.izpack.compiler.container.provider.CompressedOutputStreamProvider;
import com.izforge.izpack.compiler.container.provider.JarOutputStreamProvider;
import com.izforge.izpack.compiler.container.provider.PackCompressorProvider;
import com.izforge.izpack.compiler.container.provider.XmlCompilerHelperProvider;
import com.izforge.izpack.compiler.data.PropertyManager;
import com.izforge.izpack.compiler.helper.AssertionHelper;
import com.izforge.izpack.compiler.helper.CompilerHelper;
import com.izforge.izpack.compiler.listener.CmdlinePackagerListener;
import com.izforge.izpack.compiler.resource.ResourceFinder;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.container.PlatformProvider;
import com.izforge.izpack.core.factory.DefaultObjectFactory;
import com.izforge.izpack.core.rules.ConditionContainer;
import com.izforge.izpack.core.rules.RulesEngineImpl;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.MergeManagerImpl;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.PlatformModelMatcher;
import com.izforge.izpack.util.Platforms;

/**
 * Container for compiler.
 *
 * @author Anthonin Bonnefoy
 */
public class CompilerContainer extends AbstractContainer
{

    /**
     * Constructs a <tt>CompilerContainer</tt>
     *
     * @throws ContainerException if initialisation fails
     */
    public CompilerContainer()
    {
        initialise();
    }

    /**
     * Constructs a <tt>CompilerContainer</tt>.
     *
     * @param container the underlying container. May be <tt>null</tt>
     * @throws ContainerException if initialisation fails
     */
    protected CompilerContainer(MutablePicoContainer container)
    {
        super(container);
    }

    /**
     * Fills the container.
     *
     * @param container the underlying container
     * @throws ContainerException if initialisation fails, or the container has already been initialised
     */
    @Override
    protected void fillContainer(MutablePicoContainer container)
    {
        addComponent(Properties.class);
        addComponent(CompilerContainer.class, this);
        addComponent(CliAnalyzer.class);
        addComponent(CmdlinePackagerListener.class);
        addComponent(Compiler.class);
        addComponent(ResourceFinder.class);
        addComponent(CompilerConfig.class);
        addComponent(ConditionContainer.class, ConditionContainer.class);
        addComponent(AssertionHelper.class);
        addComponent(PropertyManager.class);
        addComponent(VariableSubstitutor.class, VariableSubstitutorImpl.class);
        addComponent(CompilerHelper.class);
        container.addComponent(RulesEngine.class, RulesEngineImpl.class,
                               new ComponentParameter(ConditionContainer.class),
                               new ComponentParameter(Platform.class));
        addComponent(MergeManager.class, MergeManagerImpl.class);
        container.addComponent(ObjectFactory.class, DefaultObjectFactory.class,
                               new ComponentParameter(CompilerContainer.class));
        container.addComponent(PlatformModelMatcher.class);
        addComponent(Platforms.class);

        new ResolverContainerFiller().fillContainer(this);
        container.addAdapter(new ProviderAdapter(new XmlCompilerHelperProvider()))
                .addAdapter(new ProviderAdapter(new JarOutputStreamProvider()))
                .addAdapter(new ProviderAdapter(new CompressedOutputStreamProvider()))
                .addAdapter(new ProviderAdapter(new PackCompressorProvider()))
                .addAdapter(new ProviderAdapter(new PlatformProvider()));

    }

    /**
     * Add CompilerDataComponent by processing command line args
     *
     * @param args command line args passed to the main
     */
    public void processCompileDataFromArgs(String[] args)
    {
        getContainer().addAdapter(new ProviderAdapter(new CompilerDataProvider(args)));
    }

}

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

import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.container.CdiInitializationContext;
import jakarta.enterprise.inject.Vetoed;

import java.util.function.Supplier;
import java.util.logging.Handler;

/**
 * Container for compiler.
 *
 * @author Anthonin Bonnefoy
 */
@Vetoed
public class CompilerContainer extends AbstractContainer
{

    /**
     * Constructs a <tt>CompilerContainer</tt>
     *
     * @throws ContainerException if initialisation fails
     */
    public CompilerContainer(Handler handler, CompilerData compilerData, Supplier<String> installFileSupplier)
    {
        initialise(ctx -> {
            ctx.addComponent(Handler.class, handler);
            ctx.addComponent(CompilerData.class, compilerData);
            ctx.addConfig("installFile", installFileSupplier.get());
        });
    }

    /**
     * Constructs a <tt>CompilerContainer</tt>.
     *
     * @param container the underlying container. May be <tt>null</tt>
     * @throws ContainerException if initialisation fails
     */
    protected CompilerContainer(CdiInitializationContext container)
    {
        super(container);
    }

    /**
     * Fills the container.
     *
     * @throws ContainerException if initialisation fails, or the container has already been initialised
     */
    @Override
    protected void fillContainer(CdiInitializationContext context)
    {
        super.fillContainer(context);
        new ResolverContainerFiller().fillContainer(context);

//        addComponent(CompilerContainer.class, this); already added by super.fillContainer()
//        addComponent(Properties.class);
//        addComponent(DefaultVariables.class);
//        addComponent(CliAnalyzer.class);
//        addComponent(CmdlinePackagerListener.class);
//        addComponent(Compiler.class);
//        addComponent(ResourceFinder.class);
//        addComponent(CompilerConfig.class);
//        addComponent(ConditionContainer.class);
//        addComponent(AssertionHelper.class);
//        addComponent(PropertyManager.class);
//        addComponent(VariableSubstitutorImpl.class);
//        addComponent(CompilerHelper.class);
//        addComponent(RulesEngineImpl.class);
//        addComponent(MergeManagerImpl.class);
//        addComponent(DefaultObjectFactory.class);
//        addComponent(PlatformModelMatcher.class);
//        addComponent(XmlCompilerHelperProvider.class);
//        addComponent(JarOutputStreamProvider.class);
//        addComponent(PlatformProvider.class);
    }

}

package com.izforge.izpack.test;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.resource.DefaultLocales;
import com.izforge.izpack.core.resource.DefaultResources;
import com.izforge.izpack.core.rules.ConditionContainer;
import com.izforge.izpack.core.rules.ConditionContainerProvider;
import com.izforge.izpack.core.rules.RulesEngineImpl;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.installer.container.provider.GUIInstallDataProvider;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.Platforms;

import java.util.Properties;

/**
 * Container for condition tests.
 *
 * @author Anthonin Bonnefoy
 */
public class TestConditionContainer extends AbstractContainer
{

    /**
     * Constructs a <tt>TestConditionContainer</tt>.
     *
     * @throws ContainerException if initialisation fails
     */
    public TestConditionContainer()
    {
        initialise();
    }

    /**
     * Invoked by {@link #initialise} to fill the container.
     *
     * @throws ContainerException if initialisation fails
     */
    @Override
    protected void fillContainer()
    {
        addComponent(InstallData.class, GUIInstallData.class);
        addComponent(RulesEngine.class, RulesEngineImpl.class);
        addComponent(VariableSubstitutor.class, VariableSubstitutorImpl.class);
        addComponent(MergeableResolver.class);
        addComponent(Properties.class);
        addComponent(Variables.class, DefaultVariables.class);
        addComponent(Container.class, this);
        addProvider(ConditionContainer.class, ConditionContainerProvider.class);
        addComponent(Platform.class, Platforms.HP_UX);
        addComponent(Locales.class, DefaultLocales.class);
        addComponent(Resources.class, DefaultResources.class);
    }
}

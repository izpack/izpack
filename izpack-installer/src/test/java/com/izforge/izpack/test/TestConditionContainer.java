package com.izforge.izpack.test;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.rules.ConditionContainer;
import com.izforge.izpack.core.rules.RulesEngineImpl;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.Platforms;
import org.picocontainer.MutablePicoContainer;

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
     * @param container the underlying container
     * @throws ContainerException if initialisation fails
     * @throws PicoException for any PicoContainer error
     */
    @Override
    protected void fillContainer(MutablePicoContainer container)
    {
        addComponent(InstallData.class, GUIInstallData.class);
        addComponent(RulesEngine.class, RulesEngineImpl.class);
        addComponent(VariableSubstitutor.class, VariableSubstitutorImpl.class);
        addComponent(MutablePicoContainer.class, container);
        addComponent(MergeableResolver.class);
        addComponent(Properties.class);
        addComponent(DefaultVariables.class);
        addComponent(ConditionContainer.class);
        addComponent(AbstractContainer.class, this);
        addComponent(Platform.class, Platforms.HP_UX);
    }
}

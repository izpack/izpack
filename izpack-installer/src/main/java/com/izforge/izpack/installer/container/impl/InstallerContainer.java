package com.izforge.izpack.installer.container.impl;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.container.PlatformProvider;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.factory.DefaultObjectFactory;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.core.rules.ConditionContainer;
import com.izforge.izpack.core.rules.ConditionContainerProvider;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.installer.base.InstallDataConfiguratorWithRules;
import com.izforge.izpack.installer.container.provider.LocalesProvider;
import com.izforge.izpack.installer.container.provider.RulesProvider;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.event.InstallerListeners;
import com.izforge.izpack.installer.event.ProgressNotifiersImpl;
import com.izforge.izpack.installer.requirement.*;
import com.izforge.izpack.installer.unpacker.FileQueueFactory;
import com.izforge.izpack.installer.unpacker.IUnpacker;
import com.izforge.izpack.installer.unpacker.UnpackerProvider;
import com.izforge.izpack.merge.MergeManagerImpl;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.merge.resolve.PathResolver;
import com.izforge.izpack.util.*;

import java.util.Properties;

/**
 * Installer container.
 */
public abstract class InstallerContainer extends AbstractContainer
{

    /**
     * Sets the locale.
     *
     * @param code the locale ISO language code
     * @throws IzPackException if the locale isn't supported
     */
    public void setLocale(String code)
    {
        Locales locales = getComponent(Locales.class);
        locales.setLocale(code);
    }

    /**
     * Invoked by {@link #initialise} to fill the container.
     *
     * @throws ContainerException if initialisation fails
     */
    @Override
    protected void fillContainer()
    {
        registerComponents();
        resolveComponents();
    }

    /**
     * Registers components with the container.
     *
     * @throws ContainerException if registration fails
     */
    protected void registerComponents()
    {
        addProvider(RulesEngine.class, RulesProvider.class);
        addProvider(Platform.class, PlatformProvider.class);
        addProvider(Locales.class, LocalesProvider.class);

        addComponent(InstallerContainer.class, this);
        addComponent(InstallDataConfiguratorWithRules.class);
        addComponent(InstallerRequirementChecker.class);
        addComponent(JavaVersionChecker.class);
        addComponent(JDKChecker.class);
        addComponent(LangPackChecker.class);
        addComponent(ExpiredChecker.class);
        addComponent(RequirementsChecker.class);
        addComponent(LockFileChecker.class);
        addComponent(MergeManagerImpl.class);
        addComponent(UninstallData.class);
        addProvider(ConditionContainer.class, ConditionContainerProvider.class);
        addComponent(Properties.class);
        addComponent(Variables.class, DefaultVariables.class);
        addComponent(Resources.class, ResourceManager.class);
        addComponent(UninstallDataWriter.class);
        addComponent(ProgressNotifiersImpl.class);
        addComponent(InstallerListeners.class);
        addComponent(CustomDataLoader.class);
        addComponent(Container.class, this);
        addComponent(RegistryDefaultHandler.class);
        addComponent(Housekeeper.class);
        addComponent(Librarian.class);
        addComponent(FileQueueFactory.class);
        addComponent(TargetFactory.class);
        addComponent(TargetPlatformFactory.class, DefaultTargetPlatformFactory.class);
        addComponent(ObjectFactory.class, DefaultObjectFactory.class);
//        addComponent(Resources.class, DefaultResources.class);
        addComponent(PathResolver.class);
        addComponent(MergeableResolver.class);
        addComponent(Platforms.class);
        addComponent(PlatformModelMatcher.class);

        addComponent(VariableSubstitutor.class, VariableSubstitutorImpl.class);
        addComponent(Variables.class, DefaultVariables.class);
    }

    /**
     * Resolve components.
     */
    protected void resolveComponents()
    {
        addProvider(IUnpacker.class, UnpackerProvider.class);
        CustomDataLoader customDataLoader = getComponent(CustomDataLoader.class);
        try
        {
            customDataLoader.loadCustomData();
        }
        catch (InstallerException exception)
        {
            throw new ContainerException(exception);
        }
    }

}

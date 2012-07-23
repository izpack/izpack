package com.izforge.izpack.installer.container.impl;

import java.util.Locale;
import java.util.Properties;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.injectors.ProviderAdapter;
import org.picocontainer.parameters.ComponentParameter;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.container.PlatformProvider;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.factory.DefaultObjectFactory;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.core.rules.ConditionContainer;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.installer.base.InstallDataConfiguratorWithRules;
import com.izforge.izpack.installer.container.provider.LocalesProvider;
import com.izforge.izpack.installer.container.provider.RulesProvider;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.event.InstallerListeners;
import com.izforge.izpack.installer.event.ProgressNotifiersImpl;
import com.izforge.izpack.installer.requirement.InstallerRequirementChecker;
import com.izforge.izpack.installer.requirement.JDKChecker;
import com.izforge.izpack.installer.requirement.JavaVersionChecker;
import com.izforge.izpack.installer.requirement.LangPackChecker;
import com.izforge.izpack.installer.requirement.LockFileChecker;
import com.izforge.izpack.installer.requirement.RequirementsChecker;
import com.izforge.izpack.installer.unpacker.FileQueueFactory;
import com.izforge.izpack.installer.unpacker.IUnpacker;
import com.izforge.izpack.merge.MergeManagerImpl;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.merge.resolve.PathResolver;
import com.izforge.izpack.util.DefaultTargetPlatformFactory;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.PlatformModelMatcher;
import com.izforge.izpack.util.Platforms;
import com.izforge.izpack.util.TargetFactory;

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
        Locale locale = locales.getLocale(code);
        if (locale != null)
        {
            locales.setLocale(locale);
        }
        else
        {
            throw new IzPackException("Unsupported locale:" + code);
        }
    }

    /**
     * Invoked by {@link #initialise} to fill the container.
     *
     * @param container the underlying container
     * @throws ContainerException if initialisation fails
     * @throws PicoException      for any PicoContainer error
     */
    @Override
    protected void fillContainer(MutablePicoContainer container)
    {
        registerComponents(container);
        resolveComponents(container);
    }

    /**
     * Registers components with the container.
     *
     * @param pico the container
     * @throws ContainerException if registration fails
     * @throws PicoException      for any PicoContainer error
     */
    protected void registerComponents(MutablePicoContainer pico)
    {
        pico.addAdapter(new ProviderAdapter(new RulesProvider()));
        pico.addAdapter(new ProviderAdapter(new PlatformProvider()));
        pico.addAdapter(new ProviderAdapter(new LocalesProvider()));

        addComponent(InstallDataConfiguratorWithRules.class);
        addComponent(InstallerRequirementChecker.class);
        addComponent(JavaVersionChecker.class);
        addComponent(JDKChecker.class);
        addComponent(LangPackChecker.class);
        addComponent(RequirementsChecker.class);
        addComponent(LockFileChecker.class);
        addComponent(MergeManagerImpl.class);
        addComponent(UninstallData.class);
        addComponent(MutablePicoContainer.class, pico);
        addComponent(ConditionContainer.class);
        addComponent(Properties.class);
        addComponent(DefaultVariables.class);
        addComponent(ResourceManager.class);
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
        addComponent(DefaultTargetPlatformFactory.class);
        addComponent(DefaultObjectFactory.class);
        addComponent(PathResolver.class);
        addComponent(MergeableResolver.class);
        addComponent(Platforms.class);
        addComponent(PlatformModelMatcher.class);

        pico.addComponent(VariableSubstitutor.class, VariableSubstitutorImpl.class,
                          new ComponentParameter(DefaultVariables.class));
    }

    /**
     * Resolve components.
     *
     * @param pico the container
     */
    protected void resolveComponents(MutablePicoContainer pico)
    {
        InstallData installData = pico.getComponent(InstallData.class);
        String className = installData.getInfo().getUnpackerClassName();
        Class<IUnpacker> unpackerClass = getClass(className, IUnpacker.class);
        pico.addComponent(IUnpacker.class, unpackerClass);

        CustomDataLoader customDataLoader = pico.getComponent(CustomDataLoader.class);
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

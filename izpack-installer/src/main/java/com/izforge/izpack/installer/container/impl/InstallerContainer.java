package com.izforge.izpack.installer.container.impl;

import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.container.CdiInitializationContext;

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
        getComponent(Locales.class).setLocale(code);
    }

    /**
     * Invoked by {@link #initialise} to fill the container.
     *
     * @throws ContainerException if initialization fails
     */
    @Override
    protected void fillContainer(CdiInitializationContext context)
    {
        super.fillContainer(context);
//        addComponent(RulesProvider.class);
//        addComponent(PlatformProvider.class);
//        addComponent(LocalesProvider.class);
//        addComponent(InstallDataConfiguratorWithRules.class);
//        addComponent(InstallerRequirementChecker.class);
//        addComponent(JavaVersionChecker.class);
//        addComponent(JDKChecker.class);
//        addComponent(LangPackChecker.class);
//        addComponent(ExpiredChecker.class);
//        addComponent(RequirementsChecker.class);
//        addComponent(LockFileChecker.class);
//        addComponent(MergeManagerImpl.class);
//        addComponent(UninstallData.class);
//        addComponent(ConditionContainer.class);
//        addComponent(Properties.class);
//        addComponent(DefaultVariables.class);
//        addComponent(ResourceManager.class);
//        addComponent(UninstallDataWriter.class);
//        addComponent(ProgressNotifiersImpl.class);
//        addComponent(InstallerListeners.class);
//        addComponent(CustomDataLoader.class);
//        addComponent(RegistryDefaultHandler.class);
//        addComponent(Housekeeper.class);
//        addComponent(Librarian.class);
//        addComponent(FileQueueFactory.class);
//        addComponent(TargetFactory.class);
//        addComponent(DefaultTargetPlatformFactory.class);
//        addComponent(DefaultObjectFactory.class);
//        addComponent(PathResolver.class);
//        addComponent(MergeableResolver.class);
//        addComponent(PlatformModelMatcher.class);
//        addComponent(VariableSubstitutorImpl.class);
        resolveComponents(context);
    }

    /**
     * Resolve components.
     */
    protected void resolveComponents(CdiInitializationContext context)
    {
//        addComponent(UnpackerProducer.class);
//        addComponent(CustomDataLoader.class);
    }
}

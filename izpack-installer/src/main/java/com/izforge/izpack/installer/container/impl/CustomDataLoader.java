/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2010 Anthonin Bonnefoy
 * Copyright 2012 Tim Anderson
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

package com.izforge.izpack.installer.container.impl;

import java.util.List;

import com.izforge.izpack.api.event.InstallerListener;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.data.CustomData;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.event.InstallerListeners;
import com.izforge.izpack.util.PlatformModelMatcher;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Reads the <em>customData</em> resource in order to populate the {@link InstallerListeners} and {@link UninstallData}.
 *
 * @author Anthonin Bonnefoy
 * @author Tim Anderson
 */
@ApplicationScoped
public class CustomDataLoader
{

    /**
     * The platform matcher.
     */
    private final PlatformModelMatcher matcher;

    /**
     * The resources.
     */
    private final Resources resources;

    /**
     * The object factory.
     */
    private final ObjectFactory factory;

    /**
     * The uninstallation data.
     */
    private final UninstallData uninstallData;

    /**
     * The installer listeners.
     */
    private final InstallerListeners listeners;


    /**
     * Constructs a {@code CustomDataLoader}.
     *
     * @param matcher       the platform matcher
     * @param resources     the resources
     * @param factory       the factory for listeners
     * @param uninstallData the uninstallation data
     * @param listeners     the installer listeners
     */
    @Inject
    public CustomDataLoader(PlatformModelMatcher matcher, Resources resources, ObjectFactory factory,
                            UninstallData uninstallData,
                            InstallerListeners listeners)
    {
        this.matcher = matcher;
        this.resources = resources;
        this.factory = factory;
        this.uninstallData = uninstallData;
        this.listeners = listeners;
    }

    /**
     * Loads custom data.
     * <p/>
     * This includes:
     * <ul>
     * <li>installer listeners</li>
     * <li>uninstaller listeners</li>
     * <li>uninstaller jars</li>
     * <li>uninstaller native libraries</li>
     * </ul>
     * @throws IzPackException if an {@link InstallerListener} throws an exception
     */
    @SuppressWarnings("unchecked")
    @PostConstruct
    public void loadCustomData()
    {
        List<CustomData> customData = (List<CustomData>) resources.getObject("customData");
        for (CustomData data : customData)
        {
            if (matcher.matchesCurrentPlatform(data.osConstraints))
            {
                switch (data.type)
                {
                    case CustomData.INSTALLER_LISTENER:
                        addInstallerListener(data.listenerName);
                        break;
                    case CustomData.UNINSTALLER_LISTENER:
                        uninstallData.addUninstallerListener(data);
                        break;
                    case CustomData.UNINSTALLER_JAR:
                        uninstallData.addJar(data);
                        break;
                    case CustomData.UNINSTALLER_LIB:
                        uninstallData.addNativeLibrary(data.contents.get(0));
                        break;
                }
            }
        }
        listeners.initialise();
    }

    /**
     * Adds an installer listener.
     *
     * @param className the listener class name
     */
    private void addInstallerListener(String className)
    {
        InstallerListener listener = factory.create(className, InstallerListener.class);
        listeners.add(listener);
    }

}

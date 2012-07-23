/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
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
package com.izforge.izpack.util;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.binding.OsModel;

/**
 * Helper to match {@link Platform Platforms}s to {@link OsModel OsModels}.
 *
 * @author Tim Anderson
 */
public class PlatformModelMatcher
{

    /**
     * The platform factory.
     */
    private final Platforms platforms;

    /**
     * The current platform.
     */
    private final Platform platform;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(PlatformModelMatcher.class.getName());

    /**
     * The platform factory.
     *
     * @param platforms the platforms
     * @param platform  the current platform
     */
    public PlatformModelMatcher(Platforms platforms, Platform platform)
    {
        this.platforms = platforms;
        this.platform = platform;
    }

    /**
     * Returns the current platform.
     *
     * @return the current platform
     */
    public Platform getCurrentPlatform()
    {
        return platform;
    }

    /**
     * Determines if the OS model matches the current platform.
     *
     * @param model the OS model
     * @return {@code true} if they match
     */
    public boolean matchesCurrentPlatform(OsModel model)
    {
        return match(platform, model);
    }

    /**
     * Determines if the OS model matches the specified platform.
     *
     * @param platform the platform
     * @param model    the OS model
     * @return {@code true} if they match
     */
    public boolean match(Platform platform, OsModel model)
    {
        boolean match = true;

        if (model.getArch() != null && model.getArch().length() != 0)
        {
            Platform.Arch arch = platforms.getArch(model.getArch());
            match = arch.equals(platform.getArch());
        }

        if (match && (model.getVersion() != null) && (model.getVersion().length() != 0))
        {
            match = platform.getVersion() != null && platform.getVersion().equals(model.getVersion());
        }

        if (match && (model.getName() != null) && (model.getName().length() != 0))
        {
            Platform.Name name = platforms.getName(model.getName());
            match = name.equals(platform.getName());
        }

        if (match && (model.getFamily() != null))
        {
            Platform.Name family = platforms.getName(model.getFamily());
            match = platform.getName().isA(family);
        }

        if (match && (model.getJre() != null) && (model.getJre().length() > 0))
        {
            match = platform.getJavaVersion() != null && platform.getJavaVersion().startsWith(model.getJre());
        }

        return match && ((model.getFamily() != null) || (model.getName() != null) || (model.getVersion() != null)
                || (model.getArch() != null) || (model.getJre() != null));
    }

    /**
     * Determines if the current platform is in the specified list of models.
     *
     * @param models the models
     * @return {@code true} if the current platform is in the list of models, or if the models are {@code null} or
     *         empty
     */
    public boolean matchesCurrentPlatform(List<OsModel> models)
    {
        return matches(platform, models);
    }

    /**
     * Determines if a platform is in the specified list of models.
     *
     * @param platform the platform
     * @param models   the models
     * @return {@code true} if the platform is in the list of models, or if the models are {@code null} or empty
     */
    public boolean matches(Platform platform, List<OsModel> models)
    {
        if (models == null || models.isEmpty())
        {
            return true;
        }
        boolean log = logger.isLoggable(Level.FINE);
        for (OsModel model : models)
        {
            if (log)
            {
                logger.fine("Checking if OS constraints " + model + " match platform=" + platform);
            }
            if (match(platform, model))
            {
                if (log)
                {
                    logger.fine("OS constraints matched platform");
                }
                return true;
            }
        }
        if (log)
        {
            logger.fine("OS constraints do not match platform=" + platform);
        }
        return false;
    }


}

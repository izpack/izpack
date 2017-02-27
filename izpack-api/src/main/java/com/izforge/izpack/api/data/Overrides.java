/*
 * Copyright 2016 Julien Ponge, René Krell and the IzPack team.
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
package com.izforge.izpack.api.data;

import java.io.File;
import java.io.IOException;

/*
 * Container for holding installer variable overrides and reading them from a file.
 */
public interface Overrides
{

    /**
     * Get override value for variable name and resolve variable references.
     *
     * @param name the Izpack variable name
     * @return the override value for the variable, if it exists
     */
    String fetch(String name);

    /**
     * Get override value for variable name and resolve variable references.
     * This method accepts a fallback value in case the override hasn't been
     * defined.
     *
     * @param name the Izpack variable name
     * @param defaultValue
     * @return the override value for the variable, if it exists, or the given
     * default value
     */
    String fetch(String name, String defaultValue);

    /**
     * Returns whether an override for a given variable name exists in this
     * container.
     *
     * @param name the Izpack variable name
     * @return true if an override exists for the given name
     */
    boolean containsKey(String name);

    /**
     * Removes the override for a given variable name from the container.
     *
     * @param name the Izpack variable name
     * @return the override value of the removed entry
     */
    String remove(String name);

    /**
     * Returns the overall number of overrides for variable values in this
     * container.
     *
     * @return the number of overrides in the current container
     */
    int size();

    /**
     * Initialize the installer runtime data.
     *
     * @param installData the installer runtime data
     */
    void setInstallData(InstallData installData);

    /**
     * Returns a reference to the overrides file used to initialize this
     * container.
     *
     * @return the overrides file
     */
    File getFile();

    /**
     * Loads the overrides from the file system. When using unresolved variables
     * in defaults file include definitions call
     * {@link #setInstallData(InstallData)} first.
     *
     * @throws IOException if an I/O error occurs during loading
     */
    void load() throws IOException;
}

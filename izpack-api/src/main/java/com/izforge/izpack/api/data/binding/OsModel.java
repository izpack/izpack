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

package com.izforge.izpack.api.data.binding;

import java.io.Serializable;

public class OsModel implements Serializable
{
    /**
     * OS architecture from java system properties
     */
    public String arch;
    /**
     * The OS family
     */
    public String family;
    /**
     * JRE version used for installation
     */
    public String jre;
    /**
     * OS name from java system properties
     */
    public String name;
    /**
     * OS version from java system properties
     */
    public String version;

    public OsModel(String arch, String family, String jre, String name, String version)
    {
        this.arch = arch;
        this.family = family;
        this.jre = jre;
        this.name = name;
        this.version = version;
    }

    public String getArch()
    {
        return arch;
    }

    public String getFamily()
    {
        return family;
    }

    public String getJre()
    {
        return jre;
    }

    public String getName()
    {
        return name;
    }

    public String getVersion()
    {
        return version;
    }

    @Override
    public String toString()
    {
        return "OsModel{" +
                "arch='" + arch + '\'' +
                ", family='" + family + '\'' +
                ", jre='" + jre + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
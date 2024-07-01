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

package com.izforge.izpack.compiler.data;

import com.izforge.izpack.api.data.Info;
import jakarta.enterprise.inject.Vetoed;

import java.io.File;
import java.util.ResourceBundle;

/**
 * Data for compiler
 */
@Vetoed
public class CompilerData
{
    /**
     * The IzPack home directory.
     */
    public static String IZPACK_HOME = ".";

    /**
     * The compiler version.
     */
    public final static String VERSION = "5.0";

    /**
     * Standard installer.
     */
    public final static String STANDARD = "standard";
    /**
     * Web installer.
     */
    public final static String WEB = "web";


    private String packCompression = null;

    /**
     * The installer kind.
     */
    private String kind = STANDARD;

    /**
     * The xml install file
     */
    private String installFile;

    /**
     * The xml install configuration text
     */
    private String installText;

    /**
     * The base directory.
     */
    private String basedir;

    /**
     * The output jar filename.
     */
    private String output;

    /**
     * Whether to recursively create parent directories of output
     */
    private boolean mkdirs = false;

    /**
     * PackCompression level
     */
    private int comprLevel = -1;

    /**
     * External Information
     */
    private Info externalInfo = new Info();

    /**
     * The IzPack version.
     */
    public final static String IZPACK_VERSION = ResourceBundle.getBundle("version").getString("izpack.version");

    /**
     * The IzPack copyright year range.
     */
    public final static String IZPACK_COPYYEARS = ResourceBundle.getBundle("version").getString("izpack.copyyears");

    private final static String IZ_TEST_FILE = "ShellLink.dll";

    private final static String IZ_TEST_SUBDIR = "bin" + File.separator + "native" + File.separator + "izpack";

    private CompilerData()
    {
        // We get the IzPack home directory
        String izHome = System.getProperty("izpack.home");
        if (izHome != null)
        {
            IZPACK_HOME = izHome;
        }
        else
        {
            izHome = System.getenv("IZPACK_HOME");
            if (izHome != null)
            {
                IZPACK_HOME = izHome;
            }
        }
    }

    public CompilerData(String packCompression, String installFile, String basedir, String output, boolean mkdirs)
    {
        this();
        this.packCompression = packCompression;
        this.installFile = installFile;
        this.basedir = basedir;
        this.output = output;
        this.mkdirs = mkdirs;
    }

    public CompilerData(String installFile, String basedir, String output, boolean mkdirs)
    {
        this(null, installFile, basedir, output, mkdirs);
    }

    public CompilerData(String packCompression, String kind, String installFile, String installText, String basedir,
                        String output, boolean mkdirs, int comprLevel)
    {
        this(packCompression, installFile, basedir, output, mkdirs);
        this.kind = kind;
        this.installText = installText;
        this.comprLevel = comprLevel;
    }

    public CompilerData(String packCompression, String kind, String installFile, String installText, String basedir,
                        String output, boolean mkdirs, int comprLevel, Info externalInfo)
    {
        this(packCompression, kind, installFile, installText, basedir, output, mkdirs, comprLevel);
        this.externalInfo = externalInfo;
    }

    /**
     * Set the IzPack home directory
     *
     * @param izHome - the izpack home directory
     */
    public static void setIzpackHome(String izHome)
    {
        IZPACK_HOME = izHome;
    }

    /**
     * Access the installation kind.
     *
     * @return the installation kind.
     */
    public String getKind()
    {
        return kind;
    }

    public void setKind(String kind)
    {
        this.kind = kind;
    }

    public String getInstallFile()
    {
        return installFile;
    }

    public String getInstallText()
    {
        return installText;
    }

    public String getBasedir()
    {
        return basedir;
    }

    public void setBasedir(String basedir)
    {
        this.basedir = basedir;
    }

    public String getOutput()
    {
        return output;
    }

    public boolean isMkdirs()
    {
        return mkdirs;
    }

    public void setMkdirs(boolean mkdirs)
    {
        this.mkdirs = mkdirs;
    }

    public String getComprFormat()
    {
        return packCompression;
    }

    public void setComprFormat(String packCompression)
    {
        this.packCompression = packCompression;
    }

    public int getComprLevel()
    {
        return comprLevel;
    }

    public void setComprLevel(int comprLevel)
    {
        this.comprLevel = comprLevel;
    }

    public Info getExternalInfo()
    {
        return this.externalInfo;
    }

}

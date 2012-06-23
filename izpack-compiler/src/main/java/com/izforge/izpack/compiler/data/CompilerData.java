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

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.util.FileUtil;

/**
 * Data for compiler
 */
public class CompilerData
{

    /**
     * The IzPack home directory.
     */
    public static String IZPACK_HOME = ".";
    /**
     * The compiler version.
     */
    public final static String VERSION = "1.0";

    /**
     * Standard installer.
     */
    public final static String STANDARD = "standard";
    /**
     * Web installer.
     */
    public final static String WEB = "web";


    private String comprFormat = "default";

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
    protected String basedir;

    /**
     * The output jar filename.
     */
    private String output;

    /**
     * Whether to recursively create parent directories of output
     */
    private boolean mkdirs = false;

    /**
     * Compression level
     */
    private int comprLevel = -1;

    /**
     * External Information
     */
    Info externalInfo = new Info();

    /**
     * The IzPack version.
     */
    public final static String IZPACK_VERSION = ResourceBundle.getBundle("version").getString("izpack.version");

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

    public CompilerData(String installFile, String basedir, String output, boolean mkdirs)
    {
        this();
        this.installFile = installFile;
        this.basedir = basedir;
        this.output = output;
        this.mkdirs = mkdirs;
    }

    public CompilerData(String comprFormat, String kind, String installFile, String installText, String basedir, String output, boolean mkdirs, int comprLevel)
    {
        this(installFile, basedir, output, mkdirs);
        this.comprFormat = comprFormat;
        this.kind = kind;
        this.installText = installText;
        this.comprLevel = comprLevel;
    }

    public CompilerData(String comprFormat, String kind, String installFile, String installText, String basedir, String output, boolean mkdirs, int comprLevel, Info externalInfo)
    {
        this(comprFormat, kind, installFile, installText, basedir, output, mkdirs, comprLevel);
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
        return comprFormat;
    }

    public void setComprFormat(String comprFormat)
    {
        this.comprFormat = comprFormat;
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

    /**
     * Try to resolve IzPack home from IZPACK_HOME value
     */
    public void resolveIzpackHome()
    {
        IZPACK_HOME = resolveIzPackHome(IZPACK_HOME);
    }

    private static String resolveIzPackHome(String home)
    {
        File test = new File(home, IZ_TEST_SUBDIR + File.separator + IZ_TEST_FILE);
        if (test.exists())
        {
            return (home);
        }
        // Try to resolve the path using compiler.jar which also should be under
        // IZPACK_HOME.
        String self = CompilerData.class.getName();
        self = self.replace('.', '/');
        self = "/" + self + ".class";
        URL url = Compiler.class.getResource(self);
        String name = FileUtil.convertUrlToFilePath(url);
        int start = name.indexOf(self);
        name = name.substring(0, start);
        if (name.endsWith("!"))
        { // Where shut IZPACK_HOME at the standalone-compiler be??
            // No idea.
            if (name.endsWith("standalone-compiler.jar!")
                    || name.endsWith("standalone-compiler-4.0.0.jar!")
                    || name.matches("standalone-compiler-[\\d\\.]+.jar!"))
            {
                return (".");
            }
            name = name.substring(0, name.length() - 1);
        }
        File root;
        if (URI.create(name).isAbsolute())
        {
            root = new File(URI.create(name));
        }
        else
        {
            root = new File(name);
        }
        while (true)
        {
            if (root == null)
            {
                throw new IllegalArgumentException(
                        "No valid IzPack home directory found");
            }
            test = new File(root, IZ_TEST_SUBDIR + File.separator + IZ_TEST_FILE);
            if (test.exists())
            {
                return (root.getAbsolutePath());
            }
            root = root.getParentFile();
        }
    }
}

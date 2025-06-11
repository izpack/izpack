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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

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
    public static final String VERSION = "5.0";

    /**
     * Standard installer.
     */
    public static final String STANDARD = "standard";
    /**
     * Web installer.
     */
    public static final String WEB = "web";


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
     * A list of key/value pairs to add to the manifest.
     */
    private Map<String, String> manifestEntries;

    private static final String VERSION_BUNDLE = "version";

    /**
     * The IzPack version.
     */
    public static final String IZPACK_VERSION = ResourceBundle.getBundle(VERSION_BUNDLE).getString("izpack.version");

    /**
     * The IzPack build number.
     */
    public static final String IZPACK_BUILD = ResourceBundle.getBundle(VERSION_BUNDLE).getString("izpack.build");

    /**
     * The IzPack copyright year range.
     */
    public static final String IZPACK_COPYYEARS = ResourceBundle.getBundle(VERSION_BUNDLE).getString("izpack.copyyears");

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
                        String output, boolean mkdirs, int comprLevel, Info externalInfo, Map<String, String> manifestEntries)
    {
        this(packCompression, kind, installFile, installText, basedir, output, mkdirs, comprLevel);
        this.externalInfo = externalInfo;
        this.manifestEntries = manifestEntries;
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

    public Map<String, String> getManifestEntries() {
        return manifestEntries;
    }

    public String getTempManifestFileWithAdditionalEntries(InputStream inputStream) throws IOException {
        Manifest manifest = new Manifest(inputStream);
        final Map<String, String> manifestEntries = getManifestEntries();
        final Attributes mainAttributes = manifest.getMainAttributes();
        if (manifestEntries != null)
        {
            for (String key : manifestEntries.keySet())
            {
                mainAttributes.putIfAbsent(new Attributes.Name(key), manifestEntries.get(key));
            }
        }
        Path tempManifest = Files.createTempFile("MANIFEST", ".MF");
        tempManifest.toFile().deleteOnExit();
        try (OutputStream manifestOutputStream = Files.newOutputStream(tempManifest))
        {
            manifest.write(manifestOutputStream);
        }
        return tempManifest.toAbsolutePath().toString();
    }
}

/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Vladimir Ralev
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

package com.izforge.izpack.installer.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * This class enumerates the availabe packs at the web repository. Parses the config files
 * - install.xml, packsinfo.xml, langpacks and is used to override the static configuration
 * in the installer jar.
 *
 * @author <a href="vralev@redhat.com">Vladimir Ralev</a>
 * @version $Revision: 1.1 $
 */
public class WebRepositoryAccessor
{
    /**
     * Files to be looked for at the repository base url
     */
    private static final String installFilename = "install.xml";

    /**
     * Files being downloaded in the buffer, 1MB max
     */
    private static final int BUFFER_SIZE = 1000000;

    /**
     * First download the jar file. The create the input stream from the
     * downloaded file. This is because the Jar connection's openInputStream
     * will blocks until the whole jar in order to unzip it (there is no way
     * to see the download progress there).
     *
     * @param url the base URL
     * @return the url
     */
    public static String getCachedUrl(String url, String tempFolder) throws IOException
    {
        byte[] raw = new byte[BUFFER_SIZE];
        WebAccessor webAccessor = new WebAccessor(null);
        URL escapedUrl;
        try
        {
            escapedUrl = new URI(null, url, null).toURL();
        }
        catch (URISyntaxException e)
        {
            throw new IOException(e);
        }
        InputStream in = webAccessor.openInputStream(escapedUrl);
        int r = in.read(raw);
        File tempDir = new File(tempFolder);

        tempDir.mkdirs();

        File temp = File.createTempFile("izpacktempfile", "jar", new File(tempFolder));
        FileOutputStream fos = new FileOutputStream(temp);
        String path = "file:///" + temp.getAbsolutePath();
        while (r > 0)
        {
            fos.write(raw, 0, r);
            r = in.read(raw);
        }
        in.close();
        fos.close();

        return path;
    }
}

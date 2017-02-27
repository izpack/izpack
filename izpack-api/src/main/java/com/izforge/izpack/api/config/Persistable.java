/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005,2009 Ivan SZKIBA
 * Copyright 2010,2011 Rene Krell
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
package com.izforge.izpack.api.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

public interface Persistable
{

    File getFile();

    void setFile(File value);

    void load() throws IOException, InvalidFileFormatException;

    void load(InputStream input) throws IOException, InvalidFileFormatException;

    void load(Reader input) throws IOException, InvalidFileFormatException;

    void load(File input) throws IOException, InvalidFileFormatException;

    void load(URL input) throws IOException, InvalidFileFormatException;

    void store() throws IOException;

    void store(OutputStream output) throws IOException;

    void store(Writer output) throws IOException;

    void store(File output) throws IOException;
}

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

package com.izforge.izpack.core.variable;

import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

import com.izforge.izpack.api.substitutor.VariableSubstitutor;


public class JarEntryConfigValue extends ZipEntryConfigFileValue
{

    public JarEntryConfigValue(String filename, String entryname, int type, String section, String key, boolean escape)
    {
        super(filename, entryname, type, section, key, escape);
    }

    @Override
    public String resolve() throws Exception
    {
        return super.resolve(getJarEntryInputStream(getFilename(), getEntryname()));
    }

    @Override
    public String resolve(VariableSubstitutor... substitutors)
            throws Exception
    {
        String _filename_ = getFilename(), _entryname_ = getEntryname();
        for (VariableSubstitutor substitutor : substitutors)
        {
            _filename_ = substitutor.substitute(_filename_);
        }
        for (VariableSubstitutor substitutor : substitutors)
        {
            _entryname_ = substitutor.substitute(_entryname_);
        }
        return super.resolve(getJarEntryInputStream(_filename_, _entryname_), substitutors);
    }

    private InputStream getJarEntryInputStream(String filename, String entryname) throws Exception
    {
        JarFile jarfile;
        try
        {
            jarfile = new JarFile(filename);
            JarEntry entry = jarfile.getJarEntry(entryname);
            if (entry == null)
            {
                throw new Exception("Jar file entry " + entryname + " not found in " + jarfile.getName());
            }
            return jarfile.getInputStream(entry);
        }
        catch (ZipException ze)
        {
            throw new Exception("Error opening jar file " + filename, ze);
        }
    }

}

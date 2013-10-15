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
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.izforge.izpack.api.substitutor.VariableSubstitutor;


public class ZipEntryConfigFileValue extends ConfigFileValue
{

    private String filename;
    private String entryname;

    public ZipEntryConfigFileValue(String filename, String entryname, int type, String section, String key, boolean escape)
    {
        super(type, section, key, escape);
        this.filename = filename;
        this.entryname = entryname;
    }

    public String getFilename()
    {
        return filename;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    public String getEntryname()
    {
        return entryname;
    }

    public void setEntryname(String entryname)
    {
        this.entryname = entryname;
    }

    @Override
    public void validate() throws Exception
    {
        super.validate();
        if (this.filename == null || this.filename.length() <= 0)
        {
            throw new Exception("No or empty file path given to read entry from");
        }
        if (this.entryname == null || this.entryname.length() <= 0)
        {
            throw new Exception("No or empty file entry given to read entry from");
        }
    }

    @Override
    public String resolve() throws Exception
    {
        return super.resolve(getZipEntryInputStream(getFilename(), getEntryname()));
    }

    @Override
    public String resolve(VariableSubstitutor... substitutors)
            throws Exception
    {
        String _filename_ = this.filename, _entryname_ = this.entryname;
        for (VariableSubstitutor substitutor : substitutors)
        {
            _filename_ = substitutor.substitute(_filename_);
        }
        for (VariableSubstitutor substitutor : substitutors)
        {
            _entryname_ = substitutor.substitute(_entryname_);
        }
        return super.resolve(getZipEntryInputStream(_filename_, _entryname_), substitutors);
    }

    private InputStream getZipEntryInputStream(String filename, String entryname) throws Exception
    {
        ZipFile zipfile;
        try
        {
            zipfile = new ZipFile(filename);
            ZipEntry entry = zipfile.getEntry(entryname);
            if (entry == null)
            {
                throw new Exception("Zip file entry " + entryname + " not found in " + zipfile.getName());
            }
            return zipfile.getInputStream(entry);
        }
        catch (ZipException ze)
        {
            throw new Exception("Error opening zip file " + filename, ze);
        }
    }
}

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

package com.izforge.izpack.compiler.helper;

import java.util.List;
import java.util.Map;

import com.izforge.izpack.api.data.Blockable;
import com.izforge.izpack.api.data.OverrideType;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.util.file.types.FileSet;


public class TargetFileSet extends FileSet
{
    private String targetDir;
    List<OsModel> osList;
    OverrideType override;
    String overrideRenameTo;
    Blockable blockable;
    Map additionals;
    String condition;

    public String getTargetDir()
    {
        return targetDir;
    }

    public void setTargetDir(String targetDir)
    {
        this.targetDir = targetDir;
    }

    public List<OsModel> getOsList()
    {
        return osList;
    }

    public void setOsList(List<OsModel> osList)
    {
        this.osList = osList;
    }

    public OverrideType getOverride()
    {
        return override;
    }

    public void setOverride(OverrideType override)
    {
        this.override = override;
    }

    public String getOverrideRenameTo()
    {
        return overrideRenameTo;
    }

    public void setOverrideRenameTo(String overrideRenameTo)
    {
        this.overrideRenameTo = overrideRenameTo;
    }

    public Blockable getBlockable()
    {
        return blockable;
    }

    public void setBlockable(Blockable blockable)
    {
        this.blockable = blockable;
    }

    public Map getAdditionals()
    {
        return additionals;
    }

    public void setAdditionals(Map additionals)
    {
        this.additionals = additionals;
    }

    public String getCondition()
    {
        return condition;
    }

    public void setCondition(String condition)
    {
        this.condition = condition;
    }
}

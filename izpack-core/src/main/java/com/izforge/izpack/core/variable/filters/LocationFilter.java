package com.izforge.izpack.core.variable.filters;

import org.apache.commons.io.FilenameUtils;

import com.izforge.izpack.api.data.ValueFilter;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;

public class LocationFilter implements ValueFilter
{

    private static final long serialVersionUID = 5557014780732715339L;

    public String baseDir;

    public LocationFilter(String baseDir)
    {
        this.baseDir = baseDir;
    }

    public String getBaseDir()
    {
        return this.baseDir;
    }

    public void setBaseDir(String baseDir)
    {
        this.baseDir = baseDir;
    }

    @Override
    public void validate() throws Exception
    {
        // Nothing to be checked
    }

    @Override
    public String filter(String value, VariableSubstitutor... substitutors) throws Exception
    {
        String baseDir0 = baseDir;
        for (VariableSubstitutor substitutor : substitutors)
        {
            baseDir0 = substitutor.substitute(baseDir0);
        }

        return FilenameUtils.concat(baseDir0, value);
    }

    @Override
    public String toString()
    {
        return "(location: " + baseDir + ")";
    }

    @Override
    public boolean equals(Object obj)
    {
        if ((obj == null) || !(obj instanceof LocationFilter))
        {
            return false;
        }
        return baseDir.equals(((LocationFilter) obj).getBaseDir());
    }
}

package com.izforge.izpack.core.variable.filters;

import com.izforge.izpack.api.data.ValueFilter;
import com.izforge.izpack.api.regex.RegularExpressionProcessor;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.regex.RegularExpressionProcessorImpl;

public class RegularExpressionFilter implements ValueFilter
{

    private static final long serialVersionUID = -6817518878070930751L;

    public String regexp;
    public String select, replace;
    public String defaultValue;
    public Boolean casesensitive;
    public Boolean global;

    public RegularExpressionFilter(String regexp, String select, String replace, String defaultValue,
                                   Boolean casesensitive, Boolean global)
    {
        this.regexp = regexp;
        this.select = select;
        this.replace = replace;
        this.defaultValue = defaultValue;
        this.casesensitive = casesensitive;
        this.global = global;
    }

    public RegularExpressionFilter(String regexp, String select, String defaultValue,
                                   Boolean casesensitive)
    {
        this(regexp, select, null, defaultValue, casesensitive, null);
    }

    public RegularExpressionFilter(String regexp, String replace, String defaultValue,
                                   Boolean casesensitive, Boolean global)
    {
        this(regexp, null, replace, defaultValue, casesensitive, global);
    }

    @Override
    public void validate() throws Exception
    {
        if (this.regexp == null || this.regexp.length() <= 0)
        {
            throw new Exception("No or empty regular expression defined");
        }
        if (this.select == null && this.replace == null)
        {
            throw new Exception("Exactly one of both select or replace expression required");
        }
        if (this.select != null && this.replace != null)
        {
            throw new Exception("Expected only one of both select or replace expression");
        }
    }

    public String getRegexp()
    {
        return regexp;
    }

    public void setRegexp(String regexp)
    {
        this.regexp = regexp;
    }

    public String getSelect()
    {
        return select;
    }

    public void setSelect(String select)
    {
        this.select = select;
    }

    public String getReplace()
    {
        return replace;
    }

    public void setReplace(String replace)
    {
        this.replace = replace;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public Boolean getCasesensitive()
    {
        return casesensitive;
    }

    public void setCasesensitive(Boolean casesensitive)
    {
        this.casesensitive = casesensitive;
    }

    public Boolean getGlobal()
    {
        return global;
    }

    public void setGlobal(Boolean global)
    {
        this.global = global;
    }

    @Override
    public String filter(String value, VariableSubstitutor... substitutors) throws Exception
    {
        String _replace = replace, _select = select,
                _regexp = regexp, _defaultValue = defaultValue;
        for (VariableSubstitutor substitutor : substitutors)
        {
            if (_replace != null)
            {
                _replace = substitutor.substitute(_replace);
            }
            if (_select != null)
            {
                _select = substitutor.substitute(_select);
            }
            if (_regexp != null)
            {
                _regexp = substitutor.substitute(_regexp);
            }
            if (_defaultValue != null)
            {
                _defaultValue = substitutor.substitute(_defaultValue);
            }
        }
        RegularExpressionProcessor processor = new RegularExpressionProcessorImpl();
        processor.setInput(value);
        processor.setRegexp(_regexp);
        processor.setCaseSensitive(casesensitive);
        if (_select != null)
        {
            processor.setSelect(_select);
        }
        else if (_replace != null)
        {
            processor.setReplace(_replace);
            processor.setGlobal(global);
        }

        processor.setDefaultValue(_defaultValue);
        return processor.execute();
    }

}

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
        String replace0 = replace, select0 = select,
                regexp0 = regexp, defaultValue0 = defaultValue;
        for (VariableSubstitutor substitutor : substitutors)
        {
            if (replace0 != null)
            {
                replace0 = substitutor.substitute(replace0);
            }
            if (select0 != null)
            {
                select0 = substitutor.substitute(select0);
            }
            if (regexp0 != null)
            {
                regexp0 = substitutor.substitute(regexp0);
            }
            if (defaultValue0 != null)
            {
                defaultValue0 = substitutor.substitute(defaultValue0);
            }
        }
        RegularExpressionProcessor processor = new RegularExpressionProcessorImpl();
        processor.setInput(value);
        processor.setRegexp(regexp0);
        processor.setCaseSensitive(casesensitive);
        if (select0 != null)
        {
            processor.setSelect(select0);
        }
        else if (replace0 != null)
        {
            processor.setReplace(replace0);
            processor.setGlobal(global);
        }

        processor.setDefaultValue(defaultValue0);
        return processor.execute();
    }

    @Override
    public String toString()
    {
        return "(regexp: " + regexp
                + ", replace: " + replace
                + ", select: " + select
                + ", default: " + defaultValue
                + ", casesensitive: " + casesensitive
                + ", global: " + global + ")";
    }

    @Override
    public boolean equals(Object obj)
    {
        if ((obj == null) || !(obj instanceof RegularExpressionFilter))
        {
            return false;
        }
        String compareRegexp = ((RegularExpressionFilter) obj).getRegexp();
        String compareReplace = ((RegularExpressionFilter) obj).getReplace();
        String compareSelect = ((RegularExpressionFilter) obj).getSelect();
        String compareDefaultValue = ((RegularExpressionFilter) obj).getDefaultValue();
        return regexp != null ? regexp.equals(compareRegexp) : compareRegexp == null
                && replace != null ? replace.equals(compareReplace) : compareReplace == null
                        && select != null ? select.equals(compareSelect) : compareSelect == null
                                && defaultValue != null ? defaultValue.equals(compareDefaultValue) : compareDefaultValue == null
                                        && Boolean.valueOf(casesensitive).equals(((RegularExpressionFilter) obj).getCasesensitive())
                                        && Boolean.valueOf(global).equals(((RegularExpressionFilter) obj).getGlobal());
    }
}

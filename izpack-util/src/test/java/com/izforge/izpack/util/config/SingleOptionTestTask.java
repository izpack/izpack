package com.izforge.izpack.util.config;

import com.izforge.izpack.api.config.Options;

public class SingleOptionTestTask extends SingleConfigurableTask
{

    private Options fromOptions, toOptions;

    public SingleOptionTestTask(Options fromOptions, Options toOptions)
    {
        this.fromOptions = fromOptions;
        this.toOptions = toOptions;
    }

    @Override
    protected void readSourceConfigurable() throws Exception
    {
        fromConfigurable = fromOptions;
    }

    @Override
    protected void readConfigurable() throws Exception
    {
        configurable = toOptions;
    }

    @Override
    protected void writeConfigurable() throws Exception
    {
    }

    public Options getResult()
    {
        return toOptions;
    }

    @Override
    protected void checkAttributes() throws Exception
    {
    }

}

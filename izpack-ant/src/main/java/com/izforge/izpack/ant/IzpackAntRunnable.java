package com.izforge.izpack.ant;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.tools.ant.BuildException;

import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.data.PropertyManager;

/**
 * @author Anthonin Bonnefoy
 */
public class IzpackAntRunnable implements Runnable
{
    private final CompilerData compilerData;
    private final String input;
    private final Properties properties;
    private final Boolean inheritAll;
    private Hashtable projectProps;

    public IzpackAntRunnable(String compression, String kind, String input, String configText, String basedir,
                             String output, boolean mkdirs, int compressionLevel, Properties properties,
                             Boolean inheritAll, Hashtable antProjectProperties, String izPackDir)
    {
        this.compilerData = new CompilerData(compression, kind, input, configText, basedir, output, mkdirs,
                                             compressionLevel);
        this.input = input;
        this.properties = properties;
        this.inheritAll = inheritAll;
        this.projectProps = antProjectProperties;
        CompilerData.setIzpackHome(izPackDir);
    }


    @Override
    public void run()
    {
        CompilerContainer compilerContainer = new CompilerContainer();
        compilerContainer.addConfig("installFile", input);
        compilerContainer.addComponent(CompilerData.class, compilerData);

        CompilerConfig compilerConfig = compilerContainer.getComponent(CompilerConfig.class);
        PropertyManager propertyManager = compilerContainer.getComponent(PropertyManager.class);

        if (properties != null)
        {
            Enumeration<?> e = properties.keys();
            while (e.hasMoreElements())
            {
                String name = (String) e.nextElement();
                String value = properties.getProperty(name);
                value = fixPathString(value);
                propertyManager.addProperty(name, value);
            }
        }

        if (inheritAll)
        {
            Enumeration<?> e = projectProps.keys();
            while (e.hasMoreElements())
            {
                String name = (String) e.nextElement();
                String value = (String) projectProps.get(name);
                value = fixPathString(value);
                propertyManager.addProperty(name, value);
            }
        }

        try
        {
            compilerConfig.executeCompiler();
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }

    }

    private static String fixPathString(String path)
    {
        /*
        * The following code fixes a bug in in codehaus classworlds loader,
        * which can't handle mixed path strings like "c:\test\../lib/mylib.jar".
        * The bug is in org.codehaus.classworlds.UrlUtils.normalizeUrlPath().
        */
        StringBuffer fixpath = new StringBuffer(path);
        for (int q = 0; q < fixpath.length(); q++)
        {
            if (fixpath.charAt(q) == '\\')
            {
                fixpath.setCharAt(q, '/');
            }
        }
        return fixpath.toString();
    }

}
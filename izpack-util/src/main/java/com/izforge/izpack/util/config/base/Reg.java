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
package com.izforge.izpack.util.config.base;

import java.io.*;
import java.net.URL;
import java.util.logging.Logger;

import com.izforge.izpack.util.config.base.spi.IniFormatter;
import com.izforge.izpack.util.config.base.spi.IniHandler;
import com.izforge.izpack.util.config.base.spi.RegBuilder;
import com.izforge.izpack.util.config.base.spi.RegParser;


/**
 * A {@link MultiMap} representation of Windows registry keys, data, and values. Handles input
 * directly from the registry, or from a file containing INI-like Registry Editor data. 
 * Can also be used to create new keys and stores changes to the registry or a file. 
 *
 * @author Ivan SZKIBA
 */
public class Reg extends BasicRegistry implements Registry, Persistable, Configurable
{
	private static final long serialVersionUID = -5050673095845660030L;
    private static final Logger logger = Logger.getLogger(Reg.class.getName());
	protected static final String DEFAULT_SUFFIX = ".reg";
    protected static final String TMP_PREFIX = "reg-";
    private static final int STDERR_BUFF_SIZE = 8192;
    private static final String PROP_OS_NAME = "os.name";
    private static final boolean WINDOWS = Config.getSystemProperty(PROP_OS_NAME, "Unknown").startsWith("Windows");
    private static final char CR = '\r';
    private static final char LF = '\n';
    private Config _config;
    private File _file;

    public Reg()
    {
        Config cfg = Config.getGlobal().clone();

        cfg.setEscape(false);
        cfg.setGlobalSection(false);
        cfg.setEmptyOption(true);
        cfg.setMultiOption(true);
        cfg.setStrictOperator(true);
        cfg.setEmptySection(true);
        cfg.setPathSeparator(KEY_SEPARATOR);
        cfg.setFileEncoding(FILE_ENCODING);
        cfg.setLineSeparator(LINE_SEPARATOR);
        _config = cfg;
    }

    /**
     * <p>Initialise a {@code Reg} object and import the specified Windows registry key.
     * The {@code Reg} object will be empty if the key is not found.</p> 
     * 
     * @param registryKey the name of the registry key to import
     * @throws IOException in the event of any error reading the registry
     */
    public Reg(String registryKey) throws IOException
    {
        this(registryKey, false);
    }

    /**
     * <p>Initialise a {@code Reg} object and import the specified Windows registry key.
     * Optionally adds the key if not found.</p> 
     * 
     * @param registryKey the name of the registry key to import
     * @param create pass {@code true} to add the key if it does not exist in the registry
     * (note that the new key is still not actually created in the registry until calling
     * {@link #store}) 
     * @throws IOException in the event of any error reading the registry
     */
    public Reg(String registryKey, boolean create) throws IOException
    {
        this();
        read(registryKey, create);
    }

    /**
     * <p>Initialise a {@code Reg} object and import the Windows registry data stored in the 
     * specified file.</p>
     * 
     * @param input the file containing the Registry Editor data
     * @throws IOException in the event of any error reading the file
     * @throws InvalidFileFormatException if the file is not a valid Registry Editor file 
     */
    public Reg(File input) throws IOException, InvalidFileFormatException
    {
        this();
        _file = input;
        load();
    }

    /**
     * <p>Initialise a {@code Reg} object and import the Windows registry data stored in the 
     * file at the specified URL.</p>
     * 
     * @param input the URL to the file containing the Registry Editor data
     * @throws IOException in the event of any error reading the file
     * @throws InvalidFileFormatException if the file is not a valid Registry Editor file 
     */
    public Reg(URL input) throws IOException, InvalidFileFormatException
    {
        this();
        load(input);
    }

    /**
     * <p>Initialise a {@code Reg} object and import the Windows registry data from the 
     * specified stream.</p>
     * 
     * @param input the stream containing the Registry Editor data
     * @throws IOException in the event of any error reading the stream
     * @throws InvalidFileFormatException if the stream does not contain valid Registry 
     * Editor data 
     */
    public Reg(InputStream input) throws IOException, InvalidFileFormatException
    {
        this();
        load(input);
    }

    /**
     * <p>Initialise a {@code Reg} object and import the Windows registry data from the 
     * specified reader.</p>
     * 
     * @param input the reader containing the Registry Editor data
     * @throws IOException in the event of any error reading from the reader
     * @throws InvalidFileFormatException if the reader does not contain valid Registry 
     * Editor data 
     */
    public Reg(Reader input) throws IOException, InvalidFileFormatException
    {
        this();
        load(input);
    }

    public static boolean isWindows()
    {
        return WINDOWS;
    }

    @Override public Config getConfig()
    {
        return _config;
    }

    public void setConfig(Config value)
    {
        _config = value;
    }

    @Override public File getFile()
    {
        return _file;
    }

    @Override public void setFile(File value)
    {
        _file = value;
    }

    @Override public void load() throws IOException, InvalidFileFormatException
    {
        if (_file == null)
        {
            throw new FileNotFoundException();
        }

        load(_file);
    }

    @Override public void load(InputStream input) throws IOException, InvalidFileFormatException
    {
        load(new InputStreamReader(input, getConfig().getFileEncoding()));
    }

    @Override public void load(URL input) throws IOException, InvalidFileFormatException
    {
        load(new InputStreamReader(input.openStream(), getConfig().getFileEncoding()));
    }

    @Override public void load(Reader input) throws IOException, InvalidFileFormatException
    {
        int newline = 2;
        StringBuilder buff = new StringBuilder();

        for (int c = input.read(); c != -1; c = input.read())
        {
            if (c == LF)
            {
                newline--;
                if (newline == 0)
                {
                    break;
                }
            }
            else if ((c != CR) && (newline != 1))
            {
                buff.append((char) c);
            }
        }

        if (buff.length() == 0)
        {
            throw new InvalidFileFormatException("Missing version header");
        }

        if (!buff.toString().equals(getVersion()))
        {
            throw new InvalidFileFormatException("Unsupported version: " + buff.toString());
        }

        RegParser.newInstance(getConfig()).parse(input, newBuilder());
    }

    @Override public void load(File input) throws IOException, InvalidFileFormatException
    {
        load(input.toURI().toURL());
    }
    
    /**
     * <p>Import the specified registry key. Does nothing if key not found.</p>
     * 
     * @param registryKey the name of the registry key to import
     * @throws IOException in the event of any error reading the registry
     */
    public void read(String registryKey) throws IOException
    {
        read(registryKey, false);
    }
    
    /**
     * <p>Import the specified registry key. Optionally, add the key if it doesn't exist.</p>
     * 
     * @param registryKey the name of the registry key to import
     * @param create pass {@code true} to add the key if it does not exist in the registry
     * (note that the new key is still not actually created in the registry until calling
     * {@link #store})
     * @throws IOException in the event of any error reading the registry
     */
    public void read(String registryKey, boolean create) throws IOException
    {
        File tmp = createTempFile();

        try
        {
            regExport(registryKey, tmp);
            if( tmp.exists() ) {
            	load(tmp);
            } else {
                if ( create ) {
                    add(registryKey);
                    logger.info("Adding new key " + registryKey);
                }
            }
        }
        finally
        {
            tmp.delete();
        }
    }

    @Override public void store() throws IOException
    {
        if (_file == null)
        {
            throw new FileNotFoundException();
        }

        store(_file);
    }

    @Override public void store(OutputStream output) throws IOException
    {
        store(new OutputStreamWriter(output, getConfig().getFileEncoding()));
    }

    @Override public void store(Writer output) throws IOException
    {
        output.write(getVersion());
        output.write(getConfig().getLineSeparator());
        output.write(getConfig().getLineSeparator());
        store(IniFormatter.newInstance(output, getConfig()));
    }

    @Override public void store(File output) throws IOException
    {
        OutputStream stream = new FileOutputStream(output);

        store(stream);
        stream.close();
    }

    public void write() throws IOException
    {
        File tmp = createTempFile();

        try
        {
            store(tmp);
            regImport(tmp);
        }
        finally
        {
            tmp.delete();
        }
    }

    protected IniHandler newBuilder()
    {
        return RegBuilder.newInstance(this);
    }

    @Override boolean isTreeMode()
    {
        return getConfig().isTree();
    }

    @Override char getPathSeparator()
    {
        return getConfig().getPathSeparator();
    }

    @Override boolean isPropertyFirstUpper()
    {
        return getConfig().isPropertyFirstUpper();
    }

    /**
     * Executes the system command given in the array {@code args}.
     * 
     * @param args
     * @throws InterruptedIOException if the system command did not complete
     * @throws IOException in the event of any other error. If the system command returns
     * a non-zero exit code, a call to {@code getCause()} will return an instance of 
     * {@code ExecutionException}.
     * @see Runtime#exec(String[])
     */
    void exec(String[] args) throws IOException
    {
        Process proc = Runtime.getRuntime().exec(args);
        Reader errStream = null;

        try
        {
            int status = proc.waitFor();
            
            if (status != 0)
            {
                errStream = new InputStreamReader(proc.getErrorStream());
                char[] buff = new char[STDERR_BUFF_SIZE];
                int n = errStream.read(buff);
                
                final String error = new String(buff, 0, n).trim();
                logger.warning(error + " - " + args[4]);
                //throw new IOException(new ExecutionException(error){});
            }
        }
        catch (InterruptedException x)
        {
            throw (IOException) (new InterruptedIOException().initCause(x));
        }
        finally
        {
        	if (errStream != null)
        	{
        		errStream.close();
        	}
        }
    }

    private File createTempFile() throws IOException
    {
        File ret = File.createTempFile(TMP_PREFIX, DEFAULT_SUFFIX);
        ret.deleteOnExit();
        return ret;
    }

    private void regExport(String registryKey, File file) throws IOException
    {
        requireWindows();
        file.delete();		// reg export hangs if the file already exists
        exec(new String[] { "cmd", "/c", "reg", "export", registryKey, file.getAbsolutePath() });
    }

    private void regImport(File file) throws IOException
    {
        requireWindows();
        file.delete();		// reg export hangs if the file already exists
        exec(new String[] { "cmd", "/c", "reg", "import", file.getAbsolutePath() });
    }

    private void requireWindows()
    {
        if (!WINDOWS)
        {
            throw new UnsupportedOperationException("Unsupported operating system or runtime environment");
        }
    }
}

/*
 * $Id$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005 Klaus Bartz
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
package com.izforge.izpack.compiler.compressor;

/**
 * IzPack will be able to support different compression methods for the packs
 * included in the installation jar file. This abstract class implements the
 * interface PackCompressor for the common needed methods.
 *
 * @author Klaus Bartz
 */
public abstract class PackCompressorBase implements PackCompressor
{

    protected String[] formatNames = null;
    protected String decoderMapper = null;
    protected String encoderClassName = null;

    protected Class[] paramsClasses = null;

    private int level = -1;

    /**
     */
    public PackCompressorBase()
    {
        super();
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#getEncoderClassName()
     */
    public String getEncoderClassName()
    {
        return (encoderClassName);
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#useStandardCompression()
     */
    public boolean useStandardCompression()
    {
        return (false);
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#getCompressionFormatSymbols()
     */
    public String[] getCompressionFormatSymbols()
    {
        return formatNames;
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#getDecoderMapperName()
     */
    public String getDecoderMapperName()
    {
        return (decoderMapper);
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#setCompressionLevel(int)
     */
    public void setCompressionLevel(int level)
    {
        this.level = level;
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#getCompressionLevel()
     */
    public int getCompressionLevel()
    {
        return (level);
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.compressor.PackCompressor#needsBufferedOutputStream()
     */
    public boolean needsBufferedOutputStream()
    {
        return (true);
    }
}

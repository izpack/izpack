/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005,2009 Ivan SZKIBA
 * Copyright 2010,2014 René Krell
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

import java.io.Serializable;
import java.nio.charset.Charset;

public class Config implements Cloneable, Serializable
{
    public static final String KEY_PREFIX = "org.ini4j.config.";
    public static final String PROP_EMPTY_OPTION = "emptyOption";
    public static final String PROP_EMPTY_SECTION = "emptySection";
    public static final String PROP_GLOBAL_SECTION = "globalSection";
    public static final String PROP_GLOBAL_SECTION_NAME = "globalSectionName";
    public static final String PROP_INCLUDE = "include";
    public static final String PROP_LOWER_CASE_OPTION = "lowerCaseOption";
    public static final String PROP_LOWER_CASE_SECTION = "lowerCaseSection";
    public static final String PROP_MULTI_OPTION = "multiOption";
    public static final String PROP_MULTI_SECTION = "multiSection";
    public static final String PROP_STRICT_OPERATOR = "strictOperator";
    public static final String PROP_OPERATOR = "operator";
    public static final String PROP_UNNAMED_SECTION = "unnamedSection";
    public static final String PROP_ESCAPE = "escape";
    public static final String PROP_ESCAPE_NEWLINE = "escapeNewline";
    public static final String PROP_PATH_SEPARATOR = "pathSeparator";
    public static final String PROP_TREE = "tree";
    public static final String PROP_PROPERTY_FIRST_UPPER = "propertyFirstUpper";
    public static final String PROP_FILE_ENCODING = "fileEncoding";
    public static final String PROP_LINE_SEPARATOR = "lineSeparator";
    public static final String PROP_COMMENT = "comment";
    public static final String PROP_EMPTY_LINES = "emptyLines";
    public static final String PROP_AUTO_NUMBERING = "autoNumbering";
    public static final boolean DEFAULT_EMPTY_OPTION = false;
    public static final boolean DEFAULT_EMPTY_SECTION = false;
    public static final boolean DEFAULT_GLOBAL_SECTION = false;
    public static final String DEFAULT_GLOBAL_SECTION_NAME = "?";
    public static final boolean DEFAULT_INCLUDE = false;
    public static final boolean DEFAULT_LOWER_CASE_OPTION = false;
    public static final boolean DEFAULT_LOWER_CASE_SECTION = false;
    public static final boolean DEFAULT_MULTI_OPTION = true;
    public static final boolean DEFAULT_MULTI_SECTION = false;
    public static final boolean DEFAULT_STRICT_OPERATOR = false;
    public static final boolean DEFAULT_UNNAMED_SECTION = false;
    public static final boolean DEFAULT_ESCAPE = true;
    public static final boolean DEFAULT_ESCAPE_NEWLINE = true;
    public static final boolean DEFAULT_TREE = true;
    public static final boolean DEFAULT_PROPERTY_FIRST_UPPER = false;
    public static final boolean DEFAULT_COMMENT = true;
    public static final boolean DEFAULT_HEADER_COMMENT = true;
    public static final boolean DEFAULT_EMPTY_LINES = false;
    public static final boolean DEFAULT_AUTO_NUMBERING = false;
    public static final char DEFAULT_PATH_SEPARATOR = '/';
    public static final String DEFAULT_LINE_SEPARATOR = getSystemProperty("line.separator", "\n");
    public static final String DEFAULT_OPERATOR = "=";
    public static final Charset DEFAULT_FILE_ENCODING = Charset.forName("UTF-8");
    private static final Config GLOBAL = new Config();
    private static final long serialVersionUID = 2865793267410367814L;
    private boolean _comment;
    private boolean _emptyOption;
    private boolean _emptySection;
    private boolean _escape;
    private boolean _escapeNewline;
    private Charset _fileEncoding;
    private boolean _globalSection;
    private String _globalSectionName;
    private boolean _headerComment;
    private boolean _include;
    private String _lineSeparator;
    private boolean _lowerCaseOption;
    private boolean _lowerCaseSection;
    private boolean _multiOption;
    private boolean _multiSection;
    private char _pathSeparator;
    private boolean _propertyFirstUpper;
    private boolean _strictOperator;
    private String _operator;
    private boolean _tree;
    private boolean _unnamedSection;
    private boolean _emptyLines;
    private boolean _autoNumbering;

    public Config()
    {
        reset();
    }

    public static String getEnvironment(String name)
    {
        return getEnvironment(name, null);
    }

    public static String getEnvironment(String name, String defaultValue)
    {
        String value;

        try
        {
            value = System.getenv(name);
        }
        catch (SecurityException x)
        {
            value = null;
        }

        return (value == null) ? defaultValue : value;
    }

    public static Config getGlobal()
    {
        return GLOBAL;
    }

    public static String getSystemProperty(String name)
    {
        return getSystemProperty(name, null);
    }

    public static String getSystemProperty(String name, String defaultValue)
    {
        String value;

        try
        {
            value = System.getProperty(name);
        }
        catch (SecurityException x)
        {
            value = null;
        }

        return (value == null) ? defaultValue : value;
    }

    public void setComment(boolean value)
    {
        _comment = value;
    }

    public boolean isEscape()
    {
        return _escape;
    }

    public boolean isEscapeNewline()
    {
        return _escapeNewline;
    }

    public boolean isInclude()
    {
        return _include;
    }

    public boolean isTree()
    {
        return _tree;
    }

    public void setEmptyOption(boolean value)
    {
        _emptyOption = value;
    }

    public void setEmptySection(boolean value)
    {
        _emptySection = value;
    }

    public void setEscape(boolean value)
    {
        _escape = value;
    }

    public void setEscapeNewline(boolean value)
    {
        _escapeNewline = value;
    }

    public Charset getFileEncoding()
    {
        return _fileEncoding;
    }

    public void setFileEncoding(Charset value)
    {
        _fileEncoding = value;
    }

    public void setGlobalSection(boolean value)
    {
        _globalSection = value;
    }

    public String getGlobalSectionName()
    {
        return _globalSectionName;
    }

    public void setGlobalSectionName(String value)
    {
        _globalSectionName = value;
    }

    public void setHeaderComment(boolean value)
    {
        _headerComment = value;
    }

    public void setEmptyLines(boolean value)
    {
        _emptyLines = value;
    }

    public void setAutoNumbering(boolean value)
    {
        _autoNumbering = value;
    }

    public void setInclude(boolean value)
    {
        _include = value;
    }

    public String getLineSeparator()
    {
        return _lineSeparator;
    }

    public void setLineSeparator(String value)
    {
        _lineSeparator = value;
    }

    public void setLowerCaseOption(boolean value)
    {
        _lowerCaseOption = value;
    }

    public void setLowerCaseSection(boolean value)
    {
        _lowerCaseSection = value;
    }

    public void setMultiOption(boolean value)
    {
        _multiOption = value;
    }

    public void setMultiSection(boolean value)
    {
        _multiSection = value;
    }

    public boolean isEmptyOption()
    {
        return _emptyOption;
    }

    public boolean isEmptySection()
    {
        return _emptySection;
    }

    public boolean isGlobalSection()
    {
        return _globalSection;
    }

    public boolean isLowerCaseOption()
    {
        return _lowerCaseOption;
    }

    public boolean isLowerCaseSection()
    {
        return _lowerCaseSection;
    }

    public boolean isMultiOption()
    {
        return _multiOption;
    }

    public boolean isMultiSection()
    {
        return _multiSection;
    }

    public boolean isUnnamedSection()
    {
        return _unnamedSection;
    }

    public char getPathSeparator()
    {
        return _pathSeparator;
    }

    public void setPathSeparator(char value)
    {
        _pathSeparator = value;
    }

    public void setPropertyFirstUpper(boolean value)
    {
        _propertyFirstUpper = value;
    }

    public boolean isPropertyFirstUpper()
    {
        return _propertyFirstUpper;
    }

    public boolean isStrictOperator()
    {
        return _strictOperator;
    }

    public void setStrictOperator(boolean value)
    {
        _strictOperator = value;
    }

    public String getOperator()
    {
        return _operator;
    }

    public void setOperator(String _operator)
    {
        this._operator = _operator;
    }

    public boolean isComment()
    {
        return _comment;
    }

    public boolean isHeaderComment()
    {
        return _headerComment;
    }

    public boolean isEmptyLines()
    {
        return _emptyLines;
    }

    public boolean isAutoNumbering()
    {
        return _autoNumbering;
    }

    public void setTree(boolean value)
    {
        _tree = value;
    }

    public void setUnnamedSection(boolean value)
    {
        _unnamedSection = value;
    }

    @Override public Config clone()
    {
        try
        {
            return (Config) super.clone();
        }
        catch (CloneNotSupportedException x)
        {
            throw new AssertionError(x);
        }
    }

    public final void reset()
    {
        _emptyOption = getBoolean(PROP_EMPTY_OPTION, DEFAULT_EMPTY_OPTION);
        _emptySection = getBoolean(PROP_EMPTY_SECTION, DEFAULT_EMPTY_SECTION);
        _globalSection = getBoolean(PROP_GLOBAL_SECTION, DEFAULT_GLOBAL_SECTION);
        _globalSectionName = getString(PROP_GLOBAL_SECTION_NAME, DEFAULT_GLOBAL_SECTION_NAME);
        _include = getBoolean(PROP_INCLUDE, DEFAULT_INCLUDE);
        _lowerCaseOption = getBoolean(PROP_LOWER_CASE_OPTION, DEFAULT_LOWER_CASE_OPTION);
        _lowerCaseSection = getBoolean(PROP_LOWER_CASE_SECTION, DEFAULT_LOWER_CASE_SECTION);
        _multiOption = getBoolean(PROP_MULTI_OPTION, DEFAULT_MULTI_OPTION);
        _multiSection = getBoolean(PROP_MULTI_SECTION, DEFAULT_MULTI_SECTION);
        _strictOperator = getBoolean(PROP_STRICT_OPERATOR, DEFAULT_STRICT_OPERATOR);
        _operator = getString(PROP_OPERATOR, DEFAULT_OPERATOR);
        _unnamedSection = getBoolean(PROP_UNNAMED_SECTION, DEFAULT_UNNAMED_SECTION);
        _escape = getBoolean(PROP_ESCAPE, DEFAULT_ESCAPE);
        _escapeNewline = getBoolean(PROP_ESCAPE_NEWLINE, DEFAULT_ESCAPE_NEWLINE);
        _pathSeparator = getChar(PROP_PATH_SEPARATOR, DEFAULT_PATH_SEPARATOR);
        _tree = getBoolean(PROP_TREE, DEFAULT_TREE);
        _propertyFirstUpper = getBoolean(PROP_PROPERTY_FIRST_UPPER, DEFAULT_PROPERTY_FIRST_UPPER);
        _lineSeparator = getString(PROP_LINE_SEPARATOR, DEFAULT_LINE_SEPARATOR);
        _fileEncoding = getCharset(PROP_FILE_ENCODING, DEFAULT_FILE_ENCODING);
        _comment = getBoolean(PROP_COMMENT, DEFAULT_COMMENT);
        _emptyLines = getBoolean(PROP_EMPTY_LINES, DEFAULT_EMPTY_LINES);
        _autoNumbering = getBoolean(PROP_AUTO_NUMBERING, DEFAULT_AUTO_NUMBERING);
    }

    private boolean getBoolean(String name, boolean defaultValue)
    {
        String value = getSystemProperty(KEY_PREFIX + name);

        return (value == null) ? defaultValue : Boolean.parseBoolean(value);
    }

    private char getChar(String name, char defaultValue)
    {
        String value = getSystemProperty(KEY_PREFIX + name);

        return (value == null) ? defaultValue : value.charAt(0);
    }

    private Charset getCharset(String name, Charset defaultValue)
    {
        String value = getSystemProperty(KEY_PREFIX + name);

        return (value == null) ? defaultValue : Charset.forName(value);
    }

    private String getString(String name, String defaultValue)
    {
        return getSystemProperty(KEY_PREFIX + name, defaultValue);
    }
}

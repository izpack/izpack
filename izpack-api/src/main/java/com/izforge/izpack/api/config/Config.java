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
package com.izforge.izpack.api.config;

import com.izforge.izpack.api.data.InstallData;

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
    private boolean comment;
    private boolean emptyOption;
    private boolean emptySection;
    private boolean escape;
    private boolean escapeNewline;
    private Charset fileEncoding;
    private boolean globalSection;
    private String globalSectionName;
    private boolean headerComment;
    private boolean include;
    private String lineSeparator;
    private boolean lowerCaseOption;
    private boolean lowerCaseSection;
    private boolean multiOption;
    private boolean multiSection;
    private char pathSeparator;
    private boolean propertyFirstUpper;
    private boolean strictOperator;
    private String operator;
    private boolean tree;
    private boolean unnamedSection;
    private boolean emptyLines;
    private boolean autoNumbering;

    private InstallData installData;

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
        this.comment = value;
    }

    public boolean isEscape()
    {
        return this.escape;
    }

    public boolean isEscapeNewline()
    {
        return this.escapeNewline;
    }

    public boolean isInclude()
    {
        return this.include;
    }

    public boolean isTree()
    {
        return this.tree;
    }

    public void setEmptyOption(boolean value)
    {
        this.emptyOption = value;
    }

    public void setEmptySection(boolean value)
    {
        this.emptySection = value;
    }

    public void setEscape(boolean value)
    {
        this.escape = value;
    }

    public void setEscapeNewline(boolean value)
    {
        this.escapeNewline = value;
    }

    public Charset getFileEncoding()
    {
        return this.fileEncoding;
    }

    public void setFileEncoding(Charset value)
    {
        this.fileEncoding = value;
    }

    public void setGlobalSection(boolean value)
    {
        this.globalSection = value;
    }

    public String getGlobalSectionName()
    {
        return this.globalSectionName;
    }

    public void setGlobalSectionName(String value)
    {
        this.globalSectionName = value;
    }

    public void setHeaderComment(boolean value)
    {
        this.headerComment = value;
    }

    public void setEmptyLines(boolean value)
    {
        this.emptyLines = value;
    }

    public void setAutoNumbering(boolean value)
    {
        this.autoNumbering = value;
    }

    public void setInclude(boolean value)
    {
        this.include = value;
    }

    public String getLineSeparator()
    {
        return this.lineSeparator;
    }

    public void setLineSeparator(String value)
    {
        this.lineSeparator = value;
    }

    public void setLowerCaseOption(boolean value)
    {
        this.lowerCaseOption = value;
    }

    public void setLowerCaseSection(boolean value)
    {
        this.lowerCaseSection = value;
    }

    public void setMultiOption(boolean value)
    {
        this.multiOption = value;
    }

    public void setMultiSection(boolean value)
    {
        this.multiSection = value;
    }

    public boolean isEmptyOption()
    {
        return this.emptyOption;
    }

    public boolean isEmptySection()
    {
        return this.emptySection;
    }

    public boolean isGlobalSection()
    {
        return this.globalSection;
    }

    public boolean isLowerCaseOption()
    {
        return this.lowerCaseOption;
    }

    public boolean isLowerCaseSection()
    {
        return this.lowerCaseSection;
    }

    public boolean isMultiOption()
    {
        return this.multiOption;
    }

    public boolean isMultiSection()
    {
        return this.multiSection;
    }

    public boolean isUnnamedSection()
    {
        return this.unnamedSection;
    }

    public char getPathSeparator()
    {
        return this.pathSeparator;
    }

    public void setPathSeparator(char value)
    {
        this.pathSeparator = value;
    }

    public void setPropertyFirstUpper(boolean value)
    {
        this.propertyFirstUpper = value;
    }

    public boolean isPropertyFirstUpper()
    {
        return this.propertyFirstUpper;
    }

    public boolean isStrictOperator()
    {
        return this.strictOperator;
    }

    public void setStrictOperator(boolean value)
    {
        this.strictOperator = value;
    }

    public String getOperator()
    {
        return this.operator;
    }

    public void setOperator(String operator)
    {
        this.operator = operator;
    }

    public boolean isComment()
    {
        return this.comment;
    }

    public boolean isHeaderComment()
    {
        return this.headerComment;
    }

    public boolean isEmptyLines()
    {
        return this.emptyLines;
    }

    public boolean isAutoNumbering()
    {
        return this.autoNumbering;
    }

    public void setTree(boolean value)
    {
        this.tree = value;
    }

    public void setUnnamedSection(boolean value)
    {
        this.unnamedSection = value;
    }

    public InstallData getInstallData()
    {
        return installData;
    }

    public void setInstallData(InstallData installData)
    {
        this.installData = installData;
    }

    @Override
    public Config clone()
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
        this.emptyOption = getBoolean(PROP_EMPTY_OPTION, DEFAULT_EMPTY_OPTION);
        this.emptySection = getBoolean(PROP_EMPTY_SECTION, DEFAULT_EMPTY_SECTION);
        this.globalSection = getBoolean(PROP_GLOBAL_SECTION, DEFAULT_GLOBAL_SECTION);
        this.globalSectionName = getString(PROP_GLOBAL_SECTION_NAME, DEFAULT_GLOBAL_SECTION_NAME);
        this.include = getBoolean(PROP_INCLUDE, DEFAULT_INCLUDE);
        this.lowerCaseOption = getBoolean(PROP_LOWER_CASE_OPTION, DEFAULT_LOWER_CASE_OPTION);
        this.lowerCaseSection = getBoolean(PROP_LOWER_CASE_SECTION, DEFAULT_LOWER_CASE_SECTION);
        this.multiOption = getBoolean(PROP_MULTI_OPTION, DEFAULT_MULTI_OPTION);
        this.multiSection = getBoolean(PROP_MULTI_SECTION, DEFAULT_MULTI_SECTION);
        this.strictOperator = getBoolean(PROP_STRICT_OPERATOR, DEFAULT_STRICT_OPERATOR);
        this.operator = getString(PROP_OPERATOR, DEFAULT_OPERATOR);
        this.unnamedSection = getBoolean(PROP_UNNAMED_SECTION, DEFAULT_UNNAMED_SECTION);
        this.escape = getBoolean(PROP_ESCAPE, DEFAULT_ESCAPE);
        this.escapeNewline = getBoolean(PROP_ESCAPE_NEWLINE, DEFAULT_ESCAPE_NEWLINE);
        this.pathSeparator = getChar(PROP_PATH_SEPARATOR, DEFAULT_PATH_SEPARATOR);
        this.tree = getBoolean(PROP_TREE, DEFAULT_TREE);
        this.propertyFirstUpper = getBoolean(PROP_PROPERTY_FIRST_UPPER, DEFAULT_PROPERTY_FIRST_UPPER);
        this.lineSeparator = getString(PROP_LINE_SEPARATOR, DEFAULT_LINE_SEPARATOR);
        this.fileEncoding = getCharset(PROP_FILE_ENCODING, DEFAULT_FILE_ENCODING);
        this.comment = getBoolean(PROP_COMMENT, DEFAULT_COMMENT);
        this.emptyLines = getBoolean(PROP_EMPTY_LINES, DEFAULT_EMPTY_LINES);
        this.autoNumbering = getBoolean(PROP_AUTO_NUMBERING, DEFAULT_AUTO_NUMBERING);
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

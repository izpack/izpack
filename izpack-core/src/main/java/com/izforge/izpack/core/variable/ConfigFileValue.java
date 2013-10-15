/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2010 Rene Krell
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

package com.izforge.izpack.core.variable;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.util.config.base.Config;
import com.izforge.izpack.util.config.base.Ini;
import com.izforge.izpack.util.config.base.Options;

public abstract class ConfigFileValue extends ValueImpl implements Serializable
{
    /**
     *
     */
    private static final long serialVersionUID = 6082215731362372562L;

    public final static int CONFIGFILE_TYPE_OPTIONS = 0;
    public final static int CONFIGFILE_TYPE_INI = 1;
    public final static int CONFIGFILE_TYPE_XML = 2;

    public int type = CONFIGFILE_TYPE_OPTIONS; // optional, default: property file
    public String section; // mandatory for type = "ini"
    public String key; // mandatory

    public boolean escape = true; // optional

    public ConfigFileValue(int type, String section, String key, boolean escape)
    {
        super();
        this.type = type;
        this.section = section;
        this.key = key;
        setEscape(escape);
    }

    public int getType()
    {
        return type;
    }

    public void setType(int type)
    {
        this.type = type;
    }

    public String getSection()
    {
        return section;
    }

    public void setSection(String section)
    {
        this.section = section;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public boolean isEscape()
    {
        return escape;
    }

    public void setEscape(boolean escape)
    {
        this.escape = escape;
    }

    @Override
    public void validate() throws Exception
    {
        if (this.type == CONFIGFILE_TYPE_INI && (this.section == null || this.section.length() <= 0))
        {
            throw new Exception("No INI file section defined");
        }
        if (this.type != CONFIGFILE_TYPE_INI && this.section != null)
        {
            throw new Exception("No INI file section expected for non-INI file types");
        }
    }

    protected String resolve(InputStream in) throws Exception
    {
        Config config;
        switch (type)
        {
            case CONFIGFILE_TYPE_OPTIONS:
                config = Config.getGlobal().clone();
                config.setEscape(isEscape());
                Options opts = new Options(config);
                opts.load(in);
                return opts.get(key);
            case CONFIGFILE_TYPE_INI:
                config = Config.getGlobal().clone();
                config.setEscape(isEscape());
                Ini ini = new Ini(config);
                ini.load(in);
                return ini.get(section, key);
            case CONFIGFILE_TYPE_XML:
                return parseXPath(in, key, System.getProperty("line.separator"));
            default:
                throw new Exception("Invalid configuration file type " + type);
        }
    }

    protected String resolve(InputStream in, VariableSubstitutor... substitutors)
            throws Exception
    {
        String _key_ = key;
        for (VariableSubstitutor substitutor : substitutors)
        {
            _key_ = substitutor.substitute(_key_);
        }

        switch (type)
        {
            case CONFIGFILE_TYPE_OPTIONS:
                Options opts;
                opts = new Options(in);
                return opts.get(_key_);
            case CONFIGFILE_TYPE_INI:
                Ini ini;
                String _section_ = section;
                for (VariableSubstitutor substitutor : substitutors)
                {
                    _key_ = substitutor.substitute(_key_);
                }
                ini = new Ini(in);
                return ini.get(_section_, _key_);
            case CONFIGFILE_TYPE_XML:
                return parseXPath(in, _key_, System.getProperty("line.separator"));
            default:
                throw new Exception("Invalid configuration file type '" + type + "'");
        }
    }

    private static String parseXPath(InputStream in, String expression, String separator)
            throws ParserConfigurationException, SAXException, IOException, XPathExpressionException
    {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder;
        builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(in);
        XPath xpath = XPathFactory.newInstance().newXPath();
        // XPath Query for showing all nodes value
        XPathExpression expr = xpath.compile(expression);
        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < nodes.getLength(); i++)
        {
            String value = nodes.item(i).getNodeValue();
            if (value != null)
            {
                if (sb.length() > 0)
                {
                    sb.append(separator);
                }
                sb.append(value);
            }
        }
        return sb.toString();
    }
}

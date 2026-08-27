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

package com.izforge.izpack.api.factory;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralizes the creation of XML-related factories to apply all security-relevant settings in one place.
 */
public final class XMLAccess
{
    private static final Logger LOGGER = Logger.getLogger(XMLAccess.class.getName());

    private XMLAccess()
    {
    }

    public static TransformerFactory transformerFactory()
    {
        TransformerFactory factory = TransformerFactory.newInstance();
        setFeature(factory, TransformerFactory::setFeature, XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return factory;
    }

    public static DocumentBuilderFactory documentBuilderFactory()
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        setFeature(factory, DocumentBuilderFactory::setFeature, XMLConstants.FEATURE_SECURE_PROCESSING, true);
        setFeature(factory, DocumentBuilderFactory::setFeature, "http://apache.org/xml/features/disallow-doctype-decl", true);
        setFeature(factory, DocumentBuilderFactory::setFeature, "http://xml.org/sax/features/external-general-entities", false);
        setFeature(factory, DocumentBuilderFactory::setFeature, "http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
    }

    public static SAXParserFactory saxParserFactory()
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        setFeature(factory, SAXParserFactory::setFeature, XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return factory;
    }

    interface FeatureAction<T>
    {
        void apply(T factory, String feature, boolean value) throws Exception;
    }

    private static <T> void setFeature(T factory, FeatureAction<T> action, String feature, boolean value)
    {
        try
        {
            action.apply(factory, feature, value);
        }
        catch (Exception e)
        {
            LOGGER.log(Level.WARNING, String.format("Failed feature %s to %s", feature, value) , e);
        }
    }
}

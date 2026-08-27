package com.izforge.izpack.api.factory;

import org.junit.Test;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class XMLAccessTest
{
    @Test
    public void testTransformerFactorySecuritySettings()
    {
        TransformerFactory factory = XMLAccess.transformerFactory();
        assertNotNull(factory);
        assertTrue("Secure processing should be enabled", factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
    }

    @Test
    public void testDocumentBuilderFactorySecuritySettings() throws Exception
    {
        DocumentBuilderFactory factory = XMLAccess.documentBuilderFactory();
        assertNotNull(factory);
        assertTrue("Secure processing should be enabled", factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
        assertTrue("Disallow doctype decl should be enabled", factory.getFeature("http://apache.org/xml/features/disallow-doctype-decl"));
        assertFalse("External general entities should be disabled", factory.getFeature("http://xml.org/sax/features/external-general-entities"));
        assertFalse("External parameter entities should be disabled", factory.getFeature("http://xml.org/sax/features/external-parameter-entities"));
        assertFalse("XIncludeAware should be false", factory.isXIncludeAware());
        assertFalse("ExpandEntityReferences should be false", factory.isExpandEntityReferences());
    }

    @Test
    public void testSaxParserFactorySecuritySettings() throws Exception
    {
        SAXParserFactory factory = XMLAccess.saxParserFactory();
        assertNotNull(factory);
        assertTrue("Secure processing should be enabled", factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
    }
}

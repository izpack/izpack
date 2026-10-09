package com.izforge.izpack.api.factory;

import static com.izforge.izpack.api.factory.XMLAccess.documentBuilderFactory;
import static com.izforge.izpack.api.factory.XMLAccess.saxParserFactory;
import static com.izforge.izpack.api.factory.XMLAccess.transformerFactory;
import static org.assertj.core.api.Assertions.assertThat;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;
import org.junit.jupiter.api.Test;


public class XMLAccessTest
{
    @Test
    public void testTransformerFactorySecuritySettings()
    {
        TransformerFactory factory = transformerFactory();
        assertThat(factory).isNotNull();
        assertThat(factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING)).as("Secure processing should be enabled").isTrue();
    }

    @Test
    public void testDocumentBuilderFactorySecuritySettings() throws Exception
    {
        DocumentBuilderFactory factory = documentBuilderFactory();
        assertThat(factory).isNotNull();
        assertThat(factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING)).as("Secure processing should be enabled").isTrue();
        assertThat(factory.getFeature("http://apache.org/xml/features/disallow-doctype-decl")).as("Disallow doctype decl should be enabled").isTrue();
        assertThat(factory.getFeature("http://xml.org/sax/features/external-general-entities")).as("External general entities should be disabled").isFalse();
        assertThat(factory.getFeature("http://xml.org/sax/features/external-parameter-entities")).as("External parameter entities should be disabled").isFalse();
        assertThat(factory.getAttribute(XMLConstants.ACCESS_EXTERNAL_DTD)).as("Access external DTD should be empty").isEqualTo("");
        assertThat(factory.getAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA)).as("Access external schema should be empty").isEqualTo("");
        assertThat(factory.isXIncludeAware()).as("XIncludeAware should be false").isFalse();
        assertThat(factory.isExpandEntityReferences()).as("ExpandEntityReferences should be false").isFalse();
    }

    @Test
    public void testSaxParserFactorySecuritySettings() throws Exception
    {
        SAXParserFactory factory = saxParserFactory();
        assertThat(factory).isNotNull();
        assertThat(factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING)).as("Secure processing should be enabled").isTrue();
    }
}

package com.izforge.izpack.util.xmlmerge;
import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.izforge.izpack.util.xmlmerge.config.ConfigurableXmlMerge;
import com.izforge.izpack.util.xmlmerge.config.PropertyXPathConfigurer;


public class XmlMergeTest
{

    @Before
    public void setUp() throws Exception
    {
    }

    @Test
    public void testComplete2Sources()
    {
        // Original
        final String s1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<el1 el1_attr1=\"el1_attr1_value_from_original\">" +
                "  <el2 NAME=\"el2_NAME_value_from_original\" el2_attr2=\"el2_attr2_value_from_original\"/>" +
                "  <el3 el3_attr1=\"el3_attr1_value_from_original\" el3_attr2=\"el3_attr2_value_from_original\">" +
                "    <el4 el4_attr1=\"el4_attr1_value_from_original\" el4_attr2=\"el4_attr2_value_from_original\"/>" +
                "  </el3>" +
                "</el1>";

        // Patch
        final String s2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<el1 el1_attr1=\"el1_attr1_value_from_patch\">" +
                "  <el2 NAME=\"el2_NAME_value_from_patch\" el2_attr2=\"el2_attr2_value_from_patch\" el2_attr3=\"el2_attr3_value_from_patch\"/>" +
                "</el1>";

        Properties confProps = new Properties();
        confProps.setProperty("action.default", "FULLMERGE");
        confProps.setProperty("xpath.path1", "/el1/el2");
        confProps.setProperty("matcher.path1", "NAME_ATTRIBUTE");
        confProps.setProperty("action.path1", "COMPLETE");
        confProps.setProperty("xpath.path2", "/el1/el3/el4");
        confProps.setProperty("matcher.path2", "ATTRIBUTE");
        confProps.setProperty("action.path2", "COMPLETE");

        String result = null;

        try {
            XmlMerge xmlMerge = new ConfigurableXmlMerge(new PropertyXPathConfigurer(confProps));
            try {
                // s1: Original
                // s2, ..., sn: Patches
                result = xmlMerge.merge(new String[] {s1, s2});
            } catch (AbstractXmlMergeException e) {
                fail(e.getMessage());
            }
        } catch (ConfigurationException e) {
            fail(e.getMessage());
        }

        assertNotNull(result);

        final String expectedResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<el1 el1_attr1=\"el1_attr1_value_from_patch\">\n" +
                "  <el2 NAME=\"el2_NAME_value_from_original\" el2_attr2=\"el2_attr2_value_from_original\" />\n" +
                "  <el2 NAME=\"el2_NAME_value_from_patch\" el2_attr2=\"el2_attr2_value_from_patch\" el2_attr3=\"el2_attr3_value_from_patch\" />\n" +
                "  <el3 el3_attr1=\"el3_attr1_value_from_original\" el3_attr2=\"el3_attr2_value_from_original\">\n" +
                "    <el4 el4_attr1=\"el4_attr1_value_from_original\" el4_attr2=\"el4_attr2_value_from_original\" />\n" +
                "  </el3>\n" +
                "</el1>\n";
        assertEquals(result, expectedResult);
    }

}

package com.izforge.izpack.util.xmlmerge;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.izforge.izpack.util.xmlmerge.config.ConfigurableXmlMerge;
import com.izforge.izpack.util.xmlmerge.config.PropertyXPathConfigurer;


public class XmlMergeTest
{
    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

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

        String expectedResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<el1 el1_attr1=\"el1_attr1_value_from_patch\">\n" +
                "  <el2 NAME=\"el2_NAME_value_from_original\" el2_attr2=\"el2_attr2_value_from_original\" />\n" +
                "  <el2 NAME=\"el2_NAME_value_from_patch\" el2_attr2=\"el2_attr2_value_from_patch\" el2_attr3=\"el2_attr3_value_from_patch\" />\n" +
                "  <el3 el3_attr1=\"el3_attr1_value_from_original\" el3_attr2=\"el3_attr2_value_from_original\">\n" +
                "    <el4 el4_attr1=\"el4_attr1_value_from_original\" el4_attr2=\"el4_attr2_value_from_original\" />\n" +
                "  </el3>\n" +
                "</el1>\n";
        expectedResult = expectedResult.replace("\n", System.getProperty("line.separator"));
        assertEquals(result, expectedResult);
    }

    /**
     * Test merging of a patch file to an original file, parallely use the original file as output file.
     * Tests several XPath-based actions and matchers.
     * @throws IOException
     * @throws AbstractXmlMergeException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    @Test
    public void testMergeFilesWithXPathTagMatcherReplace2Files() throws IOException, AbstractXmlMergeException, SAXException, ParserConfigurationException
    {
        URL patchSourceFileUrl = getClass().getResource("maps_resources_patch.xml");
        URL patchTargetFileUrl = getClass().getResource("maps_resources_original.xml");
        URL expectedFileUrl = getClass().getResource("maps_resources_expected.xml");
        assertNotNull("Patch source file missing", patchSourceFileUrl);
        assertNotNull("Patch target file missing", patchTargetFileUrl);
        assertNotNull("Expected result file missing", expectedFileUrl);

        File targetFile = tmpDir.newFile("maps_resources_merged.xml");

        // Copy target file to a temporary location,
        // it should be patch target and output at one time in this test and therefore is written to it
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(new File(patchTargetFileUrl.getFile())).getChannel();
            outputChannel = new FileOutputStream(targetFile).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            inputChannel.close();
            outputChannel.close();
        }

        XmlMerge xmlMerge;
        Properties confProps = new Properties();
        confProps.setProperty("action.default", "PRESERVE"); // Preserve with that from original
        confProps.setProperty("xpath.path1", "/maps/address-source");
        confProps.setProperty("matcher.path1", "TAG");
        confProps.setProperty("xpath.path2", "/maps/proxy");
        confProps.setProperty("matcher.path2", "TAG");
        confProps.setProperty("action.path2", "REPLACE"); // Replace with that from patch
        xmlMerge = new ConfigurableXmlMerge(new PropertyXPathConfigurer(confProps));
        xmlMerge.merge( new File[]{
                targetFile,
                new File(patchSourceFileUrl.getFile())
                },
                targetFile);


        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setCoalescing(true);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setIgnoringComments(false);
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document resultDocument = db.parse(targetFile);
        resultDocument.normalizeDocument();

        Document expectedDocument = db.parse(new File(expectedFileUrl.getFile()));
        expectedDocument.normalizeDocument();

        assertTrue("Result document does not match expected result", resultDocument.isEqualNode(expectedDocument));
    }

}

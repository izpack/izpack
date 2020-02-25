/*
 * IzPack - Copyright 2001-2020 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.api.adaptator.impl;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Tests the {@link UserInputFilter} implementation.
 * 
 * @author Patrick Reinhart <patrick@reini.net>
 */
public class UserInputFilterTest
{
	static final String INPUT_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<AutomatedInstallation langpack=\"eng\">"
			+ "<com.izforge.izpack.panels.userinput.UserInputPanel id=\"userinput.panel.id\">"
			+ "<userInput>"
			+ "<entry key=\"keyName\" value=\"theValue\"/>"
			+ "</userInput>"
			+ "<custom>"
			+ "<userInput description=\"some custom input\"/>"
			+ "</custom>"
			+ "</com.izforge.izpack.panels.userinput.UserInputPanel>"
			+ "</AutomatedInstallation>";

	static final String EXPECTED_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<AutomatedInstallation langpack=\"eng\">"
			+ "<com.izforge.izpack.panels.userinput.UserInputPanel id=\"userinput.panel.id\">"
			+ "<entry key=\"keyName\" value=\"theValue\"/>"
			+ "<custom>"
			+ "<userInput description=\"some custom input\"/>"
			+ "</custom>"
			+ "</com.izforge.izpack.panels.userinput.UserInputPanel>"
			+ "</AutomatedInstallation>";

	@Test
	public void testFilter() throws ParserConfigurationException, SAXException, TransformerException
	{
         SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

         SAXSource source = new SAXSource(new InputSource(new StringReader(INPUT_XML)));
         source.setXMLReader(new UserInputFilter(parser.getXMLReader()));

         StringWriter sw = new StringWriter();
         TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(sw));

         assertEquals(EXPECTED_XML, sw.toString());
	}	
}

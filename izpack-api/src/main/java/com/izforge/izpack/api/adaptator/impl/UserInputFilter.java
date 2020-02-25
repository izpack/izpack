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

import java.util.ArrayDeque;
import java.util.Deque;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * A custom SAX XML filter, used to remove the izPack 4 {@code userInput} elements.
 *
 * @author Patrick Reinhart <patrick@reini.net>
 */
public class UserInputFilter extends XMLFilterImpl
{
	private final Deque<String> elementStack; 

	public UserInputFilter(XMLReader xmlReader)
	{
        super(xmlReader);
        elementStack = new ArrayDeque<String>();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
	{
		if (!(elementStack.size() == 2 && "userInput".equals(qName)))
		{
			elementStack.add(qName);
			super.startElement(uri, localName, qName, atts);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		if (!(elementStack.size() == 2 && "userInput".equals(qName)))
		{
			elementStack.pop();
			super.endElement(uri, localName, qName);
		}
	}
}

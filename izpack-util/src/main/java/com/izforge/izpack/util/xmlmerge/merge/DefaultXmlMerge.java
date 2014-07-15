/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2009 Laurent Bovet, Alex Mathey
 * Copyright 2010, 2012 René Krell
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

package com.izforge.izpack.util.xmlmerge.merge;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;

import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.DOMBuilder;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.output.DOMOutputter;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.izforge.izpack.util.xmlmerge.AbstractXmlMergeException;
import com.izforge.izpack.util.xmlmerge.DocumentException;
import com.izforge.izpack.util.xmlmerge.MergeAction;
import com.izforge.izpack.util.xmlmerge.OperationFactory;
import com.izforge.izpack.util.xmlmerge.ParseException;
import com.izforge.izpack.util.xmlmerge.XmlMerge;
import com.izforge.izpack.util.xmlmerge.action.FullMergeAction;
import com.izforge.izpack.util.xmlmerge.factory.StaticOperationFactory;
import com.izforge.izpack.util.xmlmerge.mapper.IdentityMapper;
import com.izforge.izpack.util.xmlmerge.matcher.AttributeMatcher;

/**
 * Default implementation of XmlMerge. Create all JDOM documents, then perform the merge into a new
 * JDOM document.
 *
 * @author Laurent Bovet (LBO)
 * @author Alex Mathey (AMA)
 */
public class DefaultXmlMerge implements XmlMerge
{

    /**
     * Root merge action.
     */
    private MergeAction m_rootMergeAction = new FullMergeAction();

    /**
     * Creates a new DefaultXmlMerge instance.
     */
    public DefaultXmlMerge()
    {
        setRootMergeActionFactory(new StaticOperationFactory(new FullMergeAction()));
        setRootMergeMatcherFactory(new StaticOperationFactory(new AttributeMatcher()));
        setRootMergeMapperFactory(new StaticOperationFactory(new IdentityMapper()));
    }

    @Override
    public void setRootMergeActionFactory(OperationFactory factory)
    {
        this.m_rootMergeAction.setActionFactory(factory);
    }

    @Override
    public void setRootMergeMatcherFactory(OperationFactory factory)
    {
        m_rootMergeAction.setMatcherFactory(factory);
    }

    @Override
    public void setRootMergeMapperFactory(OperationFactory factory)
    {
        m_rootMergeAction.setMapperFactory(factory);
    }


    @Override
    public String merge(String[] sources) throws AbstractXmlMergeException
    {

        InputStream[] inputStreams = new InputStream[sources.length];

        for (int i = 0; i < sources.length; i++)
        {
            inputStreams[i] = new ByteArrayInputStream(sources[i].getBytes());
        }

        InputStream merged = merge(inputStreams);

        ByteArrayOutputStream result = new ByteArrayOutputStream();

        try
        {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = merged.read(buffer)) != -1)
            {
                result.write(buffer, 0, len);
            }
        }
        catch (IOException ioe)
        {
            // should never happen
            throw new RuntimeException(ioe);
        }

        return result.toString();
    }

    @Override
    public org.w3c.dom.Document merge(org.w3c.dom.Document[] sources)
            throws AbstractXmlMergeException
    {
        DOMBuilder domb = new DOMBuilder();

        // to save all XML files as JDOM objects
        Document[] docs = new Document[sources.length];

        for (int i = 0; i < sources.length; i++)
        {
            // ask JDOM to parse the given inputStream
            docs[i] = domb.build(sources[i]);
        }

        Document result = doMerge(docs);

        DOMOutputter outputter = new DOMOutputter();

        try
        {
            return outputter.output(result);
        }
        catch (JDOMException e)
        {
            throw new DocumentException(result, e);
        }
    }

    @Override
    public InputStream merge(InputStream[] sources) throws AbstractXmlMergeException
    {
        SAXBuilder builder = new SAXBuilder(XMLReaders.NONVALIDATING);
        // Xerces-specific - see: http://xerces.apache.org/xerces-j/features.html
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        Document[] docs = new Document[sources.length];

        for (int i = 0; i < sources.length; i++)
        {
            try
            {
                docs[i] = builder.build(sources[i]);
            }
            catch (Exception e)
            {
                throw new ParseException(e);
            }
        }

        Document result = doMerge(docs);

        Format prettyFormatter = Format.getPrettyFormat();
        // Use system line seperator to avoid problems
        // with carriage return under linux
        prettyFormatter.setLineSeparator(System.getProperty("line.separator"));
        XMLOutputter sortie = new XMLOutputter(prettyFormatter);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try
        {
            sortie.output(result, buffer);
        }
        catch (IOException ex)
        {
            throw new DocumentException(result, ex);
        }

        return new ByteArrayInputStream(buffer.toByteArray());
    }

    @Override
    public void merge(File[] sources, File target) throws AbstractXmlMergeException
    {
        SAXBuilder builder = new SAXBuilder(XMLReaders.NONVALIDATING);
        // Xerces-specific - see: http://xerces.apache.org/xerces-j/features.html
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        Document[] docs = new Document[sources.length];

        for (int i = 0; i < sources.length; i++)
        {
            try
            {
                docs[i] = builder.build(sources[i]);
            }
            catch (Exception e)
            {
                throw new ParseException(e);
            }
        }

        Document result = doMerge(docs);

        Format prettyFormatter = Format.getPrettyFormat();
        // Use system line separator to avoid problems
        // with carriage return under linux
        prettyFormatter.setLineSeparator(System.getProperty("line.separator"));
        XMLOutputter sortie = new XMLOutputter(prettyFormatter);

        try
        {
            sortie.output(result, new FileOutputStream(target));
        }
        catch (IOException ex)
        {
            throw new DocumentException(result, ex);
        }
    }

    /**
     * Performs the actual merge.
     *
     * @param docs The documents to merge. The first doc is assumed to be the original one to apply patches against.
     * @return The merged result document
     * @throws AbstractXmlMergeException If an error occurred during the merge
     */
    private Document doMerge(Document[] docs) throws AbstractXmlMergeException
    {
        Document originalDoc = docs[0];
        Element origRootElement = originalDoc.getRootElement();

        for (int i = 1; i < docs.length; i++)
        {
            Element comparedRootElement = docs[i].getRootElement();

            Document output = new Document();
            if (originalDoc.getDocType() != null)
            {
                output.setDocType((DocType) originalDoc.getDocType().clone());
            }
            output.setRootElement(new Element("root"));
            Element outputRootElement = output.getRootElement();

            m_rootMergeAction.perform(origRootElement, comparedRootElement,
                    outputRootElement);

            Element root = (Element) outputRootElement.getChildren().get(0);
            root.detach();

            sortRootChildrenRecursive(root);

            originalDoc.setRootElement(root);
        }

        return originalDoc;
    }

    private static void sortRootChildrenRecursive(Element root)
    {
        sortRootChildrenRecursive(root, new Comparator<Element>() {
            @Override
            public int compare(Element o1, Element o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    private static void sortRootChildrenRecursive(Element root, Comparator<Element> comparator)
    {
        for (Element element : root.getChildren())
        {
            sortRootChildrenRecursive(element, comparator);
        }

        root.sortChildren(comparator);
    }
}

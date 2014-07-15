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

package com.izforge.izpack.util.xmlmerge;

import java.io.File;
import java.io.InputStream;

import org.w3c.dom.Document;

/**
 * Entry point for merging XML documents.
 *
 * @author Laurent Bovet (LBO)
 * @author Alex Mathey (AMA)
 */
public interface XmlMerge
{

    /**
     * Merges the given InputStream sources.
     *
     * @param sources Array of InputStream sources to merge
     * @return InputStream corresponding to the merged sources
     * @throws AbstractXmlMergeException If an error occurred during the merge
     */
    public InputStream merge(InputStream[] sources) throws AbstractXmlMergeException;

    /**
     * Merges the given InputStream sources.
     *
     * @param sources Array of File sources to merge
     * @return File corresponding to the merged sources, which is automatically the first source file
     * @throws AbstractXmlMergeException If an error occurred during the merge
     */
    public void merge(File[] sources, File target) throws AbstractXmlMergeException;

    /**
     * Merges the given Document sources.
     *
     * @param sources Array of Document sources to merge
     * @return Document corresponding to the merged sources
     * @throws AbstractXmlMergeException If an error occurred during the merge
     */
    public Document merge(Document[] sources) throws AbstractXmlMergeException;

    /**
     * Merges the given String sources.
     *
     * @param sources Array of String sources to merge
     * @return String corresponding to the merged sources
     * @throws AbstractXmlMergeException If an error occurred during the merge
     */
    public String merge(String[] sources) throws AbstractXmlMergeException;

    /**
     * Sets the merge actions which will be applied to the root element.
     *
     * @param rootMergeAction The merge actions which will be applied to the root element
     */
    public void setRootMergeActionFactory(OperationFactory factory);

    /**
     * Sets the mappers which will be applied to the root element.
     *
     * @param rootMapper The mappers which will be applied to the root element
     */
    public void setRootMergeMapperFactory(OperationFactory factory);

    /**
     * Sets the matchers which will be applied to the root element.
     *
     * @param rootMapper The matchers which will be applied to the root element
     */
    public void setRootMergeMatcherFactory(OperationFactory factory);
}
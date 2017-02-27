/*
 * Copyright  2002-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.izforge.izpack.util.file.types.selectors;

import java.util.Enumeration;

/**
 * This is the interface for selectors that can contain other selectors.
 */
public interface SelectorContainer
{

    /**
     * Indicates whether there are any selectors here.
     *
     * @return whether any selectors are in this container
     */
    boolean hasSelectors();

    /**
     * Gives the count of the number of selectors in this container
     *
     * @return the number of selectors in this container
     */
    int selectorCount();

    /**
     * Returns the set of selectors as an array.
     *
     * @return an array of selectors in this container
     */
    FileSelector[] getSelectors();

    /**
     * Returns an enumerator for accessing the set of selectors.
     *
     * @return an enumerator that goes through each of the selectors
     */
    Enumeration<FileSelector> selectorElements();

    /**
     * Add a new selector into this container.
     *
     * @param selector the new selector to add
     */
    void appendSelector(FileSelector selector);

    /**
     * add an "And" selector entry on the selector list
     *
     * @param selector the selector to add
     */
    void addAnd(AndSelector selector);

    /**
     * add an "Or" selector entry on the selector list
     *
     * @param selector the selector to add
     */
    void addOr(OrSelector selector);

    /**
     * add a "Not" selector entry on the selector list
     *
     * @param selector the selector to add
     */
    void addNot(NotSelector selector);

    /**
     * add a "None" selector entry on the selector list
     *
     * @param selector the selector to add
     */
    void addNone(NoneSelector selector);

    /**
     * add a majority selector entry on the selector list
     *
     * @param selector the selector to add
     */
    void addMajority(MajoritySelector selector);

    /**
     * add a selector date entry on the selector list
     *
     * @param selector the selector to add
     */
    void addDate(DateSelector selector);

    /**
     * add a selector size entry on the selector list
     *
     * @param selector the selector to add
     */
    void addSize(SizeSelector selector);

    /**
     * add a selector filename entry on the selector list
     *
     * @param selector the selector to add
     */
    void addFilename(FilenameSelector selector);

    /**
     * add an extended selector entry on the selector list
     *
     * @param selector the selector to add
     */
    void addCustom(ExtendSelector selector);

    /**
     * add a contains selector entry on the selector list
     *
     * @param selector the selector to add
     */
    void addContains(ContainsSelector selector);

    /**
     * add a present selector entry on the selector list
     *
     * @param selector the selector to add
     */
    void addPresent(PresentSelector selector);

    /**
     * add a depth selector entry on the selector list
     *
     * @param selector the selector to add
     */
    void addDepth(DepthSelector selector);

    /**
     * add a depends selector entry on the selector list
     *
     * @param selector the selector to add
     */
    void addDepend(DependSelector selector);

    /**
     * add a regular expression selector entry on the selector list
     *
     * @param selector the selector to add
     */
    void addContainsRegexp(ContainsRegexpSelector selector);

    /**
     * add the type selector
     *
     * @param selector the selector to add
     */
    void addType(TypeSelector selector);

    /**
     * add the different selector
     *
     * @param selector the selector to add
     */
    void addDifferent(DifferentSelector selector);

    /**
     * add an arbitary selector
     *
     * @param selector the selector to add
     */
    void add(FileSelector selector);
}

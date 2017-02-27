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
package com.izforge.izpack.util.file.types;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Logger;

import com.izforge.izpack.util.file.DirectoryScanner;
import com.izforge.izpack.util.file.FileScanner;
import com.izforge.izpack.util.file.types.selectors.*;

/**
 * Class that holds an implicit patternset and supports nested patternsets and
 * creates a DirectoryScanner using these patterns.
 * <p>
 * Common base class for DirSet and FileSet.</p>
 */
public class FileSet extends DataType
        implements Cloneable, SelectorContainer
{

    private static final Logger LOGGER = Logger.getLogger(FileSet.class.getName());

    private PatternSet defaultPatterns = new PatternSet();
    private Vector<PatternSet> additionalPatterns = new Vector<PatternSet>();
    private Vector<FileSelector> selectors = new Vector<FileSelector>();

    private File dir;
    private boolean useDefaultExcludes = true;
    private boolean isCaseSensitive = true;
    private boolean followSymlinks = true;

    /**
     * Construct a new <code>FileSet</code>.
     */
    public FileSet()
    {
        super();
    }

    /**
     * Sets the base-directory for this instance.
     *
     * @param dir the directory's <code>File</code> instance.
     */
    public void setDir(File dir) throws Exception
    {
        this.dir = dir;
    }

    /**
     * Retrieves the base-directory for this instance.
     *
     * @return <code>File</code>.
     */
    public File getDir()
    {
        return dir;
    }

    /**
     * Add a name entry to the include list.
     *
     * @return <code>PatternSet.NameEntry</code>.
     */
    public PatternSet.NameEntry createInclude()
    {
        return defaultPatterns.createInclude();
    }

    /**
     * Add a name entry to the exclude list.
     *
     * @return <code>PatternSet.NameEntry</code>.
     */
    public PatternSet.NameEntry createExclude()
    {
        return defaultPatterns.createExclude();
    }

    /**
     * Creates a single file fileset.
     *
     * @param file the single <code>File</code> included in this
     * <code>AbstractFileSet</code>.
     */
    public void setFile(File file) throws Exception
    {
        if (!file.exists())
        {
            throw new FileNotFoundException("File " + file + " not found");
        }
        setDir(file.getParentFile());
        createInclude().setName(file.getName());
    }

    /**
     * Appends <code>includes</code> to the current list of include patterns.
     * <p>
     * Patterns may be separated by a comma or a space.</p>
     *
     * @param includes the <code>String</code> containing the include patterns.
     */
    public void setIncludes(String includes)
    {
        defaultPatterns.setIncludes(includes);
    }

    /**
     * Appends <code>excludes</code> to the current list of exclude patterns.
     * <p/>
     * <p>
     * Patterns may be separated by a comma or a space.</p>
     *
     * @param excludes the <code>String</code> containing the exclude patterns.
     */
    public void setExcludes(String excludes)
    {
        defaultPatterns.setExcludes(excludes);
    }

    /**
     * Sets whether default exclusions should be used or not.
     *
     * @param useDefaultExcludes <code>boolean</code>.
     */
    public void setDefaultexcludes(boolean useDefaultExcludes)
    {
        this.useDefaultExcludes = useDefaultExcludes;
    }

    /**
     * Whether default exclusions should be used or not.
     */
    public boolean getDefaultexcludes()
    {
        return useDefaultExcludes;
    }

    /**
     * Sets case sensitivity of the file system.
     *
     * @param isCaseSensitive <code>boolean</code>.
     */
    public void setCaseSensitive(boolean isCaseSensitive)
    {
        this.isCaseSensitive = isCaseSensitive;
    }

    /**
     * Sets whether or not symbolic links should be followed.
     *
     * @param followSymlinks whether or not symbolic links should be followed.
     */
    public void setFollowSymlinks(boolean followSymlinks)
    {
        this.followSymlinks = followSymlinks;
    }

    /**
     * Find out if the fileset wants to follow symbolic links.
     *
     * @return <code>boolean</code> indicating whether symbolic links should be
     * followed.
     * @since Ant 1.6
     */
    public boolean isFollowSymlinks()
    {
        return followSymlinks;
    }

    /**
     * Returns the directory scanner needed to access the files to process.
     *
     * @return a <code>DirectoryScanner</code> instance.
     */
    public DirectoryScanner getDirectoryScanner() throws Exception
    {
        if (dir == null)
        {
            throw new Exception("No directory specified for fileset");
        }
        if (!dir.exists())
        {
            throw new Exception(dir.getAbsolutePath() + " not found.");
        }
        if (!dir.isDirectory())
        {
            throw new Exception(dir.getAbsolutePath()
                    + " is not a directory.");
        }
        DirectoryScanner ds = new DirectoryScanner();
        setupDirectoryScanner(ds);
        ds.setFollowSymlinks(followSymlinks);
        ds.scan();
        return ds;
    }

    /**
     * Set up the specified directory scanner against the specified project.
     *
     * @param ds a <code>FileScanner</code> instance.
     */
    public void setupDirectoryScanner(FileScanner ds)
    {
        if (ds == null)
        {
            throw new IllegalArgumentException("ds cannot be null");
        }
        ds.setBasedir(dir);

        final int count = additionalPatterns.size();
        for (int i = 0; i < count; i++)
        {
            Object o = additionalPatterns.elementAt(i);
            defaultPatterns.append((PatternSet) o);
        }
        LOGGER.fine("Fileset setup scanner in dir " + dir
                + " with " + defaultPatterns);

        ds.setIncludes(defaultPatterns.getIncludePatterns());
        ds.setExcludes(defaultPatterns.getExcludePatterns());
        if (ds instanceof SelectorScanner)
        {
            SelectorScanner ss = (SelectorScanner) ds;
            ss.setSelectors(getSelectors());
        }
        if (useDefaultExcludes)
        {
            ds.addDefaultExcludes();
        }
        ds.setCaseSensitive(isCaseSensitive);
    }

    // SelectorContainer methods
    /**
     * Indicates whether there are any selectors here.
     *
     * @return whether any selectors are in this container.
     */
    @Override
    public boolean hasSelectors()
    {
        return !(selectors.isEmpty());
    }

    /**
     * Indicates whether there are any patterns here.
     *
     * @return whether any patterns are in this container.
     */
    public boolean hasPatterns()
    {
        if (defaultPatterns.hasPatterns())
        {
            return true;
        }
        Enumeration<PatternSet> e = additionalPatterns.elements();
        while (e.hasMoreElements())
        {
            PatternSet ps = e.nextElement();
            if (ps.hasPatterns())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Gives the count of the number of selectors in this container.
     *
     * @return the number of selectors in this container as an <code>int</code>.
     */
    @Override
    public int selectorCount()
    {
        return selectors.size();
    }

    /**
     * Returns the set of selectors as an array.
     *
     * @return a <code>FileSelector[]</code> of the selectors in this container.
     */
    @Override
    public FileSelector[] getSelectors()
    {
        return selectors.toArray(new FileSelector[selectors.size()]);
    }

    /**
     * Returns an enumerator for accessing the set of selectors.
     *
     * @return an <code>Enumeration</code> of selectors.
     */
    @Override
    public Enumeration<FileSelector> selectorElements()
    {
        return selectors.elements();
    }

    /**
     * Add a new selector into this container.
     *
     * @param selector the new <code>FileSelector</code> to add.
     */
    @Override
    public void appendSelector(FileSelector selector)
    {
        selectors.addElement(selector);
    }

    /* Methods below all add specific selectors */
    /**
     * Add an "And" selector entry on the selector list.
     *
     * @param selector the <code>AndSelector</code> to add.
     */
    @Override
    public void addAnd(AndSelector selector)
    {
        appendSelector(selector);
    }

    /**
     * Add an "Or" selector entry on the selector list.
     *
     * @param selector the <code>OrSelector</code> to add.
     */
    @Override
    public void addOr(OrSelector selector)
    {
        appendSelector(selector);
    }

    /**
     * Add a "Not" selector entry on the selector list.
     *
     * @param selector the <code>NotSelector</code> to add.
     */
    @Override
    public void addNot(NotSelector selector)
    {
        appendSelector(selector);
    }

    /**
     * Add a "None" selector entry on the selector list.
     *
     * @param selector the <code>NoneSelector</code> to add.
     */
    @Override
    public void addNone(NoneSelector selector)
    {
        appendSelector(selector);
    }

    /**
     * Add a majority selector entry on the selector list.
     *
     * @param selector the <code>MajoritySelector</code> to add.
     */
    @Override
    public void addMajority(MajoritySelector selector)
    {
        appendSelector(selector);
    }

    /**
     * Add a selector date entry on the selector list.
     *
     * @param selector the <code>DateSelector</code> to add.
     */
    @Override
    public void addDate(DateSelector selector)
    {
        appendSelector(selector);
    }

    /**
     * Add a selector size entry on the selector list.
     *
     * @param selector the <code>SizeSelector</code> to add.
     */
    @Override
    public void addSize(SizeSelector selector)
    {
        appendSelector(selector);
    }

    /**
     * Add a DifferentSelector entry on the selector list.
     *
     * @param selector the <code>DifferentSelector</code> to add.
     */
    @Override
    public void addDifferent(DifferentSelector selector)
    {
        appendSelector(selector);
    }

    /**
     * Add a selector filename entry on the selector list.
     *
     * @param selector the <code>FilenameSelector</code> to add.
     */
    @Override
    public void addFilename(FilenameSelector selector)
    {
        appendSelector(selector);
    }

    /**
     * Add a selector type entry on the selector list.
     *
     * @param selector the <code>TypeSelector</code> to add.
     */
    @Override
    public void addType(TypeSelector selector)
    {
        appendSelector(selector);
    }

    /**
     * Add an extended selector entry on the selector list.
     *
     * @param selector the <code>ExtendSelector</code> to add.
     */
    @Override
    public void addCustom(ExtendSelector selector)
    {
        appendSelector(selector);
    }

    /**
     * Add a contains selector entry on the selector list.
     *
     * @param selector the <code>ContainsSelector</code> to add.
     */
    @Override
    public void addContains(ContainsSelector selector)
    {
        appendSelector(selector);
    }

    /**
     * Add a present selector entry on the selector list.
     *
     * @param selector the <code>PresentSelector</code> to add.
     */
    @Override
    public void addPresent(PresentSelector selector)
    {
        appendSelector(selector);
    }

    /**
     * Add a depth selector entry on the selector list.
     *
     * @param selector the <code>DepthSelector</code> to add.
     */
    @Override
    public void addDepth(DepthSelector selector)
    {
        appendSelector(selector);
    }

    /**
     * Add a depends selector entry on the selector list.
     *
     * @param selector the <code>DependSelector</code> to add.
     */
    @Override
    public void addDepend(DependSelector selector)
    {
        appendSelector(selector);
    }

    /**
     * Add a regular expression selector entry on the selector list.
     *
     * @param selector the <code>ContainsRegexpSelector</code> to add.
     */
    @Override
    public void addContainsRegexp(ContainsRegexpSelector selector)
    {
        appendSelector(selector);
    }

    /**
     * Add an arbitary selector.
     *
     * @param selector the <code>FileSelector</code> to add.
     */
    @Override
    public void add(FileSelector selector)
    {
        appendSelector(selector);
    }

}

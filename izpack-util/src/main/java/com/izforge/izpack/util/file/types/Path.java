/*
 * Copyright  2000-2004 The Apache Software Foundation
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
import java.util.Vector;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.util.file.FileUtils;
import com.izforge.izpack.util.file.PathTokenizer;

/**
 * This object represents a path as used by CLASSPATH or PATH environment
 * variable.
 * <p/>
 * <code>
 * &lt;sometask&gt;<br>
 * &nbsp;&nbsp;&lt;somepath&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;pathelement location="/path/to/file.jar"
 * /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;pathelement
 * path="/path/to/file2.jar:/path/to/class2;/path/to/class3" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;pathelement location="/path/to/file3.jar"
 * /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;pathelement location="/path/to/file4.jar"
 * /&gt;<br>
 * &nbsp;&nbsp;&lt;/somepath&gt;<br>
 * &lt;/sometask&gt;<br>
 * </code>
 * <p/>
 * The object implemention <code>sometask</code> must provide a method called
 * <code>createSomepath</code> which returns an instance of <code>Path</code>.
 * Nested path definitions are handled by the Path object and must be labeled
 * <code>pathelement</code>.<p>
 * <p/>
 * The path element takes a parameter <code>path</code> which will be parsed and
 * split into single elements. It will usually be used to define a path from an
 * environment variable.
 */
public class Path extends DataType implements Cloneable
{

    private static final Logger LOGGER = Logger.getLogger(Path.class.getName());

    private Vector<Object> elements;

    /**
     * Helper class, holds the nested <code>&lt;pathelement&gt;</code> values.
     */
    public class PathElement
    {

        private String[] parts;

        public void setLocation(File loc)
        {
            parts = new String[]
            {
                translateFile(loc.getAbsolutePath())
            };
        }

        public void setPath(InstallData idata, String path)
        {
            parts = Path.translatePath(idata, path);
        }

        public String[] getParts()
        {
            return parts;
        }
    }

    /**
     * Invoked by IntrospectionHelper for <code>setXXX(Path p)</code> attribute
     * setters.
     *
     * @param idata the install data
     * @param path the path definition.
     */
    public Path(InstallData idata, String path) throws Exception
    {
        createPathElement().setPath(idata, path);
    }

    /**
     * Construct an empty Path.
     */
    public Path()
    {
        elements = new Vector<Object>();
    }

    /**
     * Adds a element definition to the path.
     *
     * @param location the location of the element to add (must not be
     * <code>null</code> nor empty.
     */
    public void setLocation(File location) throws Exception
    {
        createPathElement().setLocation(location);
    }

    /**
     * Parses a path definition and creates single PathElements.
     *
     * @param path the <CODE>String</CODE> path definition.
     */
    public void setPath(InstallData idata, String path) throws Exception
    {
        createPathElement().setPath(idata, path);
    }

    /**
     * Creates the nested <code>&lt;pathelement&gt;</code> element.
     */
    public PathElement createPathElement() throws Exception
    {
        PathElement pe = new PathElement();
        elements.addElement(pe);
        return pe;
    }

    /**
     * Adds a nested <code>&lt;fileset&gt;</code> element.
     */
    public void addFileset(FileSet fs) throws Exception
    {
        elements.addElement(fs);
        setChecked(false);
    }

    /**
     * Adds a nested <code>&lt;filelist&gt;</code> element.
     */
    public void addFilelist(FileList fl) throws Exception
    {
        elements.addElement(fl);
        setChecked(false);
    }

    /**
     * Adds a nested <code>&lt;dirset&gt;</code> element.
     */
    public void addDirset(DirSet dset) throws Exception
    {
        elements.addElement(dset);
        setChecked(false);
    }

    /**
     * Adds a nested path
     *
     * @since Ant 1.6
     */
    public void add(Path path) throws Exception
    {
        elements.addElement(path);
        setChecked(false);

    }

    /**
     * Creates a nested <code>&lt;path&gt;</code> element.
     */
    public Path createPath() throws Exception
    {
        Path p = new Path();
        elements.addElement(p);
        setChecked(false);
        return p;
    }

    /**
     * Append the contents of the other Path instance to this.
     */
    public void append(Path other) throws Exception
    {
        if (other == null)
        {
            return;
        }
        String[] pathElements = other.list();
        for (String pathElement : pathElements)
        {
            if (elements.indexOf(pathElement) == -1)
            {
                elements.addElement(pathElement);
            }
        }
    }

    /**
     * Adds the components on the given path which exist to this Path.
     * Components that don't exist, aren't added.
     *
     * @param source - source path whose components are examined for existence
     */
    public void addExisting(Path source) throws Exception
    {
        addExisting(source, false);
    }

    /**
     * Same as addExisting, but support classpath behavior if tryUserDir is
     * true. Classpaths are relative to user dir, not the project base. That
     * used to break jspc test
     *
     * @param source
     * @param tryUserDir
     */
    public void addExisting(Path source, boolean tryUserDir) throws Exception
    {
        String[] pathElements = source.list();
        File userDir = (tryUserDir) ? new File(System.getProperty("user.dir"))
                : null;

        for (String pathElement : pathElements)
        {
            File f = null;
            f = new File(pathElement);
            // probably not the best choice, but it solves the problem of
            // relative paths in CLASSPATH
            if (tryUserDir && !f.exists())
            {
                f = new File(userDir, pathElement);
            }
            if (f.exists())
            {
                setLocation(f);
            }
            else
            {
                LOGGER.warning("Dropping " + f + " from path as it doesn't exist");
            }
        }
    }

    /**
     * Returns all path elements defined by this and nested path objects.
     *
     * @return list of path elements.
     */
    public String[] list() throws Exception
    {
        Vector<String> result = new Vector<String>(2 * elements.size());
        for (int i = 0; i < elements.size(); i++)
        {
            Object o = elements.elementAt(i);

            if (o instanceof String)
            {
                // obtained via append
                addUnlessPresent(result, (String) o);
            }
            else if (o instanceof PathElement)
            {
                String[] parts = ((PathElement) o).getParts();
                if (parts == null)
                {
                    throw new Exception("You must either set location or"
                            + " path on <pathelement>");
                }
                for (String part : parts)
                {
                    addUnlessPresent(result, part);
                }
            }
            else if (o instanceof Path)
            {
                Path p = (Path) o;
                String[] parts = p.list();
                for (String part : parts)
                {
                    addUnlessPresent(result, part);
                }
            }
            else if (o instanceof DirSet)
            {
                DirSet dset = (DirSet) o;
                addUnlessPresent(result, dset.getDir(),
                        dset.getDirectoryScanner().getIncludedDirectories());
            }
            else if (o instanceof FileSet)
            {
                FileSet fs = (FileSet) o;
                addUnlessPresent(result, fs.getDir(),
                        fs.getDirectoryScanner().getIncludedFiles());
            }
            else if (o instanceof FileList)
            {
                FileList fl = (FileList) o;
                addUnlessPresent(result,
                        fl.getDir(), fl.getFiles());
            }
        }
        String[] res = new String[result.size()];
        result.copyInto(res);
        return res;
    }

    /**
     * Returns a textual representation of the path, which can be used as
     * CLASSPATH or PATH environment variable definition.
     *
     * @return a textual representation of the path.
     */
    @Override
    public String toString()
    {
        try
        {
            final String[] list = list();

            // empty path return empty string
            if (list.length == 0)
            {
                return "";
            }

            // path containing one or more elements
            final StringBuffer result = new StringBuffer(list[0].toString());
            for (int i = 1; i < list.length; i++)
            {
                result.append(File.pathSeparatorChar);
                result.append(list[i]);
            }

            return result.toString();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Splits a PATH (with : or ; as separators) into its parts.
     */
    public static String[] translatePath(InstallData idata, String source)
    {
        final Vector<String> result = new Vector<String>();
        if (source == null)
        {
            return new String[0];
        }

        PathTokenizer tok = new PathTokenizer(source);
        StringBuffer element = new StringBuffer();
        while (tok.hasMoreTokens())
        {
            String pathElement = tok.nextToken();
            try
            {
                element.append(resolveFile(idata, pathElement));
            }
            catch (Exception e)
            {
                LOGGER.warning("Dropping path element " + pathElement
                        + " as it is not a valid relative to the project"/*,
                    Project.MSG_VERBOSE*/);
            }
            for (int i = 0; i < element.length(); i++)
            {
                translateFileSep(element, i);
            }
            result.addElement(element.toString());
            element = new StringBuffer();
        }
        String[] res = new String[result.size()];
        result.copyInto(res);
        return res;
    }

    /**
     * Returns its argument with all file separator characters replaced so that
     * they match the local OS conventions.
     */
    public static String translateFile(String source)
    {
        if (source == null)
        {
            return "";
        }

        final StringBuffer result = new StringBuffer(source);
        for (int i = 0; i < result.length(); i++)
        {
            translateFileSep(result, i);
        }

        return result.toString();
    }

    /**
     * Translates all occurrences of / or \ to correct separator of the current
     * platform and returns whether it had to do any replacements.
     */
    protected static boolean translateFileSep(StringBuffer buffer, int pos)
    {
        if (buffer.charAt(pos) == '/' || buffer.charAt(pos) == '\\')
        {
            buffer.setCharAt(pos, File.separatorChar);
            return true;
        }
        return false;
    }

    /**
     * How many parts does this Path instance consist of.
     */
    public int size() throws Exception
    {
        return list().length;
    }

    /**
     * Return a Path that holds the same elements as this instance.
     */
    @Override
    public Object clone()
    {
        try
        {
            Path p = (Path) super.clone();
            p.elements = (Vector<Object>) elements.clone();
            return p;
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Resolve a filename with Project's help - if we know one that is.
     * <p/>
     * <p>
     * Assume the filename is absolute if project is null.</p>
     */
    private static String resolveFile(InstallData idata, String relativeName)
            throws Exception
    {
        File f = FileUtils.resolveFile(new File(idata.getInstallPath()), relativeName);
        return f.getAbsolutePath();
    }

    /**
     * Adds a String to the Vector if it isn't already included.
     */
    private static void addUnlessPresent(Vector<String> v, String s)
    {
        if (v.indexOf(s) == -1)
        {
            v.addElement(s);
        }
    }

    /**
     * Adds absolute path names of listed files in the given directory to the
     * Vector if they are not already included.
     */
    private static void addUnlessPresent(Vector<String> v, File dir, String[] s)
    {
        for (String value : s)
        {
            File d = new File(dir, value);
            String absolutePath = d.getAbsolutePath();
            addUnlessPresent(v, translateFile(absolutePath));
        }
    }
}

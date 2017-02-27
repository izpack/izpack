/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005,2009 Ivan SZKIBA
 * Copyright 2010,2011 Rene Krell
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
package com.izforge.izpack.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

public class IniPreferences extends AbstractPreferences
{

    /**
     * frequently used empty String array
     */
    private static final String[] EMPTY =
    {
    };

    /**
     * underlaying <code>Ini</code> implementation
     */
    private final Ini ini;

    /**
     * Constructs a new preferences node on top of <code>Ini</code> instance.
     *
     * @param ini underlaying <code>Ini</code> instance
     */
    public IniPreferences(Ini ini)
    {
        super(null, "");
        this.ini = ini;
    }

    /**
     * Constructs a new preferences node based on newly loaded <code>Ini</code>
     * instance.
     *
     * This is just a helper constructor, to make simpler constructing
     * <code>IniPreferences</code> directly from <code>Reader</code>.
     *
     * @param input the <code>Reader</code> containing <code>Ini</code> data
     * @throws IOException if an I/O error occured
     * @throws InvalidFileFormatException if <code>Ini</code> parsing error
     * occured
     */
    public IniPreferences(Reader input) throws IOException, InvalidFileFormatException
    {
        super(null, "");
        this.ini = new Ini(input);
    }

    /**
     * Constructs a new preferences node based on newly loaded <code>Ini</code>
     * instance.
     *
     * This is just a helper constructor, to make simpler constructing
     * <code>IniPreferences</code> directly from <code>InputStream</code>.
     *
     * @param input the <code>InputStream</code> containing <code>Ini</code>
     * data
     * @throws IOException if an I/O error occured
     * @throws InvalidFileFormatException if <code>Ini</code> parsing error
     * occured
     */
    public IniPreferences(InputStream input) throws IOException, InvalidFileFormatException
    {
        super(null, "");
        this.ini = new Ini(input);
    }

    /**
     * Constructs a new preferences node based on newly loaded <code>Ini</code>
     * instance.
     *
     * This is just a helper constructor, to make simpler constructing
     * <code>IniPreferences</code> directly from <code>URL</code>.
     *
     * @param input the <code>URL</code> containing <code>Ini</code> data
     * @throws IOException if an I/O error occured
     * @throws InvalidFileFormatException if <code>Ini</code> parsing error
     * occured
     */
    public IniPreferences(URL input) throws IOException, InvalidFileFormatException
    {
        super(null, "");
        this.ini = new Ini(input);
    }

    /**
     * Provide access to underlaying {@link Ini} implementation.
     *
     * @return <code>Ini</code> implementation
     */
    protected Ini getIni()
    {
        return this.ini;
    }

    /**
     * Implements the <CODE>getSpi</CODE> method as per the specification in
     * {@link java.util.prefs.AbstractPreferences#getSpi(String)}.
     *
     * This implementation doesn't support this operation, so allways throws
     * UnsupportedOperationException.
     *
     * @return if the value associated with the specified key at this preference
     * node, or null if there is no association for this key, or the association
     * cannot be determined at this time.
     * @param key key to getvalue for
     * @throws UnsupportedOperationException this implementation allways throws
     * this exception
     */
    @Override
    protected String getSpi(String key) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Implements the <CODE>childrenNamesSpi</CODE> method as per the
     * specification in
     * {@link java.util.prefs.AbstractPreferences#childrenNamesSpi()}.
     *
     * @return an array containing the names of the children of this preference
     * node.
     * @throws BackingStoreException if this operation cannot be completed due
     * to a failure in the backing store, or inability to communicate with it.
     */
    @Override
    protected String[] childrenNamesSpi() throws BackingStoreException
    {
        List<String> names = new ArrayList<String>();

        for (String name : this.ini.keySet())
        {
            if (name.indexOf(this.ini.getPathSeparator()) < 0)
            {
                names.add(name);
            }
        }

        return names.toArray(EMPTY);
    }

    /**
     * Implements the <CODE>childSpi</CODE> method as per the specification in
     * {@link java.util.prefs.AbstractPreferences#childSpi(String)}.
     *
     * @param name child name
     * @return child node
     */
    @Override
    protected SectionPreferences childSpi(String name)
    {
        Ini.Section sec = this.ini.get(name);
        boolean isNew = sec == null;

        if (isNew)
        {
            sec = this.ini.add(name);
        }

        return new SectionPreferences(this, sec, isNew);
    }

    /**
     * Implements the <CODE>flushSpi</CODE> method as per the specification in
     * {@link java.util.prefs.AbstractPreferences#flushSpi()}.
     *
     * This implementation does nothing.
     *
     * @throws BackingStoreException if this operation cannot be completed due
     * to a failure in the backing store, or inability to communicate with it.
     */
    @Override
    protected void flushSpi() throws BackingStoreException
    {
        assert true;
    }

    /**
     * Implements the <CODE>keysSpi</CODE> method as per the specification in
     * {@link java.util.prefs.AbstractPreferences#keysSpi()}.
     *
     * This implementation allways return an empty array.
     *
     * @return an empty array.
     * @throws BackingStoreException if this operation cannot be completed due
     * to a failure in the backing store, or inability to communicate with it.
     */
    @Override
    protected String[] keysSpi() throws BackingStoreException
    {
        return EMPTY;
    }

    /**
     * Implements the <CODE>putSpi</CODE> method as per the specification in
     * {@link java.util.prefs.AbstractPreferences#putSpi(String,String)}.
     *
     * This implementation doesn;t support this operation, so allways throws
     * UnsupportedOperationException.
     *
     * @param key key to set value for
     * @param value new value for key
     * @throws UnsupportedOperationException this implementation allways throws
     * this exception
     */
    @Override
    protected void putSpi(String key, String value) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Implements the <CODE>removeNodeSpi</CODE> method as per the specification
     * in {@link java.util.prefs.AbstractPreferences#removeNodeSpi()}.
     *
     * This implementation doesn;t support this operation, so allways throws
     * UnsupportedOperationException.
     *
     * @throws UnsupportedOperationException this implementation allways throws
     * this exception
     * @throws BackingStoreException this implementation never throws this
     * exception
     */
    @Override
    protected void removeNodeSpi() throws BackingStoreException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Implements the <CODE>removeSpi</CODE> method as per the specification in
     * {@link java.util.prefs.AbstractPreferences#removeSpi(String)}.
     *
     * @param key key to remove
     * @throws UnsupportedOperationException this implementation allways throws
     * this exception
     */
    @Override
    protected void removeSpi(String key) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Implements the <CODE>syncSpi</CODE> method as per the specification in
     * {@link java.util.prefs.AbstractPreferences#syncSpi()}.
     *
     * This implementation does nothing.
     *
     * @throws BackingStoreException if this operation cannot be completed due
     * to a failure in the backing store, or inability to communicate with it.
     */
    @Override
    protected void syncSpi() throws BackingStoreException
    {
        assert true;
    }

    protected class SectionPreferences extends AbstractPreferences
    {

        /**
         * underlaying <code>Section</code> implementation
         */
        private final Ini.Section section;

        /**
         * Constructs a new SectionPreferences instance on top of Ini.Section
         * instance.
         *
         * @param parent parent preferences node
         * @parem section underlaying Ini.Section instance
         * @param isNew indicate is this a new node or already existing one
         */
        SectionPreferences(AbstractPreferences parent, Ini.Section section, boolean isNew)
        {
            super(parent, section.getSimpleName());
            this.section = section;
            newNode = isNew;
        }

        /**
         * Implements the <CODE>flush</CODE> method as per the specification in
         * {@link java.util.prefs.Preferences#flush()}.
         *
         * This implementation just call parent's <code>flush()</code> method.
         *
         * @throws BackingStoreException if this operation cannot be completed
         * due to a failure in the backing store, or inability to communicate
         * with it.
         */
        @Override
        public void flush() throws BackingStoreException
        {
            parent().flush();
        }

        /**
         * Implements the <CODE>sync</CODE> method as per the specification in
         * {@link java.util.prefs.Preferences#sync()}.
         *
         * This implementation just call parent's <code>sync()</code> method.
         *
         * @throws BackingStoreException if this operation cannot be completed
         * due to a failure in the backing store, or inability to communicate
         * with it.
         */
        @Override
        public void sync() throws BackingStoreException
        {
            parent().sync();
        }

        /**
         * Implements the <CODE>getSpi</CODE> method as per the specification in
         * {@link java.util.prefs.AbstractPreferences#getSpi(String)}.
         *
         * @return if the value associated with the specified key at this
         * preference node, or null if there is no association for this key, or
         * the association cannot be determined at this time.
         * @param key key to getvalue for
         */
        @Override
        protected String getSpi(String key)
        {
            return this.section.fetch(key);
        }

        /**
         * Implements the <CODE>childrenNamesSpi</CODE> method as per the
         * specification in
         * {@link java.util.prefs.AbstractPreferences#childrenNamesSpi()}.
         *
         * This implementation allways returns an empty array.
         *
         * @return an emty array.
         * @throws BackingStoreException if this operation cannot be completed
         * due to a failure in the backing store, or inability to communicate
         * with it.
         */
        @Override
        protected String[] childrenNamesSpi() throws BackingStoreException
        {
            return this.section.childrenNames();
        }

        /**
         * Implements the <CODE>childSpi</CODE> method as per the specification
         * in {@link java.util.prefs.AbstractPreferences#childSpi(String)}.
         *
         * This implementation doesn't support this operation.
         *
         * @throws UnsupportedOperationException this implementation allways
         * throws this exception
         * @param name child name
         * @return child node
         */
        @Override
        protected SectionPreferences childSpi(String name) throws UnsupportedOperationException
        {
            Ini.Section child = this.section.getChild(name);
            boolean isNew = child == null;

            if (isNew)
            {
                child = this.section.addChild(name);
            }

            return new SectionPreferences(this, child, isNew);
        }

        /**
         * Implements the <CODE>flushSpi</CODE> method as per the specification
         * in {@link java.util.prefs.AbstractPreferences#flushSpi()}.
         *
         * This implementation does nothing.
         *
         * @throws BackingStoreException if this operation cannot be completed
         * due to a failure in the backing store, or inability to communicate
         * with it.
         */
        @Override
        protected void flushSpi() throws BackingStoreException
        {
            assert true;
        }

        /**
         * Implements the <CODE>keysSpi</CODE> method as per the specification
         * in {@link java.util.prefs.AbstractPreferences#keysSpi()}.
         *
         * @return an array of the keys that have an associated value in this
         * preference node.
         * @throws BackingStoreException if this operation cannot be completed
         * due to a failure in the backing store, or inability to communicate
         * with it.
         */
        @Override
        protected String[] keysSpi() throws BackingStoreException
        {
            return this.section.keySet().toArray(EMPTY);
        }

        /**
         * Implements the <CODE>putSpi</CODE> method as per the specification in
         * {@link java.util.prefs.AbstractPreferences#putSpi(String,String)}.
         *
         * @param key key to set value for
         * @param value new value of key
         */
        @Override
        protected void putSpi(String key, String value)
        {
            this.section.put(key, value);
        }

        /**
         * Implements the <CODE>removeNodeSpi</CODE> method as per the
         * specification in
         * {@link java.util.prefs.AbstractPreferences#removeNodeSpi()}.
         *
         * @throws BackingStoreException if this operation cannot be completed
         * due to a failure in the backing store, or inability to communicate
         * with it.
         */
        @Override
        protected void removeNodeSpi() throws BackingStoreException
        {
            ini.remove(this.section);
        }

        /**
         * Implements the <CODE>removeSpi</CODE> method as per the specification
         * in {@link java.util.prefs.AbstractPreferences#removeSpi(String)}.
         *
         * @param key key to remove
         */
        @Override
        protected void removeSpi(String key)
        {
            this.section.remove(key);
        }

        /**
         * Implements the <CODE>syncSpi</CODE> method as per the specification
         * in {@link java.util.prefs.AbstractPreferences#syncSpi()}.
         *
         * This implementation does nothing.
         *
         * @throws BackingStoreException if this operation cannot be completed
         * due to a failure in the backing store, or inability to communicate
         * with it.
         */
        @Override
        protected void syncSpi() throws BackingStoreException
        {
            assert true;
        }
    }
}

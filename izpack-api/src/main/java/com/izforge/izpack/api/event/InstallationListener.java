/**
 * Common parent of InstallerListener and UninstallerListener
 *
 * @see https://jira.codehaus.org/browse/IZPACK-1175
 */
package com.izforge.izpack.api.event;

/**
 * @author helpstone
 *
 */
public interface InstallationListener
{

    /**
     * Initializes the listener.
     *
     * @throws IzPackException for any error
     */
    void initialise();

    /**
     * Determines if the listener should be notified of every file and directory
     * installation / deletion.
     * <p/>
     * For InstallerListeners: If <tt>true</tt>, the {@link #beforeFile} and
     * {@link #afterFile} methods will be invoked for every installed file, and
     * {@link #beforeDir}, and {@link #afterDir} invoked for each directory
     * creation. For UnnstallerListeners: If <tt>true</tt>, the
     * {@link #beforeDelete(File)} and {@link #afterDelete(File)} methods will
     * be invoked for each file.
     * <p/>
     * Listeners that return <tt>true</tt> should ensure they don't do any long
     * running operations, to avoid performance issues.
     *
     * @return <tt>true</tt> if the listener should be notified, otherwise
     * <tt>false</tt>
     */
    boolean isFileListener();

}

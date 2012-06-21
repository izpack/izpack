package com.izforge.izpack.uninstaller.container;


import java.io.IOException;
import java.util.List;

import org.picocontainer.injectors.Provider;

import com.izforge.izpack.api.event.UninstallerListener;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.uninstaller.event.UninstallerListeners;


/**
 * A provider of {@link UninstallerListeners}.
 *
 * @author Tim Anderson
 */
public class UninstallerListenersProvider implements Provider
{

    /**
     * Provides an {@link UninstallerListeners} by reading the <em>uninstallerListeners</em> resource.
     *
     * @param resources used to locate the <em>uninstallerListeners</em> resource
     * @param factory   the factory to create the {@link UninstallerListener}s
     * @param prompt    the prompt
     * @return the listeners
     * @throws IOException               for any I/O error
     * @throws ClassNotFoundException    if a class of a serialized object cannot be found
     * @throws ResourceNotFoundException if <em>uninstallerListeners</em> cannot be found
     */
    @SuppressWarnings("unchecked")
    public UninstallerListeners provide(Resources resources, ObjectFactory factory, Prompt prompt)
            throws IOException, ClassNotFoundException
    {
        UninstallerListeners listeners = new UninstallerListeners(prompt);
        List<String> classNames = (List<String>) resources.getObject("uninstallerListeners");

        for (String className : classNames)
        {
            UninstallerListener listener = factory.create(className, UninstallerListener.class);
            listeners.add(listener);
        }
        listeners.initialise();
        return listeners;
    }

}

/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
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

package com.izforge.izpack.installer.automation;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.installer.panel.AbstractPanelView;
import com.izforge.izpack.installer.util.PanelHelper;


/**
 * Implementation of {@link AbstractPanelView} for {@link AutomatedPanelView}s.
 *
 * @author Tim Anderson
 */
public class AutomatedPanelView extends AbstractPanelView<PanelAutomation>
{
    /**
     * The handler.
     */
    private final AbstractUIHandler handler;


    /**
     * Constructs an {@code AutomatedPanelView}.
     *
     * @param panel       the panel
     * @param factory     the factory for creating the view
     * @param installData the installation data
     * @param handler     the handler
     */
    public AutomatedPanelView(Panel panel, ObjectFactory factory, InstallData installData,
                              AbstractUIHandler handler)
    {
        super(panel, PanelAutomation.class, factory, installData);
        this.handler = handler;
    }

    /**
     * Returns the PanelAutomation class corresponding to the panel's class name
     *
     * @return the corresponding {@link PanelAutomation} implementation class, or {@code null} if none is found
     */
    public Class<PanelAutomation> getViewClass()
    {
        Panel panel = getPanel();
        return PanelHelper.getAutomatedPanel(panel.getClassName());
    }

    /**
     * Creates a new view.
     *
     * @param panel     the panel to create the view for
     * @param viewClass the view base class
     * @return the new view
     */
    @Override
    protected PanelAutomation createView(Panel panel, Class<PanelAutomation> viewClass)
    {
        Class<PanelAutomation> impl = getViewClass();
        if (impl == null)
        {
            throw new IzPackException("Automation implementation not found for panel: " + panel.getClassName());
        }
        return getFactory().create(impl, panel);
    }

    /**
     * Returns a handler to prompt the user.
     *
     * @return the handler
     */
    @Override
    protected AbstractUIHandler getHandler()
    {
        return handler;
    }

    /**
     * Determines the behaviour when a warning is encountered during validation.
     *
     * @param message       the validation message. May be {@code null}
     * @param defaultAnswer the default response for warnings
     * @return {@code true} if the warning doesn't invalidate the panel; {@code false} if it does
     */
    @Override
    protected boolean isWarningValid(String message, boolean defaultAnswer)
    {
        if (defaultAnswer)
        {
            handler.emitNotification(message + " - ignoring");
        }
        else
        {
            handler.emitError(getMessage("data.validation.error.title"), message);
        }
        return defaultAnswer;
    }

    @Override
    public void createInstallationRecord(IXMLElement panelRoot)
    {
        //You don't create a auto.xml for an auto installation
    }
}

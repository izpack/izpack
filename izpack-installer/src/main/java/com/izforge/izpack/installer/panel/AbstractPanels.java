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
package com.izforge.izpack.installer.panel;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.IXMLWriter;
import com.izforge.izpack.api.adaptator.impl.XMLWriter;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.installer.data.UninstallData;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract implementation of the {@link PanelViews} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPanels<T extends AbstractPanelView<V>, V> implements PanelViews<T, V>
{

    /**
     * The panels.
     */
    private final List<Panel> panels;

    /**
     * The panel views.
     */
    private final List<T> panelViews;

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * The variables.
     */
    private final Variables variables;

    /**
     * The current panel index.
     */
    private int index = -1;

    /**
     * Determines if the next panel may be navigated to, if any.
     */
    private boolean nextEnabled;

    /**
     * Determines if the previous panel may be navigated to, if any.
     */
    private boolean previousEnabled;

    /**
     * The logger.
     */
    private static final Logger LOGGER = Logger.getLogger(AbstractPanels.class.getName());

    /**
     * Constructs an {@code AbstractPanels}.
     *
     * @param panels the panels
     * @param installData
     */
    public AbstractPanels(List<T> panels, InstallData installData)
    {
        this.panels = new ArrayList<Panel>();
        this.panelViews = panels;
        this.installData = installData;
        this.variables = installData.getVariables();
        nextEnabled = !panels.isEmpty();
        int index = 0;
        for (T panelView : panels)
        {
            panelView.setIndex(index++);
            this.panels.add(panelView.getPanel());
        }
    }

    /**
     * Returns the panels.
     *
     * @return the panels
     */
    @Override
    public List<Panel> getPanels()
    {
        return panels;
    }

    /**
     * Returns the current panel.
     *
     * @return the current panel, or {@code null} if there is no current panel
     */
    @Override
    public Panel getPanel()
    {
        return (index >= 0 && index < panels.size()) ? panels.get(index) : null;
    }

    /**
     * Returns the panel views.
     *
     * @return the panel views
     */
    @Override
    public List<T> getPanelViews()
    {
        return panelViews;
    }

    /**
     * Returns the current view.
     *
     * @return the current view, or {@code null} if there is none
     */
    @Override
    public V getView()
    {
        T panelView = getPanelView();
        return panelView != null ? panelView.getView() : null;
    }

    /**
     * Returns the current panel view.
     *
     * @return the current panel view, or {@code null} if there is none
     */
    @Override
    public T getPanelView()
    {
        return getPanelView(index);
    }

    /**
     * Determines if the current panel is valid.
     *
     * @return {@code true} if the current panel is valid
     */
    @Override
    public boolean isValid()
    {
        T panel = getPanelView();
        return panel == null || executeValidationActions(panel, true);
    }

    /**
     * Returns the current panel index.
     *
     * @return the current panel index, or {@code -1} if there is no current
     * panel
     */
    @Override
    public int getIndex()
    {
        return index;
    }

    /**
     * Determines if there is another panel after the current panel.
     *
     * @return {@code true} if there is another panel
     */
    @Override
    public boolean hasNext()
    {
        return getNext(index, false) != -1;
    }

    /**
     * Navigates to the next panel.
     * <br/>
     * Navigation can only occur if the current panel is valid.
     *
     * @return {@code true} if the next panel was navigated to
     */
    @Override
    public boolean next()
    {
        return next(true);
    }

    /**
     * Navigates to the next panel.
     *
     * @param validate if {@code true}, only move to the next panel if
     * validation succeeds
     * @return {@code true} if the next panel was navigated to
     */
    @Override
    public boolean next(boolean validate)
    {
        boolean result = false;

        // Evaluate and set variables for current panel before verifying panel conditions
        if (!validate || isValid())
        {
            int newIndex = getNext(index, false);
            if (newIndex != -1)
            {
                result = switchPanel(newIndex, validate);
            }
        }

        return result;
    }

    /**
     * Determines if there is panel prior to the current panel.
     *
     * @return {@code true} if there is a panel prior to the current panel
     */
    @Override
    public boolean hasPrevious()
    {
        return getPrevious(index, false) != -1;
    }

    /**
     * Navigates to the previous panel.
     *
     * @return {@code true} if the previous panel was navigated to
     */
    @Override
    public boolean previous()
    {
        return previous(index);
    }

    /**
     * Navigates to the panel before the specified index.
     * <br/>
     * The target panel must be before the current panel.
     *
     * @return {@code true} if the previous panel was navigated to
     */
    @Override
    public boolean previous(int index)
    {
        boolean result = false;
        if (hasPrevious())
        {
            int newIndex = getPrevious(index, true);
            if (newIndex != -1)
            {
                result = switchPanel(newIndex, false);
            }
        }
        return result;
    }

    /**
     * Determines if there is another panel after the specified index.
     *
     * @param index the panel index
     * @param visibleOnly if {@code true}, only examine visible panels
     * @return {@code true} if there is another panel
     */
    @Override
    public int getNext(int index, boolean visibleOnly)
    {
        int result = -1;
        List<T> panels = getPanelViews();
        for (int i = index + 1; i < panels.size(); ++i)
        {
            if (canShow(panels.get(i), visibleOnly))
            {
                result = i;
                break;
            }
        }

        return result;
    }

    /**
     * Determines if there is another panel after the current index.
     *
     * @param visibleOnly if {@code true}, only examine visible panels
     * @return {@code true} if there is another panel
     */
    @Override
    public int getNext(boolean visibleOnly)
    {
        return getNext(index, visibleOnly);
    }

    /**
     * Determines if there is another panel prior to the specified index.
     *
     * @param index the panel index
     * @param visibleOnly if {@code true}, only examine visible panels
     * @return the previous panel index, or {@code -1} if there are no more
     * panels
     */
    @Override
    public int getPrevious(int index, boolean visibleOnly)
    {
        int result = -1;
        for (int i = index - 1; i >= 0; --i)
        {
            if (canShow(getPanelView(i), visibleOnly))
            {
                result = i;
                break;
            }
        }
        return result;
    }

    /**
     * Determines if there is another panel prior to the specified index.
     *
     * @param visibleOnly if {@code true}, only examine visible panels
     * @return the previous panel index, or {@code -1} if there are no more
     * panels
     */
    @Override
    public int getPrevious(boolean visibleOnly)
    {
        return getPrevious(index, visibleOnly);
    }

    /**
     * Returns the index of a visible panel, relative to other visible panels.
     *
     * @param panel the panel
     * @return the panel's visible index, or {@code -1} if the panel is not
     * visible
     */
    @Override
    public int getVisibleIndex(T panel)
    {
        int result = -1;
        if (panel.isVisible())
        {
            for (int i = 0; i <= panel.getIndex() && i < panelViews.size(); ++i)
            {
                if (panelViews.get(i).isVisible())
                {
                    result++;
                }
            }
        }
        return result;
    }

    /**
     * Returns the number of visible panels.
     *
     * @return the number of visible panels
     */
    @Override
    public int getVisible()
    {
        int result = 0;
        for (T panelView : panelViews)
        {
            if (panelView.isVisible())
            {
                ++result;
            }
        }
        return result;
    }

    @Override
    public void writeInstallationRecord(File file, UninstallData uninstallData) throws Exception
    {
        FileOutputStream out = null;

        try
        {
            out = new FileOutputStream(file);
            IXMLWriter writer = new XMLWriter(out);
            IXMLElement panelsRoot = installData.getInstallationRecord();
            for (T panelView : panelViews)
            {
                Panel panel = panelView.getPanel();
                if (panel.isVisited())
                {
                    IXMLElement panelRoot = panelView.createPanelRootRecord(); //AbstractPanelView
                    panelView.createInstallationRecord(panelRoot);
                    panelsRoot.addChild(panelRoot);
                }
            }
            writer.write(panelsRoot);
        }
        finally
        {
            IOUtils.closeQuietly(out);

            //Only remove the automatic installation for if its placed in installation path
            //We would like to contain any removal of data within the install path
            if (file.getAbsolutePath().startsWith(installData.getInstallPath()))
            {
                uninstallData.addFile(file.getAbsolutePath(), true);
            }
        }
    }

    /**
     * TODO: validate parameter not used - refactor this Switches panels.
     *
     * @param newIndex the index of the new panel
     * @return {@code true} if the switch was successful
     */
    public boolean switchPanel(int newIndex, boolean validate)
    {
        boolean result;

        T panel = getPanelView();

        //Save data on every panel switch
        if (panel != null)
        {
            panel.saveData();
        }

        if ((newIndex > index) && !hasNext()) // NOTE: actions may change isNextEnabled() status
        {
            return false;
        }

        try
        {
            // refresh variables prior to switching panels
            variables.refresh();

            T oldPanelView = getPanelView(index);
            T newPanelView = getPanelView(newIndex);
            int oldIndex = index;
            index = newIndex;

            Panel newPanel = newPanelView.getPanel();

            newPanel.setVisited(true);

            if (switchPanel(newPanelView, oldPanelView))
            {

                if (oldIndex > newIndex)
                {
                    // Switches back in a sorted list of panels
                    // -> set unvisited all panels in order after this one to always keep history information up to date
                    // -> important for summary panel and generation of auto-install.xml
                    for (int i = panelViews.size() - 1; i > index; i--)
                    {
                        T futurePanelView = panelViews.get(i);
                        Panel futurePanel = futurePanelView.getPanel();
                        futurePanel.setVisited(false);
                        Set<String> blockedNames = futurePanel.getAffectedVariableNames();
                        variables.unregisterBlockedVariableNames(futurePanel.getAffectedVariableNames(), futurePanel);
                        if (LOGGER.isLoggable(Level.FINE))
                        {
                            LOGGER.fine("Unblocked variables on panel '" + futurePanel.getPanelId() + "': " + createListAsString(blockedNames));
                        }
                    }
                }
                else
                {
                    Set<String> blockedNames = newPanel.getAffectedVariableNames();
                    variables.registerBlockedVariableNames(blockedNames, newPanel);
                    if (LOGGER.isLoggable(Level.FINE))
                    {
                        LOGGER.fine("Blocked variables on panel '" + newPanel.getPanelId() + "': " + createListAsString(blockedNames));
                    }
                }

                LOGGER.fine("Switched panel index: " + oldIndex + " -> " + index);
                result = true;
            }
            else
            {
                index = oldIndex;
                result = false;
                newPanel.setVisited(false);
            }

            variables.refresh();
        }
        catch (IzPackException e)
        {
            result = false;
            getPanelView().getHandler().emitError(null, e.getMessage());
        }

        return result;
    }

    private String createListAsString(Set<String> list)
    {
        StringBuffer msg = new StringBuffer("{");
        if (list != null)
        {
            Iterator<String> it = list.iterator();
            while (it.hasNext())
            {
                if (LOGGER.isLoggable(Level.FINE))
                {
                    msg.append(it.next());
                    if (it.hasNext())
                    {
                        msg.append(", ");
                    }
                }
            }
        }
        msg.append("}");
        return msg.toString();
    }

    /**
     * Switches panels.
     *
     * @param newPanel the panel to switch to
     * @param oldPanel the panel to switch from, or {@code null} if there was no
     * prior panel
     * @return {@code true} if the switch was successful
     */
    protected abstract boolean switchPanel(T newPanel, T oldPanel);

    /**
     * Executes any pre and post-validation actions for a panel.
     *
     * @param panel the panel
     * @param validate if {@code true}, validate the panel after executing the
     * pre-validation actions
     * @return {@code true} if the panel is valid
     */
    protected boolean executeValidationActions(T panel, boolean validate)
    {
        boolean result;
        if (validate)
        {
            result = panel.isValid();
        }
        else
        {
            // execute the pre- and post-validation actions, but don't perform validation itself
            try
            {
                variables.refresh();
                panel.executePreValidationActions();
                panel.executePostValidationActions();
                panel.saveData();
                result = true;
            }
            catch (IzPackException e)
            {
                result = false;
                getPanelView().getHandler().emitError(null, e.getMessage());
            }
        }
        return result;
    }

    /**
     * Returns the panel view at the specified index.
     *
     * @param index the panel index
     * @return the corresponding panel, or {@code null} if there is no panel at
     * the index
     */
    private T getPanelView(int index)
    {
        List<T> panels = getPanelViews();
        return index >= 0 && index < panels.size() ? panels.get(index) : null;
    }

    /**
     * Determines if a panel can be shown.
     *
     * @param panel the panel
     * @param visibleOnly if {@code true}, only examine visible panels
     * @return {@code true} if the nominated panel can be shown
     */
    private boolean canShow(T panel, boolean visibleOnly)
    {
        return ((!visibleOnly || panel.isVisible()) && panel.canShow());
    }

}

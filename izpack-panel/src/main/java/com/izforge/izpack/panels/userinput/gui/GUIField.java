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

package com.izforge.izpack.panels.userinput.gui;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.gui.TwoColumnConstraints;
import com.izforge.izpack.panels.userinput.field.AbstractFieldView;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.util.HyperlinkHandler;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/**
 * GUI presentation of a field.
 *
 * @author Tim Anderson
 */
public abstract class GUIField extends AbstractFieldView
{

    /**
     * The components that are the view of the field.
     */
    private List<Component> components = new ArrayList<Component>();

    /**
     * The listener to notify of field updates.
     */
    private UpdateListener listener;


    /**
     * Constructs a {@code GUIField}.
     *
     * @param field the field
     */
    public GUIField(Field field)
    {
        super(field);
    }

    /**
     * Updates the field from the view.
     * <p/>
     * This implementation simply returns {@code true}.
     *
     * @param prompt the prompt to display messages
     * @return {@code true} if the field was updated, {@code false} if the view is invalid
     */
    public boolean updateField(Prompt prompt)
    {
        return updateField(prompt, false);
    }

    /**
     * Updates the field from the view.
     * <p/>
     * This implementation simply returns {@code true}.
     *
     * @param prompt the prompt to display messages
     * @return {@code true} if the field was updated, {@code false} if the view is invalid
     */
    public boolean updateField(Prompt prompt, boolean skipValidation)
    {
        return true;
    }

    /**
     * Updates the view from the field.
     * <p/>
     * This implementation simply returns {@code false}.
     *
     * @return {@code true} if the view was updated
     */
    public boolean updateView()
    {
        return false;
    }

    /**
     * Returns the components that make up the view.
     *
     * @return the components
     */
    public List<Component> getComponents()
    {
        return components;
    }

    /**
     * Registers a listener to be notified of field updates.
     *
     * @param listener the listener to notify
     */
    public void setUpdateListener(UpdateListener listener)
    {
        this.listener = listener;
    }

    /**
     * Adds the field.
     * <p/>
     * This adds the field description (if any), the field label, and field component.
     *
     * @param component the component
     */
    protected void addField(JComponent component)
    {
        addDescription();
        addLabel();
        addComponent(component);
    }

    /**
     * Adds the label for the field.
     */
    protected void addLabel()
    {
        addLabel(getField().getLabel());
    }

    /**
     * Adds a label.
     *
     * @param label the label
     */
    protected void addLabel(String label)
    {
        addComponent(new JLabel(label), new TwoColumnConstraints(TwoColumnConstraints.WEST));
    }

    /**
     * Adds a component.
     *
     * @param component   the component
     * @param constraints the component constraints
     */
    protected void addComponent(JComponent component, Object constraints)
    {
        components.add(new Component(component, constraints));
    }

    /**
     * Adds the tooltip to each component in this view.
     */
    protected void addTooltip()
    {
        String tooltipId = getField().getTooltip();

        if (tooltipId != null)
        {
            String tooltip = getInstallData().getMessages().get(tooltipId);

            for (Component component : components)
            {
                component.getComponent().setToolTipText(tooltip);
            }
        }
    }

    /**
     * Adds a field description to the list of UI elements.
     * <p/>
     * The description spans both columns.
     */
    protected void addDescription()
    {
        addText(getField().getDescription());
    }

    /**
     * Adds a static text field.
     * <p/>
     * The text spans both columns and may include html.
     *
     * @param text the text. May be {@code null}
     */
    protected void addText(String text)
    {
        if (text != null)
        {
            TwoColumnConstraints constraints = new TwoColumnConstraints(TwoColumnConstraints.BOTH);
            constraints.stretch = true;

            JTextPane label = new JTextPane();

            // Not editable, but still selectable.
            label.setEditable(false);
            label.setFocusable(false);

            // If html tags are present enable html rendering, otherwise the JTextPane
            // looks exactly like MultiLineLabel.
            if (text.startsWith("<html>") && text.endsWith("</html>"))
            {
                label.setContentType("text/html");
                label.addHyperlinkListener(new HyperlinkHandler());
            }
            label.setText(text);

            // Background color and font to match the label's.
            label.setBackground(UIManager.getColor("label.background"));
            label.setMargin(new Insets(3, 0, 3, 0));
            // workaround to cut out layout problems
            label.getPreferredSize();
            // end of workaround.

            addComponent(label, constraints);
        }
    }

    /**
     * Notifies any registered listener that the view has updated.
     */
    protected void notifyUpdateListener()
    {
        if (listener != null)
        {
            listener.updated();
        }
    }

    /**
     * Returns the installation data.
     *
     * @return the installation data
     */
    protected InstallData getInstallData()
    {
        return getField().getInstallData();
    }

    /**
     * Helper to replace variables in a string.
     *
     * @param value the string to perform variable replacement on. May be {@code null}
     * @return the string with any variables replaced with their values
     */
    protected String replaceVariables(String value)
    {
        return getInstallData().getVariables().replace(value);
    }

    /**
     * Show localized warning message dialog basing on given parameters.
     *
     * @param message the message to print out in dialog box.
     * @param prompt  the prompt to use
     */
    protected void warning(String message, Prompt prompt)
    {
        Messages messages = getInstallData().getMessages();
        prompt.warn(messages.get("UserInputPanel.error.caption"), message);
    }

    /**
     * Adds a component.
     *
     * @param component the component
     */
    private void addComponent(JComponent component)
    {
        addComponent(component, new TwoColumnConstraints(TwoColumnConstraints.EAST));
    }

}

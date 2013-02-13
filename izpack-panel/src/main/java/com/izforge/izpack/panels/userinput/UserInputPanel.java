/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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
package com.izforge.izpack.panels.userinput;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.gui.TwoColumnLayout;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.panels.userinput.field.ElementReader;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.FieldHelper;
import com.izforge.izpack.panels.userinput.field.FieldView;
import com.izforge.izpack.panels.userinput.field.UserInputPanelSpec;
import com.izforge.izpack.panels.userinput.gui.Component;
import com.izforge.izpack.panels.userinput.gui.GUIField;
import com.izforge.izpack.panels.userinput.gui.GUIFieldFactory;
import com.izforge.izpack.panels.userinput.gui.UpdateListener;
import com.izforge.izpack.util.PlatformModelMatcher;

/**
 * User input panel.
 *
 * @author Anthonin Bonnefoy
 */
public class UserInputPanel extends IzPanel
{
    private static final String TOPBUFFER = "topBuffer";

    /**
     * The parsed result from reading the XML specification from the file
     */
    private IXMLElement spec;

    private boolean eventsActivated = false;

    private Set<String> variables = new HashSet<String>();
    private List<GUIField> views = new ArrayList<GUIField>();

    private JPanel panel;

    private RulesEngine rules;

    /**
     * The factory for creating validators.
     */
    private final ObjectFactory factory;

    /**
     * The platform-model matcher.
     */
    private final PlatformModelMatcher matcher;

    /**
     * The prompt.
     */
    private final Prompt prompt;

    /**
     * The delegating prompt. This is used to switch between the above prompt and a no-op prompt when performing
     * updates.
     */
    private final DelegatingPrompt delegatingPrompt;

    private UserInputPanelSpec userInputModel;

    /*--------------------------------------------------------------------------*/
    // This method can be used to search for layout problems. If this class is
    // compiled with this method uncommented, the layout guides will be shown
    // on the panel, making it possible to see if all components are placed
    // correctly.
    /*--------------------------------------------------------------------------*/
    // public void paint (Graphics graphics)
    // {
    // super.paint (graphics);
    // layout.showRules ((Graphics2D)graphics, Color.red);
    // }
    /*--------------------------------------------------------------------------*/

    /**
     * Constructs an {@code UserInputPanel}.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent IzPack installer frame
     * @param installData the installation data
     * @param resources   the resources
     * @param rules       the rules engine
     * @param factory     factory
     * @param matcher     the platform-model matcher
     * @param prompt      the prompt
     */
    public UserInputPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
                          RulesEngine rules, ObjectFactory factory, PlatformModelMatcher matcher, Prompt prompt)
    {
        super(panel, parent, installData, resources);

        this.rules = rules;
        this.factory = factory;
        this.matcher = matcher;
        this.prompt = prompt;
        this.delegatingPrompt = new DelegatingPrompt(prompt);
    }

    /**
     * Indicates wether the panel has been validated or not. The installer won't let the user go
     * further through the installation process until the panel is validated.
     *
     * @return a boolean stating whether the panel has been validated or not.
     */
    @Override
    public boolean isValidated()
    {
        return readInput(prompt);
    }

    /**
     * This method is called when the panel becomes active.
     */
    @Override
    public void panelActivate()
    {
        this.init();

        if (spec == null)
        {
            // TODO: translate
            emitError("User input specification could not be found.",
                      "The specification for the user input panel could not be found. Please contact the packager.");
            parent.skipPanel();
        }
        else
        {
            // update UI with current values of associated variables
            updateUIElements();

            ElementReader reader = new ElementReader(userInputModel.getConfig());
            List<String> forPacks = reader.getPacks(spec);
            List<String> forUnselectedPacks = reader.getUnselectedPacks(spec);
            List<OsModel> forOs = reader.getOsModels(spec);

            if (!FieldHelper.isRequiredForPacks(forPacks, installData.getSelectedPacks())
                    || !FieldHelper.isRequiredForUnselectedPacks(forUnselectedPacks, installData.getSelectedPacks())
                    || !matcher.matchesCurrentPlatform(forOs))
            {
                parent.skipPanel();
            }
            else
            {
                buildUI();

                Dimension size = getMaximumSize();
                setSize(size.width, size.height);
                validate();
            }
        }
    }

    /**
     * Asks the panel to set its own XML installDataGUI that can be brought back for an automated installation
     * process. Use it as a blackbox if your panel needs to do something even in automated mode.
     *
     * @param panelRoot The XML root element of the panels blackbox tree.
     */
    @Override
    public void makeXMLData(IXMLElement panelRoot)
    {
        Map<String, String> entryMap = new HashMap<String, String>();

        for (String variable : variables)
        {
            entryMap.put(variable, installData.getVariable(variable));
        }
        for (FieldView view : views)
        {
            String variable = view.getField().getVariable();
            if (variable != null)
            {
                entryMap.put(variable, installData.getVariable(variable));
            }
        }

        new UserInputPanelAutomationHelper(entryMap).makeXMLData(installData, panelRoot);
    }

    private void init()
    {
        eventsActivated = false;
        TwoColumnLayout layout;
        super.removeAll();
        views.clear();

        // ----------------------------------------------------
        // read the specifications
        // ----------------------------------------------------
        if (spec == null)
        {
            spec = readSpec();
        }

        // ----------------------------------------------------
        // Set the topBuffer from the attribute. topBuffer=0 is useful
        // if you don't want your panel to be moved up and down during
        // dynamic validation (showing and hiding components within the
        // same panel)
        // ----------------------------------------------------
        int topbuff = 25;
        try
        {
            topbuff = Integer.parseInt(spec.getAttribute(TOPBUFFER));
        }
        catch (Exception ignore)
        {
            // do nothing
        }
        finally
        {
            layout = new TwoColumnLayout(10, 5, 30, topbuff, TwoColumnLayout.LEFT);
        }
        setLayout(new BorderLayout());

        panel = new JPanel();
        panel.setLayout(layout);

        if (spec == null)
        {
            // return if we could not read the spec. further
            // processing will only lead to problems. In this
            // case we must skip the panel when it gets activated.
            return;
        }

        // refresh variables specified in spec
        updateVariables();

        // ----------------------------------------------------
        // process all field nodes. Each field node is analyzed
        // for its type, then an appropriate member function
        // is called that will create the correct UI elements.
        // ----------------------------------------------------
        GUIFieldFactory viewFactory = new GUIFieldFactory(installData, this, parent, delegatingPrompt);
        UpdateListener listener = new UpdateListener()
        {
            @Override
            public void updated()
            {
                updateDialog();
            }
        };

        List<Field> fields = userInputModel.createFields(spec);
        for (Field field : fields)
        {
            GUIField view = viewFactory.create(field);
            view.setUpdateListener(listener);
            views.add(view);
        }
        eventsActivated = true;
    }

    protected void updateUIElements()
    {
        boolean updated = false;

        for (GUIField view : views)
        {
            updated |= view.updateView();
        }
        if (updated)
        {
            super.invalidate();
        }
    }

    /**
     * Builds the UI and makes it ready for display
     */
    private void buildUI()
    {
        for (GUIField view : views)
        {
            Field field = view.getField();
            if (FieldHelper.isRequired(field, installData, matcher))
            {
                if (!view.isDisplayed())
                {
                    view.setDisplayed(true);
                    for (Component components : view.getComponents())
                    {
                        panel.add(components.getComponent(), components.getConstraints());
                    }
                }
            }
            else
            {
                if (view.isDisplayed())
                {
                    view.setDisplayed(false);
                    for (Component element : view.getComponents())
                    {
                        panel.remove(element.getComponent());
                    }
                }
            }
        }

        JScrollPane scroller = new JScrollPane(panel);
        Border emptyBorder = BorderFactory.createEmptyBorder();
        scroller.setBorder(emptyBorder);
        scroller.setViewportBorder(emptyBorder);
        scroller.getVerticalScrollBar().setBorder(emptyBorder);
        scroller.getHorizontalScrollBar().setBorder(emptyBorder);
        add(scroller, BorderLayout.CENTER);
    }

    /**
     * Reads the input installDataGUI from all UI elements and sets the associated variables.
     *
     * @param prompt the prompt to display messages
     * @return {@code true} if the operation is successful, otherwise {@code false}.
     */
    private boolean readInput(Prompt prompt)
    {
        delegatingPrompt.setPrompt(prompt);

        for (GUIField view : views)
        {
            if (view.isDisplayed())
            {
                if (!view.updateField(prompt))
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Reads the XML specification for the panel layout.
     *
     * @return the panel specification
     * @throws IzPackException for any problems in reading the specification
     */
    private IXMLElement readSpec()
    {
        userInputModel = new UserInputPanelSpec(getResources(), installData, factory, rules, matcher);
        return userInputModel.getPanelSpec(getMetadata());
    }

    protected void updateVariables()
    {
        variables = userInputModel.updateVariables(spec);
    }

    private void updateDialog()
    {
        if (this.eventsActivated)
        {
            this.eventsActivated = false;
            if (readInput(LoggingPrompt.INSTANCE)) // read from the input fields, but don't display a prompt for errors
            {
                // read input
                // and update elements
                init();
                updateVariables();
                updateUIElements();
                buildUI();
                validate();
                repaint();
            }
            this.eventsActivated = true;
        }
    }

}

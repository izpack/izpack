/*
 * IzPack - Copyright 2001-2009 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Elmar Grom
 * Copyright 2009 Dennis Reil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.izforge.izpack.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.Pack;
import com.izforge.izpack.Panel;
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.IXMLParser;
import com.izforge.izpack.adaptator.impl.XMLParser;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.TwoColumnConstraints;
import com.izforge.izpack.gui.TwoColumnLayout;
import com.izforge.izpack.installer.*;
import com.izforge.izpack.rules.RulesEngine;
import com.izforge.izpack.rules.VariableExistenceCondition;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.OsConstraint;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.VariableSubstitutor;
import com.izforge.izpack.util.HyperlinkHandler;

public class UserInputPanel extends IzPanel implements ActionListener, ItemListener, FocusListener
{

    /**
     *
     */
    private static final long serialVersionUID = 3257850965439886129L;

    protected static final String ICON_KEY = "icon";

    /**
     * The name of the XML file that specifies the panel layout
     */
    private static final String SPEC_FILE_NAME = "userInputSpec.xml";

    private static final String LANG_FILE_NAME = "userInputLang.xml";

    /**
     * how the spec node for a specific panel is identified
     */
    private static final String NODE_ID = "panel";

    private static final String FIELD_NODE_ID = "field";

    private static final String INSTANCE_IDENTIFIER = "order";

    protected static final String PANEL_IDENTIFIER = "id";

    private static final String TYPE = "type";

    private static final String DESCRIPTION = "description";

    private static final String VARIABLE = "variable";

    protected static final String TEXT = "txt";

    protected static final String KEY = "id";

    private static final String SPEC = "spec";

    private static final String SET = "set";

    private static final String REVALIDATE = "revalidate";

    private static final String TOPBUFFER = "topBuffer";

    private static final String TRUE = "true";

    private static final String FALSE = "false";

    private static final String ALIGNMENT = "align";

    private static final String LEFT = "left";

    private static final String CENTER = "center";

    private static final String RIGHT = "right";

    private static final String TOP = "top";

    private static final String ITALICS = "italic";

    private static final String BOLD = "bold";

    private static final String SIZE = "size";

    protected static final String VALIDATOR = "validator";

    private static final String PROCESSOR = "processor";

    protected static final String CLASS = "class";

    private static final String TITLE_FIELD = "title";

    private static final String TEXT_FIELD = "text";

    private static final String TEXT_SIZE = "size";

    private static final String TEXT_ROWS = "rows";

    private static final String STATIC_TEXT = "staticText";

    private static final String COMBO_FIELD = "combo";

    private static final String COMBO_CHOICE = "choice";

    private static final String COMBO_VALUE = "value";

    private static final String RADIO_FIELD = "radio";

    private static final String RADIO_CHOICE = "choice";

    private static final String RADIO_VALUE = "value";

    private static final String SPACE_FIELD = "space";

    private static final String DIVIDER_FIELD = "divider";

    private static final String CHECK_FIELD = "check";

    private static final String RULE_FIELD = "rule";

    private static final String RULE_LAYOUT = "layout";

    private static final String RULE_SEPARATOR = "separator";

    private static final String RULE_RESULT_FORMAT = "resultFormat";

    private static final String RULE_PLAIN_STRING = "plainString";

    private static final String RULE_DISPLAY_FORMAT = "displayFormat";

    private static final String RULE_SPECIAL_SEPARATOR = "specialSeparator";

    private static final String RULE_ENCRYPTED = "processed";

    protected static final String RULE_PARAM_NAME = "name";

    protected static final String RULE_PARAM_VALUE = "value";

    protected static final String RULE_PARAM = "param";

    private static final String PWD_FIELD = "password";

    private static final String PWD_INPUT = "pwd";

    private static final String PWD_SIZE = "size";

    private static final String SEARCH_FIELD = "search";

    private static final String FILE_FIELD = "file";

    private static final String DIR_FIELD = "dir";

    private static final String SEARCH_CHOICE = "choice";

    private static final String SEARCH_FILENAME = "filename";

    private static final String SEARCH_RESULT = "result";

    private static final String SEARCH_VALUE = "value";

    private static final String SEARCH_TYPE = "type";

    private static final String SEARCH_FILE = "file";

    private static final String SEARCH_DIRECTORY = "directory";

    private static final String SEARCH_PARENTDIR = "parentdir";

    private static final String SEARCH_CHECKFILENAME = "checkfilename";

    private static final String SELECTEDPACKS = "createForPack"; // renamed

    private static final String UNSELECTEDPACKS = "createForUnselectedPack"; // new

    protected static final String ATTRIBUTE_CONDITIONID_NAME = "conditionid";

    protected static final String VARIABLE_NODE = "variable";

    protected static final String ATTRIBUTE_VARIABLE_NAME = "name";

    protected static final String ATTRIBUTE_VARIABLE_VALUE = "value";

    // node

    private static final String NAME = "name";

    private static final String OS = "os";

    private static final String FAMILY = "family";

    private static final String MULTIPLE_FILE_FIELD = "multiFile";

    // ------------------------------------------------------------------------
    // Variable Declarations
    // ------------------------------------------------------------------------
    private static int instanceCount = 0;

    protected int instanceNumber = 0;

    /**
     * If there is a possibility that some UI elements will not get added we can not allow to go
     * back to the PacksPanel, because the process of building the UI is not reversable. This
     * variable keeps track if any packs have been defined and will be used to make a decision for
     * locking the 'previous' button.
     */
    private boolean packsDefined = false;

    private InstallerFrame parentFrame;

    /**
     * The parsed result from reading the XML specification from the file
     */
    private IXMLElement spec;

    private boolean haveSpec = false;

    /**
     * Holds the references to all of the UI elements
     */
    // private Vector<Object[]> uiElements = new Vector<Object[]>();
    /**
     * Holds the references to all radio button groups
     */
    private Vector<ButtonGroup> buttonGroups = new Vector<ButtonGroup>();

    /**
     * Holds the references to all password field groups
     */
    private Vector<PasswordGroup> passwordGroups = new Vector<PasswordGroup>();

    /**
     * used for temporary storage of references to password groups that have already been read in a
     * given read cycle.
     */
    private Vector passwordGroupsRead = new Vector();

    /**
     * Used to track search fields. Contains SearchField references.
     */
    private Vector<SearchField> searchFields = new Vector<SearchField>();

    /**
     * Holds all user inputs for use in automated installation
     */
    private Vector<TextValuePair> entries = new Vector<TextValuePair>();

    private LocaleDatabase langpack = null;

    // Used for dynamic controls to skip content validation unless the user
    // really clicks "Next"
    private boolean validating = true;

    private boolean eventsActivated = false;

    private Vector<UIElement> elements = new Vector<UIElement>();

    private JPanel panel;

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
     * Constructs a <code>UserInputPanel</code>.
     *
     * @param parent reference to the application frame
     * @param installData shared information about the installation
     */
    /*--------------------------------------------------------------------------*/
    public UserInputPanel(InstallerFrame parent, InstallData installData)
    {
        super(parent, installData);
        instanceNumber = instanceCount++;
        this.parentFrame = parent;
    }

    private void createBuiltInVariableConditions(String variable)
    {
        if (variable != null)
        {
            VariableExistenceCondition variableCondition = new VariableExistenceCondition();
            variableCondition.setId("izpack.input." + variable);
            variableCondition.setInstalldata(idata);
            variableCondition.setVariable(variable);
            parent.getRules().addCondition(variableCondition);
        }
    }

    protected void init()
    {
        eventsActivated = false;
        TwoColumnLayout layout;
        super.removeAll();
        elements.clear();

        // ----------------------------------------------------
        // get a locale database
        // ----------------------------------------------------
        try
        {
            this.langpack = (LocaleDatabase) parent.langpack.clone();

            String resource = LANG_FILE_NAME + "_" + idata.localeISO3;
            this.langpack.add(ResourceManager.getInstance().getInputStream(resource));
        }
        catch (ResourceNotFoundException e)
        {
            Debug.trace(e);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        // ----------------------------------------------------
        // read the specifications
        // ----------------------------------------------------
        try
        {
            readSpec();
        }
        catch (Throwable exception)
        {
            // log the problem
            exception.printStackTrace();
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
        catch (Exception ex)
        {}
        finally
        {
            layout = new TwoColumnLayout(10, 5, 30, topbuff, TwoColumnLayout.LEFT);
        }
        setLayout(new BorderLayout());

        panel = new JPanel();
        panel.setLayout(layout);

        if (!haveSpec)
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
        // for its type, then an appropriate memeber function
        // is called that will create the correct UI elements.
        // ----------------------------------------------------
        Vector<IXMLElement> fields = spec.getChildrenNamed(FIELD_NODE_ID);

        for (int i = 0; i < fields.size(); i++)
        {
            IXMLElement field = fields.elementAt(i);
            String attribute = field.getAttribute(TYPE);
            String associatedVariable = field.getAttribute(VARIABLE);
            if (associatedVariable != null)
            {
                // create automatic existence condition
                createBuiltInVariableConditions(associatedVariable);
            }

            String conditionid = field.getAttribute(ATTRIBUTE_CONDITIONID_NAME);
            if (conditionid != null)
            {
                // check if condition is fulfilled
                if (!this.parent.getRules().isConditionTrue(conditionid, idata.getVariables()))
                {
                    continue;
                }
            }
            if (attribute != null)
            {
                if (attribute.equals(RULE_FIELD))
                {
                    addRuleField(field);
                }
                else if (attribute.equals(TEXT_FIELD))
                {
                    addTextField(field);
                }
                else if (attribute.equals(COMBO_FIELD))
                {
                    addComboBox(field);
                }
                else if (attribute.equals(RADIO_FIELD))
                {
                    addRadioButton(field);
                }
                else if (attribute.equals(PWD_FIELD))
                {
                    addPasswordField(field);
                }
                else if (attribute.equals(SPACE_FIELD))
                {
                    addSpace(field);
                }
                else if (attribute.equals(DIVIDER_FIELD))
                {
                    addDivider(field);
                }
                else if (attribute.equals(CHECK_FIELD))
                {
                    addCheckBox(field);
                }
                else if (attribute.equals(STATIC_TEXT))
                {
                    addText(field);
                }
                else if (attribute.equals(TITLE_FIELD))
                {
                    addTitle(field);
                }
                else if (attribute.equals(SEARCH_FIELD))
                {
                    addSearch(field);
                }
                else if (attribute.equals(MULTIPLE_FILE_FIELD))
                {
                    addMultipleFileField(field);
                }
                else if (attribute.equals(FILE_FIELD))
                {
                    addFileField(field);
                }
                else if (attribute.equals(DIR_FIELD))
                {
                    addDirectoryField(field);
                }
            }
        }
        eventsActivated = true;
    }

    private List<ValidatorContainer> analyzeValidator(IXMLElement specElement)
    {
        List<ValidatorContainer> result = null;

        // ----------------------------------------------------
        // get the validator and processor if they are defined
        // ----------------------------------------------------

        Vector<IXMLElement> validatorsElem = specElement.getChildrenNamed(VALIDATOR);
        if (validatorsElem != null && validatorsElem.size() > 0)
        {
            int vsize = validatorsElem.size();

            result = new ArrayList<ValidatorContainer>(vsize);

            for (int i = 0; i < vsize; i++)
            {
                IXMLElement element = validatorsElem.get(i);
                String validator = element.getAttribute(CLASS);
                String message = getText(element);
                HashMap<String, String> validateParamMap = new HashMap<String, String>();
                // ----------------------------------------------------------
                // check and see if we have any parameters for this validator.
                // If so, then add them to validateParamMap.
                // ----------------------------------------------------------
                Vector<IXMLElement> validateParams = element.getChildrenNamed(RULE_PARAM);
                if (validateParams != null && validateParams.size() > 0)
                {
                    Iterator<IXMLElement> iter = validateParams.iterator();
                    while (iter.hasNext())
                    {
                        element = iter.next();
                        String paramName = element.getAttribute(RULE_PARAM_NAME);
                        String paramValue = element.getAttribute(RULE_PARAM_VALUE);

                        validateParamMap.put(paramName, paramValue);
                    }
                }
                result.add(new ValidatorContainer(validator, message, validateParamMap));
            }
        }
        return result;
    }

    private void addDirectoryField(IXMLElement field)
    {
        Vector<IXMLElement> forPacks = field.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs = field.getChildrenNamed(OS);

        JLabel label;
        String set;
        int size;

        String variable = field.getAttribute(VARIABLE);
        if ((variable == null) || (variable.length() == 0)) { return; }

        boolean allowEmptyValue = false;
        boolean mustExist = true, create = true;

        List<ValidatorContainer> validatorConfig;
        IXMLElement element = field.getFirstChildNamed(SPEC);
        if (element == null)
        {
            Debug.trace("Error: no spec element defined in file field");
            return;
        }
        else
        {
            label = new JLabel(getText(element));
            // ----------------------------------------------------
            // extract the specification details
            // ----------------------------------------------------
            set = element.getAttribute(SET);
            if (set == null)
            {
                set = idata.getVariable(variable);
                if (set == null)
                {
                    set = "";
                }
            }
            else
            {
                if (set != null && !"".equals(set))
                {
                    VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
                    set = vs.substitute(set, null);

                    idata.setVariable(variable, set);
                }
            }

            try
            {
                size = Integer.parseInt(element.getAttribute(TEXT_SIZE));
            }
            catch (Throwable exception)
            {
                size = 1;
            }

            allowEmptyValue = Boolean
                    .parseBoolean(element.getAttribute("allowEmptyValue", "false"));

            mustExist = Boolean.parseBoolean(element.getAttribute("mustExist", "true"));
            create = Boolean.parseBoolean(element.getAttribute("create", "false"));
        }
        validatorConfig = analyzeValidator(field);

        TwoColumnConstraints constraints = new TwoColumnConstraints();
        constraints.position = TwoColumnConstraints.WEST;

        UIElement labelUiElement = new UIElement();
        labelUiElement.setType(UIElementType.LABEL);
        labelUiElement.setConstraints(constraints);
        labelUiElement.setComponent(label);
        labelUiElement.setForPacks(forPacks);
        labelUiElement.setForOs(forOs);
        elements.add(labelUiElement);

        TwoColumnConstraints constraints2 = new TwoColumnConstraints();
        constraints2.position = TwoColumnConstraints.EAST;

        FileInputField fileInput = new DirInputField(this, idata, true, set, size,
                validatorConfig, mustExist, create);
        
        fileInput.setAllowEmptyInput(allowEmptyValue);

        UIElement dirUiElement = new UIElement();
        dirUiElement.setType(UIElementType.DIRECTORY);
        dirUiElement.setConstraints(constraints2);
        dirUiElement.setComponent(fileInput);
        dirUiElement.setForPacks(forPacks);
        dirUiElement.setForOs(forOs);
        dirUiElement.setAssociatedVariable(variable);
        elements.add(dirUiElement);
    }

    private void addMultipleFileField(IXMLElement field)
    {
        Vector<IXMLElement> forPacks = field.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs = field.getChildrenNamed(OS);

        String labelText;
        String set;
        int size;

        String filter = null;
        String filterdesc = null;

        String variable = field.getAttribute(VARIABLE);
        if ((variable == null) || (variable.length() == 0)) { return; }

        boolean allowEmptyValue = false;
        boolean createMultipleVariables = false;

        int preferredX = 200;
        int preferredY = 200;
        int visibleRows = 10;

        IXMLElement element = field.getFirstChildNamed(SPEC);
        if (element == null)
        {
            Debug.trace("Error: no spec element defined in multi file field");
            return;
        }
        else
        {
            labelText = getText(element);
            // ----------------------------------------------------
            // extract the specification details
            // ----------------------------------------------------
            set = element.getAttribute(SET);
            if (set == null)
            {
                set = idata.getVariable(variable);
                if (set == null)
                {
                    set = "";
                }
            }
            else
            {
                if (set != null && !"".equals(set))
                {
                    VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
                    set = vs.substitute(set, null);
                }
            }

            try
            {
                size = Integer.parseInt(element.getAttribute(TEXT_SIZE));
            }
            catch (Throwable exception)
            {
                size = 1;
            }

            filter = element.getAttribute("fileext");
            if (filter == null)
            {
                filter = "";
            }
            filterdesc = element.getAttribute("fileextdesc");
            if (filterdesc == null)
            {
                filterdesc = "";
            }
            // internationalize it
            filterdesc = this.langpack.getString(filterdesc);

            String visRows = element.getAttribute("visibleRows");
            if (visRows != null)
            {
                try
                {
                    visibleRows = Integer.parseInt(visRows);
                }
                catch (Exception e)
                {
                    Debug.error("Illegal value for visibleRows found.");
                }
            }

            String prefX = element.getAttribute("prefX");
            if (prefX != null)
            {
                try
                {
                    preferredX = Integer.parseInt(prefX);
                }
                catch (Exception e)
                {
                    Debug.error("Illegal value for prefX found.");
                }
            }
            String prefY = element.getAttribute("prefY");
            if (prefY != null)
            {
                try
                {
                    preferredY = Integer.parseInt(prefY);
                }
                catch (Exception e)
                {
                    Debug.error("Illegal value for prefY found.");
                }
            }

            createMultipleVariables = Boolean.parseBoolean(element.getAttribute(
                    "multipleVariables", "false"));
            allowEmptyValue = Boolean
                    .parseBoolean(element.getAttribute("allowEmptyValue", "false"));
        }

        List<ValidatorContainer> validatorConfig = analyzeValidator(field);

        TwoColumnConstraints constraints2 = new TwoColumnConstraints();
        constraints2.position = TwoColumnConstraints.EAST;

        MultipleFileInputField fileInputField = new MultipleFileInputField(parentFrame, idata,
                false, set, size, validatorConfig, filter, filterdesc, createMultipleVariables,
                visibleRows, preferredX, preferredY, labelText);
        fileInputField.setAllowEmptyInput(allowEmptyValue);

        UIElement fileUiElement = new UIElement();
        fileUiElement.setType(UIElementType.MULTIPLE_FILE);
        fileUiElement.setConstraints(constraints2);
        fileUiElement.setComponent(fileInputField);
        fileUiElement.setForPacks(forPacks);
        fileUiElement.setForOs(forOs);
        fileUiElement.setAssociatedVariable(variable);
        elements.add(fileUiElement);
    }

    private void addFileField(IXMLElement field)
    {
        Vector<IXMLElement> forPacks = field.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs = field.getChildrenNamed(OS);

        JLabel label;
        String set;
        int size;

        String filter = null;
        String filterdesc = null;

        String variable = field.getAttribute(VARIABLE);
        if ((variable == null) || (variable.length() == 0)) { return; }

        boolean allowEmptyValue = false;

        IXMLElement element = field.getFirstChildNamed(SPEC);
        if (element == null)
        {
            Debug.trace("Error: no spec element defined in file field");
            return;
        }
        else
        {
            label = new JLabel(getText(element));
            // ----------------------------------------------------
            // extract the specification details
            // ----------------------------------------------------
            set = element.getAttribute(SET);
            if (set == null)
            {
                set = idata.getVariable(variable);
                if (set == null)
                {
                    set = "";
                }
            }
            else
            {
                if (set != null && !"".equals(set))
                {
                    VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
                    set = vs.substitute(set, null);
                    idata.setVariable(variable, set);
                }
            }

            try
            {
                size = Integer.parseInt(element.getAttribute(TEXT_SIZE));
            }
            catch (Throwable exception)
            {
                size = 1;
            }

            filter = element.getAttribute("fileext");
            if (filter == null)
            {
                filter = "";
            }
            filterdesc = element.getAttribute("fileextdesc");
            if (filterdesc == null)
            {
                filterdesc = "";
            }
            // internationalize it
            filterdesc = idata.langpack.getString(filterdesc);

            allowEmptyValue = Boolean
                    .parseBoolean(element.getAttribute("allowEmptyValue", "false"));
        }

        List<ValidatorContainer> validatorConfig = analyzeValidator(field);

        TwoColumnConstraints constraints = new TwoColumnConstraints();
        constraints.position = TwoColumnConstraints.WEST;

        UIElement labelUiElement = new UIElement();
        labelUiElement.setType(UIElementType.LABEL);
        labelUiElement.setConstraints(constraints);
        labelUiElement.setComponent(label);
        labelUiElement.setForPacks(forPacks);
        labelUiElement.setForOs(forOs);
        elements.add(labelUiElement);

        TwoColumnConstraints constraints2 = new TwoColumnConstraints();
        constraints2.position = TwoColumnConstraints.EAST;

        FileInputField fileInputField = new FileInputField(this, idata, false, set, size,
                validatorConfig, filter, filterdesc);

        fileInputField.setAllowEmptyInput(allowEmptyValue);

        UIElement fileUiElement = new UIElement();
        fileUiElement.setType(UIElementType.FILE);
        fileUiElement.setConstraints(constraints2);
        fileUiElement.setComponent(fileInputField);
        fileUiElement.setForPacks(forPacks);
        fileUiElement.setForOs(forOs);
        fileUiElement.setAssociatedVariable(variable);
        elements.add(fileUiElement);
    }

    protected void updateUIElements()
    {
        boolean updated = false;
        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());

        for (UIElement element : elements)
        {
            if (element.hasVariableAssignment())
            {
                String variable = element.getAssociatedVariable();
                String value = idata.getVariable(variable);

                Debug.trace("updateUIElements() variable=" + variable + " value=" + value + "\n");
                if (element.getType() == UIElementType.RADIOBUTTON)
                {
                    // we have a radio field, which should be updated
                    JRadioButton choice = (JRadioButton) element.getComponent();
                    if (value == null)
                    {
                        continue;
                    }
                    if (value.equals(element.getTrueValue()))
                    {
                        choice.setSelected(true);
                    }
                    else
                    {
                        choice.setSelected(false);
                    }
                }
                else if (element.getType() == UIElementType.TEXT)
                {
                    // update TextField
                    TextInputField textf = (TextInputField) element.getComponent();

                    if (value == null)
                    {
                        value = textf.getText();
                    }
                    textf.setText(vs.substitute(value, null));
                }
                else if (element.getType() == UIElementType.PASSWORD)
                {
                    // update PasswordField
                    JTextComponent textf = (JTextComponent) element.getComponent();

                    if (value == null)
                    {
                        value = textf.getText();
                    }
                    textf.setText(vs.substitute(value, null));
                }
                else if (element.getType() == UIElementType.RULE)
                {

                    RuleInputField rulef = (RuleInputField) element.getComponent();
                    if (value == null)
                    {
                        value = rulef.getText();
                    }
                }
                else if (element.getType() == UIElementType.MULTIPLE_FILE)
                {
                    MultipleFileInputField multifile = (MultipleFileInputField) element
                            .getComponent();
                    if (value != null)
                    {
                        multifile.clearFiles();
                        if (multifile.isCreateMultipleVariables())
                        {
                            multifile.addFile(value);
                            // try to read more files
                            String basevariable = element.getAssociatedVariable();
                            int index = 1;

                            while (value != null)
                            {
                                StringBuffer builder = new StringBuffer(basevariable);
                                builder.append("_");
                                builder.append(index++);
                                value = idata.getVariable(builder.toString());
                                if (value != null)
                                {
                                    multifile.addFile(value);
                                }
                            }
                        }
                        else
                        {
                            // split file string
                            String[] files = value.split(";");
                            for (String file : files)
                            {
                                multifile.addFile(file);
                            }
                        }
                    }
                }
                else if (element.getType() == UIElementType.FILE)
                {
                    FileInputField fileInput = (FileInputField) element.getComponent();
                    if (value != null)
                    {
                        fileInput.setFile(value);
                    }
                }

                else if (element.getType() == UIElementType.DIRECTORY)
                {
                    FileInputField fileInput = (FileInputField) element.getComponent();
                    if (value != null)
                    {
                        fileInput.setFile(value);
                    }
                }
                updated = true;
            }
        }

        if (updated)
        {
            super.invalidate();
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Indicates wether the panel has been validated or not. The installer won't let the user go
     * further through the installation process until the panel is validated. Default behavior is to
     * return true.
     *
     * @return A boolean stating wether the panel has been validated or not.
     */
    /*--------------------------------------------------------------------------*/
    public boolean isValidated()
    {
        return readInput();
    }

    /*--------------------------------------------------------------------------*/
    /**
     * This method is called when the panel becomes active.
     */
    /*--------------------------------------------------------------------------*/
    public void panelActivate()
    {
        this.init();

        if (spec == null)
        {
            // TODO: translate
            emitError("User input specification could not be found.",
                    "The specification for the user input panel could not be found. Please contact the packager.");
            parentFrame.skipPanel();
        }
        // update UI with current values of associated variables
        updateUIElements();
        Vector<IXMLElement> forPacks = spec.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forUnselectedPacks = spec.getChildrenNamed(UNSELECTEDPACKS);
        Vector<IXMLElement> forOs = spec.getChildrenNamed(OS);

        if (!itemRequiredFor(forPacks) || !itemRequiredForUnselected(forUnselectedPacks)
                || !itemRequiredForOs(forOs))
        {
            parentFrame.skipPanel();
            return;
        }
        if (!haveSpec)
        {
            parentFrame.skipPanel();
            return;
        }

        buildUI();

        this.setSize(this.getMaximumSize().width, this.getMaximumSize().height);
        validate();
        if (packsDefined)
        {
            parentFrame.lockPrevButton();
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Asks the panel to set its own XML data that can be brought back for an automated installation
     * process. Use it as a blackbox if your panel needs to do something even in automated mode.
     *
     * @param panelRoot The XML root element of the panels blackbox tree.
     */
    /*--------------------------------------------------------------------------*/
    public void makeXMLData(IXMLElement panelRoot)
    {
        Map<String, String> entryMap = new HashMap<String, String>();

        for (int i = 0; i < entries.size(); i++)
        {
            TextValuePair pair = entries.elementAt(i);
            // IZPACK-283: read the value from idata instead of panel data
            final String key = pair.toString();
            entryMap.put(key, idata.getVariable(key));
        }

        new UserInputPanelAutomationHelper(entryMap).makeXMLData(idata, panelRoot);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Builds the UI and makes it ready for display
     */
    /*--------------------------------------------------------------------------*/
    private void buildUI()
    {
        for (UIElement element : elements)
        {
            if (itemRequiredFor(element.getForPacks()) && itemRequiredForOs(element.getForOs()))
            {
                if (!element.isDisplayed())
                {
                    element.setDisplayed(true);
                    panel.add(element.getComponent(), element.getConstraints());
                }
            }
            else
            {
                if (element.isDisplayed())
                {
                    element.setDisplayed(false);
                    panel.remove(element.getComponent());
                }
            }
        }

        JScrollPane scroller = new JScrollPane(panel);
        Border emptyBorder = BorderFactory.createEmptyBorder();
        scroller.setViewportBorder(emptyBorder);
        scroller.getVerticalScrollBar().setBorder(emptyBorder);
        scroller.getHorizontalScrollBar().setBorder(emptyBorder);
        add(scroller, BorderLayout.CENTER);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Reads the input data from all UI elements and sets the associated variables.
     *
     * @return <code>true</code> if the operation is successdul, otherwise <code>false</code>.
     */
    /*--------------------------------------------------------------------------*/
    private boolean readInput()
    {
        boolean success = true;

        passwordGroupsRead.clear();

        for (UIElement element : elements)
        {
            if (element.isDisplayed())
            {
                if (element.getType() == UIElementType.RULE)
                {
                    success = readRuleField(element);
                }
                else if (element.getType() == UIElementType.PASSWORD)
                {
                    success = readPasswordField(element);
                }
                else if (element.getType() == UIElementType.TEXT)
                {
                    success = readTextField(element);
                }
                else if (element.getType() == UIElementType.COMBOBOX)
                {
                    success = readComboBox(element);
                }
                else if (element.getType() == UIElementType.RADIOBUTTON)
                {
                    success = readRadioButton(element);
                }
                else if (element.getType() == UIElementType.CHECKBOX)
                {
                    success = readCheckBox(element);
                }
                else if (element.getType() == UIElementType.SEARCH)
                {
                    success = readSearch(element);
                }
                else if (element.getType() == UIElementType.MULTIPLE_FILE)
                {
                    success = readMultipleFileField(element);
                }
                else if (element.getType() == UIElementType.FILE)
                {
                    success = readFileField(element);
                }
                else if (element.getType() == UIElementType.DIRECTORY)
                {
                    success = readDirectoryField(element);
                }
                if (!success) { return (false); }
            }
        }
        return (true);
    }

    private boolean readDirectoryField(UIElement field)
    {
        boolean result = false;
        try
        {
            FileInputField input = (FileInputField) field.getComponent();
            result = !validating || input.validateField();
            if (result)
            {
                String value = "";
                File inputFile = input.getSelectedFile();
                if(inputFile != null && !inputFile.getName().equals("")){
                    value = input.getSelectedFile().getAbsolutePath();
                }
                idata.setVariable(field.getAssociatedVariable(), value);
                entries.add(new TextValuePair(field.getAssociatedVariable(), value));
            }
        }
        catch (Exception e)
        {
            if (Debug.stackTracing())
            {
                Debug.trace(e);
            }
        }
        return result;
    }

    private boolean readFileField(UIElement field)
    {
        boolean result = false;
        try
        {
            FileInputField input = (FileInputField) field.getComponent();
            result = !validating || input.validateField();
            if (result)
            {
                String value = "";
                File inputFile = input.getSelectedFile();
                if(inputFile != null && !inputFile.getName().equals("")){
                    value = input.getSelectedFile().getAbsolutePath();
                }
                idata.setVariable(field.getAssociatedVariable(), value);
                entries.add(new TextValuePair(field.getAssociatedVariable(), value));
            }
        }
        catch (Exception e)
        {
            if (Debug.stackTracing())
            {
                Debug.trace(e);
            }
        }
        return result;
    }

    private boolean readMultipleFileField(UIElement field)
    {
        boolean result = false;
        try
        {
            MultipleFileInputField input = (MultipleFileInputField) field.getComponent();
            result = !validating || input.validateField();
            if (result)
            {
                List<String> files = input.getSelectedFiles();
                String variable = field.getAssociatedVariable();
                if (input.isCreateMultipleVariables())
                {
                    int index = 0;
                    for (String file : files)
                    {
                        StringBuffer indexedVariableName = new StringBuffer(variable);
                        if (index > 0)
                        {
                            indexedVariableName.append("_");
                            indexedVariableName.append(index);
                        }
                        index++;
                        idata.setVariable(indexedVariableName.toString(), file);
                        entries.add(new TextValuePair(indexedVariableName.toString(), file));
                    }

                }
                else
                {
                    StringBuffer buffer = new StringBuffer();
                    for (String file : files)
                    {
                        buffer.append(file);
                        buffer.append(";");
                    }
                    idata.setVariable(variable, buffer.toString());
                    entries.add(new TextValuePair(variable, buffer.toString()));
                }
            }
        }
        catch (Exception e)
        {
            if (Debug.stackTracing())
            {
                Debug.trace(e);
            }
        }
        return result;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Reads the XML specification for the panel layout. The result is stored in spec.
     *
     * @throws Exception for any problems in reading the specification
     */
    /*--------------------------------------------------------------------------*/
    private void readSpec() throws Exception
    {
        InputStream input = null;
        IXMLElement data;
        Vector<IXMLElement> specElements;
        String attribute;
        String panelattribute;
        String instance = Integer.toString(instanceNumber);

        String panelid = null;
        Panel p = this.getMetadata();
        if (p != null)
        {
            panelid = p.getPanelid();
        }
        try
        {
            input = parentFrame.getResource(SPEC_FILE_NAME);
        }
        catch (Exception exception)
        {
            haveSpec = false;
            return;
        }
        if (input == null)
        {
            haveSpec = false;
            return;
        }

        // initialize the parser
        IXMLParser parser = new XMLParser();

        // get the data
        data = parser.parse(input);

        // extract the spec to this specific panel instance
        if (data.hasChildren())
        {
            specElements = data.getChildrenNamed(NODE_ID);
            for (int i = 0; i < specElements.size(); i++)
            {
                data = specElements.elementAt(i);
                attribute = data.getAttribute(INSTANCE_IDENTIFIER);
                panelattribute = data.getAttribute(PANEL_IDENTIFIER);

                if (((attribute != null) && instance.equals(attribute))
                        || ((panelattribute != null) && (panelid != null) && (panelid
                                .equals(panelattribute))))
                {
                    // use the current element as spec
                    spec = data;
                    // close the stream
                    input.close();
                    haveSpec = true;
                    return;
                }
            }

            haveSpec = false;
            return;
        }

        haveSpec = false;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Adds the title to the panel. There can only be one title, if mutiple titles are defined, they
     * keep overwriting what has already be defined, so that the last definition is the one that
     * prevails.
     *
     * @param spec a <code>IXMLElement</code> containing the specification for the title.
     */
    /*--------------------------------------------------------------------------*/
    private void addTitle(IXMLElement spec)
    {
        String title = getText(spec);
        boolean italic = getBoolean(spec, ITALICS, false);
        boolean bold = getBoolean(spec, BOLD, false);
        float multiplier = getFloat(spec, SIZE, 2.0f);
        int justify = getAlignment(spec);

        String icon = getIconName(spec);

        if (title != null)
        {
            JLabel label = null;
            ImageIcon imgicon = null;
            try
            {
                imgicon = parent.icons.getImageIcon(icon);
                label = LabelFactory.create(title, imgicon, JLabel.TRAILING, true);
            }
            catch (Exception e)
            {
                Debug.trace("Icon " + icon + " not found in icon list. " + e.getMessage());
                label = LabelFactory.create(title);
            }
            Font font = label.getFont();
            float size = font.getSize();
            int style = 0;

            if (bold)
            {
                style += Font.BOLD;
            }
            if (italic)
            {
                style += Font.ITALIC;
            }

            font = font.deriveFont(style, (size * multiplier));
            label.setFont(font);
            label.setAlignmentX(0);

            TwoColumnConstraints constraints = new TwoColumnConstraints();
            constraints.align = justify;
            constraints.position = TwoColumnConstraints.NORTH;

            panel.add(label, constraints);
        }
    }

    protected String getIconName(IXMLElement element)
    {
        if (element == null) { return (null); }

        String key = element.getAttribute(ICON_KEY);
        String text = null;
        if ((key != null) && (langpack != null))
        {
            try
            {
                text = langpack.getString(key);
            }
            catch (Throwable exception)
            {
                text = null;
            }
        }

        return (text);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Adds a rule field to the list of UI elements.
     *
     * @param spec a <code>IXMLElement</code> containing the specification for the rule field.
     */
    /*--------------------------------------------------------------------------*/
    private void addRuleField(IXMLElement spec)
    {
        Vector<IXMLElement> forPacks = spec.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs = spec.getChildrenNamed(OS);
        IXMLElement element = spec.getFirstChildNamed(SPEC);
        String variable = spec.getAttribute(VARIABLE);
        RuleInputField field = null;
        JLabel label;
        String layout;
        String set;
        String separator;
        String format;
        String validator = null;
        String message = null;
        boolean hasParams = false;
        String paramName = null;
        String paramValue = null;
        HashMap<String, String> validateParamMap = null;
        Vector<IXMLElement> validateParams = null;
        String processor = null;
        int resultFormat = RuleInputField.DISPLAY_FORMAT;

        // ----------------------------------------------------
        // extract the specification details
        // ----------------------------------------------------
        if (element != null)
        {
            label = new JLabel(getText(element));
            layout = element.getAttribute(RULE_LAYOUT);
            set = element.getAttribute(SET);

            // retrieve value of variable if not specified
            // (does not work here because of special format for set attribute)
            // if (set == null)
            // {
            // set = idata.getVariable (variable);
            // }

            separator = element.getAttribute(RULE_SEPARATOR);
            format = element.getAttribute(RULE_RESULT_FORMAT);

            if (format != null)
            {
                if (format.equals(RULE_PLAIN_STRING))
                {
                    resultFormat = RuleInputField.PLAIN_STRING;
                }
                else if (format.equals(RULE_DISPLAY_FORMAT))
                {
                    resultFormat = RuleInputField.DISPLAY_FORMAT;
                }
                else if (format.equals(RULE_SPECIAL_SEPARATOR))
                {
                    resultFormat = RuleInputField.SPECIAL_SEPARATOR;
                }
                else if (format.equals(RULE_ENCRYPTED))
                {
                    resultFormat = RuleInputField.ENCRYPTED;
                }
            }
        }
        // ----------------------------------------------------
        // if there is no specification element, return without
        // doing anything.
        // ----------------------------------------------------
        else
        {
            return;
        }

        // ----------------------------------------------------
        // get the description and add it to the list of UI
        // elements if it exists.
        // ----------------------------------------------------
        element = spec.getFirstChildNamed(DESCRIPTION);
        addDescription(element, forPacks, forOs);

        // ----------------------------------------------------
        // get the validator and processor if they are defined
        // ----------------------------------------------------
        element = spec.getFirstChildNamed(VALIDATOR);
        if (element != null)
        {
            validator = element.getAttribute(CLASS);
            message = getText(element);
            // ----------------------------------------------------------
            // check and see if we have any parameters for this validator.
            // If so, then add them to validateParamMap.
            // ----------------------------------------------------------
            validateParams = element.getChildrenNamed(RULE_PARAM);
            if (validateParams != null && validateParams.size() > 0)
            {
                hasParams = true;

                if (validateParamMap == null)
                {
                    validateParamMap = new HashMap<String, String>();
                }

                for (IXMLElement validateParam : validateParams)
                {
                    element = validateParam;
                    paramName = element.getAttribute(RULE_PARAM_NAME);
                    paramValue = element.getAttribute(RULE_PARAM_VALUE);
                    validateParamMap.put(paramName, paramValue);
                }

            }

        }

        element = spec.getFirstChildNamed(PROCESSOR);
        if (element != null)
        {
            processor = element.getAttribute(CLASS);
        }

        // ----------------------------------------------------
        // create an instance of RuleInputField based on the
        // extracted specifications, then add it to the list
        // of UI elements.
        // ----------------------------------------------------
        if (hasParams)
        {
            field = new RuleInputField(layout, set, separator, validator, validateParamMap,
                    processor, resultFormat, getToolkit(), idata);
        }
        else
        {
            field = new RuleInputField(layout, set, separator, validator, processor, resultFormat,
                    getToolkit(), idata);

        }
        TwoColumnConstraints constraints = new TwoColumnConstraints();
        constraints.position = TwoColumnConstraints.WEST;

        UIElement labelUiElement = new UIElement();
        labelUiElement.setType(UIElementType.LABEL);
        labelUiElement.setConstraints(constraints);
        labelUiElement.setComponent(label);
        labelUiElement.setForPacks(forPacks);
        labelUiElement.setForOs(forOs);
        elements.add(labelUiElement);

        TwoColumnConstraints constraints2 = new TwoColumnConstraints();
        constraints2.position = TwoColumnConstraints.EAST;

        UIElement ruleField = new UIElement();
        ruleField.setType(UIElementType.RULE);
        ruleField.setConstraints(constraints2);
        ruleField.setComponent(field);
        ruleField.setForPacks(forPacks);
        ruleField.setForOs(forOs);
        ruleField.setAssociatedVariable(variable);
        ruleField.setMessage(message);
        elements.add(ruleField);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Reads the data from the rule input field and sets the associated variable.
     *
     * @param field the object array that holds the details of the field.
     * @return <code>true</code> if there was no problem reading the data or if there was an
     * irrecovarable problem. If there was a problem that can be corrected by the operator, an error
     * dialog is popped up and <code>false</code> is returned.
     */
    /*--------------------------------------------------------------------------*/
    private boolean readRuleField(UIElement field)
    {
        RuleInputField ruleField = null;
        String variable = null;
        String message = null;

        try
        {
            ruleField = (RuleInputField) field.getComponent();
            variable = field.getAssociatedVariable();
            message = field.getMessage();
        }
        catch (Throwable exception)
        {
            return (true);
        }
        if ((variable == null) || (ruleField == null)) { return (true); }

        boolean success = !validating || ruleField.validateContents();
        if (!success)
        {
            showWarningMessageDialog(parentFrame, message);
            return (false);
        }

        idata.setVariable(variable, ruleField.getText());
        entries.add(new TextValuePair(variable, ruleField.getText()));
        return (true);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Adds a text field to the list of UI elements
     *
     * @param spec a <code>IXMLElement</code> containing the specification for the text field.
     */
    /*--------------------------------------------------------------------------*/
    private void addTextField(IXMLElement spec)
    {
        Vector<IXMLElement> forPacks = spec.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs = spec.getChildrenNamed(OS);
        IXMLElement element = spec.getFirstChildNamed(SPEC);
        JLabel label;
        String set;
        int size;
        int rows = 1;
        HashMap<String, String> validateParamMap = null;
        Vector<IXMLElement> validateParams = null;
        String validator = null;
        String message = null;
        boolean hasParams = false;
        TextInputField inputField;

        String variable = spec.getAttribute(VARIABLE);
        if ((variable == null) || (variable.length() == 0)) { return; }

        // ----------------------------------------------------
        // extract the specification details
        // ----------------------------------------------------
        if (element != null)
        {
            label = new JLabel(getText(element));
            set = element.getAttribute(SET);
            if (set == null)
            {
                set = idata.getVariable(variable);
                if (set == null)
                {
                    set = "";
                }
            }
            else
            {
                if (set != null && !"".equals(set))
                {
                    VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
                    set = vs.substitute(set, null);
                }
            }

            try
            {
                size = Integer.parseInt(element.getAttribute(TEXT_SIZE));
            }
            catch (Throwable exception)
            {
                size = 1;
            }
            if (element.getAttribute(TEXT_ROWS) != null) {
                try
                {
                    rows = Integer.parseInt(element.getAttribute(TEXT_ROWS));
                }
                catch (Throwable exception)
                {
                    rows = 1;
                }
            }
        }
        // ----------------------------------------------------
        // if there is no specification element, return without
        // doing anything.
        // ----------------------------------------------------
        else
        {
            Debug.trace("No specification element, returning.");
            return;
        }

        // ----------------------------------------------------
        // get the validator if was defined
        // ----------------------------------------------------
        element = spec.getFirstChildNamed(VALIDATOR);
        if (element != null)
        {
            validator = element.getAttribute(CLASS);
            Debug.trace("Validator found for text field: " + validator);
            message = getText(element);
            // ----------------------------------------------------------
            // check and see if we have any parameters for this validator.
            // If so, then add them to validateParamMap.
            // ----------------------------------------------------------
            validateParams = element.getChildrenNamed(RULE_PARAM);
            if (validateParams != null && validateParams.size() > 0)
            {
                Debug.trace("Validator has " + validateParams.size() + " parameters.");
                hasParams = true;

                if (validateParamMap == null)
                {
                    validateParamMap = new HashMap<String, String>();
                }

                for (IXMLElement validateParam : validateParams)
                {
                    element = validateParam;
                    String paramName = element.getAttribute(RULE_PARAM_NAME);
                    String paramValue = element.getAttribute(RULE_PARAM_VALUE);
                    validateParamMap.put(paramName, paramValue);
                }

            }

        }

        // ----------------------------------------------------
        // get the description and add it to the list UI
        // elements if it exists.
        // ----------------------------------------------------
        element = spec.getFirstChildNamed(DESCRIPTION);
        addDescription(element, forPacks, forOs);

        // ----------------------------------------------------
        // construct the UI element and add it to the list
        // ----------------------------------------------------
        inputField = new TextInputField(set, size, rows, validator, hasParams ? validateParamMap : null);
        inputField.addFocusListener(this);
        TwoColumnConstraints constraints = new TwoColumnConstraints();
        constraints.position = TwoColumnConstraints.WEST;

        UIElement labelUiElement = new UIElement();
        labelUiElement.setType(UIElementType.LABEL);
        labelUiElement.setConstraints(constraints);
        labelUiElement.setComponent(label);
        labelUiElement.setForPacks(forPacks);
        labelUiElement.setForOs(forOs);
        elements.add(labelUiElement);

        // uiElements
        // .add(new Object[] { null, FIELD_LABEL, null, constraints, label, forPacks, forOs});

        TwoColumnConstraints constraints2 = new TwoColumnConstraints();
        constraints2.position = TwoColumnConstraints.EAST;

        UIElement textUiElement = new UIElement();
        textUiElement.setType(UIElementType.TEXT);
        textUiElement.setConstraints(constraints2);
        textUiElement.setComponent(inputField);
        textUiElement.setForPacks(forPacks);
        textUiElement.setForOs(forOs);
        textUiElement.setAssociatedVariable(variable);
        textUiElement.setMessage(message);
        elements.add(textUiElement);

        // uiElements.add(new Object[] { null, TEXT_FIELD, variable, constraints2, inputField,
        // forPacks, forOs, null, null, message});
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Reads data from the text field and sets the associated variable.
     *
     * @param field the object array that holds the details of the field.
     * @return <code>true</code> if there was no problem reading the data or if there was an
     * irrecovarable problem. If there was a problem that can be corrected by the operator, an error
     * dialog is popped up and <code>false</code> is returned.
     */
    /*--------------------------------------------------------------------------*/
    private boolean readTextField(UIElement field)
    {
        TextInputField textField = null;
        String variable = null;
        String value = null;
        String message = null;

        try
        {
            textField = (TextInputField) field.getComponent();
            variable = field.getAssociatedVariable();
            message = field.getMessage();
            value = textField.getText();
        }
        catch (Throwable exception)
        {
            return (true);
        }
        if ((variable == null) || (value == null)) { return (true); }

        // validate the input
        Debug.trace("Validating text field");
        boolean success = !validating || textField.validateContents();
        if (!success)
        {
            Debug.trace("Validation did not pass, message: " + message);
            if (message == null)
            {
                message = "Text entered did not pass validation.";
            }
            showWarningMessageDialog(parentFrame, message);
            return (false);
        }
        Debug.trace("Field validated");
        idata.setVariable(variable, value);
        entries.add(new TextValuePair(variable, value));
        return (true);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Adds a combo box to the list of UI elements. <br>
     * This is a complete example of a valid XML specification
     * <p/>
     *
     * <pre>
     * &lt;p/&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     *      &lt;field type=&quot;combo&quot; variable=&quot;testVariable&quot;&gt;
     *        &lt;description text=&quot;Description for the combo box&quot; id=&quot;a key for translated text&quot;/&gt;
     *        &lt;spec text=&quot;label&quot; id=&quot;key for the label&quot;/&gt;
     *          &lt;choice text=&quot;choice 1&quot; id=&quot;&quot; value=&quot;combo box 1&quot;/&gt;
     *          &lt;choice text=&quot;choice 2&quot; id=&quot;&quot; value=&quot;combo box 2&quot; set=&quot;true&quot;/&gt;
     *          &lt;choice text=&quot;choice 3&quot; id=&quot;&quot; value=&quot;combo box 3&quot;/&gt;
     *          &lt;choice text=&quot;choice 4&quot; id=&quot;&quot; value=&quot;combo box 4&quot;/&gt;
     *        &lt;/spec&gt;
     *      &lt;/field&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     * </pre>
     *
     * @param spec a <code>IXMLElement</code> containing the specification for the combo box.
     */
    /*--------------------------------------------------------------------------*/
    private void addComboBox(IXMLElement spec)
    {
        Vector<IXMLElement> forPacks = spec.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs = spec.getChildrenNamed(OS);
        IXMLElement element = spec.getFirstChildNamed(SPEC);
        String variable = spec.getAttribute(VARIABLE);
        TextValuePair listItem = null;
        JComboBox field = new JComboBox();
        JLabel label;
        
        String causesValidataion = spec.getAttribute(REVALIDATE);
        if (causesValidataion != null && causesValidataion.equals("yes"))
        {
            field.addItemListener(this);
        }
        boolean userinput = false; // is there already user input?
        // ----------------------------------------------------
        // extract the specification details
        // ----------------------------------------------------
        if (element != null)
        {
            label = new JLabel(getText(element));

            Vector<IXMLElement> choices = element.getChildrenNamed(COMBO_CHOICE);

            if (choices == null) { return; }
            // get current value of associated variable
            String currentvariablevalue = idata.getVariable(variable);
            if (currentvariablevalue != null)
            {
                // there seems to be user input
                userinput = true;
            }
            int choicenum = 0;
            for (int i = 0; i < choices.size(); i++)
            {
            	String conditionid = (choices.elementAt(i)).getAttribute(ATTRIBUTE_CONDITIONID_NAME);
            	if (conditionid != null)
            	{
                    // check if condition is fulfilled
                    if (!this.parent.getRules().isConditionTrue(conditionid, idata.getVariables()))
                    {
                        continue;
                    }
            	}
                String processorClass = (choices.elementAt(i)).getAttribute("processor");

                if (processorClass != null && !"".equals(processorClass))
                {
                    String choiceValues = "";
                    try
                    {
                        choiceValues = ((Processor) Class.forName(processorClass).newInstance())
                                .process(null);
                    }
                    catch (Throwable t)
                    {
                        t.printStackTrace();
                    }
                    String set = (choices.elementAt(i)).getAttribute(SET);
                    if (set == null)
                    {
                        set = "";
                    }
                    if (set != null && !"".equals(set))
                    {
                        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
                        set = vs.substitute(set, null);
                    }

                    StringTokenizer tokenizer = new StringTokenizer(choiceValues, ":");
                    int counter = 0;
                    while (tokenizer.hasMoreTokens())
                    {
                        String token = tokenizer.nextToken();
                        listItem = new TextValuePair(token, token);
                        field.addItem(listItem);
                        if (set.equals(token))
                        {
                            field.setSelectedIndex(field.getItemCount() - 1);
                        }
                        counter++;
                    }
                }
                else
                {
                    String value = (choices.elementAt(i)).getAttribute(COMBO_VALUE);
                    listItem = new TextValuePair(getText(choices.elementAt(i)), value);
                    field.addItem(listItem);
                    if (userinput)
                    {
                        // is the current value identical to the value associated with this element
                        if ((value != null) && (value.length() > 0)
                                && (currentvariablevalue.equals(value)))
                        {
                            // select it
                            field.setSelectedIndex(choicenum);
                        }
                        // else do nothing
                    }
                    else
                    {
                        // there is no user input
                        String set = (choices.elementAt(i)).getAttribute(SET);
                        if (set != null)
                        {
                            if (set != null && !"".equals(set))
                            {
                                VariableSubstitutor vs = new VariableSubstitutor(idata
                                        .getVariables());
                                set = vs.substitute(set, null);
                            }
                            if (set.equals(TRUE))
                            {
                                field.setSelectedIndex(choicenum);
                            }
                        }
                    }
                    choicenum++;
                }
            }
        }
        // ----------------------------------------------------
        // if there is no specification element, return without
        // doing anything.
        // ----------------------------------------------------
        else
        {
            return;
        }

        // ----------------------------------------------------
        // get the description and add it to the list of UI
        // elements if it exists.
        // ----------------------------------------------------
        element = spec.getFirstChildNamed(DESCRIPTION);
        addDescription(element, forPacks, forOs);

        TwoColumnConstraints constraints = new TwoColumnConstraints();
        constraints.position = TwoColumnConstraints.WEST;

        UIElement labelUiElement = new UIElement();
        labelUiElement.setType(UIElementType.LABEL);
        labelUiElement.setConstraints(constraints);
        labelUiElement.setComponent(label);
        labelUiElement.setForPacks(forPacks);
        labelUiElement.setForOs(forOs);
        elements.add(labelUiElement);

        // uiElements
        // .add(new Object[] { null, FIELD_LABEL, null, constraints, label, forPacks, forOs});

        TwoColumnConstraints constraints2 = new TwoColumnConstraints();
        constraints2.position = TwoColumnConstraints.EAST;

        UIElement comboUiElement = new UIElement();
        comboUiElement.setType(UIElementType.COMBOBOX);
        comboUiElement.setConstraints(constraints2);
        comboUiElement.setComponent(field);
        comboUiElement.setForPacks(forPacks);
        comboUiElement.setForOs(forOs);
        comboUiElement.setAssociatedVariable(variable);
        elements.add(comboUiElement);
        // uiElements.add(new Object[] { null, COMBO_FIELD, variable, constraints2, field, forPacks,
        // forOs});
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Reads the content of the combobox field and substitutes the associated variable.
     *
     * @param field the object array that holds the details of the field.
     * @return <code>true</code> if there was no problem reading the data or if there was an
     * irrecovarable problem. If there was a problem that can be corrected by the operator, an error
     * dialog is popped up and <code>false</code> is returned.
     */
    /*--------------------------------------------------------------------------*/
    private boolean readComboBox(UIElement field)
    {
        String variable;
        String value;
        JComboBox comboBox;

        try
        {
            variable = (String) field.getAssociatedVariable();
            comboBox = (JComboBox) field.getComponent();
            value = ((TextValuePair) comboBox.getSelectedItem()).getValue();
        }
        catch (Throwable exception)
        {
            return true;
        }
        if ((variable == null) || (value == null)) { return true; }

        idata.setVariable(variable, value);
        entries.add(new TextValuePair(variable, value));
        return true;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Adds a radio button set to the list of UI elements. <br>
     * This is a complete example of a valid XML specification
     * <p/>
     *
     * <pre>
     * &lt;p/&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     *      &lt;field type=&quot;radio&quot; variable=&quot;testVariable&quot;&gt;
     *        &lt;description text=&quot;Description for the radio buttons&quot; id=&quot;a key for translated text&quot;/&gt;
     *        &lt;spec text=&quot;label&quot; id=&quot;key for the label&quot;/&gt;
     *          &lt;choice text=&quot;radio 1&quot; id=&quot;&quot; value=&quot;&quot;/&gt;
     *          &lt;choice text=&quot;radio 2&quot; id=&quot;&quot; value=&quot;&quot; set=&quot;true&quot;/&gt;
     *          &lt;choice text=&quot;radio 3&quot; id=&quot;&quot; value=&quot;&quot;/&gt;
     *          &lt;choice text=&quot;radio 4&quot; id=&quot;&quot; value=&quot;&quot;/&gt;
     *          &lt;choice text=&quot;radio 5&quot; id=&quot;&quot; value=&quot;&quot;/&gt;
     *        &lt;/spec&gt;
     *      &lt;/field&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     * </pre>
     *
     * @param spec a <code>IXMLElement</code> containing the specification for the radio button set.
     */
    /*--------------------------------------------------------------------------*/
    private void addRadioButton(IXMLElement spec)
    {
        Vector<IXMLElement> forPacks = spec.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs = spec.getChildrenNamed(OS);
        String variable = spec.getAttribute(VARIABLE);
        String value = null;

        IXMLElement element = null;
        ButtonGroup group = new ButtonGroup();

        TwoColumnConstraints constraints = new TwoColumnConstraints();
        constraints.position = TwoColumnConstraints.BOTH;
        constraints.indent = true;
        constraints.stretch = true;

        // ----------------------------------------------------
        // get the description and add it to the list of UI
        // elements if it exists.
        // ----------------------------------------------------
        element = spec.getFirstChildNamed(DESCRIPTION);
        addDescription(element, forPacks, forOs);

        // ----------------------------------------------------
        // extract the specification details
        // ----------------------------------------------------
        element = spec.getFirstChildNamed(SPEC);

        if (element != null)
        {
            Vector<IXMLElement> choices = element.getChildrenNamed(RADIO_CHOICE);

            if (choices == null) { return; }

            // --------------------------------------------------
            // process each choice element
            // --------------------------------------------------
            for (int i = 0; i < choices.size(); i++)
            {
            	String conditionid = (choices.elementAt(i)).getAttribute(ATTRIBUTE_CONDITIONID_NAME);
            	if (conditionid != null)
            	{
                    // check if condition is fulfilled
                    if (!this.parent.getRules().isConditionTrue(conditionid, idata.getVariables()))
                    {
                        continue;
                    }
            	}
                JRadioButton choice = new JRadioButton();
                choice.setText(getText(choices.elementAt(i)));
                String causesValidataion = (choices.elementAt(i)).getAttribute(REVALIDATE);
                if (causesValidataion != null && causesValidataion.equals("yes"))
                {
                    choice.addActionListener(this);
                }
                value = ((choices.elementAt(i)).getAttribute(RADIO_VALUE));

                group.add(choice);

                String set = (choices.elementAt(i)).getAttribute(SET);
                // in order to properly initialize dependent controls
                // we must set this variable now
                if (idata.getVariable(variable) == null)
                {
                    if (set != null)
                    {
                        idata.setVariable(variable, value);
                    }
                }
                if (set != null)
                {
                    if (set != null && !"".equals(set))
                    {
                        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
                        set = vs.substitute(set, null);
                    }
                    if (set.equals(TRUE))
                    {
                        choice.setSelected(true);
                    }
                }

                buttonGroups.add(group);

                RadioButtonUIElement radioUiElement = new RadioButtonUIElement();
                radioUiElement.setType(UIElementType.RADIOBUTTON);
                radioUiElement.setConstraints(constraints);
                radioUiElement.setComponent(choice);
                radioUiElement.setForPacks(forPacks);
                radioUiElement.setForOs(forOs);
                radioUiElement.setButtonGroup(group);
                radioUiElement.setAssociatedVariable(variable);
                radioUiElement.setTrueValue(value);
                elements.add(radioUiElement);

                // uiElements.add(new Object[] { null, RADIO_FIELD, variable, constraints, choice,
                // forPacks, forOs, value, null, null, group});
            }
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Reads the content of the radio button field and substitutes the associated variable.
     *
     * @param field the object array that holds the details of the field.
     * @return <code>true</code> if there was no problem reading the data or if there was an
     * irrecovarable problem. If there was a problem that can be corrected by the operator, an error
     * dialog is popped up and <code>false</code> is returned.
     */
    /*--------------------------------------------------------------------------*/
    private boolean readRadioButton(UIElement field)
    {
        String variable = null;
        String value = null;
        JRadioButton button = null;

        try
        {
            button = (JRadioButton) field.getComponent();

            if (!button.isSelected()) { return (true); }

            variable = field.getAssociatedVariable();
            value = field.getTrueValue();
        }
        catch (Throwable exception)
        {
            return (true);
        }

        idata.setVariable(variable, value);
        entries.add(new TextValuePair(variable, value));
        return (true);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Adds one or more password fields to the list of UI elements. <br>
     * This is a complete example of a valid XML specification
     * <p/>
     *
     * <pre>
     * &lt;p/&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     *      &lt;field type=&quot;password&quot; variable=&quot;testVariable&quot;&gt;
     *        &lt;description align=&quot;left&quot; txt=&quot;Please enter your password&quot; id=&quot;a key for translated text&quot;/&gt;
     *        &lt;spec&gt;
     *          &lt;pwd txt=&quot;Password&quot; id=&quot;key for the label&quot; size=&quot;10&quot; set=&quot;&quot;/&gt;
     *          &lt;pwd txt=&quot;Retype password&quot; id=&quot;another key for the label&quot; size=&quot;10&quot; set=&quot;&quot;/&gt;
     *        &lt;/spec&gt;
     *        &lt;validator class=&quot;com.izforge.sample.PWDValidator&quot; txt=&quot;Both versions of the password must match&quot; id=&quot;key for the error text&quot;/&gt;
     *        &lt;processor class=&quot;com.izforge.sample.PWDEncryptor&quot;/&gt;
     *      &lt;/field&gt;
     * &lt;p/&gt;
     * </pre>
     *
     * Additionally, parameters and multiple validators can be used to provide separate validation
     * and error messages for each case.
     *
     * <pre>
     * &lt;p/&gt;
     *    &lt;field type=&quot;password&quot; align=&quot;left&quot; variable=&quot;keystore.password&quot;&gt;
     *      &lt;spec&gt;
     *        &lt;pwd txt=&quot;Keystore Password:&quot; size=&quot;25&quot; set=&quot;&quot;/&gt;
     *        &lt;pwd txt=&quot;Retype Password:&quot; size=&quot;25&quot; set=&quot;&quot;/&gt;
     *      &lt;/spec&gt;
     *      &lt;validator class=&quot;com.izforge.izpack.util.PasswordEqualityValidator&quot; txt=&quot;Both keystore passwords must match.&quot; id=&quot;key for the error text&quot;/&gt;
     *      &lt;validator class=&quot;com.izforge.izpack.util.PasswordKeystoreValidator&quot; txt=&quot;Could not validate keystore with password and alias provided.&quot; id=&quot;key for the error text&quot;&gt;
     *        &lt;param name=&quot;keystoreFile&quot; value=&quot;${existing.ssl.keystore}&quot;/&gt;
     *        &lt;param name=&quot;keystoreType&quot; value=&quot;JKS&quot;/&gt;
     *        &lt;param name=&quot;keystoreAlias&quot; value=&quot;${keystore.key.alias}&quot;/&gt;
     *      &lt;/validator&gt;
     *    &lt;/field&gt;
     * &lt;p/&gt;
     * </pre>
     *
     * @param spec a <code>IXMLElement</code> containing the specification for the set of password
     * fields.
     */
    /*--------------------------------------------------------------------------*/
    private void addPasswordField(IXMLElement spec)
    {
        Vector<IXMLElement> forPacks = spec.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs = spec.getChildrenNamed(OS);
        String variable = spec.getAttribute(VARIABLE);
        String processor = null;
        IXMLElement element = null;
        PasswordGroup group = null;
        int size = 0;

        // ----------------------------------------------------
        // get the description and add it to the list of UI
        // elements if it exists.
        // ----------------------------------------------------
        element = spec.getFirstChildNamed(DESCRIPTION);
        addDescription(element, forPacks, forOs);

        List<ValidatorContainer> validatorsList = analyzeValidator(spec);
        if (validatorsList == null)
        {
            validatorsList = new ArrayList<ValidatorContainer>();
        }

        element = spec.getFirstChildNamed(PROCESSOR);
        if (element != null)
        {
            processor = element.getAttribute(CLASS);
        }

        group = new PasswordGroup(idata, validatorsList, processor);

        // ----------------------------------------------------
        // extract the specification details
        // ----------------------------------------------------
        element = spec.getFirstChildNamed(SPEC);

        if (element != null)
        {
            Vector<IXMLElement> inputs = element.getChildrenNamed(PWD_INPUT);

            if (inputs == null) { return; }

            // --------------------------------------------------
            // process each input field
            // --------------------------------------------------
            IXMLElement fieldSpec;
            for (int i = 0; i < inputs.size(); i++)
            {
                fieldSpec = inputs.elementAt(i);
                String set = fieldSpec.getAttribute(SET);
                if (set != null && !"".equals(set))
                {
                    VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
                    set = vs.substitute(set, null);
                }
                JLabel label = new JLabel(getText(fieldSpec));
                try
                {
                    size = Integer.parseInt(fieldSpec.getAttribute(PWD_SIZE));
                }
                catch (Throwable exception)
                {
                    size = 1;
                }

                // ----------------------------------------------------
                // construct the UI element and add it to the list
                // ----------------------------------------------------
                JPasswordField field = new JPasswordField(set, size);
                field.setCaretPosition(0);

                TwoColumnConstraints constraints = new TwoColumnConstraints();
                constraints.position = TwoColumnConstraints.WEST;

                UIElement labelUiElement = new UIElement();
                labelUiElement.setType(UIElementType.LABEL);
                labelUiElement.setConstraints(constraints);
                labelUiElement.setComponent(label);
                labelUiElement.setForPacks(forPacks);
                labelUiElement.setForOs(forOs);
                elements.add(labelUiElement);

                // uiElements.add(new Object[] { null, FIELD_LABEL, null, constraints, label,
                // forPacks, forOs});

                TwoColumnConstraints constraints2 = new TwoColumnConstraints();
                constraints2.position = TwoColumnConstraints.EAST;

                PasswordUIElement passwordUiElement = new PasswordUIElement();
                passwordUiElement.setType(UIElementType.PASSWORD);
                passwordUiElement.setConstraints(constraints2);
                passwordUiElement.setComponent(field);
                passwordUiElement.setForPacks(forPacks);
                passwordUiElement.setForOs(forOs);
                passwordUiElement.setPasswordGroup(group);
                passwordUiElement.setAssociatedVariable(variable);
                elements.add(passwordUiElement);

                // Removed message to support pulling from multiple validators
                // uiElements.add(new Object[] { null, PWD_FIELD, variable, constraints2, field,
                // forPacks, forOs, null, null, null, group});
                // Original
                // uiElements.add(new Object[]{null, PWD_FIELD, variable, constraints2, field,
                // forPacks, forOs, null, null, message, group
                // });
                group.addField(field);
            }
        }

        passwordGroups.add(group);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Reads the content of the password field and substitutes the associated variable.
     *
     * @param field a password group that manages one or more passord fields.
     * @return <code>true</code> if there was no problem reading the data or if there was an
     * irrecovarable problem. If there was a problem that can be corrected by the operator, an error
     * dialog is popped up and <code>false</code> is returned.
     */
    /*--------------------------------------------------------------------------*/
    private boolean readPasswordField(UIElement field)
    {
        PasswordUIElement pwdField = (PasswordUIElement) field;

        PasswordGroup group = null;
        String variable = null;

        try
        {
            group = (PasswordGroup) pwdField.getPasswordGroup();
            variable = field.getAssociatedVariable();
            // Removed to support grabbing the message from multiple validators
            // message = (String) field[POS_MESSAGE];
        }
        catch (Throwable exception)
        {
            return (true);
        }
        if ((variable == null) || (passwordGroupsRead.contains(group))) { return (true); }
        passwordGroups.add(group);

        int size = group.validatorSize();
        boolean success = !validating || size < 1;

        // Use each validator to validate contents
        if (!success)
        {
            // System.out.println("Found "+(size)+" validators");
            for (int i = 0; i < size; i++)
            {
                success = group.validateContents(i);
                if (!success)
                {
                    JOptionPane.showMessageDialog(parentFrame, group.getValidatorMessage(i),
                            parentFrame.langpack.getString("UserInputPanel.error.caption"),
                            JOptionPane.WARNING_MESSAGE);
                    break;
                }
            }
        }

        if (success)
        {
            idata.setVariable(variable, group.getPassword());
            entries.add(new TextValuePair(variable, group.getPassword()));
        }
        return success;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Adds a chackbox to the list of UI elements.
     *
     * @param spec a <code>IXMLElement</code> containing the specification for the checkbox.
     */
    /*--------------------------------------------------------------------------*/
    private void addCheckBox(IXMLElement spec)
    {
        Vector<IXMLElement> forPacks = spec.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs = spec.getChildrenNamed(OS);
        String label = "";
        String set = null;
        String trueValue = null;
        String falseValue = null;
        String variable = spec.getAttribute(VARIABLE);
        String causesValidataion = null;
        IXMLElement detail = spec.getFirstChildNamed(SPEC);

        if (variable == null) { return; }

        if (detail != null)
        {
            label = getText(detail);
            set = detail.getAttribute(SET);
            trueValue = detail.getAttribute(TRUE);
            falseValue = detail.getAttribute(FALSE);
            causesValidataion = detail.getAttribute(REVALIDATE);
            String value = idata.getVariable(variable);
            Debug.trace("check: value: " + value + ", set: " + set);
            if (value != null)
            {
                // Default is not checked so we only need to check for true
                if (value.equals(trueValue))
                {
                    set = TRUE;
                }
            }
        }

        JCheckBox checkbox = new JCheckBox(label);
        // What are we doing here anyway??? BDA 20090518
        //checkbox.addItemListener(this);

        if (causesValidataion != null && causesValidataion.equals("yes"))
        {
            checkbox.addActionListener(this);
        }
        if (set != null)
        {
            if (set != null && !"".equals(set))
            {
                VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
                set = vs.substitute(set, null);
            }
            if (set.equals(FALSE))
            {
                checkbox.setSelected(false);
            }
            if (set.equals(TRUE))
            {
                checkbox.setSelected(true);
            }
        }

        // ----------------------------------------------------
        // get the description and add it to the list of UI
        // elements if it exists.
        // ----------------------------------------------------
        IXMLElement element = spec.getFirstChildNamed(DESCRIPTION);
        addDescription(element, forPacks, forOs);

        TwoColumnConstraints constraints = new TwoColumnConstraints();
        constraints.position = TwoColumnConstraints.BOTH;
        constraints.stretch = true;
        constraints.indent = true;

        UIElement checkboxUiElement = new UIElement();
        checkboxUiElement.setType(UIElementType.CHECKBOX);
        checkboxUiElement.setConstraints(constraints);
        checkboxUiElement.setComponent(checkbox);
        checkboxUiElement.setForPacks(forPacks);
        checkboxUiElement.setForOs(forOs);
        checkboxUiElement.setTrueValue(trueValue);
        checkboxUiElement.setFalseValue(falseValue);
        checkboxUiElement.setAssociatedVariable(variable);
        elements.add(checkboxUiElement);

        // uiElements.add(new Object[] { null, CHECK_FIELD, variable, constraints, checkbox,
        // forPacks,
        // forOs, trueValue, falseValue});
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Reads the content of the checkbox field and substitutes the associated variable.
     *
     * @param field the object array that holds the details of the field.
     * @return <code>true</code> if there was no problem reading the data or if there was an
     * irrecovarable problem. If there was a problem that can be corrected by the operator, an error
     * dialog is popped up and <code>false</code> is returned.
     */
    /*--------------------------------------------------------------------------*/
    private boolean readCheckBox(UIElement field)
    {
        String variable = null;
        String trueValue = null;
        String falseValue = null;
        JCheckBox box = null;

        try
        {
            box = (JCheckBox) field.getComponent();
            variable = field.getAssociatedVariable();
            trueValue = field.getTrueValue();
            if (trueValue == null)
            {
                trueValue = "";
            }

            falseValue = field.getFalseValue();
            if (falseValue == null)
            {
                falseValue = "";
            }
        }
        catch (Throwable exception)
        {
            Debug.trace("readCheckBox(): failed: " + exception);
            return (true);
        }

        if (box.isSelected())
        {
            Debug.trace("readCheckBox(): selected, setting " + variable + " to " + trueValue);
            idata.setVariable(variable, trueValue);
            entries.add(new TextValuePair(variable, trueValue));
        }
        else
        {
            Debug.trace("readCheckBox(): not selected, setting " + variable + " to " + falseValue);
            idata.setVariable(variable, falseValue);
            entries.add(new TextValuePair(variable, falseValue));
        }

        return (true);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Adds a search field to the list of UI elements.
     * <p/>
     * This is a complete example of a valid XML specification
     * <p/>
     *
     * <pre>
     * &lt;p/&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     *      &lt;field type=&quot;search&quot; variable=&quot;testVariable&quot;&gt;
     *        &lt;description text=&quot;Description for the search field&quot; id=&quot;a key for translated text&quot;/&gt;
     *        &lt;spec text=&quot;label&quot; id=&quot;key for the label&quot; filename=&quot;the_file_to_search&quot; result=&quot;directory&quot; /&gt; &lt;!-- values for result: directory, file --&gt;
     *          &lt;choice dir=&quot;directory1&quot; set=&quot;true&quot; /&gt; &lt;!-- default value --&gt;
     *          &lt;choice dir=&quot;dir2&quot; /&gt;
     *        &lt;/spec&gt;
     *      &lt;/field&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     * </pre>
     *
     * @param spec a <code>IXMLElement</code> containing the specification for the search field
     */
    /*--------------------------------------------------------------------------*/
    private void addSearch(IXMLElement spec)
    {
        Vector<IXMLElement> forPacks = spec.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs = spec.getChildrenNamed(OS);
        IXMLElement element = spec.getFirstChildNamed(SPEC);
        String variable = spec.getAttribute(VARIABLE);
        String filename = null;
        String check_filename = null;
        int search_type = 0;
        int result_type = 0;
        JComboBox combobox = new JComboBox();
        JLabel label = null;

        // System.out.println ("adding search combobox, variable "+variable);

        // allow the user to enter something
        combobox.setEditable(true);

        // ----------------------------------------------------
        // extract the specification details
        // ----------------------------------------------------
        if (element != null)
        {
            label = new JLabel(getText(element));

            // search type is optional (default: file)
            search_type = SearchField.TYPE_FILE;

            String search_type_str = element.getAttribute(SEARCH_TYPE);

            if (search_type_str != null)
            {
                if (search_type_str.equals(SEARCH_FILE))
                {
                    search_type = SearchField.TYPE_FILE;
                }
                else if (search_type_str.equals(SEARCH_DIRECTORY))
                {
                    search_type = SearchField.TYPE_DIRECTORY;
                }
            }

            // result type is mandatory too
            String result_type_str = element.getAttribute(SEARCH_RESULT);

            if (result_type_str == null)
            {
                return;
            }
            else if (result_type_str.equals(SEARCH_FILE))
            {
                result_type = SearchField.RESULT_FILE;
            }
            else if (result_type_str.equals(SEARCH_DIRECTORY))
            {
                result_type = SearchField.RESULT_DIRECTORY;
            }
            else if (result_type_str.equals(SEARCH_PARENTDIR))
            {
                result_type = SearchField.RESULT_PARENTDIR;
            }
            else
            {
                return;
            }

            // might be missing - null is okay
            filename = element.getAttribute(SEARCH_FILENAME);

            check_filename = element.getAttribute(SEARCH_CHECKFILENAME);

            Vector<IXMLElement> choices = element.getChildrenNamed(SEARCH_CHOICE);

            if (choices == null) { return; }

            for (int i = 0; i < choices.size(); i++)
            {
                IXMLElement choice_el = choices.elementAt(i);

                if (!OsConstraint.oneMatchesCurrentSystem(choice_el))
                {
                    continue;
                }

                String value = choice_el.getAttribute(SEARCH_VALUE);

                combobox.addItem(value);

                String set = (choices.elementAt(i)).getAttribute(SET);
                if (set != null)
                {
                    if (set != null && !"".equals(set))
                    {
                        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
                        set = vs.substitute(set, null);
                    }
                    if (set.equals(TRUE))
                    {
                        combobox.setSelectedIndex(i);
                    }
                }
            }
        }
        // ----------------------------------------------------
        // if there is no specification element, return without
        // doing anything.
        // ----------------------------------------------------
        else
        {
            return;
        }

        // ----------------------------------------------------
        // get the description and add it to the list of UI
        // elements if it exists.
        // ----------------------------------------------------
        element = spec.getFirstChildNamed(DESCRIPTION);
        addDescription(element, forPacks, forOs);

        TwoColumnConstraints westconstraint1 = new TwoColumnConstraints();
        westconstraint1.position = TwoColumnConstraints.WEST;

        UIElement labelUiElement = new UIElement();
        labelUiElement.setType(UIElementType.LABEL);
        labelUiElement.setConstraints(westconstraint1);
        labelUiElement.setComponent(label);
        labelUiElement.setForPacks(forPacks);
        labelUiElement.setForOs(forOs);
        elements.add(labelUiElement);

        // uiElements.add(new Object[] { null, FIELD_LABEL, null, westconstraint1, label, forPacks,
        // forOs});

        TwoColumnConstraints eastconstraint1 = new TwoColumnConstraints();
        eastconstraint1.position = TwoColumnConstraints.EAST;

        StringBuffer tooltiptext = new StringBuffer();

        if ((filename != null) && (filename.length() > 0))
        {
            tooltiptext.append(MessageFormat.format(parentFrame.langpack
                    .getString("UserInputPanel.search.location"),
                    new Object[] { new String[] { filename}}));
        }

        boolean showAutodetect = (check_filename != null) && (check_filename.length() > 0);
        if (showAutodetect)
        {
            tooltiptext.append(MessageFormat.format(parentFrame.langpack
                    .getString("UserInputPanel.search.location.checkedfile"),
                    new Object[] { new String[] { check_filename}}));
        }

        if (tooltiptext.length() > 0)
        {
            combobox.setToolTipText(tooltiptext.toString());
        }

        UIElement searchUiElement = new UIElement();
        searchUiElement.setType(UIElementType.SEARCH);
        searchUiElement.setConstraints(eastconstraint1);
        searchUiElement.setComponent(combobox);
        searchUiElement.setForPacks(forPacks);
        searchUiElement.setForOs(forOs);
        searchUiElement.setAssociatedVariable(variable);
        elements.add(searchUiElement);

        // uiElements.add(new Object[] { null, SEARCH_FIELD, variable, eastconstraint1, combobox,
        // forPacks, forOs});

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new com.izforge.izpack.gui.FlowLayout(
                com.izforge.izpack.gui.FlowLayout.LEADING));

        JButton autodetectButton = ButtonFactory.createButton(parentFrame.langpack
                .getString("UserInputPanel.search.autodetect"), idata.buttonsHColor);
        autodetectButton.setVisible(showAutodetect);

        autodetectButton.setToolTipText(parentFrame.langpack
                .getString("UserInputPanel.search.autodetect.tooltip"));

        buttonPanel.add(autodetectButton);

        JButton browseButton = ButtonFactory.createButton(parentFrame.langpack
                .getString("UserInputPanel.search.browse"), idata.buttonsHColor);

        buttonPanel.add(browseButton);

        TwoColumnConstraints eastonlyconstraint = new TwoColumnConstraints();
        eastonlyconstraint.position = TwoColumnConstraints.EASTONLY;

        UIElement searchbuttonUiElement = new UIElement();
        searchbuttonUiElement.setType(UIElementType.SEARCHBUTTON);
        searchbuttonUiElement.setConstraints(eastonlyconstraint);
        searchbuttonUiElement.setComponent(buttonPanel);
        searchbuttonUiElement.setForPacks(forPacks);
        searchbuttonUiElement.setForOs(forOs);
        elements.add(searchbuttonUiElement);

        // uiElements.add(new Object[] { null, SEARCH_BUTTON_FIELD, null, eastonlyconstraint,
        // buttonPanel, forPacks, forOs});

        searchFields.add(new SearchField(filename, check_filename, parentFrame, combobox,
                autodetectButton, browseButton, search_type, result_type));
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Reads the content of the search field and substitutes the associated variable.
     *
     * @param field the object array that holds the details of the field.
     * @return <code>true</code> if there was no problem reading the data or if there was an
     * irrecovarable problem. If there was a problem that can be corrected by the operator, an error
     * dialog is popped up and <code>false</code> is returned.
     */
    /*--------------------------------------------------------------------------*/
    private boolean readSearch(UIElement field)
    {
        String variable = null;
        String value = null;
        JComboBox comboBox = null;

        try
        {
            variable = field.getAssociatedVariable();
            comboBox = (JComboBox) field.getComponent();
            for (int i = 0; i < this.searchFields.size(); ++i)
            {
                SearchField sf = this.searchFields.elementAt(i);
                if (sf.belongsTo(comboBox))
                {
                    value = sf.getResult();
                    break;
                }
            }
        }
        catch (Throwable exception)
        {
            return (true);
        }
        if ((variable == null) || (value == null)) { return (true); }

        idata.setVariable(variable, value);
        entries.add(new TextValuePair(variable, value));
        return (true);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Adds text to the list of UI elements
     *
     * @param spec a <code>IXMLElement</code> containing the specification for the text.
     */
    /*--------------------------------------------------------------------------*/
    private void addText(IXMLElement spec)
    {
        Vector<IXMLElement> forPacks = spec.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs = spec.getChildrenNamed(OS);

        addDescription(spec, forPacks, forOs);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Adds a dummy field to the list of UI elements to act as spacer.
     *
     * @param spec a <code>IXMLElement</code> containing other specifications. At present this
     * information is not used but might be in future versions.
     */
    /*--------------------------------------------------------------------------*/
    private void addSpace(IXMLElement spec)
    {
        Vector<IXMLElement> forPacks = spec.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs = spec.getChildrenNamed(OS);
        JPanel panel = new JPanel();

        TwoColumnConstraints constraints = new TwoColumnConstraints();
        constraints.position = TwoColumnConstraints.BOTH;
        constraints.stretch = true;

        UIElement spaceUiElement = new UIElement();
        spaceUiElement.setType(UIElementType.SPACE);
        spaceUiElement.setConstraints(constraints);
        spaceUiElement.setComponent(panel);
        spaceUiElement.setForPacks(forPacks);
        spaceUiElement.setForOs(forOs);
        elements.add(spaceUiElement);

        // uiElements
        // .add(new Object[] { null, SPACE_FIELD, null, constraints, panel, forPacks, forOs});
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Adds a dividing line to the list of UI elements act as separator.
     *
     * @param spec a <code>IXMLElement</code> containing additional specifications.
     */
    /*--------------------------------------------------------------------------*/
    private void addDivider(IXMLElement spec)
    {
        Vector<IXMLElement> forPacks = spec.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs = spec.getChildrenNamed(OS);
        JPanel panel = new JPanel();
        String alignment = spec.getAttribute(ALIGNMENT);

        if (alignment != null)
        {
            if (alignment.equals(TOP))
            {
                panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.gray));
            }
            else
            {
                panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
            }
        }
        else
        {
            panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
        }

        TwoColumnConstraints constraints = new TwoColumnConstraints();
        constraints.position = TwoColumnConstraints.BOTH;
        constraints.stretch = true;

        UIElement dividerUiElement = new UIElement();
        dividerUiElement.setType(UIElementType.DIVIDER);
        dividerUiElement.setConstraints(constraints);
        dividerUiElement.setComponent(panel);
        dividerUiElement.setForPacks(forPacks);
        dividerUiElement.setForOs(forOs);
        elements.add(dividerUiElement);

        // uiElements.add(new Object[] { null, DIVIDER_FIELD, null, constraints, panel, forPacks,
        // forOs});
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Adds a description to the list of UI elements.
     *
     * @param spec a <code>IXMLElement</code> containing the specification for the description.
     */
    /*--------------------------------------------------------------------------*/
    private void addDescription(IXMLElement spec, Vector<IXMLElement> forPacks,
            Vector<IXMLElement> forOs)
    {
        String description;
        TwoColumnConstraints constraints = new TwoColumnConstraints();
        constraints.position = TwoColumnConstraints.BOTH;
        constraints.stretch = true;

        if (spec != null)
        {
            description = getText(spec);

            // if we have a description, add it to the UI elements
            if (description != null)
            {
                // String alignment = spec.getAttribute(ALIGNMENT);
                // FIX needed: where do we use this variable at all? i dont think so...
                // int justify = MultiLineLabel.LEFT;
                //
                // if (alignment != null)
                // {
                // if (alignment.equals(LEFT))
                // {
                // justify = MultiLineLabel.LEFT;
                // }
                // else if (alignment.equals(CENTER))
                // {
                // justify = MultiLineLabel.CENTER;
                // }
                // else if (alignment.equals(RIGHT))
                // {
                // justify = MultiLineLabel.RIGHT;
                // }
                // }

                javax.swing.JTextPane label = new javax.swing.JTextPane();

                // Not editable, but still selectable.
                label.setEditable(false);

                // If html tags are present enable html rendering, otherwise the JTextPane
                // looks exactly like MultiLineLabel.
                if (description.startsWith("<html>") && description.endsWith("</html>"))
                {
                    label.setContentType("text/html");
                    label.addHyperlinkListener(new HyperlinkHandler());
                }
                label.setText(description);

                // Background color and font to match the label's.
                label.setBackground(javax.swing.UIManager.getColor("label.backgroud"));
                label.setMargin(new java.awt.Insets(3, 0, 3, 0));
                // workaround to cut out layout problems
                label.getPreferredSize();
                // end of workaround.

                UIElement descUiElement = new UIElement();
                descUiElement.setType(UIElementType.DESCRIPTION);
                descUiElement.setConstraints(constraints);
                descUiElement.setComponent(label);
                descUiElement.setForPacks(forPacks);
                descUiElement.setForOs(forOs);
                elements.add(descUiElement);

                // uiElements.add(new Object[] { null, DESCRIPTION, null, constraints, label,
                // forPacks, forOs});
            }
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Retrieves the value of a boolean attribute. If the attribute is found and the values equals
     * the value of the constant <code>TRUE</code> then true is returned. If it equals
     * <code>FALSE</code> the false is returned. In all other cases, including when the attribute is
     * not found, the default value is returned.
     *
     * @param element the <code>IXMLElement</code> to search for the attribute.
     * @param attribute the attribute to search for
     * @param defaultValue the default value to use if the attribute does not exist or a illegal
     * value was discovered.
     * @return <code>true</code> if the attribute is found and the value equals the the constant
     * <code>TRUE</code>. <<code> if the
     *         attribute is <code>FALSE</code>. In all other cases the default value is returned.
     */
    /*--------------------------------------------------------------------------*/
    private boolean getBoolean(IXMLElement element, String attribute, boolean defaultValue)
    {
        boolean result = defaultValue;

        if ((attribute != null) && (attribute.length() > 0))
        {
            String value = element.getAttribute(attribute);

            if (value != null)
            {
                if (value.equals(TRUE))
                {
                    result = true;
                }
                else if (value.equals(FALSE))
                {
                    result = false;
                }
            }
        }

        return (result);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Retrieves the value of an integer attribute. If the attribute is not found or the value is
     * non-numeric then the default value is returned.
     *
     * @param element the <code>IXMLElement</code> to search for the attribute.
     * @param attribute the attribute to search for
     * @param defaultValue the default value to use in case the attribute does not exist.
     * @param element the <code>IXMLElement</code> to search for the attribute.
     * @param attribute the attribute to search for
     * @param defaultValue the default value to use in case the attribute does not exist.
     * @param element the <code>IXMLElement</code> to search for the attribute.
     * @param attribute the attribute to search for
     * @param defaultValue the default value to use in case the attribute does not exist.
     * @param element the <code>IXMLElement</code> to search for the attribute.
     * @param attribute the attribute to search for
     * @param defaultValue the default value to use in case the attribute does not exist.
     * @return the value of the attribute. If the attribute is not found or the content is not a
     * legal integer, then the default value is returned.
     */
    /*--------------------------------------------------------------------------*/
    // private int getInt(IXMLElement element, String attribute, int defaultValue)
    // {
    // int result = defaultValue;
    //
    // if ((attribute != null) && (attribute.length() > 0))
    // {
    // try
    // {
    // result = Integer.parseInt(element.getAttribute(attribute));
    // }
    // catch (Throwable exception)
    // {}
    // }
    //
    // return (result);
    // }
    /*--------------------------------------------------------------------------*/
    /**
     * Retrieves the value of a floating point attribute. If the attribute is not found or the value
     * is non-numeric then the default value is returned.
     *
     * @param element the <code>IXMLElement</code> to search for the attribute.
     * @param attribute the attribute to search for
     * @param defaultValue the default value to use in case the attribute does not exist.
     *
     * @return the value of the attribute. If the attribute is not found or the content is not a
     * legal integer, then the default value is returned.
     */
    /*--------------------------------------------------------------------------*/
    private float getFloat(IXMLElement element, String attribute, float defaultValue)
    {
        float result = defaultValue;

        if ((attribute != null) && (attribute.length() > 0))
        {
            try
            {
                result = Float.parseFloat(element.getAttribute(attribute));
            }
            catch (Throwable exception)
            {}
        }

        return (result);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Extracts the text from an <code>IXMLElement</code>. The text must be defined in the resource
     * file under the key defined in the <code>id</code> attribute or as value of the attribute
     * <code>txt</code>.
     *
     * @param element the <code>IXMLElement</code> from which to extract the text.
     * @return The text defined in the <code>IXMLElement</code>. If no text can be located,
     * <code>null</code> is returned.
     */
    /*--------------------------------------------------------------------------*/
    private String getText(IXMLElement element)
    {
        if (element == null) { return (null); }

        String key = element.getAttribute(KEY);
        String text = element.getAttribute(TEXT);
        
        if ((key != null) && (langpack != null))
        {
            try
            {
                String langPackText = langpack.getString(key);
                if (langPackText != null && !key.equals(langPackText)) {
                    text = langPackText;
                }
            }
            catch (Throwable exception)
            {
                // no localized text found
            }
        }

        // try to parse the text, and substitute any variable it finds
        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());

        return (vs.substitute(text, null));
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Retreives the alignment setting for the <code>IXMLElement</code>. The default value in case
     * the <code>ALIGNMENT</code> attribute is not found or the value is illegal is
     * <code>TwoColumnConstraints.LEFT</code>.
     *
     * @param element the <code>IXMLElement</code> from which to extract the alignment setting.
     * @return the alignement setting for the <code>IXMLElement</code>. The value is either
     * <code>TwoColumnConstraints.LEFT</code>, <code>TwoColumnConstraints.CENTER</code> or
     * <code>TwoColumnConstraints.RIGHT</code>.
     * @see com.izforge.izpack.gui.TwoColumnConstraints
     */
    /*--------------------------------------------------------------------------*/
    private int getAlignment(IXMLElement element)
    {
        int result = TwoColumnConstraints.LEFT;

        String value = element.getAttribute(ALIGNMENT);

        if (value != null)
        {
            if (value.equals(LEFT))
            {
                result = TwoColumnConstraints.LEFT;
            }
            else if (value.equals(CENTER))
            {
                result = TwoColumnConstraints.CENTER;
            }
            else if (value.equals(RIGHT))
            {
                result = TwoColumnConstraints.RIGHT;
            }
        }

        return (result);
    }

    /**
     * Verifies if an item is required for the operating system the installer executed. The
     * configuration for this feature is: <br/>
     * &lt;os family="unix"/&gt; <br>
     * <br>
     * <b>Note:</b><br>
     * If the list of the os is empty then <code>true</code> is always returnd.
     *
     * @param os The <code>Vector</code> of <code>String</code>s. containing the os names
     * @return <code>true</code> if the item is required for the os, otherwise returns
     * <code>false</code>.
     */
    public boolean itemRequiredForOs(Vector<IXMLElement> os)
    {
        if (os.size() == 0) { return true; }

        for (int i = 0; i < os.size(); i++)
        {
            String family = (os.elementAt(i)).getAttribute(FAMILY);
            boolean match = false;

            if ("windows".equals(family))
            {
                match = OsVersion.IS_WINDOWS;
            }
            else if ("mac".equals(family))
            {
                match = OsVersion.IS_OSX;
            }
            else if ("unix".equals(family))
            {
                match = OsVersion.IS_UNIX;
            }
            if (match) { return true; }
        }
        return false;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Verifies if an item is required for any of the packs listed. An item is required for a pack
     * in the list if that pack is actually selected for installation. <br>
     * <br>
     * <b>Note:</b><br>
     * If the list of selected packs is empty then <code>true</code> is always returnd. The same is
     * true if the <code>packs</code> list is empty.
     *
     * @param packs a <code>Vector</code> of <code>String</code>s. Each of the strings denotes a
     * pack for which an item should be created if the pack is actually installed.
     * @return <code>true</code> if the item is required for at least one pack in the list,
     * otherwise returns <code>false</code>.
     */
    /*--------------------------------------------------------------------------*/
    /*
     * $ @design
     *
     * The information about the installed packs comes from InstallData.selectedPacks. This assumes
     * that this panel is presented to the user AFTER the PacksPanel.
     * --------------------------------------------------------------------------
     */
    private boolean itemRequiredFor(Vector<IXMLElement> packs)
    {

        String selected;
        String required;

        if (packs.size() == 0) { return (true); }

        // ----------------------------------------------------
        // We are getting to this point if any packs have been
        // specified. This means that there is a possibility
        // that some UI elements will not get added. This
        // means that we can not allow to go back to the
        // PacksPanel, because the process of building the
        // UI is not reversable.
        // ----------------------------------------------------
        // packsDefined = true;

        // ----------------------------------------------------
        // analyze if the any of the packs for which the item
        // is required have been selected for installation.
        // ----------------------------------------------------
        for (int i = 0; i < idata.selectedPacks.size(); i++)
        {
            selected = ((Pack) idata.selectedPacks.get(i)).name;

            for (int k = 0; k < packs.size(); k++)
            {
                required = (packs.elementAt(k)).getAttribute(NAME, "");
                if (selected.equals(required)) { return (true); }
            }
        }

        return (false);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Verifies if an item is required for any of the packs listed. An item is required for a pack
     * in the list if that pack is actually NOT selected for installation. <br>
     * <br>
     * <b>Note:</b><br>
     * If the list of selected packs is empty then <code>true</code> is always returnd. The same is
     * true if the <code>packs</code> list is empty.
     *
     * @param packs a <code>Vector</code> of <code>String</code>s. Each of the strings denotes a
     * pack for which an item should be created if the pack is actually installed.
     * @return <code>true</code> if the item is required for at least one pack in the list,
     * otherwise returns <code>false</code>.
     */
    /*--------------------------------------------------------------------------*/
    /*
     * $ @design
     *
     * The information about the installed packs comes from InstallData.selectedPacks. This assumes
     * that this panel is presented to the user AFTER the PacksPanel.
     * --------------------------------------------------------------------------
     */
    private boolean itemRequiredForUnselected(Vector<IXMLElement> packs)
    {

        String selected;
        String required;

        if (packs.size() == 0) { return (true); }

        // ----------------------------------------------------
        // analyze if the any of the packs for which the item
        // is required have been selected for installation.
        // ----------------------------------------------------
        for (int i = 0; i < idata.selectedPacks.size(); i++)
        {
            selected = ((Pack) idata.selectedPacks.get(i)).name;

            for (int k = 0; k < packs.size(); k++)
            {
                required = (packs.elementAt(k)).getAttribute(NAME, "");
                if (selected.equals(required)) { return (false); }
            }
        }

        return (true);
    }

    // ----------- Inheritance stuff -----------------------------------------
    /**
     * Returns the uiElements.
     *
     * @return Returns the uiElements.
     */
    // protected Vector<Object[]> getUiElements()
    // {
    // return uiElements;
    // }
    // --------------------------------------------------------------------------
    // Inner Classes
    // --------------------------------------------------------------------------
    /*---------------------------------------------------------------------------*/

    /**
     * This class can be used to associate a text string and a (text) value.
     */
    /*---------------------------------------------------------------------------*/
    private static class TextValuePair
    {

        private String text = "";

        private String value = "";

        /*--------------------------------------------------------------------------*/
        /**
         * Constructs a new Text/Value pair, initialized with the text and a value.
         *
         * @param text the text that this object should represent
         * @param value the value that should be associated with this object
         */
        /*--------------------------------------------------------------------------*/
        public TextValuePair(String text, String value)
        {
            this.text = text;
            this.value = value;
        }

        /*--------------------------------------------------------------------------*/
        /**
         * Sets the text
         *
         * @param text the text for this object
         */
        /*--------------------------------------------------------------------------*/
        public void setText(String text)
        {
            this.text = text;
        }

        /*--------------------------------------------------------------------------*/
        /**
         * Sets the value of this object
         *
         * @param value the value for this object
         */
        /*--------------------------------------------------------------------------*/
        public void setValue(String value)
        {
            this.value = value;
        }

        /*--------------------------------------------------------------------------*/
        /**
         * This method returns the text that was set for the object
         *
         * @return the object's text
         */
        /*--------------------------------------------------------------------------*/
        public String toString()
        {
            return (text);
        }

        /*--------------------------------------------------------------------------*/
        /**
         * This method returns the value that was associated with this object
         *
         * @return the object's value
         */
        /*--------------------------------------------------------------------------*/
        public String getValue()
        {
            return (value);
        }
    }

    /*---------------------------------------------------------------------------*/
    /**
     * This class encapsulates a lot of search field functionality.
     * <p/>
     * A search field supports searching directories and files on the target system. This is a
     * helper class to manage all data belonging to a search field.
     */
    /*---------------------------------------------------------------------------*/

    private class SearchField implements ActionListener
    {

        /**
         * used in constructor - we search for a directory.
         */
        public static final int TYPE_DIRECTORY = 1;

        /**
         * used in constructor - we search for a file.
         */
        public static final int TYPE_FILE = 2;

        /**
         * used in constructor - result of search is the directory.
         */
        public static final int RESULT_DIRECTORY = 1;

        /**
         * used in constructor - result of search is the whole file name.
         */
        public static final int RESULT_FILE = 2;

        /**
         * used in constructor - result of search is the parent directory.
         */
        public static final int RESULT_PARENTDIR = 3;

        private String filename = null;

        private String checkFilename = null;

        private JButton autodetectButton = null;

        private JButton browseButton = null;

        private JComboBox pathComboBox = null;

        private int searchType = TYPE_DIRECTORY;

        private int resultType = RESULT_DIRECTORY;

        private InstallerFrame parent = null;

        /*---------------------------------------------------------------------------*/
        /**
         * Constructor - initializes the object, adds it as action listener to the "autodetect"
         * button.
         *
         * @param filename the name of the file to search for (might be null for searching
         * directories)
         * @param checkFilename the name of the file to check when searching for directories (the
         * checkFilename is appended to a found directory to figure out whether it is the right
         * directory)
         * @param combobox the <code>JComboBox</code> holding the list of choices; it should be
         * editable and contain only Strings
         * @param autobutton the autodetection button for triggering autodetection
         * @param browsebutton the browse button to look for the file
         * @param search_type what to search for - TYPE_FILE or TYPE_DIRECTORY
         * @param result_type what to return as the result - RESULT_FILE or RESULT_DIRECTORY or
         * RESULT_PARENTDIR
         */
        /*---------------------------------------------------------------------------*/
        public SearchField(String filename, String checkFilename, InstallerFrame parent,
                JComboBox combobox, JButton autobutton, JButton browsebutton, int search_type,
                int result_type)
        {
            this.filename = filename;
            this.checkFilename = checkFilename;
            this.parent = parent;
            this.autodetectButton = autobutton;
            this.browseButton = browsebutton;
            this.pathComboBox = combobox;
            this.searchType = search_type;
            this.resultType = result_type;

            this.autodetectButton.addActionListener(this);
            this.browseButton.addActionListener(this);

            /*
             * add DocumentListener to manage nextButton if user enters input
             */
            ((JTextField) this.pathComboBox.getEditor().getEditorComponent()).getDocument()
                    .addDocumentListener(new DocumentListener() {

                        public void changedUpdate(DocumentEvent e)
                        {
                            checkNextButtonState();
                        }

                        public void insertUpdate(DocumentEvent e)
                        {
                            checkNextButtonState();
                        }

                        public void removeUpdate(DocumentEvent e)
                        {
                            checkNextButtonState();
                        }

                        private void checkNextButtonState()
                        {
                            Document doc = ((JTextField) pathComboBox.getEditor()
                                    .getEditorComponent()).getDocument();
                            try
                            {
                                if (pathMatches(doc.getText(0, doc.getLength())))
                                {
                                    getInstallerFrame().unlockNextButton(false);
                                }
                                else
                                {
                                    getInstallerFrame().lockNextButton();
                                }
                            }
                            catch (BadLocationException e)
                            {/* ignore, it not happens */}
                        }
                    });

            autodetect();
        }

        /**
         * convenient method
         */
        private InstallerFrame getInstallerFrame()
        {
            return parent;
        }

        /**
         * Check whether the given combobox belongs to this searchfield. This is used when reading
         * the results.
         */
        public boolean belongsTo(JComboBox combobox)
        {
            return (this.pathComboBox == combobox);
        }

        /**
         * check whether the given path matches
         */
        private boolean pathMatches(String path)
        {
            if (path != null)
            { // Make sure, path is not null
                File file = null;

                if ((this.filename == null) || (this.searchType == TYPE_DIRECTORY))
                {
                    file = new File(path);
                }
                else
                {
                    file = new File(path, this.filename);
                }

                if (file.exists())
                {

                    if (((this.searchType == TYPE_DIRECTORY) && (file.isDirectory()))
                            || ((this.searchType == TYPE_FILE) && (file.isFile())))
                    {
                        // no file to check for
                        if (this.checkFilename == null) { return true; }

                        file = new File(file, this.checkFilename);

                        return file.exists();
                    }

                }

                // System.out.println (path + " did not match");
            } // end if
            return false;
        }

        /**
         * perform autodetection
         */
        public boolean autodetect()
        {
            Vector<String> items = new Vector<String>();

            /*
             * Check if the user has entered data into the ComboBox and add it to the Itemlist
             */
            String selected = (String) this.pathComboBox.getSelectedItem();
            if (selected == null)
            {
                parent.lockNextButton();
                return false;
            }
            boolean found = false;
            for (int x = 0; x < this.pathComboBox.getItemCount(); x++)
            {
                if (this.pathComboBox.getItemAt(x).equals(selected))
                {
                    found = true;
                }
            }
            if (!found)
            {
                // System.out.println("Not found in Itemlist");
                this.pathComboBox.addItem(this.pathComboBox.getSelectedItem());
            }

            // Checks whether a placeholder item is in the combobox
            // and resolve the pathes automatically:
            // /usr/lib/* searches all folders in usr/lib to find
            // /usr/lib/*/lib/tools.jar
            VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
            for (int i = 0; i < this.pathComboBox.getItemCount(); ++i)
            {
                String path = vs.substitute((String) this.pathComboBox.getItemAt(i), null);
                // System.out.println ("autodetecting " + path);

                if (path.endsWith("*"))
                {
                    path = path.substring(0, path.length() - 1);
                    File dir = new File(path);

                    if (dir.isDirectory())
                    {
                        File[] subdirs = dir.listFiles();
                        for (File subdir : subdirs)
                        {
                            String search = subdir.getAbsolutePath();
                            if (this.pathMatches(search))
                            {
                                items.add(search);
                            }
                        }
                    }
                }
                else
                {
                    if (this.pathMatches(path))
                    {
                        items.add(path);
                    }
                }
            }
            // Make the enties in the vector unique
            items = new Vector<String>(new HashSet<String>(items));

            // Now clear the combobox and add the items out of the newly
            // generated vector
            this.pathComboBox.removeAllItems();
            for (String item : items)
            {
                String res = vs.substitute(item, "plain");
                // System.out.println ("substitution " + item + ", result " + res);
                this.pathComboBox.addItem(res);
            }

            // loop through all items
            for (int i = 0; i < this.pathComboBox.getItemCount(); ++i)
            {
                String path = (String) this.pathComboBox.getItemAt(i);

                if (this.pathMatches(path))
                {
                    this.pathComboBox.setSelectedIndex(i);
                    parent.unlockNextButton();
                    return true;
                }

            }

            // if the user entered something else, it's not listed as an item
            if (this.pathMatches((String) this.pathComboBox.getSelectedItem()))
            {
                parent.unlockNextButton();
                return true;
            }
            parent.lockNextButton();
            return false;
        }

        /*--------------------------------------------------------------------------*/
        /**
         * This is called if one of the buttons has been pressed.
         * <p/>
         * It checks, which button caused the action and acts accordingly.
         */
        /*--------------------------------------------------------------------------*/
        public void actionPerformed(ActionEvent event)
        {
            // System.out.println ("autodetection button pressed.");

            if (event.getSource() == this.autodetectButton)
            {
                if (!autodetect())
                {
                    showMessageDialog(parent, "UserInputPanel.search.autodetect.failed.message",
                            "UserInputPanel.search.autodetect.failed.caption",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
            else if (event.getSource() == this.browseButton)
            {
                JFileChooser chooser = new JFileChooser();

                if (this.resultType != TYPE_FILE)
                {
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                }

                int result = chooser.showOpenDialog(this.parent);

                if (result == JFileChooser.APPROVE_OPTION)
                {
                    File f = chooser.getSelectedFile();

                    this.pathComboBox.setSelectedItem(f.getAbsolutePath());

                    // use any given directory directly
                    if (this.resultType != TYPE_FILE && !this.pathMatches(f.getAbsolutePath()))
                    {
                        showMessageDialog(parent, "UserInputPanel.search.wrongselection.message",
                                "UserInputPanel.search.wrongselection.caption",
                                JOptionPane.WARNING_MESSAGE);

                    }
                }

            }

            // we don't care for anything more here - getResult() does the rest
        }

        /*--------------------------------------------------------------------------*/
        /**
         * Return the result of the search according to result type.
         * <p/>
         * Sometimes, the whole path of the file is wanted, sometimes only the directory where the
         * file is in, sometimes the parent directory.
         *
         * @return null on error
         */
        /*--------------------------------------------------------------------------*/
        public String getResult()
        {
            String item = (String) this.pathComboBox.getSelectedItem();
            if (item != null)
            {
                item = item.trim();
            }
            String path = item;

            File f = new File(item);

            if (!f.isDirectory())
            {
                path = f.getParent();
            }

            // path now contains the final content of the combo box
            if (this.resultType == RESULT_DIRECTORY)
            {
                return path;
            }
            else if (this.resultType == RESULT_FILE)
            {
                if (this.filename != null)
                {
                    return path + File.separatorChar + this.filename;
                }
                else
                {
                    return item;
                }
            }
            else if (this.resultType == RESULT_PARENTDIR)
            {
                File dir = new File(path);
                return dir.getParent();
            }

            return null;
        }

    } // private class SearchFile

    protected void updateVariables()
    {
        /**
         * Look if there are new variables defined
         */
        Vector<IXMLElement> variables = spec.getChildrenNamed(VARIABLE_NODE);
        RulesEngine rules = parent.getRules();

        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
        for (int i = 0; i < variables.size(); i++)
        {
            IXMLElement variable = variables.elementAt(i);
            String vname = variable.getAttribute(ATTRIBUTE_VARIABLE_NAME);
            String vvalue = variable.getAttribute(ATTRIBUTE_VARIABLE_VALUE);

            if (vvalue == null)
            {
                // try to read value element
                if (variable.hasChildren())
                {
                    IXMLElement value = variable.getFirstChildNamed("value");
                    vvalue = value.getContent();
                }
            }

            String conditionid = variable.getAttribute(ATTRIBUTE_CONDITIONID_NAME);
            if (conditionid != null)
            {
                // check if condition for this variable is fulfilled
                if (!rules.isConditionTrue(conditionid, idata.getVariables()))
                {
                    continue;
                }
            }
            // are there any OS-Constraints?
            if (OsConstraint.oneMatchesCurrentSystem(variable))
            {
                if (vname == null)
                {}
                else
                {
                    // vname is given
                    if (vvalue != null)
                    {
                        // try to substitute variables in value field
                        vvalue = vs.substitute(vvalue, null);
                        // to cut out circular references
                        idata.setVariable(vname, "");
                        vvalue = vs.substitute(vvalue, null);
                    }
                    // try to set variable
                    idata.setVariable(vname, vvalue);

                    // for save this variable to be used later by Automation Helper
                    entries.add(new TextValuePair(vname, vvalue));
                }
            }
        }
    }

    // Repaint all controls and validate them agains the current variables
    public void actionPerformed(ActionEvent e)
    {
        // validating = false;
        // readInput();
        // panelActivate();
        // validating = true;
        Debug.trace("Setting validating to false");
        validating=false;
        updateDialog();
        Debug.trace("Setting validating back to true");
        validating=true;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Show localized message dialog basing on given parameters.
     *
     * @param parentFrame The parent frame.
     * @param message The message to print out in dialog box.
     * @param caption The caption of dialog box.
     * @param messageType The message type (JOptionPane.*_MESSAGE)
     */
    /*--------------------------------------------------------------------------*/
    private void showMessageDialog(InstallerFrame parentFrame, String message, String caption,
            int messageType)
    {
        String localizedMessage = parentFrame.langpack.getString(message);
        if ((localizedMessage == null) || (localizedMessage.trim().length() == 0))
        {
            localizedMessage = message;
        }
        String localizedCaption = parentFrame.langpack.getString(caption);
        if ((localizedCaption == null) || (localizedCaption.trim().length() == 0))
        {
            localizedCaption = caption;
        }
        JOptionPane.showMessageDialog(parentFrame, localizedMessage, localizedCaption, messageType);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Show localized warning message dialog basing on given parameters.
     *
     * @param parentFrame parent frame.
     * @param message the message to print out in dialog box.
     */
    /*--------------------------------------------------------------------------*/
    private void showWarningMessageDialog(InstallerFrame parentFrame, String message)
    {
        showMessageDialog(parentFrame, message, "UserInputPanel.error.caption",
                JOptionPane.WARNING_MESSAGE);
    }

    public void itemStateChanged(ItemEvent arg0)
    {
        updateDialog();
    }

    private void updateDialog()
    {
        if (this.eventsActivated)
        {
            this.eventsActivated = false;
            if (isValidated())
            {
                // read input
                // and update elements
                // panelActivate();
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

    public void focusGained(FocusEvent e)
    {
        // TODO Auto-generated method stub

    }

    public void focusLost(FocusEvent e)
    {
        updateDialog();
    }

} // public class UserInputPanel

/*---------------------------------------------------------------------------*/
class UserInputFileFilter extends FileFilter
{

    String fileext = "";

    String description = "";

    public void setFileExt(String fileext)
    {
        this.fileext = fileext;
    }

    public void setFileExtDesc(String desc)
    {
        this.description = desc;
    }

    public boolean accept(File pathname)
    {
        if (pathname.isDirectory())
        {
            return true;
        }
        else
        {
            return pathname.getAbsolutePath().endsWith(this.fileext);
        }
    }

    public String getDescription()
    {
        return this.description;
    }
}

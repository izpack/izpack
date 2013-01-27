/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Jan Blok
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
package com.izforge.izpack.panels;

import static com.izforge.izpack.panels.UserInputPanel.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.Pack;
import com.izforge.izpack.Panel;
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
import com.izforge.izpack.installer.ResourceManager;
import com.izforge.izpack.installer.ResourceNotFoundException;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.SpecHelper;
import com.izforge.izpack.util.VariableSubstitutor;

/**
 * The user input panel console helper class.
 *
 * @author Mounir El Hajj
 */
public class UserInputPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole
{

    protected int instanceNumber = 0;

    private static int instanceCount = 0;

    private static final String SPEC_FILE_NAME = "userInputSpec.xml";
    private static final String LANG_FILE_NAME = "userInputLang.xml";

    private static final String NODE_ID = "panel";

    private static final String INSTANCE_IDENTIFIER = "order";

    protected static final String PANEL_IDENTIFIER = "id";

    private static final String FIELD_NODE_ID = "field";

    protected static final String ATTRIBUTE_CONDITIONID_NAME = "conditionid";

    private static final String VARIABLE = "variable";

    private static final String SET = "set";

    private static final String TEXT = "txt";

    private static final String SPEC = "spec";

    private static final String PWD = "pwd";

    private static final String TYPE_ATTRIBUTE = "type";

    private static final String TEXT_FIELD = "text";

    private static final String COMBO_FIELD = "combo";

    private static final String STATIC_TEXT = "staticText";

    private static final String CHOICE = "choice";

    private static final String DIR = "dir";

    private static final String FILE = "file";

    private static final String PASSWORD = "password";

    private static final String VALUE = "value";

    private static final String RADIO_FIELD = "radio";

    private static final String TITLE_FIELD = "title";

    private static final String CHECK_FIELD = "check";

    private static final String RULE_FIELD = "rule";

    private static final String SPACE = "space";

    private static final String DIVIDER = "divider";

    static final String DISPLAY_FORMAT = "displayFormat";

    static final String PLAIN_STRING = "plainString";

    static final String SPECIAL_SEPARATOR = "specialSeparator";

    static final String LAYOUT = "layout";

    static final String RESULT_FORMAT = "resultFormat";

    private static final String DESCRIPTION = "description";

    private static final String TRUE = "true";

    private static final String NAME = "name";

    private static final String FAMILY = "family";

    private static final String OS = "os";

    private static final String SELECTEDPACKS = "createForPack";


    private static Input SPACE_INTPUT_FIELD = new Input(SPACE, null, null, SPACE, "\r", 0);
    private static Input DIVIDER_INPUT_FIELD = new Input(DIVIDER, null, null, DIVIDER, "------------------------------------------", 0);

    public List<Input> listInputs;

    public UserInputPanelConsoleHelper()
    {
        instanceNumber = instanceCount++;
        listInputs = new ArrayList<Input>();

    }

    private List<ValidatorContainer> analyzeValidator(IXMLElement specElement, AutomatedInstallData idata)
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
                String message = getText(element, idata);
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

    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p)
    {

        collectInputs(installData);
        Iterator<Input> inputIterator = listInputs.iterator();
        while (inputIterator.hasNext())
        {
            String strVariableName = ((Input) inputIterator.next()).strVariableName;
            if (strVariableName != null)
            {
                String strVariableValue = p.getProperty(strVariableName);
                if (strVariableValue != null)
                {
                    installData.setVariable(strVariableName, strVariableValue);
                }
            }
        }
        return true;
    }

    public boolean runGeneratePropertiesFile(AutomatedInstallData installData,
            PrintWriter printWriter)
    {

        collectInputs(installData);
        Iterator<Input> inputIterator = listInputs.iterator();
        while (inputIterator.hasNext())
        {
            Input input = (Input) inputIterator.next();
            if (input.strVariableName != null)
            {
                printWriter.println(input.strVariableName + "=");
            }
        }
        return true;
    }

    public boolean runConsole(AutomatedInstallData idata)
    {

        try
        {
            String resource = LANG_FILE_NAME + "_" + idata.localeISO3;
            idata.langpack.add(ResourceManager.getInstance().getInputStream(resource));
        }
        catch (ResourceNotFoundException e)
        {
            Debug.trace(e);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        boolean processpanel = collectInputs(idata);
        if (!processpanel) {
            return true;
        }
        boolean status = true;
        Iterator<Input> inputsIterator = listInputs.iterator();
        try
        {
            while (inputsIterator.hasNext())
            {
                Input input = inputsIterator.next();

                if (TEXT_FIELD.equals(input.strFieldType)
            		|| FILE.equals(input.strFieldType)
            		|| RULE_FIELD.equals(input.strFieldType)
                    || DIR.equals(input.strFieldType))
                {
                    status = status && processTextField(input, idata);
                }
                else if (COMBO_FIELD.equals(input.strFieldType)
                        || RADIO_FIELD.equals(input.strFieldType))
                {
                    status = status && processComboRadioField(input, idata);
                }
                else if (CHECK_FIELD.equals(input.strFieldType))
                {
                    status = status && processCheckField(input, idata);
                }
               else if(STATIC_TEXT.equals(input.strFieldType)
            		   || TITLE_FIELD.equals(input.strFieldType)
            		   || DIVIDER.equals(input.strFieldType)
            		   || SPACE.equals(input.strFieldType) )
               {
            	   status = status && processSimpleField(input, idata);
               }
               else if (PASSWORD.equals(input.strFieldType) ) {
                   status = status && processPasswordField(input, idata);
               }

            }
        }
        catch (RevalidationTriggeredException e)
        {
            return runConsole(idata);
        }

        int i = askEndOfConsolePanel(idata);
        if (i == 1)
        {
            return true;
        }
        else if (i == 2)
        {
            return false;
        }
        else
        {
            return runConsole(idata);
        }

    }

    public boolean collectInputs(AutomatedInstallData idata)
    {

        listInputs.clear();
        IXMLElement data;
        IXMLElement spec = null;
        Vector<IXMLElement> specElements;
        String attribute;
        String dataID;
        String panelid = ((Panel) idata.panelsOrder.get(idata.curPanelNumber)).getPanelid();
        String instance = Integer.toString(instanceNumber);

        SpecHelper specHelper = new SpecHelper();
        try
        {
            specHelper.readSpec(specHelper.getResource(SPEC_FILE_NAME));
        }
        catch (Exception e1)
        {

            e1.printStackTrace();
            return false;
        }

        specElements = specHelper.getSpec().getChildrenNamed(NODE_ID);
        for (int i = 0; i < specElements.size(); i++)
        {
            data = specElements.elementAt(i);
            attribute = data.getAttribute(INSTANCE_IDENTIFIER);
            dataID = data.getAttribute(PANEL_IDENTIFIER);
            if (((attribute != null) && instance.equals(attribute))
                    || ((dataID != null) && (panelid != null) && (panelid.equals(dataID))))
            {

                Vector<IXMLElement> forPacks = data.getChildrenNamed(SELECTEDPACKS);
                Vector<IXMLElement> forOs = data.getChildrenNamed(OS);

                if (itemRequiredFor(forPacks, idata) && itemRequiredForOs(forOs)) {
                    spec = data;
                    break;
                }
            }
        }

        if (spec == null) {
            return false;
        }
        Vector<IXMLElement> fields = spec.getChildrenNamed(FIELD_NODE_ID);
        for (int i = 0; i < fields.size(); i++)
        {
            IXMLElement field = fields.elementAt(i);

            Vector<IXMLElement> forPacks = field.getChildrenNamed(SELECTEDPACKS);
            Vector<IXMLElement> forOs = field.getChildrenNamed(OS);

            if (itemRequiredFor(forPacks, idata) && itemRequiredForOs(forOs)) {

                String conditionid = field.getAttribute(ATTRIBUTE_CONDITIONID_NAME);
                if (conditionid != null)
                {
                    // check if condition is fulfilled
                    if (!idata.getRules().isConditionTrue(conditionid, idata.getVariables()))
                    {
                        continue;
                    }
                }
                Input in = getInputFromField(field, idata);
                if (in != null) {
                    in.validators = analyzeValidator(field, idata);
                    if (in instanceof Password)
                    {
                        for (Input singleInput : ((Password) in).input)
                        {
                            singleInput.validators = in.validators;
                        }
                    }
                	listInputs.add(in);
                }
            }
         }
        return true;
    }

    boolean processSimpleField(Input input, AutomatedInstallData idata)
    {
        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
        System.out.println(vs.substitute(input.strText, null));
        return true;
    }

    boolean processPasswordField(Input input, AutomatedInstallData idata) {

        Password pwd = (Password) input;

        boolean rtn = false;
        List<String> values = new LinkedList<String>();
        for (int i = 0; i < pwd.input.length; i++)
        {
            while (true)
            {
                boolean done = true;
                rtn = processTextField(pwd.input[i], idata);
                if (!rtn) return rtn;
                values.add(idata.getVariable(pwd.input[i].strVariableName));
                if (i > 0 && pwd.validators != null && !pwd.validators.isEmpty())
                {
                    MultipleFieldValidator validation = new MultipleFieldValidator(values,
                            pwd.validators);
                    if (!validation.validate())
                    {
                        values.remove(values.size() - 1);
                        done = false;
                        System.out.println("Validation failed, please verify your input.");
                        System.out.println("Validation error: " + validation.getValidationMessage());
                    }
                }
                if (done)
                {
                    break;
                }
            }
        }

        return rtn;

    }

    boolean processTextField(Input input, AutomatedInstallData idata)
    {
        String variable = input.strVariableName;
        String set;
        String fieldText;
        if ((variable == null) || (variable.length() == 0)) { return false; }

        if (input.listChoices.size() == 0)
        {
            Debug.trace("Error: no spec element defined in file field");
            return false;
        }

        set = input.strDefaultValue;
        if (set == null)
        {
            set = idata.getVariable(variable);
            if (set == null)
            {
                set = "";
            }
        }

        if (set != null && !"".equals(set))
        {
            VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
            set = vs.substitute(set, null);
        }

        fieldText = input.listChoices.get(0).strText;
        String value = set;
        while (true) {
            boolean done = false;
            System.out.println(fieldText + " [" + set + "] ");
            try
            {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String strIn = br.readLine();
                value = !strIn.trim().equals("") ? strIn : set;

                // avant les validators
                // pour les types directory
                // il faut tester les paramètres mustExist et canCreate

                boolean bokForInputDir = true;

                if (DIR.equals(input.strFieldType) )
                {

                    java.io.File dir = new File (value).getAbsoluteFile();
                    value=dir.getAbsolutePath();

                    boolean mustExist = input.mustExist;
                    boolean canCreate = input.canCreate;

                    if (dir.isDirectory())
                    {
                        bokForInputDir = true;
                    }
                    else if (mustExist)
                    {
                        bokForInputDir = false;
                    }
                    else if (canCreate)
                    {
                        // try to create the directory, if requested
                        if (!dir.exists())
                        {
                            int nRet = emitWarning(idata, idata.langpack.getString("installer.warning"), idata.langpack.getString("TargetPanel.createdir")+ " " + value);
                            if ( nRet==3)
                            {
                                return false;
                            }
                            else if ( nRet==2)
                            {
                                bokForInputDir = false;
                            }
                            else bokForInputDir = true;
                        }

                        if (bokForInputDir)
                        {
                            // We assume, that we would install something into this dir
                            if (!isWriteable(dir.toString()))
                            {
                                emitError(idata, idata.langpack.getString("installer.error"), idata.langpack.getString("TargetPanel.notwritable"));
                                bokForInputDir = false;
                            }
                            else
                            {
                                bokForInputDir = dir.mkdirs();
                            }
                        }
                    }
                    else
                    {
                        bokForInputDir = true;
                    }
                }


                if (bokForInputDir)
                {
                    done = true;
                    if (input.validators != null && !input.validators.isEmpty())
                    {
                        StringInputProcessingClient validation = new StringInputProcessingClient(value, input.validators);
                        if (!validation.validate()) {
                            done = false;
                            System.out.println("Validation failed, please verify your input.");
                            System.out.println("Validation error: " + validation.getValidationMessage());
                        }
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
                done = false;
            }

            if (done)
            {
                break;
            }
        }

        idata.setVariable(variable, value);

        return true;

    }

    boolean processComboRadioField(Input input, AutomatedInstallData idata)
    {// TODO protection if selection not valid and no set value
        String variable = input.strVariableName;
        if ((variable == null) || (variable.length() == 0)) { return false; }
        String currentvariablevalue = idata.getVariable(variable);
		  //If we dont do this, choice with index=0 will always be displayed, no matter what is selected
		  input.iSelectedChoice = -1;
        boolean userinput = false;

        // display the description for this combo or radio field
        if (input.strText != null) {
            System.out.println(input.strText);
        }

        List<Choice> lisChoices = input.listChoices;
        if (lisChoices.size() == 0)
        {
            Debug.trace("Error: no spec element defined in file field");
            return false;
        }
        if (currentvariablevalue != null)
        {
            userinput = true;
        }
        for (int i = 0; i < lisChoices.size(); i++)
        {
            Choice choice = lisChoices.get(i);
            String value = choice.strValue;
            // if the choice value is provided via a property to the process, then
            // set it as the selected choice, rather than defaulting to what the
            // spec defines.
            if (userinput)
            {
                if ((value != null) && (value.length() > 0) && (currentvariablevalue.equals(value)))
                {
                    input.iSelectedChoice = i;
                }
            }
            else
            {
                String set = choice.strSet;
                if (set != null)
                {
                    if (set != null && !"".equals(set))
                    {
                        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
                        set = vs.substitute(set, null);
                    }
                    if (set.equals(TRUE))
                    {
                        input.iSelectedChoice = i;
                    }
                }
            }
            System.out.println(i + "  [" + (input.iSelectedChoice == i ? "x" : " ") + "] "
                    + (choice.strText != null ? choice.strText : ""));
        }

        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            boolean bKeepAsking = true;

            while (bKeepAsking)
            {
                System.out.println("input selection:");
                String strIn = br.readLine();
                // take default value if default value exists and no user input
                if (strIn.trim().equals("") && input.iSelectedChoice != -1)
                {
                    bKeepAsking = false;
                }
                int j = -1;
                try
                {
                    j = Integer.valueOf(strIn).intValue();
                }
                catch (Exception ex)
                {}
                // take user input if user input is valid
                if (j >= 0 && j < lisChoices.size())
                {
                    input.iSelectedChoice = j;
                    bKeepAsking = false;
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        String newValue = input.listChoices.get(input.iSelectedChoice).strValue;
        idata.setVariable(variable, newValue);
        if (input.revalidate && !currentvariablevalue.equals(newValue)) {
            throw new RevalidationTriggeredException();
        }
        return true;

    }

    boolean processCheckField(Input input, AutomatedInstallData idata)
    {
        String variable = input.strVariableName;
        if ((variable == null) || (variable.length() == 0)) { return false; }
        String currentvariablevalue = idata.getVariable(variable);
        if (currentvariablevalue == null)
        {
            currentvariablevalue = "";
        }
        List<Choice> lisChoices = input.listChoices;
        if (lisChoices.size() == 0)
        {
            Debug.trace("Error: no spec element defined in check field");
            return false;
        }
        Choice choice = null;
        for (int i = 0; i < lisChoices.size(); i++)
        {
            choice = lisChoices.get(i);
            String value = choice.strValue;

            if ((value != null) && (value.length() > 0) && (currentvariablevalue.equals(value)))
            {
                input.iSelectedChoice = i;
            }
            else
            {
                String set = input.strDefaultValue;
                if (set != null)
                {
                    if (set != null && !"".equals(set))
                    {
                        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
                        set = vs.substitute(set, null);
                    }
                    if (set.equals(TRUE))
                    {
                        input.iSelectedChoice = 1;
                    }
                }
            }
        }
        System.out.println("  [" + (input.iSelectedChoice == 1 ? "x" : " ") + "] "
                + (choice.strText != null ? choice.strText : ""));
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            boolean bKeepAsking = true;

            while (bKeepAsking)
            {
                System.out.println("input 1 to select, 0 to deselect:");
                String strIn = br.readLine();
                // take default value if default value exists and no user input
                if (strIn.trim().equals(""))
                {
                    bKeepAsking = false;
                }
                int j = -1;
                try
                {
                    j = Integer.valueOf(strIn).intValue();
                }
                catch (Exception ex)
                {}
                // take user input if user input is valid
                if ((j == 0) || j == 1)
                {
                    input.iSelectedChoice = j;
                    bKeepAsking = false;
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        String newValue = input.listChoices.get(input.iSelectedChoice).strValue;
        idata.setVariable(variable, newValue);
        if (input.revalidate && !currentvariablevalue.equals(newValue)) {
            throw new RevalidationTriggeredException();
        }
        return true;

    }

    public Input getInputFromField(IXMLElement field, AutomatedInstallData idata)
    {
        String strVariableName = field.getAttribute(VARIABLE);
        String strFieldType = field.getAttribute(TYPE_ATTRIBUTE);
        if (TITLE_FIELD.equals(strFieldType))
        {
            String strText = null;
            strText = getText(field, idata);
            return new Input(strVariableName, null, null, TITLE_FIELD, strText, 0);
        }

        if (STATIC_TEXT.equals(strFieldType))
        {
            String strText = null;
            strText = field.getAttribute(TEXT);
            return new Input(strVariableName, null, null, STATIC_TEXT, strText, 0);
        }

        if (TEXT_FIELD.equals(strFieldType) || FILE.equals(strFieldType) || DIR.equals(strFieldType))
        {
            List<Choice> choicesList = new ArrayList<Choice>();
            String strFieldText = null;
            String strSet = null;
            String strText = null;
            IXMLElement spec = field.getFirstChildNamed(SPEC);
            IXMLElement description = field.getFirstChildNamed(DESCRIPTION);
            if (spec != null)
            {
                strText = getText(spec, idata);
                strSet = spec.getAttribute(SET);
            }
            if (description != null)
            {
                strFieldText = description.getAttribute(TEXT);
            }
            choicesList.add(new Choice(strText, null, strSet));

            if (DIR.equals(strFieldType))
            {
                boolean mustExist = true;
                boolean create = true;
                mustExist = Boolean.parseBoolean(spec.getAttribute("mustExist", "true"));
                create = Boolean.parseBoolean(spec.getAttribute("create", "false"));
                return new Input(strVariableName, strSet, choicesList, strFieldType, strFieldText, 0, mustExist, create);
            }
            else
            {
                return new Input(strVariableName, strSet, choicesList, strFieldType, strFieldText, 0);

            }



        }

        if (RULE_FIELD.equals(strFieldType))
        {

            List<Choice> choicesList = new ArrayList<Choice>();
            String strFieldText = null;
            String strSet = null;
            String strText = null;
            IXMLElement spec = field.getFirstChildNamed(SPEC);
            IXMLElement description = field.getFirstChildNamed(DESCRIPTION);
            if (spec != null)
            {
                strText = spec.getAttribute(TEXT);
                strSet = spec.getAttribute(SET);
            }
            if (description != null)
            {
                strFieldText = description.getAttribute(TEXT);
            }
            if (strSet != null && spec.getAttribute(LAYOUT) != null)
            {
                StringTokenizer layoutTokenizer = new StringTokenizer(spec.getAttribute(LAYOUT));
                List<String> listSet = Arrays.asList(new String[layoutTokenizer.countTokens()]);
                StringTokenizer setTokenizer = new StringTokenizer(strSet);
                String token;
                while (setTokenizer.hasMoreTokens())
                {
                    token = setTokenizer.nextToken();
                    if (token.indexOf(":") > -1)
                    {
                        listSet.set(new Integer(token.substring(0, token.indexOf(":"))).intValue(),
                                token.substring(token.indexOf(":") + 1));
                    }
                }

                int iCounter = 0;
                StringBuffer sb = new StringBuffer();
                String strRusultFormat = spec.getAttribute(RESULT_FORMAT);
                String strSpecialSeparator = spec.getAttribute(SPECIAL_SEPARATOR);
                while (layoutTokenizer.hasMoreTokens())
                {
                    token = layoutTokenizer.nextToken();
                    if (token.matches(".*:.*:.*"))
                    {
                        sb.append(listSet.get(iCounter) != null ? listSet.get(iCounter) : "");
                        iCounter++;
                    }
                    else
                    {
                        if (SPECIAL_SEPARATOR.equals(strRusultFormat))
                        {
                            sb.append(strSpecialSeparator);
                        }
                        else if (PLAIN_STRING.equals(strRusultFormat))
                        {

                        }
                        else
                        // if (DISPLAY_FORMAT.equals(strRusultFormat))
                        {
                            sb.append(token);
                        }

                    }
                }
                strSet = sb.toString();
            }
            choicesList.add(new Choice(strText, null, strSet));
            return new Input(strVariableName, strSet, choicesList, TEXT_FIELD, strFieldText, 0);

        }

        if (COMBO_FIELD.equals(strFieldType) || RADIO_FIELD.equals(strFieldType))
        {
            List<Choice> choicesList = new ArrayList<Choice>();
            String strFieldText = null;
            int selection = -1;
            IXMLElement spec = field.getFirstChildNamed(SPEC);
            IXMLElement description = field.getFirstChildNamed(DESCRIPTION);
            Vector<IXMLElement> choices = null;
            if (spec != null)
            {
                choices = spec.getChildrenNamed(CHOICE);
            }
            if (description != null)
            {
                strFieldText = description.getAttribute(TEXT);
            }
            for (int i = 0; i < choices.size(); i++)
            {

                IXMLElement choice = choices.elementAt(i);
                String processorClass = choice.getAttribute("processor");

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
                    String set = choice.getAttribute(SET);
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
                        String choiceSet = null;
                        if (token.equals(set)
                                ) {
                            choiceSet="true";
                            selection=counter;
                        }
                        choicesList.add(new Choice(
                                    token,
                                    token,
                                    choiceSet));
                        counter++;

                    }
                }
                else
                {
                    String value = choice.getAttribute(VALUE);

                    String set = choice.getAttribute(SET);
                     if (set != null)
                    {
                        if (set != null && !"".equals(set))
                        {
                            VariableSubstitutor vs = new VariableSubstitutor(idata
                                    .getVariables());
                            set = vs.substitute(set, null);
                        }
                        if (set.equalsIgnoreCase(TRUE))
                        {
                            selection=i;

                        }
                    }


                    choicesList.add(new Choice(
                                choice.getAttribute(TEXT),
                                value,
                                set));

                    }
                }

            if (choicesList.size() == 1) {
                selection = 0;
            }

            return new Input(strVariableName, null, choicesList, strFieldType, strFieldText, selection, "yes".equals(spec.getAttribute("revalidate")));
        }

        if (CHECK_FIELD.equals(strFieldType))
        {
            List<Choice> choicesList = new ArrayList<Choice>();
            String strFieldText = null;
            String strSet = null;
            String strText = null;
            int iSelectedChoice = 0;
            IXMLElement spec = field.getFirstChildNamed(SPEC);
            IXMLElement description = field.getFirstChildNamed(DESCRIPTION);
            if (spec != null)
            {
                strText = getText (spec, idata);
                strSet = spec.getAttribute(SET);
                choicesList.add(new Choice(strText, spec.getAttribute("false"), null));
                choicesList.add(new Choice(strText, spec.getAttribute("true"), null));
                if (strSet != null)
                {
                    if (strSet.equalsIgnoreCase(TRUE))
                    {
                        iSelectedChoice = 1;
                    }
                }
            }
            else
            {
                System.out.println("No spec specified for input of type check");
            }

            if (description != null)
            {
                strFieldText = description.getAttribute(TEXT);
            }
            return new Input(strVariableName, strSet, choicesList, CHECK_FIELD, strFieldText,
                    iSelectedChoice, "yes".equals(spec.getAttribute("revalidate")));
        }


        if (SPACE.equals(strFieldType) )
        {
            return SPACE_INTPUT_FIELD;

        }

        if (DIVIDER.equals(strFieldType))
        {
            return DIVIDER_INPUT_FIELD;
        }


        if (PASSWORD.equals(strFieldType))
        {
            List<Choice> choicesList = new ArrayList<Choice>();
            String strFieldText = null;
            String strSet = null;
            String strText = null;

            IXMLElement spec = field.getFirstChildNamed(SPEC);
            if (spec != null)
            {

                Vector<IXMLElement> pwds = spec.getChildrenNamed(PWD);
                if (pwds == null || pwds.size() == 0) {
                    System.out.println("No pwd specified in the spec for type password");
                    return null;
                }

                Input[] inputs = new Input[pwds.size()];
                for (int i = 0; i < pwds.size(); i++)
                {

                    IXMLElement pwde = pwds.elementAt(i);
                    strText = pwde.getAttribute(TEXT);
                    strSet = pwde.getAttribute(SET);
                    choicesList.add(new Choice(strText, null, strSet));
                    inputs[i] = new Input(strVariableName, strSet, choicesList, strFieldType, strFieldText, 0);

                }
                 return new Password(strFieldType, inputs);

             }

            System.out.println("No spec specified for input of type password");
            return null;
        }


        System.out.println(strFieldType + " field collection not implemented");

        return null;
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
    private boolean itemRequiredFor(Vector<IXMLElement> packs, AutomatedInstallData idata)
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
     * Extracts the text from an <code>IXMLElement</code>. The text must be defined in the resource
     * file under the key defined in the <code>id</code> attribute or as value of the attribute
     * <code>txt</code>.
     *
     * @param element the <code>IXMLElement</code> from which to extract the text.
     * @param idata installer data
     * @return The text defined in the <code>IXMLElement</code>. If no text can be located,
     * <code>null</code> is returned.
     */
    /*--------------------------------------------------------------------------*/
    private String getText(IXMLElement element, AutomatedInstallData idata)
    {
        if (element == null) { return (null); }

        String key = element.getAttribute(KEY);
        String text = element.getAttribute(TEXT);

        if ((key != null) && (idata.langpack != null))
        {
            try
            {
                String langPackText = idata.langpack.getString(key);
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

    private class RevalidationTriggeredException extends RuntimeException
    {

    }

    public static class Input
    {

        public Input(String strFieldType)
        {
                this.strFieldType = strFieldType;
        }

        public Input(String strVariableName, String strDefaultValue, List<Choice> listChoices,
                String strFieldType, String strFieldText, int iSelectedChoice, boolean mustExist, boolean canCreate)
        {
            this ( strVariableName,  strDefaultValue,  listChoices,  strFieldType,  strFieldText,  iSelectedChoice);
            this.mustExist = mustExist;
            this.canCreate = canCreate;
        }
        public Input(String strVariableName, String strDefaultValue, List<Choice> listChoices,
                String strFieldType, String strFieldText, int iSelectedChoice)
        {
            this.strVariableName = strVariableName;
            this.strDefaultValue = strDefaultValue;
            this.listChoices = listChoices;
            this.strFieldType = strFieldType;
            this.strText = strFieldText;
            this.iSelectedChoice = iSelectedChoice;
        }

        public Input(String strVariableName, String strDefaultValue, List<Choice> listChoices,
                String strFieldType, String strFieldText, int iSelectedChoice, boolean revalidate)
        {
            this(strVariableName, strDefaultValue, listChoices, strFieldType, strFieldText, iSelectedChoice);
            this.revalidate = revalidate;
        }

        boolean canCreate = true;
        boolean mustExist = true;

        String strVariableName;

        String strDefaultValue;

        List<Choice> listChoices;

        String strFieldType;

        String strText;

        int iSelectedChoice = -1;

        boolean revalidate;

        public List<ValidatorContainer> validators;
    }

    public static class Choice
    {

        public Choice(String strText, String strValue, String strSet)
        {
            this.strText = strText;
            this.strValue = strValue;
            this.strSet = strSet;
        }

        String strText;

        String strValue;

        String strSet;
    }

    public static class Password extends Input
    {

        public Password(String strFieldType, Input[] input) {
            super(strFieldType);
            this.input = input;
        }

        Input[] input;


    }

}

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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;

import org.fest.swing.exception.ComponentLookupException;
import org.fest.swing.finder.JFileChooserFinder;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JComboBoxFixture;
import org.fest.swing.fixture.JFileChooserFixture;
import org.fest.swing.fixture.JRadioButtonFixture;
import org.fest.swing.fixture.JTextComponentFixture;
import org.fest.swing.timing.Timeout;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.data.DynamicVariableImpl;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.core.rules.process.VariableCondition;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.installer.gui.IzPanelView;
import com.izforge.izpack.panels.simplefinish.SimpleFinishPanel;
import com.izforge.izpack.panels.test.AbstractPanelTest;
import com.izforge.izpack.panels.test.TestGUIPanelContainer;
import com.izforge.izpack.panels.userinput.field.Choice;
import com.izforge.izpack.test.Container;


/**
 * Tests the {@link UserInputPanel}.
 *
 * @author Tim Anderson
 */
@Container(TestGUIPanelContainer.class)
public class UserInputPanelTest extends AbstractPanelTest
{

    /**
     * Temporary folder for 'file', 'dir' and 'search' field tests.
     */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * Constructs an {@code UserInputPanelTest}.
     *
     * @param container           the test container
     * @param installData         the installation data
     * @param resourceManager     the resource manager
     * @param factory             the panel factory
     * @param rules               the rules
     * @param icons               the icons
     * @param uninstallDataWriter the uninstallation data writer
     * @param locales             the locales
     */
    public UserInputPanelTest(TestGUIPanelContainer container, GUIInstallData installData,
                              ResourceManager resourceManager, ObjectFactory factory, RulesEngine rules,
                              IconsDatabase icons, UninstallDataWriter uninstallDataWriter, Locales locales)
    {
        super(container, installData, resourceManager, factory, rules, icons, uninstallDataWriter, locales);
    }

    /**
     * Tests rule fields.
     *
     * @throws Exception for any error
     */
    @Test
    public void testRuleField() throws Exception
    {
        // Set the base path in order to pick up com/izforge/izpack/panels/userinput/rule/userInputSpec.xml
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/userinput/rule/");
        InstallData installData = getInstallData();

        // show the panel
        FrameFixture frame = showUserInputPanel("ruleinput");

        JTextComponentFixture rule1 = frame.textBox("rule1.1");
        assertEquals("192", rule1.text());
        JTextComponentFixture rule2 = frame.textBox("rule1.2");
        assertEquals("168", rule2.text());
        JTextComponentFixture rule3 = frame.textBox("rule1.3");
        assertEquals("0", rule3.text());
        JTextComponentFixture rule4 = frame.textBox("rule1.4");
        assertEquals("1", rule4.text());

        assertEquals("192.168.0.1", installData.getVariable("rule1"));

        rule1.setText("127");
        rule2.setText("0");
        rule3.setText("0");
        rule4.setText("1");

        // attempt to navigate to the next panel
        checkNavigateNext(frame);

        assertEquals("127.0.0.1", installData.getVariable("rule1"));
    }

    /**
     * Tests text fields.
     *
     * @throws Exception for any error
     */
    @Test
    public void testTextField() throws Exception
    {
        // Set the base path in order to pick up com/izforge/izpack/panels/userinput/text/userInputSpec.xml
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/userinput/text/");
        InstallData installData = getInstallData();

        installData.setVariable("text3value", "text3 default value");

        // show the panel
        FrameFixture frame = showUserInputPanel("textinput");

        JTextComponentFixture text1 = frame.textBox("text1");
        assertEquals("", text1.text());

        JTextComponentFixture text2 = frame.textBox("text2");
        assertEquals("text2 value", text2.text());

        JTextComponentFixture text3 = frame.textBox("text3");
        assertEquals("text3 default value", text3.text());

        assertEquals("", installData.getVariable("text1"));

        String expectedText = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod " +
                "tempor invidunt ut labore et dolore magna aliquyam";

        text1.setText(expectedText);

        // attempt to navigate to the next panel
        checkNavigateNext(frame);

        assertEquals(expectedText, installData.getVariable("text1"));
        assertEquals("text2 value", installData.getVariable("text2"));
        assertEquals("text3 default value", installData.getVariable("text3"));
    }

    /**
     * Tests text fields.
     *
     * @throws Exception for any error
     */
    @Test
    public void testStaticTextField() throws Exception
    {
        // Set the base path in order to pick up com/izforge/izpack/panels/userinput/text/userInputSpec.xml
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/userinput/statictext/");
        InstallData installData = getInstallData();

        // show the panel
        FrameFixture frame = showUserInputPanel("checkStaticText");

        // attempt to navigate to the next panel
        checkNavigateNext(frame);
    }
    /**
     * Tests text fields.
     *
     * @throws Exception for any error
     */
    @Test
    public void testTextareaField() throws Exception
    {
        // Set the base path in order to pick up com/izforge/izpack/panels/userinput/text/userInputSpec.xml
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/userinput/textarea/");
        InstallData installData = getInstallData();

        installData.setVariable("textarea3value", "textarea3\ndefault value");

        // show the panel
        FrameFixture frame = showUserInputPanel("textinput");

        JTextComponentFixture text1 = frame.textBox("textarea1");
        assertEquals("", text1.text());

        JTextComponentFixture text2 = frame.textBox("textarea2");
        assertEquals("textarea2\nvalue", text2.text());

        JTextComponentFixture text3 = frame.textBox("textarea3");
        assertEquals("textarea3\ndefault value", text3.text());

        assertEquals("", installData.getVariable("textarea1"));

        text1.setText("textarea1\nvalue");

        // attempt to navigate to the next panel
        checkNavigateNext(frame);

        assertEquals("textarea1\nvalue", installData.getVariable("textarea1"));
        assertEquals("textarea2\nvalue", installData.getVariable("textarea2"));
        assertEquals("textarea3\ndefault value", installData.getVariable("textarea3"));
    }

    /*
     * Initial selection should be determined by attribute 'set'.
     */
    @Test
    public void comboWithSetShouldSelectInitialValue() throws Exception
    {
        ResourceManager rm = getResourceManager();
        rm.setResourceBasePath("/com/izforge/izpack/panels/userinput/combo/with-set/");

        FrameFixture frame = showUserInputPanel("with-set");
        checkCombo("combo", "value2", frame);
        checkNavigateNext(frame);

        InstallData installData = getInstallData();
        assertThat(installData.getVariable("combo"), equalTo("value2"));
    }

    /*
     * Initial selection should be updated if selection changes.
     */
    @Test
    public void comboWithSetShouldUpdateVariableWhenSelectionChanges() throws Exception
    {
        ResourceManager rm = getResourceManager();
        rm.setResourceBasePath("/com/izforge/izpack/panels/userinput/combo/with-set/");

        FrameFixture frame = showUserInputPanel("with-set");
        frame.comboBox("combo").selectItem(0);

        checkNavigateNext(frame);

        InstallData installData = getInstallData();
        assertThat(installData.getVariable("combo"), equalTo("value1"));
    }

    /*
     * Initial selection should be updated if variable contains selected value.
     */
    @Test
    public void comboWithSetShouldDetermineSelectionFromVariableValue() throws Exception
    {
        ResourceManager rm = getResourceManager();
        rm.setResourceBasePath("/com/izforge/izpack/panels/userinput/combo/with-set/");

        InstallData installData = getInstallData();
        installData.setVariable("combo", "value3");

        FrameFixture frame = showUserInputPanel("with-set");

        checkCombo("combo", "value3", frame);
        checkNavigateNext(frame);

        assertThat(installData.getVariable("combo"), equalTo("value3"));
    }

    /*
     * If combo selection is cleared, variable should be set to initial value.
     */
    @Test
    public void comboWithSetShouldResetVariableToInitialValueIfSelectionIsCleared() throws Exception
    {
        ResourceManager rm = getResourceManager();
        rm.setResourceBasePath("/com/izforge/izpack/panels/userinput/combo/with-set/");

        InstallData installData = getInstallData();
        installData.setVariable("combo", "value3");

        FrameFixture frame = showUserInputPanel("with-set");
        frame.comboBox("combo").clearSelection();

        checkNavigateNext(frame);

        assertThat(installData.getVariable("combo"), equalTo("value2"));
    }

    /*
     * Variable value should be evaluated to determine selected index.
     */
    @Test
    public void comboShouldDetermineSelectionFromVariableValue() throws Exception
    {
        ResourceManager rm = getResourceManager();
        rm.setResourceBasePath("/com/izforge/izpack/panels/userinput/combo/without-set/");

        InstallData installData = getInstallData();
        installData.setVariable("combo", "value2");

        FrameFixture frame = showUserInputPanel("without-set");

        checkCombo("combo", "value2", frame);
        checkNavigateNext(frame);

        assertThat(installData.getVariable("combo"), equalTo("value2"));
    }

    /*
     * If combo selection is cleared, variable should be set to the value of the first item.
     */
    @Test
    public void comboShouldResetVariableToValueOfFirstItemIfSelectionIsCleared() throws Exception
    {
        ResourceManager rm = getResourceManager();
        rm.setResourceBasePath("/com/izforge/izpack/panels/userinput/combo/without-set/");

        InstallData installData = getInstallData();
        installData.setVariable("combo", "value3");

        FrameFixture frame = showUserInputPanel("without-set");
        frame.comboBox("combo").clearSelection();

        checkNavigateNext(frame);

        assertThat(installData.getVariable("combo"), equalTo("value1"));
    }

    /*
     * If there is neither a variable value nor a set attribute, default to first item.
     */
    @Test
    public void comboShouldDefaultToFirstItemAsFallback() throws Exception
    {
        ResourceManager rm = getResourceManager();
        rm.setResourceBasePath("/com/izforge/izpack/panels/userinput/combo/without-set/");

        FrameFixture frame = showUserInputPanel("without-set");

        checkCombo("combo", "value1", frame);
        checkNavigateNext(frame);

        InstallData installData = getInstallData();
        assertThat(installData.getVariable("combo"), equalTo("value1"));
    }

    /**
     * Tests radio fields.
     *
     * @throws Exception for any error
     */
    @Test
    public void testRadioField() throws Exception
    {
        // Set the base path in order to pick up com/izforge/izpack/panels/userinput/radio/userInputSpec.xml
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/userinput/radio/");

        InstallData installData = getInstallData();

        // show the panel
        FrameFixture frame = showUserInputPanel("radioinput");

        // for radioA, the initial selection is determined by the 'set' attribute
        checkRadioButton("radioA.1", false, frame);
        checkRadioButton("radioA.2", true, frame);
        checkRadioButton("radioA.3", false, frame);

        // for radioB, there is no initial selection so default to first choice
        checkRadioButton("radioB.1", true, frame);
        checkRadioButton("radioB.2", false, frame);
        checkRadioButton("radioB.3", false, frame);

        // for radioC, the initial selection is determined by the 'set' attribute
        JRadioButton radioC1 = checkRadioButton("radioC.1", false, frame);
        checkRadioButton("radioC.2", false, frame);
        checkRadioButton("radioC.3", true, frame);

        // select the first value of C
        radioC1.setSelected(true);

        checkNavigateNext(frame);

        assertEquals("value2", installData.getVariable("radioA"));
        assertEquals("valueX", installData.getVariable("radioB"));
        assertEquals("valueQ", installData.getVariable("radioC"));
    }

    /**
     * Tests password fields.
     *
     * @throws Exception for any error
     */
    @Test
    public void testPassword() throws Exception
    {
        // Set the base path in order to pick up com/izforge/izpack/panels/userinput/password/userInputSpec.xml
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/userinput/password/");

        InstallData installData = getInstallData();

        // show the panel
        FrameFixture frame = showUserInputPanel("passwordinput");

        // for passwordA, the initial value is determined by the 'set' attribute
        JTextComponentFixture passwordA1 = frame.textBox("passwordA.1");
        JTextComponentFixture passwordA2 = frame.textBox("passwordA.2");
        assertEquals("ab1234", passwordA1.component().getText());
        assertEquals("ab1234", passwordA2.component().getText());

        // passwordB has no initial value
        JTextComponentFixture passwordB = frame.textBox("passwordB.1");
        assertEquals("", passwordB.component().getText());

        // for password C, the initial value is determined by the 'set' attribute
        JTextComponentFixture passwordC = frame.textBox("passwordC.1");
        assertEquals("qwerty", passwordC.component().getText());

        // update passwordC
        passwordC.setText("xyz");

        // for passwordD, the initial value is determined by the 'set' attribute
        JTextComponentFixture passwordD1 = frame.textBox("passwordD.1");
        JTextComponentFixture passwordD2 = frame.textBox("passwordD.2");
        assertEquals("ab2345", passwordD1.component().getText());
        assertEquals("ab2345", passwordD2.component().getText());

        assertTrue(getPanels().getView().panelValidated());

        // test password validation
        passwordA2.setText("foo");

        frame.button(GuiId.BUTTON_NEXT.id).click();
        DialogFixture dialog = frame.dialog(Timeout.timeout(10000));
        assertEquals("Passwords must match", dialog.label("OptionPane.label").text());
        dialog.button().click();
        passwordA2.setText("ab1234");

        // move to the next panel and verify the variables have updated
        checkNavigateNext(frame);

        assertEquals("ab1234", installData.getVariable("passwordA"));
        assertEquals("", installData.getVariable("passwordB"));
        assertEquals("xyz", installData.getVariable("passwordC"));
    }

    /**
     * Tests check fields.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCheck() throws Exception
    {
        // Set the base path in order to pick up com/izforge/izpack/panels/userinput/check/userInputSpec.xml
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/userinput/check/");

        RulesEngine rules = getRules();
        InstallData installData = getInstallData();

        installData.setVariable("check5", "check5set");
        installData.setVariable("check6", "check6unset");

        // set up some conditions. These determine if the check5text and check6text fields are displayed.
        // Condition cond.check5set evaluates true when check5 is selected
        VariableCondition check5set = new VariableCondition("check5", "check5set");
        check5set.setId("cond.check5set");
        check5set.setInstallData(getInstallData());
        rules.addCondition(check5set);
        assertTrue(check5set.isTrue());

        // Condition cond.check6unset evaluates true when check6 is de-selected
        VariableCondition check6unset = new VariableCondition("check6", "check6unset");
        check6unset.setId("cond.check6unset");
        check6unset.setInstallData(getInstallData());
        rules.addCondition(check6unset);
        assertTrue(check6unset.isTrue());

        // show the panel
        FrameFixture frame = showUserInputPanel("checkinput");

        checkCheckBox("check1", true, frame);
        checkCheckBox("check2", false, frame);
        checkCheckBox("check3", true, frame);
        checkCheckBox("check4", false, frame);
        checkCheckBox("check5", true, frame);
        checkCheckBox("check6", false, frame);

        // check5text and check6test should be displayed
        frame.textBox("check5text").requireVisible();
        frame.textBox("check6text").requireVisible();

        // check6text should be removed when check6 is selected
        frame.checkBox("check6").click();

        frame.textBox("check5text").requireVisible();
        try
        {
            frame.textBox("check6text");
            fail("Expected check6text to not be displayed as its condition should exclude it");
        }
        catch (ComponentLookupException expected)
        {
            // expected behaviour
        }

        // move to the next panel and verify the variables have updated
        checkNavigateNext(frame);

        assertEquals("true", installData.getVariable("check1"));
        assertEquals("false", installData.getVariable("check2"));
        assertEquals("check3set", installData.getVariable("check3"));
        assertEquals("check4unset", installData.getVariable("check4"));
        assertEquals("check5set", installData.getVariable("check5"));
        assertEquals("check6set", installData.getVariable("check6"));
    }

    /**
     * Tests search fields.
     *
     * @throws Exception for any error
     */
    @Test
    public void testSearch() throws Exception
    {
        // Set the base path in order to pick up com/izforge/izpack/panels/userinput/search/userInputSpec.xml
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/userinput/search/");

        InstallData installData = getInstallData();
        String path = temporaryFolder.getRoot().getPath();
        installData.setVariable("MY_DIR", path);
        assertTrue(new File(path, "dir1").mkdir());
        assertTrue(new File(path, "dir2").mkdir());

        // show the panel
        FrameFixture frame = showUserInputPanel("searchinput");

        JComboBoxFixture search1 = frame.comboBox("search1");

        // make sure the order is preserved
        ComboBoxModel model = search1.component().getModel();
        assertEquals(path + File.separator + "dir1", model.getElementAt(0));
        assertEquals(path + File.separator + "dir2", model.getElementAt(1));

        assertEquals(0, search1.component().getSelectedIndex()); // should default to first dir1
        search1.selectItem(1);
        assertEquals(1, search1.component().getSelectedIndex());

        // move to the next panel and verify the variables have updated
        checkNavigateNext(frame);

        assertEquals(path + File.separator + "dir2", installData.getVariable("search1"));
    }

    /**
     * Tests file fields.
     *
     * @throws Exception for any error
     */
    @Test
    public void testFile() throws Exception
    {
        // Set the base path in order to pick up com/izforge/izpack/panels/userinput/file/userInputSpec.xml
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/userinput/file/");

        InstallData installData = getInstallData();
        String path = temporaryFolder.getRoot().getPath();
        installData.setVariable("MY_DIR", path);
        assertTrue(new File(path, "fileA").createNewFile());
        assertTrue(new File(path, "fileB").createNewFile());

        // show the panel
        FrameFixture frame = showUserInputPanel("fileinput");

        JTextComponentFixture file1 = frame.textBox("file1");
        String expected = new File(path, "fileB").getPath();
        file1.setText(expected);

        // move to the next panel and verify the variables have updated
        checkNavigateNext(frame);

        assertEquals(expected, installData.getVariable("file1"));
    }

    /**
     * Tests 'multiFile' fields.
     *
     * @throws Exception for any error
     */
    @Test
    public void testMultiFile() throws Exception
    {
        // Set the base path in order to pick up com/izforge/izpack/panels/userinput/multifile/userInputSpec.xml
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/userinput/multifile/");

        InstallData installData = getInstallData();
        File tempFolder = temporaryFolder.getRoot();
        String path = tempFolder.getPath();
        installData.setVariable("MY_DIR", path);
        assertTrue(new File(path, "fileA").createNewFile());
        File fileB = new File(path, "fileB");
        assertTrue(fileB.createNewFile());
        File fileC = new File(path, "fileC");
        assertTrue(fileC.createNewFile());

        // show the panel
        FrameFixture frame = showUserInputPanel("multifileinput");
        
        // select files
        browseFileFromFileChooser(frame, tempFolder, fileB);
        browseFileFromFileChooser(frame, tempFolder, fileC);
        
        // move to the next panel and verify the variables have updated
        checkNavigateNext(frame);

        assertEquals(fileB.getPath() + ";" + fileC.getPath() + ";", installData.getVariable("multiFile1"));
    }

    /**
     * Tests dir fields.
     *
     * @throws Exception for any error
     */
    @Test
    public void testDir() throws Exception
    {
        // Set the base path in order to pick up com/izforge/izpack/panels/userinput/dir/userInputSpec.xml
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/userinput/dir/");

        InstallData installData = getInstallData();
        String path = temporaryFolder.getRoot().getPath();
        installData.setVariable("MY_DIR", path);
        assertTrue(new File(path, "dirA").mkdir());
        assertTrue(new File(path, "dirB").mkdir());

        // show the panel
        FrameFixture frame = showUserInputPanel("dirinput");

        JTextComponentFixture dir1 = frame.textBox("dir1");
        String expected = new File(path, "dirB").getPath();
        dir1.setText(expected);

        // move to the next panel and verify the variables have updated
        checkNavigateNext(frame);

        assertEquals(expected, installData.getVariable("dir1"));
    }

    /**
     * Verifies that dynamic variables are refreshed when the panel is validated.
     *
     * @throws Exception for any error
     */
    @Test
    public void testRefreshDynamicVariables() throws Exception
    {
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/userinput/refresh/");
        InstallData installData = getInstallData();

        // create a variable to be used in userInputSpec.xml to set a default value for the address field
        installData.setVariable("defaultAddress", "localhost");

        // create a dynamic variable that will be updated with the value of the address field
        installData.getVariables().add(new DynamicVariableImpl("dynamicMasterAddress", "${address}"));

        // show the panel
        FrameFixture fixture = showUserInputPanel("userinputAddress");

        JTextComponentFixture address = fixture.textBox();
        assertEquals("localhost", address.text());

        assertEquals("localhost", installData.getVariable("address"));
        assertEquals("localhost", installData.getVariable("dynamicMasterAddress"));

        address.setText("myhost");

        assertTrue(getPanels().getView().panelValidated());

        checkNavigateNext(fixture);

        assertEquals("myhost", installData.getVariable("address"));
        assertEquals("myhost", installData.getVariable("dynamicMasterAddress"));
    }

    @Test
    public void processorWithDefaultConfigurationShouldUpdateFieldProperty() throws Exception
    {
        // Set the base path in order to pick up com/izforge/izpack/panels/userinput/text/userInputSpec.xml
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/userinput/processors/");
        InstallData installData = getInstallData();

        // show the panel
        FrameFixture frame = showUserInputPanel("processors");

        JTextComponentFixture text1 = frame.textBox("processors1");
        assertEquals("ProcessorOne", text1.text());
        assertEquals("ProcessorOne", installData.getVariable("processors1"));

        // attempt to navigate to the next panel
        checkNavigateNext(frame);

        assertEquals("Processed: ProcessorOne", installData.getVariable("processors1"));
    }

    @Test
    public void processorWithToVariableShouldLeaveFieldPropertyUntouched() throws Exception
    {
        // Set the base path in order to pick up com/izforge/izpack/panels/userinput/text/userInputSpec.xml
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/userinput/processors/");
        InstallData installData = getInstallData();

        // show the panel
        FrameFixture frame = showUserInputPanel("processors");
        JTextComponentFixture textField = frame.textBox("processors2");

        // attempt to navigate to the next panel
        checkNavigateNext(frame);

        assertEquals("ProcessorTwo", textField.text());
        assertEquals("ProcessorTwo", installData.getVariable("processors2"));
        assertEquals("Processed: ProcessorTwo", installData.getVariable("processors2.processed"));
    }

    @Test
    public void fieldShouldBeAbleToHaveSeveralProcessors() throws Exception
    {
        // Set the base path in order to pick up com/izforge/izpack/panels/userinput/text/userInputSpec.xml
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/userinput/processors/");
        InstallData installData = getInstallData();

        // show the panel
        FrameFixture frame = showUserInputPanel("processors");
        JTextComponentFixture textField = frame.textBox("processors3");

        // attempt to navigate to the next panel
        checkNavigateNext(frame);

        assertEquals("ProcessorThree", textField.text());
        assertEquals("Processed: Processed: ProcessorThree", installData.getVariable("processors3"));
    }

    @Test
    public void processorWithToVariableShouldNotGiveResultToFollowingProcessors() throws Exception
    {
        // Set the base path in order to pick up com/izforge/izpack/panels/userinput/text/userInputSpec.xml
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/userinput/processors/");
        InstallData installData = getInstallData();

        // show the panel
        FrameFixture frame = showUserInputPanel("processors");
        JTextComponentFixture textField = frame.textBox("processors4");

        // attempt to navigate to the next panel
        checkNavigateNext(frame);

        assertEquals("ProcessorFour", textField.text());
        assertEquals("Processed: ProcessorFour", installData.getVariable("processors4"));
        assertEquals("Processed: ProcessorFour", installData.getVariable("processors4.processed.first"));
        assertEquals("Processed: Processed: ProcessorFour", installData.getVariable("processors4.processed.second"));
    }

    /**
     * Verifies that the named combo has the expected value.
     *
     * @param name     the combo name
     * @param expected the expected value
     * @param frame    the frame
     * @return the combo
     */
    private JComboBoxFixture checkCombo(String name, String expected, FrameFixture frame)
    {
        JComboBoxFixture combo = frame.comboBox(name);
        Choice item = (Choice) combo.component().getSelectedItem();
        if (item == null)
        {
            assertNull(expected);
        }
        else
        {
            assertEquals(expected, item.getKey());
        }
        return combo;
    }

    /**
     * Verifies that the named check box has the expected value.
     *
     * @param name     the check box name
     * @param expected the expected value
     * @param frame    the frame
     * @return the check box
     */
    private JCheckBox checkCheckBox(String name, boolean expected, FrameFixture frame)
    {
        JCheckBox check = frame.checkBox(name).component();
        assertEquals(expected, check.isSelected());
        return check;
    }

    /**
     * Verifies a radio button selection matches that expected.
     *
     * @param name     the radio button name
     * @param expected the expected value
     * @param frame    the frame
     * @return the radio button
     */
    private JRadioButton checkRadioButton(String name, boolean expected, FrameFixture frame)
    {
        JRadioButtonFixture fixture = frame.radioButton(name);
        JRadioButton button = fixture.component();
        assertEquals(expected, button.isSelected());
        return button;
    }

    /**
     * Verifies that the next panel can be navigated to.
     *
     * @param frame the frame
     * @throws InterruptedException if interrupted waiting for the panel to change
     */
    private void checkNavigateNext(FrameFixture frame) throws InterruptedException
    {
        // attempt to navigate to the next panel
        frame.button(GuiId.BUTTON_NEXT.id).click();

        waitForPanel(SimpleFinishPanel.class);

        assertThat(getPanels().getView(), instanceOf(SimpleFinishPanel.class));
    }

    /**
     * Shows the user input panel.
     *
     * @return the frame fixture
     * @throws InterruptedException if interrupted waiting for the frame to display
     */
    private FrameFixture showUserInputPanel(String id) throws InterruptedException
    {
        FrameFixture fixture = show(createPanel(UserInputPanel.class, id), createPanel(SimpleFinishPanel.class));
        waitForPanel(UserInputPanel.class);

        assertThat(getPanels().getView(), instanceOf(UserInputPanel.class));

        return fixture;
    }

    private FrameFixture show(Panel... panels)
    {
        List<IzPanelView> panelViews = new ArrayList<IzPanelView>();
        for (Panel panel : panels)
        {
            panelViews.add(createPanelView(panel));
        }
        return show(panelViews);
    }

    private Panel createPanel(Class<? extends IzPanel> panelClass)
    {
        return createPanel(panelClass, null);
    }

    private Panel createPanel(Class<? extends IzPanel> panelClass, String id)
    {
        Panel panel = new Panel();
        panel.setPanelId(id);
        panel.setClassName(panelClass.getName());
        return panel;
    }
    
    /**
     * Clicks on "browse" button and selects the given file in
     * given directory.<br>
     * A Thread.sleep() is used to ensure the "selectFile" action
     * is not launched while the current directory's update is
     * not finished yet.
     * 
     * @param frame the frame from which the browse button is
     * @param currentDirectory the directory of the file to select
     * @param fileToSelect the file to select
     * @throws InterruptedException if error occurs during Thread.sleep()
     */
    private void browseFileFromFileChooser(FrameFixture frame, File currentDirectory, 
            File fileToSelect) throws InterruptedException 
    {
        frame.button(GuiId.BUTTON_BROWSE.id).click();
        
        JFileChooserFixture fileChooser = JFileChooserFinder.findFileChooser()
                .withTimeout(10, TimeUnit.SECONDS)
                .using(frame.robot);
        
        fileChooser.setCurrentDirectory(currentDirectory);
        Thread.sleep(50);
        fileChooser.selectFile(fileToSelect).approve();
    }

}

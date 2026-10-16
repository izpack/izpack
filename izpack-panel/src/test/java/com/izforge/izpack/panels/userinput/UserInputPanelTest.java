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

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.fest.swing.finder.JFileChooserFinder.findFileChooser;
import static org.fest.swing.timing.Timeout.timeout;

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
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import org.fest.swing.exception.ComponentLookupException;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JComboBoxFixture;
import org.fest.swing.fixture.JFileChooserFixture;
import org.fest.swing.fixture.JRadioButtonFixture;
import org.fest.swing.fixture.JTextComponentFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
    @TempDir
    public Path temporaryFolder;

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
        assertThat(rule1.text()).isEqualTo("192");
        JTextComponentFixture rule2 = frame.textBox("rule1.2");
        assertThat(rule2.text()).isEqualTo("168");
        JTextComponentFixture rule3 = frame.textBox("rule1.3");
        assertThat(rule3.text()).isEqualTo("0");
        JTextComponentFixture rule4 = frame.textBox("rule1.4");
        assertThat(rule4.text()).isEqualTo("1");

        assertThat(installData.getVariable("rule1")).isEqualTo("192.168.0.1");

        rule1.setText("127");
        rule2.setText("0");
        rule3.setText("0");
        rule4.setText("1");

        // attempt to navigate to the next panel
        checkNavigateNext(frame);

        assertThat(installData.getVariable("rule1")).isEqualTo("127.0.0.1");
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
        assertThat(text1.text()).isEmpty();

        JTextComponentFixture text2 = frame.textBox("text2");
        assertThat(text2.text()).isEqualTo("text2 value");

        JTextComponentFixture text3 = frame.textBox("text3");
        assertThat(text3.text()).isEqualTo("text3 default value");

        assertThat(installData.getVariable("text1")).isEmpty();

        String expectedText = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod " +
                "tempor invidunt ut labore et dolore magna aliquyam";

        text1.setText(expectedText);

        // attempt to navigate to the next panel
        checkNavigateNext(frame);

        assertThat(installData.getVariable("text1")).isEqualTo(expectedText);
        assertThat(installData.getVariable("text2")).isEqualTo("text2 value");
        assertThat(installData.getVariable("text3")).isEqualTo("text3 default value");
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
        assertThat(text1.text()).isEmpty();

        JTextComponentFixture text2 = frame.textBox("textarea2");
        assertThat(text2.text()).isEqualTo("textarea2\nvalue");

        JTextComponentFixture text3 = frame.textBox("textarea3");
        assertThat(text3.text()).isEqualTo("textarea3\ndefault value");

        assertThat(installData.getVariable("textarea1")).isEmpty();

        text1.setText("textarea1\nvalue");

        // attempt to navigate to the next panel
        checkNavigateNext(frame);

        assertThat(installData.getVariable("textarea1")).isEqualTo("textarea1\nvalue");
        assertThat(installData.getVariable("textarea2")).isEqualTo("textarea2\nvalue");
        assertThat(installData.getVariable("textarea3")).isEqualTo("textarea3\ndefault value");
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
        assertThat(installData.getVariable("combo")).isEqualTo("value2");
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
        assertThat(installData.getVariable("combo")).isEqualTo("value1");
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

        assertThat(installData.getVariable("combo")).isEqualTo("value3");
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

        assertThat(installData.getVariable("combo")).isEqualTo("value2");
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

        assertThat(installData.getVariable("combo")).isEqualTo("value2");
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

        assertThat(installData.getVariable("combo")).isEqualTo("value1");
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
        assertThat(installData.getVariable("combo")).isEqualTo("value1");
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

        assertThat(installData.getVariable("radioA")).isEqualTo("value2");
        assertThat(installData.getVariable("radioB")).isEqualTo("valueX");
        assertThat(installData.getVariable("radioC")).isEqualTo("valueQ");
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
        assertThat(passwordA1.component().getText()).isEqualTo("ab1234");
        assertThat(passwordA2.component().getText()).isEqualTo("ab1234");

        // passwordB has no initial value
        JTextComponentFixture passwordB = frame.textBox("passwordB.1");
        assertThat(passwordB.component().getText()).isEmpty();

        // for password C, the initial value is determined by the 'set' attribute
        JTextComponentFixture passwordC = frame.textBox("passwordC.1");
        assertThat(passwordC.component().getText()).isEqualTo("qwerty");

        // update passwordC
        passwordC.setText("xyz");

        // for passwordD, the initial value is determined by the 'set' attribute
        JTextComponentFixture passwordD1 = frame.textBox("passwordD.1");
        JTextComponentFixture passwordD2 = frame.textBox("passwordD.2");
        assertThat(passwordD1.component().getText()).isEqualTo("ab2345");
        assertThat(passwordD2.component().getText()).isEqualTo("ab2345");

        assertThat(getPanels().getView().panelValidated()).isTrue();

        // test password validation
        passwordA2.setText("foo");

        frame.button(GuiId.BUTTON_NEXT.id).click();
        DialogFixture dialog = frame.dialog(timeout(10000));
        assertThat(dialog.label("OptionPane.label").text()).isEqualTo("Passwords must match");
        dialog.button().click();
        passwordA2.setText("ab1234");

        // move to the next panel and verify the variables have updated
        checkNavigateNext(frame);

        assertThat(installData.getVariable("passwordA")).isEqualTo("ab1234");
        assertThat(installData.getVariable("passwordB")).isEmpty();
        assertThat(installData.getVariable("passwordC")).isEqualTo("xyz");
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
        assertThat(check5set.isTrue()).isTrue();

        // Condition cond.check6unset evaluates true when check6 is de-selected
        VariableCondition check6unset = new VariableCondition("check6", "check6unset");
        check6unset.setId("cond.check6unset");
        check6unset.setInstallData(getInstallData());
        rules.addCondition(check6unset);
        assertThat(check6unset.isTrue()).isTrue();

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

        assertThat(installData.getVariable("check1")).isEqualTo("true");
        assertThat(installData.getVariable("check2")).isEqualTo("false");
        assertThat(installData.getVariable("check3")).isEqualTo("check3set");
        assertThat(installData.getVariable("check4")).isEqualTo("check4unset");
        assertThat(installData.getVariable("check5")).isEqualTo("check5set");
        assertThat(installData.getVariable("check6")).isEqualTo("check6set");
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
        String path = temporaryFolder.toString();
        installData.setVariable("MY_DIR", path);
        assertThat(new File(path, "dir1").mkdir()).isTrue();
        assertThat(new File(path, "dir2").mkdir()).isTrue();

        // show the panel
        FrameFixture frame = showUserInputPanel("searchinput");

        JComboBoxFixture search1 = frame.comboBox("search1");

        // make sure the order is preserved
        ComboBoxModel model = search1.component().getModel();
        assertThat(model.getElementAt(0)).isEqualTo(path + File.separator + "dir1");
        assertThat(model.getElementAt(1)).isEqualTo(path + File.separator + "dir2");

        assertThat(search1.component().getSelectedIndex()).isEqualTo(0); // should default to first dir1
        search1.selectItem(1);
        assertThat(search1.component().getSelectedIndex()).isEqualTo(1);

        // move to the next panel and verify the variables have updated
        checkNavigateNext(frame);

        assertThat(installData.getVariable("search1")).isEqualTo(path + File.separator + "dir2");
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
        String path = temporaryFolder.toString();
        installData.setVariable("MY_DIR", path);
        assertThat(new File(path, "fileA").createNewFile()).isTrue();
        assertThat(new File(path, "fileB").createNewFile()).isTrue();

        // show the panel
        FrameFixture frame = showUserInputPanel("fileinput");

        JTextComponentFixture file1 = frame.textBox("file1");
        String expected = new File(path, "fileB").getPath();
        file1.setText(expected);

        // move to the next panel and verify the variables have updated
        checkNavigateNext(frame);

        assertThat(installData.getVariable("file1")).isEqualTo(expected);
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
        File tempFolder = temporaryFolder.toFile();
        String path = tempFolder.getPath();
        installData.setVariable("MY_DIR", path);
        assertThat(new File(path, "fileA").createNewFile()).isTrue();
        File fileB = new File(path, "fileB");
        assertThat(fileB.createNewFile()).isTrue();
        File fileC = new File(path, "fileC");
        assertThat(fileC.createNewFile()).isTrue();

        // show the panel
        FrameFixture frame = showUserInputPanel("multifileinput");

        // select files
        browseFileFromFileChooser(frame, tempFolder, fileB);
        browseFileFromFileChooser(frame, tempFolder, fileC);

        // move to the next panel and verify the variables have updated
        checkNavigateNext(frame);

        assertThat(installData.getVariable("multiFile1")).isEqualTo(fileB.getPath() + ";" + fileC.getPath() + ";");
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
        String path = temporaryFolder.toString();
        installData.setVariable("MY_DIR", path);
        assertThat(new File(path, "dirA").mkdir()).isTrue();
        assertThat(new File(path, "dirB").mkdir()).isTrue();

        // show the panel
        FrameFixture frame = showUserInputPanel("dirinput");

        JTextComponentFixture dir1 = frame.textBox("dir1");
        String expected = new File(path, "dirB").getPath();
        dir1.setText(expected);

        // move to the next panel and verify the variables have updated
        checkNavigateNext(frame);

        assertThat(installData.getVariable("dir1")).isEqualTo(expected);
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
        assertThat(address.text()).isEqualTo("localhost");

        assertThat(installData.getVariable("address")).isEqualTo("localhost");
        assertThat(installData.getVariable("dynamicMasterAddress")).isEqualTo("localhost");

        address.setText("myhost");

        assertThat(getPanels().getView().panelValidated()).isTrue();

        checkNavigateNext(fixture);

        assertThat(installData.getVariable("address")).isEqualTo("myhost");
        assertThat(installData.getVariable("dynamicMasterAddress")).isEqualTo("myhost");
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
        assertThat(text1.text()).isEqualTo("ProcessorOne");
        assertThat(installData.getVariable("processors1")).isEqualTo("ProcessorOne");

        // attempt to navigate to the next panel
        checkNavigateNext(frame);

        assertThat(installData.getVariable("processors1")).isEqualTo("Processed: ProcessorOne");
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

        assertThat(textField.text()).isEqualTo("ProcessorTwo");
        assertThat(installData.getVariable("processors2")).isEqualTo("ProcessorTwo");
        assertThat(installData.getVariable("processors2.processed")).isEqualTo("Processed: ProcessorTwo");
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

        assertThat(textField.text()).isEqualTo("ProcessorThree");
        assertThat(installData.getVariable("processors3")).isEqualTo("Processed: Processed: ProcessorThree");
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

        assertThat(textField.text()).isEqualTo("ProcessorFour");
        assertThat(installData.getVariable("processors4")).isEqualTo("Processed: ProcessorFour");
        assertThat(installData.getVariable("processors4.processed.first")).isEqualTo("Processed: ProcessorFour");
        assertThat(installData.getVariable("processors4.processed.second")).isEqualTo("Processed: Processed: ProcessorFour");
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
            assertThat(expected).isNull();
        }
        else
        {
            assertThat(item.getKey()).isEqualTo(expected);
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
        assertThat(check.isSelected()).isEqualTo(expected);
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
        assertThat(button.isSelected()).isEqualTo(expected);
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

        assertThat(getPanels().getView()).isInstanceOf(SimpleFinishPanel.class);
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

        assertThat(getPanels().getView()).isInstanceOf(UserInputPanel.class);

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
     * A sleep() is used to ensure the "selectFile" action
     * is not launched while the current directory's update is
     * not finished yet.
     *
     * @param frame the frame from which the browse button is
     * @param currentDirectory the directory of the file to select
     * @param fileToSelect the file to select
     * @throws InterruptedException if error occurs during sleep()
     */
    private void browseFileFromFileChooser(FrameFixture frame, File currentDirectory,
            File fileToSelect) throws InterruptedException
    {
        frame.button(GuiId.BUTTON_BROWSE.id).click();

        JFileChooserFixture fileChooser = findFileChooser()
                .withTimeout(10, TimeUnit.SECONDS)
                .using(frame.robot);

        fileChooser.setCurrentDirectory(currentDirectory);
        sleep(50);
        fileChooser.selectFile(fileToSelect).approve();
    }

}

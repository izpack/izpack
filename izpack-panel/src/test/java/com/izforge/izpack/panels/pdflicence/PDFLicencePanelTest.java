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
package com.izforge.izpack.panels.pdflicence;

import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.gui.IzPanelView;
import com.izforge.izpack.panels.simplefinish.SimpleFinishPanel;
import com.izforge.izpack.panels.test.AbstractPanelTest;
import com.izforge.izpack.panels.test.TestConsolePanelContainer;
import com.izforge.izpack.panels.test.TestGUIPanelContainer;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import org.fest.swing.core.ComponentFoundCondition;
import org.fest.swing.core.TypeMatcher;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.timing.Timeout;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.graphics.text.PageText;
import org.icepdf.ri.common.views.DocumentViewController;
import org.icepdf.ri.common.views.OnePageView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.swing.timing.Pause.pause;
import static org.fest.swing.timing.Timeout.timeout;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

@RunWith(PicoRunner.class)
@Container(TestConsolePanelContainer.class)
public class PDFLicencePanelTest extends AbstractPanelTest {

	public PDFLicencePanelTest(com.izforge.izpack.api.container.Container container, GUIInstallData installData,
							   ResourceManager resourceManager, ObjectFactory factory,
							   RulesEngine rules, IconsDatabase icons,
							   UninstallDataWriter uninstallDataWriter, Locales locales)
	{
		super(container, installData, resourceManager, factory, rules, icons, uninstallDataWriter, locales);
	}

	@Before
    public void setUp()
    {
        ResourceManager rm = getResourceManager();
        rm.setResourceBasePath("/com/izforge/izpack/panels/panel/");
    }

	@Test
	public void shouldDisplayLicenceText() throws Exception
	{
        FrameFixture fixture = showPDFLicencePanel("licence");

		OnePageView onePageView = findOnePageView(fixture);
		DocumentViewController controller = onePageView.getParentViewController();

		Document document = controller.getDocument();
		assertThat(document, hasProperty("numberOfPages", equalTo(1)));

		PageText pageText = document.getPageText(0);
		pageText.selectAll();

		String textArea = pageText.getSelected().toString();
		assertThat(textArea, containsString("This is a licenSe panel"));
	}

    @Test
    public void shouldFindAndDisplayLicenceTextForPanelWithoutIdentifier() throws Exception
    {
        // create a panel without identifier
        FrameFixture fixture = showPDFLicencePanel(null);

        OnePageView onePageView = findOnePageView(fixture);
        DocumentViewController controller = onePageView.getParentViewController();

        Document document = controller.getDocument();
        assertThat(document, hasProperty("numberOfPages", equalTo(1)));

        PageText pageText = document.getPageText(0);
        pageText.selectAll();

        String textArea = pageText.getSelected().toString();
        assertThat(textArea, containsString("This is a licenSe panel"));
    }

    @Test
    public void shouldSelectLicenceNoRadioByDefault() throws Exception
    {
        FrameFixture fixture = showPDFLicencePanel("licence");

        fixture.radioButton("LicenceNoRadio").requireSelected();
    }

    @Test
    public void shouldDisableNextButtonByDefault() throws Exception
    {
        FrameFixture fixture = showPDFLicencePanel("licence");

        fixture.button("nextButton").requireDisabled();
    }

    @Test
    public void shouldEnableNextButtonIfLicenceIsAccepted() throws Exception
    {
        FrameFixture fixture = showPDFLicencePanel("licence");

        fixture.radioButton("LicenceYesRadio").check();

        fixture.button("nextButton").requireEnabled();
    }

    /**
     * Creates a fixture for a {@link PDFLicencePanel} and a {@link SimpleFinishPanel}.
     * <p>
     *     This method waits for the panel to become visible before it returns.
     * </p>
     *
     * @param id The panel identifier which is to be assigned to the licence panel.
     * @return A frame fixture for the created panel.
     */
    private FrameFixture showPDFLicencePanel(String id)
    {
        IzPanelView view = createPanelView(PDFLicencePanel.class);
        view.getPanel().setPanelId(id);

        FrameFixture fixture = show(view, createPanelView(SimpleFinishPanel.class));
        waitForPanel(PDFLicencePanel.class);

        assertThat(getPanels().getView(), instanceOf(PDFLicencePanel.class));

        return fixture;
    }

    /**
     * Tries to find OnePageView with a default timeout of 2000ms.
     *
     * @param fixture The root frame fixture which should contain the view.
     * @return The OnePageView instance on success.
     */
    private OnePageView findOnePageView(FrameFixture fixture)
    {
        return findOnePageView(fixture, timeout(2000));
    }

    /**
     * Tries to find OnePageView.
     *
     * @param fixture The root frame fixture which should contain the view.
     * @param timeout The amount of time to wait for the component to be found.
     * @return The OnePageView instance on success.
     */
    private OnePageView findOnePageView(FrameFixture fixture, Timeout timeout)
    {
        TypeMatcher matcher = new TypeMatcher(OnePageView.class, true);
        String description = "OnePageView to be found using matcher " + matcher;
        ComponentFoundCondition condition = new ComponentFoundCondition(description,
                fixture.robot.finder(), matcher);
        pause(condition, timeout);
        return (OnePageView) condition.found();
    }
}

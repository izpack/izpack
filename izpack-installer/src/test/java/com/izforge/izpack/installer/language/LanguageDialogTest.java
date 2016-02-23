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
package com.izforge.izpack.installer.language;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Locale;

import javax.swing.JFrame;

import com.izforge.izpack.gui.IconsDatabase;
import org.fest.swing.fixture.DialogFixture;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.container.TestLanguageContainer;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;

/**
 * Tests the {@link LanguageDialog}.
 *
 * @author Anthonin Bonnefoy
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@Container(TestLanguageContainer.class)
public class LanguageDialogTest
{

    /**
     * The resources.
     */
    private final Resources resources;

    /**
     * The installation data.
     */
    private final GUIInstallData installData;

    /**
     * The locales.
     */
    private final Locales locales;

    /**
     * The locales.
     */
    private final IconsDatabase icons;

    /**
     * The dialog fixture.
     */
    private DialogFixture fixture;

    /**
     * Constructs {@code LanguageDialogTest}.
     *
     * @param resources   the resources
     * @param installData the installation data
     * @param locales     the locales. Must contain locales "eng" and "fra"
     */
    public LanguageDialogTest(Resources resources, GUIInstallData installData, Locales locales, IconsDatabase icons)
    {
        this.resources = resources;
        this.installData = installData;
        this.locales = locales;
        this.icons = icons;
    }

    /**
     * Cleans up after the test.
     */
    @After
    public void tearBinding()
    {
        if (fixture != null)
        {
            fixture.cleanUp();
            fixture = null;
        }
    }

    /**
     * Tests the "default" language display type.
     */
    @Test
    public void testDefaultDisplayType()
    {
        fixture = new DialogFixture(createDialog("default"));

        String eng = locales.getLocale("eng").getDisplayLanguage();
        String fra = locales.getLocale("fra").getDisplayLanguage();

        checkSelectLanguage(eng, fra);
    }

    /**
     * Tests the "native" language display type.
     */
    @Test
    public void testNativeDisplayType()
    {
        LanguageDialog dialog = createDialog("native");
        fixture = new DialogFixture(dialog);

        Locale engLocale = locales.getLocale("eng");
        Locale fraLocale = locales.getLocale("fra");
        String eng = engLocale.getDisplayLanguage(engLocale);
        String fra = fraLocale.getDisplayLanguage(fraLocale);

        checkSelectLanguage(eng, fra);
    }

    /**
     * Tests the "iso3" language display type.
     */
    @Test
    public void testISO3DisplayType()
    {
        fixture = new DialogFixture(createDialog("iso3"));
        checkSelectLanguage("eng", "fra");
    }

    /**
     * Verifies that the combo box has the correct elements for English and French and that the locale is set correctly
     * when French is selected.
     *
     * @param englishDisplayName the expected display name for English
     * @param frenchDisplayName  the expected display name for French
     */
    private void checkSelectLanguage(String englishDisplayName, String frenchDisplayName)
    {
        fixture.show();
        assertThat(fixture.comboBox(GuiId.COMBO_BOX_LANG_FLAG.id).contents(),
                   Is.is(new String[]{englishDisplayName, frenchDisplayName}));
        fixture.comboBox(GuiId.COMBO_BOX_LANG_FLAG.id).selectItem(1);
        fixture.button(GuiId.BUTTON_LANG_OK.id).click();
        assertNotNull(locales.getLocale());
        assertEquals("fra", locales.getLocale().getISO3Language());
    }

    /**
     * Creates a new {@link LanguageDialog} that displays language names according to the supplied {@code
     * langDisplayType}.
     *
     * @param langDisplayType one of "native", "default", "iso3"
     * @return a new dialog
     */
    private LanguageDialog createDialog(String langDisplayType)
    {
        installData.guiPrefs.modifier.put("langDisplayType", langDisplayType);
        return new LanguageDialog(resources, locales, installData, icons);
    }

}
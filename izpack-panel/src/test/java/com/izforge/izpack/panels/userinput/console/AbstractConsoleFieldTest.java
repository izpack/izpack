package com.izforge.izpack.panels.userinput.console;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.izforge.izpack.api.data.ConsolePrefs;
import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.container.DefaultContainer;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.rules.ConditionContainer;
import com.izforge.izpack.core.rules.RulesEngineImpl;
import com.izforge.izpack.installer.data.ConsoleInstallData;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.test.util.TestConsole;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.Platforms;

/**
 * Base class for console-based user-input fields.
 *
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@Container(DefaultContainer.class)
public abstract class AbstractConsoleFieldTest
{

    /**
     * The install data.
     */
    protected final ConsoleInstallData installData;

    /**
     * The console.
     */
    protected final TestConsole console;

    /**
     * The prompt.
     */
    protected final Prompt prompt;


    /**
     * Constructs an {@code AbstractConsoleFieldTest}.
     */
    public AbstractConsoleFieldTest()
    {
      ConsolePrefs consolePrefs = new ConsolePrefs();
      consolePrefs.enableConsoleReader = false;
      installData = new ConsoleInstallData(new DefaultVariables(), usedPlatform(), consolePrefs);
      console = new TestConsole(installData);
      prompt = Mockito.mock(Prompt.class);
    }

    protected Platform usedPlatform()
    {
        return Platforms.LINUX;
    }

    protected void initialize(ConditionContainer conditionContainer)
    {
        InputStream langPack = getClass().getResourceAsStream("/com/izforge/izpack/bin/langpacks/installer/eng.xml");
        assertNotNull(langPack);
        installData.setMessages(new LocaleDatabase(langPack, Mockito.mock(Locales.class)));
        RulesEngine rules = new RulesEngineImpl(installData, conditionContainer);
        installData.setRules(rules);
    }

    /**
     * Runs the specified script for the field, and ensures its valid.
     *
     * @param field  the field
     * @param script the script to run
     */
    protected void checkValid(ConsoleField field, String... script)
    {
        console.addScript("Valid script", script);
        assertTrue(field.display());
    }

    /**
     * Runs the specified script for the field, and ensures its valid.
     *
     * @param field  the field
     * @param script the script to run
     */
    protected void checkInvalid(ConsoleField field, String... script)
    {
        console.addScript("Invalid script", script);
        assertFalse(field.display());
    }

}

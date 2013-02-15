/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2013 Tim Anderson
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
package com.izforge.izpack.installer.requirement;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.mockito.Mockito;

import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.installer.RequirementChecker;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.core.handler.ConsolePrompt;
import com.izforge.izpack.installer.data.InstallData;
import com.izforge.izpack.test.util.TestConsole;
import com.izforge.izpack.util.Platforms;

/**
 * Base class for {@link RequirementChecker} tests.
 *
 * @author Tim Anderson
 */
public abstract class AbstractRequirementCheckerTest
{

    /**
     * The installation data.
     */
    protected final InstallData installData;

    /**
     * The console.
     */
    protected final TestConsole console;

    /**
     * The prompt.
     */
    protected final Prompt prompt;

    /**
     * Constructs an {@code AbstractRequirementCheckerTest}.
     */
    public AbstractRequirementCheckerTest()
    {
        installData = new InstallData(null, Platforms.FEDORA_LINUX);
        Info info = new Info();
        installData.setInfo(info);

        InputStream langPack = getClass().getResourceAsStream("/com/izforge/izpack/bin/langpacks/installer/eng.xml");
        assertNotNull(langPack);
        installData.setMessages(new LocaleDatabase(langPack, Mockito.mock(Locales.class)));

        console = new TestConsole();
        prompt = new ConsolePrompt(console, installData.getMessages());
    }
}

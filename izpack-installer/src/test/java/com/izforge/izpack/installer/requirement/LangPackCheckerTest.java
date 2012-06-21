package com.izforge.izpack.installer.requirement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.izforge.izpack.core.resource.ResourceManager;

/**
 * Tests the {@link LangPackChecker}.
 *
 * @author Tim Anderson
 */
public class LangPackCheckerTest
{

    /**
     * Tests the {@link LangPackChecker}.
     */
    @Test
    public void testLangPackChecker()
    {
        final List<String> langPacks = new ArrayList<String>();
        ResourceManager resources = new ResourceManager()
        {
            @Override
            public List<String> getAvailableLangPacks()
            {
                return langPacks;
            }
        };

        LangPackChecker checker = new LangPackChecker(resources);

        // no lang packs - should evaluate false
        assertFalse(checker.check());

        // add a lang pack - should evaluate true
        langPacks.add("eng");
        assertTrue(checker.check());
    }
}

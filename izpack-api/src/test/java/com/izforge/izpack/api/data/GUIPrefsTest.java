package com.izforge.izpack.api.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class GUIPrefsTest {

    @Test
    public void name() throws Exception
    {
        // Assemble
        GUIPrefs prefs = new GUIPrefs();

        // Act
        GUIPrefs.LookAndFeel first = new GUIPrefs.LookAndFeel("substance");
        first.setParameter("variant", "creme");
        prefs.lookAndFeelMapping.put("windows", first);
        prefs.lookAndFeelMapping.put("unix", first);

        GUIPrefs.LookAndFeel second = new GUIPrefs.LookAndFeel("substance");
        second.setParameter("variant", "mist-aqua");
        prefs.lookAndFeelMapping.put("mac", second);

        // Assert
        assertTrue(first.is(LookAndFeels.SUBSTANCE));
        assertTrue(second.is(LookAndFeels.SUBSTANCE));
        assertEquals("creme", prefs.lookAndFeelMapping.get("windows").getVariantName());
        assertEquals("creme", prefs.lookAndFeelMapping.get("unix").getVariantName());
        assertEquals("mist-aqua", prefs.lookAndFeelMapping.get("mac").getVariantName());
    }
}

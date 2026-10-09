package com.izforge.izpack.api.data;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(first.is(LookAndFeels.SUBSTANCE)).isTrue();
        assertThat(second.is(LookAndFeels.SUBSTANCE)).isTrue();
        assertThat(prefs.lookAndFeelMapping.get("windows").getVariantName()).isEqualTo("creme");
        assertThat(prefs.lookAndFeelMapping.get("unix").getVariantName()).isEqualTo("creme");
        assertThat(prefs.lookAndFeelMapping.get("mac").getVariantName()).isEqualTo("mist-aqua");
    }
}
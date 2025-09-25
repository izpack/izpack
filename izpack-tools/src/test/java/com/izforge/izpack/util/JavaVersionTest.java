package com.izforge.izpack.util;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class JavaVersionTest {

    @Test
    public void parsePreJdk9VersionSchema() {
        JavaVersion version1 = JavaVersion.parse("1.8");
        assertEquals(8, version1.feature(), "Feature part of version 1.8 version has to be 8");

        JavaVersion version2 = JavaVersion.parse("1.8.0_124+2");
        assertEquals(8, version2.feature(), "Feature part of version 1.8.0_124+2 has to be 8");
    }

    @Test
    public void parseJdk9VersionSchema() {
        JavaVersion version1 = JavaVersion.parse("8");
        assertEquals(8, version1.feature(), "Feature part of version 8 has to be 8");

        JavaVersion version2 = JavaVersion.parse("8.0_124+2");
        assertEquals(8, version2.feature(), "Feature part of version 8.0_124+2 to be 8");

        JavaVersion version3 = JavaVersion.parse("2.8");
        assertEquals(2, version3.feature(), "Feature part of version 2.8 version has to be 2");

        JavaVersion version4 = JavaVersion.parse("11.0");
        assertEquals(11, version4.feature(), "Feature part of version 11.0 has to be 11");
    }

    @Test
    public void equals() {
        JavaVersion version1 = JavaVersion.parse("1.8.14");
        JavaVersion version2 = JavaVersion.parse("8.14");
        assertEquals(version1, version2, "Same version in Jdk9 schema and pre Jdk9 schema has to be equals");
        assertEquals(version2, version1, "Same version in Jdk9 schema and pre Jdk9 schema has to be equals");

        JavaVersion version3 = JavaVersion.parse("1.1");
        JavaVersion version4 = JavaVersion.parse("1");
        assertEquals(version3, version4, "This two versions must be equals");
    }

    @Test
    public void notEquals() {
        JavaVersion version1 = JavaVersion.parse("11.2");
        JavaVersion version2 = JavaVersion.parse("1.1.2");
        assertNotEquals(version1, version2, "This two versions must not be equals");

        JavaVersion version3 = JavaVersion.parse("1.1.3");
        JavaVersion version4 = JavaVersion.parse("1.3");
        assertNotEquals(version3, version4, "This two versions must not be equals");

        JavaVersion version5 = JavaVersion.parse("11.1_100");
        JavaVersion version6 = JavaVersion.parse("11.1_200");
        assertNotEquals(version5, version6, "This two versions must not be equals");

        JavaVersion version7 = JavaVersion.parse("1.1");
        JavaVersion version8 = JavaVersion.parse("1.1.1");
        assertNotEquals(version7, version8, "This two versions must not be equals");

        JavaVersion version9 = JavaVersion.parse("1.23_100");
        JavaVersion version10 = JavaVersion.parse("1.23+100");
        assertNotEquals(version9, version10, "This two versions must not be equals");

        JavaVersion version11 = JavaVersion.parse("1-2");
        JavaVersion version12 = JavaVersion.parse("1.2");
        assertNotEquals(version11, version12, "This two versions must not be equals");
    }

    @Test
    public void numberFormatException1() {
        assertThrows(NumberFormatException.class, () -> JavaVersion.parse("1.a"));
    }

    @Test
    public void numberFormatException2() {
        assertThrows(NumberFormatException.class, () -> JavaVersion.parse(".1"));
    }
}

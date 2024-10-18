package com.izforge.izpack.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class JavaVersionTest
{

    @Test
    public void parsePreJdk9VersionSchema()
    {
        JavaVersion version1 = JavaVersion.parse("1.8");
        assertEquals("Feature part of version 1.8 version has to be 8", 8, version1.feature());


        JavaVersion version2 = JavaVersion.parse("1.8.0_124+2");
        assertEquals("Feature part of version 1.8.0_124+2 has to be 8", 8, version2.feature());

    }

    @Test
    public void parseJdk9VersionSchema()
    {
        JavaVersion version1 = JavaVersion.parse("8");
        assertEquals("Feature part of version 8 has to be 8", 8, version1.feature());

        JavaVersion version2 = JavaVersion.parse("8.0_124+2");
        assertEquals("Feature part of version 8.0_124+2 to be 8", 8, version2.feature());

        JavaVersion version3 = JavaVersion.parse("2.8");
        assertEquals("Feature part of version 2.8 version has to be 2", 2, version3.feature());

        JavaVersion version4 = JavaVersion.parse("11.0");
        assertEquals("Feature part of version 11.0 has to be 11", 11, version4.feature());

    }

    @Test
    public void equals()
    {
        JavaVersion version1 = JavaVersion.parse("1.8.14");
        JavaVersion version2 = JavaVersion.parse("8.14");
        assertEquals("Same version in Jdk9 schema and pre Jdk9 schema has to be equals", version1, version2);
        assertEquals("Same version in Jdk9 schema and pre Jdk9 schema has to be equals", version2, version1);

        JavaVersion version3 = JavaVersion.parse("1.1");
        JavaVersion version4 = JavaVersion.parse("1");
        assertEquals("This two versions must be equals", version3, version4);
    }

    @Test
    public void notEquals()
    {
        JavaVersion version1 = JavaVersion.parse("11.2");
        JavaVersion version2 = JavaVersion.parse("1.1.2");
        assertNotEquals("This two versions must not be equals", version1, version2);

        JavaVersion version3 = JavaVersion.parse("1.1.3");
        JavaVersion version4 = JavaVersion.parse("1.3");
        assertNotEquals("This two versions must not be equals", version3, version4);

        JavaVersion version5 = JavaVersion.parse("11.1_100");
        JavaVersion version6 = JavaVersion.parse("11.1_200");
        assertNotEquals("This two versions must not be equals", version5, version6);

        JavaVersion version7 = JavaVersion.parse("1.1");
        JavaVersion version8 = JavaVersion.parse("1.1.1");
        assertNotEquals("This two versions must not be equals", version7, version8);

        JavaVersion version9 = JavaVersion.parse("1.23_100");
        JavaVersion version10 = JavaVersion.parse("1.23+100");
        assertNotEquals("This two versions must not be equals", version9, version10);

        JavaVersion version11 = JavaVersion.parse("1-2");
        JavaVersion version12 = JavaVersion.parse("1.2");
        assertNotEquals("This two versions must not be equals", version11, version12);
    }

    @Test(expected = NumberFormatException.class)
    public void numberFormatException1() {
        JavaVersion.parse("1.a");
    }

    @Test(expected = NumberFormatException.class)
    public void numberFormatException2() {
        JavaVersion.parse(".1");
    }
}
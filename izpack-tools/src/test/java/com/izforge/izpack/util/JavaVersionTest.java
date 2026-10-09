package com.izforge.izpack.util;

import static com.izforge.izpack.util.JavaVersion.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;


public class JavaVersionTest
{

    @Test
    public void parsePreJdk9VersionSchema()
    {
        JavaVersion version1 = parse("1.8");
        assertThat(version1.feature()).as("Feature part of version 1.8 version has to be 8").isEqualTo(8);


        JavaVersion version2 = parse("1.8.0_124+2");
        assertThat(version2.feature()).as("Feature part of version 1.8.0_124+2 has to be 8").isEqualTo(8);

    }

    @Test
    public void parseJdk9VersionSchema()
    {
        JavaVersion version1 = parse("8");
        assertThat(version1.feature()).as("Feature part of version 8 has to be 8").isEqualTo(8);

        JavaVersion version2 = parse("8.0_124+2");
        assertThat(version2.feature()).as("Feature part of version 8.0_124+2 to be 8").isEqualTo(8);

        JavaVersion version3 = parse("2.8");
        assertThat(version3.feature()).as("Feature part of version 2.8 version has to be 2").isEqualTo(2);

        JavaVersion version4 = parse("11.0");
        assertThat(version4.feature()).as("Feature part of version 11.0 has to be 11").isEqualTo(11);

    }

    @Test
    public void equals()
    {
        JavaVersion version1 = parse("1.8.14");
        JavaVersion version2 = parse("8.14");
        assertThat(version2).as("Same version in Jdk9 schema and pre Jdk9 schema has to be equals").isEqualTo(version1);
        assertThat(version1).as("Same version in Jdk9 schema and pre Jdk9 schema has to be equals").isEqualTo(version2);

        JavaVersion version3 = parse("1.1");
        JavaVersion version4 = parse("1");
        assertThat(version4).as("This two versions must be equals").isEqualTo(version3);
    }

    @Test
    public void notEquals()
    {
        JavaVersion version1 = parse("11.2");
        JavaVersion version2 = parse("1.1.2");
        assertThat(version2).as("This two versions must not be equals").isNotEqualTo(version1);

        JavaVersion version3 = parse("1.1.3");
        JavaVersion version4 = parse("1.3");
        assertThat(version4).as("This two versions must not be equals").isNotEqualTo(version3);

        JavaVersion version5 = parse("11.1_100");
        JavaVersion version6 = parse("11.1_200");
        assertThat(version6).as("This two versions must not be equals").isNotEqualTo(version5);

        JavaVersion version7 = parse("1.1");
        JavaVersion version8 = parse("1.1.1");
        assertThat(version8).as("This two versions must not be equals").isNotEqualTo(version7);

        JavaVersion version9 = parse("1.23_100");
        JavaVersion version10 = parse("1.23+100");
        assertThat(version10).as("This two versions must not be equals").isNotEqualTo(version9);

        JavaVersion version11 = parse("1-2");
        JavaVersion version12 = parse("1.2");
        assertThat(version12).as("This two versions must not be equals").isNotEqualTo(version11);
    }

    @Test
    public void numberFormatException1() {
        assertThatThrownBy(() -> {
            parse("1.a");
        }).isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void numberFormatException2() {
        assertThatThrownBy(() -> {
            parse(".1");
        }).isInstanceOf(NumberFormatException.class);
    }
}
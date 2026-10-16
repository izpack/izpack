package com.izforge.izpack.api.config.spi;

import static org.assertj.core.api.Assertions.assertThat;

import com.izforge.izpack.api.config.BasicProfile;
import com.izforge.izpack.api.config.Ini;
import com.izforge.izpack.api.config.InvalidFileFormatException;
import com.izforge.izpack.api.config.Profile.Section;
import com.izforge.izpack.api.config.Reg;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;


public class IniParserTest extends IniParser {

    @Test
    public void testIndexOfOperator() {
        //                                         012345678901 23
        assertThat(indexOfOperator("\"DisplayName\"=\"@%SystemRoot%\\system32")).isEqualTo(13);
        //                                         012345678901234 56
        assertThat(indexOfOperator("\"http://*:2869/\"=hex:01,00")).isEqualTo(16);
        //                                         01234567890123456 7 8 90
        assertThat(indexOfOperator("\"back-slash-quote\\\"\"=1")).isEqualTo(20);
        //                                         0123456789012345678 9 0 12
        assertThat(indexOfOperator("\"double-back-slash-\\\\\"=1")).isEqualTo(22);
    }


    @Test
    public void parseSectionLineTest() throws InvalidFileFormatException, IOException {

        final IniParser iniParser = IniParser.newInstance();

        // use the IniBuilder
        final Ini ini = new Ini();
        final IniBuilder iniBuilder = IniBuilder.newInstance(ini);

        String line0 = "[HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\GraphicsDrivers\\Configuration\\LEN41210_00_07DE_77*ACR1626JGQ110018410\n" +
         "_25_07DF_55^1A8F0E88B56475919D28F2488BC91252]\n" +
         "\"SetId\"=\"LEN41210_00_07DE_77*ACR1626JGQ110018410\n" +
         "_25_07DF_55\"\n" +
         "\"Timestamp\"=hex(b):3e,ae,9a,4c,c3,ef,d5,01";

        ByteArrayInputStream input = new ByteArrayInputStream(line0.getBytes());

        iniParser.parse(input, iniBuilder);

        assertThat(iniBuilder.getProfile()).isNotNull();
        assertThat(iniBuilder.getProfile() instanceof BasicProfile).isTrue();

        String expectedProfileName = "HKEY_LOCAL_MACHINESYSTEMCurrentControlSetControlGraphicsDriversConfigurationLEN41210_00_07DE_77*ACR1626JGQ110018410" +
                 "_25_07DF_55^1A8F0E88B56475919D28F2488BC91252";

        BasicProfile profile = (BasicProfile) iniBuilder.getProfile();

        Section section = profile.get(expectedProfileName);
        assertThat(section).isNotNull();

        String setId = section.get("\"SetId\"", String.class);
        assertThat(setId).isNotNull();
        assertThat(setId).isEqualTo("\"LEN41210_00_07DE_77*ACR1626JGQ110018410_25_07DF_55\"");

        String timeStamp = section.get("\"Timestamp\"", String.class);
        assertThat(timeStamp).isNotNull();
        assertThat(timeStamp).isEqualTo("hex(b):3e,ae,9a,4c,c3,ef,d5,01");
    }

    @Test
    public void parseSectionLineRegBuilderTest() throws InvalidFileFormatException, IOException {

        final IniParser iniParser = IniParser.newInstance();

        // use the RegBuilder
        final Reg ini = new Reg();
        final RegBuilder regBuilder = RegBuilder.newInstance(ini);

        String line0 = "[HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\GraphicsDrivers\\Configuration\\LEN41210_00_07DE_77*ACR1626JGQ110018410\n" +
         "_25_07DF_55^1A8F0E88B56475919D28F2488BC91252]\n" +
         "\"SetId\"=\"LEN41210_00_07DE_77*ACR1626JGQ110018410\n" +
         "_25_07DF_55\"\n" +
         "\"Timestamp\"=hex(b):3e,ae,9a,4c,c3,ef,d5,01";

        // Caused by: com.izforge.izpack.api.config.InvalidFileFormatException: parse error (at line: 90988):
        // [HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Control\GraphicsDrivers\Configuration\LEN41210_00_07DE_77*ACR1626JGQ110018410

        ByteArrayInputStream input = new ByteArrayInputStream(line0.getBytes());

        iniParser.parse(input, regBuilder);

        assertThat(regBuilder.getProfile()).isNotNull();
        assertThat(regBuilder.getProfile() instanceof BasicProfile).isTrue();

        String expectedProfileName = "HKEY_LOCAL_MACHINESYSTEMCurrentControlSetControlGraphicsDriversConfigurationLEN41210_00_07DE_77*ACR1626JGQ110018410" +
                 "_25_07DF_55^1A8F0E88B56475919D28F2488BC91252";

        BasicProfile profile = (BasicProfile) regBuilder.getProfile();

        Section section = profile.get(expectedProfileName);
        assertThat(section).isNotNull();

        String setId = section.get("SetId", String.class);
        assertThat(setId).isNotNull();
        assertThat(setId).isEqualTo("LEN41210_00_07DE_77*ACR1626JGQ110018410_25_07DF_55");

        String timeStamp = section.get("Timestamp", String.class);
        assertThat(timeStamp).isNotNull();
        assertThat(timeStamp).isEqualTo("3e,ae,9a,4c,c3,ef,d5,01");
    }

    @Test
    public void parseEmptyREG_MULTI_SZTest() throws InvalidFileFormatException, IOException {

        final IniParser iniParser = IniParser.newInstance();

        // use the RegBuilder
        final Reg ini = new Reg();
        final RegBuilder regBuilder = RegBuilder.newInstance(ini);

        String line0 = "[HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\MUI\\UILanguages\\en-GB]\n" +
            "\"LCID\"=dword:00000809\n" +
            "\"Type\"=dword:00000112\n" +
            "\"en-US\"=hex(7):\n" +
            "\"DefaultFallback\"=\"en-US\"";

        ByteArrayInputStream input = new ByteArrayInputStream(line0.getBytes());

        iniParser.parse(input, regBuilder);

        String expectedProfileName = "HKEY_LOCAL_MACHINESYSTEMCurrentControlSetControlMUIUILanguagesen-GB";

        BasicProfile profile = (BasicProfile) regBuilder.getProfile();

        Section section = profile.get(expectedProfileName);
        assertThat(section).isNotNull();

        String language = section.get("en-US", String.class);
        assertThat(language).isNotNull();
        assertThat(language).isEmpty();
    }
}

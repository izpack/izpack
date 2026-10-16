package com.izforge.izpack.api.config.spi;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;


public class AbstractParserTest extends AbstractParser {

    private static final String COMMENTS = ";#";
    private static final String OPERATORS = ":=";


    public AbstractParserTest() {
        super(OPERATORS, COMMENTS);
    }

    @Test
    public void indexOfOperator() {
        String line0 = "\"";
        assertThat(indexOfOperator(line0)).isEqualTo(-1);

        String line1 = "\"NextInstance\"=dword:00000001";
        assertThat(indexOfOperator(line1)).isEqualTo(14);

        String line2 = "\"4\"=\"IPBusEnumRoot\\UMB\\2&ba1ffa4&0&uuid:12345678-0000-0000-0000-00000000abcd\"";
        assertThat(indexOfOperator(line2)).isEqualTo(3);

        String line3 = "\"0\"=\"uuid:12345678-0000-0000-0000-00000000abcd\\UMB\\3&44ecbc&0&uuid:12345678-0000-0000-0000-00000000abcd\"";
        assertThat(indexOfOperator(line3)).isEqualTo(3);
    }
}

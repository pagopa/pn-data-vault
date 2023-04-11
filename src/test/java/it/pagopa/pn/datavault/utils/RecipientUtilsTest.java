package it.pagopa.pn.datavault.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecipientUtilsTest {

    @Test
    void reverseStringTest() {
        String reverseString = RecipientUtils.reverseString("prova");
        assertThat(reverseString).isEqualTo("avorp");

    }
}

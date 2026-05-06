package com.alpacafkow.meditrack.organization.organization.domain.model.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailValueObjectTest {

    @Test
    void shouldCreateEmailWhenFormatIsValid() {
        var email = new Email("casa@reposo.com");
        assertEquals("casa@reposo.com", email.value());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Email(" "));
    }

    @Test
    void shouldThrowExceptionWhenEmailFormatIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new Email("invalid-mail"));
    }
}

package br.com.example.cep;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CepValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"01310100", "01310-100", " 01310-100 "})
    void isValid_shouldReturnTrue_forValidCeps(String cep) {
        assertTrue(CepValidator.isValid(cep));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "0131010", "013101001", "abcdefgh", ""})
    void isValid_shouldReturnFalse_forInvalidCeps(String cep) {
        assertFalse(CepValidator.isValid(cep));
    }

    @Test
    void isValid_shouldReturnFalse_forNull() {
        assertFalse(CepValidator.isValid(null));
    }

    @Test
    void format_shouldFormatValidCep() {
        assertEquals("01310-100", CepValidator.format("01310100"));
        assertEquals("01310-100", CepValidator.format("01310-100"));
    }

    @Test
    void format_shouldReturnOriginal_whenNotEightDigits() {
        assertEquals("123", CepValidator.format("123"));
    }

    @Test
    void format_shouldReturnNull_forNull() {
        assertNull(CepValidator.format(null));
    }
}

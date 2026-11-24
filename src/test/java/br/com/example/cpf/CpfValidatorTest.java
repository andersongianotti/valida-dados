package br.com.example.cpf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "52998224725",      // CPF válido conhecido
            "529.982.247-25",   // com máscara
            "11144477735",
            " 390.533.447-05 "  // com espaços
    })
    void isValid_shouldReturnTrue_forValidCpfs(String cpf) {
        assertTrue(CpfValidator.isValid(cpf));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "52998224726",      // dígito verificador errado
            "11111111111",      // todos dígitos iguais
            "00000000000",
            "12345678901",      // tamanho ok, dígitos errados
            "123",
            "",
            "abcdefghijk",
            "529.982.247-2"     // 10 dígitos
    })
    void isValid_shouldReturnFalse_forInvalidCpfs(String cpf) {
        assertFalse(CpfValidator.isValid(cpf));
    }

    @Test
    void isValid_shouldReturnFalse_forNull() {
        assertFalse(CpfValidator.isValid(null));
    }

    @Test
    void format_shouldFormatValidCpf() {
        assertEquals("529.982.247-25", CpfValidator.format("52998224725"));
        assertEquals("529.982.247-25", CpfValidator.format("529.982.247-25"));
    }

    @Test
    void format_shouldReturnOriginal_whenNotElevenDigits() {
        assertEquals("123", CpfValidator.format("123"));
    }
}

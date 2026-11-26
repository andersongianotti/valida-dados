package br.com.example.cep;

/**
 * Valida o formato do CEP brasileiro (8 dígitos, com ou sem máscara).
 */
public final class CepValidator {

    private static final int CEP_LENGTH = 8;

    private CepValidator() {
    }

    /**
     * Verifica se o CEP informado tem formato válido (8 dígitos).
     * Não consulta base de dados — apenas valida o formato.
     */
    public static boolean isValid(String cep) {
        if (cep == null) {
            return false;
        }
        String digits = cep.replaceAll("\\D", "");
        return digits.length() == CEP_LENGTH;
    }

    /**
     * Formata o CEP para o padrão 00000-000.
     * Retorna o valor original se não houver 8 dígitos.
     */
    public static String format(String cep) {
        if (cep == null) {
            return null;
        }
        String digits = cep.replaceAll("\\D", "");
        if (digits.length() != CEP_LENGTH) {
            return cep;
        }
        return digits.substring(0, 5) + "-" + digits.substring(5);
    }
}

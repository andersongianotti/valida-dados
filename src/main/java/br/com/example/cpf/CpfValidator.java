package br.com.example.cpf;

/**
 * Validador de CPF conforme o algoritmo oficial da Receita Federal.
 * Aceita CPF com ou sem máscara (000.000.000-00).
 */
public final class CpfValidator {

    private static final int CPF_LENGTH = 11;

    private CpfValidator() {
    }

    /**
     * Verifica se o CPF informado é válido.
     *
     * @param cpf CPF com ou sem formatação
     * @return true se o CPF é válido, false caso contrário
     */
    public static boolean isValid(String cpf) {
        String digits = onlyDigits(cpf);
        if (digits.length() != CPF_LENGTH || allSameDigit(digits)) {
            return false;
        }
        return checkDigit(digits, 9) == digits.charAt(9)
                && checkDigit(digits, 10) == digits.charAt(10);
    }

    /**
     * Formata o CPF para o padrão 000.000.000-00.
     * Retorna o valor original se não houver 11 dígitos.
     */
    public static String format(String cpf) {
        String digits = onlyDigits(cpf);
        if (digits.length() != CPF_LENGTH) {
            return cpf;
        }
        return digits.substring(0, 3) + "."
                + digits.substring(3, 6) + "."
                + digits.substring(6, 9) + "-"
                + digits.substring(9);
    }

    private static char checkDigit(String cpf, int length) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += (cpf.charAt(i) - '0') * (length + 1 - i);
        }
        int remainder = sum % 11;
        int digit = remainder < 2 ? 0 : 11 - remainder;
        return (char) ('0' + digit);
    }

    private static boolean allSameDigit(String digits) {
        return digits.chars().distinct().count() == 1;
    }

    private static String onlyDigits(String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("\\D", "");
    }
}

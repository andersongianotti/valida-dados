package br.com.example.cep;

/**
 * Endereço retornado pela consulta de CEP.
 * Campos comuns à API dos Correios e ao ViaCEP.
 */
public record Endereco(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String localidade,
        String uf) {
}

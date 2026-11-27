package br.com.example.cep;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

/**
 * Consulta de endereço por CEP.
 *
 * Providers suportados:
 * - ViaCEP (viacep.com.br): gratuito, sem autenticação.
 * - Correios API Busca CEP (api.correios.com.br): oficial, requer
 *   contrato comercial e Bearer token (ver manual oficial).
 */
public final class CepLookup {

    private static final String VIACEP_URL = "https://viacep.com.br/ws/%s/json/";
    private static final String CORREIOS_URL = "https://api.correios.com.br/cep/v2/enderecos/%s";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient;
    private final String viacepBaseUrl;
    private final String correiosBaseUrl;

    public CepLookup() {
        this(HttpClient.newBuilder().connectTimeout(TIMEOUT).build(), VIACEP_URL, CORREIOS_URL);
    }

    CepLookup(HttpClient httpClient, String viacepBaseUrl, String correiosBaseUrl) {
        this.httpClient = httpClient;
        this.viacepBaseUrl = viacepBaseUrl;
        this.correiosBaseUrl = correiosBaseUrl;
    }

    /**
     * Busca endereço via ViaCEP (gratuito, sem autenticação).
     *
     * @return Optional com o endereço, ou vazio se CEP inválido/não encontrado
     */
    public Optional<Endereco> buscarViaCep(String cep) {
        String digits = digits(cep);
        if (!CepValidator.isValid(digits)) {
            return Optional.empty();
        }
        try {
            String body = get(String.format(viacepBaseUrl, digits));
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            if (json.has("erro") && json.get("erro").getAsBoolean()) {
                return Optional.empty();
            }
            return Optional.of(new Endereco(
                    str(json, "cep"),
                    str(json, "logradouro"),
                    str(json, "complemento"),
                    str(json, "bairro"),
                    str(json, "localidade"),
                    str(json, "uf")));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (IOException | RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Busca endereço via API oficial dos Correios (API Busca CEP).
     * Requer Bearer token obtido via API Token dos Correios (CWS).
     *
     * @param token   Bearer token do contrato comercial
     * @param baseUrl URL base da API (produção ou homologação)
     * @return Optional com o endereço, ou vazio se inválido/não encontrado/erro HTTP
     */
    public Optional<Endereco> buscarCorreios(String cep, String token, String baseUrl) {
        String digits = digits(cep);
        if (!CepValidator.isValid(digits) || token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(baseUrl, digits)))
                    .timeout(TIMEOUT)
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return Optional.empty();
            }
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            return Optional.of(new Endereco(
                    str(json, "cep"),
                    str(json, "logradouro"),
                    str(json, "complemento"),
                    str(json, "bairro"),
                    str(json, "localidade"),
                    str(json, "uf")));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (IOException | RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Busca endereço via API oficial dos Correios (URL de produção).
     */
    public Optional<Endereco> buscarCorreios(String cep, String token) {
        return buscarCorreios(cep, token, correiosBaseUrl);
    }

    private String get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " ao consultar " + url);
        }
        return response.body();
    }

    private static String digits(String input) {
        return input == null ? "" : input.replaceAll("\\D", "");
    }

    private static String str(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }
}

package br.com.example.cep;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CepLookupTest {

    private HttpServer server;
    private String baseUrl;
    private final AtomicReference<String> lastAuthHeader = new AtomicReference<>();
    private String responseBody;
    private int responseStatus = 200;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            lastAuthHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(responseStatus, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort() + "/%s";
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    private CepLookup lookup() {
        return new CepLookup(java.net.http.HttpClient.newHttpClient(), baseUrl, baseUrl);
    }

    @Test
    void buscarViaCep_shouldReturnEndereco_forValidCep() {
        responseBody = """
                {"cep":"01310-100","logradouro":"Avenida Paulista","complemento":"de 1 a 174 - lado par","bairro":"Bela Vista","localidade":"São Paulo","uf":"SP","ibge":"3550308","gia":"1004","ddd":"11","siafi":"7107"}
                """;

        Optional<Endereco> result = lookup().buscarViaCep("01310100");

        assertTrue(result.isPresent());
        assertEquals("01310-100", result.get().cep());
        assertEquals("Avenida Paulista", result.get().logradouro());
        assertEquals("Bela Vista", result.get().bairro());
        assertEquals("São Paulo", result.get().localidade());
        assertEquals("SP", result.get().uf());
    }

    @Test
    void buscarViaCep_shouldReturnEmpty_whenCepNotFound() {
        responseBody = "{\"erro\": true}";

        assertTrue(lookup().buscarViaCep("99999999").isEmpty());
    }

    @Test
    void buscarViaCep_shouldReturnEmpty_forInvalidCep_withoutCallingApi() {
        assertTrue(lookup().buscarViaCep("123").isEmpty());
        assertTrue(lookup().buscarViaCep(null).isEmpty());
    }

    @Test
    void buscarCorreios_shouldReturnEndereco_andSendBearerToken() {
        responseBody = """
                {"cep":"01001001","uf":"SP","localidade":"São Paulo","logradouro":"Praça da Sé","complemento":"- lado par","bairro":"Sé","tipoCEP":2}
                """;

        Optional<Endereco> result = lookup().buscarCorreios("01001001", "meu-token");

        assertTrue(result.isPresent());
        assertEquals("Praça da Sé", result.get().logradouro());
        assertEquals("Sé", result.get().bairro());
        assertEquals("Bearer meu-token", lastAuthHeader.get());
    }

    @Test
    void buscarCorreios_shouldReturnEmpty_whenTokenMissing() {
        assertTrue(lookup().buscarCorreios("01001001", null).isEmpty());
        assertTrue(lookup().buscarCorreios("01001001", "  ").isEmpty());
    }

    @Test
    void buscarCorreios_shouldReturnEmpty_onHttpError() {
        responseStatus = 401;
        responseBody = "{\"message\":\"unauthorized\"}";

        assertTrue(lookup().buscarCorreios("01001001", "token").isEmpty());
    }
}

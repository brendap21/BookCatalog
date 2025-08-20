package com.brendanavarro.bookcatalog.gutendex;

import com.brendanavarro.bookcatalog.gutendex.dto.BookDto;
import com.brendanavarro.bookcatalog.gutendex.dto.GutendexResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class GutendexClient {

    private static final String BASE_URL = "https://gutendex.com/books/";

    // Ajusta estos si tu red es lenta
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(45);

    // Reintentos ante timeout/IO
    private static final int MAX_ATTEMPTS = 3;
    private static final long INITIAL_BACKOFF_MS = 600; // 0.6s

    private final HttpClient http;
    private final ObjectMapper mapper;

    public GutendexClient(ObjectMapper mapper) {
        this.http = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                // Fuerza HTTP/1.1: evita algunos cuelgues extraños de HTTP/2 en ciertas redes/proxies
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.mapper = mapper;
    }

    public Optional<BookDto> searchFirstByTitle(String title, String languagesCsv)
            throws IOException, InterruptedException {

        String url = BASE_URL + "?search=" + urlEncode(title);
        if (languagesCsv != null && !languagesCsv.isBlank()) {
            url += "&languages=" + languagesCsv.toLowerCase(Locale.ROOT);
        }

        HttpRequest request = baseRequest(url);

        HttpResponse<String> response = sendWithRetries(request);
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " al consultar Gutendex: " + response.body());
        }

        GutendexResponseDto dto = mapper.readValue(response.body(), GutendexResponseDto.class);
        List<BookDto> results = dto.getResults();
        if (results == null || results.isEmpty()) return Optional.empty();
        return Optional.of(results.get(0));
    }

    public Optional<BookDto> getBookById(int id) throws IOException, InterruptedException {
        String url = BASE_URL + id;
        HttpRequest request = baseRequest(url);

        HttpResponse<String> response = sendWithRetries(request);

        if (response.statusCode() == 404) return Optional.empty();
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
        }

        BookDto book = mapper.readValue(response.body(), BookDto.class);
        return Optional.of(book);
    }

    private HttpRequest baseRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json")
                // UA explícito: útil para proxies/firewalls quisquillosos
                .header("User-Agent", "LiterAlura/0.0.1 (Java HttpClient) " + System.getProperty("java.version"))
                .build();
    }

    private HttpResponse<String> sendWithRetries(HttpRequest request) throws IOException, InterruptedException {
        IOException lastIo = null;
        long backoff = INITIAL_BACKOFF_MS;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return http.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (HttpTimeoutException e) {
                if (attempt == MAX_ATTEMPTS) throw e;
                System.err.println("⏳ Timeout (" + attempt + "/" + MAX_ATTEMPTS + ") -> reintentando en " + backoff + "ms");
            } catch (IOException e) {
                lastIo = e;
                // Errores de red intermitentes: reintenta
                if (attempt == MAX_ATTEMPTS) throw e;
                System.err.println("🌐 IO error (" + attempt + "/" + MAX_ATTEMPTS + "): " + e.getMessage() +
                        " -> reintentando en " + backoff + "ms");
            }
            Thread.sleep(backoff);
            backoff *= 2; // backoff exponencial
        }
        throw lastIo != null ? lastIo : new IOException("Fallo desconocido tras reintentos");
    }

    private static String urlEncode(String raw) {
        return URLEncoder.encode(raw, StandardCharsets.UTF_8);
    }
}

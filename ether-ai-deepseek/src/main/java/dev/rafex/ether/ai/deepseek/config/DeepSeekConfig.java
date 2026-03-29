package dev.rafex.ether.ai.deepseek.config;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * Configuración para conectarse a la API de DeepSeek.
 *
 * @param apiKey         La clave de API para autenticar las solicitudes.
 * @param baseUri        La URI base de la API.
 * @param timeout        El tiempo de espera para las solicitudes HTTP.
 * @param defaultHeaders Cabeceras HTTP por defecto para todas las solicitudes.
 */
public record DeepSeekConfig(String apiKey, URI baseUri, Duration timeout, Map<String, String> defaultHeaders) {

    private static final URI DEFAULT_BASE_URI = URI.create("https://api.deepseek.com/");
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Crea una nueva configuración y valida los parámetros.
     *
     * @param apiKey         La clave de API para autenticar las solicitudes.
     * @param baseUri        La URI base de la API.
     * @param timeout        El tiempo de espera para las solicitudes HTTP.
     * @param defaultHeaders Cabeceras HTTP por defecto para todas las solicitudes.
     * @throws NullPointerException si la apiKey es nula.
     * @throws IllegalArgumentException si la apiKey está en blanco.
     */
    public DeepSeekConfig {
        Objects.requireNonNull(apiKey, "apiKey");
        if (apiKey.isBlank()) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }
        baseUri = normalizeBaseUri(baseUri == null ? DEFAULT_BASE_URI : baseUri);
        timeout = timeout == null ? DEFAULT_TIMEOUT : timeout;
        defaultHeaders = defaultHeaders == null ? Map.of() : Map.copyOf(defaultHeaders);
    }

    /**
     * Crea una configuración con valores por defecto.
     *
     * @param apiKey La clave de API.
     * @return La configuración creada.
     */
    public static DeepSeekConfig of(final String apiKey) {
        return new DeepSeekConfig(apiKey, DEFAULT_BASE_URI, DEFAULT_TIMEOUT, Map.of());
    }

    /**
     * Devuelve la URI para generar respuestas de chat.
     *
     * @return La URI de completions de chat.
     */
    public URI chatCompletionsUri() {
        return baseUri.resolve("chat/completions");
    }

    private static URI normalizeBaseUri(final URI baseUri) {
        final String value = baseUri.toString();
        return URI.create(value.endsWith("/") ? value : value + "/");
    }
}

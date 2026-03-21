package dev.rafex.ether.ai.deepseek.config;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public record DeepSeekConfig(String apiKey, URI baseUri, Duration timeout, Map<String, String> defaultHeaders) {

    private static final URI DEFAULT_BASE_URI = URI.create("https://api.deepseek.com/");
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    public DeepSeekConfig {
        Objects.requireNonNull(apiKey, "apiKey");
        if (apiKey.isBlank()) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }
        baseUri = normalizeBaseUri(baseUri == null ? DEFAULT_BASE_URI : baseUri);
        timeout = timeout == null ? DEFAULT_TIMEOUT : timeout;
        defaultHeaders = defaultHeaders == null ? Map.of() : Map.copyOf(defaultHeaders);
    }

    public static DeepSeekConfig of(final String apiKey) {
        return new DeepSeekConfig(apiKey, DEFAULT_BASE_URI, DEFAULT_TIMEOUT, Map.of());
    }

    public URI chatCompletionsUri() {
        return baseUri.resolve("chat/completions");
    }

    private static URI normalizeBaseUri(final URI baseUri) {
        final String value = baseUri.toString();
        return URI.create(value.endsWith("/") ? value : value + "/");
    }
}

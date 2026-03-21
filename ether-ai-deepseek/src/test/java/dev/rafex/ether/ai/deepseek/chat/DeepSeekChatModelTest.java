package dev.rafex.ether.ai.deepseek.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import dev.rafex.ether.ai.core.chat.AiChatRequest;
import dev.rafex.ether.ai.core.message.AiMessage;
import dev.rafex.ether.ai.deepseek.config.DeepSeekConfig;
import dev.rafex.ether.json.JsonUtils;

class DeepSeekChatModelTest {

    private HttpServer server;
    private final AtomicReference<String> authorization = new AtomicReference<>();
    private final AtomicReference<String> requestBody = new AtomicReference<>();

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.createContext("/chat/completions", this::handleChat);
        server.start();
    }

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void shouldCallDeepSeekCompatibleChatCompletionEndpoint() throws Exception {
        final var config = new DeepSeekConfig("deepseek-key",
                URI.create("http://127.0.0.1:" + server.getAddress().getPort()), null, Map.of("X-Test", "true"));
        final var model = new DeepSeekChatModel(config);
        final var response = model
                .generate(new AiChatRequest("deepseek-chat", List.of(AiMessage.user("ping")), 0.1d, 64));

        assertEquals("pong", response.text());
        assertEquals("stop", response.finishReason());
        assertEquals(10, response.usage().inputTokens());
        assertEquals("Bearer deepseek-key", authorization.get());
        assertTrue(requestBody.get().contains("\"model\":\"deepseek-chat\""));
        assertTrue(requestBody.get().contains("\"max_tokens\":64"));
    }

    private void handleChat(final HttpExchange exchange) throws IOException {
        authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
        requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));

        final var payload = Map.of(
                "id", "deepseek-test", "model", "deepseek-chat", "choices", List.of(Map.of("index", 0, "message",
                        Map.of("role", "assistant", "content", "pong"), "finish_reason", "stop")),
                "usage", Map.of("prompt_tokens", 10, "completion_tokens", 4, "total_tokens", 14));

        final byte[] response = JsonUtils.toJsonBytes(payload);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(response);
        }
    }
}

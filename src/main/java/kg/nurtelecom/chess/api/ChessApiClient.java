package kg.nurtelecom.chess.api;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Клиент бесплатного сервиса chess-api.com — отправляет позицию (FEN),
 * получает в ответ лучший ход, посчитанный Stockfish в облаке.
 * <p>
 * Запрос выполняется полностью асинхронно (java.net.http.HttpClient.sendAsync),
 * поэтому окно JavaFX не "зависает" в ожидании ответа сервера.
 */
public class ChessApiClient {

    private static final String API_URL = "https://chess-api.com/v1";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CompletableFuture<EngineMove> requestBestMoveAsync(String fen, EngineDifficulty difficulty) {
        String requestBody;
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("fen", fen);
            payload.put("depth", difficulty.depth());
            payload.put("maxThinkingTime", difficulty.maxThinkingTimeMs());
            requestBody = objectMapper.writeValueAsString(payload);
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }

        System.out.println("[chess-api.com] запрос: " + requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::parseResponse);
    }

    private EngineMove parseResponse(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            throw new RuntimeException("chess-api.com вернул код " + response.statusCode() + ": " + response.body());
        }

        System.out.println("[chess-api.com] ответ: " + response.body());

        try {
            JsonNode json = objectMapper.readTree(response.body());
            String from = json.path("from").asText(null);
            String to = json.path("to").asText(null);
            if (from == null || to == null) {
                throw new IOException("В ответе нет полей from/to: " + response.body());
            }
            String promotion = json.hasNonNull("promotion") ? json.path("promotion").asText() : null;
            int depth = json.path("depth").asInt(-1);
            Double eval = json.hasNonNull("eval") ? json.path("eval").asDouble() : null;
            String text = json.path("text").asText(null);
            return new EngineMove(from, to, promotion, depth, eval, text);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось разобрать ответ chess-api.com", e);
        }
    }
}
 
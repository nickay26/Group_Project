package Project;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class ApiClient {

    private static final String BASE_URL = "https://NickayJohn26.pythonanywhere.com";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // ==========================================
    // 1. EXECUTE PLAYGROUND CODE (ASYNC)
    // ==========================================
    public static CompletableFuture<String> executeCodeAsync(String language, String code) {
        String jsonPayload = String.format(
                "{\"language\": \"%s\", \"code\": \"%s\"}",
                language,
                escapeJson(code)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/execute"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    // ==========================================
    // 2. GEMINI AI CODE REVIEW (ASYNC)
    // ==========================================
    public static CompletableFuture<String> reviewCodeAsync(String language, String code) {
        String jsonPayload = String.format(
                "{\"language\": \"%s\", \"code\": \"%s\"}",
                language,
                escapeJson(code)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/review"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }
    // ==========================================
    // 6. PRACTICE QUESTIONS API
    // ==========================================

    /**
     * Practice Question အတွက် Code ကို Gemini နဲ့ Review လုပ်
     */
    public static CompletableFuture<String> reviewPracticeCodeAsync(
            String language, String code, String task) {
        String jsonPayload = String.format(
                "{\"language\": \"%s\", \"code\": \"%s\", \"task\": \"%s\"}",
                language,
                escapeJson(code),
                escapeJson(task)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/questions/review"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }
    // ==========================================
    // 7. SAVE PROJECT CODE
    // ==========================================
    public static CompletableFuture<String> saveProjectCodeAsync(int projectId, String codeContent) {
        String jsonPayload = String.format(
                "{\"code_content\": \"%s\"}",
                escapeJson(codeContent)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/projects/" + projectId))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }
    // Utility helper to safely sanitize raw code into valid JSON strings
    private static String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    public static CompletableFuture<String> generateQuestionAsync(
            String language, String topic, int level) {
        // Type ကို random ရွေး
        String[] types = {"fill-blank", "drag-drop", "write-code"};
        String type = types[(int)(Math.random() * types.length)];

        String jsonPayload = String.format(
                "{\"language\": \"%s\", \"topic\": \"%s\", \"level\": %d, \"type\": \"%s\"}",
                language, escapeJson(topic), level, type
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/questions/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }
}
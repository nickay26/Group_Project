package Project;

import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class ApiClient {

    // 1. URL Domain Name ကို စာလုံးသေး ပြောင်းထားပါသည် (PythonAnywhere SSL Redirect Error ကာကွယ်ရန်)
    private static final String BASE_URL = "https://nickayjohn26.pythonanywhere.com";

    // 2. HttpClient Redirects နှင့် Timeout သတ်မှတ်ချက်
    private static final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    // ==========================================
    // 1. EXECUTE PLAYGROUND CODE (ASYNC)
    // ==========================================
    public static CompletableFuture<String> executeCodeAsync(String language, String code) {
        String jsonPayload = String.format(
                "{\"language\": \"%s\", \"code\": \"%s\"}",
                escapeJson(language),
                escapeJson(code)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/execute"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Execute Code Error: " + ex.getMessage());
                    return null;
                });
    }

    // ==========================================
    // 2. GEMINI AI CODE REVIEW (ASYNC)
    // ==========================================
    public static CompletableFuture<String> reviewCodeAsync(String language, String code) {
        String jsonPayload = String.format(
                "{\"language\": \"%s\", \"code\": \"%s\"}",
                escapeJson(language),
                escapeJson(code)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/review"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(ApiClient::cleanAndFormatAiText) // <--- AI Text မပြန်မီ အလိုအလျောက် Clean လုပ်ပေးသည်
                .exceptionally(ex -> {
                    System.err.println("Review Code Error: " + ex.getMessage());
                    return null;
                });
    }

    // ==========================================
    // 3. DIRECT CODE CHALLENGE GENERATION
    // ==========================================
    public static CompletableFuture<String> generateCodeChallengeAsync(String language, String level) {
        String jsonPayload = String.format(
                "{\"language\": \"%s\", \"level\": \"%s\"}",
                escapeJson(language),
                escapeJson(level)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/challenge/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Generate Code Challenge Error: " + ex.getMessage());
                    return null;
                });
    }

    // ==========================================
    // 4. PRACTICE QUESTIONS REVIEW API
    // ==========================================
    public static CompletableFuture<String> reviewPracticeCodeAsync(
            String language, String code, String task) {
        String jsonPayload = String.format(
                "{\"language\": \"%s\", \"code\": \"%s\", \"task\": \"%s\"}",
                escapeJson(language),
                escapeJson(code),
                escapeJson(task)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/questions/review"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(ApiClient::cleanAndFormatAiText) // <--- AI Text မပြန်မီ အလိုအလျောက် Clean လုပ်ပေးသည်
                .exceptionally(ex -> {
                    System.err.println("Review Practice Error: " + ex.getMessage());
                    return null;
                });
    }

    // ==========================================
    // 5. SAVE PROJECT CODE
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
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Save Project Error: " + ex.getMessage());
                    return null;
                });
    }

    // ==========================================
    // 6. SINGLE QUESTION GENERATION
    // ==========================================
    public static CompletableFuture<String> generateQuestionAsync(
            String language, String topic, int level) {
        String[] types = {"fill-blank", "drag-drop", "write-code"};
        String type = types[(int)(Math.random() * types.length)];

        String jsonPayload = String.format(
                "{\"language\": \"%s\", \"topic\": \"%s\", \"level\": %d, \"type\": \"%s\"}",
                escapeJson(language), escapeJson(topic), level, type
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/questions/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Generate Question Error: " + ex.getMessage());
                    return null;
                });
    }

    // ==========================================
    // 7. BATCH AI QUESTIONS GENERATION
    // ==========================================
    public static CompletableFuture<String> fetchAiQuestionsBatchAsync(String language, String level, int count) {
        String jsonPayload = String.format(
                "{\"language\": \"%s\", \"level\": \"%s\", \"count\": %d}",
                escapeJson(language),
                escapeJson(level),
                count
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/questions/batch"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Batch Questions API Error: " + ex.getMessage());
                    return null;
                });
    }

    // ==========================================
    // 8. MATCHMAKING ROOM CREATION & JOINING
    // ==========================================

    // Host (Player 1) အတွက်: Room သစ်ဆောက်ပြီး မေးခွန်းတောင်းမည်
    public static CompletableFuture<String> createMatchRoomAsync(String language, String level) {
        String jsonPayload = String.format(
                "{\"language\": \"%s\", \"level\": \"%s\"}",
                escapeJson(language),
                escapeJson(level)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/match/create"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Create Match Room Error: " + ex.getMessage());
                    return null;
                });
    }

    // Guest (Player 2) အတွက်: Room Code ရိုက်ထည့်ပြီး မေးခွန်းတူတူ လှမ်းဆွဲမည်
    public static CompletableFuture<String> joinMatchRoomAsync(String roomCode) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/match/join/" + roomCode.trim()))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Join Match Room Error: " + ex.getMessage());
                    return null;
                });
    }

    // ==========================================
    // 9. AI TEXT CLEANING HELPER METHOD
    // ==========================================
    public static String cleanAndFormatAiText(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            return "";
        }

        String extractedText = rawResponse;

        // JSON format ဖြစ်ပါက အထဲမှ သက်ဆိုင်ရာ key ကို ဖြုတ်ယူခြင်း
        try {
            JSONObject json = new JSONObject(rawResponse);
            if (json.has("review")) {
                extractedText = json.getString("review");
            } else if (json.has("result")) {
                extractedText = json.getString("result");
            } else if (json.has("explanation")) {
                extractedText = json.getString("explanation");
            } else if (json.has("output")) {
                extractedText = json.getString("output");
            }
        } catch (Exception e) {
            // JSON မဟုတ်ပါက Raw text အတိုင်း ဆက်သွားမည်
        }

        // Markdown, Backticks, LaTeX သင်္ကေတများကို ဖယ်ရှားသန့်စင်ခြင်း
        return extractedText
                .replaceAll("\\*\\*(.*?)\\*\\*", "$1")  // Bold **text**
                .replaceAll("###\\s*", "")             // H3
                .replaceAll("##\\s*", "")              // H2
                .replaceAll("#\\s*", "")               // H1
                .replaceAll("```[a-zA-Z]*", "")        // Code block ticks
                .replaceAll("`", "")                   // Inline backticks
                .replaceAll("\\$\\$(.*?)\\$\\$", "$1") // LaTeX $$
                .replaceAll("\\$(.*?)\\$", "$1")       // LaTeX $
                .trim();
    }

    // JSON String Escaping Helper Function
    private static String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
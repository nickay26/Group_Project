package Project;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class ApiClient {

    private static final String BASE_URL = "http://NickayJohn26.pythonanywhere.com";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // ==========================================
    // 1. GOOGLE AUTHENTICATION
    // ==========================================
    public static boolean sendGoogleAuthCode(String authCode) {
        try {
            String jsonPayload = String.format("{\"code\": \"%s\"}", authCode);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/auth/google"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                //UserSession.createSession(1, "user@example.com", "Google User");
                return true;
            } else {
                System.err.println("Server returned status: " + response.statusCode() + " -> " + response.body());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==========================================
    // 2. FETCH LESSONS ROADMAP (ASYNC)
    // ==========================================
    public static CompletableFuture<String> fetchLessonsAsync(String courseId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/courses/" + courseId + "/lessons"))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return response.body();
                    } else {
                        throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
                    }
                });
    }

    // ==========================================
    // 3. EXECUTE PLAYGROUND CODE (ASYNC)
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
    // 4. GEMINI AI CODE REVIEW (ASYNC)
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

    // Utility helper to safely sanitize raw code into valid JSON strings
    private static String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ==========================================
// 5. USER PROJECTS HTTP API
// ==========================================

    public static CompletableFuture<String> fetchUserProjectsAsync(int userId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/projects?user_id=" + userId))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    public static CompletableFuture<String> createProjectAsync(int userId, String title, String language) {
        String jsonPayload = String.format(
                "{\"user_id\": %d, \"title\": \"%s\", \"language\": \"%s\"}",
                userId, title, language
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/projects"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

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

    public static CompletableFuture<String> fetchCoursesAsync(int userId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/courses?user_id=" + userId))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }
}
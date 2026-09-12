package Project;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiClient {

    private static final String BASE_URL = "http://NickayJohn26.pythonanywhere.com";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

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
                // Parse response body (e.g., using Jackson/Gson or basic regex)
                // Example hardcoded extraction for storing session:
                UserSession.createSession(1, "user@example.com", "Google User");
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
}
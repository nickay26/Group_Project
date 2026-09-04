package Project;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Judge0Tester {

    private static final String RAPIDAPI_KEY = "44625cc60bmshd40be9c81327579p1bcf81jsn65a26649dd48";
    private static final String RAPIDAPI_HOST = "judge0-extra-ce1.p.rapidapi.com";
    private static final String API_URL = "https://" + RAPIDAPI_HOST + "/submissions?wait=true";

    public static void main(String[] args) {
        String codeToExecute = """
            public class Main {
                public static void main(String[] args) {
                    System.out.println("Hello from Judge0 Extra CE!");
                    int sum = 10 + 20;
                    System.out.println("Calculation Result: " + sum);
                }
            }
            """;

        System.out.println("Sending Java code to Judge0 Extra CE...");

        String sanitizedCode = codeToExecute
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");

        String jsonBody = """
            {
                "source_code": "%s",
                "language_id": 62
            }
            """.formatted(sanitizedCode);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("x-rapidapi-key", RAPIDAPI_KEY)
                .header("x-rapidapi-host", RAPIDAPI_HOST)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // Fix 1: try-with-resources closes the HttpClient automatically (Java 21 requirement)
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("\n--- Judge0 JSON Response ---");
            System.out.println(response.body());

        } catch (Exception e) {
            // Fix 2: Replaced printStackTrace() with direct error messaging
            System.err.println("Execution failed: " + e.getMessage());
        }
    }
}
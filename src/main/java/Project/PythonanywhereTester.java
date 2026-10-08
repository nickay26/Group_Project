package Project;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class PythonanywhereTester {

    private static final String USERNAME = "NickayJohn26";
    private static final String BASE_URL = "https://" + USERNAME + ".pythonanywhere.com";
    private static final String EXECUTE_ENDPOINT = BASE_URL + "/execute";
    private static final String REVIEW_ENDPOINT = BASE_URL + "/review";

    private final HttpClient httpClient;

    public PythonanywhereTester() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1) // HTTP/1.1 စနစ်ဖြင့် သေချာစေရန်
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    public String executeCode(String language, String code) {
        String jsonPayload = String.format(
                "{\"language\":\"%s\",\"code\":\"%s\"}",
                escapeJson(language),
                escapeJson(code)
        );

        return sendPostRequest(EXECUTE_ENDPOINT, jsonPayload);
    }

    public String testAiReview(String code, String language) {
        String jsonPayload = String.format(
                "{\"code\":\"%s\",\"language\":\"%s\"}",
                escapeJson(code),
                escapeJson(language)
        );

        return sendPostRequest(REVIEW_ENDPOINT, jsonPayload);
    }

    private String sendPostRequest(String endpoint, String jsonPayload) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(20))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            } else {
                return String.format("{\"error\": \"HTTP %d\", \"details\": \"%s\"}",
                        response.statusCode(),
                        escapeJson(response.body()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.toString();
            return "{\"error\": \"Client Exception\", \"message\": \"" + escapeJson(errorMsg) + "\"}";
        }
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static void main(String[] args) {
        PythonanywhereTester tester = new PythonanywhereTester();

        System.out.println("--- Testing PythonAnywhere Python ---");
        String pythonCode = "print('Hello PythonAnywhere Python!')";
        System.out.println(tester.executeCode("python", pythonCode));

        System.out.println("\n--- Testing PythonAnywhere C++ ---");
        String cppCode = "#include <iostream>\nint main() { std::cout << \"Hello PythonAnywhere C++!\"; return 0; }";
        System.out.println(tester.executeCode("cpp", cppCode));

        System.out.println("\n--- Testing PythonAnywhere Java ---");
        String javaCode = "public class Main { public static void main(String[] args) { System.out.println(\"Hello PythonAnywhere Java!\"); } }";
        System.out.println(tester.executeCode("java", javaCode));

        System.out.println("\n--- Testing Gemini AI Review ---");
        String sampleCode = "public int sum(int[] nums) { int total = 0; for(int n : nums) total += n; return total; }";
        System.out.println(tester.testAiReview(sampleCode, "java"));
    }
}
package Project;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PythonanywhereTester {

    // TODO: Replace with your actual PythonAnywhere username
    private static final String USERNAME = "NickayJohn26";
    private static final String ENDPOINT = "https://" + USERNAME + ".pythonanywhere.com/execute";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String executeCode(String language, String code) {
        String jsonPayload = String.format(
                "{\"language\": \"%s\", \"code\": \"%s\"}",
                language, escapeJson(code)
        );

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"output\": \"Client Error: " + e.getMessage() + "\"}";
        }
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public String testAiReview(String code, String language) {
        String jsonPayload = String.format(
                "{\"code\": \"%s\", \"language\": \"%s\"}",
                escapeJson(code), language
        );

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://NickayJohn26.pythonanywhere.com/review"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            return "Error testing AI: " + e.getMessage();
        }
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
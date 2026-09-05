package Project;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WandboxTester {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    // Compilers: "gcc-head" (C++), "cpython-head" (Python), "openjdk-head" (Java)
    public String executeCode(String compiler, String code) {
        try {
            String jsonPayload = String.format(
                    "{\"compiler\": \"%s\", \"code\": \"%s\"}",
                    compiler, escapeJson(code)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://wandbox.org/api/compile.json"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();

        } catch (Exception e) {
            e.printStackTrace(); // Prints the complete stack trace to the console
            return "{\"error\": \"" + e.toString().replace("\"", "\\\"") + "\"}";
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

    public static void main(String[] args) {
        WandboxTester tester = new WandboxTester();

        System.out.println("--- Testing Wandbox C++ ---");
        String cppCode = "#include <iostream>\nint main() { std::cout << \"Hello Wandbox C++!\"; return 0; }";
        System.out.println(tester.executeCode("gcc-head", cppCode));

        System.out.println("\n--- Testing Wandbox Python ---");
        String pythonCode = "print('Hello Wandbox Python!')";
        System.out.println(tester.executeCode("cpython-3.14.0", pythonCode));

        System.out.println("\n--- Testing Wandbox Java ---");
        String javaCode = "class Main { public static void main(String[] args) { System.out.println(\"Hello Wandbox Java!\"); } }";
        System.out.println(tester.executeCode("openjdk-jdk-22+36", javaCode));
    }
}
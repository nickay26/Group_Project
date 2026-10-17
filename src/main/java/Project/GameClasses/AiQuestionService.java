package Project.GameClasses;

import Project.API_Database.ApiClient;
import org.json.JSONObject;

public class AiQuestionService {

    public static String fetchAiQuestion(String selectedOption) {
        String language = "Python";
        String level = "Basic";

        try {
            if (selectedOption != null && !selectedOption.isBlank()) {
                language = selectedOption;
                if (language.contains(" (")) {
                    String[] parts = language.split(" \\(");
                    language = parts[0];
                    level = parts[1].replace(")", "");
                }
            }

            // ApiClient မှတစ်ဆင့် Coding Challenge တောင်းယူခြင်း
            String responseBody = ApiClient.fetchAiQuestionsBatchAsync(language, level, 1)
                    .exceptionally(ex -> null)
                    .join();

            if (responseBody != null && !responseBody.isBlank()) {
                return responseBody;
            }

        } catch (Exception e) {
            System.err.println("Error fetching challenge: " + e.getMessage());
        }

        // Fallback Coding Challenge JSON
        return getFallbackChallenge(language);
    }

    private static String getFallbackChallenge(String language) {
        String starter = switch (language.toLowerCase()) {
            case "java" -> "public class Main {\n    public static void main(String[] args) {\n        // Write code here\n        System.out.println(\"Hello, World!\");\n    }\n}";
            case "c++" -> "#include <iostream>\nusing namespace std;\n\nint main() {\n    // Write code here\n    cout << \"Hello, World!\";\n    return 0;\n}";
            default -> "# Write your solution here\ndef solve():\n    print('Hello, World!')\n\nsolve()";
        };

        JSONObject fallback = new JSONObject();
        fallback.put("title", language + " Coding Challenge");
        fallback.put("description", "Write a complete program in " + language + " that outputs 'Hello, World!' to standard output.");
        fallback.put("starter_code", starter);

        return fallback.toString();
    }
}
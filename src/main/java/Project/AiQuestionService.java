package Project;

import org.json.JSONArray;
import org.json.JSONObject;

public class AiQuestionService {

    public static String fetchAiQuestion(String selectedOption) {
        return fetchAiQuestionsBatch(selectedOption, 5);
    }

    public static String fetchAiQuestionsBatch(String selectedOption, int count) {
        String language = "C++";
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

            // 1. Backend မှ မေးခွန်းများ လှမ်းတောင်းမည်
            String responseBody = ApiClient.fetchAiQuestionsBatchAsync(language, level, count)
                    .exceptionally(ex -> {
                        System.err.println("API Exception: " + ex.getMessage());
                        ex.printStackTrace();
                        return null;
                    })
                    .join();

            System.out.println("Raw Server Response: " + responseBody);

            if (responseBody != null && !responseBody.isBlank()) {
                String validatedJson = cleanAndValidateJson(responseBody);
                if (validatedJson != null) {
                    return validatedJson;
                }
            }

        } catch (Exception e) {
            System.err.println("Error in AiQuestionService: " + e.getMessage());
            e.printStackTrace();
        }

        // 2. Network/Parse Error ဖြစ်ပါက Fallback မေးခွန်း ပေးမည်
        return getFallbackQuestions(language);
    }

    private static String cleanAndValidateJson(String rawText) {
        if (rawText == null || rawText.isBlank()) return null;

        String cleaned = rawText.trim();

        if (cleaned.startsWith("<") || cleaned.contains("<!DOCTYPE") || cleaned.contains("<html")) {
            return null;
        }

        if (cleaned.startsWith("{")) {
            try {
                JSONObject obj = new JSONObject(cleaned);
                if (obj.has("candidates")) {
                    cleaned = obj.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text").trim();
                } else if (obj.has("questions")) {
                    return obj.getJSONArray("questions").toString();
                } else if (obj.has("text")) {
                    cleaned = obj.getString("text").trim();
                }
            } catch (Exception ignored) {}
        }

        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        cleaned = cleaned.trim();

        try {
            JSONArray array = new JSONArray(cleaned);
            if (array.length() > 0) {
                return array.toString();
            }
        } catch (Exception e) {
            int start = cleaned.indexOf('[');
            int end = cleaned.lastIndexOf(']');
            if (start != -1 && end > start) {
                try {
                    String substring = cleaned.substring(start, end + 1);
                    JSONArray array = new JSONArray(substring);
                    if (array.length() > 0) {
                        return array.toString();
                    }
                } catch (Exception ex) {
                    System.err.println("Sub-string JSON validation failed: " + ex.getMessage());
                }
            }
            System.err.println("JSON validation failed in AiQuestionService: " + e.getMessage());
        }

        return null;
    }

    private static String getFallbackQuestions(String language) {
        return """
            [
              {
                "question": "What is the primary output stream syntax in %s?",
                "options": ["cout << 'Hello';", "System.out.println()", "printf()", "Console.WriteLine()"],
                "correctIndex": 0
              },
              {
                "question": "Which header/package is commonly used for core features in %s?",
                "options": ["Standard Library", "System Core", "Main Package", "IO Stream"],
                "correctIndex": 0
              }
            ]
            """.formatted(language, language);
    }
}
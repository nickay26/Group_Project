package Project;

import org.json.JSONArray;
import org.json.JSONObject;

public class AiQuestionService {

    public static String fetchAiQuestion(String selectedOption) {
        return fetchAiQuestionsBatch(selectedOption, 5);
    }

    public static String fetchAiQuestionsBatch(String selectedOption, int count) {
        try {
            String language = selectedOption != null ? selectedOption : "C++";
            String level = "Basic";

            if (language.contains(" (")) {
                String[] parts = language.split(" \\(");
                language = parts[0];
                level = parts[1].replace(")", "");
            }

            // ApiClient မှတစ်ဆင့် Async ဖြင့် ခေါ်ယူပြီး ရလဒ်ကို စောင့်ယူခြင်း
            String responseBody = ApiClient.fetchAiQuestionsBatchAsync(language, level, count)
                    .join(); // Async ရလဒ် ရောက်သည်အထိ စောင့်မည်

            if (responseBody != null && !responseBody.isBlank()) {
                // Backend မှ Gemini Response Envelope (candidates -> content -> parts) ပါလာပါက parse လုပ်မည်
                if (responseBody.contains("candidates")) {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String rawText = jsonResponse
                            .getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");

                    return cleanJsonString(rawText);
                }

                // Backend မှ တိုက်ရိုက် JSON Array ပြန်ပေးပါက သန့်စင်ပြီး ပြန်ပေးမည်
                return cleanJsonString(responseBody);
            } else {
                System.err.println("Empty response from ApiClient backend");
                return getFallbackQuestions(language);
            }

        } catch (Exception e) {
            System.err.println("API Exception via ApiClient: " + e.getMessage());
            return getFallbackQuestions("C++");
        }
    }
    private static String cleanJsonString(String rawText) {
        if (rawText == null || rawText.isBlank()) return getFallbackQuestions("C++");

        String cleaned = rawText.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        cleaned = cleaned.trim();

        // Object ဖြစ်နေပါက Array ပါသော နေရာကို ရှာပြီး ဆွဲထုတ်မည်
        if (cleaned.startsWith("{")) {
            try {
                JSONObject obj = new JSONObject(cleaned);
                if (obj.has("questions")) {
                    return obj.getJSONArray("questions").toString();
                } else if (obj.has("candidates")) {
                    String extractedText = obj.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");
                    return cleanJsonString(extractedText); // Recursive Clean
                }
            } catch (Exception ignored) {}

            // String ထဲမှ '[' နှင့် ']' ကြားရှိ စာသားကို သီးသန့် ဖြတ်ယူမည်
            int firstBracket = cleaned.indexOf('[');
            int lastBracket = cleaned.lastIndexOf(']');
            if (firstBracket != -1 && lastBracket > firstBracket) {
                return cleaned.substring(firstBracket, lastBracket + 1);
            }
        }

        // JSON Array မဟုတ်ပါက Fallback မေးခွန်း ပြန်ပေးမည်
        if (!cleaned.startsWith("[")) {
            return getFallbackQuestions("C++");
        }

        return cleaned;
    }

    // Backend/Network အဆင်မပြေပါက သုံးမည့် Fallback Question
    private static String getFallbackQuestions(String language) {
        return """
            [
              {
                "question": "What is the correct way to output text in %s?",
                "options": ["Standard output stream", "System print stream", "Console log", "File writer"],
                "correctIndex": 0
              }
            ]
            """.formatted(language);
    }
}
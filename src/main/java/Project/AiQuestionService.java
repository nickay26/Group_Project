package Project;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

public class AiQuestionService {

    // ⚠️ မိမိ၏ Gemini API Key (AIzaSy...) ကို ဒီနေရာတွင် ထည့်ပါ
    private static final String API_KEY = "AQ.Ab8RN6IGju_XNY-bAgyVdvQic7vwOskvS5alYQm6dZCljVuKFA";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

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

            String promptText = """
                Generate a JSON array of %d COMPLETELY DIFFERENT, UNIQUE multiple-choice questions about %s programming language at %s level.
                Do NOT repeat any questions. Each question MUST be unique.
                Format strictly as a JSON array:
                [
                  {
                    "question": "Question text here?",
                    "options": ["Option A", "Option B", "Option C", "Option D"],
                    "correctIndex": 0
                  }
                ]
                Do NOT include markdown backticks like ```json. Return ONLY valid raw JSON array text.
                """.formatted(count, language.toUpperCase(), level.toUpperCase());

            JSONObject partObj = new JSONObject();
            partObj.put("text", promptText);

            JSONArray partsArray = new JSONArray();
            partsArray.put(partObj);

            JSONObject contentObj = new JSONObject();
            contentObj.put("parts", partsArray);

            JSONArray contentsArray = new JSONArray();
            contentsArray.put(contentObj);

            JSONObject requestJson = new JSONObject();
            requestJson.put("contents", contentsArray);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                System.err.println("Gemini Error Code: " + response.statusCode());
                System.err.println("Response: " + response.body());
                return null;
            }

        } catch (Exception e) {
            System.err.println("API Exception: " + e.getMessage());
            return null;
        }
    }
}
package Project.GameClasses;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class QuestionApiClient {

    // ⭐ Security နှင့် Redirect ပြဿနာများ ကာကွယ်ရန် HTTPS သို့ ပြောင်းထားပါသည်
    private static final String BASE_URL = "https://NickayJohn26.pythonanywhere.com";

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // ၁။ PythonAnywhere ထံမှ မေးခွန်း လှမ်းတောင်းသည့် Method
    public static String getRandomQuestion(String language, String difficulty) {
        try {
            // URL safe ဖြစ်စေရန် Parameters များကို Encode လုပ်ခြင်း
            String encodedLang = URLEncoder.encode(language != null ? language : "java", StandardCharsets.UTF_8);
            String encodedDiff = URLEncoder.encode(difficulty != null ? difficulty : "easy", StandardCharsets.UTF_8);

            String url = BASE_URL + "/api/get-question?language=" + encodedLang + "&difficulty=" + encodedDiff;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                System.err.println("API Request failed with status code: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Error fetching question from API: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // ၂။ XP အမှတ် သွားပြင်ပေးသည့် Method (Asynchronous)
    public static void updateXp(int userId, int earnedXp) {
        try {
            String url = BASE_URL + "/api/update-xp";
            String jsonInputString = String.format("{\"user_id\": %d, \"xp\": %d}", userId, earnedXp);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonInputString))
                    .build();

            // Background Thread တွင် အလုပ်လုပ်မည်ဖြစ်ပြီး ရလဒ်ကို စစ်ဆေးမည်
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200) {
                            System.out.println("XP updated successfully for user: " + userId);
                        } else {
                            System.err.println("Failed to update XP. Status Code: " + response.statusCode());
                        }
                    })
                    .exceptionally(ex -> {
                        System.err.println("Error during async XP update: " + ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            System.err.println("Error building XP update request: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
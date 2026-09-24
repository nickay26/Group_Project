package Project;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class QuestionApiClient {

    private static final String BASE_URL = "http://NickayJohn26.pythonanywhere.com";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // ၁။ PythonAnywhere ထံမှ မေးခွန်း လှမ်းတောင်းသည့် Method
    public static String getRandomQuestion(String language, String difficulty) {
        try {
            String url = BASE_URL + "/api/get-question?language=" + language + "&difficulty=" + difficulty;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ၂။ XP အမှတ် သွားပြင်ပေးသည့် Method
    public static void updateXp(int userId, int earnedXp) {
        try {
            String url = BASE_URL + "/api/update-xp";
            String jsonInputString = String.format("{\"user_id\": %d, \"xp\": %d}", userId, earnedXp);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonInputString))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
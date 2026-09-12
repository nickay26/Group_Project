package Project;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DatabaseApiTest {

    private static final String BASE_URL = "https://nickayjohn26.pythonanywhere.com/api";
    private static final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) {
        System.out.println("=== 1. TESTING DATABASE INSERT (Register User) ===");
        // Unique email generated using timestamp to prevent duplicate key errors
        String testEmail = "student_" + System.currentTimeMillis() + "@example.com";
        testUserRegistration("Alice Smith", testEmail, "securePass123");

        System.out.println("\n=== 2. TESTING DATABASE SELECT (Fetch Course Lessons) ===");
        testFetchCourseLessons("JAVA_101");
    }

    /**
     * Tests inserting a new record into SQLite User_table via POST /api/register
     */
    public static void testUserRegistration(String name, String email, String password) {
        try {
            String jsonPayload = String.format(
                    "{\"name\":\"%s\", \"email\":\"%s\", \"password\":\"%s\"}",
                    name, email, password
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("HTTP Status: " + response.statusCode());
            System.out.println("DB Response: " + response.body());

        } catch (Exception e) {
            System.err.println("Database Write Failed: " + e.getMessage());
        }
    }

    /**
     * Tests querying records from SQLite Lesson_table via GET /api/courses/{id}/lessons
     */
    public static void testFetchCourseLessons(String courseId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/courses/" + courseId + "/lessons"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("HTTP Status: " + response.statusCode());
            System.out.println("DB Response: " + response.body());

        } catch (Exception e) {
            System.err.println("Database Query Failed: " + e.getMessage());
        }
    }
}
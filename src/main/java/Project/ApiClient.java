package Project;

import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class ApiClient {

    private static final String BASE_URL = "https://nickayjohn26.pythonanywhere.com";

    private static final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    // ==========================================
    // PROFILE IMAGE HELPERS & APIS (OPTIMIZED)
    // ==========================================

    public static CompletableFuture<String> requestPasswordResetAsync(String email) {
        JSONObject body = new JSONObject();
        body.put("email", email);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/auth/request-reset"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> null);
    }

    public static CompletableFuture<String> confirmPasswordResetAsync(String email, String code, String newPassword) {
        JSONObject body = new JSONObject();
        body.put("email", email);
        body.put("code", code);
        body.put("new_password", newPassword);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/auth/confirm-reset"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> null);
    }

    public static CompletableFuture<String> updatePasswordAsync(int userId, String currentPassword, String newPassword) {
        JSONObject body = new JSONObject();
        body.put("user_id", userId);
        body.put("current_password", currentPassword);
        body.put("new_password", newPassword);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/auth/update-password"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> null);
    }

    public static String getProfileImageUrl(String profilePath) {
        if (profilePath == null || profilePath.trim().isEmpty()) {
            return null;
        }
        String trimmed = profilePath.trim();

        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }

        if (trimmed.startsWith("file:") || trimmed.contains("/") || trimmed.contains("\\")) {
            java.io.File f = new java.io.File(trimmed.replace("file:", ""));
            trimmed = f.getName();
        }

        return BASE_URL + "/uploads/profiles/" + trimmed;
    }

    /**
     * Compresses and resizes large images locally before uploading
     * to prevent SSL timeouts and speed up network transmission.
     */
    private static File compressImage(File originalFile) {
        try {
            java.awt.image.BufferedImage originalImage = javax.imageio.ImageIO.read(originalFile);
            if (originalImage == null) {
                return originalFile; // Fallback if format isn't readable
            }

            int targetWidth = 300;
            int targetHeight = 300;

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            // Skip compression if already small enough
            if (originalWidth <= targetWidth && originalHeight <= targetHeight && originalFile.length() < 200 * 1024) {
                return originalFile;
            }

            double ratio = (double) originalWidth / originalHeight;
            if (ratio > 1) {
                targetHeight = (int) (targetWidth / ratio);
            } else {
                targetWidth = (int) (targetHeight * ratio);
            }

            java.awt.image.BufferedImage resizedImage = new java.awt.image.BufferedImage(targetWidth, targetHeight, java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2d = resizedImage.createGraphics();
            g2d.drawImage(originalImage.getScaledInstance(targetWidth, targetHeight, java.awt.Image.SCALE_SMOOTH), 0, 0, null);
            g2d.dispose();

            File compressedFile = File.createTempFile("avatar_compressed_", ".jpg");
            javax.imageio.ImageIO.write(resizedImage, "jpg", compressedFile);
            return compressedFile;
        } catch (Exception e) {
            System.err.println("Image compression failed, using original: " + e.getMessage());
            return originalFile;
        }
    }

    private static byte[] createMultipartBody(int userId, File file, String boundary) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String lineFeed = "\r\n";

        baos.write(("--" + boundary + lineFeed).getBytes(StandardCharsets.UTF_8));
        baos.write(("Content-Disposition: form-data; name=\"user_id\"" + lineFeed + lineFeed).getBytes(StandardCharsets.UTF_8));
        baos.write((userId + lineFeed).getBytes(StandardCharsets.UTF_8));

        String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null) {
            mimeType = "image/jpeg";
        }

        baos.write(("--" + boundary + lineFeed).getBytes(StandardCharsets.UTF_8));
        baos.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"" + lineFeed).getBytes(StandardCharsets.UTF_8));
        baos.write(("Content-Type: " + mimeType + lineFeed + lineFeed).getBytes(StandardCharsets.UTF_8));
        baos.write(Files.readAllBytes(file.toPath()));
        baos.write(lineFeed.getBytes(StandardCharsets.UTF_8));

        baos.write(("--" + boundary + "--" + lineFeed).getBytes(StandardCharsets.UTF_8));

        return baos.toByteArray();
    }

    public static CompletableFuture<String> uploadProfileImageAsync(int userId, File imageFile) {
        return CompletableFuture.supplyAsync(() -> {
            File compressedFile = null;
            try {
                // 1. Compress image locally first
                compressedFile = compressImage(imageFile);

                String boundary = "----JavaHttpClientBoundary" + System.currentTimeMillis();
                byte[] multipartBody = createMultipartBody(userId, compressedFile, boundary);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/api/user/upload-profile"))
                        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                        .header("Connection", "keep-alive")
                        .timeout(Duration.ofSeconds(30))
                        .POST(HttpRequest.BodyPublishers.ofByteArray(multipartBody))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return response.body();
            } catch (Exception ex) {
                System.err.println("Upload Profile Error: " + ex.getMessage());
                return null;
            } finally {
                // Clean up temp compressed file if created
                if (compressedFile != null && compressedFile != imageFile && compressedFile.exists()) {
                    compressedFile.delete();
                }
            }
        });
    }

    public static CompletableFuture<String> resetPasswordAsync(String email, String newPassword) {
        JSONObject body = new JSONObject();
        body.put("email", email);
        body.put("new_password", newPassword);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/auth/reset-password"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Reset Password Error: " + ex.getMessage());
                    return null;
                });
    }

    // ==========================================
    // 2. CODE EXECUTION & AI SERVICES (ASYNC)
    // ==========================================

    public static CompletableFuture<String> executeCodeAsync(String language, String code) {
        JSONObject body = new JSONObject();
        body.put("language", language);
        body.put("code", code);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/execute"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Execute Code Error: " + ex.getMessage());
                    return null;
                });
    }

    public static CompletableFuture<String> reviewCodeAsync(String language, String code) {
        JSONObject body = new JSONObject();
        body.put("language", language);
        body.put("code", code);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/review"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(ApiClient::cleanAndFormatAiText)
                .exceptionally(ex -> {
                    System.err.println("Review Code Error: " + ex.getMessage());
                    return null;
                });
    }

    public static CompletableFuture<String> generateCodeChallengeAsync(String language, String level) {
        JSONObject body = new JSONObject();
        body.put("language", language);
        body.put("level", level);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/challenge/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Generate Code Challenge Error: " + ex.getMessage());
                    return null;
                });
    }

    public static CompletableFuture<String> reviewPracticeCodeAsync(String language, String code, String task) {
        JSONObject body = new JSONObject();
        body.put("language", language);
        body.put("code", code);
        body.put("task", task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/questions/review"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(ApiClient::cleanAndFormatAiText)
                .exceptionally(ex -> {
                    System.err.println("Review Practice Error: " + ex.getMessage());
                    return null;
                });
    }

    public static CompletableFuture<String> generateQuestionAsync(String language, String topic, int level) {
        String[] types = {"fill-blank", "drag-drop", "write-code"};
        String type = types[(int)(Math.random() * types.length)];

        JSONObject body = new JSONObject();
        body.put("language", language);
        body.put("topic", topic);
        body.put("level", level);
        body.put("type", type);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/questions/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Generate Question Error: " + ex.getMessage());
                    return null;
                });
    }

    public static CompletableFuture<String> fetchAiQuestionsBatchAsync(String language, String level, int count) {
        JSONObject body = new JSONObject();
        body.put("language", language);
        body.put("level", level);
        body.put("count", count);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/questions/batch"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Batch Questions API Error: " + ex.getMessage());
                    return null;
                });
    }

    // ==========================================
    // 3. PROJECT MANAGEMENT (ASYNC)
    // ==========================================

    public static CompletableFuture<String> saveProjectCodeAsync(int projectId, String codeContent) {
        JSONObject body = new JSONObject();
        body.put("code_content", codeContent);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/projects/" + projectId))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Save Project Error: " + ex.getMessage());
                    return null;
                });
    }

    // ==========================================
    // 4. MATCHMAKING & ROOMS (ASYNC)
    // ==========================================

    public static CompletableFuture<String> createMatchRoomAsync(String language, String level) {
        JSONObject body = new JSONObject();
        body.put("language", language);
        body.put("level", level);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/match/create"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Create Match Room Error: " + ex.getMessage());
                    return null;
                });
    }

    public static CompletableFuture<String> joinMatchRoomAsync(String roomCode) {
        String cleanRoom = URLEncoder.encode(roomCode.trim(), StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/match/join/" + cleanRoom))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Join Match Room Error: " + ex.getMessage());
                    return null;
                });
    }

    // ==========================================
    // 5. BRAIN & ENERGY SYSTEM (ASYNC)
    // ==========================================

    public static CompletableFuture<String> deductBrainAsync(int userId) {
        JSONObject body = new JSONObject();
        body.put("user_id", userId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/users/brains/deduct"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Deduct Brain Error: " + ex.getMessage());
                    return null;
                });
    }

    public static CompletableFuture<String> refillBrainsAsync(int userId, int cost) {
        JSONObject body = new JSONObject();
        body.put("user_id", userId);
        body.put("cost", cost);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/users/brains/refill"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Refill Brains Error: " + ex.getMessage());
                    return null;
                });
    }

    public static CompletableFuture<String> getUserBrainsAsync(int userId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/users/" + userId + "/brains"))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Get User Brains Error: " + ex.getMessage());
                    return null;
                });
    }

    // ==========================================
    // 6. LESSON PROGRESS & STREAK SYSTEM (ASYNC)
    // ==========================================

    public static CompletableFuture<String> completeLessonAsync(int userId, String courseId, int lessonOrder, int xp) {
        JSONObject body = new JSONObject();
        body.put("user_id", userId);
        body.put("course_id", courseId);
        body.put("lesson_order", lessonOrder);
        body.put("xp", xp);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/lessons/complete"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Complete Lesson Error: " + ex.getMessage());
                    return null;
                });
    }

    public static CompletableFuture<String> getUserActivityAsync(int userId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/users/" + userId + "/activity"))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Get User Activity Error: " + ex.getMessage());
                    return null;
                });
    }

    // ==========================================
    // 7. LEADERBOARD & XP SYSTEM (ASYNC)
    // ==========================================

    public static CompletableFuture<String> getLeaderboardAsync(int leagueId) {
        return getLeaderboardAsync(null, leagueId);
    }

    public static CompletableFuture<String> getLeaderboardAsync(String seasonId, int leagueId) {
        int userId = UserSession.getUserId(); // Pass the active user ID
        String url = BASE_URL + "/api/leaderboard";
        if (seasonId != null && !seasonId.trim().isEmpty() && !seasonId.equalsIgnoreCase("current")) {
            url += "/" + URLEncoder.encode(seasonId, StandardCharsets.UTF_8);
        }
        url += "?league_id=" + leagueId + "&user_id=" + userId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Get Leaderboard Error: " + ex.getMessage());
                    return null;
                });
    }

    public static CompletableFuture<String> getUserLeagueStatusAsync(int userId) {
        return getUserLeagueStatusAsync(userId, null);
    }

    public static CompletableFuture<String> getUserLeagueStatusAsync(int userId, String seasonId) {
        String url = BASE_URL + "/api/user/" + userId + "/league-status";
        if (seasonId != null && !seasonId.trim().isEmpty() && !seasonId.equalsIgnoreCase("current")) {
            url += "/" + URLEncoder.encode(seasonId, StandardCharsets.UTF_8);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Get User League Status Error: " + ex.getMessage());
                    return null;
                });
    }

    public static CompletableFuture<String> addUserXpAsync(int userId, int xp) {
        return addUserXpAsync(userId, null, xp, 1);
    }

    public static CompletableFuture<String> addUserXpAsync(int userId, String seasonId, int xp, int leagueId) {
        JSONObject body = new JSONObject();
        body.put("user_id", userId);
        body.put("xp", xp);
        body.put("league_id", leagueId);
        if (seasonId != null && !seasonId.trim().isEmpty()) {
            body.put("season_id", seasonId);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/user/add-xp"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Add User XP Error: " + ex.getMessage());
                    return null;
                });
    }

    // ==========================================
    // 8. USER PROFILE & CERTIFICATE SYSTEM (ASYNC)
    // ==========================================

    public static CompletableFuture<String> getUserProfileAsync(String userId) {
        String url = BASE_URL + "/api/user/" + userId.trim();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Fetch User Profile Error: " + ex.getMessage());
                    return null;
                });
    }

    public static CompletableFuture<String> checkAndIssueCertificateAsync(int userId, String courseId) {
        JSONObject body = new JSONObject();
        body.put("user_id", userId);
        body.put("course_id", courseId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/certificates/check"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Check Certificate Error: " + ex.getMessage());
                    return null;
                });
    }

    public static CompletableFuture<String> getUserCertificatesAsync(int userId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/users/" + userId + "/certificates"))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    System.err.println("Get User Certificates Error: " + ex.getMessage());
                    return null;
                });
    }

    // ==========================================
    // 9. HELPER METHODS
    // ==========================================

    public static String cleanAndFormatAiText(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            return "";
        }

        String extractedText = rawResponse;

        try {
            JSONObject json = new JSONObject(rawResponse);
            if (json.has("review")) {
                extractedText = json.getString("review");
            } else if (json.has("result")) {
                extractedText = json.getString("result");
            } else if (json.has("explanation")) {
                extractedText = json.getString("explanation");
            } else if (json.has("output")) {
                extractedText = json.getString("output");
            }
        } catch (Exception e) {
            // Raw text fallback
        }

        return extractedText
                .replaceAll("\\*\\*(.*?)\\*\\*", "$1")
                .replaceAll("###\\s*", "")
                .replaceAll("##\\s*", "")
                .replaceAll("#\\s*", "")
                .replaceAll("```[a-zA-Z]*", "")
                .replaceAll("`", "")
                .replaceAll("\\$\\$(.*?)\\$\\$", "$1")
                .replaceAll("\\$(.*?)\\$", "$1")
                .trim();
    }
}
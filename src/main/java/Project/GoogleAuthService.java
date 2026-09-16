package Project;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class GoogleAuthService {

    private static final int PORT = 8888;
    private static final String CLIENT_ID = "929038060577-2510s6pu89lm46rtl9btpbsvrhq7h862.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    private static final String REDIRECT_URI = "http://localhost:" + PORT + "/callback";

    public interface GoogleUserCallback {
        void onAuthenticated(String email, String name, String picture);
    }

    public static void startGoogleLogin(Consumer<String> onCodeReceived) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            server.createContext("/callback", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String query = exchange.getRequestURI().getQuery();
                    Map<String, String> params = parseQuery(query);
                    String authCode = params.get("code");

                    String responseHtml = "<html><body style='font-family:sans-serif; text-align:center; padding-top:50px;'>"
                            + "<h2>Authentication Successful!</h2>"
                            + "<p>You can close this browser window and return to the application.</p>"
                            + "</body></html>";

                    exchange.sendResponseHeaders(200, responseHtml.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(responseHtml.getBytes());
                    os.close();

                    server.stop(1);

                    if (authCode != null) {
                        Platform.runLater(() -> onCodeReceived.accept(authCode));
                    }
                }
            });

            server.start();

            String authUrl = "https://accounts.google.com/o/oauth2/v2/auth?"
                    + "client_id=" + CLIENT_ID
                    + "&redirect_uri=" + REDIRECT_URI
                    + "&response_type=code"
                    + "&scope=email%20profile"
                    + "&access_type=offline";

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(authUrl));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void fetchUserProfile(String authCode, GoogleUserCallback callback) {
        CompletableFuture.runAsync(() -> {
            try {
                // 1. Exchange OAuth Code for Access Token
                URL tokenUrl = new URL("https://oauth2.googleapis.com/token");
                HttpURLConnection tokenConn = (HttpURLConnection) tokenUrl.openConnection();
                tokenConn.setRequestMethod("POST");
                tokenConn.setDoOutput(true);
                tokenConn.setConnectTimeout(10000);
                tokenConn.setReadTimeout(10000);
                tokenConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String tokenBody = "code=" + URLEncoder.encode(authCode, StandardCharsets.UTF_8)
                        + "&client_id=" + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8)
                        + "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, StandardCharsets.UTF_8)
                        + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)
                        + "&grant_type=authorization_code";

                try (OutputStream os = tokenConn.getOutputStream()) {
                    os.write(tokenBody.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                String tokenResponseBody = readStream(tokenConn);
                String accessToken = JsonUtils.extractKey(tokenResponseBody, "access_token");

                if (accessToken != null && !accessToken.isEmpty()) {
                    // 2. Fetch User Profile Info
                    URL userUrl = new URL("https://www.googleapis.com/oauth2/v3/userinfo");
                    HttpURLConnection userConn = (HttpURLConnection) userUrl.openConnection();
                    userConn.setRequestMethod("GET");
                    userConn.setConnectTimeout(10000);
                    userConn.setReadTimeout(10000);
                    userConn.setRequestProperty("Authorization", "Bearer " + accessToken);

                    String userResponseBody = readStream(userConn);

                    String email = JsonUtils.extractKey(userResponseBody, "email");
                    String name = JsonUtils.extractKey(userResponseBody, "name");
                    String picture = JsonUtils.extractKey(userResponseBody, "picture");

                    callback.onAuthenticated(email, name, picture);
                } else {
                    System.err.println("Token Exchange Failed: " + tokenResponseBody);
                }
            } catch (Exception e) {
                System.err.println("Error fetching Google profile:");
                e.printStackTrace();
            }
        });
    }

    private static String readStream(HttpURLConnection conn) throws IOException {
        InputStream is = (conn.getResponseCode() >= 400) ? conn.getErrorStream() : conn.getInputStream();
        if (is == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) return result;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1) {
                result.put(pair[0], pair[1]);
            }
        }
        return result;
    }
}
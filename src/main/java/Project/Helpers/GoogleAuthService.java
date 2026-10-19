package Project.Helpers;

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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class GoogleAuthService {

    private static final int PORT = 8888;
    // Must match GOOGLE_CLIENT_ID in your flask_app.py
    private static final String CLIENT_ID = "929038060577-24kqfelbq62kqcsmrv0706e7523ql7fl.apps.googleusercontent.com";
    private static final String REDIRECT_URI = "http://localhost:" + PORT + "/callback";

    // Replace with your actual PythonAnywhere URL
    private static final String BACKEND_AUTH_URL = "https://NickayJohn26.pythonanywhere.com/api/auth/google";

    public interface GoogleUserCallback {
        void onAuthenticated(int userId, String email, String name);
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
                            + "<p>You can close this window and return to the application.</p>"
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
                URL backendUrl = new URL(BACKEND_AUTH_URL);
                HttpURLConnection conn = (HttpURLConnection) backendUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestProperty("Content-Type", "application/json");

                String jsonPayload = "{\"code\":\"" + authCode + "\"}";

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                String responseBody = readStream(conn);

                if (conn.getResponseCode() == 200) {
                    String userIdStr = JsonUtils.extractKey(responseBody, "user_id");
                    int userId = userIdStr != null ? Integer.parseInt(userIdStr) : 1;
                    String email = JsonUtils.extractKey(responseBody, "email");
                    String name = JsonUtils.extractKey(responseBody, "name");

                    Platform.runLater(() -> callback.onAuthenticated(userId, email, name));
                } else {
                    System.err.println("Backend OAuth Error: " + responseBody);
                }
            } catch (Exception e) {
                System.err.println("Error authenticating with backend:");
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
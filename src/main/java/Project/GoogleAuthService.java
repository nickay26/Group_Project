package Project;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import javafx.application.Platform;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class GoogleAuthService {

    private static final int PORT = 8888;
    private static final String CLIENT_ID = "784664347763-vvt49ns53p2gau9afp6sdn5hpobo265h.apps.googleusercontent.com";
    private static final String REDIRECT_URI = "http://localhost:" + PORT + "/callback";

    public static void startGoogleLogin(Consumer<String> onCodeReceived) {
        try {
            // 1. Create local HTTP listener on port 8888
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            server.createContext("/callback", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String query = exchange.getRequestURI().getQuery();
                    Map<String, String> params = parseQuery(query);
                    String authCode = params.get("code");

                    // Webpage response sent back to browser
                    String responseHtml = "<html><body style='font-family:sans-serif; text-align:center; padding-top:50px;'>"
                            + "<h2>Authentication Successful!</h2>"
                            + "<p>You can close this browser window and return to the Mimo app.</p>"
                            + "</body></html>";

                    exchange.sendResponseHeaders(200, responseHtml.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(responseHtml.getBytes());
                    os.close();

                    // Shut down local server
                    server.stop(1);

                    // Pass authorization code back to JavaFX UI thread
                    if (authCode != null) {
                        Platform.runLater(() -> onCodeReceived.accept(authCode));
                    }
                }
            });

            server.start();

            // 2. Open Google Login consent screen in default browser
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
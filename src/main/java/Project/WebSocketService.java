package Project;

import javafx.application.Platform;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

public class WebSocketService implements WebSocket.Listener {

    private static WebSocketService instance;
    private WebSocket webSocket;
    private NotificationListener notificationListener;

    public interface NotificationListener {
        void onInviteReceived(String fromUser, String language);
        void onGameStart(String roomId, String language);
        void onQuickMatchStart(String roomId, String language, String questionsJson);
    }

    public static WebSocketService getInstance() {
        if (instance == null) {
            instance = new WebSocketService();
        }
        return instance;
    }

    // မူလ Overload Method
    public void connect(String serverUrl, String currentUserId, NotificationListener listener) {
        connect(serverUrl, currentUserId, listener, null);
    }

    // Connection ကျပြီးမှ အလုပ်လုပ်မည့် Callback ပါဝင်သော Connect Method
    public void connect(String serverUrl, String currentUserId, NotificationListener listener, Runnable onConnected) {
        this.notificationListener = listener;

        // Connection ပွင့်နေဆဲ ဖြစ်ပါက Register သာ ထပ်လုပ်မည်
        if (webSocket != null && !webSocket.isInputClosed() && !webSocket.isOutputClosed()) {
            System.out.println("WebSocket active. Re-registering user: " + currentUserId);
            registerUser(currentUserId);
            if (onConnected != null) {
                onConnected.run();
            }
            return;
        }

        this.webSocket = null;

        HttpClient client = HttpClient.newHttpClient();
        client.newWebSocketBuilder()
                .buildAsync(URI.create(serverUrl), this)
                .thenAccept(ws -> {
                    this.webSocket = ws;
                    System.out.println("Connected to Render WebSocket Server: " + serverUrl);
                    registerUser(currentUserId);

                    // Connection အောင်မြင်စွာ ရပြီးမှ Callback ကို ခေါ်ပေးမည်
                    if (onConnected != null) {
                        onConnected.run();
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("WebSocket Connection Failed: " + ex.getMessage());
                    return null;
                });
    }

    private void registerUser(String currentUserId) {
        JSONObject reg = new JSONObject();
        reg.put("type", "REGISTER");
        reg.put("userId", currentUserId);
        sendText(reg.toString());
    }

    public void sendText(String text) {
        if (webSocket != null && !webSocket.isOutputClosed()) {
            webSocket.sendText(text, true);
        } else {
            System.err.println("Cannot send message. WebSocket is not connected.");
        }
    }

    public void sendInvite(String fromUser, String toUser, String language) {
        JSONObject json = new JSONObject();
        json.put("type", "INVITE_SEND");
        json.put("fromUser", fromUser);
        json.put("toUser", toUser);
        json.put("language", language);
        sendText(json.toString());
    }

    public void respondInvite(String toUser, String action) {
        JSONObject json = new JSONObject();
        json.put("type", "INVITE_RESPONSE");
        json.put("toUser", toUser);
        json.put("action", action);
        sendText(json.toString());
    }

    public void joinQuickMatch(String currentUserId, String language) {
        JSONObject json = new JSONObject();
        json.put("type", "QUICK_MATCH_JOIN");
        json.put("userId", currentUserId);
        json.put("language", language);
        sendText(json.toString());
    }

    public void cancelQuickMatch(String currentUserId) {
        JSONObject json = new JSONObject();
        json.put("type", "QUICK_MATCH_CANCEL");
        json.put("userId", currentUserId);
        sendText(json.toString());
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        String message = data.toString();
        System.out.println("Received from Server: " + message);

        JSONObject json = new JSONObject(message);
        String type = json.optString("type");

        Platform.runLater(() -> {
            if (notificationListener == null) return;

            if ("INVITE_RECEIVED".equals(type)) {
                String fromUser = json.getString("fromUser");
                String language = json.optString("language", "java");
                notificationListener.onInviteReceived(fromUser, language);
            } else if ("START_GAME".equals(type)) {
                String roomId = json.getString("roomId");
                String language = json.optString("language", "java");
                notificationListener.onGameStart(roomId, language);
            } else if ("QUICK_MATCH_START".equals(type)) {
                String roomId = json.optString("roomId", "");
                String language = json.optString("language", "Python");
                Object questionsObj = json.opt("questions");
                String questionsJson = questionsObj != null ? questionsObj.toString() : "";

                notificationListener.onQuickMatchStart(roomId, language, questionsJson);
            }
        });

        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.err.println("WebSocket Error: " + error.getMessage());
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        System.out.println("WebSocket Disconnected: " + reason);
        this.webSocket = null;
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }
}
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

    private final StringBuilder messageBuffer = new StringBuilder();

    public interface NotificationListener {
        void onInviteReceived(String fromUser, String language);

        // ⭐ Friend Match စတင်ချိန်တွင် opponentId နှင့် questionsJson ပါဝင်သည်
        void onGameStart(String roomId, String language, String opponentId, String questionsJson);

        void onQuickMatchStart(String roomId, String language, String opponentId, String questionsJson);
        void onQuickMatchTimeout();

        // Game Sync အတွက် ထပ်တိုး Listener များ
        default void onOpponentProgress(String userId, int progress) {}
        default void onGameOver(String winnerId) {}
    }

    public static WebSocketService getInstance() {
        if (instance == null) {
            instance = new WebSocketService();
        }
        return instance;
    }

    public void setNotificationListener(NotificationListener listener) {
        this.notificationListener = listener;
    }

    public void connect(String serverUrl, String currentUserId, NotificationListener listener) {
        connect(serverUrl, currentUserId, listener, null);
    }

    public void connect(String serverUrl, String currentUserId, NotificationListener listener, Runnable onConnected) {
        this.notificationListener = listener;

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

    // ⭐ Invite တုံ့ပြန်ရာတွင် Language ပါဝင်သော မက်သဒ်
    public void respondInvite(String toUser, String action, String language) {
        JSONObject json = new JSONObject();
        json.put("type", "INVITE_RESPONSE");
        json.put("toUser", toUser);
        json.put("action", action);
        json.put("language", language);
        sendText(json.toString());
    }

    public void respondInvite(String toUser, String action) {
        respondInvite(toUser, action, "java");
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

    // Progress ပို့မည့် Method
    public void sendProgressUpdate(String roomId, String userId, int progress) {
        JSONObject json = new JSONObject();
        json.put("type", "GAME_PROGRESS");
        json.put("roomId", roomId);
        json.put("userId", userId);
        json.put("progress", progress);
        sendText(json.toString());
    }

    // Game Over / Match ပြီးဆုံးမှု ပို့မည့် Method
    public void sendGameOver(String roomId, String winnerId) {
        JSONObject json = new JSONObject();
        json.put("type", "GAME_OVER");
        json.put("roomId", roomId);
        json.put("winnerId", winnerId);
        sendText(json.toString());
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        messageBuffer.append(data);

        if (last) {
            String fullMessage = messageBuffer.toString();
            messageBuffer.setLength(0);
            System.out.println("Received from Server: " + fullMessage);

            try {
                JSONObject json = new JSONObject(fullMessage);
                String type = json.optString("type");

                Platform.runLater(() -> {
                    if (notificationListener == null) return;

                    if ("INVITE_RECEIVED".equals(type)) {
                        String fromUser = json.optString("fromUser", "");
                        String language = json.optString("language", "java");
                        notificationListener.onInviteReceived(fromUser, language);

                    } else if ("START_GAME".equals(type)) {
                        String roomId = json.optString("roomId", "");
                        String language = json.optString("language", "java");
                        String opponentId = json.optString("opponentId", "Unknown");
                        Object questionsObj = json.opt("questions");
                        String questionsJson = questionsObj != null ? questionsObj.toString() : "";

                        notificationListener.onGameStart(roomId, language, opponentId, questionsJson);

                    } else if ("QUICK_MATCH_START".equals(type)) {
                        String roomId = json.optString("roomId", "");
                        String language = json.optString("language", "java");
                        String opponentId = json.optString("opponentId", "Unknown");
                        Object questionsObj = json.opt("questions");
                        String questionsJson = questionsObj != null ? questionsObj.toString() : "";

                        notificationListener.onQuickMatchStart(roomId, language, opponentId, questionsJson);

                    } else if ("QUICK_MATCH_TIMEOUT".equals(type)) {
                        System.out.println("Executing Client Timeout Listener...");
                        notificationListener.onQuickMatchTimeout();

                    } else if ("GAME_PROGRESS".equals(type)) {
                        String userId = json.optString("userId", "");
                        int progress = json.optInt("progress", 0);
                        notificationListener.onOpponentProgress(userId, progress);

                    } else if ("GAME_OVER".equals(type)) {
                        String winnerId = json.optString("winnerId", "");
                        notificationListener.onGameOver(winnerId);
                    }
                });

            } catch (Exception e) {
                System.err.println("JSON Parsing Error in Client: " + e.getMessage() + " | Raw: " + fullMessage);
            }
        }

        webSocket.request(1);
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

    @Override
    public void onOpen(WebSocket webSocket) {
        System.out.println("WebSocket Connection Opened Successfully.");
        webSocket.request(1);
        WebSocket.Listener.super.onOpen(webSocket);
    }
}
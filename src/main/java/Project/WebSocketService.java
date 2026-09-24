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
    }

    public static WebSocketService getInstance() {
        if (instance == null) {
            instance = new WebSocketService();
        }
        return instance;
    }

    // 1. Server သို့ Connection စတင်ချိတ်ဆက်ခြင်း
    public void connect(String serverUrl, String currentUserId, NotificationListener listener) {
        this.notificationListener = listener;
        HttpClient client = HttpClient.newHttpClient();
        client.newWebSocketBuilder()
                .buildAsync(URI.create(serverUrl), this)
                .thenAccept(ws -> {
                    this.webSocket = ws;
                    // Connect ရသည်နှင့် မိမိ UserId ကို Server သို့ မှတ်ပုံတင်ပါ
                    JSONObject reg = new JSONObject();
                    reg.put("type", "REGISTER");
                    reg.put("userId", currentUserId);
                    sendText(reg.toString());
                });
    }

    // 2. Text Message ပို့ရန် အခြေခံ Method
    public void sendText(String text) {
        if (webSocket != null) {
            webSocket.sendText(text, true);
        }
    }

    // 3. Invite ပို့ရန် Helper Method
    public void sendInvite(String fromUser, String toUser, String language) {
        JSONObject json = new JSONObject();
        json.put("type", "INVITE_SEND");
        json.put("fromUser", fromUser);
        json.put("toUser", toUser);
        json.put("language", language);
        sendText(json.toString());
    }

    // 4. Invite လက်ခံ/ငြင်းပယ်ရန် Helper Method (action = "ACCEPT" သို့မဟုတ် "REJECT")
    public void respondInvite(String toUser, String action) {
        JSONObject json = new JSONObject();
        json.put("type", "INVITE_RESPONSE");
        json.put("toUser", toUser);
        json.put("action", action);
        sendText(json.toString());
    }

    // 5. Server ထံမှ Message ရောက်ရှိလာပါက လက်ခံသည့် Method
    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        String message = data.toString();
        JSONObject json = new JSONObject(message);
        String type = json.optString("type");

        // JavaFX UI Thread ပေါ်တွင် အလုပ်လုပ်စေရန် Platform.runLater သုံးရပါမည်
        Platform.runLater(() -> {
            if (notificationListener == null) return;

            if ("INVITE_RECEIVED".equals(type)) {
                String fromUser = json.getString("fromUser");
                String language = json.optString("language", "C++");
                notificationListener.onInviteReceived(fromUser, language);
            } else if ("START_GAME".equals(type)) {
                String roomId = json.getString("roomId");
                String language = json.optString("language", "C++");
                notificationListener.onGameStart(roomId, language);
            }
        });

        return WebSocket.Listener.super.onText(webSocket, data, last);
    }
}
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
        // 📌 [သစ်ထည့်ထားသည်] Quick Match အတွက် Listener Callback
        void onQuickMatchStart(String roomId, String language, String questionsJson);
    }

    public static WebSocketService getInstance() {
        if (instance == null) {
            instance = new WebSocketService();
        }
        return instance;
    }

    // 1. Server သို့ Connection စတင်ချိတ်ဆက်ခြင်း (Duplicate Connection ကာကွယ်ထားပါသည်)
    public void connect(String serverUrl, String currentUserId, NotificationListener listener) {
        this.notificationListener = listener;

        // Connection ရှိပြီးသား ဖြစ်နေပါက အသစ် ထပ်မချိတ်ဘဲ REGISTER သာ ပြန်လုပ်ပေးမည်
        if (webSocket != null && !webSocket.isInputClosed()) {
            System.out.println("WebSocket connection already active. Re-registering user: " + currentUserId);
            registerUser(currentUserId);
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        client.newWebSocketBuilder()
                .buildAsync(URI.create(serverUrl), this)
                .thenAccept(ws -> {
                    this.webSocket = ws;
                    System.out.println("Connected to Render WebSocket Server!");
                    registerUser(currentUserId);
                })
                .exceptionally(ex -> {
                    System.err.println("WebSocket Connection Failed: " + ex.getMessage());
                    return null;
                });
    }

    // Helper Method: User ကို Server တွင် မှတ်ပုံတင်ခြင်း
    private void registerUser(String currentUserId) {
        JSONObject reg = new JSONObject();
        reg.put("type", "REGISTER");
        reg.put("userId", currentUserId);
        sendText(reg.toString());
    }

    // 2. Text Message ပို့ရန် အခြေခံ Method
    public void sendText(String text) {
        if (webSocket != null && !webSocket.isOutputClosed()) {
            webSocket.sendText(text, true);
        } else {
            System.err.println("Cannot send message. WebSocket is not connected.");
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

    // 4. Invite လက်ခံ/ငင်းပယ်ရန် Helper Method (action = "ACCEPT" သို့မဟုတ် "REJECT")
    public void respondInvite(String toUser, String action) {
        JSONObject json = new JSONObject();
        json.put("type", "INVITE_RESPONSE");
        json.put("toUser", toUser);
        json.put("action", action);
        sendText(json.toString());
    }

    // 📌 [သစ်ထည့်ထားသည်] Quick Match Queue ထဲ ဝင်ရောက်ရန် Signal ပို့ပေးသည့် Method
    public void joinQuickMatch(String currentUserId, String language) {
        JSONObject json = new JSONObject();
        json.put("type", "QUICK_MATCH_JOIN");
        json.put("userId", currentUserId);
        json.put("language", language);
        sendText(json.toString());
    }

    // 📌 [သစ်ထည့်ထားသည်] Quick Match စောင့်ဆိုင်းမှုကို ပယ်ဖျက်သည့် Method
    public void cancelQuickMatch(String currentUserId) {
        JSONObject json = new JSONObject();
        json.put("type", "QUICK_MATCH_CANCEL");
        json.put("userId", currentUserId);
        sendText(json.toString());
    }

    // 5. Server ထံမှ Message ရောက်ရှိလာပါက လက်ခံသည့် Method
    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        String message = data.toString();
        System.out.println("Received from Server: " + message);

        JSONObject json = new JSONObject(message);
        String type = json.optString("type");

        // JavaFX UI Thread ပေါ်တွင် အလုပ်လုပ်စေရန် Platform.runLater သုံးရပါမည်
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
                // 📌 [သစ်ထည့်ထားသည်] Server မှ Quick Match စတင်သည့်အခါ GameView သို့ ပို့ပေးရန် Data ထုတ်ယူခြင်း
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
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }
}
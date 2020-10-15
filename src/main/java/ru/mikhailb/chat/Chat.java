package ru.mikhailb.chat;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.mikhailb.settings.Settings;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

@Getter
@Setter
public class Chat {
    private int chatId;
    private User client;
    private User operator;

    private int messageSent = 0;
    private int messageReceived = 0;

    private CountDownLatch latch;

    private WebSocket clientWs;
    private WebSocket operatorWs;

    private Map<String, MessageLog> messageLog = new HashMap<>();

    public Chat(int chatId) {
        this.chatId = chatId;
    }

    @Override
    public String toString() {
        return String.format("id: %d, client: %s, operator: %s, messageReceived: %d", chatId,
                client != null ? client.getId() : "",
                operator != null ? operator.getId() : "",
                messageReceived);
    }

    private WebSocket createWs(String token, int userId) {

        WebSocketFactory factory = new WebSocketFactory();
        WebSocket ws = null;

        try {

            ws = factory.createSocket(String.format("%s/?token=%s", Settings.getInstance().WEB_SOCKET_URL(), token));

            ws.addListener(new WebSocketAdapter() {

                @Override
                public void onTextMessage(WebSocket websocket, String text) throws Exception {
                    processIncomingMessage(text, userId);
                }

                @Override
                public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                    System.out.printf("socket message: %s \n", cause.getMessage());
                }


                @Override
                public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                    System.out.printf("connected: %s \n", headers.toString());
                }
            });

            ws.connect();

        } catch (IOException | WebSocketException e) {
            e.printStackTrace();
        }

        return ws;
    }

    private void processIncomingMessage(String message, int userId) {
        try {

            System.out.printf("Message received %s \n", message);

            JSONObject msg = new JSONObject(message);

            if (msg.getString("type").equals("message")) {

                JSONObject msgJson = msg
                        .getJSONObject("data")
                        .getJSONArray("messages")
                        .getJSONObject(0);

                int senderUserId = msgJson.getInt("userId");
                int messageId = msgJson.getInt("id");
                String clientMessageId = msgJson.getString("clientMessageId");

                System.out.printf("Received [%s][%d] sender: %d, message id: %d\n  %s \n", Thread.currentThread().getId(), userId, senderUserId, messageId, message);

                if (userId != senderUserId) {

                    latch.countDown();

                    messageReceived++;

                    MessageLog msgLog = messageLog.get(clientMessageId);
                    msgLog.setRecipientUserId(userId);
                    msgLog.setReceivedTimeMillis(System.currentTimeMillis());
                    msgLog.setReceivedTreadId(Thread.currentThread().getId());

                    sendMessageRead(userId, messageId);
                }

            }


        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private User getUserById(int userId) throws Exception {

        if (client.getId() == userId)
            return client;
        else if (operator.getId() == userId) {
            return operator;
        }
        else
            throw new Exception(String.format("No user with id = %d within the chat", userId));

    }

    private WebSocket getSocketByUserId(int userId) throws Exception {
        if (client.getId() == userId)
            return clientWs;
        else if (operator.getId() == userId) {
            return operatorWs;
        }
        else
            throw new Exception(String.format("No user with id = %d within the chat", userId));
    }

    public void sendMessage(String senderRole) {

        String messageId = UUID.randomUUID().toString();
        User sender;
        WebSocket senderSocket;

        if ("operator".equals(senderRole)) {
            sender = getOperator();
            senderSocket = getOperatorWs();
        }
        else {
            sender = getClient();
            senderSocket = getClientWs();
        }

        JSONObject j = new JSONObject();
        j.put("user_id", sender.getId());
        j.put("type", "message");
        JSONObject data = new JSONObject();
        JSONArray messages = new JSONArray();
        data.put("messages", messages);
        JSONObject message = new JSONObject();
        messages.put(message);
        message.put("clientMessageId", messageId);
        message.put("chatId", getChatId());
        message.put("type", "message");
        message.put("text", "привет");
        j.put("data", data);
        senderSocket.sendText(j.toString());

        messageLog.put(messageId, new MessageLog(System.currentTimeMillis(), "recd", sender.getId(), Thread.currentThread().getId()));
    }

    private void sendMessageRead(int recipientUserId, int messageId) {

        try {
            User recipient = getUserById(recipientUserId);
            WebSocket recipientWs = getSocketByUserId(recipientUserId);

            JSONObject j = new JSONObject();
            j.put("user_id", recipient.getId());
            j.put("type", "messageStatus");
            JSONObject data = new JSONObject();
            data.put("messageId", messageId);
            data.put("status", "read");
            data.put("chatId", getChatId());
            j.put("data", data);
            recipientWs.sendText(j.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void openWebSockets() {
        clientWs = createWs(client.getToken(), client.getId());
        operatorWs = createWs(operator.getToken(), operator.getId());
    }

    public void closeWebSockets() {
        clientWs.clearListeners();
        clientWs.sendClose();
        clientWs.disconnect();

        operatorWs.clearListeners();
        operatorWs.sendClose();
        operatorWs.disconnect();
    }
}

package ru.mikhailb.sdk;

import org.json.JSONObject;
import ru.mikhailb.chat.Chat;
import ru.mikhailb.chat.User;
import ru.mikhailb.nats.Nats;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SdkFacade {

    private static void handleError() {
        //{"error":{"message":"Ошибка запроса","code":1201}}
    }

    /**
     * @param numberOfClients - how many client is supposed to be generated
     * @return list of generated clients
     *
    **/
    public static List<User> generateClients(int numberOfClients) {


        String loginBase = UUID.randomUUID().toString();

        List<String> userRequests = IntStream
                .range(0, numberOfClients)
                .mapToObj(it -> {
                    JSONObject j = new JSONObject();
                    j.put("method", "POST");
                    j.put("path", "/user/create");
                    JSONObject body = new JSONObject();
                    body.put("login", String.format("%s_%d", loginBase, it));
                    body.put("pass", "user");
                    body.put("type", "medzdrav");
                    body.put("first_name", "User");
                    body.put("last_name", "User");
                    body.put("middle_name", "User");
                    j.put("body", body);
                    return j.toString();
                })
                .collect(Collectors.toList());

        return Nats
                .requestAll("users.1.0", userRequests)
                .stream()
                .map(json -> (new JSONObject(json)).getJSONObject("data"))
                .map(data -> new User(data.getInt("id"),
                        data.getJSONObject("access_token").getString("token"),
                        data.getJSONArray("roles").getString(0)))
                .collect(Collectors.toList());
    }

    public static List<User> generateOperators(int numberOfOperators) {

        String loginBase = UUID.randomUUID().toString();

        List<String> userRequests = IntStream
                .range(0, numberOfOperators)
                .mapToObj(it -> {
                    JSONObject j = new JSONObject();
                    j.put("method", "POST");
                    j.put("path", "/user/create");
                    JSONObject body = new JSONObject();
                    body.put("login", String.format("%s_%d", loginBase, it));
                    body.put("pass", "user");
                    body.put("type", "medzdrav");
                    body.put("first_name", "User");
                    body.put("last_name", "User");
                    body.put("middle_name", "User");
                    j.put("body", body);
                    return j.toString();
                })
                .collect(Collectors.toList());

        List<User> users = Nats
                .requestAll("users.1.0", userRequests)
                .stream()
                .map(json -> (new JSONObject(json)).getJSONObject("data"))
                .map(data -> new User(data.getInt("id"),
                        data.getJSONObject("access_token").getString("token"),
                        "operator"))
                .collect(Collectors.toList());

        List<String> rolesRequests = users
                .stream()
                .map(user -> {
                    JSONObject j = new JSONObject();
                    j.put("method", "POST");
                    j.put("path", "/role/create-or-update");
                    JSONObject body = new JSONObject();
                    body.put("user_id", user.getId());
                    JSONObject data = new JSONObject();
                    data.put("role", "operator");
                    body.put("data", data);
                    j.put("body", body);
                    return j.toString();
                })
                .collect(Collectors.toList());

        Nats.requestAll("users.1.0", rolesRequests);

        return users;

    }

    public static List<Chat> generateChatsAndSubscribe(List<User> clients, List<User> operators) {

        if (clients.size() != operators.size())
            throw new IllegalArgumentException("Number of clients and operators must be the same");

        List<String> chatRequests = clients
                .stream()
                .map(it -> {
                    JSONObject j = new JSONObject();
                    j.put("method", "POST");
                    j.put("path", "/chats/new");
                    JSONObject body = new JSONObject();
                    body.put("order_id", 1);
                    j.put("body", body);
                    return j.toString();
                })
                .collect(Collectors.toList());

        List<Chat> chats = Nats
                .requestAll("chats.1.0", chatRequests)
                .stream()
                .map(json -> (new JSONObject(json)).getJSONObject("data"))
                .map(data -> new Chat(data.getInt("chat_id")))
                .collect(Collectors.toList());

        // subscribe operators
        ListIterator<User> opIterator = operators.listIterator();
        ListIterator<User> clientIterator = clients.listIterator();
        List<String> subscribeRequests = new ArrayList<>();

        for(Chat chat: chats) {
            User client = clientIterator.next();
            JSONObject clientRq = new JSONObject();
            clientRq.put("method", "POST");
            clientRq.put("path", "/chats/user/subscribe");
            JSONObject clientBody = new JSONObject();
            clientBody.put("user_id", client.getId());
            clientBody.put("user_type", "client");
            clientBody.put("chat_id", chat.getChatId());
            clientRq.put("body", clientBody);
            subscribeRequests.add(clientRq.toString());

            chat.setClient(client);

            User operator = opIterator.next();
            JSONObject opRq = new JSONObject();
            opRq.put("method", "POST");
            opRq.put("path", "/chats/user/subscribe");
            JSONObject opBody = new JSONObject();
            opBody.put("user_id", operator.getId());
            opBody.put("user_type", operator.getRole());
            opBody.put("chat_id", chat.getChatId());
            opRq.put("body", opBody);
            subscribeRequests.add(opRq.toString());

            chat.setOperator(operator);
        }

        Nats.requestAll("chats.1.0", subscribeRequests);

        return chats;

    }

    public static void test() {

        String loginBase = UUID.randomUUID().toString();

        JSONObject j = new JSONObject();
        j.put("method", "POST");
        j.put("path", "/user/create");
        JSONObject body = new JSONObject();
        body.put("login", String.format("%s_%d", loginBase, 1));
        body.put("pass", "user");
        body.put("type", "medzdrav");
        body.put("first_name", "User");
        body.put("last_name", "User");
        body.put("middle_name", "User");
        j.put("body", body);

        String response =  Nats.request("users.1.0", j.toString());
        System.out.println(response);

    }




}

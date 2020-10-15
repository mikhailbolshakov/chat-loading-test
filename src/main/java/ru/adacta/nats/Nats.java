package ru.adacta.nats;

import io.nats.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.adacta.settings.Settings;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Nats {

    private static final Logger logger = LoggerFactory.getLogger(Nats.class);

    public static Connection connect() throws Exception {

        Options.Builder builder = new Options.Builder().
                server(Settings.getInstance().NATS_URL()).
                token(Settings.getInstance().NATS_TOKEN().toCharArray()).
                connectionTimeout(Duration.ofSeconds(5000)).
                pingInterval(Duration.ofSeconds(10)).
                reconnectWait(Duration.ofSeconds(1000)).
                noReconnect().
                errorListener(new ErrorListener() {
                    public void exceptionOccurred(Connection conn, Exception exp) {
                        logger.error("Exception:" + exp.getMessage());
                    }

                    public void errorOccurred(Connection conn, String type) {
                        logger.error("Error:" + type);
                    }

                    public void slowConsumerDetected(Connection conn, Consumer consumer) {
                        logger.error("Slow consumer:" + consumer.getClass().getName());
                    }
                }).
                connectionListener(new ConnectionListener() {
                    public void connectionEvent(Connection conn, Events type) {
                        logger.info("Status changed:" + type.toString());
                    }
                });

        return io.nats.client.Nats.connect(builder.build());
    }

    public static List<String> requestAll(String topic, List<String> requests) {

        List<String> responses = new ArrayList<>();

        ExecutorService executorService = Executors.newFixedThreadPool(4);

        try {
            try (Connection natsConnection = connect()) {

                List<Callable<String>> callables = requests.stream()
                        .map(rq -> new Callable<String>() {
                            @Override
                            public String call() throws Exception {
                                return request(natsConnection, topic, rq);
                            }
                        })
                        .collect(Collectors.toList());

                executorService
                        .invokeAll(callables)
                        .forEach(it -> {

                            try {
                                responses.add(it.get());
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }

                        });
            }

        } catch (Exception e) {
            logger.error(String.format("Error: %s", e.getMessage()), e);
        } finally {
            executorService.shutdown();
        }

        return responses;
    }

    public static String request(String subject, String message) {

        String response = null;

        try {
            try (Connection c = connect()) {
                response = request(c, subject, message);
            }
        } catch (Exception e) {
            logger.error(String.format("Error: %s", e.getMessage()), e);
        }

        return response;
    }

    public static String request(Connection c, String subject, String message) {

        String result = "";

        try {

            logger.info(String.format("request: topic: %s \n, message: %s \n", subject, message));

            Message msg = c.request(subject, message.getBytes(StandardCharsets.UTF_8), Duration.ofSeconds(10));
            result = new String(msg.getData(), StandardCharsets.UTF_8);
            logger.info(String.format("response: %s \n", result));

        } catch (Exception e) {
            logger.error(String.format("Error: %s", e.getMessage()), e);
        }

        return result;
    }

}
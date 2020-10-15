package ru.adacta.benchmark.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.adacta.benchmark.Benchmark;
import ru.adacta.benchmark.BenchmarkParams;
import ru.adacta.benchmark.BenchmarkResult;
import ru.adacta.chat.Chat;
import ru.adacta.chat.MessageLog;
import ru.adacta.chat.User;
import ru.adacta.sdk.SdkFacade;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class ClientOperatorChatBenchmark implements Benchmark {

    private static Logger logger = LoggerFactory.getLogger(ClientOperatorChatBenchmark.class);

    private BenchmarkParams params;
    private List<User> clients = null;
    private List<User> operators = null;
    private List<Chat> chats = null;
    private final Map<String, MessageLog> messageLog = new ConcurrentHashMap<>();
    private CountDownLatch latch;

    @Override
    public void prepare(BenchmarkParams params) {

        this.params = params;

        messageLog.clear();

        clients = SdkFacade.generateClients(this.params.getChatsNumber());
        operators = SdkFacade.generateOperators(this.params.getChatsNumber());

        chats = SdkFacade.generateChatsAndSubscribe(clients, operators);

        latch = new CountDownLatch(this.params.getMessageExchangeCyclesNumber() * chats.size() * 2 - 1);

        chats.forEach(c -> {
            c.setMessageLog(messageLog);
            c.setLatch(latch);
            c.openWebSockets();
        });

    }

    @Override
    public BenchmarkResult run() {

       ExecutorService executorService = Executors.newFixedThreadPool(10);

        try {

            chats
                    .stream()
                    .map(c -> new Runnable() {
                        @Override
                        public void run() {
                            IntStream.range(0, params.getMessageExchangeCyclesNumber())
                                    .forEach(a -> {

                                        try {
                                            c.sendMessage("client");
                                            Thread.sleep(params.getMessageIntervalMillis());
                                            c.sendMessage("operator");
                                            Thread.sleep(params.getMessageIntervalMillis());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    });
                        }
                    })
                    .forEach(executorService::submit);


            if (!latch.await(params.getLatchTimeout(), TimeUnit.SECONDS)) {
                logger.debug("latch timeout");
            }

            return new BenchmarkResult(params, messageLog);

        } catch(Exception e) {
            logger.error(String.format("Socket error: %s \n", e.getMessage()), e);
            return new BenchmarkResult(params, messageLog, e);
        } finally {
            executorService.shutdown();
            //executorService.awaitTermination();
        }

    }

    @Override
    public void finalization() {
        chats.forEach(Chat::closeWebSockets);
    }
}

package ru.mikhailb.benchmark.impl;

import io.bretty.console.table.Alignment;
import io.bretty.console.table.ColumnFormatter;
import io.bretty.console.table.Precision;
import io.bretty.console.table.Table;
import lombok.AllArgsConstructor;
import ru.mikhailb.benchmark.BenchmarkAnalyzer;
import ru.mikhailb.benchmark.BenchmarkResult;
import ru.mikhailb.chat.MessageLog;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BenchmarkAnalyzerImpl implements BenchmarkAnalyzer {

    @AllArgsConstructor
    private class BenchmarkAnalyze {
        public long sent;
        public long received;
        public long lost;
        public double messagePerSec;
        public double avgTime;
    }

    private BenchmarkAnalyze analyzeOne(BenchmarkResult result) {

        Map<String, MessageLog> messageLog = result.getMessages();

        int numberOfMessages = messageLog.size();
        long receivedMessages = messageLog.values().stream().filter(m -> m.getReceivedTimeMillis() > 0).count();

        // average time between sending and receiving
        double time = 0.0;
        long startTime = Long.MAX_VALUE;
        long finishTime = 0;
        for(Map.Entry<String, MessageLog> item: messageLog.entrySet()) {
            long received = item.getValue().getReceivedTimeMillis();
            long send = item.getValue().getSendTimeMillis();
            time += received > 0 ? (received - send) : 0;
            startTime = Math.min(startTime, send);
            finishTime = Math.max(finishTime, received);
        }

        double totalSec = (double)(finishTime - startTime) / 1000.0;
        long lost = numberOfMessages - receivedMessages;
        double messPerSec = receivedMessages / totalSec;
        double avgTime = (time / receivedMessages) / 1000.0;
        long sent = messageLog.entrySet().stream().filter(a -> a.getValue().getSendTimeMillis() > 0).count();
        long received = messageLog.entrySet().stream().filter(a -> a.getValue().getReceivedTimeMillis() > 0).count();

        //System.out.printf("Average sent-received time: %f (s) \n", (time / receivedMessages) / 1000.0 );
        //System.out.printf("Start time: %d, finish time: %d, lapse: %f (s), messages: %d, received: %d \n", startTime, finishTime, (float) (finishTime - startTime) / 1000, numberOfMessages, receivedMessages);
        //System.out.printf("Messages per seconds: %f \n", receivedMessages / totalSec);
        //System.out.printf("Lost: %d \n", numberOfMessages - receivedMessages);

        return new BenchmarkAnalyze(sent, received, lost, messPerSec, avgTime);

/*
        try {
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter("chart.csv"));

                for(Map.Entry<String, MessageLog> item: messageLog.entrySet()) {
                    long received = item.getValue().getReceivedTimeMillis();
                    long send = item.getValue().getSendTimeMillis();
                    time = (received - send);
                    writer.write(String.valueOf(time) + "\n");
                }

            } finally {
                Objects.requireNonNull(writer).close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void analyze(List<BenchmarkResult> benchmarkResults) {

        String[] header = {"Benchmark", "chats", "cycles", "sent", "received", "lost", "mess/sec", "avg time"};
        String[][] data = new String[benchmarkResults.size()][8];

        for(int i = 0; i < benchmarkResults.size(); i++) {
            BenchmarkResult res = benchmarkResults.get(i);
            BenchmarkAnalyze analyze = analyzeOne(res);

            data[i][0] = res.getInputParams().getName();
            data[i][1] = String.valueOf(res.getInputParams().getChatsNumber());
            data[i][2] = String.valueOf(res.getInputParams().getMessageExchangeCyclesNumber());
            data[i][3] = String.valueOf(analyze.sent);
            data[i][4] = String.valueOf(analyze.received);
            data[i][5] = String.valueOf(analyze.lost);
            data[i][6] = String.format("%.2f", analyze.messagePerSec);
            data[i][7] = String.format("%.2f", analyze.avgTime);

        }

        ColumnFormatter<String> cf = ColumnFormatter.text(Alignment.RIGHT, 15);
        Table table = Table.of(header, data, cf);

        System.out.println(table);

    }
}

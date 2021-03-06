package ru.adacta.benchmark.impl;

import io.bretty.console.table.Alignment;
import io.bretty.console.table.ColumnFormatter;
import io.bretty.console.table.Table;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.adacta.benchmark.BenchmarkReporter;
import ru.adacta.benchmark.BenchmarkResult;
import ru.adacta.chat.MessageLog;

import java.util.List;
import java.util.Map;

public class BenchmarkReporterImpl implements BenchmarkReporter {

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkReporterImpl.class);

    @AllArgsConstructor
    private class BenchmarkReportData {
        public long sent;
        public long received;
        public long lost;
        public double messagePerSec;
        public double avgTime;
    }

    private BenchmarkReportData reportOne(BenchmarkResult result) {

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

        return new BenchmarkReportData(sent, received, lost, messPerSec, avgTime);

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
    public void report(List<BenchmarkResult> benchmarkResults) {

        String[] header = {"Benchmark", "chats", "cycles", "sent", "received", "lost", "mess/sec", "avg time"};
        String[][] data = new String[benchmarkResults.size()][8];

        for(int i = 0; i < benchmarkResults.size(); i++) {
            BenchmarkResult res = benchmarkResults.get(i);
            BenchmarkReportData analyze = reportOne(res);

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

        logger.info(String.format("%s\n", table));

    }
}

package ru.adacta.benchmark;

import ru.adacta.benchmark.impl.BenchmarkReporterImpl;
import ru.adacta.benchmark.impl.ClientOperatorChatBenchmark;

import java.util.List;
import java.util.stream.Collectors;

public class Runner {

    public static void Run(List<BenchmarkParams> benchmarks) {

        List<BenchmarkResult> results = benchmarks
                .stream()
                .map(Runner::RunOne)
                .filter(r -> r.getException().isEmpty())
                .collect(Collectors.toList());

        new BenchmarkReporterImpl().report(results);


    }

    public static void Run(String benchmarkFilename) {
        // TODO: load from JSON file
    }

    private static BenchmarkResult RunOne(BenchmarkParams params) {

        Benchmark bm = new ClientOperatorChatBenchmark();

        try {

            bm.prepare(params);

            return bm.run();

        } catch (Exception e) {
            e.printStackTrace();
            return new BenchmarkResult(null, null, e);
        } finally {
            bm.finalization();
        }
    }

}

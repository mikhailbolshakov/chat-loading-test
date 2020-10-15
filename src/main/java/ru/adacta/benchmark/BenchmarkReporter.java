package ru.adacta.benchmark;

import java.util.List;

public interface BenchmarkReporter {

    void report(List<BenchmarkResult> benchmarkResults);

}

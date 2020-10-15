package ru.adacta.benchmark;

public interface Benchmark {

    /**
     * some preparations have to be executed here
     * preparations aren't included in time estimation
     */
    void prepare(BenchmarkParams params);

    /**
     * run benchmarks
     */
    BenchmarkResult run();

    /**
     * some actions needs to be done afterwards
     */
    void finalization();

}

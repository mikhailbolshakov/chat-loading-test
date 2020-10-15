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
     * create analytic data based on benchmark results
     */
    void analytics();

    /**
     * some actions needs to be done afterwards
     */
    void finish();

}

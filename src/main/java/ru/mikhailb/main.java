package ru.mikhailb;

import ru.mikhailb.benchmark.BenchmarkParams;
import ru.mikhailb.benchmark.Runner;
import ru.mikhailb.settings.Settings;

import java.util.List;

public class main {

    public static void main(String[] args) throws Exception {

        Settings settings = Settings.getInstance();

        List<BenchmarkParams> params = List.of(
                new BenchmarkParams(
                        "Benchmark 1",
                        5,
                        10,
                        5,
                        30),
                new BenchmarkParams(
                        "Benchmark 2",
                        10,
                        10,
                        5,
                        30),
                new BenchmarkParams(
                        "Benchmark 3",
                        15,
                        10,
                        5,
                        30),
                new BenchmarkParams(
                        "Benchmark 4",
                        15,
                        15,
                        5,
                        30),
                new BenchmarkParams(
                        "Benchmark 5",
                        20,
                        15,
                        5,
                        30),
                new BenchmarkParams(
                        "Benchmark 6",
                        25,
                        10,
                        5,
                        30)
        );

        Runner.Run(params);


    }

}

package ru.adacta;

import ru.adacta.benchmark.BenchmarkFileLoader;
import ru.adacta.benchmark.BenchmarkParams;
import ru.adacta.benchmark.Runner;
import ru.adacta.settings.Settings;

import java.util.List;

public class main {

    public static void main(String[] args) throws Exception {

        Settings settings = Settings.getInstance();
        Runner.Run(BenchmarkFileLoader.load(settings.BENCHMARKS_FILENAME()));

    }

}

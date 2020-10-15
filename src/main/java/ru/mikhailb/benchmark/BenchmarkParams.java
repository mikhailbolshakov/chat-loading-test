package ru.mikhailb.benchmark;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BenchmarkParams {

    private String name;
    private int chatsNumber;
    private int messageExchangeCyclesNumber;
    private int messageIntervalMillis;
    private int latchTimeout;

}

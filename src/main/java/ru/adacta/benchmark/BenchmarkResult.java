package ru.adacta.benchmark;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.adacta.chat.MessageLog;

import java.util.Map;
import java.util.Optional;

@Getter
public class BenchmarkResult {

    public BenchmarkResult(BenchmarkParams inputParams, Map<String, MessageLog> messages) {
        this.inputParams = inputParams;
        this.messages = messages;
        this.exception = Optional.empty();
    }

    public BenchmarkResult(BenchmarkParams inputParams, Map<String, MessageLog> messages, Exception exception) {
        this.inputParams = inputParams;
        this.messages = messages;
        this.exception = Optional.of(exception);
    }

    private final BenchmarkParams inputParams;
    private final Map<String, MessageLog> messages;
    private Optional<Exception> exception = Optional.empty();

}

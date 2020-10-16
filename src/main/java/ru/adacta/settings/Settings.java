package ru.adacta.settings;

import io.github.cdimascio.dotenv.Dotenv;

public class Settings {

    private static Settings instance;
    private Dotenv dotenv;

    private Settings() {
    }

    public static Settings getInstance() {
        if (instance == null) {
            synchronized (Settings.class) {
                if (instance == null) {
                    instance = new Settings();
                    instance.dotenv = Dotenv
                            .configure()
                            .ignoreIfMissing()
                            .load();
                }
            }
        }
        return instance;
    }

    public String NATS_URL() {
        return dotenv.get("NATS_URL", "nats://localhost:4222");
    }

    public String NATS_TOKEN() {
        return dotenv.get("NATS_TOKEN", "BusToken");
    }

    public String WEB_SOCKET_URL() {
        return dotenv.get("WEB_SOCKET_URL", "ws://localhost:8000/ws");
    }

    public String BENCHMARKS_FILENAME() {
        return dotenv.get("BENCHMARKS_FILENAME", "./benchmarks/benchmarks.json");
    }

    public String TEST_ORDER_ID() {
        return dotenv.get("TEST_ORDER_ID", "0");
    }


}

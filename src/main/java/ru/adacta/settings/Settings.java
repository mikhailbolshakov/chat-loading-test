package ru.adacta.settings;

import io.github.cdimascio.dotenv.Dotenv;

public class Settings {

    private Dotenv dotenv;

    private static Settings instance;

    private Settings() {
    }

    public static Settings getInstance() {
        if (instance == null) {
            synchronized (Settings.class) {
                if (instance == null) {
                    instance = new Settings();
                    instance.dotenv = Dotenv.load();
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




}

package com.rollylindenshnizzer.nexuscore.core.logging;

import com.rollylindenshnizzer.nexuscore.api.NexusLogger;

import java.time.Instant;

public final class ConsoleNexusLogger implements NexusLogger {
    private final String modId;

    public ConsoleNexusLogger(String modId) {
        this.modId = modId;
    }

    @Override
    public void info(String message) {
        log("INFO", message, null);
    }

    @Override
    public void warn(String message) {
        log("WARN", message, null);
    }

    @Override
    public void error(String message) {
        log("ERROR", message, null);
    }

    @Override
    public void error(String message, Throwable error) {
        log("ERROR", message, error);
    }

    @Override
    public void debug(String message) {
        log("DEBUG", message, null);
    }

    private void log(String level, String message, Throwable error) {
        System.out.println("[" + Instant.now() + "] [" + level + "] [" + modId + "] " + message);
        if (error != null) {
            error.printStackTrace(System.out);
        }
    }
}

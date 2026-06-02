package com.rollylindenshnizzer.nexuscore.api;

public interface NexusLogger {
    void info(String message);

    void warn(String message);

    void error(String message);

    void error(String message, Throwable error);

    default void debug(String message) {
        info(message);
    }
}

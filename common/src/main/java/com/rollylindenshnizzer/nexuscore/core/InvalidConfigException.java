package com.rollylindenshnizzer.nexuscore.core;

public final class InvalidConfigException extends NexusException {
    public InvalidConfigException(String key, String reason) {
        super("Invalid config value for '" + key + "': " + reason);
    }
}

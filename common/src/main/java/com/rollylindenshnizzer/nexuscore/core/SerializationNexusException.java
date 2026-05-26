package com.rollylindenshnizzer.nexuscore.core;

public final class SerializationNexusException extends NexusException {
    public SerializationNexusException(String path, String reason) {
        super("Serialization error at '" + path + "': " + reason);
    }
}

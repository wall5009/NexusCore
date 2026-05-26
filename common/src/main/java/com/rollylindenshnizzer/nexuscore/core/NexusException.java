package com.rollylindenshnizzer.nexuscore.core;

public class NexusException extends RuntimeException {
    public NexusException(String message) {
        super(message);
    }

    public NexusException(String message, Throwable cause) {
        super(message, cause);
    }
}

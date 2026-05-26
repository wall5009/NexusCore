package com.rollylindenshnizzer.nexuscore.util;

import java.util.Optional;

public record NexusResult<T>(T value, String error) {
    public static <T> NexusResult<T> ok(T value) {
        return new NexusResult<>(value, null);
    }

    public static <T> NexusResult<T> error(String error) {
        return new NexusResult<>(null, error);
    }

    public boolean success() {
        return error == null;
    }

    public Optional<T> optional() {
        return Optional.ofNullable(value);
    }
}

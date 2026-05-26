package com.rollylindenshnizzer.nexuscore.persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class AttachmentStore {
    private final Map<AttachmentKey<?>, Object> values = new HashMap<>();

    public <T> void put(AttachmentKey<T> key, T value) {
        values.put(key, key.type().cast(value));
    }

    public <T> Optional<T> get(AttachmentKey<T> key) {
        return Optional.ofNullable(values.get(key)).map(key.type()::cast);
    }

    public void copyTo(AttachmentStore target, AttachmentKey.CopyPolicy reason) {
        values.forEach((key, value) -> {
            if (key.copyPolicy() == AttachmentKey.CopyPolicy.ALWAYS || key.copyPolicy() == reason) {
                target.values.put(key, value);
            }
        });
    }
}

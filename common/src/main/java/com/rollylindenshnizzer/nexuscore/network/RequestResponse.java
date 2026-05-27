package com.rollylindenshnizzer.nexuscore.network;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RequestResponse<R> {
    private final Map<UUID, Pending<R>> pending = new ConcurrentHashMap<>();

    public UUID begin(Duration timeout) {
        UUID id = UUID.randomUUID();
        pending.put(id, new Pending<>(System.currentTimeMillis() + timeout.toMillis(), null, null));
        return id;
    }

    public void success(UUID id, R response) {
        pending.computeIfPresent(id, (ignored, pending) -> new Pending<>(pending.deadlineMillis(), response, null));
    }

    public void failure(UUID id, String message) {
        pending.computeIfPresent(id, (ignored, pending) -> new Pending<>(pending.deadlineMillis(), null, message));
    }

    public Optional<Result<R>> poll(UUID id) {
        Pending<R> value = pending.get(id);
        if (value == null) {
            return Optional.empty();
        }
        if (value.response() != null || value.failure() != null) {
            pending.remove(id);
            return Optional.of(new Result<>(value.response(), value.failure(), false));
        }
        if (System.currentTimeMillis() > value.deadlineMillis()) {
            pending.remove(id);
            return Optional.of(new Result<>(null, "Timed out waiting for response " + id, true));
        }
        return Optional.empty();
    }

    private record Pending<R>(long deadlineMillis, R response, String failure) {
    }

    public record Result<R>(R response, String failure, boolean timedOut) {
        public boolean successful() {
            return failure == null && !timedOut;
        }
    }
}

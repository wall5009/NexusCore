package com.rollylindenshnizzer.nexuscore.event;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class EventTrace {
    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static int maxEntries = 256;

    public static void record(String event, String detail) {
        ENTRIES.add(new Entry(Instant.now(), event, detail));
        while (ENTRIES.size() > maxEntries) {
            ENTRIES.removeFirst();
        }
    }

    public static void maxEntries(int value) {
        maxEntries = Math.max(1, value);
    }

    public static List<Entry> entries() {
        return List.copyOf(ENTRIES);
    }

    public record Entry(Instant time, String event, String detail) {
    }

    private EventTrace() {
    }
}

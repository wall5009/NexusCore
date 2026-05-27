package com.rollylindenshnizzer.nexuscore.event;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import com.rollylindenshnizzer.nexuscore.debug.DebugRegistry;

import java.util.LinkedHashMap;
import java.util.Map;

@NexusStable(since = "1.2")
public final class EventDiagnosticRegistry {
    private static final Map<String, Integer> INVOCATIONS = new LinkedHashMap<>();

    static {
        DebugRegistry.section("nexuscore.events", () -> INVOCATIONS.toString());
    }

    public static void mark(String eventName) {
        INVOCATIONS.merge(eventName, 1, Integer::sum);
    }

    public static Map<String, Integer> snapshot() {
        return Map.copyOf(INVOCATIONS);
    }

    private EventDiagnosticRegistry() {
    }
}

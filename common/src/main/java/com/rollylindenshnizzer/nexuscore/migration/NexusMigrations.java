package com.rollylindenshnizzer.nexuscore.migration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NexusMigrations {
    private static final Map<String, MigrationDiagnostics> DIAGNOSTICS = new ConcurrentHashMap<>();

    public static MigrationDiagnostics forMod(String modId) {
        return DIAGNOSTICS.computeIfAbsent(modId, ignored -> new MigrationDiagnostics());
    }

    public static Map<String, MigrationDiagnostics> diagnostics() {
        return Map.copyOf(DIAGNOSTICS);
    }

    private NexusMigrations() {
    }
}

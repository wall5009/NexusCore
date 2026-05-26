package com.rollylindenshnizzer.nexuscore.config;

import java.util.Map;
import java.util.function.Consumer;

public record ConfigMigration(int fromVersion, int toVersion, Consumer<Map<String, Object>> migration) {
    public void apply(Map<String, Object> values) {
        migration.accept(values);
    }
}

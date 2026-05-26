package com.rollylindenshnizzer.nexuscore.persistence;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.UnaryOperator;

public final class DataMigrationChain<T> {
    private final List<Step<T>> steps = new ArrayList<>();

    public DataMigrationChain<T> step(int fromVersion, int toVersion, UnaryOperator<T> migration) {
        steps.add(new Step<>(fromVersion, toVersion, migration));
        steps.sort(Comparator.comparingInt(Step::fromVersion));
        return this;
    }

    public T migrate(int currentVersion, int targetVersion, T value) {
        T result = value;
        int version = currentVersion;
        while (version < targetVersion) {
            int expected = version;
            Step<T> step = steps.stream()
                    .filter(candidate -> candidate.fromVersion() == expected)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Missing migration from version " + expected));
            result = step.migration().apply(result);
            version = step.toVersion();
        }
        return result;
    }

    private record Step<T>(int fromVersion, int toVersion, UnaryOperator<T> migration) {
    }
}

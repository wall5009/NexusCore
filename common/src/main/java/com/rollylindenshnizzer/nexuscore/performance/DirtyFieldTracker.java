package com.rollylindenshnizzer.nexuscore.performance;

import java.util.LinkedHashSet;
import java.util.Set;

public final class DirtyFieldTracker {
    private final Set<String> dirty = new LinkedHashSet<>();

    public void mark(String field) {
        dirty.add(field);
    }

    public boolean isDirty(String field) {
        return dirty.contains(field);
    }

    public Set<String> flush() {
        Set<String> result = Set.copyOf(dirty);
        dirty.clear();
        return result;
    }
}

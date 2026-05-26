package com.rollylindenshnizzer.nexuscore.debug;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class DebugRegistry {
    private static final Map<String, Supplier<String>> SECTIONS = new LinkedHashMap<>();

    public static void section(String id, Supplier<String> supplier) {
        SECTIONS.put(id, supplier);
    }

    public static Map<String, String> snapshot() {
        Map<String, String> result = new LinkedHashMap<>();
        SECTIONS.forEach((key, supplier) -> result.put(key, supplier.get()));
        return result;
    }

    private DebugRegistry() {
    }
}

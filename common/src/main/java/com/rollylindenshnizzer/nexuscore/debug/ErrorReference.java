package com.rollylindenshnizzer.nexuscore.debug;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ErrorReference {
    private static final Map<String, Entry> ENTRIES = new LinkedHashMap<>();

    static {
        register("NC-CRASH-CLIENT-ON-SERVER", "Client class loaded on server",
                "A client-only class was referenced from common/server code.",
                "Move it into a client package and load it only from the client entrypoint.");
        register("NC-DOC-DATAGEN", "Datagen validation issue",
                "Generated assets/data are missing, malformed, duplicated, or incomplete.",
                "Run datagen validation and follow the suggested fix.");
        register("NC-DOC-CONFIG", "Config graph issue",
                "Config dependencies or conflicts are invalid.",
                "Fix missing keys or circular dependencies.");
        register("NC-DOC-NETWORK", "Network diagnostic",
                "A packet/channel registration or protocol mismatch was detected.",
                "Align packet protocol versions and registered packet IDs.");
    }

    public static void register(String code, String title, String cause, String fix) {
        ENTRIES.put(code, new Entry(code, title, cause, fix));
    }

    public static Map<String, Entry> entries() {
        return Map.copyOf(ENTRIES);
    }

    public record Entry(String code, String title, String cause, String fix) {
    }

    private ErrorReference() {
    }
}

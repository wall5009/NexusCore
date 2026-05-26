package com.rollylindenshnizzer.nexuscore.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class CrashHints {
    private static final Map<String, String> HINTS = new LinkedHashMap<>();

    static {
        HINTS.put("Registry Object not present", "A registry supplier was read before registration finished. Move the access into common setup or a supplier callback.");
        HINTS.put("Duplicate registration", "Two entries use the same registry path. Rename one path or split content into separate registries.");
        HINTS.put("Invalid resource location", "Resource IDs must be lowercase and may only contain namespace/path-safe characters.");
        HINTS.put("Tried to access class net.minecraft.client", "Client-only code was loaded from common or server initialization. Move it behind a client entrypoint.");
    }

    public static void register(String needle, String hint) {
        HINTS.put(needle, hint);
    }

    public static Optional<String> explain(Throwable throwable) {
        String text = throwable.toString();
        for (Throwable cause = throwable.getCause(); cause != null; cause = cause.getCause()) {
            text += "\n" + cause;
        }
        for (Map.Entry<String, String> entry : HINTS.entrySet()) {
            if (text.contains(entry.getKey())) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }

    private CrashHints() {
    }
}

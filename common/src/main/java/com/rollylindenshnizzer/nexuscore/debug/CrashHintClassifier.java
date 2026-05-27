package com.rollylindenshnizzer.nexuscore.debug;

import java.util.List;

public final class CrashHintClassifier {
    public static List<Hint> classify(Throwable throwable) {
        String text = throwable.toString() + "\n" + stack(throwable);
        java.util.ArrayList<Hint> hints = new java.util.ArrayList<>();
        if (text.contains("net.minecraft.client") && text.contains("DEDICATED_SERVER")) {
            hints.add(new Hint("NC-CRASH-CLIENT-ON-SERVER", "Client class loaded on server",
                    "Move screen/render/UI classes behind client-only entrypoints or reflective loading."));
        }
        if (text.contains("Registry Object") || text.contains("not present")) {
            hints.add(new Hint("NC-CRASH-REGISTRY-EARLY", "Registry object accessed too early",
                    "Access registry suppliers after registries have been registered."));
        }
        if (text.contains("Codec") && text.contains("field")) {
            hints.add(new Hint("NC-CRASH-CODEC-FIELD", "Missing or invalid codec field",
                    "Check optional/default fields in the component or data codec."));
        }
        if (text.contains("Packet") || text.contains("network")) {
            hints.add(new Hint("NC-CRASH-PACKET", "Packet handling failure",
                    "Check packet thread, protocol version, permissions, distance, and dimension guards."));
        }
        return hints;
    }

    private static String stack(Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            builder.append(element).append('\n');
        }
        return builder.toString();
    }

    public record Hint(String code, String cause, String fix) {
    }

    private CrashHintClassifier() {
    }
}

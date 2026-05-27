package com.rollylindenshnizzer.nexuscore.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ConfigSyncDiagnostics {
    public static SyncReport compare(NexusConfig client, NexusConfig server, String version) {
        List<String> mismatches = new ArrayList<>();
        if (!Objects.equals(client.modId(), server.modId())) {
            mismatches.add("Config mod id mismatch: client=" + client.modId() + ", server=" + server.modId());
        }
        for (ConfigOption<?> serverOption : server.options().values()) {
            ConfigOption<?> clientOption = client.options().get(serverOption.key());
            if (clientOption == null) {
                mismatches.add("Client is missing server config option " + serverOption.key());
                continue;
            }
            if (serverOption.isServerSynced() && !Objects.equals(serverOption.get(), clientOption.get())) {
                mismatches.add("Synced option differs: " + serverOption.key()
                        + " client=" + clientOption.get() + " server=" + serverOption.get());
            }
        }
        return new SyncReport(version, mismatches);
    }

    public record SyncReport(String version, List<String> mismatches) {
        public boolean compatible() {
            return mismatches.isEmpty();
        }

        public String disconnectMessage(String customMessage) {
            if (compatible()) {
                return "";
            }
            if (customMessage != null && !customMessage.isBlank()) {
                return customMessage;
            }
            return "NexusCore config sync mismatch (" + version + "): " + String.join("; ", mismatches);
        }
    }

    private ConfigSyncDiagnostics() {
    }
}

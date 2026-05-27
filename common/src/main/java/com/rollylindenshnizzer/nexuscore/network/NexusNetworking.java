package com.rollylindenshnizzer.nexuscore.network;

import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageDecoder;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.networking.simple.SimpleNetworkManager;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public final class NexusNetworking {
    private static final Map<ResourceLocation, ChannelDiagnostics> CHANNELS = new ConcurrentHashMap<>();

    public static ChannelBuilder channel(String modId, String name) {
        return new ChannelBuilder(modId, name);
    }

    public static Map<ResourceLocation, ChannelDiagnostics> diagnostics() {
        return Map.copyOf(CHANNELS);
    }

    private NexusNetworking() {
    }

    public static final class ChannelBuilder {
        private final String modId;
        private final String name;
        private final SimpleNetworkManager manager;
        private final Set<String> packets = new LinkedHashSet<>();
        private String version = "1";
        private BiFunction<String, String, String> mismatchMessage = (client, server) ->
                "Network protocol mismatch: client=" + client + ", server=" + server;

        private ChannelBuilder(String modId, String name) {
            this.modId = NexusIds.requireNamespace(modId);
            this.name = NexusIds.normalizePath(name).replace('/', '_');
            this.manager = SimpleNetworkManager.create(this.modId);
        }

        public ChannelBuilder version(String version) {
            this.version = version;
            CHANNELS.put(channelId(), diagnosticsRecord());
            return this;
        }

        public ChannelBuilder protocolVersion(String version) {
            return version(version);
        }

        public ChannelBuilder disconnectOnMismatch(BiFunction<String, String, String> mismatchMessage) {
            this.mismatchMessage = mismatchMessage;
            CHANNELS.put(channelId(), diagnosticsRecord());
            return this;
        }

        public MessageType serverbound(String path, MessageDecoder<BaseC2SMessage> decoder) {
            return register(path, () -> manager.registerC2S(packetPath(path), decoder));
        }

        public MessageType clientbound(String path, MessageDecoder<BaseS2CMessage> decoder) {
            return register(path, () -> manager.registerS2C(packetPath(path), decoder));
        }

        public ResourceLocation id(String path) {
            return NexusIds.id(modId, packetPath(path));
        }

        public String version() {
            return version;
        }

        private MessageType register(String path, PacketRegistrar registrar) {
            String normalized = packetPath(path);
            if (!packets.add(normalized)) {
                throw new IllegalArgumentException("Duplicate packet path " + normalized + " on " + modId + ":" + name);
            }
            CHANNELS.put(channelId(), diagnosticsRecord());
            return registrar.register();
        }

        private String packetPath(String path) {
            return name + "_" + NexusIds.normalizePath(path).replace('/', '_');
        }

        private ResourceLocation channelId() {
            return NexusIds.id(modId, name);
        }

        private ChannelDiagnostics diagnosticsRecord() {
            return new ChannelDiagnostics(channelId(), version, Set.copyOf(packets), mismatchMessage);
        }
    }

    public record ChannelDiagnostics(ResourceLocation id, String protocolVersion, Set<String> packets,
                                     BiFunction<String, String, String> mismatchMessage) {
        public HandshakeReport compare(String remoteVersion, Set<String> remotePackets) {
            Set<String> missing = new LinkedHashSet<>(packets);
            missing.removeAll(remotePackets);
            Set<String> unexpected = new LinkedHashSet<>(remotePackets);
            unexpected.removeAll(packets);
            boolean compatible = protocolVersion.equals(remoteVersion) && missing.isEmpty() && unexpected.isEmpty();
            String message = compatible ? "" : mismatchMessage.apply(remoteVersion, protocolVersion);
            return new HandshakeReport(id, compatible, protocolVersion, remoteVersion, missing, unexpected, message);
        }
    }

    public record HandshakeReport(ResourceLocation channel, boolean compatible, String localVersion,
                                  String remoteVersion, Set<String> missingPackets, Set<String> unexpectedPackets,
                                  String message) {
    }

    @FunctionalInterface
    private interface PacketRegistrar {
        MessageType register();
    }
}

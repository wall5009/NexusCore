package com.rollylindenshnizzer.nexuscore.network;

import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageDecoder;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.networking.simple.SimpleNetworkManager;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashSet;
import java.util.Set;

public final class NexusNetworking {
    public static ChannelBuilder channel(String modId, String name) {
        return new ChannelBuilder(modId, name);
    }

    private NexusNetworking() {
    }

    public static final class ChannelBuilder {
        private final String modId;
        private final String name;
        private final SimpleNetworkManager manager;
        private final Set<String> packets = new LinkedHashSet<>();
        private String version = "1";

        private ChannelBuilder(String modId, String name) {
            this.modId = NexusIds.requireNamespace(modId);
            this.name = NexusIds.normalizePath(name).replace('/', '_');
            this.manager = SimpleNetworkManager.create(this.modId);
        }

        public ChannelBuilder version(String version) {
            this.version = version;
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
            return registrar.register();
        }

        private String packetPath(String path) {
            return name + "_" + NexusIds.normalizePath(path).replace('/', '_');
        }
    }

    @FunctionalInterface
    private interface PacketRegistrar {
        MessageType register();
    }
}

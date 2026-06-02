package com.rollylindenshnizzer.nexuscore.testmod;

import com.rollylindenshnizzer.nexuscore.api.network.NexusNetwork;
import com.rollylindenshnizzer.nexuscore.api.network.NexusPacket;
import com.rollylindenshnizzer.nexuscore.api.network.NexusPacketBuffer;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusRuntimeSide;

public final class TestNetwork {
    public static final NexusNetwork NETWORK = NexusNetwork.create("nexuscore_testmod", "main");

    private TestNetwork() {
    }

    public static void register() {
        NETWORK.serverbound("open_menu", OpenMenuPacket.class, OpenMenuPacket::decode)
            .handler((packet, context) -> {
                if (context.player() != null) {
                    context.player().openMenu(packet.menuId());
                }
            })
            .register();
        NETWORK.clientbound("open_menu_ack", OpenMenuAckPacket.class, OpenMenuAckPacket::decode)
            .handler((packet, context) -> {
                NexusCoreTestMod.MOD.logger().info("Received open menu acknowledgement: " + packet.menuId());
                if (context.side() == NexusRuntimeSide.CLIENT) {
                    NETWORK.sendToServer(new OpenMenuPacket(packet.menuId()));
                }
            })
            .register();
    }

    public static void sendJoinPacket(Object player) {
        NETWORK.sendToPlayer(player, new OpenMenuAckPacket("join_menu"));
    }

    public record OpenMenuPacket(String menuId) implements NexusPacket {
        public static OpenMenuPacket decode(NexusPacketBuffer buffer) {
            return new OpenMenuPacket(buffer.readString());
        }

        @Override
        public void encode(NexusPacketBuffer buffer) {
            buffer.writeString(menuId);
        }
    }

    public record OpenMenuAckPacket(String menuId) implements NexusPacket {
        public static OpenMenuAckPacket decode(NexusPacketBuffer buffer) {
            return new OpenMenuAckPacket(buffer.readString());
        }

        @Override
        public void encode(NexusPacketBuffer buffer) {
            buffer.writeString(menuId);
        }
    }
}

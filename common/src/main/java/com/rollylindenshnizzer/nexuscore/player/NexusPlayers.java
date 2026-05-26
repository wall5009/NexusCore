package com.rollylindenshnizzer.nexuscore.player;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.UUID;

public final class NexusPlayers {
    public static Optional<ServerPlayer> find(MinecraftServer server, UUID uuid) {
        return Optional.ofNullable(server.getPlayerList().getPlayer(uuid));
    }

    public static void giveOrDrop(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    public static void actionBar(ServerPlayer player, Component message) {
        player.displayClientMessage(message, true);
    }

    public static void message(ServerPlayer player, Component message) {
        player.sendSystemMessage(message);
    }

    public static void playSound(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        player.serverLevel().playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS, volume, pitch);
    }

    private NexusPlayers() {
    }
}

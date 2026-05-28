package com.rollylindenshnizzer.nexuscore.world;

import com.rollylindenshnizzer.nexuscore.api.NexusIncubating;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@NexusIncubating(since = "1.3")
public record NexusWorldEvent(Kind kind,
                              String hook,
                              Level level,
                              BlockPos pos,
                              BlockState state,
                              Player player,
                              InteractionHand hand,
                              Direction direction,
                              Instant createdAt) {
    public NexusWorldEvent {
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public static NexusWorldEvent of(Kind kind,
                                     String hook,
                                     Level level,
                                     BlockPos pos,
                                     BlockState state,
                                     Player player,
                                     InteractionHand hand,
                                     Direction direction) {
        return new NexusWorldEvent(kind, hook, level, pos, state, player, hand, direction, Instant.now());
    }

    public Optional<Level> optionalLevel() {
        return Optional.ofNullable(level);
    }

    public Optional<BlockPos> optionalPos() {
        return Optional.ofNullable(pos);
    }

    public Optional<BlockState> optionalState() {
        return Optional.ofNullable(state);
    }

    public Optional<Player> optionalPlayer() {
        return Optional.ofNullable(player);
    }

    public Optional<UUID> playerId() {
        return player == null ? Optional.empty() : Optional.of(player.getUUID());
    }

    public String dimension() {
        return level == null ? "unknown" : level.dimension().location().toString();
    }

    public String blockId() {
        if (state == null) {
            return "unknown";
        }
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
    }

    public boolean serverSide() {
        return level == null || !level.isClientSide();
    }

    public boolean hasBlockContext() {
        return level != null && pos != null;
    }

    public enum Kind {
        BLOCK_PLACE,
        BLOCK_BREAK,
        BLOCK_INTERACT,
        ITEM_INTERACT,
        ENTITY_INTERACT,
        LEVEL_TICK_START,
        LEVEL_TICK_END,
        SERVER_TICK_START,
        SERVER_TICK_END,
        UNKNOWN
    }
}

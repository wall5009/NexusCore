package com.rollylindenshnizzer.nexuscore.player;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerDataStore {
    private final Map<UUID, CompoundTag> data = new HashMap<>();

    public CompoundTag get(UUID player) {
        return data.computeIfAbsent(player, ignored -> new CompoundTag());
    }

    public void copy(UUID from, UUID to) {
        data.put(to, get(from).copy());
    }

    public void remove(UUID player) {
        data.remove(player);
    }
}

package com.rollylindenshnizzer.nexuscore.persistence;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Optional;
import java.util.UUID;

public final class NexusNbt {
    public static Optional<String> string(CompoundTag tag, String key) {
        return tag.contains(key, Tag.TAG_STRING) ? Optional.of(tag.getString(key)) : Optional.empty();
    }

    public static Optional<Integer> integer(CompoundTag tag, String key) {
        return tag.contains(key, Tag.TAG_INT) ? Optional.of(tag.getInt(key)) : Optional.empty();
    }

    public static Optional<Long> longValue(CompoundTag tag, String key) {
        return tag.contains(key, Tag.TAG_LONG) ? Optional.of(tag.getLong(key)) : Optional.empty();
    }

    public static Optional<Boolean> bool(CompoundTag tag, String key) {
        return tag.contains(key, Tag.TAG_BYTE) ? Optional.of(tag.getBoolean(key)) : Optional.empty();
    }

    public static Optional<UUID> uuid(CompoundTag tag, String key) {
        return tag.hasUUID(key) ? Optional.of(tag.getUUID(key)) : Optional.empty();
    }

    public static CompoundTag mergeCopy(CompoundTag left, CompoundTag right) {
        return left.copy().merge(right.copy());
    }

    private NexusNbt() {
    }
}

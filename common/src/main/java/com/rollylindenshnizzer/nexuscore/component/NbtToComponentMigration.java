package com.rollylindenshnizzer.nexuscore.component;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public final class NbtToComponentMigration<T> {
    private final String legacyKey;
    private final DataComponentType<T> componentType;
    private final BiFunction<CompoundTag, ItemStack, T> reader;

    public NbtToComponentMigration(String legacyKey, DataComponentType<T> componentType,
                                   BiFunction<CompoundTag, ItemStack, T> reader) {
        this.legacyKey = legacyKey;
        this.componentType = componentType;
        this.reader = reader;
    }

    public boolean migrate(ItemStack stack, CompoundTag legacyTag) {
        if (!legacyTag.contains(legacyKey)) {
            return false;
        }
        stack.set(componentType, reader.apply(legacyTag, stack));
        legacyTag.remove(legacyKey);
        return true;
    }

    public static final class Suite {
        private final List<NbtToComponentMigration<?>> migrations = new ArrayList<>();

        public Suite add(NbtToComponentMigration<?> migration) {
            migrations.add(migration);
            return this;
        }

        public int migrate(ItemStack stack, CompoundTag legacyTag) {
            int migrated = 0;
            for (NbtToComponentMigration<?> migration : migrations) {
                if (migration.migrate(stack, legacyTag)) {
                    migrated++;
                }
            }
            return migrated;
        }
    }
}

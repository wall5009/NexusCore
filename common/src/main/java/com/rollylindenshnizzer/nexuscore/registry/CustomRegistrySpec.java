package com.rollylindenshnizzer.nexuscore.registry;

import net.minecraft.resources.ResourceLocation;

public record CustomRegistrySpec<T>(ResourceLocation id, Class<T> type, boolean synced, boolean persistent) {
    public static <T> Builder<T> builder(ResourceLocation id, Class<T> type) {
        return new Builder<>(id, type);
    }

    public static final class Builder<T> {
        private final ResourceLocation id;
        private final Class<T> type;
        private boolean synced;
        private boolean persistent = true;

        private Builder(ResourceLocation id, Class<T> type) {
            this.id = id;
            this.type = type;
        }

        public Builder<T> synced() {
            synced = true;
            return this;
        }

        public Builder<T> transientRegistry() {
            persistent = false;
            return this;
        }

        public CustomRegistrySpec<T> build() {
            return new CustomRegistrySpec<>(id, type, synced, persistent);
        }
    }
}

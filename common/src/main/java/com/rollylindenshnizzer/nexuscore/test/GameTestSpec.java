package com.rollylindenshnizzer.nexuscore.test;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record GameTestSpec(ResourceLocation id, String structure, int timeoutTicks, List<BlockPos> markers) {
    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public static final class Builder {
        private final ResourceLocation id;
        private String structure = "empty";
        private int timeoutTicks = 100;
        private final List<BlockPos> markers = new ArrayList<>();

        private Builder(ResourceLocation id) {
            this.id = id;
        }

        public Builder structure(String structure) {
            this.structure = structure;
            return this;
        }

        public Builder timeoutTicks(int timeoutTicks) {
            this.timeoutTicks = timeoutTicks;
            return this;
        }

        public Builder marker(BlockPos pos) {
            markers.add(pos);
            return this;
        }

        public GameTestSpec build() {
            return new GameTestSpec(id, structure, timeoutTicks, List.copyOf(markers));
        }
    }
}

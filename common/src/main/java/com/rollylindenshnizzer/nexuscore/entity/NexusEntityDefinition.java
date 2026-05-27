package com.rollylindenshnizzer.nexuscore.entity;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import com.rollylindenshnizzer.nexuscore.data.NexusData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@NexusStable(since = "1.2")
public record NexusEntityDefinition(ResourceLocation id,
                                    MobCategory category,
                                    float width,
                                    float height,
                                    int trackingRange,
                                    int updateInterval,
                                    Map<String, Double> attributes,
                                    List<String> spawnBiomes,
                                    ProjectileDefinition projectile,
                                    boolean spawnEgg,
                                    int primaryEggColor,
                                    int secondaryEggColor) {
    public NexusEntityDefinition {
        width = Math.max(0.01F, width);
        height = Math.max(0.01F, height);
        trackingRange = Math.max(1, trackingRange);
        updateInterval = Math.max(1, updateInterval);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
        spawnBiomes = spawnBiomes == null ? List.of() : List.copyOf(spawnBiomes);
    }

    public static Builder builder(ResourceLocation id, MobCategory category) {
        return new Builder(id, category);
    }

    public NexusData.DataPlan writeTo(NexusData.DataPlan plan) {
        plan.translation("entity." + id.getNamespace() + "." + id.getPath(), title(id.getPath()));
        if (spawnEgg) {
            plan.translation("item." + id.getNamespace() + "." + id.getPath() + "_spawn_egg", title(id.getPath()) + " Spawn Egg")
                    .itemGenerated(id.getPath() + "_spawn_egg");
        }
        return plan;
    }

    private static String title(String path) {
        StringBuilder builder = new StringBuilder();
        for (String part : path.split("_")) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.isEmpty() ? path : builder.toString();
    }

    public static final class Builder {
        private final ResourceLocation id;
        private final MobCategory category;
        private float width = 0.6F;
        private float height = 1.8F;
        private int trackingRange = 64;
        private int updateInterval = 3;
        private final Map<String, Double> attributes = new LinkedHashMap<>();
        private final java.util.ArrayList<String> spawnBiomes = new java.util.ArrayList<>();
        private ProjectileDefinition projectile;
        private boolean spawnEgg;
        private int primaryEggColor = 0xFFFFFF;
        private int secondaryEggColor = 0xAAAAAA;

        private Builder(ResourceLocation id, MobCategory category) {
            this.id = id;
            this.category = category;
        }

        public Builder sized(float width, float height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder tracking(int range, int interval) {
            this.trackingRange = range;
            this.updateInterval = interval;
            return this;
        }

        public Builder attribute(String name, double value) {
            attributes.put(name, value);
            return this;
        }

        public Builder spawnBiome(String biomeOrTag) {
            spawnBiomes.add(biomeOrTag);
            return this;
        }

        public Builder projectile(ProjectileDefinition projectile) {
            this.projectile = projectile;
            return this;
        }

        public Builder spawnEgg(int primaryColor, int secondaryColor) {
            this.spawnEgg = true;
            this.primaryEggColor = primaryColor;
            this.secondaryEggColor = secondaryColor;
            return this;
        }

        public NexusEntityDefinition build() {
            return new NexusEntityDefinition(id, category, width, height, trackingRange, updateInterval,
                    attributes, spawnBiomes, projectile, spawnEgg, primaryEggColor, secondaryEggColor);
        }
    }
}

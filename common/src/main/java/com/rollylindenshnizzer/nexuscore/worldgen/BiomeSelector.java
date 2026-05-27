package com.rollylindenshnizzer.nexuscore.worldgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.rollylindenshnizzer.nexuscore.api.NexusStable;

import java.util.ArrayList;
import java.util.List;

@NexusStable(since = "1.2")
public record BiomeSelector(List<String> includes, List<String> excludes) {
    public BiomeSelector {
        includes = includes == null ? List.of() : List.copyOf(includes);
        excludes = excludes == null ? List.of() : List.copyOf(excludes);
    }

    public static BiomeSelector all() {
        return new BiomeSelector(List.of("#minecraft:is_overworld"), List.of());
    }

    public static Builder builder() {
        return new Builder();
    }

    public JsonElement toBiomeModifierPredicate() {
        if (!excludes.isEmpty()) {
            throw new IllegalStateException(
                    "NeoForge biome modifier JSON does not support NexusCore BiomeSelector excludes directly. " +
                            "Create a biome tag that already contains only the wanted biomes, then include that tag."
            );
        }

        List<String> targets = includes.isEmpty() ? BiomeSelector.all().includes() : includes;

        if (targets.size() == 1) {
            return new JsonPrimitive(targets.get(0));
        }

        JsonArray array = new JsonArray();
        targets.forEach(array::add);
        return array;
    }

    public static final class Builder {
        private final List<String> includes = new ArrayList<>();
        private final List<String> excludes = new ArrayList<>();

        public Builder include(String biomeOrTag) {
            includes.add(biomeOrTag);
            return this;
        }

        public Builder exclude(String biomeOrTag) {
            excludes.add(biomeOrTag);
            return this;
        }

        public BiomeSelector build() {
            return new BiomeSelector(includes, excludes);
        }
    }
}
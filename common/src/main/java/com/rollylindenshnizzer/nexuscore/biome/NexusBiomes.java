package com.rollylindenshnizzer.nexuscore.biome;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.data.NexusData;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@NexusStable(since = "1.3")
public final class NexusBiomes {
    private static final Map<ResourceLocation, BiomeDefinition> BIOMES = new LinkedHashMap<>();
    private static final List<BiomeModifierGroup> MODIFIERS = new ArrayList<>();

    public static BiomeDefinition.Builder biome(String namespace, String path) {
        return new BiomeDefinition.Builder(NexusIds.id(namespace, path));
    }

    public static BiomeDefinition register(BiomeDefinition definition) {
        BIOMES.put(definition.id(), definition);
        return definition;
    }

    public static Collection<BiomeDefinition> biomes() {
        return List.copyOf(BIOMES.values());
    }

    public static BiomeModifierGroup.Builder modifierGroup(String name) {
        return new BiomeModifierGroup.Builder(name);
    }

    public static BiomeModifierGroup registerModifierGroup(BiomeModifierGroup group) {
        MODIFIERS.add(group);
        return group;
    }

    public static BiomeBalanceReport balanceReport() {
        return balanceReport(BIOMES.values(), MODIFIERS);
    }

    public static NexusData.DataPlan writeAllTo(NexusData.DataPlan plan) {
        for (BiomeDefinition definition : BIOMES.values()) {
            if (definition.id().getNamespace().equals(plan.modId())) {
                definition.writeTo(plan);
            }
        }
        int index = 0;
        for (BiomeModifierGroup group : MODIFIERS) {
            plan.data("nexuscore/biome_modifier_group/" + NexusIds.normalizePath(group.name()) + "_" + index + ".json", group.toJson());
            index++;
        }
        return plan;
    }

    public static BiomeBalanceReport balanceReport(Collection<BiomeDefinition> biomes, Collection<BiomeModifierGroup> groups) {
        List<String> warnings = new ArrayList<>();
        for (BiomeDefinition biome : biomes) {
            if (biome.spawnWeight() > 200) {
                warnings.add(biome.id() + " has very high spawn weight " + biome.spawnWeight());
            }
            if (biome.generationWeight() > 200) {
                warnings.add(biome.id() + " has very high generation weight " + biome.generationWeight());
            }
        }
        return new BiomeBalanceReport(biomes.size(), groups.size(), warnings);
    }

    public static String debugSummary() {
        BiomeBalanceReport report = balanceReport();
        return "biomes=" + report.biomeCount() + ", modifierGroups=" + report.modifierGroupCount()
                + ", warnings=" + report.warnings().size();
    }

    @NexusStable(since = "1.3")
    public record BiomeDefinition(ResourceLocation id,
                                  float temperature,
                                  float downfall,
                                  int spawnWeight,
                                  int generationWeight,
                                  EnvironmentEffects effects,
                                  List<String> features,
                                  List<String> mobSpawns) {
        public BiomeDefinition {
            features = List.copyOf(features);
            mobSpawns = List.copyOf(mobSpawns);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("temperature", temperature);
            json.addProperty("downfall", downfall);
            json.addProperty("has_precipitation", downfall > 0.0F);
            json.add("effects", effects.toJson());
            json.add("features", strings(features));
            json.add("mob_spawns", strings(mobSpawns));
            JsonObject nexus = new JsonObject();
            nexus.addProperty("spawn_weight", spawnWeight);
            nexus.addProperty("generation_weight", generationWeight);
            json.add("nexuscore", nexus);
            return json;
        }

        public NexusData.DataPlan writeTo(NexusData.DataPlan plan) {
            plan.data("worldgen/biome/" + id.getPath() + ".json", toJson());
            plan.translation(NexusIds.translationKey("biome", id), NexusIds.humanName(id.getPath()));
            return plan;
        }

        public static final class Builder {
            private final ResourceLocation id;
            private float temperature = 0.8F;
            private float downfall = 0.4F;
            private int spawnWeight = 100;
            private int generationWeight = 100;
            private EnvironmentEffects effects = EnvironmentEffects.plains();
            private final List<String> features = new ArrayList<>();
            private final List<String> mobSpawns = new ArrayList<>();

            private Builder(ResourceLocation id) {
                this.id = id;
            }

            public Builder climate(float temperature, float downfall) {
                this.temperature = temperature;
                this.downfall = downfall;
                return this;
            }

            public Builder weights(int spawnWeight, int generationWeight) {
                this.spawnWeight = spawnWeight;
                this.generationWeight = generationWeight;
                return this;
            }

            public Builder effects(EnvironmentEffects effects) {
                this.effects = effects;
                return this;
            }

            public Builder feature(String feature) {
                this.features.add(feature);
                return this;
            }

            public Builder spawn(String spawn) {
                this.mobSpawns.add(spawn);
                return this;
            }

            public BiomeDefinition build() {
                return new BiomeDefinition(id, temperature, downfall, spawnWeight, generationWeight, effects, features, mobSpawns);
            }
        }
    }

    @NexusStable(since = "1.3")
    public record BiomeSelector(String expression, List<String> reasons) {
        public BiomeSelector {
            reasons = List.copyOf(reasons);
        }

        public static BiomeSelector tag(String tag) {
            return new BiomeSelector("tag:" + tag, List.of("matches biome tag"));
        }

        public static BiomeSelector dimension(String dimension) {
            return new BiomeSelector("dimension:" + dimension, List.of("matches dimension"));
        }

        public static BiomeSelector climate(float minTemp, float maxTemp, float minDownfall, float maxDownfall) {
            return new BiomeSelector("climate:" + minTemp + ".." + maxTemp + ":" + minDownfall + ".." + maxDownfall,
                    List.of("matches temperature and downfall range"));
        }

        public String explain() {
            return expression + " because " + String.join(", ", reasons);
        }
    }

    @NexusStable(since = "1.3")
    public record BiomeModifierGroup(String name, List<BiomeSelector> selectors, List<String> features, List<String> spawns) {
        public BiomeModifierGroup {
            selectors = List.copyOf(selectors);
            features = List.copyOf(features);
            spawns = List.copyOf(spawns);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("name", name);
            JsonArray selectorJson = new JsonArray();
            selectors.forEach(selector -> selectorJson.add(selector.expression()));
            json.add("selectors", selectorJson);
            json.add("features", strings(features));
            json.add("spawns", strings(spawns));
            return json;
        }

        public static final class Builder {
            private final String name;
            private final List<BiomeSelector> selectors = new ArrayList<>();
            private final List<String> features = new ArrayList<>();
            private final List<String> spawns = new ArrayList<>();

            private Builder(String name) {
                this.name = name;
            }

            public Builder selector(BiomeSelector selector) {
                this.selectors.add(selector);
                return this;
            }

            public Builder feature(String feature) {
                this.features.add(feature);
                return this;
            }

            public Builder spawn(String spawn) {
                this.spawns.add(spawn);
                return this;
            }

            public BiomeModifierGroup build() {
                return new BiomeModifierGroup(name, selectors, features, spawns);
            }
        }
    }

    @NexusStable(since = "1.3")
    public record EnvironmentEffects(int fogColor,
                                     int skyColor,
                                     int waterColor,
                                     int waterFogColor,
                                     int grassColor,
                                     int foliageColor,
                                     String ambientParticle,
                                     String ambientSound,
                                     String music) {
        public static EnvironmentEffects plains() {
            return new EnvironmentEffects(0xC0D8FF, 0x78A7FF, 0x3F76E4, 0x050533, 0x91BD59, 0x77AB2F,
                    "", "minecraft:ambient.cave", "minecraft:music.game");
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("fog_color", fogColor);
            json.addProperty("sky_color", skyColor);
            json.addProperty("water_color", waterColor);
            json.addProperty("water_fog_color", waterFogColor);
            json.addProperty("grass_color", grassColor);
            json.addProperty("foliage_color", foliageColor);
            json.addProperty("ambient_particle", ambientParticle);
            json.addProperty("ambient_sound", ambientSound);
            json.addProperty("music", music);
            return json;
        }
    }

    @NexusStable(since = "1.3")
    public record BiomeBalanceReport(int biomeCount, int modifierGroupCount, List<String> warnings) {
        public BiomeBalanceReport {
            warnings = List.copyOf(warnings);
        }
    }

    private static JsonArray strings(Collection<String> values) {
        JsonArray array = new JsonArray();
        values.forEach(array::add);
        return array;
    }

    private NexusBiomes() {
    }
}

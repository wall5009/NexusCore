package com.rollylindenshnizzer.nexuscore.dimension;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import com.rollylindenshnizzer.nexuscore.block.NexusBlocks;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.data.NexusData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@NexusStable(since = "1.3")
public final class NexusDimensions {
    private static final Map<ResourceLocation, DimensionDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, PortalDefinition> PORTALS = new LinkedHashMap<>();
    private static final Set<ResourceLocation> REGISTERED_PORTAL_BLOCKS = new HashSet<>();

    public static DimensionDefinition.Builder dimension(String namespace, String path) {
        return new DimensionDefinition.Builder(NexusIds.id(namespace, path));
    }

    public static DimensionDefinition register(DimensionDefinition definition) {
        DEFINITIONS.put(definition.id(), definition);
        return definition;
    }

    public static Collection<DimensionDefinition> definitions() {
        return List.copyOf(DEFINITIONS.values());
    }

    public static PortalDefinition.Builder portal(String namespace, String path) {
        return new PortalDefinition.Builder(NexusIds.id(namespace, path));
    }

    public static PortalDefinition registerPortal(PortalDefinition portal) {
        PORTALS.put(portal.id(), portal);
        return portal;
    }

    public static Collection<PortalDefinition> portals() {
        return List.copyOf(PORTALS.values());
    }

    public static DimensionValidationReport validate() {
        return validate(DEFINITIONS.values(), PORTALS.values());
    }

    public static NexusData.DataPlan writeAllTo(NexusData.DataPlan plan) {
        for (DimensionDefinition definition : DEFINITIONS.values()) {
            if (definition.id().getNamespace().equals(plan.modId())) {
                definition.writeTo(plan);
            }
        }
        for (PortalDefinition portal : PORTALS.values()) {
            if (portal.id().getNamespace().equals(plan.modId())) {
                portal.writeTo(plan);
            }
        }
        return plan;
    }

    public static int registerRuntimePortalBlocks(String modId) {
        int count = 0;
        for (PortalDefinition portal : PORTALS.values()) {
            if (!portal.id().getNamespace().equals(modId) || !REGISTERED_PORTAL_BLOCKS.add(portal.id())) {
                continue;
            }
            NexusBlocks.block(modId, portal.id().getPath() + "_portal")
                    .factory(properties -> new PortalRuntimeBlock(portal, properties))
                    .properties(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(1.5F, 6.0F).lightLevel(state -> 10).sound(SoundType.GLASS))
                    .withBlockItem()
                    .simpleCubeModel()
                    .dropsSelf()
                    .register();
            count++;
        }
        return count;
    }

    public static DimensionValidationReport validate(Collection<DimensionDefinition> dimensions,
                                                     Collection<PortalDefinition> portals) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Map<ResourceLocation, DimensionDefinition> byId = new LinkedHashMap<>();
        for (DimensionDefinition definition : dimensions) {
            if (byId.put(definition.id(), definition) != null) {
                errors.add("Duplicate dimension id " + definition.id());
            }
            if (definition.dimensionType() == null) {
                errors.add(definition.id() + " has no dimension type");
            }
            if (definition.biomeSource().isBlank()) {
                errors.add(definition.id() + " has no biome source");
            }
            if (definition.config().coordinateScale() <= 0.0D) {
                errors.add(definition.id() + " coordinate scale must be positive");
            }
            if (definition.spawnRules().safePlatformRadius() < 0) {
                errors.add(definition.id() + " safe platform radius cannot be negative");
            }
            if (!definition.dimensionType().fixedTime().isEmpty() && definition.config().weather()) {
                warnings.add(definition.id() + " uses fixed time and weather; confirm the intended mood.");
            }
        }
        for (PortalDefinition portal : portals) {
            if (!byId.containsKey(portal.targetDimension())) {
                errors.add(portal.id() + " targets missing dimension " + portal.targetDimension());
            }
            if (portal.cooldownTicks() < 0) {
                errors.add(portal.id() + " cooldown cannot be negative");
            }
            if (portal.frame().width() < 2 || portal.frame().height() < 3) {
                errors.add(portal.id() + " frame must be at least 2x3");
            }
        }
        return new DimensionValidationReport(errors, warnings);
    }

    public static String debugSummary() {
        DimensionValidationReport report = validate();
        return "dimensions=" + DEFINITIONS.size()
                + ", portals=" + PORTALS.size()
                + ", errors=" + report.errors().size()
                + ", warnings=" + report.warnings().size();
    }

    @NexusStable(since = "1.3")
    public record DimensionDefinition(ResourceLocation id,
                                      DimensionTypeDefinition dimensionType,
                                      String biomeSource,
                                      String chunkGenerator,
                                      DimensionEffects effects,
                                      DimensionSpawnRules spawnRules,
                                      DimensionConfigProfile config,
                                      Map<String, String> gameRules) {
        public DimensionDefinition {
            gameRules = Map.copyOf(gameRules);
        }

        public JsonObject toDimensionJson() {
            JsonObject json = new JsonObject();
            json.addProperty("type", dimensionType.id().toString());
            JsonObject generator = new JsonObject();
            generator.addProperty("type", chunkGenerator);
            generator.addProperty("biome_source", biomeSource);
            json.add("generator", generator);
            JsonObject nexus = new JsonObject();
            nexus.add("effects", effects.toJson());
            nexus.add("spawn_rules", spawnRules.toJson());
            nexus.add("config", config.toJson());
            JsonObject rules = new JsonObject();
            gameRules.forEach(rules::addProperty);
            nexus.add("game_rules", rules);
            json.add("nexuscore", nexus);
            return json;
        }

        public JsonObject toDimensionTypeJson() {
            return dimensionType.toJson();
        }

        public NexusData.DataPlan writeTo(NexusData.DataPlan plan) {
            plan.data("dimension/" + id.getPath() + ".json", toDimensionJson());
            plan.data("dimension_type/" + id.getPath() + ".json", toDimensionTypeJson());
            plan.translation(NexusIds.translationKey("dimension", id), NexusIds.humanName(id.getPath()));
            return plan;
        }

        public static final class Builder {
            private final ResourceLocation id;
            private DimensionTypeDefinition dimensionType;
            private String biomeSource = "minecraft:the_void";
            private String chunkGenerator = "minecraft:noise";
            private DimensionEffects effects = DimensionEffects.overworld();
            private DimensionSpawnRules spawnRules = DimensionSpawnRules.safeDefaults();
            private DimensionConfigProfile config = DimensionConfigProfile.defaults();
            private final Map<String, String> gameRules = new LinkedHashMap<>();

            private Builder(ResourceLocation id) {
                this.id = id;
                this.dimensionType = DimensionTypeDefinition.builder(id).build();
            }

            public Builder type(DimensionTypeDefinition dimensionType) {
                this.dimensionType = dimensionType;
                return this;
            }

            public Builder singleBiome(String biomeId) {
                this.biomeSource = "single:" + NexusIds.parse(biomeId);
                return this;
            }

            public Builder biomeTag(String biomeTag) {
                this.biomeSource = "tag:" + biomeTag;
                return this;
            }

            public Builder presetBiomeSource(String preset) {
                this.biomeSource = "preset:" + preset;
                return this;
            }

            public Builder noiseGenerator(String noiseSettings) {
                this.chunkGenerator = "minecraft:noise:" + noiseSettings;
                return this;
            }

            public Builder flatGenerator() {
                this.chunkGenerator = "minecraft:flat";
                return this;
            }

            public Builder effects(DimensionEffects effects) {
                this.effects = effects;
                return this;
            }

            public Builder spawnRules(DimensionSpawnRules spawnRules) {
                this.spawnRules = spawnRules;
                return this;
            }

            public Builder config(DimensionConfigProfile config) {
                this.config = config;
                return this;
            }

            public Builder gameRule(String key, String value) {
                this.gameRules.put(key, value);
                return this;
            }

            public DimensionDefinition build() {
                return new DimensionDefinition(id, dimensionType, biomeSource, chunkGenerator, effects, spawnRules, config, gameRules);
            }
        }
    }

    @NexusStable(since = "1.3")
    public record DimensionTypeDefinition(ResourceLocation id,
                                          Optional<Long> fixedTime,
                                          boolean skylight,
                                          boolean ceiling,
                                          boolean ultrawarm,
                                          boolean natural,
                                          double coordinateScale,
                                          boolean bedWorks,
                                          boolean respawnAnchorWorks,
                                          int minY,
                                          int height,
                                          int logicalHeight,
                                          String infiniburn,
                                          ResourceLocation effectsLocation,
                                          float ambientLight) {
        public DimensionTypeDefinition {
            fixedTime = fixedTime == null ? Optional.empty() : fixedTime;
        }

        public static Builder builder(ResourceLocation id) {
            return new Builder(id);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            fixedTime.ifPresent(value -> json.addProperty("fixed_time", value));
            json.addProperty("has_skylight", skylight);
            json.addProperty("has_ceiling", ceiling);
            json.addProperty("ultrawarm", ultrawarm);
            json.addProperty("natural", natural);
            json.addProperty("coordinate_scale", coordinateScale);
            json.addProperty("bed_works", bedWorks);
            json.addProperty("respawn_anchor_works", respawnAnchorWorks);
            json.addProperty("min_y", minY);
            json.addProperty("height", height);
            json.addProperty("logical_height", logicalHeight);
            json.addProperty("infiniburn", infiniburn);
            json.addProperty("effects", effectsLocation.toString());
            json.addProperty("ambient_light", ambientLight);
            return json;
        }

        public static final class Builder {
            private final ResourceLocation id;
            private Optional<Long> fixedTime = Optional.empty();
            private boolean skylight = true;
            private boolean ceiling;
            private boolean ultrawarm;
            private boolean natural = true;
            private double coordinateScale = 1.0D;
            private boolean bedWorks = true;
            private boolean respawnAnchorWorks;
            private int minY = -64;
            private int height = 384;
            private int logicalHeight = 384;
            private String infiniburn = "#minecraft:infiniburn_overworld";
            private ResourceLocation effectsLocation = ResourceLocation.withDefaultNamespace("overworld");
            private float ambientLight;

            private Builder(ResourceLocation id) {
                this.id = id;
            }

            public Builder fixedTime(long fixedTime) {
                this.fixedTime = Optional.of(fixedTime);
                return this;
            }

            public Builder skylight(boolean skylight) {
                this.skylight = skylight;
                return this;
            }

            public Builder ceiling(boolean ceiling) {
                this.ceiling = ceiling;
                return this;
            }

            public Builder ultrawarm(boolean ultrawarm) {
                this.ultrawarm = ultrawarm;
                return this;
            }

            public Builder natural(boolean natural) {
                this.natural = natural;
                return this;
            }

            public Builder coordinateScale(double coordinateScale) {
                this.coordinateScale = coordinateScale;
                return this;
            }

            public Builder bedWorks(boolean bedWorks) {
                this.bedWorks = bedWorks;
                return this;
            }

            public Builder respawnAnchorWorks(boolean respawnAnchorWorks) {
                this.respawnAnchorWorks = respawnAnchorWorks;
                return this;
            }

            public Builder height(int minY, int height, int logicalHeight) {
                this.minY = minY;
                this.height = height;
                this.logicalHeight = logicalHeight;
                return this;
            }

            public Builder infiniburn(String infiniburn) {
                this.infiniburn = infiniburn;
                return this;
            }

            public Builder effects(ResourceLocation effectsLocation) {
                this.effectsLocation = effectsLocation;
                return this;
            }

            public Builder ambientLight(float ambientLight) {
                this.ambientLight = ambientLight;
                return this;
            }

            public DimensionTypeDefinition build() {
                return new DimensionTypeDefinition(id, fixedTime, skylight, ceiling, ultrawarm, natural,
                        coordinateScale, bedWorks, respawnAnchorWorks, minY, height, logicalHeight,
                        infiniburn, effectsLocation, ambientLight);
            }
        }
    }

    @NexusStable(since = "1.3")
    public record DimensionEffects(int skyColor,
                                   int fogColor,
                                   int cloudColor,
                                   int ambientColor,
                                   String music,
                                   String ambience,
                                   boolean thickFog,
                                   boolean portalOverlay) {
        public static DimensionEffects overworld() {
            return new DimensionEffects(0x78A7FF, 0xC0D8FF, 0xFFFFFF, 0x000000,
                    "minecraft:music.game", "minecraft:ambient.cave", false, false);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("sky_color", skyColor);
            json.addProperty("fog_color", fogColor);
            json.addProperty("cloud_color", cloudColor);
            json.addProperty("ambient_color", ambientColor);
            json.addProperty("music", music);
            json.addProperty("ambience", ambience);
            json.addProperty("thick_fog", thickFog);
            json.addProperty("portal_overlay", portalOverlay);
            return json;
        }
    }

    @NexusStable(since = "1.3")
    public record DimensionSpawnRules(boolean playersCanSpawn,
                                      boolean mobsCanSpawn,
                                      int safePlatformRadius,
                                      List<String> defaultMobSpawns) {
        public DimensionSpawnRules {
            defaultMobSpawns = List.copyOf(defaultMobSpawns);
        }

        public static DimensionSpawnRules safeDefaults() {
            return new DimensionSpawnRules(true, true, 2, List.of());
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("players_can_spawn", playersCanSpawn);
            json.addProperty("mobs_can_spawn", mobsCanSpawn);
            json.addProperty("safe_platform_radius", safePlatformRadius);
            JsonArray spawns = new JsonArray();
            defaultMobSpawns.forEach(spawns::add);
            json.add("default_mob_spawns", spawns);
            return json;
        }
    }

    @NexusStable(since = "1.3")
    public record DimensionConfigProfile(boolean weather,
                                         boolean bedsExplode,
                                         double coordinateScale,
                                         int worldBorderDiameter,
                                         int defaultTime) {
        public static DimensionConfigProfile defaults() {
            return new DimensionConfigProfile(true, false, 1.0D, 60_000_000, 6_000);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("weather", weather);
            json.addProperty("beds_explode", bedsExplode);
            json.addProperty("coordinate_scale", coordinateScale);
            json.addProperty("world_border_diameter", worldBorderDiameter);
            json.addProperty("default_time", defaultTime);
            return json;
        }
    }

    @NexusStable(since = "1.3")
    public record PortalDefinition(ResourceLocation id,
                                   ResourceLocation targetDimension,
                                   PortalFramePattern frame,
                                   TeleportTarget returnTarget,
                                   int cooldownTicks,
                                   String particlePreset,
                                   String soundPreset,
                                   boolean syncPlayerData) {
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("id", id.toString());
            json.addProperty("target_dimension", targetDimension.toString());
            json.add("frame", frame.toJson());
            json.add("return_target", returnTarget.toJson());
            json.addProperty("cooldown_ticks", cooldownTicks);
            json.addProperty("particle_preset", particlePreset);
            json.addProperty("sound_preset", soundPreset);
            json.addProperty("sync_player_data", syncPlayerData);
            return json;
        }

        public NexusData.DataPlan writeTo(NexusData.DataPlan plan) {
            plan.data("nexuscore/portal/" + id.getPath() + ".json", toJson());
            plan.translation(NexusIds.translationKey("portal", id), NexusIds.humanName(id.getPath()));
            plan.translation("block." + id.getNamespace() + "." + id.getPath().replace('/', '.') + "_portal",
                    NexusIds.humanName(id.getPath()) + " Portal");
            return plan;
        }

        public static final class Builder {
            private final ResourceLocation id;
            private ResourceLocation targetDimension = ResourceLocation.withDefaultNamespace("overworld");
            private PortalFramePattern frame = new PortalFramePattern("minecraft:obsidian", 4, 5, true);
            private TeleportTarget returnTarget = TeleportTarget.safe(ResourceLocation.withDefaultNamespace("overworld"));
            private int cooldownTicks = 80;
            private String particlePreset = "nexuscore:portal";
            private String soundPreset = "minecraft:block.portal.travel";
            private boolean syncPlayerData = true;

            private Builder(ResourceLocation id) {
                this.id = id;
            }

            public Builder targetDimension(ResourceLocation targetDimension) {
                this.targetDimension = targetDimension;
                this.returnTarget = TeleportTarget.safe(targetDimension);
                return this;
            }

            public Builder frame(String blockId, int width, int height) {
                this.frame = new PortalFramePattern(blockId, width, height, true);
                return this;
            }

            public Builder returnTarget(TeleportTarget returnTarget) {
                this.returnTarget = returnTarget;
                return this;
            }

            public Builder cooldownTicks(int cooldownTicks) {
                this.cooldownTicks = cooldownTicks;
                return this;
            }

            public Builder particles(String particlePreset) {
                this.particlePreset = particlePreset;
                return this;
            }

            public Builder sound(String soundPreset) {
                this.soundPreset = soundPreset;
                return this;
            }

            public Builder syncPlayerData(boolean syncPlayerData) {
                this.syncPlayerData = syncPlayerData;
                return this;
            }

            public PortalDefinition build() {
                return new PortalDefinition(id, targetDimension, frame, returnTarget, cooldownTicks,
                        particlePreset, soundPreset, syncPlayerData);
            }
        }
    }

    @NexusStable(since = "1.3")
    public record PortalFramePattern(String blockId, int width, int height, boolean allowCorners) {
        public boolean matches(List<String> rows) {
            if (rows.size() != height) {
                return false;
            }
            for (String row : rows) {
                if (row.length() != width) {
                    return false;
                }
            }
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    char value = rows.get(y).charAt(x);
                    boolean border = x == 0 || y == 0 || x == width - 1 || y == height - 1;
                    boolean corner = (x == 0 || x == width - 1) && (y == 0 || y == height - 1);
                    if (border && (!corner || allowCorners) && value != '#') {
                        return false;
                    }
                    if (!border && value != ' ') {
                        return false;
                    }
                }
            }
            return true;
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("block", blockId);
            json.addProperty("width", width);
            json.addProperty("height", height);
            json.addProperty("allow_corners", allowCorners);
            return json;
        }
    }

    @NexusStable(since = "1.3")
    public record TeleportTarget(ResourceLocation dimension, double x, double y, double z, float yaw, float pitch, boolean safePlatform) {
        public static TeleportTarget safe(ResourceLocation dimension) {
            return new TeleportTarget(dimension, 0.5D, 80.0D, 0.5D, 0.0F, 0.0F, true);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("dimension", dimension.toString());
            json.addProperty("x", x);
            json.addProperty("y", y);
            json.addProperty("z", z);
            json.addProperty("yaw", yaw);
            json.addProperty("pitch", pitch);
            json.addProperty("safe_platform", safePlatform);
            return json;
        }
    }

    @NexusStable(since = "1.3")
    public record DimensionValidationReport(List<String> errors, List<String> warnings) {
        public DimensionValidationReport {
            errors = List.copyOf(errors);
            warnings = List.copyOf(warnings);
        }

        public boolean passed() {
            return errors.isEmpty();
        }

        public String summary() {
            return "Dimension validation " + (passed() ? "passed" : "failed")
                    + " with " + errors.size() + " errors and " + warnings.size() + " warnings";
        }
    }

    private NexusDimensions() {
    }
}

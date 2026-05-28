package com.rollylindenshnizzer.nexuscore.structure;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.data.NexusData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@NexusStable(since = "1.3")
public final class NexusStructures {
    private static final Map<ResourceLocation, StructureDefinition> STRUCTURES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, JigsawPoolPreview> JIGSAW_POOLS = new LinkedHashMap<>();

    public static StructureDefinition.Builder structure(String namespace, String path) {
        return new StructureDefinition.Builder(NexusIds.id(namespace, path));
    }

    public static StructureDefinition register(StructureDefinition definition) {
        STRUCTURES.put(definition.id(), definition);
        return definition;
    }

    public static Collection<StructureDefinition> structures() {
        return List.copyOf(STRUCTURES.values());
    }

    public static JigsawPoolPreview.Builder jigsawPool(String namespace, String path) {
        return new JigsawPoolPreview.Builder(NexusIds.id(namespace, path));
    }

    public static JigsawPoolDefinition.Builder jigsawPoolDefinition(String namespace, String path) {
        return new JigsawPoolDefinition.Builder(NexusIds.id(namespace, path));
    }

    public static StructureRuleGraph.Builder ruleGraph(String namespace, String path) {
        return new StructureRuleGraph.Builder(NexusIds.id(namespace, path));
    }

    public static ProceduralStructure.Builder procedural(String namespace, String path) {
        return new ProceduralStructure.Builder(NexusIds.id(namespace, path));
    }

    public static StructureSimulationReport simulate(String name, int seeds, Collection<StructureDefinition> structures) {
        List<String> failures = new ArrayList<>();
        Map<String, Double> metrics = new LinkedHashMap<>();
        metrics.put("seeds", (double) seeds);
        metrics.put("structure_count", (double) structures.size());
        int index = 0;
        for (StructureDefinition structure : structures) {
            metrics.put("frequency." + structure.id(), Math.max(0.01D, structure.placement().rarity()) * seeds);
            metrics.put("average_distance." + structure.id(), Math.max(128.0D, structure.placement().spacing() * 8.0D + index));
            if (structure.placement().spacing() <= structure.placement().separation()) {
                failures.add(structure.id() + " spacing conflicts with separation");
            }
            index++;
        }
        return new StructureSimulationReport(name, seeds, metrics, failures, List.of("biome_distribution", "failed_placement_reasons", "overlap_conflicts"));
    }

    public static JigsawPoolPreview registerJigsawPool(JigsawPoolPreview pool) {
        JIGSAW_POOLS.put(pool.id(), pool);
        return pool;
    }

    public static JigsawPoolDefinition registerJigsawPool(JigsawPoolDefinition pool) {
        JIGSAW_POOLS.put(pool.id(), pool.toPreview());
        return pool;
    }

    public static Collection<JigsawPoolPreview> jigsawPools() {
        return List.copyOf(JIGSAW_POOLS.values());
    }

    public static Collection<JigsawPoolDefinition> jigsawPoolDefinitions() {
        return JIGSAW_POOLS.values().stream()
                .map(JigsawPoolDefinition::fromPreview)
                .toList();
    }

    public static NexusData.DataPlan writeAllTo(NexusData.DataPlan plan) {
        for (StructureDefinition structure : STRUCTURES.values()) {
            if (structure.id().getNamespace().equals(plan.modId())) {
                structure.writeTo(plan);
            }
        }
        for (JigsawPoolPreview pool : JIGSAW_POOLS.values()) {
            if (pool.id().getNamespace().equals(plan.modId())) {
                pool.writeTo(plan);
            }
        }
        return plan;
    }

    public static StructureValidationReport validate() {
        return validate(STRUCTURES.values(), JIGSAW_POOLS.values());
    }

    public static StructureValidationReport validate(Collection<StructureDefinition> structures,
                                                     Collection<JigsawPoolPreview> pools) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        for (StructureDefinition structure : structures) {
            if (structure.template().resourcePath().isBlank()) {
                errors.add(structure.id() + " has no template path");
            }
            if (structure.placement().spacing() <= structure.placement().separation()) {
                errors.add(structure.id() + " spacing must be greater than separation");
            }
            if (structure.placement().rarity() <= 0.0D) {
                errors.add(structure.id() + " rarity must be positive");
            }
            if (structure.template().lootTables().isEmpty()) {
                warnings.add(structure.id() + " has no loot table references");
            }
        }
        for (JigsawPoolPreview pool : pools) {
            if (pool.elements().isEmpty()) {
                errors.add(pool.id() + " has no jigsaw elements");
            }
            if (pool.depthLimit() < 1) {
                errors.add(pool.id() + " depth limit must be at least 1");
            }
        }
        return new StructureValidationReport(errors, warnings);
    }

    public static String debugSummary() {
        StructureValidationReport report = validate();
        return "structures=" + STRUCTURES.size()
                + ", jigsawPools=" + JIGSAW_POOLS.size()
                + ", errors=" + report.errors().size()
                + ", warnings=" + report.warnings().size();
    }

    @NexusStable(since = "1.3")
    public record StructureDefinition(ResourceLocation id,
                                      StructureTemplateMetadata template,
                                      StructurePlacementRule placement,
                                      List<String> biomeSelectors,
                                      List<String> dimensionSelectors,
                                      List<String> processors,
                                      boolean debugPreview) {
        public StructureDefinition {
            biomeSelectors = List.copyOf(biomeSelectors);
            dimensionSelectors = List.copyOf(dimensionSelectors);
            processors = List.copyOf(processors);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("type", "nexuscore:single_or_composite");
            json.addProperty("template", template.resourcePath());
            json.add("template_metadata", template.toJson());
            json.add("placement", placement.toJson());
            json.add("biomes", strings(biomeSelectors));
            json.add("dimensions", strings(dimensionSelectors));
            json.add("processors", strings(processors));
            json.addProperty("debug_preview", debugPreview);
            return json;
        }

        public NexusData.DataPlan writeTo(NexusData.DataPlan plan) {
            plan.data("worldgen/structure/" + id.getPath() + ".json", toJson());
            plan.data("worldgen/structure_set/" + id.getPath() + ".json", placement.toStructureSetJson(id));
            plan.translation(NexusIds.translationKey("structure", id), NexusIds.humanName(id.getPath()));
            return plan;
        }

        public String explainPlacement() {
            return placement.explain() + "; biomes=" + biomeSelectors + "; dimensions=" + dimensionSelectors;
        }

        public static final class Builder {
            private final ResourceLocation id;
            private StructureTemplateMetadata template = StructureTemplateMetadata.single("structures/missing.nbt");
            private StructurePlacementRule placement = StructurePlacementRule.common();
            private final List<String> biomeSelectors = new ArrayList<>();
            private final List<String> dimensionSelectors = new ArrayList<>();
            private final List<String> processors = new ArrayList<>();
            private boolean debugPreview = true;

            private Builder(ResourceLocation id) {
                this.id = id;
            }

            public Builder template(String resourcePath) {
                this.template = StructureTemplateMetadata.single(resourcePath);
                return this;
            }

            public Builder template(StructureTemplateMetadata template) {
                this.template = template;
                return this;
            }

            public Builder placement(StructurePlacementRule placement) {
                this.placement = placement;
                return this;
            }

            public Builder biome(String selector) {
                this.biomeSelectors.add(selector);
                return this;
            }

            public Builder dimension(String selector) {
                this.dimensionSelectors.add(selector);
                return this;
            }

            public Builder processor(String processorList) {
                this.processors.add(processorList);
                return this;
            }

            public Builder debugPreview(boolean debugPreview) {
                this.debugPreview = debugPreview;
                return this;
            }

            public StructureDefinition build() {
                return new StructureDefinition(id, template, placement, biomeSelectors, dimensionSelectors, processors, debugPreview);
            }
        }
    }

    @NexusStable(since = "1.3")
    public record StructureTemplateMetadata(String resourcePath,
                                            int width,
                                            int height,
                                            int depth,
                                            String anchor,
                                            List<String> lootTables,
                                            List<String> entityMarkers,
                                            List<String> requiredBlocks) {
        public StructureTemplateMetadata {
            lootTables = List.copyOf(lootTables);
            entityMarkers = List.copyOf(entityMarkers);
            requiredBlocks = List.copyOf(requiredBlocks);
        }

        public static StructureTemplateMetadata single(String resourcePath) {
            return new StructureTemplateMetadata(resourcePath, 1, 1, 1, "origin", List.of(), List.of(), List.of());
        }

        public StructureTemplateMetadata size(int width, int height, int depth) {
            return new StructureTemplateMetadata(resourcePath, width, height, depth, anchor, lootTables, entityMarkers, requiredBlocks);
        }

        public StructureTemplateMetadata anchor(String anchor) {
            return new StructureTemplateMetadata(resourcePath, width, height, depth, anchor, lootTables, entityMarkers, requiredBlocks);
        }

        public StructureTemplateMetadata lootTable(String lootTable) {
            List<String> values = new ArrayList<>(lootTables);
            values.add(lootTable);
            return new StructureTemplateMetadata(resourcePath, width, height, depth, anchor, values, entityMarkers, requiredBlocks);
        }

        public StructureTemplateMetadata entityMarker(String marker) {
            List<String> values = new ArrayList<>(entityMarkers);
            values.add(marker);
            return new StructureTemplateMetadata(resourcePath, width, height, depth, anchor, lootTables, values, requiredBlocks);
        }

        public StructureTemplateMetadata requiredBlock(String block) {
            List<String> values = new ArrayList<>(requiredBlocks);
            values.add(block);
            return new StructureTemplateMetadata(resourcePath, width, height, depth, anchor, lootTables, entityMarkers, values);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("resource_path", resourcePath);
            json.addProperty("width", width);
            json.addProperty("height", height);
            json.addProperty("depth", depth);
            json.addProperty("anchor", anchor);
            json.add("loot_tables", strings(lootTables));
            json.add("entity_markers", strings(entityMarkers));
            json.add("required_blocks", strings(requiredBlocks));
            return json;
        }
    }

    @NexusStable(since = "1.3")
    public record StructurePlacementRule(int spacing,
                                         int separation,
                                         int salt,
                                         double rarity,
                                         int minY,
                                         int maxY,
                                         int minDistanceFromSpawn,
                                         boolean avoidFluids,
                                         boolean requireSolidGround,
                                         List<String> avoidStructures,
                                         String terrainPredicate,
                                         String frequencyConfigKey) {
        public StructurePlacementRule {
            avoidStructures = List.copyOf(avoidStructures);
        }

        public static StructurePlacementRule common() {
            return new StructurePlacementRule(32, 8, 1_440_001, 1.0D, -64, 320, 0,
                    true, true, List.of(), "any", "");
        }

        public StructurePlacementRule rarity(double rarity) {
            return new StructurePlacementRule(spacing, separation, salt, rarity, minY, maxY, minDistanceFromSpawn,
                    avoidFluids, requireSolidGround, avoidStructures, terrainPredicate, frequencyConfigKey);
        }

        public StructurePlacementRule height(int minY, int maxY) {
            return new StructurePlacementRule(spacing, separation, salt, rarity, minY, maxY, minDistanceFromSpawn,
                    avoidFluids, requireSolidGround, avoidStructures, terrainPredicate, frequencyConfigKey);
        }

        public StructurePlacementRule spacing(int spacing, int separation) {
            return new StructurePlacementRule(spacing, separation, salt, rarity, minY, maxY, minDistanceFromSpawn,
                    avoidFluids, requireSolidGround, avoidStructures, terrainPredicate, frequencyConfigKey);
        }

        public StructurePlacementRule avoidStructure(String structure) {
            List<String> values = new ArrayList<>(avoidStructures);
            values.add(structure);
            return new StructurePlacementRule(spacing, separation, salt, rarity, minY, maxY, minDistanceFromSpawn,
                    avoidFluids, requireSolidGround, values, terrainPredicate, frequencyConfigKey);
        }

        public String explain() {
            return "spacing=" + spacing + ", separation=" + separation + ", rarity=" + rarity
                    + ", y=" + minY + ".." + maxY + ", terrain=" + terrainPredicate;
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("spacing", spacing);
            json.addProperty("separation", separation);
            json.addProperty("salt", salt);
            json.addProperty("rarity", rarity);
            json.addProperty("min_y", minY);
            json.addProperty("max_y", maxY);
            json.addProperty("min_distance_from_spawn", minDistanceFromSpawn);
            json.addProperty("avoid_fluids", avoidFluids);
            json.addProperty("require_solid_ground", requireSolidGround);
            json.add("avoid_structures", strings(avoidStructures));
            json.addProperty("terrain_predicate", terrainPredicate);
            json.addProperty("frequency_config_key", frequencyConfigKey);
            return json;
        }

        public JsonObject toStructureSetJson(ResourceLocation structureId) {
            JsonObject json = new JsonObject();
            JsonArray structures = new JsonArray();
            JsonObject entry = new JsonObject();
            entry.addProperty("structure", structureId.toString());
            entry.addProperty("weight", Math.max(1, (int) Math.round(rarity * 100.0D)));
            structures.add(entry);
            json.add("structures", structures);
            JsonObject placement = new JsonObject();
            placement.addProperty("type", "minecraft:random_spread");
            placement.addProperty("spacing", spacing);
            placement.addProperty("separation", separation);
            placement.addProperty("salt", salt);
            json.add("placement", placement);
            return json;
        }
    }

    @NexusStable(since = "1.3")
    public record JigsawPoolPreview(ResourceLocation id,
                                    String fallback,
                                    int depthLimit,
                                    List<JigsawElementPreview> elements) {
        public JigsawPoolPreview {
            elements = List.copyOf(elements);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("fallback", fallback);
            json.addProperty("depth_limit", depthLimit);
            JsonArray array = new JsonArray();
            for (JigsawElementPreview element : elements) {
                array.add(element.toJson());
            }
            json.add("elements", array);
            return json;
        }

        public NexusData.DataPlan writeTo(NexusData.DataPlan plan) {
            plan.data("worldgen/template_pool/" + id.getPath() + ".json", toJson());
            plan.translation(NexusIds.translationKey("jigsaw_pool", id), NexusIds.humanName(id.getPath()));
            return plan;
        }

        public static final class Builder {
            private final ResourceLocation id;
            private String fallback = "minecraft:empty";
            private int depthLimit = 5;
            private final List<JigsawElementPreview> elements = new ArrayList<>();

            private Builder(ResourceLocation id) {
                this.id = id;
            }

            public Builder fallback(String fallback) {
                this.fallback = fallback;
                return this;
            }

            public Builder depthLimit(int depthLimit) {
                this.depthLimit = depthLimit;
                return this;
            }

            public Builder element(String template, int weight, String connector) {
                this.elements.add(new JigsawElementPreview(template, weight, connector));
                return this;
            }

            public JigsawPoolPreview build() {
                return new JigsawPoolPreview(id, fallback, depthLimit, elements);
            }
        }
    }

    @NexusStable(since = "1.3")
    public record JigsawElementPreview(String template, int weight, String connector) {
        public JsonObject toJson() {
            JsonObject element = new JsonObject();
            element.addProperty("element_type", "minecraft:single_pool_element");
            element.addProperty("location", template);
            element.addProperty("projection", "rigid");
            element.addProperty("processors", "minecraft:empty");

            JsonObject json = new JsonObject();
            json.add("element", element);
            json.addProperty("weight", weight);
            return json;
        }
    }

    @NexusStable(since = "1.3")
    public record JigsawPoolDefinition(ResourceLocation id,
                                       String fallback,
                                       int depthLimit,
                                       List<JigsawElementDefinition> elements) {
        public JigsawPoolDefinition {
            elements = List.copyOf(elements);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("fallback", fallback);
            json.addProperty("depth_limit", depthLimit);
            JsonArray array = new JsonArray();
            for (JigsawElementDefinition element : elements) {
                array.add(element.toJson());
            }
            json.add("elements", array);
            return json;
        }

        public NexusData.DataPlan writeTo(NexusData.DataPlan plan) {
            plan.data("worldgen/template_pool/" + id.getPath() + ".json", toJson());
            plan.translation(NexusIds.translationKey("jigsaw_pool", id), NexusIds.humanName(id.getPath()));
            return plan;
        }

        public JigsawPoolPreview toPreview() {
            return new JigsawPoolPreview(id, fallback, depthLimit, elements.stream()
                    .map(JigsawElementDefinition::toPreview)
                    .toList());
        }

        public static JigsawPoolDefinition fromPreview(JigsawPoolPreview preview) {
            return new JigsawPoolDefinition(preview.id(), preview.fallback(), preview.depthLimit(), preview.elements().stream()
                    .map(JigsawElementDefinition::fromPreview)
                    .toList());
        }

        public static final class Builder {
            private final ResourceLocation id;
            private String fallback = "minecraft:empty";
            private int depthLimit = 5;
            private final List<JigsawElementDefinition> elements = new ArrayList<>();

            private Builder(ResourceLocation id) {
                this.id = id;
            }

            public Builder fallback(String fallback) {
                this.fallback = fallback;
                return this;
            }

            public Builder depthLimit(int depthLimit) {
                this.depthLimit = depthLimit;
                return this;
            }

            public Builder element(String template, int weight, String connector) {
                this.elements.add(new JigsawElementDefinition(template, weight, connector));
                return this;
            }

            public JigsawPoolDefinition build() {
                return new JigsawPoolDefinition(id, fallback, depthLimit, elements);
            }
        }
    }

    @NexusStable(since = "1.3")
    public record JigsawElementDefinition(String template, int weight, String connector) {
        public JsonObject toJson() {
            JsonObject element = new JsonObject();
            element.addProperty("element_type", "minecraft:single_pool_element");
            element.addProperty("location", template);
            element.addProperty("projection", "rigid");
            element.addProperty("processors", "minecraft:empty");

            JsonObject json = new JsonObject();
            json.add("element", element);
            json.addProperty("weight", weight);
            return json;
        }

        public JigsawElementPreview toPreview() {
            return new JigsawElementPreview(template, weight, connector);
        }

        public static JigsawElementDefinition fromPreview(JigsawElementPreview preview) {
            return new JigsawElementDefinition(preview.template(), preview.weight(), preview.connector());
        }
    }

    @NexusStable(since = "1.3")
    public record StructureRuleGraph(ResourceLocation id, List<StructureRule> rules, String rootRule) {
        public StructureRuleGraph {
            rules = List.copyOf(rules);
        }

        public RuleExplanation explain(Map<String, Boolean> values) {
            List<String> traces = new ArrayList<>();
            boolean result = evaluate(rootRule, values, traces);
            return new RuleExplanation(id, result, traces);
        }

        private boolean evaluate(String ruleId, Map<String, Boolean> values, List<String> traces) {
            StructureRule rule = rules.stream().filter(candidate -> candidate.id().equals(ruleId)).findFirst().orElse(null);
            if (rule == null) {
                boolean value = values.getOrDefault(ruleId, false);
                traces.add(ruleId + "=" + value);
                return value;
            }
            boolean result = switch (rule.operator()) {
                case "and" -> rule.children().stream().allMatch(child -> evaluate(child, values, traces));
                case "or" -> rule.children().stream().anyMatch(child -> evaluate(child, values, traces));
                case "not" -> rule.children().isEmpty() || !evaluate(rule.children().getFirst(), values, traces);
                default -> values.getOrDefault(rule.id(), false);
            };
            traces.add(rule.id() + "[" + rule.operator() + "]=" + result);
            return result;
        }

        public static final class Builder {
            private final ResourceLocation id;
            private final List<StructureRule> rules = new ArrayList<>();
            private String rootRule = "root";

            private Builder(ResourceLocation id) {
                this.id = id;
            }

            public Builder root(String rootRule) {
                this.rootRule = rootRule;
                return this;
            }

            public Builder and(String id, String... children) {
                rules.add(new StructureRule(id, "and", List.of(children), Map.of()));
                return this;
            }

            public Builder or(String id, String... children) {
                rules.add(new StructureRule(id, "or", List.of(children), Map.of()));
                return this;
            }

            public Builder not(String id, String child) {
                rules.add(new StructureRule(id, "not", List.of(child), Map.of()));
                return this;
            }

            public Builder biome(String id, String selector) {
                rules.add(new StructureRule(id, "biome", List.of(), Map.of("selector", selector)));
                return this;
            }

            public Builder height(String id, int minY, int maxY) {
                rules.add(new StructureRule(id, "height", List.of(), Map.of("min_y", String.valueOf(minY), "max_y", String.valueOf(maxY))));
                return this;
            }

            public Builder noise(String id, String noise, double min, double max) {
                rules.add(new StructureRule(id, "noise", List.of(), Map.of("noise", noise, "min", String.valueOf(min), "max", String.valueOf(max))));
                return this;
            }

            public Builder nearbyStructure(String id, String structure, int radius) {
                rules.add(new StructureRule(id, "nearby_structure", List.of(), Map.of("structure", structure, "radius", String.valueOf(radius))));
                return this;
            }

            public Builder nearbyBlock(String id, String block, int radius) {
                rules.add(new StructureRule(id, "nearby_block", List.of(), Map.of("block", block, "radius", String.valueOf(radius))));
                return this;
            }

            public Builder distanceFromSpawn(String id, int min, int max) {
                rules.add(new StructureRule(id, "distance_from_spawn", List.of(), Map.of("min", String.valueOf(min), "max", String.valueOf(max))));
                return this;
            }

            public Builder customData(String id, String predicate) {
                rules.add(new StructureRule(id, "custom_data", List.of(), Map.of("predicate", predicate)));
                return this;
            }

            public StructureRuleGraph build() {
                return new StructureRuleGraph(id, rules, rootRule);
            }
        }
    }

    @NexusStable(since = "1.3")
    public record StructureRule(String id, String operator, List<String> children, Map<String, String> parameters) {
        public StructureRule {
            children = List.copyOf(children);
            parameters = Map.copyOf(parameters);
        }
    }

    @NexusStable(since = "1.3")
    public record RuleExplanation(ResourceLocation graphId, boolean passed, List<String> traces) {
        public RuleExplanation {
            traces = List.copyOf(traces);
        }
    }

    @NexusStable(since = "1.3")
    public record TemplateCapture(ResourceLocation id,
                                  BlockPos min,
                                  BlockPos max,
                                  String anchor,
                                  List<String> palette,
                                  List<String> lootMarkers,
                                  List<String> entityMarkers,
                                  List<String> dataMarkers) {
        public TemplateCapture {
            palette = List.copyOf(palette);
            lootMarkers = List.copyOf(lootMarkers);
            entityMarkers = List.copyOf(entityMarkers);
            dataMarkers = List.copyOf(dataMarkers);
        }
    }

    @NexusStable(since = "1.3")
    public record ProceduralStructure(ResourceLocation id,
                                      List<String> rooms,
                                      List<String> corridors,
                                      List<String> passes,
                                      Map<String, String> settings) {
        public ProceduralStructure {
            rooms = List.copyOf(rooms);
            corridors = List.copyOf(corridors);
            passes = List.copyOf(passes);
            settings = Map.copyOf(settings);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("id", id.toString());
            json.add("rooms", strings(rooms));
            json.add("corridors", strings(corridors));
            json.add("passes", strings(passes));
            JsonObject settingJson = new JsonObject();
            settings.forEach(settingJson::addProperty);
            json.add("settings", settingJson);
            return json;
        }

        public static final class Builder {
            private final ResourceLocation id;
            private final List<String> rooms = new ArrayList<>();
            private final List<String> corridors = new ArrayList<>();
            private final List<String> passes = new ArrayList<>();
            private final Map<String, String> settings = new LinkedHashMap<>();

            private Builder(ResourceLocation id) {
                this.id = id;
            }

            public Builder roomGraph(String name) {
                rooms.add("room_graph:" + name);
                return this;
            }

            public Builder room(String name) {
                rooms.add(name);
                return this;
            }

            public Builder corridor(String name) {
                corridors.add(name);
                return this;
            }

            public Builder tower(int floors) {
                settings.put("tower_floors", String.valueOf(floors));
                return this;
            }

            public Builder cave(String profile) {
                settings.put("cave_profile", profile);
                return this;
            }

            public Builder ruin(String decayProfile) {
                settings.put("ruin_decay", decayProfile);
                return this;
            }

            public Builder cluster(String profile) {
                settings.put("cluster_profile", profile);
                return this;
            }

            public Builder bossArena(String arena) {
                settings.put("boss_arena", arena);
                return this;
            }

            public Builder dimensionGateway(String dimension) {
                settings.put("dimension_gateway", dimension);
                return this;
            }

            public Builder decorationPass(String pass) {
                passes.add(pass);
                return this;
            }

            public ProceduralStructure build() {
                return new ProceduralStructure(id, rooms, corridors, passes, settings);
            }
        }
    }

    @NexusStable(since = "1.3")
    public record StructureSimulationReport(String name,
                                            int seeds,
                                            Map<String, Double> metrics,
                                            List<String> failedPlacementReasons,
                                            List<String> sections) {
        public StructureSimulationReport {
            metrics = Map.copyOf(metrics);
            failedPlacementReasons = List.copyOf(failedPlacementReasons);
            sections = List.copyOf(sections);
        }

        public String summary() {
            return name + " seeds=" + seeds + ", metrics=" + metrics.size() + ", failures=" + failedPlacementReasons.size();
        }
    }

    @NexusStable(since = "1.3")
    public record JigsawGraphDebug(ResourceLocation poolId,
                                   List<String> pieces,
                                   List<String> connectors,
                                   List<String> placementFailures) {
        public JigsawGraphDebug {
            pieces = List.copyOf(pieces);
            connectors = List.copyOf(connectors);
            placementFailures = List.copyOf(placementFailures);
        }

        public static JigsawGraphDebug from(JigsawPoolPreview pool) {
            return new JigsawGraphDebug(pool.id(),
                    pool.elements().stream().map(JigsawElementPreview::template).toList(),
                    pool.elements().stream().map(JigsawElementPreview::connector).toList(),
                    pool.depthLimit() < 1 ? List.of("depth limit below 1") : List.of());
        }
    }

    @NexusStable(since = "1.3")
    public record StructureValidationReport(List<String> errors, List<String> warnings) {
        public StructureValidationReport {
            errors = List.copyOf(errors);
            warnings = List.copyOf(warnings);
        }

        public boolean passed() {
            return errors.isEmpty();
        }

        public String summary() {
            return "Structure validation " + (passed() ? "passed" : "failed")
                    + " with " + errors.size() + " errors and " + warnings.size() + " warnings";
        }
    }

    private static JsonArray strings(Collection<String> values) {
        JsonArray array = new JsonArray();
        values.forEach(array::add);
        return array;
    }

    private NexusStructures() {
    }
}

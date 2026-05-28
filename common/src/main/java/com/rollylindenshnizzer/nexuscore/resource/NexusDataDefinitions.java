package com.rollylindenshnizzer.nexuscore.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.data.NexusData;
import com.rollylindenshnizzer.nexuscore.entity.NexusEntityDefinition;
import com.rollylindenshnizzer.nexuscore.entity.NexusEntityDefinitions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@NexusStable(since = "1.3")
public final class NexusDataDefinitions {
    private static final Map<ResourceLocation, DefinitionRegistry<?>> REGISTRIES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, DataDrivenEntityDefinition> ENTITY_DEFINITIONS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, DataDrivenWorldgenDefinition> WORLDGEN_DEFINITIONS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, DataDrivenContentDefinition> CONTENT_DEFINITIONS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, DefinitionSchemaDescriptor> SCHEMAS = new LinkedHashMap<>();
    private static final Set<ResourceLocation> REGISTERED_RUNTIME_ENTITIES = new HashSet<>();

    public static <T> DefinitionRegistry<T> registry(String namespace,
                                                     String path,
                                                     JsonSchema schema,
                                                     Function<JsonObject, T> decoder) {
        DefinitionRegistry<T> registry = new DefinitionRegistry<>(NexusIds.id(namespace, path), schema, decoder);
        REGISTRIES.put(registry.id(), registry);
        return registry;
    }

    public static Collection<DefinitionRegistry<?>> registries() {
        return List.copyOf(REGISTRIES.values());
    }

    public static DataDrivenEntityDefinition.Builder entity(String namespace, String path) {
        return new DataDrivenEntityDefinition.Builder(NexusIds.id(namespace, path));
    }

    public static DataDrivenEntityDefinition registerEntity(DataDrivenEntityDefinition definition) {
        ENTITY_DEFINITIONS.put(definition.id(), definition);
        return definition;
    }

    public static Collection<DataDrivenEntityDefinition> entityDefinitions() {
        return List.copyOf(ENTITY_DEFINITIONS.values());
    }

    public static DataDrivenWorldgenDefinition.Builder worldgen(String namespace, String path) {
        return new DataDrivenWorldgenDefinition.Builder(NexusIds.id(namespace, path));
    }

    public static DataDrivenWorldgenDefinition registerWorldgen(DataDrivenWorldgenDefinition definition) {
        WORLDGEN_DEFINITIONS.put(definition.id(), definition);
        return definition;
    }

    public static Collection<DataDrivenWorldgenDefinition> worldgenDefinitions() {
        return List.copyOf(WORLDGEN_DEFINITIONS.values());
    }

    public static DataDrivenContentDefinition.Builder content(String namespace, String path, DefinitionType type) {
        return new DataDrivenContentDefinition.Builder(NexusIds.id(namespace, path), type);
    }

    public static DataDrivenContentDefinition registerContent(DataDrivenContentDefinition definition) {
        CONTENT_DEFINITIONS.put(definition.id(), definition);
        SCHEMAS.putIfAbsent(definition.schema().id(), definition.schema());
        return definition;
    }

    public static Collection<DataDrivenContentDefinition> contentDefinitions() {
        return List.copyOf(CONTENT_DEFINITIONS.values());
    }

    public static Collection<DefinitionSchemaDescriptor> schemas() {
        return List.copyOf(SCHEMAS.values());
    }

    public static int registerRuntimeEntities(String modId) {
        int count = 0;
        for (DataDrivenEntityDefinition definition : ENTITY_DEFINITIONS.values()) {
            if (!definition.id().getNamespace().equals(modId) || !REGISTERED_RUNTIME_ENTITIES.add(definition.id())) {
                continue;
            }
            NexusEntityDefinition entityDefinition = NexusEntityDefinition.builder(definition.id(), definition.category())
                    .sized(definition.width(), definition.height())
                    .tracking(definition.trackingRange(), definition.updateInterval())
                    .build();
            NexusEntityDefinitions.registerType(entityDefinition, NexusDataDrivenEntity::new);
            count++;
        }
        return count;
    }

    public static int writeRuntimeDataTo(NexusData.DataPlan plan) {
        int count = 0;
        for (DataDrivenEntityDefinition definition : ENTITY_DEFINITIONS.values()) {
            if (definition.id().getNamespace().equals(plan.modId())) {
                definition.writeTo(plan);
                count++;
            }
        }
        for (DataDrivenWorldgenDefinition definition : WORLDGEN_DEFINITIONS.values()) {
            if (definition.id().getNamespace().equals(plan.modId())) {
                definition.writeTo(plan);
                count++;
            }
        }
        for (DataDrivenContentDefinition definition : CONTENT_DEFINITIONS.values()) {
            if (definition.id().getNamespace().equals(plan.modId())) {
                definition.writeTo(plan);
                count++;
            }
        }
        return count;
    }

    public static DefinitionValidationReport validateAll() {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        for (DefinitionRegistry<?> registry : REGISTRIES.values()) {
            DefinitionValidationReport report = registry.lastReport();
            report.errors().forEach(error -> errors.add(registry.id() + ": " + error));
            report.warnings().forEach(warning -> warnings.add(registry.id() + ": " + warning));
        }
        for (DataDrivenContentDefinition definition : CONTENT_DEFINITIONS.values()) {
            if (definition.data().toString().length() > 256_000) {
                warnings.add(definition.id() + " is very large and may impact reload time");
            }
            if (definition.type().requiresRegistryReference() && !definition.data().has("id")) {
                errors.add(definition.id() + " is missing id");
            }
            DefinitionSafetyReport safety = DefinitionSafetyReport.inspect(definition.id(), definition.data());
            errors.addAll(safety.errors());
            warnings.addAll(safety.warnings());
        }
        return new DefinitionValidationReport(errors, warnings);
    }

    public static String debugSummary() {
        DefinitionValidationReport report = validateAll();
        return "definitionRegistries=" + REGISTRIES.size() + ", errors=" + report.errors().size()
                + ", warnings=" + report.warnings().size()
                + ", dataDrivenEntities=" + ENTITY_DEFINITIONS.size()
                + ", dataDrivenWorldgen=" + WORLDGEN_DEFINITIONS.size()
                + ", contentDefinitions=" + CONTENT_DEFINITIONS.size();
    }

    @NexusStable(since = "1.3")
    public static final class DefinitionRegistry<T> {
        private final ResourceLocation id;
        private final JsonSchema schema;
        private final Function<JsonObject, T> decoder;
        private final Map<ResourceLocation, T> values = new LinkedHashMap<>();
        private DefinitionValidationReport lastReport = new DefinitionValidationReport(List.of(), List.of());

        private DefinitionRegistry(ResourceLocation id, JsonSchema schema, Function<JsonObject, T> decoder) {
            this.id = id;
            this.schema = schema;
            this.decoder = decoder;
        }

        public ResourceLocation id() {
            return id;
        }

        public DefinitionReloadReport reload(Map<ResourceLocation, JsonObject> data) {
            values.clear();
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            for (Map.Entry<ResourceLocation, JsonObject> entry : data.entrySet()) {
                List<String> schemaErrors = schema.validate(entry.getValue());
                if (!schemaErrors.isEmpty()) {
                    schemaErrors.forEach(error -> errors.add(entry.getKey() + ": " + error));
                    continue;
                }
                DefinitionSafetyReport safety = DefinitionSafetyReport.inspect(entry.getKey(), entry.getValue());
                warnings.addAll(safety.warnings());
                errors.addAll(safety.errors());
                if (!safety.errors().isEmpty()) {
                    continue;
                }
                values.put(entry.getKey(), decoder.apply(entry.getValue()));
            }
            lastReport = new DefinitionValidationReport(errors, warnings);
            return new DefinitionReloadReport(id, values.size(), lastReport);
        }

        public Optional<T> get(ResourceLocation valueId) {
            return Optional.ofNullable(values.get(valueId));
        }

        public Map<ResourceLocation, T> values() {
            return Map.copyOf(values);
        }

        public DefinitionValidationReport lastReport() {
            return lastReport;
        }

        public DefinitionSchemaDoc schemaDoc() {
            return new DefinitionSchemaDoc(id, schema);
        }
    }

    @NexusStable(since = "1.3")
    public record DefinitionValidationReport(List<String> errors, List<String> warnings) {
        public DefinitionValidationReport {
            errors = List.copyOf(errors);
            warnings = List.copyOf(warnings);
        }

        public boolean passed() {
            return errors.isEmpty();
        }
    }

    @NexusStable(since = "1.3")
    public record DefinitionReloadReport(ResourceLocation registryId, int loaded, DefinitionValidationReport validation) {
        public boolean passed() {
            return validation.passed();
        }
    }

    @NexusStable(since = "1.3")
    public record DefinitionSafetyReport(List<String> errors, List<String> warnings) {
        public DefinitionSafetyReport {
            errors = List.copyOf(errors);
            warnings = List.copyOf(warnings);
        }

        public static DefinitionSafetyReport inspect(ResourceLocation id, JsonObject json) {
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            if (json.has("unsafe_command")) {
                errors.add(id + " uses unsafe_command, which is blocked by default");
            }
            if (json.has("client_script")) {
                warnings.add(id + " declares client_script; this requires explicit development mode");
            }
            if (json.toString().length() > 256_000) {
                warnings.add(id + " is very large and may impact reload time");
            }
            return new DefinitionSafetyReport(errors, warnings);
        }
    }

    @NexusStable(since = "1.3")
    public record DefinitionSchemaDoc(ResourceLocation registryId, JsonSchema schema) {
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("registry", registryId.toString());
            json.addProperty("format", "json-schema-lite");
            json.addProperty("note", "NexusCore JsonSchema stores field rules and validates reload input.");
            return json;
        }

        public String toMarkdown() {
            return "# " + registryId + "\n\nThis registry uses NexusCore JsonSchema validation before decoding.";
        }
    }

    @NexusStable(since = "1.3")
    public record DataDrivenPreviewDefinition(ResourceLocation id, String kind, JsonObject json, List<String> generatedFiles) {
        public DataDrivenPreviewDefinition {
            generatedFiles = List.copyOf(generatedFiles);
        }
    }

    @NexusStable(since = "1.3")
    public record DataDrivenEntityDefinition(ResourceLocation id,
                                             MobCategory category,
                                             float width,
                                             float height,
                                             int trackingRange,
                                             int updateInterval,
                                             List<String> goals,
                                             JsonObject data) {
        public DataDrivenEntityDefinition {
            goals = List.copyOf(goals);
            data = data.deepCopy();
        }

        public NexusData.DataPlan writeTo(NexusData.DataPlan plan) {
            JsonObject json = data.deepCopy();
            json.addProperty("id", id.toString());
            json.addProperty("category", category.getName());
            json.addProperty("width", width);
            json.addProperty("height", height);
            json.addProperty("tracking_range", trackingRange);
            json.addProperty("update_interval", updateInterval);
            json.add("goals", strings(goals));
            plan.data("nexuscore/entity_definition/" + id.getPath() + ".json", json);
            plan.translation(NexusIds.translationKey("entity", id), NexusIds.humanName(id.getPath()));
            return plan;
        }

        public static final class Builder {
            private final ResourceLocation id;
            private MobCategory category = MobCategory.MISC;
            private float width = 0.6F;
            private float height = 1.8F;
            private int trackingRange = 64;
            private int updateInterval = 3;
            private final List<String> goals = new ArrayList<>();
            private final JsonObject data = new JsonObject();

            private Builder(ResourceLocation id) {
                this.id = id;
            }

            public Builder category(MobCategory category) {
                this.category = category;
                return this;
            }

            public Builder sized(float width, float height) {
                this.width = width;
                this.height = height;
                return this;
            }

            public Builder tracking(int trackingRange, int updateInterval) {
                this.trackingRange = trackingRange;
                this.updateInterval = updateInterval;
                return this;
            }

            public Builder goal(String goalId) {
                this.goals.add(goalId);
                return this;
            }

            public Builder property(String key, String value) {
                this.data.addProperty(key, value);
                return this;
            }

            public DataDrivenEntityDefinition build() {
                return new DataDrivenEntityDefinition(id, category, width, height, trackingRange, updateInterval, goals, data);
            }
        }
    }

    @NexusStable(since = "1.3")
    public record DataDrivenWorldgenDefinition(ResourceLocation id,
                                               String kind,
                                               Map<String, JsonObject> generatedData,
                                               List<String> biomeSelectors) {
        public DataDrivenWorldgenDefinition {
            generatedData = Map.copyOf(generatedData);
            biomeSelectors = List.copyOf(biomeSelectors);
        }

        public NexusData.DataPlan writeTo(NexusData.DataPlan plan) {
            for (Map.Entry<String, JsonObject> entry : generatedData.entrySet()) {
                plan.data(entry.getKey(), entry.getValue());
            }
            JsonObject index = new JsonObject();
            index.addProperty("id", id.toString());
            index.addProperty("kind", kind);
            index.add("biome_selectors", strings(biomeSelectors));
            plan.data("nexuscore/worldgen_definition/" + id.getPath() + ".json", index);
            return plan;
        }

        public static final class Builder {
            private final ResourceLocation id;
            private String kind = "feature";
            private final Map<String, JsonObject> generatedData = new LinkedHashMap<>();
            private final List<String> biomeSelectors = new ArrayList<>();

            private Builder(ResourceLocation id) {
                this.id = id;
            }

            public Builder kind(String kind) {
                this.kind = kind;
                return this;
            }

            public Builder data(String relativePath, JsonObject json) {
                this.generatedData.put(relativePath, json.deepCopy());
                return this;
            }

            public Builder biome(String selector) {
                this.biomeSelectors.add(selector);
                return this;
            }

            public DataDrivenWorldgenDefinition build() {
                return new DataDrivenWorldgenDefinition(id, kind, generatedData, biomeSelectors);
            }
        }
    }

    @NexusStable(since = "1.3")
    public record DataDrivenContentDefinition(ResourceLocation id,
                                              DefinitionType type,
                                              JsonObject data,
                                              DefinitionSchemaDescriptor schema,
                                              List<String> generatedDocs,
                                              List<String> crossReferences) {
        public DataDrivenContentDefinition {
            data = data.deepCopy();
            generatedDocs = List.copyOf(generatedDocs);
            crossReferences = List.copyOf(crossReferences);
        }

        public NexusData.DataPlan writeTo(NexusData.DataPlan plan) {
            JsonObject json = data.deepCopy();
            json.addProperty("schema_version", "1.3");
            json.addProperty("id", id.toString());
            json.addProperty("type", type.serializedName());
            json.add("cross_references", strings(crossReferences));
            plan.data("nexuscore/" + type.folder() + "/" + id.getPath() + ".json", json);
            for (String doc : generatedDocs) {
                JsonObject page = new JsonObject();
                page.addProperty("title", NexusIds.humanName(doc));
                page.addProperty("definition", id.toString());
                page.addProperty("definition_type", type.serializedName());
                plan.data("nexuscore/guide/" + doc + ".json", page);
            }
            plan.translation(NexusIds.translationKey(type.serializedName(), id), NexusIds.humanName(id.getPath()));
            return plan;
        }

        public static final class Builder {
            private final ResourceLocation id;
            private final DefinitionType type;
            private final JsonObject data = new JsonObject();
            private DefinitionSchemaDescriptor schema;
            private final List<String> generatedDocs = new ArrayList<>();
            private final List<String> crossReferences = new ArrayList<>();

            private Builder(ResourceLocation id, DefinitionType type) {
                this.id = id;
                this.type = type;
                this.schema = DefinitionSchemaDescriptor.forType(type);
            }

            public Builder property(String key, String value) {
                data.addProperty(key, value);
                return this;
            }

            public Builder property(String key, Number value) {
                data.addProperty(key, value);
                return this;
            }

            public Builder property(String key, boolean value) {
                data.addProperty(key, value);
                return this;
            }

            public Builder json(String key, JsonObject value) {
                data.add(key, value.deepCopy());
                return this;
            }

            public Builder reference(String id) {
                crossReferences.add(id);
                return this;
            }

            public Builder guidePage(String page) {
                generatedDocs.add(page);
                return this;
            }

            public Builder schema(DefinitionSchemaDescriptor schema) {
                this.schema = schema;
                return this;
            }

            public DataDrivenContentDefinition build() {
                return new DataDrivenContentDefinition(id, type, data, schema, generatedDocs, crossReferences);
            }
        }
    }

    @NexusStable(since = "1.3")
    public enum DefinitionType {
        SIMPLE_ITEM("item_definition", true),
        SIMPLE_BLOCK("block_definition", true),
        SIMPLE_BLOCK_ENTITY("block_entity_definition", true),
        SIMPLE_ENTITY("entity_definition", true),
        PROJECTILE("projectile_definition", true),
        FOOD("food_definition", false),
        TOOL("tool_definition", false),
        ARMOR_PRESET("armor_preset", false),
        MACHINE("machine_definition", true),
        MULTIBLOCK("multiblock", true),
        RITUAL("ritual", true),
        STRUCTURE("structure", true),
        BIOME_MODIFIER("biome_modifier", true),
        DIMENSION_SETTINGS("dimension_settings", true),
        AUTOMATION_NETWORK("automation_network", true),
        PROGRESSION_ENTRY("progression", true),
        GUIDEBOOK_PAGE("guide", true);

        private final String folder;
        private final boolean requiresRegistryReference;

        DefinitionType(String folder, boolean requiresRegistryReference) {
            this.folder = folder;
            this.requiresRegistryReference = requiresRegistryReference;
        }

        public String folder() {
            return folder;
        }

        public boolean requiresRegistryReference() {
            return requiresRegistryReference;
        }

        public String serializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    @NexusStable(since = "1.3")
    public record DefinitionSchemaDescriptor(ResourceLocation id,
                                             DefinitionType type,
                                             String schemaVersion,
                                             Map<String, String> fieldDescriptions,
                                             Map<String, List<String>> enums,
                                             List<String> examples,
                                             Map<String, String> deprecations) {
        public DefinitionSchemaDescriptor {
            fieldDescriptions = Map.copyOf(fieldDescriptions);
            enums = Map.copyOf(enums);
            examples = List.copyOf(examples);
            deprecations = Map.copyOf(deprecations);
        }

        public static DefinitionSchemaDescriptor forType(DefinitionType type) {
            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("id", "Registry id for this data-driven definition.");
            fields.put("type", "NexusCore definition type: " + type.serializedName());
            fields.put("schema_version", "Schema version metadata used by validation and IDE autocomplete.");
            return new DefinitionSchemaDescriptor(NexusIds.id("nexuscore", type.folder()), type, "1.3",
                    fields, Map.of("definition_type", List.of(type.serializedName())), List.of(type.folder() + "/example.json"), Map.of());
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("$schema", "https://json-schema.org/draft/2020-12/schema");
            json.addProperty("title", type.serializedName());
            json.addProperty("schema_version", schemaVersion);
            JsonObject properties = new JsonObject();
            fieldDescriptions.forEach((field, description) -> {
                JsonObject property = new JsonObject();
                property.addProperty("description", description);
                properties.add(field, property);
            });
            json.add("properties", properties);
            JsonObject enumJson = new JsonObject();
            enums.forEach((field, values) -> enumJson.add(field, strings(values)));
            json.add("enums", enumJson);
            json.add("examples", strings(examples));
            JsonObject deprecated = new JsonObject();
            deprecations.forEach(deprecated::addProperty);
            json.add("deprecations", deprecated);
            return json;
        }
    }

    @NexusStable(since = "1.3")
    public record CrossFileValidationReport(List<String> missingReferences,
                                            List<String> unusedDefinitions,
                                            List<String> unsafeDefinitions,
                                            List<String> suggestions) {
        public CrossFileValidationReport {
            missingReferences = List.copyOf(missingReferences);
            unusedDefinitions = List.copyOf(unusedDefinitions);
            unsafeDefinitions = List.copyOf(unsafeDefinitions);
            suggestions = List.copyOf(suggestions);
        }

        public boolean passed() {
            return missingReferences.isEmpty() && unsafeDefinitions.isEmpty();
        }
    }

    public static CrossFileValidationReport validateCrossReferences(Collection<ResourceLocation> knownIds) {
        Set<String> known = knownIds.stream().map(ResourceLocation::toString).collect(java.util.stream.Collectors.toSet());
        List<String> missing = new ArrayList<>();
        List<String> unused = new ArrayList<>();
        List<String> unsafe = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        for (DataDrivenContentDefinition definition : CONTENT_DEFINITIONS.values()) {
            for (String reference : definition.crossReferences()) {
                if (!known.contains(reference) && !CONTENT_DEFINITIONS.containsKey(ResourceLocation.tryParse(reference))) {
                    missing.add(definition.id() + " references missing " + reference);
                    suggestions.add("Check spelling for " + reference + " or add an optional-mod guard.");
                }
            }
            DefinitionSafetyReport report = DefinitionSafetyReport.inspect(definition.id(), definition.data());
            unsafe.addAll(report.errors());
        }
        for (ResourceLocation id : CONTENT_DEFINITIONS.keySet()) {
            boolean referenced = CONTENT_DEFINITIONS.values().stream()
                    .anyMatch(definition -> definition.crossReferences().contains(id.toString()));
            if (!referenced) {
                unused.add(id.toString());
            }
        }
        return new CrossFileValidationReport(missing, unused, unsafe, suggestions);
    }

    public static JsonObject machinePreview(ResourceLocation id, String machineType, int energy, int ticks) {
        JsonObject json = new JsonObject();
        json.addProperty("id", id.toString());
        json.addProperty("type", machineType);
        json.addProperty("energy", energy);
        json.addProperty("ticks", ticks);
        JsonArray inputs = new JsonArray();
        json.add("inputs", inputs);
        JsonArray outputs = new JsonArray();
        json.add("outputs", outputs);
        return json;
    }

    private NexusDataDefinitions() {
    }

    private static JsonArray strings(Collection<String> values) {
        JsonArray array = new JsonArray();
        values.forEach(array::add);
        return array;
    }
}

package com.rollylindenshnizzer.nexuscore.multiblock;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.api.NexusIncubating;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.data.NexusData;
import com.rollylindenshnizzer.nexuscore.world.NexusWorldEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@NexusIncubating(since = "1.3")
public final class NexusMultiblocks {
    private static final Map<ResourceLocation, MultiblockDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static final AssemblyManager ASSEMBLY = new AssemblyManager();
    private static PerformanceLimits performanceLimits = PerformanceLimits.defaults();
    private static SafetyPolicy safetyPolicy = SafetyPolicy.defaults();

    public static MultiblockDefinition.Builder create(String namespace, String path) {
        return create(NexusIds.id(namespace, path));
    }

    public static MultiblockDefinition.Builder create(ResourceLocation id) {
        return new MultiblockDefinition.Builder(id, false);
    }

    public static MultiblockDefinition.ScalableBuilder scalable(String namespace, String path) {
        return scalable(NexusIds.id(namespace, path));
    }

    public static MultiblockDefinition.ScalableBuilder scalable(ResourceLocation id) {
        return new MultiblockDefinition.ScalableBuilder(id);
    }

    public static MultiblockDefinition register(MultiblockDefinition definition) {
        DEFINITIONS.put(definition.id(), definition);
        return definition;
    }

    public static Collection<MultiblockDefinition> definitions() {
        return List.copyOf(DEFINITIONS.values());
    }

    public static Optional<MultiblockDefinition> find(ResourceLocation id) {
        return Optional.ofNullable(DEFINITIONS.get(id));
    }

    public static AssemblyManager assembly() {
        return ASSEMBLY;
    }

    public static PerformanceLimits performanceLimits() {
        return performanceLimits;
    }

    public static void performanceLimits(PerformanceLimits limits) {
        performanceLimits = limits == null ? PerformanceLimits.defaults() : limits;
    }

    public static SafetyPolicy safetyPolicy() {
        return safetyPolicy;
    }

    public static void safetyPolicy(SafetyPolicy policy) {
        safetyPolicy = policy == null ? SafetyPolicy.defaults() : policy;
    }

    public static NexusData.DataPlan writeAllTo(NexusData.DataPlan plan) {
        for (MultiblockDefinition definition : DEFINITIONS.values()) {
            if (definition.id().getNamespace().equals(plan.modId())) {
                definition.writeTo(plan);
            }
        }
        return plan;
    }

    public static MultiblockValidationReport validate() {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> hints = new ArrayList<>();
        for (MultiblockDefinition definition : DEFINITIONS.values()) {
            MultiblockValidationReport report = definition.validate();
            report.errors().forEach(error -> errors.add(definition.id() + ": " + error));
            report.warnings().forEach(warning -> warnings.add(definition.id() + ": " + warning));
            report.hints().forEach(hint -> hints.add(definition.id() + ": " + hint));
        }
        return new MultiblockValidationReport(errors, warnings, hints);
    }

    public static String debugSummary() {
        MultiblockValidationReport report = validate();
        return "multiblocks=" + DEFINITIONS.size()
                + ", assembled=" + ASSEMBLY.assemblies().size()
                + ", errors=" + report.errors().size()
                + ", warnings=" + report.warnings().size();
    }

    @NexusIncubating(since = "1.3")
    public record MultiblockDefinition(ResourceLocation id,
                                       String controller,
                                       List<PatternLayer> layers,
                                       Map<Character, BlockMatcher> matchers,
                                       Map<Character, PartRole> roles,
                                       List<PatternVariant> variants,
                                       boolean rotatable,
                                       boolean mirrorable,
                                       boolean automaticAssembly,
                                       boolean manualAssembly,
                                       boolean scalable,
                                       Optional<ScalableRules> scalableRules,
                                       MultiblockConstraints constraints,
                                       Optional<MachineIntegration> machine,
                                       UiDefinition ui,
                                       SafetyPolicy safety,
                                       List<String> documentationTags) {
        public MultiblockDefinition {
            layers = List.copyOf(layers);
            matchers = Map.copyOf(matchers);
            roles = Map.copyOf(roles);
            variants = List.copyOf(variants);
            scalableRules = scalableRules == null ? Optional.empty() : scalableRules;
            machine = machine == null ? Optional.empty() : machine;
            documentationTags = List.copyOf(documentationTags);
        }

        public MultiblockValidationReport validate() {
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            List<String> hints = new ArrayList<>();
            if (controller == null || controller.isBlank()) {
                errors.add("controller is required");
            }
            if (!scalable && layers.isEmpty()) {
                errors.add("fixed multiblocks require at least one pattern layer");
            }
            int width = -1;
            int depth = -1;
            for (PatternLayer layer : layers) {
                if (layer.rows().isEmpty()) {
                    errors.add("pattern layer " + layer.index() + " is empty");
                    continue;
                }
                if (width == -1) {
                    width = layer.width();
                    depth = layer.depth();
                }
                if (layer.width() != width || layer.depth() != depth) {
                    errors.add("all pattern layers must have the same width and depth");
                }
                for (String row : layer.rows()) {
                    for (int i = 0; i < row.length(); i++) {
                        char key = row.charAt(i);
                        if (key != ' ' && !matchers.containsKey(key)) {
                            errors.add("pattern key '" + key + "' has no matcher");
                        }
                    }
                }
            }
            if (scalableRules.isPresent()) {
                ScalableRules rules = scalableRules.get();
                if (rules.minWidth() > rules.maxWidth() || rules.minHeight() > rules.maxHeight() || rules.minDepth() > rules.maxDepth()) {
                    errors.add("scalable min size must not exceed max size");
                }
                if (rules.maxVolume() > performanceLimits.maxMultiblockVolume()) {
                    warnings.add("scalable max volume " + rules.maxVolume() + " exceeds configured limit " + performanceLimits.maxMultiblockVolume());
                }
            }
            if (machine.isPresent() && roles.values().stream().noneMatch(role -> role == PartRole.INPUT_HATCH || role == PartRole.ITEM_PORT)) {
                hints.add("machine multiblock has no explicit item input role");
            }
            for (BlockMatcher matcher : matchers.values()) {
                if (matcher.predicate().map(MultiblockPredicate::expensive).orElse(false)) {
                    warnings.add("matcher '" + matcher.key() + "' uses an expensive predicate; cache or throttle validation");
                }
            }
            return new MultiblockValidationReport(errors, warnings, hints);
        }

        public NexusData.DataPlan writeTo(NexusData.DataPlan plan) {
            plan.data("nexuscore/multiblock/" + id.getPath() + ".json", toJson());
            plan.translation(NexusIds.translationKey("multiblock", id), NexusIds.humanName(id.getPath()));
            for (PartRole role : roles.values()) {
                plan.translation("nexuscore.multiblock.role." + role.serializedName(), role.displayName());
            }
            return plan;
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("schema_version", "1.3");
            json.addProperty("id", id.toString());
            json.addProperty("controller", controller);
            json.addProperty("rotatable", rotatable);
            json.addProperty("mirrorable", mirrorable);
            json.addProperty("automatic_assembly", automaticAssembly);
            json.addProperty("manual_assembly", manualAssembly);
            json.addProperty("scalable", scalable);
            JsonArray layerArray = new JsonArray();
            for (PatternLayer layer : layers) {
                layerArray.add(layer.toJson());
            }
            json.add("layers", layerArray);
            JsonObject matcherJson = new JsonObject();
            for (Map.Entry<Character, BlockMatcher> entry : matchers.entrySet()) {
                matcherJson.add(String.valueOf(entry.getKey()), entry.getValue().toJson());
            }
            json.add("matchers", matcherJson);
            JsonObject roleJson = new JsonObject();
            for (Map.Entry<Character, PartRole> entry : roles.entrySet()) {
                roleJson.addProperty(String.valueOf(entry.getKey()), entry.getValue().serializedName());
            }
            json.add("roles", roleJson);
            JsonArray variantArray = new JsonArray();
            variants.forEach(variant -> variantArray.add(variant.toJson()));
            json.add("variants", variantArray);
            scalableRules.ifPresent(rules -> json.add("scalable_rules", rules.toJson()));
            json.add("constraints", constraints.toJson());
            machine.ifPresent(value -> json.add("machine", value.toJson()));
            json.add("ui", ui.toJson());
            json.add("safety", safety.toJson());
            json.add("documentation_tags", strings(documentationTags));
            return json;
        }

        public List<PreviewLayer> previewLayers() {
            List<PreviewLayer> previews = new ArrayList<>();
            for (PatternLayer layer : layers) {
                previews.add(new PreviewLayer(layer.index(), layer.rows(), roles));
            }
            return previews;
        }

        public DebugOverlay debugOverlay(BlockPos controllerPos, MultiblockValidationReport report) {
            return new DebugOverlay(id, controllerPos, report.errors(), report.warnings(), report.hints(), previewLayers());
        }

        public String javaBuilderSample() {
            StringBuilder builder = new StringBuilder();
            builder.append("NexusMultiblocks.create(NexusIds.id(\"")
                    .append(id.getNamespace())
                    .append("\", \"")
                    .append(id.getPath())
                    .append("\"))\n")
                    .append("    .controller(\"")
                    .append(controller)
                    .append("\")\n");
            for (PatternLayer layer : layers) {
                builder.append("    .aisle(");
                for (int i = 0; i < layer.rows().size(); i++) {
                    if (i > 0) {
                        builder.append(", ");
                    }
                    builder.append('"').append(layer.rows().get(i)).append('"');
                }
                builder.append(")\n");
            }
            for (Map.Entry<Character, BlockMatcher> entry : matchers.entrySet()) {
                builder.append("    .where('")
                        .append(entry.getKey())
                        .append("', \"")
                        .append(entry.getValue().value())
                        .append("\")\n");
            }
            if (rotatable) {
                builder.append("    .rotatable()\n");
            }
            if (mirrorable) {
                builder.append("    .mirrorable()\n");
            }
            builder.append("    .build();");
            return builder.toString();
        }

        public static class Builder {
            protected final ResourceLocation id;
            protected String controller = "";
            protected final List<PatternLayer> layers = new ArrayList<>();
            protected final Map<Character, BlockMatcher> matchers = new LinkedHashMap<>();
            protected final Map<Character, PartRole> roles = new LinkedHashMap<>();
            protected final List<PatternVariant> variants = new ArrayList<>();
            protected boolean rotatable;
            protected boolean mirrorable;
            protected boolean automaticAssembly = true;
            protected boolean manualAssembly = true;
            protected boolean scalable;
            protected Optional<ScalableRules> scalableRules = Optional.empty();
            protected MultiblockConstraints constraints = MultiblockConstraints.defaults();
            protected Optional<MachineIntegration> machine = Optional.empty();
            protected UiDefinition ui = UiDefinition.generated();
            protected SafetyPolicy safety = SafetyPolicy.defaults();
            protected final List<String> documentationTags = new ArrayList<>();

            protected Builder(ResourceLocation id, boolean scalable) {
                this.id = id;
                this.scalable = scalable;
            }

            public Builder controller(Object controller) {
                this.controller = matcherValue(controller);
                return this;
            }

            public Builder aisle(String... rows) {
                this.layers.add(new PatternLayer(layers.size(), List.of(rows)));
                return this;
            }

            public Builder layer(List<String> rows) {
                this.layers.add(new PatternLayer(layers.size(), rows));
                return this;
            }

            public Builder where(char key, Object matcher) {
                this.matchers.put(key, matcherFor(key, matcher, false));
                return this;
            }

            public Builder optional(char key, Object matcher) {
                this.matchers.put(key, matcherFor(key, matcher, true));
                return this;
            }

            public Builder tag(char key, String tagId) {
                this.matchers.put(key, BlockMatcher.tag(key, tagId, false));
                return this;
            }

            public Builder state(char key, String blockState) {
                this.matchers.put(key, BlockMatcher.state(key, blockState, false));
                return this;
            }

            public Builder predicate(char key, String description, boolean expensive) {
                this.matchers.put(key, BlockMatcher.predicate(key, new MultiblockPredicate(description, expensive, List.of()), false));
                return this;
            }

            public Builder air(char key) {
                this.matchers.put(key, BlockMatcher.air(key, false));
                return this;
            }

            public Builder fluid(char key, String fluidId) {
                this.matchers.put(key, BlockMatcher.fluid(key, fluidId, false));
                return this;
            }

            public Builder role(char key, PartRole role) {
                this.roles.put(key, role);
                return this;
            }

            public Builder port(char key, PartRole role) {
                this.roles.put(key, role);
                this.matchers.computeIfPresent(key, (ignored, matcher) -> matcher.withRole(role));
                return this;
            }

            public Builder variant(String name, List<PatternLayer> layers) {
                this.variants.add(new PatternVariant(name, layers));
                return this;
            }

            public Builder rotatable() {
                this.rotatable = true;
                return this;
            }

            public Builder mirrorable() {
                this.mirrorable = true;
                return this;
            }

            public Builder automaticAssembly(boolean enabled) {
                this.automaticAssembly = enabled;
                return this;
            }

            public Builder manualAssembly(boolean enabled) {
                this.manualAssembly = enabled;
                return this;
            }

            public Builder constraints(MultiblockConstraints constraints) {
                this.constraints = constraints;
                return this;
            }

            public Builder maxVolume(int maxVolume) {
                this.constraints = constraints.maxVolume(maxVolume);
                return this;
            }

            public Builder machine(MachineIntegration integration) {
                this.machine = Optional.of(integration);
                return this;
            }

            public Builder ui(UiDefinition ui) {
                this.ui = ui;
                return this;
            }

            public Builder safety(SafetyPolicy safety) {
                this.safety = safety;
                return this;
            }

            public Builder documentationTag(String tag) {
                this.documentationTags.add(tag);
                return this;
            }

            public MultiblockDefinition build() {
                return new MultiblockDefinition(id, controller, layers, matchers, roles, variants, rotatable, mirrorable,
                        automaticAssembly, manualAssembly, scalable, scalableRules, constraints, machine, ui, safety, documentationTags);
            }
        }

        public static final class ScalableBuilder extends Builder {
            private String frame = "";
            private String wall = "";
            private String interior = "minecraft:air";
            private int minWidth = 3;
            private int minHeight = 3;
            private int minDepth = 3;
            private int maxWidth = 9;
            private int maxHeight = 9;
            private int maxDepth = 9;
            private int shellThickness = 1;
            private boolean hollow = true;
            private final List<CalculatedStat> stats = new ArrayList<>();

            private ScalableBuilder(ResourceLocation id) {
                super(id, true);
            }

            @Override
            public ScalableBuilder controller(Object controller) {
                super.controller(controller);
                return this;
            }

            public ScalableBuilder frame(Object frame) {
                this.frame = matcherValue(frame);
                return this;
            }

            public ScalableBuilder wall(Object wall) {
                this.wall = matcherValue(wall);
                return this;
            }

            public ScalableBuilder interior(Object interior) {
                this.interior = matcherValue(interior);
                return this;
            }

            public ScalableBuilder minSize(int width, int height, int depth) {
                this.minWidth = width;
                this.minHeight = height;
                this.minDepth = depth;
                return this;
            }

            public ScalableBuilder maxSize(int width, int height, int depth) {
                this.maxWidth = width;
                this.maxHeight = height;
                this.maxDepth = depth;
                return this;
            }

            public ScalableBuilder shellThickness(int shellThickness) {
                this.shellThickness = Math.max(1, shellThickness);
                return this;
            }

            public ScalableBuilder hollow(boolean hollow) {
                this.hollow = hollow;
                return this;
            }

            public ScalableBuilder stat(String name, Function<ScalableContext, Number> calculator) {
                ScalableContext min = new ScalableContext(minWidth, minHeight, minDepth);
                Number preview = calculator.apply(min);
                this.stats.add(new CalculatedStat(name, "java_callback", preview.doubleValue()));
                return this;
            }

            public ScalableBuilder stat(String name, String expression, double previewValue) {
                this.stats.add(new CalculatedStat(name, expression, previewValue));
                return this;
            }

            @Override
            public MultiblockDefinition build() {
                int maxVolume = maxWidth * maxHeight * maxDepth;
                this.scalableRules = Optional.of(new ScalableRules(frame, wall, interior, minWidth, minHeight, minDepth,
                        maxWidth, maxHeight, maxDepth, shellThickness, hollow, maxVolume, stats));
                this.constraints = constraints.maxVolume(maxVolume);
                if (!frame.isBlank()) {
                    this.matchers.putIfAbsent('F', BlockMatcher.block('F', frame, false).withRole(PartRole.CASING));
                }
                if (!wall.isBlank()) {
                    this.matchers.putIfAbsent('W', BlockMatcher.block('W', wall, false).withRole(PartRole.CASING));
                }
                this.matchers.putIfAbsent('I', BlockMatcher.block('I', interior, false));
                this.roles.putIfAbsent('F', PartRole.CASING);
                this.roles.putIfAbsent('W', PartRole.CASING);
                return super.build();
            }
        }
    }

    @NexusIncubating(since = "1.3")
    public record PatternLayer(int index, List<String> rows) {
        public PatternLayer {
            rows = List.copyOf(rows);
        }

        public int width() {
            return rows.stream().mapToInt(String::length).max().orElse(0);
        }

        public int depth() {
            return rows.size();
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("index", index);
            json.add("rows", strings(rows));
            return json;
        }
    }

    @NexusIncubating(since = "1.3")
    public record PatternVariant(String name, List<PatternLayer> layers) {
        public PatternVariant {
            layers = List.copyOf(layers);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("name", name);
            JsonArray array = new JsonArray();
            layers.forEach(layer -> array.add(layer.toJson()));
            json.add("layers", array);
            return json;
        }
    }

    @NexusIncubating(since = "1.3")
    public record BlockMatcher(char key,
                               MatchKind kind,
                               String value,
                               boolean optional,
                               Optional<PartRole> role,
                               Optional<MultiblockPredicate> predicate) {
        public BlockMatcher {
            role = role == null ? Optional.empty() : role;
            predicate = predicate == null ? Optional.empty() : predicate;
        }

        public static BlockMatcher block(char key, String id, boolean optional) {
            return new BlockMatcher(key, MatchKind.BLOCK, id, optional, Optional.empty(), Optional.empty());
        }

        public static BlockMatcher tag(char key, String tag, boolean optional) {
            String value = tag.startsWith("#") ? tag : "#" + tag;
            return new BlockMatcher(key, MatchKind.TAG, value, optional, Optional.empty(), Optional.empty());
        }

        public static BlockMatcher state(char key, String state, boolean optional) {
            return new BlockMatcher(key, MatchKind.STATE, state, optional, Optional.empty(), Optional.empty());
        }

        public static BlockMatcher air(char key, boolean optional) {
            return new BlockMatcher(key, MatchKind.AIR, "minecraft:air", optional, Optional.empty(), Optional.empty());
        }

        public static BlockMatcher fluid(char key, String fluid, boolean optional) {
            return new BlockMatcher(key, MatchKind.FLUID, fluid, optional, Optional.empty(), Optional.empty());
        }

        public static BlockMatcher predicate(char key, MultiblockPredicate predicate, boolean optional) {
            return new BlockMatcher(key, MatchKind.PREDICATE, predicate.description(), optional, Optional.empty(), Optional.of(predicate));
        }

        public BlockMatcher withRole(PartRole role) {
            return new BlockMatcher(key, kind, value, optional, Optional.of(role), predicate);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("kind", kind.serializedName());
            json.addProperty("value", value);
            json.addProperty("optional", optional);
            role.ifPresent(value -> json.addProperty("role", value.serializedName()));
            predicate.ifPresent(value -> json.add("predicate", value.toJson()));
            return json;
        }
    }

    @NexusIncubating(since = "1.3")
    public record MultiblockPredicate(String description, boolean expensive, List<String> hints) {
        public MultiblockPredicate {
            hints = List.copyOf(hints);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("description", description);
            json.addProperty("expensive", expensive);
            json.add("hints", strings(hints));
            return json;
        }
    }

    @NexusIncubating(since = "1.3")
    public enum MatchKind {
        BLOCK,
        TAG,
        STATE,
        AIR,
        FLUID,
        PREDICATE,
        ANY;

        public String serializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    @NexusIncubating(since = "1.3")
    public enum PartRole {
        CONTROLLER,
        CASING,
        INPUT_HATCH,
        OUTPUT_HATCH,
        ENERGY_PORT,
        FLUID_PORT,
        ITEM_PORT,
        MAINTENANCE_PORT,
        DECORATIVE,
        RITUAL_FOCUS,
        PORTAL_FRAME;

        public String serializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public String displayName() {
            return NexusIds.humanName(serializedName());
        }
    }

    @NexusIncubating(since = "1.3")
    public record MultiblockConstraints(int minWidth,
                                        int minHeight,
                                        int minDepth,
                                        int maxWidth,
                                        int maxHeight,
                                        int maxDepth,
                                        int requiredAirSpaces,
                                        int requiredFluidSpaces,
                                        boolean requireSingleController,
                                        boolean allowMirroredControllerMigration,
                                        int maxVolume) {
        public static MultiblockConstraints defaults() {
            return new MultiblockConstraints(1, 1, 1, 16, 16, 16, 0, 0, true, true, 4_096);
        }

        public MultiblockConstraints maxVolume(int maxVolume) {
            return new MultiblockConstraints(minWidth, minHeight, minDepth, maxWidth, maxHeight, maxDepth,
                    requiredAirSpaces, requiredFluidSpaces, requireSingleController, allowMirroredControllerMigration, maxVolume);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("min_width", minWidth);
            json.addProperty("min_height", minHeight);
            json.addProperty("min_depth", minDepth);
            json.addProperty("max_width", maxWidth);
            json.addProperty("max_height", maxHeight);
            json.addProperty("max_depth", maxDepth);
            json.addProperty("required_air_spaces", requiredAirSpaces);
            json.addProperty("required_fluid_spaces", requiredFluidSpaces);
            json.addProperty("require_single_controller", requireSingleController);
            json.addProperty("allow_mirrored_controller_migration", allowMirroredControllerMigration);
            json.addProperty("max_volume", maxVolume);
            return json;
        }
    }

    @NexusIncubating(since = "1.3")
    public record ScalableRules(String frame,
                                String wall,
                                String interior,
                                int minWidth,
                                int minHeight,
                                int minDepth,
                                int maxWidth,
                                int maxHeight,
                                int maxDepth,
                                int shellThickness,
                                boolean hollow,
                                int maxVolume,
                                List<CalculatedStat> stats) {
        public ScalableRules {
            stats = List.copyOf(stats);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("frame", frame);
            json.addProperty("wall", wall);
            json.addProperty("interior", interior);
            json.addProperty("min_width", minWidth);
            json.addProperty("min_height", minHeight);
            json.addProperty("min_depth", minDepth);
            json.addProperty("max_width", maxWidth);
            json.addProperty("max_height", maxHeight);
            json.addProperty("max_depth", maxDepth);
            json.addProperty("shell_thickness", shellThickness);
            json.addProperty("hollow", hollow);
            json.addProperty("max_volume", maxVolume);
            JsonArray array = new JsonArray();
            stats.forEach(stat -> array.add(stat.toJson()));
            json.add("stats", array);
            return json;
        }
    }

    @NexusIncubating(since = "1.3")
    public record ScalableContext(int width, int height, int depth) {
        public int volume() {
            return width * height * depth;
        }

        public int innerVolume() {
            return Math.max(0, width - 2) * Math.max(0, height - 2) * Math.max(0, depth - 2);
        }
    }

    @NexusIncubating(since = "1.3")
    public record CalculatedStat(String name, String expression, double previewValue) {
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("name", name);
            json.addProperty("expression", expression);
            json.addProperty("preview_value", previewValue);
            return json;
        }
    }

    @NexusIncubating(since = "1.3")
    public record MachineIntegration(String recipeFamily,
                                     long energyBuffer,
                                     long fluidTank,
                                     int upgradeSlots,
                                     List<String> routedPorts,
                                     List<String> shutdownConditions,
                                     Map<String, Double> stats) {
        public MachineIntegration {
            routedPorts = List.copyOf(routedPorts);
            shutdownConditions = List.copyOf(shutdownConditions);
            stats = Map.copyOf(stats);
        }

        public static MachineIntegration processing(Object recipeFamily) {
            return new MachineIntegration(matcherValue(recipeFamily), 100_000, 16_000, 0, List.of(), List.of(), Map.of());
        }

        public MachineIntegration energyBuffer(long amount) {
            return new MachineIntegration(recipeFamily, amount, fluidTank, upgradeSlots, routedPorts, shutdownConditions, stats);
        }

        public MachineIntegration fluidTank(long amount) {
            return new MachineIntegration(recipeFamily, energyBuffer, amount, upgradeSlots, routedPorts, shutdownConditions, stats);
        }

        public MachineIntegration upgradeSlots(int slots) {
            return new MachineIntegration(recipeFamily, energyBuffer, fluidTank, slots, routedPorts, shutdownConditions, stats);
        }

        public MachineIntegration routedPort(String port) {
            List<String> values = new ArrayList<>(routedPorts);
            values.add(port);
            return new MachineIntegration(recipeFamily, energyBuffer, fluidTank, upgradeSlots, values, shutdownConditions, stats);
        }

        public MachineIntegration shutdownCondition(String condition) {
            List<String> values = new ArrayList<>(shutdownConditions);
            values.add(condition);
            return new MachineIntegration(recipeFamily, energyBuffer, fluidTank, upgradeSlots, routedPorts, values, stats);
        }

        public MachineIntegration stat(String name, double value) {
            Map<String, Double> values = new LinkedHashMap<>(stats);
            values.put(name, value);
            return new MachineIntegration(recipeFamily, energyBuffer, fluidTank, upgradeSlots, routedPorts, shutdownConditions, values);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("recipe_family", recipeFamily);
            json.addProperty("energy_buffer", energyBuffer);
            json.addProperty("fluid_tank", fluidTank);
            json.addProperty("upgrade_slots", upgradeSlots);
            json.add("routed_ports", strings(routedPorts));
            json.add("shutdown_conditions", strings(shutdownConditions));
            JsonObject statsJson = new JsonObject();
            stats.forEach(statsJson::addProperty);
            json.add("stats", statsJson);
            return json;
        }
    }

    @NexusIncubating(since = "1.3")
    public record UiDefinition(boolean generatedScreen,
                               boolean structurePreview,
                               boolean ghostBlocks,
                               boolean layerSelector,
                               List<String> gauges,
                               List<String> recipeViewerHooks) {
        public UiDefinition {
            gauges = List.copyOf(gauges);
            recipeViewerHooks = List.copyOf(recipeViewerHooks);
        }

        public static UiDefinition generated() {
            return new UiDefinition(true, true, true, true,
                    List.of("assembled_state", "energy", "fluid", "items", "heat", "pressure", "stability"),
                    List.of("jei", "emi", "rei"));
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("generated_screen", generatedScreen);
            json.addProperty("structure_preview", structurePreview);
            json.addProperty("ghost_blocks", ghostBlocks);
            json.addProperty("layer_selector", layerSelector);
            json.add("gauges", strings(gauges));
            json.add("recipe_viewer_hooks", strings(recipeViewerHooks));
            return json;
        }
    }

    @NexusIncubating(since = "1.3")
    public record SafetyPolicy(boolean serverAuthoritative,
                               boolean respectClaims,
                               boolean requireOwnerForManualEdit,
                               boolean allowDangerousEffects,
                               int maxRevalidationCost) {
        public static SafetyPolicy defaults() {
            return new SafetyPolicy(true, true, true, false, 4_096);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("server_authoritative", serverAuthoritative);
            json.addProperty("respect_claims", respectClaims);
            json.addProperty("require_owner_for_manual_edit", requireOwnerForManualEdit);
            json.addProperty("allow_dangerous_effects", allowDangerousEffects);
            json.addProperty("max_revalidation_cost", maxRevalidationCost);
            return json;
        }
    }

    @NexusIncubating(since = "1.3")
    public record PerformanceLimits(int maxMultiblockVolume,
                                    int maxCachedShapes,
                                    int maxAffectedBlocksPerRevalidation,
                                    int expensivePredicateWarningMicros) {
        public static PerformanceLimits defaults() {
            return new PerformanceLimits(4_096, 2_048, 512, 500);
        }
    }

    @NexusIncubating(since = "1.3")
    public record MultiblockValidationReport(List<String> errors, List<String> warnings, List<String> hints) {
        public MultiblockValidationReport {
            errors = List.copyOf(errors);
            warnings = List.copyOf(warnings);
            hints = List.copyOf(hints);
        }

        public boolean passed() {
            return errors.isEmpty();
        }

        public String summary() {
            return "Multiblock validation " + (passed() ? "passed" : "failed")
                    + " with " + errors.size() + " errors, " + warnings.size() + " warnings, " + hints.size() + " hints";
        }
    }

    @NexusIncubating(since = "1.3")
    public static final class AssemblyManager {
        private final Map<AssemblyKey, MultiblockAssembly> assemblies = new LinkedHashMap<>();
        private final List<AssemblyEvent> eventLog = new ArrayList<>();

        public List<AssemblyResult> handleWorldEvent(NexusWorldEvent event) {
            if (!event.serverSide()) {
                return List.of();
            }
            return switch (event.kind()) {
                case BLOCK_BREAK -> {
                    if (event.hasBlockContext()) {
                        invalidateAt(event.dimension(), event.pos(), "world_break:" + event.hook());
                    }
                    yield List.of();
                }
                case BLOCK_PLACE -> {
                    if (!event.hasBlockContext()) {
                        yield List.of();
                    }
                    invalidateAt(event.dimension(), event.pos(), "world_place:" + event.hook());
                    yield tryAssembleFromWorld(event.level(), event.pos(), event.player(), false, event.hook());
                }
                case BLOCK_INTERACT -> {
                    if (!event.hasBlockContext()) {
                        yield List.of();
                    }
                    yield tryAssembleFromWorld(event.level(), event.pos(), event.player(), true, event.hook());
                }
                case LEVEL_TICK_START, LEVEL_TICK_END, SERVER_TICK_START, SERVER_TICK_END -> {
                    trimCaches();
                    yield List.of();
                }
                default -> List.of();
            };
        }

        public List<AssemblyResult> tryAssembleFromWorld(Level level, BlockPos controllerPos, Player player, boolean manual, String source) {
            if (level == null || controllerPos == null) {
                return List.of();
            }
            BlockState controllerState = level.getBlockState(controllerPos);
            String dimension = level.dimension().location().toString();
            List<AssemblyResult> results = new ArrayList<>();
            for (MultiblockDefinition definition : DEFINITIONS.values()) {
                if (manual && !definition.manualAssembly()) {
                    continue;
                }
                if (!manual && !definition.automaticAssembly()) {
                    continue;
                }
                if (!controllerMatches(definition, controllerState)) {
                    continue;
                }
                AssemblyKey key = new AssemblyKey(definition.id(), dimension, controllerPos);
                if (assemblies.containsKey(key)) {
                    continue;
                }
                List<PartMetadata> parts = definition.scalable()
                        ? scalableControllerPart(controllerPos, controllerState, player, source)
                        : matchFixedPattern(level, controllerPos, definition, player, source).orElse(List.of());
                if (parts.isEmpty()) {
                    continue;
                }
                results.add(assemble(definition.id(), dimension, controllerPos, parts));
            }
            return List.copyOf(results);
        }

        private void invalidateAt(String dimension, BlockPos changed, String reason) {
            List<AssemblyKey> invalidated = assemblies.entrySet().stream()
                    .filter(entry -> entry.getKey().dimension().equals(dimension))
                    .filter(entry -> entry.getKey().controllerPos().equals(changed)
                            || entry.getValue().parts().stream().anyMatch(part -> part.pos().equals(changed)))
                    .map(Map.Entry::getKey)
                    .toList();
            for (AssemblyKey key : invalidated) {
                MultiblockAssembly previous = assemblies.remove(key);
                if (previous != null) {
                    eventLog.add(new AssemblyEvent(key, AssemblyState.DISASSEMBLED, reason, Instant.now()));
                }
            }
            trimCaches();
        }

        private static List<PartMetadata> scalableControllerPart(BlockPos controllerPos, BlockState controllerState, Player player, String source) {
            Map<String, String> metadata = new LinkedHashMap<>();
            metadata.put("runtime", "world_hook");
            metadata.put("source", source == null ? "unknown" : source);
            metadata.put("scalable", "true");
            if (player != null) {
                metadata.put("player", player.getUUID().toString());
            }
            return List.of(new PartMetadata(controllerPos, PartRole.CONTROLLER, blockId(controllerState), false, false, false, metadata));
        }

        private static Optional<List<PartMetadata>> matchFixedPattern(Level level,
                                                                      BlockPos controllerPos,
                                                                      MultiblockDefinition definition,
                                                                      Player player,
                                                                      String source) {
            if (definition.layers().isEmpty()) {
                return Optional.empty();
            }
            int width = definition.layers().getFirst().width();
            int depth = definition.layers().getFirst().depth();
            for (PatternTransform transform : transforms(definition)) {
                for (PatternOffset controllerOffset : controllerOffsets(definition)) {
                    BlockPos transformedControllerOffset = transform.apply(controllerOffset.x(), controllerOffset.y(), controllerOffset.z(), width, depth);
                    BlockPos origin = controllerPos.subtract(transformedControllerOffset);
                    Optional<List<PartMetadata>> match = matchAt(level, origin, controllerPos, definition, transform, width, depth, player, source);
                    if (match.isPresent()) {
                        return match;
                    }
                }
            }
            return Optional.empty();
        }

        private static Optional<List<PartMetadata>> matchAt(Level level,
                                                            BlockPos origin,
                                                            BlockPos controllerPos,
                                                            MultiblockDefinition definition,
                                                            PatternTransform transform,
                                                            int width,
                                                            int depth,
                                                            Player player,
                                                            String source) {
            List<PartMetadata> parts = new ArrayList<>();
            for (PatternLayer layer : definition.layers()) {
                for (int z = 0; z < layer.depth(); z++) {
                    String row = layer.rows().get(z);
                    for (int x = 0; x < width; x++) {
                        char key = x < row.length() ? row.charAt(x) : ' ';
                        if (key == ' ') {
                            continue;
                        }
                        BlockMatcher matcher = definition.matchers().get(key);
                        if (matcher == null) {
                            return Optional.empty();
                        }
                        BlockPos worldPos = origin.offset(transform.apply(x, layer.index(), z, width, depth));
                        BlockState state = level.getBlockState(worldPos);
                        if (!matches(level, worldPos, state, matcher)) {
                            if (matcher.optional()) {
                                continue;
                            }
                            return Optional.empty();
                        }
                        PartRole role = roleFor(definition, key, matcher, worldPos.equals(controllerPos));
                        Map<String, String> metadata = new LinkedHashMap<>();
                        metadata.put("pattern_key", Character.toString(key));
                        metadata.put("runtime", "world_hook");
                        metadata.put("source", source == null ? "unknown" : source);
                        metadata.put("transform", transform.serializedName());
                        if (player != null) {
                            metadata.put("player", player.getUUID().toString());
                        }
                        parts.add(new PartMetadata(worldPos, role, blockId(state), exposesItems(role), exposesEnergy(role), exposesFluids(role), metadata));
                    }
                }
            }
            if (parts.stream().noneMatch(part -> part.pos().equals(controllerPos))) {
                return Optional.empty();
            }
            return Optional.of(parts);
        }

        private static List<PatternOffset> controllerOffsets(MultiblockDefinition definition) {
            List<PatternOffset> offsets = new ArrayList<>();
            for (PatternLayer layer : definition.layers()) {
                for (int z = 0; z < layer.depth(); z++) {
                    String row = layer.rows().get(z);
                    for (int x = 0; x < row.length(); x++) {
                        char key = row.charAt(x);
                        if (key == ' ') {
                            continue;
                        }
                        BlockMatcher matcher = definition.matchers().get(key);
                        PartRole role = definition.roles().get(key);
                        boolean roleController = role == PartRole.CONTROLLER || matcher != null && matcher.role().orElse(null) == PartRole.CONTROLLER;
                        boolean valueController = matcher != null && !definition.controller().isBlank() && matcher.value().equals(definition.controller());
                        if (roleController || valueController) {
                            offsets.add(new PatternOffset(x, layer.index(), z));
                        }
                    }
                }
            }
            if (offsets.isEmpty()) {
                offsets.add(new PatternOffset(0, 0, 0));
            }
            return offsets;
        }

        private static List<PatternTransform> transforms(MultiblockDefinition definition) {
            List<PatternTransform> transforms = new ArrayList<>();
            transforms.add(PatternTransform.NONE);
            if (definition.rotatable()) {
                transforms.add(PatternTransform.CLOCKWISE);
                transforms.add(PatternTransform.HALF);
                transforms.add(PatternTransform.COUNTER_CLOCKWISE);
            }
            if (definition.mirrorable()) {
                transforms.add(PatternTransform.MIRROR);
                if (definition.rotatable()) {
                    transforms.add(PatternTransform.MIRROR_CLOCKWISE);
                    transforms.add(PatternTransform.MIRROR_HALF);
                    transforms.add(PatternTransform.MIRROR_COUNTER_CLOCKWISE);
                }
            }
            return transforms;
        }

        private static boolean controllerMatches(MultiblockDefinition definition, BlockState state) {
            if (state == null) {
                return false;
            }
            if (!definition.controller().isBlank() && matchesValue(state, definition.controller())) {
                return true;
            }
            for (BlockMatcher matcher : definition.matchers().values()) {
                if (matcher.role().orElse(null) == PartRole.CONTROLLER && matches(null, BlockPos.ZERO, state, matcher)) {
                    return true;
                }
            }
            return false;
        }

        private static PartRole roleFor(MultiblockDefinition definition, char key, BlockMatcher matcher, boolean controller) {
            if (controller) {
                return PartRole.CONTROLLER;
            }
            PartRole explicit = definition.roles().get(key);
            if (explicit != null) {
                return explicit;
            }
            return matcher.role().orElse(PartRole.CASING);
        }

        private static boolean matches(Level level, BlockPos pos, BlockState state, BlockMatcher matcher) {
            if (state == null) {
                return matcher.optional();
            }
            return switch (matcher.kind()) {
                case BLOCK -> blockId(state).equals(matcher.value());
                case TAG -> matchesTag(state, matcher.value());
                case STATE -> matchesValue(state, matcher.value());
                case AIR -> state.isAir();
                case FLUID -> matchesFluid(state.getFluidState(), matcher.value());
                case PREDICATE, ANY -> true;
            };
        }

        private static boolean matchesValue(BlockState state, String value) {
            if (value == null || value.isBlank()) {
                return false;
            }
            if (value.startsWith("#")) {
                return matchesTag(state, value);
            }
            String blockId = blockId(state);
            return blockId.equals(value) || state.toString().equals(value) || state.toString().contains(value);
        }

        private static boolean matchesTag(BlockState state, String value) {
            String raw = value.startsWith("#") ? value.substring(1) : value;
            ResourceLocation id = ResourceLocation.tryParse(raw);
            return id != null && state.is(TagKey.create(Registries.BLOCK, id));
        }

        private static boolean matchesFluid(FluidState state, String value) {
            if (state == null || state.isEmpty()) {
                return false;
            }
            return BuiltInRegistries.FLUID.getKey(state.getType()).toString().equals(value);
        }

        private static String blockId(BlockState state) {
            return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        }

        private static boolean exposesItems(PartRole role) {
            return role == PartRole.INPUT_HATCH || role == PartRole.OUTPUT_HATCH || role == PartRole.ITEM_PORT;
        }

        private static boolean exposesEnergy(PartRole role) {
            return role == PartRole.ENERGY_PORT;
        }

        private static boolean exposesFluids(PartRole role) {
            return role == PartRole.FLUID_PORT;
        }

        private record PatternOffset(int x, int y, int z) {
        }

        private enum PatternTransform {
            NONE(false, Rotation.NONE),
            CLOCKWISE(false, Rotation.CLOCKWISE),
            HALF(false, Rotation.HALF),
            COUNTER_CLOCKWISE(false, Rotation.COUNTER_CLOCKWISE),
            MIRROR(true, Rotation.NONE),
            MIRROR_CLOCKWISE(true, Rotation.CLOCKWISE),
            MIRROR_HALF(true, Rotation.HALF),
            MIRROR_COUNTER_CLOCKWISE(true, Rotation.COUNTER_CLOCKWISE);

            private final boolean mirrored;
            private final Rotation rotation;

            PatternTransform(boolean mirrored, Rotation rotation) {
                this.mirrored = mirrored;
                this.rotation = rotation;
            }

            BlockPos apply(int x, int y, int z, int width, int depth) {
                int mx = mirrored ? width - 1 - x : x;
                return switch (rotation) {
                    case NONE -> new BlockPos(mx, y, z);
                    case CLOCKWISE -> new BlockPos(depth - 1 - z, y, mx);
                    case HALF -> new BlockPos(width - 1 - mx, y, depth - 1 - z);
                    case COUNTER_CLOCKWISE -> new BlockPos(z, y, width - 1 - mx);
                };
            }

            String serializedName() {
                return name().toLowerCase(Locale.ROOT);
            }
        }

        private enum Rotation {
            NONE,
            CLOCKWISE,
            HALF,
            COUNTER_CLOCKWISE
        }

        public AssemblyResult assemble(ResourceLocation definitionId, String dimension, BlockPos controllerPos, List<PartMetadata> parts) {
            MultiblockDefinition definition = DEFINITIONS.get(definitionId);
            if (definition == null) {
                return AssemblyResult.failed(definitionId, controllerPos, "unknown_definition", List.of("No multiblock registered with id " + definitionId));
            }
            MultiblockValidationReport report = definition.validate();
            if (!report.passed()) {
                return AssemblyResult.failed(definitionId, controllerPos, "definition_invalid", report.errors());
            }
            AssemblyKey key = new AssemblyKey(definitionId, dimension, controllerPos);
            MultiblockAssembly assembly = new MultiblockAssembly(UUID.randomUUID(), key, AssemblyState.ASSEMBLED,
                    List.copyOf(parts), AggregationSnapshot.from(parts), Instant.now(), List.of());
            assemblies.put(key, assembly);
            eventLog.add(AssemblyEvent.assembled(key, "manual_or_auto"));
            trimCaches();
            return AssemblyResult.success(definitionId, controllerPos, "assembled", assembly);
        }

        public AssemblyResult invalidate(ResourceLocation definitionId, String dimension, BlockPos controllerPos, String reason) {
            AssemblyKey key = new AssemblyKey(definitionId, dimension, controllerPos);
            MultiblockAssembly previous = assemblies.remove(key);
            eventLog.add(new AssemblyEvent(key, AssemblyState.DISASSEMBLED, reason, Instant.now()));
            if (previous == null) {
                return AssemblyResult.failed(definitionId, controllerPos, "not_assembled", List.of(reason));
            }
            return AssemblyResult.success(definitionId, controllerPos, reason, previous.disassembled(reason));
        }

        public List<BlockPos> affectedRegion(BlockPos changed, int radius) {
            List<BlockPos> positions = new ArrayList<>();
            int capped = Math.min(Math.max(0, radius), 8);
            for (int x = -capped; x <= capped; x++) {
                for (int y = -capped; y <= capped; y++) {
                    for (int z = -capped; z <= capped; z++) {
                        positions.add(changed.offset(x, y, z));
                        if (positions.size() >= performanceLimits.maxAffectedBlocksPerRevalidation()) {
                            return positions;
                        }
                    }
                }
            }
            return positions;
        }

        public List<MultiblockAssembly> assemblies() {
            return List.copyOf(assemblies.values());
        }

        public List<AssemblyEvent> eventLog() {
            return List.copyOf(eventLog);
        }

        private void trimCaches() {
            while (assemblies.size() > performanceLimits.maxCachedShapes()) {
                AssemblyKey first = assemblies.keySet().iterator().next();
                assemblies.remove(first);
            }
            while (eventLog.size() > 256) {
                eventLog.removeFirst();
            }
        }
    }

    @NexusIncubating(since = "1.3")
    public record AssemblyKey(ResourceLocation definitionId, String dimension, BlockPos controllerPos) {
    }

    @NexusIncubating(since = "1.3")
    public record MultiblockAssembly(UUID instanceId,
                                     AssemblyKey key,
                                     AssemblyState state,
                                     List<PartMetadata> parts,
                                     AggregationSnapshot aggregation,
                                     Instant assembledAt,
                                     List<String> invalidationReasons) {
        public MultiblockAssembly {
            parts = List.copyOf(parts);
            invalidationReasons = List.copyOf(invalidationReasons);
        }

        public MultiblockAssembly disassembled(String reason) {
            List<String> reasons = new ArrayList<>(invalidationReasons);
            reasons.add(reason);
            return new MultiblockAssembly(instanceId, key, AssemblyState.DISASSEMBLED, parts, aggregation, assembledAt, reasons);
        }
    }

    @NexusIncubating(since = "1.3")
    public record AssemblyResult(ResourceLocation definitionId,
                                 BlockPos controllerPos,
                                 boolean assembled,
                                 String reason,
                                 List<String> messages,
                                 Optional<MultiblockAssembly> assembly) {
        public AssemblyResult {
            messages = List.copyOf(messages);
            assembly = assembly == null ? Optional.empty() : assembly;
        }

        public static AssemblyResult success(ResourceLocation definitionId, BlockPos controllerPos, String reason, MultiblockAssembly assembly) {
            return new AssemblyResult(definitionId, controllerPos, true, reason, List.of(), Optional.of(assembly));
        }

        public static AssemblyResult failed(ResourceLocation definitionId, BlockPos controllerPos, String reason, List<String> messages) {
            return new AssemblyResult(definitionId, controllerPos, false, reason, messages, Optional.empty());
        }
    }

    @NexusIncubating(since = "1.3")
    public record AssemblyEvent(AssemblyKey key, AssemblyState state, String reason, Instant createdAt) {
        public static AssemblyEvent assembled(AssemblyKey key, String reason) {
            return new AssemblyEvent(key, AssemblyState.ASSEMBLED, reason, Instant.now());
        }
    }

    @NexusIncubating(since = "1.3")
    public enum AssemblyState {
        ASSEMBLED,
        INVALID,
        DISASSEMBLED
    }

    @NexusIncubating(since = "1.3")
    public record PartMetadata(BlockPos pos,
                               PartRole role,
                               String block,
                               boolean exposesItems,
                               boolean exposesEnergy,
                               boolean exposesFluids,
                               Map<String, String> metadata) {
        public PartMetadata {
            metadata = Map.copyOf(metadata);
        }
    }

    @NexusIncubating(since = "1.3")
    public record AggregationSnapshot(int itemPorts,
                                      int energyPorts,
                                      int fluidPorts,
                                      int inputHatches,
                                      int outputHatches,
                                      int maintenancePorts) {
        public static AggregationSnapshot from(List<PartMetadata> parts) {
            int item = 0;
            int energy = 0;
            int fluid = 0;
            int input = 0;
            int output = 0;
            int maintenance = 0;
            for (PartMetadata part : parts) {
                if (part.exposesItems() || part.role() == PartRole.ITEM_PORT) {
                    item++;
                }
                if (part.exposesEnergy() || part.role() == PartRole.ENERGY_PORT) {
                    energy++;
                }
                if (part.exposesFluids() || part.role() == PartRole.FLUID_PORT) {
                    fluid++;
                }
                if (part.role() == PartRole.INPUT_HATCH) {
                    input++;
                }
                if (part.role() == PartRole.OUTPUT_HATCH) {
                    output++;
                }
                if (part.role() == PartRole.MAINTENANCE_PORT) {
                    maintenance++;
                }
            }
            return new AggregationSnapshot(item, energy, fluid, input, output, maintenance);
        }
    }

    @NexusIncubating(since = "1.3")
    public interface MultiblockController {
        ResourceLocation definitionId();

        BlockPos controllerPos();

        default void onAssembled(MultiblockAssembly assembly) {
        }

        default void onDisassembled(String reason) {
        }

        default int comparatorOutput(MultiblockAssembly assembly) {
            return assembly.state() == AssemblyState.ASSEMBLED ? 15 : 0;
        }

        default String redstoneOutput(MultiblockAssembly assembly) {
            return assembly.state().name().toLowerCase(Locale.ROOT);
        }

        default Optional<PartMetadata> part(BlockPos pos, MultiblockAssembly assembly) {
            return assembly.parts().stream().filter(part -> part.pos().equals(pos)).findFirst();
        }
    }

    @NexusIncubating(since = "1.3")
    public interface MultiblockPart {
        PartRole role();

        default boolean overrideMatch(BlockMatcher matcher) {
            return false;
        }

        default PartStatus status() {
            return new PartStatus(role(), true, List.of());
        }
    }

    @NexusIncubating(since = "1.3")
    public record PartStatus(PartRole role, boolean valid, List<String> messages) {
        public PartStatus {
            messages = List.copyOf(messages);
        }
    }

    @NexusIncubating(since = "1.3")
    public record PreviewLayer(int index, List<String> rows, Map<Character, PartRole> roles) {
        public PreviewLayer {
            rows = List.copyOf(rows);
            roles = Map.copyOf(roles);
        }
    }

    @NexusIncubating(since = "1.3")
    public record DebugOverlay(ResourceLocation definitionId,
                               BlockPos controllerPos,
                               List<String> invalidBlocks,
                               List<String> missingBlocks,
                               List<String> hints,
                               List<PreviewLayer> previewLayers) {
        public DebugOverlay {
            invalidBlocks = List.copyOf(invalidBlocks);
            missingBlocks = List.copyOf(missingBlocks);
            hints = List.copyOf(hints);
            previewLayers = List.copyOf(previewLayers);
        }
    }

    private static BlockMatcher matcherFor(char key, Object matcher, boolean optional) {
        if (matcher instanceof BlockMatcher blockMatcher) {
            return new BlockMatcher(key, blockMatcher.kind(), blockMatcher.value(), optional || blockMatcher.optional(), blockMatcher.role(), blockMatcher.predicate());
        }
        if (matcher instanceof MultiblockPredicate predicate) {
            return BlockMatcher.predicate(key, predicate, optional);
        }
        String value = matcherValue(matcher);
        if (value.equals("minecraft:air") || value.endsWith(".AIR") || value.endsWith(":air")) {
            return BlockMatcher.air(key, optional);
        }
        if (value.startsWith("#")) {
            return BlockMatcher.tag(key, value, optional);
        }
        return BlockMatcher.block(key, value, optional);
    }

    private static String matcherValue(Object value) {
        if (value instanceof ResourceLocation id) {
            return id.toString();
        }
        return String.valueOf(value);
    }

    private static JsonArray strings(Collection<String> values) {
        JsonArray array = new JsonArray();
        values.forEach(array::add);
        return array;
    }

    private NexusMultiblocks() {
    }
}

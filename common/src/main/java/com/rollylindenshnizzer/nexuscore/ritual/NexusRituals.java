package com.rollylindenshnizzer.nexuscore.ritual;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.api.NexusIncubating;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.data.NexusData;
import com.rollylindenshnizzer.nexuscore.world.NexusWorldEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@NexusIncubating(since = "1.3")
public final class NexusRituals {
    private static final Map<ResourceLocation, RitualDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static final RitualRuntime RUNTIME = new RitualRuntime();
    private static RitualLimits limits = RitualLimits.defaults();

    public static RitualDefinition.Builder create(String namespace, String path) {
        return create(NexusIds.id(namespace, path));
    }

    public static RitualDefinition.Builder create(ResourceLocation id) {
        return new RitualDefinition.Builder(id);
    }

    public static RitualDefinition register(RitualDefinition definition) {
        DEFINITIONS.put(definition.id(), definition);
        return definition;
    }

    public static Collection<RitualDefinition> definitions() {
        return List.copyOf(DEFINITIONS.values());
    }

    public static Optional<RitualDefinition> find(ResourceLocation id) {
        return Optional.ofNullable(DEFINITIONS.get(id));
    }

    public static RitualRuntime runtime() {
        return RUNTIME;
    }

    public static RitualLimits limits() {
        return limits;
    }

    public static void limits(RitualLimits limits) {
        NexusRituals.limits = limits == null ? RitualLimits.defaults() : limits;
    }

    public static NexusData.DataPlan writeAllTo(NexusData.DataPlan plan) {
        for (RitualDefinition definition : DEFINITIONS.values()) {
            if (definition.id().getNamespace().equals(plan.modId())) {
                definition.writeTo(plan);
            }
        }
        return plan;
    }

    public static RitualValidationReport validate() {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> hints = new ArrayList<>();
        for (RitualDefinition definition : DEFINITIONS.values()) {
            RitualValidationReport report = definition.validate();
            report.errors().forEach(error -> errors.add(definition.id() + ": " + error));
            report.warnings().forEach(warning -> warnings.add(definition.id() + ": " + warning));
            report.hints().forEach(hint -> hints.add(definition.id() + ": " + hint));
        }
        return new RitualValidationReport(errors, warnings, hints);
    }

    public static String debugSummary() {
        RitualValidationReport report = validate();
        return "rituals=" + DEFINITIONS.size()
                + ", active=" + RUNTIME.active().size()
                + ", errors=" + report.errors().size()
                + ", warnings=" + report.warnings().size();
    }

    @NexusIncubating(since = "1.3")
    public record RitualDefinition(ResourceLocation id,
                                   String center,
                                   List<String> requiredStructures,
                                   List<RitualRequirement> requirements,
                                   List<RitualIngredient> ingredients,
                                   List<RitualEffect> tickEffects,
                                   List<RitualEffect> completeEffects,
                                   List<RitualEffect> failureEffects,
                                   int durationTicks,
                                   int delayTicks,
                                   int cooldownTicks,
                                   boolean manualStart,
                                   boolean automaticStart,
                                   boolean channeled,
                                   boolean playerMaintained,
                                   boolean pauseWhenMissing,
                                   boolean repeatable,
                                   boolean oneTime,
                                   FailurePolicy failurePolicy,
                                   StabilityPolicy stability,
                                   RitualSafetyPolicy safety,
                                   RitualUiFeedback ui,
                                   List<String> documentationTags) {
        public RitualDefinition {
            requiredStructures = List.copyOf(requiredStructures);
            requirements = List.copyOf(requirements);
            ingredients = List.copyOf(ingredients);
            tickEffects = List.copyOf(tickEffects);
            completeEffects = List.copyOf(completeEffects);
            failureEffects = List.copyOf(failureEffects);
            documentationTags = List.copyOf(documentationTags);
        }

        public RitualValidationReport validate() {
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            List<String> hints = new ArrayList<>();
            if (center == null || center.isBlank()) {
                errors.add("center block is required");
            }
            if (durationTicks < 0 || delayTicks < 0 || cooldownTicks < 0) {
                errors.add("duration, delay, and cooldown must be non-negative");
            }
            if (!manualStart && !automaticStart) {
                errors.add("ritual must have manual or automatic start enabled");
            }
            if (requirements.isEmpty() && ingredients.isEmpty() && requiredStructures.isEmpty()) {
                hints.add("ritual has no requirements; it may start too easily");
            }
            for (RitualEffect effect : allEffects()) {
                if (effect.dangerous() && !safety.allowDangerousEffects()) {
                    errors.add("dangerous effect '" + effect.action() + "' requires explicit safety opt-in");
                }
                if (effect.serverOnly() == false && effect.dangerous()) {
                    warnings.add("dangerous effect '" + effect.action() + "' should be server-only");
                }
            }
            if (requirements.stream().anyMatch(RitualRequirement::expensive)) {
                warnings.add("one or more requirements are expensive; runtime checks will be throttled");
            }
            if (requiredStructures.size() > limits.maxStructuresPerRitual()) {
                warnings.add("ritual references many multiblocks/structures");
            }
            return new RitualValidationReport(errors, warnings, hints);
        }

        public List<RitualEffect> allEffects() {
            List<RitualEffect> effects = new ArrayList<>();
            effects.addAll(tickEffects);
            effects.addAll(completeEffects);
            effects.addAll(failureEffects);
            return effects;
        }

        public NexusData.DataPlan writeTo(NexusData.DataPlan plan) {
            plan.data("nexuscore/ritual/" + id.getPath() + ".json", toJson());
            plan.translation(NexusIds.translationKey("ritual", id), NexusIds.humanName(id.getPath()));
            plan.translation("nexuscore.ritual.requirements." + id.getPath().replace('/', '.'), requirementTooltip());
            return plan;
        }

        public String requirementTooltip() {
            List<String> values = new ArrayList<>();
            requiredStructures.forEach(structure -> values.add("structure " + structure));
            requirements.forEach(requirement -> values.add(requirement.type() + " " + requirement.target()));
            ingredients.forEach(ingredient -> values.add(ingredient.type() + " " + ingredient.target() + " x" + ingredient.amount()));
            return values.isEmpty() ? "No requirements" : String.join(", ", values);
        }

        public GuidePageDraft guidePageDraft() {
            return new GuidePageDraft(id, NexusIds.humanName(id.getPath()), requirementTooltip(),
                    completeEffects.stream().map(RitualEffect::action).toList(), documentationTags);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("schema_version", "1.3");
            json.addProperty("id", id.toString());
            json.addProperty("center", center);
            json.add("required_structures", strings(requiredStructures));
            JsonArray requirementArray = new JsonArray();
            requirements.forEach(requirement -> requirementArray.add(requirement.toJson()));
            json.add("requirements", requirementArray);
            JsonArray ingredientArray = new JsonArray();
            ingredients.forEach(ingredient -> ingredientArray.add(ingredient.toJson()));
            json.add("ingredients", ingredientArray);
            json.add("tick_effects", effects(tickEffects));
            json.add("complete_effects", effects(completeEffects));
            json.add("failure_effects", effects(failureEffects));
            json.addProperty("duration_ticks", durationTicks);
            json.addProperty("delay_ticks", delayTicks);
            json.addProperty("cooldown_ticks", cooldownTicks);
            json.addProperty("manual_start", manualStart);
            json.addProperty("automatic_start", automaticStart);
            json.addProperty("channeled", channeled);
            json.addProperty("player_maintained", playerMaintained);
            json.addProperty("pause_when_missing", pauseWhenMissing);
            json.addProperty("repeatable", repeatable);
            json.addProperty("one_time", oneTime);
            json.add("failure_policy", failurePolicy.toJson());
            json.add("stability", stability.toJson());
            json.add("safety", safety.toJson());
            json.add("ui", ui.toJson());
            json.add("documentation_tags", strings(documentationTags));
            return json;
        }

        public static final class Builder {
            private final ResourceLocation id;
            private String center = "";
            private final List<String> requiredStructures = new ArrayList<>();
            private final List<RitualRequirement> requirements = new ArrayList<>();
            private final List<RitualIngredient> ingredients = new ArrayList<>();
            private final List<RitualEffect> tickEffects = new ArrayList<>();
            private final List<RitualEffect> completeEffects = new ArrayList<>();
            private final List<RitualEffect> failureEffects = new ArrayList<>();
            private int durationTicks = 20;
            private int delayTicks;
            private int cooldownTicks;
            private boolean manualStart = true;
            private boolean automaticStart;
            private boolean channeled;
            private boolean playerMaintained;
            private boolean pauseWhenMissing = true;
            private boolean repeatable = true;
            private boolean oneTime;
            private FailurePolicy failurePolicy = FailurePolicy.defaults();
            private StabilityPolicy stability = StabilityPolicy.stable();
            private RitualSafetyPolicy safety = RitualSafetyPolicy.defaults();
            private RitualUiFeedback ui = RitualUiFeedback.defaults();
            private final List<String> documentationTags = new ArrayList<>();

            private Builder(ResourceLocation id) {
                this.id = id;
            }

            public Builder center(Object center) {
                this.center = idValue(center);
                return this;
            }

            public Builder requiresStructure(Object structure) {
                this.requiredStructures.add(idValue(structure));
                return this;
            }

            public Builder requiresItem(Object item, int count) {
                this.ingredients.add(RitualIngredient.item(idValue(item), count, true));
                return this;
            }

            public Builder requiresFluid(Object fluid, long amount) {
                this.ingredients.add(RitualIngredient.fluid(idValue(fluid), amount, true));
                return this;
            }

            public Builder requiresEnergy(long amount) {
                this.ingredients.add(RitualIngredient.energy(amount));
                return this;
            }

            public Builder requiresEntity(Object entity, int count) {
                this.requirements.add(RitualRequirement.entity(idValue(entity), count));
                return this;
            }

            public Builder requiresWeather(NexusWeather weather) {
                this.requirements.add(RitualRequirement.simple(RequirementType.WEATHER, weather.serializedName(), 1));
                return this;
            }

            public Builder requiresMoonPhase(String moonPhase) {
                this.requirements.add(RitualRequirement.simple(RequirementType.MOON_PHASE, moonPhase, 1));
                return this;
            }

            public Builder requiresDimension(Object dimension) {
                this.requirements.add(RitualRequirement.simple(RequirementType.DIMENSION, idValue(dimension), 1));
                return this;
            }

            public Builder requiresBiome(Object biomeOrTag) {
                this.requirements.add(RitualRequirement.simple(RequirementType.BIOME, idValue(biomeOrTag), 1));
                return this;
            }

            public Builder requiresTime(NexusTime time) {
                this.requirements.add(RitualRequirement.simple(RequirementType.TIME, time.serializedName(), 1));
                return this;
            }

            public Builder requiresPermission(String permissionNode) {
                this.requirements.add(RitualRequirement.simple(RequirementType.PERMISSION, permissionNode, 1));
                return this;
            }

            public Builder requiresAdvancement(Object advancement) {
                this.requirements.add(RitualRequirement.simple(RequirementType.ADVANCEMENT, idValue(advancement), 1));
                return this;
            }

            public Builder customRequirement(String serializer, String target, boolean expensive) {
                this.requirements.add(new RitualRequirement(RequirementType.CUSTOM, target, 1, false, List.of(), serializer, expensive));
                return this;
            }

            public Builder ingredient(RitualIngredient ingredient) {
                this.ingredients.add(ingredient);
                return this;
            }

            public Builder requirement(RitualRequirement requirement) {
                this.requirements.add(requirement);
                return this;
            }

            public Builder duration(int ticks) {
                this.durationTicks = ticks;
                return this;
            }

            public Builder durationSeconds(int seconds) {
                this.durationTicks = seconds * 20;
                return this;
            }

            public Builder delay(int ticks) {
                this.delayTicks = ticks;
                return this;
            }

            public Builder cooldown(int ticks) {
                this.cooldownTicks = ticks;
                return this;
            }

            public Builder manualStart(boolean manualStart) {
                this.manualStart = manualStart;
                return this;
            }

            public Builder automaticStart(boolean automaticStart) {
                this.automaticStart = automaticStart;
                return this;
            }

            public Builder channeled(boolean channeled) {
                this.channeled = channeled;
                return this;
            }

            public Builder playerMaintained(boolean playerMaintained) {
                this.playerMaintained = playerMaintained;
                return this;
            }

            public Builder pauseWhenMissing(boolean pauseWhenMissing) {
                this.pauseWhenMissing = pauseWhenMissing;
                return this;
            }

            public Builder repeatable(boolean repeatable) {
                this.repeatable = repeatable;
                return this;
            }

            public Builder oneTime() {
                this.oneTime = true;
                this.repeatable = false;
                return this;
            }

            public Builder onTick(RitualEffect effect) {
                this.tickEffects.add(effect.timing(EffectTiming.TICK));
                return this;
            }

            public Builder onComplete(RitualEffect effect) {
                this.completeEffects.add(effect.timing(EffectTiming.COMPLETE));
                return this;
            }

            public Builder onFailure(RitualEffect effect) {
                this.failureEffects.add(effect.timing(EffectTiming.FAILURE));
                return this;
            }

            public Builder failurePolicy(FailurePolicy failurePolicy) {
                this.failurePolicy = failurePolicy;
                return this;
            }

            public Builder stability(StabilityPolicy stability) {
                this.stability = stability;
                return this;
            }

            public Builder safety(RitualSafetyPolicy safety) {
                this.safety = safety;
                return this;
            }

            public Builder ui(RitualUiFeedback ui) {
                this.ui = ui;
                return this;
            }

            public Builder documentationTag(String tag) {
                this.documentationTags.add(tag);
                return this;
            }

            public RitualDefinition build() {
                return new RitualDefinition(id, center, requiredStructures, requirements, ingredients, tickEffects,
                        completeEffects, failureEffects, durationTicks, delayTicks, cooldownTicks, manualStart,
                        automaticStart, channeled, playerMaintained, pauseWhenMissing, repeatable, oneTime,
                        failurePolicy, stability, safety, ui, documentationTags);
            }
        }
    }

    @NexusIncubating(since = "1.3")
    public record RitualRequirement(RequirementType type,
                                    String target,
                                    long amount,
                                    boolean optional,
                                    List<String> alternatives,
                                    String serializer,
                                    boolean expensive) {
        public RitualRequirement {
            alternatives = List.copyOf(alternatives);
        }

        public static RitualRequirement simple(RequirementType type, String target, long amount) {
            return new RitualRequirement(type, target, amount, false, List.of(), type.serializedName(), false);
        }

        public static RitualRequirement entity(String entity, long count) {
            return simple(RequirementType.ENTITY, entity, count);
        }

        public RitualRequirement asOptional() {
            return new RitualRequirement(type, target, amount, true, alternatives, serializer, expensive);
        }

        public RitualRequirement alternative(String target) {
            List<String> values = new ArrayList<>(alternatives);
            values.add(target);
            return new RitualRequirement(type, this.target, amount, optional, values, serializer, expensive);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("type", type.serializedName());
            json.addProperty("target", target);
            json.addProperty("amount", amount);
            json.addProperty("optional", optional);
            json.add("alternatives", strings(alternatives));
            json.addProperty("serializer", serializer);
            json.addProperty("expensive", expensive);
            return json;
        }
    }

    @NexusIncubating(since = "1.3")
    public enum RequirementType {
        STRUCTURE,
        ITEM,
        FLUID,
        ENERGY,
        ENTITY,
        WEATHER,
        MOON_PHASE,
        DIMENSION,
        BIOME,
        TIME,
        PERMISSION,
        ADVANCEMENT,
        CUSTOM;

        public String serializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    @NexusIncubating(since = "1.3")
    public record RitualIngredient(IngredientType type,
                                   String target,
                                   long amount,
                                   boolean consume,
                                   boolean optional,
                                   List<String> alternatives,
                                   String serializer) {
        public RitualIngredient {
            alternatives = List.copyOf(alternatives);
        }

        public static RitualIngredient item(String item, long count, boolean consume) {
            return new RitualIngredient(IngredientType.ITEM, item, count, consume, false, List.of(), "item_stack");
        }

        public static RitualIngredient fluid(String fluid, long amount, boolean consume) {
            return new RitualIngredient(IngredientType.FLUID, fluid, amount, consume, false, List.of(), "fluid_stack");
        }

        public static RitualIngredient energy(long amount) {
            return new RitualIngredient(IngredientType.ENERGY, "nexuscore:energy", amount, true, false, List.of(), "energy");
        }

        public static RitualIngredient experience(long levels) {
            return new RitualIngredient(IngredientType.EXPERIENCE, "minecraft:levels", levels, true, false, List.of(), "experience");
        }

        public RitualIngredient asOptional() {
            return new RitualIngredient(type, target, amount, consume, true, alternatives, serializer);
        }

        public RitualIngredient alternative(String target) {
            List<String> values = new ArrayList<>(alternatives);
            values.add(target);
            return new RitualIngredient(type, this.target, amount, consume, optional, values, serializer);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("type", type.serializedName());
            json.addProperty("target", target);
            json.addProperty("amount", amount);
            json.addProperty("consume", consume);
            json.addProperty("optional", optional);
            json.add("alternatives", strings(alternatives));
            json.addProperty("serializer", serializer);
            return json;
        }
    }

    @NexusIncubating(since = "1.3")
    public enum IngredientType {
        ITEM,
        FLUID,
        ENERGY,
        EXPERIENCE,
        HEALTH,
        DURABILITY,
        ENTITY,
        BLOCK,
        CUSTOM;

        public String serializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    @NexusIncubating(since = "1.3")
    public record RitualEffect(String action,
                               String target,
                               EffectTiming timing,
                               boolean serverOnly,
                               boolean dangerous,
                               Map<String, String> parameters) {
        public RitualEffect {
            parameters = Map.copyOf(parameters);
        }

        public RitualEffect timing(EffectTiming timing) {
            return new RitualEffect(action, target, timing, serverOnly, dangerous, parameters);
        }

        public RitualEffect parameter(String key, Object value) {
            Map<String, String> values = new LinkedHashMap<>(parameters);
            values.put(key, String.valueOf(value));
            return new RitualEffect(action, target, timing, serverOnly, dangerous, values);
        }

        public RitualEffect asDangerous() {
            return new RitualEffect(action, target, timing, serverOnly, true, parameters);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("action", action);
            json.addProperty("target", target);
            json.addProperty("timing", timing.serializedName());
            json.addProperty("server_only", serverOnly);
            json.addProperty("dangerous", dangerous);
            JsonObject params = new JsonObject();
            parameters.forEach(params::addProperty);
            json.add("parameters", params);
            return json;
        }
    }

    @NexusIncubating(since = "1.3")
    public enum EffectTiming {
        START,
        TICK,
        COMPLETE,
        FAILURE,
        CANCEL;

        public String serializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    @NexusIncubating(since = "1.3")
    public record FailurePolicy(boolean failWhenInterrupted,
                                boolean rollbackSupported,
                                double baseFailureChance,
                                List<String> failureEvents) {
        public FailurePolicy {
            failureEvents = List.copyOf(failureEvents);
        }

        public static FailurePolicy defaults() {
            return new FailurePolicy(true, false, 0.0D, List.of("missing_requirement", "manual_cancel", "unsafe_effect_blocked"));
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("fail_when_interrupted", failWhenInterrupted);
            json.addProperty("rollback_supported", rollbackSupported);
            json.addProperty("base_failure_chance", baseFailureChance);
            json.add("failure_events", strings(failureEvents));
            return json;
        }
    }

    @NexusIncubating(since = "1.3")
    public record StabilityPolicy(boolean enabled,
                                  double startingStability,
                                  double instabilityPerMissingRequirement,
                                  double failureChanceMultiplier,
                                  int destructiveEffectCap) {
        public static StabilityPolicy stable() {
            return new StabilityPolicy(false, 1.0D, 0.0D, 0.0D, 0);
        }

        public static StabilityPolicy enabled(double startingStability) {
            return new StabilityPolicy(true, startingStability, 0.05D, 1.0D, 16);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("enabled", enabled);
            json.addProperty("starting_stability", startingStability);
            json.addProperty("instability_per_missing_requirement", instabilityPerMissingRequirement);
            json.addProperty("failure_chance_multiplier", failureChanceMultiplier);
            json.addProperty("destructive_effect_cap", destructiveEffectCap);
            return json;
        }
    }

    @NexusIncubating(since = "1.3")
    public record RitualSafetyPolicy(boolean serverAuthoritative,
                                     boolean respectClaims,
                                     boolean protectPlayers,
                                     boolean allowDangerousEffects,
                                     int maxAreaBlocks,
                                     int maxEntitySpawns,
                                     int maxTeleportDistance) {
        public static RitualSafetyPolicy defaults() {
            return new RitualSafetyPolicy(true, true, true, false, 256, 16, 16_000);
        }

        public RitualSafetyPolicy dangerousEffectsEnabled() {
            return new RitualSafetyPolicy(serverAuthoritative, respectClaims, protectPlayers, true,
                    maxAreaBlocks, maxEntitySpawns, maxTeleportDistance);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("server_authoritative", serverAuthoritative);
            json.addProperty("respect_claims", respectClaims);
            json.addProperty("protect_players", protectPlayers);
            json.addProperty("allow_dangerous_effects", allowDangerousEffects);
            json.addProperty("max_area_blocks", maxAreaBlocks);
            json.addProperty("max_entity_spawns", maxEntitySpawns);
            json.addProperty("max_teleport_distance", maxTeleportDistance);
            return json;
        }
    }

    @NexusIncubating(since = "1.3")
    public record RitualUiFeedback(boolean progressHud,
                                   boolean altarScreen,
                                   boolean requirementTooltips,
                                   boolean guidebookPreview,
                                   boolean particleRing,
                                   boolean beams,
                                   boolean floatingText,
                                   boolean audioCues) {
        public static RitualUiFeedback defaults() {
            return new RitualUiFeedback(true, true, true, true, true, true, true, true);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("progress_hud", progressHud);
            json.addProperty("altar_screen", altarScreen);
            json.addProperty("requirement_tooltips", requirementTooltips);
            json.addProperty("guidebook_preview", guidebookPreview);
            json.addProperty("particle_ring", particleRing);
            json.addProperty("beams", beams);
            json.addProperty("floating_text", floatingText);
            json.addProperty("audio_cues", audioCues);
            return json;
        }
    }

    @NexusIncubating(since = "1.3")
    public record RitualLimits(int maxRadius,
                               int maxActivePerChunk,
                               int requirementCheckIntervalTicks,
                               int maxStructuresPerRitual,
                               int maxParticlePacketsPerTick) {
        public static RitualLimits defaults() {
            return new RitualLimits(32, 4, 10, 4, 64);
        }
    }

    @NexusIncubating(since = "1.3")
    public record RitualValidationReport(List<String> errors, List<String> warnings, List<String> hints) {
        public RitualValidationReport {
            errors = List.copyOf(errors);
            warnings = List.copyOf(warnings);
            hints = List.copyOf(hints);
        }

        public boolean passed() {
            return errors.isEmpty();
        }

        public String summary() {
            return "Ritual validation " + (passed() ? "passed" : "failed")
                    + " with " + errors.size() + " errors, " + warnings.size() + " warnings, " + hints.size() + " hints";
        }
    }

    @NexusIncubating(since = "1.3")
    public static final class RitualRuntime {
        private final Map<UUID, ActiveRitual> active = new LinkedHashMap<>();
        private final List<TimelineEntry> timeline = new ArrayList<>();

        public List<RitualStartResult> handleWorldEvent(NexusWorldEvent event) {
            if (!event.serverSide()) {
                return List.of();
            }
            return switch (event.kind()) {
                case SERVER_TICK_END -> {
                    tickAll(event.hook());
                    yield List.of();
                }
                case BLOCK_PLACE -> {
                    disruptAt(event, "world_place:" + event.hook());
                    yield tryStartAt(event, false);
                }
                case BLOCK_BREAK -> {
                    disruptAt(event, "world_break:" + event.hook());
                    yield List.of();
                }
                case BLOCK_INTERACT -> tryStartAt(event, true);
                default -> List.of();
            };
        }

        public List<RitualProgress> tickAll(String source) {
            List<UUID> ids = new ArrayList<>(active.keySet());
            List<RitualProgress> progress = new ArrayList<>();
            for (UUID id : ids) {
                progress.add(tick(id));
            }
            if (!progress.isEmpty()) {
                record(new UUID(0L, 0L), "tick_all", source == null ? "world_hook" : source);
            }
            return List.copyOf(progress);
        }

        public List<RitualStartResult> tryStartAt(NexusWorldEvent event, boolean manual) {
            if (!event.hasBlockContext()) {
                return List.of();
            }
            List<RitualStartResult> results = new ArrayList<>();
            for (RitualDefinition definition : DEFINITIONS.values()) {
                if (manual && !definition.manualStart()) {
                    continue;
                }
                if (!manual && !definition.automaticStart()) {
                    continue;
                }
                if (!centerMatches(definition, event)) {
                    continue;
                }
                boolean alreadyActive = active.values().stream()
                        .anyMatch(activeRitual -> activeRitual.definition().id().equals(definition.id())
                                && activeRitual.context().dimension().equals(event.dimension())
                                && activeRitual.context().center().equals(event.pos()));
                if (alreadyActive) {
                    continue;
                }
                Map<String, String> properties = new LinkedHashMap<>();
                properties.put("trigger", manual ? "manual_interact" : "automatic_world_event");
                properties.put("hook", event.hook());
                properties.put("radius", Integer.toString(defaultRuntimeRadius(definition)));
                RitualContext context = new RitualContext(event.dimension(), event.pos(), event.playerId(), properties);
                results.add(start(definition.id(), context, event.hook()));
            }
            return List.copyOf(results);
        }

        private void disruptAt(NexusWorldEvent event, String reason) {
            if (!event.hasBlockContext()) {
                return;
            }
            List<UUID> affected = active.values().stream()
                    .filter(ritual -> affects(ritual, event))
                    .map(ActiveRitual::instanceId)
                    .toList();
            for (UUID id : affected) {
                ActiveRitual ritual = active.get(id);
                if (ritual == null) {
                    continue;
                }
                if (ritual.definition().pauseWhenMissing()) {
                    active.put(id, ritual.withMissing(RitualState.PAUSED, reason));
                    record(id, "pause", reason);
                } else if (ritual.definition().failurePolicy().failWhenInterrupted()) {
                    active.remove(id);
                    record(id, "fail", reason);
                } else {
                    active.remove(id);
                    record(id, "cancel", reason);
                }
            }
        }

        private static boolean affects(ActiveRitual ritual, NexusWorldEvent event) {
            if (!ritual.context().dimension().equals(event.dimension())) {
                return false;
            }
            int radius = runtimeRadius(ritual.context(), defaultRuntimeRadius(ritual.definition()));
            BlockPos center = ritual.context().center();
            BlockPos pos = event.pos();
            return Math.abs(center.getX() - pos.getX()) <= radius
                    && Math.abs(center.getY() - pos.getY()) <= radius
                    && Math.abs(center.getZ() - pos.getZ()) <= radius;
        }

        private static boolean centerMatches(RitualDefinition definition, NexusWorldEvent event) {
            if (definition.center() == null || definition.center().isBlank()) {
                return false;
            }
            String center = definition.center();
            return event.blockId().equals(center)
                    || event.optionalState().map(state -> state.toString().equals(center) || state.toString().contains(center)).orElse(false);
        }

        private static int defaultRuntimeRadius(RitualDefinition definition) {
            return Math.min(limits.maxRadius(), Math.max(1, definition.requiredStructures().isEmpty() ? 8 : 16));
        }

        private static int runtimeRadius(RitualContext context, int fallback) {
            try {
                return Math.min(limits.maxRadius(), Math.max(1, Integer.parseInt(context.properties().getOrDefault("radius", Integer.toString(fallback)))));
            } catch (NumberFormatException exception) {
                return fallback;
            }
        }

        public RitualStartResult start(ResourceLocation ritualId, RitualContext context, String source) {
            RitualDefinition definition = DEFINITIONS.get(ritualId);
            if (definition == null) {
                return RitualStartResult.failed(ritualId, "unknown_ritual", List.of("No ritual registered with id " + ritualId));
            }
            RitualValidationReport validation = definition.validate();
            if (!validation.passed()) {
                return RitualStartResult.failed(ritualId, "definition_invalid", validation.errors());
            }
            long sameChunk = active.values().stream()
                    .filter(ritual -> ritual.context().dimension().equals(context.dimension()))
                    .filter(ritual -> ritual.context().chunkKey().equals(context.chunkKey()))
                    .count();
            if (sameChunk >= limits.maxActivePerChunk()) {
                return RitualStartResult.failed(ritualId, "chunk_limit", List.of("Too many active rituals in chunk " + context.chunkKey()));
            }
            UUID instanceId = UUID.randomUUID();
            ActiveRitual ritual = new ActiveRitual(instanceId, definition, context, RitualState.DELAYED,
                    0, definition.stability().startingStability(), source, Instant.now(), List.of());
            active.put(instanceId, ritual);
            record(instanceId, "start", source);
            return RitualStartResult.started(ritual);
        }

        public RitualProgress tick(UUID instanceId) {
            ActiveRitual ritual = active.get(instanceId);
            if (ritual == null) {
                return new RitualProgress(instanceId, RitualState.FAILED, 0, 0.0D, List.of("not active"));
            }
            RitualDefinition definition = ritual.definition();
            RitualState nextState = ritual.state() == RitualState.DELAYED && ritual.ticks() >= definition.delayTicks()
                    ? RitualState.RUNNING : ritual.state();
            int nextTicks = ritual.ticks() + 1;
            if (nextState == RitualState.RUNNING && nextTicks >= definition.delayTicks() + definition.durationTicks()) {
                nextState = RitualState.COMPLETE;
            }
            ActiveRitual next = ritual.withProgress(nextState, nextTicks);
            if (nextState == RitualState.COMPLETE) {
                active.remove(instanceId);
                record(instanceId, "complete", definition.id().toString());
            } else {
                active.put(instanceId, next);
                if (nextTicks % Math.max(1, limits.requirementCheckIntervalTicks()) == 0) {
                    record(instanceId, "requirement_check", "throttled");
                }
            }
            return new RitualProgress(instanceId, nextState, nextTicks, next.stability(), List.of());
        }

        public boolean cancel(UUID instanceId, String reason) {
            ActiveRitual ritual = active.remove(instanceId);
            if (ritual == null) {
                return false;
            }
            record(instanceId, "cancel", reason);
            return true;
        }

        public List<ActiveRitual> active() {
            return List.copyOf(active.values());
        }

        public List<TimelineEntry> timeline() {
            return List.copyOf(timeline);
        }

        private void record(UUID instanceId, String event, String detail) {
            timeline.add(new TimelineEntry(instanceId, event, detail, Instant.now()));
            while (timeline.size() > 512) {
                timeline.removeFirst();
            }
        }
    }

    @NexusIncubating(since = "1.3")
    public record RitualContext(String dimension,
                                BlockPos center,
                                Optional<UUID> player,
                                Map<String, String> properties) {
        public RitualContext {
            player = player == null ? Optional.empty() : player;
            properties = Map.copyOf(properties);
        }

        public String chunkKey() {
            return (center.getX() >> 4) + "," + (center.getZ() >> 4);
        }
    }

    @NexusIncubating(since = "1.3")
    public record ActiveRitual(UUID instanceId,
                               RitualDefinition definition,
                               RitualContext context,
                               RitualState state,
                               int ticks,
                               double stability,
                               String source,
                               Instant startedAt,
                               List<String> missingRequirements) {
        public ActiveRitual {
            missingRequirements = List.copyOf(missingRequirements);
        }

        public ActiveRitual withProgress(RitualState state, int ticks) {
            return new ActiveRitual(instanceId, definition, context, state, ticks, stability, source, startedAt, missingRequirements);
        }

        public ActiveRitual withMissing(RitualState state, String missingRequirement) {
            List<String> missing = new ArrayList<>(missingRequirements);
            missing.add(missingRequirement);
            return new ActiveRitual(instanceId, definition, context, state, ticks, stability, source, startedAt, missing);
        }
    }

    @NexusIncubating(since = "1.3")
    public enum RitualState {
        DELAYED,
        RUNNING,
        PAUSED,
        COMPLETE,
        FAILED,
        CANCELLED
    }

    @NexusIncubating(since = "1.3")
    public record RitualStartResult(ResourceLocation ritualId,
                                    boolean started,
                                    String reason,
                                    List<String> messages,
                                    Optional<ActiveRitual> activeRitual) {
        public RitualStartResult {
            messages = List.copyOf(messages);
            activeRitual = activeRitual == null ? Optional.empty() : activeRitual;
        }

        public static RitualStartResult started(ActiveRitual ritual) {
            return new RitualStartResult(ritual.definition().id(), true, "started", List.of(), Optional.of(ritual));
        }

        public static RitualStartResult failed(ResourceLocation ritualId, String reason, List<String> messages) {
            return new RitualStartResult(ritualId, false, reason, messages, Optional.empty());
        }
    }

    @NexusIncubating(since = "1.3")
    public record RitualProgress(UUID instanceId, RitualState state, int ticks, double stability, List<String> messages) {
        public RitualProgress {
            messages = List.copyOf(messages);
        }
    }

    @NexusIncubating(since = "1.3")
    public record TimelineEntry(UUID instanceId, String event, String detail, Instant createdAt) {
    }

    @NexusIncubating(since = "1.3")
    public record GuidePageDraft(ResourceLocation ritualId,
                                 String title,
                                 String requirements,
                                 List<String> results,
                                 List<String> tags) {
        public GuidePageDraft {
            results = List.copyOf(results);
            tags = List.copyOf(tags);
        }
    }

    private static JsonArray effects(List<RitualEffect> effects) {
        JsonArray array = new JsonArray();
        effects.forEach(effect -> array.add(effect.toJson()));
        return array;
    }

    private static JsonArray strings(Collection<String> values) {
        JsonArray array = new JsonArray();
        values.forEach(array::add);
        return array;
    }

    private static String idValue(Object value) {
        if (value instanceof ResourceLocation id) {
            return id.toString();
        }
        return String.valueOf(value);
    }

    private NexusRituals() {
    }
}

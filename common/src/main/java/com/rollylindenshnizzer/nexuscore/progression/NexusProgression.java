package com.rollylindenshnizzer.nexuscore.progression;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.api.NexusIncubating;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.data.NexusData;
import net.minecraft.resources.ResourceLocation;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@NexusIncubating(since = "1.3")
public final class NexusProgression {
    private static final Map<ResourceLocation, ProgressionNode> NODES = new LinkedHashMap<>();
    private static final ProgressionRuntime RUNTIME = new ProgressionRuntime();

    public static ProgressionNode.Builder node(String namespace, String path) {
        return node(NexusIds.id(namespace, path));
    }

    public static ProgressionNode.Builder node(ResourceLocation id) {
        return new ProgressionNode.Builder(id);
    }

    public static ProgressionNode register(ProgressionNode node) {
        NODES.put(node.id(), node);
        return node;
    }

    public static Collection<ProgressionNode> nodes() {
        return List.copyOf(NODES.values());
    }

    public static Optional<ProgressionNode> find(ResourceLocation id) {
        return Optional.ofNullable(NODES.get(id));
    }

    public static ProgressionRuntime runtime() {
        return RUNTIME;
    }

    public static NexusData.DataPlan writeAllTo(NexusData.DataPlan plan) {
        for (ProgressionNode node : NODES.values()) {
            if (node.id().getNamespace().equals(plan.modId())) {
                node.writeTo(plan);
            }
        }
        return plan;
    }

    public static ProgressionValidationReport validate() {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        for (ProgressionNode node : NODES.values()) {
            for (ResourceLocation dependency : node.dependencies()) {
                if (!NODES.containsKey(dependency)) {
                    errors.add(node.id() + " depends on missing node " + dependency);
                }
            }
            if (node.conditions().isEmpty() && node.dependencies().isEmpty() && !node.optional()) {
                warnings.add(node.id() + " has no unlock conditions");
            }
        }
        for (List<ResourceLocation> cycle : dependencyCycles()) {
            errors.add("progression dependency cycle: " + cycle);
        }
        return new ProgressionValidationReport(errors, warnings);
    }

    public static String debugSummary() {
        ProgressionValidationReport report = validate();
        return "progressionNodes=" + NODES.size() + ", errors=" + report.errors().size() + ", warnings=" + report.warnings().size();
    }

    public static List<GuidePage> generatedGuidePages() {
        return NODES.values().stream().map(ProgressionNode::guidePage).toList();
    }

    private static List<List<ResourceLocation>> dependencyCycles() {
        List<List<ResourceLocation>> cycles = new ArrayList<>();
        Set<ResourceLocation> visited = new LinkedHashSet<>();
        Set<ResourceLocation> stack = new LinkedHashSet<>();
        Deque<ResourceLocation> path = new ArrayDeque<>();
        for (ResourceLocation id : NODES.keySet()) {
            visit(id, visited, stack, path, cycles);
        }
        return cycles;
    }

    private static void visit(ResourceLocation id,
                              Set<ResourceLocation> visited,
                              Set<ResourceLocation> stack,
                              Deque<ResourceLocation> path,
                              List<List<ResourceLocation>> cycles) {
        if (stack.contains(id)) {
            List<ResourceLocation> cycle = new ArrayList<>(path);
            cycle.add(id);
            cycles.add(cycle);
            return;
        }
        if (!visited.add(id)) {
            return;
        }
        stack.add(id);
        path.addLast(id);
        ProgressionNode node = NODES.get(id);
        if (node != null) {
            for (ResourceLocation dependency : node.dependencies()) {
                visit(dependency, visited, stack, path, cycles);
            }
        }
        path.removeLast();
        stack.remove(id);
    }

    @NexusIncubating(since = "1.3")
    public record ProgressionNode(ResourceLocation id,
                                  List<ResourceLocation> dependencies,
                                  List<UnlockCondition> conditions,
                                  List<UnlockAction> actions,
                                  boolean hidden,
                                  boolean optional,
                                  boolean repeatable,
                                  ProgressionScope scope,
                                  boolean syncToClient,
                                  List<String> guidePages,
                                  Map<String, String> metadata) {
        public ProgressionNode {
            dependencies = List.copyOf(dependencies);
            conditions = List.copyOf(conditions);
            actions = List.copyOf(actions);
            guidePages = List.copyOf(guidePages);
            metadata = Map.copyOf(metadata);
        }

        public NexusData.DataPlan writeTo(NexusData.DataPlan plan) {
            plan.data("nexuscore/progression/" + id.getPath() + ".json", toJson());
            plan.translation(NexusIds.translationKey("progression", id), NexusIds.humanName(id.getPath()));
            for (String page : guidePages) {
                ResourceLocation pageId = NexusIds.parse(page);
                String translationPath = pageId.getNamespace() + "." + pageId.getPath().replace('/', '.');
                plan.translation("nexuscore.guide." + translationPath, NexusIds.humanName(pageId.getPath()));
            }
            return plan;
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("schema_version", "1.3");
            json.addProperty("id", id.toString());
            JsonArray deps = new JsonArray();
            dependencies.forEach(dependency -> deps.add(dependency.toString()));
            json.add("dependencies", deps);
            JsonArray conditionArray = new JsonArray();
            conditions.forEach(condition -> conditionArray.add(condition.toJson()));
            json.add("conditions", conditionArray);
            JsonArray actionArray = new JsonArray();
            actions.forEach(action -> actionArray.add(action.toJson()));
            json.add("actions", actionArray);
            json.addProperty("hidden", hidden);
            json.addProperty("optional", optional);
            json.addProperty("repeatable", repeatable);
            json.addProperty("scope", scope.serializedName());
            json.addProperty("sync_to_client", syncToClient);
            json.add("guide_pages", strings(guidePages));
            JsonObject meta = new JsonObject();
            metadata.forEach(meta::addProperty);
            json.add("metadata", meta);
            return json;
        }

        public GuidePage guidePage() {
            List<String> missing = new ArrayList<>();
            dependencies.forEach(dependency -> missing.add("node " + dependency));
            conditions.forEach(condition -> missing.add(condition.type().serializedName() + " " + condition.target()));
            return new GuidePage(id, NexusIds.humanName(id.getPath()), hidden, guidePages, missing);
        }

        public static final class Builder {
            private final ResourceLocation id;
            private final List<ResourceLocation> dependencies = new ArrayList<>();
            private final List<UnlockCondition> conditions = new ArrayList<>();
            private final List<UnlockAction> actions = new ArrayList<>();
            private boolean hidden;
            private boolean optional;
            private boolean repeatable;
            private ProgressionScope scope = ProgressionScope.PLAYER;
            private boolean syncToClient = true;
            private final List<String> guidePages = new ArrayList<>();
            private final Map<String, String> metadata = new LinkedHashMap<>();

            private Builder(ResourceLocation id) {
                this.id = id;
            }

            public Builder dependsOn(Object node) {
                this.dependencies.add(toId(node));
                return this;
            }

            public Builder hidden(boolean hidden) {
                this.hidden = hidden;
                return this;
            }

            public Builder optional(boolean optional) {
                this.optional = optional;
                return this;
            }

            public Builder repeatable(boolean repeatable) {
                this.repeatable = repeatable;
                return this;
            }

            public Builder teamShared() {
                this.scope = ProgressionScope.TEAM;
                return this;
            }

            public Builder playerOnly() {
                this.scope = ProgressionScope.PLAYER;
                return this;
            }

            public Builder global() {
                this.scope = ProgressionScope.GLOBAL;
                return this;
            }

            public Builder syncToClient(boolean syncToClient) {
                this.syncToClient = syncToClient;
                return this;
            }

            public Builder condition(UnlockCondition condition) {
                this.conditions.add(condition);
                return this;
            }

            public Builder action(UnlockAction action) {
                this.actions.add(action);
                return this;
            }

            public Builder requiresAdvancement(Object advancement) {
                return condition(UnlockCondition.advancement(idValue(advancement)));
            }

            public Builder requiresItem(Object item) {
                return condition(UnlockCondition.item(idValue(item)));
            }

            public Builder requiresBlockInteraction(Object block) {
                return condition(new UnlockCondition(UnlockConditionType.BLOCK_INTERACTED, idValue(block), 1, Map.of()));
            }

            public Builder requiresEntityKilled(Object entity) {
                return condition(new UnlockCondition(UnlockConditionType.ENTITY_KILLED, idValue(entity), 1, Map.of()));
            }

            public Builder requiresRitual(Object ritual) {
                return condition(new UnlockCondition(UnlockConditionType.RITUAL_COMPLETED, idValue(ritual), 1, Map.of()));
            }

            public Builder requiresMultiblock(Object multiblock) {
                return condition(new UnlockCondition(UnlockConditionType.MULTIBLOCK_ASSEMBLED, idValue(multiblock), 1, Map.of()));
            }

            public Builder requiresDimension(Object dimension) {
                return condition(new UnlockCondition(UnlockConditionType.DIMENSION_ENTERED, idValue(dimension), 1, Map.of()));
            }

            public Builder requiresStructure(Object structure) {
                return condition(new UnlockCondition(UnlockConditionType.STRUCTURE_DISCOVERED, idValue(structure), 1, Map.of()));
            }

            public Builder requiresMachineRecipe(Object recipe) {
                return condition(new UnlockCondition(UnlockConditionType.MACHINE_RECIPE_COMPLETED, idValue(recipe), 1, Map.of()));
            }

            public Builder unlocksRecipe(Object recipe) {
                return action(UnlockAction.grantRecipe(idValue(recipe)));
            }

            public Builder unlocksGuidePage(Object page) {
                String value = idValue(page);
                this.guidePages.add(value);
                return action(new UnlockAction(UnlockActionType.UNLOCK_GUIDE_PAGE, value, Map.of()));
            }

            public Builder unlocksRitual(Object ritual) {
                return action(new UnlockAction(UnlockActionType.ENABLE_RITUAL, idValue(ritual), Map.of()));
            }

            public Builder unlocksMachineRecipe(Object recipe) {
                return action(new UnlockAction(UnlockActionType.ENABLE_MACHINE_RECIPE, idValue(recipe), Map.of()));
            }

            public Builder sendsMessage(String translationKey) {
                return action(new UnlockAction(UnlockActionType.SEND_MESSAGE, translationKey, Map.of()));
            }

            public Builder metadata(String key, String value) {
                metadata.put(key, value);
                return this;
            }

            public ProgressionNode build() {
                return new ProgressionNode(id, dependencies, conditions, actions, hidden, optional, repeatable,
                        scope, syncToClient, guidePages, metadata);
            }
        }
    }

    @NexusIncubating(since = "1.3")
    public record UnlockCondition(UnlockConditionType type, String target, long count, Map<String, String> parameters) {
        public UnlockCondition {
            parameters = Map.copyOf(parameters);
        }

        public static UnlockCondition advancement(String advancement) {
            return new UnlockCondition(UnlockConditionType.ADVANCEMENT_COMPLETED, advancement, 1, Map.of());
        }

        public static UnlockCondition item(String item) {
            return new UnlockCondition(UnlockConditionType.ITEM_OBTAINED, item, 1, Map.of());
        }

        public UnlockCondition parameter(String key, Object value) {
            Map<String, String> values = new LinkedHashMap<>(parameters);
            values.put(key, String.valueOf(value));
            return new UnlockCondition(type, target, count, values);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("type", type.serializedName());
            json.addProperty("target", target);
            json.addProperty("count", count);
            JsonObject params = new JsonObject();
            parameters.forEach(params::addProperty);
            json.add("parameters", params);
            return json;
        }
    }

    @NexusIncubating(since = "1.3")
    public enum UnlockConditionType {
        ADVANCEMENT_COMPLETED,
        ITEM_OBTAINED,
        BLOCK_INTERACTED,
        ENTITY_KILLED,
        RITUAL_COMPLETED,
        MULTIBLOCK_ASSEMBLED,
        DIMENSION_ENTERED,
        STRUCTURE_DISCOVERED,
        MACHINE_RECIPE_COMPLETED,
        CUSTOM_PREDICATE;

        public String serializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    @NexusIncubating(since = "1.3")
    public record UnlockAction(UnlockActionType type, String target, Map<String, String> parameters) {
        public UnlockAction {
            parameters = Map.copyOf(parameters);
        }

        public static UnlockAction grantRecipe(String recipe) {
            return new UnlockAction(UnlockActionType.GRANT_RECIPE, recipe, Map.of());
        }

        public static UnlockAction grantAdvancement(String advancement) {
            return new UnlockAction(UnlockActionType.GRANT_ADVANCEMENT, advancement, Map.of());
        }

        public UnlockAction parameter(String key, Object value) {
            Map<String, String> values = new LinkedHashMap<>(parameters);
            values.put(key, String.valueOf(value));
            return new UnlockAction(type, target, values);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("type", type.serializedName());
            json.addProperty("target", target);
            JsonObject params = new JsonObject();
            parameters.forEach(params::addProperty);
            json.add("parameters", params);
            return json;
        }
    }

    @NexusIncubating(since = "1.3")
    public enum UnlockActionType {
        GRANT_RECIPE,
        GRANT_ADVANCEMENT,
        UNLOCK_GUIDE_PAGE,
        ENABLE_RITUAL,
        ENABLE_MACHINE_RECIPE,
        SEND_MESSAGE,
        PLAY_SOUND,
        SPAWN_PARTICLES,
        RUN_CALLBACK;

        public String serializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    @NexusIncubating(since = "1.3")
    public enum ProgressionScope {
        PLAYER,
        TEAM,
        GLOBAL;

        public String serializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    @NexusIncubating(since = "1.3")
    public static final class ProgressionRuntime {
        private final Map<String, Set<ResourceLocation>> unlocked = new LinkedHashMap<>();
        private final List<ProgressionEvent> events = new ArrayList<>();

        public boolean unlock(String subject, ResourceLocation nodeId, String reason) {
            ProgressionNode node = NODES.get(nodeId);
            if (node == null) {
                return false;
            }
            Set<ResourceLocation> values = unlocked.computeIfAbsent(subject, ignored -> new LinkedHashSet<>());
            if (!node.repeatable() && values.contains(nodeId)) {
                return false;
            }
            if (!values.containsAll(node.dependencies())) {
                return false;
            }
            values.add(nodeId);
            events.add(new ProgressionEvent(subject, nodeId, reason, Instant.now()));
            while (events.size() > 512) {
                events.removeFirst();
            }
            return true;
        }

        public Set<ResourceLocation> unlocked(String subject) {
            return Set.copyOf(unlocked.getOrDefault(subject, Set.of()));
        }

        public List<ProgressionNode> locked(String subject) {
            Set<ResourceLocation> values = unlocked.getOrDefault(subject, Set.of());
            return NODES.values().stream().filter(node -> !values.contains(node.id())).toList();
        }

        public ProgressionInspection inspect(String subject) {
            Set<ResourceLocation> values = unlocked(subject);
            return new ProgressionInspection(subject, values, locked(subject).stream().map(ProgressionNode::id).toList(), generatedGuidePages());
        }
    }

    @NexusIncubating(since = "1.3")
    public record ProgressionEvent(String subject, ResourceLocation nodeId, String reason, Instant createdAt) {
    }

    @NexusIncubating(since = "1.3")
    public record ProgressionInspection(String subject,
                                        Set<ResourceLocation> unlockedNodes,
                                        List<ResourceLocation> lockedNodes,
                                        List<GuidePage> guidePages) {
        public ProgressionInspection {
            unlockedNodes = Set.copyOf(unlockedNodes);
            lockedNodes = List.copyOf(lockedNodes);
            guidePages = List.copyOf(guidePages);
        }
    }

    @NexusIncubating(since = "1.3")
    public record GuidePage(ResourceLocation nodeId,
                            String title,
                            boolean hiddenUntilUnlocked,
                            List<String> generatedPages,
                            List<String> missingRequirements) {
        public GuidePage {
            generatedPages = List.copyOf(generatedPages);
            missingRequirements = List.copyOf(missingRequirements);
        }
    }

    @NexusIncubating(since = "1.3")
    public record ProgressionValidationReport(List<String> errors, List<String> warnings) {
        public ProgressionValidationReport {
            errors = List.copyOf(errors);
            warnings = List.copyOf(warnings);
        }

        public boolean passed() {
            return errors.isEmpty();
        }

        public String summary() {
            return "Progression validation " + (passed() ? "passed" : "failed")
                    + " with " + errors.size() + " errors and " + warnings.size() + " warnings";
        }
    }

    private static ResourceLocation toId(Object value) {
        if (value instanceof ResourceLocation id) {
            return id;
        }
        return NexusIds.parse(String.valueOf(value));
    }

    private static String idValue(Object value) {
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

    private NexusProgression() {
    }
}

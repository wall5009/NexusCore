package com.rollylindenshnizzer.nexuscore.ai;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@NexusStable(since = "1.3")
public final class NexusAi {
    private static final Map<ResourceLocation, GoalDefinition> GOALS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, BrainDefinition> BRAINS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, GroupDefinition> GROUPS = new LinkedHashMap<>();

    public static GoalDefinition.Builder goal(String namespace, String path) {
        return new GoalDefinition.Builder(NexusIds.id(namespace, path));
    }

    public static GoalDefinition register(GoalDefinition goal) {
        GOALS.put(goal.id(), goal);
        return goal;
    }

    public static Collection<GoalDefinition> goals() {
        return List.copyOf(GOALS.values());
    }

    public static BrainDefinition.Builder brain(String namespace, String path) {
        return new BrainDefinition.Builder(NexusIds.id(namespace, path));
    }

    public static BrainDefinition register(BrainDefinition brain) {
        BRAINS.put(brain.id(), brain);
        return brain;
    }

    public static Collection<BrainDefinition> brains() {
        return List.copyOf(BRAINS.values());
    }

    public static MemoryKey memoryKey(String namespace, String path, String codec) {
        return new MemoryKey(NexusIds.id(namespace, path), codec, false, false, 0, List.of());
    }

    public static TypedSensor.Builder sensor(String namespace, String path) {
        return new TypedSensor.Builder(NexusIds.id(namespace, path));
    }

    public static BehaviorScheduleBuilder schedule(String name) {
        return new BehaviorScheduleBuilder(name);
    }

    public static GroupDefinition.Builder group(String namespace, String path) {
        return new GroupDefinition.Builder(NexusIds.id(namespace, path));
    }

    public static GroupDefinition register(GroupDefinition group) {
        GROUPS.put(group.id(), group);
        return group;
    }

    public static Collection<GroupDefinition> groups() {
        return List.copyOf(GROUPS.values());
    }

    public static AiDebugReport debugReport() {
        List<String> warnings = new ArrayList<>();
        for (GoalDefinition goal : GOALS.values()) {
            if (goal.priority() < 0) {
                warnings.add(goal.id() + " has negative priority");
            }
            if (goal.controls().isEmpty()) {
                warnings.add(goal.id() + " has no control flags");
            }
        }
        for (BrainDefinition brain : BRAINS.values()) {
            if (brain.memories().isEmpty()) {
                warnings.add(brain.id() + " has no memory definitions");
            }
            if (brain.sensors().isEmpty()) {
                warnings.add(brain.id() + " has no sensor definitions");
            }
            brain.sensors().stream()
                    .filter(sensor -> sensor.scanRateTicks() < 5)
                    .forEach(sensor -> warnings.add(brain.id() + " sensor " + sensor.id() + " scans very frequently"));
        }
        for (GroupDefinition group : GROUPS.values()) {
            if (group.maxMembers() > 64) {
                warnings.add(group.id() + " allows a large group; consider a lower cap for server safety");
            }
        }
        return new AiDebugReport(GOALS.size(), BRAINS.size(), warnings, List.of());
    }

    public static String debugSummary() {
        AiDebugReport report = debugReport();
        return "goals=" + report.goalCount() + ", brains=" + report.brainCount()
                + ", groups=" + GROUPS.size() + ", warnings=" + report.warnings().size();
    }

    @NexusStable(since = "1.3")
    public record GoalDefinition(ResourceLocation id,
                                 int priority,
                                 String goalType,
                                 List<String> controls,
                                 Map<String, String> parameters,
                                 Optional<HomePosition> home,
                                 Optional<Territory> territory,
                                 Optional<PatrolRoute> patrolRoute,
                                 String schedule) {
        public GoalDefinition {
            controls = List.copyOf(controls);
            parameters = Map.copyOf(parameters);
            home = home == null ? Optional.empty() : home;
            territory = territory == null ? Optional.empty() : territory;
            patrolRoute = patrolRoute == null ? Optional.empty() : patrolRoute;
        }

        public String explain() {
            return id + " priority=" + priority + " type=" + goalType + " controls=" + controls + " schedule=" + schedule;
        }

        public static final class Builder {
            private final ResourceLocation id;
            private int priority = 5;
            private String goalType = "custom";
            private final List<String> controls = new ArrayList<>();
            private final Map<String, String> parameters = new LinkedHashMap<>();
            private Optional<HomePosition> home = Optional.empty();
            private Optional<Territory> territory = Optional.empty();
            private Optional<PatrolRoute> patrolRoute = Optional.empty();
            private String schedule = "always";

            private Builder(ResourceLocation id) {
                this.id = id;
            }

            public Builder priority(int priority) {
                this.priority = priority;
                return this;
            }

            public Builder type(String goalType) {
                this.goalType = goalType;
                return this;
            }

            public Builder control(String control) {
                this.controls.add(control);
                return this;
            }

            public Builder parameter(String key, Object value) {
                this.parameters.put(key, String.valueOf(value));
                return this;
            }

            public Builder home(HomePosition home) {
                this.home = Optional.of(home);
                return this;
            }

            public Builder territory(Territory territory) {
                this.territory = Optional.of(territory);
                return this;
            }

            public Builder patrol(PatrolRoute patrolRoute) {
                this.patrolRoute = Optional.of(patrolRoute);
                return this;
            }

            public Builder schedule(String schedule) {
                this.schedule = schedule;
                return this;
            }

            public GoalDefinition build() {
                return new GoalDefinition(id, priority, goalType, controls, parameters, home, territory, patrolRoute, schedule);
            }
        }
    }

    @NexusStable(since = "1.3")
    public static final class GoalLibrary {
        public static GoalDefinition followOwner(String namespace, String path, double speed, float maxDistance) {
            return goal(namespace, path)
                    .type("follow_owner")
                    .priority(4)
                    .control("move")
                    .control("look")
                    .parameter("speed", speed)
                    .parameter("max_distance", maxDistance)
                    .build();
        }

        public static GoalDefinition guardHome(String namespace, String path, HomePosition home, int radius) {
            return goal(namespace, path)
                    .type("guard_home")
                    .priority(3)
                    .control("move")
                    .control("target")
                    .home(home)
                    .territory(new Territory(home.center(), radius, true))
                    .parameter("radius", radius)
                    .build();
        }

        public static GoalDefinition patrol(String namespace, String path, PatrolRoute route, double speed) {
            return goal(namespace, path)
                    .type("patrol")
                    .priority(5)
                    .control("move")
                    .patrol(route)
                    .parameter("speed", speed)
                    .build();
        }

        private GoalLibrary() {
        }
    }

    @NexusStable(since = "1.3")
    public record HomePosition(BlockPos center, int radius, boolean persistent) {
    }

    @NexusStable(since = "1.3")
    public record Territory(BlockPos center, int radius, boolean defend) {
        public boolean contains(BlockPos pos) {
            return center.distManhattan(pos) <= radius;
        }
    }

    @NexusStable(since = "1.3")
    public record PatrolRoute(List<BlockPos> points, boolean loop) {
        public PatrolRoute {
            points = List.copyOf(points);
        }

        public BlockPos next(int index) {
            if (points.isEmpty()) {
                return BlockPos.ZERO;
            }
            if (loop) {
                return points.get(Math.floorMod(index, points.size()));
            }
            return points.get(Math.min(index, points.size() - 1));
        }
    }

    @NexusStable(since = "1.3")
    public static final class PathCache {
        private final Map<String, List<BlockPos>> paths = new LinkedHashMap<>();

        public void put(String key, List<BlockPos> path) {
            paths.put(key, List.copyOf(path));
        }

        public Optional<List<BlockPos>> get(String key) {
            return Optional.ofNullable(paths.get(key));
        }

        public int size() {
            return paths.size();
        }
    }

    @NexusStable(since = "1.3")
    public static final class StuckDetector {
        private final int sampleSize;
        private final double maxDrift;
        private final List<BlockPos> samples = new ArrayList<>();

        public StuckDetector(int sampleSize, double maxDrift) {
            this.sampleSize = Math.max(2, sampleSize);
            this.maxDrift = maxDrift;
        }

        public boolean sample(BlockPos pos) {
            samples.add(pos);
            while (samples.size() > sampleSize) {
                samples.removeFirst();
            }
            if (samples.size() < sampleSize) {
                return false;
            }
            return samples.getFirst().distSqr(samples.getLast()) <= maxDrift * maxDrift;
        }
    }

    @NexusStable(since = "1.3")
    public record BrainPreview(String memoryType, String sensorType, String behavior, Duration scheduleWindow) {
    }

    @NexusStable(since = "1.3")
    public record BrainDefinition(ResourceLocation id,
                                  List<MemoryDefinition> memories,
                                  List<SensorDefinition> sensors,
                                  List<BehaviorDefinition> behaviors,
                                  BehaviorSchedule schedule) {
        public BrainDefinition {
            memories = List.copyOf(memories);
            sensors = List.copyOf(sensors);
            behaviors = List.copyOf(behaviors);
        }

        public String explain() {
            return id + " memories=" + memories.size() + ", sensors=" + sensors.size()
                    + ", behaviors=" + behaviors.size() + ", schedule=" + schedule.name();
        }

        public static final class Builder {
            private final ResourceLocation id;
            private final List<MemoryDefinition> memories = new ArrayList<>();
            private final List<SensorDefinition> sensors = new ArrayList<>();
            private final List<BehaviorDefinition> behaviors = new ArrayList<>();
            private BehaviorSchedule schedule = new BehaviorSchedule("always", Map.of("start", "0", "end", "24000"));

            private Builder(ResourceLocation id) {
                this.id = id;
            }

            public Builder memory(String id, String codec, int expiryTicks) {
                this.memories.add(new MemoryDefinition(id, codec, expiryTicks));
                return this;
            }

            public Builder memory(MemoryKey key) {
                this.memories.add(new MemoryDefinition(key.id().toString(), key.codec(), key.defaultExpiryTicks()));
                return this;
            }

            public Builder sensor(String id, int scanRateTicks, String target) {
                this.sensors.add(new SensorDefinition(id, scanRateTicks, target));
                return this;
            }

            public Builder sensor(TypedSensor sensor) {
                this.sensors.add(new SensorDefinition(sensor.id().toString(), sensor.scanRateTicks(), sensor.target()));
                return this;
            }

            public Builder behavior(String id, int priority, String activity, String trigger) {
                this.behaviors.add(new BehaviorDefinition(id, priority, activity, trigger));
                return this;
            }

            public Builder behavior(BehaviorDefinition behavior) {
                this.behaviors.add(behavior);
                return this;
            }

            public Builder schedule(BehaviorSchedule schedule) {
                this.schedule = schedule;
                return this;
            }

            public BrainDefinition build() {
                return new BrainDefinition(id, memories, sensors, behaviors, schedule);
            }
        }
    }

    @NexusStable(since = "1.3")
    public record MemoryDefinition(String id, String codec, int expiryTicks) {
    }

    @NexusStable(since = "1.3")
    public record SensorDefinition(String id, int scanRateTicks, String target) {
    }

    @NexusStable(since = "1.3")
    public record BehaviorDefinition(String id, int priority, String activity, String trigger) {
    }

    @NexusStable(since = "1.3")
    public record BehaviorSchedule(String name, Map<String, String> windows) {
        public BehaviorSchedule {
            windows = Map.copyOf(windows);
        }
    }

    @NexusStable(since = "1.3")
    public record MemoryKey(ResourceLocation id,
                            String codec,
                            boolean synced,
                            boolean persistent,
                            int defaultExpiryTicks,
                            List<String> debugTags) {
        public MemoryKey {
            debugTags = List.copyOf(debugTags);
        }

        public MemoryKey clientSynced() {
            return new MemoryKey(id, codec, true, persistent, defaultExpiryTicks, debugTags);
        }

        public MemoryKey persistentMemory() {
            return new MemoryKey(id, codec, synced, true, defaultExpiryTicks, debugTags);
        }

        public MemoryKey temporary(int expiryTicks) {
            return new MemoryKey(id, codec, synced, false, expiryTicks, debugTags);
        }

        public MemoryKey tag(String tag) {
            List<String> values = new ArrayList<>(debugTags);
            values.add(tag);
            return new MemoryKey(id, codec, synced, persistent, defaultExpiryTicks, values);
        }

        public MemoryCondition present() {
            return new MemoryCondition(id, "present", "");
        }

        public MemoryCondition absent() {
            return new MemoryCondition(id, "absent", "");
        }
    }

    @NexusStable(since = "1.3")
    public record MemoryCondition(ResourceLocation memory, String operator, String expectedValue) {
    }

    @NexusStable(since = "1.3")
    public static final class CommonMemories {
        public static MemoryKey homePosition() {
            return memoryKey("nexuscore", "home_position", "minecraft:block_pos").persistentMemory().clientSynced().tag("home");
        }

        public static MemoryKey patrolRoute() {
            return memoryKey("nexuscore", "patrol_route", "nexuscore:block_pos_list").persistentMemory().tag("movement");
        }

        public static MemoryKey targetEntity() {
            return memoryKey("nexuscore", "target_entity", "minecraft:uuid").temporary(200).clientSynced().tag("combat");
        }

        public static MemoryKey lastAttacker() {
            return memoryKey("nexuscore", "last_attacker", "minecraft:uuid").temporary(600).tag("combat");
        }

        public static MemoryKey ownerPlayer() {
            return memoryKey("nexuscore", "owner_player", "minecraft:uuid").persistentMemory().clientSynced();
        }

        public static MemoryKey hungerLevel() {
            return memoryKey("nexuscore", "hunger_level", "minecraft:int").persistentMemory().clientSynced();
        }

        public static MemoryKey fearSource() {
            return memoryKey("nexuscore", "fear_source", "minecraft:block_pos").temporary(300);
        }

        public static MemoryKey workSite() {
            return memoryKey("nexuscore", "work_site", "minecraft:block_pos").persistentMemory().clientSynced();
        }

        public static MemoryKey groupLeader() {
            return memoryKey("nexuscore", "group_leader", "minecraft:uuid").temporary(1_200).clientSynced();
        }

        public static MemoryKey ritualTarget() {
            return memoryKey("nexuscore", "ritual_target", "minecraft:block_pos").temporary(1_200).tag("ritual");
        }

        public static MemoryKey structureTarget() {
            return memoryKey("nexuscore", "structure_target", "minecraft:block_pos").temporary(1_200).tag("structure");
        }

        private CommonMemories() {
        }
    }

    @NexusStable(since = "1.3")
    public record TypedSensor(ResourceLocation id,
                              String type,
                              String target,
                              int radius,
                              int scanRateTicks,
                              boolean throttled,
                              List<String> conditions,
                              List<String> performanceWarnings) {
        public TypedSensor {
            conditions = List.copyOf(conditions);
            performanceWarnings = List.copyOf(performanceWarnings);
        }

        public SensorDefinition toDefinition() {
            return new SensorDefinition(id.toString(), scanRateTicks, target);
        }

        public static final class Builder {
            private final ResourceLocation id;
            private String type = "custom";
            private String target = "minecraft:empty";
            private int radius = 16;
            private int scanRateTicks = 20;
            private boolean throttled = true;
            private final List<String> conditions = new ArrayList<>();
            private final List<String> warnings = new ArrayList<>();

            private Builder(ResourceLocation id) {
                this.id = id;
            }

            public Builder nearbyEntities(String entitySelector, int radius) {
                this.type = "nearby_entity";
                this.target = entitySelector;
                this.radius = radius;
                return this;
            }

            public Builder nearbyBlocks(String blockSelector, int radius) {
                this.type = "nearby_block";
                this.target = blockSelector;
                this.radius = radius;
                return this;
            }

            public Builder nearbyItems(String itemSelector, int radius) {
                this.type = "nearby_item";
                this.target = itemSelector;
                this.radius = radius;
                return this;
            }

            public Builder lightLevel() {
                this.type = "light_level";
                this.target = "minecraft:light";
                return this;
            }

            public Builder biome() {
                this.type = "biome";
                this.target = "minecraft:biome";
                return this;
            }

            public Builder weather() {
                this.type = "weather";
                this.target = "minecraft:weather";
                return this;
            }

            public Builder soundEvent(String event) {
                this.type = "sound_event";
                this.target = event;
                return this;
            }

            public Builder playerReputation(String provider) {
                this.type = "player_reputation";
                this.target = provider;
                return this;
            }

            public Builder automationNetwork(String network) {
                this.type = "automation_network";
                this.target = network;
                return this;
            }

            public Builder customPredicate(String predicate) {
                this.type = "custom_predicate";
                this.target = predicate;
                return this;
            }

            public Builder scanRate(int ticks) {
                this.scanRateTicks = Math.max(1, ticks);
                if (this.scanRateTicks < 5) {
                    this.warnings.add("scan rate below 5 ticks can be expensive");
                }
                return this;
            }

            public Builder throttled(boolean throttled) {
                this.throttled = throttled;
                return this;
            }

            public Builder condition(String condition) {
                this.conditions.add(condition);
                return this;
            }

            public TypedSensor build() {
                if (radius > 48) {
                    warnings.add("large radius may scan many entities or blocks");
                }
                return new TypedSensor(id, type, target, radius, scanRateTicks, throttled, conditions, warnings);
            }
        }
    }

    @NexusStable(since = "1.3")
    public static final class BehaviorLibrary {
        public static BehaviorDefinition wander() {
            return behavior("wander", 8, "idle", "always");
        }

        public static BehaviorDefinition patrol() {
            return behavior("patrol", 5, "guard", "has_patrol_route");
        }

        public static BehaviorDefinition guardArea() {
            return behavior("guard_area", 4, "guard", "has_home_position");
        }

        public static BehaviorDefinition flee() {
            return behavior("flee", 1, "panic", "has_fear_source");
        }

        public static BehaviorDefinition chaseTarget() {
            return behavior("chase_target", 2, "fight", "has_target_entity");
        }

        public static BehaviorDefinition returnHome() {
            return behavior("return_home", 5, "idle", "away_from_home");
        }

        public static BehaviorDefinition followOwner() {
            return behavior("follow_owner", 4, "follow", "has_owner_player");
        }

        public static BehaviorDefinition defendOwner() {
            return behavior("defend_owner", 2, "fight", "owner_attacked");
        }

        public static BehaviorDefinition workAtBlock() {
            return behavior("work_at_block", 5, "work", "has_work_site");
        }

        public static BehaviorDefinition useItem() {
            return behavior("use_item", 6, "work", "has_usable_item");
        }

        public static BehaviorDefinition pickUpItem() {
            return behavior("pick_up_item", 6, "work", "nearby_item");
        }

        public static BehaviorDefinition depositItem() {
            return behavior("deposit_item", 6, "work", "inventory_full");
        }

        public static BehaviorDefinition performRitualRole() {
            return behavior("perform_ritual_role", 3, "ritual", "has_ritual_target");
        }

        public static BehaviorDefinition interactWithMultiblock() {
            return behavior("interact_with_multiblock", 4, "work", "near_multiblock");
        }

        public static BehaviorDefinition openDoor() {
            return behavior("open_door", 7, "navigation", "blocked_by_door");
        }

        public static BehaviorDefinition avoidHazard() {
            return behavior("avoid_hazard", 1, "panic", "near_hazard");
        }

        public static BehaviorDefinition sleep() {
            return behavior("sleep", 9, "rest", "night");
        }

        public static BehaviorDefinition gatherResources() {
            return behavior("gather_resources", 5, "work", "needs_resources");
        }

        public static BehaviorDefinition callAllies() {
            return behavior("call_allies", 2, "fight", "outnumbered");
        }

        public static BehaviorDefinition custom(String id, int priority, String activity, String trigger) {
            return behavior(id, priority, activity, trigger);
        }

        private static BehaviorDefinition behavior(String id, int priority, String activity, String trigger) {
            return new BehaviorDefinition(id, priority, activity, trigger);
        }

        private BehaviorLibrary() {
        }
    }

    @NexusStable(since = "1.3")
    public static final class BehaviorScheduleBuilder {
        private final String name;
        private final Map<String, String> windows = new LinkedHashMap<>();

        private BehaviorScheduleBuilder(String name) {
            this.name = name;
        }

        public BehaviorScheduleBuilder timeOfDay(String activity, int startTick, int endTick) {
            windows.put(activity, startTick + ".." + endTick);
            return this;
        }

        public BehaviorScheduleBuilder weather(String activity, String weather) {
            windows.put(activity + "@weather", weather);
            return this;
        }

        public BehaviorScheduleBuilder dimension(String activity, String dimension) {
            windows.put(activity + "@dimension", dimension);
            return this;
        }

        public BehaviorScheduleBuilder event(String activity, String event) {
            windows.put(activity + "@event", event);
            return this;
        }

        public BehaviorScheduleBuilder fallback(String activity) {
            windows.put("fallback", activity);
            return this;
        }

        public BehaviorSchedule build() {
            return new BehaviorSchedule(name, windows);
        }
    }

    @NexusStable(since = "1.3")
    public record ActivityDefinition(String name,
                                     int priority,
                                     List<MemoryCondition> requiredMemories,
                                     List<BehaviorDefinition> behaviors,
                                     String fallbackBehavior) {
        public ActivityDefinition {
            requiredMemories = List.copyOf(requiredMemories);
            behaviors = List.copyOf(behaviors);
        }
    }

    @NexusStable(since = "1.3")
    public record GroupDefinition(ResourceLocation id,
                                  int maxMembers,
                                  String leaderMemory,
                                  String sharedTargetMemory,
                                  String formation,
                                  List<String> alertEvents,
                                  List<String> groupBehaviors,
                                  boolean shareMemories,
                                  int memoryShareCap) {
        public GroupDefinition {
            alertEvents = List.copyOf(alertEvents);
            groupBehaviors = List.copyOf(groupBehaviors);
        }

        public GroupDebugReport debugReport() {
            List<String> warnings = new ArrayList<>();
            if (maxMembers > memoryShareCap && shareMemories) {
                warnings.add("memory share cap is lower than max members");
            }
            if (formation.isBlank()) {
                warnings.add("group has no formation movement mode");
            }
            return new GroupDebugReport(id, maxMembers, warnings);
        }

        public static final class Builder {
            private final ResourceLocation id;
            private int maxMembers = 16;
            private String leaderMemory = "nexuscore:group_leader";
            private String sharedTargetMemory = "nexuscore:target_entity";
            private String formation = "loose";
            private final List<String> alertEvents = new ArrayList<>();
            private final List<String> groupBehaviors = new ArrayList<>();
            private boolean shareMemories = true;
            private int memoryShareCap = 16;

            private Builder(ResourceLocation id) {
                this.id = id;
            }

            public Builder maxMembers(int maxMembers) {
                this.maxMembers = maxMembers;
                return this;
            }

            public Builder leaderMemory(MemoryKey memory) {
                this.leaderMemory = memory.id().toString();
                return this;
            }

            public Builder sharedTarget(MemoryKey memory) {
                this.sharedTargetMemory = memory.id().toString();
                return this;
            }

            public Builder formation(String formation) {
                this.formation = formation;
                return this;
            }

            public Builder alertEvent(String event) {
                this.alertEvents.add(event);
                return this;
            }

            public Builder raidLike() {
                this.groupBehaviors.add("raid_like");
                return this;
            }

            public Builder herd() {
                this.groupBehaviors.add("herd");
                return this;
            }

            public Builder packHunting() {
                this.groupBehaviors.add("pack_hunting");
                return this;
            }

            public Builder shareMemories(boolean shareMemories, int cap) {
                this.shareMemories = shareMemories;
                this.memoryShareCap = cap;
                return this;
            }

            public GroupDefinition build() {
                return new GroupDefinition(id, maxMembers, leaderMemory, sharedTargetMemory, formation,
                        alertEvents, groupBehaviors, shareMemories, memoryShareCap);
            }
        }
    }

    @NexusStable(since = "1.3")
    public record GroupDebugReport(ResourceLocation groupId, int maxMembers, List<String> warnings) {
        public GroupDebugReport {
            warnings = List.copyOf(warnings);
        }
    }

    @NexusStable(since = "1.3")
    public record BrainInspectorSnapshot(ResourceLocation brainId,
                                         List<MemoryDefinition> memories,
                                         List<SensorDefinition> sensors,
                                         List<BehaviorDefinition> behaviors,
                                         BehaviorSchedule schedule,
                                         List<String> pathingOverlay,
                                         List<String> targetOverlay,
                                         List<String> performanceWarnings) {
        public BrainInspectorSnapshot {
            memories = List.copyOf(memories);
            sensors = List.copyOf(sensors);
            behaviors = List.copyOf(behaviors);
            pathingOverlay = List.copyOf(pathingOverlay);
            targetOverlay = List.copyOf(targetOverlay);
            performanceWarnings = List.copyOf(performanceWarnings);
        }

        public static BrainInspectorSnapshot from(BrainDefinition brain) {
            List<String> warnings = brain.sensors().stream()
                    .filter(sensor -> sensor.scanRateTicks() < 5)
                    .map(sensor -> sensor.id() + " scans every " + sensor.scanRateTicks() + " ticks")
                    .toList();
            return new BrainInspectorSnapshot(brain.id(), brain.memories(), brain.sensors(), brain.behaviors(), brain.schedule(),
                    List.of("path_target", "stuck_detector"), List.of("target_entity", "last_attacker"), warnings);
        }
    }

    @NexusStable(since = "1.3")
    public record AiDebugReport(int goalCount, int brainCount, List<String> warnings, List<String> traces) {
        public AiDebugReport {
            warnings = List.copyOf(warnings);
            traces = List.copyOf(traces);
        }

        public boolean healthy() {
            return warnings.isEmpty();
        }
    }

    private NexusAi() {
    }
}

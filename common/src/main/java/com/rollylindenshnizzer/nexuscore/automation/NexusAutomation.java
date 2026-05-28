package com.rollylindenshnizzer.nexuscore.automation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import com.rollylindenshnizzer.nexuscore.block.NexusBlocks;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.data.NexusData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.HashSet;

@NexusStable(since = "1.3")
public final class NexusAutomation {
    private static final Map<ResourceLocation, AutomationNetwork> NETWORKS = new LinkedHashMap<>();
    private static final Set<String> REGISTERED_PRESET_BLOCKS = new HashSet<>();

    public static AutomationNetwork.Builder network(String namespace, String path) {
        return new AutomationNetwork.Builder(NexusIds.id(namespace, path));
    }

    public static RoutePlanner routePlanner(RouteStrategy strategy) {
        return new RoutePlanner(strategy, true, true, List.of(), List.of());
    }

    public static AutomationNetwork register(AutomationNetwork network) {
        NETWORKS.put(network.id(), network);
        return network;
    }

    public static Collection<AutomationNetwork> networks() {
        return List.copyOf(NETWORKS.values());
    }

    public static AutomationValidationReport validate() {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        for (AutomationNetwork network : NETWORKS.values()) {
            AutomationSimulationReport report = network.simulate();
            if (report.loopDetected()) {
                warnings.add(network.id() + " contains a routing loop");
            }
            if (network.nodes().isEmpty()) {
                errors.add(network.id() + " has no nodes");
            }
        }
        return new AutomationValidationReport(errors, warnings);
    }

    public static String debugSummary() {
        AutomationValidationReport report = validate();
        return "networks=" + NETWORKS.size() + ", errors=" + report.errors().size() + ", warnings=" + report.warnings().size();
    }

    public static int registerPresetBlocks(String modId) {
        int count = 0;
        for (AutomationBlockPreset preset : List.of(AutomationBlockPreset.itemPipe(), AutomationBlockPreset.energyConduit(), AutomationBlockPreset.fluidPipe())) {
            String key = modId + ":" + preset.name();
            if (!REGISTERED_PRESET_BLOCKS.add(key)) {
                continue;
            }
            NexusBlocks.block(modId, preset.name())
                    .factory(properties -> new AutomationPipeBlock(preset, properties))
                    .properties(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5F, 6.0F).sound(SoundType.METAL).noOcclusion())
                    .withBlockItem()
                    .simpleCubeModel()
                    .dropsSelf()
                    .register();
            count++;
        }
        return count;
    }

    public static NexusData.DataPlan writeAllTo(NexusData.DataPlan plan) {
        for (AutomationNetwork network : NETWORKS.values()) {
            if (network.id().getNamespace().equals(plan.modId())) {
                plan.data("nexuscore/automation_network/" + network.id().getPath() + ".json", network.toJson());
            }
        }
        for (AutomationBlockPreset preset : List.of(AutomationBlockPreset.itemPipe(), AutomationBlockPreset.energyConduit(), AutomationBlockPreset.fluidPipe())) {
            plan.data("nexuscore/automation_block/" + preset.name() + ".json", preset.toJson());
            plan.translation("block." + plan.modId() + "." + preset.name(), NexusIds.humanName(preset.name()));
        }
        return plan;
    }

    @NexusStable(since = "1.3")
    public record AutomationNetwork(ResourceLocation id, List<TransferNode> nodes, List<TransferEdge> edges, int tickBudget) {
        public AutomationNetwork {
            nodes = List.copyOf(nodes);
            edges = List.copyOf(edges);
        }

        public Optional<RoutePlan> route(BlockPos from, BlockPos to, TransferKind kind) {
            Set<BlockPos> visited = new HashSet<>();
            Map<BlockPos, BlockPos> parent = new LinkedHashMap<>();
            Queue<BlockPos> queue = new ArrayDeque<>();
            queue.add(from);
            visited.add(from);
            while (!queue.isEmpty()) {
                BlockPos current = queue.remove();
                if (current.equals(to)) {
                    return Optional.of(RoutePlan.reconstruct(from, to, parent, kind));
                }
                edges.stream()
                        .filter(edge -> edge.kind() == kind || edge.kind() == TransferKind.ANY)
                        .filter(edge -> edge.from().equals(current))
                        .sorted(Comparator.comparingInt(TransferEdge::priority).reversed())
                        .forEach(edge -> {
                            if (visited.add(edge.to())) {
                                parent.put(edge.to(), current);
                                queue.add(edge.to());
                            }
                        });
            }
            return Optional.empty();
        }

        public AutomationSimulationReport simulate() {
            boolean loop = false;
            int throughput = 0;
            for (TransferEdge edge : edges) {
                throughput += Math.max(0, edge.throughput());
                for (TransferEdge other : edges) {
                    if (edge.from().equals(other.to()) && edge.to().equals(other.from())) {
                        loop = true;
                    }
                }
            }
            return new AutomationSimulationReport(id, nodes.size(), edges.size(), throughput, loop, List.of());
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("id", id.toString());
            json.addProperty("tick_budget", tickBudget);
            JsonArray nodeArray = new JsonArray();
            for (TransferNode node : nodes) {
                nodeArray.add(node.toJson());
            }
            json.add("nodes", nodeArray);
            JsonArray edgeArray = new JsonArray();
            for (TransferEdge edge : edges) {
                edgeArray.add(edge.toJson());
            }
            json.add("edges", edgeArray);
            return json;
        }

        public static final class Builder {
            private final ResourceLocation id;
            private final List<TransferNode> nodes = new ArrayList<>();
            private final List<TransferEdge> edges = new ArrayList<>();
            private int tickBudget = 1_000;

            private Builder(ResourceLocation id) {
                this.id = id;
            }

            public Builder node(TransferNode node) {
                this.nodes.add(node);
                return this;
            }

            public Builder edge(TransferEdge edge) {
                this.edges.add(edge);
                return this;
            }

            public Builder connect(BlockPos from, BlockPos to, TransferKind kind, int throughput) {
                this.edges.add(new TransferEdge(from, to, kind, throughput, 0, ItemFilter.any()));
                return this;
            }

            public Builder tickBudget(int tickBudget) {
                this.tickBudget = tickBudget;
                return this;
            }

            public AutomationNetwork build() {
                return new AutomationNetwork(id, nodes, edges, tickBudget);
            }
        }
    }

    @NexusStable(since = "1.3")
    public record TransferNode(BlockPos pos, TransferKind kind, String role, int bufferSize, boolean serverAuthoritative) {
        public static TransferNode item(BlockPos pos, String role) {
            return new TransferNode(pos, TransferKind.ITEM, role, 64, true);
        }

        public static TransferNode energy(BlockPos pos, String role, int buffer) {
            return new TransferNode(pos, TransferKind.ENERGY, role, buffer, true);
        }

        public static TransferNode fluid(BlockPos pos, String role, int buffer) {
            return new TransferNode(pos, TransferKind.FLUID, role, buffer, true);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("x", pos.getX());
            json.addProperty("y", pos.getY());
            json.addProperty("z", pos.getZ());
            json.addProperty("kind", kind.name().toLowerCase(java.util.Locale.ROOT));
            json.addProperty("role", role);
            json.addProperty("buffer_size", bufferSize);
            json.addProperty("server_authoritative", serverAuthoritative);
            return json;
        }
    }

    @NexusStable(since = "1.3")
    public record TransferEdge(BlockPos from,
                               BlockPos to,
                               TransferKind kind,
                               int throughput,
                               int priority,
                               ItemFilter filter) {
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.add("from", pos(from));
            json.add("to", pos(to));
            json.addProperty("kind", kind.name().toLowerCase(java.util.Locale.ROOT));
            json.addProperty("throughput", throughput);
            json.addProperty("priority", priority);
            json.add("filter", filter.toJson());
            return json;
        }
    }

    @NexusStable(since = "1.3")
    public enum TransferKind {
        ITEM,
        ENERGY,
        FLUID,
        ANY
    }

    @NexusStable(since = "1.3")
    public record ItemFilter(Set<String> allow, Set<String> deny, boolean matchTags) {
        public ItemFilter {
            allow = Set.copyOf(allow);
            deny = Set.copyOf(deny);
        }

        public static ItemFilter any() {
            return new ItemFilter(Set.of(), Set.of(), true);
        }

        public static ItemFilter allow(String... ids) {
            return new ItemFilter(Set.of(ids), Set.of(), true);
        }

        public boolean accepts(String id) {
            if (deny.contains(id)) {
                return false;
            }
            return allow.isEmpty() || allow.contains(id) || (matchTags && allow.stream().anyMatch(value -> id.startsWith(value + ":")));
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            JsonArray allowJson = new JsonArray();
            allow.forEach(allowJson::add);
            JsonArray denyJson = new JsonArray();
            deny.forEach(denyJson::add);
            json.add("allow", allowJson);
            json.add("deny", denyJson);
            json.addProperty("match_tags", matchTags);
            return json;
        }
    }

    @NexusStable(since = "1.3")
    public record RoutePlan(List<BlockPos> points, TransferKind kind) {
        public RoutePlan {
            points = List.copyOf(points);
        }

        private static RoutePlan reconstruct(BlockPos from, BlockPos to, Map<BlockPos, BlockPos> parent, TransferKind kind) {
            List<BlockPos> points = new ArrayList<>();
            BlockPos current = to;
            points.add(current);
            while (!current.equals(from)) {
                current = parent.get(current);
                if (current == null) {
                    return new RoutePlan(List.of(), kind);
                }
                points.addFirst(current);
            }
            return new RoutePlan(points, kind);
        }
    }

    @NexusStable(since = "1.3")
    public record RoutePlanner(RouteStrategy strategy,
                               boolean backpressure,
                               boolean overflowRouting,
                               List<String> whitelist,
                               List<String> blacklist) {
        public RoutePlanner {
            whitelist = List.copyOf(whitelist);
            blacklist = List.copyOf(blacklist);
        }

        public RoutePlanner allow(String id) {
            List<String> values = new ArrayList<>(whitelist);
            values.add(id);
            return new RoutePlanner(strategy, backpressure, overflowRouting, values, blacklist);
        }

        public RoutePlanner deny(String id) {
            List<String> values = new ArrayList<>(blacklist);
            values.add(id);
            return new RoutePlanner(strategy, backpressure, overflowRouting, whitelist, values);
        }

        public List<TransferEdge> order(List<TransferEdge> edges) {
            Comparator<TransferEdge> comparator = switch (strategy) {
                case PRIORITY_GROUPS -> Comparator.comparingInt(TransferEdge::priority).reversed();
                case NEAREST_FIRST -> Comparator.comparingInt(edge -> edge.from().distManhattan(edge.to()));
                case FULLNESS_BASED, MACHINE_DEMAND -> Comparator.comparingInt(TransferEdge::throughput).reversed();
                case TAG_BASED, COMPONENT_SENSITIVE, ROUND_ROBIN -> Comparator.comparing(edge -> edge.kind().name());
            };
            return edges.stream().sorted(comparator).toList();
        }
    }

    @NexusStable(since = "1.3")
    public enum RouteStrategy {
        PRIORITY_GROUPS,
        ROUND_ROBIN,
        NEAREST_FIRST,
        FULLNESS_BASED,
        MACHINE_DEMAND,
        TAG_BASED,
        COMPONENT_SENSITIVE
    }

    @NexusStable(since = "1.3")
    public record PipeCablePreset(String name,
                                  TransferKind kind,
                                  int throughputTier,
                                  int capacity,
                                  boolean facadeHooks,
                                  boolean coverHooks,
                                  boolean wrenchInteractions,
                                  boolean perSideFilters,
                                  boolean redstoneControlled,
                                  Map<String, String> metadata) {
        public PipeCablePreset {
            metadata = Map.copyOf(metadata);
        }

        public static PipeCablePreset itemPipe(String name, int throughputTier) {
            return new PipeCablePreset(name, TransferKind.ITEM, throughputTier, throughputTier * 64,
                    true, true, true, true, true, Map.of());
        }

        public static PipeCablePreset energyCable(String name, int capacity) {
            return new PipeCablePreset(name, TransferKind.ENERGY, Math.max(1, capacity / 1_000), capacity,
                    true, true, true, false, true, Map.of("loss", "configurable"));
        }

        public static PipeCablePreset fluidPipe(String name, int throughputTier, String restriction) {
            return new PipeCablePreset(name, TransferKind.FLUID, throughputTier, throughputTier * 1_000,
                    true, true, true, true, true, Map.of("fluid_restriction", restriction));
        }
    }

    @NexusStable(since = "1.3")
    public record NetworkDiagnostics(ResourceLocation networkId,
                                     int connectedComponents,
                                     int blockedRoutes,
                                     int filterFailures,
                                     int unloadedRouteWarnings,
                                     int tickCost,
                                     List<String> bottlenecks) {
        public NetworkDiagnostics {
            bottlenecks = List.copyOf(bottlenecks);
        }

        public static NetworkDiagnostics inspect(AutomationNetwork network) {
            AutomationSimulationReport simulation = network.simulate();
            List<String> bottlenecks = network.edges().stream()
                    .filter(edge -> edge.throughput() <= 0)
                    .map(edge -> edge.from() + " -> " + edge.to())
                    .toList();
            return new NetworkDiagnostics(network.id(), network.nodes().isEmpty() ? 0 : 1,
                    bottlenecks.size(), 0, 0, network.edges().size(), bottlenecks);
        }
    }

    @NexusStable(since = "1.3")
    public record AutomationSimulationReport(ResourceLocation networkId,
                                             int nodeCount,
                                             int edgeCount,
                                             int totalThroughput,
                                             boolean loopDetected,
                                             List<String> notes) {
        public AutomationSimulationReport {
            notes = List.copyOf(notes);
        }
    }

    @NexusStable(since = "1.3")
    public record AutomationValidationReport(List<String> errors, List<String> warnings) {
        public AutomationValidationReport {
            errors = List.copyOf(errors);
            warnings = List.copyOf(warnings);
        }

        public boolean passed() {
            return errors.isEmpty();
        }
    }

    @NexusStable(since = "1.3")
    public record AutomationBlockPreset(String name, TransferKind kind, int throughput, int buffer, boolean hasFilter, boolean hasRedstoneMode) {
        public static AutomationBlockPreset itemPipe() {
            return new AutomationBlockPreset("item_pipe", TransferKind.ITEM, 8, 64, true, true);
        }

        public static AutomationBlockPreset energyConduit() {
            return new AutomationBlockPreset("energy_conduit", TransferKind.ENERGY, 1_000, 10_000, false, true);
        }

        public static AutomationBlockPreset fluidPipe() {
            return new AutomationBlockPreset("fluid_pipe", TransferKind.FLUID, 1_000, 4_000, true, true);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("name", name);
            json.addProperty("kind", kind.name().toLowerCase(java.util.Locale.ROOT));
            json.addProperty("throughput", throughput);
            json.addProperty("buffer", buffer);
            json.addProperty("has_filter", hasFilter);
            json.addProperty("has_redstone_mode", hasRedstoneMode);
            return json;
        }
    }

    private static JsonObject pos(BlockPos pos) {
        JsonObject json = new JsonObject();
        json.addProperty("x", pos.getX());
        json.addProperty("y", pos.getY());
        json.addProperty("z", pos.getZ());
        return json;
    }

    private NexusAutomation() {
    }
}

package com.rollylindenshnizzer.nexuscore.simulation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.api.NexusIncubating;
import com.rollylindenshnizzer.nexuscore.balance.NexusBalance;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@NexusIncubating(since = "1.3")
public final class NexusSimulation {
    public static WorldgenSimulation worldgen(String name) {
        return new WorldgenSimulation(name, 100, List.of(), Map.of(), List.of());
    }

    public static EconomySimulation economy(String name) {
        return new EconomySimulation(name, Map.of(), List.of(), List.of());
    }

    public static CombatSimulation combat(String name) {
        return new CombatSimulation(name, Map.of(), List.of());
    }

    public static Dashboard dashboard(String name, Collection<NexusBalance.BalanceReport> reports, Collection<SimulationReport> simulations) {
        List<DashboardSection> sections = new ArrayList<>();
        for (NexusBalance.BalanceReport report : reports) {
            sections.add(new DashboardSection(report.name(), report.metrics(), report.warnings()));
        }
        for (SimulationReport simulation : simulations) {
            sections.add(new DashboardSection(simulation.name(), simulation.metrics(), simulation.warnings()));
        }
        return new Dashboard(name, Instant.now(), sections);
    }

    public static SimulationDiff diff(SimulationReport before, SimulationReport after, Map<String, Double> thresholds) {
        List<String> changes = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        for (Map.Entry<String, Double> entry : after.metrics().entrySet()) {
            double oldValue = before.metrics().getOrDefault(entry.getKey(), 0.0D);
            double delta = entry.getValue() - oldValue;
            if (Double.compare(delta, 0.0D) != 0) {
                changes.add(entry.getKey() + ": " + oldValue + " -> " + entry.getValue());
            }
            double threshold = thresholds.getOrDefault(entry.getKey(), Double.POSITIVE_INFINITY);
            if (Math.abs(delta) > threshold) {
                failures.add(entry.getKey() + " delta " + delta + " exceeds threshold " + threshold);
            }
        }
        return new SimulationDiff(before.name(), after.name(), changes, failures);
    }

    public static String debugSummary() {
        return "simulationTools=worldgen,economy,combat,dashboard,diff";
    }

    @NexusIncubating(since = "1.3")
    public record WorldgenSimulation(String name,
                                     int seeds,
                                     List<String> dimensions,
                                     Map<String, Double> expectedDensityByBiome,
                                     List<String> structures) {
        public WorldgenSimulation {
            dimensions = List.copyOf(dimensions);
            expectedDensityByBiome = Map.copyOf(expectedDensityByBiome);
            structures = List.copyOf(structures);
        }

        public WorldgenSimulation seeds(int seeds) {
            return new WorldgenSimulation(name, seeds, dimensions, expectedDensityByBiome, structures);
        }

        public WorldgenSimulation dimension(String dimension) {
            List<String> values = new ArrayList<>(dimensions);
            values.add(dimension);
            return new WorldgenSimulation(name, seeds, values, expectedDensityByBiome, structures);
        }

        public WorldgenSimulation density(String biome, double density) {
            Map<String, Double> values = new LinkedHashMap<>(expectedDensityByBiome);
            values.put(biome, density);
            return new WorldgenSimulation(name, seeds, dimensions, values, structures);
        }

        public WorldgenSimulation structure(String structure) {
            List<String> values = new ArrayList<>(structures);
            values.add(structure);
            return new WorldgenSimulation(name, seeds, dimensions, expectedDensityByBiome, values);
        }

        public SimulationReport run() {
            Map<String, Double> metrics = new LinkedHashMap<>();
            metrics.put("seeds", (double) seeds);
            metrics.put("average_distance_from_spawn", Math.max(256.0D, 12_000.0D / Math.max(1, seeds)));
            expectedDensityByBiome.forEach((biome, density) -> metrics.put("density." + biome, density));
            metrics.put("structure_count", (double) structures.size());
            List<String> warnings = new ArrayList<>();
            if (seeds < 25) {
                warnings.add("worldgen simulation has a small seed sample");
            }
            if (structures.isEmpty()) {
                warnings.add("no structures included in simulation");
            }
            return new SimulationReport(name, SimulationKind.WORLDGEN, metrics, warnings, List.of("biome_distribution", "height_distribution", "conflicts"));
        }
    }

    @NexusIncubating(since = "1.3")
    public record EconomySimulation(String name,
                                    Map<String, Double> rates,
                                    List<String> recipeChains,
                                    List<String> progressionNodes) {
        public EconomySimulation {
            rates = Map.copyOf(rates);
            recipeChains = List.copyOf(recipeChains);
            progressionNodes = List.copyOf(progressionNodes);
        }

        public EconomySimulation rate(String key, double value) {
            Map<String, Double> values = new LinkedHashMap<>(rates);
            values.put(key, value);
            return new EconomySimulation(name, values, recipeChains, progressionNodes);
        }

        public EconomySimulation recipeChain(String chain) {
            List<String> values = new ArrayList<>(recipeChains);
            values.add(chain);
            return new EconomySimulation(name, rates, values, progressionNodes);
        }

        public EconomySimulation progressionNode(String node) {
            List<String> values = new ArrayList<>(progressionNodes);
            values.add(node);
            return new EconomySimulation(name, rates, recipeChains, values);
        }

        public SimulationReport run() {
            Map<String, Double> metrics = new LinkedHashMap<>(rates);
            metrics.put("recipe_chain_count", (double) recipeChains.size());
            metrics.put("progression_node_count", (double) progressionNodes.size());
            List<String> warnings = rates.entrySet().stream()
                    .filter(entry -> entry.getValue() < 0.0D)
                    .map(entry -> entry.getKey() + " is negative")
                    .toList();
            return new SimulationReport(name, SimulationKind.ECONOMY, metrics, warnings, List.of("recipe_chains", "machine_throughput", "ritual_costs", "progression_order"));
        }
    }

    @NexusIncubating(since = "1.3")
    public record CombatSimulation(String name, Map<String, Double> stats, List<String> comparedEntities) {
        public CombatSimulation {
            stats = Map.copyOf(stats);
            comparedEntities = List.copyOf(comparedEntities);
        }

        public CombatSimulation stat(String key, double value) {
            Map<String, Double> values = new LinkedHashMap<>(stats);
            values.put(key, value);
            return new CombatSimulation(name, values, comparedEntities);
        }

        public CombatSimulation compareEntity(String entity) {
            List<String> values = new ArrayList<>(comparedEntities);
            values.add(entity);
            return new CombatSimulation(name, stats, values);
        }

        public SimulationReport run() {
            Map<String, Double> metrics = new LinkedHashMap<>(stats);
            double health = stats.getOrDefault("health", 20.0D);
            double damage = stats.getOrDefault("damage", 4.0D);
            double armor = stats.getOrDefault("armor", 0.0D);
            metrics.put("estimated_threat", health * 0.5D + damage * 4.0D + armor * 2.0D);
            metrics.put("compared_entities", (double) comparedEntities.size());
            List<String> warnings = new ArrayList<>();
            if (health > 500.0D || damage > 80.0D) {
                warnings.add("extreme entity stats detected");
            }
            return new SimulationReport(name, SimulationKind.COMBAT, metrics, warnings, List.of("entity_tiers", "projectile_damage", "loot_reward_balance"));
        }
    }

    @NexusIncubating(since = "1.3")
    public record SimulationReport(String name,
                                   SimulationKind kind,
                                   Map<String, Double> metrics,
                                   List<String> warnings,
                                   List<String> sections) {
        public SimulationReport {
            metrics = Map.copyOf(metrics);
            warnings = List.copyOf(warnings);
            sections = List.copyOf(sections);
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("name", name);
            json.addProperty("kind", kind.serializedName());
            JsonObject metricJson = new JsonObject();
            metrics.forEach(metricJson::addProperty);
            json.add("metrics", metricJson);
            json.add("warnings", strings(warnings));
            json.add("sections", strings(sections));
            return json;
        }
    }

    @NexusIncubating(since = "1.3")
    public enum SimulationKind {
        WORLDGEN,
        ECONOMY,
        COMBAT,
        PERFORMANCE,
        STRUCTURE,
        AUTOMATION;

        public String serializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    @NexusIncubating(since = "1.3")
    public record SimulationDiff(String before, String after, List<String> changes, List<String> thresholdFailures) {
        public SimulationDiff {
            changes = List.copyOf(changes);
            thresholdFailures = List.copyOf(thresholdFailures);
        }

        public boolean failed() {
            return !thresholdFailures.isEmpty();
        }
    }

    @NexusIncubating(since = "1.3")
    public record Dashboard(String name, Instant createdAt, List<DashboardSection> sections) {
        public Dashboard {
            sections = List.copyOf(sections);
        }

        public String toMarkdown() {
            StringBuilder builder = new StringBuilder("# ").append(name).append("\n\n");
            builder.append("Generated: ").append(createdAt).append("\n\n");
            for (DashboardSection section : sections) {
                builder.append("## ").append(section.title()).append("\n\n");
                section.metrics().forEach((key, value) -> builder.append("- ").append(key).append(": ").append(value).append("\n"));
                for (String warning : section.warnings()) {
                    builder.append("- warning: ").append(warning).append("\n");
                }
                builder.append('\n');
            }
            return builder.toString();
        }

        public String toHtml() {
            StringBuilder builder = new StringBuilder("<!doctype html><html><body><h1>")
                    .append(name)
                    .append("</h1><p>Generated: ")
                    .append(createdAt)
                    .append("</p>");
            for (DashboardSection section : sections) {
                builder.append("<section><h2>").append(section.title()).append("</h2><ul>");
                section.metrics().forEach((key, value) -> builder.append("<li>").append(key).append(": ").append(value).append("</li>"));
                section.warnings().forEach(warning -> builder.append("<li>warning: ").append(warning).append("</li>"));
                builder.append("</ul></section>");
            }
            return builder.append("</body></html>").toString();
        }
    }

    @NexusIncubating(since = "1.3")
    public record DashboardSection(String title, Map<String, Double> metrics, List<String> warnings) {
        public DashboardSection {
            metrics = Map.copyOf(metrics);
            warnings = List.copyOf(warnings);
        }
    }

    private static JsonArray strings(Collection<String> values) {
        JsonArray array = new JsonArray();
        values.forEach(array::add);
        return array;
    }

    private NexusSimulation() {
    }
}

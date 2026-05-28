package com.rollylindenshnizzer.nexuscore.balance;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@NexusStable(since = "1.3")
public final class NexusBalance {
    public static BalanceReport report(String name) {
        return new BalanceReport(name, new LinkedHashMap<>(), List.of());
    }

    public static SimulationScenario scenario(String name) {
        return new SimulationScenario(name, new LinkedHashMap<>(), 200);
    }

    public static BalanceDiff diff(BalanceReport oldReport, BalanceReport newReport) {
        List<String> changes = new ArrayList<>();
        for (Map.Entry<String, Double> entry : newReport.metrics().entrySet()) {
            double oldValue = oldReport.metrics().getOrDefault(entry.getKey(), 0.0D);
            if (Double.compare(oldValue, entry.getValue()) != 0) {
                changes.add(entry.getKey() + ": " + oldValue + " -> " + entry.getValue());
            }
        }
        return new BalanceDiff(oldReport.name(), newReport.name(), changes);
    }

    @NexusStable(since = "1.3")
    public record BalanceReport(String name, Map<String, Double> metrics, List<String> warnings) {
        public BalanceReport {
            metrics = Map.copyOf(metrics);
            warnings = List.copyOf(warnings);
        }

        public BalanceReport metric(String key, double value) {
            Map<String, Double> values = new LinkedHashMap<>(metrics);
            values.put(key, value);
            List<String> nextWarnings = new ArrayList<>(warnings);
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                nextWarnings.add(key + " is not finite");
            }
            return new BalanceReport(name, values, nextWarnings);
        }

        public String summary() {
            return name + " metrics=" + metrics.size() + ", warnings=" + warnings.size();
        }
    }

    @NexusStable(since = "1.3")
    public record SimulationScenario(String name, Map<String, Double> parameters, int ticks) {
        public SimulationScenario {
            parameters = Map.copyOf(parameters);
        }

        public SimulationScenario parameter(String key, double value) {
            Map<String, Double> values = new LinkedHashMap<>(parameters);
            values.put(key, value);
            return new SimulationScenario(name, values, ticks);
        }

        public SimulationScenario ticks(int ticks) {
            return new SimulationScenario(name, parameters, ticks);
        }

        public SimulationReport run() {
            Map<String, Double> metrics = new LinkedHashMap<>();
            parameters.forEach((key, value) -> metrics.put(key + "_per_tick", value / Math.max(1, ticks)));
            return new SimulationReport(name, ticks, metrics, List.of("deterministic helper simulation"));
        }
    }

    @NexusStable(since = "1.3")
    public record SimulationReport(String scenarioName, int ticks, Map<String, Double> metrics, List<String> notes) {
        public SimulationReport {
            metrics = Map.copyOf(metrics);
            notes = List.copyOf(notes);
        }
    }

    @NexusStable(since = "1.3")
    public record BalanceDiff(String oldName, String newName, List<String> changes) {
        public BalanceDiff {
            changes = List.copyOf(changes);
        }

        public boolean changed() {
            return !changes.isEmpty();
        }
    }

    private NexusBalance() {
    }
}

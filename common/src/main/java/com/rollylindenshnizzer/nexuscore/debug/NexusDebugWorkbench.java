package com.rollylindenshnizzer.nexuscore.debug;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@NexusStable(since = "1.3")
public final class NexusDebugWorkbench {
    private static final Map<String, DebugWorkbenchTab> TABS = new LinkedHashMap<>();
    private static final ReportHistory HISTORY = new ReportHistory(50);

    public static DebugWorkbenchTab register(DebugWorkbenchTab tab) {
        TABS.put(tab.id(), tab);
        return tab;
    }

    public static List<DebugWorkbenchTab> tabs() {
        return List.copyOf(TABS.values());
    }

    public static void record(String category, String summary) {
        HISTORY.add(new ReportEntry(category, summary, Instant.now()));
    }

    public static ReportHistory history() {
        return HISTORY.copy();
    }

    public static void installV13Tabs() {
        register(DebugWorkbenchTab.of("multiblocks", "Multiblocks", "structure",
                "matched_structure", "controller", "parts", "invalid_blocks", "capabilities", "machine_state"));
        register(DebugWorkbenchTab.of("rituals", "Rituals", "sparkles",
                "active_rituals", "requirements", "progress", "failure_chance", "scheduled_effects", "protection_checks"));
        register(DebugWorkbenchTab.of("brain_ai", "Brain AI", "brain",
                "memories", "sensors", "active_behavior", "schedule", "path_target", "group_data", "performance_cost"));
        register(DebugWorkbenchTab.of("jigsaw_structures", "Jigsaw", "pieces",
                "graph", "pieces", "connectors", "placement_failures", "generated_instances"));
        register(DebugWorkbenchTab.of("data_entities", "Data Entities", "entity",
                "definitions", "schemas", "validation", "render_presets", "spawn_rules"));
        register(DebugWorkbenchTab.of("progression", "Progression", "tree",
                "player_unlocks", "team_unlocks", "locked_nodes", "missing_conditions", "guide_pages"));
        register(DebugWorkbenchTab.of("recipe_chains", "Recipes", "crafting",
                "dependencies", "missing_recipes", "ratios", "machine_tiers", "energy_costs"));
        register(DebugWorkbenchTab.of("compatibility", "Compatibility", "plug",
                "loaded_bridges", "missing_mods", "disabled_integrations", "failure_logs"));
        register(DebugWorkbenchTab.of("performance", "Performance", "gauge",
                "benchmarks", "memory", "slow_validation", "slow_datagen", "simulation_costs", "recommended_fixes"));
    }

    public static String debugSummary() {
        return "debugTabs=" + TABS.size() + ", reportHistory=" + HISTORY.entries().size();
    }

    @NexusStable(since = "1.3")
    public record DebugWorkbenchTab(String id, String title, String icon, List<String> sections, boolean serverOnly) {
        public DebugWorkbenchTab {
            sections = List.copyOf(sections);
        }

        public static DebugWorkbenchTab of(String id, String title, String icon, String... sections) {
            return new DebugWorkbenchTab(id, title, icon, List.of(sections), false);
        }
    }

    @NexusStable(since = "1.3")
    public static final class ReportHistory {
        private final int limit;
        private final List<ReportEntry> entries;

        public ReportHistory(int limit) {
            this(limit, new ArrayList<>());
        }

        private ReportHistory(int limit, List<ReportEntry> entries) {
            this.limit = limit;
            this.entries = entries;
        }

        public void add(ReportEntry entry) {
            entries.add(entry);
            while (entries.size() > limit) {
                entries.removeFirst();
            }
        }

        public List<ReportEntry> entries() {
            return List.copyOf(entries);
        }

        public Optional<ReportEntry> latest(String category) {
            for (int i = entries.size() - 1; i >= 0; i--) {
                ReportEntry entry = entries.get(i);
                if (entry.category().equals(category)) {
                    return Optional.of(entry);
                }
            }
            return Optional.empty();
        }

        private ReportHistory copy() {
            return new ReportHistory(limit, new ArrayList<>(entries));
        }
    }

    @NexusStable(since = "1.3")
    public record ReportEntry(String category, String summary, Instant createdAt) {
    }

    private NexusDebugWorkbench() {
    }
}

package com.rollylindenshnizzer.nexuscore.runtime;

import com.rollylindenshnizzer.nexuscore.ai.NexusAi;
import com.rollylindenshnizzer.nexuscore.automation.NexusAutomation;
import com.rollylindenshnizzer.nexuscore.authoring.NexusAuthoring;
import com.rollylindenshnizzer.nexuscore.biome.NexusBiomes;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.data.NexusData;
import com.rollylindenshnizzer.nexuscore.debug.DebugRegistry;
import com.rollylindenshnizzer.nexuscore.dimension.NexusDimensions;
import com.rollylindenshnizzer.nexuscore.entity.NexusEntityDefinitions;
import com.rollylindenshnizzer.nexuscore.multiblock.NexusMultiblockPredicates;
import com.rollylindenshnizzer.nexuscore.multiblock.NexusMultiblocks;
import com.rollylindenshnizzer.nexuscore.progression.NexusProgression;
import com.rollylindenshnizzer.nexuscore.recipe.NexusRecipeFamilies;
import com.rollylindenshnizzer.nexuscore.resource.NexusDataDefinitions;
import com.rollylindenshnizzer.nexuscore.ritual.NexusRitualActions;
import com.rollylindenshnizzer.nexuscore.ritual.NexusRituals;
import com.rollylindenshnizzer.nexuscore.ritual.NexusTime;
import com.rollylindenshnizzer.nexuscore.ritual.NexusWeather;
import com.rollylindenshnizzer.nexuscore.simulation.NexusSimulation;
import com.rollylindenshnizzer.nexuscore.structure.NexusStructures;
import com.rollylindenshnizzer.nexuscore.worldgen.NexusWorldgen;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class NexusRuntimeContent {
    private static final Map<String, RuntimeInstallReport> REPORTS = new LinkedHashMap<>();

    static {
        DebugRegistry.section("nexuscore.runtime_content", () -> REPORTS.values().stream()
                .map(RuntimeInstallReport::summary)
                .sorted()
                .toList()
                .toString());
    }

    public static RuntimeInstallReport install(String modId) {
        String namespace = NexusIds.requireNamespace(modId);
        RuntimeInstallReport existing = REPORTS.get(namespace);
        if (existing != null) {
            return existing;
        }
        ensureBuiltInDescriptors(namespace);

        NexusData.DataPlan plan = NexusData.plan(namespace);
        List<String> installed = new ArrayList<>();

        NexusDimensions.writeAllTo(plan);
        installed.add("dimensions=" + NexusDimensions.definitions().stream().filter(definition -> definition.id().getNamespace().equals(namespace)).count());
        installed.add("portals=" + NexusDimensions.registerRuntimePortalBlocks(namespace));

        NexusStructures.writeAllTo(plan);
        installed.add("structures=" + NexusStructures.structures().stream().filter(definition -> definition.id().getNamespace().equals(namespace)).count());
        installed.add("jigsawPools=" + NexusStructures.jigsawPoolDefinitions().stream().filter(pool -> pool.id().getNamespace().equals(namespace)).count());

        NexusBiomes.writeAllTo(plan);
        installed.add("biomes=" + NexusBiomes.biomes().stream().filter(definition -> definition.id().getNamespace().equals(namespace)).count());

        NexusWorldgen.writeAllTo(plan);
        installed.add("worldgenOres=" + NexusWorldgen.ores().stream().filter(ore -> ore.modId().equals(namespace)).count());

        NexusEntityDefinitions.writeAllTo(plan);
        installed.add("entities=" + NexusEntityDefinitions.definitions().stream().filter(definition -> definition.id().getNamespace().equals(namespace)).count());

        installed.add("dataDrivenEntities=" + NexusDataDefinitions.registerRuntimeEntities(namespace));
        installed.add("dataDrivenWorldgen=" + NexusDataDefinitions.writeRuntimeDataTo(plan));

        installed.add("automationBlocks=" + NexusAutomation.registerPresetBlocks(namespace));
        NexusAutomation.writeAllTo(plan);

        installed.add("aiBrains=" + NexusAi.brains().stream().filter(brain -> brain.id().getNamespace().equals(namespace)).count());
        installed.add("recipeFamilies=" + NexusRecipeFamilies.families().stream().filter(family -> family.id().getNamespace().equals(namespace)).count());

        NexusMultiblocks.writeAllTo(plan);
        installed.add("multiblocks=" + NexusMultiblocks.definitions().stream().filter(definition -> definition.id().getNamespace().equals(namespace)).count());

        NexusRituals.writeAllTo(plan);
        installed.add("rituals=" + NexusRituals.definitions().stream().filter(definition -> definition.id().getNamespace().equals(namespace)).count());

        NexusProgression.writeAllTo(plan);
        installed.add("progressionNodes=" + NexusProgression.nodes().stream().filter(node -> node.id().getNamespace().equals(namespace)).count());

        installed.add("authoringEditors=" + NexusAuthoring.editors().size());
        installed.add("simulationTools=" + NexusSimulation.debugSummary());

        RuntimeInstallReport report = new RuntimeInstallReport(namespace, installed, List.of());
        REPORTS.put(namespace, report);
        return report;
    }

    public static Map<String, RuntimeInstallReport> reports() {
        return Map.copyOf(REPORTS);
    }

    public static String debugSummary() {
        return REPORTS.values().stream()
                .map(RuntimeInstallReport::summary)
                .sorted()
                .toList()
                .toString();
    }

    public record RuntimeInstallReport(String modId, List<String> installed, List<String> warnings) {
        public RuntimeInstallReport {
            installed = List.copyOf(installed);
            warnings = List.copyOf(warnings);
        }

        public String summary() {
            return modId + " installed " + installed;
        }
    }

    private NexusRuntimeContent() {
    }

    private static void ensureBuiltInDescriptors(String namespace) {
        if (!"nexuscore".equals(namespace)) {
            return;
        }
        boolean hasRuntimeMarker = NexusDataDefinitions.entityDefinitions().stream()
                .anyMatch(definition -> definition.id().getNamespace().equals(namespace)
                        && definition.id().getPath().equals("runtime_marker"));
        if (!hasRuntimeMarker) {
            NexusDataDefinitions.registerEntity(NexusDataDefinitions.entity(namespace, "runtime_marker")
                    .sized(0.25F, 0.25F)
                    .tracking(32, 5)
                    .property("runtime", "true")
                    .build());
        }
        boolean hasRuntimeWorldgen = NexusDataDefinitions.worldgenDefinitions().stream()
                .anyMatch(definition -> definition.id().getNamespace().equals(namespace)
                        && definition.id().getPath().equals("runtime_patch"));
        if (!hasRuntimeWorldgen) {
            JsonObject feature = new JsonObject();
            feature.addProperty("type", "minecraft:no_op");
            feature.add("config", new JsonObject());
            NexusDataDefinitions.registerWorldgen(NexusDataDefinitions.worldgen(namespace, "runtime_patch")
                    .kind("configured_feature")
                    .biome("#minecraft:is_overworld")
                    .data("worldgen/configured_feature/runtime_patch.json", feature)
                    .build());
        }
        boolean hasBrain = NexusAi.brains().stream()
                .anyMatch(brain -> brain.id().getNamespace().equals(namespace)
                        && brain.id().getPath().equals("runtime_marker_brain"));
        if (!hasBrain) {
            NexusAi.register(NexusAi.brain(namespace, "runtime_marker_brain")
                    .memory("home", "minecraft:block_pos", 1_200)
                    .sensor("nearby_players", 20, "minecraft:player")
                    .behavior("idle", 1, "idle", "always")
                    .build());
        }
        boolean hasJigsaw = NexusStructures.jigsawPools().stream()
                .anyMatch(pool -> pool.id().getNamespace().equals(namespace)
                        && pool.id().getPath().equals("runtime/start_pool"));
        if (!hasJigsaw) {
            NexusStructures.registerJigsawPool(NexusStructures.jigsawPoolDefinition(namespace, "runtime/start_pool")
                    .fallback("minecraft:empty")
                    .depthLimit(1)
                    .element("nexuscore:structures/bootstrap", 1, "start")
                    .build());
        }
        boolean hasMultiblock = NexusMultiblocks.definitions().stream()
                .anyMatch(definition -> definition.id().getNamespace().equals(namespace)
                        && definition.id().getPath().equals("runtime/basic_multiblock"));
        if (!hasMultiblock) {
            NexusMultiblocks.register(NexusMultiblocks.create(namespace, "runtime/basic_multiblock")
                    .controller("nexuscore:runtime_controller")
                    .aisle("AAA", "ABA", "AAA")
                    .aisle("ACA", "ADA", "AAA")
                    .where('A', "minecraft:stone")
                    .where('B', NexusMultiblockPredicates.itemPort())
                    .where('C', NexusMultiblockPredicates.energyPort())
                    .where('D', "nexuscore:runtime_controller")
                    .role('D', NexusMultiblocks.PartRole.CONTROLLER)
                    .rotatable()
                    .mirrorable()
                    .build());
        }
        boolean hasRitual = NexusRituals.definitions().stream()
                .anyMatch(definition -> definition.id().getNamespace().equals(namespace)
                        && definition.id().getPath().equals("runtime/basic_ritual"));
        if (!hasRitual) {
            NexusRituals.register(NexusRituals.create(namespace, "runtime/basic_ritual")
                    .center("nexuscore:runtime_controller")
                    .requiresStructure(NexusIds.id(namespace, "runtime/basic_multiblock"))
                    .requiresWeather(NexusWeather.ANY)
                    .requiresTime(NexusTime.ANY)
                    .durationSeconds(5)
                    .onComplete(NexusRitualActions.sendMessage("nexuscore.ritual.runtime.complete"))
                    .build());
        }
        boolean hasProgression = NexusProgression.nodes().stream()
                .anyMatch(node -> node.id().getNamespace().equals(namespace)
                        && node.id().getPath().equals("runtime/basic_progression"));
        if (!hasProgression) {
            NexusProgression.register(NexusProgression.node(namespace, "runtime/basic_progression")
                    .requiresMultiblock(NexusIds.id(namespace, "runtime/basic_multiblock"))
                    .requiresRitual(NexusIds.id(namespace, "runtime/basic_ritual"))
                    .unlocksGuidePage(NexusIds.id(namespace, "runtime/basic_progression"))
                    .build());
        }
    }
}

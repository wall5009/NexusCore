package com.rollylindenshnizzer.nexuscore.test;

import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerAdvancedControl;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerControlSupport;
import com.rollylindenshnizzer.nexuscore.compat.recipeviewer.RecipeViewerDisplay;
import com.rollylindenshnizzer.nexuscore.config.ConfigSchemaExporter;
import com.rollylindenshnizzer.nexuscore.config.IntOption;
import com.rollylindenshnizzer.nexuscore.config.NexusConfig;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.data.NexusData;
import com.rollylindenshnizzer.nexuscore.data.NexusDataValidator;
import com.rollylindenshnizzer.nexuscore.energy.EnergyStorage;
import com.rollylindenshnizzer.nexuscore.network.NexusNetworking;
import com.rollylindenshnizzer.nexuscore.performance.NexusBenchmarks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Set;

public final class NexusCoreGameTestScenarios {
    public static void energyStorageRateLimits() {
        EnergyStorage energy = new EnergyStorage(1_000, 250, 100);
        assertEquals(250, energy.insert(500, false), "Expected max insert to limit transfer");
        assertEquals(100, energy.extract(500, false), "Expected max extract to limit transfer");
        assertEquals(150, energy.amount(), "Expected remaining energy to be tracked");
    }

    public static void configSchemaExportsSyncedOptions() {
        TestConfig config = new TestConfig();
        JsonObject schema = ConfigSchemaExporter.jsonSchema(config, "gametest");
        String encoded = schema.toString();
        assertTrue(encoded.contains("machine_energy"), "Schema should include the machine energy option");
        assertTrue(encoded.contains("\"serverSynced\":true"), "Schema should mark synced options");
        assertTrue(encoded.contains("\"group\":\"machine\""), "Schema should include option groups");
    }

    public static void datagenValidationCatchesErrorsAndAcceptsValidPlans() {
        NexusData.DataPlan plan = NexusData.plan("nexuscore_gametest")
                .translation("item.nexuscore_gametest.ruby", "Ruby")
                .itemGenerated("ruby")
                .tag("items", "gems/ruby", "nexuscore_gametest:ruby");
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shapeless");
        recipe.addProperty("category", "misc");
        JsonObject result = new JsonObject();
        result.addProperty("id", "nexuscore_gametest:ruby");
        result.addProperty("count", 1);
        recipe.add("result", result);
        JsonObject criteria = new JsonObject();
        criteria.add("has_ruby", new JsonObject());
        recipe.add("criteria", criteria);
        plan.data("recipe/ruby_copy.json", recipe);
        assertFalse(NexusDataValidator.validatePlan(plan).hasErrors(), "Valid generated data plan should not have errors");

        JsonObject badTag = new JsonObject();
        NexusData.DataPlan invalid = NexusData.plan("nexuscore_bad_gametest").data("tags/items/empty.json", badTag);
        assertTrue(NexusDataValidator.validatePlan(invalid).hasErrors(), "Invalid tag data should produce an error");
    }

    public static void packetDiagnosticsCompareVersions() {
        NexusNetworking.channel("nexuscore", "gametest")
                .protocolVersion("1.1")
                .disconnectOnMismatch((client, server) -> "client=" + client + ", server=" + server);
        var diagnostics = NexusNetworking.diagnostics().get(NexusIds.id("nexuscore", "gametest"));
        assertTrue(diagnostics != null, "Channel diagnostics should be registered");
        var report = diagnostics.compare("1.0", Set.of());
        assertFalse(report.compatible(), "Mismatched protocol versions should be incompatible");
        assertTrue(report.message().contains("client=1.0"), "Mismatch report should use the configured message");
    }

    public static void recipeViewerControlsHavePortableFallbacks() {
        RecipeViewerDisplay display = RecipeViewerDisplay.builder(
                        NexusIds.id("nexuscore", "gametest_recipe"),
                        NexusIds.id("nexuscore", "gametest_category"), 120, 60)
                .page(page -> page
                        .itemInput(4, 20, new ItemStack(Items.STONE))
                        .arrowProgress(40, 20, 200)
                        .itemOutput(78, 20, new ItemStack(Items.DIAMOND))
                        .control(RecipeViewerAdvancedControl.button("all", 4, 44, 36, 16, "Info"))
                        .control(RecipeViewerAdvancedControl.recipeTransferButton(80, 44)))
                .build();
        for (RecipeViewerAdvancedControl control : display.layout().controlsFor(display.layout().pages().getFirst())) {
            assertTrue(RecipeViewerControlSupport.supports("jei", control)
                            || !RecipeViewerControlSupport.fallbackTooltip("jei", control).isBlank(),
                    "JEI controls should either be native or have a deterministic fallback");
            assertTrue(RecipeViewerControlSupport.supports("emi", control)
                            || !RecipeViewerControlSupport.fallbackTooltip("emi", control).isBlank(),
                    "EMI controls should either be native or have a deterministic fallback");
            assertTrue(RecipeViewerControlSupport.supports("rei", control)
                            || !RecipeViewerControlSupport.fallbackTooltip("rei", control).isBlank(),
                    "REI controls should either be native or have a deterministic fallback");
        }
    }

    public static void benchmarkSuiteRecordsProfilerSamples() {
        var results = NexusBenchmarks.smokeSuite().run();
        assertTrue(results.size() >= 2, "Smoke benchmark suite should run multiple benchmarks");
        assertTrue(results.stream().allMatch(result -> result.iterations() > 0), "Benchmarks should report iterations");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    private static void assertEquals(long expected, long actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
        }
    }

    private static final class TestConfig extends NexusConfig {
        private final IntOption machineEnergy;

        private TestConfig() {
            super("nexuscore_gametest_config");
            machineEnergy = intOption("machine_energy", 100).range(1, 1_000);
            machineEnergy.group("machine").serverSynced().comment("Energy per operation");
        }
    }

    private NexusCoreGameTestScenarios() {
    }
}

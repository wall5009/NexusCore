package com.rollylindenshnizzer.nexuscore.fabric.test;

import com.rollylindenshnizzer.nexuscore.test.NexusCoreGameTestScenarios;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public final class NexusCoreFabricGameTests implements FabricGameTest {
    @GameTest(template = "nexuscore:bootstrap", timeoutTicks = 100)
    public void energyStorageRateLimits(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::energyStorageRateLimits);
    }

    @GameTest(template = "nexuscore:config_schema", timeoutTicks = 100)
    public void configSchemaExportsSyncedOptions(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::configSchemaExportsSyncedOptions);
    }

    @GameTest(template = "nexuscore:datagen_validation", timeoutTicks = 100)
    public void datagenValidationCatchesErrorsAndAcceptsValidPlans(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::datagenValidationCatchesErrorsAndAcceptsValidPlans);
    }

    @GameTest(template = "nexuscore:packet_diagnostics", timeoutTicks = 100)
    public void packetDiagnosticsCompareVersions(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::packetDiagnosticsCompareVersions);
    }

    @GameTest(template = "nexuscore:recipe_viewer_controls", timeoutTicks = 100)
    public void recipeViewerControlsHavePortableFallbacks(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::recipeViewerControlsHavePortableFallbacks);
    }

    @GameTest(template = "nexuscore:performance_benchmarks", timeoutTicks = 100)
    public void benchmarkSuiteRecordsProfilerSamples(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::benchmarkSuiteRecordsProfilerSamples);
    }

    @GameTest(template = "nexuscore:bootstrap", timeoutTicks = 100)
    public void machineFrameworkProcessesHybridRecipes(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::machineFrameworkProcessesHybridRecipes);
    }

    @GameTest(template = "nexuscore:bootstrap", timeoutTicks = 100)
    public void inventoryTransferRulesTraceRoutes(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::inventoryTransferRulesTraceRoutes);
    }

    @GameTest(template = "nexuscore:bootstrap", timeoutTicks = 100)
    public void energyAndFluidTransfersRespectSides(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::energyAndFluidTransfersRespectSides);
    }

    @GameTest(template = "nexuscore:bootstrap", timeoutTicks = 100)
    public void worldgenAndEntityDefinitionsGenerateDescriptors(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::worldgenAndEntityDefinitionsGenerateDescriptors);
    }

    @GameTest(template = "nexuscore:bootstrap", timeoutTicks = 100)
    public void typedDataLoaderValidatesDatapackJson(GameTestHelper helper) {
        run(helper, NexusCoreGameTestScenarios::typedDataLoaderValidatesDatapackJson);
    }

    private static void run(GameTestHelper helper, Runnable scenario) {
        scenario.run();
        helper.succeed();
    }
}
